package edu.asu.joseibarra.utility;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import edu.asu.joseibarra.services.name.NameIncomeThread;
import edu.asu.joseibarra.services.name.NameMapBuilder;
import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.BuildResultBean;

public class CreateSmallMaps {

	String[] names = {"SHEA", "ROUSE", "IBARRA", "MACIEJEWSKI", "FENG", "JAUREGUI", "KIMES", "RANKIN", "WELLS", "MILLER", 
			"TRAN", "HAWORTH", "CHAPLIN", "WAITS", "VALENTINO", "TARR", "BUDD", "WALZ", "PIGG", "JOSLIN",
			"EDENS", "TOWER", "RAE", "SEYMORE", "ROHR", "WESTER", "BINKLEY", "WEIS", "BOLAND", "ENG",
			"LOGUE", "TILLER", "MAAS", "KASPER", "SPINKS", "MATHIAS", "JENSON", "PETTIS", "BUI", "FALLS",
			"WILKE", "TAPP", "SILVERS", "AUGUST", "MATTOS", "LEAK", "GIRON", "TEJEDA", "FERNANDEZ", "BACA",
			"ALONSO", "MADRID", "BURNS", "COTTON", "CLAYTON", "BRIDGES", "LYNCH", "CHUA", "YEH", "PEREZ",
			"FUNG", "CHOE", "YUN", "JIN", "FELDMAN", "HAGEN", "POLLARD", "COLEMAN", "WILLS", "POST",
			"SHELLY", "SACCO", "LUONG", "LUIS", "KNOTTS", "HEINRICH", "FOREST", "SETTLES", "REY", "NOVOTNY",
			"NEMETH", "SEGAL", "PHILIPS", "WOLCOTT", "EADS", "VOLZ", "SILAS", "CHIU", "CHINN", "CLEMENTE",
			"BOWLER", "HOFMAN", "SOPER", "RODMAN", "PEDIGO", "LUNN", "LOHR", "LAHR", "JURADO", "LILIENTHAL"
	};
	
	public void generateMaps() throws IOException {
		KDEPainterEfficient painter = new KDEPainterEfficient();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(2);
		LatLng sw = new LatLng(25.109438734497637, -125.58281249999999  );
		LatLng ne = new LatLng(49.79825950635239, -65.81718749999999);
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(25.109438734497637, -65.81718749999999)));
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(49.79825950635239, -125.58281249999999  )));
		painter.setZoom(2);
		painter.setBandwidth(240);
		
		//Center lat: 37.4675118303818, lng: -95.69999999999999  
		
		for(int i = 0; i < names.length; i++){
			Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			
			long start = System.nanoTime();
			try {
				connection = connectMySQLDatabase("phonebook","root", "password");
				//SQL LOCATED HERE
				sql = "select latitude, longitude from phonebook where surname=? and latitude between ? and ? and longitude between ? and ?";

				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);

				statement.setString(1, names[i]);
				statement.setDouble(2, sw.lat());
				statement.setDouble(3, ne.lat());
				statement.setDouble(4, sw.lng());
				statement.setDouble(5, ne.lng());
				System.out.println(statement);
				
				resultset = statement.executeQuery();
				while (resultset.next()) {
					double lat = resultset.getDouble(1);
					double lng = resultset.getDouble(2);
					coordinateVec.add(new LatLng(lat, lng));
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			painter.setSampleList(coordinateVec);
			System.out.println("Generating files for " + names[i] + "...");
			painter.setFilename("C:\\Users\\jlibarr1\\Desktop\\temp\\170x90 Data\\" + names[i] +".png");
			painter.drawKDEMap(names[i]);
		    
		    NameMapBuilder builder = new NameMapBuilder();
		    BuildResultBean result = builder.nameMapBuilder(new File ("C:\\Users\\jlibarr1\\Desktop\\temp\\170x90 Data\\" + names[i] +".png"), names[i] + "WithBG.png", 170, 90, 
		    		new LatLng(49.79825950635239, -125.58281249999999), 2, "C:\\Users\\jlibarr1\\Desktop\\temp\\170x90 Data\\", "");
		}
	}
	
	public void generateForenameSmallMaps(LinkedList<String> forenameList) throws IOException {
//		KDEPainterEfficient painter = new KDEPainterEfficient();
//		GoogleMercator mercator = new GoogleMercator();
//		mercator.setZoom(2);
//		LatLng sw = new LatLng(25.109438734497637, -125.58281249999999  );
//		LatLng ne = new LatLng(49.79825950635239, -65.81718749999999);
//		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(25.109438734497637, -65.81718749999999)));
//		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(49.79825950635239, -125.58281249999999  )));
//		painter.setZoom(2);
//		painter.setBandwidth(240);
//		
//		//Center lat: 37.4675118303818, lng: -95.69999999999999  
//		
//		while(!forenameList.isEmpty()){
//			String forename = forenameList.poll();
//			
//			if(forename.length() <= 1)
//				continue;
//			
//			Vector<LatLng> coordinateVec = new Vector<LatLng>();
//			Connection connection = null;
//			String sql;
//			PreparedStatement statement = null;
//			ResultSet resultset = null;
//			
//			long start = System.nanoTime();
//			try {
//				connection = connectMySQLDatabase("phonebook","root", "password");
//				//SQL LOCATED HERE
//				sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ?";
//
//				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//
//				statement.setString(1, forename);
//				statement.setDouble(2, sw.lat());
//				statement.setDouble(3, ne.lat());
//				statement.setDouble(4, sw.lng());
//				statement.setDouble(5, ne.lng());
//				System.out.println(statement);
//				
//				resultset = statement.executeQuery();
//				while (resultset.next()) {
//					double lat = resultset.getDouble(1);
//					double lng = resultset.getDouble(2);
//					coordinateVec.add(new LatLng(lat, lng));
//				}
//				resultset.close();
//				statement.close();
//				connection.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			painter.setSampleList(coordinateVec);
//			System.out.println("Generating files for " + forename + "...");
////			painter.setFilename("C:\\Users\\jlibarr1\\Downloads\\test\\170x90 Data\\" + forename +".png");
//			painter.drawKDEMap(forename);
//		    
////		    NameMapBuilder builder = new NameMapBuilder();
////		    BuildResultBean result = builder.nameMapBuilder(new File ("C:\\Users\\jlibarr1\\Desktop\\temp\\170x90 Data\\" + names[i] +".png"), names[i] + "WithBG.png", 170, 90, 
////		    		new LatLng(49.79825950635239, -125.58281249999999), 2, "C:\\Users\\jlibarr1\\Desktop\\temp\\170x90 Data\\", "");
//		}
		
		
		System.out.println("Creating threads...");
		String cur = forenameList.poll();
		while(!cur.equals("IVY")){
			cur = forenameList.poll();
		}
		
		CreateSmallMapsThread[] threads = new CreateSmallMapsThread[6];
	    for(int i = 0; i < threads.length; i++){
	    	System.out.println("Thread created");
	    	threads[i] = new CreateSmallMapsThread(forenameList);
	    }
	    
	    for(int i = 0; i < threads.length; i++){
	    	System.out.println("Thread started");
	    	threads[i].start();
	    }
	    
//	    for(int i = 0; i < threads.length; i++){
//	    	while(threads[i].isAlive()){
//	    		try {
//					Thread.sleep(600);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//	    	}
//	    	System.out.println("Thread ended");
//	    }
	}
	
	public void generateAllMaps() throws IOException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listNames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    LinkedList<String> savedList = null;
	    try {
	    	savedList = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    KDEPainterEfficient painter = new KDEPainterEfficient();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(4);
		LatLng sw = new LatLng(22.75592037564069, -131.18019149999998  );
		LatLng ne = new LatLng(51.454006703387115, -62.97706649999998);
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(sw.lat(), ne.lng())));
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(ne.lat(), sw.lng())));
		painter.setZoom(4);
		painter.setBandwidth(240);
		
		String cur = null;
//		do{
//			cur = savedList.poll();
//		}while(!cur.equals("JOHNSHOY"));
		
	    
	    while(!savedList.isEmpty()){
	    	String name = savedList.poll();
	    	char firstChar = name.charAt(0);
	    	if(name.contains("/")){
	    		continue;
	    	}
	    	System.out.println("Generating map for " + name);
	    	Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			
			long start = System.nanoTime();
			try {
				connection = connectMySQLDatabase("phonebook","root", "password");
				//SQL LOCATED HERE
				sql = "select latitude, longitude from phonebook where surname=? and latitude between ? and ? and longitude between ? and ?";

				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);

				statement.setString(1, name);
				statement.setDouble(2, sw.lat());
				statement.setDouble(3, ne.lat());
				statement.setDouble(4, sw.lng());
				statement.setDouble(5, ne.lng());
				
				resultset = statement.executeQuery();
				while (resultset.next()) {
					double lat = resultset.getDouble(1);
					double lng = resultset.getDouble(2);
					coordinateVec.add(new LatLng(lat, lng));
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			if(coordinateVec.size() >= 25){
				if(firstChar < 65 || firstChar > 90)
					firstChar = '1';
				painter.setSampleList(coordinateVec);
				System.out.println("Generating files for " + name + "...");

				painter.drawProbabilisticKDEMap(name);
			}
			
	    }
	}
	
	public void generateAllForenameMaps() throws IOException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listForenames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    LinkedList<String> savedList = null;
	    try {
	    	savedList = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    
	    System.out.println("Creating threads...");
	    
	    GenerateForenameMapsThread[] threads = new GenerateForenameMapsThread[6];
	    for(int i = 0; i < threads.length; i++){
	    	threads[i] = new GenerateForenameMapsThread(savedList);
	    }
	    
	    System.out.println("Starting threads...");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i].start();
	    }
	    
//	    KDEPainterEfficient painter = new KDEPainterEfficient();
//		GoogleMercator mercator = new GoogleMercator();
//		mercator.setZoom(4);
//		LatLng sw = new LatLng(22.75592037564069, -131.18019149999998  );
//		LatLng ne = new LatLng(51.454006703387115, -62.97706649999998);
//		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(sw.lat(), ne.lng())));
//		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(ne.lat(), sw.lng())));
//		painter.setZoom(4);
//		painter.setBandwidth(240);		
//	    
//	    while(!savedList.isEmpty()){
//	    	String name = savedList.poll();
//	    	char firstChar = name.charAt(0);
//	    	if(name.contains("/")){
//	    		continue;
//	    	}
//	    	System.out.println("Generating map for " + name);
//	    	Vector<LatLng> coordinateVec = new Vector<LatLng>();
//			Connection connection = null;
//			String sql;
//			PreparedStatement statement = null;
//			ResultSet resultset = null;
//			
//			long start = System.nanoTime();
//			try {
//				connection = connectMySQLDatabase("phonebook","root", "password");
//				//SQL LOCATED HERE
//				sql = "select latitude, longitude from phonebook where surname=? and latitude between ? and ? and longitude between ? and ?";
//
//				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//
//				statement.setString(1, name);
//				statement.setDouble(2, sw.lat());
//				statement.setDouble(3, ne.lat());
//				statement.setDouble(4, sw.lng());
//				statement.setDouble(5, ne.lng());
//				
//				resultset = statement.executeQuery();
//				while (resultset.next()) {
//					double lat = resultset.getDouble(1);
//					double lng = resultset.getDouble(2);
//					coordinateVec.add(new LatLng(lat, lng));
//				}
//				resultset.close();
//				statement.close();
//				connection.close();
//			} catch (SQLException e) {
//				e.printStackTrace();
//			}
//			if(coordinateVec.size() >= 25){
//				if(firstChar < 65 || firstChar > 90)
//					firstChar = '1';
//				painter.setSampleList(coordinateVec);
//				System.out.println("Generating files for " + name + "...");
//
//				painter.drawKDEMap(name);
//			}
//			
//	    }
	}
	
	private LinkedList<String> get1000Names() throws IOException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listForenames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    LinkedList<String> savedNames = null;
	    try {
	    	savedNames = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    
	    inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listNumAvailForenames25.ser");
	    inBuffer = new BufferedInputStream(inFile);
	    input = new ObjectInputStream (inBuffer);
	    
	    LinkedList<Integer> savedNum = null;
	    try {
	    	savedNum = (LinkedList<Integer>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    
	    LinkedList<String> thousandList = new LinkedList<String>();
	    
	    while(!savedNum.isEmpty()){
	    	int num = savedNum.poll();
	    	if(num >= 1000)
	    		thousandList.add(savedNames.poll());
	    	else
	    		savedNames.poll();
	    }
	    
	    return thousandList;
	}
	
	private static Connection connectMySQLDatabase(String localdb,
			String username, String password) {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/"
					+ localdb, username, password);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return connection;
	}

	public static void main(String[] args) throws IOException {
	    HashMap<Point, Double> valueMap = new HashMap<Point, Double>();
	    OutputStream outFile = new FileOutputStream("C:\\Users\\jlibarr1\\Downloads\\forenameValueMap.ser");
		OutputStream outBuffer = new BufferedOutputStream(outFile);
	    ObjectOutput output = new ObjectOutputStream(outBuffer);
	    output.writeObject(valueMap);
	    
	    output.close();
	    output = null;
	    outBuffer.close();
	    outBuffer = null;
	    outFile.close();
	    outFile = null;
		CreateSmallMaps test = new CreateSmallMaps();
		test.generateAllForenameMaps();
//		test.generateAllMaps();
	}

}
