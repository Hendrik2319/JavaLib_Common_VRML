package net.schwarzbaer.java.lib.gui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;

public class MultiStepProgressDialog extends StandardDialog {
	private static final long serialVersionUID = 7276275006093575333L;
	
	private final int minWidth;
	private final String monitorObj;
	private final JPanel contentPane;
	private final ExecutorService singleThreadExecutor;
	private final Vector<Task> tasks;
	private final Vector<CancelListener> cancelListeners;
	private boolean canceled;
	private boolean wasOpened;
	private Future<?> runningThread;
	private CancelListener currentThreadCancelListener;
	private boolean restartRequired;
	private boolean haveNoSkippableTask;
	private JButton btnCancel;
	private JButton btnOk;

	public MultiStepProgressDialog(Window parent, String title, int minWidth) {
		super(parent,title);
		this.minWidth = minWidth;
		tasks = new Vector<>();
		cancelListeners = new Vector<>();
		canceled = false;
		wasOpened = false;
		restartRequired = false;
		haveNoSkippableTask = true;
		monitorObj = "";
		singleThreadExecutor = Executors.newSingleThreadExecutor();
		runningThread = null;
		btnOk = null;
		btnCancel = null;
		
		addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent e) {
				wasOpened = true;
				synchronized (monitorObj) { monitorObj.notifyAll(); }
			}
			@Override public void windowClosed (WindowEvent e) { if (isRunning()) cancel(); }
			@Override public void windowClosing(WindowEvent e) { if (isRunning()) cancel(); }
		});
		
		contentPane = new JPanel(new GridBagLayout());
		
	}

	protected void finishGUI() {
		if (haveNoSkippableTask)
			createGUI(
				contentPane,
				btnCancel = createButton("Cancel", true, e->{ cancel(); })
			);
		else
			createGUI(
				contentPane,
				btnOk     = createButton("Ok"    , true , e->{           closeDialog(); }),
				btnCancel = createButton("Cancel", false, e->{ cancel(); closeDialog(); })
			);
	}
	
	private synchronized boolean isRunning() {
		return runningThread!=null && !runningThread.isDone();
	}

	private synchronized void restartThread() {
		if (runningThread!=null) {
			if (!runningThread.isDone()) {
				restartRequired = true;
				return;
			}
			if (currentThreadCancelListener!=null)
				removeCancelListener(currentThreadCancelListener);
		}
		
		if (btnOk    !=null) btnOk    .setEnabled(false);
		if (btnCancel!=null) btnCancel.setEnabled(true);
		runningThread = singleThreadExecutor.submit(()->{
			waitUntilDialogIsVisible();
			boolean isFirstRun = true, allDone = true;
			while (isFirstRun || restartRequired) {
				isFirstRun = false;
				restartRequired = false;
				allDone = true;
				for (Task task : tasks) {
					if (task.wasExec()) continue;
					if (task.canExec) task.run();
					else allDone = false;
				}
			}
			if (allDone) SwingUtilities.invokeLater(this::closeDialog);
			else SwingUtilities.invokeLater(()->{
				if (btnOk    !=null) btnOk    .setEnabled(true);
				if (btnCancel!=null) btnCancel.setEnabled(false);
			});
		});
		currentThreadCancelListener = () -> runningThread.cancel(true);
		addCancelListener(currentThreadCancelListener);
	}
	
	public void start() {
		restartThread();
		showDialog();
		
		/*
		Thread thread = new Thread(()->{
			waitUntilDialogIsVisible();
			for (Runnable task : tasks) {
				if ()
				task.run();
			}
			SwingUtilities.invokeLater(this::closeDialog);
		});
		addCancelListener(thread::interrupt);
		thread.start();
		showDialog();
		*/
	}
	
	private void waitUntilDialogIsVisible() {
		while(!wasOpened)
			try { synchronized (monitorObj) { monitorObj.wait(); } }
			catch (InterruptedException e) {}
	}
	
	private synchronized void cancel() {
		if (!canceled) for (CancelListener cl:cancelListeners) cl.cancelTask();
		canceled = true;
	}

	public synchronized boolean wasCanceled() {
		return canceled;
	}

	public synchronized void addCancelListener(CancelListener cancelListener) {
		cancelListeners.add(cancelListener);
	}

	public synchronized void removeCancelListener(CancelListener cancelListener) {
		cancelListeners.remove(cancelListener);
	}

	public static interface CancelListener {
		public void cancelTask();
	}

	private static JButton createButton(String text, boolean enabled, ActionListener al) {
		JButton comp = new JButton(text);
		comp.setEnabled(enabled);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}

	private static JToggleButton createToggleButton(String text, boolean enabled, boolean selected, ButtonGroup bg, ActionListener al) {
		JToggleButton comp = new JToggleButton(text, selected);
		comp.setEnabled(enabled);
		if (al!=null) comp.addActionListener(al);
		if (bg!=null) bg.add(comp);
		return comp;
	}

	protected void addTask(String title, Function<ProgressView,Boolean> task) {
		addTask(title, false, true, task);
	}

	protected void addTask(String title, boolean isSkippable, boolean runByDefault, Function<ProgressView,Boolean> task) {
		if (isSkippable) haveNoSkippableTask = false;
		tasks.add(new Task(this, title, isSkippable, runByDefault, task));
	}

	private static class Task {
		
		private boolean canExec;
		private boolean wasExec;
		
		private final JToggleButton btn1;
		private final JToggleButton btn2;
		private final ProgressDialog.ProgressPanel progressPanel;
		private final Function<ProgressView, Boolean> task;
		private final MultiStepProgressDialog dlg;
		//private final String title;
	
		Task(MultiStepProgressDialog dlg, String title, boolean isSkippable, boolean runByDefault, Function<ProgressView,Boolean> task) {
			this.dlg = dlg;
			//this.title = title;
			this.task = task;
			this.canExec = !isSkippable || runByDefault;
			this.wasExec = false;
			
			GridBagConstraints c = new GridBagConstraints();
			c.fill = GridBagConstraints.BOTH;
			
			JLabel label = new JLabel(title+": ");
			label.setVerticalAlignment(JLabel.TOP);
			label.setHorizontalAlignment(JLabel.LEFT);
			
			progressPanel = new ProgressDialog.ProgressPanel(ProgressView.ProgressDisplay.None, false, false);
			//progressPanel.setDebugID(title);
			progressPanel.setTaskTitle("Waiting ...");
			progressPanel.setValue(0, 100);
			if (this.dlg.minWidth>0) progressPanel.setProgressBarMinWidth(this.dlg.minWidth);
			
			c.weighty = 1;
			c.weightx = 0;
			c.gridwidth = 1;
			this.dlg.contentPane.add(label,c);
			
			c.weightx = 1;
			c.gridwidth = isSkippable ? 1 : GridBagConstraints.REMAINDER;
			this.dlg.contentPane.add(progressPanel,c);
			
			if (isSkippable) {
				ButtonGroup bg = new ButtonGroup();
				c.weightx = 0;
				c.gridwidth = 1;
				this.dlg.contentPane.add(btn1 = createToggleButton("Run" , true,  runByDefault, bg, e->{ setCanExec(true ); this.dlg.restartThread(); }),c);
				c.gridwidth = GridBagConstraints.REMAINDER;
				this.dlg.contentPane.add(btn2 = createToggleButton("Skip", true, !runByDefault, bg, e->{ setCanExec(false); this.dlg.restartThread(); }),c);
			} else {
				btn1 = null;
				btn2 = null;
			}
		}
	
		private synchronized void setCanExec(boolean canExec) {
			if (wasExec) return;
			this.canExec = canExec;
		}
		
		public synchronized boolean wasExec() { return wasExec; }
	
		public synchronized void run() {
			if (btn1 != null) btn1.setEnabled(false);
			if (btn2 != null) btn2.setEnabled(false);
			if (dlg.wasCanceled()) {
				SwingUtilities.invokeLater(()->{
					progressPanel.setTaskTitle("Canceled");
					progressPanel.setValue(0, 100);
				});
			} else {
				//System.out.printf("Task[%s] started%n", title);
				boolean successful = task.apply(progressPanel);
				//System.out.printf("Task[%s] finished%n", title);
				//long now = System.currentTimeMillis();
				SwingUtilities.invokeLater(()->{
					//System.out.printf("Task[%s] SwingUtilities.invokeLater( \"Task Finished\" ):  submitted:%016X,  executed:%016X%n", title, now, System.currentTimeMillis());
					progressPanel.setTaskTitle(successful ? "Finished" : "Aborted");
					progressPanel.setValue(successful ? 100 : 0, 100);
				});
			}
			wasExec = true;
		}
	
	}
}

