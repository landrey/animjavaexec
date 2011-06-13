package fr.loria.madynes.animjavaexec;

import java.awt.EventQueue;
import java.awt.Frame;
import java.io.FileNotFoundException;
import java.text.MessageFormat;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.ArrayReference;
import com.sun.jdi.BooleanValue;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.IntegerValue;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Method;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.ReferenceType;
import com.sun.jdi.StackFrame;
import com.sun.jdi.StringReference;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Value;
import com.sun.jdi.VoidValue;
import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.LocatableEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.MonitorContendedEnterEvent;
import com.sun.jdi.event.MonitorContendedEnteredEvent;
import com.sun.jdi.event.MonitorWaitEvent;
import com.sun.jdi.event.MonitorWaitedEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.ThreadStartEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.EventRequestManager;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.ModificationWatchpointRequest; 
import com.sun.jdi.request.StepRequest;

import fr.loria.madynes.animjavaexec.execution.model.ExecutionModel;
import fr.loria.madynes.animjavaexec.execution.model.ManageReturn;
import fr.loria.madynes.animjavaexec.jpdautils.JdiEventListener;
import fr.loria.madynes.animjavaexec.jpdautils.SimpleTrace;
import fr.loria.madynes.animjavaexec.jpdautils.VMOutputListener;
import fr.loria.madynes.animjavaexec.jpdautils.ValueAccess;
import fr.loria.madynes.animjavaexec.jpdautils.ValueAccessor;
import fr.loria.madynes.animjavaexec.view.SourceFrame;
import fr.loria.madynes.animjavaexec.view.memoryview.FiltersFrame;
import fr.loria.madynes.animjavaexec.view.memoryview.MemoryFrame;
import fr.loria.madynes.javautils.Properties;
import fr.loria.madynes.javautils.PropertiesPreferencesEditor;


public class Main {

	private String mainSignature; // TODO: in properties...
	
	///TODO: remove, debug.
	private static DisplayValue displayValue=new DisplayValue();
	
	private  JdiEventListener jdiEventListener=null;
	private  SimpleTrace st; 
	private  ExecutionModel execModel;
	
	private SourceFrame sf;
	private PropertiesPreferencesEditor propEditor;
	private FiltersFrame filtersEditor;
	
	private Main(){ // Let create one instance... Useful ?
		execModel=new ExecutionModel();
		// To "permanent" window
		sf=new SourceFrame(Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.sourceWindowTitle","Sources"), 
                null, null, 
                50, 100); // TODO: in properties, and prefs...
		//final ControlFrame cp=new ControlFrame("Control Panel"); // panel  // TODO resource + bundle
		MemoryFrame mf=new MemoryFrame(Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.memoryWindowTitle", "Memory"), 
	              													null, 100, 50);
		
		// Link to model and view.
		sf.observe(execModel);
		//cp.observe(execModel);
		mf.observe(execModel);
		// GUI actions on controller.
	    CommandForwarder commandForwarder=new CommandForwarder(); // over kill. This object should be accessible to some other...
	    commandForwarder.addCommandListener(new MainCommandListener());
	    sf.generateToForwarder(commandForwarder);
	    //cp.generateToForwarder(commandForwarder);
	    mf.generateToForwarder(commandForwarder);
	    
	    jdiEventListener=new MainJdiEventListener();
		
	}
	private  void abortExec() {
		st.getVm().exit(0);
		// model update will be triggered by jdi event handler (VMDeath, vmDisconnect events).  
	}

	private  void runExec(String className) throws ClassNotFoundException, Exception {
		Logger.getLogger("").logp(Level.INFO,
				this.getClass().getName(), "runExec",
									System.getProperty("java.class.path"));
		assert className!=null;
		try{
			className=className.trim();
			ClassLoader.getSystemClassLoader().loadClass(className); // Just a check. We catch ClassNotFoundException
		    if (st!=null){// kill  current execution
		    	// Not useful, but add extra clean-up add catch anything as the VM is probably already aborted.
		    	try{
		    	   st.getVm().exit(0);
		    	}catch(Throwable anything){}
		    	st=null;
		    }
		    
		    mainSignature=className+".main(java.lang.String[])"; //<<<======= Side effect. Needed by jdiEventListener 
		    // TODO: use include/exclude.
		    //wantedPackage = className.substring(0, className.lastIndexOf('.'))+"*"; //<<<==== Side effect. Needed by jdiEventListener
		    execModel.getStateManager().finish();
		    execModel.getStateManager().exec(className);
			st=new SimpleTrace(new  String[]{className});
			// listen to wanted events from debuggee. (not activated now, see st.monitorJVM(); below
			st.addJdiEventListener(jdiEventListener);
			st.getVm().setDebugTraceMode(0);
				
			EventRequestManager erm=st.getVm().eventRequestManager();
			
			MethodEntryRequest 	mer=erm.createMethodEntryRequest();
			//mer.addClassFilter(wantedPackage);
			SimpleTrace.addFilters(mer, execModel.getFiltersManager().getIncludeFilters(), execModel.getFiltersManager().getExcludeFilters());
			mer.setSuspendPolicy(EventRequest.SUSPEND_ALL);
			mer.enable();
				
			MethodExitRequest mexr=erm.createMethodExitRequest();
			//mexr.addClassFilter(wantedPackage);
			SimpleTrace.addFilters(mexr, execModel.getFiltersManager().getIncludeFilters(), execModel.getFiltersManager().getExcludeFilters());
			//mexr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			mexr.setSuspendPolicy(EventRequest.SUSPEND_ALL); // Need to stop to observe return value on stack...
			mexr.enable();
			
				
			ClassPrepareRequest cpr=erm.createClassPrepareRequest();
			//cpr.addClassFilter(wantedPackage);
			SimpleTrace.addFilters(cpr, execModel.getFiltersManager().getIncludeFilters(), execModel.getFiltersManager().getExcludeFilters());
			cpr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			cpr.enable();
			
			//-- Get exception if smth sucks when starting jvm, connector... (Typically: the debugged class can not be found...)
			ExceptionRequest exr=erm.createExceptionRequest(null, false, true); // report only uncaught exception. 
			exr.setSuspendPolicy(EventRequest.SUSPEND_ALL); // TODO: OK ? Suspend => have a chance to display exception location before WM ends.
			//exr.setSuspendPolicy(EventRequest.SUSPEND_NONE);
			//exr.enable();
			// link VM std out and std error to OutputManager...
			st.addStdOutListener(new VMOutputListener(){
				@Override
				public void flush() {
				}

				@Override
				public void write(char[] cbuf, int count) {
					execModel.getOutputManager().addFromStandardOut(new String(cbuf, 0, count)); 
					//use the NON deprecated  3 args String constructor with default charset...
				}
			});
			st.addStdErrListener(new VMOutputListener(){
				@Override
				public void flush() {
				}

				@Override
				public void write(char[] cbuf, int count) {
					execModel.getOutputManager().addFromStandardOut(new String(cbuf, 0, count)); 
					//use the NON deprecated  3 args String constructor with default charset...
				}
				});
			// Keep runned class name in preferences.... As this class name seems to be ok.
			Properties.getDefaultProperties().setPreference("fr.loria.madynes.animjavaexec.Main.runClassName", className);
			st.monitorJVM();
				//st.joint(); // We get WM completion by VMEvent
				//execModel.getStateManager().finish();
		}catch(ClassNotFoundException cnfe){
			Logger.getLogger("").logp(Level.INFO,
					this.getClass().getName(), "runExec",
					MessageFormat.format(Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.canNotLoadClass", "can not load class: {0}"),
										 className));
			throw cnfe;
		}catch(Exception other){ // from new SimpleTrace
			Logger.getLogger("").logp(Level.WARNING,
					this.getClass().getName(), "runExec",
										Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.canNotStartTrace", "can not start trace"),
										other);
			throw other;
		}
	}
	
	public static void main(String[] args) throws Exception {
		// Conf log first (use by Properties package).
		//TODO: find a standard way to configure this:
		Logger.getLogger("").setLevel(Level.ALL);
		//Logger.getLogger("").setLevel(Level.WARNING);
		Properties.setMessages("fr.loria.madynes.animjavaexec.MessagesBundle");
		Properties.setDefaultProperties("fr.loria.madynes.animjavaexec.resources");
		// Properties.setProperties("fr.loria.madynes.animjavaexec.resources");
		
		
		Main main=new Main();
		 // TODO: better parsing for  options and args.
		
		if (args.length>0){
			//main.runExec(args[0]); // For now argument = main class.
			// Chiasse we have a race condition on smth...
			// So we just DISPLAY the class to run. The user has to push the run button...
			Logger.getLogger("").logp(Level.INFO,
					Main.class.getClass().getName(), "main",
					MessageFormat.format(Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.readyToRun", "ready tu run: {0}"),
							args[0]));
			main.sf.setClassText(args[0]);	
		}else{
			String className=Properties.getDefaultProperties().getProperty("fr.loria.madynes.animjavaexec.Main.runClassName").trim();
			if (!className.isEmpty()){
				main.sf.setClassText(className);
			}
			// ?  Source and Memory Frame are there... Just wait for user interaction...
			// Use a property to keep the last run class ?
		}
	}//main
	
	
	
	private static class DisplayValue implements ValueAccessor {
	    String display="null";
		@Override
		public void accesInstance(ObjectReference oi) {
			display="<"+oi.toString()+">";
		}

		@Override
		public void accesInt(IntegerValue i) {
			display=Integer.toString(i.intValue())+" (@"+i.hashCode()+")";
		}

		@Override
		public void accessBoolean(BooleanValue b) {
		}

		@Override
		public void accessNull() {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void accessString(StringReference s) {
			display=s.value();
		}

		@Override
		public void accessVoid(VoidValue v) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void accessArrayReference(ArrayReference ar) {
			accesInstance(ar);
		}
	}

	private class MainCommandListener implements CommandListener {
		// As execModelController
		@Override
		public void abort() {
			abortExec(); // don't go into a wild recursion !!
		}

		@Override
		public void exec(String className) throws ClassNotFoundException, Exception {
			runExec(className);
		}

		@Override
		public void step() {
			execModel.getStateManager().setNeedStep(false); //Order ?
			st.getVm().resume();
		}

		@Override
		public void changeFilters(String[] includeFilters,
				                  String[] excludeFilters) {
			execModel.getFiltersManager().setFilters(includeFilters, excludeFilters);
		}
		
		// Gui interaction.
		@Override
		public void quit() {
			// see http://download.oracle.com/javase/6/docs/api/java/awt/doc-files/AWTThreadIssues.html
			
			 Runnable r = new Runnable() {
		            public void run() {
		                Object o = new Object();
		                try {
		                    synchronized (o) {
		                        o.wait();
		                    }
		                } catch (InterruptedException ie) {
		                }
		            }
		        };
		        Thread t = new Thread(r);
		        t.setDaemon(false);
		       //t.start();
			
			for (Frame f:Frame.getFrames()){
				f.dispose(); // will call possible JFramePropertiesMgr saving mechanism.
			}
			EventQueue.invokeLater( new Runnable() {
				public void run() {
					if (Properties.getDefaultProperties().isPreferencesChanged()){
						try {
							Properties.getDefaultProperties().savePreferences();
						} catch (FileNotFoundException e) {
							Logger.getLogger("").logp(Level.WARNING,
									this.getClass().getName(), "quit",
									Properties.getOptionalMessage("fr.loria.madynes.animjavaexec.Main.canNotSavePrefs", 
													             "can not save  preferences"));
						}
					}
					// TODO: probably more stuff here.
					System.exit(0);	// If everything is ok, useless...
				}
			});
			// We also could use EventQueue.invokeANdWait and then have all final code here:
		}

		@Override
		public void editPreferences() {
			// preferencesEditor is NOT an observer of the editable properties => We should ensure one unique preferencesEditor Frame
			if (propEditor==null){
				propEditor=new PropertiesPreferencesEditor(Properties.getDefaultProperties(), Properties.getMessages());
			}else{
				propEditor.setVisible(true);
			} 
		}
		@Override
		public void editFilters() {
			// filtersEditor is NOT an observer of FilterModel => We should ensure one unique
			// filterEditor frame.
			if (filtersEditor==null){
				filtersEditor=new FiltersFrame("Edit filters", null, execModel);
			}else{
				filtersEditor.setVisible(true);
			}
		}

		@Override
		public void addToStdIn(String s) {
			st.writeToStdIn(s);
		}
	}//MainCommandListener
	
	private class MainJdiEventListener implements JdiEventListener { 
		String display;

		@Override
		public void accessWatchpoint(AccessWatchpointEvent e) {
			// TODO Auto-generated method stub

		}

		@Override
		public void breakpoint(BreakpointEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void classPrepare(ClassPrepareEvent e){
			try {
				execModel.getSourcesManager().addSource(
						e.referenceType().sourcePaths("Java").get(0));
				// Create ModificationWatchEvent for this class fields...
				EventRequestManager erm = st.getVm().eventRequestManager();
				for (Field fd : e.referenceType().allFields()) {
					// TODO: improve representation of classes: add static
					// values representation.
					// TODO: set ModificationWatchpointRequest only for class in includes filter and out of exclude filters
					if (!fd.isStatic()) {
						ModificationWatchpointRequest mer = erm
								.createModificationWatchpointRequest(fd);
						//mer.addClassFilter(wantedPackage);
						SimpleTrace.addFilters(mer, execModel.getFiltersManager().getIncludeFilters(), execModel.getFiltersManager().getExcludeFilters());
						mer.setSuspendPolicy(EventRequest.SUSPEND_NONE);
						mer.enable();
					}//TODO: handle static stuff.
				}
			} catch (AbsentInformationException e1) {
				e1.printStackTrace(); // TODO: log+info+give-up !
			}
		}

		@Override
		public void classUnload(ClassUnloadEvent e) {
			// TODO Auto-generated method stub
		}

		@Override
		public void exception(ExceptionEvent e) {
			ObjectReference or = e.exception();
			ReferenceType rt = or.referenceType();
			LogManager.getLogManager().getLogger("").logp(Level.INFO,
					this.getClass().getName(), "exception", "In debuggee. detailMessage="+or.getValue(rt.fieldByName("detailMessage")));
			//TODO: a dedicated Exception manager+view. 
			execModel.getOutputManager().exceptionOccurred(or, rt);
			pushToModel(e);
			this.updateStep(e);
		}

		@Override
		public void methodEntry(MethodEntryEvent e) {
			LogManager.getLogManager().getLogger("").entering(this.getClass().getName(), "methodEntry");
			Method m = e.method();
			String tmp=m.toString(); //TODO: use signature() and change mainSignature accordingly.
			if (mainSignature.equals(tmp)) {
				LogManager.getLogManager().getLogger("").logp(Level.INFO,
						this.getClass().getName(), "methodEntry",
						"Set line stepping");
				// go Stepping next into thread of the public static void
				// main...
				EventRequestManager erm = st.getVm().eventRequestManager();
				StepRequest sr = erm.createStepRequest(e.thread(),
						StepRequest.STEP_LINE, StepRequest.STEP_INTO);
				sr.setSuspendPolicy(EventRequest.SUSPEND_ALL);
				//sr.addClassFilter(wantedPackage);
				SimpleTrace.addFilters(sr, execModel.getFiltersManager().getIncludeFilters(), execModel.getFiltersManager().getExcludeFilters());
				sr.enable();
			}
			pushToModel(e); // stack update.
			if (m.isConstructor()) { // heap update. Constructor does not
										// guaranty that we are really build a
										// new instance (this(...) call.
				try {
					if (execModel.getInstanceManager().manage(
							e.thread().frame(0).thisObject()) == ManageReturn.ADDED) {
						// nothing TODO: clean
					}
				} catch (IncompatibleThreadStateException e1) {
					Logger.getLogger("").logp(Level.WARNING,
							this.getClass().getName(), "methodEntry",
							"All that sucks !", e1);
				}

				// NOTE: modification watch points for instance fields are
				// created at CLASS LOADING TIME
			}
			// At last. Enable next step.
			updateStep(e);
			LogManager.getLogManager().getLogger("").exiting(this.getClass().getName(), "methodEntry");
		}

		@Override
		public void methodExit(MethodExitEvent e) {
			pushToModel(e, true, e.returnValue());
			updateStep(e);
		}

		@Override
		public void modificationWatchpoint(ModificationWatchpointEvent e) {
			Logger.getLogger("").logp(Level.FINER,
					this.getClass().getName(), "modificationWatchpoint",
					"field " + e.field() + " is modified");
			execModel.getInstanceManager().fieldChange(e.object(), e.field(),
					e.valueToBe());
			// We are on step mode. ModificationWatchpointEvent are configured
			// with a SUSPEND_NONE policy.
			// So no update to control panel (and so no resume) is needed.
			// So if we decide to do otherwise, we are ready:}
			updateStep(e);
		}

		@Override
		public void monitorContendedEnter(MonitorContendedEnterEvent e) {
		}

		@Override
		public void monitorContendedEntered(MonitorContendedEnteredEvent e) {
		}

		@Override
		public void monitorWait(MonitorWaitEvent e) {
		}

		@Override
		public void monitorWaited(MonitorWaitedEvent e) {
		}

		@Override
		public void step(StepEvent e) {
			pushToModel(e);
			execModel.getStateManager().setNeedStep(true);
		}

		@Override
		public void threadDeath(ThreadDeathEvent e) {
		}

		@Override
		public void threadStart(ThreadStartEvent e) {
		}

		@Override
		public void vmDeath(VMDeathEvent e) {
			execModel.getStateManager().finish(); // TODO: extra param ?
		}

		@Override
		public void vmDisconnect(VMDisconnectEvent e) {
			execModel.getStateManager().finish(); // TODO: extra param ?
		}

		@Override
		public void vmStart(VMStartEvent e) {
		}

		private void updateStep(Event e) {
			if (e.request().suspendPolicy() == EventRequest.SUSPEND_ALL) {
				execModel.getStateManager().setNeedStep(true); // update control
																// panel
			}
		}

		private void pushToModel(LocatableEvent le, boolean hasReturnValue,
				Value returnValue) {
			try {
				execModel.getCurrentLineManager().changeCurline(
						le.location().sourcePath(), le.location().lineNumber());
				execModel.getExecStack().update(le.thread(), hasReturnValue,
						returnValue);
				execModel.getArrayManager().updateAll(); // NO event to detect individuall array elt change... 
			} catch (AbsentInformationException e) {
				Logger.getLogger("").logp(Level.WARNING,
						this.getClass().getName(), "pushToModel",
						"can not acces to sourcePath. Use -g to compile."+e);
			}
			// printThreadStack(le.thread());
		}

		private void pushToModel(LocatableEvent le) {
			pushToModel(le, false, null);
		}

		// for debug debug...
		@SuppressWarnings("unused")
		private void printThreadStack(ThreadReference tr) {
			try {
				for (int fi = 0; fi < tr.frameCount(); ++fi) {
					printStackFrame(tr.frame(fi), fi);
				}
			} catch (IncompatibleThreadStateException e) {
				e.printStackTrace(); // TODO:log
			}
		}

		// for debug use
		private void printStackFrame(StackFrame sf, int pos) {
			System.out.println("[---- " + pos);
			try {
				ObjectReference thisO = sf.thisObject();
				if (thisO != null) {
					System.out.println("THIS: " + thisO.referenceType() + "="
							+ thisO.uniqueID());
				}
				for (LocalVariable lv : sf.visibleVariables()) {
					display = (lv.isArgument() ? "PARAM:" : "VAR: ")
							+ lv.name() + ":" + lv.typeName() + "=";
					ValueAccess.access(sf.getValue(lv), displayValue);
					System.out.println(display + displayValue.display);
				}
			} catch (AbsentInformationException e) {
				e.printStackTrace(); // TODO: log it.
			}
			System.out.println("----] " + pos);
		}
	}// MainJdiEventListener
}
