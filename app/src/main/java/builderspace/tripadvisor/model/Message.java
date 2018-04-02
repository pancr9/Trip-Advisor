package builderspace.tripadvisor.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Rekhansh on 4/21/2017.
 */

public class Message implements Serializable {

    private String name, text, imgUrl;
    private Date time;
    private ArrayList<String> deletedByUsers = new ArrayList<>();

    public Message() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public ArrayList<String> getDeletedByUsers() {
        return deletedByUsers;
    }

    public void setDeletedByUsers(ArrayList<String> deletedByUsers) {
        this.deletedByUsers = deletedByUsers;
    }
}
