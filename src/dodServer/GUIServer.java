/**
 * A GUI for a Dungeon Of Doom server.
 * 
 * @author Zachary Shannon
 * @version 25 Apr 2015
 */
package dodServer;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import dodGUI.VisionPanel;
import dodUtil.CommandException;
import dodUtil.UpdateWatcher;

public class GUIServer extends JFrame implements UpdateWatcher {

	private static final long serialVersionUID = -200912827138901607L;
	
	private VisionPanel vp;
	
	private ServerLogic	srv;
	
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
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		
		//Create GUI stuff
		vp = new VisionPanel(1, 1, false);
		JScrollPane vpPane = new JScrollPane(vp);
		this.add(vpPane);
		this.setJMenuBar(createMenuBar());
		
		this.setLayout(new BoxLayout(this.getContentPane(), BoxLayout.PAGE_AXIS));
		this.setSize(700, 600);
		this.setMinimumSize(new Dimension(700, 600));
		
		//Start up the server object.
		while(true){
			try {				
				int port = getPort();
	
				srv = new ServerLogic(getMapName(), port);
				break;
			}
			catch (CommandException e) {				
				JOptionPane.showMessageDialog(this, "Error: " + e.getMessage());				
			}
		}
		this.update();
		
		srv.addUpdateWatcher(this);
		this.setVisible(true);
		
	}
	
	@Override
	public void update() {
		char[][] mapArr = srv.getMap();
		vp.changeSize(mapArr[0].length, mapArr.length);
		vp.writeArr(mapArr);
	}
	
	/**
	 * 
	 */
	private JMenuBar createMenuBar(){
		
		Container thisContainer = this.getContentPane(); //Allow us to refer to this container.
		
		JMenuBar menuBar = new JMenuBar();
		
		//Create menus
		JMenu application = new JMenu("Application");
		JMenu server = new JMenu("Server");
		
		//Create menu items.
		JMenuItem quit = new JMenuItem("Quit");
		
		JMenuItem start = new JMenuItem("Start Listening");
		JMenuItem stop = new JMenuItem("Stop Listening");
		
		//Add them to the appropriate menus.
		application.add(quit);
		
		server.add(start);
		server.add(stop);
		
		//Add menus to menu bar.
		menuBar.add(application);
		menuBar.add(server);
		
		//Add actionlisteners.
		quit.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				srv.stopServer();
				System.exit(0);
			}
			
		});
		
		start.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				try {
					srv.startListening();
				} catch (CommandException ce) {
					JOptionPane.showMessageDialog(thisContainer, "Error: " + ce.getMessage());
				}
			}
			
		});
		stop.addActionListener(new ActionListener(){
			
			public void actionPerformed(ActionEvent e) {
				try {
					srv.stopListening();
				} catch (CommandException ce) {
					JOptionPane.showMessageDialog(thisContainer, "Error: " + ce.getMessage());
				}
			}
			
		});
		
		return menuBar;
		
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
				System.exit(0); //Null input should mean the user wants the client to quit.
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
