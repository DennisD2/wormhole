package softtek;

/**
 * Class with utilities for accessing Tek 1240/1241 devices.
 * 
 * @author dennis
 * 
 */
public class ConversionUtil {

	/**
	 * Tek 1240/1241 display codes
	 */
	public static final int C_DSP_SPACE = 0x24;
	public static final int C_DSP_ALT_SPACE = 0x3a;
	public static final int C_DSP_DOT = 0x25;
	public static final int C_DSP_COMMA = 0x26;
	public static final int C_DSP_SLASH = 0x27;
	public static final int C_DSP_DOUBLEDOT = 0x28;
	public static final int C_DSP_HAT = 0x29;
	public static final int C_DSP_DOLLAR = 0x2a;

	public static final String HEX_PREFIX = "0x";
	public static final String DUAL_PREFIX = "";
	public static final String DUAL_DELIMITER = ".";

	/**
	 * convert a hex digit value to a number. E.g. Input '1': h=0x31 -->
	 * returned is integer value of 1 Input 'B': h=0x42 --> returned is integer
	 * value of 11 Input 'a': h=0x62 --> returned is integer value of 10
	 * 
	 * @param byte h to convert
	 * @return the integer value
	 */
	public static int hex2int(byte h) {
		int ret = 0;
		if (h >= 0x30 && h <= 0x39)
			ret = h - 0x30;
		if (h >= 0x41 && h <= 0x46)
			ret = 10 + (h - 0x41);
		if (h >= 0x61 && h <= 0x66)
			ret = 10 + (h - 0x61);
		return ret;
	}

	/**
	 * Returns a string that represents the hex value of a byte value (0..255).
	 * E.g. a byte value of 0xaa will be converted to string "0xAA"
	 * 
	 * @return
	 */
	public static String byte2hexString(int b) {
		String ret = HEX_PREFIX;

		int hiNibble = b / 16;
		int loNibble = (b % 16);

		return ret + nibble2Char(hiNibble) + nibble2Char(loNibble);
	}

	/**
	 * Returns char representing a nibble (= four bit) value. E.g. the nibble
	 * 0xa will be returned as "A"
	 * 
	 * @param n
	 * @return
	 */
	public static char nibble2Char(int n) {
		char c = '?';

		if (n >= 0 && n <= 9) {
			// 0..9
			c = (char) (n + 0x30);
		} else {
			if (n >= 10 && n <= 15) {
				// A-F
				switch (n) {
				case 10:
					c = 'a';
					break;
				case 11:
					c = 'b';
					break;
				case 12:
					c = 'c';
					break;
				case 13:
					c = 'd';
					break;
				case 14:
					c = 'e';
					break;
				case 15:
					c = 'f';
					break;
				}
			}
		}
		return c;
	}

	/**
	 * Returns a string that represents the dual/binary value of a byte value
	 * (0..255). E.g. a byte value of 0xaa will be converted to string
	 * "1010.1000"
	 * 
	 * @param n
	 * @return
	 */
	public static String byte2dualString(int n) {
		String ret = DUAL_PREFIX;

		for (int i = 7; i >= 0; i--) {
			if ((n & (1 << i)) != 0)
				ret += "1";
			else
				ret += "0";
			if (i == 4)
				ret += DUAL_DELIMITER;
		}
		return ret;
	}

	/**
	 * convert a string to a byte array
	 * 
	 * Used for creating byte arrays to use in test methods.
	 * 
	 * @param string
	 *            to convert
	 * @return the byte array
	 */
	public static byte[] String2Byte(String str) {
		int i;

		byte b[] = new byte[str.length()];

		for (i = 0; i < str.length(); i++)
			b[i] = (byte) str.charAt(i);
		return b;
	}

	/**
	 * converts a Tek 1240/1241 display code value to an ASCII value
	 * 
	 * @param c
	 *            display code value
	 * @return ASCII character for c. If no valid ASCII char is found, '?' is
	 *         returned.
	 */

	public static char DisplayCode2Char(int c) {
		int r = c;

		// 0..9
		if (c >= 0 && c <= 9) {
			r = c + 0x30;
			return (char) r;
		}
		// chars A-Z
		if (c >= 0x0a && c <= 0x23) {
			r = c - 0x0a;
			r += 0x41;
			return (char) r;
		}
		// single char values
		switch (c) {
		case C_DSP_SPACE:
		case C_DSP_ALT_SPACE:
			r = ' '; // SPACE
			break;
		case C_DSP_DOT:
			r = '.';
			break;
		case C_DSP_COMMA:
			r = ',';
			break;
		case C_DSP_SLASH:
			r = '/';
			break;
		case C_DSP_DOUBLEDOT:
			r = ':';
			break;
		case C_DSP_HAT:
			r = '^';
			break;
		case C_DSP_DOLLAR:
			r = '$';
			break;
		}
		if ((char) r == (char) c) {
			System.out.println("unknown c: " + c);
		}
		return (char) r;
	}

}
