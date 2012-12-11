package de.spurtikus.softtek.tek;

/**
 * 
 * @author dennis
 * 
 *         Implementation of the "data block" object used in GPIB data
 *         transfers. See document "GPIB Comm Pack  1200C2", Tektronix, 1986.
 * 
 *         Tek-owned BINARY implementation.
 * 
 *         A data block holds: address byte count data checksum
 * 
 *         the location field (aloc, bloc) identifies source/target of/for data
 *         block.
 * 
 */
public class TekBinaryDataBlock extends BinaryDataBlock implements
		DataBlockParser {

	/**
	 * parses a buffer and fill a single block
	 */
	@Override
	public int parseBlock(byte data[], int offset) {
		int i = offset;

		// determination of type is done in super method
		i = super.parseBlock(data,offset);

		// now read the checksum value
		// single byte value, modulo 256 sum of the
		// hex digit pairs in <bbc>, <bloc> and payload data
		checkSumTarget = data[i++];

		return i;
	}

}
