package de.spurtikus.softtek;

import de.spurtikus.comm.serial.Communication;
import de.spurtikus.softtek.tek.Tek1241Device;
import de.spurtikus.softtek.tek.Tek1241RawDevice;

/**
 * this class implements singleton pattern for application-wide aspects.
 * 
 * @author dennis
 * 
 */
public class ApplicationSingleton {

	public static final int COLORCODING_OLDSCHOOL = 0;
	public static final int COLORCODING_EE = 1;

	/** application name */
	final String applicationName = "SoftTek";

	/** singleton pattern */
	static private ApplicationSingleton instance;

	/** the view */
	private View view = null;

	/** the navigationView data of this app */
	private NavigationView navigationView = null;
	/** the navigationView data of this app */
	private DevConfigView devConfigView = null;

	private Tek1241Device tekDevice;

	private Communication communication = null;

	private int displayColorCodingStyle = COLORCODING_EE;

	public static ApplicationSingleton getInstance() {
		if (instance == null) {
			instance = new ApplicationSingleton();
		}
		return instance;
	}

	private ApplicationSingleton() {
		super();
	}

	public NavigationView getNavigationView() {
		return navigationView;
	}

	public void setNavigationView(NavigationView navigationView) {
		this.navigationView = navigationView;
	}

	public DevConfigView getDevConfigView() {
		return devConfigView;
	}

	public void setDevConfigView(DevConfigView devConfigView) {
		this.devConfigView = devConfigView;
	}

	public String getApplicationName() {
		return applicationName;
	}

	public Tek1241Device getTekDevice() {
		return tekDevice;
	}

	public void setTekDevice(Tek1241Device tek) {
		this.tekDevice = tek;
	}

	public int getDisplayColorCodingStyle() {
		return displayColorCodingStyle;
	}

	public void setDisplayColorCodingStyle(int displayColorCodingStyle) {
		this.displayColorCodingStyle = displayColorCodingStyle;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	public Communication getCommunication() {
		if (communication == null)
			communication = new Communication();
		return communication;
	}

	public void setCommunication(Communication communication) {
		this.communication = communication;
	}

}