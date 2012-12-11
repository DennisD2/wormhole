package de.spurtikus.comm.serial;

public interface BulkDataListener {
	
	/**
	 * Method called if a bulk data load is finished.
	 * 
	 * @param buffer buffer containing the bulk data
	 * @param bufferSize number of valid bytes in buffer
	 */
	void complete ( byte[] buffer, int bufferSize );
	
	/**
	 * Method called if a srq from a device finished.
	 * 
	 * @param buffer buffer containing the srq related data
	 * @param bufferSize number of valid bytes in buffer
	 */
	void srq( byte[] buffer, int bufferSize );

}
