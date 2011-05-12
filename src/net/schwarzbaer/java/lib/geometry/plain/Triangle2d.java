package net.schwarzbaer.java.lib.geometry.plain;

public class Triangle2d {
	
	public final ConstPoint2d p1;
	public final ConstPoint2d p2;
	public final ConstPoint2d p3;

	public Triangle2d(ConstPoint2d p1, ConstPoint2d p2, ConstPoint2d p3) {
		this.p1 = p1;
		this.p2 = p2;
		this.p3 = p3;
	}

	public Triangle2d(Triangle2d triangle) {
		this(triangle.p1, triangle.p2, triangle.p3);
	}

	public Triangle2d getOutlineTriangle(double outOffset12, double outOffset23, double outOffset13) {
		
		double sin_a1 = Math.abs( ConstPoint2d.getSinOfAngle(p1, p2, p3) );
		double sin_a2 = Math.abs( ConstPoint2d.getSinOfAngle(p2, p1, p3) );
		double h1 = p1.getDistance(p2) * sin_a2;
		double h2 = p2.getDistance(p1) * sin_a1;
		double h3 = p3.getDistance(p1) * sin_a1;
		
		if (h1==0) return null;
		if (h2==0) return null;
		if (h3==0) return null;
		
		double f1 = (-outOffset23)/h1;
		double f2 = (-outOffset13)/h2;
		double f3 = (-outOffset12)/h3;
		
		ConstPoint2d out3 = computeInnerPoint(f1, f2, null);
		ConstPoint2d out2 = computeInnerPoint(f1, null, f3);
		ConstPoint2d out1 = computeInnerPoint(null, f2, f3);
		
		return new Triangle2d(out1, out2, out3);
	}

	public ConstPoint2d computeInnerPoint(Double f1, Double f2, Double f3) {
		if (f1==null) {
			if (f2==null || f3==null) throw new IllegalArgumentException();
			f1 = 1-f2-f3;
		}
		if (f2==null) {
			if (f1==null || f3==null) throw new IllegalArgumentException();
			f2 = 1-f1-f3;
		}
		if (f3==null) {
			if (f2==null || f1==null) throw new IllegalArgumentException();
			f3 = 1-f2-f1;
		}
		
		double x = p1.x*f1 + p2.x*f2 + p3.x*f3;
		double y = p1.y*f1 + p2.y*f2 + p3.y*f3;
		
		return new ConstPoint2d(x, y);
	}

}
