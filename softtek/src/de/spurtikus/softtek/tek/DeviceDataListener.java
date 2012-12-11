package de.spurtikus.softtek.tek;

public interface DeviceDataListener {

	void handleSetupData(byte[] data, int dataSize);

	void handleAcquisitionData(byte[] data, int dataSize);

	void handleRefmemData(byte[] data, int dataSize);

	void handleEventData(String data);

	void handleIdData(String data);
	
	void handleServiceRequest(byte[] request, int requestSize, int eventId );

}
