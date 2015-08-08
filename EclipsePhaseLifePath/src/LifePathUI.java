import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 * 
 */

/**
 * @author Vigilant
 *
 */
public class LifePathUI implements UI {

	 final static boolean shouldWeightX = true;
	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 private GridBagConstraints cons;
	 private BorderLayout windowLayout;
	 private GridBagLayout layout,secondaryLayout, statLayout;
	 private JFrame mainWindow;
	 private JPanel mainPanel;
	 private JTextArea mainStatus;
	 private JPanel sideBar;
	 private Character currChar;
	 
	 private GridBagLayout currentLayout;
	 private JComponent currentComponent;
	
	 private HashMap<String,JComponent> mappedComponents;
	
	/**
	 * @throws HeadlessException
	 */
	public LifePathUI() throws HeadlessException {
		this.dataInit();
		cons = new GridBagConstraints();
		windowLayout = new BorderLayout();
        layout = new GridBagLayout();
        secondaryLayout = new GridBagLayout();
        statLayout = new GridBagLayout();
        mappedComponents = new HashMap<String,JComponent>(); 
        mainWindow = new JFrame();     
        mainPanel = new JPanel();
        
        mainWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	}

	/* (non-Javadoc)
	 * @see UI#promptUser(java.lang.String, java.lang.String)
	 */
	@Override
	public String promptUser(String message, String extraContext) {
		// TODO Auto-generated method stub
		String inputValue = JOptionPane.showInputDialog(message + "\n" + extraContext); 
		return inputValue;
	}

	/* (non-Javadoc)
	 * @see UI#handleError(java.lang.String)
	 */
	@Override
	public boolean handleError(String message) {
		int resp = JOptionPane.showConfirmDialog(null,"Error Resulted, redo step?", "Error", JOptionPane.YES_NO_OPTION);
		
		if (resp == JOptionPane.YES_OPTION)
		{
			return true;
		}
		else			
		{
			return false;
		}		
	}

	/* (non-Javadoc)
	 * @see UI#statusUpdate(java.lang.String)
	 */
	@Override
	public void statusUpdate(String message) {
		// TODO Auto-generated method stub
		this.appendStatusText(message);
	}

	/* (non-Javadoc)
	 * @see UI#end()
	 */
	@Override
	public void end() {
		// Marks the character gen process as stopped, disabling the buttons that used to advance it
	}
	
	/**
	 * Creates the backend code, the character class
	 */
	private void dataInit()
	{
		DataProc.init("LifepathPackages.dat","internalInfo.dat");
		Character aChar = new Character("");
		
		aChar.addSkill(Skill.CreateSkill("Academics", 10));
		aChar.addSkill(Skill.CreateSkill("Animal Handling", 10));
		aChar.addSkill(Skill.CreateSkill("Art", 10));
		aChar.addSkill(Skill.CreateSkill("Beam Weapons", 10));
		aChar.addSkill(Skill.CreateSkill("Blades", 10));
		aChar.addSkill(Skill.CreateSkill("Climbing", 10));
		aChar.addSkill(Skill.CreateSkill("Clubs", 10));
		aChar.addSkill(Skill.CreateSkill("Control", 10));
		aChar.addSkill(Skill.CreateSkill("Deception", 10));
		aChar.addSkill(Skill.CreateSkill("Demolitions", 10));
		aChar.addSkill(Skill.CreateSkill("Disguise", 10));
		aChar.addSkill(Skill.CreateSkill("Exotic Language", 10));
		aChar.addSkill(Skill.CreateSkill("Exotic Melee Weapon", 10));
		aChar.addSkill(Skill.CreateSkill("Exotic Ranged Weapon", 10));
		aChar.addSkill(Skill.CreateSkill("Flight", 10));
		aChar.addSkill(Skill.CreateSkill("Fray", 10));
		aChar.addSkill(Skill.CreateSkill("Free Fall", 10));
		aChar.addSkill(Skill.CreateSkill("Freerunning", 10));
		aChar.addSkill(Skill.CreateSkill("Gunnery", 10));
		aChar.addSkill(Skill.CreateSkill("Hardware", 10));
		aChar.addSkill(Skill.CreateSkill("Impersonation", 10));
		aChar.addSkill(Skill.CreateSkill("Infiltration", 10));
		aChar.addSkill(Skill.CreateSkill("Infosec", 10));
		aChar.addSkill(Skill.CreateSkill("Interest", 10));
		aChar.addSkill(Skill.CreateSkill("Interfacing", 10));
		aChar.addSkill(Skill.CreateSkill("Intimidation", 10));
		aChar.addSkill(Skill.CreateSkill("Investigation", 10));
		aChar.addSkill(Skill.CreateSkill("Kinesics", 10));
		aChar.addSkill(Skill.CreateSkill("Kinetic Weapons", 10));
		aChar.addSkill(Skill.CreateSkill("Language", 10));
		aChar.addSkill(Skill.CreateSkill("Medicine", 10));
		aChar.addSkill(Skill.CreateSkill("Navigation", 10));
		aChar.addSkill(Skill.CreateSkill("Networking", 10));
		aChar.addSkill(Skill.CreateSkill("Palming", 10));
		aChar.addSkill(Skill.CreateSkill("Perception", 10));
		aChar.addSkill(Skill.CreateSkill("Persuasion", 10));
		aChar.addSkill(Skill.CreateSkill("Pilot", 10));
		aChar.addSkill(Skill.CreateSkill("Profession", 10));
		aChar.addSkill(Skill.CreateSkill("Programming", 10));
		aChar.addSkill(Skill.CreateSkill("Protocol", 10));
		aChar.addSkill(Skill.CreateSkill("Psi Assault", 10));
		aChar.addSkill(Skill.CreateSkill("Psychosurgery", 10));
		aChar.addSkill(Skill.CreateSkill("Research", 10));
		aChar.addSkill(Skill.CreateSkill("Scrounging", 10));
		aChar.addSkill(Skill.CreateSkill("Seeker Weapons", 10));
		aChar.addSkill(Skill.CreateSkill("Sense", 10));
		aChar.addSkill(Skill.CreateSkill("Spray Weapons", 10));
		aChar.addSkill(Skill.CreateSkill("Swimming", 10));
		aChar.addSkill(Skill.CreateSkill("Throwing Weapons", 10));
		aChar.addSkill(Skill.CreateSkill("Unarmed Combat", 10));
		
		this.currChar = aChar;
	}
	
	public void init()
	{
        mainWindow.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        
        mainWindow.setLayout(windowLayout);
        

		cons.ipadx = 5;
		cons.ipady = 5;
		cons.anchor = GridBagConstraints.NORTHWEST;
		cons.fill = GridBagConstraints.NONE;
		cons.weighty = 1.0;
		cons.weightx = 1.0;
		
		// to make everything work right we add a mainPanel under the mainWindow
		cons.gridheight = 1;
		cons.gridwidth = 1;
		
		mainWindow.add(mainPanel);
		// Readers keep in mind if you don't set this below two lines up, strange behavior happens
		mainPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        mainPanel.setLayout(layout);
		
		currentComponent = mainPanel;
	    currentLayout = layout;
		
		int x = 0, y = 0;
		
		addMappedTF(0,0,"Character Name",30,true);
		addMappedFixedTF(2,0,"Morph","",10,true);
		addMappedFixedTF(4,0,"Background","",10,true);
		endRow(6,0);
		
		cons.gridheight = GridBagConstraints.REMAINDER;
		
		// init new Panel
		sideBar = new JPanel();
		sideBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        sideBar.setLayout(secondaryLayout);
		
		addC(sideBar,14,0);
		
		cons.gridheight = 1;
		
		
		JPanel statPanel = new JPanel();
		statPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        statPanel.setLayout(statLayout);
		
		cons.gridwidth = GridBagConstraints.RELATIVE;
		cons.gridheight = 3;
		addC(statPanel,0,1);
		
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		currentLayout = statLayout;
		currentComponent = statPanel;
		
		// these will need their own panel, using the new currentLayout, currentComponent system
		
		addMappedFixedTF(0,0,"Base COG", ""+currChar.getAptitude("COG"),5,true);
		addMappedFixedTF(2,0,"Base COO", ""+currChar.getAptitude("COO"),5,true);
		addMappedFixedTF(4,0,"Base INT", ""+currChar.getAptitude("INT"),5,true);
		addMappedFixedTF(6,0,"Base REF", ""+currChar.getAptitude("REF"),5,true);
		addMappedFixedTF(8,0,"Base SAV", ""+currChar.getAptitude("SAV"),5,true);
		addMappedFixedTF(10,0,"Base SOM", ""+currChar.getAptitude("SOM"),5,true);
		addMappedFixedTF(12,0,"Base WIL", ""+currChar.getAptitude("WIL"),5,true);
		endRow(14,0);
		
		addLabel(0,1,"Bonus");
		this.mappedComponents.put("MorphBonusCOG", addTextF(1,1,5,true));
		addLabel(2,1,"Bonus");
		this.mappedComponents.put("MorphBonusCOO", addTextF(3,1,5,true));
		addLabel(4,1,"Bonus");
		this.mappedComponents.put("MorphBonusINT", addTextF(5,1,5,true));
		addLabel(6,1,"Bonus");
		this.mappedComponents.put("MorphBonusREF", addTextF(7,1,5,true));
		addLabel(8,1,"Bonus");
		this.mappedComponents.put("MorphBonusSAV", addTextF(9,1,5,true));
		addLabel(10,1,"Bonus");
		this.mappedComponents.put("MorphBonusSOM", addTextF(11,1,5,true));
		addLabel(12,1,"Bonus");
		this.mappedComponents.put("MorphBonusWIL", addTextF(13,1,5,true));
		endRow(14,1);
		
		addMappedFixedTF(0,2,"Total COG", "",5,true);
		addMappedFixedTF(2,2,"Total COO", "",5,true);
		addMappedFixedTF(4,2,"Total INT", "",5,true);
		addMappedFixedTF(6,2,"Total REF", "",5,true);
		addMappedFixedTF(8,2,"Total SAV", "",5,true);
		addMappedFixedTF(10,2,"Total SOM", "",5,true);
		addMappedFixedTF(12,2,"Total WIL", "",5,true);
		endRow(14,2);
		
		
		// reset to normal dimensions and panel targets
		currentLayout = layout;
		currentComponent = mainPanel;
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		// create the main status window
		cons.gridwidth = 13;
		cons.gridheight = 14;
		cons.fill = GridBagConstraints.BOTH;
		
		
		mainStatus = new JTextArea(40,0);
		mainStatus.setMinimumSize(mainStatus.getPreferredSize());
		JScrollPane tempPane = new JScrollPane(mainStatus);
		tempPane.setMinimumSize(tempPane.getPreferredSize());
		
		addC(tempPane,0,4);

		// reset to normal dimensions
		cons.fill = GridBagConstraints.NONE;
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		// gives a quick export of the character
		addButton(4,25,"Export to Txt").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                update();             
                
                JTextArea updateArea= new JTextArea(currChar.toString() + DIVIDER_STRING + mainStatus.getText(),10,120);              
                updateArea.setEditable(true);
                updateArea.setLineWrap(true);
                JOptionPane.showMessageDialog(null, updateArea,"Exported Character", JOptionPane.PLAIN_MESSAGE);
                
            }	
		});
		
		endVertical(0,26);
		
		this.update();
		
		mainWindow.setSize(1400, 900);

		mainWindow.setVisible(true);
	}
	
	/**
	 * Inserts a spacer to signify the end of components for this row (effects layout so it doesn't center)
	 * @param x nonzero int
	 * @param y nonzero int
	 */
	private void endRow(int x, int y)
	{
		cons.weightx = 100.0;
		addLabel(x,y,"");
		cons.weightx = 1.0;
	}
	
	/**
	 * Inserts a spacer to signify the end of components vertically (effects layout so it doesn't center)
	 * @param x nonzero int
	 * @param y nonzero int
	 */
	private void endVertical(int x, int y)
	{ 
		cons.weighty = 100.0;
		addLabel(x,y,"");
		cons.weighty = 1.0;
	}
	
	/**
	 * Adds a JLabel JTextField pair (each weight 1 ), and adds the textField to the mappedComponents list
	 * using the labelText for a key
	 * 
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param updateListen If true will add listener that triggers UI updates when values change
	 * @param cols number of cols for the text field
	 */
	private void addMappedTF(int x, int y, String labelText, int cols, boolean updateListen)
	{
		addLabel(x,y,labelText);
		this.mappedComponents.put(labelText, addTextF(x+1,y,cols,updateListen));
	}
	
	/**
	 * Adds a JLabel JTextField pair (each weight 1 ), and adds the textField to the mappedComponents list
	 * using the labelText for a key. The text field is not edittable
	 * 
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param labelText Label to display
	 * @param value The text to display in the field
	 * @param cols number of cols for the text field
	 * @param horiztonal Whether the pair is arranged horizontally (vertically if false)
	 */
	private void addMappedFixedTF(int x, int y, String labelText, String value, int cols, boolean horizontal)
	{
		int newX,newY;
		
		if (horizontal)
		{
			newX = x+1;
			newY = y;
		}
		else
		{
			newX = x;
			newY = y+1;
		}
		
		addLabel(x,y,labelText);
		JTextField temp = addTextF(newX,newY,value,cols,false);
		temp.setEditable(false);
		this.mappedComponents.put(labelText, temp);
	}
	
	/**
	 * Shorthand command to add label at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for label
	 */
	private JLabel addLabel(int x, int y, String text)
	{
		cons.gridx = x;
		cons.gridy = y;
		JLabel temp = new JLabel(text);
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Button at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for Button
	 * @return The component created
	 */
	private JButton addButton(int x, int y, String text)
	{
		cons.gridx = x;
		cons.gridy = y;
		JButton temp = new JButton(text);
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Field at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param value the default value for the text field
	 * @param cols Number of Columns
	 * @param updateListen If true will add listener that triggers UI updates when values change
	 * @return The component created
	 */
	private JTextField addTextF(int x, int y, String value, int cols, boolean updateListen)
	{
		cons.gridx = x;
		cons.gridy = y;
		JTextField temp = new JTextField(value, cols);
		
		// this prevents the common issue of the text fields turning into slits
		temp.setMinimumSize(temp.getPreferredSize());

		if (updateListen)
		{
			temp.getDocument().addDocumentListener(new TextChangeListener());
		}
		
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand command to add Text Field at coordinates with text. No value set in this version
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param cols Number of Columns
	 * @param updateListen If true will add listener that triggers UI updates when values change
	 * @return The component created
	 */
	private JTextField addTextF(int x, int y, int cols, boolean updateListen)
	{
		return this.addTextF(x, y, "", cols, updateListen);
	}
	
	/**
	 * Shorthand command to add Text Area at coordinates with text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param rows Number of Rows
	 * @param cols Number of Columns
	 * @return The component created
	 */
	private JTextArea addTextArea(int x, int y, int rows, int cols)
	{
		cons.gridx = x;
		cons.gridy = y;
		JTextArea temp = new JTextArea(rows,cols);
		addC(temp);
		return temp;
	}
	
	/**
	 * Shorthand to add new components to the UI tab
	 * @param comp Component to add to UI
	 */
	private void addC(JComponent comp) {
		currentComponent.add(comp,cons);
	}
	
	/**
	 * Shorthand to add new components to the UI tab at given coords
	 * @param comp Component to add to UI
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * 
	 */
	private void addC(JComponent comp, int x, int y) {
		cons.gridx = x;
		cons.gridy = y;
		currentComponent.add(comp,cons);
	}
	
	/**
	 * Helper method, searches mappedComponents for a JTextField with name, and returns it properly cast to JTextField
	 * 
	 * @param name Valid name 
	 * @return The JTextField of matching name, or null if no matching JTextField exists
	 */
	private JTextField getTextF(String name)
	{
		// does it exist?
		if (!this.mappedComponents.containsKey(name) )
		{
			return null;
		}
		else
		{
			
			JComponent temp = this.mappedComponents.get(name);
			
			// is it the right type?
			if ( !temp.getClass().getSimpleName().equalsIgnoreCase("jtextfield") )
			{
				return null;
			}
			else
			{
				return (JTextField)temp;
			}
			
		}
		
	}
	
	private int getTextFVal(String name)
	{
		JTextField temp = getTextF(name);
		
		String text = "";
		
		if (temp != null && temp.getText().length() > 0)
		{
			text = temp.getText();
		}
		
		// does it exist and is it a number
		if (temp == null || !Utils.isInteger(temp.getText()))
		{
			return 0;
		}
		else
		{
			return Integer.parseInt(temp.getText());
		}
	}
	
	/**
	 * Adds text to the end of the status text area
	 * @param text Text to be added
	 */
	private void appendStatusText(String text)
	{
		this.mainStatus.setText(this.mainStatus.getText() + "\n\n" + text);
	}
	
	/**
	 * Updates all relevant display fields for the character
	 */
	private void update()
	{
		currChar.setName(getTextF("Character Name").getText());
		
		// set morph and background
		getTextF("Morph").setText(currChar.getCurrentMorph());
		getTextF("Background").setText(currChar.getBackground());
		
		int[] stats = new int[7];
		int[] bonuses = new int[7];
		int cnt = 0;
		
		stats[0] = currChar.getAptitude("COG");
		stats[1] = currChar.getAptitude("COO");
		stats[2] = currChar.getAptitude("INT");
		stats[3] = currChar.getAptitude("REF");
		stats[4] = currChar.getAptitude("SAV");
		stats[5] = currChar.getAptitude("SOM");
		stats[6] = currChar.getAptitude("WIL");
				
		
		// update base stats
		getTextF("Base COG").setText(""+stats[cnt++]);
		getTextF("Base COO").setText(""+stats[cnt++]);
		getTextF("Base INT").setText(""+stats[cnt++]);
		getTextF("Base REF").setText(""+stats[cnt++]);
		getTextF("Base SAV").setText(""+stats[cnt++]);
		getTextF("Base SOM").setText(""+stats[cnt++]);
		getTextF("Base WIL").setText(""+stats[cnt++]);
		
		// get bonus amounts
		cnt = 0;
		bonuses[cnt++] = getTextFVal("MorphBonusCOG");
		bonuses[cnt++] = getTextFVal("MorphBonusCOO");
		bonuses[cnt++] = getTextFVal("MorphBonusINT");
		bonuses[cnt++] = getTextFVal("MorphBonusREF");
		bonuses[cnt++] = getTextFVal("MorphBonusSAV");
		bonuses[cnt++] = getTextFVal("MorphBonusSOM");
		bonuses[cnt++] = getTextFVal("MorphBonusWIL");
		
		cnt = 0;
		
		// build stat totals
		getTextF("Total COG").setText(""+(stats[0] + bonuses[0]));
		getTextF("Total COO").setText(""+(stats[1] + bonuses[1]));
		getTextF("Total INT").setText(""+(stats[2] + bonuses[2]));
		getTextF("Total REF").setText(""+(stats[3] + bonuses[3]));
		getTextF("Total SAV").setText(""+(stats[4] + bonuses[4]));
		getTextF("Total SOM").setText(""+(stats[5] + bonuses[5]));
		getTextF("Total WIL").setText(""+(stats[6] + bonuses[6]));
		
		// rebuild skills panel
		sideBar.removeAll();
		
		currentLayout = secondaryLayout;
		currentComponent = sideBar;
		int x = 0, y = 0;
		for(String[] pair : currChar.getSkills())
		{
			this.addMappedFixedTF(x,y,pair[0], pair[1], 5, false);
			if (y <= 28)
			{
				y+=2;
			}
			else
			{
				x += 2;
				y =  0;
			}
		}
		
		currentComponent = mainPanel;
	    currentLayout = layout;
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				LifePathUI ui = new LifePathUI();
				ui.init();
			}
		});
		
		
		
	}

	// triggers an update if the text field changes
	private class TextChangeListener implements DocumentListener
	{
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			update();
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			update();
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			update();
		}
		
	}
}
