package builderspace.tripadvisor.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by Rekhansh on 4/21/2017.
 */

public class Trip implements Serializable {

    private String key, title, imgUrl, owner;
    private Date time;
    private ArrayList<Message> messages = new ArrayList<>();
    private HashMap<String, Date> usersJoinTime = new HashMap<>();
    private ArrayList<Location> locations = new ArrayList<>();

    public Trip() {
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public ArrayList<Message> getMessages() {
        return messages;
    }

    public void setMessages(ArrayList<Message> messages) {
        this.messages = messages;
    }

    public HashMap<String, Date> getUsersJoinTime() {
        return usersJoinTime;
    }

    public void setUsersJoinTime(HashMap<String, Date> usersJoinTime) {
        this.usersJoinTime = usersJoinTime;
    }

    public ArrayList<Location> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<Location> locations) {
        this.locations = locations;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }
}
