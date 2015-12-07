package fi.aalto.cs.drumbeat.rest;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;


public class CommonTest extends DrumbeatTest {	
	
	private static final boolean DO_TEST = true;
	
	private static final Logger logger = Logger.getLogger(CommonTest.class);
	
	
	@Test
	public void test_config_file() {
		if (!DO_TEST) {
			return;
		}
		
		String baseUrl = getApplication().getBaseUri();
		
		assertNotNull(baseUrl);
		assertNotEquals("", baseUrl);
		
		logger.info("BaseUrl: " + baseUrl);
		
	}
	

}
