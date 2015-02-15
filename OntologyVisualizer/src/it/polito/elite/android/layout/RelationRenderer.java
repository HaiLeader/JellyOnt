package it.polito.elite.android.layout;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.view.View;

public class RelationRenderer extends View{

	//Attributi per disegnare linea
	private float startX, startY, stopX, stopY;
	//la classe deve disegnare una linea
	
	public RelationRenderer(Context context) {
		super(context);
	}
	
	public void setStartX(float startX) {
		this.startX = startX;
	}

	public void setStartY(float startY) {
		this.startY = startY;
	}

	public void setStopX(float stopX) {
		this.stopX = stopX;
	}

	public void setStopY(float stopY) {
		this.stopY = stopY;
	}

	@Override
	public void onDraw(Canvas c){
		Paint p = new Paint();
		p.setAntiAlias(true);
		p.setStyle(Style.STROKE);
		p.setStrokeWidth(3);
		p.setARGB(255, 136, 168, 13);
		c.drawLine(startX, startY, stopX, stopY, p);
	}

}
