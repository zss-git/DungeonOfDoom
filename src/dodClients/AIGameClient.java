/**
 * A special bot proves it isn't cheating by operating as a network client with no special access to the server. Just moves randomly at the moment!
 * Coded very quickly for coursework 2.
 * Needs updating to use some of the new stuff I have learnt.
 * 
 * @author Zachary Shannon
 */

package dodClients;

import java.util.Random;
import java.util.Scanner;

import dodUtil.CommandException;

public class AIGameClient implements NetworkMessageListener{
	
	private NetworkClient nc;
	
	private String nextCommand = "LOOK";
	private boolean commandSet = false;
	private Object messageSync = new Object();
	
	//Stuff related to bot logic.
	private int needToWin;
	private boolean myTurn = false;
	private int lookDistance = 2;
	private boolean shouldILook = true;
	
	//List of items we have.
	private boolean hasLantern = false;
	private boolean hasArmour = false;
	private boolean hasSword = false;
	
	//For the 'look' command and reading it.
	private LookParser lp = new LookParser();
	private char[][] fov;
	
	
	/**
	 * Creates new instantiation of this class.
	 * @param args
	 */
	public static void main(String[] args) {
		Scanner scn = new Scanner(System.in);
		new AIGameClient(CLUIGameClient.getAddress(scn),CLUIGameClient.getPort(scn));
		scn.close();
	}
	/**
	 * Sets up a scanner and a NetworkClient.
	 */
	public AIGameClient(String address, int port){
		try {
			nc = new NetworkClient(address, port, this);
		} catch (CommandException e) {
			System.err.println(e.getMessage());
			return;
		}
	}
	/**
	 * Makes decisions based upon a message given to it. An implementation of the NetworkMessageListener interface.
	 * @param message Message given to this.
	 */
	@Override
	public void handleMessage(String message) {
		try{
			commandSet = false;
			
			//By default, prints everything out that its doing.
			System.out.println(message);
			
			//Makes decisions based on messages.
			
			//Handle looks.
			if(lp.isPartOfLook(message)){
				if(lp.hasLook()){
					fov = lp.getLook();
					
				}
				return;
			}
			else if(message.startsWith("WIN")){ //Winning
				System.out.println("I've won the game! Exiting...");
				nc.stopClient();
			}
			else if(message.startsWith("GOLD")){
				//Set the win conditions.
				needToWin = Integer.parseInt(message.substring(5));
				System.out.println("I've discovered that I need " + needToWin + " gold to win!");
				return;
			}
			else if(message.startsWith("STARTTURN")){
				myTurn = true;
				System.out.println("Bots turn has begun.");
				shouldILook = true;
	
			}
			else if(message.startsWith("ENDTURN")){
				myTurn = false;
				System.out.println("Bots turn has ended.");
				return;
			}
			
			//Code for making moves follows.
			if(myTurn){
				//If the client should look around it.
				if(shouldILook == true){
					nextCommand  = "LOOK";
					commandSet = true;
					shouldILook = false;
				}
				
				//Make a move
				if(commandSet == false){
					makeAMove();
				}
				
				//Execute any set commands.
				synchronized(messageSync){
					if(commandSet == true){
						messageSync.notify();
					}
					return; //Can't really test this does what I think it does, but it's here to ensure that this thread gives up what its doing before the message is written.
				}
			}
		}
		catch(NullPointerException e){
			System.err.println("Server seems to have closed connection.");
			e.printStackTrace();
			nc.stopClient();
		}
		
	}
	/**
	 * Implementation of the NetworkMessageListener interface. Blocks until given there is a message to send.
	 */
	@Override
	public String getMessage() {
		//Blocks here until we actually have something to return. From http://stackoverflow.com/questions/5999100/is-there-a-block-until-condition-becomes-true-function-in-java
		
		synchronized(messageSync){
			try {
				messageSync.wait();
			} catch (InterruptedException e) {
				//Do nothing.
			}
			
			//We want to print out everything we are doing.
			System.out.println(nextCommand);
			
			return nextCommand;
		}
		
	}
	/**
	 * Logic for making moves. Currently mostly random, pretty poor logic.
	 */
	private void makeAMove(){
		//Picks up items if they are consumable or we don't already have them.
		if(centralSquare() == 'A' && hasArmour == false){
			nextCommand = "PICKUP";
			commandSet = true;
			hasArmour = true;
			shouldILook = true;
			return;
		}
		else if(centralSquare() == 'L' && hasLantern == false){
			nextCommand = "PICKUP";
			commandSet = true;
			hasLantern = true;
			lookDistance = 3;
			return;
		}
		else if(centralSquare() == 'S' && hasSword == false){
			nextCommand = "PICKUP";
			commandSet = true;
			hasSword = true;
			shouldILook = true;
			return;
		}
		else if(centralSquare() == 'G'){
			nextCommand = "PICKUP";
			commandSet = true;
			shouldILook = true;
			return;
		}
		else if(centralSquare() == 'H'){
			nextCommand = "PICKUP";
			commandSet = true;
			shouldILook = true;
			return;
		}
		
		//Move randomly.
		nextCommand = randomDirection();
		commandSet = true;
		shouldILook = true; //Should look after each move. 
		return;
		
	}
	/**
	 * Wall bonk checking method - borrowed from my CW1 (and adapted slightly).
	 */
	private boolean checkForWalls(int directionToCheck){
		if(directionToCheck == 0){
			//North
			if(fov[lookDistance-1][lookDistance] == '#' || fov[lookDistance-1][lookDistance] == 'P'){
				return true;
			}
		}
		else if(directionToCheck == 1){
			//East
			if(fov[lookDistance][lookDistance+1] == '#' || fov[lookDistance][lookDistance+1] == 'P'){
				return true;
			}
		}
		else if(directionToCheck == 2){
			//South
			if(fov[lookDistance+1][lookDistance] == '#' || fov[lookDistance+1][lookDistance] == 'P'){
				return true;
			}
		}
		else{
			//West
			if(fov[lookDistance][lookDistance-1] == '#' || fov[lookDistance][lookDistance-1] == 'P'){
				return true;
			}
		}
		return false;
	}
	/**
	 * Method for getting a random direction. Borrowed from my cw1 (and adapted slightly).
	 * @return String with move to make.
	 */
	private String randomDirection(){
		Random rng = new Random();
		int direction;
		while(true){
			direction = rng.nextInt(4);
			
			if(checkForWalls(direction) == false){
				break;
			}
		}
		switch(direction){
		case 0: //N
			return "MOVE N";
		case 1: //E
			return "MOVE E";
		case 2: //S
			return "MOVE S";
		case 3: //W
			return "MOVE W";
		}
		return "";
	}
	/**
	 * Returns the central square of the fov.
	 * @return
	 */
	private char centralSquare(){
		return fov[(fov.length-1)/2][(fov[0].length-1)/2];
	}
}
