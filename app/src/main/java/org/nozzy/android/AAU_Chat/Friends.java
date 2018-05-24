package org.nozzy.android.AAU_Chat;

// Class to set and get the values for friends.
// Used for displaying all friends in a RecyclerView in EmailFragment.
public class Friends {

    public String name;

    public Friends(){

    }

    public Friends(String name) { this.name = name; }

    public String getName() { return name; }

    public void setName(String name) { this.name = name; }

}
