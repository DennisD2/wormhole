package de.spurtikus.softtek.test;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import de.spurtikus.softtek.tek.AsciiDataBlock;
import de.spurtikus.softtek.tek.BaseDataBlock;
import de.spurtikus.softtek.tek.MemoryImage;
import de.spurtikus.softtek.tek.Tek1241RawDevice;

import softtek.ConversionUtil;
import softtek.FileReader;

public class BaseDataBlockTest {

	/**
	 * data block testee
	 */
	BaseDataBlock data;

	/**
	 * setup memory image testee
	 */
	MemoryImage setupImage;

	/**
	 * acquisition memory image testee
	 */
	MemoryImage acqImage;

	/**
	 * ...
	 */
	String d1Str = "#H34000B1083C714893EFD3AC606FF3A00C606003B00C606013B08C606F77702C606BEF802C606BFF801E816108D66FC5E5F5DC355C6,";

	/**
	 * abc=9, aloc=0x30, payload=0x1, 0x1, 0x2, 0x1, 0x0, checksum=0xc2 Note:
	 * checksum is sum of abc,aloc and payload byte pairs (0x3e) in zweier
	 * complement modulo 256. Note: abc =6+10+2=18 = 2*9 --> byteCount=0x09,
	 */
	String d2Str = "#H090000300101020100C2.00";

	/**
	 * real string from tek
	 */
	String d3Str = "#H0E010390000000000000000000005E";

	public BaseDataBlockTest() {
		// create a block
		data = new AsciiDataBlock();
		// create memory images
		setupImage = new MemoryImage();
		acqImage = new MemoryImage();
	}

	@Test
	public void testByteCount() {
		byte[] d;

		d = ConversionUtil.String2Byte(d1Str);

		data.parseBlock(d, 0);

		assertEquals("ByteCount should return correct value", (52 * 2 - 8) / 2,
				data.getByteCount());
	}

	@Test
	public void testByteCount2() {
		byte[] d;

		d = ConversionUtil.String2Byte(d3Str);

		data.parseBlock(d, 0);

		assertEquals("ByteCount should return correct value", (14 * 2 - 8) / 2,
				data.getByteCount());
	}

	@Test
	public void testLocation() {
		byte[] d;

		d = ConversionUtil.String2Byte(d1Str);

		data.parseBlock(d, 0);

		assertEquals("Location should return correct value", 0xB10,
				data.getLocation());
	}

	@Test
	public void testChecksum() {
		byte[] d;

		d = ConversionUtil.String2Byte(d1Str);

		data.parseBlock(d, 0);

		assertEquals("Checksum should be ok", true, data.isCheckSumValid());

		d = ConversionUtil.String2Byte(d2Str);

		data.parseBlock(d, 0);

		assertEquals("Checksum should be ok", true, data.isCheckSumValid());

	}

	@Test
	public void testData() {
		byte[] d;

		d = ConversionUtil.String2Byte(d2Str);

		data.parseBlock(d, 0);

		assertEquals("Location should return correct value", 0x01,
				data.getByte(0));
		assertEquals("Location should return correct value", 0x01,
				data.getByte(1));
		assertEquals("Location should return correct value", 0x02,
				data.getByte(2));
		assertEquals("Location should return correct value", 0x01,
				data.getByte(1));
		assertEquals("Location should return correct value", 0x01,
				data.getByte(0));
	}

	@Test
	public void testDataFromFile() {
		int size;
		int cur_ptr = 0;
		int last_ptr = 0;

		int num_block = 0;
		int total_bytes = 0;

		// read in file
		// String fileName="gpib-refmem-hex.txt";
		String fileName = "testdata/tek1241-insetup.txt";

		FileReader fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		byte[] d = fileReader.getBuffer();
		System.out.println("File size: " + size);
		while (cur_ptr < size) {
			// get next byte
			byte b = d[cur_ptr++];
			// System.out.print((char) b);
			if (b == BaseDataBlock.BYTE_COMMA
					|| b == BaseDataBlock.BYTE_SEMICOLON) {
				// if we got a comma, we read in one complete data block
				System.out.println();
				System.out.print("Block " + num_block + " start=" + last_ptr
						+ ": ");
				BaseDataBlock block = new AsciiDataBlock();
				// parse in block from last point read in (last_ptr) to current
				// position
				block.parseBlock(d, last_ptr);
				last_ptr = cur_ptr;
				assertEquals("Checksum should be ok", true,
						block.isCheckSumValid());
				// update statistics
				num_block++;
				total_bytes += block.getByteCount();
			}
		}
		System.out.println(num_block + " blocks read in with " + total_bytes
				+ " bytes in total.");
	}

	@Test
	public void testInsetup() {
		int size;

		// read in file
		// String fileName="gpib-refmem-hex.txt";
		String fileName = "testdata/tek1241-insetup.txt";

		FileReader fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		byte[] d = fileReader.getBuffer();

		setupImage.build(d, size);

		System.out.println("Memory image size " + setupImage.getSize());

		// assertEquals(
		// "sum of block contents bytes and resulting memory image bytes should be equal",
		// total_bytes, setupImage.getSize());
	}

	@Test
	public void testInsetupWithAcq() {
		int size;

		// read in file
		// String fileName="gpib-refmem-hex.txt";
		// String fileName = "tek1241-insetup.txt";
		String fileName = "testdata/tek1241-insetup-acqmem-filled.txt";

		FileReader fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		byte[] d = fileReader.getBuffer();

		setupImage.build(d, size);

		System.out.println("Memory image size " + setupImage.getSize());

		// assertEquals(
		// "sum of block contents bytes and resulting memory image bytes should be equal",
		// total_bytes, setupImage.getSize());

		// create a TekDev object with the image just created
		Tek1241RawDevice tek = new Tek1241RawDevice(setupImage, acqImage);

		/**
		 * card select
		 */
		tek.printCardSelect();

		/**
		 * memstat
		 */
		tek.printMemStats();

		/**
		 * group layout
		 */
		//tek.printGroupLayout();

		/**
		 * channel group array
		 */
		// tek.printChannelGroup();
	}

	@Test
	public void testAcquireMem() {
		int size;

		// fill setupImage
		String fileName = "testdata/tek1241-insetup2.txt";
		FileReader fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		byte[] d = fileReader.getBuffer();
		setupImage.build(d, size);

		// fill acq Image
		// String fileName="gpib-refmem-hex.txt";
		// fileName = "tek1241-acqmem-filled.txt";
		fileName = "testdata/tek1241-acqmem2.txt";
		fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		d = fileReader.getBuffer();
		acqImage.build(d, size);

		System.out.println("Memory image size " + acqImage.getSize());

		// assertEquals(
		// "sum of block contents bytes and resulting memory image bytes should be equal",
		// total_bytes, acqImage.getSize());

		// create a TekDev object with the image just created
		Tek1241RawDevice tek = new Tek1241RawDevice(setupImage, acqImage);

		// tek.printAll();
		tek.printRawLength();
		tek.printRawCtr(); 
		tek.printRawPodlen();
		tek.printRawTb();
		
		tek.printCards();

		boolean b = tek.getRawGlitches();
		System.out.println("Acquisition with glitches: " + b);
		int i = tek.getRawLast();
		System.out.println("Timebase active at trigger: " + i);
		b = tek.getRawPostFig();
		System.out.println("Modified by Radix table: " + b);
		i = tek.getRawTimebase1Type();
		System.out.println("Timebase mode: " + i);
		i = tek.getRawTimebase1Async();
		System.out.println("Timebase clock period time value: " + i);

		b = tek.getRawTrig();
		System.out.println("Acquisition contains atrigger event: " + b);
		i = tek.getRawTpi(tek.C_TB_TIMEBASE1);
		System.out.println("Timebase 1 clocks after trigger event: " + i);
		i = tek.getRawTpi(tek.C_TB_TIMEBASE2);
		System.out.println("Timebase 2 clocks after trigger event: " + i);

		tek.printRawOldestYoungest();
		System.out.println();
		
		// now correlation data
		System.out.print("Correlation data for timebase 1 in rawcor1: ");
		if (tek.getRawCPod(tek.C_TB_TIMEBASE1) != -1) {
			System.out.println("valid from "
					+ tek.getRawOldest(tek.getRawCPod(tek.C_TB_TIMEBASE1)) + " to "
					+ tek.getRawYoungest(tek.getRawCPod(tek.C_TB_TIMEBASE1)));
			System.out.println("Pod that generated correlation info for timebase 1: "
							+ tek.getRawCPod(tek.C_TB_TIMEBASE1));
		} else
			System.out.println("not defined");

		System.out.print("Correlation data for timebase 2 in rawcor2: ");
		if (tek.getRawCPod(tek.C_TB_TIMEBASE2) != -1) {
			System.out.println("valid from "
					+ tek.getRawOldest(tek.getRawCPod(tek.C_TB_TIMEBASE2)) + " to "
					+ tek.getRawYoungest(tek.getRawCPod(tek.C_TB_TIMEBASE2)));
			System.out.println("Pod that generated correlation info for timebase 2: "
					+ tek.getRawCPod(tek.C_TB_TIMEBASE2));
		}
		System.out.println("not defined");

		tek.getChannelStartPos(0, 0);
		tek.getChannelStartPos(0, 1);
		tek.getChannelStartPos(0, 2);
		tek.getChannelStartPos(0, 3);
		tek.getChannelStartPos(0, 4);
		tek.getChannelStartPos(0, 5);
		tek.getChannelStartPos(0, 6);
		tek.getChannelStartPos(0, 7);
		tek.getChannelStartPos(1, 4);
		tek.getChannelStartPos(2, 0);
		tek.getChannelStartPos(3, 0);
		tek.getChannelStartPos(4, 0);
		tek.getChannelStartPos(5, 0);
		tek.getChannelStartPos(6, 0);
		tek.getChannelStartPos(7, 8);
		System.out.println();

	}

	@Test
	public void testPrintAcq() {
		// fill setupImage
		String fileName = "testdata/tek1241-insetup2.txt";
		FileReader fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		int size = fileReader.read();
		byte[] d = fileReader.getBuffer();
		setupImage.build(d, size);

		// fill acq Image
		// String fileName="gpib-refmem-hex.txt";
		fileName = "testdata/tek1241-acqmem2.txt";
		fileReader = new FileReader(fileName);
		assertNotNull("fileReader must not be null.", fileReader);
		size = fileReader.read();
		d = fileReader.getBuffer();
		acqImage.build(d, size);

		// create a TekDev object with the image just created
		Tek1241RawDevice tek = new Tek1241RawDevice(setupImage, acqImage);

		int n = tek.getRawTimebase1Async();
		String periodQualifier = tek.getClockPeriodQualifier(n);
		int periodLength = tek.getClockPeriodValue(n);
		System.out.println("ClockPeriodQualifier: " + periodQualifier);
		System.out.println("ClockPeriod: " + periodLength);

		int triggerPos = tek.getRawTpi(tek.C_TB_TIMEBASE1);
		System.out.println("Timebase 1 clocks after trigger event: " + n);

		//tek.dumpAcq(0, periodLength, periodQualifier, triggerPos);

	}

}
