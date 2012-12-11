package de.spurtikus.softtek.tek;

public interface DataBlockParser {

	/**
	 * parses a buffer and fill a single block
	 */
	public int parseBlock(byte data[], int offset);

}
