/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Class that stores service bean information for the mean
 * and medians of a person.
 */

package edu.asu.wangfeng.service.netbeans;

import java.util.LinkedList;

public class QueryNameIncomeBean{
	private LinkedList<Double> meanIncome;
	private LinkedList<Double> medianIncome;
	
	public LinkedList<Double> getMeanIncome(){
		return meanIncome;
	}
	public void setMeanIncome(LinkedList<Double> mean){
		this.meanIncome = mean;
	}
	public LinkedList<Double> getMedianIncome(){
		return medianIncome;
	}
	public void setMedianIncome(LinkedList<Double> median){
		this.medianIncome = median;
	}
	@Override
	public String toString() {
		return "QueryBean [meanIncome=" + meanIncome.toString()
				+ ", medianIncome=" + medianIncome.toString() + "]";
	}
	
}
