package fr.loria.madynes.animjavaexec.view2;

import java.awt.Image;
import javax.swing.JFrame;

import fr.loria.madynes.animjavaexec.CommandForwarder;
import fr.loria.madynes.animjavaexec.CommandGenerator;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;

public class FiltersFrame extends JFrame implements CommandGenerator {
	//TODO: do we really need to be a commandGenerator ?
	private static final long serialVersionUID = 1L;
	public FiltersFrame(String windowTitle, Image wIcone, ExecutionModel executionModel){
		if (windowTitle!=null){
			this.setTitle(windowTitle);
		}
		if (wIcone!=null){
			this.setIconImage(wIcone);
		}
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		getContentPane().add(new FiltersView(executionModel.getFiltersManager()));
		pack();
		setVisible(true);
	}
	@Override
	public void generateToForwarder(CommandForwarder commandForwarder) {
	}

}
