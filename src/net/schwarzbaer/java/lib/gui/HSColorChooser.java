package net.schwarzbaer.java.lib.gui;


import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.util.HashMap;
import java.util.Locale;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.SwingConstants;

import net.schwarzbaer.java.lib.gui.ColorCompSlider.ColorComp;
import net.schwarzbaer.java.lib.gui.ColorSlider.ColorChangeListener;
import net.schwarzbaer.java.lib.gui.ColorSlider.SliderType;

public final class HSColorChooser {
	
	public static final Position PARENT_CENTER   = Position.PARENT_CENTER;
	public static final Position LEFT_OF_PARENT  = Position.LEFT_OF_PARENT;
	public static final Position ABOVE_PARENT    = Position.ABOVE_PARENT;
	public static final Position RIGHT_OF_PARENT = Position.RIGHT_OF_PARENT;
	public static final Position BELOW_PARENT    = Position.BELOW_PARENT;
	
	public static enum Position {
		PARENT_CENTER,
		LEFT_OF_PARENT,
		ABOVE_PARENT,
		RIGHT_OF_PARENT,
		BELOW_PARENT
	}
	
	public static JButton createColorbutton(Color initialColor, Window dialogParent, String dialogTitle, Position dialogPosition, ColorReceiver colorReceiver) {
		ColorButton button = new ColorButton(initialColor);
		button.addActionListener(e->{
			Color color = showDialog(dialogParent, dialogTitle, button.getColor(), dialogPosition);
			if (color!=null) {
				button.setColor(color);
				colorReceiver.colorChanged(color);
			}
		});
		return button;
	}
	
	public static class ColorDialog extends JDialog {
		private static final long serialVersionUID = 8813919228546598198L;
		
		private final MainPanel mainPanel;
		private final Window parent;
		private final boolean repeatedUseOfDialogObject;

		public ColorDialog(Window parent, String title, boolean repeatedUseOfDialogObject, Color color) {
			this(parent, title, repeatedUseOfDialogObject, color, null);
		}
		public ColorDialog(Window parent, String title, boolean repeatedUseOfDialogObject, Color color, UserdefinedColors userdefinedColors) {
			super(parent, title);
			this.parent = parent;
			this.repeatedUseOfDialogObject = repeatedUseOfDialogObject;
			mainPanel = new MainPanel(color, this, null, userdefinedColors);
			setContentPane( mainPanel );
			pack();
			setPosition(Position.PARENT_CENTER);
			setSizeAsMinSize();
		}
		
		public void setInitialColor(Color color) {
			mainPanel.setInitialColor(color);
		}
		
		public Color getColor() {
			return mainPanel.dlgResultColor;
		}
	    
		private void setPosition(Position position) {
			Rectangle p;
			if (parent != null) p = parent.getBounds();
			else                p = getGraphicsConfiguration().getBounds();
	        Dimension d = getSize();
	        int dist = 3;
	        switch (position) {
	        case LEFT_OF_PARENT:
	            if (p.height>d.height) this.setSize(d.width, p.height);
	            setLocation( p.x-d.width-dist, p.y );
	            break;
	        case RIGHT_OF_PARENT:
	            if (p.height>d.height) this.setSize(d.width, p.height);
	            setLocation( p.x+p.width+dist, p.y );
	            break;
	        case ABOVE_PARENT:
	            if (p.width>d.width) this.setSize(p.width, d.height);
	            setLocation( p.x, p.y-d.height-dist );
	            break;
	        case BELOW_PARENT:
	            if (p.width>d.width) this.setSize(p.width, d.height);
	            setLocation( p.x, p.y+p.height+dist );
	            break;
	        case PARENT_CENTER:
	        default:
	            setLocation(
	                    (p.width -d.width )/2+p.x,
	                    (p.height-d.height)/2+p.y
	                );
	        }
		}
		
	    private void setSizeAsMinSize() {
	        Dimension d = getSize();
	        setMinimumSize(d);
	    }
	    
	    public void showDialog(Position position) {
	    	if (position!=null) setPosition(position);
	        setVisible( true );
	    }

	    public void showDialog() {
	    	showDialog(null);
	    }

	    public void closeDialog() {
	        setVisible( false );
	        if (!repeatedUseOfDialogObject) dispose();
	    }
	}
	
	public static Color showDialog(Window parent, String title, Color color, Position position) {
		return showDialog(parent, title, color, null, position);
	}
	
	public static Color showDialog(Window parent, String title, Color color, UserdefinedColors userdefinedColors, Position position) {
		ColorDialog dlgFenster = new ColorDialog(parent, title, false, color, userdefinedColors);
		dlgFenster.showDialog(position);
		return dlgFenster.getColor();
	}
	
	public static MainPanel createPanel(Color color, ColorReceiver colorReceiver) {
		return new MainPanel(color, null, colorReceiver, null);
	}
	
	public static MainPanel createPanel(Color color, ColorReceiver colorReceiver, UserdefinedColors userdefinedColors) {
		return new MainPanel(color, null, colorReceiver, userdefinedColors);
	}
	
	public interface ColorReceiver {
		public void colorChanged(Color color);
	}

	public static class UserdefinedColors {
		public final Color[] colors;
		public UserdefinedColors() {
			colors = new Color[] { null,null,null,null,null,null,null,null };
		}
	}

	private static enum ActionCommands {
		other,
		SetDual2RG, SetDual2GB, SetDual2RB,
		SetDual2HS, SetDual2SB, SetDual2HB,
		Ok, Cancel, ResetColor, SetUserColor, ReadUserColor,
	}

	public static class MainPanel extends JPanel implements ActionListener, ColorChangeListener {
		private static final long serialVersionUID = 1065328530214691959L;
		
		private final Disabler disabler;
		private final JLabel oldColorField;
		private final JLabel colForegrField;
		private final JLabel colBackgrField;
		private final JLabel colForegrFieldW;
		private final JLabel colBackgrFieldW;

		private final ColorCompPanel colorCompR;
		private final ColorCompPanel colorCompG;
		private final ColorCompPanel colorCompB;
		private final ColorCompPanel colorCompH;
		private final ColorCompPanel colorCompS;
		private final ColorCompPanel colorCompV;
		
		private final ColorCompSlider sliderDual;
		private Color oldColor;
		private Color currentColor;
		private Color dlgResultColor;
		
		private final UserdefinedColors userdefinedColors; 
		private final ColorToggleButton[] userColorButtons; 
		
		private final ColorReceiver colorReceiver;
		private final ColorDialog dialog;
		
		private MainPanel(Color color, ColorDialog dlgFenster, ColorReceiver colorReceiver, UserdefinedColors userdefinedColors) {
			super(new BorderLayout(3,3));
			this.dialog = dlgFenster;
			this.colorReceiver = colorReceiver;
			this.userdefinedColors = userdefinedColors != null ? userdefinedColors : new UserdefinedColors();
			
			disabler = new Disabler();
			disabler.setCareFor(ActionCommands.values());
			
			JPanel buttonPanel = new JPanel(new GridLayout(1,0,3,3));
			if (dialog!=null) {
				buttonPanel.add(createButton("OK", ActionCommands.Ok));
				buttonPanel.add(createButton("Cancel", ActionCommands.Cancel));
			}
			buttonPanel.add(createButton("Reset", ActionCommands.ResetColor));
			
			JPanel rgbPanel = new JPanel(new GridLayout(1,0,3,3));
			rgbPanel.setBorder(BorderFactory.createTitledBorder(""));
			rgbPanel.add(colorCompR = new ColorCompPanel( "R",ColorComp.COMP_RED ));
			rgbPanel.add(colorCompG = new ColorCompPanel( "G",ColorComp.COMP_GRN ));
			rgbPanel.add(colorCompB = new ColorCompPanel( "B",ColorComp.COMP_BLU ));
			
			JPanel hsvPanel = new JPanel(new GridLayout(1,0,3,3));
			hsvPanel.setBorder(BorderFactory.createTitledBorder(""));
			hsvPanel.add(colorCompH = new ColorCompPanel( "H",ColorComp.COMP_HUE ));
			hsvPanel.add(colorCompS = new ColorCompPanel( "S",ColorComp.COMP_SAT ));
			hsvPanel.add(colorCompV = new ColorCompPanel( "V",ColorComp.COMP_BRT ));
			
			JPanel singleSliderPanel = new JPanel(new GridLayout(1,0,3,3));
			singleSliderPanel.add(rgbPanel);
			singleSliderPanel.add(hsvPanel);
			
			ButtonGroup buttonGroup_panelType = new ButtonGroup();
			JPanel dualTypePanel = new JPanel(new GridLayout(1,0,3,3));
			dualTypePanel.add(createRadioButton("RG", ActionCommands.SetDual2RG, buttonGroup_panelType, true ));
			dualTypePanel.add(createRadioButton("GB", ActionCommands.SetDual2GB, buttonGroup_panelType, false));
			dualTypePanel.add(createRadioButton("RB", ActionCommands.SetDual2RB, buttonGroup_panelType, false));
			dualTypePanel.add(createRadioButton("HS", ActionCommands.SetDual2HS, buttonGroup_panelType, false));
			dualTypePanel.add(createRadioButton("SB", ActionCommands.SetDual2SB, buttonGroup_panelType, false));
			dualTypePanel.add(createRadioButton("HB", ActionCommands.SetDual2HB, buttonGroup_panelType, false));
			
			JPanel dualSliderPanel = new JPanel(new BorderLayout(3,3));
			dualSliderPanel.setBorder(BorderFactory.createTitledBorder(""));
			dualSliderPanel.add(sliderDual = new ColorCompSlider(SliderType.DUAL,Color.YELLOW,ColorComp.COMP_RED,ColorComp.COMP_GRN,this), BorderLayout.CENTER);
			dualSliderPanel.add(dualTypePanel, BorderLayout.SOUTH);
			sliderDual.setMinimumSize(new Dimension(100,100));
			disabler.add(ActionCommands.other, sliderDual);
			
			userColorButtons = new ColorToggleButton[this.userdefinedColors.colors.length];
			ButtonGroup buttonGroup_colorList = new ButtonGroup();
			JPanel userColorListPanel = new JPanel(new GridLayout(1,0,3,3));
			//userColorListPanel.setBorder(BorderFactory.createLoweredBevelBorder());
			Dimension prefSize = new Dimension(20,20);
			for (int i=0; i<this.userdefinedColors.colors.length; i++) {
				userColorListPanel.add( userColorButtons[i] = createColorToggleButton(this.userdefinedColors.colors[i], buttonGroup_colorList, prefSize) );
			}
			
			JPanel userColorSetButtonPanel = new JPanel(new GridLayout(1,0,3,3));
			userColorSetButtonPanel.add(createButton("set" , ActionCommands.SetUserColor ));
			userColorSetButtonPanel.add(createButton("read", ActionCommands.ReadUserColor));
			
			JPanel userColorPanel = new JPanel( new BorderLayout(3,3) );
			userColorPanel.add(userColorListPanel, BorderLayout.CENTER);
			userColorPanel.add(userColorSetButtonPanel, BorderLayout.WEST);
			userColorPanel.setBorder(BorderFactory.createTitledBorder(""));
			
			oldColorField = new JLabel("   ");
			oldColorField.setBorder(BorderFactory.createTitledBorder(""));
			oldColorField.setOpaque(true);
			oldColorField.setPreferredSize(new Dimension(30,15));
			
			JPanel examplePanel = new JPanel(new GridLayout(1,0,3,3));
			examplePanel.setBorder(BorderFactory.createTitledBorder(""));
			examplePanel.add(colForegrField  = new JLabel("Text",SwingConstants.CENTER)); colForegrField .setOpaque(false);
			examplePanel.add(colForegrFieldW = new JLabel("Text",SwingConstants.CENTER)); colForegrFieldW.setOpaque(true );
			examplePanel.add(colBackgrField  = new JLabel("Text",SwingConstants.CENTER)); colBackgrField .setOpaque(true );
			examplePanel.add(colBackgrFieldW = new JLabel("Text",SwingConstants.CENTER)); colBackgrFieldW.setOpaque(true );
			disabler.add(ActionCommands.other, oldColorField  );
			disabler.add(ActionCommands.other, colForegrField );
			disabler.add(ActionCommands.other, colForegrFieldW);
			disabler.add(ActionCommands.other, colBackgrField );
			disabler.add(ActionCommands.other, colBackgrFieldW);
			
			JPanel lowerButtonPanel = new JPanel( new BorderLayout(3,3) );
			lowerButtonPanel.add(oldColorField, BorderLayout.WEST);
			lowerButtonPanel.add(examplePanel, BorderLayout.CENTER);
			lowerButtonPanel.add(buttonPanel, BorderLayout.EAST);
			
			JPanel lowerPanel = new JPanel(new GridLayout(0,1,3,3));
			lowerPanel.add(userColorPanel);
			lowerPanel.add(lowerButtonPanel);
			
			setBorder(BorderFactory.createEmptyBorder(3,3,3,3));
			add(dualSliderPanel,BorderLayout.CENTER);
			add(singleSliderPanel,BorderLayout.EAST);
			add(lowerPanel,BorderLayout.SOUTH);
			
			setInitialColor(color);
		}
		
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			disabler.setEnableAll(enabled);
			setExamples();
		}

		public void setInitialColor(Color color) {
			setColor(color);
			this.oldColor = color;
			this.dlgResultColor = null;
			oldColorField.setBackground(color);
		}
		
		private void setColor(Color color) {
			if (colorReceiver!=null) colorReceiver.colorChanged(color);
			this.currentColor = color;
			setExamples();
			int blue  = color.getBlue();
			int green = color.getGreen();
			int red   = color.getRed();
			float[] hsb = Color.RGBtoHSB(red, green, blue, null);
			colorCompR.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			colorCompG.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			colorCompB.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			colorCompH.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			colorCompS.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			colorCompV.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
			sliderDual.setValues(red,green,blue,hsb[0],hsb[1],hsb[2]);
		}

		private void setExamples() {
			oldColorField  .setBackground(isEnabled()?oldColor:null);
			colForegrField .setForeground(isEnabled()?currentColor:null);
			colForegrFieldW.setForeground(isEnabled()?currentColor:null);
			colForegrFieldW.setBackground(isEnabled()?Color.WHITE:null);
			colBackgrField .setBackground(isEnabled()?currentColor:null);
			colBackgrFieldW.setBackground(isEnabled()?currentColor:null);
			colBackgrFieldW.setForeground(isEnabled()?Color.WHITE:null);
		}
		
		private JButton createButton( String title, ActionCommands ac) {
	    	JButton comp = new JButton(title);
	    	disabler.add(ac, comp);
	    	comp.setActionCommand( ac.toString() );
			comp.addActionListener(this);
			return comp;
	    }
	    
	    private JRadioButton createRadioButton( String title, ActionCommands ac, ButtonGroup buttonGroup, boolean isSelected ) {
			JRadioButton comp = new JRadioButton( title, isSelected );
	    	disabler.add(ac, comp);
	    	comp.setActionCommand( ac.toString() );
			comp.addActionListener(this);
			if (buttonGroup!=null) buttonGroup.add(comp);
			return comp;
		}

		private ColorToggleButton createColorToggleButton(Color color, ButtonGroup buttonGroup, Dimension prefSize) {
			ColorToggleButton colorButton = new ColorToggleButton(color);
			colorButton.setPreferredSize(prefSize);
			buttonGroup.add(colorButton);
			disabler.add(ActionCommands.other, colorButton);
			return colorButton;
		}
		
		private class ColorCompPanel extends JPanel {
			private static final long serialVersionUID = 1600488532512853843L;

			private ColorComp colorComp;
			
			private ColorCompSlider slider;
			private JTextField value;
			private ColorCompSlider.MyColorSliderModel sliderModel;
			private Color defaultTextFieldBG;

			ColorCompPanel(String title, ColorComp colorComp) {
				super( new BorderLayout(3,3) );
				this.colorComp = colorComp;
				
				JLabel label = new JLabel(title,JLabel.CENTER);
				slider = new ColorCompSlider(SliderType.VERTICAL,Color.YELLOW,colorComp,MainPanel.this);
				sliderModel = (ColorCompSlider.MyColorSliderModel)slider.model;
				value = new JTextField("");
				value.setPreferredSize(new Dimension(40,20));
				value.addActionListener(e->valueChanged());
				value.addFocusListener(new FocusListener() {
					@Override public void focusGained(FocusEvent e) {}
					@Override public void focusLost(FocusEvent e) {valueChanged();}
				});
				defaultTextFieldBG = value.getBackground();
				
				add(label, BorderLayout.NORTH);
				add(slider, BorderLayout.CENTER);
				add(value, BorderLayout.SOUTH);
				disabler.addAll(ActionCommands.other, label, slider);
			}

			private void valueChanged() {
				String text = value.getText();
				try {
					float f = 0;
					switch (colorComp) {
					case COMP_RED:
					case COMP_GRN:
					case COMP_BLU: f = Math.max( 0,Math.min( Integer.parseInt(text),255 ))/255f; break;
					case COMP_HUE:
					case COMP_SAT:
					case COMP_BRT: f = Math.max( 0,Math.min( Float.parseFloat(text),1 )); break;
					}
					value.setBackground(defaultTextFieldBG);
					sliderModel.setValue(f);
					setColor(sliderModel.getColor());
					slider.repaint();
				} catch (NumberFormatException e) {
					value.setBackground(Color.RED);
				}
			}

			public void setValues(int red, int green, int blue, float h, float s, float b) {
				slider.setValues(red, green, blue, h, s, b);
				switch (colorComp) {
				case COMP_RED: value.setText(String.format(Locale.ENGLISH, "%d", red  )); break;
				case COMP_GRN: value.setText(String.format(Locale.ENGLISH, "%d", green)); break;
				case COMP_BLU: value.setText(String.format(Locale.ENGLISH, "%d", blue )); break;
				case COMP_HUE: value.setText(String.format(Locale.ENGLISH, "%1.3f", h));  break;
				case COMP_SAT: value.setText(String.format(Locale.ENGLISH, "%1.3f", s));  break;
				case COMP_BRT: value.setText(String.format(Locale.ENGLISH, "%1.3f", b));  break;
				}
				value.setBackground(defaultTextFieldBG);
			}
		}
	
		@Override
		public void actionPerformed(ActionEvent e) {
			ActionCommands actionCommand;
			try { actionCommand = ActionCommands.valueOf(e.getActionCommand()); }
			catch (Exception e1) { e1.printStackTrace(); return; }
			
			switch(actionCommand) {
			case Ok:
				if (dialog!=null) {
					dlgResultColor = currentColor;
					dialog.closeDialog();
				}
				break;
			case Cancel:
				if (dialog!=null) {
					dlgResultColor = null;
					dialog.closeDialog();
				}
				break;
			case ResetColor:
				setColor(oldColor);
				break;
			case SetUserColor: {
				int i = getSelectedColorButton();
				if (i>=0) {
					userdefinedColors.colors[i] = currentColor;
					userColorButtons[i].setColor(currentColor);
				}
			} break;
			case ReadUserColor: {
				int i = getSelectedColorButton();
				if ( (i>=0) && (userdefinedColors.colors[i]!=null) ) {
					setColor(userdefinedColors.colors[i]);
					userColorButtons[i].setColor(currentColor);
				}
			} break;
			case SetDual2RG: sliderDual.setColorComps(ColorComp.COMP_RED,ColorComp.COMP_GRN); sliderDual.repaint(); break;
			case SetDual2GB: sliderDual.setColorComps(ColorComp.COMP_GRN,ColorComp.COMP_BLU); sliderDual.repaint(); break;
			case SetDual2RB: sliderDual.setColorComps(ColorComp.COMP_RED,ColorComp.COMP_BLU); sliderDual.repaint(); break;
			case SetDual2HS: sliderDual.setColorComps(ColorComp.COMP_HUE,ColorComp.COMP_SAT); sliderDual.repaint(); break;
			case SetDual2SB: sliderDual.setColorComps(ColorComp.COMP_SAT,ColorComp.COMP_BRT); sliderDual.repaint(); break;
			case SetDual2HB: sliderDual.setColorComps(ColorComp.COMP_HUE,ColorComp.COMP_BRT); sliderDual.repaint(); break;
			
			case other:break;
			}
		}
	
		private int getSelectedColorButton() {
			for (int i=0; i<userColorButtons.length; i++) {
				if (userColorButtons[i].isSelected()) return i;
			}
			return -1;
		}
	
		@Override
		public void colorChanged(Color color, float f) {
			setColor(color);
		}
	
		@Override
		public void colorChanged(Color color, float fH, float fV) {
			setColor(color);
		}
	}
	
	private static class ColorButtonIcon implements Icon {
		private static final Color BORDER_COLOR = new Color(128,128,128);

		private int fakeWidth;
		private int fakeHeight;
		private int realWidth;
		private int realHeight;
		private Color color;
	
		public ColorButtonIcon(Color color) {
			this.color = color;
			this.realWidth = 10;
			this.realHeight = 10;
			this.fakeWidth = 10;
			this.fakeHeight = 10;
		}
	
		public void  setColor(Color color) { this.color = color; }
		public Color getColor()            { return color; }

		public void setMastersSize(int masterWidth, int masterHeight) {
			this.realWidth  = masterWidth -8;
			this.realHeight = masterHeight-8;
		}
	
		@Override public int getIconWidth () { return fakeWidth; }
		@Override public int getIconHeight() { return fakeHeight; }
	
		@Override
		public void paintIcon(Component c, Graphics g, int x, int y) {
			int realX = x+(fakeWidth -realWidth )/2;
			int realY = y+(fakeHeight-realHeight)/2;
			if (color!=null && c.isEnabled()) {
				g.setColor(color);
				g.fillRect(realX, realY, realWidth, realHeight);
			}
			g.setColor(c.isEnabled()?BORDER_COLOR:Color.GRAY);
			g.drawRect(realX, realY, realWidth-1, realHeight-1);
		}
		
	}

	private static class ColorButton extends JButton {
		private static final long serialVersionUID = -9038988600993272882L;
		
		private ColorButtonIcon icon;

		public ColorButton(Color color) {
			icon = new ColorButtonIcon(color);
			setIcon(icon);
		}
		
		public Color getColor() {
			return icon.getColor();
		}
		public void setColor(Color color) {
			icon.setColor(color);
			repaint();
		}

		@Override public void setBounds(int x, int y, int width, int height) { icon.setMastersSize(  width,   height); super.setBounds(x, y, width, height); }
		@Override public void setBounds(Rectangle r                        ) { icon.setMastersSize(r.width, r.height); super.setBounds(r                  ); }
		@Override public void setSize  (Dimension d                        ) { icon.setMastersSize(d.width, d.height); super.setSize  (d                  ); }
		@Override public void setSize  (              int width, int height) { icon.setMastersSize(  width,   height); super.setSize  (      width, height); }
	}

	private static class ColorToggleButton extends JToggleButton {
		private static final long serialVersionUID = 7759027446832437901L;
		
		private ColorButtonIcon icon;

		public ColorToggleButton(Color color) {
			icon = new ColorButtonIcon(color);
			setIcon(icon);
		}
		
		public void setColor(Color color) {
			icon.setColor(color);
			repaint();
		}

		@Override public void setBounds(int x, int y, int width, int height) { icon.setMastersSize(  width,   height); super.setBounds(x, y, width, height); }
		@Override public void setBounds(Rectangle r                        ) { icon.setMastersSize(r.width, r.height); super.setBounds(r                  ); }
		@Override public void setSize  (Dimension d                        ) { icon.setMastersSize(d.width, d.height); super.setSize  (d                  ); }
		@Override public void setSize  (              int width, int height) { icon.setMastersSize(  width,   height); super.setSize  (      width, height); }
	}
	
	private static class Disabler {
		
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

		public void setEnable(ActionCommands actionCommand, boolean enabled) {
			Vector<JComponent> list = map.get(actionCommand);
			if (list==null) throw new UnsupportedOperationException("Disabler: Can't use method setEnable on ActionCommand "+actionCommand+".");
			for (JComponent c:list)
				c.setEnabled(enabled);
		}
	}
}