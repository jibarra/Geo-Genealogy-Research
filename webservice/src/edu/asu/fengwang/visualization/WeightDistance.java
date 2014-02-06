package edu.asu.fengwang.visualization;

public class WeightDistance implements Comparable<WeightDistance> {
//public class WeightDistance{
	public double distance;
	public double weight;
	public int id;

	@Override
	public int compareTo(WeightDistance o) {
		if (distance == o.distance) {
			return 0;
		} else if (distance > o.distance) {
			return 1;
		} else {
			return -1;
		}
	}

}
