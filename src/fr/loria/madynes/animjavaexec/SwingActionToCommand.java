package fr.loria.madynes.animjavaexec;

import java.awt.event.ActionEvent;

import fr.loria.madynes.javautils.swing.AbstractActionFromProperties;

public class SwingActionToCommand extends AbstractActionFromProperties {
	private CommandForwarder commandForwarder;
	SwingActionToCommand(String propertiesPrefix, String command, CommandForwarder commandForwarder) {
		super(propertiesPrefix, command);
		this.commandForwarder=commandForwarder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String command=e.getActionCommand();
		// We could do this by reflection and invoke...
		if ("quit".equals(command)){
			this.commandForwarder.quit();
		}else if ("abort".equals(command)){
			this.commandForwarder.abort();
		}else if ("step".equals(command)){
			this.commandForwarder.step();
		}else if ("editpreferences".equals(command)){
			this.commandForwarder.editPreferences();
		}else{
			System.err.println("SwingActionToCommand.actionPerformed: "+command+" unknown command");//TODO: log it
		}
	}
	
}
