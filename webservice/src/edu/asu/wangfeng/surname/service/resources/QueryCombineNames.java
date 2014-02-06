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

import edu.asu.joseibarra.services.name.CombineMaps;
import edu.asu.joseibarra.services.name.QueryName;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryBean;
import edu.asu.wangfeng.surname.service.netbeans.QueryCombineMapsBean;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

@Path("/queryCombineNames")
public class QueryCombineNames extends WFQuery{
	private String imageDir1;
	private String imageDir2;
	private String resultDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir1 = context.getRealPath("image/kdecachesurname") + File.separatorChar;
		imageDir2 = context.getRealPath("image/kdecacheforename") + File.separatorChar;
		resultDir = context.getRealPath("image/kdecachecombined") + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding querySurname(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("map1Loc") @DefaultValue("blank.png") String map1Loc,
			@QueryParam("map2Loc") @DefaultValue("blank.png") String map2Loc
			) throws IOException{
		QueryCombineMapsBean result = new QueryCombineMapsBean();
		CombineMaps combine = new CombineMaps();
		File imageFile = combine.combineMapsIntoFile(map1Loc, map2Loc, imageDir1, imageDir2, resultDir);
		if(imageFile == null){
			result.setImage("blank.png");
			result.setNumber(0);
		}
		else{
			result.setImage(imageFile.getName());
			result.setNumber(0);
		}
		return new JSONWithPadding(result, callback);
	}
}