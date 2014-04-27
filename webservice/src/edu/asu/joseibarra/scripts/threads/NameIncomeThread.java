/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Threaded class. Generates the incomes for
 * names based on the Census income tract data, separating
 * it into bins (10) also based on the Census income data.
 * 
 * Supports forenames but can be changed to support surnames
 * relatively easily.
 */

package edu.asu.joseibarra.scripts.threads;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.Vector;

public class NameIncomeThread extends Thread{
	
	//List of names to iterate through.
	LinkedList<String> currentNameList;
	
	public NameIncomeThread(LinkedList<String> nameList) {
		currentNameList = nameList;
	}

	public void run(){
		try {
			createAllIncomeRangesForename();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Synchronized method to iterate through the list and
	//ensure methods don't do teh same name.
	public synchronized String getName(LinkedList<String> list){
		if(list.isEmpty())
			return null;
		return list.poll();
	}
	
	/*
	 * Creates all the income ranges for all inputted forenames.
	 * Gets a count of the forenames in the phonebook then
	 * Gets an average of their income bins from the census.
	 * Stores the result within another table for quicker access.
	 */
	public void createAllIncomeRangesForename() throws SQLException, IOException{
	    Connection connectionSelect = null;
	    Connection connectionSelectCheck = null;
	    Connection connectionInsert = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double[] currentAverages = new double[10];
	    
	    connectionSelect = connectDatabase("phonebook", "root", "password");
	    connectionSelectCheck = connectDatabase("temp", "root", "password");
	    connectionInsert = connectDatabase("temp", "root", "password");
	    
	    while(!currentNameList.isEmpty()){
			String currentName = getName(currentNameList);
			
			if(currentName == null)
				continue;
			if(currentName.length() <= 1)
				continue;
			
			sql = "select COUNT(*) from forename_income_ranges_avg WHERE forename=?";
			statement = connectionSelectCheck.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, currentName);
			resultset = statement.executeQuery();
			
			resultset.next();
			if(resultset.getInt(1) > 0){
				continue;
			}
			
			System.out.println("Currently on: " + currentName);
	    	sql = "select AVG(c.income_range_1), AVG(c.income_range_2), AVG(c.income_range_3), AVG(c.income_range_4)"
					+ ", AVG(c.income_range_5), AVG(c.income_range_6), AVG(c.income_range_7), AVG(c.income_range_8)"
					+ ", AVG(c.income_range_9), AVG(c.income_range_10) from phonebook as p, census_income_ranges as c"
					+ " where p.forename =?"
					+ " AND c.census_tract_id = p.geoid_census_tract";
	    	
	    	statement = connectionSelect.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, currentName);
			System.out.println(statement);
			
			resultset = statement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					currentAverages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			statement.close();
						
			sql = "INSERT INTO forename_income_ranges_avg (forename, income_range_avg_1, income_range_avg_2,"
					+ "income_range_avg_3, income_range_avg_4, income_range_avg_5, income_range_avg_6, income_range_avg_7"
					+ ", income_range_avg_8, income_range_avg_9, income_range_avg_10) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			statement = connectionInsert.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, currentName);
			for(int i = 2; i <= 11; i++){
				statement.setDouble(i, currentAverages[i-2]);
			}
			statement.executeUpdate();
			statement.close();
	    }
	    connectionSelect.close();
	    connectionInsert.close();
	    connectionSelectCheck.close();
	}

	private static Connection connectDatabase(String localdb,
			String username, String password) {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost/"
					+ localdb, username, password);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return connection;
	}
}
