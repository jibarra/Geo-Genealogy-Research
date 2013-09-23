package edu.asu.wangfeng.geo;

import java.awt.Point;
import java.awt.geom.Point2D;

public class WGS84Mercator {
	private static final int TILE_SIZE = 256;
	private static final double MAX_LAT = 85.05112877980659;
	private static final double MIN_LAT = -85.05112877980659;
	private static final double EARTH_RADIUS = 6378137; // equatorial earth
														// radius for EPSG:3857
														// (Mercator)
	
//	private static final Point CENTER_POINT = new Point(TILE_SIZE/2, TILE_SIZE/2);
//	private static final double PIXELS_PER_LON_DEGREE = TILE_SIZE/360;
//	private static final double PIXELS_PER_LON_RADIAN = TILE_SIZE / (2*Math.PI);

	public static double getRadius(int zoomLevel) {
		return (TILE_SIZE * (1 << zoomLevel)) / (2.0 * Math.PI);
	}

	public static int getMaxPixels(int zoomLevel) {
		return TILE_SIZE * (1 << zoomLevel);
	}

	public static int falseEasting(int zoomLevel) {
		return getMaxPixels(zoomLevel) / 2;
	}

	public static int falseNorthing(int zoomLevel) {
		return (-1 * getMaxPixels(zoomLevel) / 2);
	}

	/**
	 * ! Transform longitude to pixel space
	 * 
	 * Mathematical optimization x = radius(zoomLevel) * toRadians(longitude) +
	 * falseEasting(zoomLevel) x = getMaxPixels(zoomLevel) * longitude / 360 +
	 * 180 * getMaxPixels(zoomLevel) / 360 x = getMaxPixels(zoomLevel) *
	 * (longitude + 180) / 360
	 * 
	 * @param longitude
	 *            [-180..180]
	 * @return the pixel position
	 * @author Jan Peter Stotz
	 */
	public static int lonToX(double longitude, int zoomLevel) {
		int mp = getMaxPixels(zoomLevel);
		int x = (int) ((mp * (longitude + 180l)) / 360l);
		x = Math.min(x, mp - 1);
		return x;
	}

	/**
	 * ! Transforms latitude to pixel space
	 * 
	 * Mathematical optimization y = -1 * (radius(zoomLevel) / 2 * log(u)))) -
	 * falseNorthing(zoomLevel)) y = -1 * (getMaxPixel(zoomLevel) / 2 * PI / 2 *
	 * log(u)) - -1 * getMaxPixel(zoomLevel) / 2 y = getMaxPixel(zoomLevel) /
	 * (-4 * PI) * log(u)) + getMaxPixel(zoomLevel) / 2 y =
	 * getMaxPixel(zoomLevel) * ((log(u) / (-4 * PI)) + 1/2)
	 * 
	 * @param lat
	 *            [-90...90]
	 * @return the x value in pixel space
	 * @author Jan Peter Stotz
	 */
	public static int latToY(double lat, int zoomLevel) {
		if (lat < MIN_LAT)
			lat = MIN_LAT;
		else if (lat > MAX_LAT)
			lat = MAX_LAT;

		double sinLat = Math.sin(lat * Math.PI / 180.0);
		double logvalue = Math.log((1.0 + sinLat) / (1.0 - sinLat));
		int mp = getMaxPixels(zoomLevel);
		int y = (int) (mp * (0.5 - (logvalue / (4.0 * Math.PI))));
		y = Math.min(y, mp - 1);
		return y;
	}

	/**
	 * ! Transforms pixel coordinate X to longitude Mathematical optimization
	 * lon = toDegree((aX - falseEasting(zoomLevel)) / radius(zoomLevel)) lon =
	 * 180 / PI * ((aX - getMaxPixels(zoomLevel) / 2) / getMaxPixels(zoomLevel)
	 * / (2 * PI) lon = 180 * ((aX - getMaxPixels(zoomLevel) / 2) /
	 * getMaxPixels(zoomLevel)) lon = 360 / getMaxPixels(zoomLevel) * (aX -
	 * getMaxPixels(zoomLevel) / 2) lon = 360 * aX / getMaxPixels(zoomLevel) -
	 * 180
	 * 
	 * @param x
	 *            the x value in pixel space
	 * @return longitude
	 * @author Jan Peter Stotz
	 */
	public static double xToLon(int x, int zoomLevel) {
		return ((360.0 * x) / getMaxPixels(zoomLevel)) - 180.0;
	}

	/**
	 * ! Transforms pixel coordinate Y to latitude
	 * 
	 * @param y
	 *            pixel space coordinate
	 * @return latitude
	 */
	public static double yToLat(int ay, int zoomLevel) {
		int y = ay + falseNorthing(zoomLevel);
		double latitude = (Math.PI / 2) - (2 * Math.atan(Math.exp(-1.0 * y / getRadius(zoomLevel))));
		return -1 * latitude * 180.0 / Math.PI;
	}

	public static Point lonLatToWorld(Point2D.Double p, int zoomLevel) {
		return new Point(lonToX(p.x, zoomLevel), latToY(p.y, zoomLevel));
	}

	public static Point2D.Double worldToLonLat(Point p, int zoomLevel) {
		return new Point2D.Double(xToLon(p.x, zoomLevel), yToLat(p.y, zoomLevel));
	}

	/**
	 * ! Transform pixel space to coordinates and get the distance.
	 * 
	 * @param x1
	 *            the first x coordinate
	 * @param y1
	 *            the first y coordinate
	 * @param x2
	 *            the second x coordinate
	 * @param y2
	 *            the second y coordinate
	 * 
	 * @param zoomLevel
	 *            the zoom level
	 * @return the distance
	 * @author Jason Huntley
	 */
	public static double  getDistance(Point p1, Point p2, int zoomLevel) {
		Point2D.Double lngLat1 = worldToLonLat(p1, zoomLevel);
		Point2D.Double lngLat2 = worldToLonLat(p2, zoomLevel);
		return getDistance(lngLat1, lngLat2);
	}

	/**
	 * ! Gets the distance using Spherical law of cosines.
	 * 
	 * @param la1
	 *            the Latitude in degrees
	 * @param lo1
	 *            the Longitude in degrees
	 * @param la2
	 *            the Latitude from 2nd coordinate in degrees
	 * @param lo2
	 *            the Longitude from 2nd coordinate in degrees
	 * @return the distance
	 * @author Jason Huntley
	 */
	public static double getDistance(Point2D.Double p1, Point2D.Double p2) {

		Point2D.Double start = new Point2D.Double(p1.x * Math.PI / 180.0, p1.y * Math.PI / 180.0);
		Point2D.Double end = new Point2D.Double(p2.x * Math.PI / 180.0, p2.y * Math.PI / 180.0);

		double distance = Math.acos(Math.sin(start.y) * Math.sin(end.y) + Math.cos(start.y) * Math.cos(end.y)
				* Math.cos(start.x - end.x));

		return (EARTH_RADIUS * distance);
	}
	public static void main(String[] args) {
		Point2D.Double p1 = new Point2D.Double(-112.015, 33.4483);
		Point2D.Double p2 = new Point2D.Double(-111.947, 33.4446);
		
		System.out.println(getDistance(p1, p2));
	}
}
