package kovalenko.elance.aligrabber;

import java.io.IOException;
import java.util.List;
import java.util.Properties;
import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;

abstract public class Downloadable extends Thread {

    transient private List< Downloadable > downloadables = null;

    private String url = "";

    transient private RequestConfig config;

    transient private HttpClient httpClient;

    transient final private String baseUrl = "http://www.alibaba.com/";

    transient private boolean requestFailing = false;

    transient private Properties properties = new Properties();

    transient private int attempts = 0;

    final int getAttempts() {
        return this.attempts;
    }

    final void setAttempts( int attempts ) {
        this.attempts = attempts;
    }

    final boolean isRequestFailing() {
        return this.requestFailing;
    }

    final public List< Downloadable > getDownloadables() {
        return this.downloadables;
    }

    final public void setDownloadables( List< Downloadable > downloadables ) {
        this.downloadables = downloadables;
    }

    final public RequestConfig getRequestConfig() {
        return this.config;
    }

    final public void setRequestConfig( RequestConfig config ) {
        this.config = config;
    }

    final public String getUrl() {
        return this.url;
    }

    final public void setUrl( String url ) {
        // Some URLs are relative.
        if ( ! url.startsWith( "http://" ) ) {
            this.url = this.baseUrl + url;
        } else {
            this.url = url;
        }
    }

    final public HttpClient getHttpClient() {
        return this.httpClient;
    }

    final public void setHttpClient( HttpClient httpClient ) {
        this.httpClient = httpClient;
    }

    final public Properties getProperties() {
        return this.properties;
    }

    final public void setProperties( Properties properties ) {
        this.properties = properties;
    }

    final protected void failRequest() {
        this.requestFailing = true;
    }

    @Override
    final public void run() {
        this.attempts ++;
        HttpGet get = new HttpGet( this.url );
        get.setConfig( config );
        CloseableHttpResponse response = null;
        try {
            response = ( CloseableHttpResponse ) this.httpClient.execute( get );
        } catch ( Exception e ) {
        }
        if ( response != null ) {
            try {
                HttpEntity entity = response.getEntity();
                this.setDownloadables( this.parse( entity ) );
            } catch ( OutOfMemoryError oom ) {
                System.out.println( "OUT OF MEMORY EXCEPTION: " + oom.getMessage() );
                System.gc();
                this.requestFailing = true;
            } catch ( Exception e ) {
                System.out.println( "GENERIC EXCEPTION: " + e.getMessage() );
                e.printStackTrace();
                this.requestFailing = true;
            }
            try {
                response.close();
            } catch ( IOException ioe ) {
            }
        } else {
            this.requestFailing = true;
        }
    }

    abstract List< Downloadable > parse( HttpEntity entity ) throws Exception;

}
