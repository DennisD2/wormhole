package de.spurtikus.softtek;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;

import softtek.FileWriter;

import de.spurtikus.comm.serial.Communication;
import de.spurtikus.softtek.tek.DeviceDataListener;
import de.spurtikus.softtek.tek.DisplayGroup;
import de.spurtikus.softtek.tek.EventMessages;
import de.spurtikus.softtek.tek.MemoryImage;
import de.spurtikus.softtek.tek.Tek1241Device;

public class DevConfigView extends ViewPart {

	public static final String ID = "softtek.devConfigView";

	/** data for application is in singleton */
	private ApplicationSingleton softTekApp = ApplicationSingleton
			.getInstance();
	/** Tek object */
	private Tek1241Device tek;

	private Composite parent;

	private Color green, lightGreen, blue;
	private Color default_eclipse_grey;

	/** the job to execute asyncronically */
	// Job job;

	/** some controls are used from Jobs, so they need to be defined here */

	/** communication object */
	int deviceAddress = 2;

	Label[] slotNameLabel = null;
	Label[] slotCardType = null;
	Label[] slotCardThreshold = null;

	public DevConfigView() {
		super();
		// init local variables
		tek = softTekApp.getTekDevice();

		// init variable in global singleton
		softTekApp.setDevConfigView(this);
	}

	private Label createLabel(Composite _parent, String text) {
		Label label = new Label(_parent, SWT.NONE);
		label.setText(text);
		return label;
	}

	Button createButton(Composite parent, int style, String s, String tgroup,
			boolean selected, Listener listener) {

		return createButton(parent, style, s, tgroup, selected, listener,
				default_eclipse_grey);
	}

	Button createButton(Composite parent, int style, String s, String tgroup,
			boolean selected, Listener listener, Color color) {
		Button b = new Button(parent, style);
		b.setText(s);
		b.setData(tgroup);
		b.setSelection(selected);
		b.addListener(SWT.Selection, listener);
		b.setBackground(color);
		return b;
	}

	Listener listener_db = new Listener() {
		public void handleEvent(Event e) {
			Control[] children = parent.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				if (child instanceof Group) {
					Control[] gchilds = ((Group) child).getChildren();
					for (int j = 0; j < gchilds.length; j++) {
						Control child2 = gchilds[j];
						if (e.widget != child2 && child2 instanceof Button
								&& (child2.getStyle() & SWT.TOGGLE) != 1) {
							if (((Button) child2).getData() == "db")
								((Button) child2).setSelection(false);
						}
						if (e.widget == child2) {
							((Button) e.widget).setSelection(true);
							String s = ((Button) e.widget).getText();
							softTekApp.getNavigationView().setDataBits(
									Integer.parseInt(s));
						}
					}
				}
			}
		}
	};
	Listener listener_sb = new Listener() {
		public void handleEvent(Event e) {
			Control[] children = parent.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				if (child instanceof Group) {
					Control[] gchilds = ((Group) child).getChildren();
					for (int j = 0; j < gchilds.length; j++) {
						Control child2 = gchilds[j];
						if (e.widget != child2 && child2 instanceof Button
								&& (child2.getStyle() & SWT.TOGGLE) != 1) {
							if (((Button) child2).getData() == "sb")
								((Button) child2).setSelection(false);
						}
						if (e.widget == child2) {
							((Button) e.widget).setSelection(true);
							String s = ((Button) e.widget).getText();
							softTekApp.getNavigationView().setStopBits(
									Integer.parseInt(s));
						}
					}
				}
			}
		}
	};
	
	Listener listener_p = new Listener() {
		public void handleEvent(Event e) {
			Control[] children = parent.getChildren();
			for (int i = 0; i < children.length; i++) {
				Control child = children[i];
				if (child instanceof Group) {
					Control[] gchilds = ((Group) child).getChildren();
					for (int j = 0; j < gchilds.length; j++) {
						Control child2 = gchilds[j];
						if (e.widget != child2 && child2 instanceof Button
								&& (child2.getStyle() & SWT.TOGGLE) != 1) {
							if (((Button) child2).getData() == "p")
								((Button) child2).setSelection(false);
						}
						if (e.widget == child2) {
							((Button) e.widget).setSelection(true);
							String s = ((Button) e.widget).getText();
							if (s.equals("None")) {
								softTekApp.getNavigationView().setParity(
										SerialPort.PARITY_NONE);
							}
							if (s.equals("Even")) {
								softTekApp.getNavigationView().setParity(
										SerialPort.PARITY_EVEN);
							}
							if (s.equals("Odd")) {
								softTekApp.getNavigationView().setParity(
										SerialPort.PARITY_ODD);
							}
						}
					}
				}
			}
		}
	};

	/**
	 * create all controls needed for the view
	 * 
	 * @param _parent
	 *            parent of all control elements.
	 */
	public void createControls(Composite _parent) {

		Label empty;
		Label label;

		green = new Color(_parent.getDisplay(), 31, 133, 31);
		lightGreen = new Color(_parent.getDisplay(), 149, 233, 31);
		blue = new Color(_parent.getDisplay(), 70, 136, 255);
		default_eclipse_grey = new Color(parent.getDisplay(), 214, 210, 208);

		// Create a new Gridlayout with 4 columns
		GridLayout layout = new GridLayout(4, false);
		// set the layout of the shell
		_parent.setLayout(layout);

		green = new Color(_parent.getDisplay(), 31, 133, 31);
		lightGreen = new Color(_parent.getDisplay(), 149, 233, 31);
		blue = new Color(_parent.getDisplay(), 70, 136, 255);
		default_eclipse_grey = new Color(parent.getDisplay(), 214, 210, 208);

		// Create a new Gridlayout with 4 columns
		layout = new GridLayout(4, false);
		// set the layout of the shell
		_parent.setLayout(layout);

		/*
		 * port configuration controls
		 */
		label = new Label(_parent, SWT.BORDER);
		label.setText("Serial Port Config");
		label.setBackground(green);
		// Create new layout data
		GridData data = new GridData(GridData.FILL, GridData.BEGINNING, true,
				false, 4, 1);
		data.horizontalSpan = 4;
		label.setLayoutData(data);

		// port section
		Label label0 = new Label(_parent, SWT.NONE);
		label0.setText("Port:");
		Combo combo0 = new Combo(_parent, SWT.READ_ONLY);

		// fill combo using communication object
		HashSet<CommPortIdentifier> set = softTekApp.getCommunication()
				.getAvailableSerialPorts();
		Iterator<CommPortIdentifier> iter = set.iterator();
		String ports[] = new String[10]; // cannot believe more than 10 ports in
											// a PC
		int i = 0;
		while (iter.hasNext()) {
			CommPortIdentifier p = iter.next();
			ports[i++] = p.getName();
			combo0.add(p.getName());
		}
		String[] s = new String[i--];
		for (int j = 0; j <= i; j++) {
			s[j] = ports[j];
		}
		// sort items
		Arrays.sort(s);
		combo0.setItems(s);
		// init selected item
		combo0.select(1);
		combo0.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				System.out.println("Port: " + c.getText());
				softTekApp.getNavigationView().setPortName(c.getText());
			}
		});

		empty = new Label(_parent, SWT.NONE);
		empty.setText("");
		empty = new Label(_parent, SWT.NONE);
		empty.setText("");

		// baudrate section
		Label label1 = new Label(_parent, SWT.NONE);
		label1.setText("Baudrate:");
		Combo combo = new Combo(_parent, SWT.READ_ONLY);
		combo.setItems(new String[] { "1200", "2400", "4800", "9600", "19200",
				"38400", "57600", "76800", "57600", "115200", "250000",
				"500000", "1000000", "2000000", "4000000" });
		combo.select(5);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				System.out.println("Baudrate: " + c.getText());
				int b = Integer.valueOf(c.getText());
				softTekApp.getNavigationView().setBaudRate(b);
			}
		});
		empty = new Label(_parent, SWT.NONE);
		empty.setText("");
		empty = new Label(_parent, SWT.NONE);
		empty.setText("");

		// data bits section
		Group dbs = new Group(_parent, SWT.BORDER);
		dbs.setText("Data bits");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		dbs.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		dbs.setLayoutData(data);

		Button db6 = createButton(dbs, SWT.RADIO | SWT.TOGGLE, "6", "db",
				false, listener_db);
		Button db7 = createButton(dbs, SWT.RADIO | SWT.TOGGLE, "7", "db",
				false, listener_db);
		Button db8 = createButton(dbs, SWT.RADIO | SWT.TOGGLE, "8", "db", true,
				listener_db);

		// parity section
		Group pg = new Group(_parent, SWT.BORDER);
		pg.setText("Parity");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		pg.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		pg.setLayoutData(data);

		Button pn = createButton(pg, SWT.RADIO | SWT.TOGGLE, "None", "p", true,
				listener_p);
		Button pe = createButton(pg, SWT.RADIO | SWT.TOGGLE, "Even", "p",
				false, listener_p);
		Button po = createButton(pg, SWT.RADIO | SWT.TOGGLE, "Odd", "p", false,
				listener_p);

		// stop bit section
		Group sbg = new Group(_parent, SWT.BORDER);
		sbg.setText("Stop bits");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		sbg.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		sbg.setLayoutData(data);

		Button sb1 = createButton(sbg, SWT.RADIO | SWT.TOGGLE, "1", "sb", true,
				listener_sb);
		Button sb2 = createButton(sbg, SWT.RADIO | SWT.TOGGLE, "2", "sb",
				false, listener_sb);

		/*
		 * port configuration controls
		 */
		label = new Label(_parent, SWT.BORDER);
		label.setText("Device configuration");
		label.setBackground(green);
		// Create new layout data
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		label.setLayoutData(data);

		// GPIB address
		label = createLabel(_parent, "GPIB device address");
		combo = new Combo(_parent, SWT.READ_ONLY);
		combo.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7", "8",
				"9", "10", "11", "12", "13", "14", "15" });
		combo.select(1);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				System.out.println("Device address: " + c.getText());
				int b = Integer.valueOf(c.getText());
				deviceAddress = b;
				softTekApp.getNavigationView().setDeviceAddress(b);
			}
		});

		/*
		 * Acquisition config controls
		 */
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		label = new Label(_parent, SWT.BORDER);
		label.setText("Acquisition Config");
		label.setBackground(lightGreen);
		label.setLayoutData(data);

		// cards
		dbs = new Group(_parent, SWT.BORDER);
		dbs.setText("Acquisition slots / cards / pods");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		dbs.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		dbs.setLayoutData(data);

		slotNameLabel = new Label[8];
		slotCardType = new Label[8];
		slotCardThreshold = new Label[8];
		label = createLabel(dbs, "Slot");
		label = createLabel(dbs, "Card+chain state");
		label = createLabel(dbs, "Threshold");
		empty = createLabel(dbs, "");
		for (i = 0; i < /* tek.getTekRaw().MAX_MEMCARD_COUNT */8; i++) {
			slotNameLabel[i] = createLabel(dbs, "# " + i + "        ");
			slotCardType[i] = createLabel(dbs,
					"?                                        ");
			slotCardThreshold[i] = createLabel(dbs,
					"?                                   ");
			empty = createLabel(dbs, "");
		}

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(final Composite parent) {

		this.parent = parent;
		createControls(parent);

	}

	public void redrawControls() {
		String mode = "?", s = "?";
		int m, n;

		tek = softTekApp.getTekDevice();
		if (tek != null) {

			// cards
			for (int i = 0; i < tek.getTekRaw().MAX_MEMCARD_COUNT; i++) {
				int val = tek.getTekRaw().getMemStat(i);
				boolean enabled = true;
				if (val == tek.getTekRaw().C_MEM_CARD_MISSING) {
					s = "-";
					enabled = false;
				}
				if (val == tek.getTekRaw().C_MEM_CARD_9_UNCHAINED)
					s = "9 channels, chain head";
				if (val == tek.getTekRaw().C_MEM_CARD_18_UNCHAINED)
					s = "18 channels, chain head";
				if (val == tek.getTekRaw().C_MEM_CARD_CHAINED)
					s = "chain follower";
				slotCardType[i].setText(s);

				slotCardThreshold[i].setText(""
						+ tek.getTekRaw().getPrettyThreshold(i));
				slotCardType[i].setEnabled(enabled);
				slotNameLabel[i].setEnabled(enabled);
				slotCardThreshold[i].setEnabled(enabled);
			}
		}
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// viewer.getControl().setFocus();
	}

}