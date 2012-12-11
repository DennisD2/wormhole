package de.spurtikus.softtek.tek;

import softtek.ConversionUtil;

/**
 * 
 * @author dennis
 * 
 *         Implementation of the "data block" object used in GPIB data
 *         transfers. See document "GPIB Comm Pack  1200C2", Tektronix, 1986.
 * 
 *         IEEE 728 BINARY implementation.
 * 
 *         A data block holds: address byte count data checksum
 * 
 *         the location field (bloc) identifies source/target of/for data block.
 * 
 */
public class BinaryDataBlock extends BaseDataBlock implements DataBlockParser {

	/**
	 * parses a buffer and fill a single block
	 */
	@Override
	public int parseBlock(byte data[], int offset) {
		int i = offset;
		byte b1, b2;

		// determination of type is done in super method
		i = super.parseBlock(data,offset);
		i++;

		// next two bytes is bbc value meaning byte count
		b1 = data[i++];
		b2 = data[i];

		// b1 = HI and b2 = LO byte of a 16 bit integer
		byteCount = b1 * 256 + b2;
		// checksum update
		checkSum = (byteCount % 256);
		i++;

		// next six bytes are the locb value
		// maximum allowed value is 1240 (?)
		location = 0L;
		// bloc is 24bits = 3 bytes in binary representation, MSB first
		for (int j = 0; j < 3; j++)
			location = 256 * location + ConversionUtil.hex2int(data[i++]);

		// each byte stands for itself in binary mode
		for (int j = 0; j < getByteCount() - CONTAINER_SIZE_BINARY + 1; j++) {
			payload[j] = data[i++];
			// checksum update
			checkSum = ((checkSum + payload[j]) % 256);
		}
		
		// set ByteCount to netto value
		byteCount -= CONTAINER_SIZE_BINARY;

		return i;
	}
}
