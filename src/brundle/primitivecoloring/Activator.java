package brundle.primitivecoloring;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
	PrimitiveWatcher partListener = new PrimitiveWatcher();

	// The plug-in ID
	public static final String PLUGIN_ID = "brundle.primitiveColoring"; //$NON-NLS-1$

	private static Activator plugin;

	public Activator() {
	}

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
		IWorkbench workbench = getWorkbench();
		IPartListener2 partListener = new PrimitiveWatcher();
		for (final IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			w.getPartService().addPartListener(partListener);
		}
		workbench.addWindowListener(windowListener);
		annotateAllEditors(workbench);
	}

	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	private void annotateAllEditors(IWorkbench workbench) {
		for (final IWorkbenchWindow w : workbench.getWorkbenchWindows()) {
			for (final IWorkbenchPage p : w.getPages()) {
				for (final IEditorReference e : p.getEditorReferences()) {
					annotateEditor(e);
				}
			}
		}
	}

	private void annotateEditor(IWorkbenchPartReference partref) {
		IWorkbenchPart part = partref.getPart(false);
		if (part instanceof ITextEditor) {
			PrimitiveAnnotationModel.attach((ITextEditor) part);
		}
	}
	
	private IWindowListener windowListener = new IWindowListener() {
		public void windowOpened(IWorkbenchWindow window) {
			window.getPartService().addPartListener(partListener);
		}

		public void windowClosed(IWorkbenchWindow window) {
			window.getPartService().removePartListener(partListener);
		}

		public void windowActivated(IWorkbenchWindow window) { }

		public void windowDeactivated(IWorkbenchWindow window) { }
	};

	private final class PrimitiveWatcher implements IPartListener2 {

		@Override
		public void partOpened(IWorkbenchPartReference partRef) {
			annotateEditor(partRef);
		}

		@Override public void partActivated(IWorkbenchPartReference partRef) { }

		@Override public void partBroughtToTop(IWorkbenchPartReference partRef) { }

		@Override public void partClosed(IWorkbenchPartReference partRef) { }

		@Override public void partDeactivated(IWorkbenchPartReference partRef) { }

		@Override public void partHidden(IWorkbenchPartReference partRef) { }

		@Override public void partVisible(IWorkbenchPartReference partRef) { }

		@Override public void partInputChanged(IWorkbenchPartReference partRef) { }
	}	
}
