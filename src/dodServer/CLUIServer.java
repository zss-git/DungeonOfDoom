/**
 * A neat command line interface for a Dungeon of Doom server, that provides access to all of the servers functionality.
 * 
 * @version 23 Apr 2015
 * @author Zachary Shannon
 */

package dodServer;

import java.util.Scanner;

import dodUtil.CommandException;

public class CLUIServer {
	
	private ServerLogic srv;
	private Scanner 	scn;
	
	/**
	 * Starts a server instance.
	 * @param args Command line arguments.
	 */
	public static void main(String[] args) {
		
		new CLUIServer(new Scanner(System.in));
		
	}
	
	/**
	 * Starts an instance of the CLUI server
	 * @param setScn Scanner to use to get data from.
	 */
	public CLUIServer(Scanner setScn){
		
		scn = setScn;
		
		while(true){
			
			try {
				
				int port = getPortNumber(scn);
	
				srv = new ServerLogic(getMapName(scn), port);
				break;
					
			}
			catch (CommandException e) {
				
				println("An error occured: " + e.getMessage());
				
			}
		}
		
		//Start input thread.
		(new Thread(){
			
			public void run(){
				
				while(true){
					
					String input = scn.nextLine();
					input = input.toLowerCase();
					
					try {
						
						processCommand(input);
						
					} catch (CommandException e) {
						
						println("An error occured: " + e.getMessage());
						
					}

				}
			}
			
		}).run();
		
		println("Server started");
	}
	
	/**
	 * Processes a command and performs the matching action on the server.
	 * @param input Command to process.
	 */
	public void processCommand(String input)
			throws CommandException{
		
		String[] command = input.split(" ");
		
		if(command[0].equals("help")){
			
			//A help message.
			println("Available server commands: "
					+ "\nhelp - displays this message. "
					+ "\nport - select a new port to run the server on. Requires an argument. "
					+ "\nip - returns the IP address of this system. "
					+ "\nstart - starts listening for new clients. "
					+ "\nstop - stops listening for new clients. "
					+ "\nquit - quits this application ");
			
		}
		else if(command[0].equals("port")){
			
			int newPort;
			
			//Check the argument is there.
			if(command.length < 2){
				
				throw new CommandException("port requires an argument");
				
			}
			else if(command.length > 2){
				
				throw new CommandException("port only takes one argument");
				
			}
			
			//Handle error - is the number a number.
			try{		
				
				newPort = Integer.parseInt(command[1]);
				
			}
			catch(NumberFormatException e){
				
				throw new CommandException("the argument entered is not a number");
				
			}
			
			srv.changePort(newPort);
			println("Changed port to " + newPort);
			
		}
		else if(command[0].equals("ip")){
			
			println(srv.getIp());
			
		}
		else if(command[0].equals("start")){
			
			srv.startListening();
			println("Now listening for clients.");
			
		}
		else if(input.startsWith("stop")){
			
			srv.stopListening();
			println("Stopped listening for clients.");
			
		}
		else if(command[0].equals("quit")){
			
			srv.stopServer();
			println("Server stopped");
			System.exit(0);
			
		}

		
	}
	
	/**
	 * Gets a name of a map from the command line.
	 * @param mapScn Scanner to use to get stuff from STDIN.
	 * @return Name of map.
	 */
	private static String getMapName(Scanner mapScn){
		
		println("Enter the name of the map you would like to load:");
		String mapName = mapScn.nextLine();
		return mapName;
		
	}
	
	/**
	 * Gets a port number from the command line.
	 * @param portScn Scanner to use to get port from STDIN.
	 * @return Port number.
	 */
	private static int getPortNumber(Scanner portScn) 
			throws CommandException{
		
		int port = -1;
		
		println("Enter the port to start the server on.");
		
		try{
			
			port = Integer.parseInt(portScn.nextLine());
			
		}
		catch(NumberFormatException e){
			
			throw new CommandException("value entered was not a number");
			
		}
		
		if(port < 0 || port > 65535){
			
			throw new CommandException("port was out of range.");
			
		}
		
		return port;
		
	}
	
	/**
	 * Prints the message given.
	 * @param message Message to print
	 */
	private static void println(String message){
		
		System.out.println(message);
		
	}
}
