package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

public class HexView implements ActionListener{

	public static void showAsWindow(String title, String descriptionTitle, String[] description, String contentTitle, String content) {
		new HexView(content).createWindow(title,descriptionTitle, description, contentTitle);
		
	}

	public static JPanel showAsPanel(String descriptionTitle, String[] description, String contentTitle, String content) {
		return new HexView(content).createPanel(descriptionTitle, description, contentTitle);
	}

	private final String content;
	private JTextArea hexArea;

	private HexView(String content) {
		this.content = content;
	}

	private void createWindow(String title, String descriptionTitle, String[] description, String contentTitle) {
		JPanel contentPane = createPanel(descriptionTitle, description, contentTitle);
		StandardMainWindow hauptfenster = new StandardMainWindow(title,StandardMainWindow.DefaultCloseOperation.DISPOSE_ON_CLOSE);
		hauptfenster.startGUI( contentPane );
		hauptfenster.limitSizeToFractionOfScreenSize(0.95f);
	}

	private JPanel createPanel(String descriptionTitle, String[] description, String contentTitle) {
		
		JPanel descriptionPanel = new JPanel( new GridLayout(0,1,3,3) );
		for (int i=0; i<description.length; i++)
			descriptionPanel.add(new JLabel(description[i]));
		descriptionPanel.setBorder(BorderFactory.createTitledBorder(descriptionTitle));

		int rowLength = 32;
		
		hexArea = new JTextArea();
		hexArea.setEditable(false);
		hexArea.setText(makeHex(content,rowLength));
		
		ButtonGroup formatSelectorButtonGroup = new ButtonGroup();
		JPanel formatSelector = new JPanel( new GridLayout(1,0,3,3) );
		formatSelector.add( GUI.createRadioButton("Hex (8 per row)",  "hex8",  this, formatSelectorButtonGroup, rowLength== 8, true) );
		formatSelector.add( GUI.createRadioButton("Hex (16 per row)", "hex16", this, formatSelectorButtonGroup, rowLength==16, true) );
		formatSelector.add( GUI.createRadioButton("Hex (24 per row)", "hex24", this, formatSelectorButtonGroup, rowLength==24, true) );
		formatSelector.add( GUI.createRadioButton("Hex (32 per row)", "hex32", this, formatSelectorButtonGroup, rowLength==32, true) );
		formatSelector.add( GUI.createRadioButton("ASCII",            "ascii", this, formatSelectorButtonGroup, false, true) );
		
		JPanel hexAreaPanel = new JPanel( new BorderLayout(3,3) );
		hexAreaPanel.add(new JScrollPane( hexArea ),BorderLayout.CENTER);
		hexAreaPanel.add(GUI.createLeftAlignedPanel(new JLabel("Show as:"), formatSelector, 3),BorderLayout.SOUTH);
		hexAreaPanel.setBorder(BorderFactory.createTitledBorder(contentTitle));
		
		JPanel contentPanel = new JPanel( new BorderLayout(3,3) );
		contentPanel.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPanel.add(descriptionPanel,BorderLayout.NORTH);
		contentPanel.add(hexAreaPanel,BorderLayout.CENTER);
		return contentPanel;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if ("hex8" .equals(e.getActionCommand())) hexArea.setText(makeHex(content, 8));
		if ("hex16".equals(e.getActionCommand())) hexArea.setText(makeHex(content,16));
		if ("hex24".equals(e.getActionCommand())) hexArea.setText(makeHex(content,24));
		if ("hex32".equals(e.getActionCommand())) hexArea.setText(makeHex(content,32));
		if ("ascii".equals(e.getActionCommand())) hexArea.setText(content);
		
	}

	public static String makeHex(String content, int rowLength) {
		StringBuilder sb = new StringBuilder();
		long nol = Math.round(Math.ceil(content.length()/(double)(rowLength)));
		long rowNumberLength = Math.max( 4, Math.round(Math.ceil(Math.log(content.length())/Math.log(16))) );
		String rowNumberFormat = "%0"+rowNumberLength+"X";
		for (int i=0; i<nol; i++) {
			String lineContent = content.substring(i*rowLength,Math.min((i+1)*rowLength,content.length()) );
			sb.append(String.format(rowNumberFormat+":  %s  |  %s\r\n", i*rowLength, str2hex(lineContent,rowLength), str2output(lineContent,rowLength) ));
		}
		return sb.toString();
	}

	private static String str2output(String str, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<length; i++) {
			if (i>=str.length()) sb.append(' ');
			else {
				char ch = str.charAt(i);
				if (ch<' ') sb.append('?');
				else        sb.append(ch);
			}
			if (i+1<length) {
				if ( (i&7)==7 ) sb.append(" ");
			}
		}
		return sb.toString();
	}

	private static String str2hex(String str, int length) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<length; i++) {
			if (i>=str.length()) sb.append("  ");
			else sb.append(String.format("%02X", (int)str.charAt(i) ));
			if (i+1<length) {
				sb.append(" ");
				if ( (i&7)==7 ) sb.append(" ");
			}
		}
		return sb.toString();
	}

}
