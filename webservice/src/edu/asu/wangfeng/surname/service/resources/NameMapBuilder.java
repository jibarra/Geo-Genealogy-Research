package edu.asu.wangfeng.surname.service.resources;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.imageio.ImageIO;
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
import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.google.staticmaps.GoogleStaticMapsGenerator;
import edu.asu.wangfeng.surname.service.netbeans.BuildResultBean;

@Path("/buildmap")
public class NameMapBuilder extends WFQuery {
	private String imageDir;
	private String resultDir;

	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecache") + File.separatorChar;
		resultDir = context.getRealPath("image/upload") + File.separatorChar;
	}

	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding query(@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("image") @DefaultValue("blank.png") String imageFilename,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("latcenter") @DefaultValue("0.0") double latcenter,
			@QueryParam("lngcenter") @DefaultValue("0.0") double lngcenter,
			@QueryParam("width") @DefaultValue("0") int width, @QueryParam("height") @DefaultValue("0") int height,
			@QueryParam("zoom_level") @DefaultValue("5") int zoom) throws IOException {
		BuildResultBean result = new BuildResultBean();
		File imageFile = new File(imageDir + imageFilename);
		Point2D.Double sw = new Point2D.Double(lngsw, latsw);
		Point2D.Double ne = new Point2D.Double(lngne, latne);
		if (!imageFile.exists()) {
			// deleted? oops, we have to draw another one...			
			// prepare the data
			Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			
			
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
			
			GoogleMercator mercator = new GoogleMercator();
			mercator.setZoom(zoom);
			LatLng topLeft = new LatLng(latne, lngsw);
			LatLng bottomRight = new LatLng(latsw, lngne);
			KDEPainter painter = new KDEPainter();
			painter.setLeftTopPixel(mercator.fromLatLngToPoint(topLeft));
			painter.setRightBottomPixel(mercator.fromLatLngToPoint(bottomRight));
			painter.setSampleList(coordinateVec);
			painter.setFilename(imageDir + imageFilename);
			painter.setZoom(zoom);
			painter.setBandwidth(240);
			painter.drawKDEMap();
			imageFile = new File(imageDir + imageFilename);
		}
		BufferedImage kdeImage = ImageIO.read(imageFile);
		// build base map
		int xnum = width / 640;
		int ynum = height / 640;
		if (width % 640 != 0) {
			xnum++;
		}
		if (height % 640 != 0) {
			ynum++;
		}
		int unitWidth = width / xnum;
		int unitHeight = height / ynum;
		GoogleStaticMapsGenerator mapGenerator = new GoogleStaticMapsGenerator();
		mapGenerator.setZoom(zoom);
		BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = resultImage.createGraphics();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		Point topLeft = mercator.fromLatLngToPoint(new LatLng(latne, lngsw));
		for (int i = 0; i < xnum; i++) {
			int innerWidth = unitWidth;
			if (i == (xnum - 1)) {
				innerWidth += (width - xnum * unitWidth);
			}
			for (int k = 0; k < ynum; k++) {
				int innerHeight = unitHeight;
				if (k == (ynum - 1)) {
					innerHeight += (height - ynum * unitHeight);
				}
				// get center lat/lng for current tile
				Point curCenter = new Point();
				curCenter.x = (unitWidth * i + innerWidth / 2) + topLeft.x;
				curCenter.y = (unitHeight * k + innerHeight / 2) + topLeft.y;
				LatLng latlng = mercator.fromPointToLatLng(curCenter);
				Point2D.Double latlngCenter = new Point2D.Double(latlng.lng(), latlng.lat());
				mapGenerator.setCenter(latlngCenter);
				mapGenerator.setHeight(innerHeight);
				mapGenerator.setWidth(innerWidth);
				java.net.URL tileURL = mapGenerator.generateURL();
				BufferedImage tileImage = ImageIO.read(tileURL);
				graphics.drawImage(tileImage, unitWidth * i, unitHeight * k, null);
			}
		}
		// overlay the image
		float[] scales = {1f, 1f, 1f, 0.4f};
		float[] offsets = {0, 0, 0, 0};
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		graphics.drawImage(kdeImage, rop, 0, 0);
		String resultFilename = resultDir +  imageFilename;
		ImageIO.write(resultImage, "png", new File(resultFilename));
		result.setFilename(imageFilename);
		result.setUrl("image/upload/" + imageFilename);
		return new JSONWithPadding(result, callback);
	}
}