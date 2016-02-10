package fi.aalto.cs.drumbeat.rest.common;

import java.util.List;

import javax.ws.rs.NotSupportedException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.hp.hpl.jena.rdf.model.Model;

public class DrumbeatResponseBuilder {
	
	public static Response build(
			Status status,
			Model model,
			List<MediaType> acceptableMediaTypes)
	{
		return build(status, model, null, acceptableMediaTypes);
	}
	
	
	public static Response build(
			Status status,
			Model model,
			String baseUri,
			List<MediaType> acceptableMediaTypes) {
		
		for (MediaType mediaType : acceptableMediaTypes) {
			
			try {
			
				String entity = MediaTypeConverter.convertModel(model, mediaType, baseUri);
				return Response
						.status(status)
						.entity(entity)
						.type(mediaType)
						.build();
				
			} catch (NotSupportedException e) {
				// do nothing
			} catch (Exception e) {
				throw new DrumbeatWebException(
						Response.Status.INTERNAL_SERVER_ERROR,
						String.format(
								"Unexpected error: %s",
								e.getMessage()),
						null);
			}
			
		}
		
		throw new DrumbeatWebException(
				Response.Status.UNSUPPORTED_MEDIA_TYPE,
				String.format(
						"Use supported media types: %s",
						MediaTypeConverter.getSupportedMediaTypes().toString()),
				null);
	}
	
}
