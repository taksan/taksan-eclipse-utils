package eclipse.tools;


import java.io.IOException;

import org.eclipse.jdt.internal.ui.JavaPlugin;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.text.templates.Template;
import org.eclipse.jface.text.templates.persistence.TemplateStore;
import org.eclipse.ui.IStartup;

import eclipse.tools.debug.StackFrameFilterManagerImpl;

@SuppressWarnings("restriction")
public class Startup implements IStartup {

	@Override
	public void earlyStartup() {
		new StackFrameFilterManagerImpl().start();
		JavaPlugin javaPlugin = JavaPlugin.getDefault();
		IPreferenceStore preferences = javaPlugin.getPreferenceStore(); 
		preferences.setValue("escapeStrings", true);
		
		setPreferredTemplates();
	}

	@SuppressWarnings("deprecation")
	private void setPreferredTemplates() {
		TemplateStore codeTemplateStore = JavaPlugin.getDefault().getCodeTemplateStore();
		Template methodBody = codeTemplateStore.findTemplate("methodbody");
		methodBody.setPattern("throw new RuntimeException(\"NOT IMPLEMENTED\");");
		
		Template constructorBody = codeTemplateStore.findTemplate("constructorbody");
		constructorBody.setPattern("throw new RuntimeException(\"NOT IMPLEMENTED\");");
		
		Template catchBlock = codeTemplateStore.findTemplate("catchblock");
		catchBlock.setPattern("throw new RuntimeException(${exception_var});");

		saveOrIgnore(codeTemplateStore);
	}

	private void saveOrIgnore(TemplateStore codeTemplateStore) {
		try {
			codeTemplateStore.save();
		} catch (IOException e) {
		}
	}
}
