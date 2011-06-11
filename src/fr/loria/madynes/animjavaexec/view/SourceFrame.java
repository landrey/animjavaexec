package fr.loria.madynes.animjavaexec.view;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;

import fr.loria.madynes.animjavaexec.CommandForwarder;
import fr.loria.madynes.animjavaexec.CommandGenerator;
import fr.loria.madynes.animjavaexec.execution.model.CurrentLineManager;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.OutputManager;
import fr.loria.madynes.animjavaexec.execution.model.SourcesManager;
import fr.loria.madynes.animjavaexec.execution.model.StateManager;
import fr.loria.madynes.javautils.Properties;
import fr.loria.madynes.javautils.swing.ActionFromProperties;

public class SourceFrame extends JFrame implements CommandGenerator {
	private static final long serialVersionUID = 1L;
	// part of execution mvc.
	private ControlPanel cp;
	private SourcesView sv;
	private OutStreamView outv;
	private OutStreamView errv;
	private JTextField inTxt;
	private CommandForwarder commandForwarder;
	
	public SourceFrame(String windowTitle, Image wIcone, String srcFileName, int r, int c){
		if (windowTitle!=null){
			this.setTitle(windowTitle);
		}
		if (wIcone!=null){
			this.setIconImage(wIcone);
		}
		
		//setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);// TODO: quit() command ?
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		// see http://java.sun.com/docs/books/tutorial/uiswing/components/splitpane.html#divider
        sv=new SourcesView(2*r/3,c);
	 	cp=new ControlPanel();
	 	this.outv=new OutStreamView(OutStreamView.SHOW_OUT, r/3, c/2);
	 	this.errv=new OutStreamView(OutStreamView.SHOW_ERR, r/3, c/2);
	 	this.inTxt=new JTextField();
	 	this.inTxt.setToolTipText(Properties.getMessage("fr.loria.madynes.animjavaexec.view.SourceFrame.intextip"));
	 	this.inTxt.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				commandForwarder.addToStdIn(inTxt.getText()+System.getProperty("line.separator"));
			}
	 	});
	 	this.getContentPane().add(cp,  BorderLayout.NORTH); // CENTER
	 	// vertical split (==JSplitPane.HORIZONTAL_SPLIT can not understand !!!)
	 	JSplitPane outPane=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, this.outv, this.errv);
	 	outPane.setResizeWeight(0.5);
	 	outPane.setOneTouchExpandable(true);
	 	
	 	JSplitPane ioPane=new JSplitPane(JSplitPane.VERTICAL_SPLIT,outPane, this.inTxt);
	 	// source on top of stout and stderr...
	 	JSplitPane centerPane= new JSplitPane(JSplitPane.VERTICAL_SPLIT, sv, ioPane); //outPane
	 	centerPane.setResizeWeight(1.0); // source view get any extra space.
	 	centerPane.setOneTouchExpandable(true);
	 	//this.getContentPane().add(sv, BorderLayout.CENTER); // SOUTH
	 	this.getContentPane().add(centerPane, BorderLayout.CENTER);
	 	
	 	//outPanel.setLayout(new BorderLayout());
	 	//outPanel.add(this.outv, BorderLayout.EAST);
	 	//outPanel.add(this.errv, BorderLayout.WEST);
	 	//this.getContentPane().add(outPanel, BorderLayout.SOUTH);
        //Display the window.
        pack();
        setVisible(true);
	}
	
	//public 
	public void observe(ExecutionModel em){
		this.observe(em.getSourcesManager());
		this.observe(em.getCurrentLineManager());
		this.observe(em.getStateManager());
		this.observe(em.getOutputManager());
	}
	
	private void observe(OutputManager outputManager) {
		this.errv.observe(outputManager);
		this.outv.observe(outputManager);
	}

	private void observe(SourcesManager smgr){
		this.sv.observe(smgr);
	}
	
	private void observe(CurrentLineManager clmgr){
		this.sv.observe(clmgr);
	}
	
	private void observe(StateManager stateManager) {
		this.cp.observe(stateManager);
	}
	
	@Override
	public void generateToForwarder(CommandForwarder commandForwarder) {
		assert commandForwarder!=null;
		this.cp.generateToForwarder(commandForwarder);
		this.commandForwarder=commandForwarder;
		setUpMenuBar();
	}
	
	/**
	 * Set class name displayed by controlPanel.
	 * @param className
	 */
	public void setClassText(String className){
		cp.setClassText(className);
	}
	
	private void setUpMenuBar(){
		JMenuBar menuBar=new JMenuBar();
		JMenu fileMenu=new JMenu(Properties.getMessage("fr.loria.madynes.animjavaexec.view.filemenu.name"));
		
		fileMenu.add(new JMenuItem(this.commandForwarder.getEditPrefencesSwingAction()));
		fileMenu.add(new JMenuItem(this.commandForwarder.getEditFiltersSwingAction()));
		fileMenu.addSeparator();
		fileMenu.add(new JMenuItem(this.commandForwarder.getQuitSwingAction()));
		menuBar.add(fileMenu);
		
		// We are not using command forward: event local to this JFrame and its content...
		//TODO: fixe focus shit for zomm int/out accelerators (+/1)
		JMenu viewMenu=new JMenu(Properties.getMessage("fr.loria.madynes.animjavaexec.view.viewmenu.name"));
		JMenuItem zoomInMenu=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.SourceFrame.zoominaction",
				"zoomin",
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					sv.incrementFontSize(+1);
				}
			}));
		viewMenu.add(zoomInMenu);
		JMenuItem zoomOutMenu=new JMenuItem(new ActionFromProperties(
				"fr.loria.madynes.animjavaexec.view.SourceFrame.zoomoutaction",
				"zoomout",
			new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					sv.incrementFontSize(-1);
				}
			}));
		viewMenu.add(zoomOutMenu);
		menuBar.add(viewMenu);
		this.setJMenuBar(menuBar);
		this.pack();
	}
	// Poor test
	public static void main(String[] args) throws FileNotFoundException, InterruptedException{
		String src1="testjpda/test/Test1.java";
		String sys1="/home/andreylocal/workspace/TestJpda/src/testjpda/test/Test1.java";
		String src2="testjpda/test/A.java";
		String sys2="/home/andreylocal/workspace/TestJpda/src/testjpda/test/A.java";
		String src3="testjpda/test/SourceFrame.java";
		String sys3="/home/andreylocal/workspace/TestJpda/src/testjpda/test/SourceFrame.java";
		SourceFrame sf=new SourceFrame("Test SourceFrame", null, null, 50, 100);
		//
		sf.sv.displaySrcFile(src1, new File(sys1));
		sf.sv.setCurrent(src1, 1);
		Thread.sleep(2000);
		sf.sv.setCurrent(src1, 2);
		Thread.sleep(2000);
		sf.sv.setCurrent(src1, 3);
		sf.sv.displaySrcFile(src1, new File(sys1));
		//sf.displaySrcFile(src3, new File(sys3));
	}

	


}
