package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.AbstractConverter;

import org.junit.Assert;
import org.junit.Test;

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

		System.out.println(RSpecValidation.fixVerson(rspec1));

	}

	@Test
	public void version2() {
		// System.out.println("*************URN conversion*******************");
		// String urn =
		// "urn:publicid:IDN+testbed.example.org+node+http%3A%2F%2Ftestbed.example.org%2Fresources%23motorgarage-1";
		// System.out.println("Original urn: " + urn);
		// String urlNew = CommonMethods.generateUrlFromUrn(urn);
		// System.out.println("Convert back to url: " + urlNew);
		// System.out.println();
		// System.out.println();
		//
		// Assert.assertTrue(urlNew
		// .equals("http://testbed.example.org/resources#motorgarage-1"));
	}

	@Test
	public void version3() {
		// System.out
		// .println("*************localhost hash round trip*******************");
		// String url = "http://localhost/resources#Openstack-1";
		// System.out.println("Original url: " + url);
		// String urn = CommonMethods.generateUrnFromUrl(url, "node");
		// System.out.println("Convert to urn: " + urn);
		// String urlNew = CommonMethods.generateUrlFromUrn(urn);
		// System.out.println("Convert back to url: " + urlNew);
		// System.out.println();
		// System.out.println();
		// Assert.assertTrue(urn
		// .equals("urn:publicid:IDN+localhost+node+http%3A%2F%2Flocalhost%2Fresources%23Openstack-1"));
		// ;
		// Assert.assertTrue(url.equals(urlNew));
	}
}
