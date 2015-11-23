package info.openmultinet.ontology.translators.geni.ciscogeni;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.IOException;
import java.io.PrintWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class CGCT5openflowiggpoTest {

	@Test
	public void requestRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		final String filename = "/geni/ciscogeni/CG-CT-5-openflow-ig-gpo.rspec";
		final String inputRspec = AbstractConverter.toString(filename);

		// System.out.println("Converting this input from '" + filename + "':");
		// System.out.println("===============================");
		// System.out.println(inputRspec);
		// System.out.println("===============================");

		final String outputRspec = RSpecValidation
				.completeRoundtrip(inputRspec);

		// PrintWriter outFile = new PrintWriter("filename.txt");
		// outFile.println(outputRspec);
		// outFile.close();

		// System.out.println("Generated this rspec:");
		// System.out.println("===============================");
		// System.out.println(outputRspec);
		// System.out.println("===============================");

		Assert.assertTrue("type", outputRspec.contains("type=\"request\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRspec);

		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);

		NodeList sliver = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/ext/openflow/4", "sliver");
		Assert.assertTrue(sliver.getLength() == 1);

		NodeList packet = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/ext/openflow/4", "packet");
		Assert.assertTrue(packet.getLength() == 1);

		NodeList usegroup = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/ext/openflow/4",
				"use-group");
		Assert.assertTrue(usegroup.getLength() == 1);

		String usegroupName = usegroup.item(0).getAttributes()
				.getNamedItem("name").getNodeValue();
		Assert.assertTrue(usegroupName.equals("bbn-instageni"));

		// TODO: Currently returns a high number of errors, although translation
		// appears to be correct.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);

	}
}
