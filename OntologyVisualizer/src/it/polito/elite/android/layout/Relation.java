package it.polito.elite.android.layout;


public class Relation {
	
	//devo sapere NodeA e NodeB tra cui c'è la relazione
	private NodeItem source_node;
	private NodeItem target_node;
	
	//devo sapere se puo' essere disegnata o no
	private boolean drawable = true;
	
	//devo sapere quale relazione è
	private String name;

	public Relation(NodeItem source_node, NodeItem target_node, String name) {
		super();
		this.source_node = source_node;
		this.target_node = target_node;
		this.name = name;
	}

	public boolean isDrawable() {
		return drawable;
	}

	public void setDrawable(boolean drawable) {
		this.drawable = drawable;
	}

	public double getStartX(){
		return source_node.getX();
	}
	public double getStartY(){
		return source_node.getY();
	}
	
	public double getStopX(){
		return target_node.getX();
	}
	
	public double getStopY(){
		return target_node.getY();
	}
	public NodeItem getNodeA() {
		return source_node;
	}

	public void setNodeA(NodeItem nodeA) {
		this.source_node = nodeA;
	}

	public NodeItem getNodeB() {
		return target_node;
	}

	public void setNodeB(NodeItem nodeB) {
		this.target_node = nodeB;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	
}
