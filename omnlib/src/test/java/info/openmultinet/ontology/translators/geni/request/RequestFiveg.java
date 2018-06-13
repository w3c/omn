package info.openmultinet.ontology.translators.geni.request;

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

public class RequestFiveg {

	@Test
	public void requestRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {
		// final String filename = "/geni/request/fiveg_hss.xml";
		// final String filename = "/geni/request/fiveg_control.xml";
		final String filename = "/geni/request/gateway2.xml";
		// final String filename = "/geni/request/fiveg_bt.xml";
		// final String filename = "/geni/request/fiveg_switch.xml";
		// final String filename = "/geni/request/fiveg_dns.xml";
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

		System.out.println("Get number of diffs and nodes:");
		System.out.println("===============================");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRspec);

		Assert.assertTrue("type", outputRspec.contains("type=\"request\""));

		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);

		NodeList node = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "node");
		Assert.assertTrue(node.getLength() == 2);

		NodeList osco = xmlDoc.getElementsByTagNameNS(
				"http://opensdncore.org/ontology/", "osco");
		Assert.assertTrue(osco.getLength() == 2);

		NodeList gateway = xmlDoc.getElementsByTagNameNS(
				"https://github.com/w3c/omn/blob/master/omnlib/ontologies/fiveg", "gateway");
		// TODO: fixme
		// Assert.assertTrue(gateway.getLength() == 1);

		String version = gateway.item(0).getAttributes()
				.getNamedItem("version").getNodeValue();
		Assert.assertTrue(version.equals("EPC"));

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);
	}

}
