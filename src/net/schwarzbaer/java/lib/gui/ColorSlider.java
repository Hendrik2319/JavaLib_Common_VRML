package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

import javax.swing.JComponent;
import javax.swing.border.Border;

public class ColorSlider extends JComponent implements MouseListener, MouseMotionListener {
	private static final long serialVersionUID = 6525911479150907865L;
	
	private   final ColorChangeListener colorChangeListener;
	protected final SliderType type;
	protected final ColorSliderModel model;
	private   int width;
	private   int height;
	
	public ColorSlider(SliderType type, Colorizer colorizer, float f, ColorChangeListener colorChangeListener ) {
		this(type, new SimpleColorSliderModel(f, colorizer), colorChangeListener);
		if (type==SliderType.DUAL)
			throw new UnsupportedOperationException("A ColorSlider based on a Colorizer can't be a dual slider."); 
	}
	public ColorSlider(SliderType type, ColorSliderModel model, ColorChangeListener colorChangeListener) {
		this.colorChangeListener = colorChangeListener;
		this.type = type;
		this.model = model;
        this.width = -1;
        this.height = -1;
		switch (type) {
		case VERTICAL  : setPreferredSize(new Dimension( 20, 128 )); break;
		case HORIZONTAL: setPreferredSize(new Dimension( 128, 20 )); break;
		case DUAL      : setPreferredSize(new Dimension( 128, 128 )); break;
		}
		this.addMouseListener(this);
		this.addMouseMotionListener(this);
	}
	
	public void setValue( float f ) { model.setValue(f); repaint(); }
	public void setValue( float fH, float fV ) { model.setValue(fH, fV); repaint(); }
	
    @Override protected void paintComponent(Graphics g) {
        super.paintComponent(g);
		int x = 0;
		int y = 0;
		int canvasWidth = width;
		int canvasHeight = height;
		Border border = getBorder();
		if (border!=null) {
			Insets borderInsets = border.getBorderInsets(this);
			x = borderInsets.left;
			y = borderInsets.top;
			canvasWidth  -= borderInsets.left+borderInsets.right ;
			canvasHeight -= borderInsets.top +borderInsets.bottom;
		}
        paintCanvas( g, x,y, canvasWidth,canvasHeight );
    }
	
	private void paintCanvas(Graphics g, int x, int y, int width, int height) {
		switch (type) {
		case VERTICAL  : paintV(g,x,y,width,height); break;
		case HORIZONTAL: paintH(g,x,y,width,height); break;
		case DUAL      : paintD(g,x,y,width,height); break;
		default:
			g.setColor(Color.GREEN);
			g.fillRect(x, y, width, height);
		}
	}

	private void paintV(Graphics g, int x, int y, int width, int height) {
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(x+3, y+0, width-7, height);
		} else
			for (int y1=0; y1<height; y1++) {
				g.setColor(model.calcColor(calcFraction(height-1,y1,0)));
				g.drawLine(x+3,y+y1,x+width-4,y+y1);
			}
		int y1 = Math.round((1-model.getValue())*(height-1));
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawLine(x,y+y1,x+width-1,y+y1);
	}

	private void paintH(Graphics g, int x, int y, int width, int height) {
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(x, y+3, width, height-7);
		} else
			for (int x1=0; x1<width; x1++) {
				g.setColor(model.calcColor(calcFraction(0,x1,width-1)));
				g.drawLine(x+x1,y+3,x+x1,y+height-4);
			}
		int x1 = Math.round(model.getValue()*(width-1));
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawLine(x+x1,y,x+x1,y+height-1);
	}

	private void paintD(Graphics g, int x, int y, int width, int height) {
		if (!isEnabled()) {
			g.setColor(Color.GRAY);
			g.fillRect(x+3, y+3, width-6, height-6);
		} else
			for (int x1=3; x1<width-3; x1++) {
				model.prepareColorH(calcFraction(3,x1,width-4));
				for (int y1=3; y1<height-3; y1++) {
					g.setColor(model.calcColorVFromPreparedColor(calcFraction(height-4,y1,3)));
					g.drawLine(x+x1,y+y1,x+x1,y+y1);
				}
			}
		int x1 = Math.round(   model.getValueH() *(width -7))+3;
		int y1 = Math.round((1-model.getValueV())*(height-7))+3;
		g.setColor(isEnabled()?Color.BLACK:Color.DARK_GRAY);
		g.drawOval(x+x1-3, y+y1-3, 6, 6);
	}

	private float calcFraction(int minV, int v, int maxV) {
		return (v-minV)/(float)(maxV-minV);
	}
	
	private void userChangedValue(int x, int y) {
		float f; float fH; float fV;
		switch (type) {
		case HORIZONTAL:
			if (x<0) x=0;
			if (width<=x) x=width-1;
			model.setValue( f = calcFraction(0,x,width-1) );
			colorChangeListener.colorChanged( model.getColor(), f );
			break;
		case VERTICAL:
			if (y<0) y=0;
			if (height<=y) y=height-1;
			model.setValue( f = calcFraction(height-1,y,0) );
			colorChangeListener.colorChanged( model.getColor(), f );
			break;
		case DUAL:
			if (x<3) x=3;
			if (y<3) y=3;
			if (width -3<=x) x=width -4;
			if (height-3<=y) y=height-4;
			model.setValue( fH = calcFraction(3,x,width-4), fV = calcFraction(height-4,y,3) );
			colorChangeListener.colorChanged( model.getColor(), fH, fV );
			break;
		}
		repaint();
	}
	
	@Override public void mouseDragged (MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseMoved   (MouseEvent e) {}
	@Override public void mouseClicked (MouseEvent e) {}
	@Override public void mouseEntered (MouseEvent e) {}
	@Override public void mouseExited  (MouseEvent e) {}
	@Override public void mousePressed (MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }
	@Override public void mouseReleased(MouseEvent e) { if (isEnabled()) userChangedValue(e.getX(),e.getY()); }

    @Override public void setBounds(int x, int y, int width, int height) {
    	super.setBounds( x, y, width, height );
    	this.width = width; this.height = height;
    }
    @Override public void setBounds(Rectangle r) {
    	super.setBounds( r );
    	this.width = r.width; this.height = r.height;
    }
    @Override public void setSize(Dimension d) {
    	super.setSize( d );
    	this.width = d.width; this.height = d.height;
    }
    @Override public void setSize(int width, int height) {
    	super.setSize( width, height );
    	this.width =   width; this.height =   height;
    }

	public static interface ColorChangeListener {
		public void colorChanged( Color color, float f );
		public void colorChanged( Color color, float fH, float fV );
	}
	
	public static interface ColorSliderModel {
		public Color getColor();
		public Color calcColor(float f);
		public void prepareColorH(float f);
		public Color calcColorVFromPreparedColor(float f);
		public void setValue( float f );
		public void setValue( float fH,float fV );
		public float getValue();
		public float getValueH();
		public float getValueV();
	}
	
	public static enum SliderType {
		HORIZONTAL, VERTICAL, DUAL
	}
	
	private static class SimpleColorSliderModel implements ColorSliderModel {
		
		private float fraction;
		private Colorizer colorizer;

		public SimpleColorSliderModel(float fraction, Colorizer colorizer) {
			this.fraction = fraction;
			this.colorizer = colorizer;
		}

		@Override public void  setValue(float f) { fraction = f; }
		@Override public Color getColor() { return calcColor(fraction); }
		@Override public float getValue() { return fraction; }
		
		
		@Override public void  prepareColorH(float f) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public Color calcColorVFromPreparedColor(float f) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public void  setValue(float fH, float fV) { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider.");  }
		@Override public float getValueH() { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }
		@Override public float getValueV() { throw new UnsupportedOperationException("SimpleColorSliderModel can't be used for a dual slider."); }

		@Override
		public Color calcColor(float f) {
			return colorizer.calcColor(f);
		}
		
	}
	
	public static interface Colorizer {

		Color calcColor(float f);
		
	}
}
