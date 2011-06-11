package fr.loria.madynes.animjavaexec.view2;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.LayoutManager;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.font.FontRenderContext;
import java.awt.font.TextLayout;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JPanel;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassLoaderReference;
import com.sun.jdi.ClassObjectReference;
import com.sun.jdi.Field;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.PrimitiveValue;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadGroupReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import fr.loria.madynes.animjavaexec.execution.model.ArrayManager;
import fr.loria.madynes.animjavaexec.execution.model.ExecStack;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.InstanceManager;
import fr.loria.madynes.animjavaexec.execution.model.StackElt;
import fr.loria.madynes.animjavaexec.execution.model.StackEltTag;
import fr.loria.madynes.animjavaexec.execution.model.ArrayManager.ArrayChangeEvent;
import fr.loria.madynes.animjavaexec.execution.model.InstanceManager.InstanceChangeEvent;

import fr.loria.madynes.javautils.Properties;
import fr.loria.madynes.javautils.swing.SelectionManager;


// Remark: 
// awt coordinates system:  
// 				x: 0 -> right
// 				y: 0 -> bottom

public class MemoryView extends JPanel  implements 
	Printable,
	LayoutManager,
	KeyListener,
	ComponentListener{
	private static final long serialVersionUID = 1L;
	// model.StackEltTag: THIS, PARAM, VAR, RETURN, SEPARATOR;
	// view2.ViewEltTag: THIS, PARAM, VAR, RETURN, SEPARATOR, ARRAY_HEADER, ARRAY_LENGTH, ARRAY_ELT, INSTANCE_HEADER, INSTANCE_VAR;
	private static final String[] viewEltColorKey={
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.thiscolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.paramcolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.variablecolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.returncolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.separatorcolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.arrayheadercolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.arraylengthcolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.arrayeltcolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.instanceheadercolor",
	   "fr.loria.madynes.animjavaexec.view2.MemoryView.instancevariablecolor"
	};
	// stack elt, array elt, instance attributes...
	private static final String stackEltFontNameKey=	"fr.loria.madynes.animjavaexec.view2.MemoryView.stackeltfontname";
	private static final String stackEltFontSizeKey=	"fr.loria.madynes.animjavaexec.view2.MemoryView.stackeltfontsize";
	// array or instance header
	private static final String headerFontNameKey=		"fr.loria.madynes.animjavaexec.view2.MemoryView.headerfontname";
	private static final String headerFontSizeKey=		"fr.loria.madynes.animjavaexec.view2.MemoryView.headerfontsize";
	private static final String widthHeightRatioKey=	"fr.loria.madynes.animjavaexec.view2.MemoryView.widthheightratio";
	private static final String extraHeightPerCentKey=	"fr.loria.madynes.animjavaexec.view2.MemoryView.extraheightpercent";
	
	// view2.ViewEltTag: THIS, PARAM, VAR, RETURN, SEPARATOR, ARRAY_HEADER, ARRAY_LENGTH, ARRAY_ELT, INSTANCE_HEADER, INSTANCE_VAR;
	private Color[] viewEltColor=null;
	
	private static final String callLabel=Properties.getMessage("fr.loria.madynes.animjavaexec.view2.MemoryView.callLabel");
	private Observer stackObserver=null;
	private Observer instanceObserver=null;
	private Observer arrayObserver=null;
	
	//  parallel  usage (and double search) with Instancemanager.managedRegularObjects
	private Map<ObjectReference, InstanceView> instanceToViewMap=
			new Hashtable<ObjectReference, InstanceView>();
	
    //  parallel  usage (and double search) with ArrayManager.managedArrays
	private Map<ArrayReference, ArrayView> arrayToViewMap=
			new Hashtable<ArrayReference, ArrayView>();
	
	private Set<LinkView> linkSet=new HashSet<LinkView>();
	private static final int fontSizeIncrement=1; //TODO as Property ?. For +/- action on MemorytView 
	private static final int instanceCurX0=150;
	private static final int instanceCurY0=10;
	private static final int stackGapFromBottom=20;// TODO: push in properties.
	private static final int stackHeapXgap=10; // TODO: push in properties.
	private static final String constructorLabel = Properties.getMessage("fr.loria.madynes.animjavaexec.view2.MemoryView.constructorLabel");
	private static final MessageFormat calledMethodNameFormat = 
		new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view2.MemoryView.calledMethodNameFormat"));
	private static final MessageFormat calledMethodTipFormat = 
		new  MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view2.MemoryView.calledMethodTipFormat"));
	// Slots to print instances
	private int instanceCurX=150;
	private int instanceCurY=10;
	//private int instanceLabelDy=30;
	
	private HeapGrid heapGrid=new HeapGrid();
	
	private Vector <NameValueViewAbstract> stackElts=new  Vector<NameValueViewAbstract>();
	private boolean snapToGrid=false; //todo: push in properties and gui. 
	private static int stackX=3;
	
	 // Determined from stackEltHeight, see paintComponent() 
	private int stackWidth;   
	private int stackEltHeight; 
	private int instanceHeaderWidth;  
	private int instanceHeaderHeight;  
	private int instanceFieldHeight;
	private int arrayHeaderWidth;  
	private int arrayHeaderHeight;  
	private int arrayEltHeight;  
	//TODO add instanceFieldWidth, arrayEltWidth....
	private int extraHeightPerCent;
	private int widthHeightRatio;
	private boolean drawSizesNeedRecalc=true;
	// stack elt, array elt, instance attribute
	private Font stackEltFont;
	// Instance or array header (id&type)
	private Font headerFont;
	private int previousHeight=-1;
	
	private SelectionManager<MemoryView, InstanceOrArrayView> selectionManager;
	private SelectionManager.SelectionableAdapter<MemoryView,InstanceOrArrayView > selectionable=
		new SelectionManager.SelectionableAdapter<MemoryView,InstanceOrArrayView >(){
		@Override
		public void translate(InstanceOrArrayView e, int dx, int dy) {
			e.translate(dx, dy);
			//this.getContainer()... We are in an inner class => next is enough 
			repaint(); // To redraw arrows...
		}
		@Override
		public void displaySelectionStatus(InstanceOrArrayView e,
				boolean selected) {
			if (selected){
				e.select();
			}else{
				e.deselect();
			}
		}
	};
	public MemoryView(int width, int height){
		// TO get "resize event"
		//setLayout(this);
		setLayout(null);
		// this.requestFocusInWindow(true); // to get Key event. NO MORE USE. See MemoryFrame zoomin/out action.
		this.addComponentListener(this);
		this.setPreferredSize(new Dimension(width, height));
		this.instanceCurY=height-10; // TOO: 10 in property. TODO: chiasse....
		this.viewEltColor=new Color[viewEltColorKey.length];
		Observer stackEltColorsObserver=new Observer(){
			@Override
			public void update(Observable o, Object arg){
				Properties.PropertyChangedEvent evt=(Properties.PropertyChangedEvent)arg;
				//System.out.println("MemoryView >>>>>>>>>>>>>>>> "+evt.getKey()+"="+evt.getNewVal());
				updateViewEltColors();
			}
		};
		
		// Ok ok we double the loop on colorskeys.. But who cares ?
		this.getColorsFromProperties();
		this.setColorsObserver(stackEltColorsObserver);
		
		this.getFontFromProperties();
		drawSizesNeedRecalc=true;
		this.initFontPropertiesObserver();
		this.setVisible(true);
		this.selectionManager=new SelectionManager<MemoryView,InstanceOrArrayView>(
				this, 
				this.selectionable,
				Cursor.getPredefinedCursor(Cursor.HAND_CURSOR), 
				Cursor.getPredefinedCursor(Cursor.MOVE_CURSOR));
	}
	
	private void setColorsObserver(Observer stackEltColorsObserver){
		Properties prop=Properties.getDefaultProperties();
		for (String k:viewEltColorKey){
			prop.addKeyObserver(k, stackEltColorsObserver);
		}
	}
	private void getColorsFromProperties(){
		Properties prop=Properties.getDefaultProperties();
		for (int i=0; i<this.viewEltColor.length; i++){
			this.viewEltColor[i]=prop.getOptinalColorProperty(viewEltColorKey[i], Color.white);
		}
	}
	private void updateViewEltColors() {
		this.getColorsFromProperties();
		// We don't go thru MemoryView structure but direct thru JComponent of
		// MemoryView (a JPanel)...
		for (Component c:this.getComponents()){
			if (c instanceof ColorAdjustable){
				((ColorAdjustable)c).adjustColor();
			}
		}
		// Not his.repaint(): extra paint (arrow, selection rectangle, stack frame are not configurable at all (no properties)
		// But... repaint for component is not doing well with alpha color attribute !!! That sucks...
		this.repaint();
	}

	public void observe(ExecutionModel executionModel){
		this.observe(executionModel.getArrayManager());
		this.observe(executionModel.getExecStack());
		this.observe(executionModel.getInstanceManager());
	}
	
	private void observe(ExecStack estkModel){
		if (this.stackObserver==null){
			this.stackObserver=new Observer(){
				@Override
				public void update(Observable execStack, Object unused) {
					displayStack((ExecStack)execStack);
					// Note: clear= display an empty stack.
				}
			};
		}
		estkModel.addObserver(this.stackObserver);
	}
	
	private void observe(InstanceManager im){
		if (this.instanceObserver==null){
			this.instanceObserver=new Observer(){
				@Override
				public void update(Observable im, Object ev) {
					InstanceManager.InstanceChangeEvent instanceChangeEvent=
							(InstanceManager.InstanceChangeEvent)ev;
					switch(instanceChangeEvent.getTag()){
					case InstanceChangeEvent.CREATION:
						displayNewInstance((InstanceManager)im, instanceChangeEvent.getInstance());
						break;
					case InstanceChangeEvent.FIELD_CHANGED:
						updateInstance((InstanceManager)im, 
										instanceChangeEvent.getInstance(),  
										instanceChangeEvent.getField(),
										instanceChangeEvent.getToBeValue());
						break;
					case InstanceChangeEvent.CLEAR_ALL:
						clearAllInstances();
						break;
			
					}
					
				}
			};
		}
		im.addObserver(this.instanceObserver);
	}
	
	private void observe(ArrayManager am){
		if (this.arrayObserver==null){
			this.arrayObserver=new Observer(){
				@Override
				public void update(Observable am, Object ev) {
					ArrayManager.ArrayChangeEvent arrayChangeEvent=
							(ArrayManager.ArrayChangeEvent)ev;
					switch(arrayChangeEvent.getTag()){
						case ArrayChangeEvent.CREATION:
							displayNewArray(arrayChangeEvent.getArrayReference());
							break;
						case ArrayChangeEvent.UPDATE_ALL:
							refreshAllArray();
							break;
						case ArrayChangeEvent.CLEAR_ALL:
							clearAllArrays();
							break;
					}
				}
			};
		}
		am.addObserver(this.arrayObserver);
	}
	
	private int getStackBaseY(){
		return this.getHeight()-stackGapFromBottom;
	}
	private int getStackDy(){
		return this.stackEltHeight+2;  // TODO: auto determine this value or use property
	}
	private void displayStack(ExecStack execStack){
		int y=this.getStackBaseY()-this.stackEltHeight; // We go UP, but y goes down...
		for (NameValueViewAbstract nv:this.stackElts){
			nv.remove();
			//this.remove(nv);
		}
		this.stackElts.clear();
	
		for (StackElt es:execStack.getElts()){
			NameValueViewAbstract nv=null;
			switch(es.getTag()){
			case SEPARATOR:
				Method inMethod=es.getInMethod();
				if (inMethod!=null){
					nv=createnNameValueForCalledMethod(inMethod, y);
					/*------------
					nv=new NameFixedValueView(this, callLabel, inMethod.name(),
							this.stackX, y, this.stackWidth-2, this.stackEltHeight, 
							MemoryView.stackEltColors[es.getTag().ordinal()]);
					------------*/
				}else{
					nv=new NameFixedValueView(this, "-", "-",
							this.stackX, y, this.stackWidth-2, this.stackEltHeight, 
							ViewEltTag.SEPARATOR);
				}

				break;
			case RETURN:{
				/**
				nv=new NameFixedValueView(this, "return", "-",
						this.stackX, y, this.stackWidth-2, this.stackEltHeight, 
						this.stackEltColors[es.getTag().ordinal()]);
						**/
					Value value=es.getValue2();
					nv=new NameDirectValueView(this, "return", value, //TODO : return as pproperty
							this.stackX, y, this.stackWidth-2, this.stackEltHeight, 
							ViewEltTag.RETURN);
					if (value instanceof ArrayReference){ // New instance are seen by dedicated model event.
						//this.addArrayReferenceView(arrayReference, arrayView)
					}
				}
				break;
			default:{
					Value value=es.getValue2();
					nv=new NameDirectValueView(this, es.getName(), value,
							this.stackX, y, this.stackWidth-2, this.stackEltHeight, 
							ViewEltTag.convert(es.getTag()));
					if (value instanceof ArrayReference){ // New instance are seen by dedicated model event.
						//this.addArrayReferenceView(arrayReference, arrayView)
					}
				}
			}
			this.stackElts.add(nv);
			this.add(nv);
			y-=this.getStackDy();
		}
		this.repaint();
	}
	
	private  NameFixedValueView createnNameValueForCalledMethod(Method method, int y){
		String methodName=(method.isConstructor())?constructorLabel:method.name();
		String className=method.declaringType().name();
		String shortClassName=className.substring(className.lastIndexOf('.')+1);
		String callMethodLabel=calledMethodNameFormat.format(new Object[]{methodName, shortClassName, className});
		String calledMethodTip=calledMethodTipFormat.format(new Object[]{methodName, shortClassName, className});
		
		return new NameFixedValueView(this, callLabel, callMethodLabel,calledMethodTip,
				stackX, y, stackWidth-2, stackEltHeight,
				ViewEltTag.SEPARATOR);
	}
	
	public InstanceView instanceToView(ObjectReference oi){
		return this.instanceToViewMap.get(oi);
	}
	
	public ArrayView arrayToView(ArrayReference ar){
		return this.arrayToViewMap.get(ar);
	}
	
	/** Get known view for an instance or an array reference. 
	 * Get
	 * @param or reference to an instance or an array
	 * @return the associated view or null if not known
	 */
	public InstanceOrArrayView referenceToView(ObjectReference or){
		if (or instanceof ArrayReference){
			return this.arrayToView((ArrayReference)or);
		}else{
			return this.instanceToView(or);
		}
	}

	private void displayNewInstance(InstanceManager im, ObjectReference instance){
		if (this.instanceToViewMap.containsKey(instance)){
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "displayNewInstance", "INTERNAL ERROR, Instance already present in view: "+instance);
		}else{
			InstanceView iv=new InstanceView(this, instance, 
					this, getInstanceCurX(), 
					getInstanceCurY());
			incInstanceCurY(iv.getViewHeight());
			// instanceToViewMap.put(instance, iv); // done by InstanceView constructor, to allow reference from its own fields 
			iv.moveTo(getInstanceCurX(), getInstanceCurY());
			this.repaint();
		}
	}
	
	private void updateInstance(InstanceManager im, ObjectReference instance, Field fd, Value valueToBe){
		InstanceView iv=instanceToView(instance);
		if (iv==null){
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "updateInstance", "INTERNAL ERROR, can not find view for instance update:"+instance);
		}else{
			// TODO: arrow update. //<<<<<<<<<<================================== 
			// Wizzz. Direct update from NameValueView directly linked to <ObjectReference, Field>...
			// Clipping. Arrows are a pain !
			this.repaint();
		}
	}
	
	private void displayNewArray(ArrayReference ar){
		ArrayView av=this.arrayToView(ar);
		if (av!=null){
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "displayNewArray", "NTERNAL ERROR, Array already present in view: "+ar);
		}else{
			av=new ArrayView(this, ar, getInstanceCurX(), getInstanceCurY());
			incInstanceCurY(av.getViewHeight());
			this.addArrayReferenceView(ar, av);
			av.moveTo(getInstanceCurX(), getInstanceCurY()); // TODO add: boolean createInstanceFromBottom....
		}
	}
	
	private void refreshAllArray() {
		// TODO: arrowS update. //<<<<<<<<<<================================== 
		Vector<Object> changedCells=new Vector<Object>();
		this.repaint();
	}

	private void clearAllInstances() {
		// TODO: arrows !!!
		for (InstanceView iv:this.instanceToViewMap.values()){
			//this.selectedArraysOrInstancesViews.remove(iv);
			iv.remove(); // InstanceView.remove does NOT update instanceToViewMap because iterator is backed by the list.
		}
		this.instanceToViewMap.clear();
		//this.instanceCurX=instanceCurX0;
		this.instanceCurX=stackX+this.stackWidth+stackHeapXgap;
		//this.instanceCurY=instanceCurY0;
		this.instanceCurY=this.getHeight()-10;
		// ??? /// this.repaint();
	}
	
	private void clearAllArrays() {
		// TODO: arrows.
		for (ArrayView av:this.arrayToViewMap.values()){
			//this.selectedArraysOrInstancesViews.remove(av);
			av.remove();// ArrayReferenceView.remove does NOT update arrayToViewMap because iterator is backed by the list.
		}
		//TODO: pb clear curX, CurY.
		this.arrayToViewMap.clear();
	}
	
	private void adjustFontAndSizeForAllViews(){
		// Instances views
		for (InstanceView iv:this.instanceToViewMap.values()){
			iv.adjustFontAndSize();
			if (iv.getX()<this.getInstanceCurX()){
				iv.translate(this.getInstanceCurX()-iv.getX(), 0);
			}
		}
		// Arrays views
		for (ArrayView av:this.arrayToViewMap.values()){
			av.adjustFontAndSize();
			if (av.getX()<this.getInstanceCurX()){
				av.translate(this.getInstanceCurX()-av.getX(), 0);
			}
		}
		final int dy=this.getStackDy();
		int y=this.getStackBaseY()-this.stackEltHeight; 
		for (NameValueViewAbstract e:this.stackElts){
			e.adjustFontAndBounds(stackX, y, this.stackWidth-2, this.stackEltHeight, this.stackEltFont);
			y-=dy;
		}
		this.repaint(); // stack border and arrows
	}
	private void adjustAllViewsToHeight(int oldHeight){
		int dy=this.getHeight()-oldHeight;
		for (InstanceView iv:this.instanceToViewMap.values()){
			iv.translate(0, dy);
		}
		for (ArrayView av:this.arrayToViewMap.values()){
			av.translate(0, dy);
		}
		for (NameValueViewAbstract e:this.stackElts){
			e.setLocation(e.getX(), e.getY()+dy);
		}	
		//this.repaint();
	}
	
	int getInstanceCurX() {
		this.instanceCurX=stackX+this.stackWidth+stackHeapXgap;
		return this.instanceCurX;
	}
	void setInstanceCurY(int instanceCurY) {
		this.instanceCurY = instanceCurY;
	}
	int getInstanceCurY() {
		return instanceCurY<0? 2: this.instanceCurY;
	}
	private void incInstanceCurY(int heigth) {
		//instanceCurY+=heigth+10; // TODO: push the dy value in src bundle or aot-determinate it...
		this.instanceCurY-=(heigth+5);
	}

	public void addInstanceView(ObjectReference instance, InstanceView instanceView) {
		this.instanceToViewMap.put(instance, instanceView);
	}

	public void addArrayReferenceView(ArrayReference arrayReference, ArrayView arrayView) {
		if (this.arrayToViewMap.containsKey(arrayReference)){
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "addArrayReferenceView",
					"INTERNAL ERROR,  arrayView already there !"+arrayReference.uniqueID()); 
		}else{
			this.arrayToViewMap.put(arrayReference, arrayView);
		}
	}

	void deselectAll(){
		this.selectionManager.deselectAll();
	}
	
	void selectAll(){
		this.selectionManager.selectAll();
	}
	
	@Override
	public void paintComponent(Graphics g){
		Graphics2D g2=(Graphics2D)g;
		if (this.drawSizesNeedRecalc){
			this.recalAllSizes(g2);
		}
		super.paintComponent(g);
		int h=this.getHeight();
		g2.drawRect(stackX, this.getHeight()-h-stackGapFromBottom, stackWidth, h); // TODO: check this shit... this.getHeight()-h ==0 !!! Use this.stackHeight() ?
		// draw stack border and arrows.
		//this.heapGrid.paint(g2);
		for (LinkView lv:this.linkSet){
			lv.draw(g2);
		}
		this.selectionable.drawSelectionRect(g);
	}
	
	private int stackHeight(){
		// 	int y=this.getHeight()-stackGappFromBottom-this.stackEltHeight; 
		int h=(this.getHeight()-stackGapFromBottom)/this.stackEltHeight;
		return h*stackEltHeight;
	}

	void add(LinkView linkView){
		if (this.linkSet.add(linkView)){
			// true add
			this.repaint(); //<<<== todo: re-entrance pb.
		}
	}
	
	void forget(LinkView linkView) {
		if (this.linkSet.remove(linkView)){
			this.repaint(); //<<<== todo: re-entrance pb.
		}
	}
	static boolean isViewableReference(Value value){
		// string a display has primitive value, no arrow.
		return (value instanceof ArrayReference) || 
		     !( 
		       (value==null) // null instanceof C is always false...
		              ||
		       (value instanceof  PrimitiveValue)
		              ||
		       (value instanceof  StringReference)
		    		  ||
		       (value instanceof  ClassLoaderReference) 
		              ||
		       (value instanceof  ClassObjectReference)   
		              ||
		       (value instanceof  ThreadGroupReference)
		              ||
		       (value instanceof  ThreadReference)
		       	      ||
		       (value instanceof  VoidValue)
		       );
	}

	boolean getSnapToGrid() {
		return this.snapToGrid;
	}

	void snapToGrid(InstanceOrArrayView instanceOrArrayView) {
		if (getSnapToGrid()){
			this.heapGrid.snapToGrid(instanceOrArrayView);
		}
	}

	Font getEltFont(){
		return this.stackEltFont;
    }

	Font getHeaderFont() {
		return this.headerFont;
	}

	int getInstanceHeaderHeight(){
		return this.instanceFieldHeight;
	}
	
	int getInstanceHeaderWidth() {
		return this.instanceHeaderWidth;
	}

	int getInstanceFieldHeight() {
		return this.instanceFieldHeight;
	}

	int getArrayHeaderWidth() {
		return this.arrayHeaderWidth;
	}

	int getArrayHeaderHeight() {
		return this.arrayHeaderHeight;
	}

	int getArrayEltHeight() {
		return this.arrayEltHeight;
	}

	private void initFontPropertiesObserver(){
		Observer o=new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				// No fine granularity : any font prop are reloaded...
				getFontFromProperties();
				recalAllSizes((Graphics2D) getGraphics());
				adjustFontAndSizeForAllViews();
				// Rebuild or resize all views (stackView, array views, instance views...)
				// repaint();
			}
		};
		Properties dp=Properties.getDefaultProperties();
		dp.addKeyObserver(stackEltFontNameKey, o);
		dp.addKeyObserver(stackEltFontSizeKey, o);
		dp.addKeyObserver(headerFontNameKey, o);
		dp.addKeyObserver(headerFontSizeKey, o);
		dp.addKeyObserver(widthHeightRatioKey, o);
		dp.addKeyObserver(extraHeightPerCentKey, o);
	}
	// For initialization or or update  by properties observer.
	// We Update all font stuff on any font properties change event.
	private void getFontFromProperties(){
		Properties dp=Properties.getDefaultProperties();
		this.stackEltFont=new Font(dp.getProperty(stackEltFontNameKey), Font.PLAIN, dp.getIntProperty(stackEltFontSizeKey));
		this.headerFont=new Font(dp.getProperty(headerFontNameKey), Font.BOLD, dp.getIntProperty(headerFontSizeKey));
		this.widthHeightRatio=dp.getIntProperty(widthHeightRatioKey);
		this.extraHeightPerCent=dp.getIntProperty(extraHeightPerCentKey);
		
		//this.drawSizesNeedRecalc=true;
		//recalAllSizes((Graphics2D)this.getGraphics()); // in properties observer and in 1st paintComponent...
	}
	/** Increment font sizes (elets, headers font size).
	 * 
	 * @param way -1 ou +1 to decrease or increase the fon sizes.
	 */
	 void incrementFontSize(int way) {
		//TODO: double update... => find a way to merge Properties change events..
		Properties dp=Properties.getDefaultProperties();
		dp.setPreference(headerFontSizeKey, Integer.toString(dp.getIntProperty(headerFontSizeKey)+way*fontSizeIncrement));
		dp.setPreference(stackEltFontSizeKey, Integer.toString(dp.getIntProperty(stackEltFontSizeKey)+way*fontSizeIncrement));
	}
	private void recalAllSizes(Graphics2D g2){
		if (g2!=null){
			
			FontRenderContext frc=g2.getFontRenderContext();
			TextLayout tl=new TextLayout("A", this.stackEltFont, frc);
			this.stackEltHeight=this.adjustHeight(tl.getBounds().getHeight(), this.extraHeightPerCent); //TODO  extra heights as  property.
			this.stackWidth=this.adjustWidth(this.stackEltHeight*widthHeightRatio);
			tl=new TextLayout("A", this.headerFont, frc);
			this.instanceHeaderHeight=this.adjustHeight(tl.getBounds().getHeight(), this.extraHeightPerCent);  //TODO  extra height as a  property.
			this.instanceHeaderWidth=this.adjustWidth(this.instanceHeaderHeight*widthHeightRatio);
			this.instanceFieldHeight=this.stackEltHeight; //TODO : separated properties set for instance view
			this.arrayHeaderHeight=this.instanceHeaderHeight; //TODO : separated properties set for array view
			this.arrayHeaderWidth=this.instanceHeaderWidth;
			this.arrayEltHeight=this.instanceFieldHeight;
			this.instanceCurX=stackX+this.stackWidth+stackHeapXgap;
			
			this.instanceCurY=this.getHeight()-10; 
			this.drawSizesNeedRecalc=false;
		}else{
			//TODO: log ??
		}
	}
	private int adjustHeight(double h, int pc){
		int result=(int)Math.round(h*(1+(double)pc/100)); // Double arithmetic is a pain...
		return result%2==0?result:result+1;
	}
	private int adjustWidth(int w){
		return w%2==0?w:w+1;
	}
	@Override
	public int print(Graphics g, PageFormat pf, int pi)
			throws PrinterException {
		if (pi >= 1) {
			return Printable.NO_SUCH_PAGE;
		}

		Graphics2D g2d= (Graphics2D)g;
		g2d.translate(pf.getImageableX(), pf.getImageableY()); 
		paint(g2d);
	    return Printable.PAGE_EXISTS;
	}

	Font getArrayEltFont() {
		return this.stackEltFont; //TODO: customize for array elts...
	}
	
	// LayoutManager. TO "get resize  events". NO MORE USED: WE USED ComponentListener (see after)
	@Override
	public void addLayoutComponent(String name, Component comp) {
	}
	@Override
	public void layoutContainer(Container parent) {
		adjustToHeight();
	}

	@Override
	public Dimension minimumLayoutSize(Container parent) {
		return null;
	}
	@Override
	public Dimension preferredLayoutSize(Container parent) {
		return null;
	}
	@Override
	public void removeLayoutComponent(Component comp) {
	}

	
	// KeyListener (to this set a listener on owning MemoryFrame
	@Override
    public void keyTyped(KeyEvent e) {
		switch (e.getKeyChar()){
		case '+': this.incrementFontSize(+1); 
				break;
		case '-': this.incrementFontSize(-1); 
				break;
		default:;
		}
    }
	@Override
    public void keyPressed(KeyEvent e) {
    }
	@Override
    public void keyReleased(KeyEvent e) {
    }

	
	// ComponentListener: to get size change event
	@Override
	public void componentHidden(ComponentEvent e) {
	}

	@Override
	public void componentMoved(ComponentEvent e) {
	}

	@Override
	public void componentResized(ComponentEvent e) {
		adjustToHeight();
	}

	@Override
	public void componentShown(ComponentEvent e) {
	}
	

	private void adjustToHeight() {
		if (this.previousHeight!=this.getHeight()){
			this.adjustAllViewsToHeight(this.previousHeight);
			this.previousHeight=this.getHeight();
		}
	}
	

	// Pseudo visitor pattern for owned arrays or instances
	private void accept(InstanceOrArrayViewVisitor v){
		for (InstanceView iv:this.instanceToViewMap.values()){
			v.visit(iv);
		}
		// Arrays views
		for (ArrayView av:this.arrayToViewMap.values()){
			v.visit(av);
		}
	}

	SelectionManager<MemoryView,InstanceOrArrayView> getSelectionManager() {
		return selectionManager;
	}

	private interface InstanceOrArrayViewVisitor{
		void visit(InstanceOrArrayView ioav);
	}
	
	Color getViewEltColor(ViewEltTag tag){
		return this.viewEltColor[tag.ordinal()];
	}
}//class
