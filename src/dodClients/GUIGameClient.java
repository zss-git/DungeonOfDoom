package dodClients;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JFrame;

import dodClients.gui.VisionPanel;

public class GUIGameClient implements NetworkMessageListener{
	
	public static void main(String args[]){	
		//The JFrame everything shall live on.
		JFrame mainFrame = new JFrame();
		
		//Holds all the icons.
		VisionPanel vp = new VisionPanel(5);
		vp.writeArr();
		
		//Add everything
		mainFrame.setLayout(new GridLayout(2,2));
		mainFrame.add(vp);
		mainFrame.add(new JButton("Test button - HELLO!"));
		//mainFrame.setSize(700,700);
		mainFrame.pack();
		mainFrame.setVisible(true);
	}

	@Override
	public void handleMessage(String message) {
		System.out.println(message);
	}

	@Override
	public String getMessage() {
		return null;
	}
	
}
