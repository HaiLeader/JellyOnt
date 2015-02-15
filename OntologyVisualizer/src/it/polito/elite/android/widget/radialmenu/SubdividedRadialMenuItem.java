package it.polito.elite.android.widget.radialmenu;

import java.util.ArrayList;

public class SubdividedRadialMenuItem extends RadialMenuItem {
	
	//attributo in più rispetto alla superclasse
	private ArrayList<RadialMenuItem> subdivision = new ArrayList<RadialMenuItem>();
	
	public SubdividedRadialMenuItem(String label, String ID, int drawableResource){
		super(label,ID,drawableResource);
	}

	public SubdividedRadialMenuItem(int center_x, int center_y, float startArc,float widthArc, int innerRadius, int outerRadius) {
		super(center_x, center_y, startArc, widthArc, innerRadius, outerRadius);

	}
	

	
	//aggiungere una suddivisione del menu
	public void addSubdivision(RadialMenuItem sub){
		subdivision.add(sub);
	}
	
	public void addSubdivisions(ArrayList<RadialMenuItem> subs){
		subdivision.addAll(subs);
	}
	public ArrayList<RadialMenuItem> getSubdivision() {
		return subdivision;
	}

	
	
	

}
