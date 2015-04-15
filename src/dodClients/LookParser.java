package dodClients;

public class LookParser {
	
	//Handling look replies.
	private boolean waitingForLookReplies = false;
	private int lookReplyDimensions = 0;
	private int numberLookLines = 0;
	private String lookLines;
	
	private boolean hasLook;
	
	/**
	 * Should be called by the class using the look parser every time it is given a server message to determine if it is part of a look reply.
	 * @param message Message to check.
	 * @return true if part of a look reply, false if not (and might be something else).
	 */
	public boolean isPartOfLook(String message){
		//Waits for a look.
		if(message.startsWith("LOOKREPLY")){			
			//Further messages after this one will be part of the look reply.
			
			//Reset, and wait for more.
			waitingForLookReplies = true;
			numberLookLines = 0;
			lookReplyDimensions = 0;	
			lookLines = "";
			hasLook = false;
			return true;
		}
		
		if(waitingForLookReplies){
			//Add this line to the look.
			numberLookLines++;
			lookReplyDimensions = message.length();
			lookLines = lookLines + message;
			
			//Check - have we got all the look? Note: This relies on the look being square 
			//(otherwise the server would have to send some sort of special end of look message, and we wouldn't even need this class).
			if(numberLookLines == lookReplyDimensions){
				//Let anyone who checks know we have a look, and stop waiting for more.
				waitingForLookReplies = false;
				hasLook = true;
			}
			return true;
		}
		
		return false;
	}
	
	/**
	 * Lets the class using this parser check to see if this has the look reply.
	 * @return
	 */
	public boolean hasLook(){
		return hasLook;
	}
	/**
	 * Lets the class using this parser get the returned look.
	 * @return Array of chars from the look.
	 */
	public char[][] getLook(){
		if(hasLook()){
			return createLookArr();
		}
		return null;
	}
	public int lookDimension(){
		return lookReplyDimensions;
	}
	/**
	 * Creates a char array from the lookreply.
	 * @return
	 */
	private char[][] createLookArr(){
		char[][] lookArr = new char[lookReplyDimensions][lookReplyDimensions];
		
		int i = 0;
		
		try{
		for(int rowix = 0; rowix < lookReplyDimensions; rowix++){
			for(int colix = 0; colix < lookReplyDimensions; colix++){
				lookArr[rowix][colix] = lookLines.charAt(i);
				i++;
			}
		}
		}
		catch(IndexOutOfBoundsException e){
			System.err.println("Look replies are unreadable - unable to parse.");
			return null;
		}
		
		return lookArr;
	}
	

}
