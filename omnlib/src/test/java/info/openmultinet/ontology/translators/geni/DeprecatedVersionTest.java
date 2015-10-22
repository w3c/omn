package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class DeprecatedVersionTest {

	@Test(expected = DeprecatedRspecVersionException.class)
	public void version1() throws DeprecatedRspecVersionException {

		String rspec1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>"
				+ "<rspec xmlns=\"http://www.protogeni.net/resources/rspec/0.1\""
				+ " xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\""
				+ " xsi:schemaLocation=\"http://www.protogeni.net/resources/rspec/0.1 http://www.protogeni.net/resources/rspec/0.1/ad.xsd\""
				+ " type=\"advertisement\""
				+ " generated=\"2009-07-21T19:19:06Z\""
				+ " valid_until=\"2009-07-21T19:19:06Z\" >"
				+ "<node component_manager_uuid=\"urn:publicid:IDN+emulab.geni.emulab.net+authority+cm\""
				+ " component_name=\"pc160\""
				+ " component_uuid=\"urn:publicid:IDN+emulab.geni.emulab.net+node+pc160\" >"
				+ "<node_type "
				+ " type_name=\"pc850\""
				+ " type_slots=\"1\""
				+ "/>"
				+ "<node_type "
				+ " type_name=\"pc\""
				+ " type_slots=\"1\""
				+ "/>"
				+ "<available>true</available>"
				+ "<exclusive>true</exclusive>"
				+ "<interface component_id=\"urn:publicid:IDN+emulab.geni.emulab.net+interface+pc160:eth0\"/>"
				+ "<interface component_id=\"urn:publicid:IDN+emulab.geni.emulab.net+interface+pc160:eth1\"/>"
				+ "</node>" + "</rspec>";

		RSpecValidation.fixVersion(rspec1);
	}

	@Test
	public void version2() throws DeprecatedRspecVersionException {
		String rspec2 = "<rspec xmlns=\"http://www.protogeni.net/resources/rspec/2\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://www.protogeni.net/resources/rspec/2"
				+ " http://www.protogeni.net/resources/rspec/2/request.xsd\""
				+ " type=\"request\" ></rspec>";

		String outputRspec = RSpecValidation.fixVersion(rspec2);
		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);
	}

	@Test
	public void version3() throws DeprecatedRspecVersionException {
		String rspec2 = "<rspec xmlns=\"http://www.geni.net/resources/rspec/3\" "
				+ "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
				+ "xsi:schemaLocation=\"http://www.geni.net/resources/rspec/3 "
				+ " http://www.geni.net/resources/rspec/3/request.xsd\""
				+ " type=\"request\" ></rspec>";

		String outputRspec = RSpecValidation.fixVersion(rspec2);
		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);
	}
}
