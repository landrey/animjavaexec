package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Value;

public class NameArrayElementValueView extends NameNotFixedValueView {
	private static final long serialVersionUID = 1L;
	
	private ArrayReference arrayReference;
	private int index;

	/**
	 * View for an element into an array at a fixed index.
	 * @param memoryView
	 * @param arrayReference
	 * @param index
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 * @param bgColor
	 */
	NameArrayElementValueView(MemoryView memoryView, ArrayReference arrayReference, int index,  int x, int y,
			int width, int height, ViewEltTag tag) {
		super(memoryView, Integer.toString(index), x, y, width, height, tag);
		this.arrayReference=arrayReference;
		this.index=index;
	}

	@Override
	Value getCurrentValue() {
		if (this.arrayReference!=null){ // avoid access to non initialized current object at construction time.
			return this.arrayReference.getValue(this.index);
		}else{
			return null;
		}
		
	}

}
