package it.polito.elite.android.layout;

import com.hp.hpl.jena.rdf.arp.states.HasSubjectFrameI;

import it.polito.elite.android.ontology.ViewOntologyActivity;
import it.polito.elite.android.widget.radialmenu.R;
import it.polito.elite.android.widget.radialmenu.R.drawable;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;

public class NodeRenderer extends View {
	
	//ha come attributo un drawable, cioè l'icona da disegnare
	private Drawable icon;	
	//deve avere String che è il nome della classe, quello che disegno
	private String node_name;
	//nome completo della risorsa
	private String name_complete;

	

	
	//paint per il disegno del testo, così posso prendere le misure
	private Paint mTextPaint;
	private int icon_dimension;
	
	//variabile booleana per controllare se il nodo è stato selezionato/deselezionato per essere aggiunto alla vista personalizzata
	//did efault è false
	private boolean selected;
	
	private boolean drawable = true;
	
	//memorizzo se ha sottoclassi
	private boolean hasSubclass;


	private int center_x, center_y;

	
	public NodeRenderer(Context context, String node_name, boolean hasSubclass, String name_complete) {
		super(context);
		setBackgroundColor(Color.WHITE);
		this.node_name = node_name;
		this.name_complete = name_complete;
		this.hasSubclass = hasSubclass;	

		selected=false;
		initTextDraw();
	}

	
	public String getName_complete() {
		return name_complete;
	}

	public void setName_complete(String name_complete) {
		this.name_complete = name_complete;
	}

	public final void initTextDraw(){
		mTextPaint = new Paint();
        mTextPaint.setAntiAlias(true);
        mTextPaint.setTextSize(25);
        mTextPaint.setStyle(Style.STROKE);
        mTextPaint.setColor(Color.WHITE);
	}
	
	
	@Override
	protected void onDraw(Canvas c){
		super.onDraw(c);
		Paint p = new Paint();
		p.setAntiAlias(true);
		
		if(hasSubclass){
			p.setStrokeWidth(4);
			p.setColor(Color.BLACK);
			p.setStyle(Style.FILL);
			c.drawCircle(center_x, center_y, icon_dimension, p);
			p.setARGB(255, 57, 146, 181);
			p.setStyle(Style.STROKE);
			c.drawCircle(center_x, center_y, icon_dimension, p);
			p.setStyle(Style.FILL);
			c.drawCircle(center_x, center_y, icon_dimension/2, p);
		}
		else{
			p.setStrokeWidth(4);
			p.setColor(Color.BLACK);
			p.setStyle(Style.FILL);
			c.drawCircle(center_x, center_y, icon_dimension, p);
			p.setARGB(255, 57, 146, 181);
			p.setStyle(Style.STROKE);
			c.drawCircle(center_x, center_y, icon_dimension, p);	
		}
		Rect r = new Rect(center_x-icon_dimension,center_y-icon_dimension,center_x+icon_dimension,center_y+icon_dimension);
		//misuro il testo così è centrato rispetto al noto
		float midTextLenght = (mTextPaint.measureText(node_name) + getPaddingLeft() + getPaddingRight())/2 ;
		c.drawText(node_name, center_x - midTextLenght , center_y + icon_dimension + 20, mTextPaint);
		
		if(selected){
			p.setStrokeWidth(3);
			p.setStyle(Style.STROKE);
			p.setAntiAlias(true);
			p.setARGB(255, 136, 168, 13);
			//devo includere anche il testo
			//altezza del testo
			int textHeight = (int) ((-mTextPaint.ascent()) + mTextPaint.descent());
//			int textWidth = (int) mTextPaint.measureText(node_name) + getPaddingLeft() + getPaddingRight();
			//devo vedere chi è maggiore tra textWidth/2 e icondimension
			int w = icon_dimension;
			if(midTextLenght>icon_dimension){
				w = (int) midTextLenght;
			}
		
			Rect r_selected = new Rect(center_x-w,center_y-icon_dimension,center_x+w,center_y+icon_dimension+textHeight);
			c.drawRect(r_selected, p);
		}
	}

	public boolean isDrawable() {
		return drawable;
	}

	public void setDrawable(boolean drawable) {
		this.drawable = drawable;
	}

	public int getCenter_x() {
		return center_x;
	}

	public void setCenter_x(int center_x) {
		this.center_x = center_x;
	}

	public int getCenter_y() {
		return center_y;
	}

	public void setCenter_y(int center_y) {
		this.center_y = center_y;
	}
	public void setIconDimension(int icon_dimension) {
		this.icon_dimension = icon_dimension;
	}
	public String getNode_name() {
		return node_name;
	}
	public void setNode_name(String node_name) {
		this.node_name = node_name;
	}
	
	public boolean isSelected(){
		return selected;
	}
	
	public void isSelected(boolean b){
		selected = b;
	}
	
	
	public boolean isInNode(float click_x, float click_y,Matrix m){
		Log.v("sto controllando il nodo", node_name);
		//devo applicare la matrice di trasformazione anche alla x,y del punto che rappresenta il centro. 
		//il centro deve essere traslato, quindi prendo i valori della matrice
		
		float[] matrix_values = new float[9];
		m.getValues(matrix_values);
		float[] center = {center_x, center_y};
		//applico la matrice al center
		m.mapPoints(center);
		//la posizione 0 o 4 (perchè ho lo stesso fattore di scala sia su x che su y) è il fattore di scala
//		float dim = icon_dimension*matrix_values[0];
		float dim = m.mapRadius(icon_dimension);
		
		//calcolo la distanza dalla x e dalla y
		float dist_x = click_x - center[0];
		float dist_y = click_y - center[1];
		//distanza
		float dist = (dist_x*dist_x)+(dist_y*dist_y);
//		(click_x>center[0]-dim && click_x<center[0]+dim) && (click_y>center[1]-dim && click_y<center_y+dim)
		
		if(dist<=dim*dim){
			
			return true;
		}
		else{
			return false;
		}
	}
	
	


}
