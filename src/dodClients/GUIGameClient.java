package dodClients;

import java.awt.GridLayout;
import java.util.Scanner;

import javax.swing.JFrame;

import dodClients.gui.VisionPanel;

public class GUIGameClient extends JFrame implements NetworkMessageListener{
	
	private static final long serialVersionUID = 2360372200249180419L;
	
	private Scanner scn;
	private LookParser lp;
	private VisionPanel vp;
	
	/**
	 * Creates an instance of this.
	 * @param args
	 */
	public static void main(String args[]){	
		new GUIGameClient();
	}
	
	/**
	 * This constructor initiates the GUI and then sets up a NetworkClient and a scanner.
	 */
	public GUIGameClient(){
		lp = new LookParser();
		
		//Holds all the icons.
		vp = new VisionPanel(5);
		vp.writeArr();
		
		//Add everything
		this.setLayout(new GridLayout(1,1));
		this.add(vp);
		this.setSize(600, 600);
		this.setVisible(true);
		
		scn = new Scanner(System.in);
		new NetworkClient(CLUIGameClient.getAddress(scn), CLUIGameClient.getPort(scn), this); //Give the network client a NetworkMessageListener.
	}

	/**
	 * Implementation of message handling part of the NetworkMessageListener. Displays information given by the server in a meaningful way.
	 */
	@Override
	public void handleMessage(String message) {
		
		if(message.startsWith("LOOKREPLY")){
			message = message.replaceAll("\\r|\\n", "");
		}
		
		if(lp.isPartOfLook(message)){
			//Check if the parser is done.
			if(lp.hasLook()){
				char[][] look = lp.getLook();
				vp.changeSize(look[0].length);
				vp.writeArr(look);
				
				//Refresh UI elements and re-validate.
				vp.refresh();
				this.validate();
				this.repaint();
			}
		}
		System.out.println(message);
	}
	/**
	 * Implementation of message getting part of the NetworkMessageListener. Gives the server a command when buttons are pressed.
	 */
	@Override
	public String getMessage() {
		return scn.nextLine();
	}
}
