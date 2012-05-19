package eclipse.tools;


import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.IStartup;

import eclipse.tools.debug.FilterStackTraceManager;

@SuppressWarnings("restriction")
public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		new FilterStackTraceManager().makeStackTraceFilterable();
		JavaPlugin javaPlugin = JavaPlugin.getDefault();
		IPreferenceStore preferences = javaPlugin.getPreferenceStore(); 
		preferences.setValue("escapeStrings", true);
	}
}
