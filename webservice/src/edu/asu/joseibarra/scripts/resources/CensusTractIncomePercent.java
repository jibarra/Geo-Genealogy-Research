/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Class to store the informatino about a
 * census tract, including the percentages
 * for the Census bins and its tract id
 */

package edu.asu.joseibarra.scripts.resources;

public class CensusTractIncomePercent {

	public CensusTractIncomePercent() {
		incomes = null;
		tract_id = "";
	}
	
	public CensusTractIncomePercent(String id, float[] incomes) {
		tract_id = id;
		this.incomes = incomes;	
	}
	
	public String tract_id;
	public float[] incomes;
	
	public String toString(){
		String retString = "census id: " + tract_id;
		if(!(incomes == null) && !(incomes.length < 1)){
			float total = 0;
			for(int i = 0; i < incomes.length; i++){
				total += incomes[i];
				retString += ", " + (i+1) + ": " + incomes[i];
			}
			retString += ", total: " + total;
		}
		return retString;
	}
}
