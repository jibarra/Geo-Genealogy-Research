package edu.asu.joseibarra.services.queryUtilities;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.naming.NamingException;

import edu.asu.joseibarra.utility.KDEPainterEfficient;
import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryBean;
import edu.asu.wangfeng.surname.service.resources.WFQuery;

public class QueryName extends WFQuery {
	private final int LOWER_NAME_LIMIT = 25;
	
	public QueryBean queryCreatedName(String name, String imageDir, String mapType) throws IOException{
		QueryBean result = new QueryBean();
		if(name.length() == 0){
			result.setImage("blank.png");
			result.setNumber(0);
			return result;
		}
		
		char firstChar = name.charAt(0);
		if(firstChar < 65 || firstChar > 90)
			firstChar = '1';
		
		File file = null;
		String imagePath = null;
		if(mapType.equals("regular"))
			imagePath = imageDir + "\\regular\\" + firstChar + "\\image\\" + name + "image.png";
		else
			imagePath = imageDir + "\\probabilistic\\" + firstChar + "\\image\\" + name + "image.png";
		file = new File(imagePath);
		if(file.exists()){
			result.setImage(imagePath);
			result.setNumber(0);
			return result;
		}
		else{
			result.setImage("blank.png");
			result.setNumber(0);
			return result;
		}
	}
	
	public QueryBean queryName(String name, LatLng sw, LatLng ne, LatLng center, int width,
			int height, int zoom, String nameType, String imageDir, String mapType, int sqlLimit) 
					throws IOException{
		
		QueryBean result = new QueryBean();
		if(name.length() == 0){
			result.setImage("blank.png");
			result.setNumber(0);
			return result;
		}
		String imagePath = null;
		File file = null;
		String baseString = mapType + nameType + name+"w"+width+"h"+height+"z"+zoom+"clat"+center.lat()+"clng"+center.lng();
		imagePath = baseString + ".png";
		file = new File(imageDir + imagePath);
		if(file.exists()){
			result.setImage(imagePath);
			result.setNumber(0);
			return result;
		}
		
		// prepare the data
		Vector<LatLng> coordinateVec = new Vector<LatLng>();
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "select latitude, longitude from phonebook where " + nameType + "=? and latitude between ? and ? and longitude between ? and ?";

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
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		if(coordinateVec.size() < LOWER_NAME_LIMIT){
			result.setImage("blank.png");
			result.setNumber(0);
			return result;
		}
				
		result.setImage(imagePath);
		result.setNumber(coordinateVec.size());
		if(coordinateVec.isEmpty())
		{
			result.setImage("blank.png");
			return result;
		}
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		LatLng topLeft = new LatLng(ne.lat(), sw.lng());
		LatLng bottomRight = new LatLng(sw.lat(), ne.lng());
		KDEPainterEfficient painter = new KDEPainterEfficient();
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(topLeft));
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(bottomRight));
		painter.setSampleList(coordinateVec);
		coordinateVec=null;
		painter.setFilename(imageDir + imagePath);
		painter.setZoom(zoom);
		
		System.out.println("here");
		
		if(zoom <= 4){
			painter.setBandwidth(240);
		}
		else if(zoom == 5){
			painter.setBandwidth(100);
		}
		else if(zoom > 5){
			painter.setBandwidth(50);
		}
		if(mapType.equals("regular")){
			if(nameType.equals("surname")){
				painter.drawKDEMap(1);
			}
			else if(nameType.equals("forename")){
				painter.drawKDEMap(2);
			}
			else{
				painter.drawKDEMap(3);
			}
		}
		else if(mapType.equals("probabilistic")){
			if(nameType.equals("surname")){
				painter.drawProbabilisticKDEMap(1);
			}
			else if(nameType.equals("forename")){
				painter.drawProbabilisticKDEMap(2);
			}
			else{
				painter.drawProbabilisticKDEMap(3);
			}
		}
		
		return result;
	}
}
