package dodClients.gui;

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class VisionPanel extends JPanel {

	private int size;
	

	public VisionPanel(int setSize){
		size = setSize;
		this.setLayout(new GridLayout(size, size));
		}
	
	public void writeArr(){	
		
		BufferedImage tile = null;
		BufferedImage player = null;
		try {
			tile = ImageIO.read(new File("Resources/emptyTile.png"));
			player = ImageIO.read(new File("Resources/player.png"));
		} catch (IOException e) {
			System.err.println("There was an error whilst loading resources. Are some files corrupt?");
			e.printStackTrace();
		}
				
		for(int i = 0; i < (size*size); i++){
			ImageFrame imgFrame = new ImageFrame();
			imgFrame.addImage(tile);
			imgFrame.addImage(player);
			
			this.add(imgFrame);
		}
	}
}
