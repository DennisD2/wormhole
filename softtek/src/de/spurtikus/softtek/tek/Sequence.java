package de.spurtikus.softtek.tek;

import java.util.ArrayList;

public class Sequence {

	/**
	 * maximum number of sequence steps
	 */
	private final int SEQUENCE_SIZE = 14;
	/**
	 * size of one step in bytes
	 */
	private final int STEP_SIZE = 8;

	/**
	 * defines for timebases (assigned to steps)
	 */
	public static final int TIMEBASE_1 = 0;
	public static final int TIMEBASE_2 = 1;

	/**
	 * allowed sequence actions
	 */
	public static final int ACTION_WAIT_FOR = 0;
	public static final int ACTION_WAIT_FOR_NOT = 1;
	public static final int ACTION_TRIGGER_IF = 2;
	public static final int ACTION_TRIGGER_IF_NOT = 3;
	public static final int ACTION_RESET_IF = 4;
	public static final int ACTION_RESET_IF_NOT = 5;
	public static final int ACTION_JUMP_IF = 6;
	public static final int ACTION_JUMP_IF_NOT = 7;
	public static final int ACTION_DELAY = 8;

	/**
	 * list of steps in sequence
	 */
	ArrayList<SequenceStep> sequence;

	/**
	 * the memory image used to create sequence data from
	 */
	MemoryImage memoryImage = null;

	/**
	 * Build a secuence structure from a memory image.
	 * 
	 * @param image
	 *            memory image to use. This must be of setup image type.
	 */
	public void build(MemoryImage image) {
		SequenceStep step;

		sequence = new ArrayList<SequenceStep>();
		memoryImage = image;

		for (int i = 0; i < SEQUENCE_SIZE; i++) {
			step = new SequenceStep();
			int stepOffset = i * STEP_SIZE;
			// get event time base
			int timebase = readByte(stepOffset);
			// get step action
			int action = readByte(stepOffset + 1);
			// get jump_to_level
			int level = 0;
			if (action == ACTION_JUMP_IF || action == ACTION_JUMP_IF_NOT) {
				level = readByte(stepOffset + 2);
				if (level < 1 || level > SEQUENCE_SIZE)
					System.err.println("Bad value for jump_to_level (" + level
							+ ")");
			}
			int clocks = 0;
			if (action != ACTION_DELAY) {
				clocks = readByte(stepOffset + 3);
				if (clocks < 0 || clocks > 15)
					System.err.println("Bad value for clocks (" + clocks + ")");
			}
			int occur = 0;
			if (action == ACTION_WAIT_FOR || action == ACTION_WAIT_FOR_NOT
					|| action == ACTION_DELAY) {
				int o1 = readByte(stepOffset + 4);
				int o2 = readByte(stepOffset + 5);
				int o3 = readByte(stepOffset + 6);
				occur = convertOccurBytesToInt(o1, o2, o3);
				if (occur < 1 || occur > 9999)
					System.err.println("Bad value for occur value (" + occur
							+ ")");
			}
			boolean withStorage;
			if (readByte(stepOffset + 7) == 0)
				withStorage = false;
			else
				withStorage = true;

			// set all fields in step
			step.setEventTimebase(timebase);
			step.setJumpToLevel(level);
			step.setNumberClocks(clocks);
			step.setStepAction(action);
			step.setToOccur(occur);
			step.setWithStorage(withStorage);

			// read 18 bytes containing the prlword bits for this step
			// start with prlword[1], because [0] is global event value
			for (int j = 0; j < 72; j++) {
				int b = readPrlBit(i + 1, j);
				step.setPrlBit(j, b);
			}
			// add it at correct position
			sequence.add(i, step);
		}
	}

	/**
	 * dump out the complete sequence
	 * 
	 * @param seqDepth
	 *            number of valid sequences. Only this number will be printed.
	 *            If 0 is entered, the complete sequence (including not valid
	 *            steps) are printed out.
	 */
	public void dumpSequence(int seqDepth) {
		// set to maximum size of seqDepth==0
		if (seqDepth == 0)
			seqDepth = SEQUENCE_SIZE;

		// dump out step by step
		for (int i = 0; i < seqDepth; i++) {
			SequenceStep step = sequence.get(i);

			String str = "Sequence step " + i + ": ";
			if (step.getEventTimebase() == TIMEBASE_1)
				str += "Timebase 1";
			else
				str += "Timebase 2";
			System.out.print(str + ", ");

			str = "";
			int action = step.getStepAction();
			switch (action) {
			case ACTION_WAIT_FOR:
				str = "wait for";
				break;
			case ACTION_WAIT_FOR_NOT:
				str = "wait for not";
				break;
			case ACTION_TRIGGER_IF:
				str = "trigger if";
				break;
			case ACTION_TRIGGER_IF_NOT:
				str = "trigger if not";
				break;
			case ACTION_RESET_IF:
				str = "reset if";
				break;
			case ACTION_RESET_IF_NOT:
				str = "reset if not";
				break;
			case ACTION_JUMP_IF:
				str = "jump if";
				break;
			case ACTION_JUMP_IF_NOT:
				str = "jump if not";
				break;
			case ACTION_DELAY:
				str = "delay";
				break;
			default:
				str = "unknown action value (" + action + ")";
				break;
			}
			System.out.print("action: " + str);

			str = ": ";
			for (int j = 0; j < 72; j++) {
				switch (step.getPrlWord(j)) {
				case 0:
					str += "0";
					break;
				case 1:
					str += "1";
					break;
				case 2:
					str += "G";
					break;
				case 3:
					str += "X";
					break;
				}
			}
			System.out.println(str + " ,");

			if (action == ACTION_JUMP_IF || action == ACTION_JUMP_IF_NOT) {
				System.out.print("     to level " + step.getJumpToLevel());
			}
			if (action == ACTION_WAIT_FOR || action == ACTION_WAIT_FOR_NOT
					|| action == ACTION_DELAY) {
			System.out.print("     to occur " + step.getToOccur() + " times");
			}
			if (action != ACTION_DELAY) {
				System.out.print(", filter: " + step.getNumberClocks());
			}
			System.out.println(", with Storage: " + step.isWithStorage());
		}

	}

	/**
	 * Convert 4 BCD-digits packed into three bytes back to their integer
	 * values. Last byte (o3) is always 0. o2 contains two four-bit values, o3
	 * the other two four bit values. Example: o2=0x12, o3=0x45, o1=0x00 Then
	 * v1=0x01 v2=0x02 v3=0x04 v4=0x05, resulting integer is 1245 .
	 * 
	 * @param o1
	 * @param o2
	 * @param o3
	 * @return value as described.
	 */
	private int convertOccurBytesToInt(int o1, int o2, int o3) {
		//System.out.println("o1: " + o1 + ", o2: " + o2 + ", o3: " + o3);
		int v4 = (o2 & 0xf0) >> 4; // 10
		int v3 = (o2 & 0x0f); // 1
		int v2 = (o1 & 0xf0) >> 4;
		int v1 = (o1 & 0x0f);
		//System.out.println("v4(10): " + v4 + ", v3(1): " + v3 + ", v2(1000): "
		//		+ v2 + ", v1(100): " + v1);
		//System.out.println("v=" + (v4 * 10 + v3 * 1 + v2 * 1000 + v1 * 100));
		return v2 * 1000 + v1 * 100 + v4 * 10 + v3  ;
	}

	/**
	 * read byte in seqval area of setup memory
	 * 
	 * @param address
	 *            byte address to use
	 * @return byte value at position
	 */
	int readByte(int address) {
		int position = Tek1241RawDevice.OF_TRIG_SEQVALUE + address;
		int b = memoryImage.getByte(position);
		return b;
	}

	/**
	 * read byte in trwrval area of setup memory
	 * 
	 * @param address
	 *            byte address to use
	 * @return byte value at position
	 */
	int readPrlWordByte(int address) {
		int position = Tek1241RawDevice.OF_TRIG_TRIGWRVAL + address;
		int b = memoryImage.getByte(position);
		return b;
	}

	/**
	 * Read bit value for bit at position bitPos in prlword value for step
	 * number step.
	 * 
	 * A prlword contains a trigger value to be used in a sequence step. for
	 * each channel, there is a bit value and a mask value, so the array has
	 * size 144=2*72 bit = 2*9 Bytes = 18 Bytes. The bit value together with the
	 * mask value determined the trigger condition for that bit as described in
	 * documentation page A-13 and A-14:
	 * 
	 * bitVal + maskVal --> meaning for trigger condition
	 * 
	 * 0 + 0 --> 0
	 * 
	 * 0 + 1 --> 1
	 * 
	 * 1 + 0 --> Glitch
	 * 
	 * 1 + 1 --> X (don't care)
	 * 
	 * 
	 * @param step
	 *            - number of step for that prlword value will be used
	 * @param bitPos
	 *            - bit position in prlword array.
	 * @return
	 */
	private int readPrlBit(int step, int bitPos) {
		// get position of byte that contains the bit
		int bytePos = bitPos / 8;
		// get position of the bit in the byte
		int bitPosInByte = bitPos % 8;
		int byteVal = memoryImage.getByte(Tek1241RawDevice.OF_TRIG_TRIGWRVAL
				+ step * 18 + bytePos);
		// extract bit value for the bit to use
		int bitVal = byteVal & (1 << bitPosInByte);

		// now get the mask bit (upper 72 bit of 144 bit data)
		bitPos += 72;
		// get position of byte that contains the bit
		bytePos = bitPos / 8;
		// get position of the bit in the byte
		bitPosInByte = bitPos % 8;
		byteVal = memoryImage.getByte(Tek1241RawDevice.OF_TRIG_TRIGWRVAL + step
				* 18 + bytePos);
		// extract mask bit value for the bit to use
		int maskVal = byteVal & (1 << bitPosInByte);

		// System.out.println("readPrlBit() (" + bitVal + "," + maskVal + ")");
		if (bitVal > 0)
			bitVal = 1;

		int ret;
		if (maskVal == 0)
			ret = bitVal;
		else {
			if (bitVal == 0)
				ret = 2;
			else
				ret = 3;
		}
		return ret;
	}

}
