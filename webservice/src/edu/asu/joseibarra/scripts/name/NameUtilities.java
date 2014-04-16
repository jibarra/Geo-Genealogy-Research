package edu.asu.joseibarra.scripts.name;

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

public class NameUtilities {
	
	public LinkedList<String> getAllSurnames() throws IOException{
		LinkedList<String> names = new LinkedList<String>();
		char[] currentChar = {'a', 'a', 't'};
		char[] lastChar = {'r', 'z', 'z'};
		char[] additional = {'1', 's', '1'};
		
		for(int i = 0; i < currentChar.length-1; i++){
			doQuery(currentChar[i], lastChar[i], additional[i], names, "surname");
		}
		
		return names;
	}
	
	public LinkedList<String> getAllForenames() throws IOException{
		LinkedList<String> names = new LinkedList<String>();
		char[] currentChar = {'a', 'a', 't'};
		char[] lastChar = {'r', 'z', 'z'};
		char[] additional = {'1', 's', '1'};
		
		for(int i = 0; i < currentChar.length-1; i++){
			doQuery(currentChar[i], lastChar[i], additional[i], names, "forename");
		}
		
		return names;
	}
	
	public void doQuery(char currentChar, char lastChar, char addChar, LinkedList<String> list,
				String nameType) throws IOException{
		do{
			Vector<String> coordinateVec = new Vector<String>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			try {
				connection = connectDatabase();
				//SQL LOCATED HERE
				
				sql = "SELECT DISTINCT" + nameType + " FROM average_income_" + nameType + " WHERE " + nameType + " LIKE ?";
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
	
	public void queryNameLessThanNum(String name, LinkedList<Integer> listNumAvail, LinkedList<String> listNames, 
			Connection connection, int lessNum, String nameType){
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		try {
			//SQL LOCATED HERE
			sql = "SELECT " + nameType + ", COUNT(*) FROM phonebook WHERE " + nameType + "=?";
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
	
	public void getNameLessThanNum(int lessNum, String nameType) throws IOException{
		String folderLocation = "C:\\Users\\jlibarr1\\Downloads\\";
		LinkedList<Integer> listNumAvail = new LinkedList<Integer>();
		LinkedList<String> listNames = new LinkedList<String>();
		System.out.println("Getting all names...");
		LinkedList<String> distinctNames = null;
		
		if(nameType.equals("surname")){
			distinctNames = getAllSurnames();
		}
		else if(nameType.equals("forename")){
			distinctNames = getAllForenames();
		}
		
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
			queryNameLessThanNum(distinctNames.poll(), listNumAvail, listNames, 
					connection, lessNum, nameType);
		}
		
		try {
			connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		System.out.println("Writing data structures to files...");
	    OutputStream outFile = new FileOutputStream(folderLocation + "list" + nameType + lessNum + ".ser");
		OutputStream outBuffer = new BufferedOutputStream(outFile);
	    ObjectOutput output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNames);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    outFile = new FileOutputStream(folderLocation + "listNumAvail" + nameType + lessNum + ".ser");
		outBuffer = new BufferedOutputStream(outFile);
	    output = new ObjectOutputStream(outBuffer);
	    output.writeObject(listNumAvail);
	    output.close();
	    outBuffer.close();
	    outFile.close();
	    
	    System.out.println("Creating txt file...");
        BufferedWriter out = new BufferedWriter(new FileWriter(folderLocation + "textRep" + nameType + lessNum + ".txt"));
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
		NameUtilities test = new NameUtilities();
		test.getNameLessThanNum(25, "forename");
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
