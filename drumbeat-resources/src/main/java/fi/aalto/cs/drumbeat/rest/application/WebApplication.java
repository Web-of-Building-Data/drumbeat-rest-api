package fi.aalto.cs.drumbeat.rest.application;

public class WebApplication extends DrumbeatApplication {

	public WebApplication() {
		super(DrumbeatApplication.class.getResource("/").getPath().replaceAll("classes/$", ""));
	}

}
