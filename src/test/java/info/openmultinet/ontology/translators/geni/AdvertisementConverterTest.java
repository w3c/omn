package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementConverterTest {

	private AdvertisementConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new AdvertisementConverter();
	}

	@Test
	public void testConvertingGraph2ToRSpecPaper() throws JAXBException,
			InvalidModelException {
		InputStream input = ParserTest.class
				.getResourceAsStream("/omn/offer_paper2015.ttl");
		parser.read(input);
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));
		final String rspec = converter.getRSpec(model);

		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motorgarage",
				rspec.contains("MotorGarage"));
	}

	@Test
	public void testConvertingGraphToRSpec() throws JAXBException,
			InvalidModelException {
		parser.read("/omn/request.ttl");
		final Model model = parser.getModel();
		final String rspec = converter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	public void testConvertingRSpecToGraphFromNTUA() throws JAXBException,
			InvalidModelException {
		parser.read("/omn/ntua_offer.ttl");
		final Model model = parser.getModel();
		final String rspec = converter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should be exclusive",
				rspec.contains("exclusive=\"true"));
	}

	@Test
	public void testRoundtripRSpecToRSpec() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		long start;
		String inputFile = "/geni/advertisement/advertisement_paper2015.xml";

		System.out.println("================================================");
		System.out.println("Operation: Reading reference advertisement RSpec");
		start = System.currentTimeMillis();
		final InputStream input = AdvertisementConverter.class
				.getResourceAsStream(inputFile);
		System.out.println("Input:");
		String inStr = AbstractConverter.toString(inputFile);
		System.out
				.println(StringUtils.abbreviateMiddle(inStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to object model (jaxb)");
		RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to omn graph");
		start = System.currentTimeMillis();
		final Model model = converter.getModel(rspec);
		System.out.println("Result:");
		String modStr = Parser.toString(model);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out
				.println("Converting to reference advertisement RSpec again...");
		start = System.currentTimeMillis();
		final String advertisement = converter.getRSpec(model);
		System.out.println("Result:");
		System.out.println(StringUtils.abbreviateMiddle(advertisement,
				"\n...\n", 4096));
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		System.out.println("================================================");
	}

	@Test
	public void testRoundtripRSpecToRSpecPerformance() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		long start;
		String inputFile = "/geni/advertisement/advertisement_vwall1.xml";

		final InputStream input = AdvertisementConverter.class
				.getResourceAsStream(inputFile);

		System.out.println("================================================");
		System.out.println("Operation (jaxb): Reading large RSpec '"
				+ inputFile + "' into object model");
		start = System.currentTimeMillis();
		RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration (ms): "
				+ (System.currentTimeMillis() - start));
		System.out.println("XML serialized size (kb): "
				+ AbstractConverter.toString(inputFile).length() / 1024);
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation (jena): Converting into graph");
		start = System.currentTimeMillis();
		final Model model = converter.getModel(rspec);
		System.out.println("Duration (ms): "
				+ (System.currentTimeMillis() - start));
		System.out.println("TTL serialized size (kb): "
				+ Parser.toString(model).length() / 1024);
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation (jaxb): Converting into RSpec");
		start = System.currentTimeMillis();
		final String advertisement = converter.getRSpec(model);
		System.out.println("Duration (ms): "
				+ (System.currentTimeMillis() - start));
		System.out.println("XML serialized size (kb): "
				+ advertisement.length() / 1024);
		System.out.println("================================================");
	}

}
