package edu.asu.joseibarra.scripts.name;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import edu.asu.joseibarra.name.utility.NameIncome;

public class ZillowIncomeScript {
	private final String csvFile = "C:\\Users\\jlibarr1\\Downloads\\textRepNames25.csv";
	
	
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
	
	public void addNamesToZillowTable(LinkedList<String> names){
//		NameIncome query = new NameIncome();
		for(String name : names){
			
		}
	}
	
	public static void main(String[] args){
		ZillowIncomeScript script = new ZillowIncomeScript();
		LinkedList<String> names = script.readNames();
		for(String str : names){
			System.out.println(str);
		}
	}
}
