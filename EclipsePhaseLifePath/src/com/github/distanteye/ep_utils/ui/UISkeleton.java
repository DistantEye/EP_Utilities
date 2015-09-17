/**
 * 
 */
package com.github.distanteye.ep_utils.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.ComponentOrientation;
import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Scanner;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.github.distanteye.ep_utils.containers.Rep;
import com.github.distanteye.ep_utils.core.DataProc;
import com.github.distanteye.ep_utils.core.LifePathGenerator;
import com.github.distanteye.ep_utils.wrappers.*;

/**
 * Incomplete implementation of UI holding common
 * code between the various UI's
 * @author Vigilant
 *
 */
public abstract class UISkeleton implements UI {
	protected JTextArea mainStatus;
	protected LifePathGenerator gen;
	protected JFrame mainWindow;
	protected GBagPanel mainPanel;
	private BorderLayout windowLayout;
	
	private JMenuBar menuBar;
	private JMenu fileMenu;
	private JMenuItem save,load;
	protected boolean updateEnabled;
	
	public UISkeleton()
	{
		gen = new LifePathGenerator("",this,true);
        mainWindow = new JFrame();  
        windowLayout = new BorderLayout();
        DataProc.init("LifepathPackages.dat","internalInfo.dat");
		gen = new LifePathGenerator("",this,true);
		
        mainWindow.setComponentOrientation(ComponentOrientation.LEFT_TO_RIGHT);
        mainWindow.setLayout(windowLayout);
        mainPanel = new GBagPanel();
        
        // setup menu
        save = new JMenuItem("Save");
        load = new JMenuItem("Load");
        menuBar = new JMenuBar();
        fileMenu = new JMenu("File");
        fileMenu.setMnemonic(KeyEvent.VK_F);
        fileMenu.add(save);
        fileMenu.add(load);
        menuBar.add(fileMenu);
        mainWindow.setJMenuBar(menuBar);
        save.addActionListener(new ClickListener());
        load.addActionListener(new ClickListener());
        
        updateEnabled = false;
	}
	
	/**
	 * Updates all relevant display fields for the character.
	 * 
	 * Note update should respect updateEnabled, and not act if updateEnabled=false
	 */
	abstract public void update();
	
	protected void save(String fileName)
	{
		try {
			PrintWriter fileO = new PrintWriter(new FileOutputStream(fileName));
			update();
			fileO.println(gen.getPC().getXML());
			fileO.close();
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File : \"" + fileName + "\" could not be created");
		}
	}

	protected void load(String filename) {
		try {
			Scanner fileIn = new Scanner(new FileInputStream(filename));
			String temp = "";
			
			while (fileIn.hasNextLine())
			{
				temp += fileIn.nextLine();
			}
			
			gen.getPC().loadXML(temp);
			fileIn.close();

			updateEnabled = false; // we don't want to trigger any updates during this rebuild phase, and listeners CAN trigger
								   // from setText()
			
			mainPanel.refreshAllComps(true);	
			
			updateEnabled = true;
			update();
			
			
		} catch (FileNotFoundException e) {
			throw new RuntimeException("File : \"" + filename + "\" could not be found");
		}
	}
	
	protected void reInit()
	{
		// attempt to fix some fields if there was already something there
		
	}
	
	/**
	 * Adds a set of irregular length top rows, containing basic char information, and any other start row setup classes want to implement
	 * @param p panel to add to, should be the principle panel in the UI
	 * @param sideBar Panel meant to occupy the far side of the UI
	 * @param row row to start at
	 * @param eArr array of values for whether fields added are Fixed/NonFixed
	 * @return integer value for next available row row after the content added
	 */
	protected int addHeader(GBagPanel p, GBagPanel sideBar, int row, EditState[] eArr, boolean endRow)
	{
		if (eArr.length != 5)
		{
			throw new IllegalArgumentException("eList must be length 5, found: " + eArr.length);
		}

		int cnt = 0;
		// we pass UI for listeners for all fields just incase it's needed
		// if eList makes it a fixed field, the listener stuff will be ignored
		p.addMappedTF(eArr[cnt++],0,0,"Character Name","Character Name",20,"",Orientation.HORIZONTAL,this, new CharNameWrapper(gen.getPC()));
		p.addMappedTF(eArr[cnt++],2,0,"Morph","Morph",10,"",Orientation.HORIZONTAL,this, new CharMorphWrapper(gen.getPC()));
		p.addMappedTF(eArr[cnt++],4,0,"Background","Background",10,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"{background}"));
		p.addMappedTF(eArr[cnt++],6,0,"Natural Language","Natural Language", 15,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"NatLang"));
		p.addMappedTF(eArr[cnt++],8,0,"Faction","Faction",10,"",Orientation.HORIZONTAL,this, new CharVarWrapper(gen.getPC(),"{factionName}"));
		if (endRow)
		{
			p.endRow(10,0);
		}

		return row+1;
	}

	/**
	 * Adds a list of arranged rows for for the sets of stats defined by keyArr
	 * Each set of Strings in keyArr becomes a trio of rows covering "Base value","Bonus","Final Value", 
	 * with appropriate DataFlow relationships and listeners set
	 * @param p Panel to add to
	 * @param row starting row
	 * @param eList EditState array covering whether each row should be fixed or not
	 * @param keyArr Array of arrays of keys for the various stat rows
	 * 			for every String[] in keysArr, we have a triple set of rows for "Base value","Bonus","Final Value"
	 * @return The next free row (row+keyArr.length*3)
	 */
	protected int addStatRows(GBagPanel p, int row, EditState[] eList, String[][] keyArr)
	{
		if (eList.length != keyArr.length*3)
		{
			throw new IllegalArgumentException("eList must be length " + keyArr.length*3 + ", found: " + eList.length);
		}

		int cnt = 0;

		for (String[] keys : keyArr)
		{
			// Add first row, with the Base stat values
			int idx = 0;
			for (String key : keys)
			{
				String name = "Base "+key;
				p.addMappedTF(eList[cnt],idx,row+cnt,name,name, 5,""+gen.getPC().stats().get(key).getValue(),Orientation.HORIZONTAL,null,new StatWrapper(gen.getPC(),key));
				idx +=2;
			}
			p.endRow(idx,row+cnt);

			cnt++;

			// add row for bonuses, which are fed back to the character
			idx = 0;
			for (String key : keys)
			{
				String name = "MorphBonus"+key;		
				p.addMappedTF(eList[cnt],idx, row+cnt, "Bonus", name,5, "", Orientation.HORIZONTAL, this,new CharVarWrapper(gen.getPC(),"bonus"+key));
				idx +=2;
			}
			p.endRow(idx,row+cnt);

			cnt++;

			// add final row for totals.
			idx = 0;
			for (String key : keys)
			{
				String name = "Total "+key;
				SumWrapper basePlusBonus = new SumWrapper(new TextComponentWrapper(p.getTextF("Base "+key)),
						new TextComponentWrapper(p.getTextF("MorphBonus"+key)));

				p.addMappedTF(eList[cnt],idx,row+cnt,name,name, 5,"",Orientation.HORIZONTAL,null, basePlusBonus);
				idx +=2;
			}
			p.endRow(idx,row+cnt);

			cnt++;
		}

		return row+cnt; // this should tell us how far we've gone
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
		p.addMappedTF(EditState.FIXED,0,row,"Stress","Stress", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{stress}"));
		p.addMappedTF(EditState.FIXED,2,row,"MOX","MOX", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{MOX}"));
		p.addMappedTF(EditState.FIXED,4,row,"Credits","Credits", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{credits}"));
		p.addMappedTF(EditState.FIXED,6,row,"Free CP","Free CP", 5,"",Orientation.HORIZONTAL,null, new CharVarWrapper(gen.getPC(),"{CP}"));
		p.endRow(8,row);
		return row+1;
	}

	/**
	 * Adds a status window to the specified panel with the specified row, height, width
	 * @param p Panel to add to
	 * @param row int containing row in the panel to add at
	 * @param height positive integer
	 * @param width positive integer
	 * @return integer value for next available row row after the content added
	 */
	protected int addMainStatus(GBagPanel p, int row, int height, int width)
	{
		// create the main status window		
		mainStatus = new JTextArea(40,60);
		mainStatus.setLineWrap(true);
		mainStatus.setWrapStyleWord(true);
		JScrollPane tempPane = new JScrollPane(mainStatus);
		tempPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		tempPane.setMinimumSize(tempPane.getPreferredSize());
		p.addC(tempPane,0,row,height,width,GridBagConstraints.BOTH);

		return row + height;
	}

	/**
	 * Adds horizontally oriented pairs of Textfields containing a character's rep values to a single row in panel
	 * @param p Panel to add to
	 * @param row row to add at
	 * @param eState Whether Fixed or NonFixed
	 * @return integer value for next available row row after the content added
	 */
	protected int addRepRows(GBagPanel p, int row, EditState eState)
	{
		int xIdx = 0;
		for (Rep r : gen.getPC().getAllRep())
		{
			String name = r.getName()+"-rep";
			p.addMappedTF(eState,xIdx,row,name,name, 5,""+r.getValue(),Orientation.HORIZONTAL,null, new RepWrapper(gen.getPC(),r.getName()));
			xIdx += 2;
		}
		p.endRow(xIdx,row);

		return row+1;
	}
	
	public class ClickListener implements ActionListener {
		@Override
		public void actionPerformed(ActionEvent e) {
			if (e.getSource().equals(save)) {
				JFrame frm = new SaveBox();
				frm.setSize(200,100);
				frm.setVisible(true);
			}
			else if (e.getSource().equals(load)) {
				JFrame frm = new LoadBox();
				frm.setSize(200,100);
				frm.setVisible(true);
			}
		}
	}
	
	@SuppressWarnings("serial")
	public class SaveBox extends JFrame {
		private JLabel label;
		private JTextField inputPrompt;

		private GridBagConstraints cons;
		private Container mainWindow;
		private GridBagLayout layout;

		public SaveBox() throws HeadlessException {
			super();
			this.label = new JLabel("Filename");
			this.inputPrompt = new JTextField("",256);

			
			layout = new GridBagLayout();
			mainWindow = getContentPane();
			mainWindow.setLayout(layout);
			cons = new GridBagConstraints();

			cons.gridx = 0;
			cons.gridy = 0;
			cons.gridheight = 1;
			cons.gridwidth = 1;
			cons.weightx = 50;
			cons.fill = GridBagConstraints.BOTH;
			addC(label);
			cons.gridy++;
			addC(inputPrompt);
			inputPrompt.addKeyListener(new EnterListener(this));
		}

		// done as a shorthand for some of the code
		private void addC(Component comp) {
			layout.setConstraints(comp, cons);
			mainWindow.add(comp);
		}


		public class EnterListener implements KeyListener {
			SaveBox sb;
			public EnterListener(SaveBox sb) {
				this.sb = sb;
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 10) {
					save(inputPrompt.getText());
					sb.setVisible(false);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// do nothing

			}

		}
	}

	@SuppressWarnings("serial")
	public class LoadBox extends JFrame {
		private JLabel label;
		private JTextField inputPrompt;

		private GridBagConstraints cons;
		private Container mainWindow;
		private GridBagLayout layout;

		public LoadBox() throws HeadlessException {
			super();
			this.label = new JLabel("Filename");
			this.inputPrompt = new JTextField("",256);

			layout = new GridBagLayout();
			mainWindow = getContentPane();
			mainWindow.setLayout(layout);
			cons = new GridBagConstraints();

			cons.gridx = 0;
			cons.gridy = 0;
			cons.gridheight = 1;
			cons.gridwidth = 1;
			cons.weightx = 50;
			cons.fill = GridBagConstraints.BOTH;
			addC(label);
			cons.gridy++;
			addC(inputPrompt);
			inputPrompt.addKeyListener(new EnterListener(this));
		}

		// done as a shorthand for some of the code
		private void addC(Component comp) {
			layout.setConstraints(comp, cons);
			mainWindow.add(comp);
		}


		public class EnterListener implements KeyListener {
			LoadBox sb;

			public EnterListener(LoadBox sb) {
				this.sb = sb;
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyChar() == 10) {
					load(inputPrompt.getText());
					sb.setVisible(false);
				}
			}

			@Override
			public void keyReleased(KeyEvent e) {
				// do nothing

			}

			@Override
			public void keyTyped(KeyEvent e) {
				// do nothing

			}

		}
	}
	
}