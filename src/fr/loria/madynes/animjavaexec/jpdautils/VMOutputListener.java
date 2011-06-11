package fr.loria.madynes.animjavaexec.jpdautils;

public interface VMOutputListener {
	/** Write some new OUTPUT of a VM INTO this listener
	 * 
	 * @param cbuf
	 * @param count
	 * 
	 * @see fr.loria.madynes.animjavaexec.jpdautils.SimpleTrace.addStdErrListener(VMOutputListener)
	 * @see fr.loria.madynes.animjavaexec.jpdautils.SimpleTrace.addStdOutListener(VMOutputListener)
	 */
	void write(char[] cbuf, int count);
	void flush();
}
