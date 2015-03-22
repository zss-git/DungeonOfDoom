/**
 * Simple game client for a networked command line human user.
 * 
 * @author Zachary Shannon
 */

package dod;

import java.util.Scanner;

public class GameClient implements NetworkMessageListener{
	
	private Scanner scn;
	private NetworkClient nc;
	
	/**
	 * Creates new instantiation of this class.
	 * @param args
	 */
	public static void main(String[] args) {
		new GameClient();
	}
	/**
	 * Sets up a scanner and a NetworkClient.
	 */
	public GameClient(){
		scn = new Scanner(System.in);
		nc = new NetworkClient(NetworkClient.getAddress(scn), NetworkClient.getPort(scn), this);
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