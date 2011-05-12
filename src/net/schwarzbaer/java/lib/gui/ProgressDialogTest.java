package net.schwarzbaer.java.lib.gui;

import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import net.schwarzbaer.java.lib.gui.ProgressDialog.CancelListener;

public class ProgressDialogTest {

	public static void main(String[] args) {
		GUI.setSystemLookAndFeel();
		StandardMainWindow smw = new StandardMainWindow("Test");
		smw.startGUI(new JPanel());
		
		final ProgressDialog pd = new ProgressDialog(smw,"Test");
		
		pd.addCancelListener(new CancelListener() {
			@Override public void cancelTask() {
				pd.closeDialog();
			}
		});
		
		new Thread(new Runnable() {
			@Override public void run() {
				int max = 1000000000;
				pd.setTaskTitle("Import station adresses:");
				pd.setValue(0, max);
				for (int i=0; i<max; i++) {
					Vector<String> temp = new Vector<String>();
					temp.add("");
					temp.clear();
					pd.setValue(i+1);
				}
				pd.closeDialog();
			}
		}).start();
		SwingUtilities.invokeLater(new Runnable() {
			@Override public void run() {
				pd.showDialog();
			}
		});
	}

}
