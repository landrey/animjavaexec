package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.Method;

public class StackSeparator extends StackElt {
	private Method inMethod;
	public StackSeparator(Method inMethod){
		super(null, null, null, null);
		this.setInMethod(inMethod);
	}

	@Override
	public StackEltTag getTag() {
		return StackEltTag.SEPARATOR;
	}

	private void setInMethod(Method inMethod) {
		this.inMethod = inMethod;
	}
	
	@Override
	public Method getInMethod() {
		return inMethod;
	}
	
}
