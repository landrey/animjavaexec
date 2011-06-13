package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Color;

import com.sun.jdi.Value;

import fr.loria.madynes.animjavaexec.view.memoryview.NameNotFixedValueView.ValueView;


/**
 * A name value view for Stack Element.
 * The jdi value is directly set, and can not be changed.
 * @author andrey
 *
 */
public class NameDirectValueView extends NameNotFixedValueView {
	private static final long serialVersionUID = 1L;
	
	private Value value;

	NameDirectValueView(MemoryView memoryView,  String name, Value value, 
			int x, int y, int width, int height, ViewEltTag tag) {
		super(memoryView, name, x, y, width, height, tag);
		this.value=value;
		this.postNew();
	}

	@Override
	Value getCurrentValue() {
		return this.value;
	}
}
