package de.spurtikus.softtek;

import java.io.IOException;
import java.io.PrintStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleOutputStream;


public class Console  {
	private final String defaultTitle = "My console";
	
	private IOConsole myConsole;
	private IOConsoleOutputStream outputStream;
	//private IOConsoleInputStream inputStream;
	
	public Console(String title) {
		// setup title
		if (title == null)
			title = defaultTitle;
		// create console object
		myConsole = new IOConsole(title, null);
		// adds console
		ConsolePlugin.getDefault().getConsoleManager()
				.addConsoles(new IConsole[] { myConsole });
		;
		// shows console
		ConsolePlugin.getDefault().getConsoleManager()
				.showConsoleView(myConsole);
		
		// attach System.out to consoles output stream
		outputStream = myConsole.newOutputStream();
		//final PrintStream oldOut = System.out;
		System.setOut(new PrintStream(outputStream));
		
		// get consoles input as an input stream
		//inputStream = myConsole.getInputStream();
		
		Display dpy = PlatformUI.getWorkbench().getDisplay();
		dpy.addFilter(SWT.KeyDown, new Listener() {
			@Override
			public void handleEvent(Event event) {
				char c = event.character;
				//System.out.println(c);
				//Communication comm = ApplicationSingleton.getInstance().getCommunicationInstance();
				//try {
					//comm.write(c);
				//} catch (IOException e) {
					// TODO Auto-generated catch block
					//e.printStackTrace();
				//}
			}
		});

		
	}

	/**
	 * @return
	 */
	public Color getColor() {
		return outputStream.getColor();
	}

	/**
	 * @return
	 */
	public IOConsole getConsole() {
		return myConsole;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode() {
		return outputStream.hashCode();
	}

	/**
	 * @param message
	 * @throws IOException 
	 */
	public void print(String message) throws IOException {
		outputStream.write(message);
	}

	/**
	 * @throws IOException 
   * 
   */
	public void println() throws IOException {
		outputStream.write("\n");
	}

	/**
	 * @param message
	 * @throws IOException 
	 */
	public void println(String message) throws IOException {
		outputStream.write(message+"\n");
	}

	/**
	 * @param color
	 */
	public void setColor(Color color) {
		outputStream.setColor(color);
	}


}