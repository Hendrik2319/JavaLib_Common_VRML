package net.schwarzbaer.java.lib.gui;

import javax.swing.Icon;

public final class GeneralIcons
{
	public enum GrayCommandIcons {
		Muted, UnMuted, Up, Down, Power_IsOn, Power_IsOff, Reload, Download, Image, Save, Add, Copy, Paste, Cut, Delete, Folder, AddFolder, ReloadFolder,
		Muted_Dis, UnMuted_Dis, Up_Dis, Down_Dis, Power_IsOn_Dis, Power_IsOff_Dis, Reload_Dis, Download_Dis, Image_Dis, Save_Dis, Add_Dis, Copy_Dis, Paste_Dis, Cut_Dis, Delete_Dis, Folder_Dis, AddFolder_Dis, ReloadFolder_Dis,
		;
		public Icon getIcon() { return iconSource.getCachedIcon(this); }
		private static IconSource.CachedIcons<GrayCommandIcons> iconSource = IconSource.createCachedIcons(16, 16, 18, "GeneralIcons.GrayCommandIcons.png", GrayCommandIcons.values());
	}
	
}
