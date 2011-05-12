package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import net.schwarzbaer.java.lib.gui.StandardMainWindow.DefaultCloseOperation;

public class XMLTreeView extends JTree implements TreeSelectionListener {
	private static final long serialVersionUID = -691121546633787401L;
	
	public static ScrollPane createScrollableTreeView(Document document, int prefWidth, int prefHeight) {
		return createScrollableTreeView(document,prefWidth,prefHeight,null);
	}
	
	private static ScrollPane createScrollableTreeView(Document document, int prefWidth, int prefHeight, JTextArea outputArea) {
		XMLTreeView treeView = new XMLTreeView(document);
		treeView.setOuput(outputArea);
		ScrollPane scrollPane = treeView.createScrollPane();
		scrollPane.getViewport().setPreferredSize(new Dimension(prefWidth, prefHeight));
		return scrollPane;
	}
	
	public static void createTreeViewWindow(String windowTitle, Document document, int prefWidth, int prefHeight, boolean exitOnClose, boolean withOutputField) {
		JComponent contentPane;
		if (withOutputField) {
			JTextArea outputArea = new JTextArea();
			outputArea.setEditable(false);
			JScrollPane outputScrollPane = new JScrollPane(outputArea); outputScrollPane.getViewport().setPreferredSize(new Dimension(prefWidth,150));
			
			JScrollPane treeScrollPane = createScrollableTreeView(document,prefWidth,prefHeight,outputArea);
			
			JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,true,treeScrollPane,outputScrollPane);
			splitPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
			splitPane.setResizeWeight(1);
			
			contentPane = splitPane;
		} else {
			JPanel simplePane = new JPanel(new BorderLayout(3,3));
			simplePane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
			simplePane.add( createScrollableTreeView(document,prefWidth,prefHeight,null), BorderLayout.CENTER );
			contentPane = simplePane;
		}
		StandardMainWindow window = new StandardMainWindow(windowTitle,exitOnClose?DefaultCloseOperation.EXIT_ON_CLOSE:DefaultCloseOperation.DISPOSE_ON_CLOSE);
		window.startGUI(contentPane);
	}
	
	public static void createTreeViewWindow(String windowTitle, File file, int prefWidth, int prefHeight, boolean exitOnClose, boolean withOutputField) {
		Document document = parseXML(file);
		createTreeViewWindow(windowTitle, document, prefWidth, prefHeight, exitOnClose, withOutputField);
	}
	
	private static Document parseXML(File file) {
		long timeMillis = System.currentTimeMillis();
		
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		
		DocumentBuilder db;
		try { db = factory.newDocumentBuilder(); }
		catch (ParserConfigurationException e) { e.printStackTrace(); return null; }
		
		Document document;
		try { document = db.parse(file); }
		catch (SAXException e) { e.printStackTrace(); return null; }
		catch (IOException e) { e.printStackTrace(); return null; }
		document.getDocumentElement().normalize();
		
		System.out.printf("File \"%s\" parsed (%d byte)\r\n",file.getName(),file.length());
		System.out.printf("  %d ms elapsed\r\n",System.currentTimeMillis()-timeMillis);
		return document;
	}

	private JTextArea outputArea;
	private boolean removeWhitespaceTextNodes;
	private NodeContextMenu contextMenu;
	private DefaultTreeModel treeModel;

	public XMLTreeView() {
		this(true);
	}
	public XMLTreeView(boolean removeWhitespaceTextNodes) {
		super();
		setModel(treeModel = new DefaultTreeModel(null));
		this.removeWhitespaceTextNodes = removeWhitespaceTextNodes;
		this.outputArea = null;
		addTreeSelectionListener(this);
		addMouseListener(new MouseListener() {
			@Override public void mouseReleased(MouseEvent e) {}
			@Override public void mousePressed(MouseEvent e) {}
			@Override public void mouseExited(MouseEvent e) {}
			@Override public void mouseEntered(MouseEvent e) {}
			@Override public void mouseClicked(MouseEvent e) {
				if (e.getButton()==MouseEvent.BUTTON3) XMLTreeView.this.openContextMenu(e.getX(),e.getY());
			}
		});
		this.contextMenu = new NodeContextMenu();
	}
	public XMLTreeView(Document document) {
		this();
		setDocument(document);
	}
	public XMLTreeView(Document document, boolean removeWhitespaceTextNodes) {
		this(document);
		this.removeWhitespaceTextNodes = removeWhitespaceTextNodes;
	}
	private void openContextMenu(int x, int y) {
		TreePath path = getPathForLocation(x,y);
		if (path==null) return;
		Object object = path.getLastPathComponent();
		if (!(object instanceof XMLTreeNode)) return;
		XMLTreeNode selectedTreenode = (XMLTreeNode)object;
		contextMenu.openMenu(selectedTreenode,x,y);
	}

	private ScrollPane createScrollPane() {
		return new ScrollPane(this);
	}
	private void setOuput(JTextArea outputArea) {
		this.outputArea = outputArea;
	}
	public void setDocument(Document document, boolean removeWhitespaceTextNodes) {
		this.removeWhitespaceTextNodes = removeWhitespaceTextNodes;
		setDocument(document);
	}
	public void setDocument(Document document) {
		treeModel.setRoot(document==null?null:new XMLTreeNode(document,null,0));
		//setModel(new DefaultTreeModel(document==null?null:new XMLTreeNode(document,null,0)));
	}
	
	@Override
	public void valueChanged(TreeSelectionEvent e) {
		Object object = e.getPath().getLastPathComponent();
		if (object instanceof XMLTreeNode) {
			((XMLTreeNode)object).showValue(outputArea);
		}
	}

	private static String getNodeType(short nodeType) {
		switch(nodeType) {
		case Node.ATTRIBUTE_NODE: return "ATTRIBUTE";
		case Node.CDATA_SECTION_NODE: return "CDATA_SECTION";
		case Node.COMMENT_NODE: return "COMMENT";
		case Node.DOCUMENT_FRAGMENT_NODE: return "DOCUMENT_FRAGMENT";
		case Node.DOCUMENT_NODE: return "DOCUMENT";
		case Node.DOCUMENT_TYPE_NODE: return "DOCUMENT_TYPE";
		case Node.ELEMENT_NODE: return "ELEMENT";
		case Node.ENTITY_NODE: return "ENTITY";
		case Node.ENTITY_REFERENCE_NODE: return "ENTITY_REFERENCE";
		case Node.NOTATION_NODE: return "NOTATION";
		case Node.PROCESSING_INSTRUCTION_NODE: return "PROCESSING_INSTRUCTION";
		case Node.TEXT_NODE: return "TEXT";
		}
		return "???";
	}
	private static void showNode(Node node, JTextArea outputArea) {
		String str = String.format("%s[\"%s\"]: \"%s\"",getNodeType(node.getNodeType()),node.getNodeName(),getNodeValue(node));
		if (outputArea!=null) outputArea.setText(str);
		else                  System.out.println(str);
		
		NamedNodeMap attributes = node.getAttributes();
		if (attributes!=null)
			for (int i=0; i<attributes.getLength(); i++) {
				Node item = attributes.item(i);
				if (item!=null) {
					str = String.format("\t%s = \"%s\"",item.getNodeName(),item.getNodeValue());
					if (outputArea!=null) outputArea.append("\r\n"+str);
					else                  System.out.println(str);
				}
			}
	}
	private static String getNodeValue(Node node) {
		String nodeValue = node.getNodeValue();
		if (nodeValue!=null) return nodeValue;
		return getValueOfSingleTextChild(node);
	}
	public static String getValueOfSingleTextChild(Node node) {
		NodeList childNodes = node.getChildNodes();
		if (childNodes.getLength()==1) {
			Node item = childNodes.item(0);
			if (item.getNodeType()==Node.TEXT_NODE) return item.getNodeValue();
		}
		return null;
	}
	private static boolean isEmptyTextNode(Node node) {
		if (node.getNodeType()!=Node.TEXT_NODE) return false;
		String nodeValue = getNodeValue(node);
		if (nodeValue==null) return true;
		return nodeValue.trim().isEmpty();
	}

	private class XMLTreeNode implements TreeNode {
	
		private final XMLTreeView.XMLTreeNode parent;
		private final Node node;
		private final int index;
		private final String path;
		private String value;
		private Vector<XMLTreeView.XMLTreeNode> children;
		
		public XMLTreeNode(Node node, XMLTreeView.XMLTreeNode parent, int index) {
			this.node = node;
			this.parent = parent;
			this.index = index;
			this.children = null;
			if (parent==null) this.path = this.toSimpleLabel();
			else this.path = parent.path+"/"+this.toSimpleLabel();
			this.value = getValueOfSingleTextChild(node);
			if (value!=null) children = new Vector<XMLTreeView.XMLTreeNode>(); 
		}

		public void showValue(JTextArea outputArea) { showNode(node,outputArea); }

		private void createChildList() {
			if (children==null) {
//				System.out.println("create "+path);
				children = new Vector<XMLTreeView.XMLTreeNode>();
				NodeList nodeList = node.getChildNodes();
				for (int i=0; i<nodeList.getLength(); i++) {
					Node item = nodeList.item(i);
					if (!isEmptyTextNode(item) || !removeWhitespaceTextNodes)
						children.add(new XMLTreeNode(item, this, i));
				}
			}
		}
		public String toSimpleLabel() { return "["+index+"]"+node.getNodeName(); }
		@Override public String toString() { return toSimpleLabel()+(value==null?"":": \""+value+"\""); }
		@Override public TreeNode    getParent() { return parent; }
		
		@Override public TreeNode                 getChildAt(int childIndex)   { createChildList(); return children.get(childIndex); }
		@Override public int                      getChildCount()              { createChildList(); return children.size(); }
		@Override public int                      getIndex(TreeNode childNode) { createChildList(); return children.indexOf(childNode); }
		@Override public boolean                  isLeaf()                     { createChildList(); return children.isEmpty(); }
		@Override public Enumeration<XMLTreeNode> children()                   { createChildList(); return children.elements(); }
		@Override public boolean                  getAllowsChildren()          { return !isLeaf(); }
	}
	
	public class NodeContextMenu implements ActionListener {
		
		private JPopupMenu menu;
		private XMLTreeNode selectedTreenode;

		public NodeContextMenu() {
			menu = new JPopupMenu();
			menu.add(GUI.createMenuItem("show path", "show path", this));
			selectedTreenode = null;
		}
		
		public void openMenu(XMLTreeNode selectedTreenode, int x, int y) {
			this.selectedTreenode = selectedTreenode;
			menu.show(XMLTreeView.this, x, y);
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			//System.out.println(e.getActionCommand());
			if (e.getActionCommand().equals("show path")) {
				System.out.println("Path: "+selectedTreenode.path);
				return;
			}
		}
	
	}

	public class ScrollPane extends JScrollPane {
		private static final long serialVersionUID = 3462311693659603894L;
		public ScrollPane(JComponent comp) {
			super(comp);
		}
		public void setDocument(Document document) {
			XMLTreeView.this.setDocument(document);
		}
	}
}