package it.polito.elite.android.ontology;

import it.polito.elite.android.layout.LayoutRenderer;
import it.polito.elite.android.layout.NodeItem;
import it.polito.elite.android.layout.RadialTreeLayout;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Point;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.util.iterator.NiceIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.mxgraph.view.mxGraph;

public class JenaWrapper {

	//assets e context dell'app per poter lavorare
	private AssetManager assets;
	private Context context;
	private Activity activity;
	
	GetOntology getOntology;
	
	//percorso dell'ontologia
	private String path;

	//ont_model dell'ontologia
	private OntModel ont_model;

	//La string è il nome della risorsa, che è univoco
	private HashMap<String,NodeItem> nodes = new HashMap<String,NodeItem>();
	
	boolean finished = false;
	LayoutRenderer renderer;
	RadialTreeLayout rtl2;
	


	/**
	 * La classe ha il compito di caricare una ontologia dentro un OntModel dato un path-
	 * 
	 * @param path
	 * @param assets
	 * @param context
	 */
	public JenaWrapper(String path, AssetManager assets, Activity activity, Context context, LayoutRenderer renderer) {

		this.path = path;
		this.assets = assets;
		this.context = context;
		this.renderer = renderer;

		GetOntology getOntology = new GetOntology(activity, context, this);
		Object[] params= {path,assets};
		getOntology.execute(params);

	}
	
	

	//viene richiamato dall'asynk task per caricare i nodi nell'array
	//arrivata qui devo dire al radial tree layout che puo' fare i conti!
	public void setOnt_model(OntModel ont_model) {
		this.ont_model = ont_model;
		addThingNode();
		Resource thing  = OWL.Thing;
		loadNodesAndRelations(null, nodes.get(thing.toString()));
		
		//qui devo calcolare rtl
		WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int screen_h = size.y;
		int screen_w = size.x;
		rtl2 = new RadialTreeLayout(this, screen_h, screen_w, context);
		renderer.setNodes(nodes);
		renderer.setRelations(rtl2.getRelations());
		renderer.setMaxRadius(rtl2.getMaxRadius());
//		renderer.setViewDimension(rtl2.getMaxRadius()*2);
		//perchè non è un thred ui
		renderer.postInvalidate();
	}

	public void addThingNode(){
		//per prima cosa creo il nodo thing e lo aggiungo	
		Resource thing = OWL.Thing;
		
		//il padre di thing è null
		NodeItem thing_node = new NodeItem();
		thing_node.setParent(null);
		thing_node.setResource(thing);
		thing_node.setHasSubclass(true);
		nodes.put(thing.toString(), thing_node);
	}

	
	public void loadNodesAndRelations(ExtendedIterator<OntClass> iterator, NodeItem source){
		//al primo giro nodeItem è thing
		if(iterator==null){
			//carico le rootclasses
			iterator = ont_model.listHierarchyRootClasses();			
		}
		//devo levare i blank node
		iterator = iterator.filterDrop(new Filter<OntClass>() {
			@Override
			public boolean accept(OntClass arg0) {
				// TODO Auto-generated method stub
				return ((Resource) arg0).isAnon();
			}
		});
		
		if(iterator.hasNext()){
			source.setHasSubclass(true);
			while(iterator.hasNext()){
				OntClass ontclass = iterator.next();
				//popolo l'arrey di Nodes
				NodeItem node = new NodeItem();
				node.setResource(ontclass);
				//setto il padre di ogni nodo
				node.setParent(source);
				//setto anche la dimensione del nodo, rispetto a quella del padre
				node.setIcondimension(source.getIcondimension() - 2);
				nodes.put(ontclass.toString(), node);	
				//per ogni figlio di source, aggiungo una relation
				if(ontclass.hasSubClass()){
					ExtendedIterator<OntClass> it = ontclass.listSubClasses(true);
					loadNodesAndRelations(it, node);	
				}
			}
		}
		
	}
	
	public OntModel getOnt_model() {
		return ont_model;
	}

	public HashMap<String,NodeItem> getNodes() {
		return nodes;
	}

	public static String getResourceName(Resource tmp){
		String name = tmp.toString();
		int index = name.indexOf("#")+1;
		if(index>0){
			name = name.substring(index);
		}
		return name;
	}
	
	
}






