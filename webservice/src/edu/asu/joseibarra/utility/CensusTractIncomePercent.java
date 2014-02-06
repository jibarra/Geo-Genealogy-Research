package edu.asu.joseibarra.utility;

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
