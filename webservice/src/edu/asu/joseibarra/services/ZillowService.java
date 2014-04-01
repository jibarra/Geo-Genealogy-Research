package edu.asu.joseibarra.services;

import java.io.IOException;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.xml.sax.SAXException;

import edu.asu.joseibarra.zillow.ZillowQuery;

@Path("/zillow")
public class ZillowService {
	
	
	@GET
	@Path("/getRegionChildren")
	public Response getRegionChildren(
			@QueryParam("state") String state,
			@QueryParam("childtype") String childtype,
			@QueryParam("callback") @DefaultValue("callback") String callback
			) throws SAXException, IOException{
		if(state == null || childtype == null
				|| state.length() <= 0 || childtype.length() <= 0){
			return Response.status(200).entity("Invalid input").build();
		}
			
		ZillowQuery query = new ZillowQuery();
		
		return Response.status(200).entity(query.getRegionChildren(state, childtype)).build();
	}
	
	
}
