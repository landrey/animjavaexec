package fr.loria.madynes.animjavaexec.execution.model;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public class StackParamElt extends StackElt{
	public StackParamElt(String name, String value, Value trueValue, ObjectReference ref){
		super(name, value, trueValue, ref);
	}
	public StackParamElt(String name, String value, Value trueValue){
		super(name, value, trueValue, null);
		assert (name!=null) && ((value==null) || (ref==null));
	}
	public StackParamElt(String name, ObjectReference ref){
		super(name, null, ref, ref);
	}
	@Override
	public boolean equals(Object e){
		if (!(e instanceof StackParamElt)){ // null instanceof ... is ALWAYS FALSE.
				return false;
		}
		StackParamElt e_=(StackParamElt)e;
		return 	(this.name!=null && this.name.equals(e_.name)) // names are not supposed to be null
				&& ((this.value!=null && this.value.equals(e_.value)) 
				     ||( this.ref!=null && this.ref==e_.ref));
	}
	@Override
	public boolean isParam(){
			return true;
	}
	@Override
	public StackEltTag getTag() {
		// TODO Auto-generated method stub
		return StackEltTag.PARAM;
	}
}
