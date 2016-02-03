package kovalenko.elance.aligrabber;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Category extends Downloadable {

    private int count = 0;

    public Category( RequestConfig config, HttpClient httpClient, String url, Properties properties, int attempts ) {
        this.setRequestConfig( config );
        this.setHttpClient( httpClient );
        this.setUrl( url );
        this.setProperties( properties );
        this.setAttempts( attempts );
    }

    public int getCount() {
        return this.count;
    }

    @Override
    public List< Downloadable > parse( HttpEntity entity ) throws Exception {
        Document doc = Jsoup.parse( entity.getContent(), "UTF-8", "http://www.alibaba.com/" ); // @todo Possibly the base URL shall be configurable
        try {
            this.count = Integer.parseInt( doc.select( "div.ui-breadcrumb.ml" ).first().children().last().previousElementSibling().text().replaceAll( "[.,\\s]", "" ) );
        } catch ( NumberFormatException nfe ) {
            this.count = 0;
        }
        List< Downloadable > result = new ArrayList<>();
        Elements es = doc.select( "div.item-main" );
        for ( Element e : es ) {
            Element h2 = e.select( "h2.title" ).first();
            Element h2_a = h2.select( "a" ).first();
            String title = h2_a.attr( "title" );
            String href = h2_a.attr( "href" );
            String productId = h2_a.attr( "data-pid" );
            result.add( new Article( this.getRequestConfig(), this.getHttpClient(), href, this.getProperties(), 0 ) );
        }

        Element next = doc.select( "div.ui2-pagination-pages a.next" ).first();
        if ( next != null ) {
            result.add( new Category( this.getRequestConfig(), this.getHttpClient(), next.attr( "href" ), this.getProperties(), 0 ) );
        } else {
            Element notFound = doc.select( "div.m-notfound-rfq" ).first();
            if ( notFound == null ) {
                try {
                    long currentPage = Long.parseLong( this.getUrl().substring( this.getUrl().lastIndexOf( "/" ) + 1, this.getUrl().lastIndexOf( "." ) ) );
                    String nextUrl = this.getUrl().replaceFirst( "/\\d+\\.html$", "/" + ( currentPage + 1 ) + ".html" );
                    result.add( new Category( this.getRequestConfig(), this.getHttpClient(), nextUrl, this.getProperties(), 0 ) );
                } catch ( Exception e ) {
                }
            }
        }

        return result;
    }

}
