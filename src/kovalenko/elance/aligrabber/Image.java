package kovalenko.elance.aligrabber;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.net.URL;
import java.util.List;
import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;

public class Image extends Downloadable {

    private Article article = null;

    public Image( RequestConfig config, HttpClient httpClient, String url, Article article, int attempts ) {
        this.setRequestConfig( config );
        this.setHttpClient( httpClient );
        this.setUrl( url );
        this.setArticle( article );
        this.setAttempts( attempts );
    }

    public Article getArticle() {
        return this.article;
    }

    public void setArticle( Article article ) {
        this.article = article;
    }

    @Override
    public List< Downloadable > parse( HttpEntity entity ) throws Exception {
        BufferedImage image = null;
        try {
            image = ImageIO.read( entity.getContent() );
        } catch ( IIOException e ) {
            System.out.println( "UNSUPPORTED IMAGE TYPE: " + this.getUrl() );
        }
        this.article.addImage( image );
        return new ArrayList<>();
    }

}
