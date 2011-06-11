package fr.loria.madynes.animjavaexec.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class TestStdIo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		BufferedReader	in= new BufferedReader(new InputStreamReader(System.in)); 
		String str="***";
		try {
	         str = in.readLine();
	         
		} catch (IOException ioe) {
			System.err.println("IO error trying to read user Input (Utile.lireString)!");
	    }
		System.out.println("Read: "+str);
	}

}
