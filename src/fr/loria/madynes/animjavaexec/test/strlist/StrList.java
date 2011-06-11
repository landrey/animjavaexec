package fr.loria.madynes.animjavaexec.test.strlist;


public class StrList  {
	private StrListElt head;
	public StrList(){
		head=null;
	}
	public void add(String val) {
		StrListElt e=new StrListElt(val);
		if (head==null){
			head=e;
		}else{
			e.setNext(head);
			head=e;
		}
	}
	public void removeHead() {
		if (head!=null){
			head=head.getNext();
		}
	}
	public String getHead() {
		if (head!=null){
			return head.getValue();
		}else{
			return null;
		}
	}

}
