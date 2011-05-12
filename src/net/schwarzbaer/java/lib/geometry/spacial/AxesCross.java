package net.schwarzbaer.java.lib.geometry.spacial;

public class AxesCross {
	
	public final ConstPoint3d xAxis;
	public final ConstPoint3d yAxis;
	public final ConstPoint3d zAxis;

	public AxesCross(ConstPoint3d xAxis, ConstPoint3d yAxis, ConstPoint3d zAxis) {
		// Right Hand Side
		this.xAxis = xAxis;
		this.yAxis = yAxis;
		this.zAxis = zAxis;
	}
	
	public static AxesCross compute(ConstPoint3d normal) {
		if (normal==null) throw new IllegalArgumentException();
		if (normal.isOrigin()) throw new IllegalArgumentException();
		
		normal = normal.normalize();
		ConstPoint3d initial;
		if      (normal.x<normal.y && normal.x<normal.z) initial = new ConstPoint3d(1,0,0);
		else if (normal.y<normal.x && normal.y<normal.z) initial = new ConstPoint3d(0,1,0);
		else initial = new ConstPoint3d(0,0,1);
		
		ConstPoint3d axis1 = ConstPoint3d.computeCrossProdAB(normal, initial);
		axis1 = axis1.normalize();
		ConstPoint3d axis2 = ConstPoint3d.computeCrossProdAB(normal, axis1);
		axis2 = axis2.normalize();
		
		return new AxesCross(normal,axis1,axis2);
	}

	public ConstPoint3d toGlobal(ConstPoint3d p) {
		double x = p.x*xAxis.x + p.y*yAxis.x + p.z*zAxis.x;
		double y = p.x*xAxis.y + p.y*yAxis.y + p.z*zAxis.y;
		double z = p.x*xAxis.z + p.y*yAxis.z + p.z*zAxis.z;
		return new ConstPoint3d(x, y, z);
	}

	public ConstPoint3d toLocal(ConstPoint3d p) {
		double x = ConstPoint3d.computeDotProd(xAxis,p);
		double y = ConstPoint3d.computeDotProd(yAxis,p);
		double z = ConstPoint3d.computeDotProd(zAxis,p);
		//double x = xAxis.x*p.x + xAxis.y*p.y + xAxis.z*p.z;
		//double y = yAxis.x*p.x + yAxis.y*p.y + yAxis.z*p.z;
		//double z = zAxis.x*p.x + zAxis.y*p.y + zAxis.z*p.z;
		return new ConstPoint3d(x, y, z);
	}

}
