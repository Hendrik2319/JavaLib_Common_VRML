
package net.schwarzbaer.java.lib.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultBoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileSystemView;

/**
 * 
 * @author Hendrik
 */
public final class GUI {

	public static void setSystemLookAndFeel() {
		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
		catch (ClassNotFoundException e) {}
		catch (InstantiationException e) {}
		catch (IllegalAccessException e) {}
		catch (UnsupportedLookAndFeelException e) {}
	}
	
	public static void fixTextAreaFont(JTextArea textArea) {
		String keyLabelFont = "Label.font";
//		String keyTextAreaFont = "TextArea.font";
		
		Font    labelFont = UIManager.getDefaults().getFont(keyLabelFont);
//		Font textAreaFont = UIManager.getDefaults().getFont(keyTextAreaFont);
		Font textAreaFont = textArea.getFont();
		
		if (   labelFont==null) return;
		if (textAreaFont==null) return;
		if (labelFont.getSize()==textAreaFont.getSize()) return;
		
		System.out.printf("Change font size of TextArea from %d to %d.\r\n",textAreaFont.getSize(),labelFont.getSize());
		Font newTextAreaFont = textAreaFont.deriveFont( (float)labelFont.getSize() );
		
		textArea.setFont(newTextAreaFont);
	}
	
    public static void listUIDefaults() {
    	listUIDefaults(null,(KeyAcceptor)null);
	}
	
    public static void listUIDefaults(String keyPrefix) {
    	listUIDefaults(null,keyPrefix);
	}
	
    public static void listUIDefaults(KeyAcceptor keyAcceptor) {
    	listUIDefaults(null,keyAcceptor);
	}
	
    public static void listUIDefaults(Class<?> classObj, final String keyPrefix) {
    	listUIDefaults(classObj, new KeyAcceptor(){
			@Override public boolean accept(String key) {
				if (key==null) return false;
				if (keyPrefix==null) return false;
				return key.startsWith(keyPrefix);
			}
		} );
	}
	
    public static interface KeyAcceptor {
    	public boolean accept( String key );
    }
    
    public static void listUIDefaults(Class<?> classObj, KeyAcceptor keyAcceptor ) {
		List<String> keys = new ArrayList<String>();
		int maxKeyLength = 0;
		
		for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
			if ( (classObj==null) || classObj.isAssignableFrom( entry.getValue().getClass() ) ) {
				String key = entry.getKey().toString();
				if ( (keyAcceptor==null) || keyAcceptor.accept(key) ) {
					maxKeyLength = Math.max( maxKeyLength, key.length() );
					keys.add(key);
				}
			}
		}
		Collections.sort(keys);
		
		if (classObj==null) System.out.printf("Current UIDefaults have following values:\r\n");
		else                System.out.printf("Current UIDefaults have following \"%s\" values:\r\n",classObj.getName());
		
		for (String name : keys) System.out.printf("   %-"+maxKeyLength+"s: %s\r\n",name,UIManager.getDefaults().get(name));
	}
	
    public static void listKeysOfUIDefaults() {
    	listKeysOfUIDefaults(null);
	}
	
    public static void listKeysOfUIDefaults(Class<?> classObj) {
		List<String> keys = new ArrayList<String>();
		
		for (Map.Entry<Object, Object> entry : UIManager.getDefaults().entrySet()) {
			if ( (classObj==null) || classObj.isAssignableFrom( entry.getValue().getClass() ) ) {
				keys.add(entry.getKey().toString());
			}
		}
		Collections.sort(keys);
		
		if (classObj==null) System.out.printf("Current UIDefaults have following keys:\r\n");
		else                System.out.printf("Current UIDefaults have following keys for \"%s\":\r\n",classObj);
		
		for (String name : keys) System.out.printf("   %s\r\n",name);
	}
	
    public static void moveToScreenCenter(JFrame window) {
    	Dimension size = window.getSize();
    	Rectangle screen = window.getGraphicsConfiguration().getBounds();
    	window.setLocation(
	            (screen.width -size.width )/2+screen.x,
	            (screen.height-size.height)/2+screen.y
	        );
	}
	
    public static Icon getFileIcon(File file) {
		return FileSystemView.getFileSystemView().getSystemIcon( file );
	}

	public static void addButtonToPanelAndButtonGroup( JPanel buttonPanel, ButtonGroup buttonGroup, AbstractButton btn ) {
    	buttonPanel.add(btn);
    	buttonGroup.add(btn);
    }

    public static void addLabelAndField(JPanel labelPanel, JPanel fieldPanel, String label, JComponent field) {
    	addLabelAndField(labelPanel, fieldPanel, new JLabel(label), field);
    }
    
    public static void addLabelAndField(JPanel labelPanel, JPanel fieldPanel, JComponent labelObj, JComponent field) {
        labelPanel.add( labelObj );
        fieldPanel.add( field );
	}

	public static JPanel createLabelAndFieldPanel( String labelStr, Component comp ) {
        JPanel panel = new JPanel( new BorderLayout( 3,3 ) );
        panel.add( new JLabel( labelStr ), BorderLayout.WEST );
        panel.add( comp, BorderLayout.CENTER );
        return panel;
    }
	
    private static <AC,C extends JComponent> C addToDisabler(Disabler<AC> disabler, AC actionCommand, C comp) {
		disabler.add(actionCommand, comp);
		return comp;
	}

	public static JMenu createMenu( String title, int mnemonic, boolean enabled ) {
        JMenu menu = new JMenu( title );
        menu.setMnemonic( mnemonic );
        menu.setEnabled( enabled );
        return menu;
    }
    
    public static JMenu createMenu(String title, int mnemonic, JMenuItem[] jMenuItems) {
        JMenu menu = createMenu( title, mnemonic, true );
        for (JMenuItem menuItem:jMenuItems)
        	if (menuItem==null) menu.addSeparator();
        	else                menu.add(menuItem);
        return menu;
	}

	public static JMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener ) {
    	JMenuItem menuItem = new JMenuItem( title );
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand( commandStr );
        return menuItem;
    }
    
    public static JMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, boolean enabled ) {
		JMenuItem menuItem = createMenuItem( title,commandStr,actionListener );
	    menuItem.setEnabled(enabled);
	    return menuItem;
	}

	public static JMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, boolean enabled, String keyStrokeStr ) {
    	JMenuItem menuItem = createMenuItem(title, commandStr, actionListener, enabled);
    	menuItem.setAccelerator( KeyStroke.getKeyStroke( keyStrokeStr ) );
    	return menuItem;
    }
    
    public static JMenuItem createMenuItem(String title, String commandStr, int mnemonic, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem( title );
        menuItem.setActionCommand(commandStr);
        menuItem.addActionListener( actionListener );
        menuItem.setMnemonic( mnemonic );
        return menuItem;
    }

    public static JMenuItem createMenuItem(String title, String commandStr, int mnemonic, ActionListener actionListener, int accKey, int accMask ) {
        JMenuItem menuItem = createMenuItem( title, commandStr, mnemonic, actionListener );
        menuItem.setAccelerator( KeyStroke.getKeyStroke( accKey, accMask ) );
        return menuItem;
    }

    public static JCheckBoxMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, boolean isSelected, boolean isEnabled ) {
    	JCheckBoxMenuItem menuItem = new JCheckBoxMenuItem( title );
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand( commandStr );
        menuItem.setSelected(isSelected);
        menuItem.setEnabled(isEnabled);
        return menuItem;
    }

    public static JRadioButtonMenuItem createMenuItem( String title, String commandStr, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
    	JRadioButtonMenuItem menuItem = new JRadioButtonMenuItem( title );
        menuItem.addActionListener(actionListener);
        menuItem.setActionCommand( commandStr );
        menuItem.setSelected(isSelected);
        menuItem.setEnabled(isEnabled);
        buttonGroup.add(menuItem);
        return menuItem;
	}

	public static JButton setComp( JButton comp, String commandStr, ActionListener actionListener, boolean enabled ) {
		comp.setEnabled(enabled);
		if (actionListener!=null) comp.addActionListener(actionListener);
		if (commandStr    !=null) comp.setActionCommand( commandStr );
		return comp;
	}

	public static <AC> JButton setComp( JButton comp, ActionListener actionListener, Disabler<AC> disabler, AC actionCommand, boolean enabled) {
		setComp( comp, actionCommand==null ? null : actionCommand.toString(), actionListener, enabled );
		if (disabler!=null) disabler.add(actionCommand,comp);
		return comp;
	}

    public static <AC> JButton createButton( String title, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener ) {
    	return addToDisabler(disabler, actionCommand, createButton( title, actionCommand.toString(), actionListener ));
    }

    public static JButton createButton( String title, String commandStr, ActionListener actionListener ) {
		return setComp( new JButton(title), commandStr, actionListener, true );
    }

    public static JButton createButton( String title, String commandStr, ActionListener actionListener, Icon icon ) {
		return setComp( new JButton(title,icon), commandStr, actionListener, true );
	}

    public static JButton createButton( Icon icon, String commandStr, ActionListener actionListener ) {
		return setComp( new JButton(icon), commandStr, actionListener, true );
	}

	public static JButton createButton(String title, String commandStr, ActionListener actionListener, boolean enabled) {
		return setComp( new JButton(title), commandStr, actionListener, enabled );
	}

	public static JButton createButton( String title, String commandStr, ActionListener actionListener, String toolTipText ) {
	    JButton btn = createButton( title, commandStr, actionListener );
	    btn.setToolTipText( toolTipText );
	    return btn;
	}

	public static <AC> JToggleButton createToggleButton( String title, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener ) {
        return createToggleButton( title, actionCommand, disabler, actionListener, null, false, true );
    }
	public static JToggleButton createToggleButton( String title, String commandStr, ActionListener actionListener ) {
        return createToggleButton( title, commandStr, actionListener, null, false, true );
    }

	public static <AC> JToggleButton createToggleButton( String title, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
		return addToDisabler(disabler, actionCommand, createToggleButton( title, actionCommand.toString(), actionListener, buttonGroup, isSelected, isEnabled ) );
	}
	public static JToggleButton createToggleButton( String title, String commandStr, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
    	JToggleButton button = new JToggleButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        button.setSelected(isSelected);
        button.setEnabled(isEnabled);
        if (buttonGroup!=null) buttonGroup.add(button);
        return button;
    }

	public static <AC> JRadioButton createRadioButton( String title, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
		return addToDisabler(disabler, actionCommand, createRadioButton( title, actionCommand.toString(), actionListener, buttonGroup, isSelected, isEnabled ) );
	}
	public static JRadioButton createRadioButton( String title, String commandStr, ActionListener actionListener, ButtonGroup buttonGroup, boolean isSelected, boolean isEnabled ) {
    	JRadioButton button = new JRadioButton( title );
        button.addActionListener(actionListener);
        button.setActionCommand( commandStr );
        button.setSelected(isSelected);
        button.setEnabled(isEnabled);
        if (buttonGroup!=null) buttonGroup.add(button);
        return button;
    }
	
	public static <E> void setSelectedValueOf( JComboBox<E> cmbbx, E value ) {
		cmbbx.setSelectedItem(value);
    }
	
	public static <E> E getSelectedValueOf( JComboBox<E> cmbbx ) {
		int index = cmbbx.getSelectedIndex();
		if (index<0) return null;
		return cmbbx.getItemAt(index);
	}
	
	public static <AC,E> JComboBox<E> createComboBox_Gen( ComboBoxModel<E> comboBoxModel, int selected, AC actionCommand, boolean enabled, Disabler<AC> disabler, ActionListener actionListener ) {
        return setComboBox_Gen( addToDisabler(disabler, actionCommand, new JComboBox<E>( comboBoxModel )), selected, actionCommand.toString(), enabled, actionListener);
    }
	public static <E> JComboBox<E> createComboBox_Gen( ComboBoxModel<E> comboBoxModel, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( comboBoxModel ), selected, commandStr, enabled, actionListener);
    }
	public static <E> JComboBox<E> createComboBox_Gen( ComboBoxModel<E> comboBoxModel, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( comboBoxModel ), commandStr, enabled, actionListener);
    }
	public static <AC,E> JComboBox<E> createComboBox_Gen( E[] items, int selected, AC actionCommand, boolean enabled, Disabler<AC> disabler, ActionListener actionListener ) {
        return setComboBox_Gen( addToDisabler(disabler, actionCommand, new JComboBox<E>( items )), selected, actionCommand.toString(), enabled, actionListener);
    }
	public static <E> JComboBox<E> createComboBox_Gen( E[] items, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( items ), selected, commandStr, enabled, actionListener);
    }
	public static <E> JComboBox<E> createComboBox_Gen( E[] items, int selected, boolean enabled, Consumer<E> action) {
        JComboBox<E> comp = new JComboBox<E>( items );
		return setComboBox_Gen( comp, selected, null, enabled, e->{
			int i = comp.getSelectedIndex();
			if (i<0) action.accept(null);
			else action.accept(items[i]);
		});
	}
	public static <E> JComboBox<E> createComboBox_Gen( Vector<E> items, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox_Gen( new JComboBox<E>( items ), selected, commandStr, enabled, actionListener);
    }
	private static <E> JComboBox<E> setComboBox_Gen(JComboBox<E> cmbBx, int selected, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setSelectedIndex(selected);
		return setComboBox_Gen( cmbBx, commandStr, enabled, actionListener);
    }
	private static <E> JComboBox<E> setComboBox_Gen(JComboBox<E> cmbBx, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setActionCommand(commandStr);
        cmbBx.addActionListener(actionListener);
        cmbBx.setEnabled(enabled);
        return cmbBx;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static JComboBox createComboBox( ComboBoxModel comboBoxModel, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( comboBoxModel ), commandStr, enabled, actionListener);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
	public static JComboBox createComboBox( Object[] items, int selected, String commandStr, boolean enabled, ActionListener actionListener ) {
        return setComboBox( new JComboBox( items ), selected, commandStr, enabled, actionListener);
    }

	@SuppressWarnings("rawtypes")
	private static JComboBox setComboBox(JComboBox cmbBx, int selected, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setSelectedIndex(selected);
		return setComboBox( cmbBx, commandStr, enabled, actionListener);
    }

	@SuppressWarnings("rawtypes")
	private static JComboBox setComboBox(JComboBox cmbBx, String commandStr, boolean enabled, ActionListener actionListener) {
		cmbBx.setActionCommand(commandStr);
        cmbBx.addActionListener(actionListener);
        cmbBx.setEnabled(enabled);
        return cmbBx;
	}
	
	public static <AC,E> JList<E> createList( ListModel<E> listModel, AC actionCommand, Disabler<AC> disabler, ActionListener listener ) {
		return createList( listModel, true, actionCommand, disabler, listener );
	}
	public static <AC,E> JList<E> createList( ListModel<E> listModel, boolean ignoreAdjusting, AC actionCommand, Disabler<AC> disabler, ActionListener listener ) {
		JList<E> list = new JList<E>(listModel);
		list.addListSelectionListener(new ListSelectionListener() {
			@Override public void valueChanged(ListSelectionEvent e) {
				if (ignoreAdjusting && e.getValueIsAdjusting()) return;
				listener.actionPerformed(new ActionEvent(e.getSource(), ActionEvent.ACTION_PERFORMED, actionCommand.toString()));
			}
		});
		disabler.add(actionCommand, list);
		return list;
	}
	 
    public static <AC> JCheckBox createCheckBox( String title, boolean preselected, ButtonGroup buttonGroup, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener ) {
    	JCheckBox checkBox = createCheckBox( title, preselected, actionCommand, disabler, actionListener );
    	if (buttonGroup!=null) buttonGroup.add(checkBox);
    	return checkBox;
    }
    public static <AC> JCheckBox createCheckBox( String title, boolean preselected, AC actionCommand, Disabler<AC> disabler, ActionListener actionListener ) {
    	return addToDisabler(disabler, actionCommand, createCheckBox( title, preselected, actionCommand.toString(), actionListener ) );
	}
    public static JCheckBox createCheckBox( String title, boolean preselected, String commandStr, ActionListener actionListener ) {
    	return createCheckBox( title, preselected, commandStr, SwingConstants.RIGHT, true, actionListener );
	}
    public static JCheckBox createCheckBox( String title, boolean preselected, String commandStr, int alignment, boolean enabled, ActionListener actionListener ) {
        JCheckBox checkBox = new JCheckBox(title,preselected);
        checkBox.setActionCommand( commandStr );
        checkBox.addActionListener( actionListener );
        checkBox.setHorizontalTextPosition( alignment );
        checkBox.setEnabled(enabled);
        return checkBox;
    }

    public static JPanel    createTopAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.NORTH); }
	public static JPanel createBottomAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.SOUTH); }
    public static JPanel  createRightAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.EAST); }
	public static JPanel   createLeftAlignedPanel(Component comp) { return createAlignedPanel( comp, BorderLayout.WEST); }

    public static JPanel    createTopAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.NORTH); }
	public static JPanel createBottomAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.SOUTH); }
    public static JPanel  createRightAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.EAST); }
	public static JPanel   createLeftAlignedPanel(Component comp, Component other_comp) { return createAlignedPanel( comp, other_comp, BorderLayout.WEST); }

    public static JPanel    createTopAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.NORTH, spacing); }
	public static JPanel createBottomAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.SOUTH, spacing); }
    public static JPanel  createRightAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.EAST,  spacing); }
	public static JPanel   createLeftAlignedPanel(Component comp, Component other_comp, int spacing) { return createAlignedPanel( comp, other_comp, BorderLayout.WEST,  spacing); }

	private static JPanel createAlignedPanel(Component comp, String layoutPosition) {
		return createAlignedPanel(comp,new JLabel(),layoutPosition);
	}
	private static JPanel createAlignedPanel(Component comp, Component center_comp, String layoutPosition) {
		return createAlignedPanel(comp,center_comp,layoutPosition,0);
	}
	static JPanel createAlignedPanel(Component comp, Component center_comp, String layoutPosition, int spacing) {
		JPanel panel = new JPanel( new BorderLayout(spacing,spacing) );
		panel.add(center_comp, BorderLayout.CENTER);
		panel.add(comp, layoutPosition);
		return panel;
	}

	public static JPanel createLeftRightAlignedPanel(Component leftComp, Component rightComp) {
		JPanel panel = new JPanel( new BorderLayout() );
		panel.add(leftComp, BorderLayout.WEST);
		panel.add(new JLabel(), BorderLayout.CENTER);
		panel.add(rightComp, BorderLayout.EAST);
		return panel;
	}

	public static JPanel createTitlePanel(String title, Component comp) {
		JPanel panel = new JPanel( new GridLayout(1,0,3,3) );
		panel.setBorder(BorderFactory.createTitledBorder(title));
		panel.add(comp);
		return panel;
	}

	public static JScrollPane createScrollPanel(Component comp, int width, int height) {
		return createScrollPanel(comp, width, height, 3);
	}

	public static JScrollPane createScrollPanel(Component comp, int width, int height, int innerBorder) {
		JScrollPane scrollPanel = new JScrollPane(comp);
		if (innerBorder>0) {
			scrollPanel.setBorder(
					BorderFactory.createCompoundBorder(
							BorderFactory.createEmptyBorder(innerBorder, innerBorder, innerBorder, innerBorder),
							BorderFactory.createEtchedBorder()
					)
			);
		}
		scrollPanel.getViewport().setPreferredSize(new Dimension(width, height));
		return scrollPanel;
	}

	public static JPanel createGridPanel(int rows, int cols, int hgap, int vgap, JComponent... components) {
		JPanel panel = new JPanel( new GridLayout(rows,cols,hgap,vgap) );
		if (components!=null)
			for (int i=0; i<components.length; i++) panel.add(components[i]);
		return panel;
	}

	public static JTextField createOutputTextField(int columns) {
        JTextField createOutputTextField = createOutputTextField("");
        createOutputTextField.setColumns(columns);
		return createOutputTextField;
    }

	public static JTextField createOutputTextField() {
        return createOutputTextField("");
    }

	public static JTextField createOutputTextField(String initialValue) {
        JTextField textfield = new JTextField();
        textfield.setText(initialValue);
        textfield.setEditable(false);
        return textfield;
    }
    
    private static class TextFieldFocusActionListener implements FocusListener, ActionListener {
    	private JTextField textfield;
    	private String commandStr;
    	private ActionListener actionListener;
		private boolean fireActionPerformed;
    	
	    public TextFieldFocusActionListener(JTextField textfield, String commandStr, ActionListener actionListener) {
	    	this.textfield = textfield;
			this.commandStr = commandStr;
			this.actionListener = actionListener;
			fireActionPerformed = false;
		}
		@Override public synchronized void focusGained(FocusEvent e) {
			fireActionPerformed = true;
		}
		@Override public synchronized void focusLost(FocusEvent e) {
			if (fireActionPerformed) {
				fireActionPerformed = false;
				if (textfield.isEditable())
					actionListener.actionPerformed( new ActionEvent( textfield,ActionEvent.ACTION_PERFORMED,commandStr ) );
			}
		}
		@Override public synchronized void actionPerformed(ActionEvent e) {
			if (fireActionPerformed) {
				fireActionPerformed = false;
				actionListener.actionPerformed(e);
			}
		}

	}

    public static JTextField createTextField( String commandStr, ActionListener actionListener ) {
        JTextField textfield = new JTextField();
        textfield.addActionListener( actionListener );
        textfield.setActionCommand( commandStr );
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value ) {
        JTextField textfield = createTextField( commandStr, actionListener );
        textfield.setText(value);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, int columns ) {
        JTextField textfield = createTextField( commandStr, actionListener, value );
        textfield.setColumns(columns);
        return textfield;
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, boolean observeFocusEvents ) {
    	if (observeFocusEvents) return createTextField(commandStr, actionListener, true, null);
    	else                    return createTextField(commandStr, actionListener);
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean observeFocusEvents ) {
    	if (observeFocusEvents) return createTextField(commandStr, actionListener, value, true, null);
    	else                    return createTextField(commandStr, actionListener, value);
    }

    public static <AC> JTextField createTextField( AC actionCommand, Disabler<AC> disabler, ActionListener actionListener, String value, boolean observeFocusEvents ) {
    	if (observeFocusEvents) return addToDisabler(disabler, actionCommand, createTextField(actionCommand.toString(), actionListener, value, true, null) );
    	else                    return addToDisabler(disabler, actionCommand, createTextField(actionCommand.toString(), actionListener, value) );
    }

    public static JTextField createTextField( String commandStr, ActionListener actionListener,               boolean editable,              FocusListener focusListener ) {
        JTextField textfield = new JTextField();
        if (focusListener==null) {
            TextFieldFocusActionListener listener = new TextFieldFocusActionListener(textfield,commandStr,actionListener);
        	focusListener  = listener;
        	actionListener = listener;
        }
        textfield.addFocusListener ( focusListener  );
        textfield.addActionListener( actionListener );
        textfield.setActionCommand( commandStr );
        textfield.setEditable(editable);
        return textfield;
    }
    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable,              FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        return textfield;
    }
    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable, boolean enabled, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        textfield.setEnabled(enabled);
        return textfield;
    }
    public static JTextField createTextField( String commandStr, ActionListener actionListener,               boolean editable, int columns, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setColumns( columns );
        return textfield;
    }
    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable, int columns, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        textfield.setColumns( columns );
        return textfield;
    }
    public static JTextField createTextField( String commandStr, ActionListener actionListener, String value, boolean editable, boolean enabled, int columns, FocusListener focusListener ) {
        JTextField textfield = createTextField( commandStr, actionListener, editable, focusListener );
        textfield.setText(value);
        textfield.setColumns( columns );
        textfield.setEnabled(enabled);
        return textfield;
    }

    public static JTextField createTextField_Int(int initValue, int columns, Predicate<Integer> isOK, Consumer<Integer> action) {
		Function<String, Integer> parse = str->{ try { return Integer.parseInt(str); } catch (NumberFormatException e) { return null; } };
		return createTextField(Integer.toString(initValue), columns, true, parse, v->v!=null&&isOK.test(v), action);
    }
    public static JTextField createTextField_Double(double initValue, int columns, Predicate<Double> isOK, Consumer<Double> action) {
		Function<String, Double> parse = str->{ try { return Double.parseDouble(str); } catch (NumberFormatException e) { return null; } };
		return createTextField(Double.toString(initValue), columns, true, parse, v->v!=null&&isOK.test(v), action);
    }
    public static <V> JTextField createTextField(String text, int columns, boolean waitOnFinalInput, Function<String,V> parse, Predicate<V> isOK, Consumer<V> action) {
    	return setTextField(new JTextField(text,columns), waitOnFinalInput, parse, isOK, action);
    }
    public static JTextField createTextField(String text, int columns, boolean waitOnFinalInput, Consumer<String> action) {
    	return setTextField(new JTextField(text,columns), waitOnFinalInput, action);
    }
	public static JTextField createTextField(String text, int columns, Consumer<String> onChange, Consumer<String> onInput) {
		return setTextField(new JTextField(text,columns), onChange, onInput);
	}
	public static <V> JTextField setTextField(JTextField comp, boolean waitOnFinalInput, Function<String, V> parse, Predicate<V> isOK, Consumer<V> action) {
		Color defaultBG = comp.getBackground();
		return setTextField(comp, waitOnFinalInput, (Consumer<String>) str->{
			V value = parse.apply(str);
		    if (isOK.test(value)) { action.accept(value); comp.setBackground(defaultBG); }
		    else { comp.setBackground(Color.RED); }
		});
	}

	public static JTextField setTextField(JTextField comp, boolean waitOnFinalInput, Consumer<String> action) {
		return setTextField(comp, action, waitOnFinalInput ? null : action);
	}
	public static JTextField setTextField(JTextField comp, Consumer<String> onChange, Consumer<String> onInput) {
		if (onChange!=null) {
			comp.addActionListener(e->onChange.accept(comp.getText()));
			comp.addFocusListener(new FocusListener() {
				@Override public void focusLost(FocusEvent e) { onChange.accept(comp.getText()); }
				@Override public void focusGained(FocusEvent e) {}
			});
		}
		if (onInput!=null)
			comp.addCaretListener(e->onInput.accept(comp.getText()));
		return comp;
	}

//	public static class JTextField_HS extends JTextField {
//		private static final long serialVersionUID = -1107252015179183026L;
//		
//		private String commandStr;
//
//        public JTextField_HS() {
//            super();
//        }
//
//        public JTextField_HS( String str ) {
//            super(str);
//        }
//
//        @Override
//        public void setActionCommand(String commandStr) {
//            super.setActionCommand(commandStr);
//            this.commandStr = commandStr;
//        }
//
//        public String getActionCommand() {
//            return commandStr;
//        }
//    }

//    public static class JTextField_HS_FocusListener implements FocusListener {
//
//        private ActionListener actionListener;
//        private FocusActionFlag focusActionFlag;
//
//        public JTextField_HS_FocusListener( ActionListener actionListener ) {
//            this( actionListener, null );
//        }
//
//        public JTextField_HS_FocusListener( ActionListener actionListener, FocusActionFlag focusActionFlag ) {
//            this.actionListener = actionListener;
//            this.focusActionFlag = focusActionFlag;
//        }
//
//        @Override public void focusGained(FocusEvent e) {}
//        @Override public void focusLost(FocusEvent e) {
//            if ( (focusActionFlag!=null) && !focusActionFlag.isFocusActionAllowedNow()) return;
//            if ( e.getComponent() instanceof JTextField_HS ) {
//                JTextField_HS txtf = (JTextField_HS)e.getComponent();
//                actionListener.actionPerformed( new ActionEvent( txtf, ActionEvent.ACTION_PERFORMED, txtf.getActionCommand() ) );
//            }
//        }
//
//        public static interface FocusActionFlag {
//            public boolean isFocusActionAllowedNow();
//        }
//    }
    
    public static enum VerticalAlignment { Top,Center,Bottom }
    public static enum HorizontalAlignment { Left,Center,Right }
	public static final int ALIGNMENT_TOP    = -1;
	public static final int ALIGNMENT_CENTER =  0;
	public static final int ALIGNMENT_BOTTOM =  1;
	public static final int ALIGNMENT_LEFT   = -1;
	public static final int ALIGNMENT_RIGHT  =  1;
	
	public static void drawString(Graphics g, String str, int x, int y, int hAlign, int vAlign) {
		HorizontalAlignment enumHAlign = null; 
		VerticalAlignment   enumVAlign = null; 
		switch (hAlign) {
		case ALIGNMENT_LEFT  : enumHAlign = HorizontalAlignment.Left; break;
		case ALIGNMENT_CENTER: enumHAlign = HorizontalAlignment.Center; break;
		case ALIGNMENT_RIGHT : enumHAlign = HorizontalAlignment.Right; break;
		}
		switch (vAlign) {
		case ALIGNMENT_TOP   : enumVAlign = VerticalAlignment.Top; break;
		case ALIGNMENT_CENTER: enumVAlign = VerticalAlignment.Center; break;
		case ALIGNMENT_BOTTOM: enumVAlign = VerticalAlignment.Bottom; break;
		}
		drawString(g,str,x,y,enumHAlign,enumVAlign);
	}
	public static void drawString(Graphics g, String str, int x, int y, HorizontalAlignment hAlign, VerticalAlignment vAlign) {
		Rectangle2D b = g.getFontMetrics().getStringBounds(str, g);
		switch (hAlign) {
		case Left  : x -= (int)Math.round( b.getMinX()               ); break;
		case Center: x -= (int)Math.round((b.getMinX()+b.getMaxX())/2); break;
		case Right : x -= (int)Math.round(             b.getMaxX()   ); break;
		}
		switch (vAlign) {
		case Top   : y -= (int)Math.round( b.getMinY()               ); break;
		case Center: y -= (int)Math.round((b.getMinY()+b.getMaxY())/2); break;
		case Bottom: y -= (int)Math.round(             b.getMaxY()   ); break;
		}
		g.drawString(str, x, y);
	}

	public static void makeAutoScroll(JScrollPane scrollPane) {
		makeAutoScroll(scrollPane,1000);
	}

	public static void makeAutoScroll(JScrollPane scrollPane, long delay) {
		new AutoScrollModel(delay).makeAutoScroll(scrollPane);
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output, boolean editable, int width, int height) {
		JScrollPane scrollPanel = createAutoScrollPanel(output,editable);
		scrollPanel.getViewport().setPreferredSize(new Dimension(width,height));
		return scrollPanel;
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output, boolean editable) {
		output.setEditable(editable);
		return createAutoScrollPanel(output);
	}
	
	public static JScrollPane createAutoScrollPanel(JTextArea output) {
		
		JScrollPane scrollPane = new JScrollPane( output );
		scrollPane.setBorder(BorderFactory.createLoweredBevelBorder());
		makeAutoScroll(scrollPane);
		
		return scrollPane; 
	}
	
	private static class AutoScrollModel extends DefaultBoundedRangeModel implements MouseListener, MouseWheelListener, ActionListener {

		private static final long serialVersionUID = 583924172439445131L;
		
		private boolean autoScroll = true;
		private JPopupMenu contextMenu;
		private JScrollBar verticalScrollBar;
		private final long delay;
		private Delayer delayer;
		
		public AutoScrollModel(long delay) {
			this.delay = delay;
		}
		
		public void makeAutoScroll(JScrollPane scrollPane) {
			JCheckBoxMenuItem item = new JCheckBoxMenuItem("autoscroll",autoScroll);
			item.addActionListener(this);
			item.setActionCommand("autoscroll");
			contextMenu = new JPopupMenu();
			contextMenu.add(item);
			
			delayer = new Delayer(new Runnable() {
				@Override public void run() {
					setAutoScroll(true);
				}
			});
			
			this.verticalScrollBar = scrollPane.getVerticalScrollBar();
			verticalScrollBar.setModel(this);
			verticalScrollBar.addMouseListener(this);
			verticalScrollBar.addMouseWheelListener(this);
			scrollPane.addMouseListener(this);
			scrollPane.addMouseWheelListener(this);
			
		}

		@Override
		public void actionPerformed(ActionEvent e) {
			if ("autoscroll".equals(e.getActionCommand())) {
				JCheckBoxMenuItem source = (JCheckBoxMenuItem)e.getSource();
				setAutoScroll(source.isSelected());
			}
		}

		public void setAutoScroll(boolean autoScroll) {
			this.autoScroll = autoScroll;
			if (this.autoScroll) checkSetting();
		}

		@Override
		public void setRangeProperties( int newValue, int newExtent, int newMin, int newMax, boolean adjusting ) {
			super.setRangeProperties( newValue, newExtent, newMin, newMax, adjusting );
//			System.out.println( "min:"+newMin + " val:"+newValue + " val+ext:"+(newValue+newExtent) + " max:"+newMax + " adj:"+adjusting );
			
			if (autoScroll && !adjusting) checkSetting();
		}

		private void checkSetting() {
			int val = this.getValue();
			int ext = this.getExtent();
			int max = this.getMaximum();
			if (val != max - ext)
				super.setValue(max - ext);
		}

		@Override public void mouseClicked(MouseEvent e) {}
		@Override public void mouseEntered(MouseEvent e) {}
		@Override public void mouseExited(MouseEvent e) {}
		@Override public void mouseReleased(MouseEvent e) {
			if ((e.getSource()==verticalScrollBar) && e.isPopupTrigger()) {
				contextMenu.show(verticalScrollBar, e.getX(),e.getY());
			}
		}

		@Override public void mousePressed(MouseEvent e) {
			setAutoScroll(false); 
			delayer.delayTask(delay);
		}
		@Override public void mouseWheelMoved(MouseWheelEvent e) {
			setAutoScroll(false); 
			delayer.delayTask(delay);
		}

//		@Override
//		public void setValue(int val) {
//			super.setValue(val);
//			System.out.println( "val:"+val );
//		}
		
	}

	public static Component addEmptyBorder(int width, JComponent comp) {
		comp.setBorder(BorderFactory.createEmptyBorder(width, width, width, width));
		return comp;
	}

	public static JFrame createWindowOnScreen(int screenID) {
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] screenDevices = ge.getScreenDevices();
		GraphicsConfiguration gc;
		if (screenDevices.length>screenID) {
			gc = screenDevices[screenID].getDefaultConfiguration();
			System.out.println("Create window on screen "+screenID+".");
		} else {
			gc = ge.getDefaultScreenDevice().getDefaultConfiguration();
			System.out.println("Create window on defaul screen.");
		}
		return new JFrame(gc);
	}

	public static class Delayer implements Runnable {
		
		private Runnable task;
		private long taskTime;
		private boolean running;

		public Delayer(Runnable task) {
			this.task = task;
			taskTime = 0;
			running = false;
		}
		
		public void delayTask( long ms ) {
			//System.out.println("Delayer (re)set to "+ms+" miliseconds");
			taskTime = System.currentTimeMillis()+ms;
			if (!running) new Thread(this).start();
		}
		
		@Override
		public void run() {
			//System.out.println("Delayer started");
			running = true;
			synchronized (this) {
				while (true) {
					long currentTime = System.currentTimeMillis();
					if (currentTime>=taskTime) { 
						task.run();
						break;
					} else {
						try { wait(taskTime - currentTime); } catch (InterruptedException e) {}
					}
				}
			}
			running = false;
			//System.out.println("Delayer ended");
		}

	}
	
	public static class Disabler<ActionCommands> {
		
		private HashMap<ActionCommands, Vector<JComponent>> map;

		public Disabler() {
			map = new HashMap<ActionCommands,Vector<JComponent>>();
		}
		
		public void setCareFor(ActionCommands actionCommand) {
			map.put(actionCommand,new Vector<JComponent>());
		}

		public void setCareFor(ActionCommands[] values) {
			for (ActionCommands ac:values) setCareFor(ac);
		}

		public boolean caresFor(ActionCommands actionCommand) {
			return map.containsKey(actionCommand);
		}

		public void showMap() {
			System.out.println("Disabler.map:");
			for (ActionCommands key:map.keySet()) {
				System.out.printf("   key[%s]\r\n",key);
				Vector<JComponent> list = map.get(key);
				if (list==null)
					System.out.printf("      <no entries>\r\n");
				else
					for (JComponent comp:list)
						System.out.printf("      %s\r\n",comp);
			}
		}

		public void addAll(ActionCommands actionCommand, JComponent... comps) {
			for (JComponent comp:comps)
				add(actionCommand,comp);
		}

		public JComponent add(ActionCommands actionCommand, JComponent comp) {
			Vector<JComponent> list = map.get(actionCommand);
			if (list==null) throw new UnsupportedOperationException("Disabler: Can't add components for unregistered ActionCommand "+actionCommand+". Please register it with disabler.setCareFor.");
			list.add(comp);
			return comp;
		}

		public void setEnableAll(boolean enabled) {
			for (ActionCommands key:map.keySet())
				setEnable(key, enabled);
		}

		public void setEnableAll( boolean enabled, ActionCommands exceptThis ) {
			for (ActionCommands key:map.keySet())
				if (!exceptThis.equals(key))
					setEnable(key, enabled);
		}

		private boolean contains( ActionCommands[] values, ActionCommands value ) {
			for(ActionCommands ac:values) if (ac.equals(value)) return true;
			return false;
		}
		
		public void setEnableAll( boolean enabled, ActionCommands[] exceptThis ) {
			for (ActionCommands key:map.keySet()) 
				if (!contains(exceptThis,key))
					setEnable(key, enabled);
		}

		public void setEnableAll( boolean enabled, Vector<ActionCommands> exceptThis ) {
			for (ActionCommands key:map.keySet())
				if (!exceptThis.contains(key))
					setEnable(key, enabled);
		}

		public void setEnable(Function<ActionCommands,Boolean> shouldEnable) {
			for (ActionCommands key:map.keySet()) {
				Boolean enabled = shouldEnable.apply(key);
				if (enabled!=null) setEnable(key, enabled);
			}
		}

		public void setEnable(ActionCommands[] actionCommands, boolean enabled) {
			for (ActionCommands ac:actionCommands)
				setEnable(ac, enabled);
		}

		public void setEnable(ActionCommands actionCommand, boolean enabled) {
			Vector<JComponent> list = map.get(actionCommand);
			if (list==null) throw new UnsupportedOperationException("Disabler: Can't use method setEnable on ActionCommand "+actionCommand+".");
			for (JComponent c:list)
				c.setEnabled(enabled);
		}

		public void configureAbstractButton(ActionCommands actionCommand, Consumer<AbstractButton> configure) {
			Vector<JComponent> list = map.get(actionCommand);
			if (list==null) throw new UnsupportedOperationException("Disabler: Can't use method configureAbstractButton on ActionCommand "+actionCommand+".");
			for (JComponent c:list)
				if (c instanceof AbstractButton)
					configure.accept((AbstractButton) c);
		}

		public Set<ActionCommands> keySet() {
			return map.keySet();
		}
		
		public Vector<JComponent> get(ActionCommands key) {
			return map.get(key);
		}
	}
}
