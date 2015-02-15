package it.polito.elite.android.layout;


import java.util.ArrayList;

import com.hp.hpl.jena.datatypes.RDFDatatype;

import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Resource;


public class NodeItem implements VisualNode {

	//ha l'attributo resource in cui salvo la classe o la risorsa che rappresenta
	//se è una risorsa allora è thing
	private Resource resource;
	//devo memorizzare anche il parent del node item, così quando seleziono una vista, posso risalire alla gerarchia
	//e disegnare tutti i nodi fino a thing
	private NodeItem parent;
	
	

	private int height = 0;
	private double width = Math.PI*2;
	private double angle;
	private int x = -1;
	private int y = -1;
	
	private boolean hasSubclass;
	//segno se è drawable o no
	private boolean drawable = true;
	//dimensioni dell'icona che devo poi settare
	private int icondimension;



	public NodeItem(){
		hasSubclass = false;
		icondimension = 35;
	}
	
	public void setParent(NodeItem parent) {
		this.parent = parent;
	}
	public boolean isDrawable() {
		return drawable;
	}


	public void setDrawable(boolean drawable) {
		this.drawable = drawable;
	}


	public NodeItem getParent() {
		return parent;
	}


	public int getIcondimension() {
		return icondimension;
	}

	public void setIcondimension(int icondimension) {
		this.icondimension = icondimension;
	}

	public double getX() {
		// TODO Auto-generated method stub
		return x;
	}

	public double getY() {
		// TODO Auto-generated method stub
		return y;
	}

	public int getHeight() {
		// TODO Auto-generated method stub
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
		
	}

	public double getWidth() {
		// TODO Auto-generated method stub
		return width;
	}

	public void setWidth(double aw) {
		this.width = aw;
		
	}

	public double getAngle() {
		// TODO Auto-generated method stub
		return angle;
	}

	public void setAngle(double angle) {
		this.angle = angle;
		
	}

	public Resource getResource() {
		return resource;
	}

	public void setResource(Resource resource) {
		this.resource = resource;
	}

	public void setX(double x) {
		this.x = (int) x;
		
	}

	public void setY(double y) {
		this.y = (int) y;
		
	}

	public boolean getHasSubclass() {
		return hasSubclass;
	}

	public void setHasSubclass(boolean hasSubclass) {
		this.hasSubclass = hasSubclass;
	}



	




}
