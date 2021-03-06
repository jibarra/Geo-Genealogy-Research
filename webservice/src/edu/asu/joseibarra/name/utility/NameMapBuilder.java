/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * � Arizona State University 2014
 * 
 * Generates a static image of for a heatmap file.
 * This will allow portability of the heatmap, instead of
 * a basic image lay over.
 */

package edu.asu.joseibarra.name.utility;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import edu.asu.joseibarra.geo.GoogleMercator;
import edu.asu.joseibarra.geo.LatLng;
import edu.asu.wangfeng.google.staticmaps.GoogleStaticMapsGenerator;
import edu.asu.wangfeng.service.netbeans.BuildResultBean;

public class NameMapBuilder {
	public BuildResultBean nameMapBuilder(File imageFile, String imageFilename, int width, int height, 
			LatLng topLeftLatLng, int zoom, String resultDir, String imageFolderName) throws IOException{
		BuildResultBean result = new BuildResultBean();
		BufferedImage kdeImage = ImageIO.read(imageFile);
		// build base map
		int xnum = width / 640;
		int ynum = height / 640;
		if (width % 640 != 0) {
			xnum++;
		}
		if (height % 640 != 0) {
			ynum++;
		}
		if(xnum == 0 || ynum == 0 || height == 0 || width == 0){
			result.setFilename("blank.png");
			result.setUrl("image/" + imageFolderName + "/blank.png");
			return result;
		}
		int unitWidth = width / xnum;
		int unitHeight = height / ynum;
		GoogleStaticMapsGenerator mapGenerator = new GoogleStaticMapsGenerator();
		mapGenerator.setZoom(zoom);
		BufferedImage resultImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = resultImage.createGraphics();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(zoom);
		Point topLeft = mercator.fromLatLngToPoint(topLeftLatLng);
		for (int i = 0; i < xnum; i++) {
			int innerWidth = unitWidth;
			if (i == (xnum - 1)) {
				innerWidth += (width - xnum * unitWidth);
			}
			for (int k = 0; k < ynum; k++) {
				int innerHeight = unitHeight;
				if (k == (ynum - 1)) {
					innerHeight += (height - ynum * unitHeight);
				}
				// get center lat/lng for current tile
				Point curCenter = new Point();
				curCenter.x = (unitWidth * i + innerWidth / 2) + topLeft.x;
				curCenter.y = (unitHeight * k + innerHeight / 2) + topLeft.y;
				LatLng latlng = mercator.fromPointToLatLng(curCenter);
				Point2D.Double latlngCenter = new Point2D.Double(latlng.lng(), latlng.lat());
				mapGenerator.setCenter(latlngCenter);
				mapGenerator.setHeight(innerHeight);
				mapGenerator.setWidth(innerWidth);
				java.net.URL tileURL = mapGenerator.generateURL();
				BufferedImage tileImage = ImageIO.read(tileURL);
				graphics.drawImage(tileImage, unitWidth * i, unitHeight * k, null);
			}
		}
		// overlay the image
		float[] scales = {1f, 1f, 1f, 0.4f};
		float[] offsets = {0, 0, 0, 0};
		RescaleOp rop = new RescaleOp(scales, offsets, null);
		graphics.drawImage(kdeImage, rop, 0, 0);
		String resultFilename = resultDir +  imageFilename;
		ImageIO.write(resultImage, "png", new File(resultFilename));
		result.setFilename(imageFilename);
		result.setUrl("image/" + imageFolderName + "/" + imageFilename);
		return result;
	}
}
