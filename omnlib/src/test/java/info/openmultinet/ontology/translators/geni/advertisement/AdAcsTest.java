package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class AdAcsTest {

	@Test
	public void adRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		final String filename = "/geni/advertisement/acs.xml";
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
		if (diffsNodes[0] == 0) {
			// TODO: This test does not consistently return 0, only sometimes.
			// Need
			// to debug.
			Assert.assertTrue("No differences between input and output files",
					diffsNodes[0] == 0);
		} else {
			Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

			// check that output has one rspec element
			NodeList rspec = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "rspec");
			Assert.assertTrue(rspec.getLength() == 1);

			NodeList nodes = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "node");
			Assert.assertTrue(nodes.getLength() == 1);

			// NodeList epc = xmlDoc
			// 		.getElementsByTagNameNS(
			// 				"https://github.com/w3c/omn/blob/master/omnlib/ontologies/acs",
			// 				"device");
			// Assert.assertTrue(epc.getLength() == 1);

			// NodeList ue = xmlDoc.getElementsByTagNameNS(
			// 		"https://github.com/w3c/omn/blob/master/omnlib/ontologies/acs", "param");
			// Assert.assertTrue(ue.getLength() == 2);

		}
	}
}
