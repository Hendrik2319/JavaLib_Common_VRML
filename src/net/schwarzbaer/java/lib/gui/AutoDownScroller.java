package net.schwarzbaer.java.lib.gui;

import javax.swing.DefaultBoundedRangeModel;
import javax.swing.JScrollPane;

public class AutoDownScroller extends DefaultBoundedRangeModel {
	private static final long serialVersionUID = 5753657456036410315L;
	
	private boolean isActive;
	public AutoDownScroller(boolean isActive) {
		this.isActive = isActive;
	}

	public void applyTo(JScrollPane scrollPane) {
		scrollPane.getVerticalScrollBar().setModel(this);
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public boolean isActive() {
		return isActive;
	}

	@Override
	public void setRangeProperties( int newValue, int newExtent, int newMin, int newMax, boolean adjusting ) {
		super.setRangeProperties( newValue, newExtent, newMin, newMax, adjusting );
//		System.out.println( "min:"+newMin + " val:"+newValue + " val+ext:"+(newValue+newExtent) + " max:"+newMax + " adj:"+adjusting );
		
		if (isActive && !adjusting)  {
			int val = getValue();
			int ext = getExtent();
			int max = getMaximum();
//			if (val != max - ext) super.setValue(max - ext);
			if (val != max - ext) setValue(max - ext);
		}
	}
}
