package edu.asu.joseibarra.zillow;

/*
 * @author Jose Ibarra
 * Jose.Ibarra@asu.edu
 * © Arizona State University 2014
 * 
 * Class to query the zillow API.
 * See this URL for more information: http://www.zillow.com/howto/api/APIOverview.htm
 */

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ZillowQuery {
	
	//Gets the Zestimate for a zipcode, which is the estimated house value
	//for all houses in that zip
	public String getZestimate(String zpid, boolean rentZestimate) throws SAXException, IOException{
		Document serviceDoc = docBuilder.parse(ZESTIMATE_URL + "?zws-id=" + ZWSID + "&zpid=" + zpid + "&rentzestimate=" + rentZestimate);
        Element zestimate = (Element)serviceDoc.getElementsByTagName("zestimate").item(0);
        Element amount = (Element)zestimate.getElementsByTagName("amount").item(0);
        String currency = amount.getAttribute("currency");
		return currency + " " + amount.getTextContent();
	}
	
	public String getSearchResults(String address, String cityStateZip, boolean rentZestimate) throws SAXException, IOException{
		Document serviceDoc = docBuilder.parse(SEARCH_RESULTS_URL + "?zws-id=" + ZWSID + "&address=" + address + 
				"&citystatezip=" + cityStateZip + "&rentzestimate=" + rentZestimate);
        Element zestimate = (Element)serviceDoc.getElementsByTagName("zestimate").item(0);
		Element amount = (Element)zestimate.getElementsByTagName("amount").item(0);
        String currency = amount.getAttribute("currency");
		return currency + " " + amount.getTextContent();
	}
	
	public String getDemographics(String city, String state){
		return "";
	}
	
	public String getDemographics(String zip) throws SAXException, IOException{
		Document serviceDoc = docBuilder.parse(DEMOGRAPHICS_URL + "?zws-id=" + ZWSID + "&zip=" + zip);
		System.out.println(serviceDoc.getBaseURI());
		return "";
	}
	
	/*
	 * Get the "region children" for a state.
	 * This method stores the zipcode children into a state storage and zip storage within this class.
	 * The class queries based on the state and childtype entered (which should be 'zipcode')
	 * then gets the results of all the zipcode Zindexes for that state.
	 */
	public void getRegionChildrenForState(String state, String childtype, ZillowStateZipDemographics stateStorage){
		//Georgia data is supposed to be fixed by May 1, 2014
//		if(state.equals("GA")){
//			return;
//		}
		
		Document serviceDoc = null;
		try {
			serviceDoc = docBuilder.parse(REGION_CHILDREN_URL + "?zws-id=" + ZWSID + "&state=" + state + "&childtype=" + childtype);			
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Element regionElement = (Element)serviceDoc.getElementsByTagName("list").item(0);
		NodeList regionList = regionElement.getChildNodes();
		
		int size = regionList.getLength();
		for(int i = 0; i < size; i++){
			Element region = (Element) regionList.item(i);
			Element name = (Element) region.getElementsByTagName("name").item(0);
			Element zindex = (Element) region.getElementsByTagName("zindex").item(0);
			if(zindex == null){
				continue;
			}
			stateStorage.addZipValue(name.getTextContent(), Integer.parseInt(zindex.getTextContent()));
			zipDemographics.put(name.getTextContent(), Integer.parseInt(zindex.getTextContent()));
		}
	}
	
	/*
	 * Sets up the demographics for a state (see getRegionChildrenForState above).
	 * This class initializes the required hash maps then gets all the demographics for
	 * all US states.
	 */
	public void setupStateZipDemographics(String childtype){
		ZillowQuery.stateZipDemographics = new HashMap<String, ZillowStateZipDemographics>();
		ZillowQuery.zipDemographics = new HashMap<String, Integer>();
		for(int i = 0; i < US_STATES.length; i++){
			stateZipDemographics.put(US_STATES[i], new ZillowStateZipDemographics(US_STATES[i]));
			getRegionChildrenForState(US_STATES[i], childtype, stateZipDemographics.get(US_STATES[i]));
		}
	}
	
	
	/*
	 * This class checks if the demographics (see above methods, esp. getRegionChildrenForState)
	 * are up to date. The class checks the current time and compares it to the last update.
	 * This should update within a specified number of days (ZillowQuery.numUpdateDays),
	 * using setupStateZipDemographics method.
	 * If it has only been run once, it automatically sets up the demograhpics.
	 */
	public synchronized void checkStateZipDemographics(String childtype){
		Calendar currentTime = Calendar.getInstance();
		
		//Get the state zip demographics if it has never been updated
		if(ZillowQuery.stateZipDemographics == null){
			ZillowQuery.lastUpdate = Calendar.getInstance();
			setupStateZipDemographics(childtype);
		}
		//or its last update was numUpdateDays ago
		//(it'll update before the numUpdateDays ago on a new year.
		else if(Math.abs(currentTime.get(Calendar.DAY_OF_YEAR) - 
				ZillowQuery.lastUpdate.get(Calendar.DAY_OF_YEAR)) > ZillowQuery.numUpdateDays){
			ZillowQuery.lastUpdate = Calendar.getInstance();
			setupStateZipDemographics(childtype);
		}
	}
	
	/*
	 * This class is a buffer method, which just runs the check state zip demographics.
	 */
	
	public void loadRegionChildren(String childtype) throws SAXException, IOException{
		checkStateZipDemographics(childtype);
	}
	
	public Integer getZipCodeValue(String state, String zipcode) throws StateDoesNotExistException, ZipCodeNotFoundException{
		checkStateZipDemographics("zipcode");
		
		ZillowStateZipDemographics stateDemographics = ZillowQuery.stateZipDemographics.get(state);
		if(stateDemographics == null){
			throw new StateDoesNotExistException();
		}
		
		HashMap<String, Integer> values = stateDemographics.getZipValues();
		if(values == null){
			throw new ZipCodeNotFoundException();
		}
		
		return values.get(zipcode);
	}
	
	/*
	 * This method gets the $ value for the specified zipcode. First it checks to see
	 * if the data needs an update, then it gets the information for an entered zip code.
	 */
	public Integer getZipCodeValue(String zipcode) throws ZipCodeNotFoundException{
		checkStateZipDemographics("zipcode");
		Integer value = zipDemographics.get(zipcode);
		if(value == null){
			throw new ZipCodeNotFoundException();
		}
		return zipDemographics.get(zipcode);
	}
	
	public static void main(String[] args) throws SAXException, IOException{
		ZillowQuery service = new ZillowQuery();
//		System.out.println(service.getZestimate("48749425", false));
//		System.out.println(service.getSearchResults("901 Scarlet Haze Avenue", "89183", false));
//		System.out.println(service.getDemographics("89183"));
//		System.out.println(service.getRegionChildren("az", "zipcode"));
	}
	
	//Number of days to wait to update zillow data
	private static final int numUpdateDays = 7;
	//The last update of the zillow data
	private static Calendar lastUpdate = null;
	
	//hashmap that stores the state demographics information based on state and zip
	//This is linked state(String)->demographicInfo(ZillowStateZipDemographics)
	private static HashMap<String, ZillowStateZipDemographics> stateZipDemographics = null;
	//hashmap that stores the state demographics based soley on zip (linked zip(String)->$value(Integer)
	private static HashMap<String, Integer> zipDemographics = null;
	
	//Static variables to read XML documents.
	private static final DocumentBuilderFactory dbFac;
    private static final DocumentBuilder docBuilder;
	static
    {
        try
        {
            dbFac = DocumentBuilderFactory.newInstance();
            docBuilder = dbFac.newDocumentBuilder();
        }
        catch(ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
	
	/*
	 * Exception thrown when an inputted state cannot be found.
	 */
	public class StateDoesNotExistException extends Exception
	{
	      //Parameterless Constructor
	      public StateDoesNotExistException() {}

	      //Constructor that accepts a message
	      public StateDoesNotExistException(String message)
	      {
	         super(message);
	      }
	 }
	
	/*
	 * Exception thrown when an inputted zip cannot be found.
	 */
	public class ZipCodeNotFoundException extends Exception
	{
	      //Parameterless Constructor
	      public ZipCodeNotFoundException() {}

	      //Constructor that accepts a message
	      public ZipCodeNotFoundException(String message)
	      {
	         super(message);
	      }
	 }
	
	//US States
	private static final String[] US_STATES = {"AL","AK","AZ","AR","CA","CO","CT","DE","DC","FL",
		"GA","HI","ID","IL","IN","IA","KS","KY","LA","ME","MD","MA","MI","MN","MS","MO","MT","NE",
		"NV","NH","NJ","NM","NY","NC","ND","OH","OK","OR","PA","RI","SC","SD","TN","TX","UT",
		"VT","VA","WA","WV","WI","WY"};
	
	//Zillow constants, including developer key and URL.
	private final String ZWSID = "X1-ZWz1b8t7153ksr_728x4";
	private final String ZESTIMATE_URL = "http://www.zillow.com/webservice/GetZestimate.htm";
	private final String SEARCH_RESULTS_URL = "http://www.zillow.com/webservice/GetSearchResults.htm";
	private final String DEMOGRAPHICS_URL = "http://www.zillow.com/webservice/GetDemographics.htm";
	private final String REGION_CHILDREN_URL = "http://www.zillow.com/webservice/GetRegionChildren.htm";
}
