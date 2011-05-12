package net.schwarzbaer.java.lib.image.bumpmapping;

import java.awt.Color;
import java.util.Arrays;

import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Colorizer;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.MutableNormal;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Normal;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.NormalFunctionBase;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.NormalXY;

public interface NormalFunction extends NormalFunctionBase {
	public void forceNormalCreation(boolean force);
	
	public static class Simple implements NormalFunction {
		public interface Fcn { public Normal getNormal(double x, double y, double width, double height); }
		private Fcn fcn;
		public Simple(Fcn fcn) {
			this.fcn = fcn;
			Debug.Assert(this.fcn!=null);
		}
		@Override public Normal getNormal(double x, double y, double width, double height) {
			Normal normal = fcn.getNormal(x, y, width, height);
			Debug.Assert(normal!=null);
			return normal;
		}
		@Override public void forceNormalCreation(boolean force) {}
		
	}
	
	public static class NormalMap implements NormalFunction, ExtraNormalFunction.Host {
		
		protected NormalMapData normalMap;
		protected boolean forceNormalCreation;
		protected boolean centered;
		protected boolean showExtrasOnly;
		protected ExtraNormalFunction extras;
		
		public interface Constructor { NormalMap create(NormalMapData normalMap, boolean centered, boolean forceNormalCreation); }
		
		public static class NormalMapData {
			private final Normal[][] data;
			private final int width;
			private final int height;
			public NormalMapData(int width, int height) {
				this.width = width;
				this.height = height;
				data = new Normal[width][height];
				for (int x=0; x<data.length; x++)
					Arrays.fill(data[x],null);
			}
			public Normal get(int x, int y) {
				if (x<0 || x>=width ) return null;
				if (y<0 || y>=height) return null;
				return data[x][y];
			}
			public void set(int x, int y, Normal n) {
				if (x<0 || x>=width ) return;
				if (y<0 || y>=height) return;
				data[x][y] = n;
			}
		}
		
		public NormalMap(NormalMapData normalMap, boolean centered) {
			this(normalMap, centered, true);
		}
		public NormalMap(NormalMapData normalMap, boolean centered, boolean forceNormalCreation) {
			this.normalMap = normalMap;
			this.centered = centered;
			this.forceNormalCreation = forceNormalCreation;
			showExtrasOnly = false;
		}
		
		@Override
		public void showExtrasOnly(boolean showExtrasOnly) {
			this.showExtrasOnly = showExtrasOnly;
		}

		@Override
		public NormalMap setExtras(ExtraNormalFunction extras) {
			this.extras = extras;
			Debug.Assert(this.extras!=null);
			return this;
		}

		public Normal getNormal(int x, int y) {
			return normalMap.get(x, y);
		}
		
		@Override
		public Normal getNormal(double x, double y, double width, double height) {
			int xi = (int) Math.round(x + (centered ? (normalMap.width -width )/2 : 0));
			int yi = (int) Math.round(y + (centered ? (normalMap.height-height)/2 : 0));
			Normal n = normalMap.get(xi, yi);
			if (n==null && forceNormalCreation) n = new Normal(0,0,1);
			Normal n_ = n;
			return ExtraNormalFunction.Host.getMergedNormal(x,y,width,height, showExtrasOnly, forceNormalCreation, extras, (x_,y_,w_,h_) -> n_);
		}
		@Override public void forceNormalCreation(boolean forceNormalCreation) {
			this.forceNormalCreation = forceNormalCreation;
		}
		
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale) {
			return createFromHeightMap(heightMap,cornerScale, NormalMap::new, false, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale, boolean centered) {
			return createFromHeightMap(heightMap,cornerScale, NormalMap::new, centered, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale, Constructor constructor) {
			return createFromHeightMap(heightMap,cornerScale, constructor, false, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale, Constructor constructor, boolean centered) {
			return createFromHeightMap(heightMap, cornerScale, constructor, centered, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, double cornerScale, Constructor constructor, boolean centered, boolean forceNormalCreation) {
			return createFromHeightMap(HeightMap.create(heightMap),(ColorSource)null,cornerScale, constructor, centered, forceNormalCreation);
		}
		
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale) {
			return createFromHeightMap(heightMap, colorMap, cornerScale, NormalMap::new, false, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale, boolean centered) {
			return createFromHeightMap(heightMap, colorMap, cornerScale, NormalMap::new, centered, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale, Constructor constructor) {
			return createFromHeightMap(heightMap, colorMap, cornerScale, constructor, false, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale, Constructor constructor, boolean centered) {
			return createFromHeightMap(heightMap, colorMap, cornerScale, constructor, centered, true);
		}
		public static NormalMap createFromHeightMap(float[][] heightMap, Color[][] colorMap, double cornerScale, Constructor constructor, boolean centered, boolean forceNormalCreation) {
			return createFromHeightMap(HeightMap.create(heightMap), (hv,x,y)->colorMap[x][y], cornerScale, constructor, centered, forceNormalCreation);
		}
		
		public interface ColorSource {
			Color getColor(Float heightValue, int x, int y);
		}
		public static NormalMap createFromHeightMap(HeightMap heightMap, ColorSource colorSource, double cornerScale) {
			return createFromHeightMap(heightMap,colorSource,cornerScale, NormalMap::new, false, true);
		}
		public static NormalMap createFromHeightMap(HeightMap heightMap, ColorSource colorSource, double cornerScale, boolean centered, boolean forceNormalCreation) {
			return createFromHeightMap(heightMap,colorSource,cornerScale, NormalMap::new, centered, forceNormalCreation);
		}
		public static NormalMap createFromHeightMap(HeightMap heightMap, ColorSource colorSource, double cornerScale, Constructor constructor, boolean centered, boolean forceNormalCreation) {
			NormalMapData data = new NormalMapData(heightMap.width,heightMap.height);
			for (int x1=0; x1<heightMap.width; ++x1)
				for (int y1=0; y1<heightMap.height; ++y1) {
					MutableNormal base = new MutableNormal(0,0,0, colorSource==null ? null : colorSource.getColor(heightMap.get(x1,y1),x1,y1));
					addNormal(base,computeNormal(heightMap,x1,y1,+1, 0),1); 
					addNormal(base,computeNormal(heightMap,x1,y1, 0,+1),1); 
					addNormal(base,computeNormal(heightMap,x1,y1,-1, 0),1); 
					addNormal(base,computeNormal(heightMap,x1,y1, 0,-1),1);
					if (cornerScale>0) {
						addNormal(base,computeNormal(heightMap,x1,y1,+1,-1),cornerScale);
						addNormal(base,computeNormal(heightMap,x1,y1,+1,+1),cornerScale); 
						addNormal(base,computeNormal(heightMap,x1,y1,-1,+1),cornerScale); 
						addNormal(base,computeNormal(heightMap,x1,y1,-1,-1),cornerScale); 
					}
					if (base.x!=0 || base.y!=0 || base.z!=0)
						data.set(x1,y1,base.toNormal().normalize());
				}
			return constructor.create(data, centered, forceNormalCreation);
		}
		
		public static abstract class HeightMap {
			public final int width;
			public final int height;
			protected HeightMap(int width, int heigh) {
				this.width = width;
				this.height = heigh;
			}
			public abstract Float get(int x, int y);
			
			public static HeightMap create(float[][] heightMap) {
				return new HeightMap(heightMap.length, heightMap[0].length) {
					@Override public Float get(int x, int y) { return heightMap[x][y]; }
				};
			}
		}
		
		private static void addNormal(MutableNormal base, Normal n, double scale) {
			if (n != null) {
				base.x += n.x*scale;
				base.y += n.y*scale;
				base.z += n.z*scale;
			}
		}
		private static Normal computeNormal(HeightMap heightMap, int x, int y, int dx, int dy) {
			if (x+dx<0 || x+dx>=heightMap.width) return null;
			if (y+dy<0 || y+dy>=heightMap.height) return null;
			Float h0 = heightMap.get(x,y);
			Float h1 = heightMap.get(x+dx,y+dy);
			if (h0==null || h1==null) return null;
			float dh = h0-h1;
			if ( (dx!=0) && (dy!=0) ) {
				double w = Math.atan2(dy, dx);
				double r = Math.sqrt(dx*dx+dy*dy);
				return new Normal(dh,0,r).normalize().rotateZ(w);
			}
			if (dx!=0) return new Normal(dh*dx,0,Math.abs(dx)).normalize();
			if (dy!=0) return new Normal(0,dh*dy,Math.abs(dy)).normalize();
			return null;
		}
	}
	
	public static class InterpolatingNormalMap extends NormalMap {
		
		public InterpolatingNormalMap(NormalMapData normalMap, boolean centered) {
			this(normalMap,centered,true);
		}
		public InterpolatingNormalMap(NormalMapData normalMap, boolean centered, boolean forceNormalCreation) {
			super(normalMap,centered,forceNormalCreation);
		}
		
		@Override
		public Normal getNormal(double x, double y, double width, double height) {
			int mapWidth  = normalMap.width;
			int mapHeight = normalMap.height;
			double xi = x + (centered ? (mapWidth -width )/2 : 0);
			double yi = y + (centered ? (mapHeight-height)/2 : 0);
			int x0 = (int) Math.floor(xi); int x1 = x0+1;
			int y0 = (int) Math.floor(yi); int y1 = y0+1;
			Normal n;
			if (-1<=x0 && x1<=mapWidth && -1<=y0 && y1<=mapHeight) {
				Normal n00 = normalMap.get(x0,y0);
				Normal n10 = normalMap.get(x1,y0);
				Normal n01 = normalMap.get(x0,y1);
				Normal n11 = normalMap.get(x1,y1);
				double fx = (xi-x0)/(x1-x0);
				double fy = (yi-y0)/(y1-y0);
				MutableNormal base  = new MutableNormal(0,0,0,null);
				MutableNormal color = new MutableNormal(0,0,0,null);
				int nColor = 0;
				addNormal(base, n00, (1-fx)*(1-fy)); nColor += addColor(color, n00);
				addNormal(base, n10,    fx *(1-fy)); nColor += addColor(color, n10);
				addNormal(base, n01, (1-fx)*   fy ); nColor += addColor(color, n01);
				addNormal(base, n11,    fx *   fy ); nColor += addColor(color, n11);
				if (nColor>0) {
					int r = Math.max(0, Math.min(255, (int)Math.floor(color.x/nColor)));
					int g = Math.max(0, Math.min(255, (int)Math.floor(color.y/nColor)));
					int b = Math.max(0, Math.min(255, (int)Math.floor(color.z/nColor)));
					base.color = new Color(r,g,b);
				}
				n = base.toNormal().normalize();
			} else
				n = forceNormalCreation ? new Normal(0,0,1) : null;
			return ExtraNormalFunction.Host.getMergedNormal(x,y,width,height, showExtrasOnly, forceNormalCreation, extras, (x_,y_,w_,h_) -> n);
		}
		
		private static void addNormal(MutableNormal base, Normal n, double scale) {
			if (n!=null) {
				// --> (nx,ny,nz)*scale
				base.x += n.x*scale;
				base.y += n.y*scale;
				base.z += n.z*scale;
			} else {
				// --> (0,0,1)*scale
				base.z += scale;
			}
		}
		
		private static int addColor(MutableNormal color, Normal n) {
			if (n!=null && n.color!=null) {
				color.x += n.color.getRed();
				color.y += n.color.getGreen();
				color.z += n.color.getBlue();
				return 1;
			}
			return 0;
		}
	}
	
	public static interface CartBase {
		public Normal getNormal(double x, double y);
	}
	public static interface Cart extends NormalFunction,CartBase {
		@Override public default Normal  getNormal     (double x, double y, double width, double height) { return getNormal     (x, y); }
	}
	public static interface PolarBase {
		public Normal getNormal(double w, double r);
	}

	public static interface Polar extends NormalFunction, PolarBase {
		@Override public default Normal getNormal(double x, double y, double width, double height) {
			double y1 = y-height/2.0;
			double x1 = x-width /2.0;
			double w = Math.atan2(y1,x1);
			double r = Math.sqrt(x1*x1+y1*y1);
			return getNormal(w,r);
		}
		
		public static class Simple implements Polar {
			public interface Fcn { public Normal getNormal(double w, double r); }
			private Fcn fcn;
			public Simple (Fcn fcn) {
				this.fcn = fcn;
				Debug.Assert(this.fcn!=null);
			}
			@Override public Normal getNormal(double w, double r) {
				Normal normal = fcn.getNormal(w,r);
				Debug.Assert(normal!=null);
				return normal;
			}
			@Override public void forceNormalCreation(boolean force) {}
			
		}
		public static abstract class AbstractPolar<MyClass extends AbstractPolar<MyClass>> implements Polar, ExtraNormalFunction.PolarHost {
			
			private Colorizer.Polar colorizer;
			private ExtraNormalFunction.Polar extras;
			private boolean forceNormalCreation;
			private boolean showExtrasOnly;
		
			public AbstractPolar() {
				colorizer = null;
				extras = null;
				forceNormalCreation = false;
			}
			protected abstract MyClass getThis(); // return this;
		
			public MyClass setColorizer(Colorizer.Polar colorizer) {
				this.colorizer = colorizer;
				return getThis();
			}
			
			@Override
			public void forceNormalCreation(boolean forceNormalCreation) {
				this.forceNormalCreation = forceNormalCreation;
			}
			@Override
			public void showExtrasOnly(boolean showExtrasOnly) {
				this.showExtrasOnly = showExtrasOnly;
			}
			
			@Override
			public MyClass setExtras(ExtraNormalFunction.Polar extras) {
				this.extras = extras;
				return getThis();
			}
		
			@Override
			public Normal getNormal(double w, double r) {
				
				Normal n = ExtraNormalFunction.PolarHost.getMergedNormal(w, r, showExtrasOnly, forceNormalCreation, extras, this::getBaseNormal);
				
//				boolean showAll = !showExtrasOnly;
//				
//				Normal n = null;
//				Normal en = null;
//				
//				if (extras!=null)
//					en = extras.getNormal(w,r);
//				
//				if (en!=null || showAll || forceNormalCreation)
//					n = getBaseNormal(w, r);
//				
//				if (en!=null)
//					n = ExtraNormalFunction.merge( n, en );
//				
//				if (forceNormalCreation && n==null)
//					n = new Normal(0,0,1);
				
				if (n!=null && colorizer!=null) {
					Color color = colorizer.getColor(w,r);
					if (color!=null)
						return new Normal(n,color);
				}
				return n;
			}
			
			protected abstract Normal getBaseNormal(double w, double r);
		}
		public static class RotatedProfile extends AbstractPolar<RotatedProfile> {
			
			private ProfileXY profile;
		
			public RotatedProfile(ProfileXY profile) {
				this.profile = profile;
				Debug.Assert(this.profile!=null);
			}
			@Override protected RotatedProfile getThis() { return this; }
		
			@Override
			protected Normal getBaseNormal(double w, double r) {
				NormalXY n0 = profile.getNormal(r);
				if (n0==null) return null; 
				return n0.toNormalInXZ().normalize().rotateZ(w);
			}
		}
	}
}