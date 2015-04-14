package dodClients.gui;

import java.awt.GridLayout;
import java.io.File;

import javax.swing.JPanel;

public class VisionPanel extends JPanel {

	private int size;
	

	public VisionPanel(int setSize){
		size = setSize;
		this.setLayout(new GridLayout(size, size));
		this.setSize(size*55, size*55);
	}
	
	public void writeArr(){	
		for(int i = 0; i < (size*size); i++){
			this.add(new ImageFrame(new File("Resources/tilePerson2.png")));
		}
	}
}
