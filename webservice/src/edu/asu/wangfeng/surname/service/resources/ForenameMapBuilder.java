package edu.asu.wangfeng.surname.service.resources;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

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

import edu.asu.joseibarra.services.name.NameMapBuilder;
import edu.asu.joseibarra.services.name.QueryName;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.BuildResultBean;

@Path("/forenamebuildmap")
public class ForenameMapBuilder extends WFQuery {
	private String imageDir;
	private String resultDir;
	private final String resultFolder = "uploadForename";

	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecacheforename") + File.separatorChar;
		resultDir = context.getRealPath("image/" + resultFolder) + File.separatorChar;
	}

	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding query(@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("name") @DefaultValue("") String forename,
			@QueryParam("image") @DefaultValue("blank.png") String imageFilename,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("latcenter") @DefaultValue("0.0") double latcenter,
			@QueryParam("lngcenter") @DefaultValue("0.0") double lngcenter,
			@QueryParam("width") @DefaultValue("0") int width, @QueryParam("height") @DefaultValue("0") int height,
			@QueryParam("zoom_level") @DefaultValue("5") int zoom) throws IOException {
		
		File imageFile = new File(imageDir + imageFilename);
		
		if (!imageFile.exists()) {
			QueryName query = new QueryName();
			LatLng sw = new LatLng(latsw, lngsw);
			LatLng ne = new LatLng(latne, lngne);
			LatLng center = new LatLng(latcenter, lngcenter);
			query.queryName(forename, sw, ne, center, width, height, zoom, "forename", imageFilename, "regular", -1);
		}

		NameMapBuilder builder = new NameMapBuilder();
		BuildResultBean result = builder.nameMapBuilder(imageFile, imageFilename, width, height, 
				new LatLng(latne, lngsw), zoom, resultDir, resultFolder);
		return new JSONWithPadding(result, callback);
	}
}