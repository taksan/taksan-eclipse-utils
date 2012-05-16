package objective.debug;

import java.util.HashMap;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.debug.core.IJavaThread;

@SuppressWarnings("restriction")
public class FilterStackTraceManager {

	public void makeStackTraceFilterable() {
		IAdapterManager manager= Platform.getAdapterManager();
		removeDefaultDebugFactory(manager);
		
		IAdapterFactory filterableStackTraceProviderFactory = new FilterableStackTraceProviderFactory();
		manager.registerAdapters(filterableStackTraceProviderFactory, IJavaThread.class);
	}

	@SuppressWarnings("unchecked")
	private void removeDefaultDebugFactory(IAdapterManager manager) {
		HashMap<String,IAdapterFactory> factories = ((AdapterManager)manager).getFactories();
		List<IAdapterFactory> adapterFactories = (List<IAdapterFactory>) factories.get("org.eclipse.jdt.debug.core.IJavaThread");
		IAdapterFactory factoryToRemove = null;
		
		for (Object factory : adapterFactories) {
			if (factory.getClass().getName().contains("MonitorsAdapterFactory")) {
				factoryToRemove = (IAdapterFactory) factory;
			}
		}
		manager.unregisterAdapters(factoryToRemove, IJavaThread.class);
	}


}
