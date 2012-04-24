package brundle.primitivecoloring;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class PrimitiveMarkerAnnotation extends Annotation implements PrimitiveMarker {

	private static final String PRIMITIVE_MARKER_ID = "brundle.primitivecoloring.primitiveMarker";
	private final Position position;

	public PrimitiveMarkerAnnotation(int startOff, int length) {
		super(PRIMITIVE_MARKER_ID, true, "Primitive Reservation");
		position = new Position(startOff, length);
	}

	public Position getPosition() {
		return position;
	}

}
