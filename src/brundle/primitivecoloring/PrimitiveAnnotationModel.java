package brundle.primitivecoloring;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import org.eclipse.jface.text.DocumentEvent;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IDocumentListener;
import org.eclipse.jface.text.Position;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.jface.text.source.IAnnotationModelExtension;
import org.eclipse.jface.text.source.IAnnotationModelListener;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;


public class PrimitiveAnnotationModel implements IAnnotationModel {
	private static final String PRIMITIVE_BEGINNING_MARKER = "/* PRIM BEGIN */";
	private static final String PRIMITIVE_ENDING_MARKER = "/* PRIM END */";

	private Map<IDocument, PrimitiveMarkerAnnotation> annotations = 
			new LinkedHashMap<IDocument,PrimitiveMarkerAnnotation>();
	
	/** Key used to piggyback our model to the editor's model. */
	private static final Object KEY = new Object();
	private IDocumentListener documentListener = new IDocumentListener() {

		@Override
		public void documentChanged(DocumentEvent event) {
			updateAnnotation(event.getDocument());
		}

		@Override
		public void documentAboutToBeChanged(DocumentEvent event) {
		}
	};

	public PrimitiveAnnotationModel(ITextEditor editor, IDocument document) {
	}


	public static void attach(ITextEditor editor) {
		IDocumentProvider provider = editor.getDocumentProvider();
		// there may be text editors without document providers (SF #1725100)
		if (provider == null)
			return;
		IAnnotationModel model = provider.getAnnotationModel(editor
				.getEditorInput());
		if (!(model instanceof IAnnotationModelExtension))
			return;
		IAnnotationModelExtension modelex = (IAnnotationModelExtension) model;

		IDocument document = provider.getDocument(editor.getEditorInput());

		PrimitiveAnnotationModel coveragemodel = (PrimitiveAnnotationModel) modelex
				.getAnnotationModel(KEY);
		if (coveragemodel == null) {
			coveragemodel = new PrimitiveAnnotationModel(editor, document);
			modelex.addAnnotationModel(KEY, coveragemodel);
		}
	}

	@Override
	public void addAnnotationModelListener(IAnnotationModelListener listener) {
		System.out.println("addAnnotationModelListener");
	}

	@Override
	public void removeAnnotationModelListener(IAnnotationModelListener listener) {
		System.out.println("removeAnnotationModelListener");

	}

	@Override
	public void connect(IDocument document) {
		updateAnnotation(document);
	}
	
	protected void updateAnnotation(IDocument document) {
		int primitiveStart = 
				document.get().indexOf(PRIMITIVE_BEGINNING_MARKER);
		
		if (primitiveStart < 0)
			return;
		
		int length = document.get().indexOf(PRIMITIVE_ENDING_MARKER) - primitiveStart;
		if (length < 0)
			return;
		
		if (annotations.get(document) == null)
			document.addDocumentListener(documentListener);
		
		annotations.put(
				document,
				new PrimitiveMarkerAnnotation(primitiveStart, length));
	}


	@Override
	public void disconnect(IDocument document) {
		if (annotations.get(document) == null)
			return;
		
		document.removeDocumentListener(documentListener);
		annotations.remove(document);
	}


	@SuppressWarnings("rawtypes")
	@Override
	public Iterator getAnnotationIterator() {
		return annotations.values().iterator();
	}

	@Override
	public Position getPosition(Annotation annotation) {
		return ((PrimitiveMarkerAnnotation)annotation).getPosition();
	}
	

	@Override
	public void addAnnotation(Annotation annotation, Position position) {
		throw new UnsupportedOperationException("addAnnotation");
	}

	@Override
	public void removeAnnotation(Annotation annotation) {
		throw new UnsupportedOperationException("removeAnnotation");
	}	
}
