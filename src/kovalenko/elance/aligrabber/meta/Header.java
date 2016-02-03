package kovalenko.elance.aligrabber.meta;

public class Header {

    private String categoryUrl = "";

    public Header() {
    }

    public Header( String categoryUrl ) {
        this.setCategoryUrl( categoryUrl );
    }

    public String getCategoryUrl() {
        return this.categoryUrl;
    }

    public void setCategoryUrl( String categoryUrl ) {
        this.categoryUrl = categoryUrl.trim();
    }

}
