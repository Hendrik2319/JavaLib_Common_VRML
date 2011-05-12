package net.schwarzbaer.java.lib.gui;

import java.awt.Color;

public class ColorCompSlider extends ColorSlider {
	private static final long serialVersionUID = -9018785440106166371L;
	
	public enum ColorComp {
		COMP_RED,
		COMP_GRN,
		COMP_BLU,
		COMP_HUE,
		COMP_SAT,
		COMP_BRT,
	}
//	public static final int COMP_RED = 1;
//	public static final int COMP_GRN = 2;
//	public static final int COMP_BLU = 3;
//	public static final int COMP_HUE = 4;
//	public static final int COMP_SAT = 5;
//	public static final int COMP_BRT = 6;
	
	public ColorCompSlider(SliderType type, Color color, ColorComp colorComp, ColorChangeListener colorChangeListener) {
		this(type,color,colorComp, colorComp, colorChangeListener);
	}

	public ColorCompSlider(SliderType type, Color color, ColorComp colorCompH, ColorComp colorCompV, ColorChangeListener colorChangeListener) {
		super(type,new MyColorSliderModel(type,color,colorCompH,colorCompV),colorChangeListener);
	}

	public void setColorComps(ColorComp colorCompH, ColorComp colorCompV) {
		((MyColorSliderModel)model).setColorComps(colorCompH,colorCompV);
	}

	public void setValues(int red, int green, int blue, float h, float s, float b) {
		((MyColorSliderModel)model).setColor(red, green, blue, h, s, b);
		repaint();
	}
	
	static class MyColorSliderModel implements ColorSliderModel {
		
		private ColorComp colorCompH; 
		private ColorComp colorCompV;
		
		private int colorRED;
		private int colorGRN;
		private int colorBLU;
		private float colorHUE;
		private float colorSAT;
		private float colorBRT;
		
		private Color colorH;
		private float[] colorH_hsb;
		private SliderType type;
		
		public MyColorSliderModel(SliderType type, Color color, ColorComp colorCompH, ColorComp colorCompV) {
			this.type = type;
			this.colorCompH = colorCompH;
			this.colorCompV = colorCompV;
			setColor(color);
		}

		public void setColorComps(ColorComp colorCompH, ColorComp colorCompV) {
			this.colorCompH = colorCompH;
			this.colorCompV = colorCompV;
		}

		@Override
		public Color calcColor(float f) {
			switch (type) {
			case VERTICAL  : return getColor(colorCompV,f,null,null);
			case HORIZONTAL: return getColor(colorCompH,f,null,null);
			default: return null;
			}
			
		}
		
		@Override
		public void prepareColorH(float f) {
			colorH = getColor(colorCompH,f,null,null);
			colorH_hsb = new float[3];  
			Color.RGBtoHSB(colorH.getRed(), colorH.getGreen(), colorH.getBlue(), colorH_hsb);
		}
		
		@Override
		public Color calcColorVFromPreparedColor(float f) {
			return getColor(colorCompV,f,colorH,colorH_hsb);
		}
		

		@Override
		public Color getColor() {
			return new Color(colorRED,colorGRN,colorBLU);
		}

		@Override
		public float getValue() {
			switch (type) {
			case VERTICAL  : return getValueV();
			case HORIZONTAL: return getValueH();
			default: return Float.NaN;
			}
		}

		@Override
		public float getValueH() {
			return getValue( colorCompH );
		}

		@Override
		public float getValueV() {
			return getValue( colorCompV );
		}
		
		@Override
		public void setValue(float f) {
			switch (type) {
			case HORIZONTAL: setValue( colorCompH, f ); break;
			case VERTICAL  : setValue( colorCompV, f ); break;
			case DUAL: break;
			}
		}

		@Override
		public void setValue(float fH, float fV) {
			setValue( colorCompH, fH );
			setValue( colorCompV, fV );
		}
		
		private Color getColor(ColorComp colorComp, float f, Color baseColor, float[] hsb) {
			if ( (colorComp==ColorComp.COMP_RED) || (colorComp==ColorComp.COMP_GRN) || (colorComp==ColorComp.COMP_BLU) ) {
				int RGBval = Math.round(f*255);
				if (RGBval>255) RGBval=255;
				
				if (baseColor!=null)
					switch (colorComp) {
					case COMP_RED: return new Color(     RGBval       ,baseColor.getGreen(),baseColor.getBlue());
					case COMP_GRN: return new Color(baseColor.getRed(),       RGBval       ,baseColor.getBlue());
					case COMP_BLU: return new Color(baseColor.getRed(),baseColor.getGreen(),      RGBval       );
					default: throw new IllegalStateException();
					}
				else
					switch (colorComp) {
					case COMP_RED: return new Color( RGBval ,colorGRN,colorBLU);
					case COMP_GRN: return new Color(colorRED, RGBval ,colorBLU);
					case COMP_BLU: return new Color(colorRED,colorGRN, RGBval );
					default: throw new IllegalStateException();
					}
			} else {
				if (hsb!=null) {
					switch (colorComp) {
					case COMP_HUE: return Color.getHSBColor(  f   ,hsb[1],hsb[2]);
					case COMP_SAT: return Color.getHSBColor(hsb[0],  f   ,hsb[2]);
					case COMP_BRT: return Color.getHSBColor(hsb[0],hsb[1],  f   );
					default: throw new IllegalStateException();
					}
				} else
					switch (colorComp) {
					case COMP_HUE: return Color.getHSBColor(   f    , colorSAT, colorBRT);
					case COMP_SAT: return Color.getHSBColor(colorHUE,    f    , colorBRT);
					case COMP_BRT: return Color.getHSBColor(colorHUE, colorSAT,    f    );
					default: throw new IllegalStateException();
					}
			}
		}

		private float getValue(ColorComp colorComp) {
			switch (colorComp) {
			case COMP_RED: return (colorRED/255f);
			case COMP_GRN: return (colorGRN/255f);
			case COMP_BLU: return (colorBLU/255f);
			case COMP_HUE: return colorHUE;
			case COMP_SAT: return colorSAT;
			case COMP_BRT: return colorBRT;
			}
			return Float.NaN;
		}
		private void setValue( ColorComp colorComp, float f ) {
			switch (colorComp) {
			case COMP_RED: colorRED = Math.round(f*255); updateHSB(); break;
			case COMP_GRN: colorGRN = Math.round(f*255); updateHSB(); break;
			case COMP_BLU: colorBLU = Math.round(f*255); updateHSB(); break;
			case COMP_HUE: colorHUE = f; updateRGB(); break;
			case COMP_SAT: colorSAT = f; updateRGB(); break;
			case COMP_BRT: colorBRT = f; updateRGB(); break;
			}
		}

		public void setColor(int red, int green, int blue, float h, float s, float b) {
			this.colorRED = red;
			this.colorGRN = green;
			this.colorBLU = blue;
			this.colorHUE = h;
			this.colorSAT = s;
			this.colorBRT = b;
		}

		public void setColor(Color color) {
			this.colorRED = color.getRed();
			this.colorGRN = color.getGreen();
			this.colorBLU = color.getBlue();
			updateHSB();
		}

		private void updateHSB() {
			float[] hsb = Color.RGBtoHSB(colorRED, colorGRN, colorBLU, null);
			this.colorHUE = hsb[0];
			this.colorSAT = hsb[1];
			this.colorBRT = hsb[2];
		}
		
		private void updateRGB() {
			int rgb = Color.HSBtoRGB(colorHUE, colorSAT, colorBRT);
			this.colorRED = (rgb>>16)&255;
			this.colorGRN = (rgb>> 8)&255;
			this.colorBLU = (rgb>> 0)&255;
		}
	}
}
