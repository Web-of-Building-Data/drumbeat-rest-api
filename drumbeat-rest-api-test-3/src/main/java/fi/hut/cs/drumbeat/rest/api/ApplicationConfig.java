package fi.hut.cs.drumbeat.rest.api;

import javax.ws.rs.ApplicationPath;



//import org.glassfish.jersey.media.multipart.MultiPartFeature;
//import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.glassfish.jersey.server.ResourceConfig;

@ApplicationPath("/")
public class ApplicationConfig extends ResourceConfig {

	public ApplicationConfig() {
		packages("fi.hut.cs.drumbeat.rest.api");
		register(MyResource.class);
		//packages(ApplicationConfig.class.getPackage().getName());
		//System.err.println(ApplicationConfig.class.getPackage().getName());
		throw new RuntimeException("aaaa");
//		packages(MultiPartFeature.class.getPackage().getName()); //"org.glassfish.jersey.media.multipart");
//		register(MultiPartFeature.class);
//		register(CollectionResource.class);
	}

}
