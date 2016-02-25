package fi.aalto.cs.drumbeat.rest.common;

//@ApplicationPath("/")
public class DrumbeatWebApplication extends DrumbeatApplication {

	public DrumbeatWebApplication() {
		super(DrumbeatWebApplication.class.getResource("/").getPath().replaceAll("classes/$", "").replaceAll("%20", " "));
	}

}
