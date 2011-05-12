package net.schwarzbaer.java.lib.gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.LookAndFeel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.UnsupportedLookAndFeelException;

public class LookAndFeels implements ActionListener {

	public static void showLookAndFeels() {
		System.out.println("LookAndFeels:");
		System.out.printf("   -2 CrossPlatform: \"%s\"\r\n",UIManager.getCrossPlatformLookAndFeelClassName());
		System.out.printf("   -1 System       : \"%s\"\r\n",UIManager.getSystemLookAndFeelClassName());
		LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
		for (int i=0; i<installedLookAndFeels.length; i++) {
			System.out.printf("   %2d [installed]  : \"%s\" [%s]\r\n",i,installedLookAndFeels[i].getName(),installedLookAndFeels[i].getClassName());
		}
	}

	public static void setLookAndFeel( int lafID) {
		switch (lafID) {
		case -2: setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName()); break;
		case -1: setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); break;
		default:
			LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
			if (installedLookAndFeels!=null) {
				if ( (lafID>=0) && (lafID<installedLookAndFeels.length) ) {
					setLookAndFeel(installedLookAndFeels[lafID].getClassName());
				}
			}
		}
	}

	private static void setLookAndFeel(String lookAndFeelClassName) {
		try { UIManager.setLookAndFeel(lookAndFeelClassName); }
		catch (ClassNotFoundException e) { System.out.printf("Exception in SetLookAndFeel: Can't find class of LookAndFeel \"%s\".",lookAndFeelClassName); }
		catch (InstantiationException e) { System.out.printf("Exception in SetLookAndFeel: Can't instatiate LookAndFeel \"%s\".",lookAndFeelClassName); }
		catch (IllegalAccessException e) { System.out.printf("Exception in SetLookAndFeel: Can't access LookAndFeel \"%s\".",lookAndFeelClassName); }
		catch (UnsupportedLookAndFeelException e) { System.out.printf("Exception in SetLookAndFeel: LookAndFeel \"%s\"is not supported.",lookAndFeelClassName); }
	}

	public static void fillStyleMenu(JMenu styleSubMenu, JFrame mainwindow) {
		LookAndFeels laf = new LookAndFeels(mainwindow);
		String systemLAF = String.format("System (%s)"       ,reduceClassName(UIManager.getSystemLookAndFeelClassName()));
		String crossLAF  = String.format("CrossPlatform (%s)",reduceClassName(UIManager.getCrossPlatformLookAndFeelClassName()));
		
		laf.menuItemMap.clear();;
		laf.addCheckBoxMenuItem(styleSubMenu, systemLAF, "System"); 
		laf.addCheckBoxMenuItem(styleSubMenu, crossLAF , "CrossPlatform");
		styleSubMenu.addSeparator();
		LookAndFeelInfo[] installedLookAndFeels = UIManager.getInstalledLookAndFeels();
		for (LookAndFeelInfo lafInfo:installedLookAndFeels) {
			laf.addCheckBoxMenuItem(styleSubMenu, lafInfo.getName(), lafInfo.getClassName());
		}
		styleSubMenu.addSeparator();
		styleSubMenu.add( createMenuItem("show all styles", "show all", laf) );
		
		laf.updateLAFmenu();
	}

	private static String reduceClassName(String lookAndFeelClassName) {
		
		int pos = lookAndFeelClassName.lastIndexOf('.');
		if (pos<0) return lookAndFeelClassName;
		
		String shortClassName = lookAndFeelClassName.substring(pos+1);
		if (shortClassName.endsWith("LookAndFeel")) {
			shortClassName = shortClassName.substring(0, shortClassName.length()-"LookAndFeel".length());
		}
		return shortClassName;
	}
	
	public static String getCurrentLookAndFeel() {
		LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
		if (lookAndFeel==null) return "";
		return reduceClassName(lookAndFeel.getName());
	}

	private JFrame mainwindow;
	private HashMap<String, JCheckBoxMenuItem> menuItemMap;

	public LookAndFeels(JFrame mainwindow) {
		this.mainwindow = mainwindow;
		this.menuItemMap = new HashMap<String,JCheckBoxMenuItem>();
	}

	private void addCheckBoxMenuItem(JMenu styleSubMenu, String lafName, String actionCommandStr) {
		JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem(lafName,false);
		configureMenuItem(menuItem, actionCommandStr, this);
		styleSubMenu.add( menuItem );
		menuItemMap.put(actionCommandStr, menuItem);
	}
	private static JMenuItem createMenuItem(String title, String actionCommandStr, ActionListener al) {
		return configureMenuItem(new JMenuItem(title), actionCommandStr, al);
	}

	private static JMenuItem configureMenuItem(JMenuItem menuItem, String actionCommandStr, ActionListener al) {
		menuItem.setActionCommand(actionCommandStr);
		menuItem.addActionListener(al);
		return menuItem;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (e.getActionCommand().equals("show all"     )) { showLookAndFeels(); return; }
		if (e.getActionCommand().equals("System"       )) { setLookAndFeel(-1); SwingUtilities.updateComponentTreeUI(mainwindow); updateLAFmenu(); return; }
		if (e.getActionCommand().equals("CrossPlatform")) { setLookAndFeel(-2); SwingUtilities.updateComponentTreeUI(mainwindow); updateLAFmenu(); return; }
		setLookAndFeel( e.getActionCommand() );
		SwingUtilities.updateComponentTreeUI(mainwindow);
		updateLAFmenu();
	}

	private void updateLAFmenu() {
		LookAndFeel lookAndFeel = UIManager.getLookAndFeel();
		if (lookAndFeel!=null) {
			String currentLAF = lookAndFeel.getClass().getName();
			for (String lafName:menuItemMap.keySet()) {
				JCheckBoxMenuItem menuItem = menuItemMap.get(lafName);
				if (menuItem!=null) {
					//System.out.println(lookAndFeel.getClass().getName());
					menuItem.setSelected( lafName.equals(currentLAF) );
				}
			}
		}
	}
	
}
