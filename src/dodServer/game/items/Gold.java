package dodServer.game.items;

/**
 * An item to represent gold
 * 
 */
public final class Gold extends GameItem {
	
	int val = 1;
	
    @Override
    public void processPickUp(GameItemConsumer player) {
	// Give the player gold
	player.addGold(val);
    }

    @Override
    public boolean isRetainable() {
	// Gold increments the player gold count instantly, so isn't
	// "retainable" like
	// other objects
	return false;
    }

    @Override
    public String toString() {
	return "gold";
    }

    @Override
    public char toChar() {
	return 'G';
    }
    
    /**
     * Changes the value of this gold
     * @param val New value of the gold to set.
     */
    public void setValue(int newVal){
    	val = newVal;
    }
    /**
     * Gets the value of this gold
     * @return Value of the gold.
     */
    public int getValue(){
    	return val;
    }
}
