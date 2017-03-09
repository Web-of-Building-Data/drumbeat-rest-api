package fi.aalto.cs.drumbeat.rest.api;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;

@Path("/version")
public class VersionResource extends DrumbeatApiBase {
	
	@GET
	public Response getVersion(
		@Context UriInfo uriInfo,
		@Context HttpHeaders headers,
		@Context HttpServletRequest request)
				throws IOException
	{		
		notifyRequest(uriInfo, headers, request);
		
		InputStream in = null;		
		
		try {
			in = new FileInputStream(DrumbeatApplication.getInstance().getRealServerPath("classes/version.txt")); 

			Model model = ModelFactory.createDefaultModel();
			
			RDFDataMgr.read(model, in, uriInfo.getBaseUri() + "apps/", Lang.TURTLE);
			
			model.setNsPrefix("dcterms", "http://purl.org/dc/terms/");
			
			return DrumbeatResponseBuilder.build(
					Status.OK,
					model,
					headers.getAcceptableMediaTypes());			
		} catch (NotFoundException exception) {
			throw new DrumbeatWebException(
					Status.NOT_FOUND,
					exception.getMessage(),
					exception);
		} finally {
			if (in != null) {
				in.close();
			}
		}
		
	}
	

}
