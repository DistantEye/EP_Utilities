package com.github.distanteye.ep_utils.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.github.distanteye.ep_utils.containers.*;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.LifePathGenerator;
import com.github.distanteye.ep_utils.wrappers.*;

/**
 * Visual interface for LifePath type character generation. While there is room for some user
 * editing, most of the fields are driven by table rolling and character prompt choices
 * 
 * @author Vigilant
 *
 */
public class LifePathUI implements UI {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 private BorderLayout windowLayout;
	 private JFrame mainWindow;
	 private GBagPanel mainPanel, statPanel,sideBar;
	 private JTextArea mainStatus;
	 private LifePathGenerator gen;
	
	/**
	 * @throws HeadlessException
	 */
	public LifePathUI() throws HeadlessException {
		DataProc.init("LifepathPackages.dat","internalInfo.dat");
		gen = new LifePathGenerator("",this,true);
		windowLayout = new BorderLayout();
        mainWindow = new JFrame();     
        mainPanel = new GBagPanel();
        statPanel = new GBagPanel();
        sideBar = new GBagPanel();
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
		mainPanel.remove(mainPanel.getComponentVal("Run Next Step"));
		mainWindow.revalidate();
		mainWindow.repaint();
	}
	
	/**
	 * Main setup method for the UI, creates most UI components and initializes their default state
	 */
	public void init()
	{
		int x = 0; // current column, often refreshes
		int y = 0; // current row
		
        mainWindow.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        mainWindow.setLayout(windowLayout);
        
		// to make everything work right we add a mainPanel under the mainWindow
		mainWindow.add(mainPanel);
		
		// start first row of rows of mixed size
		y = addHeaderAndSideBar(mainPanel, sideBar, 0, new EditState[]{EditState.NOTFIXED,EditState.FIXED,EditState.FIXED,EditState.FIXED,EditState.FIXED});
		
		
	
		mainPanel.addC(statPanel,0,1,6,GridBagConstraints.RELATIVE);
		
		// stats are predictable in format and appearance, so we do them via loops
		
		// Add first row, with the Base Primary stat values
		int idx = 0;
		for (String key : Aptitude.TYPES)
		{
			String name = "Base "+key;
			statPanel.addMappedTF(EditState.FIXED,idx,0,name,name, 5,""+gen.getPC().stats().get(key),Orientation.HORIZONTAL,null,new StatWrapper(gen.getPC(),key));
			idx +=2;
		}
		statPanel.endRow(idx,0);
		
		
		// add row for bonuses
		idx = 0;
		for (String key : Aptitude.TYPES)
		{
			String name = "MorphBonus"+key;
			statPanel.addMappedTF(EditState.NOTFIXED,idx, 1, "Bonus", name,5, "", Orientation.HORIZONTAL, this,null);
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
			statPanel.addMappedTF(EditState.FIXED,idx,3,key,key, 5,""+gen.getPC().stats().get(key),Orientation.HORIZONTAL,null,new StatWrapper(gen.getPC(),key));
			idx +=2;
		}
		statPanel.endRow(idx,3);
		
		// now we do bonuses
		idx = 0;
		for (String key : EpCharacter.SECONDARY_STATS)
		{
			statPanel.addMappedTF(EditState.NOTFIXED,idx, 4, "Bonus", "MorphBonus"+key,5, "", Orientation.HORIZONTAL, this,null);
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
		statPanel.addMappedTF(EditState.FIXED,0,6,"Stress","Stress", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{stress}"));
		statPanel.addMappedTF(EditState.FIXED,2,6,"MOX","MOX", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{MOX}"));
		statPanel.addMappedTF(EditState.FIXED,4,6,"Credits","Credits", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{credits}"));
		statPanel.addMappedTF(EditState.FIXED,6,6,"Free CP","Free CP", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{CP}"));
		statPanel.endRow(8,6);		
		
		// last bar is Rep values, which can vary based on configuration
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			String name = r.getName()+"-rep";
			statPanel.addMappedTF(EditState.FIXED,xIdx,7,name,name, 5,""+r.getValue(),Orientation.HORIZONTAL,null, new RepWrapper(gen.getPC(),r.getName()));
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

		// currently at 26 after mainStatus added 
		y = addActionButtons(26);
		
		mainPanel.endVertical(0,y);
		
		this.update();
		
		mainWindow.setSize(1700, 1000);

		mainWindow.setVisible(true);
	}
	
	
	// move stuff into below methods and then try and move common code into an abstract SkeletonUI class for common code reasons
	// see what overall you can gather in common into the abstract class then have the other two classes call init/update with calls to super.init, super.update,
	// etc
	
	
	private int addHeaderAndSideBar(GBagPanel p, GBagPanel sideBar, int row, EditState[] eList)
	{
		if (eList.length != 5)
		{
			throw new IllegalArgumentException("fixedList must be length 5, found: " + eList.length);
		}
		
		int cnt = 0;
		// we pass UI for listeners for all fields just incase it's needed
		// if eList makes it a fixed field, the listener stuff will be ignored
		p.addMappedTF(eList[cnt++],0,0,"Character Name","Character Name",20,"",Orientation.HORIZONTAL,this, new CharNameWrapper(gen.getPC()));
		p.addMappedTF(eList[cnt++],2,0,"Morph","Morph",10,"",Orientation.HORIZONTAL,this, new CharMorphWrapper(gen.getPC()));
		p.addMappedTF(eList[cnt++],4,0,"Background","Background",10,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"{background}"));
		p.addMappedTF(eList[cnt++],6,0,"Natural Language","Natural Language", 15,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"NatLang"));
		p.addMappedTF(eList[cnt++],8,0,"Faction","Faction",10,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"{factionName}"));
		p.endRow(10,0);		
		
		// we add Panel for the sidebar (skills displays)
		p.addC(sideBar,22,0,GridBagConstraints.REMAINDER,1);
		
		return row+1;
	}
	
	private int addStatRows(GBagPanel p, int row, EditState[] eList, String[][] keys)
	{
		// for every String[] in keys, we have a triple set of rows for "Base value","Bonus","Final Value" 
		

		return row;
	}
	
	private int addBaseStatRow(GBagPanel p, int row, EditState e)
	{
		
		return 0;
	}
	
	private int addBonusStatRow(GBagPanel p, int row, EditState e)
	{
		
		return 0;
	}
	
	private int addFinalStatRow(GBagPanel p, int row)
	{
		
		return 0;
	}
	
	private int addBaseSecStat(GBagPanel p, int row, EditState e)
	{
		
		return 0;
	}
	
	private int addBonusSecStat(GBagPanel p, int row, EditState e)
	{
		
		return 0;
	}
	
	private int addFinalSecStat(GBagPanel p, int row)
	{
		
		return 0;
	}
	
	private int addActionButtons(int startRow)
	{
		mainPanel.addMappedButton(0,startRow,"Firewall Events").addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("{firewall}"))
		    	{
		    		gen.getPC().removeVar("{firewall}");
		    		mainPanel.setButtonText("Firewall Events","Firewall Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("{firewall}", "1");
		    		mainPanel.setButtonText("Firewall Events","Firewall Events (On)");
		    	}
		    }	
		});
		
		// set this afterwards so it doesn't change the mapping name
		mainPanel.setButtonText("Firewall Events","Firewall Events (Off)");
		

		mainPanel.addMappedButton(1,startRow,"Gatecrashing Events").addActionListener(new ActionListener() {
			
		    public void actionPerformed(ActionEvent e)
		    {
		    	if (gen.getPC().hasVar("{gatecrashing}"))
		    	{
		    		gen.getPC().removeVar("{gatecrashing}");
		    		mainPanel.setButtonText("Gatecrashing Events","Gatecrashing Events (Off)");
		    	}
		    	else
		    	{
		    		gen.getPC().setVar("{gatecrashing}", "1");
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
		this.mainStatus.setText(this.mainStatus.getText() + "\n\n" + text);
	}
	
	/**
	 * Updates all relevant display fields for the character
	 */
	public void update()
	{
		// most components are setup to know what they need to do dataflow wise,
		// they only need to be told to update in a certain order to avoid race conditions
		// and even this is handled by most panels, and a simple updateAll call is all that's needed
		
		gen.getPC().calcStats(); // updates secondaries
	
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
		for(String[] pair : gen.getPC().getSkills())
		{
			String linkedApt = Skill.getSkillApt(pair[0]);
			int morphBonus = statPanel.getTextFIntVal("MorphBonus"+linkedApt);
			int finalVal = Integer.parseInt(pair[1])+morphBonus;
			
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