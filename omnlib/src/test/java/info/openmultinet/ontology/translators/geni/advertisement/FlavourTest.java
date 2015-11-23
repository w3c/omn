package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverterTest;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class FlavourTest {

	private AdvertisementConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new AdvertisementConverter();
	}

	@Test
	public void testConvertingGraph2ToRSpecPaper() throws JAXBException,
			InvalidModelException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {

		System.out.println("************************************");
		String filename = "/omn/flavour2.ttl";
		System.out.println(" Test input: " + filename);
		System.out.println("************************************");

		InputStream input = AdvertisementConverterTest.class
				.getResourceAsStream(filename);
		parser.read(input);
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));
		final String rspec = converter.getRSpec(model);

		System.out.println("************************************");
		System.out.println(" Test output input Rspec");
		System.out.println("************************************");
		System.out.println(rspec);

		InputStream stream = new ByteArrayInputStream(
				rspec.getBytes(StandardCharsets.UTF_8));
		final Model model2 = converter.getModel(stream);
		String modStr = Parser.toString(model2);

		System.out.println("************************************");
		System.out.println(" Test output of model");
		System.out.println("************************************");
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
	}

	@Test
	public void adRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		final String filename = "/geni/advertisement/flavour.xml";
		final String inputRspec = AbstractConverter.toString(filename);

		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(inputRspec);
		System.out.println("===============================");

		final String outputRspec = RSpecValidation
				.completeRoundtrip(inputRspec);

		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

		Assert.assertTrue("type",
				outputRspec.contains("type=\"advertisement\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRspec);
	}
}