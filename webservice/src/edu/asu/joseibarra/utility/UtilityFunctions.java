package edu.asu.joseibarra.utility;

import java.awt.Point;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Vector;

import javax.naming.NamingException;

import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;

public class UtilityFunctions {
	
	public LinkedList<String> getAllSurnames() throws IOException{
		LinkedList<String> names = new LinkedList<String>();
		
		char currentChar = 'a';
		char lastChar = 'r';
		char additional = '1';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		additional = 's';
		currentChar = 'a';
		lastChar = 'z';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		additional = '1';
		currentChar = 't';
		lastChar = 'z';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		
		return names;
	}
	
	public LinkedList<String> getAllForenames() throws IOException{
		LinkedList<String> names = new LinkedList<String>();
		
		char currentChar = 'a';
		char lastChar = 'r';
		char additional = '1';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		additional = 's';
		currentChar = 'a';
		lastChar = 'z';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		additional = '1';
		currentChar = 't';
		lastChar = 'z';
		doAllSurnamesQuery(currentChar, lastChar, additional, names);
		
		return names;
	}
	
	public void doAllForenamesQuery(char currentChar, char lastChar, char addChar, LinkedList<String> list) throws IOException{
		do{
			Vector<String> coordinateVec = new Vector<String>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			try {
				connection = connectDatabase();
				//SQL LOCATED HERE
				
				sql = "SELECT DISTINCT forename FROM average_income_forename WHERE forename LIKE ?";
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				if(addChar == 's'){
					statement.setString(1, "" + addChar + "" + currentChar + "" + "%");
				}
				else{
					statement.setString(1,  currentChar + "%");
				}
				resultset = statement.executeQuery();
				while (resultset.next()) {
					String surname = resultset.getString(1);
					coordinateVec.add(surname);
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			for (String string : coordinateVec) {
				list.add(string);
			}
			currentChar++;
		}while(currentChar <= lastChar);
	}
	
	public void doAllSurnamesQuery(char currentChar, char lastChar, char addChar, LinkedList<String> list) throws IOException{
		do{
			Vector<String> coordinateVec = new Vector<String>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			try {
				connection = connectDatabase();
				//SQL LOCATED HERE
				
				sql = "SELECT DISTINCT surname FROM average_income_surname WHERE surname LIKE ?";
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				if(addChar == 's'){
					statement.setString(1, "" + addChar + "" + currentChar + "" + "%");
				}
				else{
					statement.setString(1,  currentChar + "%");
				}
				resultset = statement.executeQuery();
				while (resultset.next()) {
					String surname = resultset.getString(1);
					coordinateVec.add(surname);
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (NamingException e) {
				e.printStackTrace();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			
			for (String string : coordinateVec) {
				list.add(string);
			}
			currentChar++;
		}while(currentChar <= lastChar);
	}
	
	public void querySurnameLessThanNum(String name, LinkedList<Integer> listNumAvail, LinkedList<String> listNames, Connection connection, int lessNum){
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		try {
			//SQL LOCATED HERE
			sql = "SELECT surname, COUNT(*) FROM phonebook WHERE surname=?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				if(resultset.getInt(2) >= lessNum){
					listNames.add(resultset.getString(1));
					listNumAvail.add(resultset.getInt(2));
				}
			}
			resultset.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void getSurnamesLessThanNum(int lessNum) throws IOException{
		String folderLocation = "C:\\Users\\jlibarr1\\Downloads\\";
		LinkedList<Integer> listNumAvail = new LinkedList<Integer>();
		LinkedList<String> listNames = new LinkedList<String>();
		System.out.println("Getting all names...");
		LinkedList<String> distinctNames = getAllSurnames();
		
		System.out.println("Getting the names with less than " + lessNum + " entries...");
		Connection connection = null;
		try {
			connection = connectDatabase();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!distinctNames.isEmpty()){
			querySurnameLessThanNum(distinctNames.poll(), listNumAvail, listNames, connection, lessNum);
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Writing data structures to files...");
	    OutputStream outFile = new FileOutputStream(folderLocation + "listNames" + lessNum + ".ser");
		OutputStream outBuffer = new BufferedOutputStream(outFile);
	    ObjectOutput output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNames);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    outFile = new FileOutputStream(folderLocation + "listNumAvailNames" + lessNum + ".ser");
		outBuffer = new BufferedOutputStream(outFile);
	    output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNumAvail);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    System.out.println("Creating txt file...");
        BufferedWriter out = new BufferedWriter(new FileWriter(folderLocation + "textRepNames" + lessNum + ".txt"));
        out.write("NUM,NAME");
        int numOutputted = 0;
        while(!listNames.isEmpty()){
        	String name = listNames.poll();
        	Integer num = listNumAvail.poll();
        	out.write(num + "," + name + "\n");
        	numOutputted++;
        }
        out.close();
		System.out.println("Amount of names less than " + lessNum + ": " + numOutputted);
	}
	
	public void queryForenameLessThanNum(String name, LinkedList<Integer> listNumAvail, LinkedList<String> listNames, Connection connection, int lessNum){
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		try {
			sql = "SELECT forename, COUNT(*) FROM phonebook WHERE forename=?";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, name);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				if(resultset.getInt(2) >= lessNum){
					listNames.add(resultset.getString(1));
					listNumAvail.add(resultset.getInt(2));
				}
			}
			resultset.close();
			statement.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	

	public void getForenamesLessThanNum(int lessNum) throws IOException{
		String folderLocation = "C:\\Users\\jlibarr1\\Downloads\\";
		LinkedList<Integer> listNumAvail = new LinkedList<Integer>();
		LinkedList<String> listNames = new LinkedList<String>();
		System.out.println("Getting all names...");
		LinkedList<String> distinctNames = getAllForenames();
		
		System.out.println("Getting the names with >= " + lessNum + " entries...");
		Connection connection = null;
		try {
			connection = connectDatabase();
		} catch (NamingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while(!distinctNames.isEmpty()){
			queryForenameLessThanNum(distinctNames.poll(), listNumAvail, listNames, connection, lessNum);
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Writing data structures to files...");
	    OutputStream outFile = new FileOutputStream(folderLocation + "listForenames" + lessNum + ".ser");
		OutputStream outBuffer = new BufferedOutputStream(outFile);
	    ObjectOutput output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNames);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    outFile = new FileOutputStream(folderLocation + "listNumAvailForenames" + lessNum + ".ser");
		outBuffer = new BufferedOutputStream(outFile);
	    output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNumAvail);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    System.out.println("Creating txt file...");
        BufferedWriter out = new BufferedWriter(new FileWriter(folderLocation + "textRepForenames" + lessNum + ".txt"));
        out.write("NUM,NAME\n");
        int numOutputted = 0;
        while(!listNames.isEmpty()){
        	String name = listNames.poll();
        	Integer num = listNumAvail.poll();
        	out.write(num + "," + name + "\n");
        	numOutputted++;
        }
        out.close();
		System.out.println("Amount of names more than " + lessNum + ": " + numOutputted);
	}
	
	public static void main(String[] args) throws IOException{
		UtilityFunctions test = new UtilityFunctions();
		test.getForenamesLessThanNum(25);
	}
	
	public Connection connectDatabase() throws NamingException, SQLException {		
		String url = "jdbc:mysql://127.0.0.1:3306/phonebook";
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

}
