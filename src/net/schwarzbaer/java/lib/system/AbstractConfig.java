package net.schwarzbaer.java.lib.system;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.Vector;

public abstract class AbstractConfig {
	
	protected final String[] prefixes;

	protected AbstractConfig(String[] prefixes) {
		this.prefixes = prefixes;
	}
	
	public void readConfig(String cfgFileName) {
		clearConfig();
		
		BufferedReader cfgInput;
		try { cfgInput = new BufferedReader( new FileReader(cfgFileName) ); }
		catch (FileNotFoundException e) { return; } 
		
		try {
			String str;
			while( (str = cfgInput.readLine())!=null ) {
				for (int i=0; i<prefixes.length; i++)
					if (str.startsWith(prefixes[i])) {
						setValue(i,str.substring(prefixes[i].length()));  						
					}
			}
		} catch (IOException e) {}
		
		try { cfgInput.close(); } catch (IOException e) {}
	}
	
	protected abstract void clearConfig();
	protected abstract void setValue(int itemIndex, String itemValueStr);
	protected boolean getBooleanValue(String itemValueStr) {
		return itemValueStr.equals("true");
	}

	public void writeConfig(String cfgFileName) {
		
		PrintWriter cfgOutput;
		try { cfgOutput = new PrintWriter(cfgFileName); }
		catch (FileNotFoundException e) { return; }
		
		for (int i=0; i<prefixes.length; i++) {
			writeConfigItem(cfgOutput,i);
		}
		cfgOutput.close();
	}

	protected abstract void writeConfigItem(PrintWriter cfgOutput, int itemIndex);
	
	protected void writeConfigItemValue(PrintWriter cfgOutput, int itemIndex, String itemValue) {
		cfgOutput.println(prefixes[itemIndex]+itemValue);
	}
	protected void writeConfigItemValue(PrintWriter cfgOutput, int itemIndex, boolean itemValue) {
		writeConfigItemValue(cfgOutput, itemIndex, (itemValue?"true":"false"));
	}
	protected void writeConfigItemValue(PrintWriter cfgOutput, int itemIndex, Vector<String> itemValue) {
		Iterator<String> it = itemValue.iterator();
		while( it.hasNext() )
			writeConfigItemValue(cfgOutput, itemIndex, it.next());
	}
}
