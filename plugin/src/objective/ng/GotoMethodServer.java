package objective.ng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

import org.eclipse.swt.widgets.Display;

import editor.utils.EditorUtils;

public class GotoMethodServer {

	public void start() {
		final int serverPort = getAvailableServerPort();
		new Thread() {
			public void run() {
				try {
					while (true) {
						ServerSocket serverSocket = new ServerSocket(serverPort);
						handleClientConnection(serverSocket);
					}
				} catch (IOException e) {
					throw new IllegalStateException(e);
				}
			}
		}.start();
	}
	
	private int getAvailableServerPort() {
		int port = 47922;
		return port;
	}

	private Socket handleClientConnection(ServerSocket serverSocket)
			throws IOException {
		Socket client = serverSocket.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
		while (!client.isClosed()) {
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
