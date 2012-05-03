package objective;

import objective.ng.GotoMethodServer;

import org.eclipse.core.runtime.preferences.ConfigurationScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.IStartup;
import org.osgi.service.prefs.BackingStoreException;

@SuppressWarnings("restriction")
public class Startup implements IStartup {

	@SuppressWarnings("deprecation")
	@Override
	public void earlyStartup() {
		TemplateStore codeTemplateStore = JavaPlugin.getDefault().getCodeTemplateStore();
		Template methodBody = codeTemplateStore.findTemplate("methodbody");
		methodBody.setPattern("throw new RuntimeException(\"NOT IMPLEMENTED\");");
		
		Template constructorBody = codeTemplateStore.findTemplate("constructorbody");
		constructorBody.setPattern("throw new RuntimeException(\"NOT IMPLEMENTED\");");
		
		Template catchBlock = codeTemplateStore.findTemplate("catchblock");
		catchBlock.setPattern("throw new RuntimeException(${exception_var});");

		IEclipsePreferences preferences = ConfigurationScope.INSTANCE.getNode("org.eclipse.core.runtime");
		preferences.putBoolean("escapeStrings", true);
		try {
			preferences.flush();
		} catch (BackingStoreException e) {
			throw new RuntimeException(e);
		}
		
		startGotoMethodServer();
	}

	private void startGotoMethodServer() {
		new GotoMethodServer().start();
		
	}
}
