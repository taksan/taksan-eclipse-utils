package objective_utils.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.internal.editors.text.EditorsPlugin;

@SuppressWarnings("restriction")
public class CreateTestWizard extends Wizard implements INewWizard {
	private IWorkbenchWindow fWindow;
	private IStructuredSelection selection;

	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
		fWindow= workbench.getActiveWorkbenchWindow();
	}
	
	public boolean performFinish() {
		IWorkbenchPage activePage= fWindow.getActivePage();
		
		try {
			ICompilationUnit createdTest = createOrRetrieveTestCounterpart(selection);
			if (createdTest == null)
				return false;
			
			IResource resource = createdTest.getResource();
			
			IDE.openEditor(activePage, (IFile) resource, true);
		} catch (Exception e) {
			EditorsPlugin.log(e);
			return false;
		} 
		return true;
	}

	private ICompilationUnit createOrRetrieveTestCounterpart(IStructuredSelection selection) throws JavaModelException {
		IJavaElement elem = getFirstElement(selection);

		if (elem == null) {
			MessageDialog.openWarning(fWindow.getShell(), 
					"Can't create test",
					"You need to select a class file to create a test");
			return null;
		}

		IPackageFragmentRoot newClassRoot = getNewClassRootBasedOnSelectedElement(elem);
		
		if (newClassRoot == null) {
			MessageDialog.openWarning(fWindow.getShell(), 
					"Can't create test",
					"You need a src/test/java folder");
			return null;
		}
		
		IPackageFragment unitPackage = getNewUnitPackage(elem, newClassRoot);

		String newUnitNameFileName = makeNewUnitName(elem);
		ICompilationUnit compilationUnit = unitPackage.getCompilationUnit(newUnitNameFileName);
		boolean testAlreadyExists = compilationUnit.getResource().exists();
		if (testAlreadyExists)
			return compilationUnit;
		
		return createTestClass(unitPackage, newUnitNameFileName);
	}

	private ICompilationUnit createTestClass(
			IPackageFragment unitPackage,
			String unitFileName) 
					throws JavaModelException {
		
		IProgressMonitor monitor = getMonitor();
		ICompilationUnit parentUnit = unitPackage.createCompilationUnit(unitFileName, "",
				false, monitor);

		parentUnit.becomeWorkingCopy(monitor);
		
		String typeName = unitFileName.replace(".java", "");
		IBuffer buffer = parentUnit.getBuffer();
		String unitContentContent = "package " +unitPackage.getElementName() + ";\n"+
				"\n" +
				"public class " + typeName + " { \n"+
				"}";
		buffer.setContents(unitContentContent);

		IType createdType = parentUnit.getType(typeName);

		ICompilationUnit createdTestUnit = createdType.getCompilationUnit();
		createdTestUnit.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
		return createdTestUnit;
	}

	private String makeNewUnitName(IJavaElement elem) {
		String elementName = elem.getElementName();
		if (elementName.endsWith("Test.java")) {
			return elementName.replaceFirst("Test.java", ".java");
		}
		return elementName.replaceFirst(".java", "Test.java");
	}

	private IPackageFragment getNewUnitPackage(
			IJavaElement elem,
			IPackageFragmentRoot newUnitRoot)
			throws JavaModelException {
		IProgressMonitor monitor = getMonitor();
		IPackageFragment originalPack = (IPackageFragment) elem
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		IPackageFragment newUnitPackage = newUnitRoot.createPackageFragment(
				originalPack.getElementName(), 
				true, 
				monitor);
		return newUnitPackage;
	}

	private IPackageFragmentRoot getNewClassRootBasedOnSelectedElement(IJavaElement elem)
			throws JavaModelException {
		IJavaProject project = elem.getJavaProject();
		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		
		String selecteElemPath = elem.getResource().getFullPath().toFile().getAbsolutePath();
		String target = "test";
		if (selecteElemPath.contains("/test/java")) {
			target = "main";
		}
		
		for (IPackageFragmentRoot fragment : packageFragmentRoots) {
			IPath path = fragment.getPath();
			String fragmentPath = path.toFile().getAbsolutePath();
			if (fragmentPath.contains("src/"+target+"/java")) {
				return fragment;
			}
		}
		return null;
	}

	private IJavaElement getFirstElement(IStructuredSelection selection) {
		IJavaElement elem = null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement = selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable = (IAdaptable) selectedElement;

				elem = (IJavaElement) adaptable.getAdapter(IJavaElement.class);
			}
		}
		return elem;
	}

	private IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}
}
