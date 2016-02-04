package kovalenko.elance.aligrabber;

import kovalenko.elance.aligrabber.behancerabber.Designer;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.*;

/**
 * Created by mtol on 01.02.2016.
 */
public class ImageWorder extends Thread {
    final static Logger logger = Logger.getLogger(ImageWorder.class);
    private static Main main;
    BehanceGrabber thread;
    public ImageWorder(Main main){
        this.main = main;
    }
    @Override
    public void run() {

        thread.setDaemon(true);
        thread.start();
    }
    public java.util.List<BufferedImage> images = new LinkedList<>();

    public synchronized void addImage(BufferedImage image, Designer designer, boolean forseWrite) {

        if (image != null) {
            this.images.add(image);
        } else {
            if (!forseWrite){
                BehanceGrabber.logMessage(main, "Can`t load image. author page - "+ designer.getName()+" project url - "+designer.getImageUrl(), logger);
            }
            return;
        }
        if ((this.images.size() == BehanceGrabber.expectedImages && BehanceGrabber.expectedImages > 0) || forseWrite) {
            int maxWidth = 0;
            int maxHeight = 0;
            Iterator<BufferedImage> it = this.images.iterator();
            while (it.hasNext()) {
                BufferedImage i = it.next();
                maxWidth = Math.max(i.getWidth(), maxWidth);
                maxHeight = Math.max(i.getHeight(), maxHeight);
            }

            int spaceSize = Integer.parseInt(BehanceGrabber.properties.getProperty("spaceSize", "5"));;
            spaceSize = (this.images.size() == 1) ? 0 : spaceSize;
            int imagesInRow = Integer.parseInt( BehanceGrabber.properties.getProperty( "imagesInRow", "3" ) );
            int backgroundColor =  Integer.parseInt( BehanceGrabber.properties.getProperty( "backgroundColor", "0" ) );

            int finalWidth = spaceSize;
            if (this.images.size() <= imagesInRow) {
                finalWidth += (maxWidth + spaceSize) * this.images.size();
            } else {
                finalWidth += (maxWidth + spaceSize) * imagesInRow;
            }

            int finalHeight = spaceSize;
            int rows = (int) Math.ceil((double) this.images.size() / (double) imagesInRow);
            finalHeight += (maxHeight + spaceSize) * rows;

            BufferedImage finalImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = finalImage.createGraphics();
            g2d.setColor(new Color(backgroundColor));
            g2d.fillRect(0, 0, finalWidth, finalHeight);
            int i = 0;
            int x = spaceSize - 1;
            int y = spaceSize - 1;
            it = this.images.iterator();
            while (it.hasNext()) {
                BufferedImage tile = it.next();
                finalImage.getGraphics().drawImage(tile, x, y, null);
                x += maxWidth + spaceSize;
                i++;
                if (i >= imagesInRow) {
                    i = 0;
                    x = spaceSize - 1;
                    y += maxHeight + spaceSize;
                }
            }

            try {
                //For windows
                String location = Parser.definitionLocation(designer);
                if (Parser.getNameFromLink(designer.getName()).isEmpty()||Parser.getNameFromLink(designer.getName())==null){
                    location+="default/";
                    File file = new File( location);
                    if(!file.exists()){
                        file.mkdir();
                    }
                }
                int counter = 1;
                File imageFile = new File(location + Parser.getNameFromLink(designer.getName())+"_" + counter+ ".jpg"); // for windows
//                File imageFile = new File(Parser.definitionLocation(designer) + designer.getName().replaceAll("\\/", "_") + ".jpg"); // for linux
//                File imageFile = new File(location + designer.getName() + ".jpg"); // for Mac
                if (imageFile.isFile()) {

                    do {
                        imageFile = new File(location + Parser.getNameFromLink(designer.getName()) + "_"  + ++counter + ".jpg"); // for windows
//                        imageFile = new File(location + designer.getName() + ++counter + ".jpg"); // for mac
//                        imageFile = new File(Parser.definitionLocation(designer) + designer.getName().replaceAll("\\/", "_") + "_" + ++counter + ".jpg"); for linux

                    } while (imageFile.isFile());
                }
                ImageIO.write(finalImage, "jpg", imageFile);// TODO create method for creation correct name
                BehanceGrabber.logMessage(this.main, "file is saved " + imageFile.getPath(), logger);
            } catch (Exception e) {
                Parser.saveLastLink(designer);
                logger.error(e.getMessage()+designer.getName());

            }

            finalImage.flush();

            it = this.images.iterator();
            while (it.hasNext()) {
                it.next().flush();
            }
            this.images.clear();
            // place for save state

        }

    }
}
