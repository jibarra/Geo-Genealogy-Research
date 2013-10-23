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
import java.awt.geom.Point2D;
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
		
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		
		//Convert the lat/longs from the inputted list to map points
		//then convert to pixels on the screen
		Vector<Point> pointVec = new Vector<Point>(sampleList.size());
		for (LatLng latlng : sampleList) {
			Point p = mercator.fromLatLngToPoint(latlng);
			Point q = new Point(p.x-leftTopPixel.x, p.y-leftTopPixel.y);
			pointVec.add(q);
		}
		sampleList=null;
		
		//Create a hashmap of the duplicate pixels
		HashMap<Point, Double> valueMap = new HashMap<Point, Double>();
		for (Point point : pointVec) {
			Double v = valueMap.get(point);
			Double newValue = null;
			if (v == null) {
				newValue = new Double(1);
			} else {
				newValue = new Double(v.doubleValue() + 1);
			}
			valueMap.put(point, newValue);
		}
		
		pointVec = null;
		
		//Calculate the bandwidth for the pixel distances
		int limit = width/2;
		int distanceI;
		for(distanceI=1; distanceI<=limit; distanceI++)
		{
			Point left = new Point(width/2 - distanceI, height/2);
			Point right = new Point(width/2 + distanceI, height/2);
			Point worldLeft = new Point(left.x+leftTopPixel.x, left.y+leftTopPixel.y);
			Point worldRight = new Point(right.x+leftTopPixel.x, right.y+leftTopPixel.y);
			double distance = mercator.distance(mercator.fromPointToLatLng(worldLeft), mercator.fromPointToLatLng(worldRight));
			if(distance >= bandwidth){
				break;
			}
		}
		int pixelBandwidth = 2*distanceI;

		//An array of the distinct pixels on the map.
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

		//Create an array of weighted distances for each distinct pixel
		//Each array row contains the weighted distance between another pixel
		WeightDistance[][] distance = new WeightDistance[distinctPointArray.length][distinctPointArray.length];
		
		for (int i = 0; i < distance.length; i++) {
			//Calculate the distance and weight between pixels
			for (int k = 0; k < distance[i].length; k++) {
				distance[i][k] = new WeightDistance();

				Point diff = new Point(distinctPointArray[i].x - distinctPointArray[k].x, distinctPointArray[i].y
						- distinctPointArray[k].y);
				distance[i][k].distance = diff.x * diff.x + diff.y * diff.y;

				distance[i][k].id = k;
				distance[i][k].weight = valueMap.get(distinctPointArray[i]).doubleValue();
			}
			valueMap.remove(distinctPointArray[i]);
			//Sort the row
			Arrays.sort(distance[i]);
			// prefix sum
			for (int k = 1; k < distance[i].length; k++) {
				distance[i][k].weight += distance[i][k - 1].weight;
			}
		}
		valueMap = null;

		double sumWeight = distance[0][distance[0].length - 1].weight;
		
		double[][] pixel = new double[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}

		//Color each pixel based on the weight and distance between other pixels
		//that were calculated above
		for (int i = 0; i < distance.length; i++) {
			double weight = distance[i][0].weight;
			Point centerPoint = distinctPointArray[i];
			int indexBound = pixelBandwidth;
			int[] searchRange = { centerPoint.x - indexBound, centerPoint.y - indexBound, 
					centerPoint.x + indexBound, centerPoint.y + indexBound };

			for (int x = searchRange[0]; x <= searchRange[2]; x++) {
				for (int y = searchRange[1]; y <= searchRange[3]; y++) {
					// in image?
					Point curPixel = new Point(x, y);

					if (!(curPixel.x >= 0 && curPixel.x < width && curPixel.y >= 0 && curPixel.y < height)) {
						continue;
					}
					
					// we use circle
					Point diff = new Point(curPixel.x - centerPoint.x, curPixel.y - centerPoint.y);
					double squareDistance = diff.x * diff.x + diff.y * diff.y;
					double squareBandwidth = pixelBandwidth * pixelBandwidth;
					if (squareDistance > squareBandwidth) {
						continue;
					}
					pixel[y][x] += weight * kernelFunction(diff, sumWeight, pixelBandwidth, squareBandwidth);
				}
			}
		}
		distance = null;
		distinctPointArray=null;

		// weight to image
		ColorBrewer colorBrewer = new ColorBrewer();
		colorBrewer.init(pixel);
		File file = new File(filename);
		file.createNewFile();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		int[] scanline = new int[width];
		// we use a fake image for testing
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				scanline[k] = colorBrewer.getColor(pixel[i][k]);
			}
			image.setRGB(0, i, width, 1, scanline, 0, 0);
		}
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
    }
    
    static double epanechnikov(double u) {
		return 0.75 * (1 - u * u);
	}

	static double kernelFunction(Point diff, double sumWeight, double h, double h2) {
		Point2D.Double diffF = new Point2D.Double(diff.x / h, diff.y / h);
		return epanechnikov(diffF.x) * epanechnikov(diffF.y) / (h2 * sumWeight);
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