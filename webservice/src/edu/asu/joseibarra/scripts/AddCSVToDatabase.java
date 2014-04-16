package edu.asu.joseibarra.scripts;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.LinkedList;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import com.mysql.jdbc.exceptions.jdbc4.MySQLIntegrityConstraintViolationException;

import edu.asu.joseibarra.scripts.resources.CensusTractIncomePercent;


public class AddCSVToDatabase {
	public static void main (String[] args) throws SQLException{
		AddCSVToDatabase test = new AddCSVToDatabase();
		test.addUStoDatabase();
	}
	
	public void addUStoDatabase() throws SQLException{
		String csvFile = "C:\\Users\\jlibarr1\\Desktop\\temp\\data\\ACS_12_5YR_S1901 ";
		for(int i = 1; i <= 51; i++){
			LinkedList<CensusTractIncomePercent> list = readCSV(csvFile + "(" + i +").csv");
			addLinkedListToDB(list);
		}
	}
	
	public LinkedList<CensusTractIncomePercent> readCSV(String fileLocation){
		LinkedList<CensusTractIncomePercent> list = new LinkedList<CensusTractIncomePercent>();
		BufferedReader br = null;
		String line = "";
		String cvsSplitBy = ",";
		System.out.println("Currently reading file: " + fileLocation);
		
		try {
			br = new BufferedReader(new FileReader(fileLocation));
			br.readLine();
			int count = 1;
			while ((line = br.readLine()) != null) {
				count++;
				if(line.length() > 1){
					String[] data = line.split(cvsSplitBy);
					if(data.length > 85){
						String tract_id = data[1];
						float incomes[] = new float[10];
						int spot = 0;
						for(int i = 13; i <= 85; i+=8){
							incomes[spot] = Float.parseFloat(data[i]);
							spot++;
						}
						CensusTractIncomePercent area = new CensusTractIncomePercent(tract_id, incomes);
						list.add(area);
					}
				}
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
		
		return list;
	}
	
	public void addLinkedListToDB(LinkedList<CensusTractIncomePercent> list) throws SQLException{
		Connection connection = connectMySQLDatabase("phonebook","root", "password");
		CensusTractIncomePercent area = null;
		String insertSQL = "INSERT INTO census_income_ranges(census_tract_id, income_range_1, income_range_2,"
				+ " income_range_3, income_range_4, income_range_5, income_range_6, income_range_7,"
				+ " income_range_8, income_range_9, income_range_10)"
				+ " VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		PreparedStatement insertStatement = null;
		area = list.poll();
		while(area != null){
			insertStatement = connection.prepareStatement(insertSQL, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);
			insertStatement.setString(1, area.tract_id);
			insertStatement.setDouble(2, area.incomes[0]);
			insertStatement.setDouble(3, area.incomes[1]);
			insertStatement.setDouble(4, area.incomes[2]);
			insertStatement.setDouble(5, area.incomes[3]);
			insertStatement.setDouble(6, area.incomes[4]);
			insertStatement.setDouble(7, area.incomes[5]);
			insertStatement.setDouble(8, area.incomes[6]);
			insertStatement.setDouble(9, area.incomes[7]);
			insertStatement.setDouble(10, area.incomes[8]);
			insertStatement.setDouble(11, area.incomes[9]);
			try{
				insertStatement.executeUpdate();
			}
			catch(MySQLIntegrityConstraintViolationException ex){
				System.out.println("Could not find tract id " + area.tract_id);
			}
			area = list.poll();
		}
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
