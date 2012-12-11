package de.spurtikus.softtek.tek;

import softtek.ConversionUtil;

/**
 * 
 * @author dennis
 * 
 *         Implementation of the "data block" object used in GPIB data
 *         transfers. See document "GPIB Comm Pack  1200C2", Tektronix, 1986.
 * 
 *         ASCII-HEX implementation.
 * 
 *         A data block holds: address byte count data checksum
 * 
 *         the location field (aloc) identifies source/target of/for data block.
 * 
 */
public class AsciiDataBlock extends BaseDataBlock implements DataBlockParser {

	/**
	 * parses a buffer and fill a single block The implementation of parseBlock
	 * is spread over sub-classes. This part of the implementation delegates
	 * determination of type of the data block to the super method and does the
	 * rest. it returns the byte position of the last checksum byte.
	 */
	@Override
	public int parseBlock(byte data[], int offset) {
		int i = offset;
		byte b1, b2;

		// determination of type is done in super method
		i = super.parseBlock(data, offset);
		i++;

		// next two bytes is abc value meaning byte count
		b1 = data[i++];
		b2 = data[i];
		// b1, b2 are ASCII HEX digits
		// example b1='5' and b2='1' --> hex number = 0x51
		byteCount = ConversionUtil.hex2int(b1) * 16
				+ ConversionUtil.hex2int(b2);
		// checksum update
		checkSum = (byteCount % 256);
		// abc = byte count divided by two, so multiply
		// byteCount *= 2;
		i++;

		// next six bytes are the loca value
		// maximum allowed value is 1240 (?)
		location = 0L;
		// loc is six bytes where each byte represents an ASCII HEX value
		// example 000BA4 = 0xBA4
		for (int j = 0; j < 6; j += 2) {
			// get two bytes at once
			int wordVal = ConversionUtil.hex2int(data[i++]) * 16
					+ ConversionUtil.hex2int(data[i++]);
			// shift old location values by two hex digits and add new two
			// digits
			location = 256 * location + wordVal;
			// checksum update
			checkSum = ((checkSum + wordVal) % 256);
		}

		// following bytes are payload data bytes.
		// Each byte is formed from two ASCII HEX digits
		// example 'F' 'F' becomes 0xFF
		for (int j = 0, k = 0; j < 2 * getByteCount() - CONTAINER_SIZE_ASCII
				+ 2; j += 2, k++) {
			// System.out.println("block["+j+"] = "+data[i++]+","+data[i++]);
			payload[k] = (byte) (ConversionUtil.hex2int(data[i++]) * 16 + ConversionUtil
					.hex2int(data[i++]));
			// checksum update
			checkSum = ((checkSum + payload[k]) % 256);
		}

		// now the checksum value
		// two ASCII HEX digits representing the two's complement of the
		// modulo 256 sum of the
		// hex digit pairs in <abc>, <aloc> and payload data
		checkSumTarget = (ConversionUtil.hex2int(data[i++]) * 16 + ConversionUtil
				.hex2int(data[i++]));
		// System.out.println("target: "+ ((~checkSumTarget&0xff)+1));

		// calculate target value from checksum to compare against target value
		// read from data
		int cs = ((~checkSum & 0xff) + 1);
		// System.out.println("cs: "+ cs);
		checkSum = cs;

		// set ByteCount to netto value. Attention:
		// - byte count value in data is 2* real byte count (2 per hex coding of a abyte)
		// - external byte count is real byte count 
		//byteCount = 2 * byteCount -(CONTAINER_SIZE_ASCII-2)-1;
		byteCount = byteCount -(CONTAINER_SIZE_ASCII/2-1);
		return i;
	}


}
