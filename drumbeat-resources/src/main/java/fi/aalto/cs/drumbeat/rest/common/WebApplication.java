package fi.aalto.cs.drumbeat.rest.common;

public class WebApplication extends DrumbeatWebApplication {

	public WebApplication() {
		super(DrumbeatWebApplication.class.getResource("/").getPath().replaceAll("classes/$", ""));
	}

}
