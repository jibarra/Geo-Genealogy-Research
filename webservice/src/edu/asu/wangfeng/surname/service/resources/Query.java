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

@Path("/query")
public class Query extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecache") + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding query(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
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
		if(surname.length() == 0){
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
//					file.createNewFile();
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
		
		long tstart = System.currentTimeMillis();
		
		try {
			connection = connectDatabase();
			sql = "select latitude, longitude from phonebook where surname=? and latitude between ? and ? and longitude between ? and ?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, surname);
			statement.setDouble(2, sw.x);
			statement.setDouble(3, ne.x);
			statement.setDouble(4, sw.y);
			statement.setDouble(5, ne.y);
			
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
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long tend = System.currentTimeMillis();
		System.out.println("Elapsed time for query: " + (tend-tstart) / 1000.0);
		
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
		painter.setFilename(imageDir + imagePath);
		painter.setZoom(zoom);
		painter.setBandwidth(240);
		System.out.println("Drawing KDE");
		painter.drawKDEMap();
		return new JSONWithPadding(result, callback);
	}
}