/**
 * Simple interface provides handling for messages.
 *  
 */

package dodClients;

public interface NetworkMessageListener {
	
	/**
	 * Deal with the message send.
	 * @param message Message to deal with.
	 */
	public void handleMessage(String message);
	
	/**
	 * Give a command when requested.
	 * @return
	 */
	public String getMessage();

}
