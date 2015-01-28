package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.InputStream;
import java.util.Scanner;

import javax.ws.rs.WebApplicationException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class RESTTest {

	private REST converter;

	@Before
	public void setup() {
		this.converter = new REST();
	}

	@Test
	public void testConvertToGraph() {
		final String content = this.getFilecontent("/request2.rspec");

		final String result = this.converter
				.post(AbstractConverter.RSPEC_REQUEST,
						AbstractConverter.TTL, content);
		System.out.println(result);
		Assert.assertTrue("should contain a request",
				result.contains("#Request"));
	}

	@Test
	public void testConvertToRspec() {
		final String content = this.getFilecontent("/request.ttl");

		final String result = this.converter.post(AbstractConverter.TTL,
				AbstractConverter.RSPEC_ADVERTISEMENT, content);
		System.out.println(result);
		Assert.assertTrue("should contain an advertisement rspec",
				result.contains("type=\"advertisement"));
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
