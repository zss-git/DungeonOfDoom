/**
 * An 'image frame' for the gui, effectively a JLabel with an image painted on it.
 * Taken from here: http://stackoverflow.com/questions/299495/how-to-add-an-image-to-a-jpanel
 * 
 * Works as a stack, then draws added images in the order they appear on the stack. 
 * 
 * @author Zachary Shannon
 * @version 14 Apr 2015
 */

package dodGUI;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayDeque;
import java.util.Deque;

import javax.swing.JLabel;

public class ImagePanel extends JLabel {
	
	private static final long serialVersionUID = -5198242419485658270L;

	private Deque<Image> images = new ArrayDeque<Image>();
	
	boolean preferredSizeUnset = true;
	
	/**
	 * Adds an image to the stack to be drawn on the component. An assumption is made that all images are the same size.
	 * This method may not draw images nicely if all images are not the same size.
	 * @param img Image to add.
	 */
	public void addImage(Image img){
		
		if(preferredSizeUnset){
			//Set the preferred size of this label to be the same as the dimensions of the image just added - this is why images of a different size break drawing a bit.
			this.setPreferredSize(new Dimension(img.getWidth(this), img.getHeight(this)));
			preferredSizeUnset = false;
		}
		
		if(img != null){
			images.add(img);
		}
		
	}
	/**
	 * Removes an image from the stack.
	 */
	public void popImage(){
		images.pop();
		
		//If there is nothing left, make it so the preferred size can be set again.
		if(images.size() < 1){
			preferredSizeUnset = true;
		}
	}
	
	
	/**
	 * Override the method to paint this component, and make it so that it draws our image, too.
	 */
   @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        for(Image i : images){
        	g.drawImage(i, 0, 0, null);
        }
                   
    }
}
