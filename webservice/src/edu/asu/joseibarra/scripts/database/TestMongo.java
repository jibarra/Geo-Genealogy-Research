package edu.asu.joseibarra.scripts.database;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.mongodb.*;
//for ssh conenctions
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.sql.DriverManager;


public class TestMongo {
	public static void main(String[] args) throws JSchException{
		//for ssh connections
//		String user = "ubuntu";
//	    String password = "";
//	    String host = "ec2-54-201-228-235.us-west-2.compute.amazonaws.com";
//	    int port=22;
//	    
//	    JSch jsch = new JSch();
//	    jsch.addIdentity("./resources/vader.ppk");
//        Session session = jsch.getSession(user, host, port);
//        session.setPassword(password);
//        session.setConfig("StrictHostKeyChecking", "no");
//        
//        System.out.println("Establishing Connection...");
//        session.connect();
//        System.out.println("Connection established.");
//        System.out.println("Crating SFTP Channel.");
//        ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
//        sftpChannel.connect();
//        System.out.println("SFTP Channel created.");
//		System.out.println("Disconnecting...");
//		sftpChannel.disconnect();
//		session.disconnect();
        
        

		
//		DB db = mongoClient.getDB("test");
//		DBCollection coll = db.getCollection("testData");
//		//get first object
//		DBObject myDoc = coll.findOne();
//		System.out.println("First object:");
//		System.out.println(myDoc);
//		//get number of objects
//		System.out.println("Count of objects:");
//		System.out.println(coll.getCount());
//		
//		//get all objects
//		DBCursor cursor = coll.find();
//		System.out.println();
//		System.out.println("All objects:");
//		try {
//		   while(cursor.hasNext()) {
//		       System.out.println(cursor.next());
//		   }
//		} finally {
//		   cursor.close();
//		}
//		
//		//get all joses
//		BasicDBObject query = new BasicDBObject("fname", "Jose");
//		cursor = coll.find(query);
//		System.out.println();
//		System.out.println("All fname \"Jose\":");
//		try {
//		   while(cursor.hasNext()) {
//		       System.out.println(cursor.next());
//		   }
//		} finally {
//		   cursor.close();
//		}
		TestMongo test = new TestMongo();
		String surname = "JONES";
//		test.testMongoDBQuerySurname(surname);
		test.testMySQLQuerySurname(surname);
	}
	
	public Connection connectDatabase() throws NamingException, SQLException {		
		String url = "jdbc:mysql://localhost:3306/phonebook";
		String username = "root";
		String password = "password";
		java.sql.Connection conn = null;
		
		try{
			conn = DriverManager.getConnection(url, username, password);
		}
		catch(SQLException e){
			throw new RuntimeException("Cannot connect the database!", e);
		}
		return conn;
	}
	
	public void testMySQLQuerySurname(String surname){
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		System.out.println();
		System.out.println("All surnames " + surname + " in MySQL:");
		long start = System.nanoTime();
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "select latitude, longitude from phonebook where surname=?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, surname);
			
			resultset = statement.executeQuery();
			while (resultset.next()) {
//				System.out.println("Surname: " + resultset.getString(1) + " Forename: " + resultset.getString(2));
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
    	System.out.println("Time to execute and store " + surname + " MySQL query: " + ((end-start) / 1000000000.0) + " seconds");
	}
	
	public void testMongoDBQuerySurname(String surname){
//		MongoClient mongoClient = null;
		Mongo mongoClient = null;
		MongoOptions options = new MongoOptions();
		options.connectionsPerHost = 100;
		options.maxWaitTime = 2000;
		options.socketKeepAlive = true;
		options.threadsAllowedToBlockForConnectionMultiplier = 50;
		try {
			
//			mongoClient = new Mongo( "ec2-54-201-232-101.us-west-2.compute.amazonaws.com", options);
			mongoClient = new Mongo("localhost", options);
		} catch (UnknownHostException e) {
			System.out.println("Error: " + e.getLocalizedMessage());
		}
		
		long start = System.nanoTime();
		DB db = mongoClient.getDB("phonebook");
		DBCollection coll = db.getCollection("phonebook");
		//get first object
//		DBObject myDoc = coll.findOne();
//		System.out.println("First object:");
//		System.out.println(myDoc);
		
		//get all surnames
		BasicDBObject query = new BasicDBObject("name.surname", surname);
		BasicDBObject fields = new BasicDBObject();
		fields.put("location.geometry.coordinates", 1);
		fields.put("_id", 0);
		DBCursor cursor = coll.find(query, fields);
		System.out.println();
		System.out.println("All surnames " + surname + " in MongoDB:");
		while(cursor.hasNext()) {
//			System.out.println(cursor.next());
			cursor.next();
		}
		System.out.println(cursor.explain());
		cursor.close();
		
		long end = System.nanoTime();
    	System.out.println("Time to execute and store " + surname + " MongoDB query: " + ((end-start) / 1000000000.0) + " seconds");
		
		mongoClient.close();
	}
}
