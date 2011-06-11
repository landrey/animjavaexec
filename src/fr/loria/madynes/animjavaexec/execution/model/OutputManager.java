package fr.loria.madynes.animjavaexec.execution.model;

import java.util.Observable;

import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;

/**
 * Support (model) for what application produces: std out, std err and uncaught exception.
 * @author andrey
 *
 */
public class OutputManager extends Observable {
	private ExecutionModel execModel;

	OutputManager(ExecutionModel execModel){
		this.execModel=execModel;
	}

	public void addFromStandardOut(String stuff){
		if (stuff!=null){
			this.setChanged();
			this.notifyObservers(new OutputChangeEvent(OutputChangeEvent.OUT_CHANGE, stuff));
		}
	}
	public void addFromStandardError(String stuff){
		if (stuff!=null){
			this.setChanged();
			this.notifyObservers(new OutputChangeEvent(OutputChangeEvent.ERR_CHANGE, stuff));
		}
	}
	
	public void exceptionOccurred(ObjectReference or, ReferenceType rt) {
		this.setChanged();
		this.notifyObservers(new OutputChangeEvent(or, rt));
	}
	
	public static class OutputChangeEvent{ // public for constants, but private constructors...
		public static final int OUT_CHANGE=0;
		public static final int ERR_CHANGE=1;
		public static final int UNCAUGHT_EXCEPTION_OCCURRED=2;
		private int tag;
		private String stuff;
		private ObjectReference exception;
		private ReferenceType referenceType;
		// For out or error change
		private OutputChangeEvent(int tag, String stuff){
			this.setTag(tag);
			this.setStuff(stuff);
		}

		// for exception occurred
		private OutputChangeEvent(ObjectReference e, ReferenceType referenceType){
			this.setTag(UNCAUGHT_EXCEPTION_OCCURRED);
			this.setException(e);
			this.setReferenceType(referenceType);
		}
		private void setTag(int tag) {
			this.tag = tag;
		}

		public int getTag() {
			return tag;
		}

		private void setStuff(String stuff) {
			this.stuff = stuff;
		}

		public String getStuff() {
			return stuff;
		}

		private void setException(ObjectReference exception) {
			this.exception = exception;
		}

		private ObjectReference getException() {
			return exception;
		}

		public void setReferenceType(ReferenceType referenceType) {
			this.referenceType = referenceType;
		}

		public ReferenceType getReferenceType() {
			return referenceType;
		}
		public  String getExceptionDetailed(){
			if (tag==UNCAUGHT_EXCEPTION_OCCURRED){
				return exception.getValue(referenceType.fieldByName("detailMessage")).toString();
			}else{
				return "?";
			}
		}
	}


	
	                                                  
}
