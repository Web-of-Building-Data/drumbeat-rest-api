package fi.aalto.cs.drumbeat.views;

import com.vaadin.ui.VerticalLayout;

public class ServerView extends VerticalLayout {
	
	private final String serverName;
	private final String serverUrl;
	
	public ServerView(String serverName, String serverUrl) {
		this.serverName = serverName;
		this.serverUrl = serverUrl;
		
		
	}
	
	public String getServerName() {
		return serverName;
	}
	
	public String getServerUrl() {
		return serverUrl;
	}

}
