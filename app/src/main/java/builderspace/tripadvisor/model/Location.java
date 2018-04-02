package builderspace.tripadvisor.model;

import java.io.Serializable;

/**
 * Created by Rekhansh on 5/1/2017.
 */

public class Location implements Serializable {

    private String name;
    private Double latitude;
    private Double longitude;

    public Location() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }
}
