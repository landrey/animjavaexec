package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import com.sun.jdi.ObjectReference;

/**
 * Abstract class for header and elements of arrays or object instances.
 * Note: header & tip (class and unique id) can not change once set...
 * @author andrey
 *
 */
public abstract class InstanceOrArrayView 
extends JPanel 
implements ColorAdjustable {
	private static final long serialVersionUID = 1L;
	private static Border headerBorder=BorderFactory.createLineBorder(Color.black);		         //TODO: push in properties
	private static Border selectedHeaderBorder=BorderFactory.createLineBorder(Color.black, 4);	 //TODO: push in properties
	private static Cursor moveCursor=Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR);     //TODO: push in properties
	private static Cursor pressedCursor=Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);  //TODO: push in properties
	private MemoryView memoryView;
	private ObjectReference arrayOrInstanceReference;
	private JLabel headerLabel;
	private int viewHeight; // Note: beware  JPanel.getHeight exists...
	private Set<LinkView> endTo;
	private boolean selected=false;
	private ViewEltTag headerTag;
	InstanceOrArrayView(MemoryView mv,  ObjectReference arrayOrInstanceReference,
						int x, int y, int headerWidth, int headerHeight, ViewEltTag headerTag){
		this.setLayout(null);
		this.setMemoryView(mv); // At 1st place => to allow abstract getHeader*() methods to safely work
		this.headerTag=headerTag;
		this.setArrayOrInstanceReference(arrayOrInstanceReference);
		this.setBounds(x, y, headerWidth, headerHeight);
		this.viewHeight=0;
		this.setBackground(mv.getViewEltColor(headerTag));
		this.setBorder(headerBorder);
		headerLabel=new JLabel();
		headerLabel.setFont(mv.getHeaderFont()); //TODO: font as a cons parameter...
		headerLabel.setBounds(0, 0, headerWidth, headerHeight);
		this.incViewHeight(headerHeight);
		//this.addMouseListener(new _MouseListener());
		this.add(headerLabel);
		memoryView.getSelectionManager().manage(this);
		memoryView.add(this); // add NOW, if not, no display.... 
	}

	void adjustFontAndSize(){
		this.headerLabel.setFont(this.getHeaderFont());
		int headerWidth=this.getHeaderWidth();
		int headerHeight=this.getHeaderHeight();
		this.setSize(headerWidth, headerHeight);
		this.headerLabel.setSize(headerWidth, headerHeight);
		this.viewHeight=0;
		this.incViewHeight(headerHeight);
		this.adjustEltsFontAndSize(); // instance fields or array elts.
	}
	@Override
	public void adjustColor(){
		this.setBackground(this.getMemoryView().getViewEltColor(this.headerTag));
	}
	final void setHeaderText(String headerText){
		this.headerLabel.setText(headerText);
	}
	final void setHeaderTip(String headerTip){
		this.setToolTipText(headerTip);
	}
	private void setMemoryView(MemoryView memoryView) {
		this.memoryView = memoryView;
	}

	private void setArrayOrInstanceReference(ObjectReference arrayOrInstanceReference) {
		this.arrayOrInstanceReference = arrayOrInstanceReference;
	}

	ObjectReference getArrayOrInstanceReference() {
		return this.arrayOrInstanceReference;
	}

	MemoryView getMemoryView() {
		return memoryView;
	}
	
	void select(){
		if (!this.selected){
			this.selected=true;
			this.setBorder(selectedHeaderBorder);
		}
	}
	
	void deselect(){
		if (this.selected){
			this.selected=false;
			this.setBorder(headerBorder);
		}
	}
	
	/**
	 * Remove from top level panel and clear links
	 */
	void remove(){ 
		// clear to links...
		for (LinkView lv:this.endTo){
			lv.remove(this.getMemoryView());
		}
		this.endTo.clear(); // Useless... But.
		this.getMemoryView().remove(this); // remove from Swing Component
		removeElts();
		this.memoryView.getSelectionManager().unmanage(this);
		// TODO: remove from grid.
	}
	
	/**
	 * translate on screen
	 * @param dX
	 * @param dY
	 */
	public void translate(int dX, int dY) {
		this.setLocation(this.getX()+dX, this.getY()+dY);
		this.translateElts(dX, dY);
	}
	
	/**
	 * Move to location on screen
	 * @param x
	 * @param y
	 */
	public void moveTo(int x, int y){
		this.translate(x-this.getX(), y-this.getY());
		/*--
		this.setLocation(x,y);
		this.moveElementsTo(x, x);
		**/
	}

	protected abstract void removeElts();
	protected abstract void moveElementsTo(int x, int y);	
	protected abstract void translateElts(int dX, int dY);
	protected abstract void adjustEltsFontAndSize();
	
	public void setViewHeight(int viewHeight) {
		this.viewHeight = viewHeight;
	}

	public void incViewHeight(int inc){
		this.viewHeight+=inc;
	}
	
	public int getViewHeight() {
		return viewHeight;
	}

	void addLinkEnd(LinkView linkView) {
		if (this.endTo==null){
			this.endTo=new HashSet<LinkView>();
			this.endTo.add(linkView);
		}
	}
	
	void removeLinkEnd(LinkView linkView){
		if (this.endTo!=null){
			this.endTo.remove(linkView);
		}
	}

	int getLinkEndX() {
		return this.getX();
	}

	public int getLinkEndY() {
		return this.getY()+this.getHeight()/2;
	}
	
	
	
	abstract Font getHeaderFont();
	abstract int getHeaderHeight();
	abstract int getHeaderWidth();
		
	

	boolean isSelected() {
		return this.selected;
	}

	/** Check it view is in a rectangle, top-left and bottom-right coordinates.
	 * 
	 * @param xleft
	 * @param ytop
	 * @param xright
	 * @param ybottom
	 * @return
	 */
	boolean isInRect(int xleft, int ytop, int xright, int ybottom) {
		int x=this.getX();
		int y=this.getY();	
		return (xleft<x)&&(x+this.getWidth()<xright)&&(ytop<y)&&(y+this.viewHeight<ybottom) ;
	}
}
