package edu.asu.wangfeng.surname.service.netbeans;

public class QueryFullNameBean {
	private String surnameImage;
	private String forenameImage;
	private String fullNameImage;
	private int surnameNumber;
	private int forenameNumber;
	private int fullNameNumber;
	private double surnameAverageMean;
	private double surnameAverageMedian;
	private double forenameAverageMean;
	private double forenameAverageMedian;
	
	public String getSurnameImage() {
		return surnameImage;
	}
	public void setSurnameImage(String image) {
		this.surnameImage = image;
	}
	public String getForenameImage() {
		return forenameImage;
	}
	public void setForenameImage(String image) {
		this.forenameImage = image;
	}
	public String getFullNameImage() {
		return fullNameImage;
	}
	public void setFullNameImage(String image) {
		this.fullNameImage = image;
	}
	public int getSurnameNumber() {
		return surnameNumber;
	}
	public void setSurnameNumber(int number) {
		this.surnameNumber = number;
	}
	public int getForenameNumber() {
		return forenameNumber;
	}
	public void setForenameNumber(int number) {
		this.forenameNumber = number;
	}
	public int getFullNameNumber() {
		return fullNameNumber;
	}
	public void setFullNameNumber(int number) {
		this.fullNameNumber = number;
	}
	public double getSurnameAverageMean(){
		return surnameAverageMean;
	}
	public void setSurnameAverageMean(double averageMean){
		this.surnameAverageMean = averageMean;
	}
	public double getSurnameAverageMedian(){
		return surnameAverageMedian;
	}
	public void setSurnameAverageMedian(double averageMedian){
		this.surnameAverageMedian = averageMedian;
	}
	public double getForenameAverageMean(){
		return forenameAverageMean;
	}
	public void setForenameAverageMean(double averageMean){
		this.forenameAverageMean = averageMean;
	}
	public double getForenameAverageMedian(){
		return forenameAverageMedian;
	}
	public void setForenameAverageMedian(double averageMedian){
		this.forenameAverageMedian = averageMedian;
	}
	@Override
	public String toString() {
		return "QueryBean [surname image=" + surnameImage + ", surname number=" + surnameNumber + "]";
	}
}
