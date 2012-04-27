package objective.actions;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Shell;

public class TestCounterpartCreator {
	private final Shell shell;
	private int position=-1;

	public TestCounterpartCreator(Shell shell) {
		this.shell = shell;
		
	}
	public IResource createOrRetrieve(IJavaElement elem) throws JavaModelException {
		if (elem == null) {
			MessageDialog.openWarning(this.shell, 
					"Can't create test",
					"You need to select a class file to create a test");
			return null;
		}

		IPackageFragmentRoot newClassRoot = getNewClassRootBasedOnSelectedElement(elem);
		
		if (newClassRoot == null) {
			MessageDialog.openWarning(this.shell, 
					"Can't create test",
					"You need a src/test/java folder");
			return null;
		}
		
		IPackageFragment unitPackage = getNewUnitPackage(elem, newClassRoot);

		String newUnitNameFileName = makeNewUnitName(elem);
		ICompilationUnit compilationUnit = unitPackage.getCompilationUnit(newUnitNameFileName);
		boolean testAlreadyExists = compilationUnit.getResource().exists();
		if (testAlreadyExists)
			return compilationUnit.getResource();
		
		ICompilationUnit createdClass = createTestClass(unitPackage, newUnitNameFileName);
		return createdClass.getResource();
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
				"public class " + typeName + " { \n\n"+
				"}";
		this.position = unitContentContent.length()-2;
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
		
		String counterpartFolder = "/src/"+target+"/java";
		for (IPackageFragmentRoot fragment : packageFragmentRoots) {
			IPath path = fragment.getPath();
			String fragmentPath = path.toFile().getAbsolutePath();
			if (fragmentPath.contains(counterpartFolder)) {
				return fragment;
			}
		}
		return createMissingFolder(project, counterpartFolder);
	}
	private IPackageFragmentRoot createMissingFolder(IJavaProject project, String missingFolder)
			throws JavaModelException {
		IClasspathEntry newSourceEntry = JavaCore.newSourceEntry(project.getProject().getFullPath().append(missingFolder));
		
		IClasspathEntry[] rawClasspath = project.getRawClasspath();
		List<IClasspathEntry> asList = Arrays.asList(rawClasspath);
		List<IClasspathEntry> linked = new LinkedList<IClasspathEntry>();
		linked.addAll(asList);
		linked.add(newSourceEntry);
		project.setRawClasspath(linked.toArray(new IClasspathEntry[0]), getMonitor());
		IFolder folder = project.getProject().getFolder(missingFolder);
		try {
			folder.create(true, true, getMonitor());
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return project.getPackageFragmentRoot(folder);
	}
	
	private IProgressMonitor getMonitor() {
		return new NullProgressMonitor();
	}
	public int getCaretPositionForNewFile() {
		return position;
	}
}