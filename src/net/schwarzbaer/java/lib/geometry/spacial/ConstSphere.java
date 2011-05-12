package net.schwarzbaer.java.lib.geometry.spacial;

public class ConstSphere {
	public final ConstPoint3d center;
	public final double radius;
	
	public ConstSphere(ConstPoint3d center, double radius) {
		this.center = center;
		this.radius = radius;
	}
	
	public boolean isInside(ConstPoint3d p) {
		return radius*radius > center.getSquaredDistance(p);
	}

}
