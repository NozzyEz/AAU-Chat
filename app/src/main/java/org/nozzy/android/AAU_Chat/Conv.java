package org.nozzy.android.AAU_Chat;

// Class to set and get the values for conversations.
// Used for displaying all conversations in a RecyclerView in ChatsFragment.
public class Conv {

    public boolean seen;
    public long timestamp;

    public Conv(){

    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public Conv(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }
}