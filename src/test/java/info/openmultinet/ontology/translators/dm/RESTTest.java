package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.WebApplicationException;

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
	public void testConvertToGraph() {
		String content = getFilecontent("/request2.rspec");

		String result = converter.convert(AbstractConverter.RSPEC_REQUEST,
				AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain a request", result.contains("#Request"));
	}

	@Test
	public void testConvertToRspec(){
		String content = getFilecontent("/request.ttl");
		
		String result = converter.convert(AbstractConverter.TTL, 
				AbstractConverter.RSPEC_ADVERTISEMENT,
				content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec", result.contains("type=\"advertisement"));
	}

	@Test (expected=WebApplicationException.class)
	public void testWrongParam(){
		converter.convert("a",  "b", null);
	}

	public String getFilecontent(String filename) {
		InputStream rspec = RESTTest.class
				.getResourceAsStream(filename);
		@SuppressWarnings("resource")
		String content = new Scanner(rspec, "UTF-8").useDelimiter("\\A").next();
		return content;
	}

	
}
