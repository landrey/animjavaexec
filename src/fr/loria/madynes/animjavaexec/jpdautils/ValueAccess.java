package fr.loria.madynes.animjavaexec.jpdautils;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

/** A dispatcher (~ Visitor pattern) for jdi Value.
 * 
 * @author andrey
 *
 */
public abstract class ValueAccess {
	/** Dispatch on va methods according to value actual type....
	 * 
	 * @param v value to dispatch
	 * @param va object which will to the work according to v actual type.
	 */
	public static void access(Value v, ValueAccessor va){
		if (v==null){
			va.accessNull();
		}if (v instanceof BooleanValue) {
			va.accessBoolean((BooleanValue)v);
		}else if (v instanceof IntegerValue){
			va.accesInt((IntegerValue) v);
		}else if (v instanceof StringReference){
			va.accessString((StringReference)v);
		}else if (v instanceof ArrayReference){ // This test happends BEFORE ObjectReference as ArrayReference is a SUB class of ObjectReference...
			va.accessArrayReference((ArrayReference) v);
		}else if (v instanceof ObjectReference){
			va.accesInstance((ObjectReference)v);
		}else if (v instanceof VoidValue){
			va.accessVoid((VoidValue)v);
		}else{
			assert false:"Not implemented"; //TODO continue dispatch...
		}
	}
}
