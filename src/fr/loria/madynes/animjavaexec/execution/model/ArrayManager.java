package fr.loria.madynes.animjavaexec.execution.model;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;

import com.sun.jdi.ArrayReference;


public class ArrayManager extends Observable {
	private ExecutionModel exeModel;
	private static Set<ArrayReference> managedArrays=new HashSet<ArrayReference>();	
	ArrayManager(ExecutionModel em){
		this.exeModel=em;
	}
	
	public ManageReturn manage(ArrayReference ar){
		if (managedArrays.contains(ar)){
			return ManageReturn.ALREADY_MANAGED;
		}else{
			managedArrays.add(ar);
			this.setChanged();
			this.notifyObservers(new ArrayChangeEvent(ar));
			return ManageReturn.ADDED;
		}
	}
	
	public void updateAll(){
		this.setChanged();
		this.notifyObservers(ArrayChangeEvent.updateAllEvent);
	}
	
	public void clear() {
		this.managedArrays.clear();
		this.setChanged();
		this.notifyObservers(ArrayChangeEvent.clearAllEvent);
	}
	public static class ArrayChangeEvent {
		public static final int CREATION=0;
		public static final int UPDATE_ALL=1;
		public static final int CLEAR_ALL=2;
		// Singleton for updateAll event...
		// no factory, all is done within this file.
		private static ArrayChangeEvent updateAllEvent=new ArrayChangeEvent(UPDATE_ALL);
		private static ArrayChangeEvent clearAllEvent=new ArrayChangeEvent(CLEAR_ALL);
		ArrayReference ar; // for creation (1st detection); null otherwise.
		int tag;
		/**
		 * Array creation event (1st detection).
		 * @param ar
		 */
		private ArrayChangeEvent(ArrayReference ar){
			this.tag=CREATION;
			this.ar=ar;
		}
		/**
		 * Global refresh has sense... (or update all).
		 */
		private ArrayChangeEvent(int tag){
			this.tag=tag;
			this.ar=null;
		}
		public ArrayReference getArrayReference(){
			return this.ar;
		}
		public boolean isCreationEvent(){
			return (this.ar!=null);
		}
		public int getTag(){
			return this.tag;
		}
	}
}
