package de.spurtikus.softtek.test;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import de.spurtikus.softtek.tek.EventMessages;

public class EventMessagesTest {

	@Test
	public void testEventMessages() {
		EventMessages em = EventMessages.getInstance();
		
		String testee = em.getEventMessage(101);
		
		System.out.println(testee);
		assertEquals("Return values should be equal", "Command header error (101)", testee);
	}
}
