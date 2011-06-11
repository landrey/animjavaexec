package fr.loria.madynes.animjavaexec.test.strlist;

public class StrListElt {
	private String stuff;
	private StrListElt next;
	StrListElt(String val){
		stuff=val;
		next=null;
	}
	void setNext(StrListElt e){
		next=e;
	}
	StrListElt getNext() {
		return next;
	}
	public String getValue() {
		return stuff;
	}
}
