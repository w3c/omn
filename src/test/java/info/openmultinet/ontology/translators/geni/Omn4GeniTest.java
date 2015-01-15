package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.translators.geni.exceptions.InvalidModelException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class Omn4GeniTest {

	private InputStream input;
	private Parser parser;

	@Before
	public void setup() {
		this.input = ParserTest.class.getResourceAsStream("/request.ttl");
		this.parser = new Parser(input);
	}

	@Test
	public void testModel2Manifest() throws JAXBException, InvalidModelException {
		Model model = this.parser.getModel();
		String rspec = OMN2Manifest.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest", rspec.contains("type=\"manifest\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testModel2Advertisement() throws JAXBException, InvalidModelException {
		Model model = this.parser.getModel();
		String rspec = OMN2Advertisement.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement", rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

}
