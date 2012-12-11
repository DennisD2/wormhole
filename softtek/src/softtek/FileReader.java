package softtek;

import java.io.FileInputStream;
import java.io.IOException;

/**
 * Class handles all types and methods required for file access.
 * 
 * @author dennis
 * 
 */
public class FileReader {

	//Log logger = LogFactory.getLog(FileReader.class);

	/**
	 * name of this file
	 */
	private String fileName;

	/**
	 * input stream associated to this file
	 */
	private FileInputStream in;

	/**
	 * buffer containing file content
	 */
	private byte buffer[];

	/**
	 * buffer size
	 */
	private int bufferSize = 0;

	/**
	 * constructor.
	 * 
	 * @param path
	 *            to file
	 */
	public FileReader(String theFile) {
		fileName = theFile;
		initialize();
	}

	/**
	 * initializes object before use
	 */
	private void initialize() {
		in = null;
	}

	/**
	 * This method opens an input file. It sets the bufferSize variable to the
	 * number of bytes of the file.
	 * 
	 * @return true if open is successful, else false.
	 */
	protected boolean do_open() {
		int size;

		//logger.debug("opening file " + fileName);
		try {
//			File dir = new File(".");
//
//			String[] list = dir.list();
//
//			for (int i = 0; i < list.length; i++)  {
//				System.out.println( list[i]+"\n");
//			}
			
			in = new FileInputStream(fileName);
			size = in.available();
		} catch (IOException e) {
			//logger.error("cannot open file " + fileName + ", " + e.getMessage());
			return false;
		}
		//logger.debug("opening file " + fileName + " -> ok, " + size
		//		+ " bytes available");
		bufferSize = size;
		return true;
	}

	/**
	 * This method reads the input file. It reads in the file content into the
	 * buffer and sets the bufferSize variable to the number of bytes read in.
	 * 
	 * @return number of bytes read in
	 */
	protected int do_read() {
		int size;

		// ensure that input stream is defined (that do_open was called before)
		if (in == null)
			return 0;

		// allocate buffer for read
		buffer = new byte[bufferSize];
		try {
			// read in file in one chunk
			size = in.read(buffer, 0, bufferSize);
		} catch (IOException e) {
			//logger.error("cannot read from stream " + fileName + ", "
			//		+ e.getMessage());
			return 0;
		}
		//logger.debug(bufferSize + " bytes read from file to buffer.");

		//assert (bufferSize == size);
		return size;
	}

	/**
	 * This method closes a file reader.
	 * 
	 * @return true on successful close, false otherwise.
	 */
	protected boolean do_close() {

		if (in == null)
			return false;

		try {
			in.close();
		} catch (IOException e) {
			//logger.error("cannot close stream " + fileName + ", "
			//		+ e.getMessage());
			return false;
		}
		in = null;
		//logger.debug("closeFile " + fileName + " -> ok");
		return true;
	}

	/**
	 * This method returns the input buffer (containing the raw input file).
	 * 
	 * @return byte
	 */
	public byte[] getBuffer() {
		return buffer;
	}

	/**
	 * This method returns the buffer size (containing the raw input file)
	 * 
	 * @return value of bufferSize.
	 */
	public int getBufferSize() {
		return bufferSize;
	}

	/**
	 * public method for reading the file. read() opens the file, reads it into
	 * the buffer and closes the file.
	 * 
	 * @return Size of file read in.
	 */
	public int read() {
		Boolean retVal = do_open();
		int size = 0;

		if (retVal) {
			size = do_read();
			retVal = do_close();
			//assertTrue(retVal);
			//assertEquals(getBufferSize(), size);
		}
		return size;
	}

	/**
	 * check for existence of a file.
	 * 
	 * @return true if file exists, false otherwise.
	 */
	public boolean exists() {
		//logger.debug("exists(\"" + fileName + "\")");
		try {
			in = new FileInputStream(fileName);
		} catch (IOException e) {
			//logger.debug("exists(\"" + fileName + "\") -> false, "
			//		+ e.getMessage());
			return false;
		}
		//logger.debug("exists(\"" + fileName + "\") -> true");
		return true;
	}

	/**
	 * opens a file.
	 */
	public boolean open() {
		return do_open();
	}
	
	/**
	 * closes a file.
	 */
	public boolean close() {
		return do_close();
	}

}
