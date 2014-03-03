package edu.asu.joseibarra.utility;

public class IncomeSimilarity implements Comparable<IncomeSimilarity>{
	//this similarity is inverse (0 is more similar)
	public double similarity;
	public String name;
	
	public IncomeSimilarity(String name, double similarity){
		this.name = name;
		this.similarity = similarity;
	}
	
	@Override
	public int compareTo(IncomeSimilarity o){
		if (similarity == o.similarity) {
			return 0;
		} else if (similarity > o.similarity) {
			return 1;
		} else {
			return -1;
		}
	}
}
