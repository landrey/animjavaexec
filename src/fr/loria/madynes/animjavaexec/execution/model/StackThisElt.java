package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.ObjectReference;

public class StackThisElt extends StackElt {
	
	public StackThisElt(ObjectReference ref){
		super("this", null, ref, ref);
		assert ref!=null;
	}

	@Override
	public StackEltTag getTag() {
		return StackEltTag.THIS;
	}
}
