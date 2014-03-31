/* Modified by Jose Ibarra
 * Combined surname services into one file for cleaner
 * code. Surname services can now be found at /surname/SERVICE_NAME/
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
import edu.asu.joseibarra.name.utility.QueryNameIncome;
import edu.asu.wangfeng.service.netbeans.QueryBean;
import edu.asu.wangfeng.service.netbeans.QueryNameIncomeBean;

@Path("/surname")
public class SurnameService {
	
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("") + File.separatorChar + "resources" + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	@Path("/queryIncome")
	public JSONWithPadding queryIncome(
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("callback") @DefaultValue("callback") String callback
			){
		QueryNameIncome query = new QueryNameIncome();
		QueryNameIncomeBean result = query.queryNameIncome(surname, "surname", new LatLng(latsw, lngsw), new LatLng(latne, lngne));
		return new JSONWithPadding(result, callback);
	}
	
	@GET
	@Produces("application/x-javascript")
	@Path("/queryMapRegular")
	public JSONWithPadding queryMapRegular(
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
		QueryName query = new QueryName();
		QueryBean result = query.queryCreatedName(surname, imageDir, "regular");
//		QueryBean result = query.queryName(surname, new LatLng(latsw, lngsw), new LatLng(latne, lngne), 
//				new LatLng(latcenter, lngcenter), width, height, zoom, "surname", imageDir, "regular", -1);
		return new JSONWithPadding(result, callback);
	}
	
	@GET
	@Produces("application/x-javascript")
	@Path("/queryMapProbabilistic")
	public JSONWithPadding queryMapProbabilistic(
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
		QueryName query = new QueryName();
		QueryBean result = query.queryCreatedName(surname, imageDir, "probabilistic");
//		QueryBean result = query.queryName(surname, new LatLng(latsw, lngsw), new LatLng(latne, lngne), 
//				new LatLng(latcenter, lngcenter), width, height, zoom, "surname", imageDir, "probabilistic", -1);
		return new JSONWithPadding(result, callback);
	}
}
