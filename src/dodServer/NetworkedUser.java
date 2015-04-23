/**
 * This class allows a remote client to play the game by connecting up a remote Input + Output stream.
 * Pretty much adapted by making a few changes to the HumanUser class.
 * 
 * @author Zachary Shannon
 */

package dodServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

import dodServer.game.GameLogic;

public class NetworkedUser extends CommandLineUser{
	
	
	//Client socket and client in/out streams.
	Socket client;
	InputStream in;
	OutputStream out;
	
	PrintWriter writer;
	BufferedReader reader;
	boolean connectionOpen; //For trying to close the connection elegantly.
	
	/**
	 * Constructor, sets up game
	 * @param game Instance of GameLogic this player should be associated with.
	 * @param setClient Instance of Socket where the client can be found.
	 */
	public NetworkedUser(GameLogic game, Socket setClient) {
		super(game);
		connectionOpen = true;

		client = setClient;
		try{
			in = client.getInputStream();
			writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream()), true);
			reader = new BufferedReader(new InputStreamReader(client.getInputStream()));
		}
		catch(IOException e){
			//If this breaks, something weird is going on.
			System.err.println("Failed to set up client/server connection.");
			System.exit(1);
		}
		
		addPlayer();
	}
	/**
	 *Closes this client connection and makes it so that a running instance of this thread will finish what it is doing.
	 */
	public synchronized void end(){
		connectionOpen = false;
		
		//Kill the associated player.
		removePlayer();
	}
	
    /**
     * Running thread: Attempts to get commands from the client, then runs processCommand on it.
     */
    @Override
    public void run() {
	    //Create a new buffered reader using the client input stream. 	
		while (connectionOpen) {
		    try {
				String cmd = reader.readLine(); //Get the command from the client input stream.
				processCommand(cmd);
		    }
		    catch (IOException e) {
				System.err.println("IO error with client at " + client.getInetAddress().getHostAddress() + ". Stopping client thread..");    
				break;
		    }
		    catch (RuntimeException e){
		    	System.err.println("Error with client at " + client.getInetAddress().getHostAddress() + ". Stopping client thread.");
		    	break;
		    }
		}
		
		connectionOpen = false;
		removePlayer();
		
		//If the connection isn't closed, close it.
		if(client.isClosed() == false){
			System.out.println("Closing client connection: '" +  client.getInetAddress().getHostAddress() + "'...");
			
			try {
				client.close();
			} catch (IOException e) {
				System.err.println("Couldn't remove client...?");
			}
		}
    }
    /**
     * @return Indicates whether or not the connection is still open.
     */
    public boolean isConnectionOpen(){
    	return connectionOpen;
    }

    @Override
    /**
     * Prints a message to standard out.
     * @param msg Message to send to standard out.
     */
    protected synchronized void doOutputMessage(String msg) {
    	writer.println(msg);
    }
}
