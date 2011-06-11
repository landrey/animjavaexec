package fr.loria.madynes.animjavaexec.execution.model;

public class ExecutionModel {
	private SourcesManager sourcesManager;
	private CurrentLineManager  currentLineManager;
	private StateManager stepManager;
	private ExecStack execStack;
	private InstanceManager instanceManager;
	private ArrayManager arrayManager;
	private OutputManager outputManager;
	private FiltersManager filtersManager;
	
	public ExecutionModel(){
		sourcesManager=new SourcesManager(this);
		currentLineManager=new CurrentLineManager(this);
		stepManager=new StateManager(this);
		execStack= new ExecStack(this);
		instanceManager=new InstanceManager(this);
		arrayManager=new ArrayManager(this);
		outputManager=new OutputManager(this);
		filtersManager=new FiltersManager(this);
	}
	public SourcesManager getSourcesManager() {
		return sourcesManager;
	}

	private void setSourcesManager(SourcesManager sourcesManager) {
		this.sourcesManager = sourcesManager;
	}
	
	private void setCurrentLineManager(CurrentLineManager currentLineManager) {
		this.currentLineManager = currentLineManager;
	}

	public CurrentLineManager getCurrentLineManager() {
		return currentLineManager;
	}

	private void setStepManager(StateManager stepManager) {
		this.stepManager = stepManager;
	}

	public StateManager getStateManager() {
		return stepManager;
	}

	private void setExecStack(ExecStack execStack) {
		this.execStack = execStack;
	}

	public ExecStack getExecStack() {
		return execStack;
	}

	private void setInstanceManager(InstanceManager instanceManager) {
		this.instanceManager = instanceManager;
	}

	public InstanceManager getInstanceManager() {
		return instanceManager;
	}

	private void setArrayManager(ArrayManager arrayManager) {
		this.arrayManager = arrayManager;
	}

	public ArrayManager getArrayManager() {
		return arrayManager;
	}

	private void setOutputManager(OutputManager outputManager) {
		this.outputManager = outputManager;
	}

	public OutputManager getOutputManager() {
		return outputManager;
	}

	public FiltersManager getFiltersManager() {
		return filtersManager;
	}
	public void clear() {
		this.arrayManager.clear();
		this.currentLineManager.clear();
		this.execStack.clear();
		this.instanceManager.clear();
		this.sourcesManager.clear();
	}
}
