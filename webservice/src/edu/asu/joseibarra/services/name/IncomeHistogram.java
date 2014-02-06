package edu.asu.joseibarra.services.name;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.LinkedList;
import java.util.Arrays;

import javax.imageio.ImageIO;
import javax.naming.NamingException;

import edu.asu.wangfeng.surname.service.resources.WFQuery;

public class IncomeHistogram extends WFQuery{
	
	private double maxMeanIncome;
	private double minMeanIncome;
	private double maxMedianIncome;
	private double minMedianIncome;
	private final int histogramBins = 10;
	private final String dir = "C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\webservice\\image\\histogram\\";
	private final int maxGraphWidth = 500;
	private final int graphHeight = 500;
	private final int imageRightBuffer = 50;
	private final int imageLeftBuffer = 50;
	private final int imageTopBuffer = 50;
	private final int imageBottomBuffer = 50;
	
	public void calculateMinMaxIncome(LinkedList<Double> meanList, LinkedList<Double> medianList){
		double min = 999999999;
    	double max = -999999999;
    	
    	for(Double num : meanList){
    		if(num.intValue() > max)
    			max = num;
    		if(num.intValue() < min)
    			min = num;
    	}
    	
    	maxMeanIncome = max;
    	minMeanIncome = min;
    	min = 999999999;
    	max = -999999999;
    	
    	for(Double num : medianList){
    		if(num.intValue() > max)
    			max = num;
    		if(num.intValue() < min)
    			min = num;
    	}
    	
    	maxMedianIncome = max;
    	minMedianIncome = min;
	}
	
	public String drawHistogram(String dir, int[] bins){
		String fileLoc = "";
		return fileLoc;
	}
	
	public String formatMoneyForThousands(double num){
		String money = "$";
		int thousands = (int)Math.floor(num / 1000);
//		System.out.println(num-thousands);
		int decimal = (int)Math.round((num-thousands*1000)/100);
		money += thousands + "." + decimal + "K";
		return money;
	}
	
	public void calculateHistogram(String name, String nameType) throws IOException{
		
		Connection connection = null;
		String sql;
		PreparedStatement statement = null;
		ResultSet resultset = null;
		
		LinkedList<Double> meanIncomes = new LinkedList<Double>();
		LinkedList<Double> medianIncomes = new LinkedList<Double>();
		
		long start = System.nanoTime();
		try {
			connection = connectDatabase();
			//SQL LOCATED HERE
			sql = "SELECT c.mean_income, c.median_income"
					+ " FROM phonebook as p, census as c"
					+ " WHERE p." + nameType + " = ? "
					+ " AND c.census_tract_id = p.geoid_census_tract";
			statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
					java.sql.ResultSet.CONCUR_READ_ONLY);

			statement.setString(1, name);
			
			resultset = statement.executeQuery();
			while (resultset.next()) {
				meanIncomes.add(resultset.getDouble(1));
				medianIncomes.add(resultset.getDouble(2));
			}
			resultset.close();
			statement.close();
			connection.close();
		} catch (NamingException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
				
		long end = System.nanoTime();
    	System.out.println("Time to execute and store " + name + " histogram incomes: "
		+ ((end-start) / 1000000000.0) + " seconds");
    	
    	start = System.nanoTime();
    	
    	calculateMinMaxIncome(meanIncomes, medianIncomes);
    	
    	end = System.nanoTime();
    	System.out.println("Time to execute and store " + name + " histogram incomes min/max: "
    			+ ((end-start) / 1000000000.0) + " seconds");
    	
    	int[] meanBins = new int[histogramBins];
    	Arrays.fill(meanBins, 0);
    	double divisor = (maxMeanIncome - minMeanIncome + 1) / histogramBins;
    	int bin = -1;
    	
    	for(Double num : meanIncomes){
    		bin = (int)Math.floor(num/divisor);
    		if(bin >= meanBins.length)
    			bin=9;
    		meanBins[bin]++;
    	}
    	
    	int max = -1;
    	for(int i = 0; i < meanBins.length; i++){
    		int num = meanBins[i];
    		System.out.println(i + ": " + num);
    		if(num > max)
    			max = num;
    	}
    	 	
    	
    	int barWidth = maxGraphWidth/histogramBins;
		int graphWidth = barWidth*histogramBins;
    	double pixelHeight = (double)max / graphHeight;
		BufferedImage histogramImage = new BufferedImage(maxGraphWidth, graphHeight, BufferedImage.TYPE_INT_ARGB);
		
		int white = 0xFFFFFFFF;
		int black = 0xFF000000;
		int color = 0xFFB10026;
		
		int[] barHeights = new int[histogramBins];
		Arrays.fill(barHeights, -1);
		
		//Draw the graphs from left to right, bottom to top (since y 
		for(int i = 0; i < graphWidth; i++){
			for(int j = 0; j < graphHeight; j++){
				int binPos = i/barWidth;
				double maxBlack = (j+1)*pixelHeight;
				if((meanBins[binPos]) >= maxBlack){
					if((i%barWidth == 0) || ((i+1)%barWidth == 0)){
						histogramImage.setRGB(i, graphHeight-j-1, black);
					}
					else if(((meanBins[binPos]) == maxBlack)||
							((meanBins[binPos]) < (j+2)*pixelHeight)){
						histogramImage.setRGB(i, graphHeight-j-1, black);
					}
					else{
						histogramImage.setRGB(i, graphHeight-j-1, color);
					}
				}
				else{
					if(barHeights[binPos] == -1)
						barHeights[binPos] = j;
					histogramImage.setRGB(i, graphHeight-j-1, white);
				}
			}
		}
		
		//Color the edges black
		//Color the y-axis black
		for(int i = 0; i < graphHeight; i++){
			histogramImage.setRGB(0, i, black);
		}
		//Color the x-axis black
		for(int i = 0; i < graphWidth; i++){
			histogramImage.setRGB(i, graphHeight-1, black);
		}
		
		int imageWidth = maxGraphWidth + imageLeftBuffer + imageRightBuffer;
    	int imageHeight = graphHeight + imageTopBuffer + imageBottomBuffer;
		
		BufferedImage newImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = newImage.createGraphics();
		graphics.drawImage(histogramImage, imageLeftBuffer, imageTopBuffer, null);
		graphics.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
		graphics.setPaint(Color.black);
		graphics.setColor(Color.black);
		FontMetrics fm = graphics.getFontMetrics();
		
		double binValue = (maxMeanIncome-minMeanIncome) / histogramBins;
		int middleOfBar = maxGraphWidth / histogramBins;
		int lineLabel = imageHeight-imageBottomBuffer+fm.getHeight();
		int offsetLineLabel = lineLabel+20;
		
		for(int i = 0; i <= histogramBins; i++){
			String money = formatMoneyForThousands(binValue*(i)+minMeanIncome);
			if(i%2==0)
				graphics.drawString(money, imageLeftBuffer + (i*middleOfBar)-(fm.stringWidth(money)/2), 
						lineLabel);
			else
				graphics.drawString(money, imageLeftBuffer + (i*middleOfBar)-(fm.stringWidth(money)/2), 
						offsetLineLabel);
		}
		
		int yStart = imageHeight-imageBottomBuffer;
		int yEnd = imageHeight-imageBottomBuffer-fm.getHeight() + 20;
		int yOffsetEnd = yEnd+20;
		for(int i = 1; i < histogramBins; i++){
			int x = imageLeftBuffer + (i*middleOfBar);
			if(i%2==0){
				graphics.drawLine(x, yStart, x, yEnd);
				graphics.drawLine(x-1, yStart, x-1, yEnd);
			}else{
				graphics.drawLine(x, yStart, x, yOffsetEnd);
				graphics.drawLine(x-1, yStart, x-1, yOffsetEnd);
			}
		}
		
		graphics.drawLine(imageLeftBuffer, yStart, imageLeftBuffer, yEnd);
		if((histogramBins % 2) == 0){
			graphics.drawLine(imageLeftBuffer + (middleOfBar*histogramBins) - 1, yStart, 
					imageLeftBuffer + (middleOfBar*histogramBins) - 1, yEnd);
		}
		else{
			graphics.drawLine(imageLeftBuffer + (middleOfBar*histogramBins) - 1, yStart, 
					imageLeftBuffer + (middleOfBar*histogramBins) - 1, yOffsetEnd);
		}
		
		
		
		
		
		
		
		File histogramFile = null;
		String imagePath = "";
		synchronized (this) {
			Date date = new Date();
			String baseString = date.getTime() + "-";
			// we only allow 1000 concurrency
			for (int i = 0; i < 1000; i++) {
				imagePath = baseString + i + ".png";
				histogramFile = new File(dir + imagePath);
				if (!histogramFile.exists()) {
					break;
				}
			}
		}
		ImageIO.write(newImage, "PNG", histogramFile);
		System.out.println(histogramFile.getAbsolutePath());
    	
    	int[] medianBins = new int[histogramBins];
	}
	
	public double getMaxMeanIncome(){
		return maxMeanIncome;
	}
	
	public double getMinMeanIncome(){
		return minMeanIncome;
	}
	
	public double getMaxMedianIncome(){
		return maxMedianIncome;
	}
	
	public double getMinMedianIncome(){
		return minMedianIncome;
	}
	
	public static void main(String[] args){
		IncomeHistogram income = new IncomeHistogram();
		try {
			income.calculateHistogram("maciejewski", "surname");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		System.out.println("Mean: Max: " + income.getMaxMeanIncome() + " Min: " + income.getMinMeanIncome());
		System.out.println("Median: Max: " + income.getMaxMedianIncome() + " Min: " + income.getMinMedianIncome());
	}
}
