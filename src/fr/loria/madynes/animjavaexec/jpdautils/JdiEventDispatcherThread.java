package fr.loria.madynes.animjavaexec.jpdautils;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;

import com.sun.jdi.VMDisconnectedException;
import com.sun.jdi.VirtualMachine;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.Event;
import com.sun.jdi.event.EventQueue;
import com.sun.jdi.event.EventSet;
import com.sun.jdi.event.ExceptionEvent;
import com.sun.jdi.event.MethodEntryEvent;
import com.sun.jdi.event.MethodExitEvent;
import com.sun.jdi.event.ModificationWatchpointEvent;
import com.sun.jdi.event.StepEvent;
import com.sun.jdi.event.ThreadDeathEvent;
import com.sun.jdi.event.VMDeathEvent;
import com.sun.jdi.event.VMDisconnectEvent;
import com.sun.jdi.event.VMStartEvent;
import com.sun.jdi.request.EventRequest;

public class JdiEventDispatcherThread extends Thread {

		private VirtualMachine vm;
		private boolean connected;
		private boolean vmDied=false;
		private boolean autoResume=true; // except stepEvent...
		private boolean autoResumeStep=false;
		private EventSet lastEventSet=null;
		
		private List<JdiEventListener> listeners=new LinkedList<JdiEventListener>();
		
		void setVm(VirtualMachine vm) {
			this.vm = vm;
		}
		private VirtualMachine getVm(){
			return vm;
		}
		JdiEventDispatcherThread(VirtualMachine vm){
	        super("jdi-event-dispatcher");
			this.setVm(vm);
			this.connected=true;
			this.vmDied=false;
		}
		public boolean isConnected(){
			return connected;	
		}
		
		public boolean isDead(){
			return vmDied;
		}
		public void addListener(JdiEventListener l){
			assert l!=null;
			listeners.add(l);
		}
		public void run(){
			 EventQueue queue = getVm().eventQueue();
			 while (connected) {
			     try {
			    	 EventSet eventSet = queue.remove();
			    	 for (Event e:eventSet){
			    		 LogManager.getLogManager().getLogger("").logp(Level.INFO,
									this.getClass().getName(), "run",
									"Process:"+e+".  SUSPEND_POLICY="+eventSet.suspendPolicy());
			    		 if (e instanceof ExceptionEvent) {
			    			 for(JdiEventListener l:listeners){
			    				 l.exception((ExceptionEvent)e);
			    			 }
			    	     } else if (e instanceof ModificationWatchpointEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.modificationWatchpoint((ModificationWatchpointEvent)e);
			    	    	 }
			    	     } else if (e instanceof MethodEntryEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.methodEntry((MethodEntryEvent)e);
			    	    	 }
			    	     } else if (e instanceof MethodExitEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.methodExit((MethodExitEvent)e);
			    	    	 }
			    	     } else if (e instanceof StepEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.step((StepEvent)e);
			    	    	 }
			    	     } else if (e instanceof ThreadDeathEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.threadDeath((ThreadDeathEvent)e);
			    	    	 }
			    	     } else if (e instanceof ClassPrepareEvent) {
			    	        for(JdiEventListener l:listeners){
			    	            l.classPrepare((ClassPrepareEvent)e);
			    	        }
			    	     } else if (e instanceof VMStartEvent) {
			    	    	 for(JdiEventListener l:listeners){
			    	            l.vmStart((VMStartEvent)e);
			    	    	 }
			    	     } else if (e instanceof VMDeathEvent) {
			    	    	 vmDied=true; 		//<<<===
			    	    	 for(JdiEventListener l:listeners){
			    	            l.vmDeath((VMDeathEvent)e);
			    	    	 }
			    	     } else if (e instanceof VMDisconnectEvent) {
			    	    	 connected=false; 	//<<<====
			    	    	 for(JdiEventListener l:listeners){
			    	            l.vmDisconnect((VMDisconnectEvent)e);
			    	    	 }
			    	     } else {
			    	            throw new Error("Unexpected event type"); // TODO: log. 
			    	     }
			    	 }// for Event e
			    	 if (eventSet.suspendPolicy()==EventRequest.SUSPEND_NONE){
			    		 eventSet.resume();
			    	 }
			     }catch(InterruptedException exc){
		                // Ignore. TODO: log.
		         }catch (VMDisconnectedException discExc) {
		                handleDisconnectedException();
		                connected=false;
		         }
			 }//while connected
		}
	    private synchronized void handleDisconnectedException() { // TODO: why synchronized ?
	    	EventQueue queue = getVm().eventQueue();
	    	while (connected) {
	    		try {
	    			EventSet eventSet = queue.remove();
	    			for (Event e:eventSet){
	    				if (e instanceof VMDeathEvent) {
	    					for (JdiEventListener l:listeners){
	    						l.vmDeath((VMDeathEvent)e);
	    					}
	    				} else if (e instanceof VMDisconnectEvent) {
	    					for(JdiEventListener l:listeners){
	    						l.vmDisconnect((VMDisconnectEvent)e);
	    					}
	    				} 
	    			}
	    			eventSet.resume(); // Resume the VM
	    		} catch (InterruptedException exc) {
	    			//ignore. TODO: log.
	    		}
	    	}
	    }//handleDisconnectedException
	    
		public void setAutoResume(boolean autoResume) {
			this.autoResume = autoResume;
		}
		public boolean isAutoResume() {
			return autoResume;
		}
		public void setAutoResumeStep(boolean autoResumeStep) {
			this.autoResumeStep = autoResumeStep;
		}
		public boolean isAutoResumeStep() {
			return autoResumeStep;
		}
}
