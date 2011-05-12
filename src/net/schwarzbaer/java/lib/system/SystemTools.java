package net.schwarzbaer.java.lib.system;

import java.io.File;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Comparator;
import java.util.Properties;
import java.util.Vector;


public final class SystemTools {
	
	public static File getRelativeFile( File hostFile, String filename ) {
		File newFile = new File(filename);
		if (!newFile.isAbsolute()) {
			File folder = hostFile.getParentFile();
			if (folder==null) folder = hostFile.getAbsoluteFile().getParentFile();
			newFile = new File(folder,filename);
		}
		return newFile;
	}
	public static String getRelativePath( File hostFile, File file ) {
		String parentPath = file.getParent();
		if (!parentPath.endsWith(File.separator)) parentPath = parentPath+File.separator;
		String path = file.getPath();
		if (path.startsWith(parentPath)) path = path.substring(parentPath.length());
		return path;
	}
	
	public static void listSystemPropertiesSorted(PrintStream out) {
		Properties properties = java.lang.System.getProperties();
		Vector<Object> keySet = new Vector<Object>(properties.keySet());
		Collections.sort(keySet,new Comparator<Object>(){
			@Override public int compare(Object o1, Object o2) { return o1.toString().compareTo(o2.toString()); }
		});
		for (int i=0; i<keySet.size(); i++) {
			out.println(String.format("%s=%s", objToString(keySet.get(i)),replace(objToString(properties.get(keySet.get(i))))));
		}
	}

	private static String replace(String string) {
		for (int i=0; i<32; i++)
			string = string.replace(""+(char)i, String.format("%%%02X", i));
		return string;
	}

	public static String objToString(Object object) {
		return (object==null?"<null>":object.toString());
	}

	public static void listClassInfo(Class<?> cl, boolean withSuperClasses, boolean withInterfaces, boolean withConstructors, boolean withFields, boolean withMethods) {
		System.out.printf("info of class [%s]\r\n",cl);
		if (cl==null) return;
		System.out.printf("\tName: [%s]\r\n",cl.getName());
		System.out.printf("\tSimpleName: [%s]\r\n",cl.getSimpleName());
		System.out.printf("\tCanonicalName: [%s]\r\n",cl.getCanonicalName());
		System.out.printf("\tPackage: [%s]\r\n",cl.getPackage());
		System.out.printf("\tAnnotations: %s\r\n",arrayToString("\t\t", cl.getAnnotations()));
		System.out.printf("\tSigners: %s\r\n",arrayToString("\t\t", cl.getSigners()));
		if (withSuperClasses) printAsSuperclass("\t",cl.getSuperclass());
		if (withInterfaces  ) System.out.printf("\tInterfaces: %s\r\n",arrayToString("\t\t", cl.getInterfaces()));
		if (withConstructors) System.out.printf("\tConstructors: %s\r\n",arrayToString("\t\t", cl.getConstructors()));
		if (withFields      ) System.out.printf("\tFields: %s\r\n",arrayToString("\t\t", cl.getFields()));
		if (withMethods     ) System.out.printf("\tMethods: %s\r\n",arrayToString("\t\t", cl.getMethods()));
	}

	private static void printAsSuperclass(String indention, Class<?> superclass) {
		if (superclass!=null) {
			System.out.printf("%swith superclass \"%s\"\r\n",indention,superclass);
			printAsSuperclass(indention+"\t", superclass.getSuperclass());
		}
		
	}

	private static String arrayToString(String indention, Object[] objArray) {
		if (objArray==null) return "<null>";
		return "<length:"+objArray.length+">"+(objArray.length==0?"[]":arrayToString_printList(indention,objArray));
	}

	private static String arrayToString_printList(String indention, Object[] objArray) {
		StringBuilder sb = new StringBuilder();
		for (int i=0; i<objArray.length; i++) {
			if (objArray[i]==null)
				sb.append("\r\n").append(indention).append("<null>");
			else
				sb.append("\r\n").append(indention).append("[").append(objArray[i].toString()).append("]");
		}
		return sb.toString();
	}

}
