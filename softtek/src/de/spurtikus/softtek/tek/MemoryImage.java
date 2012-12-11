package de.spurtikus.softtek.tek;

import static org.junit.Assert.assertEquals;

import java.util.LinkedList;
import java.util.List;

/**
 * Class MemoryImage
 * 
 * This class encapsulated the notion of a memory image of the Tek 1240/1241.
 * The memory image is a large data structure that can be read and written
 * from/to the Tek device. Several different memory images do exist: - reference
 * memory - aquisition memory - setup data The Tek device creates a memory image
 * from its internal memory and sends it via GPIB using datablock objects.
 * 
 * This class allows to reconstruct ("build") a complete and valid memory image
 * from a list of datablocks that have been received from a Tek device.
 * 
 * @see See also BaseDataBlock class and its children classes for the
 *      implementation of datablocks.
 * 
 * @author dennis
 * 
 */
public class MemoryImage {
	/**
	 * maximum size of an memory image Use a value that is for sure large enough
	 * for everything that might come from the Tek 1240/1241.
	 */
	final int MAX_MEM_IMAGE_SIZE = 50000;

	/**
	 * Array for all bytes in the memory image. Note that Java has no "byte"
	 * type and no 8-Bit-sized int, so we use just a standard int for all bytes.
	 * To use these values, they have to and-ed with 0xff in some cases.
	 */
	private int bytes[];

	/**
	 * size of the memory image
	 */
	private int size = 0;

	/**
	 * during build of a memory image, this is the position where the last write
	 * to the bytes array was done PLUS 1. I.e., for a new block, this is the
	 * start offset in the byte array to use.
	 */
	private int lastPos = 0;

	/**
	 * if desired, the original string can be kept
	 */
	private byte input[] = null;
	private int inputSize = 0;

	private boolean keepInput = false;

	/**
	 * Constructor.
	 */
	public MemoryImage() {
		initialize(false);
	}

	/**
	 * Constructor.
	 */
	public MemoryImage(boolean keepInput) {
		initialize(keepInput);
	}

	private void initialize(boolean keepInput) {
		this.keepInput = keepInput;

		// create a byte array large enough for all possible dumps from Tek
		// 1240/1241
		bytes = new int[MAX_MEM_IMAGE_SIZE];
		if (keepInput)
			input = new byte[2 * MAX_MEM_IMAGE_SIZE];
	}

	/**
	 * Reset a memory image.
	 * 
	 * This erases old values in the memory image object and allows to re-build
	 * the memory image from data blocks. Note: the bytes array is not cleared.
	 * Only the control variables are resetted.
	 */
	public void init() {
		size = 0;
		lastPos = 0;
	}

	/**
	 * Build a memory image from a a byte buffer
	 * 
	 * parses the byte buffer and creates a linked list of data block objects.
	 * These objects then are fed into the memoryImage object.
	 * 
	 * @param buf
	 *            the buffer to parse.
	 * @param size
	 *            size of buffer (number of bytes).
	 */
	public void build(byte[] buf, int size) {
		int cur_ptr = 0;
		int last_ptr = 0;
		int num_block = 0;
		int total_bytes = 0;
		LinkedList<BaseDataBlock> dbl = new LinkedList<BaseDataBlock>();

		// init memory image to ensure a clean build
		init();
		System.out.println("Buffer size: " + size);
		if (keepInput)
			inputSize = size;
		while (cur_ptr < size) {
			// get next byte
			byte b = buf[cur_ptr];
			if (keepInput)
				input[cur_ptr] = buf[cur_ptr];
			cur_ptr++;
			// System.out.print((char) b);
			if (b == BaseDataBlock.BYTE_COMMA
					|| b == BaseDataBlock.BYTE_SEMICOLON) {
				// if we got a comma, we read in one complete data block
				// System.out.print("(Block " + num_block + " starts at "
				// + last_ptr + ")");
				// System.out.println();
				BaseDataBlock block = new AsciiDataBlock();
				// parse in block from last point read in (last_ptr) to current
				// position
				block.parseBlock(buf, last_ptr);
				last_ptr = cur_ptr;
				// assertEquals("Checksum should be ok", true,
				// block.isCheckSumValid());
				// update statistics
				num_block++;
				total_bytes += block.getByteCount();
				dbl.add(block);
			}
		}
		System.out.println(num_block + " blocks read in with " + total_bytes
				+ " bytes in total.");

		// assertEquals(
		// "number of blocks read in and blocks in list should be equal",
		// num_block, dbl.size());

		build(dbl);

		System.out.println("Memory image size " + getSize());

		// assertEquals(
		// "sum of block contents bytes and resulting memory image bytes should be equal",
		// total_bytes, getSize());
	}

	/**
	 * Build a memory image from a list of data blocks.
	 * 
	 * It is assumed that the datablocks are corrected ordered in list. before
	 * any data is written to the memory image, the memory image is resetted by
	 * calling its init() method.
	 * 
	 * @param dbl
	 *            list of datablocks
	 */
	public void build(List<BaseDataBlock> dbl) {
		// init memory image to ensure a clean build
		init();
		for (BaseDataBlock block : dbl) {
			int offset = getLastPos();
			add(block, offset);
		}
	}

	/**
	 * Add a single datablock to a (partial) memory image. This method is used
	 * to build the image step by step and is called by build().
	 * 
	 * @param block
	 *            the block to add
	 * @param offset
	 *            the offset in the memory image where the blocks bytes will be
	 *            added.
	 */
	void add(BaseDataBlock block, int offset) {
		int i;
		for (i = 0; i < block.getByteCount(); i++) {
			bytes[i + offset] = block.getByte(i);
		}
		size += block.getByteCount();
		lastPos += block.getByteCount();
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public int getLastPos() {
		return lastPos;
	}

	public void setLastPos(int lastPos) {
		this.lastPos = lastPos;
	}

	public int getByte(int pos) {
		return bytes[pos] & 0xff;
	}

	public byte[] getInput() {
		return input;
	}

	public int getInputSize() {
		return inputSize;
	}
}
