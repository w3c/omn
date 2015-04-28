package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.translators.AbstractConverter;

import org.junit.Assert;

import org.junit.Test;

public class UrnUrlTest {

	@Test
	public void forwardSlashRoundtrip() {

		System.out
				.println("*************Forward slash round trip*******************");
		String url = "https://www.testbed.example.org/resources/Openstack-1";
		System.out.println("Original url: " + url);
		String urn = AbstractConverter.generateUrnFromUrl(url, "node");
		System.out.println("Conver to urn: " + urn);
		String urlNew = AbstractConverter.generateUrlFromUrn(urn);
		System.out.println("Conver back to url: " + urlNew);
		System.out.println();
		System.out.println();
		Assert.assertTrue(urn.equals("urn:publicid:IDN+www.testbed.example.org+node+https%3A%2F%2Fwww.testbed.example.org%2Fresources%2FOpenstack-1"));;
		Assert.assertTrue(url.equals(urlNew));
	}
	
	@Test
	public void testurn() {
		System.out
		.println("*************URN conversion*******************");
		String urn = "urn:publicid:IDN+testbed.example.org+node+http%3A%2F%2Ftestbed.example.org%2Fresources%23motorgarage-1";
		System.out.println("Conver to urn: " + urn);
		String urlNew = AbstractConverter.generateUrlFromUrn(urn);
		System.out.println("Conver back to url: " + urlNew);
		System.out.println();
		System.out.println();
		
		Assert.assertTrue(urlNew.equals("http://testbed.example.org/resources#motorgarage-1"));
	}
	
	

	@Test
	public void localhostSlashRoundtrip() {

		System.out
				.println("*************localhost slash round trip*******************");
		String url = "http://localhost/resource/VMServer-1";
		System.out.println("Original url: " + url);
		String urn = AbstractConverter.generateUrnFromUrl(url, "node");
		System.out.println("Conver to urn: " + urn);
		String urlNew = AbstractConverter.generateUrlFromUrn(urn);
		System.out.println("Conver back to url: " + urlNew);
		System.out.println();
		System.out.println();
		Assert.assertTrue(urn.equals("urn:publicid:IDN+localhost+node+http%3A%2F%2Flocalhost%2Fresource%2FVMServer-1"));;
		Assert.assertTrue(url.equals(urlNew));

	}

	@Test
	public void localhostHashRoundtrip() {
		System.out
				.println("*************localhost hash round trip*******************");
		String url = "http://localhost/resources#Openstack-1";
		System.out.println("Original url: " + url);
		String urn = AbstractConverter.generateUrnFromUrl(url, "node");
		System.out.println("Conver to urn: " + urn);
		String urlNew = AbstractConverter.generateUrlFromUrn(urn);
		System.out.println("Conver back to url: " + urlNew);
		System.out.println();
		System.out.println();
		Assert.assertTrue(urn.equals("urn:publicid:IDN+localhost+node+http%3A%2F%2Flocalhost%2Fresources%23Openstack-1"));;
		Assert.assertTrue(url.equals(urlNew));
	}

	@Test
	public void hashRoundtrip() {
		System.out
				.println("*************hash round trip*******************");
		String url = "http://www.testbed.example.org/resources#Openstack-1";
		System.out.println("Original url: " + url);
		String urn = AbstractConverter.generateUrnFromUrl(url, "node");
		System.out.println("Conver to urn: " + urn);
		String urlNew = AbstractConverter.generateUrlFromUrn(urn);
		System.out.println("Conver back to url: " + urlNew);
		System.out.println();
		System.out.println();
		Assert.assertTrue(urn.equals("urn:publicid:IDN+www.testbed.example.org+node+http%3A%2F%2Fwww.testbed.example.org%2Fresources%23Openstack-1"));;
		Assert.assertTrue(url.equals(urlNew));
	}
}
