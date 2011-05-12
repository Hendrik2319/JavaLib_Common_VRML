package net.schwarzbaer.java.lib.image.alphachar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class AlphaCharIO {
	
	public static final String ALPHACHARFONT_EXTENSION = "AlphaCharFont";

	private static void Assert(boolean condition) {
		if (!condition) throw new IllegalStateException();
	}

	public static void test(HashMap<Character, Form[]> alphabet1, File file1, File file2) {
		writeAlphaCharToFile(file1, alphabet1, true);
		HashMap<Character, Form[]> alphabet2 = readAlphaCharFontFromFile(file1, null, true);
		writeAlphaCharToFile(file2, alphabet2, true);
	}

	public static void rewriteDefaultAlphaCharFont(File file) {
		HashMap<Character, Form[]> alphabet2 = AlphaCharIO.readDefaultAlphaCharFont(null, true);
		if (alphabet2==null)
			System.out.println("No \"default\" font.");
		else
			AlphaCharIO.writeAlphaCharToFile(file, alphabet2, true);
	}

	public static HashMap<Character,Form[]> readDefaultAlphaCharFont(Form.Factory formFactory, boolean verbose) {
		return readAlphaCharFont("default",formFactory,verbose);
	}
	public static HashMap<Character,Form[]> readAlphaCharFont(String fontName, Form.Factory formFactory, boolean verbose) {
		return readAlphaCharFontFromResource(fontName+"."+ALPHACHARFONT_EXTENSION,formFactory,verbose);
	}

	public static HashMap<Character,Form[]> readAlphaCharFontFromResource(String resPath, Form.Factory formFactory, boolean verbose) {
		if (verbose) System.out.printf("Read AlphaChar Font from resource path \"%s\" ...%n", resPath);
		InputStream stream = AlphaCharIO.class.getResourceAsStream(resPath);
		if (stream != null) {
			HashMap<Character, Form[]> alphabet = readAlphaCharFontFromStream(stream,formFactory);
			if (verbose) System.out.printf("... done%n");
			return alphabet;
		}
		if (verbose) System.err.printf("Can't find resource path \"%s\".%n", resPath);
		return null;
	}

	public static HashMap<Character,Form[]> readAlphaCharFontFromFile(File file, Form.Factory formFactory, boolean verbose) {
		if (verbose) System.out.printf("Read AlphaChar Font from file \"%s\" ...%n", file);
		try (FileInputStream stream = new FileInputStream(file)) {
			HashMap<Character, Form[]> alphabet = readAlphaCharFontFromStream(stream,formFactory);
			if (verbose) System.out.printf("... done%n");
			return alphabet;
		}
		catch (FileNotFoundException e) {
			if (verbose) System.err.printf("Can't find file \"%s\".%n", file);
			//e.printStackTrace();
		}
		catch (IOException e) { e.printStackTrace(); }
		return null;
	}

	public static HashMap<Character, Form[]> readAlphaCharFontFromStream(InputStream stream, Form.Factory formFactory) {
		HashMap<Character, Form[]> alphabet = new HashMap<Character,Form[]>();
		if (formFactory==null)
			formFactory = new Form.Factory() {
				@Override public Form.PolyLine createPolyLine(double[] values) { return new Form.PolyLine().setValues(values); }
				@Override public Form.Line     createLine    (double[] values) { return new Form.Line    ().setValues(values); }
				@Override public Form.Arc      createArc     (double[] values) { return new Form.Arc     ().setValues(values); }
			};
		
		try (BufferedReader in = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
			
			String line, value;
			Character ch = null;
			Vector<Form> forms = new Vector<>();
			
			while ( (line=in.readLine())!=null ) {
				if ( (value=getValue(line,"[AlphaChar '","']"))!=null ) {
					addTo(alphabet,ch,forms);
					Assert(value.length()==1);
					ch = value.charAt(0);
				}
				if ( (value=getValue(line,"PolyLine="))!=null ) forms.add(formFactory.createPolyLine(toArray(value)));
				if ( (value=getValue(line,"Line="    ))!=null ) forms.add(formFactory.createLine    (toArray(value)));
				if ( (value=getValue(line,"Arc="     ))!=null ) forms.add(formFactory.createArc     (toArray(value)));
				if (line.isEmpty()) { addTo(alphabet,ch,forms); ch = null; }
			}
			addTo(alphabet,ch,forms);
			
		}
		catch (IOException e) { e.printStackTrace(); }
		
		return alphabet;
	}
	
	private static double[] toArray(String str) {
		String[] valueStrs = str.split(";");
		double[] values = new double[valueStrs.length];
		for (int i=0; i<values.length; i++) {
			try { values[i] = Double.parseDouble(valueStrs[i]); }
			catch (NumberFormatException e) { values[i] = Double.NaN; }
			if (Double.isNaN(values[i])) {
				System.err.printf("Can't parse Double value (\"%s\") in String \"%s\" at position %d.%n", valueStrs[i], str, i);
				return null;
			}
		}
		return values;
	}

	private static void addTo(HashMap<Character, Form[]> alphabet, Character ch, Vector<Form> forms) {
		if (ch!=null && forms!=null && !forms.isEmpty()) {
			alphabet.put(ch, forms.toArray(new Form[forms.size()]));
			forms.clear();
		}
	}

	private static String getValue(String line, String prefix) { return getValue(line, prefix, null); }
	private static String getValue(String line, String prefix, String suffix) {
		if (prefix!=null) { if (line.startsWith(prefix)) line = line.substring(prefix.length()                ); else return null; }
		if (suffix!=null) { if (line.endsWith  (suffix)) line = line.substring(0,line.length()-suffix.length()); else return null; }
		return line;
	}
	
	public static void writeAlphaCharToFile(File file, HashMap<Character,Form[]> alphabet, boolean verbose) {
		if (verbose) System.out.printf("Write AlphaChar Font to file \"%s\" ...%n", file);
		
		try (PrintWriter out = new PrintWriter(new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {
			
			Vector<Character> keys = new Vector<>(alphabet.keySet());
			keys.sort(null);
			for (Character ch:keys) {
				out.printf("[AlphaChar '%s']%n", ch);
				Form[] forms = alphabet.get(ch);
				writeForms(out,forms);
				out.printf("%n");
			}
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		
		if (verbose) System.out.printf("... done%n");
	}

	private static void writeForms(PrintWriter out, Form[] forms) {
		for (Form form:forms) {
			double[] values = form.getValues();
			String name = form.getClass().getSimpleName();
			String valuesStr = String.join(";", Arrays.stream(values).mapToObj(d->Double.toString(d)).toArray(String[]::new));
			out.printf("%s=%s%n", name, valuesStr);
		}
	}
}
