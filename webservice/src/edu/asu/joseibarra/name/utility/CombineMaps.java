/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * This class allows for the combination of two maps
 * into a new map file.
 */

package edu.asu.joseibarra.name.utility;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;

import javax.imageio.ImageIO;

import edu.asu.joseibarra.geo.LatLng;
import edu.asu.wangfeng.service.netbeans.BuildResultBean;

public class CombineMaps {
	public final int zLevel = 4;
	public final LatLng topLeft = new LatLng(61.62728630386718, -133.64112899999998);
	
	/*
	 * Combines two images into a new BufferedImage, given that
	 * they are the same width and height The color between the two
	 * can be ANDed together or ORed together.
	 */
	public BufferedImage combineMapsIntoImage(int width, int height, BufferedImage kdeImage1, 
			BufferedImage kdeImage2)
			throws IOException{
		if(width <= 0 || height <= 0 || kdeImage1 == null || kdeImage2 == null)
			return null;
		
//		//Get the colors in the image
//		HashMap<Integer, Integer> colors1 = new HashMap<Integer, Integer>();
//		HashMap<Integer, Integer> colors2 = new HashMap<Integer, Integer>();
//		for(int y = 0; y < height; y++){
//			for(int x = 0; x < width; x++){
//				if(!colors1.containsKey(kdeImage1.getRGB(x, y)))
//					colors1.put(kdeImage1.getRGB(x, y), 0);
//				if(!colors2.containsKey(kdeImage2.getRGB(x, y)))
//					colors2.put(kdeImage2.getRGB(x, y), 0);
//			}
//		}
//		
//		//Remove the "blank" color spot
//		colors1.remove(0);
//		colors2.remove(0);
//		
//		//Put the colors into an array
//		Set<Integer> colorsSet = colors1.keySet();
//		Integer[] orderedColors1 = new Integer [colorsSet.size()];
//		int iter = 0;
//		for(Integer val : colorsSet){
//			orderedColors1[iter] = val;
//			iter++;
//		}
//		
//		colorsSet = colors2.keySet();
//		Integer[] orderedColors2 = new Integer [colorsSet.size()];
//		iter = 0;
//		for(Integer val : colorsSet){
//			orderedColors2[iter] = val;
//			iter++;
//		}	
//		
//		colors1 = null;
//		colors2 = null;
//		
//		//Order the colors
//		Arrays.sort(orderedColors1);
//		Arrays.sort(orderedColors2);
//		
//		for(Integer val : orderedColors1)
//			System.out.println(val);
//		System.out.println();
//		for(Integer val : orderedColors2)
//			System.out.println(val);
		
		//Create an image of the two images combined, discounting pixels not colored in both images
		//Coloring in the new image is done by ANDing/ORing the colors of the two inputted images
		//An AND retains the coloration of the original images, combining the colors where needed
		//OR changes the colors to another scheme but may show a better heatmap of the overlap
		BufferedImage combinedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		for(int y = 0; y < height; y++){
			for(int x = 0; x < width; x++){
				if((kdeImage1.getRGB(x, y) != 0) && (kdeImage2.getRGB(x, y) != 0)){
					combinedImage.setRGB(x, y, kdeImage1.getRGB(x, y) & kdeImage2.getRGB(x, y));
//					combinedImage.setRGB(x, y, kdeImage1.getRGB(x, y) | kdeImage2.getRGB(x, y));
				}
			}
		}
		
		return combinedImage;
	}
	
	/*
	 * Combine two given map locations into a new map, and return a File of that
	 * new map. Uses the combineMapsIntoImage method above.
	 */
	public File combineMapsIntoFile(String map1Loc, String map2Loc, String mapDir1, String mapDir2, 
			String outputDir) throws IOException{
		
		//Create the file and check if it doesn't exist
		File imageMap1 = new File(mapDir1 + map1Loc);
		File imageMap2 = new File(mapDir2 + map2Loc);
		if(!imageMap1.exists() || !imageMap2.exists()){
			System.out.println("Does not exist");
			return null;
		}
		
		//Create an image from teh file
		BufferedImage kdeImage1 = ImageIO.read(imageMap1);
		BufferedImage kdeImage2 = ImageIO.read(imageMap2);
		//If the iamges don't have the same dimensions, return invalid
		if((kdeImage1.getHeight() != kdeImage2.getHeight()) || (kdeImage1.getWidth() != kdeImage2.getWidth())){
			System.out.println("Not same size");
			return null;
		}
		
		//get the dimensions of the image
		int width = kdeImage1.getWidth();
		int height = kdeImage2.getHeight();
		
		
		//Create an image of the two images combined, discounting pixels not colored in both images
		//Coloring in the new image is done by ANDing/ORing the colors of the two inputted images
		//An AND retains the coloration of the original images, combining the colors where needed
		//OR changes the colors to another scheme but may show a better heatmap of the overlap
		BufferedImage combinedImage = combineMapsIntoImage(width, height, kdeImage1, kdeImage2);
		
		if(combinedImage == null){
			System.out.println("Not same size");
			return null;
		}
		
		File combinedFile = null;
		String imagePath = "";
		synchronized (this) {
			Date date = new Date();
			String baseString = date.getTime() + "-";
			// we only allow 1000 concurrency
			for (int i = 0; i < 1000; i++) {
				imagePath = baseString + i + ".png";
				combinedFile = new File(outputDir + imagePath);
				if (!combinedFile.exists()) {
					break;
				}
			}
		}
		ImageIO.write(combinedImage, "PNG", combinedFile);
		return combinedFile;
	}
	
	/*
	 * Returns a Bean of two combiend maps, given the location of two
	 * maps to combine.
	 */
	public BuildResultBean combineMapsCapture(String map1Loc, String map2Loc, String mapDir1, String mapDir2, 
			int zoom, LatLng topLeftLatLng, String outputDir) throws IOException{
		BuildResultBean result;
		
		//Create the file and check if it doesn't exist
		File imageMap1 = new File(mapDir1 + map1Loc);
		File imageMap2 = new File(mapDir2 + map2Loc);
		if(!imageMap1.exists() || !imageMap2.exists()){
			System.out.println("Does not exist");
			result = new BuildResultBean();
			result.setFilename("blank.png");
			result.setUrl(outputDir);
			return result;
		}
		
		//Create an image from teh file
		BufferedImage kdeImage1 = ImageIO.read(imageMap1);
		BufferedImage kdeImage2 = ImageIO.read(imageMap2);
		//If the iamges don't have the same dimensions, return invalid
		if((kdeImage1.getHeight() != kdeImage2.getHeight()) || (kdeImage1.getWidth() != kdeImage2.getWidth())){
			System.out.println("Not same size");
			System.out.println("Does not exist");
			result = new BuildResultBean();
			result.setFilename("blank.png");
			result.setUrl(outputDir);
			return result;
		}
		
		//get the dimensions of the image
		int width = kdeImage1.getWidth();
		int height = kdeImage2.getHeight();
		
		
		//Create an image of the two images combined, discounting pixels not colored in both images
		//Coloring in the new image is done by ANDing/ORing the colors of the two inputted images
		//An AND retains the coloration of the original images, combining the colors where needed
		//OR changes the colors to another scheme but may show a better heatmap of the overlap
		BufferedImage combinedImage = combineMapsIntoImage(width, height, kdeImage1, kdeImage2);
		
		if(combinedImage == null){
			System.out.println("Not same size");
			result = new BuildResultBean();
			result.setFilename("blank.png");
			result.setUrl(outputDir);
			return result;
		}
		
		File combinedFile = null;
		String imagePath = "";
		synchronized (this) {
			Date date = new Date();
			String baseString = date.getTime() + "-";
			// we only allow 1000 concurrency
			for (int i = 0; i < 1000; i++) {
				imagePath = baseString + i + ".png";
				combinedFile = new File(outputDir + imagePath);
				if (!combinedFile.exists()) {
					break;
				}
			}
		}
		ImageIO.write(combinedImage, "PNG", combinedFile);
				
		//Add the combined image to a static image
		NameMapBuilder builder = new NameMapBuilder();
		result = builder.nameMapBuilder(combinedFile, imagePath, width, height, 
				topLeftLatLng, zoom, outputDir, "combined");
		
		return result;
	}
	
	
	public static void main(String[] args){
		CombineMaps combine = new CombineMaps();
		String mapDir1 = "C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\webservice\\image\\kdecachesurname\\";
		String mapDir2 = "C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\webservice\\image\\kdecacheforename\\";
		String outDir = "C:\\Users\\jlibarr1\\Documents\\Code\\Facebook Code\\.metadata\\.plugins\\org.eclipse.wst.server.core\\tmp0\\wtpwebapps\\webservice\\image\\uploadCombined\\";
		BuildResultBean result = new BuildResultBean();
		try {
			result = combine.combineMapsCapture("surnameIBARRAw1301h680z4clat34.939486447555986clng-97.07862899999998.png", 
					"forenameJOSEw1301h680z4clat34.939486447555986clng-97.07862899999998.png", mapDir1, mapDir2,
					combine.zLevel, combine.topLeft, outDir);
		} catch (IOException e) {
			e.printStackTrace();
		}
		System.out.println(result.getUrl());	
	}
}
