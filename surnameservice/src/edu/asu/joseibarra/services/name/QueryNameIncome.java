package edu.asu.joseibarra.services.name;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;

import javax.naming.NamingException;

import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.QueryNameIncomeBean;
import edu.asu.wangfeng.surname.service.resources.WFQuery;

public class QueryNameIncome extends WFQuery{
	public QueryNameIncomeBean queryNameIncome(String name, String nameType, LatLng sw, LatLng ne){
		QueryNameIncomeBean result = new QueryNameIncomeBean();
		if(name ==null || name.length() == 0){
			result.setMeanIncome(new LinkedList<Double>());
			result.setMedianIncome(new LinkedList<Double>());
			return result;
		}
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		LinkedList<Double> meanIncomes = new LinkedList<Double>();
		LinkedList<Double> medianIncomes = new LinkedList<Double>();
		
		long start = System.nanoTime();
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "SELECT c.mean_income, c.median_income"
					+ " FROM phonebook as p, census as c"
					+ " WHERE p." + nameType + " = ? "
					+ " AND latitude between ? and ? and longitude between ? and ?"
					+ " AND c.census_tract_id = p.geoid_census_tract";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, name);
			statement.setDouble(2, sw.lat());
			statement.setDouble(3, ne.lat());
			statement.setDouble(4, sw.lng());
			statement.setDouble(5, ne.lng());
			
			resultset = statement.executeQuery();
			
			while (resultset.next()) {
				meanIncomes.add(resultset.getDouble(1));
				medianIncomes.add(resultset.getDouble(2));
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		long end = System.nanoTime();
//    	System.out.println("Time to execute and store " + name + " histogram incomes: "
//		+ ((end-start) / 1000000000.0) + " seconds");
    	
    	if(meanIncomes.size() < 100){
    		result.setMeanIncome(null);
    		result.setMedianIncome(null);
			return result;
		}
    	
    	result.setMeanIncome(meanIncomes);
    	result.setMedianIncome(medianIncomes);
		return result;
	}
}