/**
 * A GUI for a Dungeon Of Doom server.
 * 
 * @author Zachary Shannon
 * @version 25 Apr 2015
 */
package dodServer;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import dodUtil.CommandException;

public class GUIServer extends JFrame {
	

	private static final long serialVersionUID = -200912827138901607L;
	
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
		
	}
	
	/**
	 * Gets a port, double checks it is valid - method largely borrowed from GUIGameClient.
	 * @return port specified by the user
	 */
	private int getPort() 
			throws CommandException{
		
		int port = -1;
		
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
