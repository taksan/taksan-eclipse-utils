package eclipse.tools.debug;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.StepFilterManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class FilterStackTraceManager {

	public void makeStackTraceFilterable() {
		IAdapterManager manager= Platform.getAdapterManager();
		removeDefaultDebugFactory(manager);
		IAdapterFactory filterableStackTraceProviderFactory = new FilterableStackTraceProviderFactory();
		manager.registerAdapters(filterableStackTraceProviderFactory, IJavaThread.class);
		
		listenPrefsToUpdateDebugViewAppropriately();
	}



	private void listenPrefsToUpdateDebugViewAppropriately() {
		IScopeContext context = InstanceScope.INSTANCE;
			
		IPreferenceChangeListener listener = new IPreferenceChangeListener() {
			@Override
			public void preferenceChange(PreferenceChangeEvent event) {
				String activeFiltersList = IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST;
				String prefUseStepFilters = StepFilterManager.PREF_USE_STEP_FILTERS;
				List<String> watchablePrefs = Arrays.asList(activeFiltersList,prefUseStepFilters);
				
				if (watchablePrefs.contains(event.getKey())) {
					LaunchView debugView = (LaunchView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView("org.eclipse.debug.ui.DebugView");
					TreeModelViewer viewer = (TreeModelViewer) debugView.getViewer();
					viewer.refresh();
				}
			}
		};
		IEclipsePreferences debugNode = context.getNode(DebugPlugin.getUniqueIdentifier());
		debugNode.addPreferenceChangeListener(listener);
		
		IEclipsePreferences debugUiNode = context.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		debugUiNode.addPreferenceChangeListener(listener);
	}
	


	@SuppressWarnings("unchecked")
	private void removeDefaultDebugFactory(IAdapterManager manager) {
		HashMap<String,IAdapterFactory> factories = ((AdapterManager)manager).getFactories();
		List<IAdapterFactory> adapterFactories = (List<IAdapterFactory>) factories.get("org.eclipse.jdt.debug.core.IJavaThread");
		IAdapterFactory factoryToRemove = null;
		if (adapterFactories == null)
			return;
		for (Object factory : adapterFactories) {
			if (factory.getClass().getName().contains("MonitorsAdapterFactory")) {
				factoryToRemove = (IAdapterFactory) factory;
			}
		}
		manager.unregisterAdapters(factoryToRemove, IJavaThread.class);
	}


}
