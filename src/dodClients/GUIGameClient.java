package dodClients;

import java.awt.GridBagLayout;

import javax.swing.JFrame;

import dodClients.gui.VisionPanel;

public class GUIGameClient implements NetworkMessageListener{
	
	public static void main(String args[]){	
		JFrame mainFrame = new JFrame();
		VisionPanel vp = new VisionPanel(7);
		vp.writeArr();
		mainFrame.setSize(700, 700);
		mainFrame.add(vp);
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
