package kovalenko.elance.aligrabber;

import kovalenko.elance.aligrabber.behancerabber.Designer;
import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Set;

/**
 * Created by mtol on 08.02.2016.
 */
public class AsyncImageCreator implements Runnable {

    private Logger logger = Logger.getLogger(AsyncImageCreator.class);

    private Main main;
    private Designer designer;

    public AsyncImageCreator(Main main, Designer d) {
        this.main = main;
        this.designer =d;
    }

    private void createImages(Designer d) {
        try {
            Set<String> tempsLink = Parser.parseImageLinks(d.getImageUrl(), d);

            ImageWorker grabber = new ImageWorker(this.main);
            if (tempsLink.isEmpty()) {
                System.out.print("Project not parsed " + d.getImageUrl() + "\n");
            } else {
                for (String link : tempsLink) {
                    grabber.addImage(tryLoadImage(link), d, false);
                }
            }
            if (BehanceGrabber.expectedImages > tempsLink.size()) { //if files in project less than expectedImages size
                grabber.addImage(null, d, true);
            }

            Parser.saveLastLink(d);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
    public BufferedImage tryLoadImage(String imageUrl) {
        try {
            URL url = new URL(imageUrl);
            InputStream in = url.openStream();
            BufferedImage image = ImageIO.read(in);
            return image;
        } catch (IOException e) {
            System.err.print("can`t load image -> " + imageUrl + "\n error message --> " + e.getMessage());


        } catch (Exception exception) {
            logger.error(exception.getMessage() + " " + imageUrl);
        }
        return null;
    }

    @Override
    public void run() {
        createImages(designer);
    }
}
