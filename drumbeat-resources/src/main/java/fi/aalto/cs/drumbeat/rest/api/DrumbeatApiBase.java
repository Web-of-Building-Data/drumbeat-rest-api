package fi.aalto.cs.drumbeat.rest.api;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.UriInfo;

import org.apache.log4j.Logger;

import fi.aalto.cs.drumbeat.rest.common.DrumbeatApplication;

public abstract class DrumbeatApiBase {
	
	private static Logger logger = Logger.getLogger(DrumbeatApiBase.class);
	
	protected void notifyRequest(UriInfo uriInfo, HttpHeaders headers, HttpServletRequest request) {		
		DrumbeatApplication.getInstance().notifyRequest(uriInfo);
		
		logger.info(
			String.format("%s %s (%s)",
				request != null ? request.getMethod() : "NULL",
				uriInfo.getAbsolutePath(),
				uriInfo.getBaseUri()));

	}
}
