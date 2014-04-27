/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * � Arizona State University 2014
 * 
 * Stores information about a name and its income
 * range.
 */

package edu.asu.joseibarra.scripts.resources;

public class NameRange {
	public String name;
	public double[] range;
	
	public NameRange(String name, double[] range){
		this.name = name;
		this.range = range;
	}
}
