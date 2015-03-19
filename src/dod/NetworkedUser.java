/**
 * This class allows a remote client to play the game by connecting up a remote Input + Output stream.
 * Pretty much adapted by making a few changes to the HumanUser class.
 * 
 * @author Zachary Shannon
 */

package dod;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

import dod.game.GameLogic;

public class NetworkedUser extends CommandLineUser{
	
	
	//Client socket and client in/out streams.
	Socket client;
	InputStream in;
	OutputStream out;
	
	BufferedWriter writer;
	BufferedReader reader;
	
	/**
	 * Constructor, sets up game
	 * @param game Instance of GameLogic this player should be associated with.
	 * @param setClient Instance of Socket where the client can be found.
	 */
	NetworkedUser(GameLogic game, Socket setClient) {
		super(game);

		client = setClient;
		try{
			in = client.getInputStream();
			writer = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
		}
		catch(IOException e){
			//If this breaks, something weird is going on.
			System.err.println("Failed to set up client/server connection.");
			System.exit(1);
			//Not sure if we want to die if something goes wrong here... Probably not?
		}
		
		addPlayer();
	}
	
	
    /**
     * Attempts to get commands from the client.
     */
    @Override
    public void run() {
	    //Create a new buffered reader using the client input stream. 
		
		boolean connectionOpen = true; //Mostly focused around elegantly closing the client connection.
	
		while (connectionOpen) {
		    try {
				String cmd = reader.readLine(); //Get the command from the client input stream.
				processCommand(cmd);
				
		    }
		    catch (IOException e) {
				System.err.println("Input/output error with client... Closing connection.");
				connectionOpen = false;		    
			}
		    catch (RuntimeException e){
		    	System.err.println("Error with client... Attempting to close their connection.");
		    	connectionOpen = false;
		    }
		}
		
		System.out.println("Closing client connection: '" +  client.getInetAddress().getHostAddress() + "'...");
		
		removePlayer();
				
		try {
			client.close();
		} catch (IOException e) {
			System.err.println("Couldn't remove client...?");
		}
		
		System.out.println("Successfully closed client connection!");
    }

    @Override
    /**
     * Prints a message to standard out.
     * @param msg Message to send to standard out.
     */
    protected void doOutputMessage(String msg) {
		 
		//Write the message.
		try{
			writer.write(msg);
			writer.flush();
			writer.newLine();
			writer.flush();
			
		}
		catch(IOException e){
			System.err.println("Error writing to the client.");
			System.exit(-1);
		}
    }
}
