package fr.loria.madynes.animjavaexec.view;

import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

import javax.swing.AbstractButton;
import javax.swing.JButton;

import fr.loria.madynes.javautils.Properties;

/*
 * TODO: move to fr.loria.madynes.javautils.swing
 */
public class GuiUtils {
	/**
	 * 
	 * @param propertiesPrefix a resource name (key) corresponding to the text of the button's label
	 * @param action action name to set to the button, or null if not needed
	 * @param vPos vertical position (see {@link AbstractButton})
	 * @param hPos horizontal position (see {@link AbstractButton})
	 * @return
	 */
	public static JButton createButtonFromProperties(String propertiesPrefix, 
													 String action,
													 int vPos, int hPos){
		JButton result=new JButton(Properties.getMessage(propertiesPrefix));
		result.setVerticalTextPosition(vPos); 
		result.setHorizontalTextPosition(hPos); //aka LEFT, for left-to-right locales
		String tmpStr=Properties.getOptionalMessage(propertiesPrefix+"_mnemo");
		if (tmpStr!=null){
			result.setMnemonic(Integer.parseInt(tmpStr)); 
		}
		if (action!=null){
			result.setActionCommand(action);
		}
		tmpStr=Properties.getOptionalMessage(propertiesPrefix+"_tip");
		if (tmpStr!=null){
			result.setToolTipText(tmpStr); 
		}
		return result;
	}
	
	/** 
	 * Build an arrow head prototype as a  shape ({@link Path2D.Double}) 
	 * what can be transformed using {@link AffineTransform}
	 * @param length
	 * @param bar
	 * @param angle in radiant (use {@link Math.toRadians} is needed)
	 * @return a shape 
	 */
	public static  Path2D.Double createArrow(int length, int barb, double angle) {

        Path2D.Double path = new Path2D.Double();
        path.moveTo(-length/2, 0);
        path.lineTo(length/2, 0);
        double x = length/2 - barb*Math.cos(angle);
        double y = barb*Math.sin(angle);
        path.lineTo(x, y);
        x = length/2 - barb*Math.cos(-angle);
        y = barb*Math.sin(-angle);
        path.moveTo(length/2, 0);
        path.lineTo(x, y);
        return path;
    }
	
	/**
	 * Give the angle in radiant between to point (x1,y1) and (x2,y2).
	 * Note: see exact meaning for X & Y in awt.
	 * TODO: add test to now exact quadrant and so 
	 * @param x1
	 * @param y1
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double toRad(int x1, int y1, int x2, int y2){
		// TODO: case distance==0... angle is undefined...
		//return -Math.acos((x2-x1)/Point2D.distance(x1, y1, x2, y2)); 
		//return Math.acos((x2-x1)/Point2D.distance(x1, y1, x2, y2));
		return Math.asin((y2-y1)/Point2D.distance(x1, y1, x2, y2));
		/*
		 * double a=Math.acos((x2-x1)/Point2D.distance(x1, y1, x2, y2)); // in [0..Pi]
		 * return  (y2>y1) ? a :  -a // or 2*Pi-a 
		 * 
		 * 
		 */
	}
}
