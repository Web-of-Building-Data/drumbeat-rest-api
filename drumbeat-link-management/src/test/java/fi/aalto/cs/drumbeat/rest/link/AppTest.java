package fi.aalto.cs.drumbeat.rest.link;

import fi.aalto.cs.drumbeat.ifc.common.guid.GuidCompressor;
import fi.aalto.cs.drumbeat.rest.client.link.LinkManager;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
    	LinkManager linkManager = new LinkManager(
    			"http://structural.drb.cs.hut.fi/linksets/hackathon-solibri-sample/hvac-arc/v1", 
    			"http://structural.drb.cs.hut.fi/datasources/hackathon-solibri-sample/hvac",
    			"http://architect.drb.cs.hut.fi/datasources/hackathon-solibri-sample/arc");
    	
    	String linkUri = "http://drumbeat.cs.hut.fi/owl/blo#hasNearSpace";
    	String fromObjectId = GuidCompressor.uncompressGuidString("0$rWkzvfL5gxBCd8kW$iSz");
    	String toObjectId = GuidCompressor.uncompressGuidString("0bAQOfU7n9K8EKNIrzc81X");
    	
    	linkManager.createLinks(linkUri, fromObjectId, toObjectId);
    	linkManager.commit();
    }
    
    
//    public void testApp2()
//    {
//    	LinkManager linkManager = new LinkManager(
//    			"http://localhost:8080/drumbeat/linksets/hackathon-solibri-sample/hvac-arc/v1", 
//    			"http://localhost:8080/drumbeat/datasources/hackathon-solibri-sample/hvac",
//    			"http://localhost:8080/drumbeat/datasources/hackathon-solibri-sample/arc");
//    	
//    	String linkUri = "http://drumbeat.cs.hut.fi/owl/blo#hasNearSpace";
//    	
//    	linkManager.createLinks(linkUri, "fromGuid", "toGuid1", "toGuid2");
//    	linkManager.commit();
//    }
    
}
