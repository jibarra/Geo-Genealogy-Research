package edu.asu.joseibarra.utility;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Vector;

import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;

public class GenerateForenameMapsThread extends Thread {

	public LinkedList<String> names;
	
	public GenerateForenameMapsThread(LinkedList<String> names) {
		this.names = names;
	}
	
	public synchronized String getListName(LinkedList<String> list){
		if(list.isEmpty())
			return null;
		return list.poll();
	}
	
	public void run(){
		
	}
	
	public void generateForenames(){
		KDEPainterEfficient painter = new KDEPainterEfficient();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(4);
		LatLng sw = new LatLng(22.75592037564069, -131.18019149999998  );
		LatLng ne = new LatLng(51.454006703387115, -62.97706649999998);
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(sw.lat(), ne.lng())));
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(ne.lat(), sw.lng())));
		painter.setZoom(4);
		painter.setBandwidth(240);		
		
		while(!names.isEmpty()){
	    	String name = getListName(names);
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

				try {
					painter.drawKDEMapForename(name);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
	    }
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
}
