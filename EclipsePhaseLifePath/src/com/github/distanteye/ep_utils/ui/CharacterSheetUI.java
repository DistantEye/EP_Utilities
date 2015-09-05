package com.github.distanteye.ep_utils.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.LifePathGenerator;
import com.github.distanteye.ep_utils.ui.validators.ExistsValidator;
import com.github.distanteye.ep_utils.ui.validators.NumericValidator;
import com.github.distanteye.ep_utils.wrappers.IntWrapper;

/**
 * Plain CharacterSheet UI, Most fields meant to be user editable, with some validation
 * and auto-calculations being made 
 * 
 * @author Vigilant
 *
 */
public class CharacterSheetUI implements UI {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 // we still hardcode some stats like this because the page would break 
	 //		if they were user definable anyways
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
        gen.getPC().setVar("{cpCalc}",new IntWrapper(1)); // enable CP calculator
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
		
		return resp == JOptionPane.YES_OPTION;
		
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
		mainPanel.addMappedTF(0,0,"Character Name",20,this);
		mainPanel.addMappedTF(2,0,"Morph",10,this).setInputVerifier(new ExistsValidator("Morph"));
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
		for (String key : Aptitude.TYPES)
		{
			statPanel.addMappedTF(idx,0,"Base "+key,5, this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("Base "+key, Math.max(1,gen.getPC().primaryStats().get(key).getValue()));
			idx +=2;
		}
		statPanel.endRow(idx,0);
		
		
		// add row for bonuses
		idx = 0;
		for (String key : Aptitude.TYPES)
		{
			statPanel.addMappedTF(idx, 1, "Bonus", "MorphBonus"+key, 5, this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("MorphBonus"+key, 0);
			idx +=2;
		}
		statPanel.endRow(idx,1);
		
		// add final row for totals.
		idx = 0;
		for (String key : Aptitude.TYPES)
		{
			statPanel.addMappedFixedTF(idx,2,"Total "+key, "",5,true);
			idx +=2;
		}
		statPanel.endRow(idx,2);
		
		
		// add row for base secondary stats
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedTF(idx,3,key,5,this).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,3);
		
		// now we do bonuses
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedTF(idx, 4, "Bonus", "MorphBonus"+key, 5, this).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,4);
		
		// now we do the totals
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedFixedTF(idx,5,"Total "+key, "",5,true);
			idx +=2;
		}
		statPanel.endRow(idx,5);
		
		// a few extra stats get factored in too
		statPanel.addMappedTF(0,6,"Stress",5,this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("Stress",0);
		statPanel.addMappedTF(2,6,"MOX",5,this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("MOX",EpCharacter.getIntConst("FREE_MOX"));
		statPanel.addMappedTF(4,6,"Credits",5,this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("Credits",EpCharacter.getIntConst("FREE_CREDIT"));
		statPanel.addMappedTF(6,6,"Base CP",5,this).setInputVerifier(new NumericValidator());
			statPanel.setTextF("Base CP",1000);
		statPanel.addMappedFixedTF(8,6,"Free CP", "",5,true);
		statPanel.endRow(10,6);
		
		// last bar is Rep values, which can vary based on configuration
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			statPanel.addMappedTF(xIdx,7,r.getName()+"-rep", ""+r.getValue(),5,this).setInputVerifier(new NumericValidator());
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
	public void update()
	{
		gen.getPC().setName(mainPanel.getTextF("Character Name").getText());
		
		gen.getPC().calcStats(); // updates secondaries
				
		// set morph and background
		JTextField morphField = mainPanel.getTextF("Morph");
		
		// if we have a valid morph we pull the value and update the character
		if (morphField.getInputVerifier().verify(morphField))
		{
			gen.getPC().setCurrentMorph(Morph.createMorph(morphField.getText()));
		}
	
		
		if (gen.getPC().hasVar("{background}"))
		{
			mainPanel.setTextF("Background",gen.getPC().getBackground());
		}
		
		if (gen.getPC().hasVar("NatLang"))
		{
			mainPanel.setTextF("Natural Language",gen.getPC().getVarVal("NatLang"));
		}
		
		if (gen.getPC().hasVar("{factionName}"))
		{
			mainPanel.setTextF("Faction",gen.getPC().getVarVal("{factionName}"));
		}
		
		int[] stats = new int[16];
		int[] bonuses = new int[16];
		int cnt = 0;
		
		// fill stats with all the primary and secondary stat values from text fields, and update character info accordingly
		for (String key : Aptitude.TYPES)
		{
			int val = Math.max(1, statPanel.getTextFVal("Base "+key));
			gen.getPC().primaryStats().get(key).setValue(val);
			stats[cnt++] = val;
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			int val = statPanel.getTextFVal(key);
			gen.getPC().secStats().put(key, val);			
			stats[cnt++] = gen.getPC().secStats().get(key);
		}
		
		cnt = 0;
		
		// get bonus amounts
		cnt = 0;
		for (String key : Aptitude.TYPES)
		{
			bonuses[cnt++] = statPanel.getTextFVal("MorphBonus"+key);
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			bonuses[cnt++] = statPanel.getTextFVal("MorphBonus"+key);
		}
				
		// build stat totals
		cnt = 0;
		for (String key : Aptitude.TYPES)
		{
			statPanel.setTextF("Total "+key,(stats[cnt] + bonuses[cnt]));cnt++;
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.setTextF("Total "+key,(stats[cnt] + bonuses[cnt]));cnt++;
		}
		cnt = 0;
		
		// update character with a few more display fields
		gen.getPC().setVar("{stress}", new IntWrapper(statPanel.getTextFVal("Stress")));
		gen.getPC().secStats().put("MOX",statPanel.getTextFVal("MOX"));
		gen.getPC().setVar("{credits}", new IntWrapper(statPanel.getTextFVal("Credits")));
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
			String linkedApt = Skill.getSkillApt(pair[0]);
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
	
}