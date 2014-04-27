/*
 * @author Feng Wang
 * © Arizona State University 2014
 * 
 * Class to create a static google map.
 */

package edu.asu.wangfeng.google.staticmaps;

import java.awt.geom.Point2D;
import java.net.MalformedURLException;
import java.net.URL;

public class GoogleStaticMapsGenerator {
	private static final String appkey = "AIzaSyDlI3z8Uvju81s33YN8fb6kxEE-afHz5Tk";
	private static final String mapURL = "https://maps.googleapis.com/maps/api/staticmap?";

	private int width;
	private int height;
	private Point2D.Double center;
	private int zoom;

	public int getWidth() {
		return width;
	}

	public void setWidth(int width) {
		this.width = width;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public Point2D.Double getCenter() {
		return center;
	}

	public void setCenter(Point2D.Double center) {
		this.center = center;
	}

	public int getZoom() {
		return zoom;
	}

	public void setZoom(int zoom) {
		this.zoom = zoom;
	}

	public URL generateURL() throws MalformedURLException{
		String resultURL = mapURL + "center=" + center.y + "," + center.x 
				+ "&size=" + width + "x" + height + "&zoom=" + zoom
				+"&key=" + appkey + "&sensor=false&format=png32";
		return new URL(resultURL);
	}
}
