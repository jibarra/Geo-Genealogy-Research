package edu.asu.wangfeng.surname.service.resources;

import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.Vector;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.fengwang.visualization.KDEPainter;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryBean;
import edu.asu.wangfeng.geo.GoogleMercator;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

@Path("/queryForename")
public class QueryForename extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecache") + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding queryForename(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("forename") @DefaultValue("") String forename,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("latcenter") @DefaultValue("0.0") double latcenter,
			@QueryParam("lngcenter") @DefaultValue("0.0") double lngcenter,
			@QueryParam("width") @DefaultValue("0") int width,
			@QueryParam("height") @DefaultValue("0") int height,
			@QueryParam("zoom_level") @DefaultValue("5") int zoom
			) throws IOException{
		
		QueryBean result = new QueryBean();
		if(forename.length() == 0){
			result.setImage("blank.png");
			result.setNumber(0);
			return new JSONWithPadding(result, callback);
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
		Point2D.Double sw = new Point2D.Double(latsw, lngsw);
		Point2D.Double ne = new Point2D.Double(latne, lngne);
		// prepare the data
		Vector<LatLng> coordinateVec = new Vector<LatLng>();
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();
			// no limit sql
//			sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ?";
			// 25,000 limit ~5-10 seconds
			sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ? LIMIT 25000";
			// 50,000 limit ~10-20 seconds
//			sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ? LIMIT 50000";
			// 100,000 limit ~30-45 seconds at max
//			sql = "select latitude, longitude from phonebook where forename=? and latitude between ? and ? and longitude between ? and ? LIMIT 100000";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, forename);
			statement.setDouble(2, sw.x);
			statement.setDouble(3, ne.x);
			statement.setDouble(4, sw.y);
			statement.setDouble(5, ne.y);
						
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
			return new JSONWithPadding(result, callback);
		}
				
		result.setImage(imagePath);
		result.setNumber(coordinateVec.size());
		if(coordinateVec.isEmpty())
		{
			result.setImage("blank.png");
			return new JSONWithPadding(result, callback);
		}
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		LatLng topLeft = new LatLng(latne, lngsw);
		LatLng bottomRight = new LatLng(latsw, lngne);
		KDEPainter painter = new KDEPainter();
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(topLeft));
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(bottomRight));
		painter.setSampleList(coordinateVec);
		coordinateVec=null;
		painter.setFilename(imageDir + imagePath);
		painter.setZoom(zoom);
		painter.setBandwidth(240);
		painter.drawKDEMap();
		queryIncome(forename, result);
		return new JSONWithPadding(result, callback);
	}
	
	public void queryIncome(String forename, QueryBean result){
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double averageMean = -1;
		double averageMedian = -1;
		try {
			connection = connectDatabase();
			sql = "select average_mean, average_median from average_income_forename where forename=?";
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
	
}