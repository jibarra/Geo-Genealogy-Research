package edu.asu.joseibarra.scripts.threads;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Vector;

import edu.asu.joseibarra.geo.GoogleMercator;
import edu.asu.joseibarra.geo.KDEPainterEfficient;
import edu.asu.joseibarra.geo.LatLng;

public class CreateSmallMapsThread extends Thread {

	private LinkedList<String> names;

	public CreateSmallMapsThread(LinkedList<String> names) {
		this.names = names;
	}

	public void run(){
		generateForenameSmallMaps(names);
	}
	
	public synchronized String getName(LinkedList<String> list){
		if(list.isEmpty())
			return null;
		return list.poll();
	}
	
	public void generateForenameSmallMaps(LinkedList<String> forenameList) {
		KDEPainterEfficient painter = new KDEPainterEfficient();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(2);
		LatLng sw = new LatLng(25.109438734497637, -125.58281249999999  );
		LatLng ne = new LatLng(49.79825950635239, -65.81718749999999);
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(25.109438734497637, -65.81718749999999)));
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(49.79825950635239, -125.58281249999999  )));
		painter.setZoom(2);
		painter.setBandwidth(240);
		
		while(!forenameList.isEmpty()){
			String forename = forenameList.poll();
			
			if(forename == null)
				continue;
			
			if(forename.length() <= 1)
				continue;
			
			Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			
			long start = System.nanoTime();
			try {
				connection = connectMySQLDatabase("phonebook","root", "password");
				//SQL LOCATED HERE
				sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ?";

				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);

				statement.setString(1, forename);
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
			System.out.println("Generating files for " + forename + "...");
			try {
				painter.drawKDEMap(forename);
			} catch (IOException e) {
				e.printStackTrace();
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
