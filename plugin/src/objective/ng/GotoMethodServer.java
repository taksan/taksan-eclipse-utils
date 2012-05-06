package objective.ng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.launching.IVMInstall2;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.swt.widgets.Display;

import editor.utils.EditorUtils;
import editor.utils.EditorUtilsException;

public class GotoMethodServer {

	public void start() {
		new Thread() {
			public void run() {
				try {
					ServerSocket serverSocket = makeServerSocket();
					while (true) {
						handleClientConnection(serverSocket.accept());
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}.start();
	}
	
	protected ServerSocket makeServerSocket() {
		int port = 47922;
		int maxRetries = 10;
		while (maxRetries > 0) {
			try {
				ServerSocket serverSocket = new ServerSocket(port);
				setPortSystemProperty(port);
				return serverSocket;
			} catch (IOException e) {
				port++;
			}
		}
		throw new EditorUtilsException("Could not bind GotoMethodServer to any port after " + maxRetries + " attempts.");
	}

	private int setPortSystemProperty(int port) {
		Set<IVMInstall2> visited = new HashSet<IVMInstall2>();
		IJavaProject[] allProjects = EditorUtils.getAllProjects();
		for (IJavaProject iJavaProject : allProjects) {
			try {
				IVMInstall2 vmInstall = (IVMInstall2) JavaRuntime.getVMInstall(iJavaProject);
				if (visited.contains(vmInstall))
					continue;
				String vmArgs = vmInstall.getVMArgs();
				vmArgs = vmArgs.replaceAll("[ ]*-DMethodServerPort=\\d+","");
				String newVmArgs = vmArgs+ " -DMethodServerPort=" + port;
				vmInstall.setVMArgs(newVmArgs);
				visited.add(vmInstall);
			} catch (CoreException e) {
			}
		}
		
		return port;
	}

	private void handleClientConnection(Socket client)
			throws IOException {
		try {
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			String message = in.readLine();
			processMessage(message);
		}
		finally {
			client.close();
		}
	}

	protected void processMessage(final String message) {
		if (message == null || message.length() == 0) {
			return;
		}
		Display.getDefault().asyncExec(new Runnable() {
		    @Override
		    public void run() {
		    	EditorUtils.goToClassMethod(message);
		    }
		});
	}
}
