package it.polito.elite.android.layout;

import it.polito.elite.android.ontology.JenaWrapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;





import com.hp.hpl.jena.graph.Graph;
import com.hp.hpl.jena.graph.GraphAdd;
import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.vocabulary.OWL;

import android.R;
import android.content.Context;
import android.graphics.Point;
import android.hardware.Camera.PreviewCallback;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

public class RadialTreeLayout {


	public static final int DEFAULT_RADIUS = 200;
//	private static final int MARGIN = 30;

	protected int m_maxDepth = 0;
	protected double m_radiusInc;
	protected double m_theta1, m_theta2;
	protected boolean m_setTheta = true;
	protected boolean m_autoScale = true;

	protected int screen_h, screen_w;
	protected Context context;

	protected Point m_origin = new Point();
	protected NodeItem m_prevRoot;	


	protected OntModel ont_model;
	protected HashMap<String,NodeItem> nodes = new HashMap<String, NodeItem>();
	//Array di relation
	private ArrayList<Relation> relations = new ArrayList<Relation>();

	/**
	 * Creates a new RadialTreeLayout. Automatic scaling of the radius
	 * values to fit the layout bounds is enabled by default.
	 * @param group the data group to process. This should resolve to
	 * either a Graph or Tree instance.
	 * 
	 * Devo avere in ingresso anche le dimensioni del display, per calcolare il punto di ancoraggio (chè è poi il centro)
	 */
	public RadialTreeLayout(JenaWrapper jw, int screen_h, int screen_w, Context context) {
		
		m_prevRoot = null;
		m_theta1 = 0;
		m_theta2 = m_theta1 + (Math.PI * 2);

		this.nodes=jw.getNodes();
		this.ont_model = jw.getOnt_model();
		int size = nodes.size();
		m_radiusInc = DEFAULT_RADIUS + (2*size);

		this.screen_h = screen_h;
		this.screen_w = screen_w;
		this.context = context;

		calculateRadialTreeLayout();
		calculateRelations(null, nodes.get(OWL.Thing.toString()));
		
	}


	/**
	 * Set the radius increment to use between concentric circles. Note
	 * that this value is used only if auto-scaling is disabled.
	 * @return the radius increment between subsequent rings of the layout
	 * when auto-scaling is disabled
	 */
	public double getRadiusIncrement() {
		return m_radiusInc;
	}

	/**
	 * Set the radius increment to use between concentric circles. Note
	 * that this value is used only if auto-scaling is disabled.
	 * @param inc the radius increment between subsequent rings of the layout
	 * @see #setAutoScale(boolean)
	 */
	public void setRadiusIncrement(double inc) {
		m_radiusInc = inc;
	}

	/**
	 * Indicates if the layout automatically scales to fit the layout bounds.
	 * @return true if auto-scaling is enabled, false otherwise
	 */
	public boolean getAutoScale() {
		return m_autoScale;
	}

	/**
	 * Set whether or not the layout should automatically scale itself
	 * to fit the layout bounds.
	 * @param s true to automatically scale to fit display, false otherwise
	 */
	public void setAutoScale(boolean s) {
		m_autoScale = s;
	}

	/**
	 * Constrains this layout to the specified angular sector
	 * @param theta the starting angle, in radians
	 * @param width the angular width, in radians
	 */
	public void setAngularBounds(double theta, double width) {
		m_theta1 = theta;
		m_theta2 = theta+width;
		m_setTheta = true;
	}

	public void calculateRadialTreeLayout(){   
		//è sempre il centro del display
		m_origin.x = screen_w/2;
		m_origin.y = screen_h/2;


		//nell'ontologia il root node sarà sempre thing
		//quindi prendo il root node
		NodeItem root_node = nodes.get((OWL.Thing).toString());

		m_maxDepth = 0;
		calcAngularWidth(root_node, 0);

		// perform the layout
		if ( m_maxDepth > 0 )
			layout(root_node, m_radiusInc, m_theta1, m_theta2);

		
		
		// update properties of the root node
		root_node.setX(m_origin.x);
		root_node.setY(m_origin.y);
		root_node.setAngle(m_theta2-m_theta1);


	}

	private double calcAngularWidth(NodeItem n, int d) {

		//aggiorno la profondità dell'albero
		if ( d > m_maxDepth ) m_maxDepth = d; 

		//prendo le dimensioni del nodo 
		double w = n.getWidth(), h = n.getHeight();

		//se sono al livello zero dell'abero(cioè thing) allora diametro è zero, altrimenti lo calcolco in base al livello
		double diameter = d==0 ? 0 : Math.sqrt(w*w+h*h) / d;

		double aw = 0;

		Resource r = n.getResource(); 


		ExtendedIterator<OntClass> iterator;

		if(r instanceof OntClass){
			OntClass r_class = (OntClass) r;
			iterator = r_class.listSubClasses(true);
		}
		else{
			iterator = ont_model.listHierarchyRootClasses();
		}
		iterator = iterator.filterDrop(new Filter<OntClass>() {

			@Override
			public boolean accept(OntClass arg0) {
				// TODO Auto-generated method stub
				return ((Resource) arg0).isAnon();
			}
		});

		if(iterator.hasNext()){
			while(iterator.hasNext()){
				OntClass subclass = iterator.next();
				//prendo il nodo
				NodeItem nextNode = nodes.get(subclass.toString());
				//aggiorno la angolar width
				aw+= calcAngularWidth(nextNode, d+1);
			}
			//prendo il massimo tra i due
			aw = Math.max(diameter, aw);
		}
		else{
			aw=diameter;
		}
		//aggiorno l'angolo del nodo in ingresso
		Log.v("node", ""+n.getResource().toString());
		Log.v("d", ""+d);
		n.setWidth(aw);
		return aw;
	}

	private static final double normalize(double angle) {
		while ( angle > Math.PI*2 ) {
			angle -= Math.PI*2;
		}
		while ( angle < 0 ) {
			angle += Math.PI*2;
		}
		return angle;
	}

	protected void layout(NodeItem n, double r, double theta1, double theta2) {
		double dtheta  = (theta2-theta1);
		double dtheta2 = dtheta / 2.0;
		double width = n.getWidth();
		double cfrac, nfrac = 0.0;

		//devo sempre controllare se è thing oppure no
		Resource res = n.getResource();
		ExtendedIterator<OntClass> iterator;

		if(res instanceof OntClass){
			//faccio il cast
			OntClass res_class = (OntClass) res;
			//prendo i figli
			iterator = res_class.listSubClasses(true);
		}
		else{
			iterator = ont_model.listHierarchyRootClasses();
			
		}
		iterator = iterator.filterDrop(new Filter<OntClass>() {

			@Override
			public boolean accept(OntClass arg0) {
				// TODO Auto-generated method stub
				return ((Resource) arg0).isAnon();
			}
		});
		if(iterator.hasNext()){
			while(iterator.hasNext()){
				OntClass subclass = (OntClass) iterator.next();
				NodeItem sub_node = nodes.get(subclass.toString());
				cfrac = sub_node.getWidth() / width;
				if(subclass.hasSubClass()){
					layout(sub_node, r+m_radiusInc, theta1 + nfrac*dtheta,theta1 + (nfrac+cfrac)*dtheta);
				}
				setPolarLocation(sub_node, n, r, theta1 + nfrac*dtheta + cfrac*dtheta2);
				sub_node.setAngle(cfrac*dtheta);
				nfrac += cfrac;
			}
		}
	}
	
	public void calculateRelations(ExtendedIterator<OntClass> iterator, NodeItem source){
		//la prima volta iterator è null, source è thing
		if(iterator == null){
			//carico le rootclasses
			iterator = ont_model.listHierarchyRootClasses();
		}
		iterator = iterator.filterDrop(new Filter<OntClass>() {
			@Override
			public boolean accept(OntClass arg0) {
				// TODO Auto-generated method stub
				return ((Resource) arg0).isAnon();
			}
		});
		
		if(iterator.hasNext()){
			while(iterator.hasNext()){
				OntClass ontclass = iterator.next();
				NodeItem node = nodes.get(ontclass.toString());
				Relation r = new Relation(source, node, "");
				relations.add(r);
				if(ontclass.hasSubClass()){
					ExtendedIterator<OntClass> it = ontclass.listSubClasses(true);
					calculateRelations(it, node);
					
				}
			}
		}
	}


	public void testNodesPosition(){
		for(TreeMap.Entry<String, NodeItem> entry : nodes.entrySet() ){
			NodeItem node = entry.getValue();
			Log.v("node", ""+node.getResource().toString());
			Log.v("x", ""+node.getX());
			Log.v("y",""+node.getY());
			Log.v("width", ""+node.getWidth());
			Log.v("angle", ""+node.getAngle());
		}
	}

	public void testNodesOrder(){
		for(TreeMap.Entry<String, NodeItem> entry : nodes.entrySet() ){
			NodeItem node = entry.getValue();
			Log.v("node", ""+JenaWrapper.getResourceName(node.getResource()));
		}
	}

	public void testNodesHierarchy(){
		for(TreeMap.Entry<String, NodeItem> entry : nodes.entrySet() ){
			NodeItem node = entry.getValue();
			Log.v("node", ""+JenaWrapper.getResourceName(node.getResource()));
			if(entry.getValue().getResource() instanceof OntClass){
				if(((OntClass) entry.getValue().getResource()).hasSubClass()){
					ExtendedIterator<OntClass> it = ((OntClass) entry.getValue().getResource()).listSubClasses(true);
					while(it.hasNext()){
						Log.v("figlio", JenaWrapper.getResourceName(it.next()));
					}
				}
			}
		}
	}
	public ArrayList<Relation> getRelations() {
		return relations;
	}
	
	public int getMaxRadius(){
		//devo fare più uno perchè la profondità parte da 0!!!
//		Log.v("max_depth",""+m_maxDepth + 1);
		return (int)((m_maxDepth*m_radiusInc)/* + MARGIN*/);
	}
	
	public int getDetph(){
		return m_maxDepth;
	}




	protected void setPolarLocation(NodeItem n, NodeItem p, double r, double t) {
			n.setX( m_origin.x + r*Math.cos(t));
			n.setY( m_origin.y + r*Math.sin(t));
	}
}
