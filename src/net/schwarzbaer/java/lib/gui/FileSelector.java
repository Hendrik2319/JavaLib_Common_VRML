package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JTextField;


public class FileSelector implements ActionListener {

	private static final boolean DEBUG = false;
	
	private final Component parent;
	private final String id;
	private final Type type;
	private final FileSelectorListener listener;
	private JFileChooser fileChooser;
	private JButton button;
	private JTextField field;
	private JComboBox<String> cmbbx;
	private ComboBoxModelWithoutRedundancy<String> cmbbxContent;
	private Color defaultFieldBackground;
	private String lastSetFileStr;
	
	public FileSelector(Component parent, String id, FileSelectorListener listener) {
		this(parent, id, Type.WITHOUT_ALTERNATIVES, listener);
	}
	
	public FileSelector(Component parent, String id, Type type, FileSelectorListener listener) {
		this.parent = parent;
		this.id = id;
		this.type = type;
		this.listener = listener;
		fileChooser = new JFileChooser(".");
		fileChooser.setMultiSelectionEnabled(false);
		button = null;
		cmbbx = null;
		cmbbxContent = null;
		field = null;
		switch(type) {
		case WITH_ALTERNATIVES   :
			cmbbxContent = new ComboBoxModelWithoutRedundancy<String>();
			cmbbx = new JComboBox<String>(cmbbxContent);
			cmbbx.setActionCommand("select field");
			cmbbx.addActionListener(this);
			cmbbx.setEditable(true);
			defaultFieldBackground = cmbbx.getBackground();
			break;
		case WITHOUT_ALTERNATIVES:
			field = GUI.createTextField("select field", this, true, null);
			defaultFieldBackground = field.getBackground();
			break;
		}
		lastSetFileStr = null;
	}
	
	public void setFileChooserDirectory(String dir) {
		File file = new File(dir);
		if (DEBUG) System.out.printf("FileSelector.setFileChooserDirectory(\"%s\")  old CurrentDirectory = \"%s\"",dir,fileChooser.getCurrentDirectory());
		if (DEBUG) System.out.printf("FileSelector.setFileChooserDirectory(\"%s\")  new CurrentDirectory = \"%s\"",dir,file);
		
		try { fileChooser.setCurrentDirectory(file); }
		catch (Exception e) {
			System.out.println("Can't change current directory of filechooser to \""+file+"\".");
		}
	}

	public void setFile(File file) {
		setFieldText(file.toString());
	}

	public void addAlternative(File file) {
		if (DEBUG) System.out.printf("FileSelector.addAlternative(\"%s\")\r\n",file);
		if (type!=Type.WITH_ALTERNATIVES) return;
		cmbbx.addItem(file.toString());
	}

	public void addAlternatives(Iterator<String> iterator) {
		if (DEBUG) System.out.printf("FileSelector.addAlternatives([%s])\r\n",iterator);
		if (type!=Type.WITH_ALTERNATIVES) return;
		cmbbx.removeAllItems();
		if (iterator!=null)
			while(iterator.hasNext())
				cmbbx.addItem(iterator.next());
		}

	public Iterator<String> getAlternatives() {
		if (type!=Type.WITH_ALTERNATIVES) return null;
		return cmbbxContent.getItemIterator();
	}

	public void setEnabled(boolean b) {
		button.setEnabled(b);
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx .setEnabled(b); break;
		case WITHOUT_ALTERNATIVES: field .setEnabled(b); break;
		}
	}

	public Component getInputField() {
		switch(type) {
		case WITH_ALTERNATIVES   : return cmbbx;
		case WITHOUT_ALTERNATIVES: return field;
		}
		return null;
	}

	public Component getSelectButton() {
		return button;
	}

	public Component createSelectButton(String title) {
		button = GUI.createButton(title, "select button", this, GUI.getFileIcon(new File(".")));
		return button;
	}

	public Component createCombinedPanel(String buttonLabel) {
		return GUI.createLeftAlignedPanel(createSelectButton(buttonLabel), getInputField());
	}

	public void setDirSelectionOnly   () { fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY); }
	public void setFileSelectionOnly  () { fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY); }
	public void setFileAndDirSelection() { fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES); }

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("select button")) {
			if (fileChooser.showOpenDialog(parent) == JFileChooser.APPROVE_OPTION) {
				File selectedFile = fileChooser.getSelectedFile();
				if (DEBUG) System.out.printf("FileSelector.actionPerformed(\"%s\") selectedFile:\"%s\"\r\n",e.getActionCommand(),selectedFile);
				if (checkDir(selectedFile)) {
					listener.fileSelectionChanged(this, id, selectedFile);
					lastSetFileStr = selectedFile.toString();
					setFieldText(lastSetFileStr);
				}
			}
			return;
		}
		if (e.getActionCommand().equals("select field")) {
			String newFileStr = getFieldText();
			if (newFileStr.equals(lastSetFileStr)) {
				lastSetFileStr = null;
				return;
			}
			lastSetFileStr = null;
			File selectedFile = new File(newFileStr);
			if (DEBUG) System.out.printf("FileSelector.actionPerformed(\"%s\") selectedFile:\"%s\"\r\n",e.getActionCommand(),selectedFile);
			if (checkDir(selectedFile) && listener.isFileANewChoice(this, id, selectedFile)) {
				listener.fileSelectionChanged(this, id, selectedFile);
				fileChooser.setSelectedFile(selectedFile);
			}
			return;
		}
	}

	private String getFieldText() {
		switch(type) {
		case WITH_ALTERNATIVES   : return cmbbx.getSelectedItem().toString();
		case WITHOUT_ALTERNATIVES: return field.getText();
		}
		return null;
	}
	
	private void setFieldText(String txt) {
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx.setSelectedItem(txt); break;
		case WITHOUT_ALTERNATIVES: field.setText(txt); break;
		}
	}
	
	private boolean checkDir(File selectedFile) {
		
		if (!listener.isFileOK(this, id, selectedFile)) {
			switch(type) {
			case WITH_ALTERNATIVES   : cmbbx.setBackground(Color.RED); break;
			case WITHOUT_ALTERNATIVES: field.setBackground(Color.RED); break;
			}
			return false;
		}
		
		switch(type) {
		case WITH_ALTERNATIVES   : cmbbx.setBackground(defaultFieldBackground); break;
		case WITHOUT_ALTERNATIVES: field.setBackground(defaultFieldBackground); break;
		}
		return true;
	}
	
	public static enum Type {
		WITH_ALTERNATIVES,
		WITHOUT_ALTERNATIVES
	}
	
	public static interface FileSelectorListener {
		public void    fileSelectionChanged(FileSelector source, String id, File file);
		public boolean isFileOK            (FileSelector source, String id, File file);
		public boolean isFileANewChoice    (FileSelector source, String id, File file);
	}
}