package edu.asu.joseibarra.zillow;

import java.util.HashMap;

public class ZillowStateZipDemographics {
	private String state;
	private HashMap<String, Integer> zipValues;
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
	
	public void addZipValue(String zip, Integer value){
		zipValues.put(zip, value);
	}
	
	public int getZipValue(String zip){
		return zipValues.get(zip);
	}
	
	public String getState(){
		return state;
	}
	
	public HashMap<String, Integer> getZipValues(){
		return zipValues;
	}
	
	public String getCurrency(){
		return currency;
	}
	
	public void setState(String state){
		this.state = state;
	}
	
	public void setZipValues(HashMap<String, Integer> zipValues){
		this.zipValues = zipValues;
	}
	
	public void setCurrency(String currency){
		this.currency = currency;
	}
}
