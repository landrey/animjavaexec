package fr.loria.madynes.animjavaexec.view;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import fr.loria.madynes.animjavaexec.execution.model.OutputManager;
import fr.loria.madynes.javautils.Properties;

public class OutStreamView extends JPanel  {
	public static final int SHOW_OUT=0x01;
	public static final int SHOW_ERR=0x02;
	public static final int SHOW_UNCAUGHT_EXCEPTION=0x04;
	private static final long serialVersionUID = 1L;
	
	private JTextArea textArea;
	private Observer outputObserver;
	private int showWhat;

	/**
	 * 
	 * @param showWhat use expression as 
	 * {@link OutStreamView.SHOW_OUT}|{@link OutStreamView.SHOW_ERR}|{@link OutStreamView.SHOW_UNCAUGHT_EXCEPTION}
	 * @param r number of rows
	 * @param c number of columns
	 */
	OutStreamView (int showWhat, int r, int c)  {
		super(new GridBagLayout());
		this.showWhat=showWhat;
		this.textArea=new JTextArea();
		String textTip="";
		if((showWhat&SHOW_OUT)!=0){
			textTip+=Properties.getMessage("fr.loria.madynes.animjavaexec.view.OutStreamView.outtip")+"\n";
		}	
		if((showWhat&SHOW_ERR)!=0){
			textTip+=Properties.getMessage("fr.loria.madynes.animjavaexec.view.OutStreamView.errtip")+"\n";
		}
		if((showWhat&SHOW_UNCAUGHT_EXCEPTION)!=0){
			textTip+=Properties.getMessage("fr.loria.madynes.animjavaexec.view.OutStreamView.uncaughtexeceptiontip")+"\n";
		}
		this.textArea.setToolTipText(textTip);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setPreferredSize(new Dimension(c*10, r*10));
		//scrollPane.setPreferredSize(new Dimension(1000, 1000));
		//Add Components to this panel.
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridwidth = GridBagConstraints.REMAINDER;

		//c.fill = GridBagConstraints.HORIZONTAL;
		//add(textField, c);

		gbc.fill = GridBagConstraints.BOTH;
		gbc.weightx = 1.0;
		gbc.weighty = 1.0;
		add(scrollPane, gbc);
	}
	
	void observe(OutputManager outputManager){
		if (this.outputObserver==null){
			this.outputObserver=new Observer(){
				@Override
				public void update(Observable outputManager, Object evt) {
					OutputManager.OutputChangeEvent outputChangeEvent=(OutputManager.OutputChangeEvent)evt;
					switch(outputChangeEvent.getTag()){
					case OutputManager.OutputChangeEvent.OUT_CHANGE:
						if ((showWhat&SHOW_OUT)!=0){
							textArea.append(outputChangeEvent.getStuff());
						}
						break;
					case OutputManager.OutputChangeEvent.ERR_CHANGE:
						if ((showWhat&SHOW_ERR)!=0){
							textArea.append(outputChangeEvent.getStuff());
						}
						break;
					case OutputManager.OutputChangeEvent.UNCAUGHT_EXCEPTION_OCCURRED:
						if ((showWhat&SHOW_UNCAUGHT_EXCEPTION)!=0){
							textArea.append(outputChangeEvent.getStuff());
						}
						break;
					}
				}
			};
		}
		outputManager.addObserver(this.outputObserver);
	}//observe
}
