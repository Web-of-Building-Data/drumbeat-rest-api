package fi.aalto.cs.drumbeat.rest;

import static org.junit.Assert.*;

import org.apache.log4j.Logger;
import org.junit.Test;


public class GeneralTest extends DrumbeatTest {	
	
	private static final boolean DO_TEST = false;
	
	public GeneralTest() {
		super(DO_TEST);
	}
	
	private static final Logger logger = Logger.getLogger(GeneralTest.class);
	
	
	@Test
	public void test_config_file() {
		if (!doTest()) {
			return;
		}
		
		String baseUrl = getApplication().getBaseUri();
		
		assertNotNull(baseUrl);
		assertNotEquals("", baseUrl);
		
		logger.info("BaseUrl: " + baseUrl);
		
	}
	

}
