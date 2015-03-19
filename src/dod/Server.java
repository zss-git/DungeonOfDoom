/**
 * Server class for a networked version of the game. Pretty much an adaptation of program, that sets up sockets and starts NetworkedUser threads.
 * 
 * @author Zachary Shannon
 */

package dod;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.ParseException;
import java.util.ArrayList;

import dod.game.GameLogic;

public class Server {

	public static void main(String[] args) {
		
		try {
			//Test this...
			
			String mapName = "defaultMap";
			
			if(args.length > 0){
				mapName = args[1];
			}
			
			GameLogic game = new GameLogic(mapName);
			System.out.println("Using map " + mapName);
						
			//Start up the server.
			ServerSocket listener = new ServerSocket(49155);
			
			System.out.println("Server started.");
			
			//Accept connections.
			while(true){
				Socket usrSocket = listener.accept();
				
				CommandLineUser usr = new NetworkedUser(game, usrSocket);
				System.out.println("Client connected from '" + usrSocket.getInetAddress().getHostAddress() + "'. Welcome to the server!");

				//Start the user thread.
				(new Thread(usr)).start();		
			}
			
			
		} //Error handling.
		catch (IOException e) {
			System.err.println("Error dealing with a client.");
			e.printStackTrace();
			System.exit(1);
		} //Following catch block taken from the 'Program' class.
		catch (ParseException e){
			System.err.println("Syntax error on line " + e.getErrorOffset()
				    + ":" + System.getProperty("line.separator") + e.getMessage());
			System.exit(2);
		}
	}
}
