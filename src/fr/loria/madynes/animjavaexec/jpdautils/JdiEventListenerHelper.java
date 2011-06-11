package fr.loria.madynes.animjavaexec.jpdautils;

import com.sun.jdi.VirtualMachine;
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

public abstract class JdiEventListenerHelper implements JdiEventListener {
	private JdiEventDispatcherThread dt;

	private void setDt(JdiEventDispatcherThread dt) {
		this.dt = dt;
	}

	public JdiEventDispatcherThread getDt() {
		return dt;
	}

	public JdiEventListenerHelper(JdiEventDispatcherThread dt){
		setDt(dt);
	}
	@Override
	public void accessWatchpoint(AccessWatchpointEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void breakpoint(BreakpointEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void classPrepare(ClassPrepareEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void classUnload(ClassUnloadEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void exception(ExceptionEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void methodEntry(MethodEntryEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void methodExit(MethodExitEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void modificationWatchpoint(ModificationWatchpointEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitorContendedEnter(MonitorContendedEnterEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitorContendedEntered(MonitorContendedEnteredEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitorWait(MonitorWaitEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void monitorWaited(MonitorWaitedEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void step(StepEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void threadDeath(ThreadDeathEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void threadStart(ThreadStartEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vmDeath(VMDeathEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vmDisconnect(VMDisconnectEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void vmStart(VMStartEvent e) {
		// TODO Auto-generated method stub

	}
	
}
