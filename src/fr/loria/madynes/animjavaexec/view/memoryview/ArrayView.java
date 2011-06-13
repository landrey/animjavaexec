package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Color;
import java.awt.Font;
import java.text.MessageFormat;

import com.sun.jdi.ArrayReference;

import fr.loria.madynes.javautils.Properties;

public class ArrayView extends InstanceOrArrayView {
	private static final long serialVersionUID = 1L;
	private static MessageFormat headerFormat=
        new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.memoryview.ArrayView.headerFormat"));
	private static MessageFormat headerTipFormat=
        new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.memoryview.ArrayView.headerTipFormat"));
	//private static final Color arrayHeaderColor=Color.pink; 
	//private static Color lengthColor=Color.yellow;
	//private static Color eltColor=Color.ORANGE; // TODO: push to properties.
	private NameFixedValueView lengthView;
	private NameArrayElementValueView[] eltView;
	
	ArrayView(MemoryView mv, ArrayReference arrayReference, int x, int y) {
		super(mv, arrayReference, x, y, mv.getArrayHeaderWidth(), mv.getArrayHeaderHeight(), ViewEltTag.ARRAY_HEADER);
		// super add header in memoryView the overall container. InstanceViewis NOT a container.
		
		mv.addArrayReferenceView(arrayReference, this); // to allow self references.
		
		String className=arrayReference.referenceType().name();
		String shortClassName=className.substring(className.lastIndexOf('.')+1);
		this.setHeaderText(headerFormat.format(new Object[]{shortClassName, className, arrayReference.uniqueID()}));
		this.setHeaderTip(headerTipFormat.format(new Object[]{shortClassName, className, arrayReference.uniqueID()}));
		eltView=new NameArrayElementValueView[arrayReference.length()];
		int arrayHeaderHeight=mv.getArrayHeaderHeight(); // TODO use this.getArrayHeaderHeight()
		int arrayHeaderWidth=mv.getArrayHeaderWidth();   // TODO use this.getArrayHeaderWidth()
		int arrayEltHeight=mv.getArrayEltHeight();       
		// this.incViewHeight(arrayHeaderHeight);  // bug, already done in super(...)
		int curY=y+arrayHeaderHeight; 
		
		this.lengthView=new  NameFixedValueView(this.getMemoryView(), "length", Integer.toString(arrayReference.length()),
				x, curY, arrayHeaderWidth, arrayEltHeight,   
				ViewEltTag.ARRAY_LENGTH);
		this.getMemoryView().add(this.lengthView); 
		this.incViewHeight(arrayEltHeight);
		curY+=arrayEltHeight;
		for (int i=0; i<arrayReference.length(); ++i){
			NameArrayElementValueView nv=new NameArrayElementValueView(this.getMemoryView(), arrayReference, i, 
					x, curY, arrayHeaderWidth, arrayEltHeight, 
					ViewEltTag.ARRAY_ELT);
			this.eltView[i]=nv;
			this.getMemoryView().add(nv);
			this.incViewHeight(arrayEltHeight);
			curY+=arrayEltHeight;
		}
	}

	@Override
	protected void removeElts() { // remove from memory view the top level container of our story.
		this.lengthView.remove();
		for (NameArrayElementValueView nv:this.eltView){
			// TODO: remove arrow
			//this.getMemoryView().remove(nv); // remove from panel
			nv.remove(); // clear from link and remove from owner panel
			// TODO: remove from this.getMemoryView().arrayToViewMap... NO we could be into an iterator of this one.
		}
	}

	@Override
	protected void translateElts(int dX, int dY) {
		this.lengthView.setLocation(this.lengthView.getX()+dX, this.lengthView.getY()+dY);
		for (NameArrayElementValueView nv:this.eltView){
			nv.setLocation(nv.getX()+dX, nv.getY()+dY);
		}
	}

	@Override
	protected void moveElementsTo(int x, int y) {
		this.lengthView.setLocation(x, y);
		for (NameArrayElementValueView nv:this.eltView){
			nv.setLocation(x,y);
		}
	}

	@Override
	Font getHeaderFont() {
		return this.getMemoryView().getHeaderFont(); // TODO ArrayHeaderFont..
	}

	@Override
	int getHeaderHeight() {
		return this.getMemoryView().getArrayHeaderHeight();
	}

	@Override
	int getHeaderWidth() {
		return this.getMemoryView().getArrayHeaderWidth();
	}

	/**
	 *  Call by InstanceOrArrayView.adjustFontAndSize()
	 */
	@Override
	protected void adjustEltsFontAndSize() {
		MemoryView mv=this.getMemoryView();
		Font font=mv.getArrayEltFont();
		int arrayHeaderHeight=this.getHeaderHeight(); // TODO use this.getArrayHeaderHeight()
		//int arrayHeaderWidth=this.getHeaderWidth();   // TODO use this.getArrayHeaderWidth()
		int arrayEltHeight=mv.getArrayEltHeight();   
		int arrayEltWidth=mv.getArrayHeaderWidth(); // TODO arrayEltWidth ?   
		this.incViewHeight(arrayHeaderHeight);  // TODO: bug, already done in InstanceOrArrayView.adjusFontAndSize()
		int x=this.getX();
		int curY=this.getY()+arrayHeaderHeight; 
		this.lengthView.adjustFontAndBounds(x, curY, arrayEltWidth, arrayEltHeight, font);
		this.incViewHeight(arrayEltHeight);
		curY+=arrayEltHeight;
		for (NameArrayElementValueView aev:this.eltView){
			aev.adjustFontAndBounds(x, curY, arrayEltWidth, arrayEltHeight, font);
			this.incViewHeight(arrayEltHeight);
			curY+=arrayEltHeight;
		}
	}
}
