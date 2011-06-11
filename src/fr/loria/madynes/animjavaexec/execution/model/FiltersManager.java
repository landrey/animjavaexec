package fr.loria.madynes.animjavaexec.execution.model;

import java.util.Arrays;
import java.util.Observable;

import fr.loria.madynes.javautils.Properties;

/**
 * Rmk: not so sure tht filters should be placed in "model"... They are simple parameters for
 * the executing VM the model is representing...
 * 
 * @author andrey
 *
 */
public class FiltersManager extends Observable {
	private ExecutionModel executionModel; // usefull ?
	// ultimate defaults (see properties...)
	private String[] includeFilters=Properties.getDefaultProperties().getOptionalStringList("fr.loria.madynes.animjavaexec.execution.model.FiltersManager.includeFilters", 
			                                                                                new String[]{"*",});
	private String[] excludeFilters=Properties.getDefaultProperties().getOptionalStringList("fr.loria.madynes.animjavaexec.execution.model.FiltersManager.excludeFilters",
																						    new String[]{"java.*", "javax.*", "sun.*", "com.sun.*",});
	FiltersManager(ExecutionModel executionModel){
		this.executionModel=executionModel;
	}
	public void setFilters(String[] includeFilters, String[] excludeFilters){
		this.includeFilters=includeFilters;
		this.excludeFilters=excludeFilters;
		this.setChanged();
		this.notifyObservers(FiltersChangeEvent.bothFiltersChanged);
	}
	public void setIncludeFilters(String[] includeFilters){
		this.includeFilters=includeFilters;
		this.setChanged();
		this.notifyObservers(FiltersChangeEvent.includeFiltersChangedEvent);
	}
	public void setExcludeFilters(String[] excludeFilters){
		this.excludeFilters=excludeFilters;
		this.setChanged();
		this.notifyObservers(FiltersChangeEvent.excludeFiltersChangedEvent); 
	}
	public String[] getIncludeFilters(){
		return Arrays.copyOf(this.includeFilters, this.includeFilters.length);
	}
	public String[] getExcludeFilters(){
		return Arrays.copyOf(this.excludeFilters, this.excludeFilters.length);
	}
	public static class FiltersChangeEvent{
		// a couple of "singletons"...
		private static FiltersChangeEvent includeFiltersChangedEvent=new FiltersChangeEvent(true, false);
		private static FiltersChangeEvent excludeFiltersChangedEvent=new FiltersChangeEvent(false, true);
		private static FiltersChangeEvent bothFiltersChanged=new FiltersChangeEvent(true,true);
		
		private boolean includeFiltersChanged;
		private boolean excludeFiltersChanged;
		private FiltersChangeEvent(boolean includeFiltersChanged, boolean excludeFiltersChanged){
			setIncludeFiltersChanged(includeFiltersChanged);
			setExcludeFiltersChanged(excludeFiltersChanged);
		}
		private void setIncludeFiltersChanged(boolean includeFiltersChanged) {
			this.includeFiltersChanged = includeFiltersChanged;
		}
		public boolean isIncludeFiltersChanged() {
			return includeFiltersChanged;
		}
		private void setExcludeFiltersChanged(boolean excludeFiltersChanged) {
			this.excludeFiltersChanged = excludeFiltersChanged;
		}
		public boolean isExcludeFiltersChanged() {
			return excludeFiltersChanged;
		}
	}
	                                      
}
