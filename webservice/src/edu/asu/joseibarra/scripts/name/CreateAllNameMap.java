package edu.asu.joseibarra.scripts.name;

import java.awt.Point;
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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.naming.NamingException;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.joseibarra.geo.GoogleMercator;
import edu.asu.joseibarra.geo.KDEPainterEfficient;
import edu.asu.joseibarra.geo.LatLng;
import edu.asu.joseibarra.name.utility.NameMapBuilder;
import edu.asu.joseibarra.name.utility.QueryName;
import edu.asu.joseibarra.services.WFQuery;
import edu.asu.wangfeng.service.netbeans.BuildResultBean;

public class CreateAllNameMap extends WFQuery{
	
	public static void main (String[] args) throws IOException{
//	    HashMap<Point, Double> valueMap = new HashMap<Point, Double>();
//	    OutputStream outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\valueMap.ser");
//		OutputStream outBuffer = new BufferedOutputStream(outFile);
//	    ObjectOutput output = new ObjectOutputStream(outBuffer);
//	    output.writeObject(valueMap);
//	    
//	    output.close();
//	    output = null;
//	    outBuffer.close();
//	    outBuffer = null;
//	    outFile.close();
//	    outFile = null;
		CreateAllNameMap allNames = new CreateAllNameMap();
		allNames.queryName("*", new LatLng(22.75592037564069, -131.83937118749998), new LatLng(51.454006703387115, -62.31788681249998), 
				new LatLng(37.1049635395139, -97.73780868749998), 776, 419, 4, "surname", "C:\\Users\\jlibarr1\\Desktop\\temp", -1);
//		allNames.createHeatMap();
	}
	
	public void createHeatMap() throws IOException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\valueMap.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    HashMap<Point, Double> savedMap = null;
	    try {
			savedMap = (HashMap<Point, Double>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
		KDEPainterEfficient painter = new KDEPainterEfficient();
		painter.setLeftTopPixel(new Point(555, 1363));
		painter.setRightBottomPixel(new Point(1331, 1782));
		painter.setZoom(4);
		painter.setBandwidth(240);
		painter.setFilename("C:\\Users\\jlibarr1\\Desktop\\test.png");
		painter.drawKDEMapWithHash(savedMap);
		
		File imageFile = new File("C:\\Users\\jlibarr1\\Desktop\\test.png");
		
		if (!imageFile.exists()) {
			QueryName query = new QueryName();
			LatLng sw = new LatLng(-22.75592037564069, -131.83937118749998);
			LatLng ne = new LatLng(51.454006703387115, -62.31788681249998);
			LatLng center = new LatLng(37.1049635395139, -97.73780868749998);
		}

		NameMapBuilder builder = new NameMapBuilder();
		BuildResultBean result = builder.nameMapBuilder(imageFile, "test.png", 776, 419, 
				new LatLng(51.454006703387115, -131.83937118749998), 4, "C:\\Users\\jlibarr1\\Desktop\\", "");
	}
	
	public void createDistinctMap(String name, LatLng sw, LatLng ne, LatLng center, int width,
			int height, int zoom, String nameType, String imageDir, int sqlLimit) throws IOException{
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		Point topLeft = mercator.fromLatLngToPoint(new LatLng(ne.lat(), sw.lng()));
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\valueMap.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    HashMap<Point, Double> savedMap = null;
	    try {
			savedMap = (HashMap<Point, Double>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    Point[] keyArray = savedMap.keySet().toArray(new Point[savedMap.keySet().size()]);
	    double size = 0;
	    for (Point point : keyArray) {
			System.out.println(savedMap.get(point));
			size += savedMap.get(point);
		}
	    System.out.println("Total size: " + size);
	    System.out.println("Key array size: " + keyArray.length);
	    
	    int[][] pixel = new int[height][width];
		for (int i = 0; i < height; i++) {
			Arrays.fill(pixel[i], 0);
		}
	    
	    for(Point point : keyArray){
	    	if(!(point.y >= height) && !(point.x >= width) && !(point.x < 0) && !(point.y < 0)){
	    		pixel[point.y][point.x] =  0xFFB10026;
	    	}
	    	
	    }
	    
	    File file = new File("C:\\Users\\jlibarr1\\Desktop\\img.png");
		file.createNewFile();
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		int[] scanline = new int[width];
		
		for (int i = 0; i < height; i++) {
			for (int k = 0; k < width; k++) {
				scanline[k] = pixel[i][k];
			}
			image.setRGB(0, i, width, 1, scanline, 0, 0);
		}
		
		pixel = null;
		image.flush();
		ImageIO.write(image, "png", file);
	}
	
	public void queryName(String name, LatLng sw, LatLng ne, LatLng center, int width,
			int height, int zoom, String nameType, String imageDir, int sqlLimit) 
					throws IOException{
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		Point topLeft = mercator.fromLatLngToPoint(new LatLng(ne.lat(), sw.lng()));
//		Point bottomRight = mercator.fromLatLngToPoint(new LatLng(sw.lat(), ne.lng()));		

		// prepare the data
		
		char currentChar = 'a';
		char lastChar = 'r';
		char additional = '1';
		doQuery(currentChar, lastChar, additional, mercator, topLeft);
		additional = 's';
		currentChar = 'a';
		lastChar = 'z';
		doQuery(currentChar, lastChar, additional, mercator, topLeft);
		additional = '1';
		currentChar = 't';
		lastChar = 'z';
		doQuery(currentChar, lastChar, additional, mercator, topLeft);
	}
	
	public void doQuery(char currentChar, char lastChar, char addChar, GoogleMercator mercator, Point topLeft) throws IOException{
		do{
			Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			try {
				connection = connectDatabase();
				//SQL LOCATED HERE
				
				sql = "select latitude, longitude from phonebook where surname LIKE ?";
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				if(addChar == 's'){
					statement.setString(1, "" + addChar + "" + currentChar + "" + "%");
				}
				else{
					statement.setString(1,  currentChar + "%");
				}
				System.out.println(statement);
				resultset = statement.executeQuery();
				while (resultset.next()) {
					double lat = resultset.getDouble(1);
					double lng = resultset.getDouble(2);
					try{
						coordinateVec.add(new LatLng(lat, lng));
					}
					catch(java.lang.NullPointerException ex){
						System.out.println(ex);
						System.out.println(lat);
						System.out.println(lng);
					}
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			Vector<Point> pointVec = new Vector<Point>(coordinateVec.size());
			System.out.println(coordinateVec.size());
			for (LatLng latlng : coordinateVec) {
				Point p = mercator.fromLatLngToPoint(latlng);
				Point q = new Point(p.x-topLeft.x, p.y-topLeft.y);
				pointVec.add(q);
			}
			coordinateVec=null;
			
			
			InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Desktop\\temp\\valueMap.ser");
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
		    
		    pointVec = null;
		    
			
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
		    
		    currentChar++;
		}while(currentChar <= lastChar);
	}
}
