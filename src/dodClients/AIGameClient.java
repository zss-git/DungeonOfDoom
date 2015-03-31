/**
 * A special bot proves it isn't cheating by operating as a network client with no special access to the server. Just moves randomly at the moment!
 * 
 * @author Zachary Shannon
 */

package dodClients;

import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class AIGameClient implements NetworkMessageListener{
	
	//IO client
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
	private ArrayList<String> lookReply = new ArrayList<String>();
	private boolean readingLookReply;
	private char[][] fov;
	
	
	/**
	 * Creates new instantiation of this class.
	 * @param args
	 */
	public static void main(String[] args) {
		new AIGameClient();
	}
	/**
	 * Sets up a scanner and a NetworkClient.
	 */
	public AIGameClient(){
		Scanner scn = new Scanner(System.in);
		nc = new NetworkClient(NetworkClient.getAddress(scn), NetworkClient.getPort(scn), this);
		scn.close();
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
			
			if(message.startsWith("WIN")){
				System.out.println("I've won the game! Exiting...");
				System.exit(0);
			}
			
			//Shouts
			if(message.startsWith("MESSAGE")){
				return;
			}
			
			if(message.startsWith("FAIL the game is over") || message.startsWith("WIN")){
				System.exit(0); //If the game is over, leave.
			}
			
			if(readingLookReply){
				//Add to the look reply.
				lookReply.add(message);
				if(lookReply.size() == ((lookDistance*2)+1)){
					readingLookReply = false;
					handleLookReply();
				}
				return;
			}
			
			//Attempts to handle look reply - expects a certain number of lines of look reply.
			if(message.startsWith("LOOKREPLY")){
				lookReply.clear(); //Clear the list.
				readingLookReply = true;
				return;
			}
			
			if(message.startsWith("GOLD")){
				//Set the win conditions.
				needToWin = Integer.parseInt(message.substring(5));
				System.out.println("I've discovered that I need " + needToWin + " gold to win!");
				return;
			}
			
			if(message.startsWith("STARTTURN")){
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
			System.err.println("Server seems to have closed connection - closing.");
			System.exit(1);
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
	 * Processes the look replys arraylist and makes it into a 2d array.
	 */
	private void handleLookReply(){
		System.out.println("Parsing lookreply.");
		fov = new char[((lookDistance*2)+1)][((lookDistance*2)+1)];
		
		for(int rowix = 0; rowix < lookReply.size(); rowix++){
			
			String curRow = lookReply.get(rowix);
			
			for(int colix = 0; colix < curRow.length(); colix++){
				fov[rowix][colix] = curRow.charAt(colix);
			}
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
			shouldILook = true;
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
		return fov[lookDistance][lookDistance];
	}
}
