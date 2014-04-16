package edu.asu.wangfeng.geo;

import java.awt.Point;
import java.awt.geom.Point2D;

public class GeoUtils {
	private GoogleMercator mercator;
	
	public GeoUtils(){
		mercator = new GoogleMercator();
	}
	
	public Point lonLatToWorld(Point2D.Double p, int zoomLevel) {
//		return WGS84Mercator.lonLatToWorld(p, zoomLevel);
		mercator.setZoom(zoomLevel);
		return mercator.fromLatLngToPoint(new LatLng(p.y, p.x));
	}

	public Point2D.Double worldToLonLat(Point p, int zoomLevel) {
		mercator.setZoom(zoomLevel);
		LatLng latlng = mercator.fromPointToLatLng(p);
		return new Point2D.Double(latlng.lat(), latlng.lng());
//		return WGS84Mercator.worldToLonLat(p, zoomLevel);
	}

	public Point worldToScreen(Point p, Point center, int width, int height) {
		Point tempCenter = new Point(width / 2, height / 2);
		return new Point(tempCenter.x - center.x + p.x, tempCenter.y - center.y + p.y);
	}

	public Point screenToWorld(Point p, Point center, int width, int height) {
		Point tempCenter = new Point(width / 2, height / 2);
		return new Point(center.x - tempCenter.x + p.x, center.y - tempCenter.y + p.y);
	}

	public Point lonLatToScreen(Point2D.Double p, int zoomLevel, Point2D.Double center, int width, int height) {
		return worldToScreen(lonLatToWorld(p, zoomLevel), lonLatToWorld(center, zoomLevel), width, height);
	}
	
	public Point2D.Double screenToLonLat(Point p, int zoomLevel, Point center, int width, int height) {
		Point world = screenToWorld(p, center, width, height);
		return worldToLonLat(world, zoomLevel);
	}
	
	public Point2D.Double screenToLonLat(Point p, int zoomLevel, Point2D.Double center, int width, int height) {
		Point worldCenter = lonLatToWorld(center, zoomLevel);
		return screenToLonLat(p, zoomLevel, worldCenter, width, height);
	}
	
	public double getDistance(Point p1, Point p2, Point center, int width, int height, int zoomLevel){
		Point2D.Double a = screenToLonLat(p1, zoomLevel, center, width, height);
		Point2D.Double b = screenToLonLat(p2, zoomLevel, center, width, height);
		return WGS84Mercator.getDistance(a, b);
	}
}