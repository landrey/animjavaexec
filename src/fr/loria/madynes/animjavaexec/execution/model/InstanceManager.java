package fr.loria.madynes.animjavaexec.execution.model;

import java.util.HashSet;
import java.util.Observable;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;

/** manager to register and forward change on instance (ObjectReference).
 * 
 * @author andrey
 *
 */
public class InstanceManager extends Observable {
	private ExecutionModel executionModel;
	private Set<ObjectReference> managedRegularObjects=new HashSet<ObjectReference>();
	InstanceManager(ExecutionModel em){
		executionModel=em;
	}
	
	/**
	 * The controller must call this method when it detects a possibly new instance.
	 * @param or the possibly new instance.
	 * @return
	 */
	public ManageReturn manage(ObjectReference or){
		if (or==null){ // Rmk: (null instanceof X) is always false.
			return ManageReturn.NOT_MY_CONCERN;
		}if (or instanceof ArrayReference){
			// jdpa can not detected array creation: 
			// see: http://forums.sun.com/thread.jspa?threadID=5326536 
			// 
			System.out.println("ArrayReference not yet supported"); // TODO: log it. Implement it.
			return ManageReturn.NOT_MY_CONCERN;
		}else if( or instanceof ClassLoaderReference 
				|| or instanceof ClassObjectReference 
				|| or instanceof ThreadGroupReference
				|| or instanceof ThreadReference
				|| or instanceof StringReference){
			// String a unmutable object => not need to be observed.
			// They are represented as primitive type for fields and stack elt values.
			// other objects are not displayed (anyway most of them would be filtered (see FiltersManager)in common toy examples.
			return ManageReturn.NOT_MY_CONCERN;
		}else{
			// Reference on a regular object..
			if (this.managedRegularObjects.contains(or)){
				return ManageReturn.ALREADY_MANAGED;
			}else{
				this.managedRegularObjects.add(or);
				this.setChanged();
				this.notifyObservers(new InstanceChangeEvent(or));
				return ManageReturn.ADDED;
			}
		}
	}
	public void fieldChange(ObjectReference or, Field fd, Value valueToBe){	
		if (this.managedRegularObjects.contains(or)){
			this.setChanged();
			this.notifyObservers(new InstanceChangeEvent(or, fd, valueToBe));
			//UGLY HACK:  array creation can not be detected via a jdi event...
			if (valueToBe instanceof ArrayReference){ 
				//valueToBe=(ObjectReference)value; // ArrayReference is a sub interface of objectReference...
				this.executionModel.getArrayManager().manage((ArrayReference)valueToBe);
			}
		}else{
			Logger.getAnonymousLogger().logp(Level.WARNING, this.getClass().getName(),  "fieldChange", 
											 "applyied non managed object (no Watchpoint set on object ?):"+or+"->"+fd);
		}
	}
	public void clear() {
		this.managedRegularObjects.clear();
		this.setChanged();
		this.notifyObservers(InstanceChangeEvent.clearAllEvent);
	}
	
	public static class InstanceChangeEvent {
		public static final int CREATION=0;
		public static final int FIELD_CHANGED=1;
		public static final int CLEAR_ALL=2;
		// A kinda singleton for clear all event.
		private static final InstanceChangeEvent clearAllEvent=new InstanceChangeEvent(CLEAR_ALL, null, null, null);
		
		private int tag;
		private ObjectReference or;
		private Field fd;
		private Value toBeValue;
		// Object instanciation...
		private InstanceChangeEvent(ObjectReference or){
			this(CREATION, or, null, null);
		}
		// Field instance change
		private  InstanceChangeEvent(ObjectReference or, Field fd, Value toBeValue){
			this(FIELD_CHANGED, or, fd, toBeValue);
		}
		private InstanceChangeEvent(int tag, ObjectReference or, Field fd, Value toBeValue){
			this.setTag(tag);
			this.or=or;
			this.fd=fd;
			this.toBeValue=toBeValue;
		}
		private void setTag(int tag) {
			this.tag=tag;
		}
		public ObjectReference getInstance(){
			return or;
		}
		public Field getField(){
			return fd;
		}
		public Value getToBeValue() {
			return toBeValue;
		}
		public int getTag() {
			return tag;
		}
	}
}
