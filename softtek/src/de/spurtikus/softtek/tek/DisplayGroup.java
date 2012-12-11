package de.spurtikus.softtek.tek;

import java.util.ArrayList;
import java.util.List;

import softtek.ConversionUtil;

/**
 * Teks GROUP object , describing sets of probe pins together with control data.
 * 
 * @author dennis
 * 
 */
public class DisplayGroup {
	
	public static final int NUM_GROUPS =10;
	
	/**
	 * tek definitions for the time base value
	 */
	public static final int TIMEBASE_1=0;
	public static final int TIMEBASE_2=1;
	public static final int TIMEBASE_UNASSIGNED=2;
	
	/**
	 * id of this group. This correlates to the group number from the raw array (0..9)
	 */
	private int id;

	private String name;
	private int timebase;
	private int numChannels;
	private int raweOffsetIntoChannelGroup;
	private int inputFieldEncoding;
	private int displayFieldEncoding;
	private int rawOffsetWordRecognizer;
	private boolean radixTableAvailable;
	private int numCharsForDisplayUsingRadix;

	private List<Channel> channels = null;

	private boolean needsBuild = true;

	public DisplayGroup(int id) {
		this.id = id;
		channels = new ArrayList<Channel>();
	}

	/**
	 * 
	 * Build group info by extracting the info from a memory image.
	 * 
	 * @param memoryImage
	 * @param groupId
	 *            group to guild.
	 */
	void build( Tek1241RawDevice tekRaw, int groupId) {
		String str = "";
		int b;
		MemoryImage memoryImage = tekRaw.getSetupImage();

		int position = Tek1241RawDevice.OF_GRP_GROUPLAYOUT + 12 * groupId;

		for (int i = 0; i < 4; i++) {
			str += ConversionUtil.DisplayCode2Char(memoryImage.getByte(position
					+ i));
		}
		name = str;

		b = memoryImage.getByte(position + 4);
		switch (b) {
		case 0:
			str = "1";
			break;
		case 1:
			str = "2";
			break;
		case 2:
			str = "none assigned";
			break;
		default:
			str = "<illegal attribute value>";
			break;
		}
		timebase = b;
		
		numChannels = memoryImage.getByte(position + 5);
		raweOffsetIntoChannelGroup = memoryImage.getByte(position + 6);
		inputFieldEncoding = memoryImage.getByte(position + 7);
		rawOffsetWordRecognizer = memoryImage.getByte(position + 8);

		b = memoryImage.getByte(position + 9);
		if (b == 0)
			radixTableAvailable = false;
		else
			radixTableAvailable = true;

		displayFieldEncoding = memoryImage.getByte(position + 10);
		numCharsForDisplayUsingRadix = memoryImage.getByte(position + 11);

		buildChannelsForGroup(memoryImage, raweOffsetIntoChannelGroup,
				numChannels);

		needsBuild = false;
	}

	/**
	 * build from "channelgroup" array data related to a group.
	 * 
	 * The info for this group starts at an offset value into the channelgroup
	 * array and has a defined size. The offset and the size can be determined
	 * using channel group layout data. See printGroupLayout(). The values in
	 * "channelgroup" contains the Pod id and the Bit-Number at the Pod for this
	 * channel. Determination of Pod/Pod-Bit is done in printBitInfo.
	 * 
	 * @param position
	 *            start offset for this group in array channelgroup.
	 * @param size
	 *            size of this group in bytes. Equals number of channels.
	 */
	public void buildChannelsForGroup(MemoryImage memoryImage, int position,
			int size) {
		// channelgroup is a 72 bit array, one byte for each channel.
		String str;
		for (int i = Tek1241RawDevice.OF_GRP_CHANNELGROUP + position; i < Tek1241RawDevice.OF_GRP_CHANNELGROUP
				+ position + size; i++) {
			int n = memoryImage.getByte(i);
			if (n == 0xff)
				str = "unused";
			else {
				int channelPos = i-Tek1241RawDevice.OF_GRP_CHANNELGROUP-position;
				// create new channel object
				Channel channel = new Channel( channelPos, getPod(n), getBit(n) );
				// add to channel list
				channels.add(channel);
				//str ="channel["+channelPos+"] pod : "+getPod(n)+", Bit: "+getBit(n);
			}
			//System.out.println("channelgroup["
			//		+ (i - Tek1241RawDevice.OF_GRP_CHANNELGROUP) + "]=" + str);
		}
	}

	/**
	 * get Pod Id for a group channel.
	 * 
	 * Input value is a byte from array channelgroup. This byte has a special
	 * meaning: Bits 5,6,7 contain the bit position (0..7). Bits 0,1,2,3 contain
	 * the prod id (0..8).
	 * 
	 * @param c
	 *            value from array channelgroup.
	 * @return Pod Id
	 */
	public int getPod(int c) {
		int pod; // 0..7 in hi bits 5,6,7

		pod = (c >> 5) & 0x7;
		return pod;
	}

	/**
	 * get bit position in pod for a group channel.
	 * 
	 * Input value is a byte from array channelgroup. This byte has a special
	 * meaning: Bits 5,6,7 contain the bit position (0..7). Bits 0,1,2,3 contain
	 * the pod id (0..8).
	 * 
	 * @param c
	 *            value from array channelgroup.
	 * @return bit position
	 */
	public int getBit(int c) {
		int channelInPod; // 0..8 in lo nibble

		channelInPod = (c & 0xf);
		return channelInPod;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getTimebase() {
		return timebase;
	}

	public void setTimebase(int timebase) {
		this.timebase = timebase;
	}

	public int getNumChannels() {
		return numChannels;
	}

	public void setNumChannels(int numChannels) {
		this.numChannels = numChannels;
	}

	public int getRaweOffsetIntoChannelGroup() {
		return raweOffsetIntoChannelGroup;
	}

	public void setRaweOffsetIntoChannelGroup(int raweOffsetIntoChannelGroup) {
		this.raweOffsetIntoChannelGroup = raweOffsetIntoChannelGroup;
	}

	public int getInputFieldEncoding() {
		return inputFieldEncoding;
	}

	public void setInputFieldEncoding(int inputFieldEncoding) {
		this.inputFieldEncoding = inputFieldEncoding;
	}

	public int getDisplayFieldEncoding() {
		return displayFieldEncoding;
	}

	public void setDisplayFieldEncoding(int displayFieldEncoding) {
		this.displayFieldEncoding = displayFieldEncoding;
	}

	public int getRawOffsetWordRecognizer() {
		return rawOffsetWordRecognizer;
	}

	public void setRawOffsetWordRecognizer(int rawOffsetWordRecognizer) {
		this.rawOffsetWordRecognizer = rawOffsetWordRecognizer;
	}

	public boolean isRadixTableAvailable() {
		return radixTableAvailable;
	}

	public void setRadixTableAvailable(boolean radixTableAvailable) {
		this.radixTableAvailable = radixTableAvailable;
	}

	public int getNumCharsForDisplayUsingRadix() {
		return numCharsForDisplayUsingRadix;
	}

	public void setNumCharsForDisplayUsingRadix(int numCharsForDisplayUsingRadix) {
		this.numCharsForDisplayUsingRadix = numCharsForDisplayUsingRadix;
	}

	/**
	 * returns list of channels for this group
	 * @return list of channels
	 */
	public List<Channel> getChannels() {
		return channels;
	}

	/**
	 * Print group info
	 * 
	 */
	void print() {
		
		if (needsBuild)
			return;

		System.out.println();
		System.out.println("Group: \"" + name + "\"");
		System.out.println("Time base: " + timebase);
		System.out.println("Channels assigned to group: " + numChannels);
		System.out.println("Offset into channelgroup array: "
				+ raweOffsetIntoChannelGroup);
		System.out.println("INPUT field encoding (hex/oct/bin): "
				+ inputFieldEncoding);
		System.out
				.println("Offset word recognizer: " + rawOffsetWordRecognizer);
		System.out.println("Radix table available: " + radixTableAvailable);
		System.out.println("DISP field encoding (hex/oct/bin): "
				+ displayFieldEncoding);
		System.out.println("# of chars needed for input radix: "
				+ numCharsForDisplayUsingRadix);

		printChannelsForGroup();
	}
	
	/**
	 * print out "channelgroup" array data related to a group.
	 * 
	 * The info for this group starts at an offset value into the channelgroup
	 * array and has a defined size. The offset and the size can be determined
	 * using channel group layout data. See printGroupLayout(). The values in
	 * "channelgroup" contains the Pod id and the Bit-Number at the Pod for this
	 * channel. Determination of Pod/Pod-Bit is done in printBitInfo.
	 * 
	 * @param position
	 *            start offset for this group in array channelgroup.
	 * @param size
	 *            size of this group in bytes. Equals number of channels.
	 */
	public void printChannelsForGroup() {
		String str;
		for (Channel c: channels) {
				str ="pod: "+c.getPod()+", Bit: "+c.getBit();
			System.out.println("channel: " + str);
		}
	}

}
