package kovalenko.elance.aligrabber;

import kovalenko.elance.aligrabber.behancerabber.Designer;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

public class ImageWorker extends Thread {
    final static Logger logger = Logger.getLogger(ImageWorker.class);
    private static Main main;
    int attemptCounter = 0;

    public ImageWorker(Main main){
        this.main = main;
    }

    public java.util.List<BufferedImage> images = new ArrayList<>();



    public synchronized boolean addImage(BufferedImage image, Designer designer, boolean forseWrite) {

        if (image != null) {
            this.images.add(image);
        } else {
            if (!forseWrite){
                BehanceGrabber.logMessage(main, "Can`t load image. author page - "+ designer.getName()+" project url - "+designer.getImageUrl(), logger);
            }
//            return;
        }
        if ((this.images.size() == BehanceGrabber.expectedImages) || forseWrite) {
            int maxWidth = 0;
            int maxHeight = 0;

            Parser.numberOfPages = Integer.parseInt(BehanceGrabber.properties.getProperty("limitImages", "5"));
            int spaceSize = Integer.parseInt(BehanceGrabber.properties.getProperty("spaceSize", "5"));;
            spaceSize = (this.images.size() == 1) ? 0 : spaceSize;
            int imagesInRow = Integer.parseInt( BehanceGrabber.properties.getProperty( "imagesInRow", "3" ) );
            int backgroundColor =  Integer.parseInt( BehanceGrabber.properties.getProperty( "backgroundColor", "0" ) );
            //  sortPicture(images);

            Collections.sort(images, new Comparator<BufferedImage>() {
                @Override
                public int compare(BufferedImage o1, BufferedImage o2) {
                    return o2.getHeight() - o1.getHeight();
                }
            });
            int height = 0;
            for(int i = 0; i< images.size(); i =i+imagesInRow){
                maxHeight+=images.get(i).getHeight();

            }




            Iterator<BufferedImage> it = this.images.iterator();
            while (it.hasNext()) {
                BufferedImage i = it.next();
                maxWidth = Math.max(i.getWidth(), maxWidth);
//                maxHeight = Math.max(i.getHeight(), maxHeight);
            }



            int finalWidth = spaceSize;
            if (this.images.size() <= imagesInRow) {
                finalWidth += (maxWidth + spaceSize) * this.images.size();
            } else {
                finalWidth += (maxWidth + spaceSize) * imagesInRow;
            }

            int finalHeight = spaceSize;
            int rows = (int) Math.ceil((double) this.images.size() / (double) imagesInRow);
            finalHeight = (maxHeight) + spaceSize * rows;

            if(finalHeight<=1 || finalWidth<=1){
                logger.error("small size  h = "+finalHeight+" width = "+ finalHeight);
                return forseWrite;
            }
            BufferedImage finalImage = new BufferedImage(finalWidth, finalHeight, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = finalImage.createGraphics();
            g2d.setColor(new Color(backgroundColor));
            g2d.fillRect(0, 0, finalWidth, finalHeight);
            int i = 0;
            int x = spaceSize - 1;
            int y = spaceSize - 1;
            int itr =0;
            it = this.images.iterator();
            while (it.hasNext()) {
                BufferedImage tile = it.next();
                finalImage.getGraphics().drawImage(tile, x, y, null);
                x += maxWidth + spaceSize;
                i++;
                if (i >= imagesInRow) {
                    i = 0;
                    x = spaceSize - 1;
                    if(itr>images.size()-1){
                        itr -=imagesInRow;
                        itr+=1;
                        if(itr<=0){
                            itr = 0; // if pictures less than images in row
                        }
                    }
                    y += images.get(itr).getHeight()+spaceSize-1;
                    itr +=imagesInRow;
                }
            }

            String location = null;
            String nameFromLink;
            try {
                //For windows
                location = Parser.definitionLocation(designer);
                nameFromLink = Parser.getNameFromLink(designer.getName());
                if (nameFromLink.isEmpty()){

                    logger.error("can`t parse the link name "+ designer.getName());
                    return false;
                }
                int counter = 1;
                File imageFile = new File(location + nameFromLink+"_" + counter+ ".jpg"); // for windows

                if (imageFile.isFile()) {

                    do {
                        imageFile = new File(location + nameFromLink + "_"  + ++counter + ".jpg"); // for windows
                    } while (imageFile.isFile());
                }
                ImageIO.write(finalImage, "jpg", imageFile);// TODO create method for creation correct name
                BehanceGrabber.logMessage(main, "file is saved " + imageFile.getPath(), logger);
                attemptCounter = 0;

                finalImage.flush();

                it = this.images.iterator();
                while (it.hasNext()) {
                    it.next().flush();
                }
                this.images.clear();

                return counter < Parser.numberOfPages;

            } catch (Exception e) {
                if(attemptCounter > 4){
                    Parser.isFinish = true;
                }
                attemptCounter++;

                BehanceGrabber.logMessage(main, "Cant`t save file "+location, logger);
                logger.error(e.getMessage(), e);

            }


            // place for save state

        }

        return true;
    }





}
