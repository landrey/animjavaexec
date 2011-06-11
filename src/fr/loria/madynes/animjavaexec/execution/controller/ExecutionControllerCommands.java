package fr.loria.madynes.animjavaexec.execution.controller;

public interface ExecutionControllerCommands {
	void abort();
	void exec(String className) throws ClassNotFoundException, Exception;
	void step();
	void addToStdIn(String s);
	/**
	 *  
	 * @param includeFilters null if not changed (use empty array when no filters are wanted.
	 * @param excludeFilters null if not changed (use empty array when no filters are wanted.
	 */
	void changeFilters(String[] includeFilters, String[] excludeFilters);
}
