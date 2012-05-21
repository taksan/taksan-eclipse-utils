package eclipse.tools.debug;


import org.eclipse.core.runtime.IAdapterFactory;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementContentProvider;
import org.eclipse.jdt.debug.core.IJavaThread;

@SuppressWarnings({ "rawtypes", "restriction" })
class FilterableJavaThreadContentFactory implements
		IAdapterFactory {
	Object fgCPThread;
	private StackFrameFilterManager manager;
	
	public FilterableJavaThreadContentFactory(StackFrameFilterManager manager) {
		this.manager = manager;
	}

	@Override
	public Class[] getAdapterList() {
		return new Class[] {IElementContentProvider.class};
	}

	@Override
	public Object getAdapter(Object adaptableObject, Class adapterType) {
		if (IElementContentProvider.class.equals(adapterType)) {
			if (adaptableObject instanceof IJavaThread) {
	        	return getThreadPresentation();
	        }
		}
		return null;
	}

	private Object getThreadPresentation() {
		if (fgCPThread == null) {
			fgCPThread = new FilteredJavaThreadContentProvider(manager);
		}
		return fgCPThread;
	}
}