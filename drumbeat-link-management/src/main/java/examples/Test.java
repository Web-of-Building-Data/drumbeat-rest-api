package examples;

import fi.aalto.cs.drumbeat.ifc.common.guid.GuidCompressor;
import fi.aalto.cs.drumbeat.rest.client.link.DrbOntology;
import fi.aalto.cs.drumbeat.rest.client.link.LinkManager;

public class Test {

	public static void main(String[] args) {
		LinkManager linkManager = new LinkManager(
				"http://structural.drb.cs.hut.fi/linksets/hackathon-solibri-sample/hvac-arc/v1",
				"http://structural.drb.cs.hut.fi/datasources/hackathon-solibri-sample/hvac",
				"http://architect.drb.cs.hut.fi/datasources/hackathon-solibri-sample/arc");

		linkManager.defineOntology(DrbOntology.BLO.NAMESPACE_PREFIX, DrbOntology.BLO.NAMESPACE_URI);

		String linkUri = DrbOntology.BLO.PROPERTY_HAS_NEAR_SPACE;
		String fromObjectId = GuidCompressor.uncompressGuidString("0$rWkzvfL5gxBCd8kW$iSz");
		String toObjectId = GuidCompressor.uncompressGuidString("0bAQOfU7n9K8EKNIrzc81X");

		linkManager.createLinks(linkUri, fromObjectId, toObjectId);
		linkManager.commit();
	}

}
