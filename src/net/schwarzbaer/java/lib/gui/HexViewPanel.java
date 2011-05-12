package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultStyledDocument;

import net.schwarzbaer.java.lib.system.Settings;

public class HexViewPanel extends JPanel
{
	private static final long serialVersionUID = 8244043702602372448L;

	protected static final int LINE_LENGTH = 16;
	
	private final JTextPane textPane;
	private final StyledDocumentInterface docInterface;

	private final JComboBox<Page> cmbbxPages;
	private final JButton btnPrevPage;
	private final JButton btnNextPage;
	private final JTextField txtfldPageSize;

	private byte[] bytes;

	protected int pageSize;
	protected int pageIndex;
	private int pageCount;
	private boolean ignorePageSelectionEvent;

	private final PageSizeStorage<?> pageSizeStorage;
	
	public HexViewPanel(int defaultPageSize, boolean isPageSizeEditable, Component... extraToolbarComponents)
	{
		this(defaultPageSize, isPageSizeEditable, null, extraToolbarComponents);
	}
	public HexViewPanel(int defaultPageSize, PageSizeStorage<?> pageSizeStorage, Component... extraToolbarComponents)
	{
		this(defaultPageSize, true, pageSizeStorage, extraToolbarComponents);
	}
	private HexViewPanel(int defaultPageSize, boolean isPageSizeEditable, PageSizeStorage<?> pageSizeStorage, Component[] extraToolbarComponents)
	{
		super(new BorderLayout());
		this.pageSizeStorage = pageSizeStorage;
		bytes = null;
		pageSize = this.pageSizeStorage==null ? defaultPageSize : this.pageSizeStorage.getValue(defaultPageSize);
		pageIndex = 0;
		pageCount = 0;
		ignorePageSelectionEvent = false;
		
		cmbbxPages = new JComboBox<>();
		cmbbxPages.addActionListener(e->{
			if (ignorePageSelectionEvent) return;
			int index = cmbbxPages.getSelectedIndex();
			Page page = index<0 ? null : cmbbxPages.getItemAt(index);
			setPage(page==null ? -1 : page.index, true);
		});
		
		JPanel controlPanel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.BOTH;
		
		c.weighty = 0;
		c.weightx = 0;
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridx = -1;
		for (Component comp : extraToolbarComponents) { c.gridx++; controlPanel.add(comp, c); }
		c.gridx++; controlPanel.add(cmbbxPages, c);
		c.gridx++; controlPanel.add(btnPrevPage = createButton("<", false, e->{ setPage(pageIndex-1, false); updatePageCombobox(); }), c);
		c.gridx++; controlPanel.add(btnNextPage = createButton(">", false, e->{ setPage(pageIndex+1, false); updatePageCombobox(); }), c);
		
		if (isPageSizeEditable)
		{
			c.gridx++; controlPanel.add(new JLabel("  Page Size (Lines): "), c);
			c.gridx++; controlPanel.add(txtfldPageSize = createPageSizeTextField(), c);
		}
		else
			txtfldPageSize = null;
		
		c.weightx = 1;
		c.gridx++; controlPanel.add(new JLabel(), c);
		
		DefaultStyledDocument doc = new DefaultStyledDocument();
		textPane = new JTextPane(doc);
		docInterface = new StyledDocumentInterface(doc, "HexView", "monospaced", 12);
		
		add(new JScrollPane(textPane), BorderLayout.CENTER);
		add(controlPanel, BorderLayout.NORTH);
	}

	private static JButton createButton(String title, boolean enabled, ActionListener al)
	{
		JButton comp = new JButton(title);
		comp.setEnabled(enabled);
		if (al!=null) comp.addActionListener(al);
		return comp;
	}

	private JTextField createPageSizeTextField()
	{
		JTextField txtfld = new JTextField(""+pageSize, 10);
		txtfld.setEnabled(false);
		Color defaultBG = txtfld.getBackground();

		Runnable processInput = ()->{
			String str = txtfld.getText();
			
			Integer value;
			try { value = Integer.parseInt(str); }
			catch (NumberFormatException e) { value = null; }
			
			if (value==null || value<=0)
				setBackground(Color.RED);
			else
			{
				setBackground(defaultBG);
				setPageSize(value);
			}
		};
		txtfld.addActionListener(e->processInput.run());
		txtfld.addFocusListener(new FocusListener()
		{
			@Override public void focusGained(FocusEvent e) {}
			@Override public void focusLost(FocusEvent e) { processInput.run(); }
		});
		
		return txtfld;
	}
	
	public void setData(byte[] bytes)
	{
		this.bytes = bytes;
		setPageSize(pageSize);
	}

	private void setPageSize(int n)
	{
		int currentPageStart = pageIndex*pageSize*LINE_LENGTH;
		pageSize = n;
		if (pageSizeStorage!=null) pageSizeStorage.setValue(pageSize);
		float byteCount = bytes==null ? 0 : bytes.length;
		pageCount = (int) Math.ceil( byteCount / (pageSize*LINE_LENGTH) );
		pageIndex = pageIndex<0 && pageCount>0 ? 0 : Math.min( currentPageStart / (pageSize*LINE_LENGTH), pageCount-1);
		Page[] pages = Page.createArray(pageCount);
		cmbbxPages.setModel(new DefaultComboBoxModel<>(pages));
		cmbbxPages.setSelectedIndex(pageIndex);
		if (txtfldPageSize!=null) txtfldPageSize.setEnabled(true);
	}
	
	private void setPage(int index, boolean wasChecked)
	{
		if (index < 0 || pageCount <= index)
		{
			if (!wasChecked) return;
			index = -1;
		}
		pageIndex = index;
		btnPrevPage.setEnabled(0 < pageIndex);
		btnNextPage.setEnabled(pageIndex+1 < pageCount);
		rebuildPage();
	}
	
	private void updatePageCombobox()
	{
		ignorePageSelectionEvent = true;
		cmbbxPages.setSelectedIndex(pageIndex);
		ignorePageSelectionEvent = false;
	}

	private static final int CHARS_PER_BYTE = 3;
	private static final int EXTRACHARS_AT_HALF_LINE = 1;
	private static final int CHARS_IN_LINE = 8+2 + LINE_LENGTH*CHARS_PER_BYTE+EXTRACHARS_AT_HALF_LINE + 5+LINE_LENGTH +2;
	private static final int FIRST_BYTE_POS_IN_LINE = 8+2 + 1;
	
	private void rebuildPage()
	{
		docInterface.clear();
		if (bytes==null || pageIndex<0) return;
		
		int pageStart = pageIndex*pageSize*LINE_LENGTH;
		for (int line=0; line<pageSize && pageStart+line*LINE_LENGTH < bytes.length; line++)
		{
			int lineIndex = pageStart + line*LINE_LENGTH;
			docInterface.append("%08X: ", lineIndex);
			String charStr = "";
			for (int i=0; i<LINE_LENGTH; i++)
			{
				if (i==LINE_LENGTH/2) docInterface.append("  ");
				else                  docInterface.append(" ");
				
				String byteStr;
				int byteIndex = lineIndex + i;
				if (byteIndex < bytes.length)
				{
					int value = bytes[byteIndex] & 0xFF;
					byteStr = String.format("%02X", value);
					charStr += value < 0x20 ? (char)0x80 : (char)value;
				}
				else
				{
					byteStr = "--";
					charStr += " ";
				}
				
				Colors colors = getInitialColorAtPos(byteIndex);
				Color foreground = colors==null ? null : colors.foreground;
				Color background = colors==null ? null : colors.background;
				docInterface.append(foreground, background, byteStr);
			}
			docInterface.append("  |  %s\r\n", charStr);
		}
	}

	protected Colors getInitialColorAtPos(int byteIndex)
	{
		return null;
	}

	public void changeColorAtPos(int bytePos, Color foreground, Color background)
	{
		if (pageIndex<0) return;
		
		int pageStart = pageIndex*pageSize*LINE_LENGTH;
		int pageEnd   = pageStart + pageSize*LINE_LENGTH-1;
		
		if (bytePos < pageStart) return;
		if (pageEnd < bytePos  ) return;
		
		int line    = (bytePos-pageStart) / LINE_LENGTH;
		int charPos = (bytePos-pageStart) % LINE_LENGTH;
		int offset = line*CHARS_IN_LINE + FIRST_BYTE_POS_IN_LINE + CHARS_PER_BYTE*charPos;
		if (charPos>=LINE_LENGTH/2) offset += EXTRACHARS_AT_HALF_LINE;
		
		docInterface.changeStyle(foreground, background, offset, 2);
	}

	public record Colors(Color foreground, Color background) {}
	
	private record Page(int index)
	{
		@Override public String toString()
		{
			return String.format("Page %d", index+1);
		}
	
		static Page[] createArray(int pageCount)
		{
			Page[] pages = new Page[pageCount];
			for (int i=0; i<pageCount; i++)
				pages[i] = new Page(i);
			return pages;
		}
	}

	public static class PageSizeStorage<ValueKey extends Enum<ValueKey>>
	{
		private final Settings<?, ValueKey> settings;
		private final ValueKey valueKey;
	
		public PageSizeStorage(Settings<?, ValueKey> settings, ValueKey valueKey)
		{
			this.settings = settings;
			this.valueKey = valueKey;
		}
		
		private int getValue(int defaultValue)
		{
			return settings.getInt(valueKey, defaultValue);
		}
		
		private void setValue(int value)
		{
			settings.putInt(valueKey, value);
		}
	}
}

