/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * This class is the public exposure of the forename query for income.
 * This service needs to be updated to match the surname service,
 * as it is behind what the surname service actually does.
 * It should be combined with QueryForename to reduce the amount
 * of classes.
 * The current method returns the mean/median income for a forename.
 */

package edu.asu.joseibarra.services;

import java.io.File;

import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.joseibarra.geo.LatLng;
import edu.asu.joseibarra.name.utility.QueryNameIncome;
import edu.asu.wangfeng.service.netbeans.QueryNameIncomeBean;



@Path("/queryIncomeForename")
public class QueryIncomeForename extends WFQuery{
	private String imageDir;
	
	@Context
	public void setServletContext(ServletContext context) {
//		imageDir = context.getRealPath("image/kdecacheforename") + File.separatorChar;
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