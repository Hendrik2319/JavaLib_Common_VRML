package net.schwarzbaer.java.lib.gui;

import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

import javax.swing.JButton;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class TextAreaDialog extends StandardDialog {
	private static final long serialVersionUID = 1135036865387978283L;
	
	public interface AddToContextMenu {
		void addToContextMenu(JPopupMenu contextMenu, JTextArea textArea);
	}
	
	private final JTextArea textArea;
	private boolean wasCanceled;

	private TextAreaDialog(
			Window parent, String title, ModalityType modality, boolean repeatedUseOfDialogObject,
			int width, int height, boolean textEditable, boolean asEditor, boolean textAreaLineWrap,
			AddToContextMenu addToContextMenu) {
		super(parent, title, modality, repeatedUseOfDialogObject);
		wasCanceled = true;
		
		JPopupMenu textViewContextMenu = new JPopupMenu();
		textArea = new JTextArea();
		textArea.setEditable(textEditable);
		textArea.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3)
					textViewContextMenu.show(textArea, e.getX(), e.getY());
			}
		});
		
		textArea.setLineWrap(textAreaLineWrap);
		textArea.setWrapStyleWord(textAreaLineWrap);
		textViewContextMenu.add(createCheckBoxMenuItem("Line Wrap", textAreaLineWrap, isChecked->{
			textArea.setLineWrap(isChecked);
			textArea.setWrapStyleWord(isChecked);
		}) );
		
		if (addToContextMenu!=null)
			addToContextMenu.addToContextMenu(textViewContextMenu, textArea);
		
		JScrollPane textAreaScrollPane = new JScrollPane(textArea);
		textAreaScrollPane.setPreferredSize(new Dimension(width,height));
		
		if (asEditor)
			createGUI(textAreaScrollPane, createButton("Ok",e->{ wasCanceled = false; closeDialog(); }), createButton("Cancel",e->closeDialog()));
		else
			createGUI(textAreaScrollPane, createButton("Close",e->closeDialog()));
	}
	
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text) {
		showText(parent, title, width, height, textAreaLineWrap, false, text);
	}
	
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, boolean nonBlocking, String text) {
		showText(parent, title, width, height, textAreaLineWrap, nonBlocking, text, null);
	}
	
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text, AddToContextMenu addToContextMenu) {
		showText(parent, title, width, height, textAreaLineWrap, false, text, addToContextMenu);
	}
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, boolean nonBlocking, String text, AddToContextMenu addToContextMenu) {
		showText(parent, title, width, height, textAreaLineWrap, nonBlocking, text, addToContextMenu, null);
	}
	public static void showText(Window parent, String title, int width, int height, boolean textAreaLineWrap, boolean nonBlocking, String text, AddToContextMenu addToContextMenu, Consumer<TextAreaDialog> modifyDialog) {
		ModalityType modalityType = nonBlocking ? ModalityType.MODELESS : ModalityType.APPLICATION_MODAL;
		TextAreaDialog dlg = new TextAreaDialog(parent, title, modalityType, false, width, height, false, false, textAreaLineWrap, addToContextMenu);
		dlg.setText(text);
		if (modifyDialog!=null) modifyDialog.accept(dlg);
		dlg.showDialog();
	}
	
	public static String editText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text) {
		return editText(parent, title, width, height, textAreaLineWrap, text, null);
	}
	public static String editText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text, AddToContextMenu addToContextMenu) {
		return editText(parent, title, width, height, textAreaLineWrap, text, addToContextMenu, null);
	}
	public static String editText(Window parent, String title, int width, int height, boolean textAreaLineWrap, String text, AddToContextMenu addToContextMenu, Consumer<TextAreaDialog> modifyDialog) {
		TextAreaDialog dlg = new TextAreaDialog(parent, title, ModalityType.APPLICATION_MODAL, false, width, height, true, true, textAreaLineWrap, addToContextMenu);
		dlg.setText(text);
		dlg.wasCanceled = true;
		if (modifyDialog!=null) modifyDialog.accept(dlg);
		dlg.showDialog();
		if (dlg.wasCanceled) return null;
		return dlg.getText();
	}

	private void setText(String text) {
		textArea.setText(text);
	}

	private String getText() {
		return textArea.getText();
	}

	private static JButton createButton(String title, ActionListener al) {
		JButton comp = new JButton(title);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}

	private static JCheckBoxMenuItem createCheckBoxMenuItem(String title, boolean isChecked, Consumer<Boolean> setValue) {
		JCheckBoxMenuItem comp = new JCheckBoxMenuItem(title,isChecked);
		if (setValue!=null) comp.addActionListener(e->setValue.accept(comp.isSelected()));
		return comp;
	}
}
