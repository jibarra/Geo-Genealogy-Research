/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * � Arizona State University 2014
 * 
 * This class is the public exposure of the forename query.
 * This service needs to be updated to match the surname service,
 * as it is behind what the surname service actually does.
 * The only current method returns a map of the inputted surname.
 */

package edu.asu.joseibarra.services;

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

import edu.asu.joseibarra.geo.LatLng;
import edu.asu.joseibarra.name.utility.QueryName;
import edu.asu.wangfeng.service.netbeans.QueryBean;



@Path("/queryForename")
public class QueryForename extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecacheforename") + File.separatorChar;
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
		QueryName query = new QueryName();
		QueryBean result = query.queryName(forename, new LatLng(latsw, lngsw), new LatLng(latne, lngne), new LatLng(latcenter, lngcenter), 
				width, height, zoom, "forename", imageDir, "regular", -1);
		return new JSONWithPadding(result, callback);
	}
}