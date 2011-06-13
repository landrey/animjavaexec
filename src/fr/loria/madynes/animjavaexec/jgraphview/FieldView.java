package fr.loria.madynes.animjavaexec.jgraphview;

import java.awt.Color;
import java.awt.Container;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Map;
import java.util.Vector;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;


import com.jgraph.layout.JGraphFacade;
import com.jgraph.layout.JGraphLayout;
import com.jgraph.layout.graph.JGraphSimpleLayout;
import com.jgraph.layout.routing.JGraphParallelRouter;
import com.jgraph.layout.simple.SimpleGridLayout;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.Field;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StringReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;

import fr.loria.madynes.animjavaexec.jpdautils.ValueAccess;
import fr.loria.madynes.animjavaexec.jpdautils.ValueAccessor;

class FieldView {
	static public final Color instanceFieldColor=Color.MAGENTA; //TODO: externalize (ressource bundle).
	
	private String strLabel;
	DefaultGraphCell labelCell;
	DefaultEdge linkEdge; // can be null, for field which is NOT a ObjectReference..
	private DefaultPort srcPort; // can be null, for field which is NOT a ObjectReference..
	private  MemoryFrame mf;
	private Field fd;
	FieldView(Field fd, Value v, 
				int x, int y,
				final DefaultGraphCell inGrp,
				final MemoryFrame mf, 
				final Vector<DefaultGraphCell> cells, 
				Map<DefaultGraphCell, Map<?, ?>> attributes ){
		this.mf=mf;
		this.fd=fd;
		strLabel=fd.name();
		labelCell=new DefaultGraphCell(this);
		inGrp.add(labelCell);
		cells.add(labelCell);
		AttributeMap at=new AttributeMap();
		JGraph.createBounds(at, x, y, instanceFieldColor); 
		GraphConstants.setEditable(at, false);
		GraphConstants.setSelectable(at, false);
		attributes.put(labelCell, at);
		try {
			if (fd.type() instanceof ReferenceType){
				this.createPort(cells, inGrp);
			}
		} catch (ClassNotLoadedException e) {
			e.printStackTrace(); // todo: log it
		}
	
		ValueAccess.access(v, new ValueAccessor(){
			@Override
			public void accesInstance(ObjectReference oi) {
				if (oi==null){
						//TODO log... should not occur, see accessNull method.
					strLabel+=": null";
				}else{
					InstanceView valueView=null;
					System.out.println("ZOBBBBBBBBBBB"); //TODO: remove it. or log.
					valueView=mf.instanceToView(oi);
					linkEdge=new DefaultEdge();
					cells.add(linkEdge);
					srcPort =new DefaultPort();
					cells.add(srcPort);
					labelCell.add(srcPort);
					srcPort.setParent(labelCell); // needed ?
					inGrp.add(srcPort); // needed ?
					// cells.add(port); // Needed ????
					GraphConstants.setLineEnd(linkEdge.getAttributes(), GraphConstants.ARROW_CLASSIC);
					GraphConstants.setEndFill(linkEdge.getAttributes(), true);
					linkEdge.setSource(srcPort);
					linkEdge.setTarget(valueView.getDestPort());
				}
			}

			@Override
			public void accesInt(IntegerValue i) {
				strLabel+=":"+i.intValue();
			}

			@Override
			public void accessBoolean(BooleanValue b) {
				strLabel+=":"+b.booleanValue();
			}

			@Override
			public void accessNull() {
				strLabel+=": null";
			}

			@Override
			public void accessString(StringReference s) {
				strLabel+=":"+s;
				
			}

			@Override
			public void accessVoid(VoidValue v) {
				System.err.println(this.getClass().getName()+"Sucks in creator"); //TODO: log it
			}

			@Override
			public void accessArrayReference(ArrayReference v) {
				accesInstance(v);
				
			}
		});
	}
	
	private void createPort(Vector<DefaultGraphCell> cells, DefaultGraphCell inGrp){
		srcPort =new DefaultPort();
		labelCell.add(srcPort);
		srcPort.setParent(labelCell); // needed ?
		//inGrp.add(srcPort); // needed ? NO NO. If added port is really attached to the group frame !
		//GraphConstants.setOffset(srcPort.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE, GraphConstants.PERMILLE/2));
		cells.add(srcPort); // Needed ????
	}
	
	public String toString(){
		return strLabel;
	}

	public void update(final InstanceView iv, Value valueToBe) {
		final JGraph graph=mf.getGraph();
		mf.getModel().beginUpdate();
		mf.getModel().cellsChanged(new Object[]{this.labelCell});
		if (this.linkEdge!=null){
			this.linkEdge.removeAllChildren();
			this.linkEdge.removeFromParent();
			graph.getModel().remove(new  Object[]{this.linkEdge});
			this.linkEdge=null;
		}
		strLabel=fd.name();
		try{
		ValueAccess.access(valueToBe, new ValueAccessor(){
			@Override
			public void accesInstance(ObjectReference oi) {
				if (oi==null){
						//TODO log...
					strLabel+=":null";
					
				}else{
					InstanceView toBeView=mf.instanceToView(oi);
					if (toBeView!=null){
						linkEdge=new DefaultEdge();
						// Update end points before any other changes ou edge (routing ?).
						linkEdge.setSource(srcPort);
						linkEdge.setTarget(toBeView.getDestPort()); 
						AttributeMap at=linkEdge.getAttributes();
						GraphConstants.setLineEnd(at, GraphConstants.ARROW_CLASSIC);
						GraphConstants.setLineBegin(at, GraphConstants.ARROW_CIRCLE);
						GraphConstants.setEndFill(at, true);
						/***
						Vector v=new Vector(4);
						v.add(new Point(10, 10));
						v.add(new Point(20,20));
						v.add(new Point(40,40));
						v.add(new Point(60,60));
						GraphConstants.setPoints(at, v);
						***/
						//GraphConstants.setLineStyle(at, GraphConstants.STYLE_ORTHOGONAL); 
						//GraphConstants.setRouting(at, GraphConstants.ROUTING_DEFAULT);
						graph.getModel().insert(new  Object[]{linkEdge}, null, null, null, null);
						strLabel+=":@"+oi.uniqueID();
						//doLayout();
					}else{
						// ref to a non displayed instance
						// Would we display this string in all cases ? 
						String cn=oi.referenceType().name(); // TODO: strip package.
						strLabel+=":"+cn+"@"+oi.uniqueID(); 
					}
				}
			}

			@Override
			public void accesInt(IntegerValue i) {
				strLabel+=":"+i.intValue();
			}

			@Override
			public void accessBoolean(BooleanValue b) {
				strLabel+=":"+b.booleanValue();
			}

			@Override
			public void accessNull() {
				strLabel+=":null";
			}

			@Override
			public void accessString(StringReference s) {
				strLabel+=":"+s;
				
			}

			@Override
			public void accessVoid(VoidValue v) {
				System.err.println(this.getClass().getName()+"Sucks in creator"); //TODO: log it
			}

			@Override
			public void accessArrayReference(ArrayReference v) {
				accesInstance(v);
			}
		});
		
		}finally{
		graph.getModel().endUpdate();
		}
	}
	// Must be in a beginUpdate/endUpdate...
	private void removeLinkEdge(){
		if (this.linkEdge!=null){
			this.linkEdge.removeAllChildren();
			this.linkEdge.removeFromParent();
			mf.getGraph().getModel().remove(new  Object[]{this.linkEdge});
			this.linkEdge=null;
		}
	}
	
	private void doLayout(){ // in begin/and update scope 
        JGraphFacade facade = new JGraphFacade(mf.getGraph()); // Pass the facade the JGraph instance
        JGraphLayout layout = new JGraphSimpleLayout(JGraphSimpleLayout.TYPE_CIRCLE ); // Create an instance of the appropriate layout
        layout.run(facade); // Run the layout on the facade. Note that
        //layouts do not implement the Runnable interface, to avoid confusion
        Map nested = facade.createNestedMap(true, true); // Obtain a map of the resulting attribute changes from the facade
        mf.getGraph().getGraphLayoutCache().edit(nested); // Apply the results to the actual graph
	}

	void clear(Vector<Object> removedCells) {
		removedCells.add(this.labelCell);
		if (this.linkEdge!=null){
			removedCells.add(this.linkEdge);
			this.linkEdge.removeAllChildren();
			this.linkEdge.removeFromParent();
		}
		if (this.srcPort!=null){
			removedCells.add(this.srcPort);
		}
	}
}
