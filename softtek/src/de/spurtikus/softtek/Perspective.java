package de.spurtikus.softtek;

import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class Perspective implements IPerspectiveFactory {

	/**
	 * The ID of the perspective as specified in the extension.
	 */
	public static final String ID = "softtek.perspective";

	public void createInitialLayout(IPageLayout layout) {
		String editorArea = layout.getEditorArea();
		layout.setEditorAreaVisible(false);
		
		//layout.addStandaloneView(NavigationView.ID,  false, IPageLayout.LEFT, 0.25f, editorArea);
		IFolderLayout folder = layout.createFolder("xx", IPageLayout.LEFT, 0.25f, editorArea);
		folder.addView(NavigationView.ID);
		folder.addView(DevConfigView.ID);
		//IFolderLayout folder = layout.createFolder("messages", IPageLayout.TOP, 0.5f, editorArea);
		//folder.addPlaceholder(View.ID + ":*");
		//folder.addView(View.ID);
		layout.addView(View.ID, IPageLayout.RIGHT, 0.75f, editorArea);
		
		layout.getViewLayout(NavigationView.ID).setCloseable(false);
	}
}
