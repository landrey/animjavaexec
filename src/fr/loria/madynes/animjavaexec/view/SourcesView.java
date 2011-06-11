package fr.loria.madynes.animjavaexec.view;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Scrollbar;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import fr.loria.madynes.animjavaexec.execution.model.CurrentLineManager;
import fr.loria.madynes.animjavaexec.execution.model.SourcesManager;
import fr.loria.madynes.animjavaexec.execution.model.SourcesManager.SourcesChangeEvent;
import fr.loria.madynes.javautils.Properties;

/** A scrolled JPanel to display sources and the last (current) executed line).
 * 
 * @see MainFrame.
 * @author andrey
 *
 */
public class SourcesView extends JPanel {
	private  static final long serialVersionUID = 1L;
	private  static final String currentLineFontFamilyKey="fr.loria.madynes.animjavaexec.view.SourcesView.currentlinefontfamily";
	private  static final String currentLineFontSizeKey="fr.loria.madynes.animjavaexec.view.SourcesView.currentlinefontsize";
	private  static final String currentLineFontForegroundKey="fr.loria.madynes.animjavaexec.view.SourcesView.currentlinefontforeground";
	private  static final String otherLineFontFamilyKey="fr.loria.madynes.animjavaexec.view.SourcesView.otherlinefontfamily";
	private  static final String otherLineFontSizeKey="fr.loria.madynes.animjavaexec.view.SourcesView.otherlinefontsize";
	private  final SimpleAttributeSet currentLineSet=new SimpleAttributeSet();
	private  final SimpleAttributeSet otherLineSet=new SimpleAttributeSet();
	private  final SimpleAttributeSet sourcePathSet=new SimpleAttributeSet();
	
	private JTextPane textPane;
	private JScrollPane scrollPane;
	private JScrollBar vScrollbar;
	private StyledDocument doc=null;
	private LineBounds curLine=null;
	
	private Observer srcObserver=null;
	private Observer currentLineObserver=null;
	
	// Map: sourceName (as in jdbi, not full system path) -> lines Bounds
	private Map<String, Vector<LineBounds>> lineBounds=new HashMap<String, Vector<LineBounds>>();
	
	public SourcesView(int r, int c)  {
		super(new GridBagLayout());
		
		StyleConstants.setFontFamily(currentLineSet, Properties.getDefaultProperties().getProperty(currentLineFontFamilyKey)); 
		Properties.getDefaultProperties().addKeyObserver(currentLineFontFamilyKey, new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				//Properties props=(Properties)o;
				Properties.PropertyChangedEvent e=(Properties.PropertyChangedEvent)arg;
				//System.out.println("Set Cur line ff:"+e.getNewVal());
				updateCurrentLineFontFamily(e.getNewVal());
			}
		});
		
		StyleConstants.setFontSize(currentLineSet, Properties.getDefaultProperties().getIntProperty(currentLineFontSizeKey));
		Properties.getDefaultProperties().addKeyObserver(currentLineFontSizeKey, new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				//Properties props=(Properties)o;
				//Properties.PropertyChangedEvent e=(Properties.PropertyChangedEvent)arg;
				updateCurrentLineFontSize(Properties.getDefaultProperties().getIntProperty(currentLineFontSizeKey));
			}
		});
		
		StyleConstants.setForeground(currentLineSet,
									 Properties.getDefaultProperties().getOptinalColorProperty(currentLineFontForegroundKey, 
											 													Color.black));
		Properties.getDefaultProperties().addKeyObserver(currentLineFontForegroundKey, new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				//Properties props=(Properties)o;
				//Properties.PropertyChangedEvent e=(Properties.PropertyChangedEvent)arg;
				updateCurrentLineForeground(Properties.getDefaultProperties().getOptinalColorProperty(currentLineFontForegroundKey, 
											Color.black));
			}
		});
		
		StyleConstants.setBold(currentLineSet, true);
	
		StyleConstants.setFontFamily(otherLineSet, Properties.getDefaultProperties().getProperty(otherLineFontFamilyKey)); 
		Properties.getDefaultProperties().addKeyObserver(otherLineFontFamilyKey, new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				//Properties props=(Properties)o;
				Properties.PropertyChangedEvent e=(Properties.PropertyChangedEvent)arg;
				updateOtherLineFontFamily(e.getNewVal());
			}
		});
		
		StyleConstants.setFontSize(otherLineSet, Properties.getDefaultProperties().getIntProperty(otherLineFontSizeKey));
		Properties.getDefaultProperties().addKeyObserver(otherLineFontSizeKey, new Observer(){
			@Override
			public void update(Observable o, Object arg) {
				//Properties props=(Properties)o;
				//Properties.PropertyChangedEvent e=(Properties.PropertyChangedEvent)arg;
				updateOtherLineFontSize(Properties.getDefaultProperties().getIntProperty(otherLineFontSizeKey));
			}
		});
		//StyleConstants.setForeground(otherLineSet, Color.BLACK);
		StyleConstants.setBold(otherLineSet, false);
		
		// TODO: externalize. Preferences would be an overkill...
		StyleConstants.setFontFamily(sourcePathSet, "Time");
		StyleConstants.setFontSize(sourcePathSet, 14);
		StyleConstants.setBold(sourcePathSet, true);
		
		this.textPane= new JTextPane();
		this.textPane.setEditable(false); // just display text
		doc= textPane.getStyledDocument();
		scrollPane = new JScrollPane(textPane);
		vScrollbar=scrollPane.getVerticalScrollBar();
		vScrollbar.setMinimum(1);
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
	
	public void displaySrcFile(String sourcePath, File systemPath) throws FileNotFoundException {
		if (lineBounds.containsKey(sourcePath)){
			System.err.println(sourcePath+" already  loaded in SourceFrame"); //TODO: log it.
			return;
		}
		final Vector <LineBounds>v=new Vector<LineBounds>();
		lineBounds.put(sourcePath, v);
		final BufferedReader in=new BufferedReader(new InputStreamReader(new FileInputStream(systemPath)));

		try {
			doc.insertString(doc.getLength(), sourcePath+"\n", sourcePathSet);
			String line;
		
			while((line=in.readLine())!=null){
				int start=doc.getLength();
				doc.insertString(start, line+"\n", otherLineSet);
				// add a LineBounds at the end of the vector. It's ok:  we are reading source file from the beginning to the end...
				v.add(new LineBounds(start, doc.getLength()-start));
			}
		}catch (BadLocationException e) {
			System.err.println("Internal error in SrcFrame !!!" +e); //TODO:log it.
		}catch(IOException ioe){
			System.err.println("IO error while reading source file: "+systemPath+":"+ioe);
		}
		//this.vScrollbar.setMaximum(doc.)
	}
	
	/**
	 * Highlight new current line in in new current spurceFile 
	 * @param sourcePath may be null => no more more Highlight
	 * @param lineNumber may be <1 no more more Highlight
	 */
	public void setCurrent(String sourcePath, int lineNumber){ // debugger line numbers start at 1...
		if (lineNumber>0 && sourcePath!=null){
			Vector<LineBounds> v=this.lineBounds.get(sourcePath);
			if(v!=null){
				final LineBounds lb=v.get(lineNumber-1); // src line numbers start at 1, but vectors are like array, start at 0...
				if (lb!=null){
					if (this.curLine!=null){
						applyAttributeSet(this.curLine, otherLineSet);
					}
					applyAttributeSet(lb, currentLineSet);
					// ensure visibility...
					// from: http://www.java2s.com/Code/Java/Swing-JFC/AppendingTextPane.htm
					// The OVERALL protection with invokeLater is mine (LA) and seems very necessary !!
					 SwingUtilities.invokeLater( new Runnable() {
						 public void run() {
							final Rectangle r;
							try {
								r = textPane.modelToView(lb.start);
								 if (r != null) { // may be null even if no exception has been raised.
									  textPane.scrollRectToVisible(r);
							     }
								   // this.textPane.scrollRectToVisible(r);
							} catch (Throwable any) {
								Logger.getLogger("").logp(Level.WARNING, this.getClass().getName(), 
										"setCurrent", "modelToView failed !", any);
							}
						 }
					 });			
					this.curLine=lb;
				}else{
					// line not found in file !
					Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), 
							"setCurrent", lineNumber+" out of range in "+sourcePath);
					// TODO: what do we do ?
					// no more Highlight...
					if (this.curLine!=null){
						applyAttributeSet(this.curLine, otherLineSet);
						this.curLine=null;
					}
				}
			}else{
				Logger.getLogger("").logp(Level.SEVERE, this.getClass().getName(), 
						"setCurrent", sourcePath+" not loaded in this SourceFrame");
			}
		}else{
			// no more Highlight
			if (this.curLine!=null){
				applyAttributeSet(this.curLine, otherLineSet);
				this.curLine=null;
			}
		}
		// TODO: clean. System.out.println(vScrollbar.getValue()+" "+vScrollbar.getMinimum()+" "+vScrollbar.getMaximum());
	}
	
	
	public void observe(SourcesManager smgr){
		if(this.srcObserver==null){
			this.srcObserver=new Observer(){
				@Override
				public void update(Observable pSourcesManager, Object ev) {
					SourcesChangeEvent sourcesChangeEvent=(SourcesChangeEvent)ev;
					switch(sourcesChangeEvent.getTag()){
						case SourcesChangeEvent.ADDED:
							try {
								displaySrcFile(sourcesChangeEvent.getSourcePath(), sourcesChangeEvent.getSystemPath());
							} catch (FileNotFoundException e) {
								e.printStackTrace(); // TODO log. Should not occur quite often (if model is ok, event is generated for an existing source file....)
							}
							break;
						case SourcesChangeEvent.CLEAR_ALL:
							clearAll();
							break;
					}
				}
			};
		}
		smgr.addObserver(this.srcObserver);
	}
	
	public void observe(CurrentLineManager clmgr){
		if (this.currentLineObserver==null){
			this.currentLineObserver=new Observer(){

				@Override
				public void update(Observable pClmgr, Object dummy) { // CurrentLineManager, <not used>
					CurrentLineManager lClmgr=(CurrentLineManager)pClmgr;
					setCurrent(lClmgr.getCurrentSourcePath(),  lClmgr.getCurrentLineNumber());
				}
			};
		}
		clmgr.addObserver(this.currentLineObserver);
	}
	
	private void applyAttributeSet(LineBounds lb, SimpleAttributeSet as){
		this.doc.setCharacterAttributes(lb.start, lb.length, as, true);
	}
	
	private void updateCurrentLineFontFamily(String ff) {
		StyleConstants.setFontFamily(currentLineSet, ff);
		updateFontSetCurrentLine();
		//updateFontSetForAllLines();
	}

	private void updateCurrentLineFontSize(int fz){
		StyleConstants.setFontSize(currentLineSet, fz);
		updateFontSetCurrentLine();
		//updateFontSetForAllLines();
	}
	
	private void updateCurrentLineForeground(Color c){
		StyleConstants.setForeground(currentLineSet, c);
		updateFontSetCurrentLine();
	}
	
	private void updateFontSetCurrentLine() {
		if (this.curLine!=null){
			applyAttributeSet(this.curLine, currentLineSet);
		}
	}

	private void updateOtherLineFontFamily(String ff){
		StyleConstants.setFontFamily(otherLineSet, ff);
		updateFontSetForAllLines();
	}
	
	private void updateOtherLineFontSize(int fz){
		StyleConstants.setFontSize(otherLineSet, fz);
		updateFontSetForAllLines();
	}
	
	private void updateFontSetForAllLines() {
		for (Vector<LineBounds> v:this.lineBounds.values()){
			// Pb maj source name...
			for(LineBounds lb:v){
				if (lb.equals(this.curLine)){
					applyAttributeSet(lb, currentLineSet); // useful ??
				}else{
					applyAttributeSet(lb, otherLineSet);
				}
			}
		}
	}
	
	private class LineBounds{
		int start;
		int length;
		private LineBounds(int start, int length){
			this.start=start;
			this.length=length;
		}
		public boolean equals(Object o){
			if (o instanceof LineBounds){ // null inst of X always false
				LineBounds lbo=(LineBounds)o;
				return (this.start==lbo.start)&&(this.length==lbo.length);
			}else{
				return false;
			}
		}
	}
	
	private void clearAll() {
		try {
			this.doc.remove(0, this.doc.getLength());
		} catch (BadLocationException e) {
			//TODO:  log.
			e.printStackTrace();
		}
		this.lineBounds.clear();
	}

	/**
	 * Increase font size
	 * @param +1/-1
	 */
	void incrementFontSize(int i) {
		assert i==1 || i==-1;
		Properties dp=Properties.getDefaultProperties();
		dp.setPreference(currentLineFontSizeKey, Integer.toString(dp.getIntProperty(currentLineFontSizeKey)+i));
		dp.setPreference(otherLineFontSizeKey, Integer.toString(dp.getIntProperty(otherLineFontSizeKey)+i));
		// Properties observer will do the job.
		// TODO: merge property change events....
	}
}
