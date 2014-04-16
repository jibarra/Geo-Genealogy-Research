package edu.asu.joseibarra.scripts.database;

import java.net.UnknownHostException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoOptions;

public class TransferLocalDB {
	public static void main(String[] args) throws SQLException,
				UnknownHostException {
		Connection mysqlConnection = connectMySQLDatabase("phonebook",
				"root", "password");
		
		MongoOptions options = new MongoOptions();
		options.connectionsPerHost = 100;
		options.maxWaitTime = 2000;
		options.socketKeepAlive = true;
		options.threadsAllowedToBlockForConnectionMultiplier = 50;
		
		Mongo mongo = new Mongo("localhost", options);
		
		DB mongoDB = mongo.getDB("phonebook");
		DBCollection collection = mongoDB.getCollection("phonebook");
		
		Statement statement = mysqlConnection.createStatement(
				java.sql.ResultSet.TYPE_FORWARD_ONLY,
				java.sql.ResultSet.CONCUR_READ_ONLY);
		statement.setFetchSize(Integer.MIN_VALUE);
		ResultSet resultset = statement
				.executeQuery("select forename, surname, address, city, state, postcode, latitude, longitude, geoid_state, geoid_county, geoid_countysub, geoid_census_tract, geoid from phonebook");
		while (resultset.next()) {
			String forename = resultset.getString(1);
			String surname = resultset.getString(2);
			String address = resultset.getString(3);
			String city = resultset.getString(4);
			String state = resultset.getString(5);
			String zipcode = resultset.getString(6);
			double lat = resultset.getDouble(7);
			double lng = resultset.getDouble(8);
			String geoidState = resultset.getString(9);
			String geoidCounty = resultset.getString(10);
			String geoidCountySub = resultset.getString(11);
			String geoidCensusTract = resultset.getString(12);
			String geoidBlock = resultset.getString(13);
		
			BasicDBObject nameObject = new BasicDBObject("forename", forename)
					.append("surname", surname);
			BasicDBObject addressObject = new BasicDBObject("address", address)
					.append("city", city).append("state", state)
					.append("zipcode", zipcode);
			ArrayList latlng = new ArrayList();
			latlng.add(lng);
			latlng.add(lat);
			BasicDBObject geometryObject = new BasicDBObject("type", "Point")
					.append("coordinates", latlng);
			BasicDBObject locationObject = new BasicDBObject("type", "Feature")
					.append("geometry", geometryObject);
			BasicDBObject geoidObject = new BasicDBObject("state", geoidState)
					.append("county", geoidCounty)
					.append("countysub", geoidCountySub)
					.append("census_tract", geoidCensusTract)
					.append("block", geoidBlock);
		
			BasicDBObject object = new BasicDBObject("name", nameObject)
					.append("address", addressObject)
					.append("location", locationObject)
					.append("geoid", geoidObject);
		
			collection.save(object);
		}
		
		resultset.close();
		statement.close();
		mongo.close();
		mysqlConnection.close();
	}

	private static Connection connectMySQLDatabase(String localdb,
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
