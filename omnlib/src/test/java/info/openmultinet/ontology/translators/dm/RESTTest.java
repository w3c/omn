package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class RESTTest {

	private REST converter;

	@Before
	public void setup() {
		this.converter = new REST();
	}

	@Test
	public void testConvertToGraphFromRspecAd() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Ad to model       *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/advertisement/advertisement_paper2015.xml");

		final String result = this.converter.post(
				AbstractConverter.RSPEC_ADVERTISEMENT, AbstractConverter.TTL,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain an ad", result.contains("#Offering"));
	}

	@Test
	public void testConvertToRdfXmlFromRspecAd() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Ad to model       *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/advertisement/advertisement_paper2015.xml");

		final String result = this.converter.post(
				AbstractConverter.RSPEC_ADVERTISEMENT,
				AbstractConverter.RDFXML, content);
		System.out.println(result);
		Assert.assertTrue("should contain an ad", result.contains("Offering"));
	}

	@Test
	// fixme: this test is slow!
	public void testConvertToRspecAd() {
		System.out.println("*******************************************");
		System.out.println("*******  convert model to RSpec Ad  *******");
		System.out.println("*******************************************");
		final String content = this.getFilecontent("/omn/offer_paper2015.ttl");

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_ADVERTISEMENT, content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec",
				result.contains("type=\"advertisement\""));
	}

	@Test
	// fixme: this test is slow!
	public void testConvertFromUnknownToRspecAd() {
		System.out.println("*******************************************");
		System.out.println("*******  convert model to RSpec Ad  *******");
		System.out.println("*******************************************");
		final String content = this.getFilecontent("/omn/offer_paper2015.ttl");

		final String result = this.converter.post(AbstractConverter.ANYFORMAT,
				AbstractConverter.RSPEC_ADVERTISEMENT, content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec",
				result.contains("type=\"advertisement\""));
	}

	@Test
	public void testConvertToGraphFromRspecRequest() {
		System.out.println("*************************************************");
		System.out.println("*******   convert RSpec Request to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/request/request_unbound.xml");

		final String result = this.converter
				.post(AbstractConverter.RSPEC_REQUEST, AbstractConverter.TTL,
						content);
		System.out.println(result);
		Assert.assertTrue("should contain a request",
				result.contains("#Request"));
	}

	@Test
	public void testConvertToRdfXmlFromRspecRequest() {
		System.out.println("*************************************************");
		System.out.println("*******   convert RSpec Request to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/request/request_unbound.xml");

		final String result = this.converter.post(
				AbstractConverter.RSPEC_REQUEST, AbstractConverter.RDFXML,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain a request",
				result.contains("Request"));
	}

	@Test
	// fixme: this test is slow!
	public void testConvertToRspecRequest() {
		System.out.println("*************************************************");
		System.out.println("*******   convert model to RSpec Request  *******");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/omn/request_unbound.ttl");
		System.out.println(content);

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_REQUEST, content);
		System.out.println(result);
		Assert.assertTrue("should contain a request rspec",
				result.contains("type=\"request\""));
	}

	@Test
	public void testConvertToGraphFromRspecManifest() {
		System.out.println("*************************************************");
		System.out
				.println("*******   convert RSpec Manifest to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/manifest/manifest_paper2015.xml");

		final String result = this.converter.post(
				AbstractConverter.RSPEC_MANIFEST, AbstractConverter.TTL,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain a manifest",
				result.contains("#Manifest"));
	}

	@Test
	public void testConvertToRdfXmlFromRspecManifest() {
		System.out.println("*************************************************");
		System.out
				.println("*******   convert RSpec Manifest to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/manifest/manifest_paper2015.xml");

		final String result = this.converter.post(
				AbstractConverter.RSPEC_MANIFEST, AbstractConverter.RDFXML,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain a manifest",
				result.contains("Manifest"));
	}

	@Test
	public void testConvertToGraphFromUnknown() {
		System.out.println("*************************************************");
		System.out
				.println("*******   convert RSpec Manifest to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/manifest/manifest_paper2015.xml");

		final String result = this.converter.post(AbstractConverter.ANYFORMAT,
				AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain a manifest",
				result.contains("#Manifest"));
	}

	@Test
	// fixme: this test is slow!
	public void testConvertToRspecManifest() {
		System.out.println("*************************************************");
		System.out
				.println("*******   convert model to RSpec Manifest  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/omn/manifest_paper2015.ttl");
		System.out.println(content);

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_MANIFEST, content);
		System.out.println(result);
		Assert.assertTrue("should contain a manifest rspec",
				result.contains("type=\"manifest\""));
	}

	@Test
	public void testConvertToTosca() {
		System.out.println("*****************************************");
		System.out.println("*******   start convert to TOSCA  *******");
		System.out.println("*****************************************");
		final String content = this
				.getFilecontent("/omn/tosca-request-dummy.ttl");

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.TOSCA, content);
		System.out.println(result);
		Assert.assertTrue(
				"should contain tosca namespace",
				result.contains("xmlns=\"http://docs.oasis-open.org/tosca/ns/2011/12\""));
	}

	@Test
	public void testConvertToGraphFromTosca() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Tosca to model  *********");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/tosca/request-dummy.xml");

		final String result = this.converter.post(AbstractConverter.TOSCA,
				AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain osco:STOPPED",
				result.contains("osco:STOPPED"));
	}

	@Test
	public void testConvertToRdfXmlFromTosca() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Tosca to model  *********");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/tosca/request-dummy.xml");

		final String result = this.converter.post(AbstractConverter.TOSCA,
				AbstractConverter.RDFXML, content);
		System.out.println(result);
		Assert.assertTrue("should contain osco:STOPPED",
				result.contains("STOPPED"));
	}

	@Test
	public void testConvertToGraphFromUnknownTosca() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Tosca to model  *********");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/tosca/request-dummy.xml");

		final String result = this.converter.post(AbstractConverter.ANYFORMAT,
				AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain osco:STOPPED",
				result.contains("osco:STOPPED"));
	}

	@Test
	@Ignore
	//todo: takes too long
	public void testRdfxmlToRequest() {
		System.out.println("*************************************************");
		System.out.println("******  start convert RDF/XML to request  *******");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/tosca/newTubitTosca.xml");

		final String result = this.converter.post(AbstractConverter.RDFXML,
				AbstractConverter.RSPEC_REQUEST, content);
		System.out.println(result);
		Assert.assertTrue("should contain a request rspec",
				result.contains("type=\"request\""));
	}

	@Test(expected = WebApplicationException.class)
	public void testWrongParam() {
		this.converter.post("a", "b", null);
	}

	public String getFilecontent(final String filename) {
		final InputStream rspec = RESTTest.class.getResourceAsStream(filename);
		@SuppressWarnings("resource")
		final String content = new Scanner(rspec, "UTF-8").useDelimiter("\\A")
				.next();
		return content;
	}

}
