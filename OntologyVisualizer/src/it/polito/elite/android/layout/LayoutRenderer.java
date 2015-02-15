package it.polito.elite.android.layout;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.TreeMap;

import com.hp.hpl.jena.ontology.OntClass;

import it.polito.elite.android.ontology.JenaWrapper;
import it.polito.elite.android.ontology.ViewOntologyActivity;
import it.polito.elite.android.widget.radialmenu.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;

public class LayoutRenderer extends View {
	//la classe su cui sidegno il layout

	//deve gestire anche lo scroll e lo zoom
	//memorizzo coordinate per fare lo scroll


	private float posX;
	private float posY;

	//dimensioni finali della vista
	private int view_w, view_h;

	private static final int INVALID_POINTER_ID = -1;

	// The ‘active pointer’ is the one currently moving our object.
	private int mActivePointerId = INVALID_POINTER_ID;

	//variabile per zoomin e zoomout
	private ScaleGestureDetector scaleDetector;
	private float scaleFactor = 1.f;

	private Context context;
	private HashMap<String, NodeItem> nodes = new HashMap<String, NodeItem>();
	private ArrayList<Relation> relations = new ArrayList<Relation>();

	//dimensione della vista
	private int maxRadius;

	//dimensione raggio
	private double radius;
	//profontidità albero
	private int depth;

	//dimensione dello schermo
	private int screen_h, screen_w;

	//booleana per sapere se lo scale è il primo (quindi automatico quando disegno la vista)
	//oppure se è dovuto al pinch dell'utente
	private boolean firstScale = true;


	//metodo per deselezionare i nodi
	public void deselectAll(){
		for (HashMap.Entry<String, NodeRenderer> entry : nodes_renderer.entrySet()){
			entry.getValue().isSelected(false);
			invalidate();
		}
	}
	//me lo tengo direttamente nel layout renderer...poi aggiungo metodo get per prenderli quando clicco su finish
	//devo tenere memorizzati i nodi cliccati
	private ArrayList<String> view_nodes = new ArrayList<String>();
	//mi servono i metodi per prendere la lista
	public ArrayList<String> getViewNodes(){
		return view_nodes;
	}

	//verrà poi aggiunta all'activity viewOntology

	//costruttore vuoto per aggiungerla all'activity
	public LayoutRenderer(Context context){
		super(context);
		this.context = context;
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screen_h = size.y;
		screen_w = size.x;

		LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//inflatto il layout
		inflater.inflate(R.layout.renderer_layout,null);	 

		setBackgroundColor(Color.TRANSPARENT);	

	}


	public HashMap<String, NodeItem> getNodes() {
		return nodes;
	}

	public void setNodes(HashMap<String, NodeItem> nodes) {
		this.nodes = nodes;
		calculateNodeRenderer();
	}

	public ArrayList<Relation> getRelations() {
		return relations;
	}

	public void setRelations(ArrayList<Relation> relations) {
		this.relations = relations;
	}

	public void setMaxRadius(int maxRadius) {
		this.maxRadius = maxRadius;
		view_h = maxRadius*2;
		view_w = maxRadius*2;
		if(firstScale){

			//calcolo scale factor
			//prima controllo che almeno una delle due dimensioni della view superino quella del canvas
			if(view_h > screen_h || view_w > screen_w){

				float scale = Math.min((float)screen_h, (float)screen_w)/(float) view_h;
				float cX = screen_w/2.0f; 
				float cY = screen_h/2.0f;
				matrix.postScale(scale, scale, cX, cY);
//				c.scale(scale, scale, cX, cY);
			}
		}
		invalidate();
	}


	@Override
	public void onDraw(Canvas c){
		super.onDraw(c);
		Paint p = new Paint();
		p.setColor(Color.WHITE);
		c.concat(matrix);
		drawLayout(c);	
	}

	private HashMap<String,NodeRenderer> nodes_renderer = new HashMap<String,NodeRenderer>();

	public void calculateNodeRenderer(){
		for (HashMap.Entry<String, NodeItem> entry : nodes.entrySet())
		{
			NodeItem node = entry.getValue();

			boolean hasSubclass = node.getHasSubclass();
			final NodeRenderer nodeRenderer = new NodeRenderer(context, JenaWrapper.getResourceName(node.getResource()),hasSubclass, node.getResource().toString());
			//setto la x e la y in node render, che ha il compito di disegnar e il nodo.
			nodeRenderer.setCenter_x((int)node.getX());
			nodeRenderer.setCenter_y((int)node.getY());
			nodeRenderer.setIconDimension(node.getIcondimension());


			nodes_renderer.put(node.getResource().toString(), nodeRenderer);

		}	
		//		invalidate();
	}


	public HashMap<String,NodeRenderer> getNodes_renderer() {
		return nodes_renderer;
	}


	public void drawLayout(Canvas c){

		if(relations.size()>0){
			for(int i = 0; i <relations.size(); i++){
				Relation r = relations.get(i);
				if(r.isDrawable()){
					RelationRenderer r_renderer = new RelationRenderer(getContext());
					r_renderer.setStartX((float) r.getStartX());
					r_renderer.setStartY((float) r.getStartY());
					r_renderer.setStopX((float) r.getStopX());
					r_renderer.setStopY((float) r.getStopY());
					r_renderer.onDraw(c);
				}
			}
		}


		if(nodes_renderer.size()>0){
			for(HashMap.Entry<String, NodeRenderer> entry : nodes_renderer.entrySet()){
				NodeRenderer nodeRenderer = entry.getValue();
				if(nodeRenderer.isDrawable()){
					nodeRenderer.onDraw(c);
				}
			}	
		}


	}
	public void setPosX(float posX){
		this.posX = posX;
	}
	public void setPosY(float posY){
		this.posY = posY;
	}
	public float getPosX(){
		return posX;
	}

	public float getPosY(){
		return posY;
	}

	public float getScaleFactor(){
		return scaleFactor;
	}
	public void setScaleFactor(float scaleFactor){
		this.scaleFactor = scaleFactor;
	}

	public PointF getPivot(){
		return mid;
	}
	public void setPivot(PointF mid){
		this.mid = mid;
	}



	// These matrices will be used to move and zoom image
	Matrix matrix = new Matrix();
	Matrix savedMatrix = new Matrix();

	// We can be in one of these 3 states
	static final int NONE = 0;
	static final int DRAG = 1;
	static final int ZOOM = 2;
	int mode = NONE;

	// Remember some things for zooming
	PointF start = new PointF();
	static PointF mid = new PointF();
	float oldDist = 1f;
	String savedItemClicked;

	//	float translX = 0;
	//	float translY = 0;
	float scale = 0;

	//provo altro metodo ontouch
	/**
	 * on touch con matrici
	 */
	@Override
	public boolean onTouchEvent(MotionEvent event) {
		// TODO Auto-generated method stub

		dumpEvent(event);

		// Handle touch events here...
		switch (event.getAction() & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			savedMatrix.set(matrix);
			start.set(event.getX(), event.getY());

			//devo controllare se ho beccato un nodo
			if(ViewOntologyActivity.NODE_CLICKABLE){
				//se i nodi sono cliccabili
				//è hasmap, devo scorrerla diversamente
				for(HashMap.Entry<String, NodeRenderer> entry : nodes_renderer.entrySet()){
					//node renderer deve avere anche nome completo della risorsa
					NodeRenderer tmp = entry.getValue();
					if(tmp.isInNode(start.x, start.y, matrix)){

						String node_name = tmp.getName_complete();
						if(!tmp.isSelected()){
							//se non era già selezionato, allora aggiungo
							tmp.isSelected(true);
							view_nodes.add(node_name);
							tmp.invalidate();

						}
						else{
							//rimuovo
							tmp.isSelected(false);
							view_nodes.remove(node_name);
							tmp.invalidate();

						}


					}
				}
			}

			Log.d("drag", "mode=DRAG");	
			mode = DRAG;
			break;
		case MotionEvent.ACTION_POINTER_DOWN:
			firstScale = false;
			oldDist = spacing(event);
			Log.d("olddist", "oldDist=" + oldDist);
			if (oldDist > 10f) {
				savedMatrix.set(matrix);
				midPoint(mid, event);
				mode = ZOOM;
				Log.d("zoom", "mode=ZOOM");
			}
			
			break;
		case MotionEvent.ACTION_UP:
			mode = NONE;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mode = NONE;
			Log.d("none", "mode=NONE");
			break;
		case MotionEvent.ACTION_MOVE:

			if (mode == DRAG) {
				// ...
				matrix.set(savedMatrix);
				matrix.postTranslate(event.getX() - start.x, event.getY() - start.y);
			} else if (mode == ZOOM) {
				float newDist = spacing(event);
				Log.d("newDist", "newDist=" + newDist);
				if (newDist > 10f) {
					matrix.set(savedMatrix);
					final float scale = newDist / oldDist;
					//					scaleFactor = scaleFactor*scale;
					scaleFactor = scale;
					matrix.postScale(scale, scale, mid.x, mid.y);
					Log.v("scale", scale+"");
				}
			}

			break;
		}

		invalidate();
		return true;
	}
	/**
	 * fine on touch metodo matrici
	 */


	private void dumpEvent(MotionEvent event) {
		String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
				"POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
		StringBuilder sb = new StringBuilder();
		int action = event.getAction();
		int actionCode = action & MotionEvent.ACTION_MASK;
		sb.append("event ACTION_").append(names[actionCode]);
		if (actionCode == MotionEvent.ACTION_POINTER_DOWN
				|| actionCode == MotionEvent.ACTION_POINTER_UP) {
			sb.append("(pid ").append(
					action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
			sb.append(")");
		}
		sb.append("[");
		for (int i = 0; i < event.getPointerCount(); i++) {
			sb.append("#").append(i);
			sb.append("(pid ").append(event.getPointerId(i));
			sb.append(")=").append((int) event.getX(i));
			sb.append(",").append((int) event.getY(i));
			if (i + 1 < event.getPointerCount())
				sb.append(";");
		}
		sb.append("]");
		Log.d("", sb.toString());
	}


	//provo ad aggiungere un metodo che mi salva la matrice e la applica al layout
	//meotodo che mi serve per i bookmark
	public void updateMatrix(Matrix MatrixBookmark){
		setAllDrawable();
		matrix.set(MatrixBookmark);
		invalidate();
	}

	//devo aggiornare la vista tenendo in conto solo i nodi presenti nell'array che passo in ingresso
	public void updateViewO(ArrayList<String> nodes_name){
		//prima devo rimettere tutto drawable, se no si tengono memorizzate le visualizzazioni precedenti
		setAllDrawable();
		matrix.reset();

		//lascio a drawable solo quelli che si trovano nell'array list in ingresso
		for(HashMap.Entry<String, NodeRenderer> entry : nodes_renderer.entrySet()){
			boolean present = false;
			String name = entry.getValue().getName_complete();

			for(int j = 0; j< nodes_name.size(); j++){
				if(name.equals(nodes_name.get(j))){
					present = true;
				}
			}
			//se alla fine del for non ho trovato nulla, allora present sarà = false;
			if(present==false){
				//non disegno più il nodo;
				entry.getValue().setDrawable(false);
			}
		}

		//devo scorrere di nuovo l'arraylist di string
		for(int j1=0; j1<nodes_name.size(); j1++){
			String name = nodes_name.get(j1);

			NodeItem node_R = nodes.get(name).getParent();
			
			while(node_R!=null){
				String key = node_R.getResource().toString();
				//devo trovare l'oggetto in node renderer...quindi l'oggetto
				//che ha come nome_complete la key
				nodes_renderer.get(key).setDrawable(true);
				node_R = nodes.get(key).getParent();
			}

		}


		//devo anche cancellare le relations
		//se il nodo sorgente e il nodo destinazione hanno drawable a false, allora anche la relazione non deve 
		//essere disegnata

		for(int k = 0; k < relations.size(); k++){
			Relation r = relations.get(k);
			//prendo il nodeItem destinazione
			NodeItem target = r.getNodeB();

			//trovo il nodeRenderer corrispondente
			NodeRenderer target_r = nodes_renderer.get(target.getResource().toString());

			//se target node ha drawable = false,
			//allora anche la relation sarà drawable = false
			if(!target_r.isDrawable()){
				r.setDrawable(false);
			}
		}
		invalidate();

	}

	public void setAllDrawable(){
		for(HashMap.Entry<String, NodeRenderer> entry : nodes_renderer.entrySet()){
			entry.getValue().setDrawable(true);
		}
		for(int k1 = 0; k1 < relations.size(); k1++){
			relations.get(k1).setDrawable(true);
		}
	}

	public Matrix getTransformationMatrix(){
		return matrix;
	}



	/** Determine the space between the first two fingers */
	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return FloatMath.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint (PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	public void testRelations(){
		for(int i =0; i<relations.size(); i++){
			Log.v("Source Node", relations.get(i).getNodeA().getResource().toString());
			Log.v("posizione", relations.get(i).getNodeA().getX()+","+relations.get(i).getNodeA().getY());
			Log.v("Target Node", relations.get(i).getNodeB().getResource().toString());
			Log.v("posizione", relations.get(i).getNodeB().getX()+","+relations.get(i).getNodeB().getY());
		}
	}

	public void clearViewNodes() {
		view_nodes.clear();

	}

	//	private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener {
	//		@Override
	//		public boolean onScaleBegin(ScaleGestureDetector detector){
	//			mid.x = detector.getFocusX();
	//			mid.y = detector.getFocusY();
	//			Log.v("mid.x",mid.x+"");
	//			Log.v("mid.y",mid.y+"");
	//			return true;
	//		}
	//
	//		@Override
	//		public boolean onScale(ScaleGestureDetector detector) {
	//			scaleFactor *= detector.getScaleFactor();
	//
	//			// Don't let the object get too small or too large.
	//			scaleFactor = Math.max(0.1f, Math.min(scaleFactor, 5.0f));
	//			
	//			Log.v("i'm scaling", "scaling");
	//			postInvalidate();
	//			return true;
	//		}
	//		@Override
	//		public void onScaleEnd(ScaleGestureDetector detector){
	//			mid.x=0;
	//			mid.y=0;
	//			Log.v("fine", "fine scaling");
	//			Log.v("posX", posX+"");
	//			Log.v("posY", posY+"");
	//			Log.v("mid.x",mid.x+"");
	//			Log.v("mid.y",mid.y+"");
	//
	//		}
	//
	//
	//	}

}
/**
 * on touch con pointer id
 */

//@Override 
//public boolean onTouchEvent(MotionEvent event) {		
//	/*
//	 * METODO CON POINTER ID
//	 */
//	
//
//	final int action = event.getAction();
//	switch (action & MotionEvent.ACTION_MASK) {
//
//	case MotionEvent.ACTION_DOWN: {
//		final float x = event.getX();
//		final float y = event.getY();
//
//		lastX = x;
//		lastY = y;
//
//		// Save the ID of this pointer
//		mActivePointerId = event.getPointerId(0);
//
//		//			//devo controllare se ho beccato un node
//		//			if(ViewOntologyActivity.NODE_CLICKABLE){
//		//				//se i nodi sono cliccabili
//		//				for(int i = 0; i<nodes_renderer.size(); i++){
//		//					NodeRenderer tmp = nodes_renderer.get(i);
//		//					if(tmp.isInNode(x, y, posX, posY, scaleFactor, mid)){
//		//						Log.v("selected", tmp.isSelected()+"");
//		//						Log.v("hai cliccato", tmp.getNode_name());
//		//					}
//		//				}
//		//			}
//		Log.v("action", "DOWN");
//		Log.v("posX", posX+"");
//		Log.v("posY", posY+"");
//		Log.v("mid.x", mid.x+"");
//		Log.v("mid.y", mid.y+"");
//		break;
//	}
//
//	case MotionEvent.ACTION_MOVE: {	    	
//		// Find the index of the active pointer and fetch its position
//		final int pointerIndex = event.findPointerIndex(mActivePointerId);
//		final float x = event.getX(pointerIndex);
//		final float y = event.getY(pointerIndex);
//
//		if(!scaleDetector.isInProgress()){
//
//
//			final float dx = x - lastX;
//			final float dy = y - lastY;
//
//			posX += dx;
//			posY += dy;
//
//			Log.v("action", "MOVE");
//			Log.v("posX", posX+"");
//			Log.v("posY", posY+"");
//			Log.v("mid.x", mid.x+"");
//			Log.v("mid.y", mid.y+"");
//			
//			postInvalidate();
//		}
//
//		lastX = x;
//		lastY = y;
//
//
//		break;
//	}
//
//	case MotionEvent.ACTION_UP: {
//		mActivePointerId = INVALID_POINTER_ID;
//		break;
//	}
//
//	case MotionEvent.ACTION_CANCEL: {
//		mActivePointerId = INVALID_POINTER_ID;
//		break;
//	}
//
//	case MotionEvent.ACTION_POINTER_DOWN: {
//		//è l'utente che sta scalando, quindi lo scale factor sarà diverso
//		firstScale = false;
//		Log.v("action", "POINTER DOWN");
//		Log.v("posX", posX+"");
//		Log.v("posY", posY+"");
//		Log.v("mid.x", mid.x+"");
//		Log.v("mid.y", mid.y+"");
//		
//		//				else{
//		//
//		//				}
//
//
//		//ci pensa l'action pointer up ad aggiornate correttamente l'ultima x e y toccate
//		break;
//
//	}
//
//	case MotionEvent.ACTION_POINTER_UP: {
//		
//		Log.v("action", "action up");
//		Log.v("posX", posX+"");
//		Log.v("posY", posY+"");
//		Log.v("mid.x", mid.x+"");
//		Log.v("mid.y", mid.y+"");
//		
//		// Extract the index of the pointer that left the touch sensor
//		final int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
//
//		final int pointerId = event.getPointerId(pointerIndex);
//
//		if (pointerId == mActivePointerId) {
//			// This was our active pointer going up. Choose a new
//			// active pointer and adjust accordingly.
//			final int newPointerIndex = pointerIndex == 0 ? 1 : 0;
//			lastX = event.getX(newPointerIndex);
//			lastY = event.getY(newPointerIndex);
//			mActivePointerId = event.getPointerId(newPointerIndex);
//
//		}
//		break;
//	}
//	
//	}
//	scaleDetector.onTouchEvent(event);
//	return true;
//
//}

/**
 * fine on touch con pointer id
 */
