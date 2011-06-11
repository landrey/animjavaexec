package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;

import javax.swing.JLabel;

public class NameFixedValueView extends NameValueViewAbstract {

	private static final long serialVersionUID = 1L;

	NameFixedValueView(MemoryView memoryView, String name, String fixedValue,
			int x, int y, int width, int height, ViewEltTag tag) {
		super(memoryView, name, x, y, width, height, tag, null);
		JLabel vl=new JLabel(fixedValue);
		vl.setFont(memoryView.getEltFont());
		this.setValueLabel(vl);
	}
	
	NameFixedValueView(MemoryView memoryView, String name, String fixedValue, String valueTip,
			int x, int y, int width, int height, ViewEltTag tag) {
		//super(memoryView, name, x, y, width, height, bgColor, null);
		this(memoryView, name, fixedValue, x, y, width, height, tag);
		this.getValueLabel().setToolTipText(valueTip);
		//this.setValueLabel(valueLabel);
	}
	@Override
	void remove(){
		this.getMemoryView().remove(this);
	}
}
