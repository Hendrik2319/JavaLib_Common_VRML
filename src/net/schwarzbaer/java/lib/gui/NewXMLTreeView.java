package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;
import org.xml.sax.SAXException;

import net.schwarzbaer.java.lib.gui.StandardMainWindow.DefaultCloseOperation;
import net.schwarzbaer.java.lib.system.ClipboardTools;

public class NewXMLTreeView extends JTree {
	private static final long serialVersionUID = -8743556098414758509L;
	
	public static JScrollPane createScrollableTreeView(Document document, int prefWidth, int prefHeight) {
		NewXMLTreeView treeView = new NewXMLTreeView();
		treeView.setDocument(document);
		JScrollPane scrollPane = new JScrollPane( treeView );
		scrollPane.setPreferredSize(new Dimension(prefWidth, prefHeight));
		return scrollPane;
	}
	
	public static void createTreeViewWindow(String windowTitle, Document document, int prefWidth, int prefHeight, boolean exitOnClose) {
		JComponent contentPane = new JPanel(new BorderLayout(3,3));
		contentPane.setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
		contentPane.add( createScrollableTreeView(document,prefWidth,prefHeight), BorderLayout.CENTER );
		
		StandardMainWindow window = new StandardMainWindow(windowTitle,exitOnClose?DefaultCloseOperation.EXIT_ON_CLOSE:DefaultCloseOperation.DISPOSE_ON_CLOSE);
		window.startGUI(contentPane);
	}
	
	public static void createTreeViewWindow(String windowTitle, File file, int prefWidth, int prefHeight, boolean exitOnClose) {
		Document document = parseXML(file);
		createTreeViewWindow(windowTitle, document, prefWidth, prefHeight, exitOnClose);
	}
	
	public static Document parseXML(File xmlFile) {
		try {
			return DocumentBuilderFactory
						.newInstance()
						.newDocumentBuilder()
						.parse(xmlFile);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			e.printStackTrace();
			return null;
		}
	}

	private DefaultTreeModel treeModel;
	private boolean ignoreEmptyTextNodes;
	private TreeNodeFactory treeNodeFactory;
	private NodeContextMenu contextMenu;
	
	public NewXMLTreeView() { this(true,null); }
	public NewXMLTreeView(boolean ignoreEmptyTextNodes) { this(ignoreEmptyTextNodes,null); }
	public NewXMLTreeView(TreeNodeFactory treeNodeFactory) { this(true,treeNodeFactory); }
	public NewXMLTreeView(boolean ignoreEmptyTextNodes, TreeNodeFactory treeNodeFactory) {
		super();
		this.ignoreEmptyTextNodes = ignoreEmptyTextNodes;
		this.treeNodeFactory = treeNodeFactory==null?DOMTreeNode::new:treeNodeFactory;
		this.treeModel = new DefaultTreeModel(null);
		setModel(treeModel);
		contextMenu = new NodeContextMenu();
		addMouseListener(contextMenu);
	}
	
	public NodeContextMenu getContextMenu() {
		return contextMenu;
	}

	public void setRoot(TreeNode root) {
		treeModel.setRoot(root);
	}
	public void setDocument(Document document) {
		treeModel.setRoot(document==null?null:treeNodeFactory.create(null, document, ignoreEmptyTextNodes));
	}
	public void setDocument(Document document,TreeNodeFactory treeNodeFactory) {
		this.treeNodeFactory = treeNodeFactory;
		setDocument(document);
	}
	
	public static boolean copyToClipBoard(String str) {
		return ClipboardTools.copyToClipBoard(str);
	}
	
	public class NodeContextMenu extends TreeContextMenu<DOMTreeNode> {
		private static final long serialVersionUID = -6877543437500214909L;

		private JMenuItem collapseRemainingTree;
		private JMenuItem copyNodeString;
		private JMenuItem copyXMLPath;

		public NodeContextMenu() {
			super(NewXMLTreeView.this);
			
			add(collapseRemainingTree = createMenuItem("Collapse remaining tree",e->{
				for (int row=getRowCount()-1; row>=0; --row) collapseRow(row);
				if (clickedTreePath!=null)
					expandPath(clickedTreePath);
			}));
			add(copyNodeString =  createMenuItem("Copy Node String",e->{
				if (clickedNode!=null)
					copyToClipBoard(clickedNode.toString());
			}));
			add(copyXMLPath = createMenuItem("Copy XML Path",e->{
				if (clickedNode!=null)
					copyToClipBoard(clickedNode.getXmlPath());
			}));
		}
		
		@Override
		protected void updateMenuItems() {
			collapseRemainingTree.setEnabled(clickedTreePath!=null);
			copyNodeString.setEnabled(clickedNode!=null);
			copyXMLPath.setEnabled(clickedNode!=null);
		}

		@Override
		protected DOMTreeNode castToTreeNodeType(TreeNode treeNode) {
			if (treeNode instanceof DOMTreeNode)
				return (DOMTreeNode) treeNode;
			return null;
		}
		
	}
	
	public static interface TreeNodeFactory {
		DOMTreeNode create(DOMTreeNode parent, Node node, boolean ignoreEmptyTextNodes);
	}
	
	public static class DOMTreeNode implements TreeNode {
		
		protected final DOMTreeNode parent;
		protected final Node node;
		protected Vector<DOMTreeNode> children;
		protected String singleTextValue;
		private boolean ignoreEmptyTextNodes;
		private TreeNodeFactory factory;
		
		public DOMTreeNode(DOMTreeNode parent, Node node, boolean ignoreEmptyTextNodes) {
			this(parent, node, ignoreEmptyTextNodes, DOMTreeNode::new);
		}
		protected DOMTreeNode(DOMTreeNode parent, Node node, boolean ignoreEmptyTextNodes, TreeNodeFactory factory) {
			this.parent = parent;
			this.node = node;
			this.ignoreEmptyTextNodes = ignoreEmptyTextNodes;
			this.factory = factory;
			this.children = null;
			this.singleTextValue = null;
		}

		public String getXmlPath() {
			if (parent == null) return "\""+node.getNodeName()+"\"";
			return parent.getXmlPath() + ", \""+node.getNodeName()+"\"";
		}
		
		protected void createChildren() {
			children = new Vector<>();
			NodeList childNodes = node.getChildNodes();
			
			if (childNodes.getLength()==1) {
				Node childNode = childNodes.item(0);
				if (childNode.getNodeType()==Node.TEXT_NODE) {
					singleTextValue = childNode.getNodeValue();
					return;
				}
			}
			
			for (int i=0; i<childNodes.getLength(); ++i){
				Node childNode = childNodes.item(i);
				if (/*!removeWhitespaceTextNodes ||*/ !isEmptyTextNode(childNode))
					children.add(factory.create(this, childNode, ignoreEmptyTextNodes));
			}
		}
		
		private boolean isEmptyTextNode(Node childNode) {
			return childNode.getNodeType()==Node.TEXT_NODE && childNode.getNodeValue().trim().isEmpty();
		}
		
		@Override public String toString() {
			return toString(node) + (singleTextValue==null?"":" :  "+singleTextValue);
		}
		
		@Override public TreeNode getParent()        { return parent; }
		@Override public boolean getAllowsChildren() { return true; }
		@Override public boolean isLeaf()            { return getChildCount()==0; }
		
		@Override public int                      getChildCount()            { if (children==null) createChildren(); return children.size(); }
		@Override public int                      getIndex(TreeNode node)    { if (children==null) createChildren(); return children.indexOf(node); }
		@Override public TreeNode                 getChildAt(int childIndex) { if (children==null) createChildren(); return children.get(childIndex); }
		@Override public Enumeration<DOMTreeNode> children()                 { if (children==null) createChildren(); return children.elements(); }
		
		private String toString(Node node) {
			switch (node.getNodeType()) {
			case Node.DOCUMENT_NODE     : return toString((Document    )node);
			case Node.ELEMENT_NODE      : return toString((Element     )node);
			case Node.TEXT_NODE         : return toString((Text        )node);
			case Node.COMMENT_NODE      : return toString((Comment     )node);
			case Node.CDATA_SECTION_NODE: return toString((CDATASection)node);
			}
			return String.format("[%d] %s", node.getNodeType(), node.getNodeName());
		}
		private String toString(Comment comment) {
			return String.format("<!-- %s -->", comment.getNodeValue());
		}
		private String toString(CDATASection cdataSection) {
			return String.format("[CDATA %s ]", cdataSection.getNodeValue());
		}
		private String toString(Text text) {
			return String.format("\"%s\"", text.getNodeValue());
		}
		private String toString(Element element) {
			StringBuilder sb = new StringBuilder();
			NamedNodeMap attributes = element.getAttributes();
			for (int i=0; i<attributes.getLength(); ++i) {
				Node attr = attributes.item(i);
				sb.append(String.format(" %s=\"%s\"", attr.getNodeName(), attr.getNodeValue()));
			}
			return String.format("<%s%s>", element.getNodeName(), sb.toString());
		}
		private String toString(Document document) {
			String str = ""; // "Document "+document.getNodeName();
			str += "<document>";
			if (document.getDoctype    ()!=null) str += " DocType:"    +document.getDoctype    ();
			//if (document.getBaseURI    ()!=null) str += " BaseURI:"    +document.getBaseURI    ();
			//if (document.getDocumentURI()!=null) str += " DocURI:"     +document.getDocumentURI();
			if (document.getXmlEncoding()!=null) str += " XmlEncoding:"+document.getXmlEncoding();
			if (document.getXmlVersion ()!=null) str += " XmlVersion:" +document.getXmlVersion ();
			return str;
		}
	}
}
