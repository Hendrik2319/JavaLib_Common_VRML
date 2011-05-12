package net.schwarzbaer.java.lib.gui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Locale;

import javax.swing.JTextArea;

public class TextAreaPrintStream extends PrintStream {

	private JTextArea output;
	private Charset charset;

	private TextAreaPrintStream(File file, String charset) throws FileNotFoundException, UnsupportedEncodingException { super(file, charset); }
	private TextAreaPrintStream(File file                ) throws FileNotFoundException                               { super(file         ); }
	private TextAreaPrintStream(String fileName, String charset) throws FileNotFoundException, UnsupportedEncodingException { super(fileName, charset); }
	private TextAreaPrintStream(String fileName                ) throws FileNotFoundException                               { super(fileName         ); }
	private TextAreaPrintStream(OutputStream out, boolean autoFlush, String charset) throws UnsupportedEncodingException { super(out, autoFlush, charset); }
	private TextAreaPrintStream(OutputStream out, boolean autoFlush                ) { super(out, autoFlush); }
	private TextAreaPrintStream(OutputStream out                                   ) { super(out); }
	
	public TextAreaPrintStream( JTextArea output ) {
		super( System.out );
		this.output = output;
		this.charset = null;
	}
	public TextAreaPrintStream( JTextArea output, String charset ) throws UnsupportedEncodingException {
		super( System.out );
		this.output = output;
		if (charset==null)
			this.charset = null;
		else
		if (Charset.isSupported(charset))
			this.charset = Charset.forName(charset);
		else
			throw new UnsupportedEncodingException("Charset \""+charset+"\" is not supported.");
	}
	
	@Override public PrintStream append(CharSequence csq, int start, int end) { output.append(csq.subSequence(start, end).toString()); return this; }
	@Override public PrintStream append(CharSequence csq                    ) { output.append(csq                        .toString()); return this; }
	@Override public PrintStream append(char c) { output.append(""+c); return this; }
	@Override
	public void write(byte[] buf, int off, int len) {
		if (charset!=null)
			output.append(new String(buf,off,len,charset));
		else
			output.append(new String(buf,off,len));
	}
	@Override public void write(byte[] buf) throws IOException { write(buf, 0, buf.length); }
	@Override public void write(int b) { output.append(""+(char)b); }
	
	@Override public void print(String  x) { output.append(               x ); }
	@Override public void print(boolean x) { output.append(String.valueOf(x)); }
	@Override public void print(char    x) { output.append(String.valueOf(x)); }
	@Override public void print(char[]  x) { output.append(String.valueOf(x)); }
	@Override public void print(double  x) { output.append(String.valueOf(x)); }
	@Override public void print(float   x) { output.append(String.valueOf(x)); }
	@Override public void print(int     x) { output.append(String.valueOf(x)); }
	@Override public void print(long    x) { output.append(String.valueOf(x)); }
	@Override public void print(Object  x) { output.append(String.valueOf(x)); }
	
	@Override public void println() { output.append("\r\n"); }
	
	@Override public void println(String  x) { output.append(               x +"\r\n"); }
	@Override public void println(boolean x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(char    x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(char[]  x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(double  x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(float   x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(int     x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(long    x) { output.append(String.valueOf(x)+"\r\n"); }
	@Override public void println(Object  x) { output.append(String.valueOf(x)+"\r\n"); }
	
	@Override public PrintStream format(Locale l, String format, Object... args)  { print( String.format(l, format, args) ); return this; }
	@Override public PrintStream printf(Locale l, String format, Object... args)  { print( String.format(l, format, args) ); return this; }
	@Override public PrintStream format(String format, Object... args) { print( String.format(format, args) ); return this; }
	@Override public PrintStream printf(String format, Object... args) { print( String.format(format, args) ); return this; }
	
	@Override public boolean checkError() { return false; }
	@Override protected void clearError() {}
	@Override protected void setError() {}
	
	@Override public void close() {}
	@Override public void flush() {}
	
}
