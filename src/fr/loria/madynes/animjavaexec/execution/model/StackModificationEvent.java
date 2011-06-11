package fr.loria.madynes.animjavaexec.execution.model;

public class StackModificationEvent {
	private StackElt[] removedElts=null;
	private int newStartIdx=-1;
	
	public StackModificationEvent(StackElt[] removedElts, int newStartIdx){
		this.removedElts=removedElts;
		this.newStartIdx=newStartIdx;
	}
	public void setRemovedElt(StackElt[] removedElt) {
		this.removedElts = removedElt;
	}
	public StackElt[] getRemovedElt() {
		return removedElts;
	}
	public void setNewStartIdx(int newStartIdx) {
		this.newStartIdx = newStartIdx;
	}
	public int getNewStartIdx() {
		return newStartIdx;
	}
	
}
