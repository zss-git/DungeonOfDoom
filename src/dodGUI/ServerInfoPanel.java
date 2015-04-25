package dodGUI;

import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ServerInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 5220220152032430466L;
	
	//All the labels shown here.
	private JLabel listeningLabel;
	private JLabel ipLabel;
	private JLabel portLabel;
	private JLabel infoLabel;
	
	/**
	 * Creates new ServerInfoPanel.
	 */
	public ServerInfoPanel(){
		
		//Construct labels.
		listeningLabel = new JLabel("");
		
		ipLabel = new JLabel("Loading...");
		portLabel = new JLabel("Loading...");
		infoLabel = new JLabel("");
		
		//Add everything and the spacers.
		this.add(Box.createHorizontalStrut(1));
		this.add(listeningLabel);
		this.add(Box.createHorizontalStrut(30));
		this.add(ipLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(portLabel);
		this.add(Box.createHorizontalStrut(30));
		this.add(infoLabel);
		this.add(Box.createHorizontalStrut(1));
		
		//Set preferred size.
		this.setPreferredSize(new Dimension(this.getWidth(), 20));
	}
	
	/**
	 * Sets the status of the listening label to listening.
	 */
	public void setListening(){
		listeningLabel.setText("Listening for clients");
	}
	
	/**
	 * Sets the status of the listening label to not listening.
	 */
	public void setNotListening(){
		listeningLabel.setText("Not listening for clients");
	}
	
	/**
	 * Set the IP
	 * @param newIp The new IP address to display.
	 */
	public void setIp(String newIp){
		ipLabel.setText("IP: " + newIp);
	}
	
	/**Set the port
	 * @param newPort The new port to display
	 */
	public void setPort(int newPort){
		portLabel.setText("Port: " + newPort);
	}
	
	/**
	 * Sets the info label.
	 * @param prt New value of info label.
	 */
	public void println(String prt){
		infoLabel.setText(prt);
		this.validate();
		this.repaint();
	}
}
