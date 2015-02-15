 package it.polito.elite.android.ontology;


import it.polito.elite.android.layout.LayoutRenderer;
import android.widget.RelativeLayout.LayoutParams;
import it.polito.elite.android.layout.NodeItem;
import it.polito.elite.android.layout.NodeRenderer;
import it.polito.elite.android.layout.RadialTreeLayout;
import it.polito.elite.android.layout.RadialTreeLayout;
import it.polito.elite.android.widget.radialmenu.ExpandableRadialMenuItem;
import it.polito.elite.android.widget.radialmenu.R;
import it.polito.elite.android.widget.radialmenu.RadialMenu;
import it.polito.elite.android.widget.radialmenu.RadialMenuItem;
import it.polito.elite.android.widget.radialmenu.R.drawable;
import it.polito.elite.android.widget.radialmenu.R.id;
import it.polito.elite.android.widget.radialmenu.R.layout;
import it.polito.elite.android.widget.radialmenu.R.menu;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ExecutionException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.DragEvent;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnDragListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ViewOntologyActivity extends Activity {

	private static final int LOW_DPI_STATUS_BAR_HEIGHT = 19;

	private static final int MEDIUM_DPI_STATUS_BAR_HEIGHT = 25;

	private static final int HIGH_DPI_STATUS_BAR_HEIGHT = 38;

	private int screen_h, screen_w;

	int statusBarHeight = 0;

	//ho un vettore con le tre toolbar, così il touch event lo passo solo alle tre (tranne a quella che ho cliccato già)
	ArrayList<RadialMenu> array_toolbar = new ArrayList<RadialMenu>();

	private RadialMenu toolbar_edit;
	private RadialMenu toolbar_search;
	private RadialMenu toolbar_browse;
	
	private RadialMenu ontologyMenu;

	//dove salvo l'ontologia
	OntModel ont_model;

	//path dell'ontologia
	String path;

	//Jena wrapper
	JenaWrapper jw;
	//	JenaWrappersAsync jwasync;

	//Radial tree layout
	RadialTreeLayout rtl;
	RadialTreeLayout rtl2;

	//vista dove disegno l'ontologia
	LayoutRenderer renderer;
	RelativeLayout viewOntologyLayout;

	//progress dialog per il caricamento dell'ontologia
	private ProgressDialog progress_dialog;

	/*
	 * GESTIONE BOOKMARK
	 */

	//	private HashMap<String, ArrayList<Bookmark>> bookmarks = new HashMap<String, ArrayList<Bookmark>>();
	private PopupWindow bookmark_popup_list;
	private PopupWindow bookmark_popup_add;
	EditText edittext;
	View bookmark_popup_list_layout;
	LinearLayout bookmark_deleteicon;
	View bookmark_popup_add_layout;
	
	ArrayList<Bookmark> bookmarks_list;
	LinearLayout bookmark_list_view;
	//memorizzo le textview dei bookmark
	//	ArrayList<TextView> bookmark_text_view;

	/*
	 * GESTIONE OPEN VIEW
	 */	
	View view_popup_layout;
	LinearLayout view_list_view;
	private PopupWindow view_popup;
	LinearLayout viewo_list_layout;
	//vista di default per ritornare all0ontologia completa
	private ViewO default_view;
	
	/*
	 * GESTIONE ADD TO VIEW
	 */
	public static boolean NODE_CLICKABLE = false;
	private LinearLayout choose_nodes_layout;
	private LinearLayout setlabel_layout;
	private PopupWindow addtoview_popup;
	private PopupWindow addtoview_setlabel_popup;
	private EditText viewLabel;
	//nodi selezionati
//	private ArrayList<NodeRenderer> view_nodes = new ArrayList<NodeRenderer>();
	//elenco delle view memorizzatr
	private ArrayList<ViewO> viewO_list = new ArrayList<ViewO>();
	//viewo corrente
//	private ViewO viewo = null;
	
	


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.view_ontology);
		viewOntologyLayout = (RelativeLayout) findViewById(R.id.viewontology);
		viewOntologyLayout.setBackgroundColor(Color.BLACK);

		

		screen_h = getIntent().getExtras().getInt("screen_h");
		screen_w = getIntent().getExtras().getInt("screen_w");

		//se arrivo qui da un'altra vista devo cmq memorizzare il path, ad esempio se ci arrivo dalla popup window
		//trovo la text view
		TextView ont_name = (TextView) findViewById(R.id.ontologyname);

		//prendo il path
		path = getIntent().getExtras().getString("path");
		//utilizzo uno di defaul per visualizzare sempre la stessa ontologia
		//		path = "ontology_folder/ontology.owl";


		//una volta passato il path, aggiorno l'ordine degli ultimi path
		//lo aggiorno solo de il path aperto è diverso dall'ultimo del vettore
		//questo aggiornamento dovrei farlo da view ontology...
		if(!HomePageActivity.last_path[2].equals(path)){
			for(int i=1; i<HomePageActivity.last_path.length; i++){
				HomePageActivity.last_path[i-1] = HomePageActivity.last_path[i];
			}
			//una volta finito metto il path corrente all'ultima posizione, cioè al 2
			HomePageActivity.last_path[2] = path;		
		}
		//devo usare le shared preferences per renderle persistenti

		//hpa.updatePaths(path);

		//aggiungo drag listener
		viewOntologyLayout.setOnDragListener(drag_listener);


		//dimensioni schermo
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);

		//è jena wrapper che dovrebbe far partire la progress dialog
		//devo passare il giusto context a jena wrapper che poi passa all'async dove c'è la dialog bar


		renderer = new LayoutRenderer(getApplicationContext());
		viewOntologyLayout.addView(renderer);

		//devo passarlo in ingresso al jw, che fa il conto di rtl e disegna su renderer

		jw = new JenaWrapper(path, getAssets(), ViewOntologyActivity.this,viewOntologyLayout.getRootView().getContext() ,renderer);

		ont_name.setText(path);
		//istanzio le toolbar

		toolbar_search = new RadialMenu(getApplicationContext(), R.drawable.ontology_search, 850, 50, 0, 360, 50, 100, true, true);
		//item della toolbar search
		RadialMenuItem sparql_item = new RadialMenuItem("SPARQL", "sparql", 0);

		//questo dovra essere expandable
		ExpandableRadialMenuItem text_item = new ExpandableRadialMenuItem("full-text", "text", 0);
		//sottomenu dell'expandable menu item
		RadialMenuItem fulltextsearch_item = new RadialMenuItem("", "fulltextsearch", 0);
		text_item.addSubItem(fulltextsearch_item);
		//aggiunto gli item alla toolbar
		toolbar_search.addItem(sparql_item);
		toolbar_search.addItem(text_item);

		//aggiungo all'arraylist
		array_toolbar.add(toolbar_search);

		//non lo aggiungo alla vista
		//aggiungo alle toolbar l'onclicklistern, così so quale id è stato cliccato
		//		toolbar_search.setOnTouchListener(checkIdClick); 


		//toolbar edit
		toolbar_edit = new RadialMenu(getApplicationContext(), R.drawable.ontology_edit, 900, 200, 0, 360, 50, 100, true, true);
		//items, sono tutti expandable, ognugno con 3 sezioni
		ExpandableRadialMenuItem concept_item = new ExpandableRadialMenuItem("Concept", "concept", 0);
		//tre sottomenu
		RadialMenuItem concept_edit = new RadialMenuItem("", "conceptedit", R.drawable.edit);
		RadialMenuItem concept_create = new RadialMenuItem("", "conceptcreate", R.drawable.create);
		RadialMenuItem concept_delete = new RadialMenuItem("", "conceptdelete", R.drawable.delete);
		//aggiungo a concept item
		concept_item.addSubItem(concept_delete);
		concept_item.addSubItem(concept_create);
		concept_item.addSubItem(concept_edit);
		//aggiungo concept item al menu
		toolbar_edit.addItem(concept_item);

		ExpandableRadialMenuItem relation_item = new ExpandableRadialMenuItem("Relation", "relation", 0);

		RadialMenuItem relation_edit = new RadialMenuItem("", "relationedit", R.drawable.edit);
		RadialMenuItem relation_create = new RadialMenuItem("", "relationcreate", R.drawable.create);
		RadialMenuItem relation_delete = new RadialMenuItem("", "relationdelete", R.drawable.delete);

		relation_item.addSubItem(relation_delete);
		relation_item.addSubItem(relation_create);
		relation_item.addSubItem(relation_edit);

		toolbar_edit.addItem(relation_item);

		//instance item
		ExpandableRadialMenuItem instance_item = new ExpandableRadialMenuItem("Instance", "instance", 0);

		RadialMenuItem instance_edit = new RadialMenuItem("", "instanceedit", R.drawable.edit);
		RadialMenuItem instance_create = new RadialMenuItem("", "instancecreate", R.drawable.create);
		RadialMenuItem instance_delete = new RadialMenuItem("", "instancedelete", R.drawable.delete);

		instance_item.addSubItem(instance_delete);
		instance_item.addSubItem(instance_create);
		instance_item.addSubItem(instance_edit);

		toolbar_edit.addItem(instance_item);


		//Restriction item
		ExpandableRadialMenuItem restriction_item = new ExpandableRadialMenuItem("Restriction", "restriction", 0);

		RadialMenuItem restriction_edit = new RadialMenuItem("", "restrictionedit", R.drawable.edit);
		RadialMenuItem restriction_create = new RadialMenuItem("", "restrictioncreate", R.drawable.create);
		RadialMenuItem restriction_delete = new RadialMenuItem("", "restrictiondelete", R.drawable.delete);

		restriction_item.addSubItem(restriction_delete);
		restriction_item.addSubItem(restriction_create);
		restriction_item.addSubItem(restriction_edit);

		toolbar_edit.addItem(restriction_item);

		//aggiungo all'arraylist
		array_toolbar.add(toolbar_edit);

		//aggiungo alle toolbar l'onclicklistern, così so quale id è stato cliccato
		//		toolbar_edit.setOnTouchListener(checkIdClick); 





		//toolbar browse
		//cambiare icona con bookmark !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
		toolbar_browse = new RadialMenu(getApplicationContext(), R.drawable.ontology_bookmark, 750, 300, 0, 360, 50, 100, true, true);

		RadialMenuItem bookmark = new RadialMenuItem("Bookmark", "bookmark", 0);		
		RadialMenuItem addtoview = new RadialMenuItem("Add to view", "addtoview", 0);
		RadialMenuItem openview = new RadialMenuItem("Open view", "openview", 0);

		toolbar_browse.addItem(bookmark);
		toolbar_browse.addItem(addtoview);
		toolbar_browse.addItem(openview);
		//aggiungo all'arraylist
		array_toolbar.add(toolbar_browse);

		toolbar_browse.setOnClickListener(browseClickListener);





		ontologyMenu = new RadialMenu(getApplicationContext(), R.drawable.ic_launcher, size.x - 220, size.y - 220 - 48, 180, -90, 100, 200, false, false);
		ontologyMenu.setIconPositionToCorner();

		//creo i tre item
		RadialMenuItem browse_item = new RadialMenuItem("", "browse", R.drawable.ontology_bookmark);
		RadialMenuItem edit_item = new RadialMenuItem("", "edit", R.drawable.ontology_edit);
		RadialMenuItem search_item = new RadialMenuItem("", "search", R.drawable.ontology_search);

		//aggiungo al menu
		ontologyMenu.addItem(browse_item);
		ontologyMenu.addItem(edit_item);
		ontologyMenu.addItem(search_item);

		ontologyMenu.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				RadialMenu rm = (RadialMenu) v;
				String idcliccato = rm.getIdcliccato();
				if(idcliccato!=null){
					//devo controllare se sono landascape o portrait
					Display display = getWindowManager().getDefaultDisplay();
					Point size = new Point();
					display.getSize(size);
					if(idcliccato.equals("browse")){
						if(!toolbar_browse.isInView()){
							viewOntologyLayout.addView(toolbar_browse);
							toolbar_browse.setInView(true);
							//serve ad evitare che la toolbar compaia al di fuori dello schermo
//							if(toolbar_browse.getOffset_x()>size.x- (toolbar_browse.getOuter_radius()*2)){
//								float off_x = (size.x - (toolbar_browse.getOuter_radius()*2));
//								toolbar_browse.setOffset_x(off_x);
//							}
//							if(toolbar_browse.getOffset_y()>size.y - (toolbar_browse.getOuter_radius()*2)){
//								float off_y = (size.y - (toolbar_browse.getOuter_radius()*2));
//								toolbar_browse.setOffset_y(off_y);
//							}
//							
//							if(toolbar_browse.getOffset_x()<0){
//								toolbar_browse.setOffset_x(0);
//							}
//							if(toolbar_browse.getOffset_y()<0){
//								toolbar_browse.setOffset_y(0);
//							}
						}
						else{
							viewOntologyLayout.removeView(toolbar_browse);
							toolbar_browse.setInView(false);
							
						}

					}
					else if(idcliccato.equals("edit")){
						//devo aprire toolbar
						//mi conviene averle già istanziate con tutti gli elementi,
						//quando clicco aggiungo o rimuovo dalla vista
						if(!toolbar_edit.isInView()){
							viewOntologyLayout.addView(toolbar_edit);
							toolbar_edit.setInView(true);
							//serve ad evitare che la toolbar compaia al di fuori dello schermo
//							if(toolbar_edit.getOffset_x()>size.x - (toolbar_edit.getOuter_radius()*2)){
//								float off_x = (size.x - (toolbar_edit.getOuter_radius()*2));
//								toolbar_edit.setOffset_x(off_x);
//							}
//							if(toolbar_edit.getOffset_y()>size.y- (toolbar_edit.getOuter_radius()*2)){
//								float off_y = (size.y - (toolbar_edit.getOuter_radius()*2));
//								toolbar_edit.setOffset_y(off_y);
//							}
//							if(toolbar_edit.getOffset_x()<0){
//								toolbar_edit.setOffset_x(0);
//							}
//							if(toolbar_edit.getOffset_y()<0){
//								toolbar_edit.setOffset_y(0);
//							}
						}
						else{
							viewOntologyLayout.removeView(toolbar_edit);
							toolbar_edit.setInView(false);
						}

					}
					else if(idcliccato.equals("search")){
						//devo aprire toolbar
						//mi conviene averle già istanziate con tutti gli elementi,
						//quando clicco aggiungo o rimuovo dalla vista
						if(!toolbar_search.isInView()){
							viewOntologyLayout.addView(toolbar_search);
							toolbar_search.setInView(true);
							//serve ad evitare che la toolbar compaia al di fuori dello schermo
//							if(toolbar_search.getOffset_x()>size.x- (toolbar_search.getOuter_radius()*2)){
//								float off_x = (size.x - (toolbar_search.getOuter_radius()*2));
//								toolbar_search.setOffset_x(off_x);
//							}
//							if(toolbar_search.getOffset_y()>size.y- (toolbar_search.getOuter_radius()*2)){
//								float off_y = (size.y - (toolbar_search.getOuter_radius()*2));
//								toolbar_search.setOffset_y(off_y);
//							}
//							if(toolbar_search.getOffset_x()<0){
//								toolbar_search.setOffset_x(0);
//							}
//							if(toolbar_search.getOffset_y()<0){
//								toolbar_search.setOffset_y(0);
//							}
						}
						else{
							viewOntologyLayout.removeView(toolbar_search);
							toolbar_search.setInView(false);
						}


					}
					else if(idcliccato=="home"){
						Log.v("idcliccato", "home");
						Intent myIntent = new Intent(getApplicationContext(), HomePageActivity.class);
						startActivity(myIntent);
					}
				}

			}
		});
		//aggiungo menu alla vista
		viewOntologyLayout.addView(ontologyMenu);


	}

	//click listener del menu browse
	private OnClickListener browseClickListener = new OnClickListener() {

		

		public void onClick(View v) {
			//cast di v a radial menu
			final RadialMenu rm = (RadialMenu) v;
			//prendo l'id cliccato
			String idcliccato = rm.getIdcliccato();
			
			

			if(idcliccato!=null){
				//prendo solo in mode dell'ontologia
				final String ontoname = path.substring((path.lastIndexOf("/"))+1);
				Log.v("idcliccato", idcliccato);
				
				
				if(idcliccato=="bookmark"){
					
//					final ArrayList<Bookmark> bookmarks_list = new ArrayList<Bookmark>();
					bookmarks_list = new ArrayList<Bookmark>();
					
					//apro popup
					LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//inflatto il layout
					bookmark_popup_list_layout =  inflater.inflate(R.layout.bookmark_popup_list_layout,null);
					//devo avere delle textview e visualizzare i bookmarks della vista

					//prendo la list view dove visualizzo i bookmark
					bookmark_list_view = (LinearLayout) bookmark_popup_list_layout.findViewById(R.id.bookmarklist);
					//prendo la view dove visualizzo le icone di delete
					bookmark_deleteicon = (LinearLayout) bookmark_popup_list_layout.findViewById(R.id.deleteicon);

					//PROVO A LEGGERE IL FILE
					try{

		                FileInputStream fis = openFileInput(ontoname+"bookmark.txt");
		                DataInputStream isr = new DataInputStream(fis);
		                BufferedReader buff = new BufferedReader(new InputStreamReader(isr));
		                String line = null;
		                int index = 0;
		                while((line = buff.readLine()) != null)
		                {
		                	
		                    //devo separare la line sul carattere ,
		                	String[] values = line.split(",");
		                	//creo il bookmark con i valori che ho
		                	final String label = values[0];
		                	
		                	//questa è la matrice
		                	float v1 = Float.parseFloat(values[1]);
		                	float v2 = Float.parseFloat(values[2]);
		                	float v3 = Float.parseFloat(values[3]);
		                	float v4 = Float.parseFloat(values[4]);
		                	float v5 = Float.parseFloat(values[5]);
		                	float v6 = Float.parseFloat(values[6]);
		                	float v7 = Float.parseFloat(values[7]);
		                	float v8 = Float.parseFloat(values[8]);
		                	float v9 = Float.parseFloat(values[9]);
		                	
		                	final Matrix matrix = new Matrix();
		                	
		                	float[] array= {v1,v2,v3,v4,v5,v6,v7,v8,v9};
		                	
		                	
		                	matrix.setValues(array);
		                	
		                	//dal file di testo devo poi leggere la matrice
		
		                	Bookmark tmp = new Bookmark(getApplicationContext(), path, label, index, renderer, matrix);
		                	
							tmp.setPadding(0, 10, 0, 10);
							tmp.setClickable(true);
							
							//lo aggiungo alla lista
		                	bookmarks_list.add(tmp);
		                	index ++;
		        
		       
		                }
		                fis.close();
		                isr.close();
		            } 
		            catch (FileNotFoundException e) 
		            {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            } 
		            catch (IOException e) 
		            {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
					
					
					bookmark_popup_list = new PopupWindow(bookmark_popup_list_layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					//la rendo focusable per poter dare la label
					bookmark_popup_list.setFocusable(true);
					
					//visualizzo la popup
					bookmark_popup_list.showAtLocation(rm, Gravity.CENTER, 0, -100);

					
					if(bookmarks_list.size()>0){
						//bookmark_list_view.removeAllViews();
						for(int i1=0; i1<bookmarks_list.size(); i1++){
							//devo semplicemente aggiungerli alla vista
							final Bookmark tmp = bookmarks_list.get(i1);
							bookmark_list_view.addView(tmp);
//							final Bookmark2 tmp = new Bookmark2(getApplicationContext(), bookmarks_list.get(i1).getLabel());
//							bookmark_list_view.addView(tmp );
							
							 //prendo l'icona
					        final ImageView iv = (ImageView) tmp.findViewById(R.id.imageView1);
					        
					       
					        iv.setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									Log.v("delete", "delete");
									bookmark_list_view.removeView(tmp);
									bookmark_list_view.invalidate();
									//devo poi rimuoverla anche dall'arraylist, altrimenti si salva sul file di testo
									int index = tmp.getIndex();
									bookmarks_list.remove(index);
									//dopo di questo dovrei aggiornare gli indici
									updateBookmarkIndex();
									
								}
							});
						}
						bookmark_list_view.invalidate();
					}

					
					//se clicco su add to bookmarks

					//prendo il bottone della popup
					Button addtobookmarks = (Button) bookmark_popup_list_layout.findViewById(R.id.addtobookmark);
					
					
					//click listener sul bottone
					
					LayoutInflater inflater_2 = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//inflatto il layout
					bookmark_popup_add_layout =  inflater_2.inflate(R.layout.bookmark_popup_add_layout,null);
					bookmark_popup_add = new PopupWindow(bookmark_popup_add_layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
					//la rendo focusable per poter dare la label
					bookmark_popup_add.setFocusable(true);
					
					
					
					
					//se cliccko su addtobookmark, apro una nuova popup
					addtobookmarks.setOnClickListener(new OnClickListener() {
						
						
						public void onClick(View v) {
							//chiudo la popup precedente
							bookmark_popup_list.dismiss();
							//visualizzo la popup
							bookmark_popup_add.showAtLocation(rm, Gravity.CENTER, 0, -100);
							//visualizzo la tastiera
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
							//prendo la label sulla seconda finestra
							edittext = (EditText) bookmark_popup_add_layout.findViewById(R.id.bookmarklabel);
							
							//prendo il bottone add (devo scrivere il file)
							Button add = (Button) bookmark_popup_add_layout.findViewById(R.id.add);
							add.setOnClickListener(new OnClickListener() {
								//
								public void onClick(View v) {	
									//devo nascondere la tastiera
									InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
									//prendo la label data al bookmark
		
									final String label = edittext.getText().toString();
									
									//controllo se la label non è vuoto
									if(label==null || label.equalsIgnoreCase("")){
										Toast toast = Toast.makeText(getApplicationContext(), "You didn't specify any label!", Toast.LENGTH_LONG);
										toast.show();
									}
									else {
										//prendo la matrice che ha il renderer
										final Matrix m = renderer.getTransformationMatrix();
										
										//creo l'oggetto bookmark
										Bookmark bookmark = new Bookmark(getApplicationContext(),path, label,bookmarks_list.size(), renderer, m);
										bookmark.setPadding(0, 10, 0, 10);
										bookmark.setClickable(true);
										
										//lo aggiungo alla lista
										bookmarks_list.add(bookmark);
			
										//lo aggiungo alla vista
										bookmark_list_view.addView(bookmark);
										//aggiorno
										bookmark_list_view.invalidate();
										//devo scrivere su file

										 try {
										    	
										        FileOutputStream output = openFileOutput(ontoname+"bookmark.txt", MODE_PRIVATE);
										   
										        OutputStreamWriter writer = new OutputStreamWriter(output);
										        
										        //devo ciclare bookmark lists
										        for(int i = 0; i <bookmarks_list.size(); i++){
										        	Bookmark tmp = bookmarks_list.get(i);
										        	
										        	//prendo la matrice del bookmark e scrivo su file
										        	Matrix towrite = tmp.getMB();
										        	float[] values = new float[9];
										        	towrite.getValues(values);
										        	
										        	writer.write(
										        			tmp.getLabel()+","
										        			+values[0]+","
										        			+values[1]+","
										        			+values[2]+","
										        			+values[3]+","
										        			+values[4]+","
										        			+values[5]+","
										        			+values[6]+","
										        			+values[7]+","
										        			+values[8]+"\r\n");
										        }
										        
//										        writer.flush();
										        writer.close();

										    } catch (IOException e) {
										        e.printStackTrace();
										    }
									    
										//chiudo quella attuale
										bookmark_popup_add.dismiss();
									}
		
											
									
		
								}
		
							});
							
							//gestione bottone close della seconda finestra
							//qui devo memorizzare il file
							ImageView close_add = (ImageView) bookmark_popup_add_layout.findViewById(R.id.close);
							close_add.setOnClickListener(new OnClickListener() {

								public void onClick(View v) {									
									edittext.setText(null);
									bookmark_popup_add.dismiss();
									//devo nascondere la tastiera
									InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(edittext.getWindowToken(), 0);
								}
							});
							
						}
					});

					//gestione bottone close
					ImageView close = (ImageView) bookmark_popup_list_layout.findViewById(R.id.close);
					close.setOnClickListener(new OnClickListener() {
						
						public void onClick(View v) {
							//scrivo di nuovo il file di testo
							 try {
							    	
							        FileOutputStream output = openFileOutput(ontoname+"bookmark.txt", MODE_PRIVATE);
							   
							        OutputStreamWriter writer = new OutputStreamWriter(output);
							        
							        //devo ciclare bookmark lists
							        for(int i = 0; i <bookmarks_list.size(); i++){
							        	Bookmark tmp = bookmarks_list.get(i);
							        	
							        	//prendo la matrice del bookmark e scrivo su file
							        	Matrix towrite = tmp.getMB();
							        	float[] values = new float[9];
							        	towrite.getValues(values);
							        	
							        	writer.write(
							        			tmp.getLabel()+","
							        			+values[0]+","
							        			+values[1]+","
							        			+values[2]+","
							        			+values[3]+","
							        			+values[4]+","
							        			+values[5]+","
							        			+values[6]+","
							        			+values[7]+","
							        			+values[8]+"\r\n");
							        }
							        
//							        writer.flush();
							        writer.close();

							    } catch (IOException e) {
							        e.printStackTrace();
							    }
							//elimino il contenuto della edittext
							bookmark_list_view.removeAllViews();
							bookmark_deleteicon.removeAllViews();
							bookmark_popup_list.dismiss();							
						}
					});
					

				}
				
				
				
				/*
				 * OPEN VIEW
				 * visualizzo la lista delle view caricate
				 */

				else if(idcliccato=="openview"){
					//devo aprire una nuova popup come per i bookmark
					//apro popup
					LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
					//inflatto il layout
					view_popup_layout =  inflater.inflate(R.layout.openview_popup_layout,null);

					view_popup = new PopupWindow(view_popup_layout,250,200);
					view_popup.setFocusable(true);
					//mostro la popup
					view_popup.showAtLocation(rm, Gravity.CENTER, 0, -100);
					
					
                	//svuoto l'elenco delle viewO prima di leggere il file
                	viewO_list.clear();
					
                	
					//devo mostrale la lista delle view, quindi devo leggere il file e memorizzare tutto nellarray
					try{

		                FileInputStream fis = openFileInput(ontoname+"viewo.txt");
		                DataInputStream isr = new DataInputStream(fis);
		                BufferedReader buff = new BufferedReader(new InputStreamReader(isr));
		                String line = null;
		                int index = 0;
		                while((line = buff.readLine()) != null)
		                {
		                	//svuoto view_nodes, ogni volta che leggo una nuova riga
	
		                    //devo separare la line sul carattere ,
		                	String[] values = line.split(",");
		                	//creo la view con i valori che ho
		                	final String label = values[0];
		                			                	
		                	//alla prima posizione c'è la label, le altre devo ciclare, perchè sono variabili.
		                	//creo array list di stringhe, che tiene memorizzato il nome completo della risorsa
		                	//è ok perchè ogni risorsa è univoca
		                	ArrayList<String> view_nodes = new ArrayList<String>();
		                	for (int i = 1; i<values.length; i++){
		                		view_nodes.add(values[i]);
		                		
		                	}
		                	
		                	//posso creare viewO tmp
		                	ViewO tmp = new ViewO(getApplicationContext(), path, view_nodes, renderer, index);
		                	tmp.setLabel(label);
		                	tmp.setPadding(0, 10, 0, 10);
							tmp.setClickable(true);
		                	//aggiungo a viewO list
		                	viewO_list.add(tmp);    
		                	index++;
		                }
		                fis.close();
		                isr.close();
		            } 
		            catch (FileNotFoundException e) 
		            {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            } 
		            catch (IOException e) 
		            {
		                // TODO Auto-generated catch block
		                e.printStackTrace();
		            }
					
					//prendo la sezione di view dove visualizzo l'elenco delle viewo;
					viewo_list_layout = (LinearLayout) view_popup_layout.findViewById(R.id.viewolist);
					viewo_list_layout.removeAllViews();
					viewo_list_layout.invalidate();
					
					//string con tutti i nodi dell'ontologia
					Set<String> all_nodes = renderer.getNodes().keySet();
					ArrayList<String> all_nodes_array = new ArrayList<String>();
					for(HashMap.Entry<String, NodeRenderer> entry : renderer.getNodes_renderer().entrySet()){
						all_nodes_array.add(entry.getKey());
					}
					//creo la view di defaul
					default_view = new ViewO(getApplicationContext(), path, all_nodes_array, renderer, -1);
					default_view.setLabel("Original View");
					viewo_list_layout.addView(default_view);
					for(int i = 0; i < viewO_list.size(); i++){
						final ViewO tmp = viewO_list.get(i);
						viewo_list_layout.addView(tmp);
						
						//prendo l'icona
				        final ImageView iv = (ImageView) tmp.findViewById(R.id.delete);
				        
				       
				        iv.setOnClickListener(new OnClickListener() {
							
							public void onClick(View v) {
								//faccio cast a viewo
								Log.v("delete", "delete");
								//rimuovo dalla view
								viewo_list_layout.removeView(tmp);
								viewo_list_layout.invalidate();
								//rimuovo dall'arraylist 
								viewO_list.remove(tmp.getIndex());
								updateViewIndex();
								
							}
						});
						
					}
					viewo_list_layout.invalidate();
					
					//prendo il bottone close
					ImageView close = (ImageView)view_popup_layout.findViewById(R.id.close);
					close.setOnClickListener(new OnClickListener() {

						public void onClick(View v) {
							viewo_list_layout.removeAllViews();
							viewo_list_layout.invalidate();
							view_popup.dismiss();
							//scrivo il file di testo
							 try {
							    	
							        FileOutputStream output = openFileOutput(ontoname+"viewo.txt", MODE_PRIVATE);
							   
							        OutputStreamWriter writer = new OutputStreamWriter(output);
							        
							        //devo ciclare la viewo
							        for(int i = 0; i <viewO_list.size(); i++){
							        	ViewO tmp = viewO_list.get(i);
							        	
							        	//array dei nodi
							        	ArrayList<String> nodes = tmp.getView_nodes();
							        	
							        	writer.write(tmp.getLabel()+",");
							        	for(int j = 0; j <nodes.size(); j++){
							        		String nodename = nodes.get(j);
							        		if(j==nodes.size()-1){
							        			//devo fare un a capo
							        			writer.append(nodename+"\r\n");
							        		}
							        		else{
							        			writer.append(nodename+",");
							        		}
							        		
							        	}
							        }
							        
//							        writer.flush();
							        writer.close();

							    } catch (IOException e) {
							        e.printStackTrace();
							    }

						}
					});
				}
				
				
				/*
				 * ADD TO VIEW
				 * permetto all'utente di selezionare i nodi
				 */
				
				
				else if(idcliccato=="addtoview"){
					
					//devo rendere i nodi cliccabili
					if(NODE_CLICKABLE == false){
						NODE_CLICKABLE=true;
						//apro popup
						LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
						choose_nodes_layout = (LinearLayout)inflater.inflate(R.layout.addtoview_popup_layout,null);
						
						//inizializzo la popup
						addtoview_popup = new PopupWindow(choose_nodes_layout,LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
						
						
						addtoview_popup.showAtLocation(rm, Gravity.NO_GRAVITY, 0, 0);
											
						//prendo il bottone close
						ImageView close = (ImageView) choose_nodes_layout.findViewById(R.id.close);
						close.setOnClickListener(new OnClickListener() {
							
							public void onClick(View v) {
								//non si possono cliccare i nodi
								NODE_CLICKABLE = false;
								//chiudo la finestra senza salvare l'arraylist
								renderer.deselectAll();
								addtoview_popup.dismiss();
								//devo svuotare l'arraylist
//								view_nodes.clear();
								//non devo salvare su testo
								
							}
						});
						//prendo il bottone finish
						Button finish = (Button) choose_nodes_layout.findViewById(R.id.finish);
						
						finish.setOnClickListener(new OnClickListener() {
							
							public void onClick(View v) {
								//non si possono cliccare i nodi
								NODE_CLICKABLE = false;
								//devo controllare che ci siano dei nodi selezionati,
								//se non ce n'è nessuno, apro pupup che dice: non hai selezionato nessun nodo
								//devo togliere la view
								addtoview_popup.dismiss();
								//deseleziono tutti i nodi
//								renderer.deselectAll();
//								view_nodes.clear();
								if(renderer.getViewNodes().size()==0){
									Toast toast = Toast.makeText(getApplicationContext(), "You didn't select any node!", Toast.LENGTH_LONG);
									toast.show();
									
								}
								else{
									//devo aprire una nuova popup per settare la label
									LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
									setlabel_layout = (LinearLayout)inflater.inflate(R.layout.addtoview_setlabel_popup_layout,null);
									
									//inizializzo la popup
									addtoview_setlabel_popup = new PopupWindow(setlabel_layout,LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
									addtoview_setlabel_popup.setFocusable(true);
									addtoview_setlabel_popup.showAtLocation(rm, Gravity.CENTER, 0, -100);
									//mostro la tastiera 
									//visualizzo la tastiera
									InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
									
									//prendo la label
									viewLabel = (EditText) setlabel_layout.findViewById(R.id.viewlabel);
									
									
									
									//prendo il bottone add
									Button add = (Button) setlabel_layout.findViewById(R.id.add);
									add.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											//devo nascondere la tastiera
											InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
											imm.hideSoftInputFromWindow(viewLabel.getWindowToken(), 0);
											
											//prendo la label data alla view
											String view_name = viewLabel.getText().toString();
											//se la label è null oppure "", non deve andare avanti
											if(view_name==null || view_name.equalsIgnoreCase("")){
												Toast toast = Toast.makeText(getApplicationContext(), "You didn't specify any label!", Toast.LENGTH_LONG);
												toast.show();
											}
											else {
												Log.v("label", view_name);
												//setto la label a viewo
												ArrayList<String> view_nodes = new ArrayList<String>();
												view_nodes.addAll(renderer.getViewNodes());
												//creo l'oggetto viewo
												ViewO viewo = new ViewO(getApplicationContext(), path, view_nodes, renderer, viewO_list.size());
												viewo.setLabel(view_name);
												viewo.setPadding(0, 10, 0, 10);
												viewo.setClickable(true);
												//la aggiungo alla vista
												viewO_list.add(viewo);
												//dopo devo rimettere i viewnodes memorizzati in renderer a zero
												renderer.clearViewNodes();
												
												renderer.deselectAll();

												//scrivo il file di testo
												 try {
												    	
												        FileOutputStream output = openFileOutput(ontoname+"viewo.txt", MODE_PRIVATE);
												   
												        OutputStreamWriter writer = new OutputStreamWriter(output);
												        
												        //devo ciclare la viewo
												        for(int i = 0; i <viewO_list.size(); i++){
												        	ViewO tmp = viewO_list.get(i);
												        	
												        	//array dei nodi
												        	ArrayList<String> nodes = tmp.getView_nodes();
												        	
												        	writer.write(tmp.getLabel()+",");
												        	for(int j = 0; j <nodes.size(); j++){
												        		String nodename = nodes.get(j);
												        		if(j==nodes.size()-1){
												        			//devo fare un a capo
												        			writer.append(nodename+"\r\n");
												        		}
												        		else{
												        			writer.append(nodename+",");
												        		}
												        		
												        	}
												        }
												        
//												        writer.flush();
												        writer.close();

												    } catch (IOException e) {
												        e.printStackTrace();
												    }
												//chiudo
												addtoview_setlabel_popup.dismiss();
											}

										}
									});
									//prendo close
									ImageView close = (ImageView) setlabel_layout.findViewById(R.id.close);
									
									close.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											addtoview_setlabel_popup.dismiss();
											//devo nascondere la tastiera
											InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
											imm.hideSoftInputFromWindow(viewLabel.getWindowToken(), 0);
											//devo deselezionare i nodi
											renderer.deselectAll();
											
											
										}
									});	
								}
								
								
																
																			
								
							}
						});
					}
					
					
					
				}
			}

		}
	};
	@Override
    public void onConfigurationChanged(Configuration newConfig) { 
		super.onConfigurationChanged(newConfig);
		Log.v("screen change", "screen change");
		//devo cambiare posto al menù angolare
		//prendo di nuovo screen size, in realta devo semplicemente invertire
		screen_h = getIntent().getExtras().getInt("screen_w");
		screen_w = getIntent().getExtras().getInt("screen_h");
		
		//devo vedere se sono landscape o portrait
		if(newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE){
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			ontologyMenu.setOffset_x(size.x-220);
			ontologyMenu.setOffset_y(size.y-220 - 48);
			
			//la parte commentata è per le toolbar 
			
//			float off_x = toolbar_edit.getOffset_x();
//			if(off_x>size.x- (toolbar_edit.getOuter_radius()*2)){
//				toolbar_edit.setOffset_x(size.x - (toolbar_edit.getOuter_radius()*2)-10);
//			}
//			float off_y = toolbar_edit.getOffset_y();
//			if(off_y>=size.y- (toolbar_edit.getOuter_radius()*2)){
//				toolbar_edit.setOffset_y(size.y - (toolbar_edit.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x<0){
//				toolbar_edit.setOffset_x(0);
//			}
//			if(off_y<0){
//				toolbar_edit.setOffset_y(0);
//			}
//			float off_x_b = toolbar_browse.getOffset_x();
//			if(off_x_b>=size.x - (toolbar_browse.getOuter_radius()*2)){
//				toolbar_browse.setOffset_x(size.x - (toolbar_browse.getOuter_radius()*2)-10);
//				
//			}
//			float off_y_b = toolbar_browse.getOffset_y();
//			if(off_y_b>=size.y - (toolbar_browse.getOuter_radius()*2)){
//				toolbar_browse.setOffset_y(size.y - (toolbar_browse.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x_b<0){
//				toolbar_browse.setOffset_x(0);
//			}
//			if(off_y_b<0){
//				toolbar_browse.setOffset_y(0);
//			}
//			float off_x_s = toolbar_search.getOffset_x();
//			if(off_x_s>=size.x- (toolbar_search.getOuter_radius()*2)){
//				toolbar_search.setOffset_x(size.x - (toolbar_search.getOuter_radius()*2)-10);
//				
//			}
//			float off_y_s = toolbar_search.getOffset_y();
//			if(off_y_s>=size.y- (toolbar_search.getOuter_radius()*2)){
//				toolbar_search.setOffset_y(size.y - (toolbar_search.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x_s<0){
//				toolbar_search.setOffset_x(0);
//			}
//			if(off_y_s<0){
//				toolbar_search.setOffset_y(0);
//			}
		}
		else if(newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
			Display display = getWindowManager().getDefaultDisplay();
			Point size = new Point();
			display.getSize(size);
			ontologyMenu.setOffset_x(size.x-220);
			ontologyMenu.setOffset_y(size.y-220-48);
//			
//			//cambio anche l'offset delle toolbar
//			//offset x, solo se > di size.x, idem per offset y
//			float off_x = toolbar_edit.getOffset_x();
//			if(off_x>size.x- (toolbar_edit.getOuter_radius()*2)){
//				toolbar_edit.setOffset_x(size.x - (toolbar_edit.getOuter_radius()*2)-10);
//			}
//			float off_y = toolbar_edit.getOffset_y();
//			if(off_y>=size.y- (toolbar_edit.getOuter_radius()*2)){
//				toolbar_edit.setOffset_y(size.y - (toolbar_edit.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x<0){
//				toolbar_edit.setOffset_x(0);
//			}
//			if(off_y<0){
//				toolbar_edit.setOffset_y(0);
//			}
//			float off_x_b = toolbar_browse.getOffset_x();
//			if(off_x_b>=size.x - (toolbar_browse.getOuter_radius()*2)){
//				toolbar_browse.setOffset_x(size.x - (toolbar_browse.getOuter_radius()*2)-10);
//				
//			}
//			float off_y_b = toolbar_browse.getOffset_y();
//			if(off_y_b>=size.y - (toolbar_browse.getOuter_radius()*2)){
//				toolbar_browse.setOffset_y(size.y - (toolbar_browse.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x_b<0){
//				toolbar_browse.setOffset_x(0);
//			}
//			if(off_y_b<0){
//				toolbar_browse.setOffset_y(0);
//			}
//			float off_x_s = toolbar_search.getOffset_x();
//			if(off_x_s>=size.x- (toolbar_search.getOuter_radius()*2)){
//				toolbar_search.setOffset_x(size.x - (toolbar_search.getOuter_radius()*2)-10);
//				
//			}
//			float off_y_s = toolbar_search.getOffset_y();
//			if(off_y_s>=size.y- (toolbar_search.getOuter_radius()*2)){
//				toolbar_search.setOffset_y(size.y - (toolbar_search.getOuter_radius()*2)-10);
//				
//			}
//			//devo controllare anche se sono a <0
//			if(off_x_s<0){
//				toolbar_search.setOffset_x(0);
//			}
//			if(off_y_s<0){
//				toolbar_search.setOffset_y(0);
//			}	
		}
		
		ontologyMenu.invalidate();
//		toolbar_browse.invalidate();
//		toolbar_edit.invalidate();
//		toolbar_search.invalidate();
    }

	@Override
	public void onStop(){
		super.onStop();

	}



	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}


	private OnDragListener drag_listener = new OnDragListener() {

		public boolean onDrag(View v, DragEvent event) {
			//prendo la vista che sto spostando
			View menu = (View) event.getLocalState();

			switch (event.getAction()) {
			case DragEvent.ACTION_DRAG_STARTED:
				Log.v("drag", "drag started");  
				break;  
			case DragEvent.ACTION_DRAG_ENTERED:
				Log.v("drag", "drag entered");
				break;
			case DragEvent.ACTION_DRAG_EXITED:
				Log.v("drag", "drag exited");
				break;
			case DragEvent.ACTION_DROP:
				Log.v("drag","drop");
				// prendo il proprietario della vista menu
				RelativeLayout owner = (RelativeLayout) menu.getParent();
				owner.removeView(menu);
				//adesso prendo il relative layout che è la v che ho in ingresso
				RelativeLayout l = (RelativeLayout) v;
				l.addView(menu);
				menu.setVisibility(View.VISIBLE);
				//prendo le coordinate dell'evento drop
				float x = event.getX();
				float y = event.getY();			 
				//risposiziono la vista del menu con il centro sul cursore
				menu.setX(x - (menu.getWidth()/2) );
				menu.setY(y - (menu.getHeight()/2));
				//questa parte di sotto mi serve evitare che le toolbar vadano fuori dallo schermo
//				if(menu instanceof RadialMenu){
//					RadialMenu m = (RadialMenu) menu;
//					float off_x = (x - (menu.getWidth()/2));
//					if(off_x<0){
//						off_x=0;
//					}
//					if(off_x>l.getWidth()-menu.getWidth()){
//						off_x = l.getWidth()-menu.getWidth();
//					}
//					m.setOffset_x(off_x );
//					float off_y = (y - (menu.getHeight()/2));
//					if(off_y<0){
//						off_y=0;
//					}
//					if(off_y>l.getHeight()-menu.getHeight()){
//						off_y = l.getHeight()-menu.getHeight();
//					}
//					m.setOffset_y(off_y);
//				}
				l.postInvalidate();
				break;
			case DragEvent.ACTION_DRAG_ENDED:
				Log.v("drag","drag ended");
			default:
				break;
			}
			//l'evento è stato gestito e non deve essere propagato ulteriormente
			return true;
		}
	};

	//conversion from dp to px
	public int DPtoPX(int dp){
		float screen_density = getApplicationContext().getResources().getDisplayMetrics().density;
		int px = (int) ((int) dp*screen_density+0.5f);
		return px;
	}
	
	public void updateBookmarkIndex(){
		for(int i = 0; i<bookmarks_list.size(); i++){
			bookmarks_list.get(i).setIndex(i);
		}
	}
	
	public void updateViewIndex(){
		for(int i = 0; i< viewO_list.size(); i++){
			 viewO_list.get(i).setIndex(i);
		}
	}
	

}
