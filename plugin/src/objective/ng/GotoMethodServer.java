package objective.ng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.javaeditor.CompilationUnitEditor;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

public class GotoMethodServer {

	public void start() {
		new Thread() {
			public void run() {
				try {
					while (true) {
						ServerSocket serverSocket = new ServerSocket(47921);
						handleClientConnection(serverSocket);
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}.start();
	}
	
	private Socket handleClientConnection(ServerSocket serverSocket)
			throws IOException {
		Socket client = serverSocket.accept();
		while (!client.isClosed()) {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = in.readLine();
			processMessage(message);
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
			}
		}
		return client;
	}

	protected void processMessage(final String message) {
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	goToClassMethod(message);
		    }
		});
	}
	
	private void goToClassMethod(String classMethod) {
		IJavaProject project = getActiveJavaProject();
        
        String className = classMethod.replaceAll("(.*)\\.[^.]+(\\(\\))?$", "$1");
        String methodName = classMethod.replaceAll(".*\\.([^.]*(\\(\\))?$)", "$1");
        try {
			IType iType = project.findType(className);
			IMethod method = iType.getMethod(methodName, new String[0]);
			ISourceRange sourceRange = method.getSourceRange();
			
			IWorkbenchPage activePage= PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			
			ITextEditor openEditor = (ITextEditor) IDE.openEditor(activePage, (IFile) iType.getResource(), true);
			openEditor.selectAndReveal(sourceRange.getOffset(), 1);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private IJavaProject getActiveJavaProject() {
		IEditorPart activeEditor = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFileEditorInput input = (IFileEditorInput)activeEditor.getEditorInput() ;
        IFile file = input.getFile();
        IProject activeProject = file.getProject();
        
        IJavaModel javaModel= JavaCore.create(ResourcesPlugin.getWorkspace().getRoot());
        IJavaProject project = (IJavaProject) javaModel.getJavaProject(activeProject.getName());
		return project;
	}

	private static void goToLine(IEditorPart editorPart, int lineNumber) {
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
}
