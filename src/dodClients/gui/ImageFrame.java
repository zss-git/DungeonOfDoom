/**
 * An 'image frame' for the gui, effectively a JLabel with an image painted on it.
 * Taken from here: http://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
 * 
 * @author Zachary Shannon
 * @version 13 Apr 2015
 */

package dodClients.gui;

import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JLabel;

public class ImageFrame extends JLabel {
	
	private BufferedImage image;
	
	public ImageFrame(File img){
		try {
			image = ImageIO.read(img);
		} catch (IOException e) {
			System.err.println("Failed to read an image file. Is something corrupt?");
			System.exit(1);
		}
	}
	
	
	/**
	 * Override the method to paint this component, and make it so that it draws our image, too.
	 */
   @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(image, 0, 0, null);           
    }
}
