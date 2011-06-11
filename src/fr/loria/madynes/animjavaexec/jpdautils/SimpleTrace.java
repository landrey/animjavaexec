package fr.loria.madynes.animjavaexec.jpdautils;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.sun.jdi.Bootstrap;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.connect.Connector;
import com.sun.jdi.connect.IllegalConnectorArgumentsException;
import com.sun.jdi.connect.LaunchingConnector;
import com.sun.jdi.connect.VMStartException;
import com.sun.jdi.request.ClassPrepareRequest;
import com.sun.jdi.request.ClassUnloadRequest;
import com.sun.jdi.request.EventRequest;
import com.sun.jdi.request.ExceptionRequest;
import com.sun.jdi.request.MethodEntryRequest;
import com.sun.jdi.request.MethodExitRequest;
import com.sun.jdi.request.MonitorContendedEnterRequest;
import com.sun.jdi.request.MonitorContendedEnteredRequest;
import com.sun.jdi.request.MonitorWaitRequest;
import com.sun.jdi.request.MonitorWaitedRequest;
import com.sun.jdi.request.StepRequest;
import com.sun.jdi.request.WatchpointRequest;

public class SimpleTrace {
	private VirtualMachine vm;
	private JdiEventDispatcherThread dt=null;
	private OutputReaderThread stdOutReader;
	private OutputReaderThread stdErrReader;
	//private OutputStream buffuredStdIn=null;
	public 	SimpleTrace(String[] args) throws Exception{
		vm=launchConnect(args);
		dt=new JdiEventDispatcherThread(vm);
		//monitorJVM(vm);
	}
	
	public VirtualMachine getVm(){
		return vm;
	}
	
	public void addJdiEventListener(JdiEventListener l){
		dt.addListener(l);
	}
	
	/**
	 * Set a listener on the standard output of the debuggee application.
	 * Note: this is the [@link VirtualMachine.process().getInputStream()} which is a little confusing...
	 * @param l
	 */
	public void addStdOutListener(VMOutputListener l){
		if (this.stdOutReader==null){
			this.stdOutReader=new OutputReaderThread(vm.process().getInputStream());
			this.stdOutReader.addListener(l);
			this.stdOutReader.start();
		}else{
			this.stdOutReader.addListener(l);
		}
	}
	
	/**
	 * Set a listener on the standard error of the debuggee application
	 * @param l
	 */
	public void addStdErrListener(VMOutputListener l){
		if (this.stdErrReader==null){
			this.stdErrReader=new OutputReaderThread(vm.process().getErrorStream());
			this.stdErrReader.addListener(l);
			this.stdErrReader.start();
		}else{
			this.stdErrReader.addListener(l);
		}
	}
	/**
	 * Get an OuputStrean piped to the standard INPUT of the debuggee application.
	 * 
	 * @return
	 */
	public OutputStream getStdIn(){
			return this.vm.process().getOutputStream();
	}
	
	public void writeToStdIn(String s){
		try {
			this.getStdIn().write(s.getBytes());
			this.getStdIn().flush();
			
		} catch (IOException e) {
			Logger.getLogger("").logp(Level.WARNING, "",
						this.getClass().getName(), "writeToStdIn", e);
		}
	}
	private VirtualMachine launchConnect(String[] args) throws Exception{
		VirtualMachine result;
		LaunchingConnector cx=getCommandLineConnector();
		Map<String,Connector.Argument> cxArgs=setMainArgs(cx, args);
		try {
			Logger.getLogger("").logp(Level.FINER, this.getClass().getName() , "launchConnect", "connection args: "+cxArgs);
			result=cx.launch(cxArgs);
			Logger.getLogger("").logp(Level.FINEST, this.getClass().getName() , "launchConnect", "connection: "+cx);
		}catch(IOException ioe){
			throw new Exception("!!!!!!!!!!!!!!!!!!!!!!Unable to launch JVM:", ioe); //TODO log
		}catch(IllegalConnectorArgumentsException icae){
			throw new Exception("!!!!!!!!!!!!!!!!!!!!!!Internal error:", icae); //TODO log
		}catch(VMStartException vmse){
			throw new Exception("!!!!!!!!!!!!!!!!!!!!JVM failed to start:", vmse); //TODO log
		}
		return result;
	}
	
	 private LaunchingConnector getCommandLineConnector() {
		 List<Connector> connectors = Bootstrap.virtualMachineManager().allConnectors();
		 for(Connector connector : connectors){
			 if (connector.name().equals("com.sun.jdi.CommandLineLaunch")) {
				 	return (LaunchingConnector)connector;
	            }	
	        }
	     throw new Error("No command line launching connector"); // log.
	 }
	 
	 /** Set class to debug and arguments in the connector 'main" argument, 
	  * which will be transmitted to the to be launched jvm 
	  * 
	  * @return
	  */
	 private Map<String,Connector.Argument> setMainArgs(LaunchingConnector cx, String[] args){
		 Map<String,Connector.Argument> cxArgs=cx.defaultArguments();
		 Connector.Argument mainArgs=(Connector.Argument)cxArgs.get("main");
		 if (mainArgs==null){
			 throw new Error("Cannot get command line launcher main arguments !"); //TODO log
		 }
		 // merge in one string all StimpleTrace args  and set it as main arg for connector
		 StringBuffer sb = new StringBuffer();
	     for (int i=0; i < args.length; ++i) {
	    	 sb.append(args[i]);
	    	 if (i!=args.length) {sb.append(' ');}
	     }
	     mainArgs.setValue(sb.toString());
	     return cxArgs;
	 }	
	 
	 public  void monitorJVM(){
		 dt.start();
		 vm.resume();
	 }

	public void joint() {
		try {
			dt.join();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// Utilities on Event, as Event type hierachy does not factorize many things.
	// Using reflection we have weird exception as: Class fr.loria.madynes.animjavaexec.jpdautils.SimpleTrace can not access a member of class com.sun.tools.jdi.EventRequestManagerImpl$ClassVisibleEventRequestImpl with modifiers "public synchronized"
    // LA: can not find out with (il is public)...
	// 
	private  static  boolean addFilter(EventRequest er, String methodName, String filter){
		assert filter!=null && methodName!=null;
		Method m=null;
		//System.out.println(this.getClass());
		try {
			m = er.getClass().getMethod(methodName, String.class); //TODO cache it.
			m.invoke(er, filter);
			return true;
		}catch (Exception e){
			Logger.getLogger("").logp(Level.SEVERE,
					"fr.loria.madynes.animjavaexec.jpdautils.addFilter", "addFilter", "can set filter on this jdi event request: "+er+ "."+methodName);
			return false;
		}
	}
	/*--
	public static  boolean addClassFilter(EventRequest er, String filter){
		return addFilter(er, "addClassFilter", filter);
	}
	
	public  static  boolean addClassExclusionFilter(EventRequest er, String filter){
		return addFilter(er, "addClassExclusionFilter", filter);
	}
	--*/
	private  static boolean addFilters(EventRequest er, String methodName, String...filters){
		assert filters!=null && methodName!=null;
		Method m=null;
		//System.out.println(this.getClass());
		try {
			synchronized (er){
				m = er.getClass().getMethod(methodName, String.class); //TODO cache it.
				for (String f:filters){
					m.invoke(er, f);
				}
			}
			return true;
		}catch (Exception e){
			Logger.getLogger("").logp(Level.SEVERE,
					"fr.loria.madynes.animjavaexec.jpdautils.addFilter", "addFilters", "can set filters on this jdi event request: "+er+ "."+methodName);
			return false;
		}
	}
	public static boolean addClassFilters(EventRequest er, String...filters){
		//return addFilters(er, "addClassFilter", filters);
		for(String f:filters){
			addClassFilter(er, f);
		}
		return true;
	}
	public static  boolean addClassExclusionFilters(EventRequest er, String...filters){
		//return addFilters(er, "addClassExclusionFilter", filters);
		for(String f:filters){
			addClassExclusionFilter(er, f);
		}
		return true;
	}
	
	public  static boolean addFilters(EventRequest er, String[] includefilters, String[] excludeFilters){
		boolean r=addClassFilters(er, includefilters);
		boolean r2=addClassExclusionFilters(er, excludeFilters); //NOTE: && short can lead to funny situation.;
		return r&&r2;
	}
	
	// What we want is delegates !!!
	public static void addClassFilter(EventRequest er, String filter){
		if (er instanceof WatchpointRequest){ // for *WatchpointRequest
			((WatchpointRequest)er).addClassFilter(filter);
		}else if (er instanceof ClassPrepareRequest){
			((ClassPrepareRequest)er).addClassFilter(filter);
		}else if (er instanceof ClassUnloadRequest){
			((ClassUnloadRequest)er).addClassFilter(filter);
		}else if (er instanceof ExceptionRequest){
			((ExceptionRequest)er).addClassFilter(filter);
		}else if (er instanceof MethodEntryRequest){
			((MethodEntryRequest)er).addClassFilter(filter);
		}else if (er instanceof MethodExitRequest){
			((MethodExitRequest)er).addClassFilter(filter);
		}else if (er instanceof MonitorContendedEnteredRequest){
			((MonitorContendedEnteredRequest)er).addClassFilter(filter);
		}else if (er instanceof MonitorContendedEnterRequest){
			((MonitorContendedEnterRequest)er).addClassFilter(filter);
		}else if (er instanceof MonitorWaitedRequest){
			((MonitorWaitedRequest)er).addClassFilter(filter);
		}else if (er instanceof MonitorWaitRequest){
			((MonitorWaitRequest)er).addClassFilter(filter);
		}else if (er instanceof StepRequest){
			((StepRequest)er).addClassFilter(filter);
		}else if (er instanceof StepRequest){
			((StepRequest)er).addClassFilter(filter);
		}else{
			Logger.getLogger("").logp(Level.WARNING, "SimpleTrace", "addFilter", "Can not apply addFilter on this object ("+er+")");
		}
	}
	
	public static void addClassExclusionFilter(EventRequest er, String filter){
		if (er instanceof WatchpointRequest){ // for *WatchpointRequest
			((WatchpointRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof ClassPrepareRequest){
			((ClassPrepareRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof ClassUnloadRequest){
			((ClassUnloadRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof ExceptionRequest){
			((ExceptionRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MethodEntryRequest){
			((MethodEntryRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MethodExitRequest){
			((MethodExitRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MonitorContendedEnteredRequest){
			((MonitorContendedEnteredRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MonitorContendedEnterRequest){
			((MonitorContendedEnterRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MonitorWaitedRequest){
			((MonitorWaitedRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof MonitorWaitRequest){
			((MonitorWaitRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof StepRequest){
			((StepRequest)er).addClassExclusionFilter(filter);
		}else if (er instanceof StepRequest){
			((StepRequest)er).addClassExclusionFilter(filter);
		}else{
			Logger.getLogger("").logp(Level.WARNING, "SimpleTrace", "addFilter", "Can not apply addFilter on this object ("+er+")");
		}
	}
	
}


