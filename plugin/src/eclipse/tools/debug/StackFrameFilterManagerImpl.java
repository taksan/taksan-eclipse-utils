package eclipse.tools.debug;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.internal.runtime.AdapterManager;
import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.core.runtime.IAdapterManager;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.IPreferenceChangeListener;
import org.eclipse.core.runtime.preferences.IEclipsePreferences.PreferenceChangeEvent;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.internal.core.StepFilterManager;
import org.eclipse.debug.internal.ui.viewers.model.provisional.TreeModelViewer;
import org.eclipse.debug.internal.ui.views.launch.LaunchView;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class StackFrameFilterManagerImpl implements StackFrameFilterManager {

	private static final String DEBUG_VIEW_ID = "org.eclipse.debug.ui.DebugView";
	private static final String DEBUG_VIEW_CONTENT_MODEL = "org.eclipse.jdt.debug.core.IJavaThread";
	private static final String DEFAULT_JAVA_THREAD_FACTORY = "MonitorsAdapterFactory";
	private static boolean hideFilteredFrames;

	public void start() {
		IAdapterManager manager= Platform.getAdapterManager();
		removeDefaultDebugFactoryFrom(manager);
		makeStackTraceFilterable(manager);
	}
	
	@Override
	public boolean isHideFilteredStackFrames() {
		return hideFilteredFrames;
	}
	
	@Override
	public void setHideFilteredStackFrames(boolean isHidden) {
		hideFilteredFrames=isHidden;
		refreshDebugView();
	}
	

	@SuppressWarnings("unchecked")
	private void removeDefaultDebugFactoryFrom(IAdapterManager manager) {
		HashMap<String,IAdapterFactory> factories = ((AdapterManager)manager).getFactories();
		List<IAdapterFactory> adapterFactories = (List<IAdapterFactory>) factories.get(DEBUG_VIEW_CONTENT_MODEL);
		
		IAdapterFactory factoryToRemove = null;
		if (adapterFactories == null)
			return;
		
		for (Object factory : adapterFactories) {
			if (factory.getClass().getName().contains(DEFAULT_JAVA_THREAD_FACTORY)) {
				factoryToRemove = (IAdapterFactory) factory;
			}
		}
		manager.unregisterAdapters(factoryToRemove, IJavaThread.class);
	}
	
	private void makeStackTraceFilterable(IAdapterManager manager) {
		IAdapterFactory filterableStackTraceProviderFactory = new FilterableJavaThreadContentFactory(this);
		manager.registerAdapters(filterableStackTraceProviderFactory, IJavaThread.class);
		
		listenPrefsToUpdateDebugViewAppropriately();
	}

	private void listenPrefsToUpdateDebugViewAppropriately() {
		IScopeContext context = InstanceScope.INSTANCE;
			
		IPreferenceChangeListener listener = new ListenStepFilterChangesToUpdateStackFrameView();
		IEclipsePreferences debugNode = context.getNode(DebugPlugin.getUniqueIdentifier());
		debugNode.addPreferenceChangeListener(listener);
		
		IEclipsePreferences debugUiNode = context.getNode(JDIDebugUIPlugin.getUniqueIdentifier());
		debugUiNode.addPreferenceChangeListener(listener);
		
		addHideStepsButton();
	}

	private void addHideStepsButton() {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
				LaunchView debugView = (LaunchView) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().findView(DEBUG_VIEW_ID);
				IToolBarManager tbm = debugView.getViewSite().getActionBars().getToolBarManager();
				IAction action = new HideFilteredStackFramesAction(StackFrameFilterManagerImpl.this);
				tbm.appendToGroup(IDebugUIConstants.RENDER_GROUP, 
						action);
				debugView.getViewSite().getActionBars().updateActionBars();
		    }});
	}
		
	private static void refreshDebugView() {
		IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		LaunchView debugView = (LaunchView) activePage.findView(DEBUG_VIEW_ID);
		TreeModelViewer viewer = (TreeModelViewer) debugView.getViewer();
		viewer.refresh();
	}

	private final class ListenStepFilterChangesToUpdateStackFrameView implements
			IPreferenceChangeListener {
		@Override
		public void preferenceChange(PreferenceChangeEvent event) {
			String activeFiltersList = IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST;
			String prefUseStepFilters = StepFilterManager.PREF_USE_STEP_FILTERS;
			List<String> watchablePrefs = Arrays.asList(activeFiltersList, prefUseStepFilters);

			if (watchablePrefs.contains(event.getKey())) {
				refreshDebugView();
			}
		}
	}
}
