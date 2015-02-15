package it.polito.elite.android.ontology;

import it.polito.elite.android.layout.LayoutRenderer;
import it.polito.elite.android.widget.radialmenu.R;
import android.R.color;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class Bookmark extends RelativeLayout {
	
	/**
	 * memorizzo i bookmark delle ontologia,
	 * ogni bookmarke deve memorizzare
	 * --> l'ontologia a cui appartiene (anche solo una URI)
	 * --> posizione x e y
	 * --> scale factor
	 * -->pivot point
	 * --> label del bookmark
	 * --> matrix
	 */

	private String ontoURI;
	private float x, y;
	private float scaleFactor;
	private PointF pivot;
	private String label;
	private Context context;
	private int index;
	private LayoutRenderer renderer;
	private Matrix MB = new Matrix();
	/*
	 * memorizzo la matrice che identifica le trasformazioni fatte sulla vista nel momento in cui salvo il bookmark
	 * */

	public Bookmark(Context context, String ontoURI, String label, int index, final LayoutRenderer renderer, final Matrix MB) {
		super(context);
		this.context = context;
		this.ontoURI = ontoURI;
		this.label = label;
		this.index = index;
		this.renderer = renderer;
		this.MB = MB;
        LayoutInflater.from(context).inflate(R.layout.bookmark_view, this, true);
        
        //devo mettere l'onclick listener sia sulla textview che sull'icona
        final TextView tv = (TextView) this.findViewById(R.id.textView1);
        tv.setText(label);
        
        setOnClickListener(new OnClickListener() {
			
			public void onClick(View v) {
				renderer.updateMatrix(MB);
			}
		});

	}

	public String getOntoURI() {
		return ontoURI;
	}


	public float getX() {
		return x;
	}


	public float getY() {
		return y;
	}


	public float getScaleFactor() {
		return scaleFactor;
	}


	public PointF getPivot() {
		return pivot;
	}


	//dopo che ho creato l'oggetto bookmar, gli setto la label
	public String getLabel() {
		return label;
	}


	public void setLabel(String label) {
		this.label = label;
	}

	public int getIndex(){
		return index;
	}
	
	public void setIndex(int index){
		this.index=index;
	}

	public Matrix getMB() {
		return MB;
	}



}
