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

import dodGUI.ServerInfoPanel;
import dodGUI.VisionPanel;
import dodUtil.CommandException;
import dodUtil.ErrorListener;
import dodUtil.UpdateWatcher;

public class GUIServer extends JFrame implements UpdateWatcher, ErrorListener{

	private static final long serialVersionUID = -200912827138901607L;
	
	private VisionPanel 	vp;
	private boolean			showMap = true;
	private ServerInfoPanel infoPanel;
	
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
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); //Exit on close.
		this.setLayout(new BorderLayout());
		
		//Create GUI stuff
		vp = new VisionPanel(1, 1, false);
		JScrollPane vpPane = new JScrollPane(vp);
		vpPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(vpPane, BorderLayout.CENTER);
		
		infoPanel = new ServerInfoPanel();
		this.add(infoPanel, BorderLayout.SOUTH);
		
		this.setJMenuBar(createMenuBar());
		
		this.setSize(450, 450); 

		//Start up the server object.
		int port = -1;
		while(true){
			try {				
				port = getPort(true);
	
				srv = new ServerLogic(getMapName(), port, this);
				break;
			}
			catch (CommandException e) {				
				JOptionPane.showMessageDialog(this, "Error: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);				
			}
		}
		
		this.update(); //Update the representation of the map.
		
		//Sort out the status bar display.
		try {
			infoPanel.setIp(srv.getIp());
		} catch (CommandException e) {
			JOptionPane.showMessageDialog(this, "Error getting ip address: " + e.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
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
	}
	
	/**
	 * Called to update the representation of the map.
	 */
	@Override
	public void update() {
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
	 * Called when an error occurs within the server.
	 */
	@Override
	public void errorOccured(String msg) {
		JOptionPane.showMessageDialog(this, "Error: " + msg, "Error", JOptionPane.ERROR_MESSAGE);
	}
	
	/**
	 * Creates a menu bar for the application.
	 */
	private JMenuBar createMenuBar(){
		JFrame thisFrame = this; //Allow us to refer to this frame.
		
		JMenuBar menuBar = new JMenuBar();
		
		//Create menus
		JMenu applicationMenu = new JMenu("Application");
		JMenu serverMenu = new JMenu("Server");
		
		//Create menu items.
		JMenuItem applicationQuit = new JMenuItem("Quit");
		JMenuItem applicationNew = new JMenuItem("New Game");
		JMenuItem applicationHideMap = new JMenuItem("Hide Map");
		JMenuItem applicationAbout = new JMenuItem("About");
		
		JMenuItem serverStart = new JMenuItem("Start Listening");
		JMenuItem serverStop = new JMenuItem("Stop Listening");
		JMenuItem changePort = new JMenuItem("Change port");
		
		//Add them to the appropriate menus.
		applicationMenu.add(applicationQuit);
		applicationMenu.add(applicationNew);
		applicationMenu.add(applicationHideMap);
		applicationMenu.add(applicationAbout);
		
		serverMenu.add(serverStart);
		serverMenu.add(serverStop);
		serverMenu.add(changePort);
		
		//Add menus to menu bar.
		menuBar.add(applicationMenu);
		menuBar.add(serverMenu);
		
		//Add actionlisteners.
		//Quits the application
		applicationQuit.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				srv.stopServer();
				System.exit(0);
			}
		});
		
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
				update();
			}
		});
		
		//Brings up an about menu.
		applicationAbout.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(thisFrame, "A simple GUI client for the Dungeon of Doom Server"
						+ "\nBy Zachary Shannon", "About", JOptionPane.PLAIN_MESSAGE);
			}
		});
		
		//Starts the server listening.
		serverStart.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					srv.startListening();
					infoPanel.setListening();
				} catch (CommandException ce) {
					JOptionPane.showMessageDialog(thisFrame, "Error: " + ce.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
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
					JOptionPane.showMessageDialog(thisFrame, "Error: " + ce.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		
		//Changes the port
		changePort.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				try {
					int newPort = getPort(false);
					srv.changePort(newPort);
					infoPanel.setPort(newPort);
				} catch (CommandException ce) {
					JOptionPane.showMessageDialog(thisFrame, "Error: " + ce.getMessage(), "Error", JOptionPane.WARNING_MESSAGE);
				}
			}
		});
		return menuBar;
	}
	
	/**
	 * Gets a port, double checks it is valid - method largely borrowed from GUIGameClient.
	 * @param quitOnCancel Whether or not to quit when the cancel button is pressed.
	 * @return port specified by the user
	 */
	private int getPort(boolean quitOnCancel) 
			throws CommandException{
		int port = -1;
		
		try{
			String userInput = JOptionPane.showInputDialog("Enter the port number for this server:");
			if(userInput == null){
				if(quitOnCancel){
					System.exit(0); //Null input should mean the user wants the client to quit.
				}
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
			System.exit(0); //Null input should mean the user wants the client to quit.
		}
		return name;
	}


}