package edu.asu.wangfeng.surname.service.resources;

import java.io.File;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryNameIncomeBean;
import edu.asu.wangfeng.surname.service.resources.WFQuery;
import edu.asu.joseibarra.services.name.QueryNameIncome;

/* Modified by Jose Ibarra
 * Added functionality for proper Google Map Coordinate to Lat
 * Long conversion
 */

@Path("/queryIncomeForename")
public class QueryIncomeForename extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("image/kdecacheforename") + File.separatorChar;
	}
	
	@GET
	@Produces("application/x-javascript")
	public JSONWithPadding querySurname(
			@QueryParam("forename") @DefaultValue("") String forename,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("callback") @DefaultValue("callback") String callback
			){
		QueryNameIncome query = new QueryNameIncome();
		QueryNameIncomeBean result = query.queryNameIncome(forename, "forename", new LatLng(latsw, lngsw), new LatLng(latne, lngne));
		return new JSONWithPadding(result, callback);
	}
}