package fi.aalto.cs.drumbeat.rest.common;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@SuppressWarnings("serial")
public class DrumbeatWebException extends WebApplicationException {
	
	public DrumbeatWebException(
			Status status,
			Object entity,
			Throwable cause)
	{
		super(
			cause,
			Response
				.status(status)
				.entity(entity)
				.build());
	}

}
