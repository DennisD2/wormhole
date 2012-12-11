package softtek;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Class handles all types and methods required for file access.
 * 
 * @author dennis
 * 
 */
public class FileWriter {

	// Log logger = LogFactory.getLog(FileReader.class);

	/**
	 * name of this file
	 */
	private String fileName;

	/**
	 * input stream associated to this file
	 */
	private FileOutputStream out;

	/**
	 * constructor.
	 * 
	 * @param path
	 *            to file
	 */
	public FileWriter(String theFile) {
		fileName = theFile;
		initialize();
	}

	/**
	 * initializes object before use
	 */
	private void initialize() {
		out = null;
	}

	/**
	 * This method opens an input file. It sets the bufferSize variable to the
	 * number of bytes of the file.
	 * 
	 * @return true if open is successful, else false.
	 */
	protected boolean do_open() {
		int size;

		// logger.debug("opening file " + fileName);
		try {
			// File dir = new File(".");
			//
			// String[] list = dir.list();
			//
			// for (int i = 0; i < list.length; i++) {
			// System.out.println( list[i]+"\n");
			// }

			out = new FileOutputStream(fileName);
		} catch (IOException e) {
			// logger.error("cannot open file " + fileName + ", " +
			// e.getMessage());
			return false;
		}
		// logger.debug("opening file " + fileName + " -> ok, " + size
		// + " bytes available");
		return true;
	}

	/**
	 * This method writes the file.
	 * 
	 * @param buffer
	 * 
	 * @return number of bytes ...
	 */
	protected boolean do_write(byte[] buffer) {
		// ensure that input stream is defined (that do_open was called before)
		if (out == null)
			return false;

		try {
			// write file in one chunk
			out.write(buffer);
		} catch (IOException e) {
			// logger.error("cannot read from stream " + fileName + ", "
			// + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * This method writes the file.
	 * 
	 * @param buffer
	 * 
	 * @return number of bytes ...
	 */
	protected boolean do_write(byte[] buffer, int size) {
		// ensure that input stream is defined (that do_open was called before)
		if (out == null)
			return false;

		try {
			// write file in one chunk
			out.write(buffer, 0, size);
		} catch (IOException e) {
			// logger.error("cannot read from stream " + fileName + ", "
			// + e.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * This method closes a file writer.
	 * 
	 * @return true on successful close, false otherwise.
	 */
	protected boolean do_close() {

		if (out == null)
			return false;

		try {
			out.close();
		} catch (IOException e) {
			// logger.error("cannot close stream " + fileName + ", "
			// + e.getMessage());
			return false;
		}
		out = null;
		// logger.debug("closeFile " + fileName + " -> ok");
		return true;
	}

	/**
	 * public method for writing the file. 
	 * 
	 * @return Size of file read in.
	 */
	public boolean write(byte[] buffer) {
		if (!do_write(buffer))
			return false;
		return true;
	}

	/**
	 * public method for writing the file. 
	 * 
	 * @return Size of file read in.
	 */
	public boolean write(byte[] buffer, int size) {
		if (!do_write(buffer, size))
			return false;
		return true;
	}

	/**
	 * check for existence of a file.
	 * 
	 * @return true if file exists, false otherwise.
	 */
	public boolean exists() {
		// logger.debug("exists(\"" + fileName + "\")");
		try {
			FileInputStream in = new FileInputStream(fileName);
		} catch (IOException e) {
			// logger.debug("exists(\"" + fileName + "\") -> false, "
			// + e.getMessage());
			return false;
		}
		// logger.debug("exists(\"" + fileName + "\") -> true");
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
