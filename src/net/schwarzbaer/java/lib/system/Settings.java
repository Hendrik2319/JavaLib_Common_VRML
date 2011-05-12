package net.schwarzbaer.java.lib.system;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.swing.JSplitPane;
import javax.swing.SwingUtilities;

public class Settings<ValueGroup extends Enum<ValueGroup> & Settings.GroupKeys<ValueKey>, ValueKey extends Enum<ValueKey>> {

	private static final String INTEGER_SEPARATOR = ",";

	public interface GroupKeys<V> {
		V[] getKeys();
	}
	
	private Preferences preferences;
	
	public Settings(Class<?> classObj) {
		this.preferences = Preferences.userNodeForPackage(classObj);
	}

	public boolean isSet(ValueGroup valueGroup) {
		return contains(valueGroup.getKeys());
	}
	
	public void    remove   (ValueKey key               ) {        preferences.remove    (key.name()       ); }
	
	public  boolean getBool  (ValueKey key               ) { return preferences.getBoolean(key.name(), true ); }
	public  boolean getBool  (ValueKey key, boolean def  ) { return preferences.getBoolean(key.name(), def  ); }
	public  void    putBool  (ValueKey key, boolean value) {        preferences.putBoolean(key.name(), value); }
	public  float   getFloat (ValueKey key               ) { return preferences.getFloat  (key.name(), 0    ); }
	public  float   getFloat (ValueKey key, float def    ) { return preferences.getFloat  (key.name(), def  ); }
	public  void    putFloat (ValueKey key, float value  ) {        preferences.putFloat  (key.name(), value); }
	public  double  getDouble(ValueKey key               ) { return preferences.getDouble (key.name(), 0    ); }
	public  double  getDouble(ValueKey key, double def   ) { return preferences.getDouble (key.name(), def  ); }
	public  void    putDouble(ValueKey key, double value ) {        preferences.putDouble (key.name(), value); }
	public  int     getInt   (ValueKey key               ) { return preferences.getInt    (key.name(), 0    ); }
	private int     getInt   (String   key, int def      ) { return preferences.getInt    (key       , def  ); }
	public  int     getInt   (ValueKey key, int def      ) { return preferences.getInt    (key.name(), def  ); }
	public  void    putInt   (ValueKey key, int value    ) {        preferences.putInt    (key.name(), value); }
	private void    putInt   (String   key, int value    ) {        preferences.putInt    (key       , value); }
	public  String  getString(ValueKey key               ) { return preferences.get       (key.name(), null ); }
	public  String  getString(ValueKey key, String def   ) { return preferences.get       (key.name(), def  ); }
	public  void    putString(ValueKey key, String value ) { if (value==null) remove(key); else preferences.put(key.name(), value); }

	public Color   getColor (ValueKey key              ) { return new Color(getInt(key, Color.BLACK.getRGB()), true); }
	public Color   getColor (ValueKey key, Color def   ) { return new Color(getInt(key,         def.getRGB()), true); }
	public void    putColor (ValueKey key, Color value ) { putInt(key, value.getRGB()); }

	public File    getFile  (ValueKey key              ) { return getFile(key,null); }
	public File    getFile  (ValueKey key, File def    ) { String str = getString(key,null); if (str==null) return def ; return new File(str); }
	public void    putFile  (ValueKey key, File value  ) { putString(key, value.getAbsolutePath()); }

	public File[] getFiles(ValueKey key) {
		return strings2file(getStrings(key, System.getProperty("path.separator")));
	}

	public void putFiles(ValueKey key, Collection<File> files) {
		if (files==null) return;
		putFiles(key, files.toArray(new File[files.size()]));
	}
	public void putFiles(ValueKey key, File... files) {
		if (files==null) return;
		putStrings(key, System.getProperty("path.separator"), files2strings(files));
	}

	public void addFiles(ValueKey key, File... files) {
		if (files==null || files.length==0) return;
		addStrings(key, System.getProperty("path.separator"), files2strings(files));
	}

	public File[] strings2file(String[] filePaths) {
		if (filePaths==null) return null;
		File[] files = new File[filePaths.length];
		for (int i=0; i<filePaths.length; i++)
			files[i] = filePaths[i]==null ? null : new File(filePaths[i]);
		return files;
	}

	public String[] files2strings(File[] files) {
		if (files==null) return null;
		String[] filePaths = new String[files.length];
		for (int i=0; i<files.length; i++)
			filePaths[i] = files[i]==null ? null : files[i].getAbsolutePath();
		return filePaths;
	}

	public String[] getStrings(ValueKey key, String delimiter) {
		String str = getString(key,null);
		if (str!=null) return str.split(delimiter);
		return null;
	}

	public void putStrings(ValueKey key, String delimiter, String... strs) {
		if (key==null || delimiter==null || strs==null) return; 
		putString(key, String.join(delimiter,strs));
	}

	public void addStrings(ValueKey key, String delimiter, String... strs) {
		if (key==null || delimiter==null || strs==null || strs.length==0) return;
		String newStr = String.join(delimiter,strs);
		String oldStr = getString(key,null);
		if (oldStr==null) putString(key, newStr);
		else putString(key, oldStr+delimiter+newStr);
	}

	public Dimension getDimension(ValueKey keyW, ValueKey keyH, int defW, int defH) { int w=getInt(keyW,defW); int h=getInt(keyH,defH); return new Dimension(w,h); }
	public Dimension getDimension(ValueKey keyW, ValueKey keyH                    ) { int w=getInt(keyW     ); int h=getInt(keyH     ); return new Dimension(w,h); }
	public void      putDimension(ValueKey keyW, ValueKey keyH, Dimension size    ) { putInt(keyW, size.width ); putInt(keyH, size.height); }
	
	private Dimension getDimension(String keyW, String keyH                    ) { int w=getInt(keyW,0); int h=getInt(keyH,0); return new Dimension(w,h); }
	private void      putDimension(String keyW, String keyH, Dimension size    ) { putInt(keyW, size.width ); putInt(keyH, size.height); }
	
	public Point getPoint(ValueKey keyX, ValueKey keyY                ) { int x=getInt(keyX); int y=getInt(keyY); return new Point(x,y); }
	public void  putPoint(ValueKey keyX, ValueKey keyY, Point location) { putInt(keyX, location.x); putInt(keyY, location.y); }
	
	private Point getPoint(String keyX, String keyY                ) { int x=getInt(keyX,0); int y=getInt(keyY,0); return new Point(x,y); }
	private void  putPoint(String keyX, String keyY, Point location) { putInt(keyX, location.x); putInt(keyY, location.y); }

	public <E extends Enum<E>> E    getEnum(ValueKey key       , Class<E> enumClass ) { return convert(getString(key,null),null, enumClass); }
	public <E extends Enum<E>> E    getEnum(ValueKey key, E def, Class<E> enumClass ) { return convert(getString(key,null),def , enumClass); }
	public <E extends Enum<E>> void putEnum(ValueKey key, E value ) { putString(key, value==null ? null : value.name()); }
	
	private <E extends Enum<E>> E convert(String string, E def, Class<E> enumClass) {
		if (string==null) return def;
		try { return Enum.valueOf(enumClass, string); }
		catch (Exception e) { return def; }
	}
	
	public void putIntArray(ValueKey key, Stream<Integer>     values) { putString(key, toString(Objects.requireNonNull(values))); }
	public void putIntArray(ValueKey key, Collection<Integer> values) { putString(key, toString(Objects.requireNonNull(values))); }
	public void putIntArray(ValueKey key, Integer[]           values) { putString(key, toString(Objects.requireNonNull(values))); }
	public void putIntArray(ValueKey key, int[]               values) { putString(key, toString(Objects.requireNonNull(values))); }
	public int[]           getIntArray     (ValueKey key) { return getIntegerStream(key).mapToInt(Integer::intValue).toArray(); }
	public Integer[]       getIntegerArray (ValueKey key) { return getIntegerStream(key).toArray(Integer[]::new); }
	public Vector<Integer> getIntegerVector(ValueKey key) { return getIntegerCollection(key, Vector<Integer>::new); }
	
	public <CollectionType extends Collection<Integer>> CollectionType getIntegerCollection(ValueKey key, Supplier<CollectionType> constructor) {
		CollectionType collection = constructor.get();
		collection.clear();
		collection.addAll(getIntegerStream(key).toList());
		return collection;
	}
	
	public Stream<Integer> getIntegerStream(ValueKey key) {
		String value = getString(key, null);
		
		if (value==null)
			return Stream.empty();
		
		String[] parts = value.split(INTEGER_SEPARATOR,-1);
		
		return Arrays
				.stream(parts)
				.map(str -> {
					try { return Integer.parseInt(str); }
					catch (NumberFormatException e) { return null; }
				})
				.filter(i -> (i != null));
	}

	private String toString(Stream<Integer> values)
	{
		Iterable<String>  it = ()->values.map(i->i.toString()).iterator();
		return String.join(INTEGER_SEPARATOR, it);
	}
	private String toString(Collection<Integer> values) { return toString(values.stream()); }
	private String toString(Integer[]           values) { return toString(Arrays.stream(values)); }
	private String toString(int[]               values) { return toString(Arrays.stream(values).mapToObj(Integer::valueOf)); }

	private boolean contains(String[] prefkeys, ValueKey key) {
		if (key==null) return true;
		if (prefkeys==null) return false;
		for (String prefkey:prefkeys)
			if (prefkey.equals(key.name()))
				return true;
		return false;
	}

	public boolean contains(ValueKey key1                                             ) { return contains(key1, null, null, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2                              ) { return contains(key1, key2, null, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3               ) { return contains(key1, key2, key3, null, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4) { return contains(key1, key2, key3, key4, null); }
	public boolean contains(ValueKey key1, ValueKey key2, ValueKey key3, ValueKey key4, ValueKey key5) {
		String[] prefkeys;
		try { prefkeys = preferences.keys(); }
		catch (BackingStoreException e) { e.printStackTrace(); return false; }
		if (!contains(prefkeys, key1)) return false;
		if (!contains(prefkeys, key2)) return false;
		if (!contains(prefkeys, key3)) return false;
		if (!contains(prefkeys, key4)) return false;
		if (!contains(prefkeys, key5)) return false;
		return true;
	}

	public boolean contains(ValueKey[] keys) {
		String[] prefkeys;
		try { prefkeys = preferences.keys(); }
		catch (BackingStoreException e) { e.printStackTrace(); return false; }
		for (ValueKey key:keys)
			if (!contains(prefkeys, key))
				return false;
		return true;
	}
	
	private interface WindowSizePosStorage {
		Point     getWindowPos (              );
		void      setWindowPos (Point location);
		Dimension getWindowSize(              );
		void      setWindowSize(Dimension size);
		boolean isPosSet (String[] prefkeys);
		boolean isSizeSet(String[] prefkeys);
	}
	
	public static class DefaultAppSettings<ValueGroup extends Enum<ValueGroup> & Settings.GroupKeys<ValueKey>, ValueKey extends Enum<ValueKey>> extends Settings<ValueGroup,ValueKey> implements WindowSizePosStorage {

		public DefaultAppSettings(Class<?> classObj, ValueKey[] allValueKeys) {
			super(classObj);
			for (ValueKey key : allValueKeys) {
				checkKeyName(key, "WindowX");
				checkKeyName(key, "WindowY");
				checkKeyName(key, "WindowWidth");
				checkKeyName(key, "WindowHeight");
			}
		}

		private void checkKeyName(ValueKey key, String keyName) {
			if (key!=null &&  keyName.equals(key.name()))
				throw new IllegalStateException("In DefaultAppSettings is no ValueKey."+keyName+" allowed. The field \""+keyName+"\" will be used internally.");
		}
		
		public void registerAppWindow(Window appWindow) {
			registerAppWindow(appWindow, -1, -1);
		}
		public void registerAppWindow(Window appWindow, int defaultWindowWidth, int defaultWindowHeight) {
			registerAppWindow(appWindow, defaultWindowWidth, defaultWindowHeight, false);
		}
		public void registerAppWindow(Window appWindow, int defaultWindowWidth, int defaultWindowHeight, boolean forceSize) {
			registerWindow(appWindow, defaultWindowWidth, defaultWindowHeight, forceSize, this);
		}
		
		public void registerExtraWindow(Window window, ValueKey windowX, ValueKey windowY, ValueKey windowWidth, ValueKey windowHeight) {
			registerExtraWindow(window, windowX, windowY, windowWidth, windowHeight, -1, -1);
		}
		public void registerExtraWindow(Window window, ValueKey windowX, ValueKey windowY, ValueKey windowWidth, ValueKey windowHeight, int defaultWindowWidth, int defaultWindowHeight) {
			registerExtraWindow(window, windowX, windowY, windowWidth, windowHeight, defaultWindowWidth, defaultWindowHeight, false);
		}
		public void registerExtraWindow(Window window, ValueKey windowX, ValueKey windowY, ValueKey windowWidth, ValueKey windowHeight, int defaultWindowWidth, int defaultWindowHeight, boolean forceSize) {
			registerWindow(window, defaultWindowWidth, defaultWindowHeight, forceSize, new WindowSizePosStorage() {
				@Override public boolean isPosSet (String[] prefkeys) { return contains(windowX, windowY); }
				@Override public boolean isSizeSet(String[] prefkeys) { return contains(windowWidth, windowHeight); }
				@Override public Point     getWindowPos (              ) { return getPoint(windowX, windowY); }
				@Override public void      setWindowPos (Point location) {        putPoint(windowX, windowY,location); }
				@Override public Dimension getWindowSize(              ) { return getDimension(windowWidth, windowHeight); }
				@Override public void      setWindowSize(Dimension size) {        putDimension(windowWidth, windowHeight,size); }
			});
		}
		
		private void registerWindow(Window window, int defaultWindowWidth, int defaultWindowHeight, boolean forceSize, WindowSizePosStorage storage) {
			String[] prefkeys;
			try {
				prefkeys = super.preferences.keys();
			} catch (BackingStoreException ex) {
				ex.printStackTrace();
				prefkeys = null;
			}
			
			if (prefkeys!=null && storage.isPosSet (prefkeys)              ) window.setLocation(storage.getWindowPos ());
			if (prefkeys!=null && storage.isSizeSet(prefkeys) && !forceSize) window.setSize    (storage.getWindowSize());
			else if (defaultWindowWidth>0 && defaultWindowHeight>0) {
				Dimension size = new Dimension(defaultWindowWidth, defaultWindowHeight);
				window.setSize( size );
				storage.setWindowSize( size );
			}
			
			window.addComponentListener(new ComponentListener() {
				@Override public void componentShown  (ComponentEvent e) {}
				@Override public void componentHidden (ComponentEvent e) {}
				@Override public void componentResized(ComponentEvent e) { storage.setWindowSize( window.getSize() ); }
				@Override public void componentMoved  (ComponentEvent e) { storage.setWindowPos ( window.getLocation() ); }
			});
		}

		@Override public boolean isPosSet (String[] prefkeys) { return isIn(prefkeys,"WindowX","WindowY"); }
		@Override public boolean isSizeSet(String[] prefkeys) { return isIn(prefkeys,"WindowWidth","WindowHeight"); }
		
		private boolean isIn(String[] prefkeys, String... keys) {
			for (String str : keys) {
				boolean found = false;
				for (String dataStr : prefkeys)
					if ( (str==null && dataStr==null) || (str!=null && str.equals(dataStr)) ) {
						found = true;
						break;
					}
				if (!found)
					return false;
			}
			return true;
		}

		@Override public Point     getWindowPos (              ) { return super.getPoint("WindowX","WindowY"); }
		@Override public void      setWindowPos (Point location) {        super.putPoint("WindowX","WindowY",location); }
		@Override public Dimension getWindowSize(              ) { return super.getDimension("WindowWidth","WindowHeight"); }
		@Override public void      setWindowSize(Dimension size) {        super.putDimension("WindowWidth","WindowHeight",size); }
		
		public void registerWindowSizeListener(Window window, ValueKey windowWidth, ValueKey windowHeight, int defaultWindowWidth, int defaultWindowHeight) {
			
			Dimension size = null;
			if (contains(windowWidth, windowHeight))
				size = getDimension(windowWidth, windowHeight);
			
			else if (defaultWindowWidth>0 && defaultWindowHeight>0)
				size = new Dimension(defaultWindowWidth, defaultWindowHeight);
			
			if (size!=null)
				window.setSize(size);
			
			window.addComponentListener(new ComponentListener() {
				@Override public void componentShown  (ComponentEvent e) {}
				@Override public void componentHidden (ComponentEvent e) {}
				@Override public void componentResized(ComponentEvent e) { putDimension(windowWidth, windowHeight, window.getSize() ); }
				@Override public void componentMoved  (ComponentEvent e) {}
			});
		}
		
		public final static class SplitPaneDividersDefinition<ValueKey extends Enum<ValueKey>>
		{
			private final Window window;
			private final EnumMap<ValueKey, JSplitPane> splitPanes;

			public SplitPaneDividersDefinition(Window window, Class<ValueKey> enumClass)
			{
				this.window = window;
				splitPanes = new EnumMap<>(enumClass);
			}

			public SplitPaneDividersDefinition<ValueKey> add(JSplitPane splitPane, ValueKey valueKey)
			{
				splitPanes.put(valueKey, splitPane);
				return this;
			}
		}
		
		public interface SaveToken
		{
			void saveDividerLocations();
		}
		
		public SaveToken registerSplitPaneDividers(SplitPaneDividersDefinition<ValueKey> def)
		{
			return registerSplitPaneDividers(def, false);
		}
		
		public static void setDividerLocation(JSplitPane pane, int dividerLoc, boolean showDividerLoc)
		{
			pane.setDividerLocation(dividerLoc);
			if (showDividerLoc) {
				System.out.printf("init  DividerLocation <- %d%n", dividerLoc);
				System.out.printf("check DividerLocation -> %d%n", pane.getDividerLocation());
			}
			
			SwingUtilities.invokeLater(()->{
				if (showDividerLoc) System.out.printf("check DividerLocation 1a -> %d%n", pane.getDividerLocation());
				if (dividerLoc!=pane.getDividerLocation()) pane.setDividerLocation(dividerLoc);
				if (showDividerLoc) System.out.printf("check DividerLocation 1b -> %d%n", pane.getDividerLocation());
				
				if (showDividerLoc) 
					SwingUtilities.invokeLater(()->{
						System.out.printf("check DividerLocation 2  -> %d%n", pane.getDividerLocation());
					});
			});
		}
		
		public SaveToken registerSplitPaneDividers(SplitPaneDividersDefinition<ValueKey> def, boolean showDividerLoc)
		{
			SwingUtilities.invokeLater(()->{
				def.splitPanes.forEach((key,pane)->{
					int storedValue = getInt(key, -1);
					if (storedValue < 0) return;
					
					pane.setDividerLocation(storedValue);
					if (showDividerLoc) {
						System.out.printf("init  DividerLocation[%s] <- %d%n", key, storedValue);
						System.out.printf("check DividerLocation[%s] -> %d%n", key, pane.getDividerLocation());
					}
				});
				
				SwingUtilities.invokeLater(()->{
					def.splitPanes.forEach((key,pane)->{
						int storedValue = getInt(key, -1);
						if (storedValue < 0) return;
						
						if (showDividerLoc) System.out.printf("check DividerLocation[%s] 1a -> %d%n", key, pane.getDividerLocation());
						if (storedValue!=pane.getDividerLocation()) pane.setDividerLocation(storedValue);
						if (showDividerLoc) System.out.printf("check DividerLocation[%s] 1b -> %d%n", key, pane.getDividerLocation());
						
						if (showDividerLoc) 
							SwingUtilities.invokeLater(()->{
								System.out.printf("check DividerLocation[%s] 2  -> %d%n", key, pane.getDividerLocation());
							});
					});
				});
			});
			
			Consumer<String> saveDividerLocations = label->{
				def.splitPanes.forEach((key,pane)->{
					int currentValue = pane.getDividerLocation();
					if (showDividerLoc) System.out.printf("save DividerLocation[%s,\"%s\"]  %d%n", key, label, currentValue);
					putInt(key, currentValue);
				});
			};
			
			def.window.addWindowListener(new WindowAdapter() {
				@Override public void windowClosing(WindowEvent e) { saveDividerLocations.accept("Closing"); }
				@Override public void windowClosed (WindowEvent e) { saveDividerLocations.accept("Closed" ); }
			});
			
			return ()->saveDividerLocations.accept("External");
		}
	}
	
	public static class Global extends Settings<Global.ValueGroup,Global.ValueKey> {
		
		private static Global instance = null;
		public static Global getInstance() {
			if (instance==null)
				instance = new Global();
			return instance;
		}

		public enum ValueGroup implements Settings.GroupKeys<ValueKey> {
			;
			ValueKey[] keys;
			ValueGroup(ValueKey...keys) { this.keys = keys;}
			@Override public ValueKey[] getKeys() { return keys; }
		}

		public enum ValueKey {
			VrmlViewer
		}

		public Global() {
			super(Global.class);
		}
	}
}
