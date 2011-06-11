package fr.loria.madynes.animjavaexec.test;

public class Ti {
	int counti=0;
	I[] ti=new I[10];
	void add(int i){
		if (counti<ti.length){
			ti[counti]=new I(i);
			++counti;
		}else{
			System.out.println("Full Ti...");
		}
	}
}
