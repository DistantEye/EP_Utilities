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
import com.github.distanteye.ep_utils.core.Step;

/**
 * Visual interface for LifePath type character generation. While there is room for some user
 * editing, most of the fields are driven by table rolling and character prompt choices
 * 
 * @author Vigilant
 *
 */
public class LifePathUI extends UISkeleton {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 private GBagPanel statPanel,sideBar;
	 
	
	/**
	 * @throws HeadlessException
	 */
	public LifePathUI() throws HeadlessException {
		super();        
        statPanel = new GBagPanel();
        sideBar = new GBagPanel();
        mainWindow.setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
	}

	public void refreshAll()
	{
		super.refreshAll();
		
		// reset button incase it was disabled
		mainPanel.getComponentVal("Run Next Step").setEnabled(true);
		mainWindow.revalidate();
		mainWindow.repaint();
		
		gen.setNextEffects(""); // blank any previous values
		gen.setHasStarted(false);
		gen.setHasFinished(false);
		
		// detect the step for a loaded character
		if (gen.getPC().getLastStep() != null)
		{
			Step currStep = (Step)DataProc.getDataObj(gen.getPC().getLastStep().getName());
			Step next = (Step)DataProc.getDataObj(currStep.getNextStep());
			
			if (currStep != null)
			{
				gen.setHasStarted(true);
			}
			
			if (next != null)
			{
				gen.setNextEffects(next.getEffects());				
			}			
			
			// auto stop if we loaded a stopped character
			if (gen.getNextEffects().length() == 0 && gen.hasStarted())
			{
				gen.setHasFinished(true);
				end();
			}
		}
		
		this.mainStatus.setText(gen.getPC().getStatusText());
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
		// Marks the character gen process as stopped, disabling the buttons that used to advance it
		mainPanel.getComponentVal("Run Next Step").setEnabled(false);
		mainWindow.revalidate();
		mainWindow.repaint();
	}
	
	/**
	 * Main setup method for the UI, creates most UI components and initializes their default state
	 */
	public void init()
	{
		int y = 0; // current row
        
		// to make everything work right we add a mainPanel under the mainWindow
		mainWindow.add(mainPanel);
		
		// start first row of rows of mixed size, setup sideBar and statPanel
		y = addHeader(mainPanel, sideBar, 0, new EditState[]{EditState.NOTFIXED,EditState.FIXED,EditState.FIXED,EditState.FIXED,EditState.FIXED},true);
		
		EditState[] statRows = new EditState[]{EditState.FIXED,EditState.NOTFIXED,EditState.FIXED,EditState.FIXED,EditState.NOTFIXED,EditState.FIXED};
		
		// There are three rows of MappedJTextFields for each stat : Base, MorphBonus, Final 
		// The below sets up a pair of these, with appropriate listeners and DataFlow relationships for updates
		y += addStatRows(statPanel,0,statRows, new String[][]{Aptitude.TYPES,EpCharacter.SECONDARY_STATS});
		
		// a few extra stats get factored in too
		y = addOtherStatFields(statPanel, y);
		
		
		// last bar is Rep values, which can vary based on configuration
		y = addRepRows(statPanel,y,EditState.FIXED);
				
		// create the main status window
		y = addMainStatus(mainPanel, y, 14, 13);
		
		// currently at 26 after mainStatus added 
		y = addActionButtons(y);
		
		mainPanel.endVertical(0,y);
		
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
	
		// we add Panel for the sidebar (skills displays)
		p.addC(sideBar,22,row,GridBagConstraints.REMAINDER,1);
				
		int statPanelHeight = 6;
		p.addC(statPanel,0,row+1,statPanelHeight,GridBagConstraints.RELATIVE);
		
		return result;
	}
	
	/**
	 * Adds the bottom set of ActionEvent buttons to the ui
	 * @param startRow row to start at
	 * @return integer value for next available row row after the content added
	 */
	protected int addActionButtons(int startRow)
	{
		mainPanel.addMappedButton(0,startRow,"Firewall Events").addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("_firewall"))
		    	{
		    		gen.getPC().removeVar("_firewall");
		    		mainPanel.setButtonText("Firewall Events","Firewall Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("_firewall", "1");
		    		mainPanel.setButtonText("Firewall Events","Firewall Events (On)");
		    	}
		    }	
		});
		
		// set this afterwards so it doesn't change the mapping name
		mainPanel.setButtonText("Firewall Events","Firewall Events (Off)");
		

		mainPanel.addMappedButton(1,startRow,"Gatecrashing Events").addActionListener(new ActionListener() {
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("_gatecrashing"))
		    	{
		    		gen.getPC().removeVar("_gatecrashing");
		    		mainPanel.setButtonText("Gatecrashing Events","Gatecrashing Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("_gatecrashing", "1");
		    		mainPanel.setButtonText("Gatecrashing Events","Gatecrashing Events (On)");
		    	}
		    }	
		});
		
		// set this afterwards so it doesn't change the mapping name
		mainPanel.setButtonText("Gatecrashing Events","Gatecrashing Events (Off)");
		
		mainPanel.addMappedButton(3,startRow,"Run Next Step").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                gen.step();
                update();
            }	
		});
		
		// gives a quick export of the character
		mainPanel.addButton(5,startRow,"Export to Txt").addActionListener(new ActionListener() {
			
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
		
		// toggle to automatically chose randomly whenever possible
		mainPanel.addMappedButton(6,startRow,"Random Choices").addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				if (gen.isAllRandom())
				{
					gen.setAllRandom(false);
					mainPanel.setButtonText("Random Choices","Random Choices (Off)");
				}
				else
				{
					gen.setAllRandom(true);
					mainPanel.setButtonText("Random Choices","Random Choices (On)");		    		
				}
			}	
		});
		
		// set this afterwards so it doesn't change the mapping name
		mainPanel.setButtonText("Random Choices","Random Choices (Off)");
		
		// rolling vs manually choosing
		mainPanel.addMappedButton(7,startRow,"Rolling").addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e)
			{
				if (gen.isRolling())
		    	{
		    		gen.setRolling(false);
		    		mainPanel.setButtonText("Rolling","Choosing Rolls");
		    	}
		    	else
		    	{
		    		gen.setRolling(true);
		    		mainPanel.setButtonText("Rolling","Rolling");		    		
		    	}
			}	
		});
		
		return startRow+1;
	}
	
	/**
	 * Adds text to the end of the status text area
	 * @param text Text to be added
	 */
	private void appendStatusText(String text)
	{
		String updatedText = this.mainStatus.getText() + "\n\n" + text;
		this.mainStatus.setText(updatedText);
		this.gen.getPC().setStatusText(updatedText);
	}
	
	public void update()
	{
		if (!updateEnabled)
		{
			return; // don't update when we're told not to
		}
		
		// most components are setup to know what they need to do dataflow wise,
		// they only need to be told to update in a certain order to avoid race conditions
		// and even this is handled by most panels, and a simple updateAll call is all that's needed
		
		// note that PC will automatically calc when a many types of changes will happen
		// some AccessWrappers will also trigger calc
	
		// updates direct fields under mainPanel, but not children
		// this mainly handles name, morph, background,etc
		mainPanel.updateAllComps(false); 
		
		// updates all fields under statPanel, including child panels
		// this will handle all the calculations using the relationships/data flows we established in init()
		statPanel.updateAllComps(true);
		
		// rebuild skills panel : this unfortunately cannot be done automatically
		sideBar.removeAll();
		
		sideBar.addC(new JLabel("Skills            "),0,0);
		sideBar.addC(new JLabel("                  "),1,0);
		int x = 0, y = 1;
		for(String[] pair : gen.getPC().getSkills(null))
		{
			String linkedApt = Skill.getSkillApt(pair[0]);
			int morphBonus = statPanel.getTextFIntVal("MorphBonus"+linkedApt);
			int finalVal = Math.min(99, Integer.parseInt(pair[1])+morphBonus);
			
			// since these are remade each update from scratch, skills don't call update()
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
				LifePathUI ui = new LifePathUI();
				ui.init();
			}
		});
		
		
		
	}

}