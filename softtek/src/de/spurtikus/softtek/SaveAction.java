package de.spurtikus.softtek;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;

import org.eclipse.ui.IWorkbenchWindow;

public class SaveAction extends Action {

	private final IWorkbenchWindow window;
	private final String viewId;

	public SaveAction(IWorkbenchWindow window, String label, String viewId) {
		this.window = window;
		this.viewId = viewId;
		setText(label);
		// The id is used to refer to the action in a menu or toolbar
		setId(ICommandIds.CMD_SAVE);
		// Associate the action with a pre-defined command, to allow key
		// bindings.
		setActionDefinitionId(ICommandIds.CMD_SAVE);
		setImageDescriptor(softtek.Activator
				.getImageDescriptor("/icons/save.gif"));
	}

	public void run() {
		//System.out.println("SAVE called");
		FileDialog dialog = new FileDialog(window.getShell(), SWT.OPEN);
		dialog.setText("Save a tek data file (setup+memory data)");
		String[] filterExt = { "*.tek", "*.*" };
		dialog.setFilterExtensions(filterExt);
		String selectedFile = dialog.open();
		if (selectedFile != null) {
			//System.out.println("SAVE to: " + selectedFile);
			ApplicationSingleton softTekApp = ApplicationSingleton.getInstance();
			softTekApp.getView().saveImages(selectedFile);
		}
	}
}
