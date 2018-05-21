package org.nozzy.android.AAU_Chat;


import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;

import static android.app.Activity.RESULT_OK;


/**
 * A simple {@link Fragment} subclass.
 */

public class SettingsFragment extends BaseFragment {

    private static final String TAG = SettingsFragment.class.getSimpleName();

    // Initialization for database, Firebase user as well as the UI
    private DatabaseReference mUserDatabase;
    private FirebaseUser mCurrentUser;

    private CircleImageView mImage;
    private TextView mDisplayName;
    private TextView mStatus;

    private Button mStatus_btn;
    private Button mImage_btn;

    private static final int GALLERY_PICK = 1;

    // This here is a storage reference to point to the correct place in our firebase storage for the user to store their profile image
    private StorageReference mImageStorage;

    private ProgressDialog mProgressDialog;

    public static String getFragmentTitle() {
        return "Profile";
    }

    @Override
    protected int getCurrentFragmentLayout() {
        return R.layout.fragment_settings;
    }
    public static String getFragmentTag() {
        return TAG;
    }



    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // First we need to setup the UI
        mImage = getView().findViewById(R.id.settings_image);
        mDisplayName = getView().findViewById(R.id.settings_display_name);
        mStatus = getView().findViewById(R.id.settings_status);
        mStatus_btn = getView().findViewById(R.id.settings_status_btn);
        mImage_btn = getView().findViewById(R.id.setting_image_btn);

        // Here we point to the correct reference for our storage
        mImageStorage = FirebaseStorage.getInstance().getReference();

        // Here we get the User ID of the current user and use that to point our database reference to the correct place in the database
        mCurrentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_UID = mCurrentUser.getUid();
        mUserDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_UID);

        // Holds previously loaded data in the app
        mUserDatabase.keepSynced(true);

        // In the database we listen for the values stored, with this we can update the fields that make up the UI
        mUserDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                // Gets all the relevant user's data from the database
                String name = dataSnapshot.child("name").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String thumbnail = dataSnapshot.child("thumb_image").getValue().toString();

                // Updates the name and status fields based on those in the database
                mDisplayName.setText(name);
                mStatus.setText(status);

                // If the user's database entry for image is not 'default' we load their image into the UI, but with our generic image in place as a placeholder
                if(!image.equals("default")) {
                    // We use the Picasso library to do the image loading, this way we can store the image offline for faster loading
                    Picasso.with(getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.generic).into(mImage, new Callback() {
                        @Override
                        public void onSuccess() { }
                        @Override
                        public void onError() {
                            // If the image fails to load, set the image to the default one
                            Picasso.with(getContext()).load(image).placeholder(R.drawable.generic).into(mImage);
                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Standard button that sends the user to the status activity where they can change their
        // status message, we pass along the users current status message with the intent, so that
        // the new activity can show the message without having to fetch it again
        mStatus_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent statusIntent = new Intent(getContext(), StatusActivity.class);
                String statusValue = mStatus.getText().toString();
                statusIntent.putExtra("statusValue", statusValue);
                statusIntent.putExtra("finisher", new ResultReceiver(null) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        getActivity().onBackPressed();

                    }
                });
                startActivityForResult(statusIntent,1);
//                startActivity(statusIntent);

            }
        });


        // With this button we use an Intent to open up our gallery chooser, for this purpose we use
        // another library called Android Image Cropper, which allows us to pick the image and crop
        // it, which we can then upload to our storage for the specific user
        mImage_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Creates an intent for the default gallery picker
                Intent galleryIntent = new Intent();
                galleryIntent.setType("image/*");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                // Starts the activity with the request code GALLERY_PICK, which is caught in the onActivityResult method
                startActivityForResult(Intent.createChooser(galleryIntent, "SELECT IMAGE"), GALLERY_PICK);

            }
        });

    }

    @Override
    // We use this method when an image is being picked, in here the user picks an image from their
    // external storage, we crop it, and we store it with firebase. We also compress it so that we
    // can use it for a thumbnail image, which we in turn also store
    public void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Checks if the activity was the default gallery picker
        if(requestCode == GALLERY_PICK && resultCode == RESULT_OK) {

            // Gets the picked image
            Uri imageUri = data.getData();

            // Starts the image cropper activity
            CropImage.activity(imageUri)
                    .setAspectRatio(1,1)
                    .setMinCropWindowSize(500, 500)
                    .start(getActivity(), this);

        }

        // Checks if the activity was the image cropper
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {

            // Gets the result from the image cropper activity
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if (resultCode == RESULT_OK) {

                // Shows a progress dialog (loading screen) until the images are uploaded
                mProgressDialog = new ProgressDialog(getContext());
                mProgressDialog.setTitle("Uploading Image");
                mProgressDialog.setMessage("Please wait while the image is being uploaded");
                mProgressDialog.setCanceledOnTouchOutside(false);
                mProgressDialog.show();

                // Gets the cropped image
                Uri resultUri = result.getUri();
                // Creates a thumbnail image file (with the path of the cropped one) for compression
                final File thumb_filePath = new File(resultUri.getPath());

                // Gets the user's ID - this will be the name of the profile picture when storing
                String current_user_id = mCurrentUser.getUid();
                // A byte array for storing the image
                byte[] thumb_byte = new byte[0];
                try {
                    // Creates a compressor, loads the image onto it
                    Bitmap thumb_bitmap = new Compressor(getActivity())
                            .setMaxWidth(200)
                            .setMaxHeight(200)
                            .setQuality(75)
                            .compressToBitmap(thumb_filePath);
                    // A BAOS stores the resulting image data after compression,
                    // which we then transfer to a byte array
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    thumb_bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                    thumb_byte = baos.toByteArray();
                } catch (IOException e) { e.printStackTrace(); }

                // Creates references with filepaths for the image and the thumbnail to be stored in the database
                StorageReference filepath = mImageStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumb_filepath = mImageStorage.child("profile_images").child("thumbnails").child(current_user_id + ".jpg");

                // Another byte array for the thumb image (God knows why)
                final byte[] finalThumb_byte = thumb_byte;

                // Attempts to put the image into the database
                filepath.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                        if (task.isSuccessful()) {

                            // Gets the download link to the image that we just uploaded
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            // Creates a task to upload the compressed image in the form of a byte array to the database
                            UploadTask uploadTask = thumb_filepath.putBytes(finalThumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task) {
                                    if(thumb_task.isSuccessful()) {
                                        // Gets the download link to the thumbnail that we just uploaded
                                        String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                        // Creates a hashmap for storing two new images in the database
                                        Map update_hashMap = new HashMap();
                                        update_hashMap.put("image", download_url);
                                        update_hashMap.put("thumb_image", thumb_downloadUrl);


                                        // Updates the image and thumb_image values of the user in the database
                                        mUserDatabase.updateChildren(update_hashMap).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()) {
                                                    mProgressDialog.dismiss();
                                                    Toast.makeText(getContext(), "Successful", Toast.LENGTH_LONG).show();
                                                }
                                            }
                                        });
                                    } else {
                                        // Shows a toast if the thumbnail upload was unsuccessful
                                        Toast.makeText(getContext(), "Error in uploading thumbnail", Toast.LENGTH_LONG).show();
                                        mProgressDialog.dismiss();

                                    }

                                }
                            });
                        } else {
                            // Shows a toast if the image upload was unsuccessful
                            Toast.makeText(getContext(), "Error in uploading image", Toast.LENGTH_LONG).show();
                            mProgressDialog.dismiss();
                        }
                    }
                });

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }
}
