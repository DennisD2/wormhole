package de.spurtikus.softtek.tek;

/**
 * 
 * @author dennis
 * 
 *         Implementation of the "data block" object used in GPIB data
 *         transfers. See document "GPIB Comm Pack  1200C2", Tektronix, 1986.
 * 
 *         A data block has a type (ASCII-HEX, BINARY, IEEE 728)
 * 
 *         A data block holds: address byte count data checksum
 * 
 *         the location field (aloc, bloc) identifies source/target of/for data
 *         block.
 * 
 */
public class BaseDataBlock implements DataBlockParser {

	/**
	 * possible types of data blocks
	 */
	public static final int TYPE_UNKNOWN = 0;
	public static final int TYPE_ASCII_HEX = 1;
	public static final int TYPE_BINARY = 2;
	public static final int TYPE_IEEE_728 = 3;

	/**
	 * The maximum size allowed for a single data block is 0x61
	 */
	static final int BLOCK_MAXSIZE = 0x61;

	/**
	 * some important characters
	 */
	public static final byte BYTE_HASH = '#';
	public static final byte BYTE_B = 'B';
	public static final byte BYTE_H = 'H';
	public static final byte BYTE_PERCENT = '%';
	public static final byte BYTE_COMMA = ',';
	public static final byte BYTE_SEMICOLON = ';';
	/**
	 * type identifier for data block
	 */
	protected int type; // --> enum

	/**
	 * number of bytes in data block The maximum size allowed is 0x61 For ASCII
	 * HEX type: This is the sum of
	 * sizeof(data)+sizeof(<abc>)+sizeof(<aloc>)+sizeof(<acs>) where
	 * sizeof(<abc>)=2, sizeof(<aloc>)=6, sizeof(<acs>)=2.
	 * 
	 * For binary type: This is the sum of
	 * sizeof(data)+sizeof(<bbc>)+sizeof(<bloc>)+sizeof(<bcs>) where
	 * sizeof(<bbc>)=2, sizeof(<bloc>)=3, sizeof(<bcs>)=1.
	 */
	protected int byteCount;

	/**
	 * container overhead size (bytes besides payload)
	 */
	protected static final int CONTAINER_SIZE_ASCII = 2 + 6 + 2;
	protected static final int CONTAINER_SIZE_BINARY = 2 + 3 + 1;

	/**
	 * location
	 */
	protected long location;

	/**
	 * checksum calculated and checksum read from block
	 */
	protected int checkSum;
	protected int checkSumTarget;

	/**
	 * byte array holding the byte values
	 */
	protected int payload[];

	/**
	 * create a new data block
	 * 
	 * @param type
	 */
	public BaseDataBlock() {
		this.type = TYPE_UNKNOWN;
		payload = new int[BLOCK_MAXSIZE];
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getByteCount() {
		return byteCount;
	}

	public void setByteCount(int byteCount) {
		this.byteCount = byteCount;
	}

	public int[] getData() {
		return payload;
	}

	public void setData(int[] data) {
		this.payload = data;
	}

	public long getLocation() {
		return location;
	}

	public void setLocation(long location) {
		this.location = location;
	}

	public boolean isCheckSumValid() {
		int test = (checkSum-checkSumTarget)%256;
		if (test!=0) {
			System.out.println("Check sum error. Calculated: " + checkSum
					+ ", but read value to match: " + checkSumTarget);
		}
		return (test==0);
	}

	public int getByte(int i) {
		return payload[i];
	}

	/**
	 * parses a byte buffer. The implementation of parseBlock is spread over
	 * sub-classes. This part of the implementation determines the type of the
	 * data block it returns the byte position of the last type byte.
	 */
	@Override
	public int parseBlock(byte[] data, int offset) {
		int i = offset;
		int dataSize = data.length;
		byte b = '?';

		// each block starts with a hash or a percent
		while (i < dataSize && b != BYTE_HASH && b != BYTE_PERCENT) {
			b = data[i];
			i++;
		}

		if (b == BYTE_PERCENT) {
			type = TYPE_IEEE_728;
		} else {
			// overread hash
			// i++;
			switch (data[i]) {
			case BYTE_H:
				type = TYPE_ASCII_HEX;
				break;
			case BYTE_B:
				type = TYPE_BINARY;
				break;
			default:
				type = TYPE_UNKNOWN;
			}
		}
		return i;
	}

	// @Override
	// public int parseBlock(byte[] data) {
	// return parseBlock(data,0);
	// }

}
