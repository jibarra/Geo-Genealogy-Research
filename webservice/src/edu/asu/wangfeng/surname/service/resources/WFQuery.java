package edu.asu.wangfeng.surname.service.resources;

import java.sql.Connection;
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
		return conn;
	}
}
