package eclipse.tools;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;


/**
 * The activator class controls the plug-in life cycle
 */
public class TaksanUtilsPlugin extends AbstractUIPlugin {
	public static final String PLUGIN_ID = "taksan-eclipse-utils"; //$NON-NLS-1$

	private static TaksanUtilsPlugin plugin;

	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}
	
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static TaksanUtilsPlugin getDefault() {
		return plugin;
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	
	public static void log(Exception e) {
		plugin.getLog().log(errorStatus(e.getMessage(), e));
	}

	public static void log(String message, Exception e) {
		plugin.getLog().log(errorStatus(message, e));
	}
	
	public static void log(String message) {
		plugin.getLog().log(infoStatus(message));
	}
	
	private static IStatus infoStatus(String message) {
		return new Status(IStatus.INFO, PLUGIN_ID, IStatus.INFO, message, null);
	}
	
	private static IStatus errorStatus(String message, Throwable t) {
		return new Status(IStatus.ERROR, PLUGIN_ID, IStatus.ERROR, message, t);
	}

}
