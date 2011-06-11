package fr.loria.madynes.animjavaexec.execution.model;

import java.util.Observable;
import java.util.Stack;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;


import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Location;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import fr.loria.madynes.animjavaexec.jpdautils.ValueAccess;

public class ExecStack extends Observable {
	private ExecutionModel execModel;
	private Stack<StackElt> elts;
	ExecStack(ExecutionModel em){
		this.execModel=em;
	}
	public void update(ThreadReference tr, boolean hasReturnValue, Value returnValue){
		Stack<StackElt> newStack=new Stack<StackElt>();	
		try {
			for (int fi=tr.frameCount()-1; fi>=0; --fi){
				StackFrame sf=tr.frame(fi);
				Location loc=sf.location();
				Method inMethod=loc.method();
				if (inMethod!=null){
					newStack.push(new StackSeparator(inMethod));
				}else{
					Logger.getAnonymousLogger().logp(Level.WARNING, this.getClass().getName(), "update", "no current method...");
				}
				ObjectReference thisO=sf.thisObject();
				if (thisO!=null){
					newStack.push(new StackThisElt(thisO));
				}
				try {
					for (LocalVariable lv:sf.visibleVariables()){
						pushValue(newStack, lv.isArgument()?StackEltTag.PARAM:StackEltTag.VAR, lv.name(), sf.getValue(lv));
					}
				} catch (AbsentInformationException e) {
					//TODO: better error message to user...
					// Shit: how to handle error in mvc.
					// Errors ARE PART OF THE MODEL !
					Logger.getLogger("").logp(Level.WARNING,
							this.getClass().getName(), "update",
							"INTERNAL ERROR: AbsentInformationException: Did you compile animated coded with -g option ?"+e);
				}//for LocalVariable
			}
		} catch (IncompatibleThreadStateException e) {
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "update",
					"INTERNAL ERROR: IncompatibleThreadStateException: "+e);
		}

		if (hasReturnValue){
			pushValue(newStack, StackEltTag.RETURN, "return", returnValue);
		}
		newStack.trimToSize();
		this.elts = newStack;
		this.setChanged();
		this.notifyObservers();
	}

	private  void pushValue(Stack<StackElt> onStack, StackEltTag stg, String name, Value value){
		String strValue=null;
		ObjectReference ref=null;
		if (value==null){
			strValue="null"; // TODO: Should be in view.
		}else if ((value instanceof PrimitiveValue)||(value instanceof StringReference)){
			strValue=value.toString();
		}else{
			// TODO: check non supported cases and support more cases.
			// TODO: string formating must be done by view..!!!
			if (value instanceof ArrayReference){
				ref=(ObjectReference)value; // ArrayReference is a sub interface of objectReference, this is safe.
				this.execModel.getArrayManager().manage((ArrayReference)ref);
			}else{
				try{
					ref=(ObjectReference)value;
				}catch(ClassCastException cce){
					LogManager.getLogManager().getLogger("").logp(Level.WARNING,
							this.getClass().getName(), "update", value.getClass().getName()+" not (yet) supported");
					// TODO: externalize.
				}
			}
		}
		switch(stg){
		case PARAM:
			onStack.push(new StackParamElt(name, strValue, value, ref));
			break;
		case VAR:
			onStack.push(new StackVarElt(name, strValue, value, ref));
			break;
		case RETURN:
			onStack.push(new StackReturnElt(strValue, value, ref));
			break;
		default:	
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "pushValue",
					"INTERNAL ERROR: Illegal case (StackEltTag)");
			break;
		}
	}
	public void clear() { // Perf ?
		this.elts=new Stack<StackElt>();
		this.setChanged();
		this.notifyObservers();
	}
	public  Stack<StackElt> getElts() {
		return elts;
	}
	public int getEltsCount(){
		return elts.size();
	}
	
}
