package editor.utils;

@SuppressWarnings("serial")
public class EditorUtilsException extends RuntimeException {

	public EditorUtilsException(String message) {
		super(message);
	}

	public EditorUtilsException(Exception e) {
		super(e);
	}

}
