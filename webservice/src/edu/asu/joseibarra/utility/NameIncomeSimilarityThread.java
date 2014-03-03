package edu.asu.joseibarra.utility;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Vector;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

public class NameIncomeSimilarityThread extends Thread{
	private LinkedList<NameRange> savedRanges;
	private LinkedList<NameRange> iterRanges;

	public NameIncomeSimilarityThread(LinkedList<NameRange> savedRanges, LinkedList<NameRange> iterRanges) {
		this.savedRanges = savedRanges;
		this.iterRanges = iterRanges;
	}
	
	
	public void run(){
		compareAndAdd();
	}
	
	public synchronized NameRange getRange(LinkedList<NameRange> list){
		if(list.isEmpty())
			return null;
		return list.poll();
	}
	
	public void compareAndAddForename(){
		Connection connection = connectDatabase("phonebook", "root", "password");
		while(!iterRanges.isEmpty()){
		    NameRange currentRange = getRange(iterRanges);
		    if(currentRange == null)
		    	continue;
		    LinkedList<NameRange> inRanges = (LinkedList<NameRange>)savedRanges.clone();
		    System.out.println("Currently on: " + currentRange.name);
		    IncomeSimilarity[] similar = null;
			try {
				similar = findSimilarNames(currentRange, inRanges);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		    
			String sql;
			PreparedStatement statement = null;
		    
			//add the list to the database
			sql = "INSERT INTO similar_incomes_surname (forename, forenameSimilar, incomeSimilarity) VALUES(?,?,?)";
			
			for(int i = 0; i < similar.length; i++){
				try {
					statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
							java.sql.ResultSet.CONCUR_READ_ONLY);
					statement.setString(1, currentRange.name);
					statement.setString(2, similar[i].name);
					statement.setDouble(3, similar[i].similarity);
					
					statement.executeUpdate();
					
				} catch (SQLException e) {
					e.printStackTrace();
				} 
			}
			try {
				statement.close();
				connection.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    }
	}
	
	public void compareAndAdd(){
		System.out.println("Starting");
		Connection connection = connectDatabase("phonebook", "root", "password");
		while(!iterRanges.isEmpty()){
		    NameRange currentRange = getRange(iterRanges);
		    if(currentRange == null)
		    	continue;
			String sql;
			PreparedStatement statement = null;

//			System.out.println("Checking " + currentRange.name);
//			sql = "SELECT COUNT(*) FROM similar_incomes_surname WHERE surname=?";
//			ResultSet resultset;
//			try {
//				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
//						java.sql.ResultSet.CONCUR_READ_ONLY);
//				statement.setString(1, currentRange.name);
//				resultset = statement.executeQuery();
//				resultset.next();
//				if(resultset.getInt(1) > 0)
//					continue;
//			} catch (SQLException e1) {
//				e1.printStackTrace();
//			}
			
		    LinkedList<NameRange> inRanges = (LinkedList<NameRange>)savedRanges.clone();
		    System.out.println("Currently on: " + currentRange.name);
		    IncomeSimilarity[] similar = null;
			try {
				similar = findSimilarNames(currentRange, inRanges);
			} catch (SQLException e) {
				e.printStackTrace();
			}
		    
		    
			//add the list to the database
			sql = "INSERT INTO similar_incomes_surname (surname, surnameSimilar, incomeSimilarity) VALUES(?,?,?)";
			
			for(int i = 0; i < similar.length; i++){
				try {
					statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
							java.sql.ResultSet.CONCUR_READ_ONLY);
					statement.setString(1, currentRange.name);
					statement.setString(2, similar[i].name);
					statement.setDouble(3, similar[i].similarity);

					statement.executeUpdate();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			try {
				statement.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}

	    }
		
		try {
			connection.close();
		} catch (SQLException e) {
			e.printStackTrace();
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
}
