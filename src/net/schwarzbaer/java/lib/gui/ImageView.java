package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.Vector;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Consumer;

import javax.swing.Icon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

public class ImageView extends ZoomableCanvas<ImageView.ViewState> {
	private static final long serialVersionUID = 4779060880687788367L;
	private static final Color COLOR_AXIS = new Color(0x70000000,true);
	//private static final Color COLOR_BACKGROUND = Color.WHITE;
	
	public enum BGPattern {
		WhiteChecker("White Checker", createCheckerPattern(10, 6,4, new Color(0xFFFFFF), new Color(0xEFEFEF))),
		GrayChecker ( "Gray Checker", createCheckerPattern(10, 6,4, new Color(0x7F7F7F), new Color(0x6F6F6F))),
		BlackChecker("Black Checker", createCheckerPattern(10, 6,4, new Color(0x000000), new Color(0x202020))),
		;
		private final String label;
		private final BufferedImage patternImage;
		private BGPattern(String label, BufferedImage patternImage) {
			this.label = label;
			this.patternImage = patternImage;
		}
		private static BufferedImage createCheckerPattern(int cellWidth, int repeatsX, int repeatsY, Color c1, Color c2) {
			BufferedImage pattern = new BufferedImage(cellWidth*2*repeatsX, cellWidth*2*repeatsY, BufferedImage.TYPE_INT_ARGB);
			Graphics g = pattern.getGraphics();
			for (int x=0; x<2*repeatsX; x++) {
				for (int y=0; y<2*repeatsY; y++) {
					g.setColor( ((x+y)&1)==0 ? c1 : c2 );
					g.fillRect(x*cellWidth, y*cellWidth, cellWidth, cellWidth);
				}
			}
			return pattern;
		}
	}
	
	private BufferedImage image;
	private Color bgColor;
	private BGPattern bgPattern;
	private boolean useInterpolation;
	private boolean useBetterInterpolation;
	private final BetterScaling betterScaling;
	private final ImageViewContextMenu contextMenu;
	private final Vector<DrawExtension> drawExtensions;
	
	public ImageView(                     int width, int height                                                                      ) { this(null , width, height, null              , false); }
	public ImageView(BufferedImage image, int width, int height                                                                      ) { this(image, width, height, null              , false); }
	public ImageView(                     int width, int height, InterpolationLevel interpolationLevel                               ) { this(null , width, height, interpolationLevel, false); }
	public ImageView(                     int width, int height, InterpolationLevel interpolationLevel, boolean withGroupedContexMenu) { this(null , width, height, interpolationLevel, withGroupedContexMenu); }
	public ImageView(BufferedImage image, int width, int height, InterpolationLevel interpolationLevel                               ) { this(image, width, height, interpolationLevel, false); }
	public ImageView(BufferedImage image, int width, int height, InterpolationLevel interpolationLevel, boolean withGroupedContexMenu) {
		this.image = image;
		bgColor = null;
		bgPattern = null;
		useInterpolation = interpolationLevel==null || interpolationLevel.isGreaterThan(InterpolationLevel.Level0_NearestNeighbor);
		useBetterInterpolation = interpolationLevel==InterpolationLevel.Level2_Better;
		setPreferredSize(width, height);
		activateMapScale(COLOR_AXIS, "px", true);
		activateAxes(COLOR_AXIS, true,true,true,true);
		
		betterScaling = new BetterScaling(this::repaint);
		addZoomListener(this::updateBetterInterpolation);
		
		contextMenu = new ImageViewContextMenu(this,interpolationLevel!=null, withGroupedContexMenu);
		contextMenu.addTo(this);
		
		drawExtensions = new Vector<DrawExtension>();
	}
	
	public ContextMenu getContextMenu() {
		return contextMenu;
	}

	public enum InterpolationLevel {
		Level0_NearestNeighbor, Level1_Bicubic, Level2_Better;

		public boolean isGreaterThan(InterpolationLevel other) {
			if (other==null) return true;
			return this.ordinal()>other.ordinal();
		}
	}
	
	private void updateBetterInterpolation() {
		if (useBetterInterpolation && this.image!=null && viewState.isOk()) {
			int imageX      = viewState.convertPos_AngleToScreen_LongX(0);
			int imageY      = viewState.convertPos_AngleToScreen_LatY (0);
			int imageWidth  = viewState.convertPos_AngleToScreen_LongX(this.image.getWidth ()) - imageX;
			int imageHeight = viewState.convertPos_AngleToScreen_LatY (this.image.getHeight()) - imageY;
			if (imageWidth<this.image.getWidth() && imageHeight<this.image.getHeight())
				betterScaling.scaleImage(this.image, imageWidth, imageHeight);
			else
				betterScaling.clearResult();
		} else
			betterScaling.clearResult();
	}
	
	public static BufferedImage computeScaledImageByBetterScaling(BufferedImage image, int targetWidth, int targetHeight, boolean ignoreAlpha) {
		return BetterScaling.computeScaledImage(image, targetWidth, targetHeight, ignoreAlpha);
	}
	
	private static class BetterScaling {
		
		private final Runnable repaint;
		private final ExecutorService scheduler;
		private Future<BufferedImage> runningTask;
		
		BetterScaling(Runnable repaint) {
			this.repaint = repaint;
			if (this.repaint==null) throw new IllegalArgumentException();
			scheduler = Executors.newSingleThreadExecutor();
			runningTask = null;
		}

		public synchronized void scaleImage(BufferedImage image, int targetWidth, int targetHeight) {
			if (image==null)
				throw new IllegalArgumentException("BetterInterpolation.recomputeImage(null, ...) is not allowed.");
			if (image.getWidth()<targetWidth || image.getHeight()<targetHeight)
				throw new IllegalArgumentException("BetterInterpolation.recomputeImage(): Target width and height must be smaller than current image size.");
			
			if (runningTask!=null && !runningTask.isCancelled() && !runningTask.isDone())
				runningTask.cancel(true);
			
			runningTask = scheduler.submit(()->{
				BufferedImage newImage = computeScaledImage(image, targetWidth, targetHeight);
				//if (Thread.currentThread().isInterrupted())
				//	System.out.println("BetterScaling.computeScaledImage -> interrupted");
				//else
				//	System.out.println("BetterScaling.computeScaledImage -> finished");
				SwingUtilities.invokeLater(repaint);
				return newImage;
			});
			
		}

		public synchronized void clearResult() {
			if (runningTask!=null && !runningTask.isCancelled() && !runningTask.isDone())
				runningTask.cancel(true);
			runningTask = null;
		}

		public synchronized BufferedImage getResult() {
			if (runningTask==null) return null;
			if (runningTask.isCancelled()) return null;
			if (!runningTask.isDone()) return null;
			
			try { return runningTask.get(); }
			catch (InterruptedException e) { System.err.printf("InterruptedException: %s%n", e.getMessage()); }
			catch (ExecutionException   e) { System.err.printf("ExecutionException: %s%n"  , e.getMessage()); }
			return null;
		}

		private static BufferedImage computeScaledImage(BufferedImage image, int targetWidth, int targetHeight) {
			return computeScaledImage(image, targetWidth, targetHeight, true);
		}
		private static BufferedImage computeScaledImage(BufferedImage image, int targetWidth, int targetHeight, boolean ignoreAlpha) {
			Thread currentThread = Thread.currentThread();
			BufferedImage newImage = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_ARGB);
			
			PixelFract[][] pixelFractionsX = computePixelFractions(image.getWidth (), targetWidth );
			PixelFract[][] pixelFractionsY = computePixelFractions(image.getHeight(), targetHeight);
			//showPixelFractions(image, targetWidth, targetHeight, pixelFractionsX, pixelFractionsY);
			if (currentThread.isInterrupted()) return null;
			
			WritableRaster origRaster = image.getRaster();
			WritableRaster newRaster  = newImage.getRaster();
			
			int[] origColor = new int[4];
			double[] sum = new double[4];
			int[] newColor = new int[4];
			
			for (int x=0; x<targetWidth; x++) {
				if (currentThread.isInterrupted()) return null;
				
				PixelFract[] fX = pixelFractionsX[x]; 
				for (int y=0; y<targetHeight; y++) {
					if (currentThread.isInterrupted()) return null;
					
					PixelFract[] fY = pixelFractionsY[y];
					
					Arrays.fill(sum,0);
					double totalWeight = 0, totalAlphaWeight = 0, fractWeight, alphaWeight;
					for (int iX=0; iX<fX.length; iX++) {
						if (currentThread.isInterrupted()) return null;
						
						PixelFract pixelFractX = fX[iX];
						for (int iY=0; iY<fY.length; iY++) {
							if (currentThread.isInterrupted()) return null;
							
							PixelFract pixelFractY = fY[iY];
							//try {
								origRaster.getPixel(pixelFractX.coord, pixelFractY.coord, origColor);
							//} catch (Exception e) {
							//	System.err.printf("%s: origRaster.getPixel( %d, %d, int[%d]) -> %s%n", e.getClass().getName(), pixelFractX.coord, pixelFractY.coord, origColor.length, e.getMessage());
							//	//e.printStackTrace();
							//}
							fractWeight = pixelFractX.fract*pixelFractY.fract;
							alphaWeight = ignoreAlpha ? 1 : origColor[3]/255.0;
							for (int i=0; i<3; i++)
								sum[i] += origColor[i]*fractWeight*alphaWeight;
							sum[3] += origColor[3]*fractWeight;
							totalWeight += fractWeight*alphaWeight;
							totalAlphaWeight += fractWeight;
						}
					}
					newColor[0] = (int) Math.max(0, Math.min(255, Math.round( sum[0] / totalWeight ) ) );
					newColor[1] = (int) Math.max(0, Math.min(255, Math.round( sum[1] / totalWeight ) ) );
					newColor[2] = (int) Math.max(0, Math.min(255, Math.round( sum[2] / totalWeight ) ) );
					newColor[3] = ignoreAlpha ? 255 : (int) Math.max(0, Math.min(255, Math.round( sum[3] / totalAlphaWeight ) ) );
					
					//try {
						newRaster.setPixel(x, y, newColor);
					//} catch (Exception e) {
					//	e.printStackTrace();
					//}
				}
			}
			
			return newImage;
			//return null;
		}

		private static PixelFract[][] computePixelFractions(int currentWidth, int targetWidth) {
			Thread currentThread = Thread.currentThread();
			PixelFract[][] pixelFractions = new PixelFract[targetWidth][];
			double pixelWidth = currentWidth / (double)targetWidth;
			for (int i=0; i<pixelFractions.length; i++) {
				if (currentThread.isInterrupted()) return null;
				double c0 = pixelWidth*i;
				double c1 = pixelWidth*(i+1);
				if (c1>currentWidth) c1 = currentWidth; // may happen as rounding error
				int firstIndexIn = (int) Math.floor(c0);
				int  lastIndexEx = (int) Math.ceil (c1);
				pixelFractions[i] = new PixelFract[lastIndexEx-firstIndexIn];
				for (int p=0; p<pixelFractions[i].length; p++) {
					if (currentThread.isInterrupted()) return null;
					int coord = firstIndexIn+p;
					double fract = 1;
					if      (coord  ==firstIndexIn) fract = 1-(c0-firstIndexIn);
					else if (coord+1== lastIndexEx) fract = 1-(lastIndexEx-c1);
					pixelFractions[i][p] = new PixelFract(coord, fract);
				}
			}
			return pixelFractions;
		}

		@SuppressWarnings("unused")
		private void showPixelFractions(BufferedImage image, int targetWidth, int targetHeight, PixelFract[][] pixelFractionsX, PixelFract[][] pixelFractionsY) {
			System.out.printf("PixelFractions:  %d x %d  ->  %d x %d%n", image.getWidth(), image.getHeight(), targetWidth, targetHeight);
			int n = Math.max(pixelFractionsX.length, pixelFractionsY.length);
			for (int i=0; i<n; i++) {
				PixelFract[] fX = i<pixelFractionsX.length ? pixelFractionsX[i] : null;
				PixelFract[] fY = i<pixelFractionsY.length ? pixelFractionsY[i] : null;
				System.out.printf("  [%d]  %20s  %20s%n", i, toString(fX), toString(fY));
			}
		}
		
		private String toString(PixelFract[] fracts) {
			if (fracts==null) return "";
			Iterator<String> iterator = Arrays.stream(fracts).map(pf->String.format(Locale.ENGLISH, "%03d:%1.3f", pf.coord, pf.fract)).iterator();
			return String.join(",", (Iterable<String>)()->iterator);
		}

		private static class PixelFract {
			final int coord;
			final double fract;
			PixelFract(int coord, double fract) {
				this.coord = coord;
				this.fract = fract;
			}
			@Override
			public String toString() {
				return String.format(Locale.ENGLISH, "%d:%1.4f", coord, fract);
			}
			
		}
		
	}
	
	public boolean useInterpolation      () { return useInterpolation      ; }
	public boolean useBetterInterpolation() { return useBetterInterpolation; }
	
	public void useInterpolation      (boolean useInterpolation) {
		this.useInterpolation = useInterpolation;
		if (!this.useInterpolation) {
			useBetterInterpolation = false;
			betterScaling.clearResult();
		}
		repaint();
	}
	
	public void useBetterInterpolation(boolean useBetterInterpolation) {
		this.useBetterInterpolation = useInterpolation && useBetterInterpolation;
		updateBetterInterpolation();
		repaint();
		//System.out.println("useBetterInterpolation: "+useBetterInterpolation);
	}
	
	public void setImage(BufferedImage image) {
		setImage(image, true);
	}
	
	public void setImage(BufferedImage image, boolean resetView) {
		this.image = image;
		if (resetView) reset();
		else repaint();
		updateBetterInterpolation();
	}

	public void setZoom(double zoom) {
		double currentZoom = viewState.convertLength_LengthToScreenF(1.0);
		addZoom(new Point(width/2,height/2), zoom/currentZoom);
	}
	
	public void setBgColor(Color bgColor) {
		setBackground(bgColor, null);	
	}
	public void setBgPattern(BGPattern bgPattern) {
		setBackground(null, bgPattern);	
	}
	public void setBackground(Color bgColor, BGPattern bgPattern) {
		this.bgColor = bgColor;
		this.bgPattern = bgPattern;
		repaint();
	}
	
	@Override
	protected void paintCanvas(Graphics g, int x, int y, int width, int height) {
		//g.setColor(COLOR_BACKGROUND);
		//g.fillRect(x, y, width, height);
		
		if (g instanceof Graphics2D && viewState.isOk()) {
			Graphics2D g2 = (Graphics2D) g;
			
			Shape prevClip = g2.getClip();
			Rectangle viewRect = new Rectangle(x, y, width, height);
			g2.setClip(viewRect);
			setRenderingHints(g2);
			
			if (image!=null) {
				int imageX      = viewState.convertPos_AngleToScreen_LongX(0);
				int imageY      = viewState.convertPos_AngleToScreen_LatY (0);
				int imageWidth  = viewState.convertPos_AngleToScreen_LongX(image.getWidth ()) - imageX;
				int imageHeight = viewState.convertPos_AngleToScreen_LatY (image.getHeight()) - imageY;
				Rectangle imageRect = new Rectangle(imageX, imageY, imageWidth, imageHeight);
				Rectangle viewableImageClip = viewRect.intersection(imageRect);
				
				if (bgColor!=null) {
					g2.setColor(bgColor);
					g2.fillRect(imageX, imageY, imageWidth, imageHeight);
				}
				if (bgPattern!=null && !viewableImageClip.isEmpty()) {
					g2.setClip(viewableImageClip);
					
					BufferedImage pattImg = bgPattern.patternImage;
					int pattWidth  = pattImg.getWidth();
					int pattHeight = pattImg.getHeight();
					int nX = width /pattWidth;
					int nY = height/pattHeight;
					
					for (int iX=0; iX<=nX; iX++) {
						
						int pattX = iX*pattWidth;
						if (pattX >= imageX+imageWidth) break;
						if (pattX+pattWidth <= imageX) continue;
						
						for (int iY=0; iY<=nY; iY++) {
							
							int pattY = iY*pattHeight;
							if (pattY >= imageY+imageHeight) break;
							if (pattY+pattHeight <= imageY) continue;
							
							g2.drawImage(pattImg, pattX, pattY, null);
						}
					}
					g2.setClip(viewRect);
				}
				
				g2.setColor(COLOR_AXIS);
				g2.drawLine(imageX, 0, imageX, height);
				g2.drawLine(0, imageY, width, imageY);
				g2.drawLine(imageX+imageWidth-1, 0, imageX+imageWidth-1, height);
				g2.drawLine(0, imageY+imageHeight-1, width, imageY+imageHeight-1);
				
				Object interpolationValue = null;
				if (useInterpolation && imageWidth<image.getWidth()) {
					interpolationValue = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
					if (useBetterInterpolation) {
						BufferedImage scaledImage = betterScaling.getResult();
						if (scaledImage != null) {
							drawImage(g2, scaledImage, true, viewRect, imageRect);
							//g2.drawImage(scaledImage, imageX, imageY, null);
							interpolationValue = null;
						}
					}
				} else {
					interpolationValue = RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR;
				}
				
				if (interpolationValue!=null) {
					g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, interpolationValue);
					drawImage(g2, image, false, viewRect, imageRect);
					//g2.drawImage(image, imageX, imageY, imageWidth, imageHeight, null);
				}
			}
			
			drawBeforeMapDecoration(g2, x, y, width, height);
			
			drawMapDecoration(g2, x, y, width, height);
			
			for (DrawExtension de : drawExtensions)
				de.draw(g2, x, y, width, height, viewState);
			
			g2.setClip(prevClip);
		}
	}
	
	protected void setRenderingHints(Graphics2D g2)
	{
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	}
	
	protected void drawBeforeMapDecoration(Graphics2D g2, int x, int y, int width, int height)
	{
	}
	
	protected void drawImage(Graphics2D g2, BufferedImage image, boolean imageWasScaled, Rectangle viewRect, Rectangle imageRect)
	{
		if (imageWasScaled)
			g2.drawImage(image, imageRect.x, imageRect.y, null);
		else
			g2.drawImage(image, imageRect.x, imageRect.y, imageRect.width, imageRect.height, null);
	}

	public interface DrawExtension {
		void draw(Graphics2D g2, int x, int y, int width, int height, ViewState viewState);
	}
	
	public void    addDrawExtensions(DrawExtension de) { drawExtensions.   add(de); }
	public void removeDrawExtensions(DrawExtension de) { drawExtensions.remove(de); }
	
	@Override
	protected ViewState createViewState() {
		return new ViewState(this);
	}
	
	public ViewState getViewState() { return viewState; }

	public class ViewState extends ZoomableCanvas.ViewState {
		
		ViewState(ZoomableCanvas<?> canvas) {
			super(canvas,0.1f);
			setPlainMapSurface();
			setVertAxisDownPositive(true);
			//debug_showChanges_scalePixelPerLength = true;
		}

		@Override
		protected void determineMinMax(MapLatLong min, MapLatLong max) {
			min.longitude_x = 0.0;
			min.latitude_y  = 0.0;
			max.longitude_x = image==null ? 100.0 : image.getWidth ();
			max.latitude_y  = image==null ? 100.0 : image.getHeight();
		}
	}
	
	private static class ImageViewContextMenu extends ContextMenu{
		private static final long serialVersionUID = 4090306246829034171L;
		private JCheckBoxMenuItem chkbxBetterInterpolation;
		private ImageView imageView;

		public ImageViewContextMenu(ImageView imageView, boolean predefinedInterpolationLevel, boolean grouped) {
			this.imageView = imageView;
			JCheckBoxMenuItem chkbxInterpolation = null;
			if (!predefinedInterpolationLevel) {
				chkbxBetterInterpolation = createCheckBoxMenuItem("Better Interpolation", imageView.useBetterInterpolation(), b -> {
					imageView.useBetterInterpolation(b);
				});
				chkbxInterpolation = createCheckBoxMenuItem("Interpolation", imageView.useInterpolation(), b -> {
					imageView.useInterpolation(b);
					chkbxBetterInterpolation.setEnabled(b);
				});
			} else
				chkbxBetterInterpolation = null;
			
			MenuWrapper zoomMenu;
			if (grouped) {
				JMenu menu = new JMenu("Zoom");
				add(menu);
				zoomMenu = MenuWrapper.createFor(menu);
			} else {
				zoomMenu = MenuWrapper.createFor(this);
			}
			zoomMenu.add(createMenuItem("10%",e->imageView.setZoom(0.10f)));
			zoomMenu.add(createMenuItem("25%",e->imageView.setZoom(0.25f)));
			zoomMenu.add(createMenuItem("50%",e->imageView.setZoom(0.50f)));
			zoomMenu.add(createMenuItem("75%",e->imageView.setZoom(0.75f)));
			zoomMenu.addSeparator();
			zoomMenu.add(createMenuItem("100%",e->imageView.setZoom(1)));
			zoomMenu.addSeparator();
			zoomMenu.add(createMenuItem("150%",e->imageView.setZoom(1.5f)));
			zoomMenu.add(createMenuItem("200%",e->imageView.setZoom(2.0f)));
			zoomMenu.add(createMenuItem("300%",e->imageView.setZoom(3.0f)));
			zoomMenu.add(createMenuItem("400%",e->imageView.setZoom(4.0f)));
			zoomMenu.add(createMenuItem("600%",e->imageView.setZoom(6.0f)));
			
			if (!grouped) addSeparator();
			
			MenuWrapper bgMenu;
			if (grouped) {
				JMenu menu = new JMenu("Background");
				add(menu);
				bgMenu = MenuWrapper.createFor(menu);
			} else {
				bgMenu = MenuWrapper.createFor(this);
			}
			bgMenu.add(createSetBgPatternMenuItem(imageView, BGPattern.WhiteChecker));
			bgMenu.add(createSetBgPatternMenuItem(imageView, BGPattern.GrayChecker ));
			bgMenu.add(createSetBgPatternMenuItem(imageView, BGPattern.BlackChecker));
			bgMenu.add(createSetBgColorMenuItem(imageView, Color.BLACK  , "Set Background to Black"));
			bgMenu.add(createSetBgColorMenuItem(imageView, Color.WHITE  , "Set Background to White"));
			bgMenu.add(createSetBgColorMenuItem(imageView, Color.MAGENTA, "Set Background to Magenta"));
			bgMenu.add(createSetBgColorMenuItem(imageView, Color.GREEN  , "Set Background to Green"));
			bgMenu.add(createSetBgColorMenuItem(imageView, null         , "Remove Background"));
			
			if (!predefinedInterpolationLevel) {
				if (grouped) {
					JMenu menu = new JMenu("Interpolation");
					menu.add(chkbxInterpolation);
					menu.add(chkbxBetterInterpolation);
					add(menu);
				} else {
					addSeparator();
					add(chkbxInterpolation);
					add(chkbxBetterInterpolation);
				}
			}
			
			if (!grouped) addSeparator();
			add(createMenuItem("Reset View",e->imageView.reset()));
		}
		
		interface MenuWrapper {
			JMenuItem add(JMenuItem mi);
			void addSeparator();
			static MenuWrapper createFor(JPopupMenu menu) {
				return new MenuWrapper() {
					@Override public JMenuItem add(JMenuItem mi) { return menu.add(mi); }
					@Override public void addSeparator() { menu.addSeparator(); }
				};
			}
			static MenuWrapper createFor(JMenu menu) {
				return new MenuWrapper() {
					@Override public JMenuItem add(JMenuItem mi) { return menu.add(mi); }
					@Override public void addSeparator() { menu.addSeparator(); }
				};
			}
		}

		@Override
		public void show(Component invoker, int x, int y) {
			if (chkbxBetterInterpolation!=null)
				chkbxBetterInterpolation.setSelected(imageView.useBetterInterpolation());
			super.show(invoker, x, y);
		}

		private JMenuItem createMenuItem(String title, ActionListener al) {
			JMenuItem comp = new JMenuItem(title);
			if (al!=null) comp.addActionListener(al);
			return comp;
		}

		private JCheckBoxMenuItem createCheckBoxMenuItem(String title, boolean selected, Consumer<Boolean> setValue) {
			JCheckBoxMenuItem comp = new JCheckBoxMenuItem(title,selected);
			if (setValue!=null) comp.addActionListener(e->{
				setValue.accept(comp.isSelected());
			});
			return comp;
		}

		private JMenuItem createSetBgPatternMenuItem(ImageView imageView, BGPattern pattern) {
			JMenuItem comp = createMenuItem(String.format("Set Background to %s", pattern.label), e->imageView.setBackground(null, pattern));
			comp.setIcon(new ColorIcon(pattern,32,16));
			return comp;
		}

		private JMenuItem createSetBgColorMenuItem(ImageView imageView, Color color, String title) {
			JMenuItem comp = createMenuItem(title, e->imageView.setBackground(color, null));
			comp.setIcon(new ColorIcon(color,32,16,3));
			return comp;
		}

		private static class ColorIcon implements Icon {
		
			private final Color color;
			private final BGPattern pattern;
			private final int width;
			private final int height;
			private final int cornerRadius;
		
			ColorIcon(BGPattern pattern, int width, int height) {
				this(null, pattern, width, height, 0);
			}
			ColorIcon(Color color, int width, int height, int cornerRadius) {
				this(color, null, width, height, cornerRadius);
			}
			private ColorIcon(Color color, BGPattern pattern, int width, int height, int cornerRadius) {
				this.color = color;
				this.pattern = pattern;
				this.width = width;
				this.height = height;
				this.cornerRadius = cornerRadius;
			}
			@Override public int getIconWidth () { return width;  }
			@Override public int getIconHeight() { return height; }
		
			@Override
			public void paintIcon(Component c, Graphics g, int x, int y) {
				if (g instanceof Graphics2D) {
					Graphics2D g2 = (Graphics2D) g;
					g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
					
					if (color != null) {
						g2.setColor(color);
						g2.fillRoundRect(x, y, width, height, cornerRadius*2, cornerRadius*2);
						
					} else if (pattern != null) {
						Shape clip = g2.getClip();
						g2.setClip(x, y, width, height);
						g2.drawImage(pattern.patternImage, x, y, null);
						g2.setClip(clip);
						
					} else {
						g2.setColor(Color.BLACK);
						g2.drawRoundRect(x, y, width-1, height-1, cornerRadius*2, cornerRadius*2);
					}
				} else {
					if (color != null) {
						g.setColor(color);
						g.fillRoundRect(x, y, width, height, cornerRadius*2, cornerRadius*2);
						
					} else if (pattern != null) {
						Shape clip = g.getClip();
						g.setClip(x, y, width, height);
						g.drawImage(pattern.patternImage, x, y, null);
						g.setClip(clip);
						
					} else {
						g.setColor(Color.BLACK);
						g.drawRoundRect(x, y, width-1, height-1, cornerRadius*2, cornerRadius*2);
					}
				}
			}
		
		
		}
		
	}
}