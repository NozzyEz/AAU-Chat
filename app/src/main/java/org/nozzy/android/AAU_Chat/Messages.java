package org.nozzy.android.AAU_Chat;

// Class to set and get the values for the messages.
// Used for displaying all messages in a RecyclerView in ChatActivity.
public class Messages {

    private String message, type, from;
    private long time;
    private boolean seen;

    public Messages(String message, boolean seen, long time, String type, String from) {

        this.message = message;
        this.seen = seen;
        this.time = time;
        this.type = type;
        this.from = from;

    }

    public Messages() {

    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSeen() {
        return seen;
    }

    public void setSeen(boolean seen) {
        this.seen = seen;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }
}
