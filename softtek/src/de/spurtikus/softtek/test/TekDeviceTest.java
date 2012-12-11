package de.spurtikus.softtek.test;

import org.junit.Test;

import de.spurtikus.softtek.tek.Tek1241Device;

public class TekDeviceTest {

	@Test
	public void testIdStringParsing() {
		String tek1241IdString = "ID TEK/1240,V81.1,SYS:V1.1,COMM:V1.0,ACQ:1:1:2:2;";
		Tek1241Device tek = new Tek1241Device(null, null);
		
		tek.parseIdString(tek1241IdString);
	}
	
	
}
