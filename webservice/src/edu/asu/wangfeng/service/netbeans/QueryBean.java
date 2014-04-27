/*
 * @author Fang Weng
 * © Arizona State University 2014
 * 
 * Class to store the return bean for services.
 * Stores the image name and the number of the image
 */

package edu.asu.wangfeng.service.netbeans;

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
