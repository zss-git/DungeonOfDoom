/**
 * A comprehensive, functional graphical client for the Dungeon Of Doom game.
 * 
 * @version 21 Apr 2015
 * @author Zachary Shannon
 */

package dodClients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import dodGUI.GameInfoPanel;
import dodGUI.ItemInfoPanel;
import dodGUI.VisionPanel;
import dodUtil.Interrupter;

public class GUIGameClient extends JFrame implements NetworkMessageListener{
	
	private static final long serialVersionUID = 2360372200249180419L;
	
	private LookParser lp;
	private VisionPanel vp;
	private GameInfoPanel infoPanel;
	private ItemInfoPanel itemPanel;
	
	private NetworkClient nc;
	
	private ActionListener commandAl = new ActionListener(){
		
		public void actionPerformed(ActionEvent event) {
			
			if(nc.stopped() == true){
				infoPanel.println("No longer connected to server.");
				return;
			}
			
			try{
				messageStack.put("LOOK");
				
				String command = event.getActionCommand();
				
				if(command != null){
					messageStack.put(event.getActionCommand());
				}
				
				messageStack.put("LOOK");
			}
			catch(InterruptedException except){
				
			}
		}
	}; //Action listener for talking to the server.
	
	//This 'message stack' can be pushed to in order to queue up a command to be sent to the server.
	private BlockingDeque<String> messageStack = new LinkedBlockingDeque<String>(1);
	
	private String commandWaitingForResponse = ""; //Commands that are awaiting a response are put here - and cleared when the response is received.
	private boolean waitingForResponse = false; //Tells the message getting thread if it should wait.
	
	private Interrupter interrupter;
	
	/**
	 * Start new clientGUI.
	 * @param args CL arguments.
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
		
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Exit on close.
		
		//Create new interrupter and associated runnable.
		interrupter = new Interrupter(new Runnable(){
			public void run(){
				nc.stopClient();
				infoPanel.println("Command timed out.");
				waitingForResponse = false;
				commandWaitingForResponse = "";
				messageStack.pop();
			}
			
		});
		
		//Get IP address and port.
		String address = getIPAddress();
		int port = getPort();
		
		//Get the player name.
		String playerName = getPlayerName();
		
		//Start the look parser and game info panel and item info panel.
		lp = new LookParser();
		infoPanel = new GameInfoPanel();
		itemPanel = new ItemInfoPanel();
		
		//Holds all the icons.
		vp = new VisionPanel(5, 5, true);
		vp.writeArr();
		
		//Setup this underlying main pane.
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.setSize(700, 600);
		this.setMinimumSize(new Dimension(700, 600));
		this.setVisible(true);
		
		
		this.setJMenuBar(createMenuBar());
		
		//Add everything to this panel - including struts and glue.
		this.add(Box.createVerticalGlue());
		this.add(createTopPanel());
		this.add(Box.createVerticalStrut(5));
		this.add(Box.createVerticalGlue());
		this.add(createGameActionPanel());
		this.add(Box.createVerticalGlue());
		this.add(infoPanel);
		this.add(Box.createVerticalGlue());
		this.add(itemPanel);
		this.add(Box.createVerticalGlue());
		
		//Connect to client.
		nc = new NetworkClient(address, port, this);
		infoPanel.println("Connected to server: " + address + ":" + port);
		
		try {
			messageStack.put("HELLO " + playerName); //Say hello
			messageStack.put("LOOK"); //Look (and so draw the map).
		} catch (InterruptedException e) {
		}

	}
	

	/**
	 * Implementation of message handling part of the NetworkMessageListener. Displays information given by the server in a meaningful way.
	 */
	@Override
	public void handleMessage(String message) {
		
		if(message == null){
			return;
		}
		
		if(lp.isPartOfLook(message)){
			//Check if the parser is done.
			if(lp.hasLook()){
				char[][] look = lp.getLook();
				vp.changeSize(look[0].length, look.length);
				vp.writeArr(look);
				
				//Refresh UI elements and re-validate.
				vp.refresh();
				this.validate();
				this.repaint();
			}
		}
		else if(message.startsWith("MESSAGE")){
			//Informational message for the user.
			commandWaitingForResponse = "";
			
			message = message + " ";
			message = message.replace("MESSAGE ", "");
			infoPanel.println(message);	
		}
		else if(message.startsWith("FAIL")){
			//Failure message for the user.
			commandWaitingForResponse = "";
			
			message = message + " ";
			message = message.replace("FAIL ", "");
			infoPanel.println("Failed!: " + message);
		}
		else if(message.startsWith("SUCCESS")){
			//Success message for the user (smarter handling).
			message = message.replace("SUCCESS", "");
			
			//If we are responding to an attack message...
			if(commandWaitingForResponse.startsWith("ATTACK")){
				commandWaitingForResponse = "";
				
				infoPanel.println("Attack hit! " + message);
			}
			else if(commandWaitingForResponse.startsWith("MOVE")){ //If responding to a move message...
				String respondDirection = commandWaitingForResponse.replace("MOVE ", "");
				commandWaitingForResponse = "";
				
				infoPanel.println("Moved " + respondDirection);
			}
			else if(commandWaitingForResponse.startsWith("PICKUP")){
				commandWaitingForResponse = "";
				
				//Handle pickup code here.
				char[][] lookArr = lp.getLook();
				char centerChar = lookArr[(lookArr[0].length-1)/2][(lookArr[0].length-1)/2];
				
				if(centerChar == 'L'){
					infoPanel.println("Picked up lantern.");
					itemPanel.gotLantern();
				}
				else if(centerChar == 'A'){
					infoPanel.println("Picked up armour.");
					itemPanel.gotArmour();
				}
				else if(centerChar == 'S'){
					infoPanel.println("Picked up sword.");
					itemPanel.gotSword();
				}
			}
		}
		else if(message.startsWith("STARTTURN")){
			//Players turn begins.
			infoPanel.resetAp();
			infoPanel.println("It is now your turn.");
		}
		else if(message.startsWith("ENDTURN")){
			//Players turn ends.
			infoPanel.println("End of turn.");
		}
		else if(message.startsWith("HELLO")){
			commandWaitingForResponse = "";
			infoPanel.println("Hello, " + message.replaceAll("HELLO ", ""));
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
		else if(message.startsWith("WIN")){
			//The player won the game.
			JOptionPane.showMessageDialog(this, "You won!");
			infoPanel.println("You won the game!");
			
		}
		else if(message.startsWith("LOSE")){
			//The player lost the game.
			infoPanel.println("You lost..");
			
			if(infoPanel.getHp() <= 0){
				JOptionPane.showMessageDialog(this, "You died.");
			}
			else{
				JOptionPane.showMessageDialog(this, "You lost.");
			}
		}
		else if(message.startsWith("CHANGE")){
			//Look again.
			try {
				messageStack.put("LOOK");
			} catch (InterruptedException e) {
			}
		}

	}
	/**
	 * Implementation of message getting part of the NetworkMessageListener. Gives the server a command from the stack.
	 */
	@Override
	public String getMessage() {
		try {
			String lastMessage = messageStack.peekLast();
			
			if(lastMessage == null){
				lastMessage = "";
			}
			
			//If we aren't waiting for a response right now, and whatever is on the queue we should wait for, then start waiting.
			if(shouldWaitForResponse(lastMessage) && waitingForResponse == false){
				waitingForResponse = true;
				commandWaitingForResponse = lastMessage;
				
				//Schedule a timeout.
				interrupter.interruptIn(500);
				
				if(usesAp(lastMessage)){
					//Update ui.
					infoPanel.modifyAp(-1);
				}
				
				return lastMessage;		
			}
			
			//If we are waiting for a response, check to see if it has been handled. If it has, sort out the queue, then return nothing again.
			if(waitingForResponse == true){
				
				if(commandWaitingForResponse == ""){
					waitingForResponse = false;
					messageStack.pop();
					
					//Tell the client it doesn't have to self destruct, then refresh the timer.
					interrupter.cancel();
				}
				return "";
			}
			
			//Wait for a command.
			String newCommand = messageStack.take();
			
			if(usesAp(newCommand)){
				//Update ui.
				infoPanel.modifyAp(-1);
			}
			
			return newCommand;
			
			
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
			
			if(address == null){
				System.exit(0); //Null input should mean the user wants the client to quit.
			}

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
	 * Gets the name from the user.
	 */
	private String getPlayerName(){
		String name = JOptionPane.showInputDialog("Enter your name:"); //Prompt the user.
		
		if(name == null){
			name = "player"; //Null input means we can just use some random name.
		}
		
		return name;
	}
	/**
	 * Gets a port, double checks it is valid.
	 * @return port specified by user
	 */
	private int getPort(){
		int port = -1;
		
		while(port < 0 || port > 65535){
			try{
				String userInput = JOptionPane.showInputDialog("Enter the port number for the server to connect to:");
				if(userInput == null){
					System.exit(0); //Null input should mean the user wants the client to quit.
				}
				else{
					port = Integer.parseInt(userInput);
				}
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
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(new JScrollPane(vp)); //Put the visionpanel in a scrollpane.
		topPanel.add(Box.createHorizontalGlue());
		topPanel.add(nav);
		topPanel.add(Box.createHorizontalGlue());
		
		return topPanel;
	}
	/**
	 * Creates a JPanel containing buttons for in game actions.
	 * @return
	 */
	private JPanel createGameActionPanel(){
		JPanel buttonPanel = new JPanel();
		//buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		
		//Create JButtons - in game actions.
		JButton pickup = new JButton("Pickup");
		JButton endTurn = new JButton("End Turn");
		pickup.setActionCommand("PICKUP");
		endTurn.setActionCommand("ENDTURN");
		pickup.addActionListener(commandAl);
		endTurn.addActionListener(commandAl);
		
		this.add(Box.createHorizontalStrut(1));
		buttonPanel.add(pickup);
		this.add(Box.createHorizontalStrut(1));
		buttonPanel.add(endTurn);
		this.add(Box.createHorizontalStrut(1));
		
		return buttonPanel;
	}
	
	/**
	 * Creates the menu bar for this gui.
	 * @return Menu bar created.
	 */
	private JMenuBar createMenuBar(){
		JMenuBar menuBar = new JMenuBar();
		
		//Create 'game' menu.
		JMenu game = new JMenu("Game");
		
		//Create menu items.
		JMenuItem newGame = new JMenuItem("Switch Server");
		JMenuItem quitGame = new JMenuItem("Quit");
		
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
		
		newGame.addActionListener(newGameAl);
		quitGame.addActionListener(quitGameAl);
		
		game.add(newGame);
		game.add(quitGame);
		menuBar.add(game);
		
		
		return menuBar;
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
	/**
	 * Given a server command, returns whether or not the client should wait for a response for that command.
	 * @param command Command to check.
	 * @return True if should wait for response, false otherwise.
	 */
	private boolean shouldWaitForResponse(String command){
		if(command.startsWith("ATTACK") || command.startsWith("MOVE") || command.startsWith("PICKUP") || command.startsWith("HELLO")){
			return true;
		}
		else{
			return false;
		}
	}
	/**
	 * Given a server command, returns whether or not that command uses ap.
	 * @param command Command to check.
	 * @return True if uses ap, false otherwise.
	 */
	private boolean usesAp(String command){
		if(command.startsWith("ATTACK") || command.startsWith("MOVE") || command.startsWith("PICKUP")){
			return true;
		}
		else{
			return false;
		}
	}
}