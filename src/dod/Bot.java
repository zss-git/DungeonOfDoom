package dod;

import java.util.Random;

import dod.game.GameLogic;

/**
 * This class plays the game as a very basic bot that will just move around
 * randomly and pick up anything that it lands on.
 * 
 * Note that there is a (very slim) chance that it will go on forever because
 * it's just moving at random. If you have a non-randomly moving bot from the
 * first piece of coursework then it might be better to use that instead of this
 * one!
 * 
 * Or of course you could just put your movement methods in place of
 * randomMove()
 * 
 * The bot extends CommandLineUser to "prove" it isn't cheating. This also means
 * that that the bot could easily be modified to work over the network.
 */
public class Bot extends CommandLineUser {
    // The bot needs to know what is has since it will act like a player on the
    // map.
    private boolean hasLantern = false;
    private boolean hasSword = false;
    private boolean hasArmour = false;

    private char[][] currentLookReply;

    /**
     * Constructs a new instance of the Bot.
     * 
     * @param game
     *            The instance of the game, to run on. This is only needed for
     *            the parent class, as the Bot sends text commands to it.
     */
    Bot(GameLogic game) {
	super(game);
	addPlayer();
    }

    /**
     * Controls the playing logic of the bot
     */
    @Override
    public void run() {
	processCommand("HELLO R2D2");

	while (true) {
	    lookAtMap();
	    pickupIfAvailable();
	    makeRandomMove();

	    // Pause
	    try {
		Thread.sleep(500);
	    } catch (final InterruptedException e) {
		// This will not happen with the current app
	    }
	}
    }

    @Override
    /**
     * Allows the bot to receive and act on messages send from the game.
     * For now, we just handle the LOOKREPLY and WIN.
     * 
     * @param the message sent by the game
     */
    protected void doOutputMessage(String message) {
	if (!message.equals("")) {
	    // Print the message for the benefit of a human observer
	    System.out.println(message);

	    final String[] lines = message.split(System
		    .getProperty("line.separator"));
	    final String firstLine = lines[0];

	    if (firstLine.equals("LOOKREPLY")) {
		handleLookReply(lines);

	    } else if (firstLine.equals("WIN")) {
		System.out.println("SHOUT I won the game");
		processCommand("SHOUT I won the game");

		System.exit(0);

	    } else if (firstLine.startsWith("FAIL")) {
		throw new RuntimeException("Bot entered invalid command");
	    }
	}
    }

    /**
     * Issues a LOOK to update what the bot can see. Returns when it is updated.
     **/
    private void lookAtMap() {
	this.currentLookReply = null;

	// Have a look at the map
	System.out.println("LOOK");
	processCommand("LOOK");

	// For now the game is not concurrent, but it's worth checking the look
	// reply has been updated. This will also help with a network, where
	// it'll take
	// a while to process.
	while (this.currentLookReply == null) {
	    // Wait
	}
    }

    /**
     * Handles the LOOKREPLY from the game, updating the bot's array.
     * 
     * @param lines
     *            the lines returned as part of the LOOKREPLY
     */
    private void handleLookReply(String[] lines) {
	if (lines.length < 2) {
	    throw new RuntimeException("FAIL: Invalid LOOKREPLY dimensions");
	}

	final int lookReplySize = lines[1].length();
	if (lines.length != lookReplySize + 1) {
	    throw new RuntimeException("FAIL: Invalid LOOKREPLY dimensions");
	}

	this.currentLookReply = new char[lookReplySize][lookReplySize];

	for (int row = 0; row < lookReplySize; row++) {
	    for (int col = 0; col < lookReplySize; col++) {
		this.currentLookReply[row][col] = lines[row + 1].charAt(col);
	    }
	}
    }

    /**
     * Picks up anything the bot is standing on, if possible
     */
    private void pickupIfAvailable() {
	switch (getCentralSquare()) {
	// We can't pick these up if we already have them, so don't even try
	case 'A':
	    if (!this.hasArmour) {
		System.out.println("PICKUP");
		processCommand("PICKUP");
		// We assume that this will be successful, but we could check
		// the reply from the game.
		this.hasArmour = true;
	    }
	    break;

	case 'L':
	    if (!this.hasLantern) {
		System.out.println("PICKUP");
		processCommand("PICKUP");
		this.hasLantern = true;
	    }
	    break;

	case 'S':
	    if (!this.hasSword) {
		System.out.println("PICKUP");
		processCommand("PICKUP");
		this.hasSword = true;

		System.out.println("SHOUT I am a killer robot now");
		processCommand("SHOUT I am a killer robot now");
	    }
	    break;

	// We'll always get some gold or health
	case 'G':
	    System.out.println("PICKUP");
	    processCommand("PICKUP");

	    System.out.println("SHOUT I got some gold");
	    processCommand("SHOUT I got some gold");
	    break;

	case 'H':
	    System.out.println("PICKUP");
	    processCommand("PICKUP");
	    break;

	default:
	    break;
	}
    }

    /**
     * Makes a random move, not into a wall
     */
    private void makeRandomMove() {
	try {
	    final char dir = generateRandomMove();
	    final String moveString = "MOVE " + dir;
	    System.out.println(moveString);
	    processCommand(moveString);

	} catch (final IllegalStateException e) {
	    System.err.println(e.getMessage());
	    System.exit(1);
	}
    }

    /**
     * Return a direction to move in. Note that we do checks to see what it in
     * the square before sending the request to move to the game logic.
     * 
     * @return direction in which to move
     */
    private char generateRandomMove() {
	// First, ensure there is a move
	if (!isMovePossible()) {
	    System.out.println("SHOUT I am stuck and so will terminate.");
	    processCommand("SHOUT I am stuck and so will terminate.");

	    throw new IllegalStateException(
		    "The bot is stuck in a dead end and cannot move anymore!");
	}

	final Random random = new Random();
	while (true) {
	    final int dir = (int) (random.nextFloat() * 4F);

	    switch (dir) {
	    case 0: // N
		if (getSquareWithOffset(0, -1) != '#') {
		    return 'N';
		}
		break;

	    case 1: // E
		if (getSquareWithOffset(1, 0) != '#') {
		    return 'E';
		}
		break;

	    case 2: // S
		if (getSquareWithOffset(0, 1) != '#') {
		    return 'S';
		}
		break;

	    case 3: // W
		if (getSquareWithOffset(-1, 0) != '#') {
		    return 'W';
		}
		break;
	    }
	}
    }

    /**
     * Obtains the square in the centre of the LOOKREPLY, i.e. that over which
     * the bot is standing
     * 
     * @return the square under the bot
     */
    private char getCentralSquare() {
	// Return the square with 0 offset
	return getSquareWithOffset(0, 0);
    }

    /**
     * Obtains a square in of the LOOKREPLY with an offset to the bot
     * 
     * @return the square corresponding to the bot and offset
     */
    private char getSquareWithOffset(int xOffset, int yOffset) {
	final int lookReplySize = this.currentLookReply.length;
	final int lookReplyCentreIndex = lookReplySize / 2; // We rely on
							    // truncation

	return this.currentLookReply[lookReplyCentreIndex + yOffset][lookReplyCentreIndex
		+ xOffset];
    }

    /**
     * Check if the there is a possible move from the centre of the vision field
     * to another tile
     * 
     * @return true if the bot is not encircled with walls, false otherwise
     */
    private boolean isMovePossible() {
	if ((getSquareWithOffset(-1, 0) != '#')
		|| (getSquareWithOffset(0, 1) != '#')
		|| (getSquareWithOffset(1, 0) != '#')
		|| (getSquareWithOffset(0, -1) != '#')) {
	    return true;
	}

	return false;
    }

}
