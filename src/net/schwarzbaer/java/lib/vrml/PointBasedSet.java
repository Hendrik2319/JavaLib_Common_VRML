package net.schwarzbaer.java.lib.vrml;

import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import net.schwarzbaer.java.lib.geometry.spacial.ConstPoint3d;

public abstract class PointBasedSet {
	
	protected final HashSet<String> pointSet;
	protected final Vector<String> points;
	protected final String pointCoordFormat;

	protected PointBasedSet(String pointCoordFormat, boolean optimizePointSet) {
		this.pointCoordFormat = pointCoordFormat;
		points = new Vector<>();
		pointSet = !optimizePointSet ? null : new HashSet<>();
	}
	
	public int addPoint(ConstPoint3d p) {
		return addPoint(p.x, p.y, p.z);
	}
	public int addPoint(double x, double y, double z) {
		String str = String.format(Locale.ENGLISH, pointCoordFormat+" "+pointCoordFormat+" "+pointCoordFormat, x,y,z);
		int index;
		if (pointSet!=null && pointSet.contains(str))
			index = points.indexOf(str);
		else {
			index = points.size();
			points.add(str);
			if (pointSet!=null) pointSet.add(str);
		}
		return index;
	}

}
