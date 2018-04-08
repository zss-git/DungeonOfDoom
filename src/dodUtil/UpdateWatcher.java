/**
 * Represents an object interested an update message
 * 
 * @author Zachary Shannon
 * @version 24 Apr 2015
 */
package dodUtil;

public interface UpdateWatcher {
	/**
	 * Gives the listener an update message.
	 */
	public void update(int update);
}
