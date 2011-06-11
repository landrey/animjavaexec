package fr.loria.madynes.animjavaexec.gui;

import javax.swing.JFrame;
import javax.swing.JMenu;

import fr.loria.madynes.javautils.Properties;

public class HelpMenu extends JMenu {
	public 	HelpMenu(JFrame f){
		super(Properties.getMessage("fr.loria.madynes.animjavaexec.gui.HelpMenu.name"));
		
	}
}
