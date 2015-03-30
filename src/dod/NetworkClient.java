/**
 * This class established a client - host connection and allows the client and host to exchange String messages through stdin and stdout.
 * 
 * @author Zachary Shannon
 */

package dod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class NetworkClient {
	
	private Socket sckt;
	private BufferedReader rdr;
	private PrintWriter wrtr;
	private NetworkMessageListener listener;
	
	
	
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
	 * Called to start up the thread which reads from the host.
	 */
	private void startInputThread(){
		(new Thread(){
			public void run(){
				while(true){
					try {
						listener.handleMessage(rdr.readLine());
					} catch (IOException e) {
						//Something has gone wrong.
						System.err.println("Error reading from the host.");
						System.exit(1);
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
				while(true){
					String cmd = listener.getMessage();
					
					//Check for eof
					if(cmd == null){
						System.exit(0); //Exit nicely.
					}
					wrtr.println(cmd);
				}
			}
		}).start();
	}
	
	/**
	 * Static method asks stdin for a network address. No validation is provided.
	 * @return String containing a network address
	 */
	public static String getAddress(Scanner scn){
		
		System.out.println("Enter the host address:");
		String address = scn.nextLine();
		
		
		//Check for eof
		if(address == null){
			System.exit(0); //Exit nicely.
		}
		
		return address;
	}
	/**
	 * Static method asks stdin for a port number. If that 'number' is not a valid integer, asks again until it gets one that is.
	 * @return int containing a port number.
	 */
	public static int getPort(Scanner scn){
		
		System.out.println("Enter the host port:");
		String port = scn.nextLine();
		
		
		//Check for eof
		if(port == null){
			System.exit(0); //Exit nicely.
		}
		
		int val = 0;
		
		try{
			val = Integer.parseInt(port);
		}
		catch(NumberFormatException e){
			System.err.println("Could not recognise port number entered. Please try again.");
			return getPort(scn);
		}
		
		return val;
	
	}
}
