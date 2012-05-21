package eclipse.tools.debug;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;

import eclipse.tools.TaksanUtilsPlugin;

public class HideFilteredStackFramesAction extends Action implements IAction {

	boolean isFilterOn = false;
	private final StackFrameFilterManager manager;
	
	public HideFilteredStackFramesAction(StackFrameFilterManager manager) {
		this.manager = manager;
	}
	
	public ImageDescriptor getImageDescriptor() {
		return TaksanUtilsPlugin.getImageDescriptor("icons/hide_filter_steps.png");
	}
	
	@Override
	public void run() {
		this.manager.setHideFilteredStackFrames(isChecked());
	}
	
	@Override
	public String getToolTipText() {
		return "Hides/Shows the stack trace steps defined in step filters";
	}
	
	@Override
	public void setChecked(boolean checked) {
		isFilterOn = checked;
	}

	@Override
	public boolean isChecked() {
		return isFilterOn;
	}
	
	public int getStyle() {
    	return AS_CHECK_BOX;
    }
}
