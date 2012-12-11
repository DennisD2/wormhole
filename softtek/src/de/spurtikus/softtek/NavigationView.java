package de.spurtikus.softtek;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.IOException;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.part.WorkbenchPart;

import softtek.FileWriter;

import de.spurtikus.comm.serial.Communication;
import de.spurtikus.softtek.decorators.TimeLineDecorator;
import de.spurtikus.softtek.tek.DeviceDataListener;
import de.spurtikus.softtek.tek.DisplayGroup;
import de.spurtikus.softtek.tek.EventMessages;
import de.spurtikus.softtek.tek.MemoryImage;
import de.spurtikus.softtek.tek.Tek1241Device;
import de.spurtikus.softtek.decorators.I2CDecorator;

public class NavigationView extends ViewPart implements DeviceDataListener {

	public static final String ID = "softtek.navigationView";

	/** data for application is in singleton */
	private ApplicationSingleton softTekApp = ApplicationSingleton
			.getInstance();
	/** Tek object */
	private Tek1241Device tek;
	
	/** memory images to be used */
	private MemoryImage newSetupImage = null;
	private MemoryImage newMemImage = null;

	/**
	 * Communication port configuration values
	 */
	String portName = "/dev/ttyS4";
	int baudRate = 38400;
	int dataBits = SerialPort.DATABITS_8;
	int parity = SerialPort.PARITY_NONE;
	int stopBits = SerialPort.STOPBITS_1;

	/** GPIB communication object */
	int deviceAddress = 2;

	private Composite parent;

	private Color green, lightGreen, blue;
	private Color default_eclipse_grey;

	/** the job to execute asyncronically */
	Job job;

	/** some controls are used from Jobs, so they need to be defined here */
	ProgressBar bar;
	Label totalBytesLabel;

	/** grid scale */
	int gridScale = 1;

	Label timeBase1PeriodLabel;
	Label timeBase1ModeLabel;
	Label timeBase2PeriodLabel;
	Label timeBase2ModeLabel;
	Label groupConfigLabel;

	Label[] groupNameLabel = null;
	Label[] groupTbLabel = null;
	Label[] groupLevelLabel = null;
	Label[] groupChannelLabel = null;
	Label[] slotNameLabel = null;
	Label[] slotCardType = null;
	Label[] slotCardThreshold = null;

	/**
	 * group display buttons
	 */
	Button b_group[] = new Button[12];
	
	/**
	 * decorators
	 */
	boolean viewDecorator=false;
	TimeLineDecorator i2cDecorator=null;

	public NavigationView() {
		super();
		// init local variables
		tek = softTekApp.getTekDevice();

		// init variable in global singleton
		softTekApp.setNavigationView(this);
	}

	/**
	 * Convenience button creation
	 * @param parent
	 * @param type
	 * @param text
	 * @param listener
	 * @return
	 */
	private Button createButton(Group parent, int type, String text,
			SelectionListener listener) {
		Button b = new Button(parent, type);
		b.setText(text);
		b.addSelectionListener(listener);
		return b;
	}

	/**
	 * Convenience label creation
	 * @param _parent
	 * @param text
	 * @return
	 */
	private Label createLabel(Composite _parent, String text) {
		Label label = new Label(_parent, SWT.NONE);
		label.setText(text);
		return label;
	}

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

//		//
//		// port configuration controls
//		//
//		label = new Label(_parent, SWT.BORDER);
//		label.setText("Device configuration");
//		label.setBackground(green);
//		// Create new layout data
//		GridData data = new GridData(GridData.FILL, GridData.BEGINNING, true,
//				false, 4, 1);
//		data.horizontalSpan = 4;
//		label.setLayoutData(data);
//
//		// GPIB address
//		label = createLabel(_parent, "GPIB device address");
//		Combo combo = new Combo(_parent, SWT.READ_ONLY);
//		combo.setItems(new String[] { "1", "2", "3", "4", "5", "6", "7", "8",
//				"9", "10", "11", "12", "13", "14", "15" });
//		combo.select(1);
//		combo.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				Combo c = (Combo) e.widget;
//				System.out.println("Device address: " + c.getText());
//				int b = Integer.valueOf(c.getText());
//				deviceAddress = b;
//			}
//		});
//		empty = createLabel(_parent, "");
//		empty = createLabel(_parent, "");

		//
		// device access / port control controls
		//
		label = new Label(_parent, SWT.BORDER);
		label.setText("Device access control");
		label.setBackground(blue);
		// Create new layout data
		GridData data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		label.setLayoutData(data);

		HashSet<CommPortIdentifier> set = softTekApp.getCommunication()
				.getAvailableSerialPorts();
		Iterator<CommPortIdentifier> iter = set.iterator();
		String ports[] = new String[10]; // cannot believe more than 10 ports in
											// a PC
		int ii = 0;
		while (iter.hasNext()) {
			CommPortIdentifier p = iter.next();
			ports[ii++] = p.getName();
			System.out.println("" + p.getName());
		}

		Group deviceAccessGroup = new Group(_parent, SWT.BORDER);
		deviceAccessGroup.setText("Device access");
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		deviceAccessGroup.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		deviceAccessGroup.setLayoutData(data);

		Button b_connect = createButton(deviceAccessGroup, SWT.PUSH, "Connect",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						System.out
								.println("Connecting to Tek device, using GPIB address "
										+ deviceAddress);
						tek = softTekApp.getTekDevice();
						// register me for data reads in tek device
						tek.addDeviceDataListener(softTekApp
								.getNavigationView());

						softTekApp.getCommunication().start(portName, baudRate,
								dataBits, parity, stopBits);
						tek.setDeviceAddress(deviceAddress);
						redrawControls();
						softTekApp.getDevConfigView().redrawControls();
					}
				});

		Button b_Disconnect = createButton(deviceAccessGroup, SWT.PUSH,
				"Disconnect", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						System.out.println("Disconnecting from Tek Device");
						softTekApp.getCommunication().close();
					}
				});
		Button b_getId = createButton(deviceAccessGroup, SWT.PUSH, "Get Id",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// System.out.println("id?");
						tek.readBulkData("id?");
					}
				});

		Button b_getEvent = createButton(deviceAccessGroup, SWT.PUSH,
				"Get Event", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						tek.readBulkData("event?");
					}
				});

		Group memDownloadGroup = new Group(_parent, SWT.BORDER);
		memDownloadGroup.setText("Memory download from device");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		memDownloadGroup.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		memDownloadGroup.setLayoutData(data);

		Button b_getSetup = createButton(memDownloadGroup, SWT.PUSH, "Setup",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// writeToDevice("dis 10,1,ASCII,\"Setup Memory Upload\"");
						tek.readBulkData("ins?");
						redrawControls();
						softTekApp.getDevConfigView().redrawControls();
					}
				});

		Button b_User1 = createButton(memDownloadGroup, SWT.PUSH, "ProcMon",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						Job job = new Job("Mein Job") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								// Set total number of work units
								monitor.beginTask(
										"Doing something time consuming here",
										100);
								for (int i = 0; i < 5; i++) {
									try {
										Thread.sleep(1000);
										monitor.subTask("I'm doing something here "
												+ i);
										// Report that 20 units are done
										monitor.worked(20);
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
								}
								System.out.println("Called save");
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				});
		empty = createLabel(memDownloadGroup, "");
		empty = createLabel(memDownloadGroup, "");

		Button b_getAcqMem = createButton(memDownloadGroup, SWT.PUSH,
				"Acquisition Memory", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// ((Button)e.widget).setEnabled(false);
						// writeToDevice("DIS 10,1,ASCII,\"Acquisition Memory Upload\"");
						final MemoryImage oldImage = tek.getTekRaw()
								.getAcquisitionImage();
						Job job = new Job("AcqMemLoad") {
							@Override
							protected IStatus run(IProgressMonitor monitor) {
								Communication communication = softTekApp
										.getCommunication();

								// Set total number of work units
								monitor.beginTask("Loading Acq Mem Data",
										softTekApp.getTekDevice()
												.getSizeEstimationMem());
								boolean endLoop = false;
								// clear total bytes value
								communication.setTotalBytes(0);
								// send the command
								tek.readBulkData("acq?");

								long old_n = 0;
								while (!endLoop) {
									try {
										Thread.sleep(300);
										// Report that some units are done
										long n = (int) (communication
												.getTotalBytes());
										if (n > old_n) {
											int nn = (int) n - (int) old_n;
											monitor.subTask("I'm doing something here "
													+ n);
											monitor.worked(nn);
											old_n = n;
										}
										// criteria to end loop
										endLoop = (oldImage != tek.getTekRaw()
												.getAcquisitionImage());
									} catch (InterruptedException e1) {
										e1.printStackTrace();
									}
								}
								// System.out.println("Finished loading");
								return Status.OK_STATUS;
							}
						};
						job.schedule();
					}
				});

		Button b_getRefMem = createButton(memDownloadGroup, SWT.PUSH,
				"Reference Memory", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						tek.readBulkData("ref?");
					}
				});

		Group memUploadGroup = new Group(_parent, SWT.BORDER);
		memUploadGroup.setText("Memory upload to device");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		memUploadGroup.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		memUploadGroup.setLayoutData(data);

		Button b_putSetup = createButton(memUploadGroup, SWT.PUSH, "Setup",
				new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// writeToDevice("dis 10,1,ASCII,\"Setup Memory Download\"");
						tek.writeBulkData(tek.getTekRaw().getSetupImage());
					}
				});
		Button b_putAcqMem = createButton(memUploadGroup, SWT.PUSH,
				"Acquisition Memory", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// writeToDevice("dis 10,1,ASCII,\"Setup Memory Download\"");
						tek.writeBulkData(tek.getTekRaw().getAcquisitionImage());
					}
				});
		Button b_sendLoadCmdAcq = createButton(memUploadGroup, SWT.PUSH,
				"Load ACQ", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						if (!softTekApp.getCommunication().getStarted()) {
							System.out
									.println("Communication was not yet started.");
							return;
						}
						// writeToDevice("dis 10,1,ASCII,\"Setup Memory Download\"");
						try {
							softTekApp.getCommunication().write("LO AC");
						} catch (IOException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				});
		empty = createLabel(memUploadGroup, "");

		/*
		 * data display config controls
		 */
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		label = new Label(_parent, SWT.BORDER);
		label.setText("Data Display Config");
		label.setBackground(lightGreen);
		label.setLayoutData(data);

		// color scheme
		label = createLabel(_parent, "Color scheme:");
		Combo combo = new Combo(_parent, SWT.READ_ONLY);
		combo.setItems(new String[] { "old school", "color coded" });
		combo.select(1);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				System.out.println("color scheme: " + c.getText());
				if (c.getText().equals("old school"))
					softTekApp
							.setDisplayColorCodingStyle(softTekApp.COLORCODING_OLDSCHOOL);
				else
					softTekApp
							.setDisplayColorCodingStyle(softTekApp.COLORCODING_EE);
				softTekApp.getView().loadAndDrawTekData();
			}
		});
		empty = createLabel(_parent, "");
		empty = createLabel(_parent, "");

		label = createLabel(_parent, "Grid scale:");
		combo = new Combo(_parent, SWT.READ_ONLY);
		combo.setItems(new String[] { "1", "2", "3", "4", "5", "10", "15",
				"20", "50" });
		combo.select(0);
		combo.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				Combo c = (Combo) e.widget;
				gridScale = Integer.valueOf(c.getText());
				System.out.println("grid scale: " + gridScale);
				softTekApp.getView().scalePeriodWidth(gridScale);
				softTekApp.getView().clearView();
				softTekApp.getView().drawTekData();
			}
		});

		Group displayGroup = new Group(_parent, SWT.BORDER);
		displayGroup.setText("Groups to display");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 5;
		displayGroup.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		displayGroup.setLayoutData(data);

		for (int i = 0; i < DisplayGroup.NUM_GROUPS; i++) {
			b_group[i] = createButton(displayGroup, SWT.CHECK, "" + (i),
					new SelectionAdapter() {
						public void widgetSelected(SelectionEvent e) {
							Button b = (Button) e.widget;
							System.out.println(b.getText());
							softTekApp.getView().clearView();
							softTekApp.getView().drawTekData();
						}

					});
		}
		b_group[0].setSelection(true);
		b_group[1].setSelection(true);
		b_group[2].setSelection(true);
		b_group[5].setSelection(true);
		b_group[6].setSelection(true);
		b_group[7].setSelection(true);

		/*
		 * Cursors
		 */
		Group cg = new Group(_parent, SWT.BORDER);
		cg.setText("Cursor control");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		cg.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		cg.setLayoutData(data);

		Button b_c1 = new Button(cg, SWT.CHECK);
		b_c1.setText("Cursor 1");
		b_c1.setSelection(false);
		b_c1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				softTekApp
						.getView()
						.getTimeCursor(0)
						.setEnabled(
								!softTekApp.getView().getTimeCursor(0)
										.isEnabled());
			}
		});

		Button b_c2 = new Button(cg, SWT.CHECK);
		b_c2.setText("Cursor 2");
		b_c2.setSelection(false);
		b_c2.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				softTekApp
						.getView()
						.getTimeCursor(1)
						.setEnabled(
								!softTekApp.getView().getTimeCursor(1)
										.isEnabled());
			}
		});

		/*
		 * Decorators
		 */
		Group dg = new Group(_parent, SWT.BORDER);
		dg.setText("Decorators");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		dg.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		dg.setLayoutData(data);

		Button b_d1 = new Button(dg, SWT.CHECK);
		b_d1.setText("I2C");
		b_d1.setSelection(false);
		b_d1.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				if (i2cDecorator==null) {
					i2cDecorator = new I2CDecorator();
					i2cDecorator.init(tek);
					i2cDecorator.decorate();
					i2cDecorator.print();
					i2cDecorator.draw();
					softTekApp.getView().redrawView();				
				}
				viewDecorator=true;
			}
		});

		/* display commands */
		Group displayCmdGroup = new Group(_parent, SWT.BORDER);
		displayCmdGroup.setText("Display commands");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		displayCmdGroup.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		displayCmdGroup.setLayoutData(data);

		Button b_clearView = createButton(displayCmdGroup, SWT.PUSH,
				"Clear View", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						softTekApp.getView().clearView();
						softTekApp.getView().redrawView();
					}
				});

		Button b_redrawView = createButton(displayCmdGroup, SWT.PUSH,
				"Redraw View", new SelectionAdapter() {
					public void widgetSelected(SelectionEvent e) {
						softTekApp.getView().redrawView();
					}
				});

		/*
		 * group config controls
		 */
		groupConfigLabel = new Label(_parent, SWT.BORDER);
		groupConfigLabel.setText("Group Config");
		groupConfigLabel.setBackground(lightGreen);
		// Create new layout data
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		groupConfigLabel.setLayoutData(data);

		groupNameLabel = new Label[10];
		groupTbLabel = new Label[10];
		groupChannelLabel = new Label[10];

		// create labels for later use
		for (int i = 0; i < 10; i++) {
			groupNameLabel[i] = createLabel(_parent, "Name: ?        ");
			groupTbLabel[i] = createLabel(_parent, "Timebase: ?      ");
			groupChannelLabel[i] = createLabel(_parent, "Channels:      ");
			empty = createLabel(_parent, "");
		}

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
		// Group dbs = new Group(_parent, SWT.BORDER);
		// dbs.setText("Acquisition slots / cards / pods");
		// gridLayout = new GridLayout();
		// gridLayout.numColumns = 4;
		// dbs.setLayout(gridLayout);
		// data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		// data.horizontalSpan = 4;
		// dbs.setLayoutData(data);
		//
		// slotNameLabel = new Label[8];
		// slotCardType = new Label[8];
		// slotCardThreshold = new Label[8];
		// label = createLabel(dbs, "Slot");
		// label = createLabel(dbs, "Card+chain state");
		// label = createLabel(dbs, "Threshold");
		// empty = createLabel(dbs, "");
		// for (int i = 0; i < /* tek.getTekRaw().MAX_MEMCARD_COUNT */8; i++) {
		// slotNameLabel[i] = createLabel(dbs, "# " + i + "        ");
		// slotCardType[i] = createLabel(dbs,
		// "?                                        ");
		// slotCardThreshold[i] = createLabel(dbs,
		// "?                                   ");
		// empty = createLabel(dbs, "");
		// }

		/*
		 * time bases
		 */
		Group dbs = new Group(_parent, SWT.BORDER);
		dbs.setText("Time bases");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		dbs.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		dbs.setLayoutData(data);

		// Time bases
		String s = "?         ";
		label = createLabel(dbs, "Timebase 1");
		timeBase1ModeLabel = createLabel(dbs, "Mode: " + s);
		timeBase1PeriodLabel = createLabel(dbs, "Clock period: " + s);
		empty = createLabel(dbs, "");

		label = createLabel(dbs, "Timebase 2");
		timeBase2ModeLabel = createLabel(dbs, "Mode: " + s);
		timeBase2PeriodLabel = createLabel(dbs, "Clock period: " + s);
		empty = createLabel(dbs, "");

		/*
		 * port monitoring controls
		 */
		label = new Label(_parent, SWT.BORDER);
		label.setText("Port Monitoring");
		label.setBackground(blue);
		// Create new layout data
		data = new GridData(GridData.FILL, GridData.BEGINNING, true, false, 4,
				1);
		data.horizontalSpan = 4;
		label.setLayoutData(data);

		Group mg = new Group(_parent, SWT.BORDER);
		mg.setText("Monitoring");
		gridLayout = new GridLayout();
		gridLayout.numColumns = 4;
		mg.setLayout(gridLayout);
		data = new GridData(GridData.FILL, GridData.CENTER, true, false);
		data.horizontalSpan = 4;
		mg.setLayoutData(data);

		Label label30 = new Label(mg, SWT.NONE);
		label30.setText("bits/s:");
		bar = new ProgressBar(mg, SWT.SMOOTH);
		bar.setMaximum(38400);
		bar.setMinimum(0);
		empty = createLabel(dbs, "");
		empty = createLabel(dbs, "");

		Label label31 = createLabel(mg, "Bytes total:");
		totalBytesLabel = createLabel(mg, "000000000000");

	}

	/**
	 * This is a callback that will allow us to create the viewer and initialize
	 * it.
	 */
	public void createPartControl(final Composite parent) {

		this.parent = parent;
		createControls(parent);

		job = new Job("CpsJob") {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				if (parent.isDisposed())
					return Status.OK_STATUS;
				parent.getDisplay().getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						bar.setSelection(softTekApp.getCommunication().getCps() * 8);
						bar.setToolTipText(""
								+ softTekApp.getCommunication().getCps() * 8
								+ "Bits/s");
						totalBytesLabel.setText(Long.toString(softTekApp
								.getCommunication().getTotalBytes()));
					}
				});
				schedule(17);
				return Status.OK_STATUS;
			}
		};

		// Start the Job
		job.setSystem(true);
		job.schedule();
	}

	void redrawControls() {
		String mode = "?", s = "?";
		int m, n;

		tek = softTekApp.getTekDevice();
		if (tek != null) {

			// cards
			// for (int i = 0; i < tek.getTekRaw().MAX_MEMCARD_COUNT; i++) {
			// int val = tek.getTekRaw().getMemStat(i);
			// boolean enabled = true;
			// if (val == tek.getTekRaw().C_MEM_CARD_MISSING) {
			// s = "-";
			// enabled = false;
			// }
			// if (val == tek.getTekRaw().C_MEM_CARD_9_UNCHAINED)
			// s = "9 channels, chain head";
			// if (val == tek.getTekRaw().C_MEM_CARD_18_UNCHAINED)
			// s = "18 channels, chain head";
			// if (val == tek.getTekRaw().C_MEM_CARD_CHAINED)
			// s = "chain follower";
			// slotCardType[i].setText(s);
			//
			// slotCardThreshold[i].setText(""
			// + tek.getTekRaw().getPrettyThreshold(i));
			// slotCardType[i].setEnabled(enabled);
			// slotNameLabel[i].setEnabled(enabled);
			// slotCardThreshold[i].setEnabled(enabled);
			// }

			// time bases
			int tb = tek.getTekRaw().getRawTbActive();
			boolean tb1_enabled = false, tb2_enabled = false;

			if (tb == 0 || tb == 2) {
				tb1_enabled = true;
			}
			if (tb == 1 || tb == 2) {
				tb2_enabled = true;
			}

			// time base 1
			m = tek.getTekRaw().getRawTimebase1Type();
			switch (m) {
			case 0:
				mode = "async";
				break;
			case 1:
				mode = "sync";
				break;
			}
			n = tek.getTekRaw().getRawTimebase1Async();
			s = "" + tek.getTekRaw().getClockPeriodValue(n)
					+ tek.getTekRaw().getClockPeriodQualifier(n);
			timeBase1ModeLabel.setText("Mode: " + mode);
			timeBase1PeriodLabel.setText("Clock period: " + s);
			timeBase1ModeLabel.setEnabled(tb1_enabled);
			timeBase1PeriodLabel.setEnabled(tb1_enabled);

			// time base 2
			m = tek.getTekRaw().getRawTimebase2Type();
			switch (m) {
			case 0:
				mode = "demux";
				break;
			case 1:
				mode = "sync";
				break;
			}
			// n = tek.getTekRaw().getRawTimebase1Async();
			// s = "" + tek.getTekRaw().getClockPeriodValue(n)
			// + tek.getTekRaw().getClockPeriodQualifier(n);
			s = "(?)";
			timeBase2ModeLabel.setText("Mode: " + mode);
			timeBase2PeriodLabel.setText("Clock period: " + s);
			timeBase2ModeLabel.setEnabled(tb2_enabled);
			timeBase2PeriodLabel.setEnabled(tb2_enabled);
			//
			// group config controls
			//
			// ensure that group array is filled
			tek.buildGroupLayout();

			boolean enabled;
			for (int i = 0; i < 10; i++) {

				if (tek.getGroup(i).getNumChannels() == 0)
					enabled = false;
				else
					enabled = true;

				groupNameLabel[i].setText(tek.getGroup(i).getName());
				groupNameLabel[i].setEnabled(enabled);

				tb = tek.getGroup(i).getTimebase();
				switch (tb) {
				case DisplayGroup.TIMEBASE_1:
					s = "1";
					break;
				case DisplayGroup.TIMEBASE_2:
					s = "2";
					break;
				case DisplayGroup.TIMEBASE_UNASSIGNED:
					s = "-";
					break;

				}
				groupTbLabel[i].setText("Timebase: " + s);
				groupTbLabel[i].setEnabled(enabled);

				groupChannelLabel[i].setText("Channels: "
						+ tek.getGroup(i).getNumChannels());
				groupChannelLabel[i].setEnabled(enabled);
			}
		}
		if (softTekApp.getDevConfigView() != null)
			softTekApp.getDevConfigView().redrawControls();
	}

	/**
	 * Passing the focus request to the viewer's control.
	 */
	public void setFocus() {
		// viewer.getControl().setFocus();
	}

	@Override
	public void handleSetupData(byte[] data, int dataSize) {
		System.out.println("setup data read in");
		newSetupImage = new MemoryImage(true);
		newSetupImage.build(data, dataSize);
	}

	@Override
	public void handleAcquisitionData(byte[] data, int dataSize) {
		System.out.println("acq data read in");
		newMemImage = new MemoryImage(true);
		newMemImage.build(data, dataSize);

		if (newSetupImage != null && newMemImage != null) {
			// remove listener of old tek object
			tek.removeDeviceDataListener(softTekApp.getNavigationView());
			// create new tek object from new data
			tek = new Tek1241Device(newSetupImage, newMemImage);
			// add listener for data reads in new tek device
			tek.addDeviceDataListener(softTekApp.getNavigationView());

			softTekApp.setTekDevice(tek);

			// clear old canvas content
			softTekApp.getView().clearView();
			softTekApp.getView().drawTekDataFromDevice();

			redrawControls();
			softTekApp.getDevConfigView().redrawControls();
			softTekApp.getView().redrawView();

		}

	}

	@Override
	public void handleRefmemData(byte[] data, int dataSize) {
		// TODO Auto-generated method stub
		System.out.println("ref data available");
	}

	@Override
	public void handleEventData(String data) {
		System.out.println(data);
	}

	@Override
	public void handleIdData(String data) {
		// System.out.println(data);
		tek.parseIdString(data);
	}

	@Override
	public void handleServiceRequest(byte[] request, int requestSize,
			int eventId) {
		System.out.println("Received service request from device: "
				+ EventMessages.getInstance().getEventMessage(eventId));
		if (eventId == EventMessages.EVENT_REQ_ACQMEM_UPLOAD) {
			// writeToDevice("DIS 10,1,ASCII,\"Acquisition Memory  Upload");
			tek.readBulkData("acq?");
			return;
		}
		if (eventId == EventMessages.EVENT_REQ_SETUP_UPLOAD) {
			// writeToDevice("dis 10,1,ASCII,\"Setup Memory Upload");
			tek.readBulkData("ins?");
			return;
		}
		// unknown SRQ data , dump it out
		for (int i = 0; i < requestSize; i++)
			System.out.print((char) (request[i]));

	}

	public void writeToDevice(String s) {
		try {
			softTekApp.getCommunication().write(s + "\r");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Returns display state for group groupId
	 * 
	 * @param groupId
	 *            group to check
	 * @return display state for group
	 */
	public boolean isGroupVisible(int groupId) {
		return b_group[groupId].getSelection();
	}

	public int getGridScale() {
		return gridScale;
	}

	public int getBaudRate() {
		return baudRate;
	}

	public void setBaudRate(int baudRate) {
		this.baudRate = baudRate;
	}

	public int getDataBits() {
		return dataBits;
	}

	public void setDataBits(int dataBits) {
		this.dataBits = dataBits;
	}

	public int getParity() {
		return parity;
	}

	public void setParity(int parity) {
		this.parity = parity;
	}

	public int getStopBits() {
		return stopBits;
	}

	public void setStopBits(int stopBits) {
		this.stopBits = stopBits;
	}

	public int getDeviceAddress() {
		return deviceAddress;
	}

	public void setDeviceAddress(int deviceAddress) {
		this.deviceAddress = deviceAddress;
	}

	public String getPortName() {
		return portName;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}
}