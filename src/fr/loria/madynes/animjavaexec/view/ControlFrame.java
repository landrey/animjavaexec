package fr.loria.madynes.animjavaexec.view;

import java.awt.event.ActionListener;

import javax.swing.JFrame;

import fr.loria.madynes.animjavaexec.CommandForwarder;
import fr.loria.madynes.animjavaexec.CommandGenerator;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.StateManager;

public class ControlFrame extends JFrame implements CommandGenerator {
	private static final long serialVersionUID = 1L;
	private ControlPanel cp;
	public  ControlFrame(String wTitle){
		this.setTitle(wTitle);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE); // do no kill application !
		cp=new ControlPanel();
        setContentPane(cp);
        pack();
        setVisible(true);
	}
	
	// execution model/view
	public void observe(ExecutionModel executionModel){
		this.observe(executionModel.getStateManager());
	}
	
	private void observe(StateManager stateManager) {
		this.cp.observe(stateManager);
	}
	  // Commands to controller
	@Override
	public void generateToForwarder(CommandForwarder commandForwarder) {
		this.cp.generateToForwarder(commandForwarder);
	}
	
	/**
	 * Set class name displayed by controlPanel.
	 * @param className
	 */
	public void setClassText(String className){
		cp.setClassText(className);
	}
	
}
