package net.schwarzbaer.java.lib.image;

import java.awt.Image;

public class ImageCache<I extends Image> {
	
	private I image;
	private int width;
	private int height;
	private ImageSource<I> imageSource;
	
	public ImageCache(ImageSource<I> imageSource) {
		this.width = 0;
		this.height = 0;
		setImageSource(imageSource);
	}
	
	public void setImageSource(ImageSource<I> imageSource) {
		this.imageSource = imageSource;
		resetImage();
	}
	
	public void resetImage() {
		this.image = null;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}

	public static interface ImageSource<I> {
		public I getImage(int width, int height);
	}

	public I getImage(int width, int height) {
		if (!hasImage(width, height)) {
			this.image = imageSource.getImage(width, height);
			this.width = width;
			this.height = height;
		}
		return image;
	}

	public boolean hasImage(int width, int height) {
		return image!=null && this.width==width && this.height==height;
	}
}
