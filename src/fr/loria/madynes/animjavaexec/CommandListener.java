package fr.loria.madynes.animjavaexec;

import fr.loria.madynes.animjavaexec.execution.controller.ExecutionControllerCommands;

public interface CommandListener extends ExecutionControllerCommands {
	// general command
	void editPreferences();
	void editFilters();
	void quit();
	
	// execution controller commands. See ExecutionControlerCommands
	//void abort();
	//void exec(String className);
	//void step();
}
