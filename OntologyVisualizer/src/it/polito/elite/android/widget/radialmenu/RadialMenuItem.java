package it.polito.elite.android.widget.radialmenu;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PathMeasure;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Paint.Style;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

public class RadialMenuItem extends Path implements MenuItem {

	protected boolean isDrawable = true;
	
	protected String label;
	
	protected String ID;

	protected int iconID=0;

	protected int center_x;

	protected int center_y;

	protected int innerRadius;

	protected int outerRadius;

	public float startArc;

	public float widthArc;

	public RadialMenu big_parent;

	public ExpandableRadialMenuItem parent;
	
	protected boolean textToCorner = false;
	
	//offset per centrare il testo che segue il path
	private float offset = 0;
	
	//spessore dell'arco per posizionare il testo a metà spessore
	private float arc_thickness = 0;
	
	//path da fa seguire al testo
	Path forText = new Path();

	//costruttore
	public RadialMenuItem(String label, String ID, int drawableResource){
		this.label = label;
		this.iconID = drawableResource;
		this.ID = ID;
	}

	public RadialMenuItem(int center_x, int center_y, float startArc, float widthArc, int innerRadius, int outerRadius){
		this.center_x = center_x;
		this.center_y = center_y;
		this.startArc = startArc;
		this.widthArc = widthArc;
		this.innerRadius = innerRadius;
		this.outerRadius = outerRadius;
		this.isDrawable = true;
		setFillType(Path.FillType.WINDING);
		buildWedge();
	}

	public void buildWedge() {
		//primo rettangolo (più piccolo)
		final RectF rect = new RectF(center_x-innerRadius,center_y-innerRadius, center_x+innerRadius, center_y+innerRadius);
		//secondo rettangolo (più grande, quello esterno)
		final RectF rect2 = new RectF(center_x-outerRadius, center_y-outerRadius, center_x+outerRadius, center_y+outerRadius);
		//setto il valore dello spessore dell'arco
		arc_thickness = (outerRadius - innerRadius)/2;
		final RectF rect_for_text = new RectF(center_x-(innerRadius+arc_thickness), center_y-(innerRadius+arc_thickness), center_x+(innerRadius+arc_thickness), center_y+(innerRadius+arc_thickness));
		forText.reset();
		//cancello
		this.reset();
		if(startArc>=0 && startArc<135){
			//disegno l'arco al contrario
			//creo il path che deve seguire il testo a metà tra i due path appena creati
			forText.addArc(rect_for_text, startArc+widthArc, -widthArc);
			//disegno l'arco interno, tornando indietro, dalla fine dell'arco (start+width) a -width
			PathMeasure pm = new PathMeasure(forText, false);
			offset = pm.getLength()/2;
		}
		else{
			//disegno l'arco esterno
			//creo il path che deve seguire il testo a metà tra i due path appena creati
			forText.addArc(rect_for_text, startArc, widthArc);
			PathMeasure pm = new PathMeasure(forText, false);
			offset = pm.getLength()/2;
		}
		this.arcTo(rect2, startArc, widthArc);
		//disegno l'arco interno, tornando indietro, dalla fine dell'arco (start+width) a -width
		this.arcTo(rect, startArc+widthArc, -widthArc);
		this.close();
	}


	public Path getForText() {
		return forText;
	}

	public void setForText(Path forText) {
		this.forText = forText;
	}

	public float getOffset() {
		return offset;
	}

	public void setOffset(float offset) {
		this.offset = offset;
	}

	public float getArc_thickness() {
		return arc_thickness;
	}

	public void setArc_thickness(float arc_thickness) {
		this.arc_thickness = arc_thickness;
	}

	public boolean isInWedge(float click_x, float click_y) {
		//prendo distanza del click dal centro sull'asse delle x e sull'asse delle y
		float dist_x = click_x - center_x;
		float dist_y = click_y - center_y;
		//calcolo l'angolo
		double angle = Math.atan2(dist_y, dist_x);
		//trasformo da radianti a gradi
		angle = angle*(180)/Math.PI;
		if(angle<0){
			angle = angle + 360;
		}
		//controllo se l'angolo è compreso
		if(angle>=startArc && angle<=(startArc+widthArc)){
			//controllo il modulo
			float dist = (dist_x*dist_x) + (dist_y*dist_y);
			if(dist>=innerRadius*innerRadius && dist<= outerRadius*outerRadius){
				return true;
			}
			else{
				return false;
			}
		}
		else{
			return false;
		}
		
	}
	
	
	private Point p_text = new Point();
	
	public Point buildText() { 
		//media dei raggi
		float radius = (innerRadius + outerRadius)/2;
		//media degli angoli
		double angle = (startArc+(startArc+widthArc))/2;
		//ricavo x e y della posizione del testo (dovrò poi calcolare l'offset da aggiungere al testo)
		
		//conversion from degrees to radians
		angle = angle *(Math.PI/180);
		p_text.x = (int) (radius*Math.cos(angle));
		p_text.x = p_text.x + center_x;
		p_text.y = (int) (radius*Math.sin(angle));
		p_text.y = p_text.y + center_y ;
		//se il testo deve essere al lato
		if(textToCorner){
			p_text.x = p_text.x - 30;
			p_text.y = p_text.y - 30;
		}
		return p_text;
	}
	
	public Point buildIcon() {
		Point p = new Point();
		//media dei raggi
		float radius = (innerRadius + outerRadius)/2;
		//media degli angoli
		double angle = (startArc+(startArc+widthArc))/2;
		//ricavo x e y della posizione del testo (dovrò poi calcolare l'offset da aggiungere al testo)
		
		//conversion from degrees to radians
		angle = angle *(Math.PI/180);
		p.x = (int) (radius*Math.cos(angle));
		p.x = p.x + center_x ;
		p.y = (int) (radius*Math.sin(angle));
		p.y = p.y + center_y ;
		return p;
	}
	
	public boolean isTextToCorner() {
		return textToCorner;
	}

	public void setTextToCorner(boolean textToCorner) {
		this.textToCorner = textToCorner;
	}

	public boolean collapseActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean expandActionView() {
		// TODO Auto-generated method stub
		return false;
	}

	

	public String getID() {
		return ID;
	}

	public Boolean getIsDrawable() {
		return isDrawable;
	}

	public void setIsDrawable(Boolean isDrawable) {
		this.isDrawable = isDrawable;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getIconID() {
		return iconID;
	}

	public void setIconID(Integer iconID) {
		this.iconID = iconID;
	}

	public Integer getCenter_x() {
		return center_x;
	}

	public void setCenter_x(Integer center_x) {
		this.center_x = center_x;
	}

	public Integer getCenter_y() {
		return center_y;
	}

	public void setCenter_y(Integer center_y) {
		this.center_y = center_y;
	}

	public Integer getInnerRadius() {
		return innerRadius;
	}

	public void setInnerRadius(Integer innerRadius) {
		this.innerRadius = innerRadius;
	}

	public Integer getOuterRadius() {
		return outerRadius;
	}

	public void setOuterRadius(Integer outerRadius) {
		this.outerRadius = outerRadius;
	}

	public float getStartArc() {
		return startArc;
	}

	public void setStartArc(float startArc) {
		this.startArc = startArc;
	}

	public float getWidthArc() {
		return widthArc;
	}

	public void setWidthArc(float widthArc) {
		this.widthArc = widthArc;
	}

	public RadialMenu getBig_parent() {
		return big_parent;
	}

	public void setBig_parent(RadialMenu big_parent) {
		this.big_parent = big_parent;
	}

	public ExpandableRadialMenuItem getParent() {
		return parent;
	}

	public void setParent(ExpandableRadialMenuItem parent) {
		this.parent = parent;
	}
	
	public ActionProvider getActionProvider() {
		// TODO Auto-generated method stub
		return null;
	}

	public View getActionView() {
		// TODO Auto-generated method stub
		return null;
	}

	public char getAlphabeticShortcut() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getGroupId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public Drawable getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public Intent getIntent() {
		// TODO Auto-generated method stub
		return null;
	}

	public int getItemId() {
		// TODO Auto-generated method stub
		return 0;
	}

	public ContextMenuInfo getMenuInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	public char getNumericShortcut() {
		// TODO Auto-generated method stub
		return 0;
	}

	public int getOrder() {
		// TODO Auto-generated method stub
		return 0;
	}

	public SubMenu getSubMenu() {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence getTitle() {
		// TODO Auto-generated method stub
		return null;
	}

	public CharSequence getTitleCondensed() {
		// TODO Auto-generated method stub
		return null;
	}

	public boolean hasSubMenu() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isActionViewExpanded() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isCheckable() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isChecked() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isEnabled() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isVisible() {
		// TODO Auto-generated method stub
		return false;
	}

	public MenuItem setActionProvider(ActionProvider actionProvider) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setActionView(View view) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setActionView(int resId) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setAlphabeticShortcut(char alphaChar) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setCheckable(boolean checkable) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setChecked(boolean checked) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setEnabled(boolean enabled) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setIcon(Drawable icon) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setIcon(int iconRes) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setIntent(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setNumericShortcut(char numericChar) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setOnActionExpandListener(OnActionExpandListener listener) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setOnMenuItemClickListener(
			OnMenuItemClickListener menuItemClickListener) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setShortcut(char numericChar, char alphaChar) {
		// TODO Auto-generated method stub
		return null;
	}

	public void setShowAsAction(int actionEnum) {
		// TODO Auto-generated method stub

	}

	public MenuItem setShowAsActionFlags(int actionEnum) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setTitle(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setTitle(int title) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setTitleCondensed(CharSequence title) {
		// TODO Auto-generated method stub
		return null;
	}

	public MenuItem setVisible(boolean visible) {
		// TODO Auto-generated method stub
		return null;
	}

}