/**
 * Provides a panel which indicates whether or not the player possessed various items.
 * 
 * @author Zachary Shannon
 * @version 18 Apr 2015
 */

package dodGUI;

import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class ItemInfoPanel extends JPanel {
	
	private static final long serialVersionUID = 6320406535814170663L;
	
	JLabel swordLabel;
	JLabel armourLabel;
	JLabel lanternLabel;

	/**
	 * Creates new instance.
	 */
	public ItemInfoPanel(){
		
		swordLabel = new JLabel("");
		armourLabel = new JLabel("");
		lanternLabel = new JLabel("");
		
		this.add(Box.createHorizontalStrut(1));
		this.add(swordLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(armourLabel);
		this.add(Box.createHorizontalStrut(1));
		this.add(lanternLabel);
		this.add(Box.createHorizontalStrut(1));
		
	}
	
	/**
	 * Indicates a sword has been picked up.
	 */
	public void gotSword(){
		swordLabel.setText("Sword ");
	}
	/**
	 * Indicates armour has been picked up.
	 */
	public void gotArmour(){
		armourLabel.setText("Armour");
	}
	/**
	 * Indicates a lantern has been picked up.
	 */
	public void gotLantern(){
		lanternLabel.setText("Lantern");
	}
}
