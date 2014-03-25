package edu.asu.wangfeng.surname.service.resources;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

public class WFQuery {
	public Connection connectDatabase() throws NamingException, SQLException {
		javax.naming.Context initContext = new InitialContext();
		javax.naming.Context envContext = (javax.naming.Context) initContext.lookup("java:/comp/env");
		DataSource ds = (DataSource) envContext.lookup("jdbc/phonebook");
		Connection conn = ds.getConnection();
		
		//OPTIONALLY: Comment above code and uncomment below code
		//Use only if web service server is not hosted on this computer, using Tomcat
//		String url = "jdbc:mysql://localhost:3306/phonebook";
//		String username = "root";
//		String password = "password";
//		java.sql.Connection conn = null;
//		
//		try{
//			conn = DriverManager.getConnection(url, username, password);
//		}
//		catch(SQLException e){
//			throw new RuntimeException("Cannot connect the database!", e);
//		}
		
		return conn;
	}
}
