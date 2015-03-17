package dod;

import java.text.ParseException;

import dod.game.GameLogic;

/**
 * Class to handle command line arguments and initialise the correct instances.
 */
public class Program {
    /**
     * Main method, used to parse the command line arguments.
     * 
     * @param args
     *            Command line arguments
     */
    public static void main(String[] args) {

	try {
	    GameLogic game = null;
	    CommandLineUser user = null;

	    switch (args.length) {
	    case 0:
		// No Command line arguments - default map
		System.out.println("Starting Game with Default Map");

		game = new GameLogic("defaultMap");
		user = new HumanUser(game);
		break;

	    case 1:
		// Either -b or the name of the map
		if (args[0].equals("-b")) {
		    System.out.println("Starting bot game with default Map");

		    game = new GameLogic("defaultMap");
		    user = new Bot(game);
		} else {
		    // Try to load the specified map
		    System.out.println("Starting Game with Map " + args[0]);
		    game = new GameLogic(args[0]);
		    user = new HumanUser(game);
		}
		break;

	    case 2:
		// The first one needs to be -b
		if (args[0].equals("-b")) {
		    game = new GameLogic(args[1]);
		    user = new Bot(game);
		} else {
		    System.err
			    .println("The wrong number of arguments have been provided, you can either specify \"-b\" " + System.getProperty("line.separator")
				    + "to play with a bot, the name of the map you want to play on, or the \"-b\" followed " + System.getProperty("line.separator")
				    + "by the name of the map you want the bot to play on");
		}
		break;

	    default:
		System.err
			.println("The wrong number of arguments have been provided, you can either specify \"-b\"" + System.getProperty("line.separator") 
				+ "to play with a bot, the name of the map you want to play on, or the \"-b\" followed " +System.getProperty("line.separator") 
				+ "by the name of the map you want the bot to play on");
		break;
	    }

	    user.run();
	} catch (final ParseException e) {
	    System.err.println("Syntax error on line " + e.getErrorOffset()
		    + ":" + System.getProperty("line.separator") + e.getMessage());
	    System.exit(2);
	} catch (final Exception e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }
}
