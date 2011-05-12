package net.schwarzbaer.java.lib.image.bumpmapping;

import java.util.Arrays;
import java.util.Comparator;
import java.util.function.BiFunction;

import net.schwarzbaer.java.lib.image.bumpmapping.BumpMapping.NormalXY;

public abstract class ProfileXY {
	
	public final double minR; // inclusive
	public final double maxR; // exclusive

	protected ProfileXY(double minR, double maxR) {
		this.minR = minR;
		this.maxR = maxR;
		Debug.Assert(!Double.isNaN(minR));
		Debug.Assert(!Double.isNaN(maxR));
		Debug.Assert(minR<=maxR);
	}
	
	public boolean contains(double r) {
		return minR<=r && r<maxR;
	}

	protected abstract NormalXY getNormal(double r);
	
	
	public static class Constant extends ProfileXY {

		public static NormalXY computeNormal(double minR, double maxR, double heightAtMinR, double heightAtMaxR) {
			Debug.Assert(Double.isFinite(minR));
			Debug.Assert(Double.isFinite(maxR));
			Debug.Assert(minR<=maxR);
			Debug.Assert(Double.isFinite(heightAtMinR));
			Debug.Assert(Double.isFinite(heightAtMaxR));
			return new NormalXY(heightAtMinR-heightAtMaxR,maxR-minR).normalize();
		}

		private final NormalXY constN;

		public Constant(double minR, double maxR) { this(minR, maxR, new NormalXY(0,1)); }
		public Constant(double minR, double maxR, double heightAtMinR, double heightAtMaxR) { this(minR, maxR, computeNormal(minR, maxR, heightAtMinR, heightAtMaxR)); }
		public Constant(double minR, double maxR, NormalXY constN) {
			super(minR, maxR);
			this.constN = constN;
		}

		@Override
		protected NormalXY getNormal(double r) {
			return constN;
		}
	}
	
	public static class RoundBlend extends ProfileXY {

		private NormalXY normalAtMinR;
		private NormalXY normalAtMaxR;
		private boolean linearBlend;
		private double f1;
		private double f2;
		private int f0;

		public RoundBlend(double minR, double maxR, NormalXY normalAtMinR, NormalXY normalAtMaxR) {
			super(minR, maxR);
			this.normalAtMinR = normalAtMinR;
			this.normalAtMaxR = normalAtMaxR;
			Debug.Assert(this.normalAtMinR!=null);
			Debug.Assert(this.normalAtMaxR!=null);
			Debug.Assert(0<=this.normalAtMinR.y);
			Debug.Assert(0<=this.normalAtMaxR.y);
			prepareValues();
		}

		private void prepareValues() {
			double a1 = Math.atan2(this.normalAtMinR.y, this.normalAtMinR.x);
			double a2 = Math.atan2(this.normalAtMaxR.y, this.normalAtMaxR.x);
			Debug.Assert(a1<=Math.PI);
			Debug.Assert(0<=a1);
			Debug.Assert(a2<=Math.PI);
			Debug.Assert(0<=a2);
			//System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> a1:%6.2f� a2:%6.2f�%n", a1/Math.PI*180, a2/Math.PI*180); 
			if (a1==a2) { linearBlend=true; /*System.out.println("linearBlend");*/ return; } else linearBlend=false;
			if (a1<a2) { a1 += Math.PI; a2 += Math.PI; f0=-1; } else f0=1;
			//System.out.printf(Locale.ENGLISH,"RoundBlend.prepareValues() -> cos(a1):%1.5f cos(a2):%1.5f%n", Math.cos(a1), Math.cos(a2)); 
			
			double R = (maxR-minR)/(Math.cos(a2)-Math.cos(a1));
			f1 = R*Math.cos(a1) - minR;
			f2 = R*R;
			// x = r + R*Math.cos(a1) - minR;
			// y = Math.sqrt( R*R - x*x );
		}

		@Override
		protected NormalXY getNormal(double r) {
			if (linearBlend)
				return NormalXY.blend(r,minR,maxR,normalAtMinR,normalAtMaxR);
			// x = r + R*Math.cos(a1) - minR;
			// y = Math.sqrt( R*R - x*x );
			double x = f0*(r + f1);
			double y = Math.sqrt( f2 - x*x );
			return new NormalXY(x,y);
		}
	}
	
	public static class LinearBlend extends ProfileXY {

		private final NormalXY normalAtMinR;
		private final NormalXY normalAtMaxR;

		public LinearBlend(double minR, double maxR, NormalXY normalAtMinR, NormalXY normalAtMaxR) {
			super(minR, maxR);
			this.normalAtMinR = normalAtMinR;
			this.normalAtMaxR = normalAtMaxR;
			Debug.Assert(this.normalAtMinR!=null);
			Debug.Assert(this.normalAtMaxR!=null);
		}

		@Override
		protected NormalXY getNormal(double r) {
			return NormalXY.blend(r,minR,maxR,normalAtMinR,normalAtMaxR);
		}
		
	}
	
	public static class Group extends ProfileXY {

		private static double getR(ProfileXY[] children, BiFunction<Double,Double,Double> compare) {
			Debug.Assert(children!=null);
			Debug.Assert(children.length>0);
			Debug.Assert(children[0]!=null);
			
			double r = children[0].minR;
			for (ProfileXY child:children) {
				Debug.Assert(child!=null);
				r = compare.apply(compare.apply(r, child.minR), child.maxR);
			}
			return r;
		}

		private static double getMaxR(ProfileXY[] children) {
			return getR(children,Math::max);
		}

		private static double getMinR(ProfileXY[] children) {
			return getR(children,Math::min);
		}

		private ProfileXY[] children;

		public Group(ProfileXY... children) { this(getMinR(children), getMaxR(children), children); }
		public Group(double minR, double maxR, ProfileXY... children) {
			super(minR, maxR);
			setGroup(children);
		}
		
		public void setGroup(ProfileXY... children) {
			Debug.Assert(children!=null);
			for (ProfileXY child:children)
				Debug.Assert(child!=null);
			this.children = children;
		}
		
		public boolean hasGaps() {
			if (children.length==0) return minR<maxR;
			if (minR==maxR) return false;
			
			Arrays.sort(children,Comparator.<ProfileXY,Double>comparing(fcn->fcn.minR).thenComparing(fcn->fcn.maxR));
			
			int first = -1;
			for (int i=0; i<children.length; i++)
				if (children[i].contains(minR)) {
					first = i;
					break;
				}
			if (first == -1) return true;
			
			// [r0,r1) [r1,r2) ...
			// [0.5,1.0) [0.8,1.3) [1.2,1.5) ...
			// -->  child[n].contains( child[n-1].maxR )
			//  &&  child[first].contains( minR )
			//  &&  maxR <= child[last].maxR
			double r = minR;
			for (int i=first; i<children.length; i++) {
				if (!children[i].contains(r)) return true;
				r = children[i].maxR;
			}
			
			return r<maxR;
		}

		@Override
		protected NormalXY getNormal(double r) {
			for (ProfileXY child:children)
				if (child.contains(r))
					return child.getNormal(r);
			return null;
		}
		
	}
	
}