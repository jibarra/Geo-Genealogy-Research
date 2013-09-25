package edu.asu.fengwang.visualization;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;

public class KDEPainter {
    private int zoom;
    private double bandwidth;
    private Point leftTopPixel;
    private Point rightBottomPixel;
    private List<LatLng> sampleList;
    private String filename;
    private double[][] weightMatrix;
    private int width, height;
    private final int personBandwidth = 20;

    public void setZoom(int zoom) {
    	this.zoom = zoom;
    }

    public void setBandwidth(double bandwidth) {
    	this.bandwidth = bandwidth;
    }

    public void setLeftTopPixel(Point leftTopPixel) {
    	this.leftTopPixel = leftTopPixel;
    }

    public void setRightBottomPixel(Point rightBottomPixel) {
    	this.rightBottomPixel = rightBottomPixel;
    }

    public void setSampleList(List<LatLng> sampleList) {
    	this.sampleList = sampleList;
    }

    public void setFilename(String filename) {
    	this.filename = filename;
    }

    public void drawKDEMap() throws IOException {
		width = rightBottomPixel.x - leftTopPixel.x;
		height = rightBottomPixel.y - leftTopPixel.y;
		weightMatrix = new double[height][];
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
	
		long tstart = System.currentTimeMillis();
		
		Vector<Point> personPoints = new Vector<Point>();
		for(LatLng latlng : sampleList){
			Point p = mercator.fromLatLngToPoint(latlng);
			personPoints.add(p);
		}
		
		HashMap<Point, Double> valueMap = new HashMap<Point, Double>();
		for(Point point : personPoints){
			Double v = valueMap.get(point);
			Double newValue = null;
			if(v==null)
				newValue = new Double(1);
			else
				newValue = new Double(v.doubleValue() + 1);
			valueMap.put(point, newValue);
		}
		
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);
		
		
//		for (int i = 0; i < height; i++) {
//		    weightMatrix[i] = new double[width];
//		}
//		for (int i = 0; i < height; i++) {
//		    for (int k = 0; k < width; k++) {
//				double sum = 0;
//		
//				Point curPoint = new Point();
//				curPoint.x = leftTopPixel.x + k;
//				curPoint.y = leftTopPixel.y + i;
//	
//				LatLng latLng = mercator.fromPointToLatLng(curPoint);
//				for (LatLng sample : sampleList) {
//				    double distance = mercator.distance(latLng, sample);
//				    if (distance <= bandwidth) {
//						double temp = distance / bandwidth;
//						sum += 2 / Math.PI * (1 - temp * temp);
//				    }
//				}
//				weightMatrix[i][k] = sum;
//		    }
//		}
		
		long tend = System.currentTimeMillis();
		System.out.println("Elapsed time for drawing: " + (tend-tstart) / 1000.0);
		writeIntoPNG();
    }

    private void writeIntoPNG() throws IOException {
		File file = new File(filename);
		file.createNewFile();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		ColorBrewer colorBrewer = new ColorBrewer();
		colorBrewer.init(weightMatrix);
		int[] scanline = new int[width];
		for (int i = 0; i < height; i++) {
		    for (int k = 0; k < width; k++) {
		    	scanline[k] = colorBrewer.getColor(weightMatrix[i][k]);
		    }
		    image.setRGB(0, i, width, 1, scanline, 0, 0);
		}
	
		image.flush();
		ImageIO.write(image, "png", file);
    }
	
	    private class ColorBrewer {
		private final int[] colorBrewer = { 0xFFFFFFB2, 0xFFFED976, 0xFFFEB24C,
			0xFFFD8D3C, 0xFFFC4E2A, 0xFFE31A1C, 0xFFB10026, 0xFFB10026 };
		private double maxValue;
	
		public int getColor(double value) {
		    if (value < Double.MIN_NORMAL) {
		    	return 0;
		    }
		    return colorBrewer[(int) (value * 7 / maxValue)];
	}

	public void init(double[][] pixel) {
	    maxValue = 0;
	    for (int i = 0; i < pixel.length; i++) {
			for (int k = 0; k < pixel[i].length; k++) {
			    maxValue = Math.max(maxValue, pixel[i][k]);
			}
	    }
	}

    }

}