package org.nozzy.android.lapitchat;

/**
 * Created by Nozzy on 29/03/2018.
 */

// Class to set and get the values for the users
public class Users {

    public String name;
    public String image;
    public String status;
    public String thumb_image;


    public Users() {

    }

    public Users(String name, String image, String status, String thumbImage) {
        this.name = name;
        this.image = image;
        this.status = status;
        this.thumb_image = thumbImage;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getThumbImage() {
        return thumb_image;
    }

    public void setThumbImage(String thumb_image) {
        this.thumb_image = thumb_image;
    }
}
