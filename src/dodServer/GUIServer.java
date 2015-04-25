/**
 * A GUI for a Dungeon Of Doom server, that provides access to all of the servers functionality.
 * 
 * @author Zachary Shannon
 * @version 25 Apr 2015
 */
package dodServer;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import dodClients.AIGameClient;
import dodGUI.ServerInfoPanel;
import dodGUI.VisionPanel;
import dodServer.game.GameLogic;
import dodUtil.CommandException;
import dodUtil.ErrorListener;
import dodUtil.UpdateWatcher;

public class GUIServer extends JFrame implements UpdateWatcher, ErrorListener{

	private static final long serialVersionUID = -200912827138901607L;
	
	private VisionPanel 	vp;
	private boolean			showMap = true;
	private ServerInfoPanel infoPanel;
	
	private String 			mapName;
	private int				port;
	
	private ServerLogic		srv;
	
	
	/**
	 * Creates a new instance of this GUI.
	 * @param args command line args (not used)
	 */
	public static void main(String args[]){
		//Running on the event dispatch thread as per the java tutorial recommendation: https://docs.oracle.com/javase/tutorial/uiswing/concurrency/initial.html
		SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
		    	new GUIServer();
		    }
		});
	}
	public GUIServer(){
		
		this.setLayout(new BorderLayout());
		
		//Create GUI stuff
		vp = new VisionPanel(1, 1, false);
		JScrollPane vpPane = new JScrollPane(vp);
		vpPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(vpPane, BorderLayout.CENTER);
		
		infoPanel = new ServerInfoPanel();
		this.add(infoPanel, BorderLayout.SOUTH);
		
		this.setJMenuBar(createMenuBar());
		
		this.setSize(650, 450); 
		

		//Start up the server object.
		port = -1;
		try {				
			port = getPort();
			mapName = getMapName();
			srv = new ServerLogic(mapName, port, this);
		}
		catch (CommandException e) {				
			showWarning(e.getMessage());
			System.exit(-1);
		}
		
		this.updateMap(); //Update the representation of the map.
		
		//Sort out the status bar display.
		try {
			infoPanel.setIp(srv.getIp());
		} catch (CommandException e) {
			showWarning(e.getMessage());	
			infoPanel.setIp("error");
		}
		
		infoPanel.setPort(port);
		
		if(srv.isListening()){
			infoPanel.setListening();
		}
		else{
			infoPanel.setNotListening();
		}
		
		srv.addUpdateWatcher(this);
		this.setVisible(true);
		
		//Add a window listener, that shuts down the server on close - 
		//from http://stackoverflow.com/questions/9093448/do-something-when-the-close-button-is-clicked-on-a-jframe
		this.addWindowListener(new java.awt.event.WindowAdapter() {
		    @Override
		    public void windowClosing(java.awt.event.WindowEvent windowEvent) {
		    	System.exit(0);
		    }
		});
	}
	
	/**
	 * Called to update the representation of the map.
	 */
	@Override
	public void update(int update) {
		if(update == GameLogic.MAP_UPDATED){
			updateMap();
		}
		else if(update == GameLogic.GAME_OVER){
			JOptionPane.showMessageDialog(this, "A player won the game", "Winner found", JOptionPane.PLAIN_MESSAGE);
		}
	}
	
	/**
	 * Called when an error occurs within the server.
	 */
	@Override
	public void errorOccured(String msg) {
		
		if(msg.startsWith("Failed to start server on socket")){
			infoPanel.setNotListening();
		}
		
		showError(msg);		
	}
	
	/**
	 * Creates a menu bar for the application.
	 */
	private JMenuBar createMenuBar(){
		final JFrame thisFrame = this; //Allow us to refer to this frame.
		
		JMenuBar menuBar = new JMenuBar();
		
		//Create menus
		JMenu applicationMenu = new JMenu("Application");
		JMenu serverMenu = new JMenu("Server");
		JMenu clientsMenu = new JMenu("Clients");
		
		//Create menu items.
		final JMenuItem applicationNew = new JMenuItem("New Game");
		final JMenuItem applicationHideMap = new JMenuItem("Hide Map");
		final JMenuItem applicationAbout = new JMenuItem("About");
		final JMenuItem applicationQuit = new JMenuItem("Quit");
		
		final JMenuItem serverStart = new JMenuItem("Start Listening");
		final JMenuItem serverStop = new JMenuItem("Stop Listening");
		final JMenuItem changePort = new JMenuItem("Change port");
		
		final JMenuItem clientBot = new JMenuItem("Add Bot");
		
		//Add them to the appropriate menus.
		applicationMenu.add(applicationNew);
		applicationMenu.add(applicationHideMap);
		applicationMenu.add(applicationAbout);
		applicationMenu.add(applicationQuit);
		
		serverMenu.add(serverStart);
		serverMenu.add(serverStop);
		serverMenu.add(changePort);
		
		clientsMenu.add(clientBot);
		
		//Add menus to menu bar.
		menuBar.add(applicationMenu);
		menuBar.add(serverMenu);
		menuBar.add(clientsMenu);
		
		//Add actionlisteners.
		//Quits the application
		
		//Starts a new game
		applicationNew.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				srv.stopServer();
				thisFrame.dispose();
				new GUIServer();
			}
		});
		
		//Hides/Shows the map.
		applicationHideMap.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				if(showMap == true){
					showMap = false;
					applicationHideMap.setText("Show Map");
				}
				else{
					showMap = true;
					applicationHideMap.setText("Hide Map");
				}
				updateMap();
			}
		});
		
		//Brings up an about menu.
		applicationAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(thisFrame, "A simple GUI client for the Dungeon of Doom Server."
						+ "\nBy Zachary Shannon", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		applicationQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		
		//Starts the server listening.
		serverStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					srv.startListening();
					infoPanel.setListening();
				} catch (CommandException ce) {
					showWarning(ce.getMessage());		
				}
			}
		});
		
		//Stops the server listening
		serverStop.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					srv.stopListening();
					infoPanel.setNotListening();
				} catch (CommandException ce) {
					showWarning(ce.getMessage());		
				}
			}
		});
		
		//Changes the port
		changePort.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					int newPort = getPort();
					srv.changePort(newPort);
					infoPanel.setPort(newPort);
				} catch (CommandException ce) {
					showWarning(ce.getMessage());		
				}
			}
		});
		
		//Adds a bot to the game.
		clientBot.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				new AIGameClient("localhost", port);
			}
		});
		
		return menuBar;
	}
	
	/**
	 * Updates the map from the server.
	 */
	private void updateMap(){
		if(showMap == true){
			char[][] mapArr = srv.getMap();
			vp.changeSize(mapArr[0].length, mapArr.length);
			vp.writeArr(mapArr);
		}
		else{
			vp.writeArr();
		}
		
		this.validate();
		this.repaint();
	}
	
	/**
	 * Gets a port, double checks it is valid - method largely borrowed from GUIGameClient.
	 * @return port specified by the user
	 */
	private int getPort() 
			throws CommandException{
		int port = -1;
		
		try{
			String userInput = JOptionPane.showInputDialog("Enter the port number for this server:");
			
			if(userInput == null){
				System.exit(0);
			}
			else{
				port = Integer.parseInt(userInput);
			}
		}
		catch(NumberFormatException e){
			throw new CommandException("specified port is not in a valid format");
		}
		
		if(port < 0 || port > 65535){
			throw new CommandException("specified port is out of range");
		}
		
		return port;
	}
	
	/**
	 * Gets the path to the map from the user.
	 * @return String path to map.
	 */
	private String getMapName(){
		String name = JOptionPane.showInputDialog("Enter the path to the map to load"); //Prompt the user.
		
		if(name == null){
			System.exit(0);
		}
		return name;
	}
	
	/**
	 * @param msg Shows an error with this message.
	 */
	private void showError(String msg){
		JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * @param msg Shows a warning with this message
	 */
	private void showWarning(String msg){
		JOptionPane.showMessageDialog(this, "Warning: " + msg, "Warning", JOptionPane.WARNING_MESSAGE);
	}
}