package fi.hut.cs.drumbeat.rest.api;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

//@ApplicationPath("/")
public class Application extends ResourceConfig {

	public Application() {
//		System.err.println(MultiPartFeature.class.getPackage().getName());
		packages(Application.class.getPackage().getName());
//		packages(MultiPartFeature.class.getPackage().getName()); //"org.glassfish.jersey.media.multipart");
//		register(MultiPartFeature.class);
//		register(CollectionResource.class);
	}

}
