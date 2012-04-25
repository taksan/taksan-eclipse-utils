package objective_utils.actions;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
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
			ICompilationUnit createdTest = createTestClass(selection);
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

	private ICompilationUnit createTestClass(IStructuredSelection selection) throws JavaModelException {
		IJavaElement elem = getFirstElement(selection);

		if (elem == null) {
			MessageDialog.openWarning(fWindow.getShell(), 
					"Can't create test",
					"You need to select a class file to create a test");
			return null;
		}

		IPackageFragmentRoot newClassRoot = getNewClassRoot(elem);
		
		if (newClassRoot == null) {
			MessageDialog.openWarning(fWindow.getShell(), 
					"Can't create test",
					"You need a src/test/java folder");
			return null;
		}
		
		IPackageFragment testPackage = getTestPackage(elem, newClassRoot);

		String testUnitNameFileName = makeTestUnitName(elem);
		ICompilationUnit compilationUnit = testPackage.getCompilationUnit(testUnitNameFileName);
		boolean testAlreadyExists = compilationUnit != null;
		if (testAlreadyExists)
			return compilationUnit;
		
		return createTestClass(testPackage, testUnitNameFileName);
	}

	private ICompilationUnit createTestClass(IPackageFragment testPackage,
			String testUnitNameFileName) throws JavaModelException {
		IProgressMonitor monitor = getMonitor();
		ICompilationUnit parentUnit = testPackage.createCompilationUnit(testUnitNameFileName, "",
				false, monitor);

		parentUnit.becomeWorkingCopy(monitor);
		
		String typeName = testUnitNameFileName.replace(".java", "");
		IBuffer buffer = parentUnit.getBuffer();
		String testContent = "package " +testPackage.getElementName() + ";\n"+
				"\n" +
				"public class " + typeName + " { \n"+
				"}";
		buffer.setContents(testContent);

		IType createdType = parentUnit.getType(typeName);

		ICompilationUnit createdTestUnit = createdType.getCompilationUnit();
		createdTestUnit.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
		return createdTestUnit;
	}

	private String makeTestUnitName(IJavaElement elem) {
		return elem.getElementName().replaceFirst(".java", "Test.java");
	}

	private IPackageFragment getTestPackage(
			IJavaElement elem,
			IPackageFragmentRoot testRoot)
			throws JavaModelException {
		IProgressMonitor monitor = getMonitor();
		IPackageFragment originalPack = (IPackageFragment) elem
				.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
		IPackageFragment testPackage = testRoot.createPackageFragment(
				originalPack.getElementName(), 
				true, 
				monitor);
		return testPackage;
	}

	private IPackageFragmentRoot getNewClassRoot(IJavaElement elem)
			throws JavaModelException {
		IJavaProject project = elem.getJavaProject();
		IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();
		IPackageFragmentRoot testRoot = null;
		
		for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots) {
			if (iPackageFragmentRoot.getPath().toString().contains("src/test/java")) {
				testRoot = iPackageFragmentRoot;
			}
		}
		return testRoot;
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
