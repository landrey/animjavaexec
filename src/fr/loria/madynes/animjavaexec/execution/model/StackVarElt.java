package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public class StackVarElt extends StackElt {
	public StackVarElt(String name, String value, Value trueValue){
		super(name, value, trueValue, null);
		assert (name!=null) && ((value==null) || (ref==null));
	}
	public StackVarElt(String name, ObjectReference ref){
		super(name, null, ref, ref);
	}
	// TODO What is this case ?
	public StackVarElt(String name, String value, Value trueValue, ObjectReference ref){
		super(name, value, trueValue, ref);
	}
	@Override
	public boolean isVar(){
		return true;
	}
	@Override
	public StackEltTag getTag() {
		return StackEltTag.VAR;
	}
}
