/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

public class StandardDialog extends JDialog implements WindowListener {
	private static final long serialVersionUID = -2236026007551538954L;
	
	private Window parent;
	private boolean repeatedUseOfDialogObject; 
	
	public StandardDialog( Window parent, String title ) {
		this(parent, title, Dialog.ModalityType.APPLICATION_MODAL);
	}
	
	public StandardDialog( Window parent, String title, ModalityType modality ) {
		this(parent, title, modality, true);
	}
	
	public StandardDialog( Window parent, String title, ModalityType modality, boolean repeatedUseOfDialogObject ) {
		super( parent, title, modality );
		this.parent = parent;
		this.repeatedUseOfDialogObject = repeatedUseOfDialogObject;
		addWindowListener(this);
		setDefaultCloseOperation(repeatedUseOfDialogObject?HIDE_ON_CLOSE:DISPOSE_ON_CLOSE);
	}
    
	@Override public void windowOpened      (WindowEvent e) { /*System.out.printf("[%08X] dialogOpened     \r\n", this.hashCode());*/ }
	@Override public void windowClosed      (WindowEvent e) { /*System.out.printf("[%08X] dialogClosed     \r\n", this.hashCode());*/ }
	@Override public void windowClosing     (WindowEvent e) { /*System.out.printf("[%08X] dialogClosing    \r\n", this.hashCode());*/ }
	@Override public void windowIconified   (WindowEvent e) { /*System.out.printf("[%08X] dialogIconified  \r\n", this.hashCode());*/ }
	@Override public void windowDeiconified (WindowEvent e) { /*System.out.printf("[%08X] dialogDeiconified\r\n", this.hashCode());*/ }
	@Override public void windowActivated   (WindowEvent e) { /*System.out.printf("[%08X] dialogActivated  \r\n", this.hashCode());*/ }
	@Override public void windowDeactivated (WindowEvent e) { /*System.out.printf("[%08X] dialogDeactivated\r\n", this.hashCode());*/ }

	public void createGUI( JComponent contentPane, Component... buttons ) {
		
		JPanel buttonPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		c.weightx = 1;
		buttonPanel.add(new JLabel(),c);
		c.weightx = 0;
		for (Component btn:buttons)
			if (btn!=null) buttonPanel.add(btn,c);
		
		JPanel dlgContentPane = new JPanel(new BorderLayout(3,3));
		dlgContentPane.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
		dlgContentPane.add(contentPane,BorderLayout.CENTER);
		dlgContentPane.add(buttonPanel,BorderLayout.SOUTH);
		
    	createGUI( dlgContentPane, null, null );
	}
	public void createGUI( JComponent contentPane ) {
    	createGUI( contentPane, null, null );
	}
    
    public void createGUI( JComponent contentPane, Position position ) {
    	createGUI( contentPane, position, null );
    }
    
    public void createGUI( JComponent contentPane, Dimension preferredSize ) {
    	createGUI( contentPane, null, preferredSize );
    }
    
    public void createGUI( JComponent contentPane, Position position, Dimension preferredSize ) {
        setContentPane( contentPane );
        setPositionAndSize(position, preferredSize);
    }

    public void setPositionAndSize(Position position, Dimension preferredSize) {
		if (preferredSize!=null) setPreferredSize(preferredSize);
        pack();
        if (position==null) position = Position.PARENT_CENTER;
        setPosition(position);
	}

	private void setPosition(Position position) {
		Rectangle p;
		if (parent != null) p = parent.getBounds();
		else                p = getGraphicsConfiguration().getBounds();
        Dimension d = getSize();
        int dist = 3;
        switch (position) {
        case LEFT_OF_PARENT:
            if (p.height>d.height) this.setSize(d.width, p.height);
            setLocation( p.x-d.width-dist, p.y );
            break;
        case RIGHT_OF_PARENT:
            if (p.height>d.height) this.setSize(d.width, p.height);
            setLocation( p.x+p.width+dist, p.y );
            break;
        case ABOVE_PARENT:
            if (p.width>d.width) this.setSize(p.width, d.height);
            setLocation( p.x, p.y-d.height-dist );
            break;
        case BELOW_PARENT:
            if (p.width>d.width) this.setSize(p.width, d.height);
            setLocation( p.x, p.y+p.height+dist );
            break;
        case PARENT_CENTER:
        default:
            setLocation(
                    (p.width -d.width )/2+p.x,
                    (p.height-d.height)/2+p.y
                );
        }
	}

    public void setSizeAsMinSize() {
        Dimension d = getSize();
        setMinimumSize(d);
    }

    public void showDialog(Position position) {
    	if (position!=null) setPosition(position);
        setVisible( true );
    }

    public void showDialog() {
    	showDialog(null);
    }

    public void closeDialog() {
        setVisible( false );
        if (!repeatedUseOfDialogObject) dispose();
    }

	public static enum Position {
		PARENT_CENTER,
		LEFT_OF_PARENT,
		ABOVE_PARENT,
		RIGHT_OF_PARENT,
		BELOW_PARENT
	}
    
}
