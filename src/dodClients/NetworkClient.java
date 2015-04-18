/**
 * This class established a client - host connection and allows the client and host to exchange String messages through stdin and stdout.
 * 
 * @author Zachary Shannon
 */

package dodClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class NetworkClient {
	
	private Socket sckt;
	private BufferedReader rdr;
	private PrintWriter wrtr;
	private NetworkMessageListener listener;
	
	private boolean runInputThread = true;
	private boolean runOutputThread = true;
	
	
	/**
	 * Constructor - sets up the connection.
	 */
	public NetworkClient(String address, int port, NetworkMessageListener setListener){

		//Setup ports.
		try {
			sckt = new Socket(address, port);
			listener = setListener;
			rdr = new BufferedReader(new InputStreamReader(sckt.getInputStream()));
			wrtr = new PrintWriter(new OutputStreamWriter(sckt.getOutputStream()), true);
			
		} catch (UnknownHostException e) {
			System.err.println("Host not found.");
			System.exit(1);
		} catch (IOException e) {
			System.err.println("Error connecting to host. Are you sure the host listening is on this port?");
			System.exit(1);
		}
		
		startInputThread();
		startOutputThread();
	}
	/**
	 * Stops the client from running by ditching both the input and output threads then closing the socket.
	 */
	public void stopClient(){
		runInputThread = false;
		runOutputThread = false;
		try {
			sckt.close();
		} catch (IOException e) {
			System.err.println("Failed to close old client socket!!! Maybe it is already closed?");
		}
	}
	/**
	 * Called to start up the thread which reads from the host.
	 */
	private void startInputThread(){
		(new Thread(){
			public void run(){
				while(runInputThread){
					try {
						listener.handleMessage(rdr.readLine());
					} catch (IOException e) {
						//Something has gone wrong - this probably means we've disconnected.
						System.err.println("Error reading from the host.");
					}
				}
			}
		}).start();
	}

	/**
	 * Called to start up the thread which sends to the host.
	 */
	private void startOutputThread(){
		(new Thread(){
			public void run(){
				while(runOutputThread){
					String cmd = listener.getMessage();
					if(cmd != "" && cmd != null){
						wrtr.println(cmd);
					}
				}
			}
		}).start();
	}
}
