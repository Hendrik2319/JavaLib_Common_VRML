package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

public class ProgressDialog extends StandardDialog implements ProgressView {
	private static final long serialVersionUID = 1401683964054921965L;

	public static void runWithProgressDialog(Window parent, String title, int minWidth, Consumer<ProgressDialog> useProgressDialog) {
		runWithProgressDialog(parent, title, minWidth, false, useProgressDialog);
	}
	public static void runWithProgressDialog(Window parent, String title, int minWidth, boolean allowSwitchToBackground, Consumer<ProgressDialog> useProgressDialog) {
		new ProgressDialogWrapper<Object>()
				.runWithProgressDialog(parent, title, minWidth, allowSwitchToBackground, pd->{ useProgressDialog.accept(pd); return null; });
	}
	public static <ReturnValue> ReturnValue runWithProgressDialogRV(Window parent, String title, int minWidth, Function<ProgressDialog,ReturnValue> useProgressDialog) {
		return runWithProgressDialogRV(parent, title, minWidth, false, useProgressDialog);
	}
	public static <ReturnValue> ReturnValue runWithProgressDialogRV(Window parent, String title, int minWidth, boolean allowSwitchToBackground, Function<ProgressDialog,ReturnValue> useProgressDialog) {
		return new ProgressDialogWrapper<ReturnValue>()
				.runWithProgressDialog(parent, title, minWidth, allowSwitchToBackground, useProgressDialog);
	}
	
	private static class ProgressDialogWrapper<ReturnValue> {
		
		private ReturnValue result;

		ReturnValue runWithProgressDialog(Window parent, String title, int minWidth, boolean allowSwitchToBackground, Function<ProgressDialog,ReturnValue> useProgressDialog) {
			result = null;
			ProgressDialog pd = new ProgressDialog(parent,title,minWidth,allowSwitchToBackground);
			Thread thread = new Thread(()->{
				pd.waitUntilDialogIsVisible();
				result = useProgressDialog.apply(pd);
				SwingUtilities.invokeLater(pd::closeDialog);
			});
			pd.addCancelListener(thread::interrupt);
			thread.start();
			pd.showDialog();
			return result;
		}
	}
	
	private final Vector<CancelListener> cancelListeners;
	private boolean canceled;
	private boolean wasOpened;
	private final String monitorObj;
	private ProgressPanel progressPanel;
	
	public ProgressDialog(Window parent, String title, int minWidth, ModalityType modality, boolean allowSwitchToBackground) {
		super(parent, title, modality);
		this.cancelListeners = new Vector<>();
		this.canceled = false;
		this.wasOpened = false;
		this.monitorObj = "";
		
		addWindowListener(new WindowAdapter() {
			@Override public void windowOpened(WindowEvent e) {
				wasOpened = true;
				synchronized (monitorObj) {
					monitorObj.notifyAll();
				}
			}
			@Override public void windowClosed (WindowEvent e) { cancel(); }
			@Override public void windowClosing(WindowEvent e) { cancel(); }
		});
		
		progressPanel = new ProgressPanel();
		
		JPanel southPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT,5,5));
		if (allowSwitchToBackground)
			southPanel.add(createButton("Switch to Background", (e,btn)->{ setVisible(false); setModalityType(Dialog.ModalityType.MODELESS); btn.setEnabled(false); setVisible(true); }));
		southPanel.add(createButton("Cancel", e->{ cancel(); closeDialog(); }));
		
		JPanel contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPane.add(progressPanel);
		contentPane.add(southPanel,BorderLayout.SOUTH);
		
		progressPanel.progressbar.setIndeterminate(true);
		if (minWidth>0) progressPanel.setProgressBarMinWidth(minWidth);
		
		super.createGUI( contentPane );
		super.setSizeAsMinSize();
	}

	public ProgressDialog(Window parent, String title, int minWidth, ModalityType modality) {
		this(parent, title, minWidth, modality, false);
	}
	public ProgressDialog(Window parent, String title, ModalityType modality) {
		this(parent, title, -1, modality);
	}

	public ProgressDialog(Window parent, String title, int minWidth, boolean allowSwitchToBackground) {
		this(parent, title, minWidth, Dialog.ModalityType.APPLICATION_MODAL, allowSwitchToBackground);
	}
	public ProgressDialog(Window parent, String title, int minWidth) {
		this(parent, title, minWidth, Dialog.ModalityType.APPLICATION_MODAL);
	}

	public ProgressDialog(Window parent, String title) {
		this(parent, title, -1, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	@Override public void displayProgressString(ProgressDisplay progressDisplay) {
		progressPanel.displayProgressString(progressDisplay);
	}
	@Override public void setTaskTitle(String str) {
		progressPanel.setTaskTitle(str);
	}
	@Override public void setIndeterminate(boolean isIndeterminate) {
		progressPanel.setIndeterminate(isIndeterminate);
	}
	@Override public void setValue(int min, int value, int max) {
		progressPanel.setValue(min, value, max);
	}
	@Override public void setValue(int value) {
		progressPanel.setValue(value);
	}
	
	private static JButton createButton(String title, ActionListener al) {
		JButton comp = new JButton(title);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}
	private static JButton createButton(String title, BiConsumer<ActionEvent,JButton> al) {
		JButton comp = new JButton(title);
		if (al!=null) comp.addActionListener(e->al.accept(e,comp));
		return comp;
	}

	//	@Override
//	public void showDialog(final Position position) {
//		new Thread(new Runnable() {
//			@Override public void run() {
//				ProgressDialog.super.showDialog(position);
//			}
//		}).start();
//	}
//
//	@Override
//	public void showDialog() {
//		new Thread(new Runnable() {
//			@Override public void run() {
//				ProgressDialog.super.showDialog();
//			}
//		}).start();
//	}
	
	public void waitUntilDialogIsVisible() {
		if (wasOpened) return;
		while(!wasOpened)
			try { synchronized (monitorObj) { monitorObj.wait(); } }
			catch (InterruptedException e) {}
	}

//	@Override
//	public void closeDialog() {
//		super.closeDialog();
////		dispose();
//	}

	private void cancel() {
		if (!canceled) for (CancelListener cl:cancelListeners) cl.cancelTask();
		canceled = true;
	}

	public boolean wasCanceled() {
		return canceled;
	}

	public static interface CancelListener {
		public void cancelTask();
	}

	public void addCancelListener(CancelListener cancelListener) {
		cancelListeners.add(cancelListener);
	}

	public static class ProgressPanel extends JPanel implements ProgressView {
		private static final long serialVersionUID = -1297486337641452128L;
		
		private final JLabel taskTitle;
		private final JProgressBar progressbar;
		private ProgressDisplay progressDisplay;
		private String debugID;
		
		public ProgressPanel() {
			this(ProgressDisplay.None, true, false);
		}
		public ProgressPanel(ProgressDisplay progressDisplay, boolean withEmptyBorder, boolean titleBelowProgressBar) {
			super(new BorderLayout(3,3));
			debugID = null;
			
			if (withEmptyBorder) setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			add(taskTitle = new JLabel("  "), titleBelowProgressBar ? BorderLayout.SOUTH : BorderLayout.NORTH);
			add(progressbar = new JProgressBar(JProgressBar.HORIZONTAL), BorderLayout.CENTER);
			
			displayProgressString(progressDisplay);
		}
		
		public void setProgressBarMinWidth(int minWidth) {
			progressbar.setPreferredSize(new Dimension(minWidth,14));
		}

		@Override
		public void displayProgressString(ProgressDisplay progressDisplay) {
			this.progressDisplay = progressDisplay;
			progressbar.setStringPainted(this.progressDisplay!=ProgressDisplay.None);
			progressbar.setString(this.progressDisplay==ProgressDisplay.None?null:"");
		}
		
		public void setDebugID(String debugID) {
			this.debugID = debugID;
		}
	
		@Override
		public void setTaskTitle(String str) {
			taskTitle.setText(str);
			if (debugID!=null) System.out.printf("ProgressPanel[%s].setTaskTitle( \"%s\" )%n", debugID, str);
		}
	
		@Override
		public void setIndeterminate(boolean isIndeterminate) {
			progressbar.setIndeterminate(isIndeterminate);
			if (debugID!=null) System.out.printf("ProgressPanel[%s].setIndeterminate( %s )%n", debugID, isIndeterminate);
		}
	
		@Override
		public void setValue(int min, int value, int max) {
			progressbar.setMinimum(min);
			progressbar.setValue(value);
			progressbar.setMaximum(max);
			if (progressbar.isIndeterminate())
				progressbar.setIndeterminate(false);
			displayProgressString();
			if (debugID!=null) System.out.printf("ProgressPanel[%s].setValue( %d, %d, %d )%n", debugID, min, value, max);
		}
		
		@Override
		public void setValue(int value) {
			if (progressbar.isIndeterminate())
				throw new IllegalStateException("Can't set value of progress without setting min and max.");
			progressbar.setValue(value);
			displayProgressString();
		}
	
		private void displayProgressString() {
			if (progressDisplay==ProgressDisplay.None) return;
			int minimum = progressbar.getMinimum();
			int maximum = progressbar.getMaximum()-minimum;
			int value   = progressbar.getValue()-minimum;
			switch (progressDisplay) {
			case None      : break;
			case Number    : progressbar.setString(String.format("%d / %d", value, maximum)); break;
			case Percentage: progressbar.setString(String.format("%1.2f%%", value/(float)maximum)); break;
			}
		}
	}
}
