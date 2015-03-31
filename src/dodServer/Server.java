/**
 * Server class for a networked version of the game. Pretty much an adaptation of program, that sets up sockets and starts NetworkedUser threads.
 * 
 * @author Zachary Shannon
 */

package dodServer;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import dodServer.game.GameLogic;

public class Server implements Runnable {
	
	private GameLogic game;
	private ServerSocket connectionListener;
	private boolean acceptingConnections;
	private boolean gameStarted;
	private List<NetworkedUser> usrList;
	
	/**
	 * Creates an instance of a server object, which can be run to accept connections.
	 * @param mapName Name of map to load into the game.
	 * @param socket Socket to listen on later.
	 * @param scn Scanner to use for stdin input.
	 */
	public Server(String mapName, int socket){
		
		//Stuff that'll be needed later
		acceptingConnections = true;
		gameStarted = false;
		usrList =  Collections.synchronizedList(new ArrayList<NetworkedUser>());
		
		//Create new gamelogic and load in the map.
		try{
			game = new GameLogic(mapName);
			System.out.println("Using map " + mapName);
		}
		catch (ParseException e){
			System.err.println("Syntax error on line " + e.getErrorOffset()
				    + ":" + System.getProperty("line.separator") + e.getMessage());
			System.exit(1);
		} catch (FileNotFoundException e) {
			System.err.println("Map file not found.");
			System.exit(1);
		}
		
		//Start up the server.
		try {
		  connectionListener = new ServerSocket(socket);
		} catch (IOException e) {
			System.out.println("Failed to start server on port " + socket);
			System.exit(1);
		}
		
		startPlayerWatcher();
		startInputThread();
		
		System.out.println("Server started.");
	}
	/**
	 * Stops the server
	 * @param reason Message to give that explains why the server is stopping.
	 */
	public synchronized void stopServer(String reason){
		acceptingConnections = false;
		
		System.out.println("Trying to kill server: " + reason);
		System.out.println("Server is going down... Goodbye!");
		System.exit(0);
		
	}
	/**
	 * Thread that accepts server connections
	 */
	@Override
	public void run() {
		try {		
			//Accept connections.
			while(acceptingConnections){
				Socket usrSocket = connectionListener.accept();
				
				NetworkedUser usr = new NetworkedUser(game, usrSocket);
				System.out.println("Client connected from '" + usrSocket.getInetAddress().getHostAddress() + "'. Welcome to the server!");
				
				usrList.add(usr); //Make sure we keep track of all the users.
				
				//Start the user thread.
				(new Thread(usr)).start();		
				
				gameStarted = true;
			}			
		} //Error handling.
		catch (IOException e) {
			System.err.println("Error dealing with a client.");
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	
	/**
	 * Starts a server instance.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		Scanner setupScn = new Scanner(System.in);
		Server srv = new Server(getMapName(setupScn), 49155);
		
		(new Thread(srv)).start();
	}
	/**
	 * Gets a name of a map from the command line.
	 * @param mapScn Scanner to use to get stuff from STDIN.
	 * @return String containing name of map.
	 */
	public static String getMapName(Scanner mapScn){
		System.out.println("Enter the name of the map you would like to load:");
		String mapName = mapScn.nextLine();
		return mapName;
	}
	/**
	 * Starts a thread that watches to see if there are any players in the game. If there are no players left in the game (and the game is started), it ends the game.
	 */
	private void startPlayerWatcher(){
		(new Thread(){
			public void run(){
				while(true){
					
					int connectedUsers = 0;
					
					//Counts the number of connected users..
					synchronized(usrList){
						for(NetworkedUser curUsr : usrList){
							if(curUsr.isConnectionOpen() == true){
								connectedUsers++;
							}
						}
					}
					
					//If there are none left, ensures the game ends.
					if(gameStarted == true){
						if(connectedUsers < 1){
							stopServer("No players left in the game.");	
						}
					}
				}
			}
		}).start();
	}
	/**
	 * Starts a thread that watches for input from STDIN. Acts on that input.
	 * stop - stops server.
	 * help - lists commands.
	 */
	private void startInputThread(){
		(new Thread(){
			public void run(){
				while(true){
					Scanner scn = new Scanner(System.in);
					String input = scn.nextLine();
					input = input.toLowerCase();
					
					if(input.startsWith("help")){
						System.out.print("Available server commands: \nhelp - displays this message \nstop - stops the server");
					}
					else if(input.startsWith("stop")){
						stopServer("Requested stop from command line.");
					}
				}
			}
		}).start();
	}
}