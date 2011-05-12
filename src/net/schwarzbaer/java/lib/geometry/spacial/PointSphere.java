package net.schwarzbaer.java.lib.geometry.spacial;

import java.util.Vector;

public class PointSphere<Point extends ConstPoint3d> extends ConstSphere {
	
	public final Vector<Point> points;
	private final CreatePoint<Point> createPoint;

	public PointSphere(ConstPoint3d center, double radius, CreatePoint<Point> createPoint) {
		super(center, radius);
		this.createPoint = createPoint;
		points = new Vector<>();
	}
	public PointSphere(ConstPoint3d center, double radius, double pointDensity_perSqU, CreatePoint<Point> createPoint) {
		this(center, radius, createPoint);
		generatePointsPerDensity(pointDensity_perSqU);
	}

	public PointSphere(ConstPoint3d center, double radius, int nPoints, CreatePoint<Point> createPoint) {
		this(center, radius, createPoint);
		generatePoints(nPoints);
	}

	public void generatePointsPerDensity(double pointDensity_perSqU) {
		double A = 4*Math.PI*this.radius*this.radius;
		long n = Math.round( A*pointDensity_perSqU );
		generatePoints((int)n);
	}

	public void generatePoints(int nPoints) {
		points.clear();
		points.ensureCapacity(nPoints);
		for (int i=0; i<nPoints; i++) {
			double angle  = Math.random()*Math.PI*2;
			double height = (Math.random()*2-1)*this.radius;
			double rh = Math.sqrt(this.radius*this.radius-height*height);
			
			double x = center.x + Math.cos(angle)*rh;
			double y = center.y + Math.sin(angle)*rh;
			double z = center.z + height;
			
			points.add(createPoint.create(center, x, y, z));
		}
	}
	
	public interface CreatePoint<Point> {
		Point create(ConstPoint3d center, double x, double y, double z);
	}
}
