/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Stores income similarity information (the similarity
 * to another name) and the name it represents
 */

package edu.asu.joseibarra.utility;

import edu.asu.wangfeng.service.netbeans.IncomeComparisonBean;

public class IncomeSimilarity implements Comparable<IncomeSimilarity>{
	//this similarity is inverse (0 is more similar)
	public double similarity;
	public String name;
	
	public IncomeSimilarity(String name, double similarity){
		this.name = name;
		this.similarity = similarity;
	}

	@Override
	public int compareTo(IncomeSimilarity b) {
		if(similarity > b.similarity) {
			return -1;
		}
		if(similarity < b.similarity) {
			return 1;
		}
		return 0;
	}
}