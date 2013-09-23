package edu.asu.wangfeng.surname.service.netbeans;

public class QueryBean{
	private String image;
	private int number;
	
	public String getImage() {
		return image;
	}
	public void setImage(String image) {
		this.image = image;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	@Override
	public String toString() {
		return "QueryBean [image=" + image + ", number=" + number + "]";
	}
	
}
