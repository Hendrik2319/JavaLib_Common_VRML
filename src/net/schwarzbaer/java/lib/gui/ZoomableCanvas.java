package net.schwarzbaer.java.lib.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.font.FontRenderContext;
import java.awt.geom.Rectangle2D;
import java.util.Locale;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Supplier;

public abstract class ZoomableCanvas<VS extends ZoomableCanvas.ViewState> extends Canvas implements MouseListener, MouseMotionListener, MouseWheelListener {
	private static final long serialVersionUID = -1282219829667604150L;

	protected final VS viewState;
	private Point panStart;

	private Scale mapScale;
	private Axes verticalAxes;
	private Axes horizontalAxes;

	private boolean withTopAxis;
	private boolean withRightAxis;
	private boolean withBottomAxis;
	private boolean withLeftAxis;

	private boolean isEditorMode;
	private int scrollWidth;

	private boolean isFirstSizeChanged;
	
	protected ZoomableCanvas() {
		setPreferredSize(20,50);
		
		zoomListeners = new Vector<>();
		panListeners = new Vector<>();
		viewState = createViewState();
		panStart = null;
		
		mapScale = null;
		verticalAxes = null;
		horizontalAxes = null;
		
		withTopAxis = false;
		withRightAxis = false;
		withBottomAxis = false;
		withLeftAxis = false;
		
		isEditorMode = false;
		scrollWidth = 0;
		
		addMouseListener(this);
		addMouseMotionListener(this);
		addMouseWheelListener(this);
		
		isFirstSizeChanged = true;
	}
	
	@Override
	protected void sizeChanged(int width, int height)
	{
		if (withDebugOutput)
			System.out.printf("ZoomableCanvas.sizeChanged(w:%d, h:%d, first:%s)%n", width, height, isFirstSizeChanged);
		
		super.sizeChanged(width, height);
		
		if (isFirstSizeChanged)
		{
			isFirstSizeChanged=false;
			reset();
		}
	}

	public boolean isEditorMode() { return  isEditorMode; }
	public boolean isViewerMode() { return !isEditorMode; }
	
	public void activateEditorMode() { activateEditorMode(50); }
	public void activateEditorMode(int scrollWidth) {
		this.isEditorMode = true;
		this.scrollWidth = scrollWidth;
		if (this.scrollWidth<=0) throw new IllegalArgumentException();
	}
	
	public void activateViewerMode() {
		isEditorMode = false;
	}
	
	@Override public void mousePressed   (MouseEvent e) { if (!isEditorMode && e.getButton()==MouseEvent.BUTTON1) {   startPan(e.getPoint()); repaint(); } }
	@Override public void mouseDragged   (MouseEvent e) { if (!isEditorMode                                     ) { proceedPan(e.getPoint()); repaint(); }  }
	@Override public void mouseReleased  (MouseEvent e) { if (!isEditorMode && e.getButton()==MouseEvent.BUTTON1) {    stopPan(e.getPoint()); repaint(); }  }
	@Override public void mouseWheelMoved(MouseWheelEvent e) {
		if (!isEditorMode)
			zoom(e.getPoint(),-e.getPreciseWheelRotation());
		else {
			if (e.isControlDown())
				zoom(e.getPoint(),-e.getPreciseWheelRotation());
			else {
				Point p0 = e.getPoint(), p1;
				int sw = -scrollWidth*e.getWheelRotation();
				if (e.isShiftDown()) p1 = new Point(p0.x+sw,p0.y);
				else                 p1 = new Point(p0.x,p0.y+sw);
				startPan(p0);
				stopPan(p1);
				repaint();
			}
		}
	}
	
	@Override public void mouseEntered(MouseEvent e) {}
	@Override public void mouseMoved  (MouseEvent e) {}
	@Override public void mouseExited (MouseEvent e) {}
	@Override public void mouseClicked(MouseEvent e) {}
	
	protected abstract VS createViewState();
	
	protected void addTextToMapScale(Supplier<String[]> textSource) { if (mapScale!=null) mapScale.addAdditionalTextSource(textSource); }
	
	protected void activateMapScale(Color color, String unit                       ) { activateMapScale(color, unit,          1f,         false); }
	protected void activateMapScale(Color color, String unit, double unitScaling   ) { activateMapScale(color, unit, unitScaling,         false); }
	protected void activateMapScale(Color color, String unit, boolean showZoomValue) { activateMapScale(color, unit,          1f, showZoomValue); }
	protected void activateMapScale(Color color, String unit, double unitScaling, boolean showZoomValue) {
		mapScale = new Scale(viewState, color, unit, unitScaling, showZoomValue);
	}
	protected void activateAxes(Color color, boolean withTopAxis, boolean withRightAxis, boolean withBottomAxis, boolean withLeftAxis) {
		activateAxes(color, withTopAxis, withRightAxis, withBottomAxis, withLeftAxis, 1, 1);
	}
	protected void activateAxes(Color color, boolean withTopAxis, boolean withRightAxis, boolean withBottomAxis, boolean withLeftAxis, double vertUnitScaling, double horizUnitScaling) {
		this.withTopAxis = withTopAxis;
		this.withRightAxis = withRightAxis;
		this.withBottomAxis = withBottomAxis;
		this.withLeftAxis = withLeftAxis;
		verticalAxes   = !this.withLeftAxis && !this.withRightAxis ? null : new Axes (viewState, true , color, vertUnitScaling);
		horizontalAxes = !this.withTopAxis && !this.withBottomAxis ? null : new Axes (viewState, false, color, horizUnitScaling);
	}
	
	protected void setMapScaleUnitScaling(double unitScaling) {
		if (mapScale!=null) mapScale.setUnitScaling(unitScaling);
		updateMapScale();
	}
	
	protected void setAxesUnitScaling(double vertUnitScaling, double horizUnitScaling) {
		if (verticalAxes  !=null) verticalAxes  .setUnitScaling(vertUnitScaling);
		if (horizontalAxes!=null) horizontalAxes.setUnitScaling(horizUnitScaling);
		updateAxes();
	}

	public void reset() {
		if (withDebugOutput) System.out.println("ZoomableCanvas.reset()");
		if (viewState.reset()) {
			updateAxes();
			updateMapScale();
		}
		repaint();
	}

	public void update() {
		if (!viewState.isOk())
			reset();
		repaint();
	}

	protected static Rectangle2D getBounds(Graphics2D g2, Font font, String str) {
		return font.getStringBounds(str==null?"":str, g2.getFontRenderContext());
	}

	protected static Rectangle2D getBounds(Graphics2D g2, String str) {
		return g2.getFontMetrics().getStringBounds(str==null?"":str, g2);
	}

	private Point sub(Point p1, Point p2) {
		return new Point(p1.x-p2.x,p1.y-p2.y);
	}

	private void startPan(Point point) {
		panStart = point;
		viewState.tempPanOffset = new Point();
		for (PanListener pl : panListeners) pl.panStarted();
	}

	private void proceedPan(Point point) {
		if (panStart != null)
			viewState.tempPanOffset = sub(point,panStart);
	}

	private void stopPan(Point point) {
		if (panStart!=null)
			if (viewState.pan(sub(point,panStart))) {
				updateAxes();
			}
		
		panStart = null;
		viewState.tempPanOffset = null;
		for (PanListener pl : panListeners) pl.panStopped();
	}

	private void zoom(Point point, double preciseWheelRotation) {
		double f =  Math.pow(1.1f, preciseWheelRotation);
		addZoom(point, f);
	}
	protected void addZoom(Point zoomCenter, double f) {
		if (viewState.zoom(zoomCenter,f)) {
			updateAxes();
			updateMapScale();
			for (ZoomListener zl:zoomListeners) zl.zoomChanged();
			repaint();
		}
	}
	
	public interface PanListener {
		void panStarted();
		void panStopped();
	}
	
	private final Vector<PanListener> panListeners;
	public void    addPanListener(PanListener zl) { panListeners.   add(zl); }
	public void removePanListener(PanListener zl) { panListeners.remove(zl); }
	
	public interface ZoomListener {
		void zoomChanged();
	}
	
	private final Vector<ZoomListener> zoomListeners;
	public void    addZoomListener(ZoomListener zl) { zoomListeners.   add(zl); }
	public void removeZoomListener(ZoomListener zl) { zoomListeners.remove(zl); }

	private void updateMapScale() {
		if (mapScale!=null) mapScale.update();
	}

	private void updateAxes() {
		if (viewState.isOk()) {
			if (verticalAxes  !=null)   verticalAxes.updateTicks();
			if (horizontalAxes!=null) horizontalAxes.updateTicks();
		}
	}
	
	protected void drawMapDecoration(Graphics2D g2, int x, int y, int width, int height) {
		if (  withLeftAxis)   verticalAxes.drawAxis ( g2, x+5       , y+20, height-40, true  );
		if ( withRightAxis)   verticalAxes.drawAxis ( g2, x+width -5, y+20, height-40, false );
		if (   withTopAxis) horizontalAxes.drawAxis ( g2, y+5       , x+20, width -40, true  );
		if (withBottomAxis) horizontalAxes.drawAxis ( g2, y+height-5, x+20, width -40, false );
		if (mapScale!=null) mapScale      .drawScale( g2, x+width-110, y+height-50, 60,15 );
	}

	public static class MapLatLong {
		
		public Double latitude_y;
		public Double longitude_x;
		
		public MapLatLong() {
			this.latitude_y = null;
			this.longitude_x = null;
		}
		
		public MapLatLong(double latitude_y, double longitude_x) {
			this.latitude_y = latitude_y;
			this.longitude_x = longitude_x;
		}
	
		public MapLatLong(MapLatLong other) {
			this.latitude_y = other.latitude_y;
			this.longitude_x = other.longitude_x;
		}
	
		@Override
		public String toString() {
			return String.format("MapLatLong [latitude=%s, longitude=%s]", latitude_y, longitude_x);
		}
	
		public void setMin(MapLatLong location) {
			if (location.latitude_y!=null) {
				if (latitude_y==null) latitude_y = location.latitude_y;
				else latitude_y = Math.min( latitude_y, location.latitude_y );
			}
			if (location.longitude_x!=null) {
				if (longitude_x==null) longitude_x = location.longitude_x;
				else longitude_x = Math.min( longitude_x, location.longitude_x );
			}
		}
	
		public void setMax(MapLatLong location) {
			if (location.latitude_y!=null) {
				if (latitude_y==null) latitude_y = location.latitude_y;
				else latitude_y = Math.max( latitude_y, location.latitude_y );
			}
			if (location.longitude_x!=null) {
				if (longitude_x==null) longitude_x = location.longitude_x;
				else longitude_x = Math.max( longitude_x, location.longitude_x );
			}
		}
		
	}

	public static abstract class ViewState {
		
		private ZoomableCanvas<?> canvas;
		
		Point tempPanOffset;
		protected MapLatLong center;
		private double scalePixelPerLength;
		private double scaleLengthPerAngleLatY;
		private double scaleLengthPerAngleLongX;
		private double lowerZoomLimit;

		private boolean mapIsSpherical;
		private double sphereRadius;
		private double fixedMapScalingX;
		private double fixedMapScalingY;
		private double vAxisSign;
		private double hAxisSign;

		protected boolean debug_showChanges_scalePixelPerLength;


		protected ViewState(ZoomableCanvas<?> canvas, double lowerZoomLimit) {
			this.canvas = canvas;
			this.lowerZoomLimit = lowerZoomLimit;
			tempPanOffset = null;
			clearValues();
			
			mapIsSpherical = false;
			sphereRadius = Double.NaN;
			fixedMapScalingX = 1;
			fixedMapScalingY = 1;
			vAxisSign = -1;
			hAxisSign =  1;
			
			debug_showChanges_scalePixelPerLength = false;
		}
		
		public void setSphericalMapSurface(double sphereRadius) {
			this.mapIsSpherical = true;
			this.sphereRadius     = sphereRadius;
			this.fixedMapScalingX = Double.NaN;
			this.fixedMapScalingY = Double.NaN;
		}
		
		public void setPlainMapSurface() { setPlainMapSurface(1,1); }
		public void setPlainMapSurface(double fixedMapScalingX, double fixedMapScalingY) {
			this.mapIsSpherical = false;
			this.sphereRadius     = Double.NaN;
			this.fixedMapScalingX = fixedMapScalingX;
			this.fixedMapScalingY = fixedMapScalingY;
		}
		
		public void setVertAxisDownPositive(boolean vertAxisIsDownPositive) {
			vAxisSign = !mapIsSpherical && vertAxisIsDownPositive ? 1 : -1;
		}
		
		public void setHorizAxisRightPositive(boolean horizAxisIsRightPositive) {
			hAxisSign = !mapIsSpherical && horizAxisIsRightPositive ? 1 : -1;
		}
		
		protected void clearValues() {
			center = null;
			scaleLengthPerAngleLatY  = Double.NaN;
			scaleLengthPerAngleLongX = Double.NaN;
			scalePixelPerLength      = Double.NaN;
			if (debug_showChanges_scalePixelPerLength) System.out.printf(Locale.ENGLISH, "reset -> scalePixelPerLength: %f %n", scalePixelPerLength);
		}

		public boolean haveScalePixelPerLength() {
			return !Double.isNaN(scalePixelPerLength);
		}
		
		public boolean isOk() {
			return center!=null && haveScalePixelPerLength() && !Double.isNaN(scaleLengthPerAngleLatY) && !Double.isNaN(scaleLengthPerAngleLongX);
		}

		protected abstract void  determineMinMax(MapLatLong min, MapLatLong max);
		
		protected boolean reset() {
			
			if (canvas.width==0 || canvas.height==0) {
				clearValues();
				return false;
			}
			
			MapLatLong min = new MapLatLong();
			MapLatLong max = new MapLatLong();
			determineMinMax(min, max);
			
			if (min.latitude_y==null || min.longitude_x==null || max.latitude_y==null || max.longitude_x==null ) {
				clearValues();
				return false;
			}
			
			center = new MapLatLong( (min.latitude_y+max.latitude_y)/2, (min.longitude_x+max.longitude_x)/2 );
			
			updateScaleLengthPerAngle();
			double neededHeight = (max.latitude_y -min.latitude_y )*scaleLengthPerAngleLatY;
			double neededWidth  = (max.longitude_x-min.longitude_x)*scaleLengthPerAngleLongX;
			if (neededHeight==0 || neededWidth==0) {
				clearValues();
				return false;
			}
			
			double scalePixelPerLengthLatY  = (canvas.height-30) / neededHeight;
			double scalePixelPerLengthLongY = (canvas.width -30) / neededWidth;
			scalePixelPerLength = Math.min(scalePixelPerLengthLatY, scalePixelPerLengthLongY);
			if (debug_showChanges_scalePixelPerLength) System.out.printf(Locale.ENGLISH, "reset -> scalePixelPerLength: %f %n", scalePixelPerLength);
			if (scalePixelPerLength<lowerZoomLimit) {
				scalePixelPerLength = lowerZoomLimit;
				if (debug_showChanges_scalePixelPerLength) System.out.printf(Locale.ENGLISH, "reset[Limit] -> scalePixelPerLength: %f %n", scalePixelPerLength);
			}
			
			return true;
		}

		boolean zoom(Point point, double f) {
			if (!isOk()) return false;
			
			MapLatLong centerOld = new MapLatLong(center);
			MapLatLong location = convertScreenToAngle(point);
			
			if (scalePixelPerLength*f < lowerZoomLimit) {
				if (debug_showChanges_scalePixelPerLength) System.out.printf(Locale.ENGLISH, "zoom[Limit] -> scalePixelPerLength: %f %n", scalePixelPerLength);
				return false;
			}
			
			scalePixelPerLength *= f;
			if (debug_showChanges_scalePixelPerLength) System.out.printf(Locale.ENGLISH, "zoom -> scalePixelPerLength: %f %n", scalePixelPerLength);
			
			//System.out.printf(Locale.ENGLISH, "zoom( Point[%d,%d], %1.4f) -> MapLatLong[ lat:%1.3f, long:%1.3f ] -> scalePixelPerLength: %1.3f %n", point.x,point.y, f, location.latitude,location.longitude, scalePixelPerLength);
			//System.out.printf(Locale.ENGLISH, "OLD: center:[ lat:%1.3f, long:%1.3f ] scaleLengthPerAngle:[ lat:%1.3f, long:%1.3f ] %n", center.latitude,center.longitude, scaleLengthPerAngleLat, scaleLengthPerAngleLong);
			
			center.latitude_y  = (centerOld.latitude_y  - location.latitude_y ) / f + location.latitude_y;
			double sphericalCorrection = mapIsSpherical ? Math.cos(centerOld.latitude_y/180*Math.PI) / Math.cos(center.latitude_y/180*Math.PI) : 1;
			center.longitude_x = (centerOld.longitude_x - location.longitude_x) * sphericalCorrection / f + location.longitude_x;
			updateScaleLengthPerAngle();
			//System.out.printf(Locale.ENGLISH, "NEW: center:[ lat:%1.3f, long:%1.3f ] scaleLengthPerAngle:[ lat:%1.3f, long:%1.3f ] %n", center.latitude,center.longitude, scaleLengthPerAngleLat, scaleLengthPerAngleLong);
			
			return true;
		}
	
		boolean pan(Point offsetOnScreen) {
			if (!isOk()) return false;
			
			center.latitude_y  -= convertLength_ScreenToAngle_LatY (vAxisSign*offsetOnScreen.y);
			center.longitude_x -= convertLength_ScreenToAngle_LongX(hAxisSign*offsetOnScreen.x);
			updateScaleLengthPerAngle();
			
			return true;
		}
	
		private void updateScaleLengthPerAngle() {
			if (mapIsSpherical) {
				scaleLengthPerAngleLatY  = 2*Math.PI*sphereRadius / 360;
				scaleLengthPerAngleLongX = 2*Math.PI*sphereRadius / 360 * Math.cos(center.latitude_y/180*Math.PI);
			} else {
				scaleLengthPerAngleLatY  = fixedMapScalingY;
				scaleLengthPerAngleLongX = fixedMapScalingX;
			}
		}

		public double convertLength_ScreenToLength(double length_px) {
			return length_px / scalePixelPerLength;
		}
		
		public Integer convertLength_LengthToScreen(Double length_u) {
			Double length_px = convertLength_LengthToScreenF(length_u);
			if (length_px==null) return null;
			return (int) Math.round( length_px );
		}
		public Double convertLength_LengthToScreenF(Double length_u) {
			if (length_u==null || Double.isNaN(length_u)) return null;
			return length_u * scalePixelPerLength;
		}

		public Point convertPos_AngleToScreen(MapLatLong location) {
			if (location==null || location.latitude_y==null || location.longitude_x==null) return null;
			return convertPos_AngleToScreen(location.longitude_x, location.latitude_y);
		}
		public Point convertPos_AngleToScreen(double longitude_x, double latitude_y) {
			return new Point(
				convertPos_AngleToScreen_LongX(longitude_x),
				convertPos_AngleToScreen_LatY (latitude_y )
			);
		}
		
		public double convertPos_AngleToScreen_LongXf(double longitude_x) {
			double x = canvas.width /2f + hAxisSign * convertLength_AngleToScreen_LongX(longitude_x - center.longitude_x);
			if (tempPanOffset!=null) x += tempPanOffset.x;
			return x;
		}

		public double convertPos_AngleToScreen_LatYf(double latitude_y) {
			double y = canvas.height/2f + vAxisSign * convertLength_AngleToScreen_LatY (latitude_y  - center.latitude_y );
			if (tempPanOffset!=null) y += tempPanOffset.y;
			return y;
		}

		public int convertPos_AngleToScreen_LongX(double longitude_x) { return (int) Math.round(convertPos_AngleToScreen_LongXf(longitude_x)); }
		public int convertPos_AngleToScreen_LatY (double  latitude_y) { return (int) Math.round(convertPos_AngleToScreen_LatYf ( latitude_y)); }

		private double convertLength_AngleToScreen_LongX(double length_a) { return length_a * scaleLengthPerAngleLongX * scalePixelPerLength; }
		private double convertLength_AngleToScreen_LatY (double length_a) { return length_a * scaleLengthPerAngleLatY  * scalePixelPerLength; }
		
		public MapLatLong convertScreenToAngle(Point point) {
			return new MapLatLong(
				convertPos_ScreenToAngle_LatY (point.y),
				convertPos_ScreenToAngle_LongX(point.x)
			);
		}
		public double convertPos_ScreenToAngle_LongX(int x) {
			if (tempPanOffset!=null) x -= tempPanOffset.x;
			return center.longitude_x + hAxisSign * convertLength_ScreenToAngle_LongX(x - canvas.width /2f);
		}
		public double convertPos_ScreenToAngle_LatY(int y) {
			if (tempPanOffset!=null) y -= tempPanOffset.y;
			return center.latitude_y  + vAxisSign * convertLength_ScreenToAngle_LatY (y - canvas.height/2f);
		}
		public double convertLength_ScreenToAngle_LongX(double length_px) { return length_px / scalePixelPerLength / scaleLengthPerAngleLongX; }
		public double convertLength_ScreenToAngle_LatY (double length_px) { return length_px / scalePixelPerLength / scaleLengthPerAngleLatY ; }
	}

	private static class Axes {
		private static final int minMinorTickUnitLength_px = 7;
		private static final int majorTickLength_px = 10;
		private static final int minorTickLength_px = 4;
		
		private Double majorTickUnit_a = null;
		private Double minorTickUnit_a = null;
		private Integer minorTickCount = null;
		private int precision = 1;
		
		private boolean isVertical;
		private ViewState viewState;
		private Color axisColor;
		private double unitScaling;
		
		Axes(ViewState viewState, boolean isVertical, Color axisColor, double unitScaling) {
			this.viewState = viewState;
			this.isVertical = isVertical;
			this.axisColor = axisColor;
			this.unitScaling = unitScaling;
		}
		
		void setUnitScaling(double unitScaling) {
			this.unitScaling = unitScaling;
		}

		private String toString(double angle) {
			return String.format(Locale.ENGLISH, "%1."+precision+"f", angle);
		}
		
		void updateTicks() {
			double minMinorTickUnitLength_a;
			if (isVertical) minMinorTickUnitLength_a = viewState.convertLength_ScreenToAngle_LatY (minMinorTickUnitLength_px) * unitScaling;
			else            minMinorTickUnitLength_a = viewState.convertLength_ScreenToAngle_LongX(minMinorTickUnitLength_px) * unitScaling;
			
			//System.out.printf("updateTicks(%s): minMinorTickUnitLength_a = %s%n", isVertical ? "Vertical" : "Horizontal", minMinorTickUnitLength_a);
			majorTickUnit_a = 1d;
			minorTickCount = 5; // minorTickUnit_a = 0.2
			precision = 0;
			while (0 < minMinorTickUnitLength_a && majorTickUnit_a/10 > minMinorTickUnitLength_a) {
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2; // majorTickUnit_a = 0.5
					minorTickCount = 5;   // minorTickUnit_a = 0.1
					precision += 1;
				}
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2.5f; // majorTickUnit_a = 0.2
					minorTickCount = 4;      // minorTickUnit_a = 0.05
				}
				
				if (majorTickUnit_a/10 > minMinorTickUnitLength_a) {
					majorTickUnit_a /= 2; // majorTickUnit_a = 0.1
					minorTickCount = 5;   // minorTickUnit_a = 0.02
				}
			};
			while (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2; // majorTickUnit_a = 2
					minorTickCount = 4;   // minorTickUnit_a = 0.5
				}
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2.5f; // majorTickUnit_a = 5
					minorTickCount = 5;      // minorTickUnit_a = 1
				}
				
				if (majorTickUnit_a/minorTickCount < minMinorTickUnitLength_a) {
					majorTickUnit_a *= 2; // majorTickUnit_a = 10
					minorTickCount = 5;   // minorTickUnit_a = 2
				}
			};
			minorTickUnit_a = majorTickUnit_a/minorTickCount;
			//System.out.printf("updateTicks(): majorTickUnit_a = %s, minorTickCount = %s, precision = %s%n", majorTickUnit_a, minorTickCount, precision);
		}

		void drawAxis(Graphics2D g2, int c0, int c1, int width1, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			if (width1<0) return; // display area too small
			
			double minAngle_a,maxAngle_a,angleWidth_a;
			if (isVertical) minAngle_a = viewState.convertPos_ScreenToAngle_LatY (c1) * unitScaling;
			else            minAngle_a = viewState.convertPos_ScreenToAngle_LongX(c1) * unitScaling;
			if (isVertical) maxAngle_a = viewState.convertPos_ScreenToAngle_LatY (c1+width1) * unitScaling;
			else            maxAngle_a = viewState.convertPos_ScreenToAngle_LongX(c1+width1) * unitScaling;
			
			if (maxAngle_a<minAngle_a) {
				angleWidth_a = minAngle_a; // angleWidth_a  used as temp. storage
				minAngle_a = maxAngle_a;
				maxAngle_a = angleWidth_a;
			}
			angleWidth_a = maxAngle_a-minAngle_a;
			
			
			double firstMajorTick_a = Math.ceil(minAngle_a / majorTickUnit_a) * majorTickUnit_a;
			
			g2.setPaint(axisColor);
			if (isVertical) g2.drawLine(c0, c1, c0, c1+width1);
			else            g2.drawLine(c1, c0, c1+width1, c0);
			
			for (int j=1; minAngle_a < firstMajorTick_a-j*minorTickUnit_a; j++)
				drawMinorTick( g2, c0, firstMajorTick_a - j*minorTickUnit_a, labelsRightBottom );
			
			for (int i=0; firstMajorTick_a+i*majorTickUnit_a < maxAngle_a; i++) {
				double majorTick_a = firstMajorTick_a + i*majorTickUnit_a;
				drawMajorTick( g2, c0, majorTick_a, labelsRightBottom );
				for (int j=1; j<minorTickCount && majorTick_a + j*minorTickUnit_a < maxAngle_a; j++)
					drawMinorTick( g2, c0, majorTick_a + j*minorTickUnit_a, labelsRightBottom );
			}
		}
	
		private void drawMajorTick(Graphics2D g2, int c0, double angle, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			int c1;
			if (isVertical) c1 = viewState.convertPos_AngleToScreen_LatY (angle/unitScaling);
			else            c1 = viewState.convertPos_AngleToScreen_LongX(angle/unitScaling);
			
			int halfTick = majorTickLength_px/2;
			int tickLeft  = halfTick;
			int tickRight = halfTick;
			if (labelsRightBottom) tickLeft = 0;
			else                   tickRight = 0;
			if (isVertical) g2.drawLine(c0-tickLeft, c1, c0+tickRight, c1);
			else            g2.drawLine(c1, c0-tickLeft, c1, c0+tickRight);
			
			String label = toString(angle);
			Rectangle2D bounds = g2.getFontMetrics().getStringBounds(label, g2);
			
			if (isVertical) {
				if (labelsRightBottom) g2.drawString(label, (float)(c0-bounds.getX()+halfTick+4                  ), (float)(c1-bounds.getY()-bounds.getHeight()/2));
				else                   g2.drawString(label, (float)(c0-bounds.getX()-halfTick-4-bounds.getWidth()), (float)(c1-bounds.getY()-bounds.getHeight()/2));
			} else {
				if (labelsRightBottom) g2.drawString(label, (float)(c1-bounds.getX()-bounds.getWidth()/2), (float)(c0-bounds.getY()+halfTick+4                   ));
				else                   g2.drawString(label, (float)(c1-bounds.getX()-bounds.getWidth()/2), (float)(c0-bounds.getY()-halfTick-4-bounds.getHeight()));
			}
		}
	
		private void drawMinorTick(Graphics2D g2, int c0, double angle, boolean labelsRightBottom) {
			//   isVertical:  c0 = x, c1 = y, width1 = height
			// ! isVertical:  c0 = y, c1 = x, width1 = width
			int c1;
			if (isVertical) c1 = viewState.convertPos_AngleToScreen_LatY (angle/unitScaling);
			else            c1 = viewState.convertPos_AngleToScreen_LongX(angle/unitScaling);
			
			int tickLeft  = minorTickLength_px/2;
			int tickRight = minorTickLength_px/2;
			if (labelsRightBottom) tickLeft  = 0;
			else                   tickRight = 0;
			if (isVertical) g2.drawLine(c0-tickLeft, c1, c0+tickRight, c1);
			else            g2.drawLine(c1, c0-tickLeft, c1, c0+tickRight);
		}
	}

	private static class Scale {
		
		private static final int minScaleLength_px = 50;
	
		private ViewState viewState;

		private double scaleLength_u;
		private int   scaleLength_px;
		private Color scaleColor;
		private String unit;
		private double unitScaling;
		private double zoomValue;
		private boolean showZoomValue;
		private Supplier<String[]> additionalTextSource;
	
		Scale(ViewState viewState, Color scaleColor, String unit, double unitScaling, boolean showZoomValue) {
			this.viewState = viewState;
			this.scaleColor = scaleColor;
			this.unit = unit;
			this.unitScaling = unitScaling;
			this.showZoomValue = showZoomValue;
			scaleLength_px = minScaleLength_px;
			scaleLength_u = 1;
			additionalTextSource = null;
		}
		
		void addAdditionalTextSource(Supplier<String[]> additionalTextSource)
		{
			this.additionalTextSource = additionalTextSource;
		}

		void setUnitScaling(double unitScaling) {
			this.unitScaling = unitScaling;
		}

		void update() {
			if (!viewState.haveScalePixelPerLength()) return;
			
			scaleLength_u = 1;
			
			if (( viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) )
				while ( viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) {
					double base = scaleLength_u;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u = 1.5f*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    2*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    3*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    4*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    5*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    6*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =    8*base;
					if (viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling) < minScaleLength_px) scaleLength_u =   10*base;
				}
			else
				while ( viewState.convertLength_LengthToScreen(scaleLength_u*0.80f/unitScaling) > minScaleLength_px) {
					double base = scaleLength_u;
					if (viewState.convertLength_LengthToScreen(base*0.80f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.80f;
					if (viewState.convertLength_LengthToScreen(base*0.60f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.60f;
					if (viewState.convertLength_LengthToScreen(base*0.50f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.50f;
					if (viewState.convertLength_LengthToScreen(base*0.40f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.40f;
					if (viewState.convertLength_LengthToScreen(base*0.30f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.30f;
					if (viewState.convertLength_LengthToScreen(base*0.20f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.20f;
					if (viewState.convertLength_LengthToScreen(base*0.15f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.15f;
					if (viewState.convertLength_LengthToScreen(base*0.10f/unitScaling) > minScaleLength_px) scaleLength_u = base*0.10f;
				}
			scaleLength_px = viewState.convertLength_LengthToScreen(scaleLength_u/unitScaling);
			zoomValue = viewState.convertLength_LengthToScreenF(1.0);
		}
		
		private String getScaleLengthStr() {
			double f = scaleLength_u;
			//double f = scaleLength_u*unitScaling;
			if (f<0.002) return String.format(Locale.ENGLISH, "%1.5f%s", f, unit);
			if (f<0.02 ) return String.format(Locale.ENGLISH, "%1.4f%s", f, unit);
			if (f<0.2  ) return String.format(Locale.ENGLISH, "%1.3f%s", f, unit);
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.2f%s", f, unit);
			if (f<1000 ) return String.format(Locale.ENGLISH, "%1.0f%s", f, unit);
			f /= 1000;
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.1fk%s", f, unit);
			if (f<1000 ) return String.format(Locale.ENGLISH, "%1.0fk%s", f, unit);
			f /= 1000;
			if (f<2    ) return String.format(Locale.ENGLISH, "%1.1fM%s", f, unit);
			else         return String.format(Locale.ENGLISH, "%1.0fM%s", f, unit);
		}
		
		void drawScale(Graphics2D g2, int x, int y, int w, int h) {
			//g2.setColor(Color.RED);
			//g2.drawRect(x, y, w, h);
			
			g2.setColor(scaleColor);
			
			int x2 = x+w;
			int y2 = y+h;
			int x1 = x2-scaleLength_px;
			g2.drawLine(x2, y , x2, y2);
			g2.drawLine(x2, y2, x1, y2);
			g2.drawLine(x1, y2, x1, y );
			
			String str = getScaleLengthStr();
			Rectangle2D bounds = getBounds(g2, str);
			
			float textX = (float) (x2-bounds.getX()-bounds.getWidth ()-3);
			float textY = (float) (y2-bounds.getY()-bounds.getHeight()-3);
			g2.drawString( str, textX, textY );
			
			double textBottom = y;
			if (showZoomValue) {
				str = String.format(Locale.ENGLISH, "%1.1f%%", zoomValue*100);
				Rectangle2D bounds2 = getBounds(g2, str);
				textBottom -= bounds2.getHeight();
				float textX2 = (float) (x2-bounds2.getWidth()-bounds2.getX()-3);
				float textY2 = (float) (textBottom           -bounds2.getY()-3);
				g2.drawString( str, textX2, textY2 );
			}
			
			if (additionalTextSource!=null)
			{
				String[] strings = additionalTextSource.get();
				for (int i=strings.length-1; 0<=i; i--)
				{
					String str_ = strings[i];
					Rectangle2D bounds2 = getBounds(g2, str_);
					textBottom -= bounds2.getHeight();
					float textX2 = (float) (x2-bounds2.getWidth()-bounds2.getX()-3);
					float textY2 = (float) (textBottom           -bounds2.getY()-3);
					g2.drawString( str_, textX2, textY2 );
				}
			}
		}
	}

	public static class TextBox {
		
		private enum AxisPos {
			Left, Right, Top, Bottom, Center
		}
		
		public enum Anchor {
			TopLeft    (AxisPos.Top   , AxisPos.Left  ),
			Top        (AxisPos.Top   , AxisPos.Center),
			TopRight   (AxisPos.Top   , AxisPos.Right ),
			Left       (AxisPos.Center, AxisPos.Left  ),
			Center     (AxisPos.Center, AxisPos.Center),
			Right      (AxisPos.Center, AxisPos.Right ),
			BottomLeft (AxisPos.Bottom, AxisPos.Left  ),
			Bottom     (AxisPos.Bottom, AxisPos.Center),
			BottomRight(AxisPos.Bottom, AxisPos.Right ),
			;
			private final AxisPos axisPosX;
			private final AxisPos axisPosY;
			private Anchor(AxisPos axisPosY, AxisPos axisPosX) {
				this.axisPosX = axisPosX;
				this.axisPosY = axisPosY;
			}
		}
		
		private String[] texts;
		private Rectangle2D[] bounds;
		private boolean isEnabled;
		protected int x;
		protected int y;
		private Color borderColor;
		private Color fillColor;
		private Color textColor;
		private int offsetX;
		private int offsetY;
		private TextBox.Anchor anchor;
		private int paddingX;
		private int paddingY;
		private int textOffsetX;
		private int textOffsetY;
		private int rowHeight;

		public TextBox(String... texts) {
			this(Anchor.TopLeft, texts);
		}
		public TextBox(Anchor anchor, String... texts) {
			setText(texts);
			setPos(0,0);
			setOffset(0, 0);
			setAnchor(anchor);
			setEnabled(true);
			setColors(Color.GRAY, new Color(0xFFF9D7), Color.BLACK);
			setPadding(5,2);
			setTextOffset(0,0);
			setRowHeight(15);
		}
		
		public void setText(String... texts)
		{
			//texts = new String[] {"line 1", "line2 txt txt", "line3 txt" };
			this.texts = removeNulls(Objects.requireNonNull(texts));
			bounds = null;
		}

		private static String[] removeNulls(String[] texts)
		{
			int count = 0;
			for (String str : texts)
				if (str!=null)
					count++;
			
			String[] result = new String[count];
			
			int i=0;
			for (String str : texts)
				if (str!=null)
					result[i++] = str;
			
			return result;
		}
		
		public void setPos(int x, int y) { this.x = x; this.y = y; }

		public void setEnabled(boolean enabled) { this.isEnabled = enabled; }
		public boolean isEnabled() { return isEnabled; }

		public void setRowHeight(int rowHeight)
		{
			this.rowHeight = rowHeight;
		}

		public void setTextOffset(int textOffsetX, int textOffsetY)
		{
			this.textOffsetX = textOffsetX;
			this.textOffsetY = textOffsetY;
		}

		public void setPadding(int paddingX, int paddingY)
		{
			this.paddingX = paddingX;
			this.paddingY = paddingY;
			bounds = null;
		}

		public void setOffset(int offsetX, int offsetY)
		{
			this.offsetX = offsetX;
			this.offsetY = offsetY;
		}

		public void setAnchor(TextBox.Anchor anchor)
		{
			this.anchor = anchor;
		}

		public void setColors(Color borderColor, Color fillColor, Color textColor) {
			this.borderColor = borderColor;
			this.fillColor = fillColor;
			this.textColor = textColor;
		}

		public void draw(Graphics2D g2) {
			if (texts.length==0)
				return;
			
			if (borderColor!=null) draw(g2,0);
			if (fillColor  !=null) draw(g2,1);
			if (textColor  !=null) draw(g2,2);
		}

		public void draw(Graphics2D g2, int stage)
		{
			switch (stage)
			{
				case -1: break;
				case 0: g2.setColor(borderColor); break;
				case 1: g2.setColor(fillColor  ); break;
				case 2: g2.setColor(textColor  ); break;
			}
			
			if (bounds == null) {
				bounds = new Rectangle2D[texts.length];
				Font font = g2.getFont();
				FontRenderContext frc = g2.getFontRenderContext();
				for (int i=0; i<texts.length; i++)
				{
					bounds[i] = font.getStringBounds(texts[i], frc);
					bounds[i].setRect(
							bounds[i].getX()-paddingX-textOffsetX,
							bounds[i].getY()-paddingY-textOffsetY,
							bounds[i].getWidth ()+2*paddingX,
							bounds[i].getHeight()+2*paddingY
						);
				}
			}
			
			forEachRow((i,boxX,boxY,boxW,boxH,strX,strY) -> {
				switch (stage)
				{
					case -1:
						if (borderColor!=null) { g2.setColor(borderColor); g2.drawRect(x+boxX-1, y+boxY-1, boxW+1, boxH+1); }
						if (fillColor  !=null) { g2.setColor(fillColor  ); g2.fillRect(x+boxX, y+boxY, boxW, boxH); }
						if (textColor  !=null) { g2.setColor(textColor  ); g2.drawString(texts[i], x+strX, y+strY); }
						break;
					case 0:
						g2.drawRect(x+boxX-1, y+boxY-1, boxW+1, boxH+1);
						break;
					case 1:
						g2.fillRect(x+boxX, y+boxY, boxW, boxH);
						break;
					case 2:
						g2.drawString(texts[i], x+strX, y+strY);
						break;
				}
				return true;
			});
		}
		
		protected interface RowAction
		{
			boolean perfom(int i, int boxX, int boxY, int boxW, int boxH, int strX, int strY);
		}

		protected void forEachRow(RowAction action)
		{
			if (bounds == null)
				throw new IllegalStateException();
			
			int totalHeight = (texts.length-1)*rowHeight + (int) Math.round( bounds[bounds.length-1].getHeight() );
			
			int localOffsetY = offsetY;
			if (anchor.axisPosY==AxisPos.Bottom)
				localOffsetY -= totalHeight;
			else if (anchor.axisPosY==AxisPos.Center)
				localOffsetY -= totalHeight/2;
			
			for (int i=0; i<texts.length; i++)
			{
				int boxX = (int) Math.round( bounds[i].getX() );
				int boxY = (int) Math.round( bounds[i].getY() );
				int boxW = (int) Math.round( bounds[i].getWidth() );
				int boxH = (int) Math.round( bounds[i].getHeight() );
				
				int localOffsetX = offsetX;
				if (anchor.axisPosX==AxisPos.Right)
					localOffsetX -= boxW;
				else if (anchor.axisPosX==AxisPos.Center)
					localOffsetX -= boxW/2;
				
				int strX = localOffsetX-boxX; boxX = localOffsetX;
				int strY = localOffsetY-boxY; boxY = localOffsetY;
				
				boolean continueLoop = action.perfom(i, boxX, boxY, boxW, boxH, strX, strY);
				if (!continueLoop) break;
				
				localOffsetY += rowHeight;
			}
		}
	}
}
