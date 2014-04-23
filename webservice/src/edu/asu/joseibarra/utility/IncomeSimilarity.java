/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Stores income similarity information (the similarity
 * to another name) and the name it represents
 */

package edu.asu.joseibarra.utility;

public class IncomeSimilarity {
	//this similarity is inverse (0 is more similar)
	public double similarity;
	public String name;
	
	public IncomeSimilarity(String name, double similarity){
		this.name = name;
		this.similarity = similarity;
	}
}