package edu.asu.joseibarra.utility;

import java.awt.Point;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.naming.NamingException;

import edu.asu.joseibarra.services.name.NameMapBuilder;
import edu.asu.wangfeng.geo.GoogleMercator;
import edu.asu.wangfeng.geo.LatLng;
import edu.asu.wangfeng.surname.service.netbeans.BuildResultBean;

public class CreateSmallMaps {

	String[] names = {"SHEA", "ROUSE", "IBARRA", "MACIEJEWSKI", "FENG", "JAUREGUI", "KIMES", "RANKIN", "WELLS", "MILLER", 
			"TRAN", "HAWORTH", "CHAPLIN", "WAITS", "VALENTINO", "TARR", "BUDD", "WALZ", "PIGG", "JOSLIN",
			"EDENS", "TOWER", "RAE", "SEYMORE", "ROHR", "WESTER", "BINKLEY", "WEIS", "BOLAND", "ENG",
			"LOGUE", "TILLER", "MAAS", "KASPER", "SPINKS", "MATHIAS", "JENSON", "PETTIS", "BUI", "FALLS",
			"WILKE", "TAPP", "SILVERS", "AUGUST", "MATTOS", "LEAK", "GIRON", "TEJEDA", "FERNANDEZ", "BACA",
			"ALONSO", "MADRID", "BURNS", "COTTON", "CLAYTON", "BRIDGES", "LYNCH", "CHUA", "YEH", "PEREZ",
			"FUNG", "CHOE", "YUN", "JIN", "FELDMAN", "HAGEN", "POLLARD", "COLEMAN", "WILLS", "POST",
			"SHELLY", "SACCO", "LUONG", "LUIS", "KNOTTS", "HEINRICH", "FOREST", "SETTLES", "REY", "NOVOTNY",
			"NEMETH", "SEGAL", "PHILIPS", "WOLCOTT", "EADS", "VOLZ", "SILAS", "CHIU", "CHINN", "CLEMENTE",
			"BOWLER", "HOFMAN", "SOPER", "RODMAN", "PEDIGO", "LUNN", "LOHR", "LAHR", "JURADO", "LILIENTHAL"
	};
	
	public void generateMaps() throws IOException {
		KDEPainterEfficient painter = new KDEPainterEfficient();
		GoogleMercator mercator = new GoogleMercator();
		mercator.setZoom(2);
		LatLng sw = new LatLng(25.109438734497637, -125.58281249999999  );
		LatLng ne = new LatLng(49.79825950635239, -65.81718749999999);
		painter.setRightBottomPixel(mercator.fromLatLngToPoint(new LatLng(25.109438734497637, -65.81718749999999)));
		painter.setLeftTopPixel(mercator.fromLatLngToPoint(new LatLng(49.79825950635239, -125.58281249999999  )));
		painter.setZoom(2);
		painter.setBandwidth(240);
		
		//Center lat: 37.4675118303818, lng: -95.69999999999999  
		
		for(int i = 0; i < names.length; i++){
			Vector<LatLng> coordinateVec = new Vector<LatLng>();
			Connection connection = null;
			String sql;
			PreparedStatement statement = null;
			ResultSet resultset = null;
			
			long start = System.nanoTime();
			try {
				connection = connectMySQLDatabase("phonebook","root", "password");
				//SQL LOCATED HERE
				sql = "select latitude, longitude from phonebook where surname=? and latitude between ? and ? and longitude between ? and ?";

				statement = connection.prepareStatement(sql, java.sql.ResultSet.TYPE_FORWARD_ONLY,
						java.sql.ResultSet.CONCUR_READ_ONLY);

				statement.setString(1, names[i]);
				statement.setDouble(2, sw.lat());
				statement.setDouble(3, ne.lat());
				statement.setDouble(4, sw.lng());
				statement.setDouble(5, ne.lng());
				System.out.println(statement);
				
				resultset = statement.executeQuery();
				while (resultset.next()) {
					double lat = resultset.getDouble(1);
					double lng = resultset.getDouble(2);
					coordinateVec.add(new LatLng(lat, lng));
				}
				resultset.close();
				statement.close();
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
			painter.setSampleList(coordinateVec);
			System.out.println("Generating files for " + names[i] + "...");
			painter.setFilename("C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\" + names[i] +".png");
			painter.drawKDEMap(names[i]);
		    
		    NameMapBuilder builder = new NameMapBuilder();
		    BuildResultBean result = builder.nameMapBuilder(new File ("C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\" + names[i] +".png"), names[i] + "WithBG.png", 170, 90, 
		    		new LatLng(49.79825950635239, -125.58281249999999), 2, "C:\\Users\\jlibarr1\\Desktop\\temp\\100x100 data\\", "");
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

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		CreateSmallMaps test = new CreateSmallMaps();
		test.generateMaps();
	}

}
