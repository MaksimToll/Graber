package kovalenko.elance.aligrabber.behancerabber;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fagim on 29.01.16.
 */
public class Designer {
    private String name;
    private String country;
    private String imageUrl;

    public String getMainUrl() {
        return mainUrl;
    }

    public void setMainUrl(String mainUrl) {
        this.mainUrl = mainUrl;
    }

    private String mainUrl; // url for save state if process will interrupt


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

}
