package net.schwarzbaer.java.lib.gui;

import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JPopupMenu;

public class ContextMenu extends JPopupMenu {
	private static final long serialVersionUID = 7336661746627669558L;
	
	private Vector<ContextMenuInvokeListener> listeners;
	
	public ContextMenu() {
		listeners = new Vector<>();
	}
	
	public void addTo(Component comp) {
		comp.addMouseListener(new MouseAdapter() {
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3) {
					for (ContextMenuInvokeListener listener:listeners)
						listener.contextMenuWillBeInvoked(comp, e.getX(), e.getY());
					show(comp, e.getX(), e.getY());
				}
			}
		});
	}
	
	public void    addContextMenuInvokeListener( ContextMenuInvokeListener listener ) { listeners.   add(listener); } 
	public void removeContextMenuInvokeListener( ContextMenuInvokeListener listener ) { listeners.remove(listener); }
	
	public interface ContextMenuInvokeListener {
		public void contextMenuWillBeInvoked(Component comp, int x, int y);
	}
}
