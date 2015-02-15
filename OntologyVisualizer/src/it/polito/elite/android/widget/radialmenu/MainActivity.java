package it.polito.elite.android.widget.radialmenu;

import android.os.Bundle;
import android.app.Activity;
import android.graphics.Color;
import android.util.Log;
import android.view.DragEvent;
import android.view.Menu;
import android.view.View;
import android.widget.RelativeLayout;

public class MainActivity extends Activity {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		RelativeLayout container = (RelativeLayout)findViewById(R.id.container);
		container.setBackgroundColor(Color.BLACK);
		//aggiungo il drag listener
		container.setOnDragListener(new View.OnDragListener() {

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
					//devo aggiornare offset!!!
					if(menu instanceof RadialMenu){
						RadialMenu m = (RadialMenu) menu;
						float off_x = m.getOffset_x();
						float transl_x = menu.getTranslationX();
//						//l'operazione che devo fare dipende se è più grande la nuova posizione o quella vecchia
						
						off_x = off_x + transl_x;
						
						
						m.setOffset_x(off_x);
//						m.invalidate();
					}
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
		});
		
		//creo due menu, uno senza expandable e uno con
		RadialMenu menu1 = new RadialMenu(getApplicationContext(), R.drawable.ontology_search, 0, 0, 0, 360, 100,200,true, true);
		
		
		
		RadialMenuItem item1 = new RadialMenuItem("item1", "item1",0);
		RadialMenuItem item2 = new RadialMenuItem("item2","item2",0);
		RadialMenuItem item3 = new RadialMenuItem("item3","item3", 0);
		RadialMenuItem item4 = new RadialMenuItem("item4","item4",0);
		SubdividedRadialMenuItem s_item = new SubdividedRadialMenuItem("","", 0);
		//aggiungo i figli ad s_item
		RadialMenuItem figlio1 = new RadialMenuItem("figlio1","figlio1", 0);
		RadialMenuItem figlio2 = new RadialMenuItem("figlio2", "figlio2",0);
		s_item.addSubdivision(figlio1);
		s_item.addSubdivision(figlio2);
		
		menu1.addItem(item1);
		menu1.addItem(item2);
		menu1.addItem(item3);
		menu1.addItem(item4);
		menu1.addItem(s_item);
		
		container.addView(menu1);
		
		RadialMenu menu2 = new RadialMenu(getApplicationContext(), R.drawable.ontology_browse, 400, 200, 0, 360, 50,100,true, true);
		ExpandableRadialMenuItem e_item1 = new ExpandableRadialMenuItem("e_item1","e_item1",0);
		ExpandableRadialMenuItem e_item2 = new ExpandableRadialMenuItem("e_item2","e_item2",0);
		
		RadialMenuItem subItem1 = new RadialMenuItem("subItem1","subItem1",0);
		RadialMenuItem subItem2 = new RadialMenuItem("subItem2","subItem2",0);
		RadialMenuItem subItem3 = new RadialMenuItem("subItem3","subItem3",R.drawable.create);
		RadialMenuItem subItem4 = new RadialMenuItem("subItem4","subItem4",R.drawable.edit);
		RadialMenuItem subItem5 = new RadialMenuItem("subItem5","subItem5",R.drawable.delete);
		
		e_item1.addSubItem(subItem1);
		e_item1.addSubItem(subItem2);
		e_item2.addSubItem(subItem3);
		e_item2.addSubItem(subItem4);
		e_item2.addSubItem(subItem5);
		
		menu2.addItem(e_item1);
		menu2.addItem(e_item2);
		
		container.addView(menu2);
		
//		//aggiungo un terzo menu non draggabile
//		RadialMenu menu3 = new RadialMenu(getApplicationContext(), 0, -200, -200, 0, 90,100,200, false, false);
//		RadialMenuItem not_draggable_item = new RadialMenuItem("no drag", 0);
//		RadialMenuItem no_draggable_item2 = new RadialMenuItem("no drag 2", R.drawable.ic_launcher);
//		menu3.addItem(not_draggable_item);
//		menu3.addItem(no_draggable_item2);
//		container.addView(menu3);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}
}
