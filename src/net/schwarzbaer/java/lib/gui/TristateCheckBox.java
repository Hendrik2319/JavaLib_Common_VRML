package net.schwarzbaer.java.lib.gui;

import java.awt.AWTEvent;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.ButtonModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.plaf.ActionMapUIResource;

/*
 * Source :
 *     https://www.javaspecialists.eu/archive/Issue145.html
 *     TristateCheckBox Revisited
 *     Posted: 2007-05-25
 *     Category: GUI
 *     Java Version: 1.5+
 *     Dr. Heinz M. Kabutz
 */

public class TristateCheckBox extends JCheckBox {
	private static final long serialVersionUID = 4523921498193751829L;

	private static class LookAndFeelData {
		private String name;
		private String className;
		public LookAndFeelData(String name, String className) {
			this.name = name;
			this.className = className;
		}
	}
	
	public static void main(String[] args) throws Exception {
//		try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); }
//		catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {}
		
		JFrame frame = new JFrame("TristateCheckBox Test");
		frame.setLayout(new GridLayout(0, 2, 15, 15));
		
		Vector<LookAndFeelData> lfs = new Vector<>();
		lfs.add(new LookAndFeelData("<System>",UIManager.getSystemLookAndFeelClassName()));
		lfs.add(new LookAndFeelData("<CrossPlatform>",UIManager.getCrossPlatformLookAndFeelClassName()));
		for (UIManager.LookAndFeelInfo lf : UIManager.getInstalledLookAndFeels()) {
			lfs.add(new LookAndFeelData(lf.getName(),lf.getClassName()));
		}
		
		for (LookAndFeelData data : lfs) {
			System.out.println("Look&Feel " + data.name);
			UIManager.setLookAndFeel(data.className);
			frame.add(makeExamplePanel(data.name));
		}
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	private static JPanel makeExamplePanel(String name) {
		final TristateCheckBox tristateBox = new TristateCheckBox("Tristate checkbox");
		tristateBox.addItemListener(new ItemListener() {
			@Override public void itemStateChanged(ItemEvent e) {
				switch (tristateBox.getState()) {
				case SELECTED:
					System.out.println("Selected");
					break;
				case DESELECTED:
					System.out.println("Not Selected");
					break;
				case UNDEFINED:
					System.out.println("Tristate Selected");
					break;
				}
			}
		});
		tristateBox.addActionListener(e -> System.out.println(e));
		final JCheckBox normalBox = new JCheckBox("Normal checkbox");
		normalBox.addActionListener(e -> System.out.println(e));

		final JCheckBox enabledBox = new JCheckBox("Enable", true);
		enabledBox.addItemListener(e -> {
			tristateBox.setEnabled(enabledBox.isSelected());
			normalBox.setEnabled(enabledBox.isSelected());
		});

		JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
		panel.setBorder(BorderFactory.createTitledBorder(name));
		panel.add(new JLabel(UIManager.getLookAndFeel().getName()));
		panel.add(tristateBox);
		panel.add(normalBox);
		panel.add(enabledBox);
		return panel;
	}


	// Listener on model changes to maintain correct focusability
	private final ChangeListener enableListener = e -> setFocusable(getModel().isEnabled());

	public TristateCheckBox(String text) {
		this(text, State.UNDEFINED);
	}

	public TristateCheckBox(String text, State initial) {
		super(text, null);

		// Set default single model
		setModel(new TristateButtonModel(initial));

		// override action behaviour
		super.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		
		ActionMap actions = new ActionMapUIResource();
		actions.put("pressed", new AbstractAction() {
			private static final long serialVersionUID = -2091611500677873154L;
			@Override public void actionPerformed(ActionEvent e) {
				TristateCheckBox.this.iterateState();
			}
		});
		actions.put("released", null);
		SwingUtilities.replaceUIActionMap(this, actions);
	}

	// Next two methods implement new API by delegation to model
	public void setUndefined() {
		getTristateModel().setUndefined();
	}

	public boolean isUndefined() {
		return getTristateModel().isUndefined();
	}

	public State getState() {
		return getTristateModel().getState();
	}

	// Overrides superclass method
	@Override
	public void setModel(ButtonModel newModel) {
		super.setModel(newModel);

		// Listen for enable changes
		if (model instanceof TristateButtonModel)
			model.addChangeListener(enableListener);
	}

	// Empty override of superclass method
	@Override
	public void addMouseListener(MouseListener l) {
	}

	// Mostly delegates to model
	@SuppressWarnings("deprecation")
	private void iterateState() {
		// Maybe do nothing at all?
		if (!getModel().isEnabled())
			return;

		grabFocus();

		// Iterate state
		getTristateModel().iterateState();

		// Fire ActionEvent
		int modifiers = 0;
		AWTEvent currentEvent = EventQueue.getCurrentEvent();
		if (currentEvent instanceof InputEvent) {
			modifiers = ((InputEvent) currentEvent).getModifiers();
		} else if (currentEvent instanceof ActionEvent) {
			modifiers = ((ActionEvent) currentEvent).getModifiers();
		}
		fireActionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, getText(),
				System.currentTimeMillis(), modifiers));
	}

	// Convenience cast
	public TristateButtonModel getTristateModel() {
		return (TristateButtonModel) super.getModel();
	}

	public enum State {
		SELECTED   { @Override public State next() { return DESELECTED; } },
		DESELECTED { @Override public State next() { return UNDEFINED; } },
		UNDEFINED  { @Override public State next() { return SELECTED; } };
		public abstract State next();
	}

	public static class TristateButtonModel extends ToggleButtonModel {
		private static final long serialVersionUID = 7263102475538798840L;
		private State state = State.DESELECTED;
	
		public TristateButtonModel(State state) {
			setState(state);
		}
	
		public TristateButtonModel() {
			this(State.DESELECTED);
		}
	
		public void setUndefined() {
			setState(State.UNDEFINED);
		}
	
		public boolean isUndefined() {
			return state == State.UNDEFINED;
		}
	
		// Overrides of superclass methods
		@Override
		public void setEnabled(boolean enabled) {
			super.setEnabled(enabled);
			// Restore state display
			displayState();
		}
	
		@Override
		public void setSelected(boolean selected) {
			setState(selected ? State.SELECTED : State.DESELECTED);
		}
	
		// Empty overrides of superclass methods
		@Override
		public void setArmed(boolean b) {
		}
	
		@Override
		public void setPressed(boolean b) {
		}
	
		void iterateState() {
			setState(state.next());
		}
	
		private void setState(State state) {
			// Set internal state
			this.state = state;
			displayState();
			if (state == State.UNDEFINED && isEnabled()) {
				// force the events to fire
	
				// Send ChangeEvent
				fireStateChanged();
	
				// Send ItemEvent
				int indeterminate = 3;
				fireItemStateChanged(new ItemEvent(this, ItemEvent.ITEM_STATE_CHANGED, this, indeterminate));
			}
		}
	
		private void displayState() {
			super.setSelected(state == State.SELECTED);
			super.setArmed   (state == State.UNDEFINED);
			super.setPressed (state == State.UNDEFINED);
		}
	
		public State getState() {
			return state;
		}
	}
}
