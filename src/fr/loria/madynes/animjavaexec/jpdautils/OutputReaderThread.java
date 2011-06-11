package fr.loria.madynes.animjavaexec.jpdautils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Vector;

class OutputReaderThread extends Thread {
	private final Reader in;

    private static final int BUFFER_SIZE = 2048;
    
    private Vector<VMOutputListener> listeners=new Vector<VMOutputListener>();
    
    /**
     * 
     * @param in one OUT of a VirtualMachine.process().... 
     */
    OutputReaderThread(String name, InputStream in){
    	super(name);
    	this.in=new InputStreamReader(in);
    	setPriority(Thread.MAX_PRIORITY-1);
    }
    
    OutputReaderThread(InputStream in){
    	this("OutputReaderThread", in);
    }
    void addListener(VMOutputListener l){
    	// NOTE: Vector is thread safe.
    	this.listeners.add(l);
    }

    public void run() {
        try {
        	char[] cbuf = new char[BUFFER_SIZE];
        	int count;
        	while ((count = in.read(cbuf, 0, BUFFER_SIZE)) >= 0) {
        		//TODO: clean : System.out.println("OutputReaderThread - read"+(new String(cbuf, 0, count)));
        		//out.write(cbuf, 0, count);
        		for (VMOutputListener l:listeners){
        			l.write(cbuf, count);
        		}
        	}
        	//out.flush();
        	for (VMOutputListener l:listeners){
    			l.flush();
    		}
        } catch(IOException exc) {
        	System.err.println("Child I/O Transfer - " + exc);
        }
    }
}
