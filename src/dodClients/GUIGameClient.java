package dodClients;

public class GUIGameClient implements NetworkMessageListener{
	
	public static void main(String args[]){
		
	}

	@Override
	public void handleMessage(String message) {
		System.out.println(message);
	}

	@Override
	public String getMessage() {
		return null;
	}
	
}
