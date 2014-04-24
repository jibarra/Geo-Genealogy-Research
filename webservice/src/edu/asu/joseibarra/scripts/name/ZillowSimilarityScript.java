/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Generate the similarity of zillow household value
 * for all the names within the database.
 */

package edu.asu.joseibarra.scripts.name;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
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

import edu.asu.joseibarra.scripts.resources.NameRange;
import edu.asu.joseibarra.scripts.threads.ZillowSimilarityThread;

public class ZillowSimilarityScript {
	
	/*
	 * Reads the names to input from a file.
	 */
	public LinkedList<String> readNames(){
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		LinkedList<String> names = new LinkedList<String>();
		
		try {
			 
			br = new BufferedReader(new FileReader(csvFile));
			while ((line = br.readLine()) != null) {
	 
			        // use comma as separator
				String[] lineName = line.split(cvsSplitBy);
	 
				names.add(lineName[1]);
			}
	 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (br != null) {
				try {
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return names;
	}
	
	/*
	 * Finds all the similar surnames, using the ZillowSimilarityThread class
	 */
	public void findAllSimilarSurnames() throws IOException, SQLException{
	    //List of the names of people to search the similarity of
	    LinkedList<String> savedNameList = readNames();

	    LinkedList<NameRange> iterRanges = getAllAverages(savedNameList);
	    LinkedList<NameRange> savedRanges = (LinkedList<NameRange>)iterRanges.clone();
	    
	    ZillowSimilarityThread[] threads = new ZillowSimilarityThread[6];
	    System.out.println("Creating threads...");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i] = new ZillowSimilarityThread(savedRanges, iterRanges);
	    }
	    
	    System.out.println("Starting threads..");
	    for(int i = 0; i < threads.length; i++){
	    	threads[i].start();
	    }
	}
	
	/*
	 * Get the averages of all the names.
	 */
	private LinkedList<NameRange> getAllAverages(LinkedList<String> names) throws SQLException{
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		connection = connectDatabase("phonebook", "root", "password");
		
		LinkedList<NameRange> averages = new LinkedList<NameRange>();
		
		while(!names.isEmpty()){
			String currentName = names.poll();
	    	
	    	sql = "SELECT income_range_avg_1, income_range_avg_2, income_range_avg_3, income_range_avg_4"
	    			+ ", income_range_avg_5, income_range_avg_6, income_range_avg_7, income_range_avg_8"
	    			+ ", income_range_avg_9, income_range_avg_10 FROM surname_zillow_income_ranges_avg WHERE surname = ?";
	    	
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
	
	private final String csvFile = "C:\\Users\\jlibarr1\\Downloads\\textRepNames25.csv";
	
	public static void main(String[] args)
	{
		ZillowSimilarityScript script = new ZillowSimilarityScript();
		try {
			script.findAllSimilarSurnames();
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
	}
}
