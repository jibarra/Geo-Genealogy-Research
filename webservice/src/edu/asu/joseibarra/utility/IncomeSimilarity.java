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