package de.spurtikus.softtek.decorators;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Drawable;
import org.eclipse.swt.graphics.GC;

import softtek.ConversionUtil;

import de.spurtikus.softtek.ApplicationSingleton;
import de.spurtikus.softtek.tek.Tek1241Device;

public class I2CDecorator implements TimeLineDecorator {

	/** data for application is in singleton */
	private ApplicationSingleton softTekApp = ApplicationSingleton
			.getInstance();

	private static String clazz = "I2C";

	/**
	 * I2C pod id
	 */
	int podId = 0;

	/**
	 * I2C clock line (pod pin id)
	 */
	int CLOCK = 0;
	/**
	 * ISC data line pod pin id
	 */
	int DATA = 1;

	/**
	 * Values for HI and LO
	 */
	final int LO = 0;
	final int HI = 1;

	/**
	 * simple position descriptor
	 * */
	class Position {
		public int begin;
		public int end;
	}

	/**
	 * array with clock bits
	 */
	int clock[];
	/**
	 * array with data bits
	 */
	int data[];

	/**
	 * pointer into arrays
	 */
	int clockPtr;

	int lastPtr = 0;

	/**
	 * list of decoration items
	 */
	ArrayList<Decoration> decorations = null;

	/**
	 * the tek device to sue
	 */
	Tek1241Device tek = null;

	private Color green = null;

	@Override
	public void init(Tek1241Device tekDevice) {
		tek = tekDevice;
		decorations = new ArrayList<Decoration>();
	}

	@Override
	public void decorate() {

		clock = tek.getTimelineBits(0, 0);
		data = tek.getTimelineBits(0, 1);

		boolean end = false;
		clockPtr = 0;
		int d, c;
		int hiCount = 0, loCount = 0;
		int outBit = 0;
		int bitCounter = 0;
		int outByte = 0;
		boolean waitForAck = false;

		// for (int i = 0; i < clock.length; i++)
		// System.out
		// .println(i + " clock: " + clock[i] + ", data: " + data[i]);

		while (!end) {

			d = nextDataBit();
			c = nextClockBit();
			// System.out.println(clockPtr+": "+c+" "+d);
			clockPtr++;

			if (c == HI) {
				hiCount++;
				loCount = 0;

				if (hiCount == 1) {
					outBit = d;
				} else {
					if (d != outBit) {
						if (outBit == HI && d == LO) {
							createEvent(I2CDecoration.EV_START, lastPtr,
									clockPtr, -1);
							bitCounter = 0;
							outByte = 0;
							hiCount = 0;
							overreadHiClock();
						} else {
							createEvent(I2CDecoration.EV_STOP, lastPtr,
									clockPtr, -1);
							overreadHiClock();
						}
						hiCount = 0;
					}
				}
			} else {
				loCount++;
				if (hiCount > 0) {
					createEvent(I2CDecoration.EV_BITVAL, lastPtr, clockPtr,
							outBit, bitCounter);
					if (bitCounter < 8) {
						outByte = 2 * outByte + outBit;
						bitCounter++;
					}
					if (bitCounter == 8) {
						createEvent(I2CDecoration.EV_BYTEVAL, lastPtr,
								clockPtr, outByte);
						waitForAck = true;
						bitCounter = 0;
						outByte = 0;
					}
					hiCount = 0;
				}
			}

			if (waitForAck) {
				while ((c = nextClockBit()) == LO)
					clockPtr++;
				// clockPtr--;
				// createEvent("clockptr=" + clockPtr, lastPtr, clockPtr,
				// clockPtr);
				d = nextDataBit();
				if (d == LO) {
					createEvent(I2CDecoration.EV_ACK, lastPtr, clockPtr, -1);
					// overread complete HI clock cycle
					overreadHiClock();
					waitForAck = false;
				} else {
					createEvent(I2CDecoration.EV_MISSING_ACK, lastPtr,
							clockPtr, clockPtr);
					clockPtr++;
					waitForAck = false;
				}

			}

			if (clockPtr >= clock.length)
				end = true;
		}
	}

	/**
	 * Emit I2C event as a decoration
	 * 
	 * @param string
	 * @param cPtr
	 * 
	 */
	private void createEvent(int type, int lPtr, int cPtr, int value) {
		System.out.println(">>>>> >>>>> I2C "
				+ I2CDecoration.Event2Message(type) + " (position: " + lPtr
				+ " to " + (cPtr - 1) + ")");
		I2CDecoration d = new I2CDecoration();
		d.setType(type);
		d.setBegin(lPtr);
		d.setEnd(cPtr - 1);

		d.setValue("" + value);

		lastPtr = cPtr;
		decorations.add(d);
	}

	private void createEvent(int type, int lPtr, int cPtr, int value,
			int bitCounter) {
//		System.out.println(">>>>> >>>>> I2C "
//				+ I2CDecoration.Event2Message(type) + " (position: " + lPtr
//				+ " to " + (cPtr - 1) + ")");
		I2CDecoration d = new I2CDecoration();
		d.setType(type);
		d.setBegin(lPtr);
		d.setEnd(cPtr - 1);

		d.setValue("" + value);
		d.setBitNumber(bitCounter);

		lastPtr = cPtr;
		decorations.add(d);
	}

	/**
	 * Get clock bit value at next position (denoted by clockPtr)
	 * 
	 * @return
	 */
	private int nextClockBit() {
		return clock[clockPtr];
	}

	/**
	 * Get data bit value at next position (denoted by clockPtr)
	 * 
	 * @return
	 */
	private int nextDataBit() {
		return data[clockPtr];
	}

	private void overreadHiClock() {
		int lastData, currentData;
		while (clockPtr < clock.length && clock[clockPtr] == HI) {
			lastData = data[clockPtr - 1];
			currentData = data[clockPtr];
			if (lastData == LO && currentData == HI) {
				createEvent(I2CDecoration.EV_START, lastPtr, clockPtr, -1);
				return;
			}
			clockPtr++;
		}

	}

	public void print() {
		for (Decoration d : decorations)
			d.print();
	}

	@Override
	public void setClazz(String clazz) {
		I2CDecorator.clazz = clazz;

	}

	@Override
	public String getClazz() {
		return I2CDecorator.clazz;
	}

	@Override
	public void draw() {
		int groupId = 0;
		int xstart = softTekApp.getView().getXOffsetForGroup(groupId);
		int ystart = softTekApp.getView().getYOffsetForGroup(groupId);

		// create own GC
		// GC gc = new GC( (Drawable) softTekApp.getView().getCanvas());
		GC gc = softTekApp.getView().getGc();
		green = softTekApp.getView().getCanvas().getDisplay()
				.getSystemColor(SWT.COLOR_GREEN);
		Color white = softTekApp.getView().getCanvas().getDisplay()
				.getSystemColor(SWT.COLOR_WHITE);
		Color black = softTekApp.getView().getCanvas().getDisplay()
				.getSystemColor(SWT.COLOR_BLACK);
		Color blue = softTekApp.getView().getCanvas().getDisplay()
				.getSystemColor(SWT.COLOR_BLUE);
		Color cyan = softTekApp.getView().getCanvas().getDisplay()
				.getSystemColor(SWT.COLOR_CYAN);
		Color areaColor = green;
		int lineWidth = 10;
		int lineStyle = SWT.LINE_SOLID;
		int areaAlpha = 50;

		Color saveColor = gc.getForeground();
		int saveLineWidth = gc.getLineWidth();
		int saveLineSytle = gc.getLineStyle();
		int saveAlpha = gc.getAlpha();

		for (Decoration d : decorations) {
			// set values for glitch area
			gc.setLineStyle(lineStyle);
			gc.setLineWidth(lineWidth);
			gc.setForeground(black);
			gc.setBackground(areaColor);
			gc.setAlpha(areaAlpha);

			int x1 = softTekApp.getView().getPixelPositionFromBitPosition(
					d.getBegin());
			int w;
			if (d.getEnd() - d.getBegin() >= 5)
				w = softTekApp.getView().getPixelPositionFromBitPosition(
						d.getEnd())
						- x1;
			else
				w = softTekApp.getView().getPixelPositionFromBitPosition(
						d.getEnd() + 5)
						- x1;

			int h = softTekApp.getView().DEFAULT_PERIOD_HEIGHT;
			int y1 = ystart - h;
			switch (d.getType()) {
			case I2CDecoration.EV_START:
			case I2CDecoration.EV_STOP:
				gc.setBackground(blue);
				gc.setForeground(white);
				break;
			case I2CDecoration.EV_BYTEVAL:
				gc.setBackground(white);
				y1 += 15;
				break;
			case I2CDecoration.EV_BITVAL:
				break;
			case I2CDecoration.EV_ACK:
				gc.setBackground(cyan);
				//y1 -= 15;
				break;
			default:
			}
			gc.fillRectangle(x1, y1, w, h);

			// dump out text for the decoration
			String s = I2CDecoration.Event2Message(d.getType());
			// bit number, if available
			if (((I2CDecoration)d).getBitNumber()!=-1)
				s = s +" "+((I2CDecoration)d).getBitNumber();
			// value, if available
			if (!d.getValue().equals("-1")) {
				// Bytes are printed as 0xnn, bits as 0 or 1
				if (d.getType()==I2CDecoration.EV_BYTEVAL)
					s = s +": "+ ConversionUtil.byte2hexString(Integer.parseInt(d.getValue()));
				else
					s = s +": "+ d.getValue();
			}
			gc.setAlpha(150);
			gc.drawText(s, x1 + 5, y1);
		}

		// set back values
		gc.setForeground(saveColor);
		gc.setLineStyle(saveLineSytle);
		gc.setLineWidth(saveLineWidth);
		gc.setAlpha(255);

	}
}
