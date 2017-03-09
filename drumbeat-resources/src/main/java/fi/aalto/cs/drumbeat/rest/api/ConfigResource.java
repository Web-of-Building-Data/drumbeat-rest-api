package fi.aalto.cs.drumbeat.rest.api;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map.Entry;
import java.util.Properties;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.shared.NotFoundException;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatResponseBuilder;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;

@Path("/config")
public class ConfigResource extends DrumbeatApiBase {
	
	@GET
	public Response getConfig(
		@Context UriInfo uriInfo,
		@Context HttpHeaders headers,
		@Context HttpServletRequest request,
		@Context SecurityContext securityContext)
				throws IOException
	{		
		notifyRequest(uriInfo, headers, request);
		
		InputStream in = null;		
		
		try {
			Model model = ModelFactory.createDefaultModel();
			
			Properties properties = DrumbeatApplication.getInstance().getConfigurationProperties();
			
			String baseUri = DrumbeatApplication.getInstance().getBaseUri();
			
			String configBaseUri = "http://drumbeat.cs.hut.fi/config/";
			
			model.setNsPrefix("config", configBaseUri);
			
			for (Entry<Object, Object> pair : properties.entrySet()) {
				String propertyName = (String)pair.getKey();
				String propertyValue = (String)pair.getValue();
				
//				if (propertyName.contains("password")) {
//					propertyValue = "*****";
//				}
				
				model.add(
						model.createResource(baseUri),
						model.createProperty(configBaseUri + propertyName),
						model.createLiteral(propertyValue));
				
			}
			
			String requestBaseUri = "http://drumbeat.cs.hut.fi/request/";
			model.setNsPrefix("request", requestBaseUri);

			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "baseUri"),
					model.createTypedLiteral(uriInfo.getBaseUri().toString()));
			
			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "absolutePath"),
					model.createTypedLiteral(uriInfo.getAbsolutePath().toString()));
			
			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "requestClass"),
					model.createTypedLiteral(request.getClass().getName()));

			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "isRequestSecure"),
					model.createTypedLiteral(request.isSecure()));
			
			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "schema"),
					model.createTypedLiteral(request.getScheme()));
			
			model.add(
					model.createResource(baseUri),
					model.createProperty(requestBaseUri + "isSecurityContextSecure"),
					model.createTypedLiteral(securityContext.isSecure()));
			

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
