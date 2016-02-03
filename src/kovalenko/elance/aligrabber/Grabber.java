package kovalenko.elance.aligrabber;

import java.io.File;
import java.io.FileWriter;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import javax.swing.ImageIcon;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;

public class Grabber extends Thread {

    final public static String TEMPLATE = ""
        + "Start time:         {startTime}" + System.getProperty( "line.separator" )
        + "Active threads:     {activeThreads}" + System.getProperty( "line.separator" )
        + "Delayed URLs:       {delayedUrls}" + System.getProperty( "line.separator" )
        + "Pages reviewed:     {pagesReviewed}" + System.getProperty( "line.separator" )
        + "Total articles:     {totalArticles}" + System.getProperty( "line.separator" )
        + "Downloaded so far:  {totalDownloaded}" + System.getProperty( "line.separator" )
        + "Failed to download: {totalFailed}" + System.getProperty( "line.separator" )
        + System.getProperty( "line.separator" )
        + "Activity:" + System.getProperty( "line.separator" )
        + "{activity}" + System.getProperty( "line.separator" )
        + "{lastMessage}"  + System.getProperty( "line.separator" )
        + "";

    private PoolingHttpClientConnectionManager manager;

    private CookieStore cookieStore;

    private RequestConfig config;

    private HttpClient httpclient;

    private boolean running = true;

    private Main main;

    private String url;

    private Date startTime = Calendar.getInstance().getTime();

    private List< Downloadable > pool = new ArrayList<>();

    private List< Downloadable > future = new ArrayList< >();

    private int totalArticles = 0;

    private int totalDownloaded = 0;

    private int totalFailed = 0;

    private SimpleDateFormat dateFormat = new SimpleDateFormat( "yyyy-MMM-dd HH:mm:ss" );

    private Connection db;

    private String lastMessage = "";

    private int downloadedCategories = 1;

    public Grabber( Main main, String url ) {
        super();
        this.main = main;
        this.url = url;

        // Disable UI controls.
        this.main.start.setText( "Stop" );
        this.main.start.setIcon( new ImageIcon( this.getClass().getResource( "/kovalenko/elance/aligrabber/resources/cross-button.png" ) ) );
//        this.main.url.setEditable( false );

        try {
            this.db = DriverManager.getConnection( "jdbc:hsqldb:file:" + new File( this.main.targetDirectory.getText(), ".aligrabber.meta" ).getAbsolutePath() + System.getProperty( "file.separator" ) + "db" );
        } catch ( SQLException e ) {
            e.printStackTrace();
            // @todo Issue a warning.
        }

        this.lastMessage = "Preparing to start the download...";
        this.updateProgress();

        try {
            PreparedStatement stmt = this.db.prepareStatement( "SELECT 1 FROM articles" );
        } catch ( SQLException e ) {
            try {
                PreparedStatement stmt = this.db.prepareStatement( "CREATE TABLE articles ( URL LONGVARCHAR NOT NULL )" );
                stmt.execute();
            } catch ( SQLException ex ) {
                ex.printStackTrace();
                // @todo Issue a warning.
            }
        }

        this.lastMessage = "";
        this.updateProgress();

        int genericTimeout = Integer.parseInt( this.main.attemptTimeout.getValue().toString() );

        this.manager = new PoolingHttpClientConnectionManager();
        this.cookieStore = new BasicCookieStore();
        this.config = RequestConfig.custom()
            .setCookieSpec( CookieSpecs.STANDARD )
            .setExpectContinueEnabled( true )
            .setSocketTimeout( genericTimeout * 1000 )
            .setConnectTimeout( genericTimeout * 1000 )
            .setCircularRedirectsAllowed( false )
            .setConnectionRequestTimeout( genericTimeout * 1000 )
            .build();
        // @todo ^^^ Add proxy if set
        this.httpclient = HttpClients.custom()
            .setUserAgent( "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.1; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727)" )
            .setConnectionManager( this.manager )
            .setDefaultCookieStore( this.cookieStore )
            .setDefaultRequestConfig( this.config )
            .build();
    }

    private void updateProgress() {
        String activity = "";
        synchronized ( this.pool ) {
            if ( this.pool.size() > 0 ) {
                for ( Downloadable d : this.pool ) {
                    activity += d.getUrl() + System.getProperty( "line.separator" );
                }
            } else {
                activity = "None.";
            }
        }
        double percent = this.totalDownloaded * 100.0 / Math.max( ( double ) this.totalArticles, 1.0 );
        this.main.progress.setText( Grabber.TEMPLATE
            .replace( "{startTime}", dateFormat.format( this.startTime ) )
            .replace( "{totalArticles}", "" + this.totalArticles )
            .replace( "{totalDownloaded}", "" + this.totalDownloaded + " (" + BigDecimal.valueOf( percent ).setScale( 2, BigDecimal.ROUND_HALF_UP ) + "%)" )
            .replace( "{activeThreads}", "" + this.pool.size() )
            .replace( "{delayedUrls}", "" + this.future.size() )
            .replace( "{totalFailed}", "" + this.totalFailed )
            .replace( "{pagesReviewed}", "" + this.downloadedCategories )
            .replace( "{activity}", activity )
            .replace( "{lastMessage}", this.lastMessage.isEmpty() ? "" : "Message:" + System.getProperty( "line.separator" ) + this.lastMessage )
        );
    }

    @Override
    public void run() {
        this.main.state.setText( Main.GRABBER_DOWNLOADING );

        Properties properties = new Properties();
        properties.setProperty( "spaceSize", this.main.spaceBetweenImages.getValue().toString() );
        properties.setProperty( "imagesInRow", this.main.imagesInRow.getValue().toString() );
        properties.setProperty( "targetDirectory", this.main.targetDirectory.getText() );
        properties.setProperty( "backgroundColor", "" + this.main.backgroundColor.getBackground().getRGB() );
        properties.setProperty( "extractImagesFromDetails", this.main.extractImagesFromDetails.isSelected() ? "Y" : "N" );
        properties.setProperty( "ignoreImages", this.main.ignoreImages.isSelected() ? "Y" : "N" );
        properties.setProperty( "ignoreImagesWidth", this.main.ignoreImagesWidth.getValue().toString() );
        properties.setProperty( "ignoreImagesHeight", this.main.ignoreImagesWidth.getValue().toString() );

        properties.put( "db", this.db );

        int downloadAttempts = Integer.parseInt( this.main.projectLimit.getValue().toString() );

        try {
            FileWriter w = new FileWriter( new File( this.main.targetDirectory.getText(), "source.txt" ), false );
            w.write( this.url + System.getProperty( "line.separator" ) );
            w.close();
        } catch ( Exception e ) {
            e.printStackTrace();
            // @todo Possibly issue a warning popup.
        }

        boolean usePagesLimit = this.main.usePagesLimit.isSelected();
        int pagesLimit = Integer.parseInt( this.main.pagesLimit.getValue().toString() );
        boolean useSpaceLimit = this.main.useSpaceLimit.isSelected();
        long spaceLimit = Long.parseLong( this.main.spaceLimit.getValue().toString() ) * 1000000;
        long currentSpace;

        Category first = new Category( this.config, this.httpclient, this.url, properties, 0 );
        this.pool.add( first );
        first.start();

        while ( this.running ) {
            this.updateProgress();
            if ( usePagesLimit && this.downloadedCategories > pagesLimit ) {
                this.lastMessage = "Download stopped due to reaching limit of pages.";
                this.running = false;
            }
            if ( useSpaceLimit ) {
                currentSpace = new File( this.main.targetDirectory.getText() ).getUsableSpace();
                if ( currentSpace < spaceLimit ) {
                    this.lastMessage = "Download stopped due to reaching limit of free space.";
                    this.running = false;
                }
            }
            synchronized ( this.pool ) {
                Iterator< Downloadable > ip = this.pool.iterator();
                while ( ip.hasNext() ) {
                    Downloadable d = ip.next();
                    if ( d.getState().equals( Thread.State.TERMINATED ) ) {
                        ip.remove();
                        if ( d.getDownloadables() != null ) {
                            for ( Downloadable ds : d.getDownloadables() ) {
                                if ( ds instanceof Article ) {
                                    try {
                                        PreparedStatement stmt = this.db.prepareStatement( "SELECT * FROM articles WHERE url = ?" );
                                        stmt.setString( 1, ( ( Article ) ds ).getMeta().getUniqueName() );
                                        if ( ! stmt.executeQuery().next() ) {
                                            this.future.add( ds );
                                        } else {
                                            // System.out.println( "SKIPPING: " + ds.getUrl() );
                                        }
                                    } catch ( SQLException e ) {
                                    }
                                } else {
                                    this.future.add( ds );
                                }
                            }
                            if ( d instanceof Category ) {
                                this.totalArticles = this.totalArticles == 0 ? ( ( Category ) d ).getCount() : this.totalArticles;
                                this.downloadedCategories ++;
                            } else if ( d instanceof Article ) {
                                this.totalDownloaded ++;
                            }
                        } else if ( d.isRequestFailing() ) {
                            System.out.println( "FAILING: " + d.getUrl() + ", attempts=" + d.getAttempts() );
                            if ( d.getAttempts() < downloadAttempts ) {
                                if ( d instanceof Category ) {
                                    this.future.add( new Category( d.getRequestConfig(), d.getHttpClient(), d.getUrl(), d.getProperties(), d.getAttempts() ) );
                                } else if ( d instanceof Article ) {
                                    this.future.add( new Article( d.getRequestConfig(), d.getHttpClient(), d.getUrl(), d.getProperties(), d.getAttempts() ) );
                                } else if ( d instanceof Image ) {
                                    this.future.add( new Image( d.getRequestConfig(), d.getHttpClient(), d.getUrl(), ( ( Image ) d ).getArticle(), d.getAttempts() ) );
                                }
                            } else {
                                this.totalFailed ++;
                            }

                        }
                    }
                }

                Iterator< Downloadable > itf = this.future.iterator();
//                while ( itf.hasNext() && this.pool.size() < Integer.parseInt( this.main.countProjectForParsing.getValue().toString() ) ) {
//                    Downloadable f = itf.next();
//                    if ( f.getState().equals( Thread.State.NEW ) ) {
//                        this.pool.add( f );
//                        f.start();
//                    }
//                    itf.remove();
//                }

                if ( this.pool.size() == 0 && this.future.size() == 0 ) {
                    this.running = false;
                    this.lastMessage = "Download completed normally.";
                }
            }

            System.gc();

            synchronized ( this ) {
                try {
                    this.wait( 100 );
                } catch ( Exception e ) {
                    this.lastMessage = "Download interrupted by user's request.";
                    this.running = false;
                }
            }
        }

        synchronized ( this.pool ) {
            for ( Downloadable t : this.pool ) {
                t.interrupt();
            }
        }
        this.pool.clear();
        this.future.clear();

        this.updateProgress();

        this.main.state.setText( Main.GRABBER_WORKING );

        try {
            if ( this.db != null && ! this.db.isClosed() ) {
                this.db.commit();
                this.db.close();
            }
        } catch ( SQLException e ) {
            e.printStackTrace();
            // @todo Issue a warning.
        }

        this.main.state.setText( Main.GRABBER_IDLING );

        // Enable UI controls.
        this.main.start.setText( "Start" );
        this.main.start.setIcon( new ImageIcon( this.getClass().getResource( "/kovalenko/elance/aligrabber/resources/tick-button.png" ) ) );
//        this.main.url.setEditable( false );
        this.main.changeTargetDirectory.setEnabled( true );
        this.main.pruneTargetDirectory.setEnabled( true );

        // @todo Remove after debugging
        System.out.println( "DONE" );
    }

}
