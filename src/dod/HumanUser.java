package dod;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import dod.game.GameLogic;

/**
 * A class to allow a local human to play the game. A similar class could be
 * created for a network client, by obtaining commands from a network Socket and
 * relaying the messages back.
 */

public class HumanUser extends CommandLineUser {
    HumanUser(GameLogic game) {
	super(game);
	addPlayer();
    }

    /**
     * Constantly asks the user for new commands
     */
    @Override
    public void run() {
	// Set up Buffered Reader so we can get user input
	final BufferedReader br = new BufferedReader(new InputStreamReader(
		System.in));

	// Keep listening forever
	while (true) {
	    try {
		// Try to grab a command from the command line
		final String command = br.readLine();

		// Test for EOF (ctrl-D)
		if (command == null) {
		    System.exit(0);
		}

		processCommand(command);
	    } catch (final RuntimeException e) {
		// Die if something goes wrong.
		System.err.println(e.toString());
		System.exit(1);
	    } catch (final IOException e) {
		// Die if something goes wrong.
		System.err.println(e.toString());
		System.exit(1);
	    }
	}
    }

    @Override
    /**
     * Simply prints the message to the terminal 
     */
    protected void doOutputMessage(String message) {
	System.out.println(message);
    }

}
