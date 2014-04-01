package edu.asu.joseibarra.name.utility;

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

import javax.naming.NamingException;

import edu.asu.joseibarra.services.WFQuery;
import edu.asu.joseibarra.utility.IncomeSimilarity;

public class NameIncome extends WFQuery{
//public class NameIncome{
	
	public double[] queryIncomeRangeSurname(String surname, String incomeType) throws NamingException{
		if(incomeType == "census"){
			return queryIncomeRangeName("surname", surname);
		}
		else if(incomeType == "zillow"){
			return queryIncomeRangeNameZillow("surname", surname);
		}
		return new double[10];
	}
	
	public double[] queryIncomeRangeForename(String forename, String incomeType) throws NamingException{
		return queryIncomeRangeName("forename", forename);
	}
	
	private double[] queryIncomeRangeNameZillow(String nameType, String name){
		return new double[10];
	}
	
	private double[] queryIncomeRangeName(String nameType, String name) throws NamingException{
		double[] averages = new double[10];
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase();

			sql = "select income_range_avg_1, income_range_avg_2, income_range_avg_3, income_range_avg_4"
					+ ", income_range_avg_5, income_range_avg_6, income_range_avg_7, income_range_avg_8"
					+ ", income_range_avg_9, income_range_avg_10 from "+ nameType + "_income_ranges_avg"
					+ " where " + nameType + "=?";

			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, name);

			resultset = statement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					averages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		return averages;
	}
	
	private double compareRanges(double[] range1, double[] range2){
		if(range1.length != range2.length){
			return 1000000;
		}
		double sum = 0;
		for(int i = 0; i < range1.length; i++){
			sum += (range1[i] - range2[i]) * (range1[i] - range2[i]);
		}
		return Math.sqrt(sum);
	}
	
	private IncomeSimilarity[] findSimilarNames(double[] incomeRange, LinkedList<String> compareList, String compareName) throws SQLException{
		//List of the similar names
	    IncomeSimilarity[] incomeSimilarity = new IncomeSimilarity[100];
	    for(int i = 0; i < 100; i++){
	    	incomeSimilarity[i] = new IncomeSimilarity("", -100000);
	    }
	    
	    LinkedList<String> nameList = compareList;
	    if(!nameList.removeFirstOccurrence(compareName)){
	    	System.out.println("Could not remove current name");
	    	return null;
	    }
	    
	    Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double[] currentAverages = null;
	    
	    while(!nameList.isEmpty()){
			String currentName = nameList.poll();
			currentAverages = new double[10];
			
			connection = connectDatabase("phonebook", "root", "password");
	    	sql = "select AVG(c.income_range_1), AVG(c.income_range_2), AVG(c.income_range_3), AVG(c.income_range_4)"
					+ ", AVG(c.income_range_5), AVG(c.income_range_6), AVG(c.income_range_7), AVG(c.income_range_8)"
					+ ", AVG(c.income_range_9), AVG(c.income_range_10) from phonebook as p, census_income_ranges as c"
					+ " where p.surname =?"
					+ " AND c.census_tract_id = p.geoid_census_tract";
	    	
	    	statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, currentName);
			
			resultset = statement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					currentAverages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			statement.close();
			connection.close();
			
			IncomeSimilarity similarity = new IncomeSimilarity(currentName, compareRanges(incomeRange, currentAverages));
			
			for(int i = 0; i < 100; i++){
				if(similarity.similarity < incomeSimilarity[i].similarity){
					IncomeSimilarity[] newSimilarity = new IncomeSimilarity[100];
					int j;
					for(j = 0; j < i; j++){
						newSimilarity[j] = incomeSimilarity[j];
					}
					newSimilarity[j] = similarity;
					j++;
					while(j < 100){
						newSimilarity[j] = incomeSimilarity[j-1];
						j++;
					}
					incomeSimilarity = newSimilarity;
					break;
				}
			}
		}
	    
	    return incomeSimilarity;
	}
	
	private void findAllSimilarNames() throws IOException, SQLException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listNames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    //List of the names of people to search the similarity of
	    LinkedList<String> currentNameList = null;
	    try {
	    	currentNameList = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    
	    LinkedList<String> savedList = currentNameList;
	    //List of the names of people to continually remove from when finding similarity
//	    LinkedList<String> compareList = null;
	  //List of the similar names
	    IncomeSimilarity[] incomeSimilarity = null;
	    
	    Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double[] currentAverages = new double[10];
		connection = connectDatabase("phonebook", "root", "password");
	    
	    while(!currentNameList.isEmpty()){
	    	String currentName = currentNameList.poll();
	    	
	    	sql = "select AVG(c.income_range_1), AVG(c.income_range_2), AVG(c.income_range_3), AVG(c.income_range_4)"
					+ ", AVG(c.income_range_5), AVG(c.income_range_6), AVG(c.income_range_7), AVG(c.income_range_8)"
					+ ", AVG(c.income_range_9), AVG(c.income_range_10) from phonebook as p, census_income_ranges as c"
					+ " where p.surname =?"
					+ " AND c.census_tract_id = p.geoid_census_tract";
	    	
	    	statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, currentName);
			
			resultset = statement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					currentAverages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			statement.close();
			
			
			incomeSimilarity = findSimilarNames(currentAverages, savedList, currentName);
			//add the list to the database
			sql = "INSERT INTO similar_income_ranges (surname, surnameSimilar, similartiy) VALUES(?,?,?)";
			
			for(int i = 0; i < incomeSimilarity.length; i++){
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, currentName);
				statement.setString(2, incomeSimilarity[i].name);
				statement.setDouble(3, incomeSimilarity[i].similarity);
				
				statement.addBatch();
			}
			statement.executeBatch();
			connection.commit();
			resultset.close();
			statement.close();
	    }
	    
	    connection.close();
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
	
	public static void main(String[] args) throws NamingException {
		NameIncome test = new NameIncome();
		test.queryIncomeRangeSurname("IBARRA", "census");
	}
}