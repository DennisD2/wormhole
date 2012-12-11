package de.spurtikus.softtek.tek;

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
 *         the location field (bloc) identifies source/target of/for data
 *         block.
 * 
 */
public class Ieee728DataBlock extends BinaryDataBlock implements
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
		// hex digit pairs in <bloc> and payload data
		
		// TODO: for IEEE728, the checksum differs from TekBinary format in that 
		// bbc is NOT used for checksum calculation
		// this is currently handled wrong below
		System.out.println("FixMe: IEEE728 checksum calculation");
		checkSumTarget = data[i++];

		return i;
	}

}
