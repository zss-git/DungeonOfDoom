/**
 * An uneditable output frame.
 * Provides a 'println' method.
 */

package dodGUI;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class OutputPanel extends JScrollPane {
	
	private static final long serialVersionUID = -6252339337057044846L;
	
	private JTextArea textArea;
	
	/**
	 * Create new instance
	 */
	public OutputPanel(){
		
		//Set up text area.
		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);
		
		//Set up scroll pane.
		this.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		this.setViewportView(textArea);
	}
	
	/**
	 * Prints specified line out to the frame. Doesn't print out null messages - use addln to add blank lines.
	 * @param ln String to print.
	 */
	public void println(String ln){
		if(ln != null){
			textArea.append(ln);
			textArea.append(System.getProperty("line.separator"));
			
			//Set caret to end of document so we get an autoscroll.
            textArea.setCaretPosition(textArea.getDocument().getLength());
		}
	}
	
	/**
	 * Adds a blank empty line.
	 */
	public void addln(){
		textArea.append(System.getProperty("line.separator"));
		
		//Set caret to end of document so we get an autoscroll.
        textArea.setCaretPosition(textArea.getDocument().getLength());
	}
}