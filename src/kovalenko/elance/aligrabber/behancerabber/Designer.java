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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Designer designer = (Designer) o;

        if (name != null ? !name.equals(designer.name) : designer.name != null) return false;
        if (imageUrl != null ? !imageUrl.equals(designer.imageUrl) : designer.imageUrl != null) return false;
        return mainUrl != null ? mainUrl.equals(designer.mainUrl) : designer.mainUrl == null;

    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (imageUrl != null ? imageUrl.hashCode() : 0);
        result = 31 * result + (mainUrl != null ? mainUrl.hashCode() : 0);
        return result;
    }
}
