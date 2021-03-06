package dodGUI;

import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.JPanel;

public class VisionPanel extends JPanel {

	private static final long serialVersionUID = -1963944153087665904L;

	private int rows;
	private int cols;
	
	private boolean hasUser; //Whether or not this is for a user.
	
	/**
	 * Panel all the tiles live on.
	 */
	private JPanel gridPanel;
	
	//All the images that are drawn.
	private BufferedImage tile;
	private BufferedImage wall;
	private BufferedImage unknown;
	private BufferedImage exit;
	private BufferedImage armour;
	private BufferedImage hp;
	private BufferedImage sword;
	private BufferedImage lantern;
	private BufferedImage gold;
	private BufferedImage player;
	private BufferedImage enemy;

	/**
	 * Sets up a new VisionPanel, loading in images and setting the size of the grid.
	 * @param setCols Number of columns
	 * @param setRows Number of rows
	 * @param isUSer whether or not to draw user.
	 */
	public VisionPanel(int setCols, int setRows, boolean isUser){
		
		//This is a bit hacky, but makes it so that the tiles don't clip or move around.
		//It works but I don't know why.
		//I feel it's better than having the GUI be ugly.
		
		hasUser = isUser;
		
		this.setLayout(new FlowLayout(FlowLayout.LEFT));
		gridPanel = new JPanel();
		
		//Make the gridlayout a specific size.
		rows = setRows;
		cols = setCols;
		gridPanel.setLayout(new GridLayout(rows, cols));
		this.add(gridPanel);
		
		//Load in the images.
		try {
			tile 	= ImageIO.read(new File("Resources/tilePlain.png"));
			wall 	= ImageIO.read(new File("Resources/wall.png"));
			unknown = ImageIO.read(new File("Resources/unknown.png"));
			exit 	= ImageIO.read(new File("Resources/exit.png"));
			armour 	= ImageIO.read(new File("Resources/armour.png"));
			hp 		= ImageIO.read(new File("Resources/hp.png"));
			sword 	= ImageIO.read(new File("Resources/sword.png"));
			lantern = ImageIO.read(new File("Resources/lantern.png"));
			gold 	= ImageIO.read(new File("Resources/money.png"));
			player 	= ImageIO.read(new File("Resources/gosling.png"));
			enemy 	= ImageIO.read(new File("Resources/gates.png"));
		} catch (IOException e) {
			System.err.println("There was an error whilst loading resources. Are some files corrupt?");
			e.printStackTrace();
		}

		
	}
	/**
	 * Change the size. Must redraw afterwards.
	 * @param size Size to change to.
	 */
	public void changeSize(int setCols, int setRows){
		
		rows = setRows;
		cols = setCols;
		gridPanel.removeAll();
		gridPanel.setLayout(new GridLayout(rows, cols));
		writeArr();
		return;
		
	}
	/**
	 * Refreshes the panel.
	 */
	public void refresh(){
		gridPanel.validate();
		gridPanel.repaint();
		this.validate();
		this.repaint();	
	}
	/**
	 * Writes empty tile image to all tiles on the panel.
	 */
	public void writeArr(){	
		gridPanel.removeAll();
		for(int i = 0; i < (rows*cols); i++){
			ImagePanel imgFrame = new ImagePanel();
			imgFrame.addImage(tile);
			gridPanel.add(imgFrame);
		}
	}
	/**
	 * Writes a char representation of a part of a Dungeon Of Doom map to the panel.
	 * Extension for later - throw an exception if the array is the wrong size.
	 * @param arr Char array containing char representation of this panel, same size as this panel.
	 */
	public void writeArr(char[][] arr){	
		
		gridPanel.removeAll();
		
		for(int rowix = 0; rowix < arr.length; rowix++){
			for(int colix = 0; colix < arr[0].length; colix++){
				
				ImagePanel imgFrame = new ImagePanel();
			
				switch(arr[rowix][colix]){
					case '.':
						imgFrame.addImage(tile);
						break;
					case '#':
						imgFrame.addImage(tile);
						imgFrame.addImage(wall);
						break;
					case 'X':
						imgFrame.addImage(tile);
						imgFrame.addImage(unknown);
						break;
					case 'E':
						imgFrame.addImage(tile);
						imgFrame.addImage(exit);
						break;
					case 'A':
						imgFrame.addImage(tile);
						imgFrame.addImage(armour);
						break;
					case 'H':
						imgFrame.addImage(tile);
						imgFrame.addImage(hp);
						break;
					case 'S':
						imgFrame.addImage(tile);
						imgFrame.addImage(sword);
						break;
					case 'L':
						imgFrame.addImage(tile);
						imgFrame.addImage(lantern);
						break;
					case 'G':
						imgFrame.addImage(tile);
						imgFrame.addImage(gold);
						break;
					case 'P':
						imgFrame.addImage(tile);
						imgFrame.addImage(enemy);
						break;
				}
				
				//On the center, where we have the user, draw the user.
				if((rowix == (arr.length-1)/2) && colix == ((arr[0].length-1)/2) && hasUser == true){
					imgFrame.addImage(player);
				}
				
				gridPanel.add(imgFrame);
				
			}
		}
		refresh();	
	}
}
