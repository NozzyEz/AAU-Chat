package org.nozzy.android.lapitchat;

import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.text.Layout;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder>{

    private List<Messages> mMessageList;
    private FirebaseAuth mAuth;

    public MessageAdapter(List<Messages> mMessageList) {
        this.mMessageList = mMessageList;
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
        public CircleImageView profileImage;
        public View messageView;

        public MessageViewHolder(View itemView) {
            super(itemView);

            messageView = itemView.findViewById(R.id.message_single_layout);
            messageText = itemView.findViewById(R.id.message_item_text);
            profileImage = itemView.findViewById(R.id.message_profile_image);
            displayName = itemView.findViewById(R.id.message_display_name);


        }
    }

    @Override
    public void onBindViewHolder(MessageAdapter.MessageViewHolder holder, int position) {

        mAuth = FirebaseAuth.getInstance();
        String current_user_id = mAuth.getCurrentUser().getUid();


        Messages c = mMessageList.get(position);

        String from_user = c.getFrom();

        if (from_user.equals(current_user_id)) {
            ((RelativeLayout) holder.messageView).setGravity(Gravity.END);
            holder.messageText.setBackgroundResource(R.drawable.message_text_background_current_user);
            holder.messageText.setTextColor(Color.BLACK);
            holder.profileImage.setVisibility(View.INVISIBLE);
        } else {
            holder.messageText.setBackgroundResource(R.drawable.message_text_background);
            holder.messageText.setTextColor(Color.WHITE);

        }
        holder.messageText.setText(c.getMessage());
        holder.displayName.setText(c.getFrom());
    }

    @Override
    public int getItemCount() {
        return mMessageList.size();
    }
}
