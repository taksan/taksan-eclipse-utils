package objective_utils.actions;

import java.io.ByteArrayInputStream;
import java.io.File;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.internal.corext.util.JavaModelUtil;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;

public class CreateTestAction implements org.eclipse.ui.IWorkbenchWizard {
	private IProgressMonitor monitor;


	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		Object firstElement = ((StructuredSelection)selection).getFirstElement();
		
		if (!(firstElement instanceof IFile)) {
			MessageDialog.openWarning(workbench.getActiveWorkbenchWindow().getShell(), 
					"Can't create test", 
					"You need to select a class file to create a test");
			return;
		}
			
		IFile currentFile = (IFile)firstElement;
		IFile file = currentFile.getProject().getFile("test");
		
		String name = currentFile.getName().replace(".java", "");
		name+="Test";
		String testPath = currentFile.getFullPath().toFile().getPath().replace("main", "test").replace(".java","Test.java");
		IFile newFile = currentFile.getProject().getFile("/bar/src/test/java/some/pkg/FoopTest.java");
		try {
			newFile.create(null, true, null);
		} catch (CoreException e1) {
			e1.printStackTrace();
		}
		File testFile = new File(testPath);
		try {
			String initialContents = "public class "+  name + " {\n"+
					"\n" +
					"}";
			ByteArrayInputStream contents = new ByteArrayInputStream(initialContents.getBytes());
			
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}
	
	private void createTestClass(IStructuredSelection selection){
		IJavaElement elem = getFirstElement(selection);
		

		
	
		if (elem != null) {
			IPackageFragmentRoot root= null;
			root= JavaModelUtil.getPackageFragmentRoot(elem);
	
			IPackageFragment pack = (IPackageFragment) elem.getAncestor(IJavaElement.PACKAGE_FRAGMENT);
	
			if (!pack.exists()) {
					String packName= pack.getElementName();
					pack= root.createPackageFragment(packName, true, new SubProgressMonitor(monitor, 1));
				} else {
	
				String cuName;
				ICompilationUnit parentCU= pack.createCompilationUnit(
						cuName, "", false, new SubProgressMonitor(monitor, 2));
	
	
				String cuName= getCompilationUnitName(typeName);
				ICompilationUnit parentCU= pack.createCompilationUnit(cuName, "", false, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
				// create a working copy with a new owner
	
				needsSave= true;
				parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1)); // cu is now a (primary) working copy
				connectedCU= parentCU;
	
				IBuffer buffer= parentCU.getBuffer();
	
				String simpleTypeStub= constructSimpleTypeStub();
				String cuContent= constructCUContent(parentCU, simpleTypeStub, lineDelimiter);
				buffer.setContents(cuContent);
	
				CompilationUnit astRoot= createASTForImports(parentCU);
				existingImports= getExistingImports(astRoot);
	
				imports= new ImportsManager(astRoot);
				// add an import that will be removed again. Having this import solves 14661
				imports.addImport(JavaModelUtil.concatenateName(pack.getElementName(), typeName));
	
				String typeContent= constructTypeStub(parentCU, imports, lineDelimiter);
	
				int index= cuContent.lastIndexOf(simpleTypeStub);
				if (index == -1) {
					AbstractTypeDeclaration typeNode= (AbstractTypeDeclaration) astRoot.types().get(0);
					int start= ((ASTNode) typeNode.modifiers().get(0)).getStartPosition();
					int end= typeNode.getStartPosition() + typeNode.getLength();
					buffer.replace(start, end - start, typeContent);
				} else {
					buffer.replace(index, simpleTypeStub.length(), typeContent);
				}
	
				createdType= parentCU.getType(typeName);
	
		ICompilationUnit cu= createdType.getCompilationUnit();
		cu.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
	}

	private IJavaElement getFirstElement(IStructuredSelection selection) {
		IJavaElement elem= null;
		if (selection != null && !selection.isEmpty()) {
			Object selectedElement= selection.getFirstElement();
			if (selectedElement instanceof IAdaptable) {
				IAdaptable adaptable= (IAdaptable) selectedElement;
	
				elem= (IJavaElement) adaptable.getAdapter(IJavaElement.class);
			}
		}
		return elem;
	}

	@Override
	public void addPages() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean canFinish() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public void createPageControls(Composite pageContainer) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public void dispose() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardContainer getContainer() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public Image getDefaultPageImage() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IDialogSettings getDialogSettings() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardPage getNextPage(IWizardPage page) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardPage getPage(String pageName) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public int getPageCount() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardPage[] getPages() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardPage getPreviousPage(IWizardPage page) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public IWizardPage getStartingPage() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public RGB getTitleBarColor() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public String getWindowTitle() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean isHelpAvailable() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean needsPreviousAndNextButtons() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean needsProgressMonitor() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean performCancel() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public boolean performFinish() {
		throw new RuntimeException("NOT IMPLEMENTED");
	}

	@Override
	public void setContainer(IWizardContainer wizardContainer) {
		throw new RuntimeException("NOT IMPLEMENTED");
	}
	
	
	
	private IProgressMonitor getMonitor() {
		if (monitor == null) {
			monitor= new NullProgressMonitor();
		}
		return monitor;
	}
}
