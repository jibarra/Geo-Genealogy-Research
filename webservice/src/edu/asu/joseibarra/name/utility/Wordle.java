/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Class to hold the information about a wordle.
 * Can hold the closeness value to an entered name
 * and the name that the closeness value represents.
 */

package edu.asu.joseibarra.name.utility;

public class Wordle {
	private String name;
	private float closeness; 
	
	public Wordle(String name, float closeness){
		this.name = name;
		this.closeness = closeness;
	}
	
	public String getName(){
		return name;
	}
	
	public void setName(String name){
		this.name = name;
	}
	
	public float getCloseness(){
		return closeness;
	}
	
	public void setCloseness(float closeness){
		this.closeness = closeness;
	}
}
