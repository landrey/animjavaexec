package fr.loria.madynes.animjavaexec.jgraphview;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import com.sun.jdi.Field;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.Value;


class InstanceView extends InstanceOrArrayView {
	static private final Color instanceLabelColor=Color.PINK;    //TODO: externalize (ressource bundle). 
	
	private ObjectReference instance;

	private Map<Field, FieldView> fieldToViewMap=new Hashtable<Field, FieldView>(); // TODO: usefull ?

	
	InstanceView(ObjectReference instance, MemoryFrame mf, int x, final int y){
		String className=instance.referenceType().name();
		this.labelStr="instance of "+className.substring(className.lastIndexOf('.')+1)+"@"+instance.uniqueID(); 	
		// TODO: improve label to display. Externalize
		int curY=y;
		GraphModel gm=mf.getModel();
		
		gm.beginUpdate();
		try{
			Map<DefaultGraphCell, Map<?, ?>> attributes = 
				new Hashtable<DefaultGraphCell, Map<?, ?>>();
			Vector<DefaultGraphCell> newCells=new Vector<DefaultGraphCell>();
			AttributeMap at;
			grpCell = new DefaultGraphCell();
			newCells.add(getGrpCell());
			//at=new AttributeMap();
			labelCell=new DefaultGraphCell(this);
			newCells.add(labelCell);
			getGrpCell().add(labelCell);
			at=new AttributeMap();
			JGraph.createBounds(at, x, curY, instanceLabelColor); 
			GraphConstants.setEditable(at, false);
			GraphConstants.setSelectable(at, false);
			destPort=new DefaultPort();
			GraphConstants.setOffset(destPort.getAttributes(), new Point2D.Double(0.0, 0.0));
			labelCell.add(destPort);
			destPort.setParent(labelCell); // needed ?
			// getGrpCell().add(destPort); // NO NON NEIN !!!
			newCells.add(destPort); // useful ????
			Rectangle2D rect=mf.getGraph().getCellBounds(labelCell);
			System.out.println("Rect="+rect); // DEBUG
			attributes.put(labelCell, at);
			curY+=dy;
			for (Field f:instance.referenceType().allFields()){
				/***
				DefaultGraphCell fVtx=new DefaultGraphCell(f);
				newCells.add(fVtx);
				at=new AttributeMap();
				JGraph.createBounds(at, x, y, instanceFieldColor); 
				grpCell.add(fVtx);
				attributes.put(fVtx, at);
				*/
				fieldToViewMap.put(f, new FieldView(f, instance.getValue(f), 
									x,  curY,
									getGrpCell(),
									mf, 
									newCells, 
									attributes));
						
				//gm.insert(new Object[]{fVtx}, attributes, null, null, null);
				curY+=dy;
			}
			Object[] newCellsArray=newCells.toArray();
			gm.insert(newCellsArray, attributes, null, null, null);
			gm.toFront(newCellsArray);
			this.height=curY-y;
		}finally{
			gm.endUpdate();
		}
	}
	public String toString(){
		return this.labelStr;
	}
	public Object getDestPort() {
		return destPort;
	}
	DefaultGraphCell getGrpCell() {
		return grpCell;
	}
	public int getHeigth() {
		return height;
	}
	void updateField(JGraph graph, Field fd, Value valueToBe) {
		FieldView fv=this.fieldToViewMap.get(fd);
		if (fv==null){
			System.out.println("INTERNAL ERROR can not find field view for update:"+this.instance+"->"+fd);//TODO: log it
		}else{
			fv.update(this, valueToBe);
		}
	}
	void clear(Vector<Object> removedCells) {
		super.clear(removedCells);
		for (FieldView fv:this.fieldToViewMap.values()){
			fv.clear(removedCells);
		}
	}
}
