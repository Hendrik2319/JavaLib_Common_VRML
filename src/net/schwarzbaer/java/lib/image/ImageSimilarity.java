package net.schwarzbaer.java.lib.image;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Comparator;

public class ImageSimilarity<ImageID> {
	
	private RasterSource<ImageID> rasterSource;
	
	public ImageSimilarity(RasterSource<ImageID> rasterSource) {
		this.rasterSource = rasterSource;
	}
	
	public static <ImageID> int[] computeOrder(ImageID baseImageID, ImageID[] imageIDs, RasterSource<ImageID> rasterSource) {
		return new ImageSimilarity<ImageID>(rasterSource).computeOrder(baseImageID, imageIDs);
	}
	
	public int[] computeOrder(ImageID baseImageID, ImageID[] imageIDs) {
		ComparableImage baseImage = new ComparableImage(-1, baseImageID, rasterSource);
		
		ComparableImage[] images = new ComparableImage[imageIDs.length];
		for (int i=0; i<imageIDs.length; i++) {
			images[i] = new ComparableImage(i, imageIDs[i], rasterSource);
			images[i].similarity = images[i].computeSimilarityTo(baseImage);
		}
		
		int[] sortedIndexes = Arrays.stream(images)
				.sorted(Comparator.comparing(img->Double.isNaN(img.similarity) ? null : img.similarity, Comparator.nullsLast(Comparator.naturalOrder())))
				.mapToInt(img->img.index)
				.toArray();
		return sortedIndexes;
	}
	
	public static byte[] computeHash(BufferedImage image) {
		return computeHash(image.getRaster());
	}
	
	public static byte[] computeHash(WritableRaster raster) {
		ComparableImage compImg = new ComparableImage(-1, raster);
		return compImg.hash;
	}
	
	public static byte[] computeHash(PrimitiveRasterSource rasterSource) {
		ComparableImage compImg = new ComparableImage(-1, "", (imageID, backgroundColor, width, height)->rasterSource.createRaster(backgroundColor, width, height));
		return compImg.hash;
	}
	
	public static double computeSimilarity(byte[] hash1, byte[] hash2) {
		if (hash1==null) return Double.NaN;
		if (hash2==null) return Double.NaN;
		
		if (hash1.length!=hash2.length)
			throw new IllegalArgumentException();
		
		int n = hash1.length/3;
		double similarity = 0;
		for (int i=0; i<n; i++){
			int r1 = hash1[i*3+0] & 0xFF;
			int r2 = hash2[i*3+0] & 0xFF;
			int g1 = hash1[i*3+1] & 0xFF;
			int g2 = hash2[i*3+1] & 0xFF;
			int b1 = hash1[i*3+2] & 0xFF;
			int b2 = hash2[i*3+2] & 0xFF;
			similarity += Math.sqrt( (r1-r2)*(r1-r2) + (g1-g2)*(g1-g2) + (b1-b2)*(b1-b2) );
		}
		return similarity/n;
	}

	public interface PrimitiveRasterSource {
		WritableRaster createRaster(int backgroundColor, int width, int height);
	}
	
	public interface RasterSource<ImageID> {
		WritableRaster createRaster(ImageID image, int backgroundColor, int width, int height);
	}

	private static class ComparableImage {
		
		final byte[] hash;
		final int index;
		double similarity;

		<ImageID> ComparableImage(int index, ImageID imageID, RasterSource<ImageID> rasterSource) {
			this(index,rasterSource.createRaster(imageID,0xFFFFFF,256,256));
		}

		ComparableImage(int index, WritableRaster raster) {
			//this.raster = raster;
			this.index = index;
			this.similarity = Double.NaN;
			if (raster==null)
				hash = null;
			else {
				Rectangle bounds = raster.getBounds();
				int[] rgb = new int[4];
				hash = new byte[bounds.width*bounds.height*3];
				for (int x=0; x<bounds.width; x++)
					for (int y=0; y<bounds.height; y++) {
						raster.getPixel(x+bounds.x, y+bounds.y, rgb);
						hash[3*(x+y*bounds.height)+0] = (byte) (rgb[0] & 0xFF);
						hash[3*(x+y*bounds.height)+1] = (byte) (rgb[1] & 0xFF);
						hash[3*(x+y*bounds.height)+2] = (byte) (rgb[2] & 0xFF);
					}
			}
		}

		private double computeSimilarityTo(ComparableImage other) {
			if (other==null) return Double.NaN;
			return computeSimilarity(this.hash,other.hash);
		}
	}
}
