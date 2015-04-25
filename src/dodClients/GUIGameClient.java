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
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.LinkedBlockingDeque;

import javax.swing.BorderFactory;
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
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import dodGUI.GameInfoPanel;
import dodGUI.ItemInfoPanel;
import dodGUI.OutputPanel;
import dodGUI.VisionPanel;
import dodUtil.CommandException;
import dodUtil.Interrupter;

public class GUIGameClient extends JFrame implements NetworkMessageListener{
	
	private static final long serialVersionUID = 2360372200249180419L;
	
	private LookParser lp;
	private VisionPanel vp;
	private GameInfoPanel infoPanel;
	private ItemInfoPanel itemPanel;
	private OutputPanel chatOutput;
	
	private NetworkClient nc;
	
	//This 'message stack' can be pushed to in order to queue up a command to be sent to the server.
	private BlockingDeque<String> messageStack = new LinkedBlockingDeque<String>(1);
	
	private String commandWaitingForResponse = ""; //Commands that are awaiting a response are put here - and cleared when the response is received.
	private boolean waitingForResponse = false; //Tells the message getting thread if it should wait.
	
	private Interrupter interrupter; //Special interrupter.
	
	private String nextMessageFrom; //Keeps track of who sent the last message.
	
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
		
		//Output chat messages here
		chatOutput = new OutputPanel();
		
		//Setup this underlying main pane.
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.setSize(600, 500);
		//this.setMinimumSize(new Dimension(700, 600));
		this.setVisible(true);
		
		this.setJMenuBar(createMenuBar());
		
		//Add top panel
		this.add(createTopPanel());
		this.add(createGameActionPanel());
		this.add(infoPanel);
		this.add(itemPanel);
		
		//Add bottom panel.
		//this.add(bottomPanel, BorderLayout.SOUTH);
		
		//Connect to client.
		try {
			nc = new NetworkClient(address, port, this);
		} catch (CommandException ce) {
			showError(ce.getMessage());
			System.exit(0);
		}
		infoPanel.println("Connected to server: " + address + ":" + port);
		
		try {
			messageStack.put("HELLO " + playerName); //Say hello
			messageStack.put("LOOK"); //Look (and so draw the map).
		} catch (InterruptedException e) {
		}
		
		//Add a window listener, that shuts down the nc on close - 
		//from http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	nc.stopClient();
		    	System.exit(0);
		    }
		});
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
		else if(message.startsWith("FROM")){
			//Next message will be from this person.
			nextMessageFrom = message.replace("FROM ", "");
		}
		else if(message.startsWith("MESSAGE")){
			//Informational message for the user.
			commandWaitingForResponse = "";
			
			message = message + " ";
			message = message.replace("MESSAGE ", "");
			chatOutput.println("<" + nextMessageFrom + ">: " +
					message);	
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
				
				infoPanel.modifyAp(-1);
				infoPanel.println("Attack hit! " + message);
			}
			else if(commandWaitingForResponse.startsWith("MOVE")){ //If responding to a move message...
				String respondDirection = commandWaitingForResponse.replace("MOVE ", "");
				commandWaitingForResponse = "";
				
				infoPanel.modifyAp(-1);
				infoPanel.println("Moved " + respondDirection);
			}
			else if(commandWaitingForResponse.startsWith("PICKUP")){
				commandWaitingForResponse = "";
				//infoPanel.modifyAp(-1);
				
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
				System.exit(0);
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
					System.exit(0);
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
		JPanel leftHandSide = new JPanel();
		leftHandSide.setLayout(new GridLayout(2,1));
		
		//Create pane for chat and nav.
		JPanel nav = navPanel();
		JPanel chat = chatPanel(chatOutput);
		leftHandSide.add(nav);
		leftHandSide.add(chat);
		
		//Create pane for vp.
		JScrollPane vpPane = new JScrollPane(vp);
		vpPane.setBorder(BorderFactory.createEmptyBorder());
		
		//Create the panel to return;
		JPanel topPanel = new JPanel();
		topPanel.setLayout(new GridLayout(1,2));
		
		topPanel.add(vpPane);
		topPanel.add(leftHandSide); //Put the visionpanel in a scrollpane.
		
		return topPanel;
	}
	
	/**
	 * Creates the navigation panel.
	 */
	private JPanel navPanel(){
		//Everything will go in a border layout.
		JPanel nav = new JPanel();
		nav.setLayout(new BorderLayout());
		
		//Create buttons.
		JButton north = new JButton("N");
		JButton east = new JButton("E");
		JButton south = new JButton("S");
		JButton west = new JButton("W");
		JButton attackToggle = new JButton("Move Mode");
		
		//Set prefered sizes.
		north.setPreferredSize(new Dimension(50, 50));
		east.setPreferredSize(new Dimension(50, 50));
		west.setPreferredSize(new Dimension(50, 50));
		south.setPreferredSize(new Dimension(50, 50));

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
		
		return nav;
	}
	
	/**
	 * Creates the chat panel
	 */
	private JPanel chatPanel(OutputPanel outputArea){
		JPanel chatPanel = new JPanel();
		chatPanel.setLayout(new BorderLayout());
		
		//Set up the input panel.
		JPanel inputPanel = new JPanel();
		inputPanel.setLayout(new GridLayout(1,2));
		
		JTextField inputArea = new JTextField(); 
		
		JButton submitButton = new JButton("Send");
	
		inputPanel.add(inputArea);;
		inputPanel.add(submitButton);
		
		ActionListener sendMessage = new ActionListener(){
			public void actionPerformed(ActionEvent e){
				if(inputArea.getText().equals("") == false){
					try {
						messageStack.put("SHOUT " + inputArea.getText());
					} catch (InterruptedException e1) {
					}
					inputArea.setText("");
				}
			}
		};
		
		//Sends the message when the button is pressed.
		submitButton.addActionListener(sendMessage);
		//Or when enter is pressed/
		inputArea.addActionListener(sendMessage);
		
		//Add these all to the chatPanel.
		chatPanel.add(outputArea, BorderLayout.CENTER);
		chatPanel.add(inputPanel, BorderLayout.SOUTH);
	
		return chatPanel;
	}
	
	/**
	 * Creates a JPanel containing buttons for in game actions.
	 * @return
	 */
	private JPanel createGameActionPanel(){
		JPanel buttonPanel = new JPanel();
		
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
		//Some way to refer to this JFrame:
		JFrame thisFrame = this;
			
		JMenuBar menuBar = new JMenuBar();
		
		//Create 'game' menu.
		JMenu game = new JMenu("Game");
		
		//Create menu items.
		JMenuItem newGame = new JMenuItem("Switch Server");
		JMenuItem aboutGame = new JMenuItem("About");
		JMenuItem quitGame = new JMenuItem("Quit");
		
		//Action Listener to start a new game.
		newGame.addActionListener(new ActionListener(){	
			public void actionPerformed(ActionEvent e) {
				nc.stopClient();
				thisFrame.dispose();
				new GUIGameClient();
			}
		});
		
		//Action listener to quit the game.
		aboutGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(thisFrame, "Play as James Gosling (father of the Java language) and race to collect the JDollars that Sun desperately needs. "
						+ "\nBut watch out for Bill Gates and his evil clones!"
						+ "\n\nA GUI client for the Dungeon Of Doom Coursework 3 server."
						+ "\nBy Zachary Shannon "
						+ "\nzs380@bath.ac.uk", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		//Action listener to quit the game.
		quitGame.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		
		game.add(newGame);
		game.add(aboutGame);
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
	 * @param msg Shows an error with this message.
	 */
	private void showError(String msg){
		JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
//	/**
//	 * @param msg Shows a warning with this message
//	 */
//	private static void showWarning(String msg){
//		JOptionPane.showMessageDialog(new JFrame(), "Warning: " + msg, "Warning", JOptionPane.WARNING_MESSAGE);
//	}
}