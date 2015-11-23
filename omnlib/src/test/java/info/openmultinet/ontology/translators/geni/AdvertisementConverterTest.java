package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

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
		InputStream input = AdvertisementConverterTest.class
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

	// not an Advertisement!!
	// @Test
	// public void testConvertingGraphToRSpec() throws JAXBException,
	// InvalidModelException {
	// parser.read("/omn/request.ttl");
	// final Model model = parser.getModel();
	// final String rspec = converter.getRSpec(model);
	// System.out.println(rspec);
	// Assert.assertTrue("should be an advertisement",
	// rspec.contains("type=\"advertisement\""));
	// Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	// }

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
	public void testPaper2015Roundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {
		long start;
		String inputFile = "/geni/advertisement/advertisement_paper2015.xml";

		System.out.println("================================================");
		System.out.println("Operation: Reading reference advertisement RSpec");
		start = System.nanoTime();
		final InputStream input = AdvertisementConverter.class
				.getResourceAsStream(inputFile);
		System.out.println("Input:");
		String inStr = AbstractConverter.toString(inputFile);
		System.out
				.println(StringUtils.abbreviateMiddle(inStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to object model (jaxb)");
		RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to omn graph");
		start = System.nanoTime();
		final Model model = converter.getModel(rspec);
		System.out.println("Result:");
		String modStr = Parser.toString(model);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out
				.println("Converting to reference advertisement RSpec again...");
		start = System.nanoTime();
		final String advertisement = converter.getRSpec(model);
		System.out.println("Result:");
		System.out.println(StringUtils.abbreviateMiddle(advertisement,
				"\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");
	}

	@Test
	@Ignore
	// fixme: this test is slow!
	public void testRoundtripRSpecToRSpecPerformance() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			InterruptedException, MissingRspecElementException,
			InvalidRspecValueException {
		String inputFile = "/geni/advertisement/advertisement_vwall1.xml";

		InputStream input = null;
		long start = 0;
		Model model = null;
		JAXBElement<RSpecContents> advertisement = null;
		RSpecContents rspec = null;
		final int repetitions = 1;
		final int warmups = 1;
		final int pause = 1;
		String method = "";
		String result = "";
		Writer writer = null;

		method = "convert.xml2jaxb";
		System.out.println("Operation: " + method);
		writer = new BufferedWriter(new FileWriter(new File(method + ".data")));

		for (int i = 0; i < warmups; i++)
			converter.getRspec(AdvertisementConverter.class
					.getResourceAsStream(inputFile));
		for (int i = 0; i < repetitions; i++) {
			Thread.sleep(pause);
			input = AdvertisementConverter.class.getResourceAsStream(inputFile);
			start = System.nanoTime();
			rspec = converter.getRspec(input);
			result = i + " " + (System.nanoTime() - start) + "\n";
			System.out.print(result);
			writer.write(result);
			input = null;
		}
		writer.close();
		System.out.println("XML serialized size (kb): "
				+ AbstractConverter.toString(inputFile).length() / 1024);

		method = "convert.jaxb2rdf";
		System.out.println("Operation: " + method);
		writer = new BufferedWriter(new FileWriter(new File(method + ".data")));
		for (int i = 0; i < warmups; i++)
			converter.getModel(rspec);
		for (int i = 0; i < repetitions; i++) {
			Thread.sleep(pause);
			converter.resetModel();
			start = System.nanoTime();
			model = converter.getModel(rspec);
			result = i + " " + (System.nanoTime() - start) + "\n";
			System.out.print(result);
			writer.write(result);
		}
		writer.close();
		System.out.println("TTL serialized size (kb): "
				+ Parser.toString(model).length() / 1024);

		method = "convert.rdf2jaxb";
		System.out.println("Operation: " + method);
		writer = new BufferedWriter(new FileWriter(new File(method + ".data")));
		for (int i = 0; i < warmups; i++)
			converter.getRSpec(model);
		for (int i = 0; i < repetitions; i++) {
			Thread.sleep(pause);
			start = System.nanoTime();
			advertisement = converter.getRSpecObject(model);
			result = i + " " + (System.nanoTime() - start) + "\n";
			System.out.print(result);
			writer.write(result);
		}
		writer.close();

		method = "convert.jaxb2xml";
		System.out.println("Operation: " + method);
		writer = new BufferedWriter(new FileWriter(new File(method + ".data")));
		for (int i = 0; i < warmups; i++)
			AdvertisementConverter.toString(advertisement);
		for (int i = 0; i < repetitions; i++) {
			Thread.sleep(pause);
			start = System.nanoTime();
			AdvertisementConverter.toString(advertisement);
			result = i + " " + (System.nanoTime() - start) + "\n";
			System.out.print(result);
			writer.write(result);
		}
		writer.close();
	}

}
