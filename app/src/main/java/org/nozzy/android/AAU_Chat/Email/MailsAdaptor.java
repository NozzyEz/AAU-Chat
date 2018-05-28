package org.nozzy.android.AAU_Chat.Email;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.nozzy.android.AAU_Chat.R;

import java.util.List;

public class MailsAdaptor extends RecyclerView.Adapter<MailsAdaptor.ViewHolder> {

    private List<String> mData;
    private List<String> mDate;
    private List<String> mSender;
    private List<String> mRead;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;
    private boolean IsRead = false;

    // data is passed into the constructor
    MailsAdaptor(Context context, List<String> data, List<String> sender, List<String> date, List<String> read) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
        this.mDate = date;
        this.mSender = sender;
        this.mRead = read;

    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.mail_list_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String subject = mData.get(position);
        holder.subjectTextView.setText(subject);
        String sender = mSender.get(position);
        holder.senderTextView.setText(sender);
        String isRead = mRead.get(position);
        String date = mDate.get(position);
        holder.dateTextView.setText(date);



        if(isRead.equals("1"))
        holder.imageView.setImageResource(R.drawable.read);
        else
            holder.imageView.setImageResource(R.drawable.unread);



    }


    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView subjectTextView;
        TextView dateTextView;
        TextView senderTextView;
        ImageView imageView;

        ViewHolder(View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextViewID);
            senderTextView = itemView.findViewById(R.id.senderTextViewID);
            dateTextView = itemView.findViewById(R.id.dateTextViewID);
            imageView = itemView.findViewById(R.id.imageViewID);




            itemView.setOnClickListener(this);
                    }


        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}