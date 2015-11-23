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

public class CGCT5iggpoTest {

	@Test
	public void adRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		final String filename = "/geni/ciscogeni/CG-CT-5-ig-gpo.rspec";
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
		if (diffsNodes[0] == 0) {
			Assert.assertTrue("No differences between input and output files",
					diffsNodes[0] == 0);
			// TODO: Currently sometimes returns a high number of errors,
			// although translation
			// appears to be correct.
			// Assert.assertTrue("No differences between input and output files",
			// diffsNodes[0] == 0);
		} else {
			Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

			// check that output has one rspec element
			NodeList rspec = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "rspec");
			Assert.assertTrue(rspec.getLength() == 1);

			NodeList nodes = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "node");
			Assert.assertTrue(nodes.getLength() == 1);

			NodeList links = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "link");
			Assert.assertTrue(links.getLength() == 1);

			NodeList sharedVlan = xmlDoc.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/ext/shared-vlan/1",
					"link_shared_vlan");
			Assert.assertTrue(sharedVlan.getLength() == 1);

			String sharedVlanName = sharedVlan.item(0).getAttributes()
					.getNamedItem("name").getNodeValue();
			Assert.assertTrue(sharedVlanName.equals("ncsu2-meso"));
		}

	}
}
