package fr.loria.madynes.animjavaexec.view;

import java.util.Vector;

import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;

public abstract class InstanceOrArrayView {
	
	static final int dy=40; // TODO: auto-determine this value 
	
	String labelStr;
	DefaultGraphCell grpCell;
	DefaultGraphCell labelCell;
	DefaultPort destPort;
	int  height;
	void clear(Vector<Object> removedCells) {
		removedCells.add(this.labelCell);
		removedCells.add(this.grpCell);
		removedCells.add(this.destPort);
	}
}
