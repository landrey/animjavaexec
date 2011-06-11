package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public class StackReturnElt extends StackElt {
	public StackReturnElt(String value, Value trueValue, ObjectReference ref){
		super(null, value, trueValue, null);
	}
	public StackReturnElt(ObjectReference ref){ //TODO: what for ? Remove ?
		super(null, null, ref, ref);
	}
	@Override
	public boolean isReturn(){
		return true;
	}
	@Override
	public StackEltTag getTag() {
		return StackEltTag.RETURN;
	}
}
