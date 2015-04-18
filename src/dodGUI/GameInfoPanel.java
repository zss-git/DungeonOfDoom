/**
 * This panel provides information about the currently running game, and provides an interface to change the displayed characters.
 * Also keeps track of ap, health and gold.
 */

package dodGUI;

import java.awt.Color;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class GameInfoPanel extends JPanel {
	
	private static final long serialVersionUID = -4602944224317752612L;
	
	//Labels.
	private JLabel goldWinLabel;
	private JLabel goldLabel;
	private JLabel healthLabel;
	
	private int goldWin;
	
	//Gold
	private final int defaultGold = 0;
	private int gold;
	
	//HP.
	private final int defaultHp = 3;
	private int hp;
	
	/**
	 * Creates a new instance.
	 */
	public GameInfoPanel(){
		this.setLayout(new BoxLayout(this, BoxLayout.LINE_AXIS));
		
		//Make all tracked values their defaults.
		gold = defaultGold;
		hp = defaultHp;
		
		//Construct labels.
		goldWinLabel = new JLabel("Need: ?g");
		goldLabel = new JLabel("Have: " + gold + "g");
		healthLabel = new JLabel(hp + "hp");
		
		goldWinLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		goldLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		healthLabel.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
		
		this.add(goldWinLabel);
		this.add(goldLabel);
		this.add(healthLabel);
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
	 * @param hpMod Value to add - can by negative.
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
	
}
