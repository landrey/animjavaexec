package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Font;
import java.text.MessageFormat;
import java.util.Vector;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;

import fr.loria.madynes.javautils.Properties;

public class InstanceView extends InstanceOrArrayView {
	private static final long serialVersionUID = 1L;
	private static MessageFormat headerFormat=
        new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.memoryview.InstanceView.headerFormat"));
	private static MessageFormat headerTipFormat=
        new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.memoryview.InstanceView.headerTipFormat"));
	//private static Color instanceHeadColor=Color.cyan;
	//private static Color fieldColor=Color.MAGENTA; 
	
	private Vector<NameFieldValueView> fieldsView=new  Vector<NameFieldValueView>();
	
	public InstanceView(MemoryView memoryView, 
			ObjectReference instance,
			MemoryView memoryView2, int x, int y) {
		super(memoryView, instance, x, y, memoryView.getInstanceHeaderWidth(), memoryView.getInstanceHeaderHeight(), ViewEltTag.INSTANCE_HEADER);
		// super add header in memoryView the overall container. InstanceView is NOT a container.
		memoryView.addInstanceView(instance, this);
		
		String className=instance.referenceType().name();
		String shortClassName=className.substring(className.lastIndexOf('.')+1);
		this.setHeaderText(headerFormat.format(new Object[]{shortClassName, className, instance.uniqueID()}));
		this.setHeaderTip(headerTipFormat.format(new Object[]{shortClassName, className, instance.uniqueID()}));
		int instanceHeaderHeight=memoryView.getInstanceHeaderHeight();
		int instanceHeaderWidth=memoryView.getInstanceHeaderWidth();
		int instanceFieldHeight=memoryView.getInstanceFieldHeight();
		int curY=y+instanceHeaderHeight;
		for (Field f:instance.referenceType().allFields()){
			NameFieldValueView nv=new NameFieldValueView(this.getMemoryView(), f.name(), instance, f,
					x, curY, instanceHeaderWidth, instanceFieldHeight,   
					ViewEltTag.INSTANCE_VAR);
			fieldsView.add(nv);
			memoryView.add(nv);		
			//gm.insert(new Object[]{fVtx}, attributes, null, null, null);
			this.incViewHeight(instanceFieldHeight);
			curY+=instanceFieldHeight;
		}
		this.fieldsView.trimToSize();
	}

	@Override
	protected void removeElts() {
		for (NameFieldValueView nv:this.fieldsView){
			//this.getMemoryView().remove(nv); // memoryView.instanceToViewMap is not update because we can be into in Iterator of this map...
			nv.remove(); // clear from link and remove from owner panel
		}
	}

	@Override
	protected void translateElts(int dX, int dY) {
		for (NameFieldValueView nv:this.fieldsView){
			nv.setLocation(nv.getX()+dX, nv.getY()+dY);
		}
	}

	@Override
	protected void moveElementsTo(int x, int y) {
		for (NameFieldValueView nv:this.fieldsView){
			nv.setLocation(x,y);
		}
	}

	@Override
	/**
	 * Call by InstanceOrArrayView.adjustFontAndSize()
	 */
	protected void adjustEltsFontAndSize() {
		MemoryView memoryView=this.getMemoryView();
		int instanceHeaderHeight=memoryView.getInstanceHeaderHeight();
		//int instanceHeaderWidth=memoryView.getInstanceHeaderWidth();
		int instanceFieldHeight=memoryView.getInstanceFieldHeight();
		int instanceFieldWidth=memoryView.getInstanceHeaderWidth(); //TODO add a MemoryView.getInstanceFieldWidth()
		Font font=memoryView.getEltFont(); // TODO add a MemoryView.getInstanceFieldFont
		int curY=this.getY()+instanceHeaderHeight;
		int x=this.getX();
		for (NameFieldValueView nfvv:this.fieldsView){
			nfvv.adjustFontAndBounds(x, curY, instanceFieldWidth, instanceFieldHeight, font);
			this.incViewHeight(instanceFieldHeight);
			curY+=instanceFieldHeight;
		}
	}

	@Override
	Font getHeaderFont() {
		return this.getMemoryView().getHeaderFont();//TODO: add a MemoryView.getInstanceHeaderFont()
	}

	@Override
	int getHeaderHeight() {
		return this.getMemoryView().getInstanceHeaderHeight(); 
	}

	@Override
	int getHeaderWidth() {
		return this.getMemoryView().getInstanceHeaderWidth();
	}

}
