package it.polito.elite.android.widget.radialmenu;
import java.util.ArrayList;
import java.util.Vector;

public class ExpandableRadialMenuItem extends RadialMenuItem {

	public boolean isExpanded = false;

	public ArrayList<RadialMenuItem>  subItems = new ArrayList<RadialMenuItem>();

	//costruttore
	public ExpandableRadialMenuItem(String label, String ID, int drawableResource){
		super(label,ID, drawableResource);
	}
	
	public ExpandableRadialMenuItem(int center_x, int center_y, float startArc,float widthArc, int innerRadius, int outerRadius) {
		super(center_x, center_y, startArc, widthArc, innerRadius, outerRadius);
		this.isExpanded = false;
	}

	public void addSubItem(RadialMenuItem subItem) {
		subItems.add(subItem);
	}

	public void addSubItems(ArrayList<RadialMenuItem> subChildren) {
		subItems.addAll(subChildren);
	}

	public ArrayList <RadialMenuItem> getSubItems() {
		return subItems;
	}

	public boolean isExpanded() {
		return isExpanded;
	}

	public void setExpanded(boolean isExpanded) {
		this.isExpanded = isExpanded;
	}

}