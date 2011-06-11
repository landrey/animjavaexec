package fr.loria.madynes.animjavaexec.test;

public class B {
	int a;
	B b;
	B(int a){
		this.a=a;
	}
	B(int a, B b){
		this.a=a;
		this.b=b;
	}
	public static void main(String[] args) {
		int i=0;
		B a=new B(i++);
		B b=new B(i++, a);
		B c=new B(i++, b);
		B d=new B(i++, c);
	}

}
