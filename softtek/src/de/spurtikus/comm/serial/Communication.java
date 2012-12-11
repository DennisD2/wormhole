package de.spurtikus.comm.serial;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.TooManyListenersException;

import de.spurtikus.softtek.ApplicationSingleton;

public class Communication implements Runnable {

	private ApplicationSingleton commApp = ApplicationSingleton.getInstance();

	// private TextFormatter formatter = new TextFormatter();

	/**
	 * some default configuration
	 */
	private int baudrate = 38400;
	private int dataBits = SerialPort.DATABITS_8;
	private int stopBits = SerialPort.STOPBITS_1;
	private int parity = SerialPort.PARITY_NONE;
	private String portName = "/dev/ttyS4";

	private CommPortIdentifier serialPortId;
	private Enumeration enumComm;
	private SerialPort serialPort;
	private OutputStream outputStream;
	private InputStream inputStream;
	private Boolean portIsOpen = false;

	// statistics data
	/** current bytes per seconds value for running transmission */
	private int bytesPerSecond;
	/** last bytes per seconds value for current transmission, more stable value */
	private int bytesPerSecondSave = 0;
	/** total bytes transfered value for running transmission since last START */
	private long totalBytes = 0;

	Boolean started = false;

	/**
	 * readLine: read complete line in, i.e. until CR or LF occurs in byte
	 * stream.
	 */
	Boolean readLine = false;

	/** internal counter for data in bulkBuffer */
	int readLineCounter = 0;
	/** buffer for bulk data read in */
	byte bulkBuffer[] = new byte[10000];
	/** size of bulk buffer content */
	int bulkSize = 0;

	/**
	 * buffer to collect "unrequested" data read from serial port, usually SRQs
	 * from devices
	 */
	byte srqBuffer[] = new byte[1000];
	/** size of srq Buffer content */
	int srqSize = 0;

	/** list of all listeners for bulk data */
	ArrayList<BulkDataListener> bdListener;
	/** list of all listeners for srq data */
	ArrayList<BulkDataListener> srqListener;

	public Communication() {
		bdListener = new ArrayList<BulkDataListener>();
		srqListener = new ArrayList<BulkDataListener>();
	}

	/**
	 * start communication
	 * 
	 * reads in and saves parameters, resets some vars and start the thread. See
	 * run() what is happening in thread.
	 * 
	 * @param portName
	 *            e.g. "/dev/ttyS4"
	 * @param baudRate
	 *            e.g. 38400
	 * @param dataBits
	 *            e.g. 8
	 * @param parity
	 *            See {@link SerialPort} parity defines.
	 * @param stopBits
	 *            e.g. 1
	 */
	public void start(String portName, int baudRate, int dataBits, int parity,
			int stopBits) {
		if (started) {
			System.out.println("port is already started");
			return;
		}
		// config vales
		this.portName = portName;
		this.baudrate = baudRate;
		this.dataBits = dataBits;
		this.parity = parity;
		this.stopBits = stopBits;
		// statistic values
		bytesPerSecond = 0;
		bytesPerSecondSave = 0;
		totalBytes = 0;

		// start this thread, see run()
		new Thread(this).start();
		started = true;
	}

	/**
	 * Thread run()
	 * 
	 * opens port by calling open(). open() adds a event listener to the serial
	 * port which is called if data becomes available at the port.
	 * 
	 * After the port is open and the event listener is attached, we enter a
	 * loop that sleeps for 1 second and then updates statistics data.
	 */
	public void run() {
		// open port
		if (open(portName) != true)
			return;

		while (portIsOpen) {
			try {
				// update value (Bytes Per Second)
				bytesPerSecondSave = bytesPerSecond;
				// reset local var
				bytesPerSecond = 0;
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

	/**
	 * open a port.
	 * 
	 * @param portName
	 *            name of port
	 * @return true on success, false otherwise.
	 */
	boolean open(String portName) {
		Boolean foundPort = false;

		if (portIsOpen) {
			System.out.println("port is already open");
			return false;
		}
		// System.out.println("open port");

		// search for this port in list of available ports
		enumComm = CommPortIdentifier.getPortIdentifiers();
		while (enumComm.hasMoreElements()) {
			serialPortId = (CommPortIdentifier) enumComm.nextElement();
			if (portName.contentEquals(serialPortId.getName())) {
				foundPort = true;
				break;
			}
		}
		if (!foundPort) {
			System.out.println("port " + portName + " not found");
			return false;
		}

		// open port
		try {
			serialPort = (SerialPort) serialPortId.open("de.spurtikus.comm",
					500);
		} catch (PortInUseException e) {
			System.err.println("port in use.");
			return false;
		}

		// get output stream for that port (used for writing to serial port)
		try {
			outputStream = serialPort.getOutputStream();
		} catch (IOException e) {
			System.out.println("cannot access OutputStream");
			return false;
		}

		// get input stream for that port (used for reading from serial port)
		try {
			inputStream = serialPort.getInputStream();
		} catch (IOException e) {
			System.out.println("cannot access InputStream");
			return false;
		}

		// add event listener for incoming data (read listener)
		try {
			serialPort.addEventListener(new serialPortEventListener());
		} catch (TooManyListenersException e) {
			System.out.println("TooManyListenersException for port");
			return false;
		}
		serialPort.notifyOnDataAvailable(true);

		// set port configuration
		try {
			serialPort
					.setSerialPortParams(baudrate, dataBits, stopBits, parity);
		} catch (UnsupportedCommOperationException e) {
			System.out.println("cannot set port configuration");
			return false;
		}

		if (inputStream == null || outputStream == null) {
			System.err.println("Failed in getting input or output stream");
		}
		// all good
		portIsOpen = true;
		return true;
	}

	/**
	 * close port
	 * 
	 * closes port, set CPS back to zero.
	 */
	public void close() {
		if (portIsOpen == true) {
			// System.out.println("closing port.");
			serialPort.close();
			portIsOpen = false;
			bytesPerSecondSave = 0;
			bytesPerSecond = 0;
		} else {
			System.out.println("port is already closed.");
		}
	}

	/**
	 * read from port
	 * 
	 * while data is available, read that data in and dump it to stdout. Update
	 * statistics vars.
	 * 
	 */
	void read() {
		try {
			byte[] data = new byte[150];
			int num;
			while (inputStream.available() > 0) {
				num = inputStream.read(data, 0, data.length);
				for (int i = 0; i < num; i++) {
					// System.out.print((char) (data[i]));
					if (readLine)
						bulkBuffer[bulkSize + i] = data[i];
					else {
						// we are receiving chars without expecting them.
						// this may be a SRQ request by some device
						// System.out.print((char) (data[i]));
						srqBuffer[srqSize + i] = data[i];
					}
					// System.out.println((char) (data[i]) + "," + (int)
					// (data[i])
					// + " rlc=" + readLineCounter + " rl=" + readLine);

					if ((data[i] == 0x0d || data[i] == 0x0a)) {
						// System.out.println("(CR or LF)");
						if (readLine) {
							// finish filling buffer
							readLineCounter++;
							if (readLineCounter == 9) {
								setReadLine(false);
								bulkSize += i;
								// System.out.println("Bulksize " + bulkSize);
								callBulkDataListener();
							}
						}
					}
					if (!readLine && data[i] == '>') {
						srqSize += num;
						System.out.println("End of SRQ Data, srqSize="
								+ srqSize);
						for (int k = 0; k < srqSize; i++)
							System.out.print((char) (srqBuffer[i]));
						callSrqListener();
						srqSize = 0;
					}

				}
				if (readLine) {
					bulkSize += num;
					// System.out.println("\nbulkSize=" + bulkSize);
				} else {
					srqSize += num;
				}
				// statistics update
				totalBytes += num;
				bytesPerSecond += num;
			}
		} catch (IOException e) {
			System.err.println("error while reading data");
		}
	}

	/**
	 * Write char to port
	 * 
	 * Single char is written to port.
	 * 
	 * @param c
	 *            char to be written
	 * @throws IOException
	 */
	public void write(char c) throws IOException {
		final int wait = 5000000;
		int i = wait, j = i;
		outputStream.write((int) c);
		// inputStream.read();
		// busy wait
		while (i-- > 0) {
			j = i + j;
		}
		totalBytes++;
		bytesPerSecond++;
		bytesPerSecond += j + i;

	}

	/**
	 * Write string to port
	 * 
	 * String is written to port.
	 * 
	 * TODO: we get usually an error on first write, despite the write works.
	 * ??? -> needs analyzation.
	 * 
	 * @param c
	 *            char to be written
	 * @throws IOException
	 */
	public void write(String s) throws IOException {
		if (s == null)
			return;
		System.out.println("write(\"" + s + "\")");
		if (outputStream == null) {
			System.err
					.println("Output stream not initialized when writing data");
			return;
		}
		for (int i = 0; i < s.length(); i++)
			write(s.charAt(i));
	}

	/**
	 * list available serial ports to stdout
	 * 
	 * @return hash map with all available ports
	 */
	public HashSet<CommPortIdentifier> getAvailableSerialPorts() {
		HashSet<CommPortIdentifier> h = new HashSet<CommPortIdentifier>();
		Enumeration thePorts = CommPortIdentifier.getPortIdentifiers();
		while (thePorts.hasMoreElements()) {
			CommPortIdentifier com = (CommPortIdentifier) thePorts
					.nextElement();
			switch (com.getPortType()) {
			case CommPortIdentifier.PORT_SERIAL:
				try {
					CommPort thePort = com.open("CommUtil", 50);
					thePort.close();
					h.add(com);
				} catch (PortInUseException e) {
					System.out.println("Port, " + com.getName()
							+ ", is in use.");
				} catch (Exception e) {
					System.err.println("Failed to open port " + com.getName());
					e.printStackTrace();
				}
			}
		}
		return h;
	}

	/**
	 * serialPortEventListener
	 * 
	 * This is the event listener that is added to the serial port in open().
	 * For incoming data the listener is called. The listener itself calls
	 * read() to read the data if the current event equals
	 * SerialPortEvent.DATA_AVAILABLE. All other events are ignored so far.
	 * 
	 */
	class serialPortEventListener implements SerialPortEventListener {
		public void serialEvent(SerialPortEvent event) {
			// System.out.println("serialPortEventlistener");
			switch (event.getEventType()) {
			case SerialPortEvent.DATA_AVAILABLE:
				read();
				break;
			case SerialPortEvent.BI:
			case SerialPortEvent.CD:
			case SerialPortEvent.CTS:
			case SerialPortEvent.DSR:
			case SerialPortEvent.FE:
			case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
			case SerialPortEvent.PE:
			case SerialPortEvent.RI:
			default:
			}
		}
	}

	/**
	 * get Chars-Per-Second value for current transmission. Value is in chars/s.
	 * 
	 * @return chars per second value.
	 */
	public int getCps() {
		return bytesPerSecondSave;
	}

	/**
	 * get total transfered bytes for current running transmission. After a
	 * stop, the value of the last transmission is available until next start of
	 * a transmission.
	 * 
	 * @return
	 */
	public long getTotalBytes() {
		return totalBytes;
	}

	public void setTotalBytes(long bytes) {
		this.totalBytes = bytes;
	}

	/**
	 * Get value of "read line" behaviour.
	 * 
	 * @return
	 */
	public Boolean getReadLine() {
		return readLine;
	}

	/**
	 * Set "read line" behaviour. Set to true if data shall be collected in
	 * buffer until a CR or LF occurs in incoming datastream. Set to false to
	 * not use a buffer.
	 * 
	 * @param readLine
	 */
	public void setReadLine(Boolean readLine) {
		if (readLine == true)
			bulkSize = 0;
		readLineCounter = 0;
		this.readLine = readLine;
	}

	public byte[] getBulkBuffer() {
		return bulkBuffer;
	}

	public int getBulkSize() {
		return bulkSize;
	}

	/**
	 * Add a listener
	 * 
	 * @param listener
	 */
	public void removeBulkDataListener(BulkDataListener listener) {
		bdListener.remove(listener);
	}

	/**
	 * Remove a listener
	 * 
	 * @param listener
	 */
	public void addBulkDataListener(BulkDataListener listener) {
		if (!bdListener.contains(listener)) {
			bdListener.add(listener);
		}
	}

	/**
	 * Call all bulk data listeners
	 */
	void callBulkDataListener() {
		for (BulkDataListener l : bdListener) {
			l.complete(bulkBuffer, bulkSize);
		}
	}

	/**
	 * Add a srq listener
	 * 
	 * @param listener
	 */
	public void removeSrqDataListener(BulkDataListener listener) {
		srqListener.remove(listener);
	}

	/**
	 * Remove a srq listener
	 * 
	 * @param listener
	 */
	public void addSrqListener(BulkDataListener listener) {
		if (!srqListener.contains(listener)) {
			srqListener.add(listener);
		}
	}

	/**
	 * Call all srq listeners
	 */
	void callSrqListener() {
		for (BulkDataListener l : srqListener) {
			l.srq(srqBuffer, srqSize);
		}
	}

	public Boolean getStarted() {
		return started;
	}
}
