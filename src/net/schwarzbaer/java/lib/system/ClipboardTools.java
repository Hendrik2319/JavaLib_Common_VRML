package net.schwarzbaer.java.lib.system;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.Reader;
import java.io.StringWriter;

public class ClipboardTools {

	public static boolean copyToClipBoard(BufferedImage image) {
		if (image==null) return false;
		return copyToClipBoard(new TransferableImage(image), null);
	}

	public static boolean copyToClipBoard(String str) {
		return copyStringSelectionToClipBoard(str);
//		if (str==null) return false;
//		return copyToClipBoard(new DataHandler(str,"text/plain"), null);
	}

	public static boolean copyStringSelectionToClipBoard(String str) {
		if (str==null) return false;
		StringSelection data = new StringSelection(str);
		return copyToClipBoard(data,data);
	}

	public static boolean copyToClipBoard(Transferable content, ClipboardOwner owner) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit==null) return false;
		Clipboard clipboard = toolkit.getSystemClipboard();
		if (clipboard==null) return false;
		try { clipboard.setContents(content,null); }
		catch (IllegalStateException e1) { e1.printStackTrace(); return false; }
		return true;
	}

	private static class TransferableImage implements Transferable {

		private BufferedImage image;
		public TransferableImage(BufferedImage image) { this.image = image; }

		@Override public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException, IOException {
			if (isDataFlavorSupported(flavor)) return image;
			throw new UnsupportedFlavorException( flavor );
		}

		@Override public DataFlavor[] getTransferDataFlavors() { return new DataFlavor[] { DataFlavor.imageFlavor }; }
		@Override public boolean isDataFlavorSupported(DataFlavor flavor) { return DataFlavor.imageFlavor.equals(flavor); }
		
	}

	@SuppressWarnings("unused")
	private static String getStringFromClipBoard_simple() {
		Clipboard systemClip = Toolkit.getDefaultToolkit().getSystemClipboard();
		if (systemClip.isDataFlavorAvailable(DataFlavor.stringFlavor)) {
			try {
				Object obj = systemClip.getData(DataFlavor.stringFlavor);
				if ( (obj!=null) && (obj instanceof String))
					return obj.toString();
			}
			catch (UnsupportedFlavorException ex) {}
			catch (IOException ex) {}
		}
		return null;
	}

	public static String getStringFromClipBoard(boolean showOtherDataFlavors) {
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit==null) return null;
		Clipboard clipboard = toolkit.getSystemClipboard();
		if (clipboard==null) return null;
		Transferable transferable = clipboard.getContents(null);
		if (transferable==null) return null;
		
		DataFlavor textFlavor = new DataFlavor(String.class, "text/plain; class=<java.lang.String>");
		
		if (!transferable.isDataFlavorSupported(textFlavor)) {
			DataFlavor[] transferDataFlavors = transferable.getTransferDataFlavors();
			if (transferDataFlavors==null || transferDataFlavors.length==0) return null;
			
			if (showOtherDataFlavors) System.out.println("transferDataFlavors: "+toString(transferDataFlavors));
			textFlavor = DataFlavor.selectBestTextFlavor(transferDataFlavors);
		}
		
		if (textFlavor==null) return null;
		
		try (Reader reader = textFlavor.getReaderForText(transferable);) {
			
			StringWriter sw = new StringWriter();
			int n; char[] cbuf = new char[100000];
			while ((n=reader.read(cbuf))>=0) if (n>0) sw.write(cbuf, 0, n);
			return sw.toString();
			
		} catch (IOException | UnsupportedFlavorException e1) {
			e1.printStackTrace();
			return null;
		}
	}

	private static String toString(DataFlavor[] dataFlavors) {
		if (dataFlavors==null) return "<null>";
		String str = "";
		for (DataFlavor df:dataFlavors) {
			if (!str.isEmpty()) str+=",\r\n";
			str+=""+df;
		}
		return "[\r\n"+str+"\r\n]";
	}
	
	public static BufferedImage getImageFromClipBoard()
	{
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		if (toolkit==null) return null;
		Clipboard clipboard = toolkit.getSystemClipboard();
		if (clipboard==null) return null;
		
		Transferable content = null;
		try { content = clipboard.getContents(null); }
		catch (Exception ex) {
			System.err.printf("Exception: \"%s\"%n", ex.getMessage());
			return null;
		}
		
		if (content!=null && content.isDataFlavorSupported(DataFlavor.imageFlavor)) {
			try {
				Object data = content.getTransferData(DataFlavor.imageFlavor);
				if (data instanceof BufferedImage) {
					BufferedImage image = (BufferedImage) data;
					//System.out.printf("Found BufferedImage: %d x %d%n", image.getWidth(), image.getHeight());
					return image;
				}
				if (data instanceof RenderedImage) {
					RenderedImage image = (RenderedImage) data;
					//System.out.printf("Found RenderedImage: %d x %d%n", image.getWidth(), image.getHeight());
					BufferedImage buffImage = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
					buffImage.createGraphics().drawRenderedImage(image, new AffineTransform());
					return buffImage;
				}
				
			} catch (UnsupportedFlavorException ex) {
				System.err.printf("UnsupportedFlavorException: \"%s\"%n", ex.getMessage());
			} catch (IOException ex) {
				System.err.printf("IOException while reading TransferData: \"%s\"%n", ex.getMessage());
			}
		}// else
		//	System.err.printf("Found Other Content:  %s%n", content);
		
		return null;
	}
}
