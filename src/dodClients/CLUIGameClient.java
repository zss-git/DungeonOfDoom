/**
 * Simple game client for a networked command line human user.
 * 
 * @author Zachary Shannon
 */

package dodClients;

import java.util.Scanner;

public class CLUIGameClient implements NetworkMessageListener{
	
	private Scanner scn;
	
	/**
	 * Creates new instantiation of this class.
	 * @param args
	 */
	public static void main(String[] args) {
		new CLUIGameClient();
	}
	/**
	 * Sets up a scanner and a NetworkClient.
	 */
	public CLUIGameClient(){
		scn = new Scanner(System.in);
		new NetworkClient(CLUIGameClient.getAddress(scn), CLUIGameClient.getPort(scn), this);
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
	/**
	 * For implementation of NetworkMessageListener - simply prints message to stdout.
	 */
	@Override
	public void handleMessage(String message) {
		System.out.println(message);
	}

	/**
	 * For implementation of NetworkMessageListener - simply gets message from stdin using the scanner.
	 */
	@Override
	public String getMessage() {
		return scn.nextLine();
	}
}