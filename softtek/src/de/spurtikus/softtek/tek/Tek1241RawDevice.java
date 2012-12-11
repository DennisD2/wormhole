package de.spurtikus.softtek.tek;

import java.lang.reflect.Field;
import java.text.DecimalFormat;

import softtek.AbstractRawDevice;
import softtek.ConversionUtil;

public class Tek1241RawDevice extends AbstractRawDevice {

	/**
	 * maximum number of pods in device
	 */
	private final int MAX_POD_COUNT = 8;

	/**
	 * maximum number of acquisition/memory cards in device Equals number of
	 * slots of device.
	 */
	public static final int MAX_MEMCARD_COUNT = 8;

	/**
	 * setup image offsets
	 */
	private static final int OF_TRIG_SEQDEPTH = 14;
	private static final int OF_TRIG_SEQCMD = 15;
	private static final int OF_TRIG_SEQSTORE = 16;
	public static final int OF_TRIG_SEQVALUE = 17;
	public static final int OF_TRIG_TRIGWRVAL = 129;
	private final int OF_GRP_CARDSELECT = 709;
	private final int OF_MEM_MEMSTAT = 430;
	public static final int OF_GRP_GROUPLAYOUT = 710;
	public static final int OF_GRP_CHANNELGROUP = 830;

	/**
	 * acquisition image offsets
	 */
	private final int OFM_RAWCOR1 = 0;
	private final int OFM_RAWCOR2 = 257;
	private final int OFM_THRESHOLD = 443;
	private final int OFM_RAWTBACTIVE = 487;
	private final int OFM_RAWTB2TYPE = 507;
	private final int OFM_RAWPODLEN = 514;
	private final int OFM_RAWOLDEST = 530;
	private final int OFM_RAWYOUNGEST = 546;
	private final int OFM_RAWEMPTY = 562;
	private final int OFM_RAWTPI1 = 570;
	private final int OFM_RAWTPI2 = 572;
	private final int OFM_RAWTRIG = 574;
	private final int OFM_RAWC1POD = 575;
	private final int OFM_RAWC2POD = 576;
	private final int OFM_RAWLAST = 577;
	private final int OFM_RAWCTRUNITS = 578;
	private final int OFM_RAWGLITCHES = 579;
	private final int OFM_RAWTB1TYPE = 580;
	private final int OFM_RAWTB1ASYNCH = 581;
	private final int OFM_RAWTIMEVALID = 582;
	private final int OFM_RAWD9 = 583;
	private final int OFM_RAWD18 = 585;
	private final int OFM_RAWTB = 587;
	private final int OFM_RAWCTR = 595;
	private final int OFM_RAWLENGTH = 600;
	private final int OFM_RAWPOSTFIG = 602;
	private final int OFM_RAWMISC = 604;
	private final int OFM_RAWDATA = 614;

	private final int OFM_RAWOPLEVEL = 428;
	private final int OFM_RAWTPGPAT = 429;
	private final int OFM_RAWPOLARITY = 471;
	private final int OFM_RAWTB1CLOCK = 491;

	/**
	 * memstat card attribute values
	 */
	public static final int C_MEM_CARD_MISSING = 0;
	public static final int C_MEM_CARD_9_UNCHAINED = 1;
	public static final int C_MEM_CARD_18_UNCHAINED = 2;
	public static final int C_MEM_CARD_CHAINED = 3;

	public final int C_RAW_CTRUNITS_EVENT = 0;
	public final int C_RAW_CTRUNITS_TIME = 1;
	public final int C_RAW_CTRUNITS_NA = 2;

	public final int C_RAW_CHEAD_TB1 = 0;
	public final int C_RAW_CHEAD_TB2 = 1;
	public final int C_RAW_CFOLLOWER = 2;
	public final int C_RAW_CNOMEM = 0xff;

	public final int C_RAW_NOT_MODIFIED = 0;
	public final int C_RAW_MODIFIED = 1;

	public final int C_TB_TIMEBASE1 = 1;
	public final int C_TB_TIMEBASE2 = 0;

	public final int C_TB_ASYNC = 0;
	public final int C_TB_SYNC = 1;
	public final int C_TB_DEMUX = 0; // tb2 only

	/**
	 * Return values for getRawTBActive
	 */
	public final int C_TB_ACTIVE_TB1 = 0;
	public final int C_TB_ACTIVE_TB2 = 1;
	public final int C_TB_ACTIVE_BOTH = 2;

	/**
	 * clock period constants for timebases
	 */
	public final int C_TB_10NS = 0;
	public final int C_TB_20NS = 1;
	public final int C_TB_50NS = 2;
	public final int C_TB_100NS = 3;
	public final int C_TB_200NS = 4;
	public final int C_TB_500NS = 5;
	public final int C_TB_1US = 6;
	public final int C_TB_2US = 7;
	public final int C_TB_5US = 8;
	public final int C_TB_10US = 9;
	public final int C_TB_20US = 10;
	public final int C_TB_50US = 11;
	public final int C_TB_100US = 12;
	public final int C_TB_200US = 13;
	public final int C_TB_500US = 14;
	public final int C_TB_1MS = 15;
	public final int C_TB_2MS = 16;
	public final int C_TB_5MS = 17;
	public final int C_TB_10MS = 18;
	public final int C_TB_20MS = 19;
	public final int C_TB_50MS = 20;
	public final int C_TB_100MS = 21;
	public final int C_TB_200MS = 22;
	public final int C_TB_500MS = 23;
	public final int C_TB_1S = 24;

	/**
	 * sequence result actions
	 */
	static final int C_SEQ_CMD_TRIGGER = 0;
	static final int C_SEQ_CMD_RESET = 1;
	static final int C_SEQ_CMD_NOP = 2;

	/**
	 * Test pattern generator pattern - 12 Mhz , no glitches - 6 Mhz, glitches -
	 * T1, no glitches - T1/2, glitches
	 */
	private final int C_TPG_12 = 0;
	private final int C_TPG_06_G = 1;
	private final int C_TPG_T1 = 2;
	private final int C_TPG_T12_G = 3;

	/**
	 * Values for clock signal edge sensitivity for ...RawTb1Clock()
	 */
	public final int C_TB1_FALLING_EDGE = 0;
	public final int C_TB1_RISING_EDGE = 1;
	public final int C_TB1_DONT_CARE = 2;

	/**
	 * format string for "0xff" type
	 */
	private final String formatString = "0x%02x";

	private MemoryImage setupImage;
	private MemoryImage acquisitionImage;

	/*
	 * Set variable if the memory image contains data containing glitches. This
	 * variable exists for performance reasons, preventing the call of
	 * getRawGlitches() for every bit in getBitValue() and getGlitchValue().
	 */
	private boolean containingGlitches = false;

	/**
	 * CTR
	 * 
	 * @param setupImage
	 * @param acquisitionImage
	 */
	public Tek1241RawDevice(MemoryImage setupImage, MemoryImage acquisitionImage) {
		this.setupImage = setupImage;
		this.acquisitionImage = acquisitionImage;
		// check if memory is with glitches or not
		containingGlitches = getRawGlitches();
	}

	public MemoryImage getSetupImage() {
		return setupImage;
	}

	public MemoryImage getAcquisitionImage() {
		return acquisitionImage;
	}

	/**
	 * Returns true if the memory contains data with glitches.
	 * 
	 * To work correctly, the variable containingGlitches must have been set
	 * before with the correct value (true/false).
	 * 
	 * @return
	 */
	public boolean isContainingGlitches() {
		return containingGlitches;
	}

	/**
	 * Set to true if the memory contains data with glitches.
	 * 
	 * @param containingGlitches
	 */
	public void setContainingGlitches(boolean containingGlitches) {
		this.containingGlitches = containingGlitches;
	}

	/*****************
	 * SETUP MEM PART
	 * 
	 * 
	 * 
	 ***************** 
	 */

	/**
	 * returns memstat value for a acquisition card.
	 * 
	 * @param cardId
	 * @return memory config status for this card.
	 */
	public int getMemStat(int cardId) {
		return setupImage.getByte(OF_MEM_MEMSTAT + cardId);
	}

	/**
	 * returns raw threshold value for a card
	 * 
	 * Returns real raw data only for first 4 cards, which may be 9 channel
	 * cards. Only 9 channel cards define a variable threshold value. 18 channel
	 * cards have a fixed threshold (TTL). Because the first 18 channel card has
	 * id 4, we return the correct value for TTL threshold for all card ids
	 * larger than 3.
	 * 
	 * @param cardId
	 * @return threshold value as described in manual page A-12.
	 */
	public int getThreshold(int cardId) {
		int len = 257; // 257 is default value which means TTL
		if (cardId < 4) {
			int lo = setupImage.getByte(OFM_THRESHOLD + 2 * cardId);
			int hi = setupImage.getByte(OFM_THRESHOLD + 2 * cardId + 1);
			len = hi * 256 + lo;
		}
		// System.out.println("rawlength = " + len);
		return len;
	}

	/**
	 * returns threshold value for a card as a pretty string.
	 * 
	 * The string is either a voltage value like e.g. "5.25" or one of the
	 * internal string values defined by Tek.
	 * 
	 * @param cardId
	 * @return string as described.
	 */
	public String getPrettyThreshold(int cardId) {
		int t = getThreshold(cardId);
		String s = "";
		double d = 0;
		DecimalFormat df = new DecimalFormat("0.00");
		if (t >= 0 && t < 255) {
			d = t * 0.05 - 6.35;
			s = df.format(d);
		} else {
			switch (t) {
			case 255:
				s = "-ECL (Vil<-1.7V, Vih>-1V)";
				break;
			case 256:
				s = "TPG (Vt=+3.7V)";
				break;
			case 257:
				s = "TTL (Vil<0.8V, Vih>2.0V)";
				break;
			case 258:
				s = "like card 0";
				break;

			}
		}

		return s;
	}

	/**
	 * get Group for podId
	 * 
	 * @param podId
	 */
	// public int getGroupForPod(int podId) {
	// return 0;
	// }

	/**
	 * returns the number of assigned channels for a group
	 * 
	 * 
	 * @param groupId
	 * @return
	 */
	public int getChannelsForGroup(int groupId) {
		int offset_to_number_of_channels = 5;
		int ret;
		int position = OF_GRP_GROUPLAYOUT + 12 * groupId
				+ offset_to_number_of_channels;
		ret = setupImage.getByte(position);
		// System.out.println();
		// System.out.println("group " + groupId + " has " + ret + " channels");
		return ret;
	}

	/**
	 * get content of rawyoungest array for a pod
	 * 
	 * @param podId
	 *            id of pod to look for
	 * @return rawyoungest value for that pod
	 */
	public int getRawYoungest(int podId) {
		int lo = acquisitionImage.getByte(OFM_RAWYOUNGEST + 2 * podId);
		int hi = acquisitionImage.getByte(OFM_RAWYOUNGEST + 2 * podId + 1);
		int val = hi * 256 + lo;
		// it seems that val, val1 are of type "-512" etc.
		// Because they were 16-Bit values, they are like 0xfe00
		// and java prints them out as 65024 etc.
		// correct this:
		if (val != 0)
			val -= 65536 + 1;
		return val;

	}

	/**
	 * get content of rawyoldest array for a pod
	 * 
	 * @param podId
	 *            id of pod to look for
	 * @return rawoldest value for that pod
	 */
	public int getRawOldest(int podId) {
		int lo = acquisitionImage.getByte(OFM_RAWOLDEST + 2 * podId);
		int hi = acquisitionImage.getByte(OFM_RAWOLDEST + 2 * podId + 1);
		int val = hi * 256 + lo;
		// it seems that val, val1 are of type "-512" etc.
		// Because they were 16-Bit values, they are like 0xfe00
		// and java prints them out as 65024 etc.
		// correct this:
		if (val != 0)
			val -= 65536 + 1;
		return val;

	}

	/**
	 * dump memory card status
	 */
	public void printMemStats() {
		String str;
		System.out.println();
		for (int i = 0; i < MAX_MEMCARD_COUNT; i++) {
			switch (getMemStat(i)) {
			case C_MEM_CARD_MISSING:
				str = "card missing";
				break;
			case C_MEM_CARD_9_UNCHAINED:
				str = "9 channel card unchained";
				break;
			case C_MEM_CARD_18_UNCHAINED:
				str = "18 channel card unchained";
				break;
			case C_MEM_CARD_CHAINED:
				str = "cad chained";
				break;
			default:
				str = "illegal attribute value";
				break;
			}
			System.out.println("slot #" + i + ": " + str);
		}
	}

	/**
	 * dump cardselect value
	 */
	public void printCardSelect() {
		System.out.println();
		System.out.print("Card select byte: ");
		int b = setupImage.getByte(OF_GRP_CARDSELECT);
		System.out.format(formatString, b);
		System.out.println();
	}

	/*****************
	 * ACQ MEM PART
	 * 
	 * 
	 * 
	 ***************** 
	 */

	/**
	 * dump out complete memory contents
	 */
	public void printAll() {
		int b;

		for (int i = 0; i < acquisitionImage.getSize(); i++) {
			b = acquisitionImage.getByte(OFM_RAWCTR + i);
			System.out.format(formatString, b);
			System.out.println();
		}
	}

	/**
	 * OK print amount of bytes in rawdata array in total
	 */
	public void printRawLength() {
		int lo = acquisitionImage.getByte(OFM_RAWLENGTH);
		int hi = acquisitionImage.getByte(OFM_RAWLENGTH + 1);
		int len = hi * 256 + lo;
		System.out.println();
		// System.out.format(formatString, hi);
		// System.out.println();
		// System.out.format(formatString, lo);
		// System.out.println();
		System.out.println("rawlength = " + len);
	}

	/**
	 * print type of pointer at start of acquisition. Possible content: a time
	 * value, event count, undefined
	 */
	public void printRawCtr() {
		int b;
		String str = "";

		System.out.println();
		b = acquisitionImage.getByte(OFM_RAWCTRUNITS);
		switch (b) {
		case C_RAW_CTRUNITS_EVENT:
			str = "Events";
			break;
		case C_RAW_CTRUNITS_TIME:
			str = "Time";
			break;
		case C_RAW_CTRUNITS_NA:
			str = "No Valid";
			break;
		}
		System.out.print("RawCtr contains " + str + " value, {");
		for (int i = 0; i < 5; i++) {
			b = acquisitionImage.getByte(OFM_RAWCTR + i);
			System.out.format(formatString, b);
			System.out.print(", ");
		}
		System.out.println("}");
	}

	/**
	 * OK print for each pod its amount of bytes in rawdata array
	 */
	public void printRawPodlen() {
		System.out.println();
		for (int i = 0; i < MAX_POD_COUNT; i++) {
			int lo = acquisitionImage.getByte(OFM_RAWPODLEN + 2 * i);
			int hi = acquisitionImage.getByte(OFM_RAWPODLEN + 2 * i + 1);
			int len = hi * 256 + lo;
			System.out.println("pod[" + i + "] = " + len
					+ " bytes in rawdata array, data available: "
					+ getRawEmpty(i));
		}
	}

	/**
	 * OK print for each pod its chaining state. A pod is either a chain head or
	 * a follower to some chain head. (third case: a pod is not installed).
	 */
	public void printRawTb() {
		String str = "";
		System.out.println();
		for (int i = 0; i < MAX_POD_COUNT; i++) {
			int b = getPodChainState(i);
			switch (b) {
			case C_RAW_CHEAD_TB1:
				str = "chain head, assigned to time base 1";
				break;
			case C_RAW_CHEAD_TB2:
				str = "chain head, assigned to time base 2";
				break;
			case C_RAW_CFOLLOWER:
				str = "chain follower";
				break;
			case C_RAW_CNOMEM:
				str = "N/A (no memory installed behind this pod position)";
				break;
			default:
				str = "<illegal attribute value>";
				break;
			}
			System.out.println("pod[" + i + "] is " + str);
		}
	}

	/**
	 * OK print for each pod its rawoldest/rawyoungest value.
	 * 
	 * For each pod that was used (in aquisition), the rawoldest value indicates
	 * the oldest meaningful bit in rawdata for channels from that pod. For each
	 * pod that was used (in aquisition), the rawyoungest value indicates the
	 * youngest meaningful bit in rawdata for channels from that pod.
	 * 
	 * Values are not useful for a) followers , b) not installed and c) pods
	 * that were "empty" (see printRawEmpty())
	 * 
	 * Furthermore, we print out correlation info.
	 * 
	 * For convention of bit offsets (non-glitch/glitch mode) see manual, A-17.
	 */
	public void printRawOldestYoungest() {
		System.out.println();
		for (int i = 0; i < MAX_POD_COUNT; i++) {
			int youngest = getRawYoungest(i);
			int oldest = getRawOldest(i);
			System.out.print("pod[" + i + "] : ");
			if (getRawEmpty(i))
				System.out.println("(rawoldest, rawyoungest)=(" + oldest + ", "
						+ youngest + ")");
			else
				System.out.println("no data available");

		}

	}

	/**
	 * OK dump out Tektronix reserved bytes
	 * 
	 * Seems that they always are 0x00.
	 */
	public void printRawMisc() {
		System.out.println();
		for (int i = 0; i < 10; i++) {
			int val = acquisitionImage.getByte(OFM_RAWMISC + i);
			System.out.println("rawmisc[" + i + "] =" + val);
		}
	}

	/**
	 * OK Check if data was collected for pod podId.
	 * 
	 * @param podId
	 *            pod id to check
	 * @return true if data was collected, false otherwise.
	 */
	public boolean getRawEmpty(int podId) {
		boolean ret;
		// check if this pod is a chain head
		int b = acquisitionImage.getByte(OFM_RAWEMPTY + podId);
		// System.out.println("rawempty"+podId+"="+b);
		if (b == 0x00)
			ret = true;
		else
			ret = false;
		return ret;
	}

	/**
	 * OK Get chain state for a pod. Possible states: - head assigned to
	 * timebase 1 - head assigned to timebase 2 - follower in chain - no memory
	 * installed behind this pod position
	 * 
	 * @param podId
	 *            pod id to check
	 * @return state. C_RAW_CHEAD_TB1, C_RAW_CHEAD_TB2, C_RAW_CFOLLOWER,
	 *         C_RAW_CNOMEM .
	 */
	public int getPodChainState(int podId) {
		int b = acquisitionImage.getByte(OFM_RAWTB + podId);
		return b;
	}

	/**
	 * returns the number of assigned channels for a pod
	 * 
	 * Assumption: always 9. Seems correct for 1240/1241.
	 * 
	 * @param podId
	 * @return
	 */
	public int getChannelsForPod(int podId) {
		return 9;
	}

	/**
	 * OK returns data size in bytes per channel this pod. For unchained
	 * 9-channel pods, the 1241 returns 64. For chained pods, the 1241 returns
	 * (chain depth)*64+1, where chain depth is 1..4.
	 * 
	 * @param podId
	 * @return data site in bytes for one channel needed for this pod in array
	 *         rawdata.
	 */
	public int getPodDataSize(int podId) {
		int lo = acquisitionImage.getByte(OFM_RAWPODLEN + 2 * podId);
		int hi = acquisitionImage.getByte(OFM_RAWPODLEN + 2 * podId + 1);
		int len = hi * 256 + lo;
		// System.out.println("pod[" + podId + "] has " + len
		// + " bytes in rawdata array");
		return len;
	}

	/**
	 * OK Returns start byte in rawdata for the pair (pod, channel)
	 * 
	 * @param podId
	 *            the number of the pod.
	 * @param channel
	 *            bit position in pod (..9/0..18)
	 * @return start byte position
	 */
	public int getChannelStartPos(int podId, int channel) {
		int ret;
		int len;

		// System.out.println();
		// check if this pod is a chain head
		int b = getPodChainState(podId);
		if (b != C_RAW_CHEAD_TB1 && b != C_RAW_CHEAD_TB2) {
			// System.out.println("ERROR: this pod (" + podId
			// + ") is not a chain head");
			return -1;
		}

		// sum up all preceeding pod sizes
		ret = 0;
		for (int i = 0; i < podId; i++) {
			if (getPodChainState(i) != C_RAW_CNOMEM) {
				len = getPodDataSize(i) * getChannelsForPod(i);
				ret += len;
			}
		}

		// now compute offset for channel (bit)
		ret += (getPodDataSize(podId) * channel);

		// System.out.println("Offset for pod " + podId + " channel "+channel+
		// " is " + ret );
		return ret;
	}

	/**
	 * Check if acquisition was with glitches
	 * 
	 * @return
	 */
	public boolean getRawGlitches() {
		int b = acquisitionImage.getByte(OFM_RAWGLITCHES);
		return (b == 0x01);
	}

	/**
	 * get last time base that was active before trigger event occured.
	 * 
	 * @return
	 */
	public int getRawLast() {
		int ret;

		int b = acquisitionImage.getByte(OFM_RAWLAST);
		if (b == 0)
			ret = C_TB_TIMEBASE2;
		else
			ret = C_TB_TIMEBASE1;
		return ret;
	}

	/**
	 * check if acquisition memory bytes were manipulated after acquisition
	 * using a radix table
	 * 
	 * @return
	 */
	public boolean getRawPostFig() {
		int b = acquisitionImage.getByte(OFM_RAWPOSTFIG);
		return (b == C_RAW_MODIFIED);
	}

	/**
	 * return ASYNC/SYNC mode value for timebase 1
	 * 
	 * @return
	 */
	public int getRawTimebase1Type() {
		int ret;
		int b = acquisitionImage.getByte(OFM_RAWTB1TYPE);
		if (b == 0)
			ret = C_TB_ASYNC;
		else
			ret = C_TB_SYNC;
		return ret;
	}

	/**
	 * return DEMUX/SYNC mode value for timebase 2
	 * 
	 * @return
	 */
	public int getRawTimebase2Type() {
		int ret;
		int b = acquisitionImage.getByte(OFM_RAWTB2TYPE);
		if (b == 0)
			ret = C_TB_DEMUX;
		else
			ret = C_TB_SYNC;
		return ret;
	}

	/**
	 * returns raw tbactive value. This value shows what timebases are active.
	 * TB1 only, TB2 only, TB1+TB2.
	 * 
	 * @return 0,1 or 2. In error case, -1.
	 */
	public int getRawTbActive() {
		int ret = -1;
		int b = acquisitionImage.getByte(OFM_RAWTBACTIVE);
		switch (b) {
		case 0:
			ret = C_TB_ACTIVE_TB1;
			break;
		case 1:
			ret = C_TB_ACTIVE_TB2;
			break;
		case 2:
			ret = C_TB_ACTIVE_BOTH;
			break;
		}
		return ret;
	}

	/**
	 * returns time period length used in aquisition. Value is between 0..24.
	 * This value is associated with periods like 10ns, 20ns, 50ns, 100ns, ..
	 * See manual.
	 * 
	 * @return 0..24 meaning a period length 10ns..1s.
	 * 
	 */
	public int getRawTimebase1Async() {
		int b = acquisitionImage.getByte(OFM_RAWTB1ASYNCH);
		return b;
	}

	/**
	 * Get number of qualified clocks for the timebase fater a trigger occured.
	 * 
	 * @param timebaseId
	 *            : 1 for TB1, 2 for TB2
	 * @return number of qualified clocks (in Tek this is an 16 bit integer
	 *         value)
	 */
	public int getRawTpi(int timebaseId) {
		int lo, hi;
		if (timebaseId == 1) {
			lo = acquisitionImage.getByte(OFM_RAWTPI1);
			hi = acquisitionImage.getByte(OFM_RAWTPI1 + 1);
		} else {
			lo = acquisitionImage.getByte(OFM_RAWTPI2);
			hi = acquisitionImage.getByte(OFM_RAWTPI2 + 1);
		}
		int len = hi * 256 + lo;
		return len;
	}

	/**
	 * return trigger state. Answers the question: does a trigger occured during
	 * acquisition or is the acquisition finishes with a full buffer but without
	 * trigger.
	 * 
	 * @return true or false
	 */
	public boolean getRawTrig() {
		boolean ret = false;

		int b = acquisitionImage.getByte(OFM_RAWTRIG);
		if (b == 0)
			ret = true;
		return ret;
	}

	/**
	 * Returns the pod that generated the correlation information for a
	 * timebase. See manual page A-15.
	 * 
	 * @param timebaseId
	 *            : 1 for TB1, 2 for TB2
	 * 
	 * @return
	 */
	public int getRawCPod(int timebaseId) {
		int b;
		if (timebaseId == C_TB_TIMEBASE1) {
			b = acquisitionImage.getByte(OFM_RAWC1POD);
		} else {
			b = acquisitionImage.getByte(OFM_RAWC2POD);
		}
		if (b == 0xff)
			b = -1;
		return b;

	}

	/**
	 * Returns theTPI .... ????????????????? See manual page ...
	 * 
	 * @param timebaseId
	 *            : 1 for TB1, 2 for TB2
	 * 
	 * @return
	 */
	public int getRawTpiPod(int timebaseId) {
		int b;
		if (timebaseId == C_TB_TIMEBASE1) {
			b = acquisitionImage.getByte(OFM_RAWTPI1);
		} else {
			b = acquisitionImage.getByte(OFM_RAWTPI2);
		}
		return b;

	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getBitValue(int podId, int channel, int bitPos) {

		// for glitches, we must scale the position because each position
		// contains the bit itself and the glitch value
		// we do not call getRawGlitches() here for performance reasons.
		// So it is required that SetContainingGlitches() has been called
		// before for glitch data.
		if (containingGlitches)
			bitPos = 2 * bitPos + 1;
		// get position of byte that contains the bit
		int bytePos = bitPos / 8;
		// get position of the bit in the byte
		int bitPosInByte = bitPos % 8;

		// offset for this pod in rawdata
		int byteOffset = OFM_RAWDATA + getChannelStartPos(podId, channel)
				+ bytePos;

		// get complete byte (all 8 bits)
		int byteVal = acquisitionImage.getByte(byteOffset);

		// extract bit value for the bit to use
		int bitVal = byteVal & (1 << bitPosInByte);

		// return true if bit is 1, false otherwise
		return (bitVal != 0);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean getGlitchValue(int podId, int channel, int glitchPos) {

		// calculate glitch position. glitch data is 1 bit after the bit data
		glitchPos = 2 * glitchPos;
		// get position of byte that contains the bit
		int bytePos = glitchPos / 8;
		// get position of the bit in the byte
		int bitPosInByte = glitchPos % 8;

		// offset for this pod in rawdata
		int byteOffset = OFM_RAWDATA + getChannelStartPos(podId, channel)
				+ bytePos;

		// get complete byte (all 8 bits)
		int byteVal = acquisitionImage.getByte(byteOffset);

		// extract bit value for the bit to use
		int bitVal = byteVal & (1 << bitPosInByte);

		// return true if bit is 1, false otherwise
		return (bitVal != 0);
	}

	/**
	 * extract an integer value from a clock period constant
	 * 
	 * @param theVal
	 *            string value to extract the qualifier from. It is assumed that
	 *            theVal satisfies the pattern "C_TB_(0..9)*(NS|US|MS|S). These
	 *            values corresponds to the name of the constants defined, the
	 *            names are extracted from an TekDev1241-object using
	 *            introspection.
	 * @return the qualifier (ns,us,ms,s)
	 */
	private String getQualifierVal(String theVal) {
		int endPos = theVal.length() - 1;
		String ret = "?";
		if (theVal.charAt(endPos) == 'S') {
			ret = "s";
			switch (theVal.charAt(endPos - 1)) {
			case 'N':
				ret = "ns";
				break;
			case 'U':
				ret = "us";
				break;
			case 'M':
				ret = "ms";
				break;
			default:
				System.out.println("Error: illegal argument");
			}
		}
		return ret;
	}

	/**
	 * extract an integer value from a clock period constant
	 * 
	 * @param theVal
	 *            string value to extract the qualifier from. It is assumed that
	 *            theVal satisfies the pattern "C_TB_(0..9)*(NS|US|MS|S). These
	 *            values corresponds to the name of the constants defined, the
	 *            names are extracted from an TekDev1241-object using
	 *            introspection.
	 * @return the clock period value
	 */
	private int getIntVal(String theVal) {
		int endPos = theVal.length() - 1;
		if (theVal.charAt(endPos) == 'S') {
			if (theVal.charAt(endPos - 1) == 'N'
					|| theVal.charAt(endPos - 1) == 'U'
					|| theVal.charAt(endPos - 1) == 'M')
				endPos -= 1;
		}

		String s = theVal.substring(5, endPos);
		// System.out.println("s: " + s);
		return Integer.parseInt(s);
	}

	/**
	 * get qualifier value for clock period length value. A clock period length
	 * value consists of a integer and the qualifier. Example: 10ns -> "10" is
	 * the integer, "ns" the qualifier.
	 * 
	 * @param n
	 *            the value returned from getRawTimebase1Async (0..24)
	 * @return the clock period qualifier
	 */
	public String getClockPeriodQualifier(int n) {
		Class<? extends Tek1241RawDevice> clazz = this.getClass();
		String[] v = new String[25];
		int i = 0;

		// get all fields from this class
		Field[] fields = clazz.getDeclaredFields();
		// put into array v[] all clock period constants
		for (Field f : fields) {
			if (f.getName().startsWith("C_TB_") && f.getName().endsWith("S")) {
				// System.out.println("Field: " + f.getName());
				v[i++] = f.getName();
			}
		}
		// get the constant's string for n
		String theVal = v[n];
		// extract the qualifier string
		String qVal = getQualifierVal(theVal);
		return qVal;
	}

	/**
	 * Get depth of sequence (number of sequence steps)
	 * 
	 * @return
	 */
	public int getRawSeqDepth() {
		int depth = setupImage.getByte(OF_TRIG_SEQDEPTH);
		return depth;
	}

	/**
	 * Get command to execute if sequence succeeds
	 * 
	 * @return
	 */
	public int getRawSeqCmd() {
		int cmd = setupImage.getByte(OF_TRIG_SEQCMD);
		return cmd;
	}

	/**
	 * Get store value (do storing if sequence succeeds?)
	 * 
	 * @return
	 */
	public boolean getRawSeqStore() {
		int rawStore = setupImage.getByte(OF_TRIG_SEQSTORE);
		boolean store;
		if (rawStore == 0)
			store = false;
		else
			store = true;
		return store;
	}

	/**
	 * get integer value for clock period length value. A clock period length
	 * value consists of a integer and the qualifier. Example: 10ns -> "10" is
	 * the integer, "ns" the qualifier.
	 * 
	 * @param n
	 *            the value returned from getRawTimebase1Async (0..24)
	 * @return the clock period value
	 */
	public int getClockPeriodValue(int n) {
		Class<? extends Tek1241RawDevice> clazz = this.getClass();
		String[] v = new String[25];
		int i = 0;

		// get all fields from this class
		Field[] fields = clazz.getDeclaredFields();
		// put into array v[] all clock period constants
		for (Field f : fields) {
			if (f.getName().startsWith("C_TB_") && f.getName().endsWith("S")) {
				// System.out.println("Field: " + f.getName());
				v[i++] = f.getName();
			}
		}
		// get the constant's string for n
		String theVal = v[n];
		// extract the integer value
		int intVal = getIntVal(theVal);
		return intVal;
	}

	/**
	 * Return number of 9 channel cards
	 * 
	 * @return
	 */
	public int getNum9ChannelCards() {
		return acquisitionImage.getByte(OFM_RAWD9);
	}

	/**
	 * Return number of 18 channel cards
	 * 
	 * @return
	 */
	public int getNum18ChannelCards() {
		return acquisitionImage.getByte(OFM_RAWD18);
	}

	/**
	 * Return width of a 9 channel chain
	 * 
	 * @return
	 */
	public int getWidth9Ch() {
		int num = acquisitionImage.getByte(OFM_RAWD9);
		int b2 = acquisitionImage.getByte(OFM_RAWD9 + 1);
		int width = -1;

		switch (num) {
		case 0:
			width = 0;
			break;
		case 1:
			width = 9;
			break;
		case 2:
			switch (b2) {
			case 0:
				width = 18;
				break;
			case 1:
				width = 9;
				break;
			}
			break;
		case 3:
			switch (b2) {
			case 0:
				width = 27;
				break;
			case 1:
				width = 9;
				break;
			}
			break;
		case 4:
			switch (b2) {
			case 0:
				width = 36;
				break;
			case 1:
				width = 18;
				break;
			case 2:
				width = 9;
				break;
			}
			break;
		}
		return width;
	}

	/**
	 * Return depth of a 9 channel chain
	 * 
	 * @return
	 */
	public int getDepth9Ch() {
		int num = acquisitionImage.getByte(OFM_RAWD9);
		int b2 = acquisitionImage.getByte(OFM_RAWD9 + 1);
		boolean glitches = getRawGlitches();
		int depth = -1;

		switch (num) {
		case 0:
			depth = 0;
			break;
		case 1:
			depth = 513;
			break;
		case 2:
			switch (b2) {
			case 0:
				if (glitches)
					depth = 257;
				else
					depth = 513;
				break;
			case 1:
				if (glitches)
					depth = 513;
				else
					depth = 1025;
				break;
			}
			break;
		case 3:
			switch (b2) {
			case 0:
				if (glitches)
					depth = 257;
				else
					depth = 513;
				break;
			case 1:
				if (glitches)
					depth = 769;
				else
					depth = 1537;
				break;
			}
			break;
		case 4:
			switch (b2) {
			case 0:
				if (glitches)
					depth = 257;
				else
					depth = 513;
				break;
			case 1:
				if (glitches)
					depth = 513;
				else
					depth = 1025;
				break;
			case 2:
				if (glitches)
					depth = 1025;
				else
					depth = 2049;
				break;
			}
			break;
		}
		return depth;
	}

	/**
	 * Return width of a 18 channel chain
	 * 
	 * @return
	 */
	public int getWidth18Ch() {
		int num = acquisitionImage.getByte(OFM_RAWD18);
		int b2 = acquisitionImage.getByte(OFM_RAWD18 + 1);
		int width = -1;

		switch (num) {
		case 0:
			width = 0;
			break;
		case 1:
			width = 18;
			break;
		case 2:
			switch (b2) {
			case 0:
				width = 36;
				break;
			case 1:
				width = 18;
				break;
			}
			break;
		case 3:
			switch (b2) {
			case 0:
				width = 54;
				break;
			case 1:
				width = 18;
				break;
			}
			break;
		case 4:
			switch (b2) {
			case 0:
				width = 72;
				break;
			case 1:
				width = 36;
				break;
			case 2:
				width = 18;
				break;
			}
			break;
		}
		return width;
	}

	/**
	 * Return depth of a 18 channel chain
	 * 
	 * @return
	 */
	public int getDepth18Ch() {
		int num = acquisitionImage.getByte(OFM_RAWD18);
		int b2 = acquisitionImage.getByte(OFM_RAWD18 + 1);
		int depth = -1;

		switch (num) {
		case 0:
			depth = 0;
			break;
		case 1:
			depth = 513;
			break;
		case 2:
			switch (b2) {
			case 0:
				depth = 513;
				break;
			case 1:
				depth = 1025;
				break;
			}
			break;
		case 3:
			switch (b2) {
			case 0:
				depth = 513;
				break;
			case 1:
				depth = 1537;
				break;
			}
			break;
		case 4:
			switch (b2) {
			case 0:
				depth = 513;
				break;
			case 1:
				depth = 1025;
				break;
			case 2:
				depth = 2049;
				break;
			}
			break;
		}
		return depth;
	}

	/**
	 * Return operation level value (0..3)
	 * 
	 * @return operation level value
	 */
	public int getRawOpLevel() {
		int val = setupImage.getByte(OFM_RAWOPLEVEL);
		return val;
	}

	/**
	 * Return test pattern generator, pattern type (0..3)
	 * 
	 * @return test pattern generator, pattern type
	 */
	public int getRawTpgPat() {
		int val = setupImage.getByte(OFM_RAWTPGPAT);
		return val;
	}

	/**
	 * dump out acquisition card configuration
	 */
	public void printCards() {
		System.out.println("number of 9ch cards: " + getNum9ChannelCards());
		System.out.println("width: " + getWidth9Ch());
		System.out.println("depth: " + getDepth9Ch());

		System.out.println("number of 18ch cards: " + getNum18ChannelCards());
		System.out.println("width: " + getWidth18Ch());
		System.out.println("depth: " + getDepth18Ch());
	}

	public void printRawSeqDepth() {
		System.out.println("Seq Depth: " + getRawSeqDepth());
	}

	public void printRawSeqCmd() {
		int cmd = getRawSeqCmd();
		String str;
		switch (cmd) {
		case C_SEQ_CMD_TRIGGER:
			str = "Trigger";
			break;
		case C_SEQ_CMD_RESET:
			str = "Reset";
			break;
		case C_SEQ_CMD_NOP:
			str = "Do nothing";
			break;
		default:
			str = "(unknown value)";
			break;
		}
		System.out.println("Seq Cmd: " + str + "(" + cmd + ")");
	}

	public void printRawSeqStore() {
		boolean store = getRawSeqStore();
		System.out.println("Seq Store: " + store);
	}

	public void printRawOpLevel() {
		int opLevel = getRawOpLevel();
		System.out.println("Operation level (0..3): " + opLevel);
	}

	public void printRawTpgPat() {
		int pat = getRawOpLevel();
		System.out.print("TP generator pattern (0..3): " + pat + ": ");
		switch (pat) {
		case C_TPG_12:
			System.out.println("12 Mhz, no glitches");
			break;
		case C_TPG_06_G:
			System.out.println("12 Mhz, no glitches");
			break;
		case C_TPG_T1:
			System.out.println("12 Mhz, no glitches");
			break;
		case C_TPG_T12_G:
			System.out.println("12 Mhz, no glitches");
			break;
		default:
			System.out.println("(unknown pattern id)");
		}
	}

	public void printRawTb1Clock() {
		int val;

		if (getRawTimebase1Type() != C_TB_SYNC) {
			System.out.println("TB1 clock is not in sync mode");
			return;
		}
		for (int i = 0; i < 8; i++) {
			val = setupImage.getByte(OFM_RAWTB1CLOCK + i);
			System.out.print("TB1 clock def [" + i + "] = " + val + ". ");
			switch (val) {
			case C_TB1_FALLING_EDGE:
				System.out.println("Falling edge");
				break;
			case C_TB1_RISING_EDGE:
				System.out.println("Rising edge");
				break;
			case C_TB1_DONT_CARE:
				System.out.println("Dont't care");
				break;
			default:
				System.out.println("(unknown edge definition)");
			}
		}
	}

	/**
	 * Dump polarity per pod. "1" means positive true, "0" means negative true
	 * 
	 * Note that we have 16 bit elements, but they are not a 16-bit word!
	 * b2 are the first eight channels, bit 0 of b2 is channel 9 (clock bit).
	 */
	public void printRawPolarity() {
		int b1, b2;

		for (int i = 0; i < 8; i++) {
			b2 = setupImage.getByte(OFM_RAWPOLARITY + 2 * i);
			b1 = setupImage.getByte(OFM_RAWPOLARITY + 2 * i + 1);
			b1 &= 0x01;
			System.out.println("Polarity [" + i + "] = "
					+ ConversionUtil.byte2hexString(b1)
					+ ConversionUtil.byte2hexString(b2) + ". ");
		}
	}
	
	/**
	 * ************************* WORK IN PROGRESS BELOW
	 */


}
