import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
public class CharacterSheetUI implements UI {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 // we still hardcode some stats like this because the page would break 
	 //		if they were user definable anyways
	 final static String[] primStats = {"COG","COO","INT","REF","SAV","SOM","WIL"}; 
	 final static String[] secStats = {"DUR","WT","DR","LUC","TT","IR","INIT","SPD","DB"};
	 private BorderLayout windowLayout;
	 private JFrame mainWindow;
	 private GridBagUIPanel mainPanel, statPanel,sideBar;
	 private JTextArea mainStatus;
	 private LifePathGenerator gen;
	
	/**
	 * @throws HeadlessException
	 */
	public CharacterSheetUI() throws HeadlessException {
		DataProc.init("LifepathPackages.dat","internalInfo.dat");
		gen = new LifePathGenerator("",this,true);
		windowLayout = new BorderLayout();
        mainWindow = new JFrame();     
        mainPanel = new GridBagUIPanel();
        statPanel = new GridBagUIPanel();
        sideBar = new GridBagUIPanel();
        mainWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        gen.getPC().setVar("{cpCalc}","1"); // enable CP calculator
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
		int resp = JOptionPane.showConfirmDialog(null,"Error Resulted, redo step?\n"+message, 
													"Error", JOptionPane.YES_NO_OPTION);
		
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
		// nop
	}
	
	/**
	 * Main setup method for the UI, creates most UI components and initializes their default state
	 */
	public void init()
	{
        mainWindow.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        mainWindow.setLayout(windowLayout);
        
        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setMinimumSize(mainWindow.getPreferredSize());
        
		// add mainScroll to window, then add mainPanel to that
		mainWindow.add(mainScroll);
		
		// start first row of rows of mixed size
		mainPanel.addMappedTF(0,0,"Character Name",20,new TextChangeListener());
		mainPanel.addMappedTF(2,0,"Morph",10,new TextChangeListener());
		mainPanel.addMappedFixedTF(4,0,"Background","",10,true);
		mainPanel.addMappedFixedTF(6,0,"Natural Language", "",15,true);
		mainPanel.addMappedFixedTF(8,0,"Faction","",10,true);
		
		// gives a quick export of the character
		mainPanel.addMappedButton(12,0,"Export to Txt").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                update();             
                
                JTextArea updateArea= new JTextArea(gen.getPC().toString() 
                										+ DIVIDER_STRING + mainStatus.getText(),10,120);              
                updateArea.setEditable(true);
                updateArea.setLineWrap(true);
                JScrollPane scroll = new JScrollPane (updateArea, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS, 
                										JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
                JOptionPane.showMessageDialog(null, scroll,"Exported Character", JOptionPane.PLAIN_MESSAGE);
                
            }	
		});		
		mainPanel.endRow(14,0);
		
		// we add Panel for the sidebar (skills displays)
		
		// addC is (component,x,y,height,width)
		mainPanel.addC(sideBar,14,0,GridBagConstraints.REMAINDER,1);
	
		mainPanel.addC(statPanel,0,1,6,GridBagConstraints.RELATIVE);
		
		// stats are predictable in format and appearance, so we do them via loops
		
		// Add first row, with the Base Primary stat values
		int idx = 0;
		for (String key : primStats)
		{
			statPanel.addMappedTF(idx,0,"Base "+key,5, new TextChangeListener());
			statPanel.setTextF("Base "+key, Math.max(1,gen.getPC().getAptitude(key)));
			idx +=2;
		}
		statPanel.endRow(idx,0);
		
		
		// add row for bonuses
		idx = 0;
		for (String key : primStats)
		{
			statPanel.addMappedTF(idx, 1, "Bonus", "MorphBonus"+key, 5, new TextChangeListener());
			statPanel.setTextF("MorphBonus"+key, 0);
			idx +=2;
		}
		statPanel.endRow(idx,1);
		
		// add final row for totals.
		idx = 0;
		for (String key : primStats)
		{
			statPanel.addMappedFixedTF(idx,2,"Total "+key, "",5,true);
			idx +=2;
		}
		statPanel.endRow(idx,2);
		
		
		// add row for base secondary stats
		idx = 0;
		for (String key : secStats)
		{
			statPanel.addMappedTF(idx,3,key, ""+gen.getPC().getSecStat(key),5,new TextChangeListener());
			idx +=2;
		}
		statPanel.endRow(idx,3);
		
		// now we do bonuses
		idx = 0;
		for (String key : secStats)
		{
			statPanel.addMappedTF(idx, 4, "Bonus", "MorphBonus"+key, 5, new TextChangeListener());
			idx +=2;
		}
		statPanel.endRow(idx,4);
		
		// now we do the totals
		idx = 0;
		for (String key : secStats)
		{
			statPanel.addMappedFixedTF(idx,5,"Total "+key, "",5,true);
			idx +=2;
		}
		statPanel.endRow(idx,5);
		
		// a few extra stats get factored in too
		statPanel.addMappedTF(0,6,"Stress",5,new TextChangeListener());
			statPanel.setTextF("Stress",0);
		statPanel.addMappedTF(2,6,"MOX",5,new TextChangeListener());
			statPanel.setTextF("MOX",Character.getIntConst("FREE_MOX"));
		statPanel.addMappedTF(4,6,"Credits",5,new TextChangeListener());
			statPanel.setTextF("Credits",Character.getIntConst("FREE_CREDIT"));
		statPanel.addMappedTF(6,6,"Base CP",5,new TextChangeListener());
			statPanel.setTextF("Base CP",1000);
		statPanel.addMappedFixedTF(8,6,"Free CP", "",5,true);
		statPanel.endRow(10,6);
		
		// last bar is Rep values, which can vary based on configuration
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			statPanel.addMappedTF(xIdx,7,r.getName()+"-rep", ""+r.getValue(),5,new TextChangeListener());
			xIdx += 2;
		}
		statPanel.endRow(xIdx,7);
				
		// create the main status window		
		mainStatus = new JTextArea(40,60);
		mainStatus.setMinimumSize(mainStatus.getPreferredSize());
		mainStatus.setMaximumSize(mainStatus.getPreferredSize());
		mainStatus.setLineWrap(true);
		mainStatus.setWrapStyleWord(true);
		JScrollPane tempPane = new JScrollPane(mainStatus);
		tempPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tempPane.setMinimumSize(tempPane.getPreferredSize());
		
		mainPanel.addC(tempPane,0,9,14,13,GridBagConstraints.BOTH);
		mainPanel.endRow(14, 9);

		
		mainPanel.endVertical(0,27);
		
		this.update();
		
		mainWindow.setSize(1700, 1000);

		mainWindow.setVisible(true);
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
		gen.getPC().setName(mainPanel.getTextF("Character Name").getText());
		
		gen.getPC().calcStats(); // updates secondaries
				
		// set morph and background
		String morphName = "";
		
		if (gen.getPC().getCurrentMorph() != null)
		{
			morphName = gen.getPC().getCurrentMorph().getName();
		}
		
		mainPanel.getTextF("Morph").setText(morphName);
		
		if (gen.getPC().hasVar("{background}"))
		{
			mainPanel.setTextF("Background",gen.getPC().getBackground());
		}
		
		if (gen.getPC().hasVar("NatLang"))
		{
			mainPanel.setTextF("Natural Language",gen.getPC().getVar("NatLang"));
		}
		
		if (gen.getPC().hasVar("{factionName}"))
		{
			mainPanel.setTextF("Faction",gen.getPC().getVar("{factionName}"));
		}
		
		int[] stats = new int[16];
		int[] bonuses = new int[16];
		int cnt = 0;
		
		// fill stats with all the primary and secondary stat values from text fields, and update character info accordingly
		for (String key : primStats)
		{
			int val = Math.max(1, statPanel.getTextFVal("Base "+key));
			gen.getPC().setAptitude(key, val);
			stats[cnt++] = val;
		}
		for (String key : secStats)
		{
			int val = statPanel.getTextFVal("MorphBonus"+key);
			gen.getPC().setSecStat(key, val);			
			stats[cnt++] = gen.getPC().getSecStat(key);
		}
		
		cnt = 0;
		
		// get bonus amounts
		cnt = 0;
		for (String key : primStats)
		{
			bonuses[cnt++] = statPanel.getTextFVal("MorphBonus"+key);
		}
		for (String key : secStats)
		{
			bonuses[cnt++] = statPanel.getTextFVal("MorphBonus"+key);
		}
				
		// build stat totals
		cnt = 0;
		for (String key : primStats)
		{
			statPanel.setTextF("Total "+key,(stats[cnt] + bonuses[cnt]));cnt++;
		}
		for (String key : secStats)
		{
			statPanel.setTextF("Total "+key,(stats[cnt] + bonuses[cnt]));cnt++;
		}
		cnt = 0;
		
		// update character with a few more display fields
		gen.getPC().setVar("{stress}", ""+statPanel.getTextFVal("Stress"));
		gen.getPC().setSecStat("MOX",statPanel.getTextFVal("MOX"));
		gen.getPC().setVar("{credits}", ""+statPanel.getTextFVal("Credits"));
		int freeCP = Math.max(0,statPanel.getTextFVal("Base CP") - gen.getPC().getVarInt("{cpUsed}")); // we don't want negative
		statPanel.setTextF("Free CP",freeCP);
		
		// update rep
		for (Rep r : gen.getPC().getAllRep())
		{
			statPanel.setTextF(r.getName()+"-rep",+r.getValue());
		}
		
		// rebuild skills panel
		sideBar.removeAll();

		sideBar.addC(new JLabel("Skills            "),0,0);
		sideBar.addC(new JLabel("                  "),1,0);
		int x = 0, y = 1;
		for(String[] pair : gen.getPC().getSkills())
		{
			String linkedApt = gen.getPC().getSkillApt(pair[0]);
			int morphBonus = statPanel.getTextFVal("MorphBonus"+linkedApt);
			int finalVal = Integer.parseInt(pair[1])+morphBonus;


			sideBar.addMappedFixedTF(x,y,pair[0], ""+finalVal, 5, false);
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
		
	    mainPanel.revalidate();
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {

		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				CharacterSheetUI ui = new CharacterSheetUI();
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
	
	// TODO repurpose this code (if applicable as an input verifier
	private class TextListenerWhenExists implements DocumentListener
	{
		private Class<?> c;
		private Method m;
		private JTextField field;
		
		
		public TextListenerWhenExists(String name, JTextField field)
		{
			try {
				this.c = Class.forName(name);
			} catch (ClassNotFoundException e) {
				throw new IllegalArgumentException("No such class(" + name + ")");
			}
			
			Class<?>[] cArg = new Class[1];
	        cArg[0] = String.class;
	        
			try {
				m = c.getMethod("exists",cArg);
			} catch (NoSuchMethodException e) {
				throw new IllegalArgumentException("Class(" + name + ") lacks exists method!");
			} catch (SecurityException e) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + name + ")!");
			}
			
			this.field = field;
		}
		
		@Override
		public void changedUpdate(DocumentEvent e) {
			try {
				if ((boolean) m.invoke(null, field.getText()))
				{
					update();
				}
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (IllegalArgumentException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			}
		}

		@Override
		public void insertUpdate(DocumentEvent e) {
			try {
				if ((boolean) m.invoke(null, field.getText()))
				{
					update();
				}
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (IllegalArgumentException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			}
		}

		@Override
		public void removeUpdate(DocumentEvent e) {
			try {
				if ((boolean) m.invoke(null, field.getText()))
				{
					update();
				}
			} catch (IllegalAccessException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (IllegalArgumentException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			} catch (InvocationTargetException e1) {
				throw new IllegalArgumentException("Could not access exists method for Class(" + c.getName() + ")!");
			}
		}
			
	}
}