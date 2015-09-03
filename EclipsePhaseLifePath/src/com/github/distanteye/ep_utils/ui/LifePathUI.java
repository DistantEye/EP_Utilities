package com.github.distanteye.ep_utils.ui;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.github.distanteye.ep_utils.containers.Aptitude;
import com.github.distanteye.ep_utils.containers.Rep;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.LifePathGenerator;

/**
 * Visual interface for LifePath type character generation. While there is room for some user
 * editting, most of the fields are driven by table rolling and character prompt choices
 * 
 * @author Vigilant
 *
 */
public class LifePathUI implements UI {

	 final static String DIVIDER_STRING = "\n------------------------------------------\n";
	 
	 // we still hardcode some stats like this because the page would break 
	 //		if they were user definable anyways
	 final static String[] primStats = Aptitude.aptitudes; 
	 final static String[] secStats = {"DUR","WT","DR","LUC","TT","IR","INIT","SPD","DB"};
	 private BorderLayout windowLayout;
	 private JFrame mainWindow;
	 private GridBagUIPanel mainPanel, statPanel,sideBar;
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
        mainPanel = new GridBagUIPanel();
        statPanel = new GridBagUIPanel();
        sideBar = new GridBagUIPanel();
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
		mainPanel.remove(mainPanel.getComponent("Run Next Step"));
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
        
		// to make everything work right we add a mainPanel under the mainWindow
		mainWindow.add(mainPanel);
		
		// start first row of rows of mixed size
		mainPanel.addMappedTF(0,0,"Character Name",20,this);
		mainPanel.addMappedFixedTF(2,0,"Morph","",10,true);
		mainPanel.addMappedFixedTF(4,0,"Background","",10,true);
		mainPanel.addMappedFixedTF(6,0,"Natural Language", "",15,true);
		mainPanel.addMappedFixedTF(8,0,"Faction","",10,true);
		mainPanel.endRow(10,0);
		
		// we add Panel for the sidebar (skills displays)		
		
		// addC is (component,x,y,height,width)
		mainPanel.addC(sideBar,22,0,GridBagConstraints.REMAINDER,1);
	
		mainPanel.addC(statPanel,0,1,6,GridBagConstraints.RELATIVE);
		
		// stats are predictable in format and appearance, so we do them via loops
		
		// Add first row, with the Base Primary stat values
		int idx = 0;
		for (String key : primStats)
		{
			statPanel.addMappedFixedTF(idx,0,"Base "+key, ""+gen.getPC().getAptitude(key),5,true);
			idx +=2;
		}
		statPanel.endRow(idx,0);
		
		
		// add row for bonuses
		idx = 0;
		for (String key : primStats)
		{
			statPanel.addMappedTF(idx, 1, "Bonus", "MorphBonus"+key, 5, this);
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
			statPanel.addMappedFixedTF(idx,3,key, ""+gen.getPC().getSecStat(key),5,true);
			idx +=2;
		}
		statPanel.endRow(idx,3);
		
		// now we do bonuses
		idx = 0;
		for (String key : secStats)
		{
			statPanel.addMappedTF(idx, 4, "Bonus", "MorphBonus"+key, 5, this);
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
		statPanel.addMappedFixedTF(0,6,"Stress", "",5,true);
		statPanel.addMappedFixedTF(2,6,"MOX", "",5,true);
		statPanel.addMappedFixedTF(4,6,"Credits", "",5,true);
		statPanel.addMappedFixedTF(6,6,"Free CP", "",5,true);
		statPanel.endRow(8,6);
		
		// last bar is Rep values, which can vary based on configuration
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			statPanel.addMappedFixedTF(xIdx,7,r.getName()+"-rep", ""+r.getValue(),5,true);
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

		
		mainPanel.addMappedButton(0,26,"Firewall Events").addActionListener(new ActionListener() {
					
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
		

		mainPanel.addMappedButton(1,26,"Gatecrashing Events").addActionListener(new ActionListener() {
			
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
		
		mainPanel.addMappedButton(3,26,"Run Next Step").addActionListener(new ActionListener() {
			
            public void actionPerformed(ActionEvent e)
            {
                gen.step();
                update();
            }	
		});
		
		// gives a quick export of the character
		mainPanel.addButton(5,26,"Export to Txt").addActionListener(new ActionListener() {
			
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
		mainPanel.addMappedButton(7,26,"Rolling").addActionListener(new ActionListener() {

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
		
		mainPanel.setTextF("Natural Language",gen.getPC().getVarSF("NatLang"));
		
		mainPanel.setTextF("Faction",gen.getPC().getVarSF("{factionName}"));
		
		
		int[] stats = new int[16];
		int[] bonuses = new int[16];
		int cnt = 0;
		
		// fill stats with all the primary and secondary stat values
		for (String key : primStats)
		{
			stats[cnt++] = gen.getPC().getAptitude(key);
		}
		for (String key : secStats)
		{
			stats[cnt++] = gen.getPC().getSecStat(key);
		}
		
		cnt = 0;
		
		// update base stats for both
		for (String key : primStats)
		{
			statPanel.setTextF("Base "+key,stats[cnt++]);
		}
		for (String key : secStats)
		{
			statPanel.setTextF(key,stats[cnt++]);
		}
		
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
		
		// update a few more display fields
		statPanel.setTextF("Stress",gen.getPC().getVarInt("{stress}"));
		statPanel.setTextF("MOX",gen.getPC().getSecStat("MOX"));
		statPanel.setTextF("Credits",gen.getPC().getVarInt("{credits}"));
		statPanel.setTextF("Free CP",gen.getPC().getVarInt("{CP}"));
		
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
				LifePathUI ui = new LifePathUI();
				ui.init();
			}
		});
		
		
		
	}

}