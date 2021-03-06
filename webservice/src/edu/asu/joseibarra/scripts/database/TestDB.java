package edu.asu.joseibarra.scripts.database;

import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestDB {

	
	public static void main(String[] args){
//		try{
//			System.out.println("Loading driver...");
//			Class.forName("com.mysql.jdbc.Driver");
//			System.out.println("Driver loaded!");
//		} catch (ClassNotFoundException e){
//			throw new RuntimeException("Cannot find the driver in the classpath!", e);
//		}
//		String url = "jdbc:mysql://localhost:3306/phonebook";
//		String username = "root";
//		String password = "password";
//		java.sql.Connection connection = null;
//		try{
//			System.out.println("Connectnig database...");
//			connection = DriverManager.getConnection(url, username, password);
//			System.out.println("Database connected!");
//		}
//		catch(SQLException e){
//			throw new RuntimeException("Cannot connect the database!", e);
//		}
//		
//		String insertSQL = "INSERT INTO average_income_forename(forename) SELECT distinct p.forename FROM phonebook as p where p.forename like ?";
//		PreparedStatement insertStatement = null;
//		
//		char val = 'a';
//		
//		try{
//			while(val <= 'z'){
//				insertStatement = connection.prepareStatement(insertSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				insertStatement.setString(1, val + "%");
//				System.out.println(insertStatement);
//				insertStatement.executeUpdate();
//				val++;
//			}
//		}
//		catch(SQLException e){
//			e.printStackTrace();
//		}
		
		//update for average income forename table
		String url = "jdbc:mysql://localhost:3306/phonebook";
		String username = "root";
		String password = "password";
		java.sql.Connection connection = null;
		try{
			System.out.println("Connectnig database...");
			connection = DriverManager.getConnection(url, username, password);
			System.out.println("Database connected!");
		}
		catch(SQLException e){
			throw new RuntimeException("Cannot connect the database!", e);
		}
		
		String querySQL = "SELECT DISTINCT forename FROM average_income_forename WHERE forename LIKE ?";
		String updateSQL = "UPDATE average_income_forename "+
							" SET average_median = (SELECT AVG(census.median_income)"+
							" FROM census, phonebook"+
							" WHERE phonebook.geoid_census_tract = census.census_tract_id"+
							" AND phonebook.forename = ?),"+
							" average_mean = (SELECT AVG(census.mean_income)"+
							" FROM census, phonebook "+
							" WHERE phonebook.geoid_census_tract = census.census_tract_id"+
							" AND phonebook.forename = ?)" +
							" WHERE forename = ?";
		PreparedStatement queryStatement = null;
		PreparedStatement updateStatement = null;
		ResultSet queryResults = null;
		char firstChar = 'z';
		char secondChar = 'o';
		String forename = null;
		try {
			//update all the surnames from a up to (non inclusive) s
			while(firstChar <= 'z'){
				while(secondChar <= 'z'){
					queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
							java.sql.ResultSet.CONCUR_READ_ONLY);
					queryStatement.setString(1, "" + firstChar + secondChar + "%");
					System.out.println(queryStatement);
					queryResults = queryStatement.executeQuery();
					
					while(queryResults.next()){
						forename = queryResults.getString(1);
						updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
								java.sql.ResultSet.CONCUR_READ_ONLY);
						updateStatement.setString(1, forename);
						updateStatement.setString(2, forename);
						updateStatement.setString(3, forename);
						updateStatement.executeUpdate();
					}
					secondChar++;
				}
				secondChar='a';
				firstChar++;
			}
		}
		 catch (SQLException e) {
			e.printStackTrace();
		 }
		
		//update for average income surname table
//		String url = "jdbc:mysql://localhost:3306/phonebook";
//		String username = "root";
//		String password = "password";
//		java.sql.Connection connection = null;
//		try{
//			System.out.println("Connectnig database...");
//			connection = DriverManager.getConnection(url, username, password);
//			System.out.println("Database connected!");
//		}
//		catch(SQLException e){
//			throw new RuntimeException("Cannot connect the database!", e);
//		}
//		
//		String querySQL = "SELECT DISTINCT forename FROM average_income_forename WHERE forename LIKE ?";
//		String updateSQL = "UPDATE average_income_surname "+
//							" SET average_median = (SELECT AVG(census.median_income)"+
//							" FROM census, phonebook"+
//							" WHERE phonebook.geoid_census_tract = census.census_tract_id"+
//							" AND phonebook.surname = ?),"+
//							" average_mean = (SELECT AVG(census.mean_income)"+
//							" FROM census, phonebook "+
//							" WHERE phonebook.geoid_census_tract = census.census_tract_id"+
//							" AND phonebook.surname = ?)" +
//							" WHERE surname = ?";
//		PreparedStatement queryStatement = null;
//		PreparedStatement updateStatement = null;
//		ResultSet queryResults = null;
//		String stringVal = "";
//		char val = 'a';
//		String surname = null;
//		try {
//			//update all the surnames from a up to (non inclusive) s
//			while(val < 's'){
//				queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				queryStatement.setString(1, stringVal + val + "%");
//				System.out.println(queryStatement);
//				queryResults = queryStatement.executeQuery();
//				
//				while(queryResults.next()){
//					surname = queryResults.getString(1);
//					updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//							java.sql.ResultSet.CONCUR_READ_ONLY);
//					updateStatement.setString(1, surname);
//					updateStatement.setString(2, surname);
//					updateStatement.setString(3,surname);
//					updateStatement.executeUpdate();
//				}
//				
//				val++;
//			}
//			
//			//update all the values from 'sa' to (non inclusive) 'sm'
//			stringVal = "s";
//			val = 'a';
//			while(val < 'm'){
//				queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				queryStatement.setString(1, stringVal + val + "%");
//				System.out.println(queryStatement);
//				queryResults = queryStatement.executeQuery();
//				
//				while(queryResults.next()){
//					surname = queryResults.getString(1);
//					updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//							java.sql.ResultSet.CONCUR_READ_ONLY);
//					updateStatement.setString(1, surname);
//					updateStatement.setString(2, surname);
//					updateStatement.setString(3,surname);
//					updateStatement.executeUpdate();
//				}
//				
//				val++;
//			}
//			
//			//update all the values in 'sma' to 'smz' (inclusive)
//			stringVal = "sm";
//			val = 'a';
//			while(val <= 'z'){
//				queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				queryStatement.setString(1, stringVal + val + "%");
//				System.out.println(queryStatement);
//				queryResults = queryStatement.executeQuery();
//				
//				while(queryResults.next()){
//					surname = queryResults.getString(1);
//					updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//							java.sql.ResultSet.CONCUR_READ_ONLY);
//					updateStatement.setString(1, surname);
//					updateStatement.setString(2, surname);
//					updateStatement.setString(3,surname);
//					updateStatement.executeUpdate();
//				}
//				
//				val++;
//			}
//			
//			//update the remaining s surnames, from 'sn' to 'sz' (inclusive)
//			stringVal = "s";
//			val = 'n';
//			while(val <= 'z'){
//				queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				queryStatement.setString(1, stringVal + val + "%");
//				System.out.println(queryStatement);
//				queryResults = queryStatement.executeQuery();
//				
//				while(queryResults.next()){
//					surname = queryResults.getString(1);
//					updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//							java.sql.ResultSet.CONCUR_READ_ONLY);
//					updateStatement.setString(1, surname);
//					updateStatement.setString(2, surname);
//					updateStatement.setString(3,surname);
//					updateStatement.executeUpdate();
//				}
//				
//				val++;
//			}
//			
//			//Update the last of the surnames, from 't' to 'z' inclusive
//			stringVal = "";
//			val = 't';
//			while(val <= 'z'){
//				queryStatement = connection.prepareStatement(querySQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				queryStatement.setString(1, stringVal + val + "%");
//				System.out.println(queryStatement);
//				queryResults = queryStatement.executeQuery();
//				
//				while(queryResults.next()){
//					surname = queryResults.getString(1);
//					updateStatement = connection.prepareStatement(updateSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//							java.sql.ResultSet.CONCUR_READ_ONLY);
//					updateStatement.setString(1, surname);
//					updateStatement.setString(2, surname);
//					updateStatement.setString(3,surname);
//					updateStatement.executeUpdate();
//				}
//				
//				val++;
//			}
//		} catch (SQLException e) {
//			e.printStackTrace();
//		}
		
		System.out.println("Closing the connection.");
	    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
	}
}
