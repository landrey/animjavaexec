package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public abstract class StackElt {
	protected String name;
	protected String value;
	protected ObjectReference ref;
	private Value trueValue;
	public  boolean isSeparator(){return false;}
	public  boolean isVar(){return false;}
	public  boolean isParam(){return false;}
	public  boolean isThis(){return false;}
	public  boolean isReturn(){return false;}
	public  boolean isValue(){return value!=null;}
	public  boolean isReference(){return ref!=null;}
	public String getName(){return name;}
	public String getValue(){return value;}
	public Object getRef(){return ref;}
	public Value getValue2() {return this.trueValue;}
	public Method getInMethod(){ return null;} // see StackSeperator
	public StackElt(String name, String value, Value trueValue, ObjectReference ref){
		this.name=name;
		this.value=value;
		this.ref=ref;
		this.trueValue=trueValue; // TODO: remove String value indeed...
	}
	public String toString(){
		String result=getName()+":";
		String v=getValue();
		if (v!=null){
			result+=v+":";
		}
		if (ref!=null){
			result+="@"+ref.uniqueID();
		}
		return result;
	}
	public abstract StackEltTag getTag();

}
