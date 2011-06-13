package fr.loria.madynes.animjavaexec.view.memoryview;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Point;
import java.util.Arrays;
import java.util.Random;

public class HeapGrid {
	private int xLeft=100;  // in points
	private int yTop= 10;  //in points
	private int cellWidth=120; // in points
	private int cellHeight=20;
	private int interColunmGap=40; // in points even
	private int interBlockGap=40; // in points even
	private int blockHeight=5; // cells count
	private int nbOfBlocksPerColumn=5;
	private int initialNumberOfColumns=6;
	
	private int blockHeightInPts=blockHeight*cellHeight;
	private int colWidthInPt=cellWidth+interColunmGap;
	private int rowHeightInPt=blockHeightInPts+interBlockGap;
	private int xRight=xLeft+initialNumberOfColumns*colWidthInPt;
	private int yBot=yTop+nbOfBlocksPerColumn*rowHeightInPt;
	
	private static final Random rand=new Random();
	
	private Block[][] blocks;// Row x Col
	//private boolean[][] free; // Row x Col
	HeapGrid(){
		blocks=new Block[nbOfBlocksPerColumn][initialNumberOfColumns];
		for (int ir=0; ir<nbOfBlocksPerColumn; ++ir){
			for(int ic=0; ic<initialNumberOfColumns; ++ic){
				blocks[ir][ic]=new Block(ir, ic);
				
			}
		}
		/*--
		free=new boolean[nbOfBlocksPerColumn][initialNumberOfColumns]; // TODO: use BitSet ?

		for (boolean[] r:free){
			Arrays.fill(r, true);
		}
		--*/
	}
	void paint(Graphics2D g){
		for (int c=0; c<initialNumberOfColumns; ++c){
			for (int r=0; r<nbOfBlocksPerColumn; ++r){
				/*--
				if (blocks[r][c].elt==null){
					g.setColor(Color.green);
				}else{
					g.setColor(Color.red);
				}
				g.fillRect(xLeft+c*colWidthInPt+interColunmGap/2, 
						     yTop+r*rowHeightInPt+interBlockGap/2, 
						     cellWidth, blockHeightInPts);
				**/
				g.drawRect(xLeft+c*colWidthInPt+interColunmGap/2, 
					     yTop+r*rowHeightInPt+interBlockGap/2, 
					     cellWidth, blockHeightInPts);
			}
		}
	}
	Point  xyToRC(int x, int y){ //TODO: remove, debug purpose.
		if (x<xLeft){
			x=xLeft;
		}else if(x>xRight){
			x=xRight-1;
		}
		if (y<yTop){
			y=yTop;
		}else if(y>yBot){
			y=yBot-1;
		}
		return new Point((x-xLeft)/colWidthInPt , (y-yTop)/rowHeightInPt);
	}
	
	void snapToGrid(InstanceOrArrayView instanceOrArrayView){ // return x=Column, y=row (as awt coordinates).
		Point startrc=this.xyToRC(instanceOrArrayView.getX(), instanceOrArrayView.getY());
		Block b=getClosestFreeBlockFromRC(startrc.y, startrc.x);
		b.elt=instanceOrArrayView;
		instanceOrArrayView.moveTo(b.x, b.y);
	}
	private Block getClosestFreeBlockFromRC(int r, int c){
		int d=0;
		int tryr;
		int tryc;
		boolean go=true;
		while (go){
			go=false;
			tryc=c+d;
			if (tryc<initialNumberOfColumns){
				for (tryr=Math.max(0, r-d); tryr<Math.min(r+d+1, nbOfBlocksPerColumn); ++tryr){
					if (blocks[tryr][tryc].elt==null){
						return blocks[tryr][tryc];
					}
					go=true;
				}
			}
			tryc=c-d;
			if (0<=tryc){
				for (tryr=Math.max(0, r-d); tryr<Math.min(r+d+1, nbOfBlocksPerColumn); ++tryr){
					if (blocks[tryr][tryc].elt==null){
						return blocks[tryr][tryc];
					}
					go=true;
				}
			}
			tryr=r-d;
			if (0<=tryr){
				for (tryc=Math.max(0, c-d+1); tryc<Math.min(c+d, initialNumberOfColumns); ++tryc){
					if (blocks[tryr][tryc].elt==null){
						return blocks[tryr][tryc];
					}
					go=true;
				}
			}
			tryr=r+d;
			if (tryr<nbOfBlocksPerColumn){
				for (tryc=Math.max(0, c-d+1); tryc<Math.min(c+d, initialNumberOfColumns); ++tryc){
					if (blocks[tryr][tryc].elt==null){
						return blocks[tryr][tryc];
					}
					go=true;
				}
			}
			++d;
		}
		return null;
	}
	private class Block{ // non static => back link to grid... Heavy Heavy Heavy....
		public Block(int r, int c) {
			this.r=r;
			this.c=c;
			this.x=xLeft+c*colWidthInPt+interColunmGap/2;
			this.y=yTop+r*rowHeightInPt+interBlockGap/2;
		}
		private InstanceOrArrayView elt;
		int r; // redundant but handy...
		int c; 
		int x;
		int y;
	}
}
