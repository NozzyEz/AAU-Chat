package org.nozzy.android.AAU_Chat;

// Class to set and get the values for conversations.
// Used for displaying all conversations in a RecyclerView in ChatsFragment.
public class Conv {

    public String type;
    public Long timestamp;

    public Conv(){

    }

    public Conv(String type, Long timestamp) {
        this.type = type;
        this.timestamp = timestamp;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}