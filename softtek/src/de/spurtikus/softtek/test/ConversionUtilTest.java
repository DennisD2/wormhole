package de.spurtikus.softtek.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import softtek.ConversionUtil;

public class ConversionUtilTest {

	@Test
	public void testByte2StringMethods() {
		int a = 0xa8;
		String testee;

		testee = ConversionUtil.byte2hexString(a);
		// System.out.println(testee);
		assertEquals("Return values should be equal", "0xa8", testee);

		testee = ConversionUtil.byte2dualString(a);
		// System.out.println(testee);
		assertEquals("Return values should be equal", "1010.1000", testee);
	}

}
