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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
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
			IType iType = getClassTypeOrCry(className);
				
			
			IMethod method = iType.getMethod(methodName, methodSignature);
			ISourceRange sourceRange = method.getSourceRange();
			
			IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			ITextEditor openEditor = (ITextEditor) IDE.openEditor(activePage, (IFile) iType.getResource(), true);
			int length = method.getSource().split("\n")[0].length();
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

    public static IJavaModel getJavaModel() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return JavaCore.create(workspace.getRoot());
    }

	public static void goToLineInCurrentFile(int lineNumber) {
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		goToLine(activeEditor, lineNumber);
	}

	public static void goToLine(IEditorPart editorPart, int lineNumber) {
		if (!(editorPart instanceof ITextEditor) || lineNumber <= 0) {
			return;
		}
		ITextEditor editor = (ITextEditor) editorPart;
		IDocument document = editor.getDocumentProvider().getDocument(
				editor.getEditorInput());
		if (document != null) {
			IRegion lineInfo = null;
			try {
				// line count internaly starts with 0, and not with 1 like in
				// GUI
				lineInfo = document.getLineInformation(lineNumber - 1);
			} catch (BadLocationException e) {
				// ignored because line number may not really exist in document,
				// we guess this...
			}
			if (lineInfo != null) {
				editor.selectAndReveal(lineInfo.getOffset(),
						lineInfo.getLength());
			}
		}
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
