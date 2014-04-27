/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Generates the income ranges for a name.
 */

package edu.asu.joseibarra.scripts.name;

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
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import javax.naming.NamingException;

import edu.asu.joseibarra.scripts.resources.NameRange;
import edu.asu.joseibarra.scripts.threads.NameIncomeSimilarityThread;
import edu.asu.joseibarra.scripts.threads.NameIncomeThread;
import edu.asu.joseibarra.utility.IncomeSimilarity;

//public class NameIncome extends WFQuery{
public class NameIncomeScript{
	
	public void transferDB() throws SQLException{
		Connection connectionSelect = null;
	    Connection connectionInsert = null;
		String selectSQL;
		PreparedStatement selectStatement = null;
		String insertSQL;
		PreparedStatement insertStatement = null;
		ResultSet resultset = null;
		double[] currentAverages = new double[10];
		LinkedList<String> names = new LinkedList<String>();
		String name = null;
		
		connectionSelect = connectDatabase("temp", "root", "password");
	    connectionInsert = connectDatabase("phonebook", "root", "password");
		
		selectSQL = "SELECT DISTINCT(surname) FROM surname_income_ranges_avg";
		selectStatement = connectionSelect.prepareStatement(selectSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
				java.sql.ResultSet.CONCUR_READ_ONLY);
		resultset = selectStatement.executeQuery();
		while (resultset.next()) {
			names.add(resultset.getString(1));
		}
		
		while(!names.isEmpty()){
			name = names.poll();
			System.out.println("Doing " + name);
			selectSQL = "SELECT income_range_avg_1, income_range_avg_2,"
					+ "income_range_avg_3, income_range_avg_4, income_range_avg_5, income_range_avg_6, income_range_avg_7"
					+ ", income_range_avg_8, income_range_avg_9, income_range_avg_10 FROM"
					+ " surname_income_ranges_avg WHERE surname=?";
			selectStatement = connectionSelect.prepareStatement(selectSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			selectStatement.setString(1, name);
			resultset = selectStatement.executeQuery();
			while (resultset.next()) {
				for(int i = 0; i < 10; i++){
					currentAverages[i] = resultset.getDouble(i+1);
				}
			}
			resultset.close();
			selectStatement.close();
			
			insertSQL = "INSERT INTO surname_income_ranges_avg (surname, income_range_avg_1, income_range_avg_2,"
					+ "income_range_avg_3, income_range_avg_4, income_range_avg_5, income_range_avg_6, income_range_avg_7"
					+ ", income_range_avg_8, income_range_avg_9, income_range_avg_10) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			insertStatement = connectionInsert.prepareStatement(insertSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			insertStatement.setString(1, name);
			for(int i = 2; i <= 11; i++){
				insertStatement.setDouble(i, currentAverages[i-2]);
			}
			insertStatement.executeUpdate();
			insertStatement.close();
			
		}
		connectionSelect.close();
		connectionInsert.close();
	}
	
	/*
	 * Generates income ranges for all forename.
	 */
	public void createAllIncomeRangesForename() throws SQLException, IOException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listForenames25.ser");
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
	    
	    //ROCHELL
	    String cur = null;
	    do{
	    	cur = currentNameList.poll();
	    }while(!cur.equals("SAWYER"));
	    	    
	    System.out.println("Creating threads...");
	    
	    NameIncomeThread[] threads = new NameIncomeThread[6];
	    for(int i = 0; i < threads.length; i++){
	    	threads[i] = new NameIncomeThread(currentNameList);
	    }
	    
	    System.out.println("Starting threads...");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i].start();
	    }
	    
//	    for(int i = 0; i < threads.length; i++){
//	    	while(threads[i].isAlive()){
//	    		try {
//					Thread.sleep(60000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//	    	}
//	    }
	}
	

	/*
	 * Generates income ranges for all suranmes.
	 */
	public void createAllIncomeRanges() throws SQLException, IOException{
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
	    
	    Connection connectionSelect = null;
	    Connection connectionInsert = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		double[] currentAverages = null;
	    int count = 0;
	    int numSwitches = 0;
	    
	    connectionSelect = connectDatabase("phonebook", "root", "password");
	    connectionInsert = connectDatabase("temp", "root", "password");
	    while(!currentNameList.isEmpty()){
			String currentName = currentNameList.poll();
			System.out.println("Currently on: " + currentName);
			currentAverages = new double[10];
			
			
	    	sql = "select AVG(c.income_range_1), AVG(c.income_range_2), AVG(c.income_range_3), AVG(c.income_range_4)"
					+ ", AVG(c.income_range_5), AVG(c.income_range_6), AVG(c.income_range_7), AVG(c.income_range_8)"
					+ ", AVG(c.income_range_9), AVG(c.income_range_10) from phonebook as p, census_income_ranges as c"
					+ " where p.surname =?"
					+ " AND c.census_tract_id = p.geoid_census_tract";
	    	
	    	statement = connectionSelect.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
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
						
			sql = "INSERT INTO surname_income_ranges_avg (surname, income_range_avg_1, income_range_avg_2,"
					+ "income_range_avg_3, income_range_avg_4, income_range_avg_5, income_range_avg_6, income_range_avg_7"
					+ ", income_range_avg_8, income_range_avg_9, income_range_avg_10) VALUES(?,?,?,?,?,?,?,?,?,?,?)";
			statement = connectionInsert.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			statement.setString(1, currentName);
			for(int i = 2; i <= 11; i++){
				statement.setDouble(i, currentAverages[i-2]);
			}
			statement.executeUpdate();
			statement.close();
	    }
	    connectionSelect.close();
	    connectionInsert.close();
	}
	
	public double[] queryIncomeRangeSurname(String surname) throws NamingException{
		return queryIncomeRangeName("surname", surname);
	}
	
	/*
	 * Creates an income range for a specified name
	 */
	private double[] queryIncomeRangeName(String nameType, String name) throws NamingException{
		// prepare the data
		double[] averages = new double[10];
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		try {
			connection = connectDatabase("phonebook", "root", "password");
//			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "select AVG(c.income_range_1), AVG(c.income_range_2), AVG(c.income_range_3), AVG(c.income_range_4)"
					+ ", AVG(c.income_range_5), AVG(c.income_range_6), AVG(c.income_range_7), AVG(c.income_range_8)"
					+ ", AVG(c.income_range_9), AVG(c.income_range_10) from phonebook as p, census_income_ranges as c"
					+ " where p." + nameType + "=?"
					+ " AND c.census_tract_id = p.geoid_census_tract";

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
	
	private int binarySearch(IncomeSimilarity compare, Vector<IncomeSimilarity> vector, int min, int max){
		if(max <= min){
			if(max < min)
				return min;
			else
				return max;
		}
		
		int mid = (max+min)/2;
		
		//in the top half
		if(compare.similarity > vector.get(mid).similarity)
			return binarySearch(compare, vector, mid+1, max);
		//in the bottom half
		else if(compare.similarity < vector.get(mid).similarity)
			return binarySearch(compare, vector, min, mid-1);
		else
			return mid;
	}
	
	private void addSimilarityToVector(IncomeSimilarity currentSim, Vector<IncomeSimilarity> addVector, int max){
		if(currentSim.similarity > addVector.get(max).similarity)
			return;
		
		int addSpot = binarySearch(currentSim, addVector, 0, max);
		
		if(addSpot != 0 && currentSim.similarity < addVector.get(addSpot-1).similarity)
			System.out.println("bad");
		
		if(currentSim.similarity < addVector.get(addSpot).similarity){
			addVector.add(addSpot, currentSim);
		}
		else{
			addVector.add(addSpot+1, currentSim);
		}
			
	}
	
	private IncomeSimilarity[] findSimilarNames(NameRange compareRange, LinkedList<NameRange> ranges) throws SQLException{
		ranges.remove(compareRange);
		IncomeSimilarity[] arraySimilar = new IncomeSimilarity[201];
	    for(int i = 0; i < 201; i++){
	    	NameRange current = ranges.poll();
	    	arraySimilar[i] = new IncomeSimilarity(current.name, compareRanges(compareRange.range, current.range));
	    }
	    Arrays.sort(arraySimilar);
	    //List of the similar names
	    Vector<IncomeSimilarity> incomeSimilarity = new Vector<IncomeSimilarity>();
	    int size = arraySimilar.length;
	    for(int i = 0; i < size; i++){
	    	incomeSimilarity.add(i, arraySimilar[i]);
	    }
	    arraySimilar = null;
	    
	    //use add on the vector to add at the correct spot
	    while(!ranges.isEmpty()){
	    	NameRange current = ranges.poll();
	    	IncomeSimilarity currentSim = new IncomeSimilarity(current.name, compareRanges(compareRange.range, current.range));
	    	addSimilarityToVector(currentSim, incomeSimilarity, 199);
	    }
	    
	    arraySimilar = new IncomeSimilarity[200];
	    for(int i = 0; i < 200; i++){
	    	arraySimilar[i] = incomeSimilarity.get(i);
	    }

	    return arraySimilar;
	}
	
	private LinkedList<NameRange> getAllAverages(LinkedList<String> names) throws SQLException{
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		connection = connectDatabase("temp", "root", "password");
		
		LinkedList<NameRange> averages = new LinkedList<NameRange>();
		
		while(!names.isEmpty()){
			String currentName = names.poll();
	    	
	    	sql = "SELECT income_range_avg_1, income_range_avg_2, income_range_avg_3, income_range_avg_4"
	    			+ ", income_range_avg_5, income_range_avg_6, income_range_avg_7, income_range_avg_8"
	    			+ ", income_range_avg_9, income_range_avg_10 FROM surname_income_ranges_avg WHERE surname = ?";
	    	
	    	statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, currentName);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				double[] curAverage = new double[10];
				for(int i = 0; i < 10; i++){
					curAverage[i] = resultset.getDouble(i+1);
				}
				averages.add(new NameRange(currentName, curAverage));
			}
			resultset.close();
			statement.close();
		}
		
		return averages;
	}
	
	private LinkedList<NameRange> getAllAveragesForename(LinkedList<String> names) throws SQLException{
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		connection = connectDatabase("temp", "root", "password");
		
		LinkedList<NameRange> averages = new LinkedList<NameRange>();
		
		while(!names.isEmpty()){
			String currentName = names.poll();
	    	
	    	sql = "SELECT income_range_avg_1, income_range_avg_2, income_range_avg_3, income_range_avg_4"
	    			+ ", income_range_avg_5, income_range_avg_6, income_range_avg_7, income_range_avg_8"
	    			+ ", income_range_avg_9, income_range_avg_10 FROM forename_income_ranges_avg WHERE forename = ?";
	    	
	    	statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, currentName);
			resultset = statement.executeQuery();
			while (resultset.next()) {
				double[] curAverage = new double[10];
				for(int i = 0; i < 10; i++){
					curAverage[i] = resultset.getDouble(i+1);
				}
				averages.add(new NameRange(currentName, curAverage));
			}
			resultset.close();
			statement.close();
		}
		
		return averages;
	}
	
	public synchronized NameRange getRange(LinkedList<NameRange> list){
		if(list.isEmpty())
			return null;
		return list.poll();
	}
	
	public void compareAndAdd(LinkedList<NameRange> savedRanges, LinkedList<NameRange> iterRanges) throws SQLException{
		while(!iterRanges.isEmpty()){
		    NameRange currentRange = getRange(iterRanges);
		    if(currentRange == null)
		    	continue;
		    LinkedList<NameRange> inRanges = (LinkedList<NameRange>)savedRanges.clone();
		    System.out.println("Currently on: " + currentRange.name);
		    IncomeSimilarity[] similar = findSimilarNames(currentRange, inRanges);
		    
			String sql;
			PreparedStatement statement = null;
		    
			//add the list to the database
			sql = "INSERT INTO similar_incomes_surname (surname, surnameSimilar, incomeSimilarity) VALUES(?,?,?)";
			
			Connection connection = connectDatabase("phonebook", "root", "password");
			for(int i = 0; i < similar.length; i++){
				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);
				statement.setString(1, currentRange.name);
				statement.setString(2, similar[i].name);
				statement.setDouble(3, similar[i].similarity);
				
				statement.executeUpdate();
			}
			statement.close();
			connection.close();
	    }
	}
	
	public void findAllSimilarNames() throws IOException, SQLException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listNames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    //List of the names of people to search the similarity of
	    LinkedList<String> savedNameList = null;
	    try {
	    	savedNameList = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    
	    LinkedList<NameRange> iterRanges = getAllAverages(savedNameList);
	    LinkedList<NameRange> savedRanges = (LinkedList<NameRange>)iterRanges.clone();
	    NameRange iter = iterRanges.poll();
	    
	    //need to do benerofe, stop at benepe
	    //GIANNATTASIO, stop at giannasi
	    //TOCHTROP
	    //ULIN, stop at ULIK, ULICNY
	    while(!iter.name.equals("VANDENBROECK")){
	    	iter = iterRanges.poll();
	    }
	    iter = iterRanges.poll();
	    
	    NameIncomeSimilarityThread[] threads = new NameIncomeSimilarityThread[6];
	    System.out.println("Creating threads...");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i] = new NameIncomeSimilarityThread(savedRanges, iterRanges);
	    }
	    
	    System.out.println("Starting threads..");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i].start();
	    }
	}
	
	public void findAllSimilarForenames() throws IOException, SQLException{
		InputStream inFile = new FileInputStream("C:\\Users\\jlibarr1\\Downloads\\test\\listForenames25.ser");
	    InputStream inBuffer = new BufferedInputStream(inFile);
	    ObjectInput input = new ObjectInputStream (inBuffer);
	    
	    //List of the names of people to search the similarity of
	    LinkedList<String> savedNameList = null;
	    try {
	    	savedNameList = (LinkedList<String>)input.readObject();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
	    input.close();
	    inBuffer.close();
	    inFile.close();
	    
	    LinkedList<NameRange> iterRanges = getAllAveragesForename(savedNameList);
	    LinkedList<NameRange> savedRanges = (LinkedList<NameRange>)iterRanges.clone();
	    NameRange iter = iterRanges.poll();

	    NameIncomeSimilarityThread[] threads = new NameIncomeSimilarityThread[1];
	    for(int i = 0; i < threads.length; i++){
	    	threads[i] = new NameIncomeSimilarityThread(savedRanges, iterRanges);
	    }
	    
	    for(int i = 0; i < threads.length; i++){
	    	threads[i].start();
	    }
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
	
	public static void main(String[] args) throws NamingException, IOException, SQLException {
		NameIncomeScript test = new NameIncomeScript();
//		test.transferDB();
//		test.createAllIncomeRangesForename();
		test.findAllSimilarNames();
	}
}
