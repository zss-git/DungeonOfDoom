/**
 * Server class for a networked version of the Dungeon of Doom game.
 * 
 * @author Zachary Shannon
 */

package dodServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.text.ParseException;

import dodServer.game.GameLogic;
import dodUtil.CommandException;

public class ServerLogic{
	
	private GameLogic 		game;
	private ServerSocket 	connectionListener;
	private boolean 		acceptingConnections;
	
	private int socket;
	
	/**
	 * Creates an instance of a server object, which can be run to accept connections.
	 * @param mapName Name of map to load into the game.
	 * @param socket Socket to listen on later.
	 * @param scn Scanner to use for stdin input.
	 */
	public ServerLogic(String mapName, int setSocket) 
			throws CommandException{
		
		//Set the socket.
		socket = setSocket;
		
		//Create new gamelogic and load in the map.
		try{
			
			game = new GameLogic(mapName);
			
		}
		catch (ParseException e){

			throw new CommandException("Syntax error on line " + e.getErrorOffset() + ":" + System.getProperty("line.separator") + e.getMessage());
			
		} catch (FileNotFoundException e) {
			
			throw new CommandException("Map file not found.");
			
		} catch (IllegalStateException e){
			
			throw new CommandException("Entered map is not valid: " + e.getMessage());
			
		}
		
		acceptingConnections = false;
		startListening(); //Start server
		
	}
	
	/**
	 * Stops the server
	 */
	public synchronized void stopServer(){

		acceptingConnections = false;
		
	}
	
	/**
	 * Stop listening for new clients.
	 */
	public synchronized void stopListening() 
			throws CommandException{
		
		if(acceptingConnections == true){
			
			acceptingConnections = false;
			
			try {
				
				connectionListener.close();
				
			} catch (IOException e) {
				
			}
			
		}
		else{
			
			throw new CommandException("already stopped");
			
		}
		
	}
	
	/**
	 * Starts the serving for the game.
	 */
	public synchronized void startListening()
			throws CommandException{
		
		if(acceptingConnections == false){
			
			acceptingConnections = true;
			
			//Start the listener thread.
			(new Thread(){
				
				public void run(){
					
					try {

					  connectionListener = new ServerSocket(socket);
					  
					} catch (IOException e) {
						
						System.err.println("A client handling error occured.");
						
					}
							
					while(acceptingConnections){
						
						try {		
							
							//Accept connections.
							Socket usrSocket = connectionListener.accept();
							
							NetworkedUser usr = new NetworkedUser(game, usrSocket);
							
							//Start the user thread.
							(new Thread(usr)).start();
										
						} 
						catch (SocketException e){
							
							//The server has stopped listening.
							
						}
						catch (IOException e) {
							
							System.err.println("An error with a client occured.");
							
						}
					}
				}
			}).start();
			
		}
		else{
			
			throw new CommandException("already started");
			
		}
		
	}
	
	/**
	 * Changes the port the server is running on
	 */
	public synchronized void changePort(int port) 
			throws CommandException{
		
		if(isListening() == true){
			
			throw new CommandException("server is currently listening, port cannot be changed");
			
		}
		
		if(port < 0 || port > 65535){
			
			throw new CommandException("port was out of range.");
			
		}
		
		socket = port;
		
	}
	
	/**
	 * Gets the IP of this server
	 */
	public String getIp() 
			throws CommandException{
		
		try {
			
			return InetAddress.getLocalHost().getHostAddress(); //This is not 100% reliable.
			
		} catch (UnknownHostException e) {
			
			throw new CommandException("unknown host");
			
		}
		
	}
	
	/**
	 * Returns whether or not the server is listening.
	 * @return True if server is listening, otherwise false.
	 */
	public synchronized boolean isListening(){
		
		return acceptingConnections;
		
	}
}
