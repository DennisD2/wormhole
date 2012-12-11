package de.spurtikus.softtek;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IWorkbenchWindow;

public class LoadAction extends Action {

	private final IWorkbenchWindow window;

	LoadAction(String text, IWorkbenchWindow window) {
		super(text);
		this.window = window;
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_OPEN_MESSAGE);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(ICommandIds.CMD_OPEN_MESSAGE);
		setImageDescriptor(softtek.Activator
				.getImageDescriptor("/icons/load.gif"));
	}

	public void run() {
		// System.out.println("LOAD called");
		FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN);
		dialog.setText("Load a tek data file (setup+memory data)");
		String[] filterExt = { "*.tek", "*.*" };
		dialog.setFilterExtensions(filterExt);
		String selectedFile = dialog.open();
		if (selectedFile != null) {
			// System.out.println("LOAD from: " + selectedFile);
			ApplicationSingleton softTekApp = ApplicationSingleton
					.getInstance();
			softTekApp.getView().clearView();
			softTekApp.getView().loadImages(selectedFile);
			softTekApp.getView().drawTekData();
		}
	}
}