package org.nozzy.android.AAU_Chat;

// Class to set and get the values for conversations.
// Used for displaying all conversations in a RecyclerView in ChatsFragment.
public class Conv {

    public Long timestamp;

    public Conv(){

    }

    public Conv(Long timestamp) {
        this.timestamp = timestamp;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}