package editor.utils;

import objective.ObjectiveUtilsPlugin;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class EditorUtils {
	
	public static void goToClassMethod(String classMethod) {
		if (classMethod == null || classMethod.length() == 0) {
			return;
		}
        
        String className = getClassName(classMethod);
        String methodName = getMethodName(classMethod);
        String[] methodSignature = getMethodSignature(classMethod);
        try {
			IType klass = getClassTypeOrCry(className);
			ITextEditor openEditor = openClassInEditor(klass);
			
			IMethod method = klass.getMethod(methodName, methodSignature);
			ISourceRange sourceRange = method.getSourceRange();
			int length = getMethodSignatureLength(method);
			openEditor.selectAndReveal(sourceRange.getOffset(), length);
		} catch (Exception e) {
			ObjectiveUtilsPlugin.log(e);
		}
	}

	public static IJavaProject[] getAllProjects() {
	    try {
			return getJavaModel().getJavaProjects();
		} catch (JavaModelException e) {
			throw new ObjectiveEclipseUtilsException(e);
		}
	}

	private static IJavaModel getJavaModel() {
	    IWorkspace workspace = ResourcesPlugin.getWorkspace();
	    return JavaCore.create(workspace.getRoot());
	}

	private static ITextEditor openClassInEditor(IType iType)
			throws PartInitException {
		IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
		ITextEditor openEditor = (ITextEditor) IDE.openEditor(activePage, (IFile) iType.getResource(), true);
		return openEditor;
	}

	private static int getMethodSignatureLength(IMethod method)
			throws JavaModelException {
		return method.getSource().split("\n")[0].length();
	}

	private static String[] getMethodSignature(String classMethod) {
		if (!classMethod.contains("("))
			return new String[0];
		String methodParameters = classMethod.replaceAll(".*\\((.*)\\)", "$1");
		if (methodParameters.trim().length() == 0)
			return new String[0];
		
        return methodParameters.split(",");
	}

	private static String getMethodName(String classMethod) {
		return classMethod.replaceAll(".*\\.([^.]*?)(\\(.*\\))?$", "$1");
	}

	private static String getClassName(String classMethod) {
		return classMethod.replaceAll("(.*)\\.[^.]+(\\(.*\\))?$", "$1");
	}

	private static IType getClassTypeOrCry(String className) throws JavaModelException {
		IJavaProject[] projects = getAllProjects();
		for(IJavaProject project: projects){
		
			IType iType = project.findType(className);
			if (iType != null)
				return iType;
		}
		throw new ObjectiveEclipseUtilsException("Class "+ className + " not found.");
	}
}
