package kovalenko.elance.aligrabber;

import org.apache.log4j.Logger;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.Callable;

/**
 * Created by mtol on 11.02.2016.
 */
public class ImageLoader implements Callable<BufferedImage> {
    Logger logger = Logger.getLogger(ImageLoader.class);
    private String imageUrl;

    public ImageLoader(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    @Override
    public BufferedImage call() throws Exception {
        return tryLoadImage(imageUrl);
    }

    private BufferedImage tryLoadImage(String imageUrl) {
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

}
