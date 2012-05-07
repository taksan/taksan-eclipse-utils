package objective.primitivehighlight;

import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;

public class PrimitiveErrorMarkerMissing extends Annotation implements PrimitiveMarker {

	private static final String PRIMITIVE_MARKER_MISSING_ID = "objective.eclipse.utils.primitiveMarkerMissing";
	private Position position;

	public PrimitiveErrorMarkerMissing(int startOff, int length) {
		super(PRIMITIVE_MARKER_MISSING_ID, true, "Primitive Reservation End Missing");
		position = new Position(startOff, length);
	}

	public Position getPosition() {
		return position;
	}
}
