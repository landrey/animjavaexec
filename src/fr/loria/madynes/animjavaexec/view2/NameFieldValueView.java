package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

public class NameFieldValueView extends NameNotFixedValueView {
	private static final long serialVersionUID = 1L;
	
	private ObjectReference instance;
	private Field field;

	NameFieldValueView(MemoryView memoryView, String name, ObjectReference instance, Field field,  
			int x, int y, int width, int height, ViewEltTag tag) {
		super(memoryView, name, x, y, width, height, tag);
		this.instance=instance;
		this.field=field;
		this.postNew();
	}

	@Override
	Value getCurrentValue() {
		if (this.instance!=null){// avoid access to non initialized current object at construction time.
			return this.instance.getValue(this.field);
		}else{
			return null;
		}
	}

}
