package kovalenko.elance.aligrabber;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.FontUIResource;
import java.awt.*;
import java.util.ArrayList;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

public class Main extends javax.swing.JFrame {
    final static Logger logger = Logger.getLogger(Main.class);

    final public static String PREFS_FIRST_RUN = "FIRST_RUN";
    final public static String PREFS_FRAME_X = "FRAME_X";
    final public static String PREFS_FRAME_Y = "FRAME_Y";
    final public static String PREFS_FRAME_WIDTH = "FRAME_WIDTH";
    final public static String PREFS_FRAME_HEIGHT = "FRAME_HEIGHT";
    final public static String PREFS_TARGET_DIRECTORY = "TARGET_DIRECTORY";

    final public static String PREFS_LIMIT_IMAGES = "LIMIT_IMAGES";
    final public static String PREFS_USE_PROXY_SERVER = "USE_PROXY_SERVER";
    final public static String PREFS_PROXY_SERVER = "PROXY_SERVER";
    final public static String PREFS_MAX_ACTIVE_THREADS = "MAX_ACTIVE_THREADS";
    final public static String PREFS_DOWNLOAD_ATTEMPTS = "DOWNLOAD_ATTEMPTS";
    final public static String PREFS_ATTEMPT_TIMEOUT = "ATTEMPT_TIMEOUT";
    final public static String PREFS_BACKGROUND_COLOR_RGB = "BACKGROUND_COLOR_RGB";
    final public static String PREFS_LAST_URL = "LAST_URL";
    final public static String PREFS_EXTRACT_IMAGES_FROM_DETAILS = "EXTRACT_IMAGES_FROM_DETAILS";
    final public static String PREFS_USE_PAGES_LIMIT = "USE_PAGES_LIMIT";
    final public static String PREFS_PAGES_LIMIT = "PAGES_LIMIT";
    final public static String PREFS_USE_SPACE_LIMIT = "USE_SPACE_LIMIT";
    final public static String PREFS_SPACE_LIMIT = "SPACE_LIMIT";
    final public static String PREFS_IGNORE_IMAGES = "IGNORE_IMAGES";
    final public static String PREFS_IGNORE_IMAGES_WIDTH = "IGNORE_IMAGES_WIDTH";
    final public static String PREFS_IGNORE_IMAGES_HEIGHT = "IGNORE_IMAGES_HEIGHT";

    final public static String GRABBER_IDLING = "Working...";
    final public static String GRABBER_DOWNLOADING = "Download in progress...";
    final public static String GRABBER_WORKING = "Working...";

    private Preferences prefs = Preferences.userNodeForPackage( Main.class );

    private JFileChooser chooser = new JFileChooser();

    //    private Grabber grabber;
    private BehanceGrabber grabber;

    public Main() {
        logger.debug("starting program");
        BehanceGrabber.fillGategories();
        initComponents();

        try {
            this.setIconImage( ImageIO.read( this.getClass().getResourceAsStream( "/kovalenko/elance/aligrabber/resources/logo.png" ) ) );
        } catch ( Exception e ) {
            e.printStackTrace();
        }

        this.chooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );

        // Restore frame position.
        if ( this.prefs.getBoolean( Main.PREFS_FIRST_RUN, true ) ) {
            this.setLocationByPlatform( true );
            this.prefs.putBoolean( Main.PREFS_FIRST_RUN, false );
        } else {
            this.setBounds(
                    this.prefs.getInt( Main.PREFS_FRAME_X, 0 ),
                    this.prefs.getInt( Main.PREFS_FRAME_Y, 0 ),
                    this.prefs.getInt( Main.PREFS_FRAME_WIDTH, 640 ),
                    this.prefs.getInt( Main.PREFS_FRAME_HEIGHT, 480 )
            );
        }

        // Restore settings.
        this.targetDirectory.getDocument().addDocumentListener( new DocumentListener() {
            @Override
            public void insertUpdate( DocumentEvent e ) {
                pruneTargetDirectory.setEnabled( ! targetDirectory.getText().isEmpty() );
            }
            @Override
            public void removeUpdate( DocumentEvent e ) {
                pruneTargetDirectory.setEnabled( ! targetDirectory.getText().isEmpty() );
            }
            @Override
            public void changedUpdate( DocumentEvent e ) {
                pruneTargetDirectory.setEnabled( ! targetDirectory.getText().isEmpty() );
            }
        } );

//        this.url.setText( this.prefs.get( Main.PREFS_LAST_URL, "" ) );
        this.targetDirectory.setText( this.prefs.get( Main.PREFS_TARGET_DIRECTORY, "" ) );
        this.limitOfImages.setValue(this.prefs.getInt(Main.PREFS_LIMIT_IMAGES, 5));
//        this.useProxyServer.setSelected( this.prefs.getBoolean( Main.PREFS_USE_PROXY_SERVER, false ) );
//        this.proxyServer.setEditable( this.useProxyServer.isSelected() );
        this.proxyServer.setText( this.prefs.get( Main.PREFS_PROXY_SERVER, "" ) );
        this.countProjectForParsing.setValue( this.prefs.getInt( Main.PREFS_MAX_ACTIVE_THREADS, 72 ) );

        this.imagesInRow.setValue(this.prefs.getInt("imagesInRow",3));
        this.projectLimit.setValue( this.prefs.getInt( Main.PREFS_DOWNLOAD_ATTEMPTS, 100 ) );
        this.attemptTimeout.setValue( this.prefs.getInt( Main.PREFS_ATTEMPT_TIMEOUT, 60 ) );
        this.extractImagesFromDetails.setSelected( this.prefs.getBoolean( Main.PREFS_EXTRACT_IMAGES_FROM_DETAILS, false ) );

        // Limit by number of pages
        this.usePagesLimit.setSelected( this.prefs.getBoolean( Main.PREFS_USE_PAGES_LIMIT, false ) );
        this.pagesLimit.setValue( this.prefs.getInt( Main.PREFS_PAGES_LIMIT, 1 ) );
        this.pagesLimit.setEnabled( this.usePagesLimit.isSelected() );
        this.jLabel4.setEnabled( this.usePagesLimit.isSelected() );

        // Limit by free disk space
        this.useSpaceLimit.setSelected( this.prefs.getBoolean( Main.PREFS_USE_SPACE_LIMIT, false ) );
        this.spaceLimit.setValue( this.prefs.getInt( Main.PREFS_SPACE_LIMIT, 50 ) );
        this.spaceLimit.setEnabled( this.useSpaceLimit.isSelected() );
        this.jLabel5.setEnabled( this.useSpaceLimit.isSelected() );


        this.backgroundColor.setBackground( new Color( this.prefs.getInt( Main.PREFS_BACKGROUND_COLOR_RGB, -12763843 ) ) ); // #616161

        // Ignore images by size
        this.ignoreImages.setSelected( this.prefs.getBoolean( Main.PREFS_IGNORE_IMAGES, true ) );
        this.ignoreImagesWidth.setValue( new Integer( this.prefs.getInt( Main.PREFS_IGNORE_IMAGES_WIDTH, 200 ) ) );
        this.ignoreImagesWidth.setEnabled( this.ignoreImages.isSelected() );
        this.ignoreImagesHeight.setValue( new Integer( this.prefs.getInt( Main.PREFS_IGNORE_IMAGES_HEIGHT, 200 ) ) );
        this.ignoreImagesHeight.setEnabled( this.ignoreImages.isSelected() );


        this.jLabel13.setEnabled( this.ignoreImages.isSelected() );
        this.jLabel14.setEnabled( this.ignoreImages.isSelected() );

        // Off we go.
        this.setVisible( true );
    }

    private void confirmQuit() {
        boolean readyToQuit = true;

        // Poke the grabber thread with a stick.
        if ( this.grabber != null && this.grabber.isAlive() ) {
            readyToQuit = false;
            if ( JOptionPane.showConfirmDialog( this.getRootPane(), "The download is in progress! Are you sure you want to quit?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION ) {
                this.grabber.interrupt();
                readyToQuit = true;
            }
        }

        if ( readyToQuit ) {
            this.dispose();
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings( "unchecked" )
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTabbedPane1 = new javax.swing.JTabbedPane();
        grabberPanel = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
//        url = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        targetDirectory = new javax.swing.JTextField();
        changeTargetDirectory = new javax.swing.JButton();
        pruneTargetDirectory = new javax.swing.JButton();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        progress = new javax.swing.JTextArea();
        jLabel7 = new javax.swing.JLabel();
        state = new javax.swing.JLabel();
        start = new javax.swing.JButton();
        settingsPanel = new javax.swing.JPanel();
//        useProxyServer = new javax.swing.JCheckBox();
        proxyServer = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        countProjectForParsing = new javax.swing.JSpinner();
        jPanel2 = new javax.swing.JPanel();
        usePagesLimit = new javax.swing.JCheckBox();
        pagesLimit = new javax.swing.JSpinner();
        jLabel4 = new javax.swing.JLabel();
        useSpaceLimit = new javax.swing.JCheckBox();
        spaceLimit = new javax.swing.JSpinner();
        jLabel11 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new javax.swing.JButton();
        jLabel6 = new javax.swing.JLabel();
        spaceBetweenImages = new javax.swing.JSpinner();
        jLimitImage = new javax.swing.JLabel();

        limitOfImages = new javax.swing.JSpinner();
        jLabel8 = new javax.swing.JLabel();
        backgroundColor = new javax.swing.JLabel();
        jLabel9 = new javax.swing.JLabel();
        imagesInRow = new javax.swing.JSpinner();
        extractImagesFromDetails = new javax.swing.JCheckBox();
        jLabel10 = new javax.swing.JLabel();
        projectLimit = new javax.swing.JSpinner();
        selectCategory = new javax.swing.JSpinner();
        selectCat = new javax.swing.JComboBox<String>();

        attemptTimeout = new javax.swing.JSpinner();
        jLabel12 = new javax.swing.JLabel();
        ignoreImages = new javax.swing.JCheckBox();
        ignoreImagesWidth = new javax.swing.JSpinner();
        jLabel13 = new javax.swing.JLabel();
        ignoreImagesHeight = new javax.swing.JSpinner();
        jLabel14 = new javax.swing.JLabel();
        jLabel15 = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("Grabber");
        setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        setMinimumSize(new java.awt.Dimension(640, 480));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        jTabbedPane1.setFocusable(false);
        jTabbedPane1.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N

        grabberPanel.setFocusable(false);
        grabberPanel.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        jLabel2.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel2.setText("URL:");
        jLabel2.setVisible(false);

//        url.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        jLabel1.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel1.setText("Target directory:");

        targetDirectory.setEditable(false);
        targetDirectory.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        changeTargetDirectory.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        changeTargetDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kovalenko/elance/aligrabber/resources/folder-open.png"))); // NOI18N
        changeTargetDirectory.setText("Change...");
        changeTargetDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                changeTargetDirectoryActionPerformed(evt);
            }
        });

        pruneTargetDirectory.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        pruneTargetDirectory.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kovalenko/elance/aligrabber/resources/minus-button.png"))); // NOI18N
        pruneTargetDirectory.setText("Prune...");
        pruneTargetDirectory.setEnabled(false);
        pruneTargetDirectory.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                pruneTargetDirectoryActionPerformed(evt);
            }
        });

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Progress", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        jPanel1.setFocusable(false);

        progress.setEditable(false);
        progress.setFont(new java.awt.Font("Monospaced", 0, 11)); // NOI18N
        jScrollPane1.setViewportView(progress);

        jLabel7.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel7.setText("State:");

        state.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        state.setForeground(new java.awt.Color(153, 0, 0));
        state.setText("");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 674, Short.MAX_VALUE)
                                        .addGroup(jPanel1Layout.createSequentialGroup()
                                                .addComponent(jLabel7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(state, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel7)
                                        .addComponent(state))
                                .addGap(18, 18, 18)
                                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 339, Short.MAX_VALUE)
                                .addContainerGap())
        );

        start.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        start.setIcon(new javax.swing.ImageIcon(getClass().getResource("/kovalenko/elance/aligrabber/resources/tick-button.png"))); // NOI18N
        start.setText("Start");
        start.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout grabberPanelLayout = new javax.swing.GroupLayout(grabberPanel);
        grabberPanel.setLayout(grabberPanelLayout);
        grabberPanelLayout.setHorizontalGroup(
                grabberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(grabberPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(grabberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(grabberPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
//                        .addComponent(url)
                                                .addComponent(selectCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(start))
                                        .addGroup(grabberPanelLayout.createSequentialGroup()
                                                .addComponent(jLabel1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(targetDirectory)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(changeTargetDirectory)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(pruneTargetDirectory)))
                                .addContainerGap())
        );
        grabberPanelLayout.setVerticalGroup(
                grabberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(grabberPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(grabberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel2)
                                        .addComponent(selectCat, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE, Short.MAX_VALUE)
                                        .addComponent(start))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(grabberPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel1)
                                        .addComponent(targetDirectory, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(changeTargetDirectory)
                                        .addComponent(pruneTargetDirectory))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addContainerGap())
        );

        jTabbedPane1.addTab("Grabber", grabberPanel);

//        useProxyServer.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
//        useProxyServer.setText("Use proxy server:");
//        useProxyServer.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
//        useProxyServer.addActionListener(new java.awt.event.ActionListener() {
//            public void actionPerformed(java.awt.event.ActionEvent evt) {
//                useProxyServerActionPerformed(evt);
//            }
//        });

        proxyServer.setEditable(false);
        proxyServer.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N

        jLabel3.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel3.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel3.setText("count of thread");
        jLabel3.setVisible(false);

        countProjectForParsing.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        countProjectForParsing.setModel(new javax.swing.SpinnerNumberModel(72, 24, 1200, 12));
        countProjectForParsing.setVisible(false);

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Limits", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N
        jPanel2.setVisible(false);
        usePagesLimit.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        usePagesLimit.setText("Limit grabbing to:");
        usePagesLimit.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        usePagesLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                usePagesLimitActionPerformed(evt);
            }
        });

        pagesLimit.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        pagesLimit.setModel(new javax.swing.SpinnerNumberModel(8, 1, 10000, 1));
        pagesLimit.setEnabled(false);

        jLabel4.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel4.setText("page(s)");
        jLabel4.setEnabled(false);

        useSpaceLimit.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        useSpaceLimit.setText("Stop grabbing when:");
        useSpaceLimit.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        useSpaceLimit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useSpaceLimitActionPerformed(evt);
            }
        });

        spaceLimit.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        spaceLimit.setModel(new javax.swing.SpinnerNumberModel(50, 50, 1000000, 50));
        spaceLimit.setEnabled(false);

        jLabel5.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel5.setText("MB of disk space is left");
        jLabel5.setEnabled(false);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                        .addComponent(useSpaceLimit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addComponent(usePagesLimit, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(pagesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel4))
                                        .addGroup(jPanel2Layout.createSequentialGroup()
                                                .addComponent(spaceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jLabel5)))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
                jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel2Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(usePagesLimit)
                                        .addComponent(pagesLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel4))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(useSpaceLimit)
                                        .addComponent(spaceLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel5))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Composition of images", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 11))); // NOI18N

        jButton1.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jButton1.setText("Background color...");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jLabel6.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel6.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel6.setText("Space between images:");

        jLimitImage.setFont(new java.awt.Font("Dialog", 0, 11));
        jLimitImage.setHorizontalAlignment(SwingConstants.TRAILING);
        jLimitImage.setText(" limit of created image(s) ");

        spaceBetweenImages.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        spaceBetweenImages.setModel(new javax.swing.SpinnerNumberModel(10, 0, 500, 1));

        limitOfImages.setFont(new java.awt.Font("Dialog", 0, 11));
        limitOfImages.setModel(new javax.swing.SpinnerNumberModel(5, 1, 15, 1 ));

        jLabel8.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel8.setText("pixel(s)");
        jLabel8.setEnabled(false);

        backgroundColor.setBackground(new java.awt.Color(97, 97, 97));
        backgroundColor.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        backgroundColor.setText("    ");
        backgroundColor.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        backgroundColor.setOpaque(true);

        jLabel9.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel9.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel9.setText("Images in a row:");

        imagesInRow.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        imagesInRow.setModel(new javax.swing.SpinnerNumberModel(3, 1, 5, 1));

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addComponent(jButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(backgroundColor))
                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                        .addComponent(jLabel9, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel6, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLimitImage, GroupLayout.Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(jPanel3Layout.createSequentialGroup()
                                                                .addComponent(spaceBetweenImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel8))
                                                                .addComponent(jLimitImage)
                                                                .addComponent(limitOfImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)

                                                        .addComponent(imagesInRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                                .addComponent(limitOfImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE).addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
                jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(jPanel3Layout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jButton1)
                                        .addComponent(backgroundColor))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel6)
                                        .addComponent(spaceBetweenImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel8))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)


                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLimitImage)
                                        .addComponent(limitOfImages, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))

                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)

//                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)


                                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel9)
                                        .addComponent(imagesInRow, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        extractImagesFromDetails.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        extractImagesFromDetails.setText("Extract images from Details");
        extractImagesFromDetails.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        extractImagesFromDetails.setVisible(false);

        jLabel10.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        jLabel10.setText("Number of projects for parsing :");
        jLabel10.setVisible(false);
        projectLimit.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        projectLimit.setModel(new javax.swing.SpinnerNumberModel(46, 12, 1200, 12));
        projectLimit.setVisible(false);

        selectCat.setFont(new java.awt.Font("Dialog", 0, 11));
        selectCat.setMaximumRowCount(20);
        selectCat.setModel(new DefaultComboBoxModel<>(new ArrayList(BehanceGrabber.categoryMap.keySet()).toArray()));

        selectCategory.setFont(new java.awt.Font("Dialog", 0, 11));
        selectCategory.setModel(new javax.swing.SpinnerListModel(
                new ArrayList(BehanceGrabber.categoryMap.keySet())
        ));

        jLabel11.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel11.setText("Attempt timeout:");
        jLabel11.setVisible(false);

        attemptTimeout.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        attemptTimeout.setModel(new javax.swing.SpinnerNumberModel(60, 1, 180, 1));
        attemptTimeout.setVisible(false);
        jLabel12.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel12.setText("second(s)");
        jLabel12.setVisible(false);

        ignoreImages.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        ignoreImages.setText("Ignore images less than:");
        ignoreImages.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        ignoreImages.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ignoreImagesActionPerformed(evt);
            }
        });
        ignoreImages.setVisible(false);

        ignoreImagesWidth.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        ignoreImagesWidth.setModel(new javax.swing.SpinnerNumberModel(200, 1, 99999, 1));
        ignoreImagesWidth.setVisible(false);

        jLabel13.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel13.setText("x");
        jLabel13.setVisible(false);

        ignoreImagesHeight.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        ignoreImagesHeight.setModel(new javax.swing.SpinnerNumberModel(200, 1, 99999, 1));
        ignoreImagesHeight.setVisible(false);

        jLabel14.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jLabel14.setText("pixel(s)");
        jLabel14.setVisible(false);

        jLabel15.setFont(new java.awt.Font("Dialog", 1, 11)); // NOI18N
        jLabel15.setForeground(new java.awt.Color(153, 0, 0));
        jLabel15.setText("Setting this too high will cause severe impact on performance!");
        jLabel15.setToolTipText("");
        jLabel15.setVisible(false);

        javax.swing.GroupLayout settingsPanelLayout = new javax.swing.GroupLayout(settingsPanel);
        settingsPanel.setLayout(settingsPanelLayout);
        settingsPanelLayout.setHorizontalGroup(
                settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                        .addComponent(jPanel3, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
//                            .addComponent(useProxyServer, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                        .addComponent(jLabel3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 184, Short.MAX_VALUE))
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                                .addComponent(countProjectForParsing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel15, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                        .addComponent(proxyServer)))
                                        .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                                                        .addComponent(ignoreImages, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                                                        .addComponent(extractImagesFromDetails, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                                                                .addGap(18, 18, 18)
                                                                .addComponent(ignoreImagesWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel13)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(ignoreImagesHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel14))
                                                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                                                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 184, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addGap(21, 21, 21)
                                                                .addComponent(projectLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                                .addComponent(jLabel11)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(attemptTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                                .addComponent(jLabel12)))
                                                .addGap(69, 215, Short.MAX_VALUE)))
                                .addContainerGap())
        );
        settingsPanelLayout.setVerticalGroup(
                settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(settingsPanelLayout.createSequentialGroup()
                                .addContainerGap()
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
//                    .addComponent(useProxyServer)
                                        .addComponent(proxyServer, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel3)
                                        .addComponent(countProjectForParsing, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel15))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(jLabel10)
                                        .addComponent(projectLimit, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel11)
                                        .addComponent(attemptTimeout, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel12))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(extractImagesFromDetails)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(settingsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                        .addComponent(ignoreImages)
                                        .addComponent(ignoreImagesWidth, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel13)
                                        .addComponent(ignoreImagesHeight, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addComponent(jLabel14))
                                .addGap(18, 18, 18)
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(18, 18, 18)
                                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(104, Short.MAX_VALUE))
        );

        jTabbedPane1.addTab("Settings", settingsPanel);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
                layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(jTabbedPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // Save position and dimensions of the frame.
        this.prefs.putInt( Main.PREFS_FRAME_X, this.getX() );
        this.prefs.putInt( Main.PREFS_FRAME_Y, this.getY() );
        this.prefs.putInt( Main.PREFS_FRAME_WIDTH, this.getWidth() );
        this.prefs.putInt( Main.PREFS_FRAME_HEIGHT, this.getHeight() );
        // Save settings.
        this.prefs.put( Main.PREFS_LIMIT_IMAGES, this.limitOfImages.getValue().toString() );
        this.prefs.put( Main.PREFS_TARGET_DIRECTORY, this.targetDirectory.getText() );
        this.prefs.put( Main.PREFS_PROXY_SERVER, this.proxyServer.getText() );
        this.prefs.putInt( Main.PREFS_MAX_ACTIVE_THREADS, Integer.parseInt( this.countProjectForParsing.getValue().toString() ) );
        this.prefs.putInt( Main.PREFS_DOWNLOAD_ATTEMPTS, Integer.parseInt( this.projectLimit.getValue().toString() ) );
        this.prefs.putInt( Main.PREFS_ATTEMPT_TIMEOUT, Integer.parseInt( this.attemptTimeout.getValue().toString() ) );
        this.prefs.putInt( Main.PREFS_BACKGROUND_COLOR_RGB, this.backgroundColor.getBackground().getRGB() );
        this.prefs.putBoolean( Main.PREFS_EXTRACT_IMAGES_FROM_DETAILS, this.extractImagesFromDetails.isSelected() );
        this.prefs.putBoolean( Main.PREFS_USE_PAGES_LIMIT, this.usePagesLimit.isSelected() );
        this.prefs.putInt( Main.PREFS_PAGES_LIMIT, Integer.parseInt( this.pagesLimit.getValue().toString() ) );
        this.prefs.putBoolean( Main.PREFS_USE_SPACE_LIMIT, this.useSpaceLimit.isSelected() );
        this.prefs.putInt( Main.PREFS_SPACE_LIMIT, Integer.parseInt( this.spaceLimit.getValue().toString() ) );
        this.prefs.putBoolean( Main.PREFS_IGNORE_IMAGES, this.ignoreImages.isSelected() );
        this.prefs.putInt( Main.PREFS_IGNORE_IMAGES_WIDTH, Integer.parseInt( this.ignoreImagesWidth.getValue().toString() ) );
        this.prefs.putInt( Main.PREFS_IGNORE_IMAGES_HEIGHT, Integer.parseInt( this.ignoreImagesHeight.getValue().toString() ) );
        this.prefs.putInt("imagesInRow", Integer.parseInt(this.imagesInRow.getValue().toString()));

        // Ensure that preferences are written into the store.
        try {
            this.prefs.flush();
        } catch ( BackingStoreException bse ) {
            bse.printStackTrace();
            // @todo Possibly it's needed to come up with a warning here.
        } catch ( Exception e ) {
            e.printStackTrace();
            // @todo Possibly it's needed to come up with a warning here.
        }
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing

        this.confirmQuit();
    }//GEN-LAST:event_formWindowClosing


    private void startActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startActionPerformed
        if ( "Start".equals( evt.getActionCommand() ) ) {
            if ( JOptionPane.showConfirmDialog( this.getRootPane(), "Are you sure you want to start?", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE ) == JOptionPane.OK_OPTION ) {
                // Start a grabber thread.
                this.changeTargetDirectory.setEnabled( false );
                this.pruneTargetDirectory.setEnabled( false );

                state.setText("working...");
                if(grabber!=null){
                    grabber.interrupt();
                }
                try {
                    this.grabber = new BehanceGrabber(this);
                }catch (BehanceException ex){
                    JOptionPane.showMessageDialog(null, "Target directory doesnt exist ", "Error", JOptionPane.ERROR_MESSAGE);
                }
                grabber.setDaemon(true);
                this.grabber.start();

            }
        } else if ( "Stop".equals( evt.getActionCommand() ) ) {
            if ( JOptionPane.showConfirmDialog( this.getRootPane(), "The download is in progress! Are you sure you want to stop?", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION ) {
                // Stop a grabber thread.
                this.grabber.interrupt();
                state.setText("");
                this.changeTargetDirectory.setEnabled( true );
                this.pruneTargetDirectory.setEnabled( true );
            }
        }
    }//GEN-LAST:event_startActionPerformed

    private void pruneTargetDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_pruneTargetDirectoryActionPerformed
        if ( JOptionPane.showConfirmDialog( this.getRootPane(), "Are you sure you want to prune the selected directory? This operation is irreversible!", "Warning", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE ) == JOptionPane.OK_OPTION ) {
            // @todo Prune directory
        }
    }//GEN-LAST:event_pruneTargetDirectoryActionPerformed

    private void changeTargetDirectoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_changeTargetDirectoryActionPerformed
        boolean selected = false;
        if ( System.getProperty( "os.name" ).contains( "Mac" ) ) {
            FileDialog dialog = new FileDialog( this );
            System.setProperty( "apple.awt.fileDialogForDirectories", "true" );
            dialog.setVisible( true );
            dialog.setMultipleMode( false );
            System.setProperty( "apple.awt.fileDialogForDirectories", "false" );
            if ( dialog.getDirectory() != null ) {
                this.targetDirectory.setText( dialog.getDirectory() + dialog.getFile() );
                selected = true;
            }
        } else {
            if ( this.chooser.showOpenDialog( this ) == JFileChooser.APPROVE_OPTION ) {
                this.targetDirectory.setText( this.chooser.getSelectedFile().getAbsolutePath() );
                selected = true;
            }
        }
        if ( selected ) {
            // @todo Check and use metadata.
        }
    }//GEN-LAST:event_changeTargetDirectoryActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        Color color = JColorChooser.showDialog( this, "Choose background color", this.backgroundColor.getBackground() );
        if ( color != null ) {
            this.backgroundColor.setBackground( color );
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void usePagesLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_usePagesLimitActionPerformed
        if ( this.usePagesLimit.isSelected() ) {
            this.pagesLimit.setEnabled( true );
            this.jLabel4.setEnabled( true );
        } else {
            this.pagesLimit.setEnabled( false );
            this.jLabel4.setEnabled( false );
        }
    }//GEN-LAST:event_usePagesLimitActionPerformed

    private void useSpaceLimitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useSpaceLimitActionPerformed
        if ( this.useSpaceLimit.isSelected() ) {
            this.spaceLimit.setEnabled( true );
            this.jLabel5.setEnabled( true );
        } else {
            this.spaceLimit.setEnabled( false );
            this.jLabel5.setEnabled( false );
        }
    }//GEN-LAST:event_useSpaceLimitActionPerformed

    private void ignoreImagesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ignoreImagesActionPerformed
        this.ignoreImagesWidth.setEnabled( this.ignoreImages.isSelected() );
        this.ignoreImagesHeight.setEnabled( this.ignoreImages.isSelected() );
        this.jLabel13.setEnabled( this.ignoreImages.isSelected() );
        this.jLabel14.setEnabled( this.ignoreImages.isSelected() );
    }//GEN-LAST:event_ignoreImagesActionPerformed

    public static void main( String args[] ) {
        FontUIResource dialogFont = new FontUIResource( new Font( "Dialog", Font.PLAIN, 11 ) );
        UIManager.put( "Button.font", dialogFont );
        UIManager.put( "ColorChooser.font", dialogFont );
        UIManager.put( "ComboBox.font", dialogFont );
        UIManager.put( "Label.font", dialogFont );
        UIManager.put( "List.font", dialogFont );
        UIManager.put( "OptionPane.buttonFont", dialogFont );
        UIManager.put( "OptionPane.messageFont", dialogFont );
        UIManager.put( "Menu.font", dialogFont );
        UIManager.put( "MenuItem.font", dialogFont );
        UIManager.put( "RadioButtonMenuItem.font", dialogFont );
        UIManager.put( "TableHeader.font", dialogFont );
        UIManager.put( "TextField.font", dialogFont );
        UIManager.put( "ToolTip.font", dialogFont );

        /* Create and display the form */
        java.awt.EventQueue.invokeLater( new Runnable() {
            public void run() {
                new Main();
            }
        } );
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JSpinner attemptTimeout;
    protected javax.swing.JLabel backgroundColor;
    protected javax.swing.JButton changeTargetDirectory;
    protected javax.swing.JSpinner projectLimit;
    protected javax.swing.JSpinner selectCategory;
    protected javax.swing.JCheckBox extractImagesFromDetails;
    private javax.swing.JPanel grabberPanel;
    protected javax.swing.JCheckBox ignoreImages;
    protected javax.swing.JSpinner ignoreImagesHeight;
    protected javax.swing.JSpinner ignoreImagesWidth;
    protected javax.swing.JSpinner imagesInRow;
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel11;
    private javax.swing.JLabel jLabel12;
    private javax.swing.JLabel jLabel13;
    private javax.swing.JLabel jLabel14;
    private javax.swing.JLabel jLabel15;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabel9;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JLabel jLimitImage;


    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTabbedPane jTabbedPane1;
    protected javax.swing.JSpinner countProjectForParsing;
    protected javax.swing.JSpinner pagesLimit;
    public javax.swing.JTextArea progress;
    //    protected javax.swing.JTextArea progress;
    private javax.swing.JTextField proxyServer;
    protected javax.swing.JButton pruneTargetDirectory;
    private javax.swing.JPanel settingsPanel;
    protected javax.swing.JSpinner spaceBetweenImages;
    protected javax.swing.JSpinner spaceLimit;
    protected javax.swing.JSpinner limitOfImages;

    protected javax.swing.JButton start;
    protected javax.swing.JLabel state;
    protected javax.swing.JTextField targetDirectory;
    //    protected javax.swing.JTextField url;
    protected javax.swing.JCheckBox usePagesLimit;
    //    private javax.swing.JCheckBox useProxyServer;
    protected javax.swing.JCheckBox useSpaceLimit;
    protected javax.swing.JComboBox selectCat;
    // End of variables declaration//GEN-END:variables
}
