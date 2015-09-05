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
import com.github.distanteye.ep_utils.wrappers.*;

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
	 private GBagPanel mainPanel, statPanel,sideBar;
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
        mainPanel = new GBagPanel();
        statPanel = new GBagPanel();
        sideBar = new GBagPanel();
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
		mainPanel.addMappedTF(EditState.NOTFIXED,0,0,"Character Name","Character Name",20,"",Orientation.HORIZONTAL,this, new CharNameWrapper(gen.getPC()));
		mainPanel.addMappedTF(EditState.NOTFIXED,2,0,"Morph","Morph",10,"",Orientation.HORIZONTAL,this, 
									new CharMorphWrapper(gen.getPC())).setInputVerifier(new ExistsValidator(Morph.class.getName()));
		
		mainPanel.addMappedTF(EditState.FIXED,4,0,"Background","Background",10,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{background}"));
		mainPanel.addMappedTF(EditState.FIXED,6,0,"Natural Language","Natural Language", 15,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"NatLang"));
		mainPanel.addMappedTF(EditState.FIXED,8,0,"Faction","Faction",10,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{faction}"));
		
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
		
		// stats are predictable in format and appearance, so we do them via loops
		
		// Add first row, with the Base Primary stat values
		int idx = 0;
		for (String key : Aptitude.TYPES)
		{
			String name = "Base "+key;
			statPanel.addMappedTF(EditState.NOTFIXED,idx,0,name,name, 5,"",Orientation.HORIZONTAL,null,
										new StatWrapper(gen.getPC(),key)).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,0);


		// add row for bonuses
		idx = 0;
		for (String key : Aptitude.TYPES)
		{
			String name = "MorphBonus"+key;
			statPanel.addMappedTF(EditState.NOTFIXED,idx, 1, "Bonus", name,5, "", Orientation.HORIZONTAL, this,null).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,1);

		// add final row for totals.
		idx = 0;
		for (String key : Aptitude.TYPES)
		{
			String name = "Total "+key;
			SumWrapper basePlusBonus = new SumWrapper(new TextComponentWrapper(statPanel.getTextF("Base "+key)),
					new TextComponentWrapper(statPanel.getTextF("MorphBonus"+key)));

			statPanel.addMappedTF(EditState.FIXED,idx,2,name,name, 5,"",Orientation.HORIZONTAL,null, basePlusBonus);
			idx +=2;
		}
		statPanel.endRow(idx,2);


		// add row for base secondary stats
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedTF(EditState.NOTFIXED,idx,3,key,key, 5,"",Orientation.HORIZONTAL,null,
									new StatWrapper(gen.getPC(),key)).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,3);

		// now we do bonuses
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedTF(EditState.NOTFIXED,idx, 4, "Bonus", "MorphBonus"+key,5, "", Orientation.HORIZONTAL, this,null).setInputVerifier(new NumericValidator());
			idx +=2;
		}
		statPanel.endRow(idx,4);

		// now we do the totals
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			String name = "Total "+key;
			SumWrapper basePlusBonus = new SumWrapper(new TextComponentWrapper(statPanel.getTextF(key)),
					new TextComponentWrapper(statPanel.getTextF("MorphBonus"+key)));
			statPanel.addMappedTF(EditState.FIXED,idx,5,name,name, 5,"",Orientation.HORIZONTAL,null,basePlusBonus);
			idx +=2;
		}
		statPanel.endRow(idx,5);
		
				
		// a few extra stats get factored in too
		int fMox = EpCharacter.getIntConst("FREE_MOX");
		int fCred = EpCharacter.getIntConst("FREE_CREDIT");
		int bCP = 1000;
		
		statPanel.addMappedTF(EditState.NOTFIXED,0,6,"Stress","Stress",5,"0", Orientation.HORIZONTAL,this, 
							  new CharVarWrapper(gen.getPC(),"{stress}")).setInputVerifier(new NumericValidator());
			
		statPanel.addMappedTF(EditState.NOTFIXED,2,6,"MOX","MOX",5,""+fMox, Orientation.HORIZONTAL,this,
				 			  new CharVarWrapper(gen.getPC(),"{MOX}")).setInputVerifier(new NumericValidator());
			
		statPanel.addMappedTF(EditState.NOTFIXED,4,6,"Credits","Credits",5,""+fCred, Orientation.HORIZONTAL,this,
				 			  new CharVarWrapper(gen.getPC(),"{credits}")).setInputVerifier(new NumericValidator());
			
		statPanel.addMappedTF(EditState.NOTFIXED,6,6,"Base CP","Base CP",5,""+bCP,Orientation.HORIZONTAL,this,null).setInputVerifier(new NumericValidator());
		
		SubtractWrapper baseMinusUsed = new SubtractWrapper(new TextComponentWrapper(statPanel.getTextF("Base CP")),
															new CharVarWrapper(gen.getPC(),"{cpUsed}"));
				
		statPanel.addMappedTF(EditState.FIXED,8,6,"Free CP","Free CP", 5,"",Orientation.HORIZONTAL,null,baseMinusUsed);
		statPanel.endRow(10,6);
		
		// last bar is Rep values, which can vary based on configuration
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			statPanel.addMappedTF(EditState.NOTFIXED,xIdx,7,r.getName()+"-rep", ""+r.getValue(),5,"",Orientation.HORIZONTAL,this,
									new RepWrapper(gen.getPC(),r.getName())).setInputVerifier(new NumericValidator());
			xIdx += 2;
		}
		statPanel.endRow(xIdx,7);
				
		// create the main status window		
		mainStatus = new JTextArea(40,60);
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
		// most components are setup to know what they need to do dataflow wise,
		// they only need to be told to update in a certain order to avoid race conditions

		mainPanel.updateComp("Character Name");

		gen.getPC().calcStats(); // updates secondaries

		// set morph and background
		mainPanel.updateComp("Morph");		
		mainPanel.updateComp("Background");

		// natural language and faction next
		mainPanel.updateComp("Natural Language");
		mainPanel.updateComp("Faction");

		// update base stats for primary and secondary stat values
		for (String key : Aptitude.TYPES)
		{
			statPanel.updateComp("Base "+key);
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.updateComp(key);
		}

		// run updates on bonus amounts for both
		for (String key : Aptitude.TYPES)
		{
			statPanel.updateComp("MorphBonus"+key);
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.updateComp("MorphBonus"+key);
		}

		// build stat totals
		for (String key : Aptitude.TYPES)
		{
			statPanel.updateComp("Total "+key);
		}
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.updateComp("Total "+key);
		}
		
		// update character with a few more display fields
		gen.getPC().setVar("{stress}", ""+statPanel.getTextFIntVal("Stress"));
		gen.getPC().setMox(statPanel.getTextFIntVal("MOX"));
		gen.getPC().setVar("{credits}", ""+statPanel.getTextFIntVal("Credits"));
		int freeCP = Math.max(0,statPanel.getTextFIntVal("Base CP") - gen.getPC().getVarInt("{cpUsed}")); // we don't want negative
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
			int morphBonus = statPanel.getTextFIntVal("MorphBonus"+linkedApt);
			int finalVal = Integer.parseInt(pair[1])+morphBonus;

			// TODO may need to carefully implement update() for this section in future
			sideBar.addMappedTF(EditState.FIXED,x,y,pair[0],pair[0], 5, ""+finalVal, Orientation.VERTICAL,null,null);
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