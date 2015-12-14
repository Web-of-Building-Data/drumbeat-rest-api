package fi.aalto.cs.drumbeat.rest.common;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@SuppressWarnings("serial")
public class DrumbeatWebException extends WebApplicationException {
	
	public DrumbeatWebException(
			Status status,
			String message,
			Throwable cause)
	{
		super(
			cause,
			Response
				.status(status)
				.entity(message)
				.type(MediaType.TEXT_PLAIN)
				.build());
	}

}
