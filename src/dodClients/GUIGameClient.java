package dodClients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SpringLayout;
import javax.swing.SwingUtilities;

import dodGUI.GameInfoPanel;
import dodGUI.OutputPanel;
import dodGUI.VisionPanel;

public class GUIGameClient extends JFrame implements NetworkMessageListener{
	
	private static final long serialVersionUID = 2360372200249180419L;
	
	private LookParser lp;
	private VisionPanel vp;
	private OutputPanel messageOutput;
	private GameInfoPanel infoPanel;
	
	private NetworkClient nc;
	
	private ActionListener commandAl = new ActionListener(){
		
		public void actionPerformed(ActionEvent e) {
			messageStack.push("LOOK");
			
			String command = e.getActionCommand();
			
			if(command != null){
				messageStack.push(e.getActionCommand());
			}
			
			messageStack.push("LOOK");
		}
	}; //Simple action listener that just feeds whatever is set as the 'action command' to the stack.
	
	private BlockingDeque<String> messageStack = new LinkedBlockingDeque<String>();
	private String lastMessageHandled;
	
	/**
	 * Start new clientGUI.
	 * @param args
	 */
	public static void main(String[] args){
		
		//Running on the event dispatch thread as per the java tutorial recommendation: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		        new GUIGameClient();
		    }
		});
	}
	
	/**
	 * This constructor initiates the GUI and then sets up a NetworkClient and a scanner.
	 */
	public GUIGameClient(){
		
		//Get IP address and port.
		String address = getIPAddress();
		int port = getPort();
		
		//Start the look parser and game info panel
		lp = new LookParser();
		infoPanel = new GameInfoPanel();
		
		//Holds all the icons.
		vp = new VisionPanel(5);
		vp.writeArr();
		
		//Set up message output area.
		messageOutput = new OutputPanel();
		messageOutput.println("****Game Begins****");
		
		//Setup this underlying main pane.
		SpringLayout mainLayout = new SpringLayout();
		this.setLayout(mainLayout);
		this.setSize(600, 600);
		this.setVisible(true);
		
		JPanel topPanel = createTopPanel();
		JPanel gameActionPanel = createGameActionPanel();
		
		//Add everything to this panel.
		this.add(topPanel);
		this.add(gameActionPanel);
		this.add(messageOutput);
		this.add(infoPanel);
		
		//Set all the springs.
		//Vertically - 3 pixels apart
		mainLayout.putConstraint(SpringLayout.NORTH, topPanel, 3, SpringLayout.NORTH, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.NORTH, gameActionPanel, 3, SpringLayout.SOUTH, topPanel);
		mainLayout.putConstraint(SpringLayout.NORTH, infoPanel, 3, SpringLayout.SOUTH, gameActionPanel);
		mainLayout.putConstraint(SpringLayout.NORTH, messageOutput, 3, SpringLayout.SOUTH, infoPanel);
		mainLayout.putConstraint(SpringLayout.SOUTH, messageOutput, 3, SpringLayout.SOUTH, this.getContentPane());
		
		//Horizontally - 3 pixels from edges.
		mainLayout.putConstraint(SpringLayout.WEST, topPanel, 3, SpringLayout.WEST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.EAST, topPanel, 3, SpringLayout.EAST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.WEST, gameActionPanel, 3, SpringLayout.WEST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.EAST, gameActionPanel, 3, SpringLayout.EAST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.WEST, infoPanel, 3, SpringLayout.WEST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.EAST, infoPanel, 3, SpringLayout.EAST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.WEST, messageOutput, 3, SpringLayout.WEST, this.getContentPane());
		mainLayout.putConstraint(SpringLayout.EAST, messageOutput, 3, SpringLayout.EAST, this.getContentPane());
		
		//Connect to client.
		nc = new NetworkClient(address, port, this);
		messageOutput.println("Connected to server: " + address + ":" + port);
		
		messageStack.push("LOOK"); //Look (and so draw the map).
		
	}
	

	/**
	 * Implementation of message handling part of the NetworkMessageListener. Displays information given by the server in a meaningful way.
	 */
	@Override
	public void handleMessage(String message) {
		
		//messageOutput.println(message);
		
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
		else if(message.startsWith("MESSAGE")){
			//Informational message for the user.
			message = message + " ";
			message = message.replace("MESSAGE ", "");
			messageOutput.println(message);	
		}
		else if(message.startsWith("FAIL")){
			//Failure message for the user.
			message = message + " ";
			message = message.replace("FAIL ", "");
			messageOutput.println(message);
		}
		else if(message.startsWith("SUCCESS")){
			//Success message for the user (smarter handling).
			if(lastMessageHandled.startsWith("MOVE ") == false){ //there is no point printing success for move, as the player already gets feedback for this.
				message = message.replace("SUCCESS", "");
				messageOutput.println("Success! " + message);
			}
		}
		else if(message.startsWith("STARTTURN")){
			//Players turn begins.
			messageOutput.println("It is now your turn.");
		}
		else if(message.startsWith("GOLD")){
			//Amount of gold needed to win received.
			message = message.replace("GOLD ", "");
			infoPanel.setWinGold(parseInt(message));
		}
		else if(message.startsWith("TREASUREMOD")){
			//Modification to amount of gold.
			message = message.replace("TREASUREMOD ", "");			
			infoPanel.modifyGold(parseInt(message));
		}
		else if(message.startsWith("HITMOD")){
			//Modification to amount of health.
			message = message.replace("HITMOD ", "");			
			infoPanel.modifyHp(parseInt(message));
		}
		
		lastMessageHandled = message;

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
	
	/**
	 * Gets an IP address, double checking it is valid.
	 * @return
	 */
	private String getIPAddress(){
		
		//Ensure IP address is valid.
		String address = "";
		
		while(true){
			address = JOptionPane.showInputDialog("Enter the IP address of the server to connect to:"); //Prompt the user.
			
			//Cast to an Inetaddress and look for exceptions.
			try {
				InetAddress.getByName(address);
			} catch (UnknownHostException e) {
				JOptionPane.showMessageDialog(this, "This address was not valid.");
				continue;
			}
			
			break; //Address must be valid.
		}
		return address;
	}
	/**
	 * Gets a port, double checks it is valid.
	 * @return
	 */
	private int getPort(){
		int port = -1;
		
		while(port < 0){
			try{
				port = Integer.parseInt(JOptionPane.showInputDialog("Enter the port number for the server to connect to:"));
			}
			catch(NumberFormatException e){
				JOptionPane.showMessageDialog(this, "This port was not valid.");
			}
		}
		return port;
	}
	
	/**
	 * Creates the top part of the GUI - contains the navigation buttons and the visual representation of the map.
	 * @return The top panel.
	 */
	private JPanel createTopPanel(){
		//Create the navigation panel.
		//Everything will go in a border layout.
		JPanel nav = new JPanel();
		nav.setLayout(new BorderLayout());
		
		//Create buttons.
		JButton north = new JButton("N");
		JButton east = new JButton("E");
		JButton south = new JButton("S");
		JButton west = new JButton("W");
		JButton attackToggle = new JButton("Move Mode");

		//Set action commands.
		north.setActionCommand("MOVE N");
		east.setActionCommand("MOVE E");
		south.setActionCommand("MOVE S");
		west.setActionCommand("MOVE W");
		attackToggle.setActionCommand("movemode");
		
		//Add action listener.
		north.addActionListener(commandAl);
		east.addActionListener(commandAl);
		south.addActionListener(commandAl);
		west.addActionListener(commandAl);
		
		//Add everything.
		nav.add(north, BorderLayout.NORTH);
		nav.add(west, BorderLayout.WEST);
		nav.add(south, BorderLayout.SOUTH);
		nav.add(east, BorderLayout. EAST);
		nav.add(attackToggle, BorderLayout.CENTER);		
		
		attackToggle.setBackground(new Color(165, 196, 131));

		//Attack/Move toggle ActionListner.
		ActionListener toggleAl = new ActionListener(){
			
			public void actionPerformed(ActionEvent e){
				
				if(e.getActionCommand() == "movemode"){
					
					//Switch the N,E,S,W buttons to attack.
					attackToggle.setText("Attack Mode");
					attackToggle.setBackground(new Color(237, 107, 90));
					attackToggle.setActionCommand("attackmode");
					
					north.setActionCommand("ATTACK N");
					east.setActionCommand("ATTACK E");
					south.setActionCommand("ATTACK S");
					west.setActionCommand("ATTACK W");
				}
				else{
					//Or switch them back.
					attackToggle.setText("Move Mode");
					attackToggle.setBackground(new Color(165, 196, 131));
					attackToggle.setActionCommand("movemode");
					north.setActionCommand("MOVE N");
					east.setActionCommand("MOVE E");
					south.setActionCommand("MOVE S");
					west.setActionCommand("MOVE W");
				}
			}
		};
		
		attackToggle.addActionListener(toggleAl);
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new BoxLayout(topPanel, BoxLayout.LINE_AXIS));
		topPanel.add(vp);
		topPanel.add(nav);
		
		return topPanel;
	}
	/**
	 * Creates a jpanel containing buttons for in game actions.
	 * @return
	 */
	private JPanel createGameActionPanel(){
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		
		//Create JButtons - in game actions.
		JButton pickup = new JButton("Pickup");
		JButton endTurn = new JButton("End Turn");
		pickup.setActionCommand("PICKUP");
		endTurn.setActionCommand("ENDTURN");
		pickup.addActionListener(commandAl);
		endTurn.addActionListener(commandAl);
		
		//Action listener to quit the game.
		ActionListener quitGameAl = new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		};
		
		//Action Listener to start a new game.
		
		//Some way to refer to this JFrame:
		JFrame thisFrame = this;
		
		ActionListener newGameAl = new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				nc.stopClient();
				thisFrame.dispose();
				new GUIGameClient();
			}
		};
		
		JButton newGame = new JButton("Switch Server");
		JButton quitGame = new JButton("Quit");
		newGame.addActionListener(newGameAl);
		quitGame.addActionListener(quitGameAl);
		
		buttonPanel.add(pickup);
		buttonPanel.add(endTurn);
		buttonPanel.add(newGame);
		buttonPanel.add(quitGame);
		
		return buttonPanel;
	}
	
	/**
	 * Turns integer into string, defaults it to 0 if it fails, where that value is nonessential, this is useful.
	 * @param stringInt String to turn into an integer.
	 * @return Value of string (or, if it failed, 0).
	 */
	private int parseInt(String stringInt){
		int val = 0;
		try{
		val = Integer.parseInt(stringInt);
		}
		catch(NumberFormatException e){
		}
		catch(NullPointerException e){
			
		}
		return val;
	}
}
