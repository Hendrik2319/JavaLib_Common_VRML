package net.schwarzbaer.java.lib.gui;

import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class MultiLineStatusLog extends JPanel {
	private static final long serialVersionUID = 7951627891309363600L;
	private final JLabel[] lines;
	
	public MultiLineStatusLog() {
		this(1);
	}
	
	public MultiLineStatusLog( int numberOfLines ) {
		super( new GridLayout( 0,1, 3,3 ) );
		lines = new JLabel[numberOfLines];
		for (int i=0; i<lines.length; i++) {
			lines[i] = new JLabel(""); 
			add(lines[i]);
		}
	}
	
	public void appendToLog( String newLogLine ) {
		for (int i=0; i<lines.length-1; i++) {
			lines[i].setText( lines[i+1].getText() );
		}
		lines[ lines.length-1 ].setText( newLogLine );
	}

}
