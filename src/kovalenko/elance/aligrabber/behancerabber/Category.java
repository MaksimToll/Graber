package kovalenko.elance.aligrabber.behancerabber;

/**
 * Created by fagim on 01.02.16.
 */
public class Category
{
    public Category(String key, String category) {
        this.key = key;
        this.category = category;
    }

    private String key;
    private String category;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
