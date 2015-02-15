package it.polito.elite.android.ontology;

import it.polito.elite.android.layout.LayoutRenderer;
import it.polito.elite.android.layout.NodeRenderer;
import it.polito.elite.android.widget.radialmenu.R;

import java.util.ArrayList;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ViewO extends RelativeLayout {

	/**
	 * memorizzo la vista delle ontologia,
	 * --> l'ontologia a cui appartiene (anche solo una URI)
	 * --> label della view
	 * --> arraylist di node renderer
	 */
	private String ontoURI;
	private String label;
	private Context context;
	private TextView tv;
	private ImageView delete;
	
	//mi basta un arraylist di string, dato che posso memorizzare solo i nomi dei nodi in modo permanente
	private ArrayList<String> view_nodes = new ArrayList<String>();
	LayoutRenderer renderer;
	//indice, per poterlo poi eliminare
	private int index;
	
	//quando clicco finish, credo l'oggetto view, quindi nel costruttore ho solo l'arraylist
	public ViewO(Context context, String ontoURI, final ArrayList<String> view_nodes, final LayoutRenderer renderer, int index) {
		super(context);
		this.context = context;
		this.ontoURI = ontoURI;
		this.view_nodes = view_nodes;
		this.renderer = renderer;
		this.index = index;
		LayoutInflater.from(context).inflate(R.layout.viewo_view, this, true);
        
        //devo mettere l'onclick listener sia sulla textview che sull'icona
        tv = (TextView) this.findViewById(R.id.textView1);
        tv.setText(label);
        
        tv.setOnClickListener(tv_click_listener);
        //se index è -1, allora è l'original view...
        //quindi elimino l'icona cestino
        if(index==-1){
        	ImageView delete =  (ImageView) this.findViewById(R.id.delete);
        	delete.setVisibility(INVISIBLE);
//        	this.removeView(delete);
//        	this.invalidate();
        }
	}
	
	public int getIndex() {
		return index;
	}
	
	public void setIndex(int index) {
		this.index = index;
	}

	private OnClickListener tv_click_listener = new OnClickListener() {
		
		public void onClick(View v) {
			Log.v("hai cliccato", label);
			
			renderer.updateViewO(view_nodes);
			
		}
	};

	
	//quando clicco add, setto la label
	public void setLabel(String label){
		this.label = label;
		tv.setText(label);
		invalidate();
	}
	
	public String getLabel() {
		return label;
	}

	public ArrayList<String> getView_nodes() {
		return view_nodes;
	}

	public void setView_nodes(ArrayList<String> view_nodes) {
		this.view_nodes = view_nodes;
	}


	
	

	
}
