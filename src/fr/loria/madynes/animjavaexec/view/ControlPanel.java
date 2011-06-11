package fr.loria.madynes.animjavaexec.view;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.MessageFormat;
import java.util.Observable;
import java.util.Observer;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import fr.loria.madynes.animjavaexec.CommandForwarder;
import fr.loria.madynes.animjavaexec.CommandGenerator;
import fr.loria.madynes.animjavaexec.execution.model.StateManager;
import fr.loria.madynes.javautils.Properties;
/**
 * A control panel for execution control.
 * Observe a StepManager, so only  the button "step" is a
 * view (disabled between 2 steps).
 * Other buttons just raise event to controller which has to provide
 * some awt listeners in some way...
 *  
 * @author andrey
 *
 */
public class ControlPanel extends 
//JPanel
JToolBar
implements CommandGenerator  {
	private static final long serialVersionUID = 1L;
	private JButton stepButton;
	private JButton abortButton;
	private JButton quitButton;
	private JButton runButton;
	private JTextField classText;

	private Observer stateObserver=null;
	private CommandForwarder commandForwarder;
	
	public ControlPanel(){
		stepButton=GuiUtils.createButtonFromProperties("fr.loria.madynes.animjavaexec.view.ControlPanel.step",
													   "step", 
													   AbstractButton.CENTER, AbstractButton.LEADING);
	
		abortButton=GuiUtils.createButtonFromProperties("fr.loria.madynes.animjavaexec.view.ControlPanel.abort",
				   "abort", 
				   AbstractButton.CENTER, AbstractButton.LEADING);
		
		runButton=GuiUtils.createButtonFromProperties("fr.loria.madynes.animjavaexec.view.ControlPanel.run",
				   "run", 
				   AbstractButton.CENTER, AbstractButton.LEADING);
		
		classText=new JTextField(30);
		quitButton = GuiUtils.createButtonFromProperties("fr.loria.madynes.animjavaexec.view.ControlPanel.quit",
				   										"quit", 
				   										AbstractButton.CENTER, AbstractButton.LEADING);
			

		// we are in the blue, not even a  registered stateManager
		stepButton.setEnabled(false);
		abortButton.setEnabled(false);
		stepButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				commandForwarder.step();
			}
		});
		abortButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				commandForwarder.abort();
			}
		});
		ActionListener rl=new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				
				try {
					commandForwarder.exec(classText.getText());
				} catch (ClassNotFoundException cnfe) {
					fr.loria.madynes.javautils.Properties.getDefaultProperties();
					fr.loria.madynes.javautils.Properties.getDefaultProperties();
					// TODO Auto-generated catch block
					JOptionPane.showMessageDialog(null, //TODO: check this
					//JOptionPane.showInternalMessageDialog(ControlPanel.this,
							MessageFormat.format(
									Properties.getMessage("fr.loria.madynes.animjavaexec.view.ControlPanel.classNotFoundErrorMessage"),
									classText.getText()),
									Properties.getMessage("fr.loria.madynes.animjavaexec.view.ControlPanel.errorDialogTitle"),
						    JOptionPane.ERROR_MESSAGE);

				} catch (Exception other) {
					JOptionPane.showMessageDialog(null, //TODO: check this
							MessageFormat.format(
									Properties.getMessage("fr.loria.madynes.animjavaexec.view.ControlPanel.otherErrorMessage"),
									other.getLocalizedMessage()),
									Properties.getMessage("fr.loria.madynes.animjavaexec.view.ControlPanel.errorDialogTitle"),
						    JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		runButton.addActionListener(rl);
		classText.addActionListener(rl);
		quitButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				commandForwarder.quit();
			}
		});
		this.add(stepButton);
		this.add(this.abortButton);
		this.add(this.runButton);
		this.add(this.classText);
		this.add(quitButton);
		this.setOpaque(true);
	}
	
	
	// MVC for execution model
	public void observe(StateManager stateMgr){
		if (this.stateObserver==null){
			this.stateObserver=new Observer(){

				@Override
				public void update(Observable stateManager, Object className) { 
					updateGuiStateFrom((StateManager)stateManager); 
					if(className!=null){
						classText.setText((String)className);
					}
				}
			};
		}
		stateMgr.addObserver(this.stateObserver);
		updateGuiStateFrom(stateMgr); // do not wait for an event....
	} 
	
	private void updateGuiStateFrom(StateManager stateManager){
		this.stepButton.setEnabled(stateManager.needStep());
		boolean running=stateManager.isOnARunningState();
		this.abortButton.setEnabled(running);
		this.runButton.setEnabled(!running);
		this.classText.setEnabled(!running);
			// Quit always true
	}

   // Commands to controller 
	@Override
	public void generateToForwarder(CommandForwarder commandForwarder) {
		this.commandForwarder=commandForwarder;
	}
	
	public void setClassText(String className){
		this.classText.setText(className);
	}
}
