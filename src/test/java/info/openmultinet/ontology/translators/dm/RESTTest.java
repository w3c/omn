package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import javax.ws.rs.WebApplicationException;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

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
	public void testConvertToRspecRequest() {
		System.out.println("*************************************************");
		System.out.println("*******   convert model to RSpec Request  *******");
		System.out.println("*************************************************");
		final String content = this.getFilecontent("/omn/request_unbound.ttl");
		System.out.println(content);

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_REQUEST, content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec",
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
		Assert.assertTrue("should contain an advertisement rspec",
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
