package eclipse.utils;

@SuppressWarnings("serial")
public class EclipseUtilsException extends RuntimeException {

	public EclipseUtilsException(String message) {
		super(message);
	}

	public EclipseUtilsException(Exception e) {
		super(e);
	}

}
