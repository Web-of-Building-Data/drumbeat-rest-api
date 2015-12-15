package fi.aalto.cs.drumbeat.rest.common;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

//import org.apache.commons.lang3.StringEscapeUtils;

@SuppressWarnings("serial")
public class DrumbeatWebException extends WebApplicationException {
	
	public DrumbeatWebException(
			Status status,
			Throwable cause)
	{
		this(
			status,
			cause != null ? cause.getClass() + ": " + cause.getMessage() : null,
			cause);
	}

	public DrumbeatWebException(
			Status status,
			String message,
			Throwable cause)
	{
		// TODO: Support different media types
		super(
			cause,
			Response
				.status(status)
//				.entity(message != null ? StringEscapeUtils.escapeHtml4(message) : null)
				.entity(message)
				.type(MediaType.TEXT_PLAIN)
				.build());
	}

}
