package fi.aalto.cs.drumbeat.rest.api;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;

import fi.aalto.cs.drumbeat.common.string.StringUtils;
import fi.aalto.cs.drumbeat.ifc.common.guid.GuidCompressor;
import fi.aalto.cs.drumbeat.rest.common.DrumbeatWebException;

@Path("/utils")
public class UtilsResource extends DrumbeatApiBase {
	
	public static final String IFC_GUID_ACTION_COMPRESS = "compress";
	public static final String IFC_GUID_ACTION_DECOMPRESS = "decompress";
	
	@PUT
	@Path("/ifcguid")	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String compressIfcGuid(
			@FormParam("action") String action,
			@FormParam("guid") String guid)
	{		
		if (StringUtils.isEmptyOrNull(action)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Param 'action' is required", null);
		}
		
		if (StringUtils.isEmptyOrNull(guid)) {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Param 'guid' is required", null);
		}
		
		if (action.equalsIgnoreCase(IFC_GUID_ACTION_COMPRESS)) {
			return GuidCompressor.compressGuidString(guid);			
		} else if (action.equalsIgnoreCase(IFC_GUID_ACTION_DECOMPRESS)) {			
			return GuidCompressor.uncompressGuidString(guid);
		} else {
			throw new DrumbeatWebException(Status.BAD_REQUEST, "Invalid param 'action'", null);			
		}
		
		
	}
	
	@GET
	@Path("/ifcguid")	
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	public String compressIfcGuid2(
			@QueryParam("action") String action,
			@QueryParam("guid") String guid)
	{
		return compressIfcGuid(action, guid);
	}

}
