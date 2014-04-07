package edu.asu.wangfeng.service.netbeans;

public class IncomeComparisonBean implements Comparable<IncomeComparisonBean>{
	public String name;
	public double closeness;
	
	public IncomeComparisonBean() {
	}
	
	public IncomeComparisonBean(String name, double closeness) {
		this.name = name;
		this.closeness = closeness;
	}
	@Override
	public int compareTo(IncomeComparisonBean b) {
		if(closeness > b.closeness) {
			return -1;
		}
		if(closeness < b.closeness) {
			return 1;
		}
		return 0;
	}
}
