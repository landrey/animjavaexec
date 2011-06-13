package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.util.Observable;
import java.util.Observer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import fr.loria.madynes.animjavaexec.CommandForwarder;
import fr.loria.madynes.animjavaexec.CommandGenerator;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.javautils.Properties;
import fr.loria.madynes.javautils.swing.ActionFromProperties;
import fr.loria.madynes.javautils.swing.JFramePropertiesMgr;


// TODO: add what needed to get a correct scroll bar management...
public class MemoryFrame extends JFrame  implements CommandGenerator, ComponentListener {
	private static final long serialVersionUID = 1L;
	private static String alwaysOnTopKey="fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame.alwaysontop"; // key Properties
	private MemoryView mv;
	private CommandForwarder commandForwarder;
	// Always on top property, frame behavior and  menu item...
	private JCheckBoxMenuItem alwaysOnTopMenuItem;
	private JScrollPane sp;
	public MemoryFrame(String windowTitle, Image wIcone, int c, int r){
		if (windowTitle!=null){
			this.setTitle(windowTitle);
		}
		if (wIcone!=null){
			this.setIconImage(wIcone);
		}
		JFramePropertiesMgr.manage(this, Properties.getDefaultProperties(), "fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		//setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//this.mv=new MemoryView(c, r);
		this.mv=new MemoryView(this.getPreferredSize().width, this.getPreferredSize().height);
		// this.addKeyListener(this.mv);
		this.sp=new JScrollPane(mv);
		//sp.setPreferredSize(new Dimension(c*10, r*10));
		Dimension prefSz=this.getPreferredSize(); 
		this.sp.setPreferredSize(prefSz);
		this.sp.getVerticalScrollBar().setValue(prefSz.height);
		this.sp.getHorizontalScrollBar().setValue(0);
		JPanel extraPanel=new JPanel();
		extraPanel.setLayout(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		extraPanel.add(sp, gbc);
		
		//getContentPane().add(sp);
		this.getContentPane().add(extraPanel);
		this.pack();
		//setSize(c*10, r*10);
		this.addComponentListener(this);
		this.setVisible(true);
	}

	public void observe(ExecutionModel executionModel) {
		this.mv.observe(executionModel);
	}

	@Override
	public void generateToForwarder(CommandForwarder commandForwarder) {
		assert commandForwarder!=null;
		this.commandForwarder=commandForwarder;
		setUpMenuBar(); //TODO: Gloups ? Menubar related actions are not all linked to commandForwarder...
	}

	private void setUpMenuBar(){
		JMenuBar menuBar=new JMenuBar();
		JMenu fileMenu=new JMenu(Properties.getMessage("fr.loria.madynes.animjavaexec.view.filemenu.name"));
		JMenuItem printMenu=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame.printaction",
				"printsh",
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						printStackAndHeap();
					}
				}));
		fileMenu.add(printMenu);
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(this.commandForwarder.getEditPrefencesSwingAction()));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(this.commandForwarder.getQuitSwingAction()));
		menuBar.add(fileMenu);
		
		// Edit menu
		JMenu editMenu=new JMenu(Properties.getMessage("fr.loria.madynes.animjavaexec.view.editmenu.name"));
		JMenuItem selectAllMenu=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.selectallaction",
				"selectall",
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					mv.selectAll();
				}
			}));
		
		editMenu.add(selectAllMenu);
		menuBar.add(editMenu);
		
		JMenu viewMenu=new JMenu(Properties.getMessage("fr.loria.madynes.animjavaexec.view.viewmenu.name"));
		// We are not using command forward: event local to this JFrame and its content...
		JMenuItem zoomInMenuItem=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame.zoominaction",
				"zoomin",
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					mv.incrementFontSize(+1);
				}
			}));
		viewMenu.add(zoomInMenuItem);
		JMenuItem zoomOutMenuItem=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame.zoomoutaction",
				"zoomout",
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					mv.incrementFontSize(-1);
				}
			}));
		viewMenu.add(zoomOutMenuItem);
		
		if (this.isAlwaysOnTopSupported()){
			viewMenu.addSeparator();
			alwaysOnTopMenuItem=new JCheckBoxMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame.alwaysontopaction",
				"alwaysontop",
				new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						Properties.getDefaultProperties().setPreference(alwaysOnTopKey, Boolean.toString(((JCheckBoxMenuItem)e.getSource()).isSelected()));
					}
				}));
			updateAlwaysOnTopFromProperties();
			Properties.getDefaultProperties().addKeyObserver(alwaysOnTopKey, new Observer(){
				@Override
				public void update(Observable o, Object arg) {
					updateAlwaysOnTopFromProperties();
				}
			});
			viewMenu.add(alwaysOnTopMenuItem);
			//viewMenu.addSeparator();
		}
		menuBar.add(viewMenu);
		this.setJMenuBar(menuBar);
		this.pack();
	}

	private void updateAlwaysOnTopFromProperties() {
		// alwaysOnTopKey is a kinda model, menu and windows alwayson top feature views controler 
		// Got Properties.....setPreferences.... Shazan!
		boolean alwaysOnTop=Properties.getDefaultProperties().getBooleanProperty(alwaysOnTopKey);
		alwaysOnTopMenuItem.setSelected(alwaysOnTop);
		this.setAlwaysOnTop(alwaysOnTop);
	}

	private void printStackAndHeap() {
		PrinterJob printJob = PrinterJob.getPrinterJob();
		printJob.setPrintable(this.mv);
		if (printJob.printDialog())
			try {
				printJob.print();
			} catch (PrinterException pe) {
				Logger.getLogger("").logp(Level.WARNING,
						this.getClass().getName(), "printStackAndHeap", "Error printing: ", pe);
				//TODO: externalize error message. Add error dialog.
			}
	}

	@Override
	public void componentHidden(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentMoved(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void componentResized(ComponentEvent e) {
		// As Stack is left down we stick scroll bar to this position.
		// May be a pain if user has changed scrollbar position...
		//System.out.println("Memory frame resiezd...");
		//this.mv.scrollRectToVisible(getBounds());
		this.sp.getVerticalScrollBar().setValue(this.getBounds().height);
		this.sp.getHorizontalScrollBar().setValue(0);
	}

	@Override
	public void componentShown(ComponentEvent e) {
		// TODO Auto-generated method stub
		
	}

}
