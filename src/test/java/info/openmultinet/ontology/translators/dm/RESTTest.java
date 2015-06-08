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
	public void testConvertToGraphFromRspecRequest() {
		System.out.println("*************************************************");
		System.out.println("*******   start convert Request to model  *******");
		System.out.println("*************************************************");
		final String content = this
				.getFilecontent("/geni/request/request_unbound2.xml");

		final String result = this.converter
				.post(AbstractConverter.RSPEC_REQUEST, AbstractConverter.TTL,
						content);
		System.out.println(result);
		Assert.assertTrue("should contain a request",
				result.contains("#Request"));
	}

	@Test
	public void testConvertToRspec() {
		System.out.println("*****************************************");
		System.out.println("*******   start convert to RSpec  *******");
		System.out.println("*****************************************");
		final String content = this.getFilecontent("/omn/request.ttl");

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_ADVERTISEMENT, content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec",
				result.contains("type=\"advertisement"));
	}

	@Test
	public void testConvertToTosca() {
		System.out.println("*****************************************");
		System.out.println("*******   start convert to TOSCA  *******");
		System.out.println("*****************************************");
		final String content = this
				.getFilecontent("/omn/tosca-request-dummy.ttl");

		// System.out.println("orginal file: ");
		// System.out.println(content);

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.TOSCA, content);
		System.out.println(result);
		Assert.assertTrue(
				"should contain tosca namespace",
				result.contains("xmlns=\"http://docs.oasis-open.org/tosca/ns/2011/12\""));
	}

	// TODO
	// @Test
	// public void testConvertToGraphFromTosca() {
	// System.out.println("*************************************************");
	// System.out.println("*******   start convert Tosca to model  *********");
	// System.out.println("*************************************************");
	// final String content = this
	// .getFilecontent("/tosca/request_dummy.xml");
	//
	// final String result = this.converter
	// .post(AbstractConverter.TOSCA, AbstractConverter.TTL,
	// content);
	// System.out.println(result);
	// Assert.assertTrue("should contain a request",
	// result.contains("#Request"));
	// }

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
