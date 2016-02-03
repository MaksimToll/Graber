package kovalenko.elance.aligrabber.meta;

public class MetaArticle {

    private String uniqueName = "";

    public MetaArticle() {
    }

    public MetaArticle( String url ) {
        try {
            this.uniqueName = url.substring( url.lastIndexOf( "/" ) + 1, url.lastIndexOf( "_" ) ).toLowerCase();
        } catch ( IndexOutOfBoundsException e ) {
            this.uniqueName = url.toLowerCase();
        }
    }

    public String getUniqueName() {
        return this.uniqueName;
    }

}
