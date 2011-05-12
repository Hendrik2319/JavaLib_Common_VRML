package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.schwarzbaer.java.lib.gui.StandardDialog.Position;

public abstract class AbstractWizard implements ActionListener {
	
	private StandardDialog dialog;
	private JLabel description;
	private JButton btnPrev;
	private JButton btnNext;
	private int currentSlideIndex;
	private JPanel contentPane;
	private final String btnPrevName;
	private final String btnNextName;
	private final String btnCancelName;
	private final String btnOKName;
	public Result result;
	
	public AbstractWizard(String btnPrevName, String btnNextName, String btnCancelName, String btnOKName) {
		this.btnPrevName = btnPrevName;
		this.btnNextName = btnNextName;
		this.btnCancelName = btnCancelName;
		this.btnOKName = btnOKName;
		result = Result.CREATED;
	}
	
	public void createGUI(Window parent, String title, ModalityType modality, Position position, int width, int height) {
		
		currentSlideIndex = 0;
		
		description = new JLabel(getSlideDescription(currentSlideIndex));
		
		JPanel buttonPanel = new JPanel( new GridLayout(1,0,3,3) );
		buttonPanel.add(btnPrev = GUI.createButton(btnPrevName,   "btnPrev", this, false));
		buttonPanel.add(btnNext = GUI.createButton(btnNextName,   "btnNext", this, false));
		buttonPanel.add(          GUI.createButton(btnCancelName, "btnCancel", this, true));
		
		contentPane = new JPanel( new BorderLayout(3,3) );
		contentPane.add(description,BorderLayout.NORTH);
		contentPane.add(GUI.createRightAlignedPanel(buttonPanel),BorderLayout.SOUTH);
		contentPane.add(getSlide(currentSlideIndex),BorderLayout.CENTER);
		
		dialog = new StandardDialog(parent, title, modality);
		dialog.createGUI(contentPane, position);
		dialog.setMinimumSize(new Dimension(width,height));
	}
	
	public void showDialog() {
		result = Result.EDITING;
		dialog.showDialog();
	}
	
	@Override
	public void actionPerformed(ActionEvent e) {
		if ("btnPrev".equals(e.getActionCommand())) {
			if (currentSlideIndex<=0) return;
			currentSlideIndex--;
			changeSlide();
			return;
		}
		if ("btnNext".equals(e.getActionCommand())) {
			if (!isLastSlide(currentSlideIndex)) {
				currentSlideIndex++;
				changeSlide();
			} else {
				result = Result.OK;
				dialog.closeDialog();
			}
			return;
		}
		if ("btnCancel".equals(e.getActionCommand())) {
			result = Result.CANCEL; 
			dialog.closeDialog();
			return;
		}
		
	}

	private void changeSlide() {
		description.setText(getSlideDescription(currentSlideIndex));
		contentPane.add(getSlide(currentSlideIndex),BorderLayout.CENTER);
		btnPrev.setEnabled(currentSlideIndex>0);
		btnNext.setEnabled(btnNextEnabled(currentSlideIndex));
		if (isLastSlide(currentSlideIndex)) btnNext.setText(btnOKName);
		else                                btnNext.setText(btnNextName);
	}

	protected abstract String getSlideDescription(int slideIndex);
	protected abstract JPanel getSlide(int slideIndex);
	protected abstract boolean isLastSlide(int slideIndex);
	protected abstract boolean btnNextEnabled(int slideIndex);

	protected void enableBtnNext() {
		btnNext.setEnabled(true);
	}
	
	
	public enum Result { OK,CANCEL,EDITING,CREATED }
}
