package dodServer.game;

import java.io.FileNotFoundException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import dodServer.game.items.Armour;
import dodServer.game.items.GameItem;
import dodServer.game.items.Sword;

/**
 * This class controls the game logic and interaction between players. Caution:
 * not thread-safe, but will need to be made so for a networked game.
 * 
 * Synchronized some methods for network play and added a render hint method - Zachary Shannon.
 */
public class GameLogic {
    Map map;

    // Has a player won already?
    private boolean playerWon = false;

    private final List<Player> players;

    // The current player's turn, -1 indicates game not started
    private int currentPlayer = -1;

    /**
     * Constructor that specifies the map which the game should be played on.
     * 
     * @param mapFile
     *            The name of the file to load the map from.
     * @throws FileNotFoundException
     *             , ParseException
     */
    public GameLogic(String mapFile) throws FileNotFoundException,
	    ParseException {
	this.map = new Map(mapFile);

	// Check if there is enough gold to win
	if (this.map.remainingGold() < this.map.getGoal()) {
	    throw new IllegalStateException(
		    "There isn't enough gold on this map for you to win");
	}

	this.players = Collections.synchronizedList(new ArrayList<Player>());
    }

    /**
     * Adds a new player to the game.
     * 
     * @param player
     *            take a refence to the player, so messages can be sent and
     *            interactions communicated
     * 
     * @return the id of the player
     */
    public int addPlayer(PlayerListener player) {
	final int playerID = this.players.size();

	this.players.add(new Player("Player " + playerID,
		generateRandomStartLocation(), player));

	if (this.players.size() == 1) {
	    startNewGame();
	}

	return playerID;
    }

    /**
     * Removes a player from the game. The player is killed within the game, but
     * the reference is held. 
     * 
     * Made some changes to support networking - Zachary Shannon
     */
    public synchronized void removePlayer(int playerID) {
    	this.players.get(playerID).kill();
    	
		if (this.currentPlayer == playerID) {
		    // Advance turn to handle death on player's turn
		    advanceTurn(playerID);
		}
    }

    /**
     * Starts a new game of the Dungeon of Dooooooooooooom.
     */
    public synchronized void startNewGame() {
	if (this.currentPlayer != -1) {
	    throw new RuntimeException("The game has already started.");
	}

	if (this.players.size() == 0) {
	    throw new RuntimeException(
		    "The game cannot be started with a single player");
	}

	this.currentPlayer = 0;

	startTurn();
    }

    /**
     * Handles the client message HELLO
     * 
     * @param newName
     *            the name of the player
     * @return the message to be passed back to the command line
     * @throws CommandException
     */
    public void clientHello(String newName, int playerID)
	    throws CommandException {
	assertPlayerExists(playerID);

	// Change the player name and then say hello to them
	this.players.get(playerID).setName(newName);
    }

    /**
     * Handles the client message LOOK Shows the portion of the map that the
     * player can currently see.
     * 
     * 
     * @return the part of the map that the player can currently see.
     */
    public String clientLook(int playerID) {
	assertPlayerExists(playerID);

	final Player player = this.players.get(playerID);

	// Work out how far the player can see
	final int distance = player.lookDistance();

	String lookReply = "";
	// Iterate through the rows.
	for (int rowOffset = -distance; rowOffset <= distance; ++rowOffset) {
	    String line = "";

	    // Iterate through the columns.
	    for (int colOffset = -distance; colOffset <= distance; ++colOffset) {

		// Work out the location
		final Location location = player.getLocation().atOffset(
			colOffset, rowOffset);

		char content = '?';
		if (!player.canSeeTile(rowOffset, colOffset)) {
		    // It's outside the FoV so we don't know what it is.
		    content = 'X';
		} else if (!this.map.insideMap(location)) {
		    // It's outside the map, so just call it a wall.
		    content = '#';
		} else if (otherPlayerOnTile(location, playerID)) {
		    content = 'P';
		} else {
		    // Look up and see what's on the map
		    content = this.map.getMapCell(location).toChar();
		}

		// Add to the line
		line += content;
	    }

	    // Send a line of the look message
	    lookReply += line + System.getProperty("line.separator");
	}

	return lookReply;
    }
    /**
     * Handles the client message MOVE
     * 
     * Move the player in the specified direction - assuming there isn't a wall
     * in the way
     * 
     * Made some modifications to this so that it 
     * 
     * @param direction
     *            The direction (NESW) to move the player
     * @return An indicator of the success or failure of the movement.
     * @throws CommandException
     */
    public synchronized void clientMove(CompassDirection direction, int playerID)
	    throws CommandException {
	assertPlayerExists(playerID);

	ensureNoWinner();
	assertPlayersTurn(playerID);
	assertPlayerAP(playerID);

	final Player player = this.players.get(playerID);

	// Work out where the move would take the player
	final Location location = player.getLocation().atCompassDirection(
		direction);
	
	//The players current location
	final Location curLocation = player.getLocation();

	// Ensure that the movement is within the bounds of the map and not
	// into a wall
	if (!this.map.insideMap(location)
		|| !this.map.getMapCell(location).isWalkable()) {
	    throw new CommandException("can't move into a wall");
	}

	if (otherPlayerOnTile(location, playerID)) {
	    throw new CommandException("can't move into another player");
	}

	// Costs one action point
	player.decrementAp();

	// Move the player
	player.setLocation(location);
	
	notifyPlayersOfChange(location, curLocation, playerID); //Notify of changes on squares.

	advanceTurn(playerID);
	return;
    }

    /**
     * Handles the client message ATTACK
     * 
     * Edited by Zachary Shannon: Implemented attacking in network game.
     *  
     * @param direction
     *            The direction in which to attack
     * @return A message indicating the success or failure of the attack
     * @throws CommandException
     */
    public synchronized void clientAttack(CompassDirection direction, int playerID) throws CommandException {
		assertPlayerExists(playerID);
		ensureNoWinner();
		assertPlayersTurn(playerID);
		assertPlayerAP(playerID);
		
		Player thisPlayer = this.players.get(playerID);
	
		// Work out which square we're targeting
		Location location = thisPlayer.getLocation().atCompassDirection(direction);
		
		boolean someoneToAttack = false;
		Player playerToAttack = null;
		
		//Look to see if there is a player present in the specified square.
		for(Player p : players){
			if(location.equals(p.getLocation())){
				someoneToAttack = true;
				playerToAttack = p;
				break;
			}
		}
		
		if(someoneToAttack==true){
			int dmg = 1; //By default the player does 1 damage.
			Random rand = new Random();
			
			if(rand.nextInt(13) > 3){
				
				//If the player has a sword, they should do more damage.
				if(thisPlayer.hasItem(new Sword())){
					dmg++;
				}
				
				//If the enemy has armour, they should take less damage.
				if(playerToAttack.hasItem(new Armour())){
					dmg--;
				}
				
				playerToAttack.incrementHealth(-dmg);
				
				if(playerToAttack.getHp() < 1){
					playerToAttack.kill();
					playerToAttack.lose();
				}
			}
			else{
				throw new CommandException("You missed!!!");
			}
		}
		else{
			throw new CommandException("There is no one to attack here.");
		}
	    }

    /**
     * Handles the client message PICKUP. Generally it decrements AP, and gives
     * the player the item that they picked up Also removes the item from the
     * map
     * 
     * @return A message indicating the success or failure of the action of
     *         picking up.
     * @throws CommandException
     */
    public synchronized void clientPickup(int playerID) throws CommandException {
	assertPlayerExists(playerID);
	ensureNoWinner();
	assertPlayersTurn(playerID);
	assertPlayerAP(playerID);

	final Player player = this.players.get(playerID);

	final Tile playersTile = this.map.getMapCell(player.getLocation());

	// Check that there is something to pick up
	if (!playersTile.hasItem()) {
	    throw new CommandException("nothing to pick up");
	}

	// Get the item
	final GameItem item = playersTile.getItem();

	if (player.hasItem(item)) {
	    throw new CommandException("already have item");
	}

	player.giveItem(item);
	playersTile.removeItem();
	
	advanceTurn(playerID);
    }

    /**
     * Sends a message to all players of the game.
     * 
     * @param message
     *            The message to be shouted
     */
    public void clientShout(String message) {
	for (final Player player : this.players) {
	    player.sendMessage(message);
	}
    }

    /**
     * Handles the client message ENDTURN
     * 
     * Just sets the AP to zero and advances as normal.
     */
    public synchronized void clientEndTurn(int playerID) {
	assertPlayerExists(playerID);
	this.players.get(playerID).endTurn();

	// Advance to the next alive player
	do {
	    this.currentPlayer++;

	    if (this.currentPlayer >= this.players.size()) {
		this.currentPlayer = 0;
	    }
	} while (this.players.get(this.currentPlayer).isDead());

	startTurn();
    }

    /**
     * Sets the player's position. This is used as a cheating or debug command.
     * It is particularly useful for testing, as it gets rounds the randomness
     * of the player start position.
     * 
     * @param col
     *            the column of the location to put the player
     * @param row
     *            the row to location to put the player
     * @throws CommandException
     */
    public synchronized void setPlayerPosition(int col, int row, int playerID)
	    throws CommandException {
	assertPlayerExists(playerID);
	final Location location = new Location(col, row);

	if (!this.map.insideMap(location)) {
	    throw new CommandException("invalid position");
	}

	if (!this.map.getMapCell(location).isWalkable()) {
	    throw new CommandException("cannot walk on this tile");
	}

	this.players.get(playerID).setLocation(location);
    }

    /**
     * Passes the goal back
     * 
     * @return the current goal
     */
    public int getGoal() {
	return this.map.getGoal();
    }

    /**
     * Generates a randomised start location
     * 
     * @return a random location where a player can start
     */
    private Location generateRandomStartLocation() {
	if (!atLeastOneNonWallLocation()) {
	    throw new IllegalStateException(
		    "There is no free tile available for the player to be placed");
	}

	while (true) {
	    // Generate a random location
	    final Random random = new Random();
	    final int randomRow = random.nextInt(this.map.getMapHeight());
	    final int randomCol = random.nextInt(this.map.getMapWidth());

	    final Location location = new Location(randomCol, randomRow);

	    if (this.map.getMapCell(location).isWalkable()
		    && !otherPlayerOnTile(location, -1)) {
		// If it's not a wall then we can put them there
		return location;
	    }
	}
    }

    /**
     * Searches a possible tile to use by the player, i.e. non-wall. The map is
     * traversed from (0,0) to (maxY,MaxX).
     * 
     * @return true if there is at least one non-wall location, false otherwise
     */
    private boolean atLeastOneNonWallLocation() {
	for (int x = 0; x < this.map.getMapWidth(); x++) {
	    for (int y = 0; y < this.map.getMapHeight(); y++) {

		if (this.map.getMapCell(new Location(x, y)).isWalkable()) {
		    // If it's not a wall then we can put them there
		    return true;
		}
	    }
	}

	return false;
    }

    private boolean otherPlayerOnTile(Location location, int currentPlayerID) {
	for (int otherPlayerID = 0; otherPlayerID < this.players.size(); otherPlayerID++) {
	    if ((otherPlayerID != currentPlayerID)
		    && this.players.get(otherPlayerID).getLocation()
			    .equals(location)) {
		return true;
	    }
	}

	return false;
    }

    /**
     * Ensures a player has been added to the map. Otherwise, an exception is
     * raised. In a multiplayer scenario, this could ensure a player by given ID
     * exists.
     * 
     * @throws RuntimeException
     */
    private void assertPlayerExists(int playerID) throws RuntimeException {
	if ((playerID < 0) || (playerID >= this.players.size())) {
	    throw new IllegalStateException(": Player has not been added.");
	}
    }

    /**
     * Ensures a player has enough AP, otherwise a runtime error is raised,
     * since the turn should have been advanced. In a multiplayer example, this
     * is still a bug, since the server should have checked whose turn it was.
     * 
     * @throws RuntimeException
     */
    private void assertPlayerAP(int playerID) throws RuntimeException {
	if (this.players.get(playerID).remainingAp() == 0) {
	    throw new IllegalStateException("Player has 0 ap");
	}
    }

    /**
     * @throws CommandException
     * 
     */
    private void assertPlayersTurn(int playerID) throws CommandException {
	if (playerID != this.currentPlayer) {
	    throw new CommandException("not your turn");
	}
    }

    /**
     * Ensure that no player has won the game. Throws a CommandException if
     * someone has one, preventing the command from executing
     * 
     * @throws CommandException
     */
    private void ensureNoWinner() throws CommandException {
	if (this.playerWon) {
	    throw new CommandException("the game is over");
	}
    }
    /**
     * Notify all players of changes to a tile - Zachary Shannon
     * 
     * @param changedLocation The location at which the change has occurred.
     * @param secondLocation Another location at which a change may have occurred
     * @param playerID The ID of the current player.
     */
    private void notifyPlayersOfChange(Location changedLocation, int playerID){
    	notifyPlayersOfChange(changedLocation, changedLocation, playerID);
    }
    /**
     * Notify all players of changes to multiple tiles - Zachary Shannon
     * 
     * @param changedLocation The location at which the change has occurred.
     * @param secondLocation Another location at which a change may have occurred
     * @param playerID The ID of the current player.
     */
    private void notifyPlayersOfChange(Location firstLocation, Location secondLocation, int playerID){
    	//Iterate through all the players
    	for(Player p: players){
    		Location playerLocation = p.getLocation();
    		
    		//Calculate offsets.
    		int firstRowOffset = playerLocation.getRow() - firstLocation.getRow();
    		int firstColOffset = playerLocation.getCol() - firstLocation.getCol();
    		
    		int secondRowOffset = playerLocation.getRow() - secondLocation.getRow();
    		int secondColOffset = playerLocation.getCol() - secondLocation.getCol();
    		
    		//Correct column offsets.
    		if(firstColOffset > 0){
    			firstColOffset++;
    		}
    		else{
    			firstColOffset--;
    		}
    		
    		if(secondColOffset > 0){
    			secondColOffset++;
    		}
    		else{
    			secondColOffset--;
    		}
    				
    		//Can the player see the tiles?
    		if(p.canSeeTile(firstRowOffset, firstColOffset) || p.canSeeTile(secondRowOffset, secondColOffset)){
    			p.lookChange(); //Notify them.
    		}
    	}
    }
    private synchronized void startTurn() {
	this.players.get(this.currentPlayer).startTurn();
    }

    /**
     * Once a player has performed an action the game needs to move onto the
     * next turn to do this the game needs to check for a win and then test to
     * see if the current player has more AP left.
     * 
     * Note that in this implementation we currently playing this as a single
     * player game so the next turn will always be the current player so we
     * simply start their turn again.
     * 
     * @param the
     *            ID of a player
     */
    private synchronized void advanceTurn(int playerID) {
	final Player player = this.players.get(playerID);
	
		// Check if the player has won
		if ((player.getGold() >= this.map.getGoal())
			&& (this.map.getMapCell(player.getLocation()).isExit())) {
	
		    // Player should not be able to move if they have won.
		    assert (!this.playerWon);
		    
		    if(playerWon == false){
			    System.out.println("A winner was found.");
		
			    // Everyone is informed of the win.
			    Iterator<Player> playersIt = players.iterator();
			    while(playersIt.hasNext()){
				    Player p = (Player) playersIt.next();
				    p.sendMessage("Someone has won the game!!");
			    }
		    }
		    
		    this.playerWon = true;
		    player.win();
		    
		} else {
		    if ((player.remainingAp() == 0) || player.isDead()) {
		    	// Force the end of turn
		    	clientEndTurn(playerID);
		    }
		}
    }

}
