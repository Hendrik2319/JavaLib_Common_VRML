package net.schwarzbaer.java.lib.image.bumpmapping;

import java.awt.Color;

import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Indexer;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Normal;

public abstract class Shading {
	protected int[] color;
	protected Normal sun;
	
	private Shading(Normal sun) {
		this.sun = sun.normalize();
		this.color = new int[4];
	}
	public Shading(Shading shading) {
		this(shading.sun);
	}

	public Normal getSun() { return sun; }
	public void setSun(double x, double y, double z) {
		sun = new Normal(x,y,z).normalize();
	}

	public abstract int[] getColor(double x, double y, double width, double height, Normal normal);
	
	public static Shading clone(Shading shading) {
		if (shading==null) return null;
		if (shading instanceof NormalImage      ) return new NormalImage      ((NormalImage      ) shading);
		if (shading instanceof MixedShading     ) return new MixedShading     ((MixedShading     ) shading);
		if (shading instanceof MaterialShading  ) return new MaterialShading  ((MaterialShading  ) shading);
		if (shading instanceof GUISurfaceShading) return new GUISurfaceShading((GUISurfaceShading) shading);
		Debug.Assert(false);
		return null;
	}

	public static class MixedShading extends Shading {
		
		private final Shading[] shadings;
		private Indexer indexer;
		
		public MixedShading(Indexer indexer, Shading...shadings) {
			super(new Normal(0,0,1));
			this.indexer = indexer;
			this.shadings = shadings;
			Debug.Assert(indexer!=null);
			Debug.Assert(shadings!=null);
			Debug.Assert(shadings.length>0);
		}
		public MixedShading(MixedShading shading) {
			super(shading);
			Debug.Assert(shading.indexer!=null);
			Debug.Assert(shading.shadings!=null);
			Debug.Assert(shading.shadings.length>0);
			indexer = shading.indexer;
			shadings = new Shading[shading.shadings.length];
			for (int i=0; i<shadings.length; i++)
				shadings[i] = Shading.clone(shading.shadings[i]);
		}
		
		@Override
		public void setSun(double x, double y, double z) {
			super.setSun(x, y, z);
			for (Shading sh:shadings)
				sh.setSun(x, y, z);
		}
		
		@Override
		public int[] getColor(double x, double y, double width, double height, Normal normal) {
			int i = indexer.getIndex(x, y, width, height);
			Debug.Assert(0<=i);
			Debug.Assert(i<shadings.length);
			return shadings[i].getColor(x, y, width, height, normal);
		}
	}
	
	public static class NormalImage extends Shading {
		
		public NormalImage() {
			super(new Normal(0,0,1));
		}
		public NormalImage(NormalImage shading) {
			super(shading);
		}

		@Override
		public int[] getColor(double x, double y, double width, double height, Normal normal) {
			color[0] = (int) Math.round(((normal.x+1)/2)*255); Debug.Assert(0<=color[0] && color[0]<=255);
			color[1] = (int) Math.round(((normal.y+1)/2)*255); Debug.Assert(0<=color[1] && color[1]<=255);
			color[2] = (int) Math.round(((normal.z+1)/2)*255); Debug.Assert(0<=color[2] && color[2]<=255);
			color[3] = 255;
			return color;
		}
		
	}
	
	public static class MaterialShading extends Shading {
		
		private Color materialColor;
		private double ambientIntensity;
		private double phongExp;
		private Normal maxSunRefl;
		private boolean withReflection;
		private double reflectionIntensity;

		public MaterialShading(Normal sun, Color materialColor, double ambientIntensity, double phongExp, boolean withReflection, double reflectionIntensity) {
			super(sun);
			this.materialColor = materialColor;
			this.ambientIntensity = Math.max(0,Math.min(ambientIntensity,1));
			this.phongExp = Math.max(0,phongExp);
			this.withReflection = withReflection;
			this.reflectionIntensity = Math.max(0,Math.min(reflectionIntensity,1));
			updateMaxSunRefl();
		}
		public MaterialShading(MaterialShading shading) {
			this(shading.sun,shading.materialColor,shading.ambientIntensity,shading.phongExp,shading.withReflection,shading.reflectionIntensity);
		}

		public Color getMaterialColor() { return materialColor; }
		public void  setMaterialColor(Color color) { this.materialColor = color; }

		public double  getAmbientIntensity() { return ambientIntensity   ; }
		public double  getPhongExp        () { return phongExp           ; }
		public boolean getReflection      () { return withReflection     ; }
		public double  getReflIntensity   () { return reflectionIntensity; }
		public void setAmbientIntensity(double  ambientIntensity   ) { this.ambientIntensity    = Math.max(0,Math.min(ambientIntensity,1)); }
		public void setPhongExp        (double  phongExp           ) { this.phongExp            = Math.max(0,phongExp); }
		public void setReflection      (boolean withReflection     ) { this.withReflection      = withReflection  ; }
		public void setReflIntensity   (double  reflectionIntensity) { this.reflectionIntensity = Math.max(0,Math.min(reflectionIntensity,1)); }

		@Override
		public void setSun(double x, double y, double z) {
			super.setSun(x, y, z);
			updateMaxSunRefl();
			//System.out.println("maxRefl = "+maxRefl);
		}

		private void updateMaxSunRefl() {
			maxSunRefl = new Normal(0,0,1).add(sun).normalize();
		}
		
		@Override
		public int[] getColor(double x, double y, double width, double height, Normal normal) {
			
			if (normal==null) return null;
			
			Color c = normal.color==null?materialColor:normal.color;
			double diffuseIntensity = getF(sun,normal);
			double intensity = mul_inverse(ambientIntensity, diffuseIntensity);
			color[0] = (int) Math.round(c.getRed  ()*intensity);
			color[1] = (int) Math.round(c.getGreen()*intensity);
			color[2] = (int) Math.round(c.getBlue ()*intensity);
			
			if (withReflection) {
				//add( getReflectedLandscape(Math.atan2(normal.z,normal.y)), 0.8 );
				Color c2 = getReflectedLandscape(Math.atan2(normal.z,normal.y));
				desaturate(reflectionIntensity);
				//c2 = brighter(c2, reflectionIntensity);
				mul_inverse( c2 );
				//intensityDiff = 1;
			}
			
			double intensityRefl = getF(maxSunRefl,normal);
			intensityRefl = Math.max(0,Math.min(intensityRefl,1));
			intensityRefl = Math.pow(intensityRefl,phongExp);
			brighter(intensityRefl);
			
			color[3] = 255;
			return color;
		}

		private int brighter(int c, double f) {
			return (int) Math.round(255-(255-c)*(1-f));
		}
		@SuppressWarnings("unused")
		private Color brighter(Color c, double f) {
			int r = brighter(c.getRed  (),f);
			int g = brighter(c.getGreen(),f);
			int b = brighter(c.getBlue (),f);
			return new Color(r, g, b);
		}
		private void brighter(double f) {
			color[0] = brighter(color[0],f);
			color[1] = brighter(color[1],f);
			color[2] = brighter(color[2],f);
		}
		
		private double mul_inverse(double f1, double f2) {
			f1 = Math.max(0,Math.min(f1,1));
			f2 = Math.max(0,Math.min(f2,1));
			return 1-(1-f1)*(1-f2);
		}
		
		private void desaturate(double f) {
			int r = color[0];
			int g = color[1];
			int b = color[2];
			int gray = (r+g+b)/3;
			color[0] = (int) Math.floor( r*(1-f) + gray*f );
			color[1] = (int) Math.floor( g*(1-f) + gray*f );
			color[2] = (int) Math.floor( b*(1-f) + gray*f );
		}

		@SuppressWarnings("unused")
		private void add(Color c, float scale) {
			color[0] = Math.min( 255, (int) Math.floor((c.getRed  ()+color[0])*scale) );
			color[1] = Math.min( 255, (int) Math.floor((c.getGreen()+color[1])*scale) );
			color[2] = Math.min( 255, (int) Math.floor((c.getBlue ()+color[2])*scale) );
		}
		
		private void mul_inverse(Color c) {
			color[0] = (int) Math.floor( mul_inverse(c.getRed  ()/255f,color[0]/255f)*255 );
			color[1] = (int) Math.floor( mul_inverse(c.getGreen()/255f,color[1]/255f)*255 );
			color[2] = (int) Math.floor( mul_inverse(c.getBlue ()/255f,color[2]/255f)*255 );
		}

		private Color getReflectedLandscape(double angle) {
			// angle == 0    --> down
			// angle == PI/2 --> to viewer
			// angle == PI   --> up
			double f;
			Color minC,maxC;
			if (angle<Math.PI/2) {
				f = Math.max(0, angle)/(Math.PI/2);
//					minC = new Color(0x442c16);
//					maxC = new Color(0x202020);
				minC = new Color(0xb57652);
				maxC = new Color(0xc7c7c7);
			} else {
				f = (Math.min(Math.PI, angle)-Math.PI/2)/(Math.PI/2);
//					minC = new Color(0x202020);
//					maxC = new Color(0x002d4c);
				minC = new Color(0xc7c7c7);
				maxC = new Color(0x60c7ff);
			}
			
			int r = (int)(Math.floor( (minC.getRed  ()*(1-f)) + (maxC.getRed  ()*f))*reflectionIntensity );
			int g = (int)(Math.floor( (minC.getGreen()*(1-f)) + (maxC.getGreen()*f))*reflectionIntensity );
			int b = (int)(Math.floor( (minC.getBlue ()*(1-f)) + (maxC.getBlue ()*f))*reflectionIntensity );
			return new Color(r, g, b);
		}

		private double getF(Normal v1, Normal v2) {
			return Math.max(0,v1.dotP(v2));
		}
		
	}
	
	public static class GUISurfaceShading extends Shading {
		private Color highlightColor;
		private Color faceColor;
		private Color shadowColor;
		private double faceF;
		
		public GUISurfaceShading(Normal sun, Color highlightColor, Color faceColor, Color shadowColor) {
			super(sun);
			this.highlightColor = highlightColor;
			this.faceColor = faceColor;
			this.shadowColor = shadowColor;
			this.faceF = getAbsF(new Normal(0,0,1));
		}
		
		public GUISurfaceShading(GUISurfaceShading shading) {
			this(shading.sun,shading.highlightColor,shading.faceColor,shading.shadowColor);
		}

		public Color getHighlightColor() { return highlightColor; }
		public Color getFaceColor     () { return faceColor; }
		public Color getShadowColor   () { return shadowColor; }

		public void setHighlightColor(Color color) { this.highlightColor = color; }
		public void setFaceColor     (Color color) { this.faceColor      = color; }
		public void setShadowColor   (Color color) { this.shadowColor    = color; }

		@Override
		public void setSun(double x, double y, double z) {
			super.setSun(x, y, z);
			faceF = getAbsF(new Normal(0,0,1));
		}

		@Override
		public int[] getColor(double x, double y, double width, double height, Normal normal) {
			color[3] = 255;
			double f1 = getF(normal);
			double f = Math.max(0,f1);
			
			if ( (faceF<f && f<=1) || (faceF>0 && f==faceF)) {
				if (faceF==1) f = 0;
				else f = (f-faceF)/(1-faceF);
				color[0] = (int) Math.round(highlightColor.getRed  ()*f + faceColor.getRed  ()*(1-f));
				color[1] = (int) Math.round(highlightColor.getGreen()*f + faceColor.getGreen()*(1-f));
				color[2] = (int) Math.round(highlightColor.getBlue ()*f + faceColor.getBlue ()*(1-f));
				
			} else if ( (0<=f && f<faceF) || (faceF==0 && f==faceF) ) {
				if (faceF==0) { if (f1==0) f=1; else f=0; }
				else f = f/faceF;
				color[0] = (int) Math.round(faceColor.getRed  ()*f + shadowColor.getRed  ()*(1-f));
				color[1] = (int) Math.round(faceColor.getGreen()*f + shadowColor.getGreen()*(1-f));
				color[2] = (int) Math.round(faceColor.getBlue ()*f + shadowColor.getBlue ()*(1-f));
				
			} else {
				color[0] = 255;
				color[1] = 0;
				color[2] = 0;
			}
			return color;
		}
		
		private double getAbsF(Normal normal) {
			return Math.max(0,getF(normal));
		}

		private double getF(Normal normal) {
			return sun.dotP(normal);
		}
	}
}
