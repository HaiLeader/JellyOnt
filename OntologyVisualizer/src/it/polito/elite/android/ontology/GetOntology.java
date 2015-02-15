package it.polito.elite.android.ontology;

import it.polito.elite.android.layout.NodeItem;
import it.polito.elite.android.layout.RadialTreeLayout;
import it.polito.elite.android.widget.radialmenu.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.zip.Inflater;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.util.iterator.Filter;
import com.hp.hpl.jena.vocabulary.OWL;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.Color;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;



public class GetOntology extends AsyncTask<Object, Void, OntModel> {
	
	//se è un URL allora uso get Ontology
	
	OntDocumentManager ont_manager;
	OntModel ont_model;
	AssetManager assets;
	JenaWrapper jw;
	Activity activity;
	Context context;
//	private ProgressDialog dialog;
//	private AlertDialog alertDialog;
	private Dialog alertDialog;
	AlertDialog.Builder builder; 
	
	//provo con popup window normale
	private PopupWindow progressPopup;
	
	
	//La string è il nome della risorsa, che è univoco
	private HashMap<String,NodeItem> nodes = new HashMap<String,NodeItem>();

	
	public GetOntology(Activity activity, Context context, JenaWrapper jw){
		this.context = context;
		this.jw = jw;
		this.activity = activity;
		//inizializzo la dialog
//		dialog = new ProgressDialog(context);
		
		//inflatto l'xml
		LayoutInflater inflater = (LayoutInflater)  context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		//inflatto il layout
		View progressdialog_layout =  inflater.inflate(R.layout.progressdialog_layout,null);
//		progressPopup = new PopupWindow(progressdialog_layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
		
		//trovo la textView
		TextView tv = (TextView)progressdialog_layout.findViewById(R.id.loading);
		tv.setTextColor(Color.WHITE);
		alertDialog = new Dialog(context, R.style.Theme_Dialog_Translucent);
		alertDialog.setContentView(progressdialog_layout);
		//non viene elimata se clicco fuori dalla finestra
		alertDialog.setCanceledOnTouchOutside(false);
//		builder = new AlertDialog.Builder(context, android.R.style.Theme_NoDisplay);
//		builder.setView(progressdialog_layout);
				
	}
	
	@Override
	protected void onPreExecute(){
//		dialog.setTitle("Ontology Visualizer");
//		dialog.setMessage("Loading ontology data...");
//		dialog.show();
//		dialog.setCancelable(false);
		
		
//		alertDialog = builder.create();
		alertDialog.show();
//		progressPopup.showAsDropDown(null);

		super.onPreExecute();
		
	}


    
	@Override
	protected OntModel doInBackground(Object... params) {
		String path = (String)params[0];
		assets = (AssetManager) params[1];
		if(path.startsWith("http")){
			ont_model = ModelFactory.createOntologyModel();
			ont_manager = new OntDocumentManager();
			ont_model = ont_manager.getOntology(path, OntModelSpec.OWL_MEM);
			return ont_model;
		}
		else{
			ont_model =  ModelFactory.createOntologyModel();

			//creo input stream per leggere il file
			InputStream is = null;
			try {
				is = assets.open(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			ont_model.read(is, "");
			return ont_model;
		}
		
		
	}
	
	@Override
	protected void onPostExecute(OntModel ont_model){
//		dialog.dismiss();
		alertDialog.dismiss();
//		progressPopup.dismiss();
		
		jw.setOnt_model(ont_model);
		Log.v("fine", "fine");
	}
	

	

	
	
		



}
