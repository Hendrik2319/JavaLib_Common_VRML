package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.util.HashMap;

import javax.swing.text.BadLocationException;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

public class StyledDocumentInterface {

	private final StyledDocument doc;
	private final String styleNamesPrefix;
	private final javax.swing.text.Style mainStyle;
	private HashMap<Style,javax.swing.text.Style> subStyles;

	public StyledDocumentInterface(StyledDocument doc, String styleNamesPrefix, String fontFamily, Integer fontSize) {
		this.doc = doc;
		this.styleNamesPrefix = styleNamesPrefix;
		
		mainStyle = this.doc.addStyle(this.styleNamesPrefix+".Main", null);
		if (fontFamily!=null) StyleConstants.setFontFamily(mainStyle, fontFamily);
		if (fontSize  !=null) StyleConstants.setFontSize  (mainStyle, fontSize  );
		subStyles = new HashMap<>();
	}
	
	public void append(String format, Object... args) {
		append((Style) null, format, args);
	}
	
	public void append(Color textColor, String format, Object... args) {
		append(new Style(textColor), format, args);
	}
	
	public void append(Color textColor, Color backgroundColor, String format, Object... args) {
		append(new Style(textColor, backgroundColor), format, args);
	}
	
	public void append(Style style, String format, Object... args) {
		append(getOrCreate(style), String.format(format, args));
	}

	private void append(javax.swing.text.Style style, String text) {
		try {
			doc.insertString(doc.getLength(), text, style); }
		catch (BadLocationException e) {
			System.err.printf("[StyledDocumentInterface] BadLocationException while inserting Strings into StyledDocument: %s%n", e.getMessage());
			//e.printStackTrace();
		}
	}
	
	private javax.swing.text.Style getOrCreate(Style style) {
		if (style==null)
			return mainStyle;
		
		javax.swing.text.Style docStyle = subStyles.get(style);
		if (docStyle != null)
			return docStyle;
		
		String styleName = String.format("%s.SubStyle.%s", styleNamesPrefix, style.getID());
		//System.out.printf("Add Style \"%s\"%n", styleName);
		docStyle = doc.addStyle(styleName, mainStyle);
		style.setValuesTo(docStyle);
		subStyles.put(style, docStyle);
		
		return docStyle;
	}

	public void changeStyle(Color textColor, int offset, int length)
	{
		changeStyle(new Style(textColor), offset, length);
	}

	public void changeStyle(Color textColor, Color backgroundColor, int offset, int length)
	{
		changeStyle(new Style(textColor, backgroundColor), offset, length);
	}

	public void changeStyle(Style style, int offset, int length)
	{
		doc.setCharacterAttributes(offset, length, getOrCreate(style), true);
	}

	public void clear()
	{
		try { doc.remove(0, doc.getLength()); }
		catch (BadLocationException e) {
			System.err.printf("[StyledDocumentInterface] BadLocationException while clearing content of StyledDocument: %s%n", e.getMessage());
			e.printStackTrace();
		}
	}
	
	public static class Style {
		public static final Style BOLD   = new Style(true, false);
		public static final Style ITALIC = new Style(false, true);
		
		public final Color textColor;
		public final Color backgroundColor;
		public final boolean isBold;
		public final boolean isItalic;
		public final String fontFamily;
		
		public Style(Color textColor                                                     ) { this(textColor, null, false , false   , null      ); }
		public Style(                 boolean isBold, boolean isItalic                   ) { this(null     , null, isBold, isItalic, null      ); }
		public Style(Color textColor, boolean isBold, boolean isItalic                   ) { this(textColor, null, isBold, isItalic, null      ); }
		public Style(                                                   String fontFamily) { this(null     , null, false , false   , fontFamily); }
		public Style(Color textColor,                                   String fontFamily) { this(textColor, null, false , false   , fontFamily); }
		public Style(                 boolean isBold, boolean isItalic, String fontFamily) { this(null     , null, isBold, isItalic, fontFamily); }
		public Style(Color textColor, boolean isBold, boolean isItalic, String fontFamily) { this(textColor, null, isBold, isItalic, fontFamily); }
		public Style(Color textColor, Color backgroundColor                              ) { this(textColor, backgroundColor, false, false, null); }
		
		public Style(Color textColor, Color backgroundColor, boolean isBold, boolean isItalic, String fontFamily) {
			this.textColor = textColor;
			this.backgroundColor = backgroundColor;
			this.isBold = isBold;
			this.isItalic = isItalic;
			this.fontFamily = fontFamily;
		}
		
		public String getID() {
			return String.format("%s:%s::%s:%s:%s",
					textColor      ==null ? "--------" : String.format("%08X", textColor      .getRGB()),
					backgroundColor==null ? "--------" : String.format("%08X", backgroundColor.getRGB()),
					isBold   ? "B" : "-",
					isItalic ? "I" : "-",
					fontFamily==null ? "-----" : fontFamily);
		}
		
		public void setValuesTo(javax.swing.text.Style subStyle) {
			StyleConstants.setBold  (subStyle, isBold);
			StyleConstants.setItalic(subStyle, isItalic);
			if (textColor      !=null) StyleConstants.setForeground(subStyle, textColor);
			if (backgroundColor!=null) StyleConstants.setBackground(subStyle, backgroundColor);
			if (fontFamily     !=null) StyleConstants.setFontFamily(subStyle, fontFamily);
		}
		
		@Override
		public int hashCode() {
			int hashCode = 0;
			if (isBold  ) hashCode ^= 0xF0F0F0F0;
			if (isItalic) hashCode ^= 0x0F0F0F0F;
			if (textColor!=null) hashCode ^= textColor.getRGB();
			if (backgroundColor!=null) hashCode ^= backgroundColor.getRGB();
			if (fontFamily!=null) hashCode ^= fontFamily.hashCode();
			return hashCode;
		}
		@Override
		public boolean equals(Object obj) {
			if (!(obj instanceof Style)) return false;
			Style other = (Style) obj;
			if (other.isBold   != this.isBold  ) return false;
			if (other.isItalic != this.isItalic) return false;
			if ( (other.textColor==null) != (this.textColor==null) ) return false;
			if (  other.textColor!=null  &&  this.textColor!=null && other.textColor.getRGB()!=this.textColor.getRGB()) return false;
			if ( (other.backgroundColor==null) != (this.backgroundColor==null) ) return false;
			if (  other.backgroundColor!=null  &&  this.backgroundColor!=null && other.backgroundColor.getRGB()!=this.backgroundColor.getRGB()) return false;
			if ( (other.fontFamily==null) != (this.fontFamily==null) ) return false;
			if (  other.fontFamily!=null  &&  this.fontFamily!=null && !other.fontFamily.equals(this.fontFamily)) return false;
			return true;
		}
		
	}
}
