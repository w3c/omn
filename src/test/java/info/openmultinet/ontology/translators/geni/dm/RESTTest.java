package info.openmultinet.ontology.translators.geni.dm;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.AbstractConverter;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RESTTest {

	private RESTConverter converter;

	@Before
	public void setup() {
		this.converter = new RESTConverter();
	}
	
	@Test
	@SuppressWarnings("resource")
	public void testConvertToGraph() throws JAXBException, IOException, InvalidModelException {
		String content = getFilecontent("/request2.rspec");

		String result = converter.convert(AbstractConverter.RSPEC_REQUEST,
				AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain a request", result.contains("#Request"));
	}

	@Test
	@SuppressWarnings("resource")
	public void testConvertToRspec() throws JAXBException, IOException, InvalidModelException {
		String content = getFilecontent("/request.ttl");
		
		String result = converter.convert(AbstractConverter.TTL, 
				AbstractConverter.RSPEC_ADVERTISEMENT,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec", result.contains("type=\"advertisement"));
	}

	public String getFilecontent(String filename) {
		InputStream rspec = RESTTest.class
				.getResourceAsStream(filename);
		String content = new Scanner(rspec, "UTF-8").useDelimiter("\\A").next();
		return content;
	}

	
}
