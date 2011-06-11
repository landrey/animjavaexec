package fr.loria.madynes.animjavaexec.test.strlist;

public class TestStrList {
	public static void main(String[] args){
		StrList l=new StrList();
		l.add("toto");
		l.add("titi");
		l.add("tutu");
		l.removeHead();
		System.out.println(l.getHead());
	}
}
