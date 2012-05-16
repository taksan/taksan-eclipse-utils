package objective;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.model.IDebugElement;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenCountUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IHasChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.ui.JavaDebugUtils;
import org.eclipse.jdt.internal.debug.core.model.JDIStackFrame;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jdt.internal.debug.ui.IJDIPreferencesConstants;
import org.eclipse.jdt.internal.debug.ui.JDIDebugUIPlugin;
import org.eclipse.jdt.internal.debug.ui.monitors.JavaElementContentProvider;
import org.eclipse.jdt.internal.debug.ui.monitors.NoMonitorInformationElement;
import org.eclipse.jface.preference.IPreferenceStore;

@SuppressWarnings("restriction")
public class FilteredJavaThreadContentProvider extends
		JavaElementContentProvider {
	private static final Pattern STEPFILTER_ESCAPE = Pattern
			.compile("\\.|\\$|\\||\\+");
	private static final Pattern STEPFILTER_WILDCARD = Pattern.compile("\\*");
	private static final Pattern STEPFILTER_SEPARATOR = Pattern.compile(",");

	protected int getChildCount(Object element, IPresentationContext context,
			IViewerUpdate monitor) throws CoreException {
		IJavaThread thread = (IJavaThread) element;
		if (!thread.isSuspended()) {
			return 0;
		}
		int childCount = getStackFrames(thread).length;
		if (isDisplayMonitors()) {
			if (((IJavaDebugTarget) thread.getDebugTarget())
					.supportsMonitorInformation()) {
				childCount += thread.getOwnedMonitors().length;
				if (thread.getContendedMonitor() != null)
					childCount++;
			} else {
				childCount++;
			}
		}
		return childCount;
	}

	protected Object[] getChildren(Object parent, int index, int length,
			IPresentationContext context, IViewerUpdate monitor)
			throws CoreException {
		IJavaThread thread = (IJavaThread) parent;
		if (!thread.isSuspended()) {
			return EMPTY;
		}
		return getElements(getChildren(thread), index, length);
	}

	protected Object[] getChildren(IJavaThread thread) {
		try {
			if ((thread instanceof JDIThread)) {
				JDIThread jThread = (JDIThread) thread;
				if ((!jThread.getDebugTarget().isSuspended())
						&& (jThread.isSuspendVoteInProgress())) {
					return EMPTY;
				}
			}

			IStackFrame[] frames = getStackFrames(thread);
			if (!isDisplayMonitors()) {
				return frames;
			}

			int length = frames.length;
			Object[] children;
			if (((IJavaDebugTarget) thread.getDebugTarget())
					.supportsMonitorInformation()) {
				IDebugElement[] ownedMonitors = JavaDebugUtils
						.getOwnedMonitors(thread);
				IDebugElement contendedMonitor = JavaDebugUtils
						.getContendedMonitor(thread);

				if (ownedMonitors != null) {
					length += ownedMonitors.length;
				}
				if (contendedMonitor != null) {
					length++;
				}
				children = new Object[length];
				if ((ownedMonitors != null) && (ownedMonitors.length > 0)) {
					System.arraycopy(ownedMonitors, 0, children, 0,
							ownedMonitors.length);
				}
				if (contendedMonitor != null) {
					children[ownedMonitors.length] = contendedMonitor;
				}
			} else {
				children = new Object[length + 1];
				children[0] = new NoMonitorInformationElement(
						thread.getDebugTarget());
			}
			int offset = children.length - frames.length;
			System.arraycopy(frames, 0, children, offset, frames.length);
			return children;
		} catch (DebugException e) {
		}
		return EMPTY;
	}

	protected boolean hasChildren(Object element, IPresentationContext context,
			IViewerUpdate monitor) throws CoreException {
		if ((element instanceof JDIThread)) {
			JDIThread jThread = (JDIThread) element;
			if ((!jThread.getDebugTarget().isSuspended())
					&& (jThread.isSuspendVoteInProgress())) {
				return false;
			}

		}

		return (((IJavaThread) element).hasStackFrames())
				|| ((isDisplayMonitors()) && (((IJavaThread) element)
						.hasOwnedMonitors()));
	}

	protected ISchedulingRule getRule(IChildrenCountUpdate[] updates) {
		return getThreadRule(updates);
	}

	protected ISchedulingRule getRule(IChildrenUpdate[] updates) {
		return getThreadRule(updates);
	}

	protected ISchedulingRule getRule(IHasChildrenUpdate[] updates) {
		return getThreadRule(updates);
	}

	private ISchedulingRule getThreadRule(IViewerUpdate[] updates) {
		if (updates.length > 0) {
			Object element = updates[0].getElement();
			if ((element instanceof JDIThread)) {
				return ((JDIThread) element).getThreadRule();
			}
		}
		return null;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private IStackFrame[] getStackFrames(IJavaThread thread)
			throws DebugException {
		IStackFrame[] stackFrames = thread.getStackFrames();
		if (!DebugPlugin.isUseStepFilters())
			return stackFrames;
		List frames = new ArrayList();
		Pattern filters = getStepFilters();
		for (int i = 0; i < stackFrames.length; i++) {
			JDIStackFrame frame = (JDIStackFrame) stackFrames[i];
			boolean exclude = filters.matcher(frame.getDeclaringTypeName())
					.matches();
			if (!exclude)
				frames.add(stackFrames[i]);
		}
		return (IStackFrame[]) frames.toArray(new IStackFrame[frames.size()]);
	}

	private Pattern getStepFilters() {
		IPreferenceStore store = JDIDebugUIPlugin.getDefault()
				.getPreferenceStore();
		String filters = store
				.getString(IJDIPreferencesConstants.PREF_ACTIVE_FILTERS_LIST);
		filters = STEPFILTER_ESCAPE.matcher(filters).replaceAll("\\\\$0");
		filters = STEPFILTER_WILDCARD.matcher(filters).replaceAll(".*");
		filters = STEPFILTER_SEPARATOR.matcher(filters).replaceAll("|");
		Pattern pattern = Pattern.compile("^(" + filters + ")$");
		return pattern;
	}
}