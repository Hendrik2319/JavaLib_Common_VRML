package net.schwarzbaer.java.lib.image.alphachar;

import java.awt.geom.Point2D;
import java.util.Locale;
import java.util.Vector;

import net.schwarzbaer.java.lib.image.alphachar.Form.PolyLine.Point;

public interface Form {
	
	static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}
	
	double[] getValues();
	Form setValues(double[] values);
	
	public interface Factory {
		PolyLine createPolyLine(double[] values);
		Line     createLine    (double[] values);
		Arc      createArc     (double[] values);
	}
	
	public static class PolyLine implements Form {
		
		public final Vector<Point> points;
		public PolyLine() { points = new Vector<>(); }
		public PolyLine(double xStart, double yStart) { this(); points.add(new Point(xStart,yStart)); }
		
		public Point get(int i) { return points.get(i); }
		public int size() { return points.size(); }
		public double getFirstX() { return points.get(0).x; }
		public double getFirstY() { return points.get(0).y; }
		
		public PolyLine add(double x, double y) {
			points.add(new Point(x,y));
			return this;
		}
		
		@Override public double[] getValues() {
			double[] values = new double[points.size()*2];
			for (int i=0; i<points.size(); i++) {
				Point p = points.get(i);
				values[i*2+0] = p.x;
				values[i*2+1] = p.y;
			}
			return values;
		}
		@Override public PolyLine setValues(double[] values) {
			Assert(values!=null);
			Assert((values.length&1)==0);
			points.clear();
			for (int i=0; i<values.length; i+=2)
				points.add(new Point(values[i], values[i+1]));
			return this;
		}

		public static class Point {
			public double x,y;
			public Point(double x, double y) { set(x,y); }
			public void set(double x, double y) { this.x = x; this.y = y; }
			public void set(Point2D.Float p) { this.x = p.x; this.y = p.y; }
		}
	}
	
	public static class Line implements Form {
		public double x1, y1, x2, y2;
		public Line() { this(0,0,0,0); }
		public Line(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		@Override public double[] getValues() {
			return new double[] { x1, y1, x2, y2 };
		}
		@Override public Line setValues(double[] values) {
			Assert(values.length==4);
			this.x1 = values[0];
			this.y1 = values[1];
			this.x2 = values[2];
			this.y2 = values[3];
			return this;
		}
		
		public Point computePoint(double f) {
			return new Point( x1*(1-f)+x2*f, y1*(1-f)+y2*f );
		}
		public LineDistance getDistance(double x, double y) {
			return new LineDistance(x,y);
		}
		
		public class LineDistance {
			public final double r;
			public final double f;
			LineDistance(double x, double y) {
				double length = Math.sqrt((x2-x1)*(x2-x1)+(y2-y1)*(y2-y1));
				f =          ((x2-x1)*(x-x1)+(y2-y1)*(y-y1))/length/length; // cos(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|� -> (x1,y1) ..f.. (x2,y2)
				r = Math.abs(((x2-x1)*(y-y1)-(y2-y1)*(x-x1))/length      ); // sin(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  sin(a)*|x-x1,y-y1|  =  r
			}
			@Override
			public String toString() {
				return String.format(Locale.ENGLISH, "(r:%1.3f,f:%1.3f)", r, f);
			}
			
		}
		
	}
	
	public static class Arc implements Form {
		public double xC,yC,r,aStart,aEnd;
		public Arc() { this(0,0,0,0,0); }
		public Arc(double xC, double yC, double r, double aStart, double aEnd) {
			this.xC     = xC;
			this.yC     = yC;
			this.r      = r;
			this.aStart = aStart;
			this.aEnd   = aEnd;
		}
		@Override public double[] getValues() {
			return new double[] { xC,yC,r,aStart,aEnd };
		}
		@Override public Arc setValues(double[] values) {
			Assert(values.length==5);
			this.xC     = values[0];
			this.yC     = values[1];
			this.r      = values[2];
			this.aStart = values[3];
			this.aEnd   = values[4];
			return this;
		}
	}
}
