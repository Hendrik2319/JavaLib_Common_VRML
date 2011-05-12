package net.schwarzbaer.java.lib.gui;

import java.awt.Component;
import java.awt.HeadlessException;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

public class FileChooser extends JFileChooser {
	private static final long serialVersionUID = 692398561522315453L;
	
	private String fileTypeExt;
	private boolean addCorrectExt = true;
	private boolean disableOverwriteWaring = false;
	private boolean disableFileNotExistsWaring = false;
	private File selectedFile = null;
	
	public FileChooser(String fileTypeName, String fileTypeExt) {
		super("./");
		this.fileTypeExt = fileTypeExt;
		selectedFile = null;
		setFileFilter( new FileNameExtensionFilter(fileTypeName+" (*."+this.fileTypeExt+")",this.fileTypeExt));
		setFileSelectionMode(JFileChooser.FILES_ONLY);
		setMultiSelectionEnabled(false);
	}
	public void setAddCorrectExt          (boolean b) { addCorrectExt             =b; };
	public void disableOverwriteWaring    (boolean b) { disableOverwriteWaring    =b; };
	public void disableFileNotExistsWaring(boolean b) { disableFileNotExistsWaring=b; };

	@Override
	public int showOpenDialog(Component parent) throws HeadlessException {
		int result = super.showOpenDialog(parent);
		selectedFile = super.getSelectedFile();
		if (result==APPROVE_OPTION && selectedFile!=null) {
			if (!selectedFile.isFile() && !disableFileNotExistsWaring) {
				String title = "Selected File not exists";
				String message = "Selected file does not exist.\r\nDo you really want to open it?";
				if (confirm(parent, title, message)) return APPROVE_OPTION;
				return CANCEL_OPTION;
			}
		}
		return result;
	}

	@Override
	public int showSaveDialog(Component parent) throws HeadlessException {
		int result = super.showSaveDialog(parent);
		selectedFile = super.getSelectedFile();
		if (result==APPROVE_OPTION && selectedFile!=null) {
			
			if (addCorrectExt && !hasCorrectExt(selectedFile.getName()))
				selectedFile = new File( selectedFile.getParentFile(), addCorrectExt(selectedFile.getName()) );
			
			if (selectedFile.exists() && !disableOverwriteWaring) {
				if (selectedFile.isDirectory()) {
					String title = "Selected File is a Folder";
					String message = "Selected file is a folder.\r\nYou can't open it for writing.";
					JOptionPane.showMessageDialog(parent, message, title, JOptionPane.ERROR_MESSAGE);
				} else {
					String title = "Selected File already exists";
					String message = "Selected file already exists.\r\nDo you really want to overwrite it?";
					if (confirm(parent, title, message)) return APPROVE_OPTION;
				}
				return CANCEL_OPTION;
			}
		}
		return result;
	}
	
	@Override
	public void setSelectedFile(File file) {
		selectedFile = file;
		super.setSelectedFile(file);
	}
	@Override
	public File getSelectedFile() {
		return selectedFile;
	}
	
	private boolean confirm(Component parent, String title, String message) {
		return JOptionPane.showConfirmDialog(parent, message, title, JOptionPane.YES_NO_CANCEL_OPTION)==JOptionPane.YES_OPTION;
	}
	
	public void suggestFileName(String fileName) {
		if (addCorrectExt && !hasCorrectExt(fileName))
			fileName = addCorrectExt(fileName);
		setSelectedFile(new File(getCurrentDirectory(),fileName));
	}
	private boolean hasCorrectExt(String fileName) {
		return fileName.endsWith("."+fileTypeExt);
	}
	private String addCorrectExt(String fileName) {
		return fileName+"."+fileTypeExt;
	}
	
}
