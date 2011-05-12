package net.schwarzbaer.java.lib.image.bumpmapping;

import java.util.HashMap;
import java.util.Vector;
import java.util.function.BiFunction;

import net.schwarzbaer.java.lib.image.alphachar.Form;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Filter;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.Normal;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.NormalFunctionBase;
import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.NormalXY;
import net.schwarzbaer.java.lib.image.bumpmapping.ExtraNormalFunction.Cart.AlphaChar.XRange;

public interface ExtraNormalFunction extends NormalFunctionBase {

	public static Normal merge(Normal n, Normal en) {
		if (en==null) return  n;
		if ( n==null) return en;
		double wZ = Math.atan2(n.y, n.x);
		 n =  n.rotateZ(-wZ);
		en = en.rotateZ(-wZ);
		double wY = Math.atan2(n.x, n.z);
		en = en.rotateY(wY);
		en = en.rotateZ(wZ);
		if (en.color==null && n.color!=null);
			en = new Normal(en, n.color);
		return en;
	}
	
	public boolean isInsideBounds(double x, double y, double width, double height);
	
	public static class Centerer implements ExtraNormalFunction {
		private Cart extras;
		public Centerer(Cart extras) { this.extras = extras; Debug.Assert(this.extras!=null); }
		@Override public Normal  getNormal     (double x, double y, double width, double height) { return extras.getNormal     (x-width/2, y-height/2); }
		@Override public boolean isInsideBounds(double x, double y, double width, double height) { return extras.isInsideBounds(x-width/2, y-height/2); }
	}
	
	public interface Host {
		public void showExtrasOnly(boolean showExtrasOnly);
		public Object setExtras(ExtraNormalFunction extras);
		
		public static Normal getMergedNormal(double x, double y, double width, double height, boolean showExtrasOnly, boolean forceNormalCreation, ExtraNormalFunction extras, NormalFunctionBase base) {
			boolean showAll = !showExtrasOnly;
			Normal n=null, en=null;
			if (extras!=null                              ) en = extras.getNormal(x,y,width,height);
			if (en!=null || showAll || forceNormalCreation) n = base.getNormal(x,y,width,height);
			if (en!=null                                  ) n = merge( n, en );
			if (forceNormalCreation && n==null            ) n = new Normal(0,0,1);
			return n;
		}
	}
	
	public interface CartHost {
		public void showExtrasOnly(boolean showExtrasOnly);
		public Object setExtras(Cart extras);
		
		public static Normal getMergedNormal(double x, double y, boolean showExtrasOnly, boolean forceNormalCreation, Cart extras, NormalFunction.CartBase base) {
			boolean showAll = !showExtrasOnly;
			Normal n=null, en=null;
			if (extras!=null                              ) en = extras.getNormal(x,y);
			if (en!=null || showAll || forceNormalCreation) n = base.getNormal(x,y);
			if (en!=null                                  ) n = merge( n, en );
			if (forceNormalCreation && n==null            ) n = new Normal(0,0,1);
			return n;
		}
	}
	
	public interface PolarHost {
		public void showExtrasOnly(boolean showExtrasOnly);
		public Object setExtras(Polar extras);
		
		public static Normal getMergedNormal(double w, double r, boolean showExtrasOnly, boolean forceNormalCreation, Polar extras, NormalFunction.PolarBase base) {
			boolean showAll = !showExtrasOnly;
			Normal n=null, en=null;
			if (extras!=null                              ) en = extras.getNormal(w,r);
			if (en!=null || showAll || forceNormalCreation) n = base.getNormal(w,r);
			if (en!=null                                  ) n = merge( n, en );
			if (forceNormalCreation && n==null            ) n = new Normal(0,0,1);
			return n;
		}
	}

	public interface Cart extends ExtraNormalFunction {
		@Override public default Normal  getNormal     (double x, double y, double width, double height) { return getNormal     (x, y); }
		@Override public default boolean isInsideBounds(double x, double y, double width, double height) { return isInsideBounds(x, y); }
		public Normal  getNormal     (double x, double y);
		public boolean isInsideBounds(double x, double y);
		
		public static class BoundingRectangle {
			private final double xMin,yMin,xMax,yMax;
			public BoundingRectangle(double x, double y) { this(x,y,x,y); }
			public BoundingRectangle(double xMin, double yMin, double xMax, double yMax) {
				this.xMin = xMin;
				this.yMin = yMin;
				this.xMax = xMax;
				this.yMax = yMax;
			}
			public BoundingRectangle add(double x, double y) {
				return new BoundingRectangle(Math.min(xMin,x),Math.min(yMin,y),Math.max(xMax,x),Math.max(yMax,y));
			}
			public BoundingRectangle add(BoundingRectangle other) {
				if (other==null) return this;
				return add(other.xMin,other.yMin).add(other.xMax,other.yMax);
			}
			public boolean isInside(double x, double y) {
				return xMin<=x && yMin<=y && x<=xMax && y<=yMax;
			}
		}

		public static abstract class AbstractGroup<ElementType extends Cart, ThisType extends AbstractGroup<ElementType,ThisType>> implements Cart {
		
			protected final Vector<ElementType> elements;
			public AbstractGroup(ElementType[] elements) {
				this.elements = new Vector<>();
				add(elements);
			}
		
			public ThisType add(ElementType element) {
				if (element!=null)
					elements.add(element);
				return getThis();
			}
			public ThisType add(ElementType[] elements) {
				if (elements!=null)
					for (ElementType el:elements)
						add(el);
				return getThis();
			}
			
			protected abstract ThisType getThis();

			@Override
			public boolean isInsideBounds(double x, double y) {
				for (ElementType el:elements)
					if (el.isInsideBounds(x, y))
						return true;
				return false;
			}
		}
		
		public static class MergeGroup extends AbstractGroup<ProfileXYbasedLineElement,MergeGroup> {
		
			public MergeGroup(ProfileXYbasedLineElement...elements) {
				super(elements);
			}
			@Override protected MergeGroup getThis() { return this; }

			@Override
			public Normal getNormal(double x, double y) {
				ProfileXYbasedLineElement.Distance d0 = null;
				ProfileXYbasedLineElement el0 = null;
				for (ProfileXYbasedLineElement el:elements) {
					ProfileXYbasedLineElement.Distance d = el.getDistance(x,y);
					if (d!=null && (d0==null || d0.r>d.r)) { d0=d; el0 = el; }
				}
				if (el0==null || d0==null) return null;
				return el0.getNormal(d0);
			}
		}

		public static abstract class ProfileXYbasedLineElement implements Cart {
			
			protected final ProfileXY profile;
		
			public ProfileXYbasedLineElement(ProfileXY profile) {
				this.profile = profile;
			}
			
			public abstract Distance getDistance(double x, double y);
			public abstract BoundingRectangle getBoundingRectangle();
		
			@Override
			public Normal getNormal(double x, double y) {
				return getNormal(getDistance(x, y));
			}
		
			public Normal getNormal(Distance distance) {
				if (distance==null) return null;
				
				NormalXY n0 = profile.getNormal(distance.r);
				if (n0==null) return null;
				
				return n0.toNormalInXZ().normalize().rotateZ(distance.w);
			}
			
			public static class Distance {
				public final double r,w;
				private Distance(double r, double w) {
					this.r = r;
					this.w = w;
					Debug.Assert(r>=0);
				}
				public static double computeW(double xC, double yC, double x, double y) {
					double localX = x-xC;
					double localY = y-yC;
					return Math.atan2(localY, localX);
				}
				public static double computeR(double xC, double yC, double x, double y) {
					double localX = x-xC;
					double localY = y-yC;
					return Math.sqrt(localX*localX+localY*localY);
				}
				public static Distance compute(double xC, double yC, double x, double y) {
					double localX = x-xC;
					double localY = y-yC;
					double localR = Math.sqrt(localX*localX+localY*localY);
					double localW = Math.atan2(localY, localX);
					return new Distance(localR,localW);
				}
			}
			
			public static class Line extends ProfileXYbasedLineElement {
		
				private final double x1, y1, x2, y2;
				private final double length;
				private final double angle;
				private final BoundingRectangle bounds;
		
				public Line(double x1, double y1, double x2, double y2, ProfileXY profile) {
					super(profile);
					this.x1 = x1;
					this.y1 = y1;
					this.x2 = x2;
					this.y2 = y2;
					Debug.Assert(Double.isFinite(this.x1));
					Debug.Assert(Double.isFinite(this.y1));
					Debug.Assert(Double.isFinite(this.x2));
					Debug.Assert(Double.isFinite(this.y2));
					length = Math.sqrt((x1-x2)*(x1-x2)+(y1-y2)*(y1-y2));
					Debug.Assert(length>0);
					angle = Math.atan2(y2-y1, x2-x1);
					
					double xMin = Math.min(x1,x2)-profile.maxR;
					double yMin = Math.min(y1,y2)-profile.maxR;
					double xMax = Math.max(x1,x2)+profile.maxR;
					double yMax = Math.max(y1,y2)+profile.maxR;
					bounds = new BoundingRectangle(xMin, yMin, xMax, yMax);
				}
				
				@Override public BoundingRectangle getBoundingRectangle() { return bounds; }

				@Override
				public Distance getDistance(double x, double y) {
					double f = ((x2-x1)*(x-x1)+(y2-y1)*(y-y1))/length/length; // cos(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|� -> (x1,y1) ..f.. (x2,y2)
					if (f>1) {
						// after (x2,y2)
						Distance d = Distance.compute(x2,y2,x,y);
						if (d.r>profile.maxR) return null;
						return d;
					}
					if (f<0) {
						// before (x1,y1)
						Distance d = Distance.compute(x1,y1,x,y);
						if (d.r>profile.maxR) return null;
						return d;
					}
					// between (x1,y1) and (x2,y2)
					double r = ((x2-x1)*(y-y1)-(y2-y1)*(x-x1))/length; // sin(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  sin(a)*|x-x1,y-y1|  =  r
					if (Math.abs(r)>profile.maxR) return null;
					if (r>0) return new Distance( r, angle+Math.PI/2);
					else     return new Distance(-r, angle-Math.PI/2);
				}
		
				@Override
				public boolean isInsideBounds(double x, double y) {
					if (!bounds.isInside(x,y)) return false;
					
					double r = ((x2-x1)*(y-y1)-(y2-y1)*(x-x1))/length; // sin(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  sin(a)*|x-x1,y-y1|  =  r
					if (Math.abs(r)>profile.maxR) return false;
					
					double s = ((x2-x1)*(x-x1)+(y2-y1)*(y-y1))/length; // cos(a)*|x-x1,y-y1|*|x2-x1,y2-y1| / |x2-x1,y2-y1|  =  cos(a)*|x-x1,y-y1|  =  s
					return -profile.maxR<=s && s<=length+profile.maxR;
				}
			}
			
			public static class Arc extends ProfileXYbasedLineElement {
		
				private final double xC,yC,r,aStart,aEnd, xS,yS,xE,yE;
				private final BoundingRectangle bounds;

				public Arc(double xC, double yC, double r, double aStart, double aEnd, ProfileXY profile) {
					super(profile);
					this.xC = xC;
					this.yC = yC;
					this.r  =  r;
					this.aStart = aStart;
					this.aEnd   = aEnd;
					Debug.Assert(Double.isFinite(this.xC    ));
					Debug.Assert(Double.isFinite(this.yC    ));
					Debug.Assert(Double.isFinite(this.r     ));
					Debug.Assert(Double.isFinite(this.aStart));
					Debug.Assert(Double.isFinite(this.aEnd  ));
					Debug.Assert(this.r>=0);
					Debug.Assert(this.aStart<this.aEnd);
					xS = this.xC+this.r*Math.cos(this.aStart);
					yS = this.yC+this.r*Math.sin(this.aStart);
					xE = this.xC+this.r*Math.cos(this.aEnd);
					yE = this.yC+this.r*Math.sin(this.aEnd);
					
					double xMin = Math.min(xS,xE)-this.profile.maxR;
					double yMin = Math.min(yS,yE)-this.profile.maxR;
					double xMax = Math.max(xS,xE)+this.profile.maxR;
					double yMax = Math.max(yS,yE)+this.profile.maxR;
					BoundingRectangle tempBounds = new BoundingRectangle(xMin, yMin, xMax, yMax);
					
					double R = this.r+this.profile.maxR;
					if (BumpMapping.isInsideAngleRange(this.aStart,this.aEnd,       0  )) tempBounds = tempBounds.add(this.xC+R,this.yC  );
					if (BumpMapping.isInsideAngleRange(this.aStart,this.aEnd, Math.PI  )) tempBounds = tempBounds.add(this.xC-R,this.yC  );
					if (BumpMapping.isInsideAngleRange(this.aStart,this.aEnd, Math.PI/2)) tempBounds = tempBounds.add(this.xC  ,this.yC+R);
					if (BumpMapping.isInsideAngleRange(this.aStart,this.aEnd,-Math.PI/2)) tempBounds = tempBounds.add(this.xC  ,this.yC-R);
					
					bounds = tempBounds;
				}
				
				@Override public BoundingRectangle getBoundingRectangle() { return bounds; }
		
				@Override
				public Distance getDistance(double x, double y) {
					Distance dC = Distance.compute(xC,yC,x,y);
					if (Math.abs(dC.r-r)>profile.maxR) return null;
					
					if (BumpMapping.isInsideAngleRange(aStart, aEnd, dC.w)) {
						if (dC.r>r) return new Distance(dC.r-r, dC.w);
						return             new Distance(r-dC.r, dC.w+Math.PI);
					}
					Distance dS = Distance.compute(xS,yS,x,y);
					Distance dE = Distance.compute(xE,yE,x,y);
					if (dS.r<dE.r) {
						if (dS.r<=profile.maxR) return dS;
					} else {
						if (dE.r<=profile.maxR) return dE;
					}
					return null;
				}
		
				@Override
				public boolean isInsideBounds(double x, double y) {
					if (!bounds.isInside(x,y)) return false;
					Distance dC = Distance.compute(xC,yC,x,y);
					if (Math.abs(dC.r-r)>profile.maxR) return false;
					if (BumpMapping.isInsideAngleRange(aStart, aEnd, dC.w)) return true;
					return Distance.computeR(xS,yS,x,y)<=profile.maxR || Distance.computeR(xE,yE,x,y)<=profile.maxR;
				}
			}

			public static class LineGroup extends ProfileXYbasedLineElement {
			
				protected final Vector<ProfileXYbasedLineElement> elements;
				private BoundingRectangle bounds;
				
				public LineGroup(ProfileXY profile) {
					super(profile);
					this.elements = new Vector<>();
					bounds = null;
				}
				
				@Override public BoundingRectangle getBoundingRectangle() { return bounds; }
				
				public void addLine(double x1, double y1, double x2, double y2) {
					add(new Line(x1,y1,x2,y2,profile));
				}

				public void addArc(double xC, double yC, double r, double aStart, double aEnd) {
					add(new Arc(xC,yC, r,aStart,aEnd, profile));
				}

				private void add(ProfileXYbasedLineElement element) {
					elements.add(element);
					if (bounds==null) bounds = element.getBoundingRectangle();
					else bounds = bounds.add(element.getBoundingRectangle());
				}
				
				@Override
				public Distance getDistance(double x, double y) {
					Distance d0 = null;
					for (ProfileXYbasedLineElement el:elements) {
						Distance d = el.getDistance(x,y);
						if (d!=null && (d0==null || d0.r>d.r)) d0=d;
					}
					return d0;
				}
			
				@Override
				public boolean isInsideBounds(double x, double y) {
					if (!bounds.isInside(x,y)) return false;
					for (ProfileXYbasedLineElement el:elements)
						if (el.isInsideBounds(x, y))
							return true;
					return false;
				}
			}
		}
		
		public static class AlphaCharSquence extends AbstractGroup<AlphaChar,AlphaCharSquence> {
			
			private double x;
			private double y;
			private double scale;
			private ProfileXY profile;
			private String text;
			private BoundingRectangle bounds;
			private HashMap<Character, Form[]> font;

			public AlphaCharSquence(double x, double y, double scale, ProfileXY profile, String text, HashMap<Character,Form[]> font) {
				super(null);
				bounds = null;
				set(x, y, scale, profile, text, font);
			}
			@Override protected AlphaCharSquence getThis() { return this; }

			public void set(double x, double y, double scale, ProfileXY profile, String text, HashMap<Character,Form[]> font) {
				this.x = x;
				this.y = y;
				this.scale = scale;
				this.profile = profile;
				this.text = text;
				this.font = font;
				Debug.Assert(Double.isFinite(this.x));
				Debug.Assert(Double.isFinite(this.y));
				Debug.Assert(Double.isFinite(this.scale));
				Debug.Assert(this.scale>0);
				Debug.Assert(this.profile!=null);
				Debug.Assert(this.text!=null);
				Debug.Assert(!this.text.isEmpty());
				Debug.Assert(this.font!=null);
				updateChars();
			}

			public void setX      (double x                      ) { set(x, y, scale, profile, text, font); }
			public void setY      (double y                      ) { set(x, y, scale, profile, text, font); }
			public void setPos    (double x, double y            ) { set(x, y, scale, profile, text, font); }
			public void setScale  (double scale                  ) { set(x, y, scale, profile, text, font); }
			public void setProfile(ProfileXY profile             ) { set(x, y, scale, profile, text, font); }
			public void setText   (String text                   ) { set(x, y, scale, profile, text, font); }
			public void setFont   (HashMap<Character,Form[]> font) { set(x, y, scale, profile, text, font); }

			private void updateChars() {
				elements.clear();
				double pos = x;
				double whitespaceOffset = 40*scale;
				double charOffset       = 10*scale;
				boolean lastCharWasSpace = true;
				bounds = null;
				for (char ch:text.toCharArray()) {
					if (ch==' ') { pos += whitespaceOffset; lastCharWasSpace = true; continue; }
					if (!lastCharWasSpace) pos += charOffset;
					lastCharWasSpace = false;
					pos += profile.maxR;
					AlphaChar alphaChar = new AlphaChar(pos, y, scale, profile, font.get(ch));
					XRange range = alphaChar.getRange();
					pos += range.maxX + profile.maxR;
					add(alphaChar);
					if (bounds==null) bounds = alphaChar.getBoundingRectangle();
					else bounds = bounds.add( alphaChar.getBoundingRectangle() );
				}
			}
			
			@Override
			public Normal getNormal(double x, double y) {
				for (AlphaChar alphaChar:elements)
					if (alphaChar.isInsideBounds(x,y)) {
						Normal n = alphaChar.getNormal(x,y);
						if (n!=null) return n;
					}
				return null;
			}

			@Override
			public boolean isInsideBounds(double x, double y) {
				if (!bounds.isInside(x,y)) return false;
				return super.isInsideBounds(x,y);
			}
		}
		
		public static class AlphaChar extends ProfileXYbasedLineElement.LineGroup {
			
			private XRange range;
			private final LineForm[] lineSets;
			private final double x,y;
			private double scale;

			public AlphaChar(double x, double y, double scale, ProfileXY profile, Form[] forms) {
				super(profile);
				this.x = x;
				this.y = y;
				this.scale = scale;
				Debug.Assert(Double.isFinite(this.x));
				Debug.Assert(Double.isFinite(this.y));
				Debug.Assert(Double.isFinite(this.scale));
				lineSets = getLineSet(forms);
				updateLines();
			}
			
			private static LineForm[] getLineSet(Form[] forms) {
				if (forms==null || forms.length==0)
					return new LineForm[] { new PolyLine(0,100).add(0,0).add(50,0).add(50,100).add(0,100), new Line(0, 0,50,100), new Line(50, 0,0,100) };
				
				LineForm[] lineForms = new LineForm[forms.length];
				for (int i=0; i<lineForms.length; i++) {
					if (forms[i] instanceof Form.PolyLine) lineForms[i] = new PolyLine((Form.PolyLine) forms[i]);
					if (forms[i] instanceof Form.Line    ) lineForms[i] = new Line    ((Form.Line    ) forms[i]);
					if (forms[i] instanceof Form.Arc     ) lineForms[i] = new Arc     ((Form.Arc     ) forms[i]);
				}
				return lineForms;
			}

			@SuppressWarnings("unused")
			private static LineForm[] getLineSet(char letter) {
				//  top:   0
				//  mid:  40
				// base: 100
				switch (letter) {
				case 'X': return new LineForm[] { new Line(0, 0,50,100), new Line(50, 0,0,100) };
				case 'x': return new LineForm[] { new Line(0,40,50,100), new Line(50,40,0,100) };
				case 'M': return new LineForm[] { new PolyLine(0,100).add(0,0).add(35,60).add(70,0).add(70,100) };
				case 'B': return new LineForm[] { new Line(0,0,0,100), new Line(0,0,20,0), new Line(0,40,20,40), new Line(0,100,20,100), new Arc(20,20,20,-Math.PI/2,Math.PI/2), new Arc(20,70,30,-Math.PI/2,Math.PI/2) };
				}
				return new LineForm[] { new PolyLine(0,100).add(0,0).add(50,0).add(50,100).add(0,100), new Line(0, 0,50,100), new Line(50, 0,0,100) };
			}
			
			public void setScale(double scale) {
				this.scale = scale;
				Debug.Assert(Double.isFinite(this.scale));
				updateLines();
			}

			private void updateLines() {
				elements.clear();
				range = null;
				for (LineForm form:lineSets) {
					if (range==null) range = form.getXRange(scale);
					else range = range.add(form.getXRange(scale));
					form.addTo(this,x,y,scale);
				}
			}

			public XRange getRange() {
				return range;
			}

			public interface LineForm {
				void addTo(ProfileXYbasedLineElement.LineGroup lineGroup, double x, double y, double scale);
				XRange getXRange(double scale);
			}
			
			public static class XRange {
				public final double minX,maxX;
				public XRange(double minX, double maxX) {
					this.minX = minX;
					this.maxX = maxX;
					Debug.Assert(!Double.isNaN(this.minX));
					Debug.Assert(!Double.isNaN(this.maxX));
					Debug.Assert(this.minX<=this.maxX);
				}
				public XRange add(double x) {
					return add(x,x);
				}
				public XRange add(XRange other) {
					return other==null ? this : add(other.minX, other.maxX);
				}
				public XRange add(double minX, double maxX) {
					return new XRange(Math.min(this.minX, minX), Math.max(this.maxX, maxX));
				}
				public XRange mul(double f) {
					return new XRange(minX*f,this.maxX*f);
				}
			}
			
			public static class PolyLine implements LineForm {
				
				private final Vector<Point> points;
				private XRange range;
				
				public PolyLine(double xStart, double yStart) {
					Debug.Assert(Double.isFinite(xStart));
					Debug.Assert(Double.isFinite(yStart));
					points = new Vector<>();
					points.add(new Point(xStart,yStart));
					range = new XRange(xStart,xStart);
				}
				
				public PolyLine(Form.PolyLine polyLine) {
					this(polyLine.getFirstX(),polyLine.getFirstY());
					for (int i=1; i<polyLine.size(); ++i) {
						Form.PolyLine.Point p = polyLine.get(i);
						add(p.x,p.y);
					}
				}

				public PolyLine add(double x, double y) {
					points.add(new Point(x,y));
					range =  range.add(x);
					return this;
				}
				
				@Override
				public void addTo(ProfileXYbasedLineElement.LineGroup lineGroup, double x, double y, double scale) {
					for (int i=1; i<points.size(); i++) {
						Point p1 = points.get(i-1);
						Point p2 = points.get(i);
						lineGroup.addLine(x+p1.x*scale, y+p1.y*scale, x+p2.x*scale, y+p2.y*scale);
					}
				}

				@Override
				public XRange getXRange(double scale) {
					return range.mul(scale);
				}
				
				private static class Point {
					final double x,y;
					private Point(double x, double y) { this.x = x; this.y = y; }
				}
			}
			
			public static class Line implements LineForm {
				private final double x1, y1, x2, y2;
				private final XRange range;
				public Line(Form.Line line) { this(line.x1,line.y1,line.x2,line.y2); }
				public Line(double x1, double y1, double x2, double y2) {
					this.x1 = x1;
					this.y1 = y1;
					this.x2 = x2;
					this.y2 = y2;
					Debug.Assert(Double.isFinite(this.x1));
					Debug.Assert(Double.isFinite(this.y1));
					Debug.Assert(Double.isFinite(this.x2));
					Debug.Assert(Double.isFinite(this.y2));
					Debug.Assert(this.x1!=this.x2 || this.y1!=this.y2);
					this.range = new XRange(Math.min(x1,x2), Math.max(x1,x2));
				}
				@Override public void addTo(ProfileXYbasedLineElement.LineGroup lineGroup, double x, double y, double scale) { lineGroup.addLine(x+x1*scale, y+y1*scale, x+x2*scale, y+y2*scale); }
				@Override public XRange getXRange(double scale) { return range.mul(scale); }
			}
			
			public static class Arc implements LineForm {
				private final double xC,yC,r,aStart,aEnd;
				private final XRange range;
				public Arc(Form.Arc arc) { this(arc.xC,arc.yC,arc.r,arc.aStart,arc.aEnd); }
				public Arc(double xC, double yC, double r, double aStart, double aEnd) {
					this.xC     = xC;
					this.yC     = yC;
					this.r      = r;
					this.aStart = aStart;
					this.aEnd   = aEnd;
					Debug.Assert(Double.isFinite(this.xC    ));
					Debug.Assert(Double.isFinite(this.yC    ));
					Debug.Assert(Double.isFinite(this.r     ));
					Debug.Assert(Double.isFinite(this.aStart));
					Debug.Assert(Double.isFinite(this.aEnd  ));
					Debug.Assert(this.r>=0);
					Debug.Assert(this.aStart<this.aEnd);
					double x1 = this.xC + this.r * Math.cos(this.aStart);
					double x2 = this.xC + this.r * Math.cos(this.aEnd);
					XRange tempRange = new XRange(Math.min(x1,x2), Math.max(x1,x2));
					if (BumpMapping.isInsideAngleRange(aStart, aEnd,       0)) tempRange = tempRange.add(this.xC + this.r);
					if (BumpMapping.isInsideAngleRange(aStart, aEnd, Math.PI)) tempRange = tempRange.add(this.xC - this.r);
					this.range = tempRange;
				}
				@Override public void addTo(ProfileXYbasedLineElement.LineGroup lineGroup, double x, double y, double scale) { lineGroup.addArc(x+xC*scale, y+yC*scale, r*scale, aStart, aEnd); }
				@Override public XRange getXRange(double scale) { return range.mul(scale); }
			}
			
		}
	}
	
	public interface Polar extends ExtraNormalFunction {
		@Override public default Normal getNormal(double x, double y, double width, double height) {
			double y1 = y-height/2.0;
			double x1 = x-width /2.0;
			double w = Math.atan2(y1,x1);
			double r = Math.sqrt(x1*x1+y1*y1);
			return getNormal(w,r);
		}
		@Override public default boolean isInsideBounds(double x, double y, double width, double height) {
			double y1 = y-height/2.0;
			double x1 = x-width /2.0;
			double w = Math.atan2(y1,x1);
			double r = Math.sqrt(x1*x1+y1*y1);
			return isInsideBounds(w,r);
		}
		public Normal  getNormal     (double w, double r);
		public boolean isInsideBounds(double w, double r);
		
		public static class Stencil implements Polar {
			
			private final Filter.Polar filter;
			private final Polar extra;
		
			public Stencil(Filter.Polar filter, Polar extra) {
				this.filter = filter;
				this.extra = extra;
				Debug.Assert(this.filter!=null);
				Debug.Assert(this.extra!=null);
			}
		
			@Override
			public boolean isInsideBounds(double w, double r) {
				return filter.passesFilter(w,r);
			}
		
			@Override
			public Normal getNormal(double w, double r) {
				if (filter.passesFilter(w,r))
					return extra.getNormal(w,r);
				return null;
			}
		}
		public static class Group implements Polar {
			
			private final Vector<Polar> elements;
			
			public Group(Polar... elements) {
				this.elements = new Vector<>();
				add(elements);
			}
			public void add(Polar... elements) {
				if (elements!=null)
					for (Polar el:elements)
						if (el!=null) this.elements.add(el);
			}
		
			@Override
			public boolean isInsideBounds(double w, double r) {
				for (Polar el:elements)
					if (el.isInsideBounds(w, r))
						return true;
				return false;
			}
			
			@Override
			public Normal getNormal(double w, double r) {
				for (Polar el:elements) {
					if (!el.isInsideBounds(w,r)) continue;
					Normal en = el.getNormal(w,r);
					if (en!=null) return en;
				}
				return null;
			}
		}
		public static class Rotated implements Polar {
			
			private final double anglePos;
			private final Polar extra;
		
			public Rotated(double anglePosDegree, Polar extraNormalizedAtXaxis) {
				this.anglePos = anglePosDegree/180.0*Math.PI;
				this.extra = extraNormalizedAtXaxis;
				Debug.Assert(Double.isFinite(this.anglePos));
				Debug.Assert(this.extra!=null);
			}
		
			@Override
			public boolean isInsideBounds(double w, double r) {
				return extra.isInsideBounds(w-anglePos, r);
			}
		
			@Override
			public Normal getNormal(double w, double r) {
				if (!isInsideBounds(w,r)) return null;
				Normal en = extra.getNormal(w-anglePos,r);
				if (en==null) return null;
				return en.rotateZ(anglePos);
			}
		}
		public static class Bounds {
			
			final double minW,maxW,minR,maxR;
			
			private Bounds() {
				this(0,BumpMapping.FULL_CIRCLE,0,Double.POSITIVE_INFINITY);
			}
			private Bounds(double minW, double maxW, double minR, double maxR) {
				this.minW = minW;
				this.maxW = maxW;
				this.minR = minR;
				this.maxR = maxR;
				Debug.Assert(Double.isFinite(this.minW));
				Debug.Assert(Double.isFinite(this.maxW));
				Debug.Assert(this.minW<=this.maxW);
				Debug.Assert(Double.isFinite(this.minR));
				Debug.Assert(!Double.isNaN(this.maxR));
				Debug.Assert(0<=this.minR);
				Debug.Assert(this.minR<=this.maxR);
			}
			public boolean isInside(double w, double r) {
				if (r<minR) return false;
				if (r>maxR) return false;
				return BumpMapping.isInsideAngleRange(minW, maxW, w);
			}
			public Bounds rotate(double w) {
				return new Bounds(minW+w, maxW+w, minR, maxR);
			}
		}
		
		public static class BentCartExtra implements Polar {
			
			protected double zeroYRadius;
			protected double zeroXAngle;
			protected final Cart extra;

			public BentCartExtra(double zeroYRadius, double zeroXAngle, Cart extra) {
				setZeroYRadius(zeroYRadius);
				setZeroXAngle(zeroXAngle);
				this.extra = extra;
				Debug.Assert(this.extra!=null);
			}

			public double getZeroYRadius() { return zeroYRadius; }
			public double getZeroXAngle () { return zeroXAngle ; }
			
			public void setZeroYRadius(double zeroYRadius) {
				this.zeroYRadius = zeroYRadius;
				Debug.Assert(Double.isFinite(this.zeroYRadius));
				Debug.Assert(this.zeroYRadius>=0);
			}
			public void setZeroXAngle(double zeroXAngle) {
				this.zeroXAngle  = zeroXAngle ;
				Debug.Assert(Double.isFinite(this.zeroXAngle));
			}

			@Override
			public Normal getNormal(double w, double r) {
				Normal n = convert(w,r, extra::getNormal);
				if (n!=null) n = n.rotateZ(w+Math.PI/2);
				return n;
			}

			@Override
			public boolean isInsideBounds(double w, double r) {
				return convert(w,r, extra::isInsideBounds);
			}
			
			protected <V> V convert(double w, double r, BiFunction<Double,Double,V> getCartValue) {
				w = BumpMapping.normalizeAngle(zeroXAngle,w);
				double x = (w-zeroXAngle)*zeroYRadius;
				double y = zeroYRadius-r;
				return getCartValue.apply(x,y);
			}
			
		}
		
		public static class SpiralBentCartExtra extends BentCartExtra {
			
			private double rowHeight;
			private double radiusOffset;

			public SpiralBentCartExtra(double zeroYRadius, double zeroXAngle, double rowHeight, double radiusOffset, Cart extra) {
				super(zeroYRadius, zeroXAngle, extra);
				setRowHeight(rowHeight);
			}
			
			public double getRowHeight   () { return rowHeight   ; }
			public double getRadiusOffset() { return radiusOffset; }
			
			public void setRowHeight(double rowHeight) {
				this.rowHeight = rowHeight;
				Debug.Assert(Double.isFinite(this.rowHeight));
				Debug.Assert(this.rowHeight>0);
			}
			public void setRadiusOffset(double radiusOffset) {
				this.radiusOffset = radiusOffset;
				Debug.Assert(Double.isFinite(this.radiusOffset));
			}
			
			@Override
			protected <V> V convert(double w, double r, BiFunction<Double,Double,V> getCartValue) {
				w = BumpMapping.normalizeAngle(zeroXAngle,w);
				r = r-radiusOffset;
				double rBase = zeroYRadius - (w-zeroXAngle)/2/Math.PI*rowHeight;
				double i = Math.floor((rBase-r)/rowHeight);
				
				double y = rBase - i*rowHeight - r;
				
				double wx = w - zeroXAngle + i*2*Math.PI;
				double x = wx*zeroYRadius - wx*wx/2 * rowHeight/2/Math.PI;
				
				return getCartValue.apply(x,y);
			}
			
		}
		
		public static class LineOnX implements Polar {
		
			private final double minR;
			private final double maxR;
			private final ProfileXY profile;
			private final Bounds bounds;
		
			public LineOnX(double minR, double maxR, ProfileXY profile) {
				this.minR = minR;
				this.maxR = maxR;
				this.profile = profile;
				Debug.Assert(this.profile!=null);
				Debug.Assert(Double.isFinite(this.minR));
				Debug.Assert(Double.isFinite(this.maxR));
				Debug.Assert(0<=this.minR);
				Debug.Assert(this.minR<=this.maxR);
				double maxProfileR = profile.maxR;
				double w = Math.asin(maxProfileR/this.minR);
				bounds = new Bounds(-w,w,this.minR-maxProfileR,this.maxR+maxProfileR);
			}
			
			@Override
			public boolean isInsideBounds(double w, double r) {
				return bounds.isInside(w, r);
			}
		
			@Override
			public Normal getNormal(double w, double r) {
				//if (!isInsideBounds(w,r)) return null;
				double x = r*Math.cos(w);
				double y = r*Math.sin(w);
				double maxProfileR = profile.maxR;
				if (x < minR-maxProfileR) return null;
				if (x > maxR+maxProfileR) return null;
				if (y <     -maxProfileR) return null;
				if (y >      maxProfileR) return null;
				
				double local_r;
				double local_w;
				if (x < minR) {
					local_r = Math.sqrt( y*y + (x-minR)*(x-minR) );
					local_w = Math.atan2(y, x-minR);
					
				} else if (x > maxR) {
					local_r = Math.sqrt( y*y + (x-maxR)*(x-maxR) );
					local_w = Math.atan2(y, x-maxR);
					
				} else if (y>0) {
					local_r = y;
					local_w = Math.PI/2;
					
				} else {
					local_r = -y;
					local_w = -Math.PI/2;
				}
				
				NormalXY n0 = profile.getNormal(local_r);
				if (n0==null) return null; 
				return n0.toNormalInXZ().normalize().rotateZ(local_w);
			}
		}
	}
}