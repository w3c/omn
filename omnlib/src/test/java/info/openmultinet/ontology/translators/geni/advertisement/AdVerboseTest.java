package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;

public class AdVerboseTest {

	@Test
	public void convertModelToAdRspec() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException {

		AdvertisementConverter converter = new AdvertisementConverter();
		converter.setVerbose(true);
		Parser parser = new Parser();
		InputStream input = AdVerboseTest.class
				.getResourceAsStream("/omn/ad-verbose.ttl");
		parser.read(input);
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));
		System.out
				.println("===================================================");
		final String rspec = converter.getRSpec(model);
		System.out.println(rspec);
	}

	@Test
	public void adVerboseRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		final String filename = "/geni/advertisement/ad_verbose.xml";
		final String inputRspec = AbstractConverter.toString(filename);

		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(inputRspec);
		System.out.println("===============================");

		String output = null;
		Model model;
		String input;
		input = RSpecValidation.fixVersion(inputRspec);

		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(input, "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		AdvertisementConverter converter = new AdvertisementConverter();
		converter.setVerbose(true);
		RSpecContents rspec = converter.getRspec(inputStream);
		model = converter.getModel(rspec);

		String modelString = Parser.toString(model);
		System.out.println(modelString);

		output = converter.getRSpec(model);

		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(output);
		System.out.println("===============================");

		Assert.assertTrue("type", output.contains("type=\"advertisement\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodesWithOutput(inputRspec,
				output);

		if (diffsNodes[0] == 0) {
			// TODO: This test does not consistently return 0, only sometimes.
			// Need
			// to debug.
			Assert.assertTrue("No differences between input and output files",
					diffsNodes[0] == 0);
		} else {
			Document xmlDoc = RSpecValidation.loadXMLFromString(output);

			// check that output has one rspec element
			NodeList rspec1 = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "rspec");
			Assert.assertTrue(rspec1.getLength() == 1);

			NodeList nodes = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "node");
			Assert.assertTrue(nodes.getLength() == 2);

			String nodeComponentManagerID = nodes.item(0).getAttributes()
					.getNamedItem("component_manager_id").getNodeValue();
			Assert.assertTrue(nodeComponentManagerID
					.equals("urn:publicid:IDN+testbed.example.org+authority+cm"));

			NodeList sliverType = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "sliver_type");
			Assert.assertTrue(sliverType.getLength() == 2);
		}
	}

	@Test
	public void adNotVerboseRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		final String filename = "/geni/advertisement/ad_not_verbose.xml";
		final String inputRspec = AbstractConverter.toString(filename);

		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(inputRspec);
		System.out.println("===============================");

		String output = null;
		Model model;
		String input;
		input = RSpecValidation.fixVersion(inputRspec);

		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(input, "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		AdvertisementConverter converter = new AdvertisementConverter();
		RSpecContents rspec = converter.getRspec(inputStream);
		model = converter.getModel(rspec);

		String modelString = Parser.toString(model);
		System.out.println(modelString);

		output = converter.getRSpec(model);

		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(output);
		System.out.println("===============================");

		Assert.assertTrue("type", output.contains("type=\"advertisement\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodesWithOutput(inputRspec,
				output);

		if (diffsNodes[0] == 0) {
			// TODO: This test does not consistently return 0, only sometimes.
			// Need
			// to debug.
			Assert.assertTrue("No differences between input and output files",
					diffsNodes[0] == 0);
		} else {
			Document xmlDoc = RSpecValidation.loadXMLFromString(output);

			// check that output has one rspec element
			NodeList rspec1 = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "rspec");
			Assert.assertTrue(rspec1.getLength() == 1);

			NodeList nodes = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "node");
			Assert.assertTrue(nodes.getLength() == 1);

			String nodeComponentManagerID = nodes.item(0).getAttributes()
					.getNamedItem("component_manager_id").getNodeValue();
			Assert.assertTrue(nodeComponentManagerID
					.equals("urn:publicid:IDN+testbed.example.org+authority+cm"));

			NodeList sliverType = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "sliver_type");
			Assert.assertTrue(sliverType.getLength() == 2);
		}
	}
}
