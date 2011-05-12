package net.schwarzbaer.java.lib.vrml;

import java.awt.Color;
import java.io.PrintWriter;
import java.util.Vector;

import net.schwarzbaer.java.lib.geometry.spacial.AxesCross;
import net.schwarzbaer.java.lib.geometry.spacial.ConstPoint3d;

public class IndexedFaceSet extends PointBasedSet {
	
	private final Vector<Integer> faces;
	private final Vector<Color> colors;
	private final boolean colorPerFace;

	public IndexedFaceSet(String pointCoordFormat, boolean optimizePointSet, boolean colorPerFace) {
		super(pointCoordFormat, optimizePointSet);
		this.colorPerFace = colorPerFace;
		faces = new Vector<>();
		colors = colorPerFace ? new Vector<>() : null;
	}

	public void addFace(ConstPoint3d... points) {
		if (colorPerFace) throw new UnsupportedOperationException();
		addFace(null, points);
	}

	public void addFace(Color faceColor, ConstPoint3d... points) {
		if (colorPerFace && faceColor==null) throw new IllegalArgumentException();
		for (ConstPoint3d p : points) {
			int pIndex = addPoint(p);
			faces.add(pIndex);
		}
		faces.add(-1);
		if (colorPerFace) colors.add(faceColor);
	}
	public void addFace(int... indexes) {
		if (colorPerFace) throw new UnsupportedOperationException();
		addFace(null, indexes);
	}
	public void addFace(Color faceColor, int... indexes) {
		if (colorPerFace && faceColor==null) throw new IllegalArgumentException();
		for (int i : indexes)
			faces.add(i);
		faces.add(-1);
		if (colorPerFace) colors.add(faceColor);
	}

	public void addPointFace(ConstPoint3d p, ConstPoint3d normal, double diameter, int nPoints) {
		if (colorPerFace) throw new UnsupportedOperationException();
		addPointFace(null, p, normal, diameter, nPoints);
	}
	
	public void addPointFace(Color color, ConstPoint3d p, ConstPoint3d normal, double diameter, int nPoints) {
		if (colorPerFace && color==null) throw new IllegalArgumentException();
		
		AxesCross axes = AxesCross.compute(normal);
		ConstPoint3d[] facePoints = new ConstPoint3d[nPoints];
		for (int i=0; i<nPoints; i++) {
			double angle = 2*Math.PI/nPoints * i;
			facePoints[i] = ConstPoint3d.computePointOnCircle(angle, diameter/2, p, axes.yAxis, axes.zAxis);
		}
		
		addFace(color,facePoints);
	}
	
	public void writeToVRML(PrintWriter out, boolean isSolid, Color diffuseColor, Color specularColor, Color emissiveColor) {
		Iterable<String> coordIndexesIterable = ()->faces.stream().map(i->i.toString()).iterator();
		out.println("Shape {");
		out.printf ("	appearance Appearance {%n");
		out.printf ("		material Material {%n");
		if (diffuseColor !=null) out.printf ("			diffuseColor %s%n", VrmlTools.toString(diffuseColor));
		if (specularColor!=null) out.printf ("			specularColor %s%n", VrmlTools.toString(specularColor));
		if (emissiveColor!=null) out.printf ("			emissiveColor %s%n", VrmlTools.toString(emissiveColor));
		out.printf ("		}%n");
		out.printf ("	}%n");
		out.printf ("	geometry IndexedFaceSet {%n");
		out.printf ("		colorPerVertex FALSE%n");
		out.printf ("		solid %s%n", isSolid ? "TRUE" : "FALSE");
		out.printf ("		coord Coordinate { point [ %s ] }%n", String.join(", ", points));
		out.printf ("		coordIndex [ %s ]%n", String.join(" ", coordIndexesIterable));
		if (colorPerFace) {
			Iterable<String> colorIterable = ()->colors.stream().map(c->VrmlTools.toString(c)).iterator();
			out.printf("		color Color { color [ %s ] }%n", String.join(", ", colorIterable));
		}
		out.println("	}");
		out.println("}");
	}
}
