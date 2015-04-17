package dodClients;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Scanner;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import dodGUI.VisionPanel;

public class GUIGameClient extends JFrame implements NetworkMessageListener{
	
	private static final long serialVersionUID = 2360372200249180419L;
	
	private Scanner scn;
	private LookParser lp;
	private VisionPanel vp;
	
	private BlockingDeque<String> messageStack = new LinkedBlockingDeque<String>();
	
	/**
	 * Creates an instance of this.
	 * @param args CL arguments
	 */
	public static void main(String args[]){	
		new GUIGameClient();
	}
	
	/**
	 * This constructor initiates the GUI and then sets up a NetworkClient and a scanner.
	 */
	public GUIGameClient(){
		
		//Get IP address and port.
		
		String address = JOptionPane.showInputDialog("Enter the IP address of the server to connect to:");
		
		int port = -1;
		
		while(port < 0){
			try{
				port = Integer.parseInt(JOptionPane.showInputDialog("Enter the port number for the server to connect to:"));
			}
			catch(NumberFormatException e){
				JOptionPane.showMessageDialog(this, "This port was not valid.");
			}
		}
		
		lp = new LookParser();
		
		//Setup network client.
		scn = new Scanner(System.in);
		new NetworkClient(address, 49155, this); //Give the network client a NetworkMessageListener. A
		
		//Holds all the icons.
		vp = new VisionPanel(5);
		vp.writeArr();
		
		//ActionListener for performing actions directly
		ActionListener commandAl = new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				messageStack.push("LOOK");
				messageStack.push(e.getActionCommand());
				messageStack.push("LOOK");
				
			}
			
		};
		
		//Create the navigation panel.
		//Everything will go in a border layout.
		JPanel nav = new JPanel();
		nav.setLayout(new BorderLayout());
		
		//Create buttons.
		JButton north = new JButton("N");
		JButton east = new JButton("E");
		JButton south = new JButton("S");
		JButton west = new JButton("W");
		JButton pickup = new JButton("Pickup");
		
		//Set action commands.
		north.setActionCommand("MOVE N");
		east.setActionCommand("MOVE E");
		south.setActionCommand("MOVE S");
		west.setActionCommand("MOVE W");
		pickup.setActionCommand("PICKUP");
		
		//Add action listener.
		north.addActionListener(commandAl);
		east.addActionListener(commandAl);
		south.addActionListener(commandAl);
		west.addActionListener(commandAl);
		pickup.addActionListener(commandAl);
		
		//Add everything.
		nav.add(north, BorderLayout.NORTH);
		nav.add(west, BorderLayout.WEST);
		nav.add(south, BorderLayout.SOUTH);
		nav.add(east, BorderLayout. EAST);
		nav.add(pickup, BorderLayout.CENTER);
		
		//Top box layout panel.
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(vp);
		topPanel.add(nav);
		
		//Setup this underlying main pane.
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.setSize(600, 600);
		this.setVisible(true);
		
		//Add everything to this panel.
		this.add(topPanel);
				
		messageStack.push("LOOK"); //Look and draw the map.
		
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
		try {
			//Blocks until something is available.
			return messageStack.take();
		} catch (InterruptedException e) {
			return "";
		}
	}
}
