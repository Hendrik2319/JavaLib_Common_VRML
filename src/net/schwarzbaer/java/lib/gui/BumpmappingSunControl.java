package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.border.Border;
import javax.swing.event.MouseInputListener;

public class BumpmappingSunControl extends JComponent implements MouseInputListener {
	private static final long serialVersionUID = -516958122726918986L;
	
	private int width;
	private int height;
	
	private Vector<ValueChangeListener> vcl;
	private Vector<ActionListener> al;

	private double alpha;
	private double beta;
	private boolean isAdjusting = false;

	private int xCenter=0;
	private int yCenter=0;
	private double radius=0;

	private String actionCommand = null;
	
	public BumpmappingSunControl(double x, double y, double z) {
        width = -1;
        height = -1;
		alpha = Math.atan2(y,x);
		beta = Math.atan2(z,Math.sqrt(x*x+y*y));
		
		vcl = new Vector<>();
		al  = new Vector<>();
		
		addMouseListener(this);
		addMouseMotionListener(this);
	}
	
	public boolean isAdjusting() {
		return isAdjusting;
	}
	public void getValues(ValueChangeListener out) {
		double dx = Math.cos(alpha)*Math.cos(beta);
		double dy = Math.sin(alpha)*Math.cos(beta);
		double dz = Math.sin(beta);
		out.valueChanged(dx,dy,dz);
	}
	
	public void setActionCommand(String actionCommand) {
		this.actionCommand = actionCommand;
	}

	private void fireValueChangeEvent() {
		double dx = Math.cos(alpha)*Math.cos(beta);
		double dy = Math.sin(alpha)*Math.cos(beta);
		double dz = Math.sin(beta);
		for (ValueChangeListener l:vcl) l.valueChanged(dx,dy,dz);
		
		ActionEvent e = new ActionEvent(this,ActionEvent.ACTION_PERFORMED,actionCommand);
		for (ActionListener l:al) l.actionPerformed(e);;
	}

	public interface ValueChangeListener {
		public void valueChanged(double x, double y, double z);
	}
	public void    addValueChangeListener(ValueChangeListener vcl) { this.vcl.   add(vcl); }
	public void removeValueChangeListener(ValueChangeListener vcl) { this.vcl.remove(vcl); }
	public void    addActionListener(ActionListener al) { this.al.   add(al); }
	public void removeActionListener(ActionListener al) { this.al.remove(al); }
	
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
			canvasWidth  -= borderInsets.left + borderInsets.right ;
			canvasHeight -= borderInsets.top  + borderInsets.bottom;
		}
        paintCanvas( g, x,y, canvasWidth,canvasHeight );
    }

	private void paintCanvas(Graphics g, int xOffset, int yOffset, int width, int height) {
		Graphics2D g2 = null;
		if (g instanceof Graphics2D) {
			g2 = (Graphics2D) g;
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		}
		
		g.setColor(Color.WHITE);
		g.fillRect(xOffset, yOffset, width, height);
		g.setColor(Color.GRAY);
		g.drawRect(xOffset, yOffset, width-1, height-1);
		
		xCenter = xOffset+width/2;
		yCenter = yOffset+height/2;
		radius = Math.min(width, height)/2;
		double ticklength = radius*0.1;
		radius *= 0.9;
		int i = (int)(ticklength/2);
		
		g.setColor(Color.LIGHT_GRAY);
		g.drawOval(xCenter-(int)(radius  ), yCenter-(int)(radius  ), 2*(int)radius, 2*(int)radius);
		g.drawOval(xCenter-(int)(radius/2), yCenter-(int)(radius/2),   (int)radius,   (int)radius);
		g.drawLine(xCenter                -i, yCenter, xCenter                +i, yCenter);
		g.drawLine(xCenter-(int)(radius/2)-i, yCenter, xCenter-(int)(radius/2)+i, yCenter);
		g.drawLine(xCenter-(int)(radius  )-i, yCenter, xCenter-(int)(radius  )+i, yCenter);
		g.drawLine(xCenter+(int)(radius/2)-i, yCenter, xCenter+(int)(radius/2)+i, yCenter);
		g.drawLine(xCenter+(int)(radius  )-i, yCenter, xCenter+(int)(radius  )+i, yCenter);
		g.drawLine(xCenter, yCenter                -i, xCenter, yCenter                +i);
		g.drawLine(xCenter, yCenter-(int)(radius/2)-i, xCenter, yCenter-(int)(radius/2)+i);
		g.drawLine(xCenter, yCenter-(int)(radius  )-i, xCenter, yCenter-(int)(radius  )+i);
		g.drawLine(xCenter, yCenter+(int)(radius/2)-i, xCenter, yCenter+(int)(radius/2)+i);
		g.drawLine(xCenter, yCenter+(int)(radius  )-i, xCenter, yCenter+(int)(radius  )+i);
		
		int dx = (int) (Math.cos(alpha)*radius*(1-beta/(Math.PI/2)));
		int dy = (int) (Math.sin(alpha)*radius*(1-beta/(Math.PI/2)));
		g.setColor(Color.BLACK);
		g.drawLine(xCenter, yCenter, xCenter+dx, yCenter+dy);
	}
	
	private void setPoint(int x, int y) {
		int dx = x-xCenter;
		int dy = y-yCenter;
		beta = (1-Math.min(1,Math.sqrt(dx*dx+dy*dy)/radius))*Math.PI/2;
		alpha = Math.atan2(dy,dx);
		//System.out.printf(Locale.ENGLISH,"setPoint(%3d,%3d) -> alpha=%2.5f, beta=%2.5f%n",x,y,alpha,beta);
		repaint();
		fireValueChangeEvent();
	}
	
	@Override public void mouseClicked (MouseEvent e) {}
	@Override public void mousePressed (MouseEvent e) { setPoint(e.getX(),e.getY()); }
	@Override public void mouseReleased(MouseEvent e) { isAdjusting=false; setPoint(e.getX(),e.getY()); }
	@Override public void mouseEntered (MouseEvent e) {}
	@Override public void mouseExited  (MouseEvent e) {}
	
	@Override public void mouseDragged(MouseEvent e) { isAdjusting=true; setPoint(e.getX(),e.getY()); }
	@Override public void mouseMoved  (MouseEvent e) {}

}
