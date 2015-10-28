package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;

public class AdToscaTest {

	// @Test
	// public void adRoundtrip() throws JAXBException, InvalidModelException,
	// IOException, XMLStreamException, MissingRspecElementException,
	// DeprecatedRspecVersionException {
	//
	// final String filename = "/geni/advertisement/osco2.xml";
	// final String inputRspec = AbstractConverter.toString(filename);
	//
	// System.out.println("Converting this input from '" + filename + "':");
	// System.out.println("===============================");
	// System.out.println(inputRspec);
	// System.out.println("===============================");
	//
	// final String outputRspec = RSpecValidation.completeRoundtripVerbose(
	// inputRspec, true);
	//
	// System.out.println("Generated this rspec:");
	// System.out.println("===============================");
	// System.out.println(outputRspec);
	// System.out.println("===============================");
	//
	// Assert.assertTrue("type",
	// outputRspec.contains("type=\"advertisement\""));
	//
	// System.out.println("===============================");
	// System.out.println("Diffs:");
	// int[] diffsNodes = RSpecValidation.getDiffsNodesVerbose(inputRspec,
	// true);
	//
	// // TODO: This test does not consistently return 0, only sometimes. Need
	// // to debug.
	// // Assert.assertTrue("No differences between input and output files",
	// // diffsNodes[0] == 0);
	// // System.out.println("===============================");
	// // System.out.println("Diffs:");
	//
	// if (diffsNodes[0] == 0) {
	// // TODO: This test does not consistently return 0, only sometimes.
	// // Need
	// // to debug.
	// Assert.assertTrue("No differences between input and output files",
	// diffsNodes[0] == 0);
	// } else {
	// Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);
	//
	// // check that output has one rspec element
	// NodeList rspec = xmlDoc.getElementsByTagNameNS(
	// "http://www.geni.net/resources/rspec/3", "rspec");
	// Assert.assertTrue(rspec.getLength() == 1);
	//
	// NodeList nodes = xmlDoc.getElementsByTagNameNS(
	// "http://www.geni.net/resources/rspec/3", "node");
	// Assert.assertTrue(nodes.getLength() == 2);
	//
	// NodeList osco = xmlDoc.getElementsByTagNameNS(
	// "http://opensdncore.org/ontology/", "osco");
	// Assert.assertTrue(osco.getLength() == 2);
	// }
	// }

	@Test
	public void modelToRspec() throws FileNotFoundException,
			InvalidModelException, JAXBException {
		InputStream input = new FileInputStream(
				"./src/test/resources/omn/daniel.ttl");

		Parser parser = new Parser();
		parser.read(input);
		final Model model = parser.getModel();
		String modelString = Parser.toString(model);
		System.out.println(modelString);

		AdvertisementConverter converter = new AdvertisementConverter();
		converter.setVerbose(true);
		String outputRspec = converter.getRSpec(model);
		System.out.println(outputRspec);
	}
}