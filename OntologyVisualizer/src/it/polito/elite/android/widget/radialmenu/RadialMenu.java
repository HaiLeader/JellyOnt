package it.polito.elite.android.widget.radialmenu;

import java.util.ArrayList;
import java.util.Vector;

import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.graphics.Matrix;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.SubMenu;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;

public class RadialMenu extends View implements Menu{

	protected boolean isClosed = false;

	protected boolean isDrawable = true;
	protected boolean onLongClickEnd = false;


	//nel caso delle toolbar, mi serve controllare se è già stato aggiunto alla vista.
	//in realtà mi serve solo per le toolbar, gli altri menu fissi non ne ho bisogno
	//posso fare magari una sottoclasse ToolbarMenu che ha questa variabile???

	private boolean isInView = false;

	protected boolean isDraggable;

	protected boolean isClosable;


	//centro della view (cioè del display)
	protected int center_x;

	protected int center_y;


	//dimensione angolare del menu
	protected float startArc;
	protected float widthArc;

	//posizione del menù
	protected float offset_x;
	protected float offset_y;

	//dimensioni della vista
	protected int viewWidth;

	protected int viewHeight;

	//memorizzo il numero di item con figli che sono aperti
	protected int n_open_items = 0;

	//salvo l'id della drawable resource
	protected int iconID;
	//salvo l'elemento drawable 
	protected Drawable icon;

	private String idcliccato=null;

	protected Paint p = new Paint();


	//così dal costruttore posso scegliere la grandezza del menu;

	protected  int inner_radius;
	protected  int outer_radius;
	protected  int wedge_thickness;

	//NB: in realtà è la misura della metà
	protected final int ICON_DIMENSION = 30;
	//di default è al centro
	private Rect icon_bound;

	protected final int CLICKTIME = 800;

	protected final int SPACE = 10;

	LayoutParams params;

	private int h=0;
	private int w=0;



	//coordinate click
	protected float click_x_down;
	protected float click_y_down;

	//figli
	protected ArrayList<RadialMenuItem>  items = new ArrayList<RadialMenuItem>();


	//costruttore
	public RadialMenu(Context context, int drawableResource, int offset_x, int offset_y, float startArc, float widthArc,final int inner_radius, final int outer_radius,  final boolean isDraggable, final boolean isClosable){
		//costruttore superclasse view
		super(context);

		//salvo l'icona

		this.iconID = drawableResource;
		this.inner_radius = inner_radius;
		this.outer_radius = outer_radius;
		//wedge_thikness la setto io come differenza dei due raggi
		wedge_thickness = outer_radius - inner_radius;

		//inizializzo le dimensioni della vista
		initSizeView();
		initTextDraw();
		//memorizzo il centro della vista
		center_x = viewWidth/2;
		center_y = viewHeight/2;

		//memorizzo offset
//		this.offset_x=offset_x;
//		this.offset_y = offset_y;

		icon_bound = new Rect(center_x-ICON_DIMENSION,center_y-ICON_DIMENSION,center_x+ICON_DIMENSION,center_y+ICON_DIMENSION);

		//do l'offset che ho nel costruttore
		params = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.leftMargin = offset_x;
		params.topMargin = offset_y;
		//do altezza e larghezza
		params.height = viewHeight;
		params.width = viewWidth;

		this.setLayoutParams(params);

		this.startArc = startArc;
		this.widthArc = widthArc;

		isClosed = false;

		isDrawable = true;

		this.isDraggable = isDraggable;
		this.isClosable = isClosable;

		//definisco lo stile di default del paint
		p.setARGB(255, 57, 146, 181);
		p.setStyle(Style.STROKE);
		p.setAntiAlias(true);
		p.setStrokeWidth(3);

		this.setBackgroundColor(Color.TRANSPARENT);

		//definisco onTouch e onLongClick
		this.setOnTouchListener(myTouchListener);

		this.setOnLongClickListener(myLongClickListener);

//		this.setOnDragListener(myDragListener);


	}

	//definisco i due listener onTouch e onLongClick 
	private OnTouchListener myTouchListener = new OnTouchListener() {
		long start_time;
		public boolean onTouch(View v, MotionEvent event) {				
			//prendo il tempo iniziale, poi calcolo la differenza
			if(event.getAction() == MotionEvent.ACTION_DOWN){
				click_x_down = event.getX();
				click_y_down = event.getY();
				start_time = event.getEventTime();
			}

			if(event.getAction() == MotionEvent.ACTION_UP){
				//controllo il tempo
				if((event.getEventTime()-start_time)<=CLICKTIME){


					//controllo se è all'interno del cerchio
					if(isInCircle(click_x_down, click_y_down)){
						//controllo se è chiudibile o no
						if(isClosable){
							if(!isClosed){
								//chiudo il menu

								//update view dimension
								//se è chiuso, la grandezza è (innerradius + 10)*2

								// memorizzo le vecchie dimensioni 
								h = params.height;
								w = params.width;

								//setto le nuove
								params.height = (inner_radius+SPACE)*2;
								params.width = (inner_radius+SPACE)*2;

								//offset
								params.leftMargin = params.leftMargin + (w-params.width)/2; 
								params.topMargin = params.topMargin + (h-params.height)/2;

								//centro della view
								center_x = params.width/2;
								center_y = params.height/2;
								icon_bound = new Rect(center_x-ICON_DIMENSION,center_y-ICON_DIMENSION,center_x+ICON_DIMENSION,center_y+ICON_DIMENSION);

								//se apro o chiudo il menu, allora id cliccato deve essere settato a null
								setIdcliccato(null);
								closeMenu();

								v.setLayoutParams(params);


								isClosed = true;
								//v.invalidate();
								v.postInvalidate();
								//								v.postInvalidateOnAnimation();
							}
							else {
								//update view dimension

								//prendo le dimensioni vecchie
								w = params.width;
								h = params.height;

								//se è apro, la grandezza è (outerradius+10*2)
								params.height = (outer_radius+SPACE)*2;
								params.width = (outer_radius+SPACE)*2;

								//offset
								params.leftMargin = params.leftMargin - (params.width-w)/2 ;
								params.topMargin = params.topMargin - (params.height-h)/2;
								//centro della view
								center_x = params.width/2;
								center_y = params.height/2;
								icon_bound = new Rect(center_x-ICON_DIMENSION,center_y-ICON_DIMENSION,center_x+ICON_DIMENSION,center_y+ICON_DIMENSION);

								v.setLayoutParams(params);

								//se apro o chiudo il menu, allora id cliccato deve essere settato a null
								setIdcliccato(null);
								openMenu();

								isClosed = false;
								//v.invalidate();
								v.postInvalidate();
								//								v.postInvalidateOnAnimation();
							}
						}
						else{
							setIdcliccato("home");
						}
					}
					else{

						//controllo se è nel wedge
						//controllo quale wedges è stato cliccato
						int wedges_index = -1;
						for(int i = 0; i<items.size(); i++){
							RadialMenuItem tmp = items.get(i);
							//controllo se so preso un wedges
							if(tmp.isInWedge(click_x_down, click_y_down)){
								wedges_index=i;
							}
							//controllo se ho un expandable....

						}

						//controllo a che classe appartiene
						if(wedges_index>-1){
							//se ha drawable = true vuol dire che è disegnato e quindi posso aprire il menu
							if(items.get(wedges_index) instanceof ExpandableRadialMenuItem  && items.get(wedges_index).isDrawable==true){
								//prendo i subitems
								//cast della classe per accedere al metodo
								ExpandableRadialMenuItem e_item = (ExpandableRadialMenuItem) items.get(wedges_index);
								if(e_item.isExpanded==false){
									n_open_items++;
									Log.v("open items", ""+n_open_items);

									//prendo le dimensioni vecchie
									h = params.height;
									w = params.width;

									//aumento dimensioni della vista. deve essere outer radius + thickness + space*2
									params.width = (outer_radius + wedge_thickness + SPACE*2)*2;
									params.height = (outer_radius + wedge_thickness + SPACE*2)*2;

									//diminuisco offset
									params.leftMargin = params.leftMargin - (params.width - w)/2;
									params.topMargin = params.topMargin - (params.height  - h)/2;

									//ricalcolo il centro
									center_y = params.height/2;
									center_x = params.width/2;
									icon_bound = new Rect(center_x-ICON_DIMENSION,center_y-ICON_DIMENSION,center_x+ICON_DIMENSION,center_y+ICON_DIMENSION);

									v.setLayoutParams(params);

									expandMenu(e_item);
									e_item.isExpanded=true;

								}
								else{
									n_open_items--;
									Log.v("open items", ""+n_open_items);
									p.setARGB(255, 57, 146, 181);

									//devo cambiare le dimensioni della view, solo se il numero di item è <1
									if(n_open_items<1){
										//prendo dimensioni vecchie
										h=params.height;
										w=params.width;

										//diminuisco dimensioni della vista. deve essere outer radius  space*2
										params.width = (outer_radius + SPACE*2)*2;
										params.height = (outer_radius+ SPACE*2)*2;

										//diminuisco offset
										params.leftMargin = params.leftMargin + (w-params.width)/2;
										params.topMargin = params.topMargin + (h-params.height)/2;

										v.setLayoutParams(params);

										//ricalcolo il centro
										center_y = params.height/2;
										center_x = params.width/2;
										icon_bound = new Rect(center_x-ICON_DIMENSION,center_y-ICON_DIMENSION,center_x+ICON_DIMENSION,center_y+ICON_DIMENSION);
									}


									contractMenu(e_item);
									e_item.isExpanded=false;
								}

							}
							else{
								//se una section, devo vedere quale ho cliccato
								if(items.get(wedges_index) instanceof SubdividedRadialMenuItem){
									SubdividedRadialMenuItem s_tmp = (SubdividedRadialMenuItem) items.get(wedges_index);
									if(s_tmp.getSubdivision().size()>0){
										int section_index = -1;
										for(int i = 0; i<s_tmp.getSubdivision().size(); i++){
											if(s_tmp.getSubdivision().get(i).isInWedge(click_x_down, click_y_down)){
												section_index = i;
											}
										}
										setIdcliccato(s_tmp.getSubdivision().get(section_index).getID());
										Log.v("hai cliccato", s_tmp.getSubdivision().get(section_index).getID()+"");
									}
								}
								else {
									//è un wedges normale, dovrò fare un'altra azione
									setIdcliccato(items.get(wedges_index).getID());
									Log.v("hai cliccato", items.get(wedges_index).getID()+"");
								}

							}

						}
						else{
							//non ho cliccato nessun wedge index, l'id deve essere messo a null
							setIdcliccato(null);
						}



					}

				}
			}
			//devo propagare l'evento
			return false;
		}
	};

	private OnLongClickListener myLongClickListener = new OnLongClickListener() {
		public boolean onLongClick(View v) {
			//se è draggabile
			if(isDraggable){
				//controllo le coordinate
				if(isInCircle(click_x_down, click_y_down)){
					ClipData data = ClipData.newPlainText("", "");
					DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(v);
					v.startDrag(data, shadowBuilder, v, 0);
					v.setVisibility(View.INVISIBLE);
					v.postInvalidate();

					//					v.postInvalidateOnAnimation();
				}

			}
			return true;
		}
	};

	//devo creare un draglistener per sapere quando sono in drop
	//in drop devo aggiornare i params della view del radial menu
//	private OnDragListener myDragListener = new OnDragListener() {
//
//		public boolean onDrag(View v, DragEvent event) {
//			final int action = event.getAction();
//			switch(action) {
//			case DragEvent.ACTION_DRAG_STARTED:{
//				//niente
//				break;
//			}
//
//			case DragEvent.ACTION_DRAG_ENTERED:{
//				break;
//			}
//			case DragEvent.ACTION_DRAG_EXITED:{
//				break;
//			}
//			case DragEvent.ACTION_DROP:{
//				Log.v("drop", "drop");
//				float x = event.getX();
//				float y = event.getY();
//				break;
//			}
//			case DragEvent.ACTION_DRAG_ENDED:{
//				
//			}
//
//			}
//			return true;
//		}
//	};
	public void initSizeView(){
		viewWidth = DPtoPX((outer_radius+SPACE)*2);
		viewHeight = DPtoPX((outer_radius+SPACE)*2);
	}


	public void addItem(RadialMenuItem item) {
		items.add(item);
		calculateWedges(startArc, widthArc);
	}

	public void addItems(Vector<RadialMenuItem> children) {
		items.addAll(children);
		calculateWedges(startArc, widthArc);
	}

	public void closeMenu() {
		//tutti gli items devono essere settati a non disegnabili
		for(int i = 0; i< items.size(); i++){
			items.get(i).setIsDrawable(false);
			//anche se ho dei figli aperti devo mettere il drawable a false
			if(items.get(i) instanceof ExpandableRadialMenuItem){
				//trasformo in expandable così ho l'accesso ai metodi
				ExpandableRadialMenuItem tmp = (ExpandableRadialMenuItem) items.get(i);
				//se ha figli
				if(tmp.isExpanded==true && tmp.getSubItems().size()>0){
					//metto drawable a false
					for(int j=0; j<tmp.getSubItems().size(); j++){
						tmp.getSubItems().get(j).setIsDrawable(false);
					}
				}
				//metto isEspanded a false
				tmp.isExpanded = false;
			}
			if(items.get(i) instanceof SubdividedRadialMenuItem){
				//cast
				SubdividedRadialMenuItem s_tmp = (SubdividedRadialMenuItem) items.get(i);
				//se ha sezioni
				if(s_tmp.getSubdivision().size()>0){
					for(int k=0; k<s_tmp.getSubdivision().size(); k++){
						s_tmp.getSubdivision().get(k).setIsDrawable(false);
					}
				}
			}
		}
		//ricalcolo di nuovo i wedges così si aggiorna il centro
		calculateWedges(startArc, widthArc);
		//numero di open items di nuovo a zero
		n_open_items = 0;
	}

	public void openMenu() {
		//tutti gli items devono essere risettati a disegnabili
		//ridisegno solo i menu di primo livello, non i figli
		for(int i = 0; i< items.size(); i++){
			items.get(i).setIsDrawable(true);
			if(items.get(i) instanceof SubdividedRadialMenuItem){
				//cast
				SubdividedRadialMenuItem s_tmp = (SubdividedRadialMenuItem) items.get(i);
				//se ha sezioni
				if(s_tmp.getSubdivision().size()>0){
					for(int k=0; k<s_tmp.getSubdivision().size(); k++){
						s_tmp.getSubdivision().get(k).setIsDrawable(true);
					}
				}
			}
		}
		//ricalcolo di nuovo i wedges così si aggiorna il centro
		calculateWedges(startArc, widthArc);
	}

	//ha come ingresso inizio e fine arco, il vettore dei figli 
	//così posso utilizzarlo sia per items che per subitems
	public void calculateWedges(float startArc, float widthArc) {
		//se il menu ha dei figli
		if(items.size()>0){
			//calcolo l'ampiezza di ogni singolo wedges
			float degSlice = (startArc+widthArc)/items.size();
			//per ogni item costruisco il path
			for(int i=0; i<items.size(); i++){
				RadialMenuItem tmp = items.get(i);
				//setto tutti i valori
				tmp.setCenter_x(center_x);
				tmp.setCenter_y(center_y);
				tmp.setStartArc((i*degSlice) + startArc);
				tmp.setWidthArc(degSlice);
				tmp.setInnerRadius(DPtoPX(inner_radius));
				tmp.setOuterRadius(DPtoPX(outer_radius));
				tmp.buildWedge();

			}
			checkSubdivison();
		}
	}

	public void checkSubdivison(){
		for(int i=0; i<items.size(); i++){
			RadialMenuItem tmp = items.get(i);
			//controllo se ha subdivision
			//se l'item appartiene alla classe subdivided
			if(tmp instanceof SubdividedRadialMenuItem){
				//faccio cast
				SubdividedRadialMenuItem s_tmp = (SubdividedRadialMenuItem) tmp;
				//calculateWedges(tmp.startArc, tmp.widthArc);
				//prendo i figli
				ArrayList<RadialMenuItem> children = s_tmp.getSubdivision();
				if(children.size()>0){
					float s_degSlice = (s_tmp.widthArc)/children.size();
					for(int j = 0; j<children.size(); j++){
						RadialMenuItem cs_tmp = children.get(j);
						cs_tmp.setCenter_x(center_x);
						cs_tmp.setCenter_y(center_y);
						cs_tmp.setStartArc((j*s_degSlice) + s_tmp.startArc);
						cs_tmp.setWidthArc(s_degSlice);
						cs_tmp.setInnerRadius(DPtoPX(inner_radius));
						cs_tmp.setOuterRadius(DPtoPX(outer_radius));
						cs_tmp.buildWedge();
					}
				}
			}
		}

	}


	public void expandMenu(ExpandableRadialMenuItem e_item){
		ArrayList<RadialMenuItem> subItems = e_item.getSubItems();
		if(subItems.size()>0){
			//costruisco il wedge dei subitem
			//calcolo la fettina di ogni sub item, tanto è uguale per tutti
			float subItem_slice = e_item.widthArc/subItems.size();

			for(int i=0; i<subItems.size(); i++){
				RadialMenuItem tmp = subItems.get(i);
				tmp.setCenter_x(center_x);
				tmp.setCenter_y(center_y);
				tmp.setInnerRadius(e_item.getOuterRadius() + SPACE);
				tmp.setOuterRadius(e_item.getOuterRadius() + wedge_thickness + SPACE);
				tmp.setStartArc((i*subItem_slice) + e_item.startArc);
				tmp.setWidthArc(subItem_slice);
				tmp.setIsDrawable(true);
				tmp.buildWedge();
			}
			//ricalcolo di nuovo i wedges così si aggiorna il centro
			calculateWedges(startArc, widthArc);
		}
		//		invalidate();
		postInvalidate();
		//		postInvalidateOnAnimation();

	}

	public void contractMenu(ExpandableRadialMenuItem e_item){
		ArrayList<RadialMenuItem> subItems = e_item.getSubItems();
		if(subItems.size()>0){
			for(int i = 0; i <subItems.size(); i++){
				subItems.get(i).setIsDrawable(false);
			}
			//ricalcolo di nuovo i wedges così si aggiorna il centro
			calculateWedges(startArc, widthArc);
		}
		//		invalidate();
		postInvalidate();
		//		postInvalidateOnAnimation();

	}




	public void setIconPositionToCorner(){
		icon_bound = new Rect(center_x-2*ICON_DIMENSION,center_y-2*ICON_DIMENSION,center_x,center_y);
		invalidate();
	}



	@Override
	public void onDraw(Canvas c){
		//disegno il cerchio così quando chiudo il menu non scompare
		//sempre due volte per fare il contorno e l'interno
		p.setStyle(Style.FILL);
		p.setColor(Color.BLACK);
		c.drawCircle(center_x, center_y, inner_radius, p);
		p.setStyle(Style.STROKE);
		p.setARGB(255, 57, 146, 181);
		c.drawCircle(center_x, center_y, inner_radius, p);

		//disegno l'icona al centro del cerchio
		//costruisco l'icona
		if(iconID!=0){
			icon = getResources().getDrawable(iconID);
			icon.setBounds(icon_bound);
			icon.draw(c);
		}

		//richiamo drawWedges
		drawWedges(c, p);
	}

	private int textWidth;
	private Paint mTextPaint;
	//per calcolare la metà della lunghezza del testo
	public void initTextDraw(){
		mTextPaint = new Paint();
		mTextPaint.setAntiAlias(true);
		//uso uno stroke più sottile per il testo
		mTextPaint.setStrokeWidth(2);
		//uso il verde per le scritte
		mTextPaint.setARGB(255, 136, 168, 13);

		mTextPaint.setTextSize(18);
	}


	//
	public void drawWedges(Canvas c, Paint p) {
		Point pnt = new Point();
		for(int i = 0; i < items.size(); i++){
			//deve avere is drawable a true

			//disegno due volte per avere lo sfondo nero!!!

			if(items.get(i).getIsDrawable()){
				p.setStyle(Style.FILL);
				p.setColor(Color.BLACK);

				c.drawPath(items.get(i), p);
				//per ognugno disegno l'icona
				if(items.get(i).getIconID()!=0){
					int n_icon =  items.get(i).getIconID();
					Drawable d_icon = getResources().getDrawable(n_icon);
					pnt = items.get(i).buildIcon();
					Rect r = new Rect(pnt.x-ICON_DIMENSION, pnt.y-ICON_DIMENSION, pnt.x+ICON_DIMENSION, pnt.y+ICON_DIMENSION);
					d_icon.setBounds(r);
					d_icon.draw(c);
				}
				p.setStyle(Style.STROKE);
				p.setARGB(255, 57, 146, 181);			

				c.drawPath(items.get(i), p);
				//per ognugno disegno l'icona
				if(items.get(i).getIconID()!=0){
					int n_icon =  items.get(i).getIconID();
					Drawable d_icon = getResources().getDrawable(n_icon);
					pnt = items.get(i).buildIcon();
					Rect r = new Rect(pnt.x-ICON_DIMENSION, pnt.y-ICON_DIMENSION, pnt.x+ICON_DIMENSION, pnt.y+ICON_DIMENSION);
					d_icon.setBounds(r);
					d_icon.draw(c);
				}
				p.setStyle(Style.FILL);


				if( items.get(i).isTextToCorner()){
					mTextPaint.setTextSize(15);
					pnt = items.get(i).buildText();
					c.drawText(items.get(i).getLabel(), pnt.x, pnt.y, mTextPaint);
				}
				else{
					p.setTextSize(18);
					float off = items.get(i).getOffset();
					//devo togliere metà lunghezza del testo
					float text_width = mTextPaint.measureText(items.get(i).getLabel()) + getPaddingLeft() + getPaddingRight();
					off = off - (text_width/2);

					//mi serve metodo initTextDraw per saperla
					//se l0inizio dell'angolo dell'item è compreso tra o e 180 gradi, devo specchiare il testo
					//NB startarc è in gradi
					//					Matrix flipping = new Matrix();
					//					flipping.preRotate(-45, items.get(i).center_x, items.get(i).center_y);
					if(items.get(i).getStartArc()>=0 && items.get(i).getStartArc()<135){
						////						
						////						items.get(i).transform(flipping);
						//						//dovrei sapere lo spessore dell'arco
						//						//e mettere la distanza di spessore-30 per avere tutti i nomi posizionati nello stesso modo
						////						mTextPaint.setTextSize(13);
						c.drawTextOnPath(items.get(i).getLabel(), items.get(i).getForText(), off, 0, mTextPaint);
						//						
					}
					else{
						//						mTextPaint.setTextSize(18);
						//in questo caso metto altro offset pari all'altezza del testo
						int textHeight = (int) (-mTextPaint.ascent() + mTextPaint.descent() + getPaddingBottom() + getPaddingTop());
						c.drawTextOnPath(items.get(i).getLabel(), items.get(i).getForText(), off,textHeight/2, mTextPaint);
					}


				}

				//				c.drawText(items.get(i).getLabel(), pnt.x, pnt.y, p);
				//ritorno a quello spesso per i wedges
				p.setStrokeWidth(3);
				//ritorno al blu
				p.setARGB(255, 57, 146, 181);
				//ritorno a solo stroke
				p.setStyle(Style.STROKE);

			}
			//controllo se ha figli e se sono disegnabili
			if(items.get(i) instanceof ExpandableRadialMenuItem){
				ExpandableRadialMenuItem tmp = (ExpandableRadialMenuItem) items.get(i);
				if(tmp.isExpanded==true && tmp.getSubItems().size()>0){
					//ridisegno il path cliccato con un colore diverso
					tmp.reset();

					p.setARGB(255, 51, 204, 204);
					tmp.buildWedge();
					c.drawPath(tmp,p);
					for(int j = 0; j <tmp.getSubItems().size(); j++){


						RadialMenuItem subitem = tmp.getSubItems().get(j);
						//disegno il controno
						p.setStyle(Style.STROKE);
						p.setARGB(255, 51, 204, 204);
						c.drawPath(subitem, p);
						//disegno l'interno
						p.setStyle(Style.FILL);
						p.setColor(Color.BLACK);
						c.drawPath(subitem, p);
						//per ognugno disegno l'icona
						if(subitem.getIconID()!=0){
							int n_icon =  subitem.getIconID();
							Drawable d_icon = getResources().getDrawable(n_icon);
							pnt = subitem.buildIcon();
							Rect r = new Rect(pnt.x-ICON_DIMENSION, pnt.y-ICON_DIMENSION, pnt.x+ICON_DIMENSION, pnt.y+ICON_DIMENSION);
							d_icon.setBounds(r);
							d_icon.draw(c);
						}
						//disegno il testo
						//ritorno al colore verde
						//uso uno stroke più sottile per il testo
						p.setStrokeWidth(1);
						p.setARGB(255, 51, 204, 204);
						pnt=tmp.getSubItems().get(j).buildText();
						//c.drawTextOnPath(tmp.getSubItems().get(j).getLabel(), tmp.getSubItems().get(j), pnt.x, 20, p);
						c.drawText(tmp.getSubItems().get(j).getLabel(), pnt.x, pnt.y, mTextPaint);
						//ritorno a quello spesso per i wedges
						p.setStrokeWidth(3);
						//ritorno al blu
						p.setARGB(255, 57, 146, 181);
					}
				}
			}


		}

		drawSubdivision(c, p);
		invalidate();
	}

	public void drawSubdivision(Canvas c, Paint p){
		Point pnt = new Point();
		for(int i=0; i<items.size(); i++){
			if(items.get(i) instanceof SubdividedRadialMenuItem){
				SubdividedRadialMenuItem s_tmp = (SubdividedRadialMenuItem) items.get(i);
				for(int k = 0; k <s_tmp.getSubdivision().size(); k++){
					RadialMenuItem s_subitem = s_tmp.getSubdivision().get(k);
					if(s_subitem.isDrawable == true){
						c.drawPath(s_subitem, p);	
						//per ognugno disegno l'icona
						if(s_subitem.getIconID()!=0){
							int n_icon =  s_subitem.getIconID();
							Drawable d_icon = getResources().getDrawable(n_icon);
							pnt = s_subitem.buildIcon();
							Rect r = new Rect(pnt.x-ICON_DIMENSION, pnt.y-ICON_DIMENSION, pnt.x+ICON_DIMENSION, pnt.y+ICON_DIMENSION);
							d_icon.setBounds(r);
							d_icon.draw(c);
						}
						//disegno il testo
						//ritorno al colore di prima
						p.setARGB(255, 57, 146, 181);
						//uso uno stroke più sottile per il testo
						p.setStrokeWidth(1);
						mTextPaint.setARGB(255, 57, 146, 181);
						mTextPaint.setStrokeWidth(1);
						mTextPaint.setTextSize(15);
						pnt=s_subitem.buildText();
						c.drawText(s_tmp.getSubdivision().get(k).getLabel(), pnt.x, pnt.y, mTextPaint);
						//ritorno a quello spesso per i wedges
						p.setStrokeWidth(3);
					}

				}
			}
		}

	}

	//controllo se il click è all'interno del raggio interno
	public boolean isInCircle(float click_x, float click_y) {
		//controllo il modulo se è all'interno dei due raggi
		float dist_x = click_x - center_x;
		float dist_y = click_y - center_y;

		float dist_mod = (dist_x*dist_x) + (dist_y*dist_y); 

		float radius_mod = inner_radius*inner_radius;

		if(dist_mod<radius_mod){
			return true;
		}
		else{
			return false;
		}
	}



	public float getOffset_x() {
		return params.leftMargin;
	}


	public float getOffset_y() {
		return params.topMargin;
	}


	public void setOffset_x(float offset_x) {
		this.offset_x = offset_x;
		params.leftMargin = (int) offset_x;
		setLayoutParams(params);
		invalidate();
	}


	public void setOffset_y(float offset_y) {
		this.offset_y = offset_y;
		params.topMargin = (int) offset_y;
		setLayoutParams(params);
		invalidate();
	}


	public int getInner_radius() {
		return inner_radius;
	}

	public void setInner_radius(int inner_radius) {
		this.inner_radius = inner_radius;
	}

	public int getOuter_radius() {
		return outer_radius;
	}

	public void setOuter_radius(int outer_radius) {
		this.outer_radius = outer_radius;
	}

	public Boolean getIsClosed() {
		return isClosed;
	}

	public void setIsClosed(Boolean isClosed) {
		this.isClosed = isClosed;
	}

	public Boolean getIsDrawable() {
		return isDrawable;
	}

	public void setIsDrawable(Boolean isDrawable) {
		this.isDrawable = isDrawable;
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



	public int getViewWidth() {
		return viewWidth;
	}

	public void setViewWidth(int viewWidth) {
		this.viewWidth = viewWidth;
	}

	public int getViewHeight() {
		return viewHeight;
	}

	public void setViewHeight(int viewHeight) {
		this.viewHeight = viewHeight;
	}

	public int getIconID() {
		return iconID;
	}

	public void setIconID(int drawableResource) {
		this.iconID = drawableResource;
	}

	public Paint getP() {
		return p;
	}

	public void setP(Paint p) {
		this.p = p;
	}

	public int getINNER_RADIUS() {
		return inner_radius;
	}

	public int getOUTER_RADIUS() {
		return outer_radius;
	}

	public int getCLICKTIME() {
		return CLICKTIME;
	}

	public int getSPACE() {
		return SPACE;
	}

	public int getWEDGE_THICKNESS() {
		return wedge_thickness;
	}

	public void setItems(ArrayList<RadialMenuItem> items) {
		this.items = items;
	}

	//ritorno i figli diretti di questo menù (non i figli degli item espandibili)
	public ArrayList<RadialMenuItem> getItems() {
		return items;
	}


	//conversion from dp to px
	public int DPtoPX(int dp){
		float screen_density = getContext().getResources().getDisplayMetrics().density;
		int px = (int) ((int) dp*screen_density+0.5f);
		return px;
	}
















	public MenuItem add(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem add(int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem add(int groupId, int itemId, int order, CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem add(int groupId, int itemId, int order, int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	public int addIntentOptions(int groupId, int itemId, int order,
			ComponentName caller, Intent[] specifics, Intent intent, int flags,
			MenuItem[] outSpecificItems) {
		// TODO Auto-generated method stub
		return 0;
	}

	public SubMenu addSubMenu(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public SubMenu addSubMenu(int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	public SubMenu addSubMenu(int groupId, int itemId, int order, CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public SubMenu addSubMenu(int groupId, int itemId, int order, int titleRes) {
		// TODO Auto-generated method stub
		return null;
	}

	public void clear() {
		// TODO Auto-generated method stub

	}

	public void close() {
		// TODO Auto-generated method stub

	}

	public MenuItem findItem(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem getItem(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasVisibleItems() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isShortcutKey(int keyCode, KeyEvent event) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean performIdentifierAction(int id, int flags) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean performShortcut(int keyCode, KeyEvent event, int flags) {
		// TODO Auto-generated method stub
		return false;
	}

	public void removeGroup(int groupId) {
		// TODO Auto-generated method stub

	}

	public void removeItem(int id) {
		// TODO Auto-generated method stub

	}

	public void setGroupCheckable(int group, boolean checkable, boolean exclusive) {
		// TODO Auto-generated method stub

	}

	public void setGroupEnabled(int group, boolean enabled) {
		// TODO Auto-generated method stub

	}

	public void setGroupVisible(int group, boolean visible) {
		// TODO Auto-generated method stub

	}

	public void setQwertyMode(boolean isQwerty) {
		// TODO Auto-generated method stub

	}

	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}


	public String getIdcliccato() {
		return idcliccato;
	}


	public void setIdcliccato(String idcliccato) {
		this.idcliccato = idcliccato;
	}


	public boolean isInView() {
		return isInView;
	}


	public void setInView(boolean isInView) {
		this.isInView = isInView;
	}



}