/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * This combines all the queries for a surname within one class.
 * All surname services exist here and this is the entry point
 * for any surname service.
 */

package edu.asu.joseibarra.services;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.PriorityQueue;

import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;

import com.sun.jersey.api.json.JSONWithPadding;

import edu.asu.joseibarra.geo.LatLng;
import edu.asu.joseibarra.name.utility.NameIncome;
import edu.asu.joseibarra.name.utility.QueryName;
import edu.asu.joseibarra.name.utility.QueryNameIncome;
import edu.asu.joseibarra.name.utility.Wordle;
import edu.asu.wangfeng.service.netbeans.IncomeComparisonBean;
import edu.asu.wangfeng.service.netbeans.QueryBean;
import edu.asu.wangfeng.service.netbeans.QueryNameIncomeBean;

@Path("/surname")
public class SurnameService extends WFQuery{
	
	private String imageDir;
	private String fileDir;
	
	//On server start, fills in information for the image directory and file directory.
	@Context
	public void setServletContext(ServletContext context) {
		imageDir = context.getRealPath("") + File.separatorChar + "resources" + File.separatorChar;
		fileDir = context.getRealPath("") + File.separatorChar + "resources" + File.separatorChar;
	}
	
	/*
	 * Outputs the income wordle based on the surname inputted.
	 * Queries the database to find the similar surnames ordered by
	 * similar incomes and returns the result. Values are limited
	 * by the inputted limit.
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/incomeWordle")
	public JSONWithPadding incomeWordle(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("limit") @DefaultValue("10") int limit
			) throws IOException, NamingException{
		if(surname.length() < 1 || limit < 1){
			return new JSONWithPadding(new LinkedList<Wordle>(), callback);
		}
			
		surname = surname.toUpperCase();
		LinkedList<Wordle> names = new LinkedList<Wordle>();
		
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();
			sql = "SELECT surnameSimilar, incomeSimilarity FROM similar_incomes_surname WHERE surname=? ORDER BY incomeSimilarity LIMIT ?";

			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, surname);
			statement.setInt(2, limit);

			resultset = statement.executeQuery();
			
			while (resultset.next()) {
				names.add(new Wordle(resultset.getString(1), resultset.getFloat(2) * 100));
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new JSONWithPadding(names, callback);
	}
	
	/*
	 * Returns the map similarity wordle based on the type inputted.
	 * Types can be 'core' or 'l2'. The surname is then queried and
	 * the query is limited based on an input.
	 * A collection of the similar names is returned.
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/mapWordle")
	public JSONWithPadding mapWordle(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("type") @DefaultValue("") String type,
			@QueryParam("limit") @DefaultValue("10") int limit
			) throws IOException, NamingException{
		if(surname.length() < 1 || limit < 1){
			return new JSONWithPadding(new LinkedList<Wordle>(), callback);
		}
			
		surname = surname.toUpperCase();
		LinkedList<Wordle> names = new LinkedList<Wordle>();
		
		Connection connection = null;
		String sql = null;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();
			if(type.equals("l2")){
				sql = "SELECT surnameSimilar, similarity FROM similar_income_ranges WHERE surname=? ORDER BY similarity LIMIT ?";
			}
			else if(type.equals("core")){
				sql = "SELECT surnameSimilar, incomeSimilarity FROM similar_core_surname WHERE surname=? ORDER BY incomeSimilarity LIMIT ?";
			}
			
			if(sql == null)
				return new JSONWithPadding(new LinkedList<Wordle>(), callback);

			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, surname);
			statement.setInt(2, limit);

			resultset = statement.executeQuery();
			
			while (resultset.next()) {
				names.add(new Wordle(resultset.getString(1), resultset.getFloat(2)));
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return new JSONWithPadding(names, callback);
	}
	
	/*
	 * Returns a wordle based on a set of bins inputted.
	 * This method must have 10 bins inputted or defaults will be
	 * used instead. The method queries the database for similar names,
	 * comparing each bin inputted to the data within the census
	 * income bin table, calculating a simple l2 norm between bins.
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/incomeToolWordle")
	public JSONWithPadding incomeToolWordle(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("limit") @DefaultValue("10") int limit,
			@QueryParam("bin1") @DefaultValue("10") double bin1,
			@QueryParam("bin2") @DefaultValue("10") double bin2,
			@QueryParam("bin3") @DefaultValue("10") double bin3,
			@QueryParam("bin4") @DefaultValue("10") double bin4,
			@QueryParam("bin5") @DefaultValue("10") double bin5,
			@QueryParam("bin6") @DefaultValue("10") double bin6,
			@QueryParam("bin7") @DefaultValue("10") double bin7,
			@QueryParam("bin8") @DefaultValue("10") double bin8,
			@QueryParam("bin9") @DefaultValue("10") double bin9,
			@QueryParam("bin10") @DefaultValue("10") double bin10
			) throws IOException, NamingException, SQLException{
		double input[] = {bin1, bin2, bin3, bin4, bin5, bin6, bin7, bin8, bin9, bin10};
		
		BufferedReader br = null;
		String line = "";
		LinkedList<String> names = new LinkedList<String>();

		br = new BufferedReader(new FileReader(fileDir + "surname_count_fileterd.csv"));
		while((line = br.readLine())!=null){
			String[] splitLine = line.split(",");
			names.add(splitLine[0]);
		}
		br.close();
		
		PriorityQueue closest = new PriorityQueue<IncomeComparisonBean>();
		
		String sql = "SELECT * FROM surname_income_ranges_avg WHERE surname=?";
		PreparedStatement statement = null;
		Connection connection = null;
		ResultSet resultset = null;
		
		//Already put one name in
		int size = names.size()-1;
		for(int i = 1; i < size; i++){
			sql += " OR surname=?";
		}
		
		connection = connectDatabase();
		statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
		java.sql.ResultSet.CONCUR_READ_ONLY);
		
		for(int i = 0; i < size; i++){
			statement.setString(i+1, names.poll());
		}
		
		resultset = statement.executeQuery();
		double max = -1000;
		while(resultset.next()){
			double [] compare = {resultset.getDouble(2), resultset.getDouble(3), resultset.getDouble(4), 
					resultset.getDouble(5), resultset.getDouble(6), resultset.getDouble(7), resultset.getDouble(8), 
					resultset.getDouble(9), resultset.getDouble(10), resultset.getDouble(11)};
			double comparisonValue = compareIncomeRanges(input, compare);
			closest.offer(new IncomeComparisonBean(resultset.getString(1), comparisonValue));
			if(closest.size() > limit+1){
				closest.poll();
			}
		}
		
		return new JSONWithPadding(closest, callback);
	}
	
	/*
	 * Simple method for incomeToolWordle method to compare between
	 * incomes. Returns the l2 norm for two incomes.
	 */
	public double compareIncomeRanges(double[] base, double[] compare){
		if(base.length != compare.length){
			return -1;
		}
		double value = 0;
		for(int i = 0; i < base.length; i++){
			value += (base[i] - compare[i]) * (base[i] - compare[i]);
		}
		
		return Math.sqrt(value);
	}
	
	/*
	 * Returns a similarity wordle based on a selected income bin.
	 * The income bin inputted will just compare one bin from a surname
	 * to all other names by the same bin #. This is an l2 comparison.
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/incomeBinWordle")
	public JSONWithPadding incomeBinWordle(
			@QueryParam("callback") @DefaultValue("callback") String callback,
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("limit") @DefaultValue("10") int limit,
			@QueryParam("bin") @DefaultValue("1") int bin
			) throws IOException, NamingException{
		if(surname.length() < 1 || limit < 1 || bin < 0 || bin > 10){
			return new JSONWithPadding(new LinkedList<Wordle>(), callback);
		}
		String incomeBin = "income_range_avg_" + bin;
		surname = surname.toUpperCase();
		LinkedList<Wordle> names = new LinkedList<Wordle>();
		
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();
			double compareNum = 0;
			
			sql = "SELECT " + incomeBin + " FROM surname_income_ranges_avg WHERE surname=?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, surname);
			resultset = statement.executeQuery();
			
			while (resultset.next()) {
				compareNum = resultset.getDouble(1);
			}

			sql = "SELECT surname, ABS(" + incomeBin + "-?) AS distance FROM("
					+ " (SELECT surname, "+ incomeBin
							+ " FROM surname_income_ranges_avg"
							+ " WHERE " + incomeBin + " >= ?"
							+ " AND surname != ?"
							+ " ORDER BY " + incomeBin
							+ " LIMIT ?)"
						+ " UNION all"
						+ " (SELECT surname, "+ incomeBin
							+ " FROM surname_income_ranges_avg"
							+ " WHERE " + incomeBin + " < ?"
							+ " ORDER BY " + incomeBin + " DESC"
							+ " LIMIT ?)"
					+ ") AS n ORDER BY distance LIMIT ?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			
			statement.setDouble(1, compareNum);
			statement.setDouble(2, compareNum);
			statement.setString(3, surname);
			statement.setInt(4, limit);
			statement.setDouble(5, compareNum);
			statement.setInt(6, limit);
			statement.setInt(7, limit);

			resultset = statement.executeQuery();
			
			while (resultset.next()) {
				names.add(new Wordle(resultset.getString(1), resultset.getFloat(2) * 100));
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		
		return new JSONWithPadding(names, callback);
	}
	
	/*
	 * Gets the zillow income based on an input. Can compare either from the regular
	 * Zillow API call (which takes a while) or query a precomputed average table (much faster)
	 * It will return a set of bins (10 total)
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/queryZillowIncome")
	public JSONWithPadding queryZillowIncome(
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("latsw") @DefaultValue("0.0") double latsw,
			@QueryParam("lngsw") @DefaultValue("0.0") double lngsw,
			@QueryParam("latne") @DefaultValue("0.0") double latne,
			@QueryParam("lngne") @DefaultValue("0.0") double lngne,
			@QueryParam("callback") @DefaultValue("callback") String callback
			){
		NameIncome query = new NameIncome();
		//This method queries the Zillow API, which takes a while.
//		double result[] = query.queryIncomeRangeNameZillow(surname, "surname", new LatLng(latsw, lngsw), new LatLng(latne, lngne));
		//This method queries the precomputed table.
		double result[] = query.queryPrecomputeIncomeRangeNameZillow(surname, "surname");
		return new JSONWithPadding(result, callback);
	}
	
	/*
	 * Queries the income mean and median of a name. This method isn't really used
	 * anymore as its not as telling of a name's income distribution.
	 */
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
	
	/*
	 * Returns the income range of a name, placed into bins according
	 * to the Census (10 bin ranges). This will query the precomputed table
	 * for averages.
	 */
	@GET
	@Produces("application/x-javascript")
	@Path("/queryIncomeRanges")
	public JSONWithPadding queryIncomeRanges(
			@QueryParam("surname") @DefaultValue("") String surname,
			@QueryParam("incomeType") @DefaultValue("census") String incomeType,
			@QueryParam("callback") @DefaultValue("callback") String callback
			) throws NamingException{
		if(incomeType == null || surname == null || incomeType.equals("") || surname.equals("")){
			return new JSONWithPadding(new double[10]);
		}
		
		NameIncome income = new NameIncome();
		double[] result = income.queryIncomeRangeSurname(surname, incomeType);
		return new JSONWithPadding(result, callback);
	}
	
	/*
	 * Queries the regular map image based on the inputted surname.
	 * Can return a precomputed map (fast) or create a new map (slow).
	 */
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
		//Precomputed map method call.
		QueryBean result = query.queryCreatedName(surname, imageDir, "regular");
		//Non precomptued map method call. Will create a brand new map.
//		QueryBean result = query.queryName(surname, new LatLng(latsw, lngsw), new LatLng(latne, lngne), 
//				new LatLng(latcenter, lngcenter), width, height, zoom, "surname", imageDir, "regular", -1);
		return new JSONWithPadding(result, callback);
	}
	
	/*
	 * Returns the probabilistic map based on inputted surname.
	 * Can return a precomputed map or create a new map.
	 */
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
		//Precomputed map method
		QueryBean result = query.queryCreatedName(surname, imageDir, "probabilistic");
		//Create a new map
//		QueryBean result = query.queryName(surname, new LatLng(latsw, lngsw), new LatLng(latne, lngne), 
//				new LatLng(latcenter, lngcenter), width, height, zoom, "surname", imageDir, "probabilistic", -1);
		return new JSONWithPadding(result, callback);
	}
}
