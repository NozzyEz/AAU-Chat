package org.nozzy.android.AAU_Chat;

// Class to set and get the values for friends. Used for displaying all friends in a RecyclerView.
public class Friends {

    public String date;

    public Friends(){

    }

    public Friends(String date) { this.date = date; }

    public String getDate() { return date; }

    public void setDate(String date) { this.date = date; }

}
