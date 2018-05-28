//class used for representing email objects

package org.nozzy.android.AAU_Chat.Email;

public class Mail {

    private String ID;
    private String IsRead;
    private String From;
    private String Subject;
    private String Message;
    private String TimeStamp;
    private String Folder;

    public Mail() {}

    public Mail(String ID) {
        this.ID = ID;
    }

    public Mail(String ID, String IsRead, String From, String Subject,
                String Message, String TimeStamp, String Folder) {

        this.ID = ID;
        this.IsRead = IsRead;
        this.From = From;
        this.Subject = Subject;
        this.Message = Message;
        this.TimeStamp = TimeStamp;

    }

    public String getID() {
        return ID;
    }

    public String getIsRead() {
        return IsRead;
    }

    public String getFrom() {
        return From;
    }

    public String getSubject() {
        return Subject;
    }

    public String getMessage(){
        return Message;
    }

    public String getTimeStamp() {
        return TimeStamp;
    }

    public String getFolder() {
        return Folder;
    }

    public void setID(String ID) {
        this.ID = ID;
    }

    public void setIsRead(String IsRead) {
        this.IsRead = IsRead;
    }

    public void setFrom(String From) {
        this.From = From;
    }

    public void setSubject(String Subject) {
        this.Subject = Subject;
    }

    public void setMessage(String Message){
        this.Message = Message;
    }

    public void setTimeStamp(String TimeStamp) {
        this.TimeStamp = TimeStamp;
    }

    public void setFolder(String Folder) {
        this.Folder = Folder;
    }

}
