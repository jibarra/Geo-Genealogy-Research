/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Calculates the bin averages for all names and
 * puts into a database to make the query times
 * much shorter.
 */

package edu.asu.joseibarra.scripts.name;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;

import edu.asu.joseibarra.geo.LatLng;
import edu.asu.joseibarra.name.utility.NameIncome;

public class ZillowIncomeScript {
	private final String csvFile = "C:\\Users\\jlibarr1\\Downloads\\textRepNames25.csv";
	
	
	public LinkedList<String> readNames(){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		LinkedList<String> names = new LinkedList<String>();
		
		try {
			 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] lineName = line.split(cvsSplitBy);
	 
				names.add(lineName[1]);
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return names;
	}
	
	public void addNamesToZillowTable(LinkedList<String> names) throws SQLException{
		NameIncome query = new NameIncome();
		String insertSQL = "INSERT INTO surname_zillow_income_ranges_avg (surname, income_range_avg_1, "
				+ "income_range_avg_2, income_range_avg_3, income_range_avg_4, income_range_avg_5, "
				+ "income_range_avg_6, income_range_avg_7, income_range_avg_8, income_range_avg_9, "
				+ "income_range_avg_10) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
		PreparedStatement insertStatement = null;
	    Connection connectionInsert = connectDatabase("phonebook", "root", "password");
	    
//	    String nameTemp = names.poll();
//	    while(!nameTemp.equals("LEFCOURT")){
//	    	nameTemp = names.poll();
//	    }
		
		for(String name : names){
			double incomes[] = query.queryIncomeRangeNameZillow(name, "surname", 
					new LatLng(22.75592037564069, -131.83937118749998),
					new LatLng(51.454006703387115, -62.31788681249998));
			
			if(incomes == null){
				continue;
			}
			
			System.out.println("Adding " + name);
			
			insertStatement = connectionInsert.prepareStatement(insertSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			
			insertStatement.setString(1, name);

			for(int i = 2; i <= 11; i++){
				if(Double.isNaN(incomes[i-2])){
					insertStatement.setDouble(i, 0.0);
				}
				else{
					insertStatement.setDouble(i, incomes[i-2]);
				}
			}
			
			insertStatement.executeUpdate();
			insertStatement.close();
		}
		
		connectionInsert.close();
	}
	
	private static Connection connectDatabase(String localdb,
			String username, String password) {
		Connection connection = null;
		try {
			connection = DriverManager.getConnection("jdbc:mysql://localhost:3306/"
					+ localdb, username, password);
		} catch (SQLException ex) {
			ex.printStackTrace();
		}
		return connection;
	}
	
	public static void main(String[] args) throws SQLException{
		ZillowIncomeScript script = new ZillowIncomeScript();
		LinkedList<String> names = script.readNames();
		script.addNamesToZillowTable(names);
	}
}
