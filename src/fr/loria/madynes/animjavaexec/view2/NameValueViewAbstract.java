package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

public abstract class NameValueViewAbstract 
extends JPanel
implements ColorAdjustable {
	private static final long serialVersionUID = 1L;
	static Font valueFont=new Font("Serif", Font.PLAIN, 12); // TODO: remove ? Really used ?
	static Border valueBorder=BorderFactory.createLineBorder(Color.black);
	
	private final MemoryView memoryView;
	private JLabel nameLabel;
	private JLabel valueLabel=null;
	private ViewEltTag tag;
	//private final JLabel valueLabel;
	NameValueViewAbstract(MemoryView memoryView, String name, int x, int y, int width, int height, ViewEltTag tag,
						  JLabel valueLabel){
		this.memoryView=memoryView;
		this.tag=tag;
		this.setLayout(null);
		this.setBounds(x, y, width, height);
		//this.setBackground(bgColor);
		this.setBackground(memoryView.getViewEltColor(tag));
		this.setBorder(valueBorder);
		nameLabel=new JLabel(name);
		nameLabel.setToolTipText(name);
		nameLabel.setBounds(0, 0, width/2, height);
		nameLabel.setBorder(valueBorder);
		nameLabel.setFont(memoryView.getEltFont()); // to do stack, array, attribute customization...
		this.add(nameLabel);
		if (valueLabel!=null){
			setValueLabel(valueLabel);
		}
		//this.add(valueLabel);
	//	this.valueLabel=valueLabel;
	}

	NameValueViewAbstract(MemoryView memoryView, String name, int x, int y, int width, int height, ViewEltTag tag){
		this(memoryView, name, x, y, width, height, tag, null);
	}
	
	MemoryView getMemoryView() {
		return memoryView;
	}
	
	/**
	 * Remove from swing and clear any stuff...
	 */
	abstract void remove();
	
	/** Set the value part of this panel + 2 JLabel.
	 * This is provided to allow value label set-up AFTER construction time when 
	 * enclosing object has its instance variables properly initialized.
	 * The valueLabel parameter is correctly resized and set with a border.
	 * 
	 * @param valueLabel
	 */
	void setValueLabel(JLabel valueLabel){
		this.valueLabel=valueLabel;
		int width=this.getWidth();
		// TODO: clean: System.out.println(width/2+ " "+0+" "+width/2+ " "+this.getHeight());
		valueLabel.setBounds(width/2, 0, width/2, this.getHeight());
		valueLabel.setBorder(valueBorder);
		this.add(valueLabel);
	}
	
	JLabel getValueLabel(){
		return this.valueLabel;
	}
	void adjustFontAndBounds(int x, int y,int width, int height, Font font){
		this.setBounds(x, y, width, height);
		this.nameLabel.setBounds(0, 0, width/2, height);
		this.nameLabel.setFont(font);
		this.valueLabel.setBounds(width/2, 0, width/2, height);
		this.valueLabel.setFont(font);
	}
	public void adjustColor(){
		this.setBackground(this.memoryView.getViewEltColor(this.tag));
	}
}
