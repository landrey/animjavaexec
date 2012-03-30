package fr.loria.madynes.animjavaexec.execution.model;

import java.text.MessageFormat;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import fr.loria.madynes.animjavaexec.Main;
import fr.loria.madynes.javautils.Properties;

public class StateManager extends Observable {
	// States
	// TODO: go for enumerate...
	public static final int INIT=0;
	/**  a "step" button  should disable when on this state
	 */
	public static final int STEPPING=1;   
	/**  a "step" button  should enable when on this state
	 */
	public static final int ON_STEP=2; 
	public static final int FINISHED=3;   // 
	
	// Addition to states
	
	// For FINISHED
	public static final int FORRESTART=0;
	
	private static final String[] STATE_TO_STR={"INIT", "STEPPING", "ON_STEP", "FINISHED", 	"FORRESTART"};
	
	private int state=INIT;
	private ExecutionModel execModel;
	// private boolean needStep=false;
	private String executedClass;
	
	StateManager(ExecutionModel em){
		this.execModel=em;
		this.state=INIT;
	}
	public void setNeedStep(boolean needStep) {
		int odlState=this.state;
		if (needStep&&this.state==STEPPING){
			this.state=ON_STEP;
		}else if(!needStep&&this.state==ON_STEP){
			this.state=STEPPING;
		}else{
			Logger.getLogger("").logp(Level.WARNING,
					Main.class.getClass().getName(), "setNeedStep",
					MessageFormat.format(Properties.getOptionalMessage(Main.class.getClass().getName()+".cannotstep", "can not step {0} on current state {1}"),
							needStep, STATE_TO_STR[this.state]));
		}
		if (odlState!=this.state){
			this.setChanged();
			this.notifyObservers(); // no param, observers will use getState()...
		}
	}
	public int getState(){
		return this.state;
	}
	/** Regular normal complexion of execution
	 * TODO: handle abnormal cases with extra stuff (parameters).
	 */
	public void finish() {
		if(this.state==ON_STEP || this.state==STEPPING){
			this.state=FINISHED;
			this.setChanged();
			this.notifyObservers(); // no param, observers will use getState()...
		}
	}
	public void exec(String className) {
		assert executedClass!=null;
		if (!this.isOnARunningState()){
			this.executedClass=className;
			this.state=STEPPING;
			this.execModel.clear();
			this.setChanged();
			this.notifyObservers(className);
		}
	}
	public boolean isOnARunningState() {
		return (this.state==ON_STEP || this.state==STEPPING);
	}
	public boolean needStep() {
		return (this.state==ON_STEP);
	}
	private void setExecutedClass(String executedClass) {
		this.executedClass = executedClass;
	}
	public String getExecutedClass() {
		return executedClass;
	}
}
