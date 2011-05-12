package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;
import java.util.Iterator;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.event.MouseInputAdapter;

public class ImageGridPanel extends JPanel {
	private static final long serialVersionUID = -189481388341606323L;
	
	private Color   COLOR_BACKGROUND = null;
	private Color   COLOR_BACKGROUND_SELECTED = null;
	private Color   COLOR_BACKGROUND_PRESELECTED = null;
	private Color[] COLOR_BACKGROUND_MARKED = null;
	private Color   COLOR_FOREGROUND = null;
	private Color   COLOR_FOREGROUND_SELECTED = null;
	private Font defaultFont;
	
	private int cols;
	private boolean imageItemFillGridCell;
	public int selectedIndex = -1;
	public Vector<ImageItem> imageItems = new Vector<>();
	private Vector<FocusListener> focusListeners = new Vector<>();
	private Vector<SelectionListener> selectionListeners = new Vector<>();
	private Vector<RightClickListener> rightClickListener = new Vector<>();
	private Vector<DoubleClickListener> doubleClickListener = new Vector<>();
	private int prefTxtWidth;
	private int prefTxtHeight;
	
	public ImageGridPanel(int cols, String preselectedImageID, boolean imageItemFillGridCell, Iterable<ImageData> images) {
		this(cols, preselectedImageID, imageItemFillGridCell, images, 100, 60);
	}
	public ImageGridPanel(int cols, String preselectedImageID, boolean imageItemFillGridCell, Iterable<ImageData> images, int prefTxtWidth, int prefTxtHeight) {
		super(new GridBagLayout());
		this.cols = cols;
		this.imageItemFillGridCell = imageItemFillGridCell;
		this.prefTxtWidth = prefTxtWidth;
		this.prefTxtHeight = prefTxtHeight;
		
		defaultFont = new JLabel().getFont();
		JTextArea dummy = new JTextArea();
		COLOR_BACKGROUND = Color.WHITE;
		COLOR_FOREGROUND = Color.BLACK;
		COLOR_BACKGROUND_SELECTED = dummy.getSelectionColor();
		COLOR_FOREGROUND_SELECTED = dummy.getSelectedTextColor();
		COLOR_BACKGROUND_PRESELECTED = brighter(COLOR_BACKGROUND_SELECTED,0.7f);
		COLOR_BACKGROUND_MARKED = null;
		
		createImageItems(preselectedImageID,images,null);
		
		//setBorder(BorderFactory.createEtchedBorder());
		setBackground(COLOR_BACKGROUND);
	}
	
	public void fixVerticalScrolling(JScrollPane imageGridScrollPane) {
		imageGridScrollPane.getVerticalScrollBar().setUnitIncrement(10);
	}

	protected void createImageItems(String preselectedImageID, Iterable<ImageData> images, Consumer<Integer> indexOutput) {
		selectedIndex = -1;
		imageItems.clear();
		if (images==null) return;
		
		GridBagConstraints c = new GridBagConstraints();
		c.insets = new Insets(0, 0, 2, 2);
		c.weightx = 1.0;
		c.weighty = 0.0;
		c.fill = imageItemFillGridCell?GridBagConstraints.BOTH:GridBagConstraints.NONE;
		
		int index = 0;
		int col=0; 
		for (ImageData imageData : images) {
			boolean isSelected = imageData.ID.equals(preselectedImageID);
			if (isSelected) selectedIndex=index;
			ImageItem imageLabel = new ImageItem(imageData.ID,imageData.name,index,imageData.image,isSelected);
			imageItems.add(imageLabel);
			
			c.gridwidth = col+1==cols?GridBagConstraints.REMAINDER:1;
			col = (col+1)%cols;
			
			add(imageLabel,c);
			++index;
			if (indexOutput!=null)
				indexOutput.accept(index);
		}
	}
	
	
	
	@Override
	public void setBackground(Color bg) {
		super.setBackground(bg);
		COLOR_BACKGROUND = bg;
		if (imageItems!=null)
			imageItems.forEach(il->il.setColors());
	}

	public void setMarkerColors(Color[] colors) {
		COLOR_BACKGROUND_MARKED = colors;
	}
	
	public void setLabelSize(int width, int height) {
		prefTxtWidth  = width;
		prefTxtHeight = height;
	}
	
	public void disableLabelSizePredefinition() {
		prefTxtWidth = -1;
		prefTxtHeight = -1;
	}
	
	public static Color brighter(Color color, float fraction) {
		// fraction==0.0:  same color
		// fraction==1.0:  WHITE
		int r = color.getRed();
		int g = color.getGreen();
		int b = color.getBlue();
		r = Math.min(255, Math.round(255-(255-r)*(1-fraction)));
		g = Math.min(255, Math.round(255-(255-g)*(1-fraction)));
		b = Math.min(255, Math.round(255-(255-b)*(1-fraction)));
		return new Color(r,g,b);
	}
	
	public static class ImageData {
		public String ID;
		public String name;
		public BufferedImage image;
		public ImageData(String ID, String name, BufferedImage image) {
			this.ID = ID;
			this.name = name;
			this.image = image;
		}
	}

	public void resetImages(Iterator<ImageData> images) {
		resetImages(new Iterable<ImageData>(){
			@Override public Iterator<ImageData> iterator() { return images; }
		});
	}

	public void resetImages(Iterable<ImageData> images) {
		String selectedImageID = getSelectedImageID();
		removeAll();
		createImageItems(selectedImageID,images,null);
		revalidate();
	}

	protected String getSelectedImageID() {
		String selectedImageID = null;
		if (selectedIndex>=0)
			selectedImageID = imageItems.get(selectedIndex).ID;
		return selectedImageID;
	}

	public void setImageName(int index, String newName) {
		imageItems.get(index).changeName(newName);
	}

	public void setImage(int index, BufferedImage image) {
		imageItems.get(index).changeImage(image);
	}

	public void setSelectedImage(String ID) {
		if (ID!=null)
			for (int i=0; i<imageItems.size(); i++)
				if (imageItems.get(i).ID.equals(ID)) {
					setSelectedImage(ID,i);
					return;
				}
		setSelectedImage(null,-1);
	}

	public void setSelectedImage(int index) {
		setSelectedImage(index<0?null:imageItems.get(index).ID, index);
	}

	private void setSelectedImage(String ID, int index) {
		if (selectedIndex>=0)
			imageItems.get(selectedIndex).setSelected(false);
		
		selectedIndex=index;
		for (SelectionListener l:selectionListeners)
			l.imageWasSelected(ID);
		
		if (selectedIndex>=0)
			imageItems.get(selectedIndex).setSelected(true);
	}

	protected void notifyFocusListeners(String ID, int index, boolean gainedFocus) {
		focusListeners.forEach(l->l.imageFocusChanged(ID, index, gainedFocus));
	}

	protected void notifyRightClickListeners(String ID, int index, Component source, int x, int y) {
		rightClickListener.forEach(l->l.imageWasRightClicked(ID, index, source, x, y));
	}

	protected void notifyDoubleClickListeners(String ID, int index, Component source, int x, int y) {
		doubleClickListener.forEach(l->l.imageWasDoubleClicked(ID, index, source, x, y));
	}

	public void    addFocusListener( FocusListener l ) { focusListeners.   add(l); }
	public void removeFocusListener( FocusListener l ) { focusListeners.remove(l); }

	public static interface FocusListener {
		public void imageFocusChanged(String ID, int index, boolean gainedFocus);
	}

	public void    addSelectionListener( SelectionListener l ) { selectionListeners.   add(l); }
	public void removeSelectionListener( SelectionListener l ) { selectionListeners.remove(l); }

	public static interface SelectionListener {
		public void imageWasSelected(String ID);
	}
	
	public void    addRightClickListener( RightClickListener l ) { rightClickListener.   add(l); }
	public void removeRightClickListener( RightClickListener l ) { rightClickListener.remove(l); }
	
	public static interface RightClickListener {
		public void imageWasRightClicked(String ID, int index, Component source, int x, int y);
	}
	
	public void    addDoubleClickListener( DoubleClickListener l ) { doubleClickListener.   add(l); }
	public void removeDoubleClickListener( DoubleClickListener l ) { doubleClickListener.remove(l); }
	
	public static interface DoubleClickListener {
		public void imageWasDoubleClicked(String ID, int index, Component source, int x, int y);
	}

	public void scrollToPreselectedImage(JScrollPane imageScrollPane) {
		if (selectedIndex>=0) {
			int row = selectedIndex/cols;
			int rowCount = Math.round((float)Math.ceil(imageItems.size()/(double)cols));
			//System.out.printf("Row %d/%d was preselected\r\n",row,rowCount);
			
			JScrollBar scrollBar = imageScrollPane.getVerticalScrollBar();
			int val = scrollBar.getValue();
			int max = scrollBar.getMaximum();
			int min = scrollBar.getMinimum();
			int ext = scrollBar.getVisibleAmount();
			//System.out.printf("VerticalScrollBar is at %d..%d(%d)..%d \r\n",min,val,ext,max);
			
			int h = (max-min)/rowCount;
			//System.out.printf("h = %d \r\n",h);
			val = row*h - (ext-h)/2 + min;
			//System.out.printf("val = %d \r\n",val);
			val = Math.max(min,val);
			val = Math.min(max-ext,val);
			
			scrollBar.setValue(val);
			//System.out.printf("VerticalScrollBar set to %d..%d(%d)..%d \r\n",min,val,ext,max);
		}
	}

	public class ImageItem extends JPanel {
		private static final long serialVersionUID = 4629632101041946456L;

		private JLabel imageContainer;
		private JTextArea label;
		
		private boolean isSelected;
		private boolean hasFocus;
		private int markerIndex;
		public String ID;
		private int index;

		public ImageItem(String ID, String name, int index, BufferedImage image, boolean isSelected) {
			super(new BorderLayout(3,3));
			setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
			
			this.ID = ID;
			this.index = index;
			this.isSelected = isSelected;
			this.hasFocus = false;
			this.markerIndex = 0;
			
			label = new JTextArea(name);
			if (prefTxtWidth>0 && prefTxtHeight>0) label.setPreferredSize(new Dimension(prefTxtWidth,prefTxtHeight));
			label.setLineWrap(true);
			label.setWrapStyleWord(false);
			label.setEditable(false);
			label.setFont(defaultFont);
			label.setBackground(null);
			MouseListener[] mouseListeners = label.getMouseListeners();
			MouseMotionListener[] mouseMotionListeners = label.getMouseMotionListeners();
			for (MouseListener l:mouseListeners) label.removeMouseListener(l);
			for (MouseMotionListener l:mouseMotionListeners) label.removeMouseMotionListener(l);
			
			
			imageContainer = new JLabel(image!=null?new ImageIcon(image):null);
			add(imageContainer,BorderLayout.NORTH);
			add(label,BorderLayout.CENTER);
			
			MouseInputAdapter m = new MouseInputAdapter() {
				@Override public void mouseClicked(MouseEvent e) {
					if (e.getButton()==MouseEvent.BUTTON3) notifyRightClickListeners(ImageItem.this.ID, ImageItem.this.index, ImageItem.this, e.getX(), e.getY());
					else if (e.getClickCount()==2) notifyDoubleClickListeners(ImageItem.this.ID, ImageItem.this.index, ImageItem.this, e.getX(), e.getY());
					else setSelectedImage(ImageItem.this.index);
					requestFocusInWindow();
				}
				@Override public void mouseEntered(MouseEvent e) { hasFocus=true;  setColors(); notifyFocusListeners(ImageItem.this.ID, ImageItem.this.index, hasFocus); }
				@Override public void mouseExited (MouseEvent e) { hasFocus=false; setColors(); notifyFocusListeners(ImageItem.this.ID, ImageItem.this.index, hasFocus); }
				
			};
			
			setColors();
			addMouseListener(m);
			addMouseMotionListener(m);
			label.addMouseListener(m);
			label.addMouseMotionListener(m);
		}
		
		public int getIndex() {
			return index;
		}
		
		public void changeImage(BufferedImage image) {
			imageContainer.setIcon(image!=null?new ImageIcon(image):null);
		}

		public void changeName(String newName) {
			label.setText(newName);
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
			setColors();
			//repaint();
		}

		public void setMarkerIndex(int markerIndex) {
			this.markerIndex = markerIndex;
			setColors();
			//repaint();
		}
		
		private void setColors() {
			Color bgColor; 
			if      (hasFocus     ) bgColor = COLOR_BACKGROUND_SELECTED;
			else if (isSelected   ) bgColor = COLOR_BACKGROUND_PRESELECTED;
			else if (markerIndex>0) bgColor = COLOR_BACKGROUND_MARKED[markerIndex-1];
			else                    bgColor = COLOR_BACKGROUND;
			setBackground(bgColor);
			if (hasFocus)
				label.setForeground(COLOR_FOREGROUND_SELECTED);
			else {
				if (isDark(bgColor))
					label.setForeground(Color.WHITE);
				else
					label.setForeground(COLOR_FOREGROUND);
			}
		}

		private boolean isDark(Color color) {
			return color.getRed()<0x7F && color.getGreen()<0x7F && color.getBlue()<0x7F;
		}
	
	}

}