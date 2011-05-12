package net.schwarzbaer.java.lib.vrml;

import java.awt.Color;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.function.Consumer;

public class VrmlTools {
	
	public static void writeVRML(File file, Consumer<PrintWriter> writeContent) {
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			
			writeHeader(out);
			writeContent.accept(out);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}

	public static void writeHeader(PrintWriter out) { writeHeader(out, 0.6, 0.7, 0.8); }
	public static void writeHeader(PrintWriter out, Color sky) {
		out.println("#VRML V2.0 utf8");
		out.println();
		writeBackground(out, sky);
		out.println();
	}
	public static void writeHeader(PrintWriter out, double skyR, double skyG, double skyB) {
		out.println("#VRML V2.0 utf8");
		out.println();
		writeBackground(out, skyR, skyG, skyB);
		out.println();
	}

	public static void writeBackground(PrintWriter out, Color sky) {
		out.printf (Locale.ENGLISH, "Background { skyColor %s }%n", toString(sky));
	}

	public static void writeBackground(PrintWriter out, double skyR, double skyG, double skyB) {
		out.printf (Locale.ENGLISH, "Background { skyColor %1.3f %1.3f %1.3f }%n", skyR, skyG, skyB);
	}

	public static void writeDirectionalLight(PrintWriter out, double x, double y, double z, Color color) {
		out.printf (Locale.ENGLISH, "DirectionalLight { direction %1.3f %1.3f %1.3f color %s }%n", x,y,z, toString(color));
	}
	
	public static String toString(Color c) {
		return String.format(Locale.ENGLISH, "%1.3f %1.3f %1.3f", c.getRed()/255f, c.getGreen()/255f, c.getBlue()/255f);
	}
	
	public static void writeBox(PrintWriter out, String coordFormat, double sizeX, double sizeY, double sizeZ, Color diffuseColor, Color specularColor, Color emissiveColor) {
		out.println("Shape {");
		out.printf ("	appearance Appearance {%n");
		out.printf ("		material Material {%n");
		if (diffuseColor !=null) out.printf ("			diffuseColor %s%n", VrmlTools.toString(diffuseColor));
		if (specularColor!=null) out.printf ("			specularColor %s%n", VrmlTools.toString(specularColor));
		if (emissiveColor!=null) out.printf ("			emissiveColor %s%n", VrmlTools.toString(emissiveColor));
		out.printf ("		}%n");
		out.printf ("	}%n");
		out.printf (Locale.ENGLISH, "	geometry Box { size "+coordFormat+" "+coordFormat+" "+coordFormat+" }%n", sizeX, sizeY, sizeZ);
		out.println("}");
	}
}
