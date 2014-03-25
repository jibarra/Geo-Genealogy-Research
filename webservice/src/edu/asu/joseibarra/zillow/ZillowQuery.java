package edu.asu.joseibarra.zillow;

import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class ZillowQuery {
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
	
	public String getRegionChildren(String state, String childtype) throws SAXException, IOException{
		Document serviceDoc = docBuilder.parse(REGION_CHILDREN_URL + "?zws-id=" + ZWSID + "&state=" + state+ "&childtype=" + childtype);
		Element regionElement = (Element)serviceDoc.getElementsByTagName("list").item(0);
		NodeList regionList = regionElement.getChildNodes();
		String outputList = "";
		
		int size = regionList.getLength();
		for(int i = 0; i < size; i++){
			Element region = (Element) regionList.item(i);
			Element name = (Element) region.getElementsByTagName("name").item(0);
			Element zindex = (Element) region.getElementsByTagName("zindex").item(0);
			if(zindex == null){
				continue;
			}
			outputList += name.getTextContent() + ": " + zindex.getAttribute("currency") + " " + zindex.getTextContent() + "\n";
		}
		return outputList;
	}
	
	public static void main(String[] args) throws SAXException, IOException{
		ZillowQuery service = new ZillowQuery();
//		System.out.println(service.getZestimate("48749425", false));
//		System.out.println(service.getSearchResults("901 Scarlet Haze Avenue", "89183", false));
//		System.out.println(service.getDemographics("89183"));
		System.out.println(service.getRegionChildren("az", "zipcode"));
	}
	
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
	
	private final String ZWSID = "X1-ZWz1b8t7153ksr_728x4";
	private final String ZESTIMATE_URL = "http://www.zillow.com/webservice/GetZestimate.htm";
	private final String SEARCH_RESULTS_URL = "http://www.zillow.com/webservice/GetSearchResults.htm";
	private final String DEMOGRAPHICS_URL = "http://www.zillow.com/webservice/GetDemographics.htm";
	private final String REGION_CHILDREN_URL = "http://www.zillow.com/webservice/GetRegionChildren.htm";
}