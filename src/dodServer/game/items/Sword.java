package dodServer.game.items;

/**
 * If a player is present, having a sword makes them do 1 more damage.
 */
public class Sword extends GameItem {
    @Override
    public boolean isRetainable() {
	// A sword is retained
	return true;
    }

    @Override
    public String toString() {
	return "sword";
    }

    @Override
    public char toChar() {
	return 'S';
    }
}
