package fr.loria.madynes.animjavaexec.jgraphview;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;
import org.jgraph.graph.GraphModel;

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ArrayType;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.PrimitiveType;

import fr.loria.madynes.javautils.Properties;

class ArrayView extends InstanceOrArrayView {

	static private final Color arrayLabelColor=Color.PINK;    //TODO: externalize (ressource bundle). 
	static private final Color lengthLabelColor = Color.YELLOW;
	static private final MessageFormat labelFormat=
			new MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.ArrayView.labelFormat"));
	static private final MessageFormat lengthFormat=
		new MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.ArrayView.lengthFormat"));
	//static private final MessageFormat eltFormat=
	//	new MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.ArrayView.eltFormat"));

	static private final int dy=35; // TODO: auto-determine this value 

	
	private ArrayReference aRef;	
	private DefaultGraphCell lengthCell; // TODO: useful to keep ?
	
	//private Map<Field, FieldView> fieldToViewMap=new Hashtable<Field, FieldView>(); // TODO: usefull ?
	private ArrayElementView[] eltViews;
	
	ArrayView(ArrayReference ar, MemoryFrame mf, int x, int y) {
		this.aRef=ar;
		eltViews=new ArrayElementView[ar.length()];
		String className=ar.referenceType().name();
		this.labelStr=labelFormat.format(new Object[]{className.substring(className.lastIndexOf('.')+1), ar.uniqueID()});
		int curY=y;
		GraphModel gm=mf.getModel();
		
		gm.beginUpdate();
		try{
			Map<DefaultGraphCell, Map<?, ?>> attributes = 
				new Hashtable<DefaultGraphCell, Map<?, ?>>();
			Vector<DefaultGraphCell> newCells=new Vector<DefaultGraphCell>();
			AttributeMap at;
			this.grpCell = new DefaultGraphCell();
			newCells.add(this.grpCell);
			//at=new AttributeMap();
			labelCell=new DefaultGraphCell(this);
			newCells.add(labelCell);
			grpCell.add(labelCell);
			at=new AttributeMap();
			JGraph.createBounds(at, x, curY, arrayLabelColor); 
			GraphConstants.setEditable(at, false);
			GraphConstants.setSelectable(at, false);
			destPort=new DefaultPort();
			GraphConstants.setOffset(destPort.getAttributes(), new Point2D.Double(0.0, 0.0));
			labelCell.add(destPort);
			destPort.setParent(labelCell); // needed ?
			// getGrpCell().add(destPort); // NO NON NEIN !!!
			newCells.add(destPort); // useful ????
			attributes.put(labelCell, at);
			curY+=dy;
			
			// Length field (unmutable)
			at=new AttributeMap();
			DefaultGraphCell lengthCell=JGraphUtils.createLabelCell(
				new DefaultGraphCell(lengthFormat.format(new Object[]{new Integer(ar.length())})),
				grpCell, newCells, at, x, curY, lengthLabelColor);
			attributes.put(lengthCell, at);
			curY+=dy;
			
			for (int i=0; i<ar.length(); ++i){
				try {
					this.eltViews[i]=new ArrayElementView(i, ar.getValue(i), 
															x, curY, grpCell, mf, 
															newCells, attributes,
															!(((ArrayType)ar.referenceType()).componentType() instanceof PrimitiveType)
															);
				} catch (ClassNotLoadedException e) {
					e.printStackTrace(); // TODO: log it
				}
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
	int getHeigth() {
		return this.height;
	}

	void update(Vector<Object> changedCells) {
		for(int i=0; i<aRef.length(); ++i){
			this.eltViews[i].update(aRef.getValue(i), changedCells);
		}
	}

	void clear(Vector<Object> removedCells) {
		super.clear(removedCells);
		removedCells.add(this.lengthCell);
		for (ArrayElementView aev:this.eltViews){
			aev.clear(removedCells);
		}
	}
}
