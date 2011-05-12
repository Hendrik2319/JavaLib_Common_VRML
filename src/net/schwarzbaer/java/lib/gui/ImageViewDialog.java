package net.schwarzbaer.java.lib.gui;

import java.awt.Window;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

import javax.swing.JDialog;

public class ImageViewDialog extends JDialog {
	private static final long serialVersionUID = 2981906616002170627L;
	private ImageView imageView;

	public ImageViewDialog(Window parent, BufferedImage image, String title, int width, int height) {
		this(parent, image, title, width, height, false);
	}
	public ImageViewDialog(Window parent, BufferedImage image, String title, int width, int height, boolean exitOnESC) {
		this(parent, image, title, width, height, null, exitOnESC);
	}
	public ImageViewDialog(Window parent, BufferedImage image, String title, int width, int height, ImageView.InterpolationLevel interpolationLevel, boolean exitOnESC) {
		this(parent, image, title, width, height, interpolationLevel, exitOnESC, false);
	}
	public ImageViewDialog(Window parent, BufferedImage image, String title, int width, int height, ImageView.InterpolationLevel interpolationLevel, boolean exitOnESC, boolean withGroupedContexMenu) {
		super(parent,title,ModalityType.APPLICATION_MODAL);
		imageView = new ImageView(image,width,height,interpolationLevel, withGroupedContexMenu);
		setContentPane(imageView);
		pack();
		setLocationRelativeTo(parent);
		imageView.reset();
		if (exitOnESC) {
			KeyAdapter keyAdapter = new KeyAdapter() {
				@Override public void keyPressed(KeyEvent e) {
					if (e.getKeyCode()==KeyEvent.VK_ESCAPE)
						ImageViewDialog.this.setVisible(false);
				}
			};
			addKeyListener(keyAdapter);
			imageView.addKeyListener(keyAdapter);
		}
	}

	public void setImage(BufferedImage image) {
		imageView.setImage(image);
		imageView.reset();
	}
	
	public void setZoom(double zoom)
	{
		imageView.setZoom(zoom);
	}
}
