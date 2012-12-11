package de.spurtikus.softtek.tek;

import java.util.HashMap;

public class EventMessages {

	public static final int EVENT_NONE = 0;
	public static final int EVENT_CMD_HEADER_ERROR = 101;
	public static final int EVENT_MSG_UNIT_DELIM_ERROR = 107;
	public static final int EVENT_ONLINE = 401;
	public static final int EVENT_REQ_ACQMEM_UPLOAD = 711;
	public static final int EVENT_REQ_REFMEM_UPLOAD = 712;
	public static final int EVENT_REQ_REFMEM_DOWNLOAD = 713;
	public static final int EVENT_REQ_SETUP_UPLOAD = 714;
	public static final int EVENT_REQ_SETUP_DOWNLOAD = 715;
	public static final int EVENT_END_OF_ACQUISITION = 721;

	static HashMap<Integer, String> eventMessages = null;

	static EventMessages singleton = null;

	public EventMessages() {
		eventMessages = new HashMap<Integer, String>();
		initialize();
	}

	public static EventMessages getInstance() {
		if (singleton == null) {
			singleton = new EventMessages();
		}
		return singleton;
	}

	public void initialize() {
		eventMessages.put(new Integer(EVENT_NONE), "No event available");
		eventMessages.put(new Integer(EVENT_CMD_HEADER_ERROR),
				"Command header error");
		eventMessages.put(new Integer(EVENT_MSG_UNIT_DELIM_ERROR),
				"Invalid message unit delimiter");
		eventMessages.put(new Integer(EVENT_ONLINE), "Tek 1240 is online");
		eventMessages.put(new Integer(EVENT_REQ_ACQMEM_UPLOAD),
				"Request acqmem upload");
		eventMessages.put(new Integer(EVENT_REQ_REFMEM_UPLOAD),
				"Request refmem upload");
		eventMessages.put(new Integer(EVENT_REQ_REFMEM_DOWNLOAD),
				"Request refmem download");
		eventMessages.put(new Integer(EVENT_REQ_SETUP_UPLOAD),
				"Request setup upload");
		eventMessages.put(new Integer(EVENT_REQ_SETUP_DOWNLOAD),
				"Request setup download");
		eventMessages.put(new Integer(EVENT_END_OF_ACQUISITION),
				"End of acquisition");
	}

	/**
	 * Returns event message for an event code.
	 * 
	 * If an event message is found for the event code, the message is returned,
	 * the event code is appended in brackets to the message. If no event code
	 * is found, a default string including the event code is returned.
	 * 
	 * @param event
	 * @return
	 */
	public String getEventMessage(int event) {
		String ret = eventMessages.get(new Integer(event));
		if (ret == null)
			ret = "Event code: " + event;
		else
			ret += " (" + event + ")";
		return ret;
	}
}
