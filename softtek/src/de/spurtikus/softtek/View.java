package de.spurtikus.softtek;

import java.io.IOException;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ScrollBar;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.part.ViewPart;

import de.spurtikus.comm.serial.BulkDataListener;
import de.spurtikus.comm.serial.Communication;
import de.spurtikus.softtek.tek.Channel;
import de.spurtikus.softtek.tek.DisplayGroup;
import de.spurtikus.softtek.tek.MemoryImage;
import de.spurtikus.softtek.tek.Tek1241Device;

import softtek.ConversionUtil;
import softtek.FileReader;
import softtek.FileWriter;

public class View extends ViewPart {

	public static final String ID = "softtek.view";

	/**
	 * Scaling factor x direction
	 */
	int xScale = 1;

	public final int DEFAULT_PERIOD_WIDTH = 10;
	public final int DEFAULT_PERIOD_HEIGHT = 20;
	public final int DEFAULT_PERIOD_MARGIN = DEFAULT_PERIOD_HEIGHT;

	public final int DEFAULT_NUM_PERIODS = 1025;
	public final int DEFAULT_NUM_CHANNELS = 72;

	public final int VIEW_START_X_OFFSET = 100;
	public final int VIEW_START_Y_OFFSET = 100;

	public final int GROUP_Y_MARGIN = 100;

	public final int DEFAULT_CANVAS_WIDTH = DEFAULT_NUM_PERIODS
			* DEFAULT_PERIOD_WIDTH + 2 * VIEW_START_X_OFFSET;
	public final int DEFAULT_CANVAS_HEIGHT = DEFAULT_NUM_CHANNELS
			* (DEFAULT_PERIOD_HEIGHT + DEFAULT_PERIOD_MARGIN) + 2
			* VIEW_START_Y_OFFSET;

	private final int CURSOR_WIDTH = DEFAULT_PERIOD_WIDTH;
	private final int CURSOR_HEIGHT = 360; // TODO: ...

	/** data for application is in singleton */
	private ApplicationSingleton softTekApp = ApplicationSingleton
			.getInstance();

	private Tek1241Device tek;

	private String periodQualifier;
	private int periodLength;
	private int triggerPos;

	private Canvas canvas = null;
	private GC gc = null;

	/** period width and height values to use */
	private int periodWidth;
	private int periodHeight;
	private int periodMargin;

	private Color yellow = null;
	private Color black = null;
	private Color darkGray = null;
	private Color red = null;
	private Color green = null;
	private Color white = null;
	private Color cursorColor = null;

	final int NUM_CURSORS = 2;
	TimeCursor[] timeCursor;

	Image originalImage;

	/**
	 * Color coding scheme colors
	 */
	private Color colorCodes[] = null;

	/**
	 * CTR
	 * 
	 * Does basic initialization
	 */
	public View() {
		super();
		// start values for plotline
		periodWidth = DEFAULT_PERIOD_WIDTH;
		periodHeight = DEFAULT_PERIOD_HEIGHT;
		periodMargin = DEFAULT_PERIOD_MARGIN;
		softTekApp.setView(this);
		timeCursor = new TimeCursor[NUM_CURSORS];
		for (int i = 0; i < NUM_CURSORS; i++)
			timeCursor[i] = null;
	}

	/**
	 * Create all required controls
	 * 
	 * @param parent
	 *            - root object to create controls below
	 */
	public void createPartControl(Composite parent) {

		// create the console object for later use
		// Console cons = new Console(softTekApp.getApplicationName());

		Display display = parent.getDisplay();

		yellow = display.getSystemColor(SWT.COLOR_YELLOW);
		black = display.getSystemColor(SWT.COLOR_BLACK);
		darkGray = display.getSystemColor(SWT.COLOR_DARK_GRAY);
		red = display.getSystemColor(SWT.COLOR_RED);
		green = display.getSystemColor(SWT.COLOR_GREEN);
		white = display.getSystemColor(SWT.COLOR_WHITE);

		cursorColor = display.getSystemColor(SWT.COLOR_RED);

		// color scheme: EE color codes
		colorCodes = new Color[10];
		colorCodes[0] = new Color(display, 60, 60, 60); // CLOSE TO BLACK (but
														// different from
														// background)
		colorCodes[1] = new Color(display, 128, 67, 0); // BROWN
		colorCodes[2] = new Color(display, 246, 4, 0); // RED
		colorCodes[3] = new Color(display, 249, 135, 0); // ORANGE
		colorCodes[4] = new Color(display, 254, 255, 1); // YELLOW
		colorCodes[5] = new Color(display, 8, 251, 1); // GREEN
		colorCodes[6] = new Color(display, 1, 0, 254); // BLUE
		colorCodes[7] = new Color(display, 128, 3, 133);// VIOLET
		colorCodes[8] = new Color(display, 132, 132, 132); // GRAY
		colorCodes[9] = display.getSystemColor(SWT.COLOR_WHITE); // WHITE

		int width = DEFAULT_CANVAS_WIDTH, height = DEFAULT_CANVAS_HEIGHT;
		System.out.println("Canvas width: " + width + ", height: " + height);
		originalImage = new Image(display, width, height);
		gc = new GC(originalImage);
		gc.setBackground(black);
		gc.fillRectangle(0, 0, width, height);

		// final Image image = originalImage;
		final Point origin = new Point(0, 0);
		canvas = new Canvas(parent, SWT.NO_BACKGROUND | SWT.NO_REDRAW_RESIZE
				| SWT.V_SCROLL | SWT.H_SCROLL);

		loadAndDrawTekData();

		final ScrollBar hBar = canvas.getHorizontalBar();
		hBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int hSelection = hBar.getSelection();
				int destX = -hSelection - origin.x;
				Rectangle rect = originalImage.getBounds();
				canvas.scroll(destX, 0, 0, 0, rect.width, rect.height, false);
				origin.x = -hSelection;
			}
		});

		final ScrollBar vBar = canvas.getVerticalBar();
		vBar.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				int vSelection = vBar.getSelection();
				int destY = -vSelection - origin.y;
				Rectangle rect = originalImage.getBounds();
				canvas.scroll(0, destY, 0, 0, rect.width, rect.height, false);
				origin.y = -vSelection;
			}
		});

		/**
		 * Resize repaints complete window content (visible part of canvas)
		 */
		canvas.addListener(SWT.Resize, new Listener() {
			public void handleEvent(Event e) {
				Rectangle rect = originalImage.getBounds();
				Rectangle client = canvas.getClientArea();
				hBar.setMaximum(rect.width);
				vBar.setMaximum(rect.height);
				hBar.setThumb(Math.min(rect.width, client.width));
				vBar.setThumb(Math.min(rect.height, client.height));
				int hPage = rect.width - client.width;
				int vPage = rect.height - client.height;
				int hSelection = hBar.getSelection();
				int vSelection = vBar.getSelection();
				if (hSelection >= hPage) {
					if (hPage <= 0)
						hSelection = 0;
					origin.x = -hSelection;
				}
				if (vSelection >= vPage) {
					if (vPage <= 0)
						vSelection = 0;
					origin.y = -vSelection;
				}
				canvas.redraw();
			}
		});

		/*
		 * called e.g. during scrolling
		 */
		canvas.addListener(SWT.Paint, new Listener() {
			public void handleEvent(Event e) {
				GC gc = e.gc;
				gc.drawImage(originalImage, origin.x, origin.y);
				Rectangle rect = originalImage.getBounds();
				Rectangle client = canvas.getClientArea();
				int marginWidth = client.width - rect.width;
				if (marginWidth > 0) {
					gc.fillRectangle(rect.width, 0, marginWidth, client.height);
				}
				int marginHeight = client.height - rect.height;
				if (marginHeight > 0) {
					gc.fillRectangle(0, rect.height, client.width, marginHeight);
				}
				// System.out.println("Redrawed canvas ("+e.x+","+e.y+" "+e.width+","+e.height+")");
				int pos = timeCursor[0].getPosition();
				int bv = tek.getByteValue(gc, getGroupFromPixelPosition(pos),
						getBitPositionFromPixelPosition(pos));
				timeCursor[0].redraw(ConversionUtil.byte2hexString(bv),
						ConversionUtil.byte2dualString(bv),
						getBitPositionFromPixelPosition(pos));
				pos = timeCursor[1].getPosition();
				bv = tek.getByteValue(gc, getGroupFromPixelPosition(pos),
						getBitPositionFromPixelPosition(pos));
				timeCursor[1].redraw(ConversionUtil.byte2hexString(bv),
						ConversionUtil.byte2dualString(bv),
						getBitPositionFromPixelPosition(pos));
			}
		});
	}

	/**
	 * Clear the canvas
	 */
	public void clearView() {
		System.out.println("clear canvas");
		gc.setBackground(black);
		gc.fillRectangle(0, 0, DEFAULT_CANVAS_WIDTH, DEFAULT_CANVAS_HEIGHT);
		// canvas.redraw();
	}

	/**
	 * Init cursor array
	 */
	private void initCursors() {
		for (int i = 0; i < NUM_CURSORS; i++) {
			if (timeCursor[i] == null) {
				timeCursor[i] = new TimeCursor("Cursor " + (i + 1), canvas,
						VIEW_START_X_OFFSET + i * 100, VIEW_START_Y_OFFSET
								- periodHeight - periodMargin,
						CURSOR_WIDTH - 4, CURSOR_HEIGHT, CURSOR_WIDTH,
						VIEW_START_X_OFFSET, VIEW_START_Y_OFFSET - periodHeight
								- periodMargin, green, black);
			}
		}
	}

	/**
	 * Use tek data loaded from file or from device and (re)draw the canvas
	 * content.
	 * 
	 */
	public void drawTekData() {
		// use "latest" incarnation of tek device
		tek = softTekApp.getTekDevice();
		NavigationView nv = softTekApp.getNavigationView();

		// dump visible groups to canvas
		for (int i = 0; i < DisplayGroup.NUM_GROUPS; i++)
			if (nv.isGroupVisible(i))
				drawGroup(gc, i, triggerPos);
		// if this is a redraw (e.g. color coding has changed), we must force
		// the repaint.
		// otherwise, old data is just used.
		canvas.redraw();

		// (re) init cursors
		initCursors();

		// update controls
		nv.redrawControls();
		// softTekApp.getDevConfigView().redrawControls();
	}

	/**
	 * Read in some default tek data from file and (re)draw the canvas content.
	 * 
	 */
	public void loadAndDrawTekData() {
		loadImages("/home/dennis/softtek-data/tek1241-tpg-test.tek");
		drawTekData();
	}

	/**
	 * Use tek data read in from device and (re)draw the canvas content.
	 * 
	 * The content was asyncronically read in from serial port in own reader
	 * thread. So we do synchronize the UI thread with external thread using a
	 * "Job". This Job is newly created and executed one time for each access to
	 * the device.
	 */
	public void drawTekDataInJob() {

		Job job = new Job("DrawCanvasJob") {
			@SuppressWarnings("static-access")
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				Canvas parent = (Canvas) softTekApp.getView().getCanvas();
				if (parent.isDisposed())
					return Status.OK_STATUS;
				parent.getDisplay().getDefault().asyncExec(new Runnable() {
					@Override
					public void run() {
						// System.out.println("drawJob");
						// let drawTekData() do the real work
						drawTekData();
					}
				});
				return Status.OK_STATUS;
			}
		};
		// Start the Job
		job.schedule();

	}

	/**
	 * (Re)draw the canvas content after a read in of data from device. called
	 * 1x
	 * 
	 * Currently, some groups are read. Needs to be generalized.
	 */
	public void drawTekDataFromDevice() {
		tek = softTekApp.getTekDevice();

		int n = tek.getTekRaw().getRawTimebase1Async();
		periodQualifier = tek.getTekRaw().getClockPeriodQualifier(n);
		periodLength = tek.getTekRaw().getClockPeriodValue(n);
		// System.out.println("ClockPeriodQualifier: " + periodQualifier);
		// System.out.println("ClockPeriod: " + periodLength);
		triggerPos = tek.getTekRaw().getRawTpi(tek.getTekRaw().C_TB_TIMEBASE1);
		// System.out.println("Timebase 1 clocks after trigger event: " + n);

		drawTekDataInJob();
		softTekApp.getNavigationView().redrawControls();
		softTekApp.getDevConfigView().redrawControls();
	}

	/**
	 * Save setup image + acquisition image into a file.
	 * 
	 * The images are stored in their ASCII HEX format, as they came from the
	 * tek device. To be able to extract the ASCII hex data after read in the
	 * image, the image has to be created by setting the parameter keepInput to
	 * true. The resulting file has two large lines.
	 * 
	 * @param file
	 */
	public void saveImages(String file) {
		int size;
		byte endLine[] = { 0x0a };

		System.out.println("Writing to file: " + file);
		FileWriter writer = new FileWriter(file);

		writer.open();

		// setup image part
		MemoryImage image = softTekApp.getTekDevice().getTekRaw()
				.getSetupImage();
		byte[] buf = image.getInput();
		size = image.getInputSize();
		writer.write(buf, image.getInputSize());
		writer.write(endLine);

		// acquisition memory part
		image = softTekApp.getTekDevice().getTekRaw().getAcquisitionImage();
		buf = image.getInput();
		writer.write(buf, image.getInputSize());
		size += image.getInputSize();
		writer.write(endLine);

		writer.close();
		System.out.println("Wrote " + size + " bytes.");
	}

	/**
	 * Reads in data from a tek file and sets up "tek" variable The tek file is
	 * a file ending usually with the postfix ".tek". It contains ASCII HEX
	 * values for setup and memory data. The file consists of two lines. Line 1
	 * is of type "INSET ..." Line 2 is of type "ACQ ..." or "REF ..."
	 * 
	 * From these two lines, two memory images are constructed (one for the
	 * setup data, and one for the memory data. From these two images, a new
	 * "TekDevice" object is then created. This results in display of the data
	 * just loaded.
	 * 
	 * @param file
	 *            complete path of file to load.
	 */
	public void loadImages(String file) {
		// create memory images
		MemoryImage setupImage = new MemoryImage(true);
		MemoryImage acqImage = new MemoryImage(true);

		// open input file
		FileReader fileReader = new FileReader(file);
		int size = fileReader.read();
		byte[] d = fileReader.getBuffer();

		// do a simple chack for file content validity by checking the first
		// some bytes
		boolean fileIsValidBySimpleCheck = false;
		if (((char) d[0] == 'I') && ((char) d[1] == 'N')
				&& ((char) d[2] == 'S') && ((char) d[3] == 'E')
				&& ((char) d[4] == 'T'))
			fileIsValidBySimpleCheck = true;
		if (!fileIsValidBySimpleCheck) {
			System.err.println("File " + file
					+ " seems not to be a valid tek file");
			return;
		} else {
			System.out.println("File " + file + " looks like a valid tek file");

		}
		// fill setup image
		int i = 0;
		// read until first CR or LF is reached. This is the end of the setup
		// line.
		while ((i < size) && ((d[i] != 0x0a) && (d[i] != 0x0d))) {
			// System.out.print((char) d[i]);
			i++;
		}
		System.out.println("Setup data ends at file position " + i);
		// create image from buffer part
		setupImage.build(d, i);
		// overread CRs and LFs until next line starts
		while ((d[i] == 0x0a) || (d[i] == 0x0d))
			i++;
		// keep j, this is the start offset of second line
		int j = i;
		// allocate buffer that can be filled with second line data
		byte a[] = new byte[size];
		// read until first CR or LF is reached. This is the end of the memory
		// data line.
		while ((i < size) && ((d[i] != 0x0a) && (d[i] != 0x0d))) {
			a[i - j] = d[i];
			// System.out.print((char) d[i]);
			i++;
		}
		System.out.println("Memory data starts at position " + j
				+ " and goes until position " + i);
		// create image from buffer part
		acqImage.build(a, i - j);

		// create a TekDev object with the image just created
		tek = new Tek1241Device(setupImage, acqImage);

		int n = tek.getTekRaw().getRawTimebase1Async();
		periodQualifier = tek.getTekRaw().getClockPeriodQualifier(n);
		periodLength = tek.getTekRaw().getClockPeriodValue(n);
		// System.out.println("ClockPeriodQualifier: " + periodQualifier);
		// System.out.println("ClockPeriod: " + periodLength);
		triggerPos = tek.getTekRaw().getRawTpi(tek.getTekRaw().C_TB_TIMEBASE1);
		// System.out.println("Timebase 1 clocks after trigger event: " + n);

		softTekApp.setTekDevice(tek);
		tek.getTekRaw().printCards();

	}

	/**
	 * draw out a complete group data set.
	 * 
	 * All bits of that group are dumped in a time diagram. The related group
	 * name and the bit number is printed left from the timing data.
	 * 
	 * Precondition: group array must be filled.
	 * 
	 * @param gc
	 *            graphics context to use.
	 * @param groupId
	 *            the groupId of the group to dump
	 * @param triggerPos
	 *            trigger position value.
	 */
	public void drawGroup(GC gc, int groupId, int triggerPos) {
		int xstart;
		int ystart;
		int bitposInPod;
		int pod = 0;

		// offset of plotline
		xstart = getXOffsetForGroup(groupId);
		ystart = getYOffsetForGroup(groupId);

		// get depth for a group
		// Assumption: all channels of a group are of the same depth,
		// so it is ok to get the depth of the pod that is assigned to channel 0
		// TODO: make sure that assumption is true
		int depth = tek.getDepth(tek.getGroup(groupId).getPod(0));

		// draw the time grid for this group
		drawTimeGrid(groupId, gc, xstart, ystart - 70, depth, 10);

		// draw channel by channel for this group
		for (Channel c : tek.getGroup(groupId).getChannels()) {
			pod = c.getPod();
			bitposInPod = c.getBit();

			// get array of bit values
			int[] p = tek.getTimelineBits(pod, bitposInPod);

			Color color;
			// set color to use for drawing this channel
			if (softTekApp.getDisplayColorCodingStyle() == ApplicationSingleton.COLORCODING_EE)
				color = colorCodes[bitposInPod];
			else
				color = yellow;

			// let drawChannel do the rest
			drawChannel(gc, xstart, ystart, p, color);

			// leading text
			gc.setForeground(color);
			gc.drawText(tek.getGroup(groupId).getName() + " " + c.getPos(),
					xstart - 80, ystart - 25);
			gc.drawText("Pod " + pod + ", Pin " + bitposInPod, xstart - 80,
					ystart - 10);
			// increment position values for next channel
			ystart += (periodHeight + periodMargin);
			xstart = getXOffsetForGroup(groupId);
		}
	}

	/**
	 * Draw time grid
	 * 
	 * The time grid is a line of time marks. A time mark is a vertical line
	 * with an optional text above that line. The text is a time value then.
	 * 
	 * @param groupId
	 *            Id of the group to draw time grid for. Needed to inquire
	 *            height of group.
	 * @param gc
	 * @param xstart
	 *            x position to start the time grid from
	 * @param ystart
	 *            y position to start the time grid from
	 * @param depth
	 *            length of the time line (=number of periods)
	 * @param module
	 *            modulo value to print out additional text at each modulo-th
	 *            mark. Also, the corresponding line is drawn larger than the
	 *            other ones.
	 */
	public void drawTimeGrid(int groupId, GC gc, int xstart, int ystart,
			int depth, int module) {
		// create point list for polygon to draw
		int i;
		int x, y;
		int markOffset = 0;
		x = xstart;
		y = ystart;
		int timePointVal = 0;

		// modulo factor is the modulo value, i.e. each "moduloFactor"th
		// vertical line, a time value ("1200ns" i printed out in background
		// grid.
		int moduloFactor = 5;
		// moduloValue is the value in pixel between two vertical time lines
		// (background grid)
		int moduloValue = 0;

		// calculate pixel value from desired factor
		moduloValue = moduloFactor * periodLength * xScale;

		// line length is height of group plus one signal line height (because
		// the group is higher than just the sum of its signal lines)
		int lineLength = getGroupHeight(groupId)
				+ (periodHeight + periodMargin);
		gc.setForeground(darkGray);
		gc.drawText("Time unit: " + periodQualifier, x - 80, y);
		gc.setLineWidth(1);
		gc.setLineStyle(SWT.LINE_DOT);
		for (i = 0; i < depth - 1; i++) {
			timePointVal = (triggerPos - (depth - 1) + i + 1) * periodLength;
			if (timePointVal == 0) {
				gc.setForeground(red);
				gc.setLineStyle(SWT.LINE_SOLID);
			} else {
				gc.setForeground(darkGray);
				gc.setLineStyle(SWT.LINE_DOT);
			}
			if (timePointVal % moduloValue == 0) {
				gc.drawText("" + timePointVal, x + 7, y);
				markOffset = 20;
			} else {
				markOffset = 0;
			}
			x += periodWidth;
			if (xScale != 1) {
				if (timePointVal % (moduloValue / xScale) == 0) {
					gc.drawLine(x, y + 40 - markOffset, x, y + lineLength);
				}
			} else {
				gc.drawLine(x, y + 40 - markOffset, x, y + lineLength);
			}
		}
	}

	/**
	 * draw a single channel of a pod. Channel can contain non-glitched and
	 * glitched data.
	 * 
	 * @param gc
	 * @param xstart
	 *            x position to start the timeline from
	 * @param ystart
	 *            y position to start the timeline from
	 * @param bit
	 *            array including all bits of that channel
	 * @param color
	 */
	public void drawChannel(GC gc, int xstart, int ystart, int[] bit,
			Color color) {
		int[] resBit = new int[8192];
		int k = 0;
		int i, j;
		int x, y;

		gc.setForeground(color);
		gc.setLineWidth(4);
		gc.setLineStyle(SWT.LINE_SOLID);

		// create point list for polygon to draw
		x = xstart;
		y = ystart;

		int max;
		int bitOffset;
		if (tek.isGlitchData()) {
			max = bit.length / 2 - 1;
			bitOffset = 2;
		} else {
			max = bit.length - 1;
			bitOffset = 1;
		}
		for (i = 0; i < max; i++) {

			// handle glitched data
			if (tek.isGlitchData())
				j = 2 * i;
			else
				j = i;

			if ((bit[j] == 0 && bit[j + bitOffset] == 0)
					|| (bit[j] == 1 && bit[j + bitOffset] == 1)) {
				// no change LO->LO or HI->HI
				// start point
				resBit[k++] = x;
				resBit[k++] = y;
				// calc end point
				x += periodWidth;
				resBit[k++] = x;
				resBit[k++] = y;
			}

			if (bit[j] == 0 && bit[j + bitOffset] == 1) {
				// LO -> HI
				// 1. horiz. line
				resBit[k++] = x;
				resBit[k++] = y;
				x += periodWidth;
				resBit[k++] = x;
				resBit[k++] = y;
				// 2. vertical line up
				y -= periodHeight;
				resBit[k++] = x;
				resBit[k++] = y;
			}

			if (bit[j] == 1 && bit[j + bitOffset] == 0) {
				// HI -> LO
				// 1. horiz. line
				resBit[k++] = x;
				resBit[k++] = y;
				x += periodWidth;
				resBit[k++] = x;
				resBit[k++] = y;
				// 2. vertical line down
				y += periodHeight;
				resBit[k++] = x;
				resBit[k++] = y;

			}
		}
		if (tek.isGlitchData())
			j = 2 * i;
		else
			j = i;
		// last single point needs special care
		if ((bit[j] == 0 && bit[j - bitOffset] == 0)
				|| (bit[j] == 1 && bit[j - 2] == 1)) {
			x += periodWidth;
			resBit[k++] = x;
			resBit[k++] = y;
		}
		if ((bit[j] == 0 && bit[j - bitOffset] == 1)
				|| (bit[j] == 1 && bit[j - bitOffset] == 0)) {
			x += periodWidth;
			resBit[k++] = x;
			resBit[k++] = y;
		}

		// copy points into array of accurate size
		int[] points = new int[k];
		for (i = 0; i < k; i++)
			points[i] = resBit[i];
		gc.drawPolyline(points);

		// draw glitches on top of polygon drawn
		if (tek.isGlitchData()) {
			// some visual defaults values for glitch area
			Color glitchAreaColor = red;
			int glitchLineWidth = 10;
			int glitchLineType = SWT.LINE_SOLID;
			int glitchAreaAlpha = 120;

			// save current values
			Color saveColor = gc.getForeground();
			int saveLineWidth = gc.getLineWidth();
			int saveLineSytle = gc.getLineStyle();
			int saveAlpha = gc.getAlpha();
			// set values for glitch area
			gc.setLineStyle(glitchLineType);
			gc.setLineWidth(glitchLineWidth);
			gc.setForeground(glitchAreaColor);
			gc.setAlpha(glitchAreaAlpha);
			for (i = 0; i < k; i++) {
				if ((2 * i + 1 < bit.length) && (bit[2 * i + 1] == 2)) {
					// System.out.println("draw Glitch, pos=" + i + ", "
					// + (xstart + i * xdelta + 3));
					int yy = ystart - (10 - saveLineWidth) / 2;
					if (bit[2 * i] == 1)
						yy -= (periodHeight - (10 - saveLineWidth));
					gc.drawLine(xstart + i * periodWidth, yy, xstart + i
							* periodWidth + periodWidth, yy);
				}
			}
			// set back values
			gc.setForeground(saveColor);
			gc.setLineStyle(saveLineSytle);
			gc.setLineWidth(saveLineWidth);
			gc.setAlpha(255);

		}
		// System.out.println("Points needed: " + k);
	}

	@Override
	public void setFocus() {
		canvas.setFocus();
	}

	/**
	 * Returns canvas in use
	 * 
	 * @return
	 */
	public Widget getCanvas() {
		return canvas;
	}

	/**
	 * Calculate bit position (e.g. a range 0..512) from a cursor x pixel
	 * position on the screen.
	 * 
	 * @param x
	 *            cursor x position
	 * @return bit position the cursor is pointing to
	 */
	int getBitPositionFromPixelPosition(int x) {
		x -= VIEW_START_X_OFFSET;
		x /= periodWidth;
		return x + 1;
	}

	/**
	 * Calculate pixel x position (e.g. a range 0..512) from a bit position
	 * 
	 * @param x
	 *            bit position
	 * @return pixel position
	 */
	public int getPixelPositionFromBitPosition(int x) {
		x *= periodWidth;
		x += VIEW_START_X_OFFSET;
		return x;
	}

	/**
	 * Calculate pixel position (e.g. a range 0..512) from a cursor y position
	 * on the screen.
	 * 
	 * TODO: methods works only for first group. Needs to be extended to all
	 * groups.
	 * 
	 * @param y
	 *            cursor y position
	 * @return bit position the cursor is pointing to
	 */
	int getGroupFromPixelPosition(int y) {
		y -= VIEW_START_Y_OFFSET;
		// TODO: calculate heights etc.
		return 0;
	}

	/**
	 * Calculates start position in X direction for a group.
	 * 
	 * 
	 * @param groupId
	 * @return
	 */
	public int getXOffsetForGroup(int groupId) {
		return VIEW_START_X_OFFSET;
	}

	/**
	 * Calculates start position in Y direction for a group.
	 * 
	 * Honors the different heights of the other groups.
	 * 
	 * @param groupId
	 * @return
	 */
	public int getYOffsetForGroup(int groupId) {
		int ret = VIEW_START_Y_OFFSET;
		NavigationView nv = softTekApp.getNavigationView();

		for (int i = 0; i < groupId; i++) {
			if (nv.isGroupVisible(i))
				ret += getGroupHeight(i) + GROUP_Y_MARGIN;
		}
		return ret;
	}

	/**
	 * Calculates group height.
	 * 
	 * Honors number of channels etc.
	 * 
	 * @param groupId
	 * @return
	 */
	int getGroupHeight(int groupId) {
		return (periodHeight + periodMargin)
				* tek.getGroup(groupId).getNumChannels();
	}

	/**
	 * Redraw the view
	 */
	public void redrawView() {
		canvas.redraw();
	}

	/**
	 * Get time cursor object cursorId
	 * 
	 * @param cursorId
	 *            id/number of cursor
	 * @return
	 */
	TimeCursor getTimeCursor(int cursorId) {
		TimeCursor ret = null;
		if (cursorId < NUM_CURSORS)
			ret = timeCursor[cursorId];
		return ret;
	}

	public void scalePeriodWidth(int scale) {
		xScale = scale;
		periodWidth = DEFAULT_PERIOD_WIDTH / scale;
	}

	public int getPeriodWidth() {
		return periodWidth;
	}

	public GC getGc() {
		return gc;
	}

}
