package fr.loria.madynes.animjavaexec.execution.model;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.StringTokenizer;
import java.util.Vector;

/**
 * Keep track of used source file by the current execution.
 * 
 * The actual texts of the source code files are not load, this action is
 * left to the various views....
 * 
 * But the manager resolves source name coming from debug interface to a usable system pathfile...
 * 
 * No dedicated model is created for source name, a String to the (relative) source path file is the model.
 * The associated system path can be fetched using the {@link getSystemPath} method. 
 * 
 * A source manager object is observable by the views....
 * @author andrey
 * @see SourceChangeEvent
 * 
 */
public class SourcesManager extends Observable {
	private ExecutionModel executionModel;
	// Map: dbi source name -> system path man.
	private Map<String, File> dbiTiSystem =new HashMap<String,File>();
	private String[] searchPaths;
	
	SourcesManager(ExecutionModel executionModel) {
		this.executionModel=executionModel;
		
		//TODO: improve source path specification by user... use Properties, add params in SourceManager cons
		StringTokenizer stk=new StringTokenizer(System.getProperties().getProperty("java.class.path"), File.pathSeparator);
		Vector<String> tmpSps=new Vector<String>();
		while(stk.hasMoreTokens()){ // sucks! StringTokenizer is not Iterable !
			tmpSps.add(stk.nextToken());
		}
		this.searchPaths=tmpSps.toArray(new String[tmpSps.size()]);
	}

	public void addSource(String sourcePath){
		if (!this.dbiTiSystem.containsKey(sourcePath)){
			File sysPath=resolveToSystemSourceFile(sourcePath);
			if(sysPath==null){
				//TODO: log...
				System.err.println("no source file found for: "+sourcePath);
			}else{
				this.dbiTiSystem.put(sourcePath, sysPath);
				this.setChanged();
				this.notifyObservers(new SourcesChangeEvent(sourcePath, this.getSystemPath(sourcePath)));
			}
		}//else source already there...
	}
	public File getSystemPath(String sourcePath){
		return this.dbiTiSystem.get(sourcePath);
	}
	

	public void clear() {
		this.dbiTiSystem.clear();
		this.setChanged();
		this.notifyObservers(SourcesChangeEvent.clearAllEvent);
	}
	
	/**
	 * Get a system file (name) from a (relative) source name as returned by dbi.
	 */
	private File resolveToSystemSourceFile(String sourcePath){
		for (String s:searchPaths){
			File sysPath=new File(s, sourcePath);
			if(sysPath.exists()){
				return sysPath;
			}
		}
		return null;
	}
	
	public static class SourcesChangeEvent {
		public static final int ADDED=0;
		public static final int CLEAR_ALL=1;
		// A kinda singleton for clear all event.
		private static final SourcesChangeEvent clearAllEvent=new SourcesChangeEvent(CLEAR_ALL, null, null);
		private int tag;
		private String sourcePath;
		private File systemPath;
		private SourcesChangeEvent(String sourcePath, File systemPath){
			this(ADDED, sourcePath, systemPath);
		}
		private SourcesChangeEvent(int tag, String sourcePath, File systemPath){
			this.setTag(tag);
			this.setSourcePath(sourcePath);
			this.setSystemPath(systemPath);
		}
		private void setTag(int tag) {
			this.tag = tag;
		}
		public int getTag() {
			return tag;
		}
		private void setSourcePath(String sourcePath) {
			this.sourcePath = sourcePath;
		}
		public String getSourcePath() {
			return sourcePath;
		}
		public void setSystemPath(File systemPath2) {
			this.systemPath = systemPath2;
		}
		public File getSystemPath() {
			return systemPath;
		}
	}
}
