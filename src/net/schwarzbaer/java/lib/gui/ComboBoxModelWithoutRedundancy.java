package net.schwarzbaer.java.lib.gui;

import java.util.Iterator;
import java.util.Vector;

import javax.swing.MutableComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

public class ComboBoxModelWithoutRedundancy<ItemType> implements MutableComboBoxModel<ItemType> {
	
	private static final boolean DEBUG = false;
	
	private final Vector<ListDataListener> listeners;
	private final Vector<ItemType> items;
	private String selectedItem;

	public ComboBoxModelWithoutRedundancy() {
		this.listeners = new Vector<ListDataListener>();
		this.items = new Vector<ItemType>();
	}
	
	public ComboBoxModelWithoutRedundancy(ItemType[] items) {
		this.listeners = new Vector<ListDataListener>();
		this.items = new Vector<ItemType>();
		for(ItemType item:items) this.items.add(item);
	}

	public Iterator<ItemType> getItemIterator() {
		return items.iterator();
	}

	@Override public void    addListDataListener(ListDataListener ldl) { listeners.   add(ldl); }
	@Override public void removeListDataListener(ListDataListener ldl) { listeners.remove(ldl); }

	private void fireContentsChanged(int i1, int i2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.CONTENTS_CHANGED, i1, i2); 
		for (ListDataListener l:listeners) l.contentsChanged(e);
	}
	private void fireIntervalAdded(int i1, int i2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.INTERVAL_ADDED, i1, i2);
		for (ListDataListener l:listeners) l.intervalAdded(e);
	}
	private void fireIntervalRemoved(int i1, int i2) {
		ListDataEvent e = new ListDataEvent(this,ListDataEvent.INTERVAL_REMOVED, i1, i2); 
		for (ListDataListener l:listeners) l.intervalRemoved(e);
	}

	@Override
	public Object getSelectedItem() {
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.getSelectedItem() [=\"%s\"]\r\n",this.selectedItem);
		return this.selectedItem;
	}

	@Override
	public void setSelectedItem(Object selectedItem) {
		this.selectedItem = (selectedItem==null?null:selectedItem.toString());
		fireContentsChanged(-1, -1);
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.setSelectedItem(\"%s\")\r\n",selectedItem);
	}

	@Override public int getSize() { return items.size(); }
	
	@Override public ItemType getElementAt(int i) {
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.getElementAt(%d) [=\"%s\"]\r\n",i,items.get(i));
		return items.get(i);
	}

	public int getIndex(ItemType item) {
		return items.indexOf(item);
	}

	@Override
	public void addElement(ItemType item) {
		if (items.contains(item)) {
			if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.addElement(\"%s\")  not added, already in list \r\n",item);
			return;
		}
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.addElement(\"%s\")\r\n",item);
		items.add(item);
		int index = items.size()-1;
		fireIntervalAdded(index, index);
	}

	@Override
	public void insertElementAt(ItemType item, int index) {
		if (items.contains(item)) {
			if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.insertElementAt(\"%s\",%d)  not inserted, already in list \r\n",item,index);
			return;
		}
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.insertElementAt(\"%s\",%d)\r\n",item,index);
		items.insertElementAt(item,index);
		fireIntervalAdded(index, index);
	}

	@Override
	public void removeElement(Object item) {
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.removeElement(\"%s\")\r\n",item);
		int index = items.indexOf(item);
		if (index<0) return;
		items.remove(item);
		fireIntervalRemoved(index, index);
	}

	@Override
	public void removeElementAt(int index) {
		if (DEBUG) System.out.printf("ComboBoxModelWithoutRedundancy.removeElementAt(%d)\r\n",index);
		items.removeElementAt(index);
		fireIntervalRemoved(index, index);
	}
}