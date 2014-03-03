package edu.asu.joseibarra.services.queryUtilities;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.NamingException;

import edu.asu.wangfeng.surname.service.resources.WFQuery;

public class NameIncome extends WFQuery{
//public class NameIncome{
	
	public double[] queryIncomeRangeSurname(String surname) throws NamingException{
		return queryIncomeRangeName("surname", surname);
	}
	
	public double[] queryIncomeRangeForename(String forename) throws NamingException{
		return queryIncomeRangeName("forename", forename);
	}
	
	private double[] queryIncomeRangeName(String nameType, String name) throws NamingException{
		double[] averages = new double[10];
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();

			sql = "select income_range_avg_1, income_range_avg_2, income_range_avg_3, income_range_avg_4"
					+ ", income_range_avg_5, income_range_avg_6, income_range_avg_7, income_range_avg_8"
					+ ", income_range_avg_9, income_range_avg_10 from "+ nameType + "_income_ranges_avg"
					+ " where " + nameType + "=?";

			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, name);

			resultset = statement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					averages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return averages;
	}
}
