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
	 private LifePathGenerator gen;
	 
	 private GridBagLayout currentLayout;
	 private JComponent currentComponent;
	
	 private HashMap<String,JComponent> mappedComponents;
	
	/**
	 * @throws HeadlessException
	 */
	public LifePathUI() throws HeadlessException {
		DataProc.init("LifepathPackages.dat","internalInfo.dat");
		gen = new LifePathGenerator("",this,true);
		
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
		
		String prompt = message + "\n" + extraContext;
		
		// check for a few things that lets us provide extra info
		String[] result = DataProc.getExtraPromptOptions(message,extraContext);
		if (result != null )
		{
			prompt += result[1];
		}
		
		String inputValue = "";
		
		// for longer prompts we use a more proper scrollable UI object
		// for short prompts we use a more compact/prettier/simpler one
		if (prompt.length() > 3000)
		{
			JTextArea textArea = new JTextArea(prompt);
			JScrollPane scrollPane = new JScrollPane(textArea);  
			textArea.setLineWrap(true);  
			textArea.setWrapStyleWord(true); 
			textArea.setEditable(false);
			scrollPane.setPreferredSize( new Dimension( 800, 500 ) );
			inputValue = JOptionPane.showInputDialog(null, scrollPane, "Enter Choice",  
															JOptionPane.QUESTION_MESSAGE);
		}
		else
		{
			inputValue = JOptionPane.showInputDialog(prompt);
		}
		
		return inputValue;
	}

	/* (non-Javadoc)
	 * @see UI#handleError(java.lang.String)
	 */
	@Override
	public boolean handleError(String message) {
		int resp = JOptionPane.showConfirmDialog(null,"Error Resulted, redo step?\n"+message, "Error", JOptionPane.YES_NO_OPTION);
		
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
		this.appendStatusText(message);
	}

	/* (non-Javadoc)
	 * @see UI#end()
	 */
	@Override
	public void end() {
		// Marks the character gen process as stopped, disabling the buttons that used to advance it
		mainPanel.remove(mappedComponents.get("Run Next Step"));
		mainWindow.revalidate();
		mainWindow.repaint();
	}
	
	/**
	 * Main setup method for the UI, creates most UI components and initializes their default state
	 */
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
		
		addMappedTF(0,0,"Character Name",20,true);
		addMappedFixedTF(2,0,"Morph","",10,true);
		addMappedFixedTF(4,0,"Background","",10,true);
		addMappedFixedTF(6,0,"Natural Language", "",15,true);
		addMappedFixedTF(8,0,"Faction","",10,true);
		endRow(10,0);
		
		cons.gridheight = GridBagConstraints.REMAINDER;
		
		// init new Panel
		sideBar = new JPanel();
		sideBar.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        sideBar.setLayout(secondaryLayout);
		
		addC(sideBar,22,0);
		
		cons.gridheight = 1;
		
		
		JPanel statPanel = new JPanel();
		statPanel.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        statPanel.setLayout(statLayout);
		
		cons.gridwidth = GridBagConstraints.RELATIVE;
		cons.gridheight = 6;
		addC(statPanel,0,1);
		
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		currentLayout = statLayout;
		currentComponent = statPanel;
		
		// these will need their own panel, using the new currentLayout, currentComponent system
		
		addMappedFixedTF(0,0,"Base COG", ""+gen.getPC().getAptitude("COG"),5,true);
		addMappedFixedTF(2,0,"Base COO", ""+gen.getPC().getAptitude("COO"),5,true);
		addMappedFixedTF(4,0,"Base INT", ""+gen.getPC().getAptitude("INT"),5,true);
		addMappedFixedTF(6,0,"Base REF", ""+gen.getPC().getAptitude("REF"),5,true);
		addMappedFixedTF(8,0,"Base SAV", ""+gen.getPC().getAptitude("SAV"),5,true);
		addMappedFixedTF(10,0,"Base SOM", ""+gen.getPC().getAptitude("SOM"),5,true);
		addMappedFixedTF(12,0,"Base WIL", ""+gen.getPC().getAptitude("WIL"),5,true);
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
		
		addMappedFixedTF(0,3,"DUR", ""+gen.getPC().getSecStat("DUR"),5,true);
		addMappedFixedTF(2,3,"WT", ""+gen.getPC().getSecStat("WT"),5,true);
		addMappedFixedTF(4,3,"DR", ""+gen.getPC().getSecStat("DR"),5,true);
		addMappedFixedTF(6,3,"LUC", ""+gen.getPC().getSecStat("LUC"),5,true);
		addMappedFixedTF(8,3,"TT", ""+gen.getPC().getSecStat("TT"),5,true);
		addMappedFixedTF(10,3,"IR", ""+gen.getPC().getSecStat("IR"),5,true);
		addMappedFixedTF(12,3,"INIT", ""+gen.getPC().getSecStat("INIT"),5,true);
		addMappedFixedTF(14,3,"SPD", ""+gen.getPC().getSecStat("SPD"),5,true);
		addMappedFixedTF(16,3,"DB", ""+gen.getPC().getSecStat("DB"),5,true);
		endRow(18,3);
		
		addLabel(0,4,"Bonus");
		this.mappedComponents.put("MorphBonusDUR", addTextF(1,4,5,true));
		addLabel(2,4,"Bonus");
		this.mappedComponents.put("MorphBonusWT", addTextF(3,4,5,true));
		addLabel(4,4,"Bonus");
		this.mappedComponents.put("MorphBonusDR", addTextF(5,4,5,true));
		addLabel(6,4,"Bonus");
		this.mappedComponents.put("MorphBonusLUC", addTextF(7,4,5,true));
		addLabel(8,4,"Bonus");
		this.mappedComponents.put("MorphBonusTT", addTextF(9,4,5,true));
		addLabel(10,4,"Bonus");
		this.mappedComponents.put("MorphBonusIR", addTextF(11,4,5,true));
		addLabel(12,4,"Bonus");
		this.mappedComponents.put("MorphBonusINIT", addTextF(13,4,5,true));
		addLabel(14,4,"Bonus");
		this.mappedComponents.put("MorphBonusSPD", addTextF(15,4,5,true));
		addLabel(16,4,"Bonus");
		this.mappedComponents.put("MorphBonusDB", addTextF(17,4,5,true));
		endRow(18,4);
		
		addMappedFixedTF(0,5,"Total DUR", "",5,true);
		addMappedFixedTF(2,5,"Total WT", "",5,true);
		addMappedFixedTF(4,5,"Total DR", "",5,true);
		addMappedFixedTF(6,5,"Total LUC", "",5,true);
		addMappedFixedTF(8,5,"Total TT", "",5,true);
		addMappedFixedTF(10,5,"Total IR", "",5,true);
		addMappedFixedTF(12,5,"Total INIT", "",5,true);
		addMappedFixedTF(14,5,"Total SPD", "",5,true);
		addMappedFixedTF(16,5,"Total DB", "",5,true);		
		endRow(18,5);
		
		addMappedFixedTF(0,6,"Stress", "",5,true);
		addMappedFixedTF(2,6,"MOX", "",5,true);
		addMappedFixedTF(4,6,"Credits", "",5,true);
		addMappedFixedTF(6,6,"Free CP", "",5,true);
		endRow(8,6);
		
		int xIdx = 0;
		
		for (Rep r : gen.getPC().getAllRep())
		{
			addMappedFixedTF(xIdx,7,r.getName()+"-rep", ""+r.getValue(),5,true);
			xIdx += 2;
		}
		endRow(xIdx,7);
		
		// reset to normal dimensions and panel targets
		currentLayout = layout;
		currentComponent = mainPanel;
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		// create the main status window
		cons.gridwidth = 13;
		cons.gridheight = 14;
		cons.fill = GridBagConstraints.BOTH;
		
		
		mainStatus = new JTextArea(40,160);
		mainStatus.setMinimumSize(mainStatus.getPreferredSize());
		mainStatus.setMaximumSize(mainStatus.getPreferredSize());
		mainStatus.setLineWrap(true);
		mainStatus.setWrapStyleWord(true);
		JScrollPane tempPane = new JScrollPane(mainStatus);
		tempPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tempPane.setMinimumSize(tempPane.getPreferredSize());
		
		addC(tempPane,0,9);

		// reset to normal dimensions
		cons.fill = GridBagConstraints.NONE;
		cons.gridwidth = 1;
		cons.gridheight = 1;
		
		addMappedButton(0,26,"Firewall Events").addActionListener(new ActionListener() {
					
			public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("{firewall}"))
		    	{
		    		gen.getPC().removeVar("{firewall}");
		    		((JButton)mappedComponents.get("Firewall Events")).setText("Firewall Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("{firewall}", "1");
		    		((JButton)mappedComponents.get("Firewall Events")).setText("Firewall Events (On)");
		    	}
		    }	
		});
		
		// set this afterwards so it doesn't change the mapping name
		((JButton)mappedComponents.get("Firewall Events")).setText("Firewall Events (Off)");
		

		addMappedButton(1,26,"Gatecrashing Events").addActionListener(new ActionListener() {
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("{gatecrashing}"))
		    	{
		    		gen.getPC().removeVar("{gatecrashing}");
		    		((JButton)mappedComponents.get("Gatecrashing Events")).setText("Gatecrashing Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("{gatecrashing}", "1");
		    		((JButton)mappedComponents.get("Gatecrashing Events")).setText("Gatecrashing Events (On)");
		    	}
		    }	
		});
		
		// set this afterwards so it doesn't change the mapping name
		((JButton)mappedComponents.get("Gatecrashing Events")).setText("Gatecrashing Events (Off)");
		
		addMappedButton(3,26,"Run Next Step").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                gen.step();
                update();
            }	
		});
		
		// gives a quick export of the character
		addButton(5,26,"Export to Txt").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                update();             
                
                JTextArea updateArea= new JTextArea(gen.getPC().toString() + DIVIDER_STRING + mainStatus.getText(),10,120);              
                updateArea.setEditable(true);
                updateArea.setLineWrap(true);
                JScrollPane scroll = new JScrollPane (updateArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JOptionPane.showMessageDialog(null, scroll,"Exported Character", JOptionPane.PLAIN_MESSAGE);
                
            }	
		});
		
		// gives a quick export of the character
		addMappedButton(7,26,"Rolling").addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				if (gen.isRolling())
		    	{
		    		gen.setRolling(false);
		    		((JButton)mappedComponents.get("Rolling")).setText("Choosing Rolls");
		    	}
		    	else
		    	{
		    		gen.setRolling(true);
		    		((JButton)mappedComponents.get("Rolling")).setText("Rolling");		    		
		    	}
			}	
		});
		
		endVertical(0,27);
		
		this.update();
		
		mainWindow.setSize(1700, 1000);

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
	 * Shorthand command to add Button at coordinates with text add adds it to the mapped components list
	 * with key = text
	 * @param x non-negative integer
	 * @param y non-negative integer
	 * @param text Display Text for Button
	 * @return The component created
	 */
	private JButton addMappedButton(int x, int y, String text)
	{
		JButton temp = addButton(x,y,text);
		this.mappedComponents.put(text, temp);
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
		gen.getPC().setName(getTextF("Character Name").getText());
		
		gen.getPC().calcStats(); // updates secondaries
				
		// set morph and background
		String morphName = "";
		
		if (gen.getPC().getCurrentMorph() != null)
		{
			morphName = gen.getPC().getCurrentMorph().getName();
		}
		
		getTextF("Morph").setText(morphName);
		
		if (gen.getPC().hasVar("{background}"))
		{
			getTextF("Background").setText(gen.getPC().getBackground());
		}
		
		if (gen.getPC().hasVar("NatLang"))
		{
			getTextF("Natural Language").setText(gen.getPC().getVar("NatLang"));
		}
		
		if (gen.getPC().hasVar("{factionName}"))
		{
			getTextF("Faction").setText(gen.getPC().getVar("{factionName}"));
		}
		
		int[] stats = new int[16];
		int[] bonuses = new int[16];
		int cnt = 0;
		
		stats[cnt++] = gen.getPC().getAptitude("COG");
		stats[cnt++] = gen.getPC().getAptitude("COO");
		stats[cnt++] = gen.getPC().getAptitude("INT");
		stats[cnt++] = gen.getPC().getAptitude("REF");
		stats[cnt++] = gen.getPC().getAptitude("SAV");
		stats[cnt++] = gen.getPC().getAptitude("SOM");
		stats[cnt++] = gen.getPC().getAptitude("WIL");
		
		stats[cnt++] = gen.getPC().getSecStat("DUR");
		stats[cnt++] = gen.getPC().getSecStat("WT");
		stats[cnt++] = gen.getPC().getSecStat("DR");
		stats[cnt++] = gen.getPC().getSecStat("LUC");
		stats[cnt++] = gen.getPC().getSecStat("TT");
		stats[cnt++] = gen.getPC().getSecStat("IR");
		stats[cnt++] = gen.getPC().getSecStat("INIT");
		stats[cnt++] = gen.getPC().getSecStat("SPD");
		stats[cnt++] = gen.getPC().getSecStat("DB");
		
		cnt = 0;
		
		// update base stats
		getTextF("Base COG").setText(""+stats[cnt++]);
		getTextF("Base COO").setText(""+stats[cnt++]);
		getTextF("Base INT").setText(""+stats[cnt++]);
		getTextF("Base REF").setText(""+stats[cnt++]);
		getTextF("Base SAV").setText(""+stats[cnt++]);
		getTextF("Base SOM").setText(""+stats[cnt++]);
		getTextF("Base WIL").setText(""+stats[cnt++]);
		
		getTextF("DUR").setText(""+stats[cnt++]);
		getTextF("WT").setText(""+stats[cnt++]);
		getTextF("DR").setText(""+stats[cnt++]);
		getTextF("LUC").setText(""+stats[cnt++]);
		getTextF("TT").setText(""+stats[cnt++]);
		getTextF("IR").setText(""+stats[cnt++]);
		getTextF("INIT").setText(""+stats[cnt++]);
		getTextF("SPD").setText(""+stats[cnt++]);
		getTextF("DB").setText(""+stats[cnt++]);
		
		// get bonus amounts
		cnt = 0;
		bonuses[cnt++] = getTextFVal("MorphBonusCOG");
		bonuses[cnt++] = getTextFVal("MorphBonusCOO");
		bonuses[cnt++] = getTextFVal("MorphBonusINT");
		bonuses[cnt++] = getTextFVal("MorphBonusREF");
		bonuses[cnt++] = getTextFVal("MorphBonusSAV");
		bonuses[cnt++] = getTextFVal("MorphBonusSOM");
		bonuses[cnt++] = getTextFVal("MorphBonusWIL");
		
		bonuses[cnt++] = getTextFVal("MorphBonusDUR");
		bonuses[cnt++] = getTextFVal("MorphBonusWT");
		bonuses[cnt++] = getTextFVal("MorphBonusDR");
		bonuses[cnt++] = getTextFVal("MorphBonusLUC");
		bonuses[cnt++] = getTextFVal("MorphBonusTT");
		bonuses[cnt++] = getTextFVal("MorphBonusIR");
		bonuses[cnt++] = getTextFVal("MorphBonusINIT");
		bonuses[cnt++] = getTextFVal("MorphBonusSPD");
		bonuses[cnt++] = getTextFVal("MorphBonusDB");
		
		cnt = 0;
		
		// build stat totals
		getTextF("Total COG").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total COO").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total INT").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total REF").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total SAV").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total SOM").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total WIL").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		
		getTextF("Total DUR").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total WT").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total DR").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total LUC").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total TT").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total IR").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total INIT").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total SPD").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		getTextF("Total DB").setText(""+(stats[cnt] + bonuses[cnt]));cnt++;
		
		cnt = 0;
		
		// update a few more display fields
		getTextF("Stress").setText(""+gen.getPC().getVarInt("{stress}"));
		getTextF("MOX").setText(""+gen.getPC().getSecStat("MOX"));
		getTextF("Credits").setText(""+gen.getPC().getVarInt("{credits}"));
		getTextF("Free CP").setText(""+gen.getPC().getVarInt("{CP}"));
		
		// update rep
		for (Rep r : gen.getPC().getAllRep())
		{
			getTextF(r.getName()+"-rep").setText(""+r.getValue());
		}
		
		// rebuild skills panel
		sideBar.removeAll();
		
		currentLayout = secondaryLayout;
		currentComponent = sideBar;
		addC(new JLabel("Skills            "),0,0);
		addC(new JLabel("                  "),1,0);
		int x = 0, y = 1;
		for(String[] pair : gen.getPC().getSkills())
		{
			this.addMappedFixedTF(x,y,pair[0], pair[1], 5, false);
			if (y <= 32)
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
	    
	    mainPanel.revalidate();
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
