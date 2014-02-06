package edu.asu.wangfeng.surname.service.resources;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.joseibarra.services.name.QueryName;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryFullNameBean;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

@Path("/queryFullName")
public class QueryFullName extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecache") + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding queryFullName(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
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
		
		QueryName query = new QueryName();
		QueryFullNameBean result = new QueryFullNameBean();
//		QueryFullNameBean result = query.queryFullName(surname, forename, new LatLng(latsw, lngsw), 
//				new LatLng(latne, lngne), new LatLng(latcenter, lngcenter), width, height, zoom, imageDir,
//				"STRING", -1);
		return new JSONWithPadding(result, callback);
	}
}