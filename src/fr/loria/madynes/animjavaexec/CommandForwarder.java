package fr.loria.madynes.animjavaexec;

import java.awt.event.ActionEvent;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.Action;

import fr.loria.madynes.javautils.swing.AbstractActionFromProperties;

/**
 * This is an over kill... I ain't see any case where severals applications would register to listen to commands !
 * @author andrey
 *
 */
public class CommandForwarder implements CommandListener { //implements CommandListener just to be sure to forward all commands
	// action strings...
	private static final String quitActionStr="quit";
	private static final String editPreferencesActionStr="editpreferences";
	private static final String abortActionStr="abort";
	private static final String stepActionStr="step";
	private static final String editFiltersActionStr = "editfilters";
	
	private Vector<CommandListener> listeners=new Vector<CommandListener>();

	private SwingActionToCommand quitSwingAction;
	private SwingActionToCommand editPreferencesAction;
	private SwingActionToCommand abortSwingAction;
	private SwingActionToCommand stepSwingAction;
	private SwingActionToCommand editFiltersAction;
	
		
	// CommandListener ---
	@Override
	public void abort() {
		for (CommandListener l:listeners){ // What we need is DELEGATE (=>JPerfect Pattern...)
			l.abort();
		}
	}

	@Override
	public void exec(String className) throws ClassNotFoundException, Exception {
		for (CommandListener l:listeners){ 
			l.exec(className); //TODO: exception trouble. Try each elt ? 
		}
	}

	@Override
	public void quit() {
		for (CommandListener l:listeners){ 
			l.quit();
		}
	}
	
	@Override
	public void editPreferences() {
		for (CommandListener l:listeners){ 
			l.editPreferences();
		}
		
	}
	@Override
	public void editFilters() {
		for (CommandListener l:listeners){ 
			l.editFilters();
		}
	}
	@Override
	public void step() {
		for (CommandListener l:listeners){ 
			l.step();
		}
	} 
	
	@Override
	public void addToStdIn(String s) {
		for (CommandListener l:listeners){ 
			l.addToStdIn(s);
		}
	}
	
	@Override
	public void changeFilters(String[] includeFilters, String[] excludeFilters) {
		// TODO Auto-generated method stub	
	}

	public void addCommandListener(CommandListener l){
		assert l!=null;
		listeners.add(l);
	}
	
	public Action getQuitSwingAction(){
		if (this.quitSwingAction==null){
			this.quitSwingAction=new SwingActionToCommand("fr.loria.madynes.animjavaexec.CommandForwarder.quitaction", 
														  quitActionStr);
		}
		return this.quitSwingAction;
	}
	
	public Action getEditPrefencesSwingAction(){
		if (this.editPreferencesAction==null){
			this.editPreferencesAction=new SwingActionToCommand("fr.loria.madynes.animjavaexec.CommandForwarder.editpreferencesaction", 
																editPreferencesActionStr);
		}
		return this.editPreferencesAction;
	}
	
	public Action getEditFiltersSwingAction(){
		if (this.editFiltersAction==null){
			this.editFiltersAction=new SwingActionToCommand("fr.loria.madynes.animjavaexec.CommandForwarder.editfiltersaction", 
																editFiltersActionStr);
		}
		return this.editFiltersAction;
	}
	public Action getAbortSwingAction(){
		if (this.abortSwingAction==null){
			this.abortSwingAction=new SwingActionToCommand("fr.loria.madynes.animjavaexec.CommandForwarder.quitaction", abortActionStr);
		}
		return this.abortSwingAction;
	}
	
	public Action getStepSwingAction(){
		if (this.stepSwingAction==null){
			this.stepSwingAction=new SwingActionToCommand("fr.loria.madynes.animjavaexec.CommandForwarder.quitaction", stepActionStr);
		}
		return this.stepSwingAction;
	}
	
	private class SwingActionToCommand extends AbstractActionFromProperties {
		private static final long serialVersionUID = 1L;

		SwingActionToCommand(String propertiesPrefix, String command) {
			super(propertiesPrefix, command);
		}

		@Override
		// swing action to application command dispatch.
		public void actionPerformed(ActionEvent e) {
			String command=e.getActionCommand();
			// We could do this by reflexion and invoke...
			if (quitActionStr.equals(command)){
				quit();
			}else if (abortActionStr.equals(command)){
				abort();
			}else if (editPreferencesActionStr.equals(command)){
				editPreferences();
			}else if (stepActionStr.equals(command)){
				step();
			}else if (editFiltersActionStr.equals(command)){
				editFilters();
			}else{
				Logger.getLogger("").logp(Level.WARNING,
						this.getClass().getName(), "actionPerformed",
							command+" unknown command or not yet implemented");
			}
		}
	}// SwingActionToCommand


}//class
