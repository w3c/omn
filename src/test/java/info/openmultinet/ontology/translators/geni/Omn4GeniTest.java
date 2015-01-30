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
		Omn4GeniTest.input = ParserTest.class
				.getResourceAsStream("/omn/request.ttl");
		Omn4GeniTest.parser = new Parser(Omn4GeniTest.input);
	}

	@Test
	public void testModel2Manifest() throws JAXBException,
			InvalidModelException {
		final Model model = Omn4GeniTest.parser.getInfModel();
		final String rspec = ManifestConverter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest",
				rspec.contains("type=\"manifest\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testModel2Advertisement() throws JAXBException,
			InvalidModelException {
		final Model model = Omn4GeniTest.parser.getInfModel();
		final String rspec = AdvertisementConverter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testNTUAAdvertisement() throws JAXBException,
			InvalidModelException {
		final Model model = new Parser("/omn/ntua_offer.ttl").getInfModel();
		final String rspec = AdvertisementConverter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should be exclusive",
				rspec.contains("exclusive=\"true"));
	}
}
