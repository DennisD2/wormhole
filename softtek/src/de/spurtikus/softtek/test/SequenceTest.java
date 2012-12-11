package de.spurtikus.softtek.test;

import org.junit.Test;

import softtek.FileReader;
import de.spurtikus.softtek.tek.MemoryImage;
import de.spurtikus.softtek.tek.Sequence;
import de.spurtikus.softtek.tek.Tek1241Device;

public class SequenceTest {

	@Test
	public void testSequenceRead() {
		loadImages("/home/dennis/tek1241-setup-sequence-1.tek");
	}

	public void loadImages(String file) {
		// create memory images
		MemoryImage setupImage = new MemoryImage(true);
		MemoryImage acqImage = new MemoryImage(true);

		// open input file
		FileReader fileReader = new FileReader(file);
		int size = fileReader.read();
		byte[] d = fileReader.getBuffer();

		// do a simple check for file content validity by checking the first
		// some bytes
		boolean fileIsValidBySimpleCheck = false;
		if (((char) d[0] == 'I') && ((char) d[1] == 'N')
				&& ((char) d[2] == 'S') && ((char) d[3] == 'E')
				&& ((char) d[4] == 'T'))
			fileIsValidBySimpleCheck = true;
		if (!fileIsValidBySimpleCheck) {
			System.err.println("File " + file
					+ " seems not to be a valid tek file");
			return;
		} else {
			System.out.println("File " + file + " looks like a valid tek file");

		}
		// fill setup image
		int i = 0;
		// read until first CR or LF is reached. This is the end of the setup
		// line.
		while ((i < size) && ((d[i] != 0x0a) && (d[i] != 0x0d))) {
			// System.out.print((char) d[i]);
			i++;
		}
		System.out.println("Setup data ends at file position " + i);
		// create image from buffer part
		setupImage.build(d, i);
		// overread CRs and LFs until next line starts
		while ((d[i] == 0x0a) || (d[i] == 0x0d))
			i++;
		// keep j, this is the start offset of second line
		int j = i;
		// allocate buffer that can be filled with second line data
		byte a[] = new byte[size];
		// read until first CR or LF is reached. This is the end of the memory
		// data line.
		while ((i < size) && ((d[i] != 0x0a) && (d[i] != 0x0d))) {
			a[i - j] = d[i];
			System.out.print((char) d[i]);
			i++;
		}
		System.out.println("Memory data starts at position " + j
				+ " and goes until position " + i);
		// create image from buffer part
		acqImage.build(a, i - j);

		// create a TekDev object with the image just created
		Tek1241Device tek = new Tek1241Device(setupImage, acqImage);

		int n = tek.getTekRaw().getRawTimebase1Async();
		String periodQualifier = tek.getTekRaw().getClockPeriodQualifier(n);
		int periodLength = tek.getTekRaw().getClockPeriodValue(n);
		System.out.println("ClockPeriodQualifier: " + periodQualifier);
		System.out.println("ClockPeriod: " + periodLength);
		int triggerPos = tek.getTekRaw().getRawTpi(tek.getTekRaw().C_TB_TIMEBASE1);
		System.out.println("Timebase 1 clocks after trigger event: " + n);
		
		Sequence s = new Sequence();
		tek.getTekRaw().printRawSeqDepth();
		tek.getTekRaw().printRawSeqCmd();
		tek.getTekRaw().printRawSeqStore();
		s.build( tek.getTekRaw().getSetupImage() );
		s.dumpSequence(tek.getTekRaw().getRawSeqDepth());
	}

}
