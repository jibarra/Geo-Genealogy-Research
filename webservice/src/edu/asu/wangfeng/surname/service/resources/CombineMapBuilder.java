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

import edu.asu.joseibarra.services.name.CombineMaps;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.BuildResultBean;

@Path("/combinebuildmap")
public class CombineMapBuilder extends WFQuery {
	private String imageDir1;
	private String imageDir2;
	private String resultDir;

	@Context
	public void setServletContext(ServletContext context) {
		imageDir1 = context.getRealPath("image/kdecachesurname") + File.separatorChar;
		imageDir2 = context.getRealPath("image/kdecacheforename") + File.separatorChar;
		resultDir = context.getRealPath("image/uploadCombined") + File.separatorChar;
	}

	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding query(@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("map1Loc") @DefaultValue("blank.png") String map1Loc,
			@QueryParam("map2Loc") @DefaultValue("blank.png") String map2Loc,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("zoom") @DefaultValue("5") int zoom) throws IOException {
		
		BuildResultBean result = new BuildResultBean();
		
		if(map1Loc.length() <= 0 || map2Loc.length() <= 0){
			result.setFilename("blank.png");
			result.setUrl(resultDir);
			return new JSONWithPadding(result, callback);
		}
		
		CombineMaps combine = new CombineMaps();
		result = combine.combineMapsCapture(map1Loc, map2Loc, imageDir1, imageDir2, zoom, new LatLng(latne, lngsw), resultDir);
		return new JSONWithPadding(result, callback);
	}
}