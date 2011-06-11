package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JLabel;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StringReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import fr.loria.madynes.animjavaexec.jpdautils.ValueAccess;
import fr.loria.madynes.animjavaexec.jpdautils.ValueAccessor;
import fr.loria.madynes.javautils.Properties;


public abstract class NameNotFixedValueView extends NameValueViewAbstract {
	private static final long serialVersionUID = 1L;

	private static MessageFormat objectInstanceFormat=
        new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view2.NameValueView.objectInstanceFormat"));
	private static String nullText=Properties.getMessage("fr.loria.madynes.animjavaexec.view2.NameValueView.nullText");
	private static String voidText=Properties.getMessage("fr.loria.madynes.animjavaexec.view2.NameValueView.voidText");
	
	private LinkView linkStart;
	
	NameNotFixedValueView(MemoryView memoryView, String name, 
			int x, int y, int width, int height, ViewEltTag tag) {
		super(memoryView, name, x, y, width, height, tag);
		this.setValueLabel(new ValueView());
		// sub class this do things in constructor and then call postNew()
	}
	
	// To be called by sub classes constructors once  stuff for getCurrentValue() has been initialized...
	protected void postNew(){ 
		// Create possible link.
		Value value=this.getCurrentValue(); // lookup is effective at construction time in Java. Yes but NO. NameDirectValueView
		                                    // has no set value field yet... see postNew() in sub classes...
		if (MemoryView.isViewableReference(value)){ //TODO: with static Map indeed ?
			InstanceOrArrayView iav=this.getMemoryView().referenceToView((ObjectReference)value);
			// Instances or array Views should have been previously created after interaction with 
			// InstanceManager or Array Manager.
			if (iav==null){
				Logger.getLogger("").logp(Level.WARNING,
						this.getClass().getName(), "postNew", "view to ref failed..."+((ObjectReference)value).uniqueID());
			}else{
				Logger.getLogger("").logp(Level.FINEST,
						this.getClass().getName(), "postNew", "Create link....for "+((ObjectReference)value).uniqueID());
				this.linkStart=new LinkView(this.getMemoryView(), this, iav);
			}
		}
	}
	abstract Value getCurrentValue();

	
	void addLinkStart(MemoryView in, LinkView linkView) {
		if (this.linkStart!=null){
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "addLinkStart", "linkStart not null!"); 
			this.linkStart.remove(in);
		}
		this.linkStart=linkView;
	}
	
	void removeLink(LinkView linkView) {
		this.linkStart=null;
	}
	
	int getLinkSourceX() {
		return this.getX()+this.getWidth();
	}
	int getLinkSourceY() {
		return this.getY()+this.getHeight()/2;
	}
	
	@Override
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		// Check for change and update link.
		Value value;
		try{
			value=this.getCurrentValue();
		}catch(Throwable anyException){ // VM can be disconnected...
			value=null;
		}
		if (value==null){
			clearLink();
		}else{ 
			// check for change.
			if (MemoryView.isViewableReference(value)){ // => not null
				if (this.linkStart!=null){
					if (!this.linkStart.getTo().getArrayOrInstanceReference().equals(value)){//TODO: equals or == ?
						// reference has changed
						this.createLink((ObjectReference)value);
					}// else: link target (to) has NOT changed.
				}else{ // new link
					this.createLink((ObjectReference)value);
				}
			}else{
				// Strange case, previously handled value was a  reference and no more now !
				this.clearLink();
			}
		}
	}
	
	private void clearLink(){
		if (this.linkStart!=null){
			this.linkStart.remove(getMemoryView());
			this.linkStart=null;
		}
	}
	
	private void createLink(ObjectReference toRef){
		this.clearLink();
		InstanceOrArrayView iav=this.getMemoryView().referenceToView(toRef);
		if (iav!=null){
			this.linkStart=new LinkView(this.getMemoryView(), this, iav);	// update memoryView arrows list.
		}else{
			Logger.getLogger("").logp(Level.INFO,
					this.getClass().getName(), "createLink", "can not find view for ObjectReference: "+toRef);
		}
	}
	
	void remove() {
		this.getMemoryView().remove(this);
		this.clearLink();
	}
	
	public class ValueView extends JLabel {
		private static final long serialVersionUID = 1L;
		ValueView(){
			super(); // will call getText() but this is not initialized... but outer object is !
			this.setFont(getMemoryView().getEltFont());
			this.setToolTipText("-"); // to allow top tip .. ?!
		}
		
		/**
		 * Return the value viewed.
		 * This method is called by ValueViewAbstract.getText(), itself called at construction
		 * time by JPanel. So this method may called before this is fully initialized by the constructor of the present class. 
		 */
		public String getText(){ // Call by super()... sucks !
			try {
				ValueAccess.access(getCurrentValue(), valueAccessor);
				return valueAccessor.result;
			}catch(Throwable anyException){ // The observed VM can be disconnected => direct access to stuff  raise a bunch of errors
				return "??"; // TODO: push in properties.
			}
		}
				
		public String getToolTipText(MouseEvent event){
			try {
				Value lValue=getCurrentValue();
				if (lValue!=null){
					ValueAccess.access(lValue, tipAccessor);
					return tipAccessor.result;
				}else{
					return null; //TODO: Ok ?
				}
			}catch(Throwable anyException){ // The observed VM can be disconnected => direct access to stuff  raise a bunch of errors
				return "??"; // TODO: push in properties.
			}
		}
	}
	// TODO: definitely not thread safe !!
	// TODO: format& properties  everywhere...
	private static _ValueAccessor valueAccessor=new _ValueAccessor();
	private static class _ValueAccessor implements ValueAccessor{
		String result;
		@Override
		public void accesInstance(ObjectReference oi) {
			result=objectInstanceFormat.format(new Object[]{oi.uniqueID()});
		}

		@Override
		public void accesInt(IntegerValue i) {
			result=i.toString();
		}

		@Override
		public void accessBoolean(BooleanValue b) {
			result=b.toString();
		}

		@Override
		public void accessNull() {
			result=nullText;
		}

		@Override
		public void accessString(StringReference s) {
			result=s.toString();
		}

		@Override
		public void accessVoid(VoidValue v) {
			result=voidText;
		}

		@Override
		public void accessArrayReference(ArrayReference ar) {
			result=objectInstanceFormat.format(new Object[]{ar.uniqueID()}); 
		}
	}
	
	// TODO: not thread safe.
	private static TipAccessor tipAccessor=new TipAccessor();
	// TODO array format everywhere.
	private static class TipAccessor implements ValueAccessor{
		String result;
		@Override
		public void accesInstance(ObjectReference oi) { // TODO: format.
			result=oi.referenceType().name();
		}

		@Override
		public void accesInt(IntegerValue i) {
			result=i.type().name();
		}

		@Override
		public void accessBoolean(BooleanValue b) {
			result=b.type().name();
		}

		@Override
		public void accessNull() {
			result=null;
		}

		@Override
		public void accessString(StringReference s) {
			result=s.referenceType().name();
		}

		@Override
		public void accessVoid(VoidValue v) {
			result=null;
		}

		@Override
		public void accessArrayReference(ArrayReference ar) {
			result=ar.referenceType().name();
		}
	}
}
