package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;

import fr.loria.madynes.animjavaexec.view.GuiUtils;

public class LinkView {
	private static final Color linkColor = Color.BLACK;//TODO: push in properties
	private static Path2D.Double arrowHead= GuiUtils.createArrow(0, 15, Math.toRadians(10)); //TODO: push arrow shape in properties. 
	private NameNotFixedValueView from;
	private InstanceOrArrayView to;
	public InstanceOrArrayView getTo() {
		return to;
	}
	LinkView(MemoryView memoryView, NameNotFixedValueView  from, InstanceOrArrayView to){
		this.from=from;
		this.to=to;
		to.addLinkEnd(this);
		from.addLinkStart(memoryView, this);
		memoryView.add(this);
	}
	void remove(MemoryView memoryView){
		this.from.removeLink(this);
		this.to.removeLinkEnd(this);
		memoryView.forget(this);
	}
	// do not call it paint to avoid any usage confusion
	void draw(Graphics2D g){
		//System.out.println("link paint for ");
		int fromX=from.getLinkSourceX();
		int fromY=from.getLinkSourceY();
		int toX=to.getLinkEndX();
		int toY=to.getLinkEndY();
		AffineTransform at = AffineTransform.getTranslateInstance(toX, toY);
		at.rotate(GuiUtils.toRad(fromX, fromY, toX, toY)); // Math.acos((toX-fromX)/Point2D.distance(fromX, fromY, toX, toY));
		g.setColor(linkColor);
		g.drawLine(fromX, fromY, toX, toY);
		g.draw(at.createTransformedShape(arrowHead));
	}
}
