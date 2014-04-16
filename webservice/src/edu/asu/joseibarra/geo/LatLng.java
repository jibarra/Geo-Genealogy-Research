package edu.asu.joseibarra.geo;

public class LatLng {
	private double lat;
	private double lng;
	
	public LatLng(){
		this.lat = 0;
		this.lng = 0;
	}
	
	public LatLng(double lat, double lng){
		this.lat = lat;
		this.lng = lng;
	}
	
	public double lat(){
		return lat;
	}
	
	public double lng(){
		return lng;
	}
	
	public void lat(double lat){
		this.lat = lat;
	}
	
	public void lng(double lng){
		this.lng = lng;
	}
	
	@Override
	public String toString(){
		return "Lat: " + lat() + ", Lng: " + lng();
	}
}
