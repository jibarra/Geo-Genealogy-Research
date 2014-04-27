/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Class to store the demographics for a state/zip relationship.
 */

package edu.asu.joseibarra.zillow;

import java.util.HashMap;

public class ZillowStateZipDemographics {
	//The US state of the object.
	private String state;
	//Hashmap linking zip(String) to its value(Integer)
	private HashMap<String, Integer> zipValues;
	//Currency of this demographics information
	private String currency;
	
	public ZillowStateZipDemographics(String state){
		this.state = state;
		this.currency = "USD";
		zipValues = new HashMap<String, Integer>();
	}
	
	public ZillowStateZipDemographics(String state, String currency){
		this.state = state;
		this.currency = currency;
		zipValues = new HashMap<String, Integer>();
	}
	
	//Adds a zip value to teh hashmap
	public void addZipValue(String zip, Integer value){
		zipValues.put(zip, value);
	}
	
	//Gets a $value based on an inputted zip
	public int getZipValue(String zip){
		return zipValues.get(zip);
	}
	
	public String getState(){
		return state;
	}
	
	//Gets all teh zip values for this state.
	public HashMap<String, Integer> getZipValues(){
		return zipValues;
	}
	
	public String getCurrency(){
		return currency;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	//Sets the entire zip value hashmap
	public void setZipValues(HashMap<String, Integer> zipValues){
		this.zipValues = zipValues;
	}
	
	public void setCurrency(String currency){
		this.currency = currency;
	}
}
