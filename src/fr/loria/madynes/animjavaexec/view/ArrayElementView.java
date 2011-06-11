package fr.loria.madynes.animjavaexec.view;

import java.awt.Color;
import java.text.MessageFormat;
import java.util.Map;
import java.util.Vector;

import org.jgraph.graph.AttributeMap;
import org.jgraph.graph.DefaultEdge;
import org.jgraph.graph.DefaultGraphCell;
import org.jgraph.graph.DefaultPort;

import com.sun.jdi.Field;
import com.sun.jdi.Value;

import fr.loria.madynes.javautils.Properties;

class ArrayElementView {
	private static MessageFormat arrayEltFormat=
		new MessageFormat(Properties.getMessage("fr.loria.madynes.animjavaexec.view.ArrayElementView.labelFormat")); 
	private static Color arrayEltColor=Color.CYAN; // TODO: externalize it
	
	private String labelStr;
	DefaultGraphCell labelCell;
	DefaultEdge linkEdge; // can be null, for field which is NOT a ObjectReference..
	private DefaultPort srcPort; // can be null, for field which is NOT a ObjectReference..
	private  MemoryFrame mf;
	private int index;
	private Value curValue;
	ArrayElementView(int i, Value value, int x, int y,
			DefaultGraphCell grpCell, MemoryFrame mf,
			Vector<DefaultGraphCell> newCells,
			Map<DefaultGraphCell, Map<?, ?>> attributes,
			boolean needPort) {
		
		this.mf=mf;
		this.index=i;
		this.curValue=value;
		AttributeMap at=new AttributeMap();
		this.labelCell=JGraphUtils.createLabelCell(this, grpCell, newCells, at, x, y, arrayEltColor);
		attributes.put(labelCell, at);
		if (needPort){
			this.srcPort=JGraphUtils.createPortCellForElt(null, newCells, labelCell);
		}
		//TODO edge stuff
		setLabelStr();
	}

	void update(Value newValue, Vector<Object> changedCells ) {
		if ( 
			(newValue!=null)&&(newValue.equals(this.curValue))
					||
			(newValue!=this.curValue)
			){
				this.curValue=newValue;
				this.setLabelStr();
				changedCells.add(this.labelCell);
		}
	}
	
	private void setLabelStr(){ 
		String valueStr=(this.curValue==null)?"null":this.curValue.toString(); // TODO: format value.
		this.labelStr=arrayEltFormat.format(new Object[]{new Integer(index), valueStr});
	}
	
	public String toString(){
		return this.labelStr;
	}

	void clear(Vector<Object> removedCells) {
		removedCells.add(this.labelCell);
		if (this.linkEdge!=null){
			removedCells.add(this.linkEdge);
			removedCells.add(this.labelCell);
			if (this.linkEdge!=null){
				removedCells.add(this.linkEdge);
			}
			if (this.srcPort!=null){
				removedCells.add(this.srcPort);
			}
		}
		if (this.srcPort!=null){
			removedCells.add(this.srcPort);
		}
	}
}
