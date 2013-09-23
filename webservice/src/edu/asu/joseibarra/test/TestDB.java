package edu.asu.joseibarra.test;

import java.sql.DriverManager;
import java.sql.SQLException;

import com.mysql.jdbc.Connection;

public class TestDB {

	
	public static void main(String[] args){
		try{
			System.out.println("Loading driver...");
			Class.forName("com.mysql.jdbc.Driver");
			System.out.println("Driver loaded!");
		} catch (ClassNotFoundException e){
			throw new RuntimeException("Cannot find the driver in the classpath!", e);
		}
		
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
		finally{
			System.out.println("Closing the connection.");
		    if (connection != null) try { connection.close(); } catch (SQLException ignore) {}
		}
	}
}
