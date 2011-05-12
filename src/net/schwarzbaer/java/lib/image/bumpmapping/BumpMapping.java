package net.schwarzbaer.java.lib.image.bumpmapping;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Locale;

import net.schwarzbaer.java.lib.image.ImageCache;

public class BumpMapping {
	
	private Shading shading;
	private NormalFunction normalFunction;
	private ImageCache<BufferedImage> imageCache;
	private OverSampling overSampling = OverSampling.None;
	private NormalCache normalCache = null;
	private final boolean cacheNormals;
	
	public BumpMapping(boolean cacheImage, boolean cacheNormals) {
		this.cacheNormals = cacheNormals;
		imageCache = !cacheImage?null:new ImageCache<>(this::renderImage_uncached);
	}
	
	public BumpMapping setNormalFunction(NormalFunction normalFunction) {
		this.normalFunction = normalFunction;
		resetImageCache();
		resetNormalCache();
		return this;
	}
	public NormalFunction getNormalFunction() {
		return normalFunction;
	}
	public void setSun(double x, double y, double z) {
		shading.setSun(x,y,z);
		resetImageCache();
	}
//	public void getSun(Normal sunOut) {
//		sunOut.x = shading.sun.x;
//		sunOut.y = shading.sun.y;
//		sunOut.z = shading.sun.z;
//	}
	public void setShading(Shading shading) {
		this.shading = shading;
		resetImageCache();
	}
	public Shading getShading() { return shading; }
	
	public void setOverSampling(OverSampling overSampling) {
		this.overSampling = overSampling;
		resetImageCache();
		resetNormalCache();
	}
	public OverSampling getOverSampling() {
		return overSampling;
	}
	public void reset() {
		resetImageCache();
		resetNormalCache();
	}
	
	private void resetImageCache() {
		if (imageCache!=null) imageCache.resetImage();
	}
	
	private void resetNormalCache() {
		normalCache = null;
	}

	
	public BufferedImage renderImage(int width, int height) {
		if (imageCache!=null) return imageCache.getImage(width, height);
		return renderImage_uncached(width, height);
	}
	
	public BufferedImage renderImage_uncached(int width, int height) { return renderImage_uncached(width, height, null); }
	public BufferedImage renderImage_uncached(int width, int height, RenderProgressListener listener) {
		
		if (normalCache==null || !cacheNormals || (cacheNormals && !normalCache.hasSize(width, height))) {
			if (cacheNormals) normalCache = new NormalCache(width, height, overSampling, (x,y)->normalFunction.getNormal(x,y,width,height));
			else              normalCache = new NormalCache.Dummy(                       (x,y)->normalFunction.getNormal(x,y,width,height));
		}
		
		PixelRenderer pixelRenderer = new PixelRenderer(overSampling,1,normalCache,
			b       -> normalFunction.forceNormalCreation(b),
			(x,y,n) -> shading.getColor(x,y,width,height,n)
		);
		
		BufferedImage image = renderImage(1, width, height, pixelRenderer, listener);
		normalCache.setFixed();
		
		return image;
	}
	public BufferedImage renderImage_uncached(int width, int height, float scale) { return renderImage_uncached(width, height, scale, null); }
	public BufferedImage renderImage_uncached(int width, int height, float scale, RenderProgressListener listener) {
		PixelRenderer pixelRenderer = new PixelRenderer(overSampling, 1/scale,
			(d1,d2,d3,x,y  ) -> normalFunction.getNormal(x,y,width,height),
			b                -> normalFunction.forceNormalCreation(b),
			(         x,y,n) -> shading       .getColor (x,y,width,height,n)
		);
		
		return renderImage(scale, Math.round(width *scale), Math.round(height*scale), pixelRenderer, listener);
	}

	private BufferedImage renderImage(float scale, int scaledWidth, int scaledHeight, PixelRenderer pixelRenderer, RenderProgressListener listener) {
		if (listener!=null) listener.setSize(scaledWidth, scaledHeight);
		BufferedImage image = new BufferedImage(scaledWidth, scaledHeight, BufferedImage.TYPE_INT_ARGB);
		WritableRaster raster = image.getRaster();
		for (int pixX=0; pixX<scaledWidth; pixX++)
			for (int pixY=0; pixY<scaledHeight; pixY++) {
				int[] color = pixelRenderer.computeColor(pixX,pixY,pixX/scale,pixY/scale);
				if (color!=null) raster.setPixel(pixX, pixY, color);
				if (listener!=null) listener.wasRendered(pixX, pixY);
			}
		
		return image;
	}
	
	public interface RenderProgressListener {
		void setSize(int width, int height);
		void wasRendered(int x, int y);
	}
	
	private static class NormalCache implements PixelRenderer.NormalSource {
		
		public static class Dummy extends NormalCache {
			Dummy(NormalSource normalSource) { super(0,0,null, normalSource); }
			@Override public Normal getNormal(int pixX, int pixY, int spIndex, double x, double y) { return normalSource.getNormal(x, y); }
		}

		protected final NormalSource normalSource;
		private boolean isFixed;
		private Normal[][][] cache;
		private int width;
		private int height;

		NormalCache(int width, int height, OverSampling overSampling, NormalSource normalSource) {
			this.width = width;
			this.height = height;
			this.normalSource = normalSource;
			this.isFixed = false;
			int n = overSampling==null || overSampling==OverSampling.None ? 1 : overSampling.samplingPoints.length;
			cache = new Normal[n][width][height];
			for (Normal[][] arr1:cache)
				for (Normal[] arr2:arr1)
					Arrays.fill(arr2, null);
		}

		public boolean hasSize(int width, int height) {
			return this.width==width && this.height==height;
		}

		public void setFixed() { isFixed = true; }

		@Override
		public Normal getNormal(int pixX, int pixY, int spIndex, double x, double y) {
			Normal n;
			if (!isFixed) cache[spIndex][pixX][pixY] = n = normalSource.getNormal(x, y);
			else          n = cache[spIndex][pixX][pixY];
			return n;
		}
		
		public interface NormalSource { Normal getNormal(double x,double y); }
	}
	
	private static class PixelRenderer {
		
		public interface NormalSource       { Normal getNormal(int pixX, int pixY, int spIndex, double x,double y); }
		public interface NormalSourceSwitch { void forceNormalCreation(boolean b); }
		public interface ColorSource        { int[]  getColor(double x,double y, Normal n); }
		public interface Source extends ColorSource, NormalSource, NormalSourceSwitch {}

		private final NormalSource normalSource;
		private final NormalSourceSwitch normalSourceSwitch;
		private final ColorSource colorSource;
		private final OverSampling overSampling;
		private final double pixWidth;

		@SuppressWarnings("unused")
		PixelRenderer(OverSampling overSampling, double pixWidth, Source source) { this(overSampling, pixWidth, source,source,source); }
		PixelRenderer(OverSampling overSampling, double pixWidth, NormalSource normalSource, NormalSourceSwitch normalSourceSwitch, ColorSource colorSource) {
			this.overSampling = overSampling;
			this.pixWidth = pixWidth;
			this.normalSource = normalSource;
			this.normalSourceSwitch = normalSourceSwitch;
			this.colorSource = colorSource;
		}
	
		public int[] computeColor(int pixX, int pixY, double x, double y) {
			
			if (overSampling==null || overSampling==OverSampling.None || overSampling.samplingPoints.length==0) {
				Normal normal = normalSource.getNormal(pixX, pixY, 0, x,y);
				return colorSource.getColor(x,y,normal);
			}
			
			boolean miss = false;
			boolean hit  = false;
			Normal[] normals = new Normal[overSampling.samplingPoints.length];
			for (int i=0; i<normals.length; i++) {
				OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
				normals[i] = getNormal(pixX, pixY, i, x, y, sp);
				if (normals[i]==null) {
					if (!miss && hit) normalSourceSwitch.forceNormalCreation(true);
					miss = true;
				} else {
					if (!hit && miss) normalSourceSwitch.forceNormalCreation(true);
					hit = true;
				}
			}
			if (hit && miss) {
				// normalSourceSwitch.forceNormalCreation is already set
				for (int i=0; i<normals.length; i++) {
					OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
					normals[i] = getNormal(pixX, pixY, i, x,y, sp );
					Debug.Assert(normals[i]!=null);
				}
				normalSourceSwitch.forceNormalCreation(false);
			}
			
			if (miss && !hit)
				return null;
			
			int[] sumColor = null;
			for (int i=0; i<overSampling.samplingPoints.length; i++) {
				OverSampling.SamplingPoint sp = overSampling.samplingPoints[i];
				int[] color = getColor( x,y, sp, normals[i] );
				sumColor = add(sumColor,color);
			}
			return div(sumColor,overSampling.samplingPoints.length);
		}
		
		private int[] getColor(double x, double y, OverSampling.SamplingPoint sp, Normal n) {
			return colorSource.getColor(
				x+sp.x*pixWidth,
				y+sp.y*pixWidth,
				n
			);
		}
		
		private Normal getNormal(int pixX, int pixY, int spIndex, double x, double y, OverSampling.SamplingPoint sp) {
			return normalSource.getNormal(
				pixX, pixY, spIndex, 
				x+sp.x*pixWidth,
				y+sp.y*pixWidth
			);
		}
		
		private int[] div(int[] color, int divisor) {
			Debug.Assert(color!=null);
			for (int i=0; i<color.length; i++)
				color[i] /= divisor;
			return color;
		}
		
		private int[] add(int[] sumColor, int[] color) {
			Debug.Assert(color!=null);
			Debug.Assert(color.length>0);
			if (sumColor==null) {
				sumColor = new int[color.length];
				Arrays.fill(sumColor,0);
			}
			Debug.Assert(sumColor.length == color.length);
			for (int i=0; i<sumColor.length; i++)
				sumColor[i] += color[i];
			return sumColor;
		}
	}

	public enum OverSampling {
		None         ("None (1x)"),
		_2x_Diagonal1("2x (diagonal1)"  , new SamplingPoint(-0.25,-0.25), new SamplingPoint(0.25,0.25)),
		_2x_Diagonal2("2x (diagonal2)"  , new SamplingPoint(-0.25,0.25), new SamplingPoint(0.25,-0.25)),
		_4x_Square   ("4x (square)"     , new SamplingPoint(-0.25,-0.25), new SamplingPoint(0.25,0.25), new SamplingPoint(-0.25,0.25), new SamplingPoint(0.25,-0.25)),
		_5x_Cross    ("5x (cross,\"x\")", new SamplingPoint(0,0), new SamplingPoint(-0.3,-0.3), new SamplingPoint(0.3,0.3), new SamplingPoint(-0.3,0.3), new SamplingPoint(0.3,-0.3)),
		_9x_Square   ("9x (square)"     , new SamplingPoint(0,0), new SamplingPoint(0,0.33), new SamplingPoint(0,-0.33), new SamplingPoint(0.33,0), new SamplingPoint(-0.33,0), new SamplingPoint(0.33,0.33), new SamplingPoint(0.33,-0.33), new SamplingPoint(-0.33,0.33), new SamplingPoint(-0.33,-0.33)),
		;
		
		private final String label;
		private final SamplingPoint[] samplingPoints;
		OverSampling(String label, SamplingPoint... samplingPoints) {
			this.label = label;
			this.samplingPoints = samplingPoints;
		}
		@Override
		public String toString() {
			return label;
		}
	
		private static class SamplingPoint {
			final double x,y;
			private SamplingPoint(double x, double y) { this.x = x; this.y = y;
			}
		}
	}
	
	static final double FULL_CIRCLE = 2*Math.PI;

	public static double normalizeAngle(double minW, double w) {
		double wDiff = w-minW;
		if (wDiff<0 || FULL_CIRCLE<wDiff) w -= Math.floor(wDiff/FULL_CIRCLE)*FULL_CIRCLE;
		Debug.Assert(minW<=w);
		Debug.Assert(w<=minW+FULL_CIRCLE);
		return w;
	}
	public static boolean isInsideAngleRange(double minW, double maxW, double w) {
		Debug.Assert(Double.isFinite(minW));
		Debug.Assert(Double.isFinite(maxW));
		Debug.Assert(minW<=maxW);
		
		w = normalizeAngle(minW,w);
		return w<=maxW;
	}
	
	public interface NormalFunctionBase {
		public Normal getNormal(double x, double y, double width, double height);
	}

	public static class MutableNormal {
		public double x,y,z;
		public Color color;
		public MutableNormal(Normal n) { this(n.x,n.y,n.z,n.color); }
		public MutableNormal(double x, double y, double z, Color color) { this.color=color; this.x=x; this.y=y; this.z=z; }
		public Normal toNormal() { return new Normal( x,y,z, color ); }
	}

	public static class Normal {
		public final double x,y,z;
		public final Color color;
		
		public Normal() { this(0,0,0); }
		public Normal(Normal n) { this(n.x,n.y,n.z,n.color); }
		public Normal(Normal n, Color color) { this(n.x,n.y,n.z,color); }
		public Normal(double x, double y, double z) { this(x,y,z,null); }
		public Normal(double x, double y, double z, Color color) { this.color=color; this.x=x; this.y=y; this.z=z; }
		
		public Normal add(Normal v) {
			return new Normal(x+v.x,y+v.y,z+v.z,color);
		}
		public double dotP(Normal v) {
			return x*v.x+y*v.y+z*v.z;
		}
		public Normal normalize() { return mul(1/getLength()); }
		public Normal mul(double d) { return new Normal(x*d,y*d,z*d,color); }
		public double getLength() { return Math.sqrt(x*x+y*y+z*z); }
		
		public Normal rotateZ(double w) {
			return new Normal(
				x*Math.cos(w)-y*Math.sin(w),
				x*Math.sin(w)+y*Math.cos(w),
				z,
				color
			);
		}
		
		public Normal rotateY(double w) {
			return new Normal(
				x*Math.cos(w)+z*Math.sin(w),
				y,
				-x*Math.sin(w)+z*Math.cos(w),
				color
			);
		}
		
		public static Normal blend(double f, double fmin, double fmax, Normal vmin, Normal vmax) {
			f = (f-fmin)/(fmax-fmin); 
			return new Normal(
					vmax.x*f+vmin.x*(1-f),
					vmax.y*f+vmin.y*(1-f),
					vmax.z*f+vmin.z*(1-f)
				);
		}
		@Override public String toString() {
			return String.format(Locale.ENGLISH, "Normal[%1.5f,%1.5f,%1.5f%s]", x, y, z, color==null?"":String.format(",0x%08X", color.getRGB()) );
		}
		
	}
	
	public static class NormalXY {
		public final double x,y;
		public final Color color;
		
		public NormalXY() { this(0,0,null); }
		public NormalXY(NormalXY n) { this(n.x,n.y,n.color); }
		public NormalXY(NormalXY n, Color color) { this(n.x,n.y,color); }
		public NormalXY(double x, double y) { this(x,y,null); }
		public NormalXY(double x, double y, Color color) { this.x=x; this.y=y; this.color=color; }
		
		public static NormalXY blend(double f, double fmin, double fmax, NormalXY vmin, NormalXY vmax) {
			f = (f-fmin)/(fmax-fmin); 
			return new NormalXY(
					vmax.x*f+vmin.x*(1-f),
					vmax.y*f+vmin.y*(1-f)
				);
		}
		
		public NormalXY normalize()   { return mul(1/getLength()); }
		public NormalXY mul(double d) { return new NormalXY(x*d,y*d,color); }
		public double   getLength()   { return Math.sqrt(x*x+y*y); }
		
		public Normal toNormalInXZ() { return new Normal( x,0,y, color ); }
		
		@Override public String toString() {
			return String.format(Locale.ENGLISH, "NormalXY[%1.5f,%1.5f,%1.5f%s]", x, y, color==null?"":String.format(",0x%08X", color.getRGB()) );
		}
	}

	public interface Indexer {
		
		public int getIndex(double x, double y, double width, double height);
		
		public interface Cart extends Indexer {
			@Override public default int getIndex(double x, double y, double width, double height) {
				return getIndex(x, y);
			}
			public int getIndex(double x, double y);
		}
		public interface Polar extends Indexer {
			@Override public default int getIndex(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width/2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getIndex(w,r);
			}
			public int getIndex(double w, double r);
		}
	}
	
	public interface Colorizer {
		
		public Color getColor(double x, double y, double width, double height);
		
		public interface Cart extends Colorizer {
			@Override public default Color getColor(double x, double y, double width, double height) {
				return getColor(x, y);
			}
			public Color getColor(double x, double y);
		}
		public interface Polar extends Colorizer {
			@Override public default Color getColor(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return getColor(w, r);
			}
			public Color getColor(double w, double r);
		}
	}
	
	public interface Filter {
		
		public boolean passesFilter(double x, double y, double width, double height);
		
		public interface Cart extends Filter {
			@Override public default boolean passesFilter(double x, double y, double width, double height) {
				return passesFilter(x, y);
			}
			public boolean passesFilter(double x, double y);
		}
		public interface Polar extends Filter {
			@Override public default boolean passesFilter(double x, double y, double width, double height) {
				double y1 = y-height/2.0;
				double x1 = x-width /2.0;
				double w = Math.atan2(y1,x1);
				double r = Math.sqrt(x1*x1+y1*y1);
				return passesFilter(w,r);
			}
			public boolean passesFilter(double w, double r);
		}
	}
}
