package de.spurtikus.softtek.decorators;

public class I2CDecoration extends Decoration {

	/**
	 * I2C line events
	 */
	public static final int EV_START = 1;
	public static final int EV_STOP = 2;
	public static final int EV_ACK = 3;
	/**
	 * "synthetic" events
	 */
	public static final int EV_BITVAL = 4;
	public static final int EV_BYTEVAL = 5;
	public static final int EV_MISSING_ACK = 10;

	int bitNumber;

	public I2CDecoration() {
		super("I2C", -1, -1, -1, "default", (String[]) null);
		bitNumber=-1;
	}

	public int getBitNumber() {
		return bitNumber;
	}

	public void setBitNumber(int bitNumber) {
		this.bitNumber = bitNumber;
	}

	public void print() {
		System.out.println("Event : " + Event2Message(type) );
		if (bitNumber!=-1)
			System.out.println(" bitNumber: " + bitNumber);
		super.print();
	}

	public static String Event2Message(int event) {
		String ret = "Unknown event";
		switch (event) {
		case 1:
			ret = "START";
			break;
		case 2:
			ret = "STOP";
			break;
		case 3:
			ret = "ACK";
			break;
		case 4:
			ret = "BIT";
			break;
		case 5:
			ret = "BYTE";
			break;
		}
		return ret;
	}

}
