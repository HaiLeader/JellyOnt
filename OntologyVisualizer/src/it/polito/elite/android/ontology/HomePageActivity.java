package it.polito.elite.android.ontology;

import it.polito.elite.android.widget.radialmenu.R;
import it.polito.elite.android.widget.radialmenu.RadialMenu;
import it.polito.elite.android.widget.radialmenu.RadialMenuItem;
import it.polito.elite.android.widget.radialmenu.SubdividedRadialMenuItem;
import it.polito.elite.android.widget.radialmenu.R.drawable;
import it.polito.elite.android.widget.radialmenu.R.id;
import it.polito.elite.android.widget.radialmenu.R.layout;
import it.polito.elite.android.widget.radialmenu.R.menu;
import android.app.Activity;
import android.app.ProgressDialog;


import android.os.Bundle;
	import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
	import android.view.Gravity;
import android.view.LayoutInflater;
	import android.view.Menu;
	import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

	public class HomePageActivity extends Activity {
		
		public static final String PATHS = "paths";
		
		//elementi per popup windows
		EditText url;
		Button go;
		ImageView fm;
		ImageView close;
		PopupWindow pw;
		
		
		//memorizzo le ultime tre ontologie viste
		//dato che ho un numero fisso, uso un array di 3 stringhe.
		private static String path0 = "ontology_folder/ontology3.owl";
		private static String path1 = "ontology_folder/ontology2.owl";
		private static String path2 = "ontology_folder/ontology.owl";
		
		
		
		public static  String[] last_path =  new String[3];
		//dovrei salvarle nelle shared preference
		
		
		//variabili menù, per aggiornare le label;
		private RadialMenuItem section1;
		private RadialMenuItem section2;
		private RadialMenuItem section3;
		
		//shared preferences
		SharedPreferences pref_path;
		
		@Override
		public void onCreate(Bundle savedInstanceState) {
			super.onCreate(savedInstanceState);
			setContentView(R.layout.activity_main);
			RelativeLayout container = (RelativeLayout)findViewById(R.id.container);
			container.setBackgroundColor(Color.BLACK);
			//quando l'activity si crea, prendo i valori e li salvo nel vettore
			pref_path = this.getSharedPreferences(PATHS, MODE_PRIVATE);	
			last_path[0] = pref_path.getString("0", path0);
			last_path[1] = pref_path.getString("1", path1);
			last_path[2] = pref_path.getString("2", path2);
			
			
			
			//creo il menu principale
			//deve essere posizionato al centro
			Display display = getWindowManager().getDefaultDisplay();
			final Point size = new Point();
			display.getSize(size);
			
			//centro dello schermo
			int center_x = size.x/2;
			int center_y = size.y/2;
			
			


			
			RadialMenu homePageMenu = new RadialMenu(getApplicationContext(), R.drawable.ic_launcher, center_x - 220, center_y - 220, 0, 360, 100,200, false, false);

			//il primo item è suddiviso in 4 parti
			SubdividedRadialMenuItem item1 = new SubdividedRadialMenuItem("","", 0);
			section1 = new RadialMenuItem(getPathName(pref_path.getString("0", path0)),"percorso1", R.drawable.ontology_min);
			section1.setTextToCorner(true);
			section2 = new RadialMenuItem(getPathName(pref_path.getString("1", path1)),"percorso2", R.drawable.ontology_min);
			section2.setTextToCorner(true);
			section3 = new RadialMenuItem(getPathName(pref_path.getString("2", path2)),"percorso3",R.drawable.ontology_min);
			section3.setTextToCorner(true);
			RadialMenuItem section4 = new RadialMenuItem("","browse", R.drawable.ontology_browse_fm);
			
			item1.addSubdivision(section1);
			item1.addSubdivision(section2);
			item1.addSubdivision(section3);
			item1.addSubdivision(section4);
			
			RadialMenuItem item2 = new RadialMenuItem("","create",R.drawable.ontology_create);
			
			homePageMenu.addItem(item2);
			homePageMenu.addItem(item1);

			
			//se aggiungo on clik listener, che mi ritorna l'id cliccato
			homePageMenu.setOnClickListener(new View.OnClickListener() {
				RadialMenu rm ;
				public void onClick(View v) {
					//devo risettare a null
					String idcliccato = null;
					rm = (RadialMenu) v;
					idcliccato = rm.getIdcliccato();
					Log.v("sei nell'activity",""+idcliccato);
					
					//qui controllo quale id è stato cliccato e cosa fare in base a questo
					if(idcliccato!=null){
						if(idcliccato.equals("create")){
							LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							//inflatto il layout
							View notavailable_layout =  inflater.inflate(R.layout.notavailableyet_layout,null);	
							final PopupWindow notavailable = new PopupWindow(notavailable_layout, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
							ImageView close = (ImageView) notavailable_layout.findViewById(R.id.close);
							close.setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									notavailable.dismiss();
								}
							});
							notavailable.showAtLocation(rm, Gravity.CENTER, 0, 0);
						}
						else if(idcliccato.equals("browse")){
							//apro popup
							LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
							//inflatto il layout
							View popup_layout =  inflater.inflate(R.layout.popup_layout,null);	
							pw = new PopupWindow(popup_layout, LayoutParams.WRAP_CONTENT, 200);
							//visualizzo la tastiera
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,0);
							
							pw.setFocusable(true);
							
							//visualizzo e posiziono
							pw.showAtLocation(v, Gravity.CENTER, 0, -100);
							
							//prendo tutti gli elementi della vista
							url = (EditText) popup_layout.findViewById(R.id.URL);
							go = (Button) popup_layout.findViewById(R.id.url_button);
							fm = (ImageView) popup_layout.findViewById(R.id.fileManager);
							close = (ImageView) popup_layout.findViewById(R.id.close);
							
							//se clicco su go, prendo il path e vado in view activity
							go.setOnClickListener(go_listener);
							//se clicco su close chiudo la popup
							close.setOnClickListener(close_listener);
							//se clicco sul fm, vado in una nuova attività
							fm.setOnClickListener(new OnClickListener() {
								
								public void onClick(View v) {
									//devo nascondere la tastiera
									InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
									imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
//									pw.dismiss();
									LayoutInflater inflater = (LayoutInflater)  v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
									//inflatto il layout
									View notavailable_layout =  inflater.inflate(R.layout.notavailableyet_layout,null);	
									final PopupWindow notavailable = new PopupWindow(notavailable_layout, LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
									ImageView close = (ImageView) notavailable_layout.findViewById(R.id.close);
									close.setOnClickListener(new OnClickListener() {
										
										public void onClick(View v) {
											notavailable.dismiss();
										}
									});
									notavailable.showAtLocation(rm, Gravity.CENTER, 0, 0);
									
									pw.dismiss();
									
								}
							});
							 
							
							
						}
						else if(idcliccato.startsWith("percorso")){
							
							//prendo il percorso dell'ontologia cliccata
							int index = Integer.parseInt(idcliccato.substring(8)) ;
							String path = last_path[index-1];
							Log.v("", ""+path);												
							Intent myIntent = new Intent(getApplicationContext(), ViewOntologyActivity.class);
							//passo alla nuova attività il path cliccato
							myIntent.putExtra("path", path);
							myIntent.putExtra("screen_h", size.y);
							myIntent.putExtra("screen_w", size.x);
							startActivity(myIntent);
							//devo passare alla nuova activity il percorso cliccato
						}
					}
					
				}
			});
			
			container.addView(homePageMenu);
			
		}
		
		@Override
		public void onStart(){
			super.onStart();
			//devo aggiornare le label delle ontologie
			section1.setLabel(getPathName(pref_path.getString("0", path0)));
			section2.setLabel(getPathName(pref_path.getString("1", path1)));
			section3.setLabel(getPathName(pref_path.getString("2", path2)));
			
		}
		@Override
		public void onRestart(){
			super.onRestart();
			//devo aggiornare le label delle ontologie
			section1.setLabel(getPathName(pref_path.getString("0", path0)));
			section2.setLabel(getPathName(pref_path.getString("1", path1)));
			section3.setLabel(getPathName(pref_path.getString("2", path2)));
			
		}
		@Override
		public void onResume(){
			super.onResume();
			//devo aggiornare le label delle ontologie
			section1.setLabel(getPathName(pref_path.getString("0", path0)));
			section2.setLabel(getPathName(pref_path.getString("1", path1)));
			section3.setLabel(getPathName(pref_path.getString("2", path2)));
			
		}
		
		
		//my click listener
		private OnClickListener go_listener = new OnClickListener() {
			
			public void onClick(View v) {
				String path = url.getText().toString();
				//devo nascondere la tastiera
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
				Log.v("path", path);
				if(!path.equals("") && path!=null){
					Log.v("path", path);
					//devo passare all'altra attività
					Intent myIntent = new Intent(getApplicationContext(), ViewOntologyActivity.class);
					//passo alla nuova attività il path cliccato
					myIntent.putExtra("path", path);
					startActivity(myIntent);
					pw.dismiss();
				}
				
				
			}
			
		};
		
		private OnClickListener close_listener = new OnClickListener() {
			
			public void onClick(View v) {
				//devo nascondere la tastiera
				InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(url.getWindowToken(), 0);
				//devo chiudere la popup
				pw.dismiss();
				
			}
		};
		

		

		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			getMenuInflater().inflate(R.menu.activity_main, menu);
			return true;
		}
		
		
		@Override
		public void onStop(){
			super.onStop();
			//quando l'applicazione si chiude
			SharedPreferences pref_path = getSharedPreferences(PATHS, MODE_PRIVATE);
			//le memorizzo
			SharedPreferences.Editor editor = pref_path.edit();
			editor.putString("0", last_path[0]);
			editor.putString("1", last_path[1]);
			editor.putString("2", last_path[2]);
			editor.commit();
		}
		@Override
		public void onPause(){
			super.onPause();
			//quando l'applicazione si chiude
			SharedPreferences pref_path = getSharedPreferences(PATHS, MODE_PRIVATE);
			//le memorizzo
			SharedPreferences.Editor editor = pref_path.edit();
			editor.putString("0", last_path[0]);
			editor.putString("1", last_path[1]);
			editor.putString("2", last_path[2]);
			editor.commit();
		}
		
		//metodo per il nome da visualizzare del path
		public String getPathName(String path){
			int index = path.lastIndexOf("/") + 1;
			String path_name = path.substring(index);
			return path_name;
		}
	}

