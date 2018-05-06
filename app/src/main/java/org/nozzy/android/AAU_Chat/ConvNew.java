package org.nozzy.android.AAU_Chat;

// Class to set and get the values for conversations.
// Used for displaying all conversations in a RecyclerView in ChatsFragment.
public class ConvNew {

    public String type;

    public ConvNew(){

    }

    public ConvNew(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}