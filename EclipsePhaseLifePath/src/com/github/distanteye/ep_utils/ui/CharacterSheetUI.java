package com.github.distanteye.ep_utils.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.ui.validators.ExistsValidator;
import com.github.distanteye.ep_utils.ui.validators.NumericValidator;
import com.github.distanteye.ep_utils.wrappers.*;

/**
 * Plain CharacterSheet UI, Most fields meant to be user editable, with some validation
 * and auto-calculations being made 
 * 
 * NOTE : This UI is not yet fully implemented!
 * 
 * @author Vigilant
 *
 */
public class CharacterSheetUI extends UISkeleton {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 private GBagPanel mainPanel, statPanel,sideBar;
	
	/**
	 * @throws HeadlessException
	 */
	public CharacterSheetUI() throws HeadlessException {
		super();
 
        statPanel = new GBagPanel();
        sideBar = new GBagPanel();
        mainWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        gen.getPC().setVar("_cpCalc","1"); // enable CP calculator
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
		int y = 0;
        
        JScrollPane mainScroll = new JScrollPane(mainPanel);
        mainScroll.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScroll.setMinimumSize(mainWindow.getPreferredSize());
        
		// add mainScroll to window, then add mainPanel to that
		mainWindow.add(mainScroll);
		
		// start first row of rows of mixed size, setup sideBar and statPanel
		y = addHeader(mainPanel, sideBar, y, new EditState[]{EditState.NOTFIXED,EditState.NOTFIXED,EditState.FIXED,EditState.FIXED,EditState.FIXED},false);
		
		EditState[] statRows = new EditState[]{EditState.NOTFIXED,EditState.NOTFIXED,EditState.FIXED,EditState.NOTFIXED,EditState.NOTFIXED,EditState.FIXED};
		
		// There are three rows of MappedJTextFields for each stat : Base, MorphBonus, Final 
		// The below sets up a pair of these, with appropriate listeners and DataFlow relationships for updates
		y += addStatRows(statPanel,0,statRows, new String[][]{Aptitude.TYPES,EpCharacter.SECONDARY_STATS});
		
				
		// a few extra stats get factored in too
		y = addOtherStatFields(statPanel, y);
		
		// last bar is Rep values, which can vary based on configuration
		y = addRepRows(statPanel,y,EditState.NOTFIXED);
						
		// create the main status window
		y = addMainStatus(mainPanel, y, 14, 13);

		mainPanel.endVertical(0,27);
		
		this.update();
		
		mainWindow.setSize(1700, 1000);

		mainWindow.setVisible(true);
	}
	
	/**
	 * Adds a set of irregular length top rows, containing basic char information, and creates the sidebar and statPanel for later population
	 * @param p panel to add to, should be the principle panel in the UI
	 * @param sideBar Panel meant to occupy the far side of the UI
	 * @param row row to start at
	 * @param eArr array of values for whether fields added are Fixed/NonFixed
	 * @return integer value for next available row row after the content added
	 */
	protected int addHeader(GBagPanel p, GBagPanel sideBar, int row, EditState[] eArr, boolean endRow)
	{
		int result = super.addHeader(p, sideBar, row, eArr, endRow);
		
		// do work on top of that for the remaining details
		
		// add necessary text validator onto the JTextField
		p.getTextF("Morph").setInputVerifier(new ExistsValidator(Morph.class.getName()));
		
		// gives a quick export of the character
		p.addMappedButton(12,row,"Export to Txt").addActionListener(new ActionListener() {

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
		p.endRow(14,row);
		
		// we add Panel for the sidebar (skills displays)
		p.addC(sideBar,15,row,GridBagConstraints.REMAINDER,1);
						
		int statPanelHeight = 6;
		p.addC(statPanel,0,row+1,statPanelHeight,GridBagConstraints.RELATIVE);
		
		return result;
	}
	
	/**
	 * Adds misc stat and var-type fields under the main stat block,
	 * this row often changes implementation between different UI implementations
	 * @param p Panel to add to
	 * @param row Row to start adding at
	 * @return  integer value for next available row row after the content added
	 */
	protected int addOtherStatFields(GBagPanel p, int row)
	{
		int fMox = EpCharacter.getIntConst("FREE_MOX");
		int fCred = EpCharacter.getIntConst("FREE_CREDIT");
		int bCP = 1000;
		
		p.addMappedTF(EditState.NOTFIXED,0,row,"Stress","Stress",5,"0", Orientation.HORIZONTAL,this, 
							  new CharVarWrapper(gen.getPC(),"_stress")).setInputVerifier(new NumericValidator());
			
		p.addMappedTF(EditState.NOTFIXED,2,row,"MOX","MOX",5,""+fMox, Orientation.HORIZONTAL,this,
				 			  new CharVarWrapper(gen.getPC(),"_MOX")).setInputVerifier(new NumericValidator());
			
		p.addMappedTF(EditState.NOTFIXED,4,row,"Credits","Credits",5,""+fCred, Orientation.HORIZONTAL,this,
				 			  new CharVarWrapper(gen.getPC(),"_credits")).setInputVerifier(new NumericValidator());
			
		p.addMappedTF(EditState.NOTFIXED,6,row,"Base CP","Base CP",5,""+bCP,Orientation.HORIZONTAL,this,null).setInputVerifier(new NumericValidator());
		
		SubtractWrapper baseMinusUsed = new SubtractWrapper(new TextComponentWrapper(p.getTextF("Base CP")),
															new CharVarWrapper(gen.getPC(),"_cpUsed"));
				
		p.addMappedTF(EditState.FIXED,8,row,"Free CP","Free CP", 5,"",Orientation.HORIZONTAL,null,baseMinusUsed);
		p.endRow(10,6);
		
		return row+1;
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
		if (!updateEnabled)
		{
			return; // don't update when we're told not to
		}
		
		// most components are setup to know what they need to do dataflow wise,
		// they only need to be told to update in a certain order to avoid race conditions
		// and even this is handled by most panels, and a simple updateAll call is all that's needed
				
		gen.getPC().calc(); // updates secondaries
			
		// updates direct fields under mainPanel, but not children
		// this mainly handles name, morph, background,etc
		mainPanel.updateAllComps(false); 
				
		// updates all fields under statPanel, including child panels
		// this will handle all the calculations using the relationships/data flows we established in init()
		statPanel.updateAllComps(true);
		
		// rebuild skills panel
		sideBar.removeAll();

		sideBar.addC(new JLabel("Skills            "),0,0);
		sideBar.addC(new JLabel("                  "),1,0);
		int x = 0, y = 1;
		for(String[] pair : gen.getPC().getSkills(null))
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