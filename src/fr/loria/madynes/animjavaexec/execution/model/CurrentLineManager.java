package fr.loria.madynes.animjavaexec.execution.model;

import java.util.Observable;

public class CurrentLineManager extends Observable {
	private ExecutionModel execModel;
	private String curSourcePath; // relative path, as returned by dbi (Location...). May be null (cleared state)
	private int curLineNumber; // may be -1 (cleared state).
	
	CurrentLineManager(ExecutionModel execModel){
		this.execModel=execModel;
		curSourcePath=null;
		curLineNumber=-1;
	}
	
	public void changeCurline(String sourcePath, int curLineNumber){
		if (sourcePath==null || curLineNumber==-1){
			boolean changed=this.curSourcePath!=null && this.curLineNumber!=-1;
			this.curSourcePath=null;
			this.curLineNumber=-1;
			if (changed){
				this.setChanged();
				this.notifyObservers();
			}
		}else{
			if (curLineNumber!=this.curLineNumber || !sourcePath.equals(this.curSourcePath)){ // Order is useful: short cut, and line varies more often that source file.
				this.curLineNumber=curLineNumber;
				this.curSourcePath=sourcePath;
				this.setChanged();
				this.notifyObservers(); // access to CurrentLIneManger to get new model state.
			}
		}
	}
	
	public String getCurrentSourcePath(){
		return this.curSourcePath;
	}
	public int getCurrentLineNumber(){
		return this.curLineNumber;
	}

	public void clear() {
		changeCurline(null, -1);
	}
}
