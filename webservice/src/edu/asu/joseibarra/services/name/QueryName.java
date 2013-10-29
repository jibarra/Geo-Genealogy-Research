package edu.asu.joseibarra.services.name;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.naming.NamingException;

import edu.asu.fengwang.visualization.KDEPainter;
import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryBean;
import edu.asu.wangfeng.surname.service.netbeans.QueryFullNameBean;
import edu.asu.wangfeng.surname.service.resources.WFQuery;

public class QueryName extends WFQuery {
	//SQL LIMIT HERE
	public final int SQL_LIMIT = 25000;
	
	public QueryBean queryName(String name, LatLng sw, LatLng ne, LatLng center, int width,
			int height, int zoom, String nameType, String imageDir, int sqlLimit) throws IOException{
		
		QueryBean result = new QueryBean();
		if(name.length() == 0){
			result.setImage("blank.png");
			result.setNumber(0);
			return result;
		}
		String imagePath = null;
		File file = null;
		synchronized (this) {
			Date date = new Date();
			String baseString = date.getTime() + "-";
			// we only allow 1000 concurrency
			for (int i = 0; i < 1000; i++) {
				imagePath = baseString + i + ".png";
				file = new File(imageDir + imagePath);
				if (!file.exists()) {
					break;
				}
			}
		}
		// prepare the data
		Vector<LatLng> coordinateVec = new Vector<LatLng>();
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		if(sqlLimit <= 0){
			sqlLimit = SQL_LIMIT;
		}
		
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "select latitude, longitude from phonebook where " + nameType + "=? and latitude between ? and ? and longitude between ? and ? LIMIT " + sqlLimit;
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
		
		if(coordinateVec.size() < 100){
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
		KDEPainter painter = new KDEPainter();
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(topLeft));
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(bottomRight));
		painter.setSampleList(coordinateVec);
		coordinateVec=null;
		painter.setFilename(imageDir + imagePath);
		painter.setZoom(zoom);
		painter.setBandwidth(240);
		if(nameType.equals("surname")){
			painter.drawKDEMap(1);
		}
		else if(nameType.equals("forename")){
			painter.drawKDEMap(2);
		}
		else{
			painter.drawKDEMap(3);
		}
		//CAN BE COMMENTED OUT TO IGNORE INCOME
		queryIncome(name, nameType, result);
		//If the above is commented out, uncomment the next two commented lines:
//		result.setAverageMean(0);
//		result.setAverageMedian(0);
		return result;
	}
	
	public void queryIncome(String forename, String nameType, QueryBean result){
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double averageMean = -1;
		double averageMedian = -1;
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "select average_mean, average_median from average_income_" + nameType + " where " + nameType 
					+ "=?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, forename);
						
			resultset = statement.executeQuery();
			while (resultset.next()) {
				averageMean = resultset.getDouble(1);
				averageMedian = resultset.getDouble(2);
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		result.setAverageMean(averageMean);
		result.setAverageMedian(averageMedian);
	}
	
	public QueryFullNameBean queryFullName(String surname, String forename, LatLng sw, LatLng ne, 
			LatLng center, int width, int height, int zoom, String imageDir, int sqlLimit) throws IOException{
		QueryFullNameBean result = new QueryFullNameBean();
		QueryBean queryResult = queryName(surname, sw, ne, center, width, height, zoom, "surname", 
				imageDir, sqlLimit);
		result.setSurnameImage(queryResult.getImage());
		result.setSurnameNumber(queryResult.getNumber());
		result.setSurnameAverageMean(queryResult.getAverageMean());
		result.setSurnameAverageMedian(queryResult.getAverageMedian());
		queryResult = queryName(forename, sw, ne, center, width, height, zoom, "forename", 
				imageDir, sqlLimit);
		result.setForenameImage(queryResult.getImage());
		result.setForenameNumber(queryResult.getNumber());
		result.setForenameAverageMean(queryResult.getAverageMean());
		result.setForenameAverageMedian(queryResult.getAverageMedian());
		return result;
	}
}
