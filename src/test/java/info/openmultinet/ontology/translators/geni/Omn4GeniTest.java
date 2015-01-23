package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class Omn4GeniTest {

	static private InputStream input;
	static private Parser parser;

	@BeforeClass
	public static void setup() throws InvalidModelException {
		input = ParserTest.class.getResourceAsStream("/request.ttl");
		parser = new Parser(input);
	}

	@Test
	public void testModel2Manifest() throws JAXBException, InvalidModelException {
		Model model = parser.getModel();
		String rspec = OMN2Manifest.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest", rspec.contains("type=\"manifest\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testModel2Advertisement() throws JAXBException, InvalidModelException {
		Model model = parser.getModel();
		String rspec = OMN2Advertisement.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement", rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testNTUAAdvertisement() throws JAXBException, InvalidModelException {
		Model model = new Parser("/ntua_offer.ttl").getModel();
		String rspec = OMN2Advertisement.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement", rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should be exclusive", rspec.contains("exclusive=\"true"));
	}
}
