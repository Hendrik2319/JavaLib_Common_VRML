package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Vector;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class ValueListOutput extends Vector<ValueListOutput.Entry> {
	private static final long serialVersionUID = -5898390765518030500L;
	
	private Style nextEntryLabelStyle = null;
	private Style nextEntryValueStyle = null;
	private Style labelStyle = null;
	private Style valueStyle = null;

	public void add(int indentLevel, String label, int     value) { add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, long    value) { add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, float   value) { add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, double  value) { add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, boolean value) { add(indentLevel, label, "%s", value); }
	public void add(int indentLevel, String label, Integer value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Integer"); else add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, Long    value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Long"   ); else add(indentLevel, label, "%d", value); }
	public void add(int indentLevel, String label, Float   value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Float"  ); else add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, Double  value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Double" ); else add(indentLevel, label, "%f", value); }
	public void add(int indentLevel, String label, Boolean value) { if (value==null) add(indentLevel, label, "<null> (%s)", "Boolean"); else add(indentLevel, label, "%s", value); }
	public void add(int indentLevel, String label, String  value) { if (value==null) add(indentLevel, label, "<null> (%s)", "String" ); else add(indentLevel, label, "\"%s\"", value); }
	public void add(int indentLevel, String label, boolean value, String trueStr, String falseStr) {                                                                          add(indentLevel, label, "%s", value ? trueStr : falseStr); }
	public void add(int indentLevel, String label, Boolean value, String trueStr, String falseStr) { if (value==null) add(indentLevel, label, "<null> (%s)", "Boolean"); else add(indentLevel, label, "%s", value ? trueStr : falseStr); }
	
	public enum StyleTarget { Label, Value, CompleteLine }
	public void setStyle(Style style, StyleTarget target, boolean forNextEntryOnly) {
		if (target==StyleTarget.CompleteLine || target==StyleTarget.Label) {
			if (forNextEntryOnly)
				nextEntryLabelStyle = style;
			else
				labelStyle = style;
		}
		if (target==StyleTarget.CompleteLine || target==StyleTarget.Value) {
			if (forNextEntryOnly)
				nextEntryValueStyle = style;
			else
				valueStyle = style;
		}
	}
	
	public void addEmptyLine() { add(null); }
	
	public void add(int indentLevel, String label, String format, Object... args) {
		Style labelStyle, valueStyle;
		if (nextEntryLabelStyle==null) labelStyle = this.labelStyle; else { labelStyle = nextEntryLabelStyle; nextEntryLabelStyle = null; }
		if (nextEntryValueStyle==null) valueStyle = this.valueStyle; else { valueStyle = nextEntryValueStyle; nextEntryValueStyle = null; }
		add(new Entry(indentLevel, labelStyle, label, valueStyle, format, args));
	}
	public void add(int indentLevel, String label) {
		Style labelStyle;
		if (nextEntryLabelStyle==null) labelStyle = this.labelStyle; else { labelStyle = nextEntryLabelStyle; nextEntryLabelStyle = null; }
		nextEntryValueStyle = null;
		add(new Entry(indentLevel, labelStyle, label));
	}

	public String generateOutput() {
		return generateOutput("");
	}
	
	public String generateOutput(String baseIndent) {
		StringBuilderOutput out = new StringBuilderOutput();
		generateOutput(baseIndent, out);
		return out.toString();
	}
	
	public void generateOutput(String baseIndent, StyledDocument doc, String styleNamesPrefix) {
		generateOutput(baseIndent, new StyledDocumentOutput(doc, styleNamesPrefix, null));
	}
	
	public void generateOutput(String baseIndent, StyledDocument doc, String styleNamesPrefix, int fontSize) {
		generateOutput(baseIndent, new StyledDocumentOutput(doc, styleNamesPrefix, fontSize));
	}
	
	public void generateOutput(String baseIndent, OutputTarget out) {
		HashMap<Integer,Integer> labelLengths = new HashMap<>();
		HashMap<Integer,String> indents = new HashMap<>();
		
		for (Entry entry:this)
			if (entry!=null)
			{
				String indent = indents.get(entry.indentLevel);
				if (indent==null) // first entry at this indentLevel
				{
					String str = "";
					for (int i=0; i<entry.indentLevel; i++)
						str += "    ";
					indents.put(entry.indentLevel, str);
				}
				
				if (!entry.valueStr.isEmpty())
				{
					Integer maxLength = labelLengths.get(entry.indentLevel);
					if (maxLength==null) maxLength=0;
					maxLength = Math.max(entry.label.length(), maxLength);
					labelLengths.put(entry.indentLevel,maxLength);
				}
			}
		
		out.prepareOutput(this);
		for (Entry entry:this)
			if (entry == null)
				out.appendEmptyLine();
			else {
				String spacer = entry.valueStr.isEmpty() ? "" : entry.label.isEmpty() ? "  " : ": ";
				String indent = indents.get(entry.indentLevel);
				Integer labelLength_ = labelLengths.get(entry.indentLevel);
				int labelLength = labelLength_==null ? 0 : labelLength_.intValue();
				String labelFormat = labelLength==0 ? "%s" : "%-"+labelLength+"s";
				out.appendLine(entry.labelStyle, String.format("%s%s"+labelFormat+"%s", baseIndent, indent, entry.label, spacer), entry.valueStyle, entry.valueStr);
			}
		
	}
	
	interface OutputTarget {
		void appendEmptyLine();
		void prepareOutput(Vector<Entry> entries);
		void appendLine(Style prefixStyle, String prefix, Style valueStyle, String valueStr);
	}
	
	static class StringBuilderOutput implements OutputTarget {
		private final StringBuilder sb;
		StringBuilderOutput() {
			this.sb = new StringBuilder();;
		}
		@Override public void prepareOutput(Vector<Entry> entries) {}
		@Override public void appendEmptyLine() {
			sb.append(String.format("%n"));
		}
		@Override public void appendLine(Style prefixStyle, String prefix, Style valueStyle, String valueStr) {
			sb.append(String.format("%s%s%n", prefix, valueStr));
		}
		@Override public String toString() {
			return sb.toString();
		}
	}
	
	static class StyledDocumentOutput implements OutputTarget {

		private final StyledDocument doc;
		private final String styleNamesPrefix;
		private javax.swing.text.Style mainStyle;
		private HashMap<Style,javax.swing.text.Style> subStyles;
		private final Integer fontSize;

		public StyledDocumentOutput(StyledDocument doc, String styleNamesPrefix, Integer fontSize) {
			this.doc = doc;
			this.styleNamesPrefix = styleNamesPrefix;
			this.fontSize = fontSize;
			mainStyle = null;
			subStyles = null;
		}

		@Override public void prepareOutput(Vector<Entry> entries) {
			HashSet<Style> styles = new HashSet<>();
			for (Entry entry : entries) {
				if (entry.labelStyle!=null) styles.add(entry.labelStyle);
				if (entry.valueStyle!=null) styles.add(entry.valueStyle);
			}
			
			mainStyle = doc.addStyle(styleNamesPrefix+".Main", null);
			StyleConstants.setFontFamily(mainStyle, "Monospaced");
			if (fontSize!=null) StyleConstants.setFontSize(mainStyle, fontSize);
			subStyles = new HashMap<>();
			
			Vector<Style> list = new Vector<>(styles);
			for (int i=0; i<list.size(); i++) {
				Style style = list.get(i);
				String styleName = String.format("%s.SubStyle.%s", styleNamesPrefix, style.getID());
				//System.out.printf("Add Style \"%s\"%n", styleName);
				javax.swing.text.Style subStyle = doc.addStyle(styleName, mainStyle);
				style.setValuesTo(subStyle);
				subStyles.put(style, subStyle);
			}
		}

		@Override public void appendEmptyLine() {
			append(String.format("%n"), mainStyle);
		}

		@Override public void appendLine(Style prefixStyle, String prefix, Style valueStyle, String valueStr) {
			valueStr = String.format("%s%n", valueStr);
			append(  prefix, prefixStyle);
			append(valueStr,  valueStyle);
		}

		private void append(String text, Style style) {
			javax.swing.text.Style subStyle = subStyles.get(style);
			append(text,  subStyle==null ? mainStyle :  subStyle);
		}
		
		private void append(String text, javax.swing.text.Style style) {
			try {
				doc.insertString(doc.getLength(), text, style); }
			catch (BadLocationException e) {
				System.err.printf("[ValueListOutput.StyledDocumentOutput] BadLocationException while inserting Strings into StyledDocument: %s%n", e.getMessage());
				//e.printStackTrace();
			}
		}
		
	}
	
	public static class Style {
		public static final Style BOLD   = new Style(true, false);
		public static final Style ITALIC = new Style(false, true);
		
		public final Color color;
		public final boolean isBold;
		public final boolean isItalic;
		
		public Style(Color color) {
			this(color, false, false);
		}
		public Style(boolean isBold, boolean isItalic) {
			this(null, isBold, isItalic);
		}
		public Style(Color color, boolean isBold, boolean isItalic) {
			this.color = color;
			this.isBold = isBold;
			this.isItalic = isItalic;
		}
		
		public String getID() {
			return String.format("%s:%s:%s", color==null ? "--------" : String.format("%08X", color.getRGB()), isBold ? "B" : "-", isItalic ? "I" : "-");
		}
		
		public void setValuesTo(javax.swing.text.Style subStyle) {
			StyleConstants.setBold  (subStyle, isBold);
			StyleConstants.setItalic(subStyle, isItalic);
			if (color!=null) StyleConstants.setForeground(subStyle, color);
		}
		
		@Override
		public int hashCode() {
			int hashCode = 0;
			if (isBold  ) hashCode ^= 0xF0F0F0F0;
			if (isItalic) hashCode ^= 0x0F0F0F0F;
			if (color!=null) hashCode ^= color.getRGB();
			return hashCode;
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Style)) return false;
			Style other = (Style) obj;
			if (other.isBold!=this.isBold) return false;
			if (other.isItalic!=this.isItalic) return false;
			if (other.color==null && this.color==null) return true;
			if (other.color==null || this.color==null) return false;
			return other.color.getRGB()==this.color.getRGB();
		}
		
	}
	

	static class Entry {
		final int indentLevel;
		final String label;
		final String valueStr;
		final Style valueStyle;
		final Style labelStyle;
		
		Entry(int indentLevel, Style labelStyle, String label, Style valueStyle, String format, Object[] args) {
			this.indentLevel = indentLevel;
			this.labelStyle = labelStyle;
			this.label = label==null ? "" : label.trim();
			this.valueStyle = valueStyle;
			this.valueStr = String.format(Locale.ENGLISH, format, args);
		}
		Entry(int indentLevel, Style labelStyle, String label) {
			this(indentLevel, labelStyle, label, null, "", new Object[0]);
		}
	}
	
}