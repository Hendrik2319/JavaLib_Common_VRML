package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.EventObject;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileSystemView;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

public class DirTree {

	private final DefaultTreeModel treeModel;
	private final JTree tree;
	private DirTreeNode rootNode;
	private final DirTreeCellEditorRenderer renderer;
	private final DirTreeCellEditorRenderer editor;
	private boolean sortDirs;

	public DirTree(UserTreeNode rootUserTreeNode) {
		rootNode = DirTreeNode.createEmptyRootNode( rootUserTreeNode );
		treeModel = new DefaultTreeModel( rootNode );
		tree = new JTree( treeModel );
		tree.setCellRenderer( renderer = new DirTreeCellEditorRenderer() );
		tree.setCellEditor  ( editor   = new DirTreeCellEditorRenderer() );
		tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		tree.setRowHeight(20);
		tree.setEditable( true );
		sortDirs = false;
	}

	public void addTreeSelectionListener(TreeSelectionListener tsl) {
		tree.addTreeSelectionListener(tsl);
	}
	
	public void setDirSorting(boolean sortDirs) {
		this.sortDirs = sortDirs;
	}

	public void setCellEditingEnable(boolean isCellEditingEnabled) {
		editor.setCellEditingEnable(isCellEditingEnabled);
	}

	public void setRowHeight(int rh) {
		tree.setRowHeight(rh);
		renderer.setRowHeight(rh);
		editor.setRowHeight(rh);
	}

	public void setRoot(File newDir, UserTreeNode userTreeNode) {
		treeModel.setRoot( rootNode = new DirTreeNode( newDir, userTreeNode, sortDirs ) );
	}

	public DirTreeNode getRoot() {
		return rootNode;
	}

	public DefaultTreeModel getTreeModel() { return treeModel; }
	public JTree getTree() { return tree; }
	public JScrollPane getTreeInScrollPane() { return new JScrollPane( tree ); }

	public void prepareChildren() {
		rootNode.prepareChildren_recursive(sortDirs);
	}

	public static interface UserTreeNode {

		public void setDirTreeNode(DirTreeNode dirTreeNode);
		public UserTreeNode createChildNode();
		public void analyseChildren();
		public Color getTreeItemBGColorForCheckBox();
		public Color getTreeItemBGColorForLabel();
		public boolean isSelectable();

	}
	public static class DirTreeNode implements TreeNode, Comparable<DirTreeNode> {

		public final File file;
		public final DirTreeNode parent;
		public final UserTreeNode userTreeNode;
		public DirTreeNode[] children;
		private Icon icon;
		private boolean isSelected;
		private final boolean sortDirs;

		public static DirTreeNode createEmptyRootNode(UserTreeNode userTreeNode) {
			DirTreeNode dummyRootNode = new DirTreeNode( null, null, userTreeNode, false );
			dummyRootNode.children = new DirTreeNode[0];
			return dummyRootNode;
		}

		public DirTreeNode(File file, UserTreeNode userTreeNode, boolean sortDirs) {
			this( file, null, userTreeNode, sortDirs );
		}

		private DirTreeNode(File file, DirTreeNode parent, UserTreeNode userTreeNode, boolean sortDirs) {
			this.file = file;
			this.parent = parent;
			this.userTreeNode = userTreeNode;
			this.sortDirs = sortDirs;
			this.children = null;
			this.icon = null;
			if (file != null) this.icon = FileSystemView.getFileSystemView().getSystemIcon( file );
			userTreeNode.setDirTreeNode(this);
			isSelected = false;
		}

		public void prepareChildren_recursive(boolean sortDirs) {
			prepareChildren( "collector" );
			for (int i=0; i<children.length; i++ ) {
				children[i].prepareChildren_recursive(sortDirs);
			}
		}

		public void prepareChildren() {
			prepareChildren( "node" );
		}

		private synchronized void prepareChildren( String callerStr ) {
			if (children!=null) return;
//			System.out.printf( "DirTreeNode.prepareChildren(\"%s\"): %s\r\n",callerStr,file );
            
			if ( (file == null) || file.isFile() ) {
				children = new DirTreeNode[0];
				return;
			}
			File[] files = file.listFiles();
			if (files==null) {
				children = new DirTreeNode[0];
				return;
			}
			if (sortDirs) {
				Arrays.sort(files);
			}
			children = new DirTreeNode[files.length];
			for (int i=0; i<files.length; i++ ) {
				//                System.out.println( "   create child: "+files[i] );
				children[i] = new DirTreeNode( files[i], this, userTreeNode.createChildNode(), sortDirs );
			}

			userTreeNode.analyseChildren();
		}

		public File getFileObj() {
			return file;
		}

		public Icon getIcon() {
			return icon;
			//            if (file != null) return FileSystemView.getFileSystemView().getSystemIcon( file );
			//            return null;
		}

		@Override
		public TreeNode getChildAt(int childIndex) {
			prepareChildren();
			return children[childIndex];
		}

		@Override
		public int getChildCount() {
			prepareChildren();
			return children.length;
		}
		@Override
		public TreeNode getParent() {
			return parent;
		}

		@Override
		public int getIndex(TreeNode node) {
			prepareChildren();
			for ( int i=0; i<children.length; i++)
				if (children[i].equals(node)) return i;
			return -1;
		}

		public void setSelected( boolean isSelected ) {
			this.isSelected = isSelected;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public boolean isDirectory() {
			if (file==null) return false;
			return file.isDirectory();
		}

		public boolean isFile() {
			if (file==null) return false;
			return file.isFile();
		}

		@Override
		public boolean getAllowsChildren() {
			return isDirectory();
		}
		@Override
		public boolean isLeaf() {
			prepareChildren();
			return children.length==0;
		}

		@Override
		public Enumeration<DirTreeNode> children() {
			prepareChildren();
			return new TreeNodeEnumeration(children);
		}

		@Override
		public String toString() {
			if (file==null) return "<empty>";
			if (parent==null) return file.getPath();
			return file.getName();
		}

		@Override
		public int compareTo(DirTreeNode other) {
			if ( (this.file==null) && (other.file==null) ) return 0;
			if (this.file==null) return +1;
			if (other.file==null) return -1;
			return this.file.getName().compareToIgnoreCase( other.file.getName() );
		}

		private static class TreeNodeEnumeration implements Enumeration<DirTreeNode> {

			private DirTreeNode[] treeNodes;
			private int index;

			public TreeNodeEnumeration(DirTreeNode[] treeNodes) {
				this.treeNodes = treeNodes;
				this.index = 0;
			}

			@Override public boolean hasMoreElements() { return (treeNodes!=null) && (index<treeNodes.length); }
			@Override public DirTreeNode nextElement() { index++; return treeNodes[index-1]; }

		}

		public boolean isSelectable() {
			if (userTreeNode==null) return false;
			return userTreeNode.isSelectable();
		}
	}

	public static class DirTreeCellEditorRenderer implements TreeCellRenderer, TreeCellEditor, ActionListener {

		private static final Color Const_SelectedCellRendererColor = new Color( 0.6f, 0.8f, 1.0f );
		private static final Color Const_FocussedCellRendererColor = new Color( 0.6f, 1.0f, 0.8f );
		private static final Color Const_CellEditorColor           = new Color( 0.6f, 0.6f, 0.8f );
		private JPanel compChkBx_Panel;
		private JLabel compChkBx_Label;
		private JCheckBox compChkBx_CheckBox;
		private JPanel compLabel_Panel;
		private JLabel compLabel_Label;
		private Vector<CellEditorListener> CellEditorListeners;
		private DirTreeNode currentEditorValue;
		private int rowHeight;
		private boolean isCellEditingEnabled;

		public DirTreeCellEditorRenderer() {
			rowHeight = 20;
			isCellEditingEnabled = false;

			compChkBx_CheckBox = new JCheckBox();
			compChkBx_CheckBox.setActionCommand("checkbox");
			compChkBx_CheckBox.addActionListener(this);

			compChkBx_Panel = new JPanel( new BorderLayout() );
			compChkBx_Panel.add( compChkBx_CheckBox, BorderLayout.WEST );
			compChkBx_Panel.add( compChkBx_Label = new JLabel(), BorderLayout.CENTER );

			compLabel_Panel = new JPanel( new BorderLayout() );
			compLabel_Panel.add( compLabel_Label = new JLabel(), BorderLayout.CENTER );

			compChkBx_Panel.setBorder( BorderFactory.createEmptyBorder( 2,0,2,0 ) );
			compLabel_Panel.setBorder( BorderFactory.createEmptyBorder( 2,0,2,0 ) );

			compChkBx_Label.setFont( new JTextField().getFont() );
			compLabel_Label.setFont( new JTextField().getFont() );

			compChkBx_Panel   .setOpaque( false );
			compChkBx_CheckBox.setOpaque( false );
			compChkBx_Label   .setOpaque( false );

			compLabel_Panel   .setOpaque( false );
			compLabel_Label   .setOpaque( false );

			CellEditorListeners = new Vector<CellEditorListener>();
			currentEditorValue = null;
		}

		public void setCellEditingEnable(boolean isCellEditingEnabled) {
			this.isCellEditingEnabled = isCellEditingEnabled;
		}

		public void setRowHeight(int rowHeight) {
			this.rowHeight = rowHeight;
		}

		private JComponent getTreeCellEditorRendererComponent(JTree tree, DirTreeNode dtn, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {

			compChkBx_Panel   .setOpaque( false );
			compChkBx_CheckBox.setOpaque( false );
			compChkBx_Label   .setOpaque( false );

			compLabel_Panel   .setOpaque( false );
			compLabel_Label   .setOpaque( false );

			JComponent panel = compLabel_Panel;
			JLabel     label = compLabel_Label;

			String text = "???";

			if (dtn!=null) {
				text = dtn.toString();

				if ( dtn.isSelectable() ) {
					panel = compChkBx_Panel;
					label = compChkBx_Label;

					Color chkBxBGColor = dtn.userTreeNode.getTreeItemBGColorForCheckBox();
					if (chkBxBGColor!=null) {
						compChkBx_CheckBox.setOpaque( true );
						compChkBx_CheckBox.setBackground( chkBxBGColor );
					}

					compChkBx_CheckBox.setSelected( dtn.isSelected() );
				}

				Color labelBGColor = dtn.userTreeNode.getTreeItemBGColorForLabel();
				if (labelBGColor!=null) {
					label.setOpaque( true );
					label.setBackground( labelBGColor );
				}

				label.setIcon( dtn.getIcon() );
			} else {
				label.setIcon( null );
			}

			if (hasFocus) {
				panel.setOpaque( true );
				panel.setBackground( Const_FocussedCellRendererColor );
			} else
			if (selected) {
				panel.setOpaque( true );
				panel.setBackground( Const_SelectedCellRendererColor );
			}

			label.setText( text );

			Dimension d = new Dimension(20,rowHeight);
			//            Dimension d = cellRendererLabelComponent.getPreferredSize();
			d.width = label.getFontMetrics( label.getFont() ).stringWidth(text);
			d.width = d.width + d.width/20 + 30;
			label.setPreferredSize( d );

			return panel;
		}

		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			DirTreeNode dtn = null;
			if ( value instanceof DirTreeNode ) dtn = (DirTreeNode)value;
//			System.out.println("getTreeCellRendererComponent: "+dtn);
			return getTreeCellEditorRendererComponent( tree, dtn, isSelected, expanded, leaf, row, hasFocus);
		}

		@Override
		public Component getTreeCellEditorComponent(JTree tree, Object value, boolean isSelected, boolean expanded, boolean leaf, int row) {
			DirTreeNode dtn = null;
			if ( value instanceof DirTreeNode ) dtn = (DirTreeNode)value;
//			System.out.println("getTreeCellEditorComponent: "+dtn);
			JComponent cellEditorComponent = getTreeCellEditorRendererComponent( tree, dtn, isSelected, expanded, leaf, row, true );
			if ( value instanceof DirTreeNode ) currentEditorValue = (DirTreeNode)value;
			else                                currentEditorValue = null;
			cellEditorComponent.setOpaque( true );
			cellEditorComponent.setBackground( Const_CellEditorColor );
			return cellEditorComponent;
		}

		@Override
		public Object  getCellEditorValue()                  {
			return currentEditorValue;
		}
		@Override
		public boolean isCellEditable(EventObject anEvent) {
			if (!isCellEditingEnabled) return false;
			
//			System.out.println("isCellEditable: anEvent:"+anEvent);
			if (anEvent==null) return false;
			if (!(anEvent instanceof MouseEvent)) return false;
			MouseEvent mouseEvent = (MouseEvent)anEvent;  
			
//			System.out.println("isCellEditable: source:"+mouseEvent.getSource());
			if (mouseEvent.getSource()==null) return false;
			if (!(mouseEvent.getSource() instanceof JTree)) return false;
			JTree tree = (JTree)mouseEvent.getSource();
			
			TreePath path = tree.getPathForLocation(mouseEvent.getX(), mouseEvent.getY());  
//			System.out.println("isCellEditable: selected path:"+path);
			if (path==null) return false;
			Object lastPathComponent = path.getLastPathComponent();
			if (lastPathComponent==null) return false;
			if (!(lastPathComponent instanceof DirTreeNode)) return false;
			DirTreeNode selectedDirTreeNode = (DirTreeNode)lastPathComponent;
			
//			System.out.println("isCellEditable: selected DirTreeNode:"+selectedDirTreeNode);
			return selectedDirTreeNode.isSelectable();
		}
		@Override
		public boolean shouldSelectCell(EventObject anEvent) { return true; }

		@Override
		public boolean stopCellEditing() {
//			System.out.println("stopCellEditing");
			currentEditorValue = null;
			Iterator<CellEditorListener> it = CellEditorListeners.iterator();
			while (it.hasNext()) it.next().editingStopped( new ChangeEvent(this) );
			return true;
		}

		@Override
		public void cancelCellEditing() {
//			System.out.println("cancelCellEditing");
			currentEditorValue = null;
			Iterator<CellEditorListener> it = CellEditorListeners.iterator();
			while (it.hasNext()) it.next().editingCanceled( new ChangeEvent(this) );
		}

		@Override public void addCellEditorListener   (CellEditorListener l) { CellEditorListeners.add(l); }
		@Override public void removeCellEditorListener(CellEditorListener l) { CellEditorListeners.remove(l); }

		@Override
		public void actionPerformed(ActionEvent e) {
			if ("checkbox".equals(e.getActionCommand())) {
				if (currentEditorValue != null) {
					currentEditorValue.setSelected( compChkBx_CheckBox.isSelected() );
				}
				return;
			}
		}
	}
	public abstract static class TreeDataCollector implements Runnable {

		private final DirTree dirTree;
		private final boolean withDebugOutput;

		public TreeDataCollector(DirTree dirTree, boolean withDebugOutput) {
			this.dirTree = dirTree;
			this.withDebugOutput = withDebugOutput;
		}

		public void start() {
			new Thread(this).start();
		}

		protected abstract void disableDirChange( boolean disable );

		@Override
		public void run() {
			disableDirChange(true);
			if (withDebugOutput) System.out.println( "DirTree.prepareChildren: "+dirTree.getRoot() );
			dirTree.prepareChildren();
			if (withDebugOutput) System.out.println( "DirTree.prepareChildren: "+dirTree.getRoot()+" --> treeDidChange" );
			dirTree.getTree().treeDidChange();
			disableDirChange(false);
		}

	}
}
