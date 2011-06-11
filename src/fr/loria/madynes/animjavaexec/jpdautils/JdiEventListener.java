package fr.loria.madynes.animjavaexec.jpdautils;

import java.util.EventListener;

import com.sun.jdi.event.AccessWatchpointEvent;
import com.sun.jdi.event.BreakpointEvent;
import com.sun.jdi.event.ClassPrepareEvent;
import com.sun.jdi.event.ClassUnloadEvent;
import com.sun.jdi.event.ExceptionEvent;
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

public interface JdiEventListener  extends EventListener {
	void vmStart(VMStartEvent e);
	void vmDisconnect(VMDisconnectEvent e);
	void vmDeath(VMDeathEvent e);
	
	void threadStart(ThreadStartEvent e);
	void threadDeath(ThreadDeathEvent e);
	
	void classPrepare(ClassPrepareEvent e);
	void classUnload(ClassUnloadEvent e);
	
	void accessWatchpoint(AccessWatchpointEvent e);
	void modificationWatchpoint(ModificationWatchpointEvent e);
	
	void exception(ExceptionEvent e);
	
	void methodExit(MethodExitEvent e);
	void methodEntry(MethodEntryEvent e);
	
	void monitorContendedEntered(MonitorContendedEnteredEvent e);
	void monitorContendedEnter(MonitorContendedEnterEvent e);
	void monitorWait(MonitorWaitEvent e);
	void monitorWaited(MonitorWaitedEvent e);

	void breakpoint(BreakpointEvent e);
	void step(StepEvent e);
	
}
