package org.nozzy.android.AAU_Chat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// A custom adapter meant for adapting a list of messages into a RecyclerView.
public class PinsAdapter extends RecyclerView.Adapter<PinsAdapter.PinsViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private PinnedMessagesActivity context;
    private String mChatID;
    private String mChatRole;
    private DatabaseReference mChatRef;
    private String current_user_id;

    public PinsAdapter(List<Messages> mMessageList, Context context, String chatID) {
        this.mMessageList = mMessageList;
        this.context = (PinnedMessagesActivity) context;
        this.mChatID = chatID;
        this.mChatRef = FirebaseDatabase.getInstance().getReference().child("Chats").child(mChatID);

        // Gets the current user
        mAuth = FirebaseAuth.getInstance();
        this.current_user_id = mAuth.getCurrentUser().getUid();

        mChatRef.child("members").child(current_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                mChatRole = dataSnapshot.getValue(String.class);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

    }

    @Override
    // Setting up the view for holding a single message
    public PinsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);
        return new PinsViewHolder(v);
    }

    public class PinsViewHolder extends RecyclerView.ViewHolder {

        // UI of a single message
        public TextView displayName;
        public TextView messageText;
        public TextView messageTime;
        public CircleImageView profileImage;
        public ImageView messageImage;
        public View messageView;

        public PinsViewHolder(View itemView) {
            super(itemView);
            // Setting up the UI
            messageView = itemView.findViewById(R.id.message_single_layout);
            messageText = itemView.findViewById(R.id.message_item_text);
            messageImage = itemView.findViewById(R.id.message_image_layout);
            messageTime = itemView.findViewById(R.id.message_time_text);
            profileImage = itemView.findViewById(R.id.message_profile_image);
            displayName = itemView.findViewById(R.id.message_display_name);
            // This is for making the image view rounded
            messageImage.setClipToOutline(true);
        }
    }

    @Override
    public void onBindViewHolder(final PinsAdapter.PinsViewHolder holder, final int position) {

        // Gets the current message from the list
        final Messages c = mMessageList.get(position);

        // Gets the sender and type of the message
        final String from_user = c.getFrom();
        String message_type = c.getType();

        // Adds a listener to the user who sent the message
        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);
        mUsersDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Sets the sender's user name in the message
                String name = dataSnapshot.child("name").getValue().toString();
                holder.displayName.setText(name);

                // Sets the sender's profile image in the message
                if (!from_user.equals(current_user_id)) {
                    final String image = dataSnapshot.child("thumb_image").getValue().toString();
                    Picasso.with(holder.profileImage.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.generic).into(holder.profileImage, new Callback() {
                        @Override
                        public void onSuccess() { }
                        @Override
                        public void onError() {
                            // If the profile image can't be set, set it to the default one
                            Picasso.with(holder.profileImage.getContext()).load(image).placeholder(R.drawable.generic).into(holder.profileImage);
                        }
                    });
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) { }
        });

        // Changes the layout based on who sent the message and the type of message
        if (from_user.equals(current_user_id)) {
            // If the message has been sent by the current user:
            // Hide the profile image
            holder.profileImage.setVisibility(View.GONE);
            // Move the message to the right
            ((LinearLayout) holder.messageView).setGravity(Gravity.END);
            // Sets the background of the message to white
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_current_user);
            // Sets the text to black
            holder.messageText.setTextColor(Color.BLACK);

            if(message_type.equals("text")) {
                // If it's a text message, set the text and hide the image view
                holder.messageText.setText(c.getMessage());
                holder.messageText.setVisibility(View.VISIBLE);
                holder.messageImage.setVisibility(View.GONE);
            } else if (message_type.equals("image")) {
                // If it's an image, hide the text and load in the image
                holder.messageText.setVisibility(View.GONE);
                holder.messageImage.setVisibility(View.VISIBLE);
                Picasso.with(holder.profileImage.getContext()).load(c.getMessage()).placeholder(R.drawable.generic).into(holder.messageImage);
            }

        } else {
            // Else, if the message is sent from someone else:
            // Display the profile image
            holder.profileImage.setVisibility(View.VISIBLE);
            // Move the message to the left
            ((LinearLayout) holder.messageView).setGravity(Gravity.START);
            // Sets the background of the message to the primary colour
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            // Sets the text colour to white
            holder.messageText.setTextColor(Color.WHITE);

            if(message_type.equals("text")) {
                // If it's a text message, set the text and hide the image view
                holder.messageText.setText(c.getMessage());
                holder.messageText.setVisibility(View.VISIBLE);
                holder.messageImage.setVisibility(View.GONE);
            } else if (message_type.equals("image")) {
                // If it's an image, hide the text and load in the image
                holder.messageText.setVisibility(View.GONE);
                holder.messageImage.setVisibility(View.VISIBLE);
                Picasso.with(holder.profileImage.getContext()).load(c.getMessage()).placeholder(R.drawable.generic).into(holder.messageImage);
            }

        }

        // To show the time the message has been sent we first have to retrieve the value from firebase,
        // we do that through our messages class, like any other entry
        Long time = c.getTime();

        // This long we can then convert to the appropriate string to show the clock
        String convertedTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);

        // TODO: Check if time is yesterday or more, then show different information to the user.
        // And finally we can assign that string to the viewholder's text field
        holder.messageTime.setText(convertedTime);

        // A listener on each message for pinning or deleting
        holder.messageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // If the user is an admin or it's their message, they can unpin it
                if (mChatRole.equals("admin"))
                    showAlertDialog(c, "Unpin Message");
            }
        });
    }

    // A method for showing the message options
    private void showAlertDialog(final Messages c, String... options) {
        // Start building the alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Select Options");
        // The dialog depends on the options put in
        if (options.length == 1) {
            builder.setItems(options, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    // Click event for each item: 0 for unpinning the message
                    switch (i) {
                        case 0:
                            // Unpins the message
                            mChatRef.child("pinned").child(c.getKey()).removeValue();
                            Toast.makeText(context, "Message unpinned", Toast.LENGTH_SHORT).show();
                            context.refreshMessages();
                            break;
                    }
                }
            });
        }
        builder.show();
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
