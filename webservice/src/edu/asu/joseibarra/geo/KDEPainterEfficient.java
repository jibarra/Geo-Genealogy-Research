/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * � Arizona State University 2014
 * 
 * This class produced a KDE heatmap of a name.
 * Some variables must be set for it to work
 * properly before calling a method including:
 * the topleft and rightbottom pixels,
 * the list of latitude and longitude points (sampleList)
 * the zoom level, the bandwidth and the 
 * filename for output.
 * 
 * This class creates many, many repeated methods and
 * can be refactored. Methods will be commented to show
 * what each one can do but lots of code within methods are
 * repeated.
 */

package edu.asu.joseibarra.geo;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;
import javax.naming.NamingException;

public class KDEPainterEfficient {
    private int zoom;
    private double bandwidth;
    private Point leftTopPixel;
    private Point rightBottomPixel;
    private List<LatLng> sampleList;
    private String filename;
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
    
    
    /*
     * Create a hashmap of the points inputted.
     * This converts the latitude and longitude inputted into this
     * class into a hashmap of map points (pixels) and the amount
     * of people on that pixel (which would be the Double part
     * of the hashmap)
     */
    public HashMap<Point, Double> createValueMap(GoogleMercator mercator){
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
		return valueMap;
    }
    
    /*
     * Calculates the bandwidth of the map. Typically this will
     * always be the input.
     */
    public int calculateBandwidth(GoogleMercator mercator){
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
		return 2*distanceI;
    }
    
    /*
     * Weight teh distances between distinct points that were inputted.
     * Results will be placed in teh distance array.
     */
    public void weightDistances(HashMap<Point, Double> valueMap, Point[] distinctPointArray, double[][] distance){
    	for (int i = 0; i < distance.length; i++) {
			//Calculate the distance and weight between pixels
			for (int k = 0; k < distance[i].length; k++) {
				distance[i][k] = valueMap.get(distinctPointArray[i]).doubleValue();
			}
			valueMap.remove(distinctPointArray[i]);
			// prefix sum
			for (int k = 1; k < distance[i].length; k++) {
				distance[i][k] += distance[i][k - 1];
			}
		}
    }
    
    /*
     * Computes a proabilistic KDE map based on the precomputed population
     * of the United States. What this method does is create a regular
     * map of a person, then divide that person's population at each
     * point by the US population to deemphasize densely populated areas.
     */
    @SuppressWarnings("unchecked")
	public void drawProbabilisticKDEMap(int type) throws IOException {
		width = rightBottomPixel.x - leftTopPixel.x;
		height = rightBottomPixel.y - leftTopPixel.y;
		
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		
		//Create a hashmap of the duplicate pixels
		HashMap<Point, Double> valueMap = createValueMap(mercator);
		HashMap<Point, Double> savedMap = null;
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\webservice\\resources\\USvalueMap.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    try {
			savedMap = (HashMap<Point, Double>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    input = null;
	    inBuffer = null;
	    inFile = null;
	    
	    Double amount;
		Point[] keyArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);
	    HashMap<Point, Double> probMap = new HashMap<Point, Double>();
	    for (Point point : keyArray) {
			amount = savedMap.get(point);
			if(amount != null && amount > 0.0001){
				probMap.put(point, valueMap.get(point)/amount);
			}
			else{
			}
		}
	    keyArray = null;
	    savedMap = null;
	    
	    keyArray = probMap.keySet().toArray(new Point[probMap.keySet().size()]);
	    for (Point point : keyArray) {
	    	valueMap.put(point, probMap.get(point));
	    }
	    probMap = null;
	    keyArray = null;
	    
		int pixelBandwidth = calculateBandwidth(mercator);

		//An array of the distinct pixels on the map.
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

		//Create an array of weighted distances for each distinct pixel
		//Each array row contains the weighted distance between another pixel
		double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
		
		weightDistances(valueMap, distinctPointArray, distance);
		
		valueMap = null;

		double sumWeight = distance[0][distance[0].length - 1];
		
		double[][] pixel = new double[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}

		//Color each pixel based on the weight and distance between other pixels
		//that were calculated above
		for (int i = 0; i < distance.length; i++) {
			double weight = distance[i][0];
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
		if(type == 1){
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
		else{
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getForenameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
				
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
    }
    
    /*
     * Draws a KDE map based on an already calculated distinct point hashmap
     */
    public void drawKDEMapWithHash(HashMap<Point, Double> valueMap) throws IOException{
    	long start = System.nanoTime();
		width = rightBottomPixel.x - leftTopPixel.x;
		height = rightBottomPixel.y - leftTopPixel.y;
		
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		
		//Create a hashmap of the duplicate pixels
		int pixelBandwidth = calculateBandwidth(mercator);

		//An array of the distinct pixels on the map.
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

		//Create an array of weighted distances for each distinct pixel
		//Each array row contains the weighted distance between another pixel
		double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
		
		weightDistances(valueMap, distinctPointArray, distance);
		
		valueMap = null;

		double sumWeight = distance[0][distance[0].length - 1];
		
		double[][] pixel = new double[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}

		//Color each pixel based on the weight and distance between other pixels
		//that were calculated above
		for (int i = 0; i < distance.length; i++) {
			double weight = distance[i][0];
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
				scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
			}
			image.setRGB(0, i, width, 1, scanline, 0, 0);
		}
				
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
    	long end = System.nanoTime();
    	System.out.println("Time to create KDE map from hash: " + ((end-start) / 1000000000.0) + " seconds");
    }
    
    /*
     * Draws a regular KDE map, comparing it to nothing.
     * Will output to a specified file.
     * Current implementation also outputs the computed name to another file
     * that will hold the information of the US overall. This can be
     * removed to eliminate this. This is a similar implementation to the
     * drawKDEMap() function below. Use that instead for simiply drawing
     * maps.
     */
    public void drawKDEMap(String surname) throws IOException{
		width = rightBottomPixel.x - leftTopPixel.x;
		height = rightBottomPixel.y - leftTopPixel.y;
		
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		
		//Create a hashmap of the duplicate pixels
		HashMap<Point, Double> valueMap = createValueMap(mercator);
		int pixelBandwidth = calculateBandwidth(mercator);

		//An array of the distinct pixels on the map.
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

		//Create an array of weighted distances for each distinct pixel
		//Each array row contains the weighted distance between another pixel
		double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
		
		weightDistances(valueMap, distinctPointArray, distance);
		
		OutputStream outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\"+surname+"valueMap.ser");
		OutputStream outBuffer = new BufferedOutputStream(outFile);
	    ObjectOutput output = new ObjectOutputStream(outBuffer);
	    output.writeObject(valueMap);
	    output.close();
	    outBuffer.close();
	    outFile.close();
		
		valueMap = null;

		double sumWeight = distance[0][distance[0].length - 1];
		
		double[][] pixel = new double[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}

		//Color each pixel based on the weight and distance between other pixels
		//that were calculated above
		for (int i = 0; i < distance.length; i++) {
			double weight = distance[i][0];
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
		
		outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\"+surname+"distance.ser");
		outBuffer = new BufferedOutputStream(outFile);
	    output = new ObjectOutputStream(outBuffer);
	    output.writeObject(distance);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\"+surname+"distinctPointArray.ser");
		outBuffer = new BufferedOutputStream(outFile);
	    output = new ObjectOutputStream(outBuffer);
	    output.writeObject(distinctPointArray);
	    output.close();
	    outBuffer.close();
	    outFile.close();
		
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
				scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
			}
			image.setRGB(0, i, width, 1, scanline, 0, 0);
		}
				
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
    }
    
    /*
     * Draws a KDE map based on the inputted pixels and type of the map. 
     */
    public void drawKDEMap(double[][] pixel, int type) throws IOException{
    	ColorBrewer colorBrewer = new ColorBrewer();
		colorBrewer.init(pixel);
		File file = new File(filename);
		file.createNewFile();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		int[] scanline = new int[width];
		// we use a fake image for testing
		if(type == 1){
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
		else{
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getForenameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
				
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
    }

    /*
     * Draws a regular KDE map based on inputs.
     */
    public void drawKDEMap(int type) throws IOException {
		width = rightBottomPixel.x - leftTopPixel.x;
		height = rightBottomPixel.y - leftTopPixel.y;
		
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		
		//Create a hashmap of the duplicate pixels
		HashMap<Point, Double> valueMap = createValueMap(mercator);
		int pixelBandwidth = calculateBandwidth(mercator);

		//An array of the distinct pixels on the map.
		Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

		//Create an array of weighted distances for each distinct pixel
		//Each array row contains the weighted distance between another pixel
		double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
		
		weightDistances(valueMap, distinctPointArray, distance);
		
		valueMap = null;

		double sumWeight = distance[0][distance[0].length - 1];
		
		double[][] pixel = new double[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}

		//Color each pixel based on the weight and distance between other pixels
		//that were calculated above
		for (int i = 0; i < distance.length; i++) {
			double weight = distance[i][0];
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
		if(type == 1){
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
		else{
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getForenameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
		}
				
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
//    	System.out.println("Time to create KDE: " + ((end-start) / 1000000000.0) + " seconds");
    }
    
    /*
     * Calculates an epanechnikov value based on input
     */
    static double epanechnikov(double u) {
		return 0.75 * (1 - u * u);
	}

    /*
     * The kernel function for the KDE calculation
     */
	static double kernelFunction(Point diff, double sumWeight, double h, double h2) {
		Point2D.Double diffF = new Point2D.Double(diff.x / h, diff.y / h);
		return epanechnikov(diffF.x) * epanechnikov(diffF.y) / (h2 * sumWeight);
	}
	
	    private class ColorBrewer {
			private final int[] surnameColorBrewer = { 0xFFFFFFB2, 0xFFFED976, 0xFFFEB24C,
				0xFFFD8D3C, 0xFFFC4E2A, 0xFFE31A1C, 0xFFB10026, 0xFFB10026 };
			private final int[] forenameColorBrewer = { 0xFFB2B2FF, 0xFF7C7DF7, 0xFF004CFF,
				0xFF0080FF, 0xFF00B3FF, 0xFF05C6ED, 0xFF00FFFF, 0xFF00FBFF };
			private double maxValue;
	
			public int getSurnameColor(double value) {
			    if (value < Double.MIN_NORMAL) {
			    	return 0;
			    }
			    return surnameColorBrewer[(int) (value * 7 / maxValue)];
			}
			
			public int getForenameColor(double value){
				if (value < Double.MIN_NORMAL) {
			    	return 0;
			    }
			    return forenameColorBrewer[(int) (value * 7 / maxValue)];
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
	    
	    /*
	     * Does a query of a surname and generates a map to a test file.
	     */
	    public void sqlQuery(String surname) throws IOException{
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			Vector<LatLng> coordinateVec = new Vector<LatLng>();

			try {
				connection = connectDatabase();
				//SQL LOCATED HERE
				sql = "select latitude, longitude from phonebook where surname=? and latitude between 22.75592037564069 and 51.454006703387115 and longitude between -131.18019149999998 and -62.97706649999998";
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);

				statement.setString(1, surname);
				resultset = statement.executeQuery();
				while (resultset.next()) {
					double lat = resultset.getDouble(1);
					double lng = resultset.getDouble(2);
					coordinateVec.add(new LatLng(lat, lng));
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			
			setLeftTopPixel(new Point(555, 1363));
	    	setRightBottomPixel(new Point(1331, 1782));
	    	setZoom(4);
	    	setBandwidth(240);
	    	setSampleList(coordinateVec);
	    	coordinateVec = null;
	    	setFilename("C:\\Users\\jlibarr1\\Desktop\\test.png");
	    	drawKDEMap(1);
		}
	    
	    /*
	     * Connects to the database.
	     */
		public Connection connectDatabase() throws NamingException, SQLException {
//			javax.naming.Context initContext = new InitialContext();
//			javax.naming.Context envContext = (javax.naming.Context) initContext.lookup("java:/comp/env");
//			DataSource ds = (DataSource) envContext.lookup("jdbc/phonebook");
//			Connection conn = ds.getConnection();
			
			String url = "jdbc:mysql://localhost:3306/phonebook";
			String username = "root";
			String password = "password";
			java.sql.Connection conn = null;
			
			try{
				conn = DriverManager.getConnection(url, username, password);
			}
			catch(SQLException e){
				throw new RuntimeException("Cannot connect the database!", e);
			}
			return conn;
		}
	    
	    public static void main (String[] args) throws IOException{
	    	KDEPainterEfficient test = new KDEPainterEfficient();
	    	test.sqlQuery("SIMPSON");
	    }
	    
	    /*
	     * BELOW ARE THE UTILITY PARTS OF THIS CLASS.
	     * NEEDS TO BE REFACTORED
	     */
	    
	    
	    
	    /*
	     * Draws a probabilistic heatmap of the iniputted name.
	     * Also outputs to a file.
	     */
	    public void drawProbabilisticKDEMap(String surname) throws IOException {
	    	long start = System.nanoTime();
			width = rightBottomPixel.x - leftTopPixel.x;
			height = rightBottomPixel.y - leftTopPixel.y;
			
			GoogleMercator mercator = new GoogleMercator();
			mercator.setZoom(zoom);
			
			//Create a hashmap of the duplicate pixels
			HashMap<Point, Double> valueMap = createValueMap(mercator);
			HashMap<Point, Double> savedMap = null;
			InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\webservice\\resources\\USvalueMap.ser");
		    InputStream inBuffer = new BufferedInputStream(inFile);
		    ObjectInput input = new ObjectInputStream (inBuffer);
		    try {
				savedMap = (HashMap<Point, Double>)input.readObject();
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    
		    input.close();
		    inBuffer.close();
		    inFile.close();
		    input = null;
		    inBuffer = null;
		    inFile = null;
		    
		    Double amount;
			Point[] keyArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);
		    HashMap<Point, Double> probMap = new HashMap<Point, Double>();
		    int nulls = 0;
		    for (Point point : keyArray) {
				amount = savedMap.get(point);
				if(amount != null && amount > 0.0001){
					probMap.put(point, valueMap.get(point)/amount);
				}
				else{
					nulls++;
				}
			}
		    keyArray = null;
		    savedMap = null;
		    
		    keyArray = probMap.keySet().toArray(new Point[probMap.keySet().size()]);
		    for (Point point : keyArray) {
		    	valueMap.put(point, probMap.get(point));
		    }
		    probMap = null;
		    keyArray = null;
		    
			int pixelBandwidth = calculateBandwidth(mercator);

			//An array of the distinct pixels on the map.
			Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

			//Create an array of weighted distances for each distinct pixel
			//Each array row contains the weighted distance between another pixel
			double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
			
			weightDistances(valueMap, distinctPointArray, distance);
			
			valueMap = null;

			double sumWeight = distance[0][distance[0].length - 1];
			
			double[][] pixel = new double[height][width];
			for (int i = 0; i < height; i++) {
				Arrays.fill(pixel[i], 0);
			}

			//Color each pixel based on the weight and distance between other pixels
			//that were calculated above
			for (int i = 0; i < distance.length; i++) {
				double weight = distance[i][0];
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
			
			char firstChar = surname.charAt(0);
			if(firstChar < 65 || firstChar > 90)
				firstChar = '1';

			// weight to image
			ColorBrewer colorBrewer = new ColorBrewer();
			colorBrewer.init(pixel);
			File file = new File("C:\\Users\\jlibarr1\\Downloads\\test\\surnameFiles\\probabilistic\\"+firstChar+"\\image\\"+surname+"image.png");
			file.createNewFile();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			int[] scanline = new int[width];
			// we use a fake image for testing
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getSurnameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
					
			pixel = null;
			image.flush();
			ImageIO.write(image, "png", file);
	    	long end = System.nanoTime();
	    }

	    /*
	     * Draws a KDE map for a surname and outputs to a file.
	     */
	    public void drawKDEMapForename(String name) throws IOException{
	    	long start = System.nanoTime();
			width = rightBottomPixel.x - leftTopPixel.x;
			height = rightBottomPixel.y - leftTopPixel.y;
			
			GoogleMercator mercator = new GoogleMercator();
			mercator.setZoom(zoom);
			
			//Create a hashmap of the duplicate pixels
			HashMap<Point, Double> valueMap = createValueMapWithOverallSave(mercator);
			
			int pixelBandwidth = calculateBandwidth(mercator);

			//An array of the distinct pixels on the map.
			Point[] distinctPointArray = valueMap.keySet().toArray(new Point[valueMap.keySet().size()]);

			//Create an array of weighted distances for each distinct pixel
			//Each array row contains the weighted distance between another pixel
			double[][] distance = new double[distinctPointArray.length][distinctPointArray.length];
			
			weightDistances(valueMap, distinctPointArray, distance);
			
			valueMap = null;

			double sumWeight = distance[0][distance[0].length - 1];
			
			double[][] pixel = new double[height][width];
			for (int i = 0; i < height; i++) {
				Arrays.fill(pixel[i], 0);
			}

			//Color each pixel based on the weight and distance between other pixels
			//that were calculated above
			for (int i = 0; i < distance.length; i++) {
				double weight = distance[i][0];
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
			
			char firstChar = name.charAt(0);
			if(firstChar < 65 || firstChar > 90)
				firstChar = '1';
			
			
			distance = null;
			distinctPointArray=null;
			
			// weight to image
			ColorBrewer colorBrewer = new ColorBrewer();
			colorBrewer.init(pixel);
			File file = new File("C:\\Users\\jlibarr1\\Downloads\\test\\forenameFiles\\"+firstChar+"\\regularforename"+name+"w776h419z4clat37.10496353951387clng-97.07862899999998.png");
			file.createNewFile();
			BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			int[] scanline = new int[width];
			// we use a fake image for testing
			for (int i = 0; i < height; i++) {
				for (int k = 0; k < width; k++) {
					scanline[k] = colorBrewer.getForenameColor(pixel[i][k]);
				}
				image.setRGB(0, i, width, 1, scanline, 0, 0);
			}
					
			pixel = null;
			image.flush();
			ImageIO.write(image, "png", file);
	    	long end = System.nanoTime();
			
	    }

	    /*
	     * Creates a value map of the US overall and saves it.
	     * Currently does this for forenames.
	     */
	    public HashMap<Point, Double> createValueMapWithOverallSave(GoogleMercator mercator) throws IOException{
	    	//Convert the lat/longs from the inputted list to map points
			//then convert to pixels on the screen
			Vector<Point> pointVec = new Vector<Point>(sampleList.size());
			for (LatLng latlng : sampleList) {
				Point p = mercator.fromLatLngToPoint(latlng);
				Point q = new Point(p.x-leftTopPixel.x, p.y-leftTopPixel.y);
				pointVec.add(q);
			}
			
			InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\forenameValueMap.ser");
		    InputStream inBuffer = new BufferedInputStream(inFile);
		    ObjectInput input = new ObjectInputStream (inBuffer);
		    HashMap<Point, Double> savedMap = null;
		    try {
				savedMap = (HashMap<Point, Double>)input.readObject();
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
		    
		    input.close();
		    input = null;
		    inBuffer.close();
		    inBuffer = null;
		    inFile.close();
		    inFile = null;
		    		    
		    for (Point point : pointVec) {
				Double v = savedMap.get(point);
				Double newValue = null;
				if (v == null) {
					newValue = new Double(1);
				} else {
					newValue = new Double(v.doubleValue() + 1);
				}
				savedMap.put(point, newValue);
			}	    
			
			OutputStream outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Downloads\\forenameValueMap.ser");
			OutputStream outBuffer = new BufferedOutputStream(outFile);
		    ObjectOutput output = new ObjectOutputStream(outBuffer);
		    output.writeObject(savedMap);
		    
		    output.close();
		    output = null;
		    outBuffer.close();
		    outBuffer = null;
		    outFile.close();
		    outFile = null;
			
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
			return valueMap;
	    }

}