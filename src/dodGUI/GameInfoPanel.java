/**
 * This panel provides information about the currently running game, and provides an interface to change the displayed characters.
 * Also keeps track of ap, health and gold.
 * 
 * Note that all the default values in this class are hard coded. This is not ideal. 
 * The server tells us how much gold is needed to win, so it should (ideally) also tell us the values of these defaults, so we don't have to 'guess'
 * like we do here. I did not, however, want to make any extensions to the specification for this project.
 * 
 * Should later additions have been made, it would be trivial to capture the defaultValues and feed them to this class. In the meanwhile,
 * the default values are stored here for the sake of readability.
 * 
 * @version 18 Apr 2015 - Edit 23 Apr 2015
 * @author Zachary Shannon
 */

package dodGUI;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameInfoPanel extends JPanel {
	
	private static final long serialVersionUID = -4602944224317752612L;
	
	//Labels.
	private JLabel goldWinLabel;
	private JLabel goldLabel;
	private JLabel healthLabel;
	private JLabel apLabel;
	private JLabel infoLabel;
	
	private int goldWin;
	
	//Gold
	private final int defaultGold = 0;
	private int gold;
	
	//HP.
	private final int defaultHp = 3;
	private int hp;
	
	//AP
	private final int defaultAp = 6;
	private int ap;
	
	/**
	 * Creates a new instance.
	 */
	public GameInfoPanel(){
		
		//this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		//Make all tracked values their defaults.
		gold = defaultGold;
		hp = defaultHp;
		ap = defaultAp;
		
		goldWin = 0; //Placeholder.
		
		//Construct labels.
		goldWinLabel = new JLabel("Need: ?g");
		goldLabel = new JLabel("Have: " + gold + "g");
		healthLabel = new JLabel(hp + "hp");
		apLabel = new JLabel(ap + "ap");
		infoLabel = new JLabel("info");
		
		//Add everything and the spacers.
		this.add(Box.createHorizontalStrut(1));
		this.add(goldWinLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(goldLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(healthLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(apLabel);
		this.add(Box.createHorizontalStrut(30));
		this.add(infoLabel);
		this.add(Box.createHorizontalStrut(1));
	}
	
	/**
	 * Sets the amount of gold needed to win.
	 * @param winGold Sets the amount of gold needed to win the game.
	 */
	public void setWinGold(int goldWinSet){
		goldWin = goldWinSet;
		goldWinLabel.setText("Need: " + goldWin + "g");	
	}
	
	/**
	 * Gets how much gold needed to win.
	 * @return How much gold the player needs to win.
	 */
	public int getWinGold(){
		return goldWin;
	}
	
	/**
	 * Modifies the amount of gold possessed.
	 * @param goldMod Value to add - can be negative.
	 */
	public void modifyGold(int goldMod){
		gold = gold + goldMod;
		goldLabel.setText("Have: " + gold + "g" );
	}
	
	/**
	 * Gets gold.
	 * @return How much gold the player possesses.
	 */
	public int getGold(){
		return gold;
	}
	
	/**
	 * Modifies the amount of HP possessed.
	 * @param hpMod Value to add - can be negative.
	 */
	public void modifyHp(int hpMod){
		hp = hp + hpMod;
		healthLabel.setText(hp + "hp");
	}
	
	/**
	 * Gets hp.
	 * @return How much HP the player possesses.
	 */
	public int getHp(){
		return hp;
	}
	
	/**
	 * Modifies the amount of AP possessed.
	 * @param apMod Value to add - can be negative.
	 */
	public void modifyAp(int apMod){
		ap = ap + apMod;
		apLabel.setText(ap + "ap");
	}
	
	/**
	 * Gets ap.
	 * @return How much AP the player possesses.
	 */
	public int getAp(){
		return ap;
	}
	
	/**
	 * Resets ap to the default.
	 */
	public void resetAp(){
		ap = defaultAp;
		apLabel.setText(ap + "ap");
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
