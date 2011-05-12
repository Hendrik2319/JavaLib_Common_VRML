package net.schwarzbaer.java.lib.geometry.spacial;

public class ConstPoint3d {
	public final double x,y,z;

	public ConstPoint3d(double x, double y, double z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	@Override
	public String toString() {
		return String.format("ConstPoint3d [x=%s, y=%s, z=%s]", x, y, z);
	}

	public double getDistance       (ConstPoint3d p) { return getDistance       (p.x, p.y, p.z); }
	public double getSquaredDistance(ConstPoint3d p) { return getSquaredDistance(p.x, p.y, p.z); }
	public double getDistance       (double x, double y, double z) { return Math.sqrt( getSquaredDistance(x,y,z) ); }
	public double getSquaredDistance(double x, double y, double z) { return (this.x-x)*(this.x-x) + (this.y-y)*(this.y-y) + (this.z-z)*(this.z-z); }

	public ConstPoint3d add(ConstPoint3d p) { return add(p.x,p.y,p.z); }
	public ConstPoint3d sub(ConstPoint3d p) { return sub(p.x,p.y,p.z); }
	public ConstPoint3d add(double x, double y, double z) { return new ConstPoint3d(this.x+x, this.y+y, this.z+z); }
	public ConstPoint3d sub(double x, double y, double z) { return new ConstPoint3d(this.x-x, this.y-y, this.z-z); }
	public ConstPoint3d mul(double f) { return new ConstPoint3d(x*f, y*f, z*f); }

	public ConstPoint3d normalize() {
		if (isOrigin()) return this;
		return mul(1/getDistance(0,0,0));
	}

	public boolean isOrigin() {
		return x==0 && y==0 && z==0;
	}
	
	public static ConstPoint3d computeCrossProd3P(ConstPoint3d center, ConstPoint3d p1, ConstPoint3d p2) {
		ConstPoint3d a = p1.sub(center);
		ConstPoint3d b = p2.sub(center);
		return computeCrossProdAB(a, b);
	}

	public static ConstPoint3d computeCrossProdAB(ConstPoint3d a, ConstPoint3d b) {
		return new ConstPoint3d(
			a.y*b.z - a.z*b.y,
			a.z*b.x - a.x*b.z,
			a.x*b.y - a.y*b.x
		);
	}

	public static double computeDotProd(ConstPoint3d p1, ConstPoint3d p2) {
		return p1.x*p2.x + p1.y*p2.y + p1.z*p2.z;
	}

	public static ConstPoint3d computePointOnCircle(double angle, double radius, ConstPoint3d center, ConstPoint3d axis1, ConstPoint3d axis2) {
		ConstPoint3d p = center
				.add(axis1.mul(radius*Math.cos(angle)))
				.add(axis2.mul(radius*Math.sin(angle)));
		return p;
	}
}
