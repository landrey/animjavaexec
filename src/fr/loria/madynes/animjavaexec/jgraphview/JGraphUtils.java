package fr.loria.madynes.animjavaexec.jgraphview;

import java.awt.Color;
import java.util.Vector;

import org.jgraph.JGraph;
import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;
import org.jgraph.graph.GraphConstants;

public class JGraphUtils {
	public static DefaultGraphCell createLabelCell(Object userData, 
												DefaultGraphCell inGrp, 
												Vector<DefaultGraphCell> newCells,
												AttributeMap at, 
												int x, int y, Color color){
		DefaultGraphCell result=new DefaultGraphCell(userData);
		newCells.add(result);
		inGrp.add(result);
		JGraph.createBounds(at, x, y, color); 
		GraphConstants.setEditable(at, false);
		GraphConstants.setSelectable(at, false);
		return result;
	}
	
	// create a for Field or array elt or stackelt labelcell
	public static DefaultPort createPortCellForElt(Object userData,
												   Vector<DefaultGraphCell> newCells,
												   DefaultGraphCell forCell){
		DefaultPort result=new DefaultPort(userData);
		forCell.add(result);
		result.setParent(forCell); // needed ?
	    //inGrp.add(forCell); // needed ? NO NO. If added port is really attached to the group frame !
		// Floating port.
	    //GraphConstants.setOffset(srcPort.getAttributes(), new Point2D.Double(GraphConstants.PERMILLE, GraphConstants.PERMILLE/2));
	    newCells.add(forCell); // Needed ????
	    return result;
	}   
}
