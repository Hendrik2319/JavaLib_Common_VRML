package net.schwarzbaer.java.lib.gui;

public interface ProgressView {
	public enum ProgressDisplay { Percentage, Number, None }
	
	public void displayProgressString(ProgressDisplay progressDisplay);
	public void setTaskTitle(String str);
	public void setIndeterminate(boolean isIndeterminate);
	public void setValue(int min, int value, int max);
	public default void setValue(int value, int max) { setValue(0, value, max); }
	public void setValue(int value);
	
}
