package fr.loria.madynes.animjavaexec.jgraphview;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.util.Hashtable;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultGraphModel;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;
import org.jgraph.graph.ParentMap;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;

import fr.loria.madynes.animjavaexec.execution.model.ArrayManager;
import fr.loria.madynes.animjavaexec.execution.model.ExecStack;
import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.InstanceManager;
import fr.loria.madynes.animjavaexec.execution.model.StackElt;
import fr.loria.madynes.animjavaexec.execution.model.ArrayManager.ArrayChangeEvent;
import fr.loria.madynes.animjavaexec.execution.model.InstanceManager.InstanceChangeEvent;


public class MemoryFrame extends JFrame {
	private static final long serialVersionUID = 1L;
	private  JGraph graph;
	private DefaultGraphModel gModel;
	private static Color[] stackEltColors={Color.red, Color.green, Color.blue, Color.black};
	private Observer stackObserver=null;
	private Observer instanceObserver=null;
	private Observer arrayObserver=null;
	
	//  parallel  usage (and double search) with Instancemanager.managedRegularObjects
	private Map<ObjectReference, InstanceView> instanceToViewMap=
			new Hashtable<ObjectReference, InstanceView>();
	
    //  parallel  usage (and double search) with ArrayManager.managedArrays
	private Map<ArrayReference, ArrayView> arrayToViewMap=
			new Hashtable<ArrayReference, ArrayView>();
	
	private static final int instanceCurX0=150;
	private static final int instanceCurY0=10;
	// Slots to print instances
	private int instanceCurX=150;
	private int instanceCurY=10;
	//private int instanceLabelDy=30;
	
	private Object[] currentStackCells=null; 
	private DefaultGraphCell stackGroup=null;
	public MemoryFrame(String windowTitle, Image wIcone, int c, int r){
		if (windowTitle!=null){
			this.setTitle(windowTitle);
		}
		if (wIcone!=null){
			this.setIconImage(wIcone);
		}
		gModel=new DefaultGraphModel();
		graph=new JGraph(gModel);
		graph.setEditable(true); // TODO set to false ?
		graph.setMoveable(true); // TODO set to false ?
		graph.setSelectionEnabled(true);// TODO set to false ?
		graph.setGridEnabled(true);
		
		initStackGroupCell();
		
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // kill  application when closing memory frame 
		getContentPane().add(new GraphPanel(graph, c, r));
		pack();
		//setSize(c*10, r*10);
		setVisible(true);
	}
	
	private void initStackGroupCell(){
		Map<DefaultGraphCell, Map<?, ?>> attributes = 
			new Hashtable<DefaultGraphCell, Map<?, ?>>();
		stackGroup=new DefaultGraphCell("StackGroup");
		AttributeMap agrp=new AttributeMap();
		GraphConstants.setEditable(agrp, false);
		GraphConstants.setSelectable(agrp, false);
		attributes.put(stackGroup, agrp);
		GraphModel gmodel=graph.getModel();
	    gmodel.beginUpdate();
	    try{
	    	gmodel.insert(new Object[]{stackGroup}, attributes, null, null, null);
	    }finally{
	    	gmodel.endUpdate();
	    }
	}
	public JGraph getGraph(){
		return graph;
	}
	
	private class GraphPanel extends JPanel  {
		private static final long serialVersionUID = 1L;
		private  GraphPanel(JGraph g, int c, int r) {
			super(new GridBagLayout());
			JScrollPane scrollPane = new JScrollPane(graph);
			scrollPane.setPreferredSize(new Dimension(c*10, r*10));
			//Add Components to this panel.
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.gridwidth = GridBagConstraints.REMAINDER;

			//c.fill = GridBagConstraints.HORIZONTAL;
			//add(textField, c);

			gbc.fill = GridBagConstraints.BOTH;
			gbc.weightx = 1.0;
			gbc.weighty = 1.0;
			add(scrollPane, gbc);
		}
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
	
	private void displayStack(ExecStack execStack){
		System.out.println("ZOBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBBB...........");
		final int dy=40; // TODO: auto determine this value.
		int y=400; // TODO: get panel size....
		// Beware the graphmodel is NOT part of our application model...
		GraphModel gmodel=graph.getModel();
		Map<DefaultGraphCell, Map<?, ?>> attributes = 
				new Hashtable<DefaultGraphCell, Map<?, ?>>();
		gmodel.beginUpdate();
		try {
			if (this.currentStackCells!=null){
				ParentMap pm = new ParentMap(currentStackCells, null);
				graph.getGraphLayoutCache().edit(null, null, pm, null);
				gmodel.remove(currentStackCells);
				currentStackCells=null;
			}
			currentStackCells=new Object[execStack.getEltsCount()];
			int esi=0;
			for (StackElt es:execStack.getElts()){
				DefaultGraphCell vtx=new DefaultGraphCell(es);
				this.stackGroup.add(vtx);
				AttributeMap at=new AttributeMap();
				JGraph.createBounds(at, 3, y, MemoryFrame.stackEltColors[es.getTag().ordinal()]);
				GraphConstants.setEditable(at, false);
				GraphConstants.setSelectable(at, false);
				attributes.put(vtx, at);
				currentStackCells[esi]=vtx;
				y-=dy;
				esi++;
			}
			if (esi!=0){
				gmodel.insert(currentStackCells, attributes, null, null, null);
			}else{
				System.out.println("Empty stack");
			}
		}finally{
			gmodel.endUpdate();
		}
	}
	
	public InstanceView instanceToView(ObjectReference oi){
		return this.instanceToViewMap.get(oi);
	}
	
	public ArrayView arrayToView(ArrayReference ar){
		return this.arrayToViewMap.get(ar);
	}
	
	// Could be simplified... Merge the to maps ???
	public InstanceOrArrayView referenceToView(ObjectReference or){
		if (or instanceof ArrayReference){
			return this.arrayToView((ArrayReference)or);
		}else{
				return this.instanceToView(or);
		}
	}
	//TODO: remove im param.
	private void displayNewInstance(InstanceManager im, ObjectReference instance){
		// TODO: filter double (should not happen)....
		InstanceView iv=new InstanceView(instance, this, getInstanceCurX(), 
				 getInstanceCurY());
		incInstanceCurY(iv.getHeigth());
		instanceToViewMap.put(instance, iv);
	}
	
	//TODO: remove im param.
	private void updateInstance(InstanceManager im, ObjectReference instance, Field fd, Value valueToBe){
		InstanceView iv=instanceToView(instance);
		if (iv==null){
			System.out.println("INTERNAL ERROR, can not find view for instance update:"+instance);
		}else{
			iv.updateField(graph,fd, valueToBe);
		}
	}
	
	private void displayNewArray(ArrayReference ar){
		ArrayView av=this.arrayToView(ar);
		if (av!=null){
			System.out.println("INTERNAL ERROR array already displayed..."); // TODO: log it.
		}else{
			av=new ArrayView(ar, this, getInstanceCurX(), getInstanceCurY());
			incInstanceCurY(av.getHeigth());
			this.arrayToViewMap.put(ar, av);
		}
	}
	
	private void refreshAllArray() {
		Vector<Object> changedCells=new Vector<Object>();
		this.gModel.beginUpdate();
		try{
			for (ArrayView av:this.arrayToViewMap.values()){
				av.update(changedCells);
			}
			gModel.cellsChanged(changedCells.toArray());
		}finally{
			this.gModel.endUpdate();
		}
	}
	
	

	private void clearAllInstances() {
		Vector<Object> removedCells=new Vector<Object>();
		this.gModel.beginUpdate();
		try{
			for (InstanceView iv:this.instanceToViewMap.values()){
				iv.clear(removedCells);
			}
			Object[] removedCellsArray=removedCells.toArray();
			ParentMap pm = new ParentMap(removedCellsArray, null);
			graph.getGraphLayoutCache().edit(null, null, pm, null);
			gModel.remove(removedCellsArray);
		}finally{
			this.gModel.endUpdate();
		}
		this.instanceToViewMap.clear();
		this.instanceCurX=instanceCurX0;
		this.instanceCurY=instanceCurY0;
	}
	
	private void clearAllArrays() {
		Vector<Object> removedCells=new Vector<Object>();
		this.gModel.beginUpdate();
		try{
			for (ArrayView av:this.arrayToViewMap.values()){
				av.clear(removedCells);
			}
			Object[] removedCellsArray=removedCells.toArray();
			ParentMap pm = new ParentMap(removedCellsArray, null);
			graph.getGraphLayoutCache().edit(null, null, pm, null);
			gModel.remove(removedCellsArray);
		}finally{
			this.gModel.endUpdate();
		}
		this.arrayToViewMap.clear();
	}


	int getInstanceCurX() {
		// 
		return instanceCurX;
	}
	void setInstanceCurY(int instanceCurY) {
		this.instanceCurY = instanceCurY;
	}
	int getInstanceCurY() {
		return instanceCurY;
	}
	private void incInstanceCurY(int heigth) {
		instanceCurY+=heigth+10; // TODO: push the dy value in src bundle or aot-determinate it...
	}

	private void setgModel(DefaultGraphModel gModel) {
		this.gModel = gModel;
	}

	public DefaultGraphModel getModel() {
		return gModel;
	}
}
