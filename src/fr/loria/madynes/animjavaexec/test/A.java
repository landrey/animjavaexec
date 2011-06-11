package fr.loria.madynes.animjavaexec.test;

public class A {
	int a;
	A(int a){
		int b=0;
		b++;
		this.a=a;
	}
	void m(){
		System.out.println("** a="+a);
	}
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String s="toto";
		System.out.println(s);
		A a=new A(10);
		a.m();
		a.m();
		A b=new A(20);
		b.m();
	}

}
