package de.spurtikus.softtek.tek;

import java.io.IOException;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.swt.graphics.GC;

import softtek.AbstractDevice;
import de.spurtikus.comm.serial.BulkDataListener;
import de.spurtikus.comm.serial.Communication;
import de.spurtikus.softtek.ApplicationSingleton;

public class Tek1241Device extends AbstractDevice implements BulkDataListener {

	/** data for application is in singleton */
	private ApplicationSingleton softTekApp = ApplicationSingleton
			.getInstance();

	/**
	 * display group structure
	 */
	private DisplayGroup[] displayGroup;

	private boolean glitchData;

	// /**
	// * format string for "0xfff" type
	// */
	// private final String formatStringPod = "0x%03x";
	// /**
	// * format string for "    45" type
	// */
	// private final String formatStringDump = "%5d";

	/**
	 * data identifier we are waiting for during a bulk data read ("acq", "ins",
	 * ...)
	 */
	private String bulkDataIdentifier = null;

	/**
	 * listener for device data
	 */
	ArrayList<DeviceDataListener> ddListener = null;

	/**
	 * the raw device
	 */
	private Tek1241RawDevice tekRaw = null;

	/**
	 * size estimation values. See related get methods
	 */
	private int sizeEstimationSetup = 2500;
	private int sizeEstimationMem = 0;
	private int sizeEstimationRampack = 64000 / 10 * 25;

	/**
	 * Constructor. Creates a raw device and initializes group structure
	 * 
	 * @param setupImage
	 * @param acquisitionImage
	 */
	public Tek1241Device(MemoryImage setupImage, MemoryImage acquisitionImage) {
		tekRaw = new Tek1241RawDevice(setupImage, acquisitionImage);
		displayGroup = new DisplayGroup[10];
		ddListener = new ArrayList<DeviceDataListener>();

		if (setupImage != null && acquisitionImage != null) {
			buildGroupLayout();
			glitchData = tekRaw.getRawGlitches();

			Communication communication = softTekApp.getCommunication();
			// set read mode top read in large data line
			communication.setReadLine(true);
			// add listener to listen to completion of read in all data
			communication.addBulkDataListener(this);
			// add listener to listen to SRQs
			communication.addSrqListener(this);
		}

	}

	/**
	 * build group info
	 * 
	 * Uses DisplayGroup object.
	 * 
	 * @param position
	 */
	public void buildGroupLayout() {
		for (int i = 0; i < 10; i++) {
			displayGroup[i] = new DisplayGroup(i);
			displayGroup[i].build(tekRaw, i);
		}
	}

	/**
	 * dump out info about all groups.
	 * 
	 * Uses DisplayGroup object.
	 * 
	 * @param position
	 */
	public void printGroupLayout() {
		for (int i = 0; i < 10; i++) {
			displayGroup[i].print();
		}
	}

	/**
	 * get Group for podId
	 * 
	 * @param podId
	 */
	public DisplayGroup getGroup(int groupId) {
		return displayGroup[groupId];
	}

	public Tek1241RawDevice getTekRaw() {
		return tekRaw;
	}

	public boolean isGlitchData() {
		return glitchData;
	}

	/**
	 * Set partner address (GPIB address of device to use)
	 * 
	 * @param deviceAddress
	 *            address of device to use
	 */
	public void setDeviceAddress(int deviceAddress) {
		try {
			softTekApp.getCommunication().write(".a " + deviceAddress + "\r");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			softTekApp.getCommunication().write("id?\r");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	/**
	 * reads in bulk data from device
	 * 
	 */
	public void readBulkData(String s) {
		Communication communication = softTekApp.getCommunication();
		// set read mode top read in large data line
		communication.setReadLine(true);
		// add listener to listen to completion of read in all data
		communication.addBulkDataListener(this);
		bulkDataIdentifier = s;
		try {
			communication.write(s + "\r");
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println("Waiting for bulk data read completion with ID: "
				+ s);
	}

	@Override
	/**
	 * BulkDataListener interface method
	 * 
	 * Called when a bulk read is finished.
	 * Extracts payload from bulk data and calls related listeners.
	 * 
	 * @param buffer buffer containing bulk data
	 * @param bufferSize size of buffer
	 */
	public void complete(byte[] buffer, int bufferSize) {
		int lineCtr = 0;
		byte payload[] = new byte[bufferSize];
		int j = 0;
		boolean readPayload = false;

		System.out.println("bulk read for ID " + bulkDataIdentifier
				+ " finished. buffer contains " + bufferSize + " bytes");

		for (int i = 0; i < bufferSize; i++) {
			System.out.print("*" + (char) buffer[i]);
			// if we are in the payload, extract it
			if (readPayload)
				payload[j++] = buffer[i];
			// check if payload part is reached
			if ((buffer[i] == 0x0d || buffer[i] == 0x0a)) {
				lineCtr++;
				// System.out.println("ctr="+lineCtr);
				if (lineCtr == 7) {
					// System.out.println("overread header at position i="+i);
					// System.out.println("{");
					readPayload = true;
				}
			}
		}
		// System.out.println("}");
		// for (int k = 0; k < j; k++)
		// System.out.print((char) payload[k]);

		// for (DeviceDataListener l : ddListener)
		// System.out.println("listener: " + l.toString());

		// remove the listener
		softTekApp.getCommunication().removeBulkDataListener(this);

		if (bulkDataIdentifier.toLowerCase().startsWith("acq")) {
			System.out.println("acq memory was read");
			for (DeviceDataListener l : ddListener)
				l.handleAcquisitionData(payload, j);

		}
		if (bulkDataIdentifier.toLowerCase().startsWith("ref")) {
			System.out.println("ref memory was read");
			for (DeviceDataListener l : ddListener)
				l.handleRefmemData(payload, j);
		}
		if (bulkDataIdentifier.toLowerCase().startsWith("ins")) {
			System.out.println("ins memory was read");
			for (DeviceDataListener l : ddListener)
				l.handleSetupData(payload, j);
		}
		if (bulkDataIdentifier.toLowerCase().startsWith("event")) {
			String s = "";
			for (int k = 0; k < j; k++)
				s += (char) (payload[k]);
			// System.out.println("event was read: " + event2Message(s));
			for (DeviceDataListener l : ddListener)
				l.handleEventData(event2Message(s));
		}
		if (bulkDataIdentifier.toLowerCase().startsWith("id")) {
			// System.out.println("id was read");
			String s = "";
			for (int k = 0; k < j; k++)
				s += (char) (payload[k]);
			for (DeviceDataListener l : ddListener)
				l.handleIdData(s);
		}
	}

	/**
	 * converts the answer to the command "event?", which looks like "EVENT 123"
	 * ro the message associated to the ID 123, e.g.
	 * "Argument is too large (123)".
	 * 
	 * @param eventString
	 * @return
	 */
	public String event2Message(String eventString) {
		int eventId = extractIdFromEventMessage(eventString);
		EventMessages em = EventMessages.getInstance();
		return em.getEventMessage(eventId);
	}

	/**
	 * extract the event code as integer from a string. String is e.g.
	 * "EVENT 123"
	 * 
	 * @param eventString
	 *            the string to check
	 * @return the event code extracted
	 */
	private int extractIdFromEventMessage(String eventString) {
		int i = 0;
		String s = "";
		while (!Character.isDigit(eventString.charAt(i)))
			i++;
		while (Character.isDigit(eventString.charAt(i)))
			s += eventString.charAt(i++);

		return Integer.parseInt(s);

	}

	/**
	 * Add a listener to device data events
	 * 
	 * @param listener
	 */
	public void addDeviceDataListener(DeviceDataListener listener) {
		if (!ddListener.contains(listener))
			ddListener.add(listener);
	}

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public void removeDeviceDataListener(DeviceDataListener listener) {
		ddListener.remove(listener);
	}

	@Override
	public void srq(byte[] buffer, int bufferSize) {
		for (DeviceDataListener l : ddListener)
			l.handleServiceRequest(buffer, bufferSize,
					SrqString2Event(buffer, bufferSize));
	}

	/**
	 * Converts a byte sequence created from SRQ sequence to the EventId that is
	 * contained inside that sequence. The Event Id string part is of type
	 * "^EVENT 714$" as an example.
	 * 
	 * @param buffer
	 *            buffer to check for event id
	 * @param bufferSize
	 *            size of buffer
	 * @return event id if found or -1
	 */
	public int SrqString2Event(byte[] buffer, int bufferSize) {
		String s = "";
		String r = "";
		int ret = -1;

		// create a string from byte buffer
		int i = 0;
		while (i < bufferSize)
			s += (char) buffer[i++];

		int p = s.lastIndexOf("EVENT");
		if (p != -1) {
			// System.out.println("event pos=" + p);
			p += 5 + 1; // strlen("EVENT"+" ")
			i = p;
			while (Character.isDigit(s.charAt(p))) {
				r += s.charAt(p);
				p++;
			}
			// System.out.println("eventId=" + r);
		}
		try {
			ret = Integer.parseInt(r);
		} catch (NumberFormatException e) {
		}
		return ret;
	}

	/**
	 * Parses Id string
	 * 
	 * Parses ID string and dumps some info. If string does not comply with the
	 * expected format, an error message is printed out.
	 * 
	 * @param s
	 *            - ID string as returned by Tek 1241
	 * 
	 */

	public void parseIdString(String s) {

		String requiredPatternS = "ID TEK/1240,V(\\d\\d\\.\\d),SYS:V(\\d.\\d),COMM:V(\\d.\\d),ACQ:(\\d):(\\d):(\\d):(\\d).*";

		Pattern requiredPattern = Pattern.compile(requiredPatternS,
				Pattern.DOTALL);
		Matcher m = requiredPattern.matcher(s);
		if (m.matches()) {
			System.out.println("Code and format version: " + m.group(1));
			System.out.println("System software version: " + m.group(2));
			System.out.println("COMM PACK software version: " + m.group(3));
			System.out.println("Slot 1: " + m.group(4));
			System.out.println("Slot 2 " + m.group(5));
			System.out.println("Slot 3: " + m.group(6));
			System.out.println("Slot 4: " + m.group(7));
			s = m.group(1);
		} else {
			System.err.println("String does not look like an ID string: \"" + s
					+ "\"");
			return;
		}
	}

	/**
	 * write a memory image to device.
	 * 
	 * Supports only ASCII hex data format currently.
	 * 
	 * TODO: this method is not able to convert memory raw data to ASCII HEX. It
	 * relies on ASCII HEX data kept from reading in by using the method
	 * getInput(). It would be desirable to add the ability to create ASCII HEX
	 * from raw data on the fly.
	 * 
	 * @param acquisitionImage
	 *            the image data to write.
	 */
	public void writeBulkData(MemoryImage image) {
		System.out.println("Uploading bulk data");

		byte[] p = image.getInput();
		int pSize = image.getInputSize();
		String data = "";

		// make sure that we have data to send.
		if (p == null) {
			System.err
					.println("Cannot upload image data because it was downloaded without keeping the original raw data.");
			return;
		}

		// copy bytes into a string
		for (int i = 0; i < pSize; i++)
			data += (char) p[i];

		try {
			for (int i = 0; i < pSize; i++)
				System.out.print((char) (data.charAt(i)));
			softTekApp.getCommunication().write(data + "\r");

		} catch (IOException e) {
			System.err.println("Error during upload of bulk data");
			e.printStackTrace();
		}
		System.out.println("Uploading bulk data finished");
	}

	/**
	 * Returns boolean value of bit number 'bitPos' in channel 'channel'
	 * 
	 * Note: To correctly handle glitch data, it is required that
	 * SetContainingGlitches(true) has been called before accessing bits and
	 * glitches. This is usually done in the CTR.
	 * 
	 * @param podId
	 *            the number of the pod.
	 * @param channel
	 *            the channel to use
	 * @param bitPos
	 *            the position of the bit
	 * @return true or false
	 */
	public boolean getBitValue(int podId, int channel, int bitPos) {
		return tekRaw.getBitValue(podId, channel, bitPos);
	}

	/**
	 * Returns boolean value of glitch number 'bitPos' in channel 'channel'
	 * 
	 * This method does only give correct values for acquisitions containing
	 * data with glitches.The method does not check if the data contains
	 * glitches.
	 * 
	 * @param podId
	 *            the number of the pod.
	 * @param channel
	 *            the channel to use
	 * @param glitchPos
	 *            the position of the glitch
	 * @return true or false
	 */
	public boolean getGlitchValue(int podId, int channel, int glitchPos) {
		return tekRaw.getGlitchValue(podId, channel, glitchPos);
	}

	/**
	 * Create array containing bit values (0 or 1) from raw tek device data for
	 * a (pod,bit) pair
	 * 
	 * @param podId
	 *            - pod id to use
	 * @param bitposInPod
	 *            bit position in pod to use
	 * @return array of bit values. A 'true' is represented as a integer 1, a
	 *         'false' as a integer 0. The size of the array is equal to the
	 *         number of bits in the array.
	 */
	public int[] getTimelineBits(int podId, int bitposInPod) {
		int lastClockPos;
		int b[] = new int[1520];
		int i;

		// decide last bit position value
		lastClockPos = getTekRaw().getDepth9Ch();
		// over all bit positions
		for (i = 0; i < lastClockPos; i++) {
			boolean bitVal = false;
			boolean glitchVal = false;
			// get value of bit
			bitVal = getBitValue(podId, bitposInPod, i);

			if (isGlitchData()) {
				// GLITCHES
				glitchVal = getGlitchValue(podId, bitposInPod, i);
				// System.out.println("Bit[" + i + "]=" + bitVal
				// + ", Glitch: " + glitchVal);
				if (bitVal)
					b[2 * i] = 1;
				else
					b[2 * i] = 0;
				if (glitchVal)
					b[2 * i + 1] = 2;
				else
					b[2 * i + 1] = 0;
			} else {
				// NO GLITCHES
				// get bit value
				if (bitVal)
					b[i] = 1;
				else
					b[i] = 0;
			}
		}
		// make copy of correct size for drawChannel call
		int max;
		if (isGlitchData())
			max = 2 * i + 1;
		else
			max = i;
		int[] p = new int[max];
		for (int j = 0; j < max; j++)
			p[j] = b[j];

		return p;
	}

	/**
	 * Returns calculated position of last valid clock in timeline
	 * 
	 * @return
	 */
//	public int getLastClockPosition() {
//		int lastClockPos;
//
//		// decide last bit position value
//		// we have 65 Bytes * 8 Bit depth
//		// TODO: chaining!
//		if (isGlitchData())
//			lastClockPos = 65 * 4;
//		else
//			lastClockPos = 65 * 8;
//
//		return lastClockPos;
//	}

	/**
	 * Returns calculated byte value (e.g. 0xaf) for coordinates (groupId,
	 * bitPosition). We get all bits of all channels and calculate the
	 * corresponding byte value.
	 * 
	 * TODO: needs to be checked if it works also for groups with more than 8
	 * channels.
	 * 
	 * @param gc
	 * @param groupId
	 * @param bitPosition
	 * @return
	 */
	public int getByteValue(GC gc, int groupId, int bitPosition) {
		int bitposInPod;
		int pod = 0;
		int byteValue = 0;

		// iterate over all channels of this group
		for (Channel c : getGroup(groupId).getChannels()) {
			pod = c.getPod();
			bitposInPod = c.getBit();

			byteValue = 2 * byteValue;
			if (getBitValue(pod, bitposInPod, bitPosition)) {
				byteValue += 1;
			}
		}
		return byteValue;
	}

	/**
	 * Returns estimation for number of bytes of the answer to an INSET? inquiry
	 * 
	 * @return
	 */
	public int getSizeEstimationSetup() {
		return sizeEstimationSetup;
	}

	/**
	 * Returns estimation for number of bytes of the answer to an ACQ? or REF?
	 * inquiry
	 * 
	 * @return
	 */
	public int getSizeEstimationMem() {
		int num9ChCards = getTekRaw().getNum9ChannelCards();
		int num18ChCards = getTekRaw().getNum18ChannelCards();

		sizeEstimationMem = 24 * (590 + 9 * ((num9ChCards * 65) + (num18ChCards * 130))) / 10;
		System.out.println("(" + num9ChCards + "," + num18ChCards
				+ ") Estimated size: " + sizeEstimationMem);
		return sizeEstimationMem;
	}

	/**
	 * Returns estimation for number of bytes of the answer to an RAM? inquiry
	 * 
	 * @return
	 */
	public int getSizeEstimationRampack() {
		return sizeEstimationRampack;
	}

	/**
	 * Returns depth of a podId which is a chain head. Note that a chain
	 * follower has no depth, so the method is only applicable to pod ids
	 * representing a chain head
	 * 
	 * @param chainHead
	 * @return
	 */
	public int getDepth(int chainHead) {
		int d=-1;
		int val = getTekRaw().getMemStat(chainHead);
		if (val == getTekRaw().C_MEM_CARD_9_UNCHAINED)
			d = getTekRaw().getDepth9Ch();
		if (val == getTekRaw().C_MEM_CARD_18_UNCHAINED)
			d = getTekRaw().getDepth18Ch();
		return d;
	}

}
