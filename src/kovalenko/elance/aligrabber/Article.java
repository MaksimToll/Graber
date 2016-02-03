package kovalenko.elance.aligrabber;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import javax.imageio.ImageIO;
import kovalenko.elance.aligrabber.meta.MetaArticle;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class Article extends Downloadable {

    transient private List< BufferedImage > images = new LinkedList<>();

    private int expectedImages = 0;

    private MetaArticle meta;

    private boolean ignoreImages = true;

    private int ignoreImagesWidth = 0;

    private int ignoreImagesHeight = 0;

    public Article( RequestConfig config, HttpClient httpClient, String url, Properties properties, int attempts ) {
        this.setRequestConfig( config );
        this.setHttpClient( httpClient );
        this.setUrl( url );
        this.meta = new MetaArticle( this.getUrl() );
        this.setProperties( properties );
        this.ignoreImages = this.getProperties().getProperty( "ignoreImages", "Y" ).equals( "Y" );
        this.ignoreImagesWidth = Integer.parseInt( this.getProperties().getProperty( "ignoreImagesWidth", "0" ) );
        this.ignoreImagesHeight = Integer.parseInt( this.getProperties().getProperty( "ignoreImagesHeight", "0" ) );
        this.setAttempts( attempts );
    }

    public MetaArticle getMeta() {
        return this.meta;
    }

    private boolean isAllowed( String url ) {
        String m = "";
        try {
            m = url.substring( url.lastIndexOf( "/" ) + 1 );
        } catch ( Exception e ) {
            return true;
        }
        if ( m.equalsIgnoreCase( "placeholder_100x100.png" ) ) {
            return false;
        }
        return true;
    }

    @Override
    public List< Downloadable > parse( HttpEntity entity ) throws Exception {
        Document doc = Jsoup.parse( entity.getContent(), "UTF-8", "http://www.alibaba.com/" ); // @todo Base URL shall be configurable
        List< Downloadable > result = new ArrayList<>();
        Element f = doc.select( "div.imain img.photo.pic.J-pic" ).first();
        if ( f != null && this.isAllowed( f.attr( "src" ).replaceAll( "_\\d+x\\d+\\..+$", "" ) ) ) {
            result.add( new Image( this.getRequestConfig(), this.getHttpClient(), f.attr( "src" ).replaceAll( "_\\d+x\\d+\\..+$", "" ), this, 0 ) );
            this.expectedImages ++;
        }
        for ( Element e : doc.select( "ul.inav div.thumb img" ) ) {
            if ( this.isAllowed( e.attr( "src" ).replaceAll( "_\\d+x\\d+\\..+$", "" ) ) ) {
                result.add( new Image( this.getRequestConfig(), this.getHttpClient(), e.attr( "src" ).replaceAll( "_\\d+x\\d+\\..+$", "" ), this, 0 ) );
                this.expectedImages ++;
            }
        }
        if ( this.getProperties().getProperty( "extractImagesFromDetails", "N" ).equals( "Y" ) ) {
            for ( Element e : doc.select( "div#J-rich-text-description img" ) ) {
                if ( this.isAllowed( e.attr( "src" ) ) ) {
                    result.add( new Image( this.getRequestConfig(), this.getHttpClient(), e.attr( "src" ), this, 0 ) );
                    this.expectedImages ++;
                }
            }
        }
        return result;
    }

    public List< BufferedImage > getImages() {
        return this.images;
    }

    public void addImage( BufferedImage image ) {
        if ( image != null && ( ( ! this.ignoreImages ) || ( this.ignoreImages && image.getWidth() > this.ignoreImagesWidth && image.getHeight() > this.ignoreImagesHeight ) ) ) {
            this.images.add( image );
        } else {
            this.expectedImages --;
        }
        if ( this.images.size() == this.expectedImages && this.expectedImages > 0 ) {
            int maxWidth = 0;
            int maxHeight = 0;
            Iterator< BufferedImage > it = this.images.iterator();
            while ( it.hasNext() ) {
                BufferedImage i = it.next();
                maxWidth = Math.max( i.getWidth(), maxWidth );
                maxHeight = Math.max( i.getHeight(), maxHeight );
            }

            int spaceSize = Integer.parseInt( this.getProperties().getProperty( "spaceSize", "0" ) );
            spaceSize = ( this.images.size() == 1 ) ? 0 : spaceSize;
            int imagesInRow = Integer.parseInt( this.getProperties().getProperty( "imagesInRow", "0" ) );
            int backgroundColor = Integer.parseInt( this.getProperties().getProperty( "backgroundColor", "0" ) );

            int finalWidth = spaceSize;
            if ( this.images.size() <= imagesInRow ) {
                finalWidth += ( maxWidth + spaceSize ) * this.images.size();
            } else {
                finalWidth += ( maxWidth + spaceSize ) * imagesInRow;
            }

            int finalHeight = spaceSize;
            int rows = ( int ) Math.ceil( ( double ) this.images.size() / ( double ) imagesInRow );
            finalHeight += ( maxHeight + spaceSize ) * rows;

            BufferedImage finalImage = new BufferedImage( finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB );
            Graphics2D g2d = finalImage.createGraphics();
            g2d.setColor( new Color( backgroundColor ) );
            g2d.fillRect( 0, 0, finalWidth, finalHeight );
            int i = 0;
            int x = spaceSize - 1;
            int y = spaceSize - 1;
            it = this.images.iterator();
            while ( it.hasNext() ) {
                BufferedImage tile = it.next();
                finalImage.getGraphics().drawImage( tile, x, y, null );
                x += maxWidth + spaceSize;
                i ++;
                if ( i >= imagesInRow ) {
                    i = 0;
                    x = spaceSize - 1;
                    y += maxHeight + spaceSize;
                }
            }

            try {
                ImageIO.write( finalImage, "jpg", new File( this.getProperties().getProperty( "targetDirectory" ), this.getUrl().replaceAll( "^.+\\/", "" ) + ".jpg" ) );
            } catch ( Exception e ) {
                e.printStackTrace();
            }

            finalImage.flush();

            it = this.images.iterator();
            while ( it.hasNext() ) {
                it.next().flush();
            }
            this.images.clear();

            // Save the downloaded article metadata.
            try {
                Connection db = ( Connection ) this.getProperties().get( "db" );
                if ( db != null && ! db.isClosed() ) {
                    PreparedStatement stmt = db.prepareStatement( "INSERT INTO articles VALUES( ? )" );
                    stmt.setString( 1, this.getMeta().getUniqueName() );
                    stmt.execute();
                }
            } catch ( SQLException e ) {
                e.printStackTrace();
            }

        }
    }

}
