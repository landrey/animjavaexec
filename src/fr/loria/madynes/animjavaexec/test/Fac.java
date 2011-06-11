package fr.loria.madynes.animjavaexec.test;

public class Fac {
	static int fac(int n){
		if(n==0){
			return 1;
		}else{
			return fac(n-1)
					       *n;
		}
	}
			
	public static void main(String[] args) {
		fac(3);
	}

}
