package org.nozzy.android.AAU_Chat;

import android.app.Application;
import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.text.format.DateUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

// A custom adapter meant for adapting a list of messages into a RecyclerView.
public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;
    private DatabaseReference mUsersDatabase;
    private Context context;

    public MessageAdapter(List<Messages> mMessageList, Context context) {
        this.mMessageList = mMessageList;
        this.context = context;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.message_single_layout, parent, false);

        return new MessageViewHolder(v);
    }

    public class MessageViewHolder extends RecyclerView.ViewHolder {

        public TextView displayName;
        public TextView messageText;
        public TextView messageTime;
        public CircleImageView profileImage;
        public ImageView messageImage;
        public View messageView;



        public MessageViewHolder(View itemView) {
            super(itemView);

            messageView = itemView.findViewById(R.id.message_single_layout);
            messageText = itemView.findViewById(R.id.message_item_text);
            messageImage = itemView.findViewById(R.id.message_image_layout);
            messageTime = itemView.findViewById(R.id.message_time_text);
            profileImage = itemView.findViewById(R.id.message_profile_image);
            displayName = itemView.findViewById(R.id.message_display_name);



        }
    }

    @Override
    public void onBindViewHolder(final MessageAdapter.MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();


        final Messages c = mMessageList.get(position);

        String from_user = c.getFrom();
        String message_type = c.getType();

        mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(from_user);

        mUsersDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                String name = dataSnapshot.child("name").getValue().toString();
                holder.displayName.setText(name);

                final String image = dataSnapshot.child("thumb_image").getValue().toString();
                Picasso.with(holder.profileImage.getContext()).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                        .placeholder(R.drawable.generic).into(holder.profileImage, new Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {
                        Picasso.with(holder.profileImage.getContext()).load(image).placeholder(R.drawable.generic).into(holder.profileImage);
                    }
                });

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        // Changes the layout based on who sent the message and the type of message
        if (from_user.equals(current_user_id)) {
            // If the message has been sent by the current user:
            // Hide the profile image
            holder.profileImage.setVisibility(View.INVISIBLE);
            // Move the message to the right
            ((LinearLayout) holder.messageView).setGravity(Gravity.END);
            // Sets the background of the message to white
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_current_user);
            // Sets the text to black
            holder.messageText.setTextColor(Color.BLACK);

            // If it's a text message
            if(message_type.equals("text")) {
                holder.messageText.setText(c.getMessage());
                holder.messageImage.setVisibility(View.INVISIBLE);
            } else if (message_type.equals("image")) {
                holder.messageText.setVisibility(View.INVISIBLE);
                Picasso.with(holder.profileImage.getContext()).load(c.getMessage()).placeholder(R.drawable.generic).into(holder.messageImage);
            }

        } else {
            holder.profileImage.setVisibility(View.VISIBLE);
            ((LinearLayout) holder.messageView).setGravity(Gravity.START);
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);

            if(message_type.equals("text")) {

                holder.messageText.setText(c.getMessage());
                holder.messageImage.setVisibility(View.INVISIBLE);

            } else if (message_type.equals("image")) {

                holder.messageText.setVisibility(View.INVISIBLE);

                Picasso.with(holder.profileImage.getContext()).load(c.getMessage()).placeholder(R.drawable.generic).into(holder.messageImage);

            }

        }
        holder.displayName.setText(c.getFrom());

        // To show the time the message has been sent we first have to retrieve the value from firebase,
        // we do that through our messages class, like any other entry
        Long time = c.getTime();

        // This long we can then convert to the apropiate string to show the clock
        String convertedTime = DateUtils.formatDateTime(context, time, DateUtils.FORMAT_SHOW_TIME);

        // And finally we can assign that string to the viewholder's text field
        holder.messageTime.setText(convertedTime);
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
