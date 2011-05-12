package net.schwarzbaer.java.lib.gui;

import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JTree;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

public abstract class TreeContextMenu<TreeNodeType extends TreeNode> extends JPopupMenu implements MouseListener {
	private static final long serialVersionUID = -8810588233589025185L;
	
	public interface ContextMenuUpdateListener {
		public void updateMenuItems();
	}
	
	public TreePath clickedTreePath = null;
	public TreeNodeType clickedNode = null;

	private ContextMenuUpdateListener updateListener;
	private JTree tree;

	public TreeContextMenu(JTree tree) {
		this.tree = tree;
		this.updateListener = null;
	}

	public void setUpdateListener(ContextMenuUpdateListener updateListener) {
		this.updateListener = updateListener;
	}
	
	protected abstract void updateMenuItems();
	protected abstract TreeNodeType castToTreeNodeType(TreeNode treeNode);

	protected JMenuItem createMenuItem(String title, ActionListener al) {
		JMenuItem comp = new JMenuItem(title);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}
	
	@Override public void mousePressed(MouseEvent e) {}
	@Override public void mouseReleased(MouseEvent e) {}
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseExited(MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {
		clickedTreePath = tree.getPathForLocation(e.getX(), e.getY());
		clickedNode = null;
		if (clickedTreePath!=null) {
			Object object = clickedTreePath.getLastPathComponent();
			if (object instanceof TreeNode)
				clickedNode = castToTreeNodeType((TreeNode)object);
		}
		if (e.getButton() == MouseEvent.BUTTON3) {
			updateMenuItems();
			if (updateListener!=null) updateListener.updateMenuItems();
			show(tree, e.getX(), e.getY());
		}
	}
}
