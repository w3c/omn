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

public class RequestDemo201504 {

	@Test
	public void requestRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {
		final String filename = "/geni/request/request_demo201504.xml";
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
		Assert.assertTrue("client id",
				outputRspec.contains("client_id=\"f4f-demo-gent-2015\""));

		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);

		NodeList node = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "node");
		Assert.assertTrue(node.getLength() == 1);

		String componentId = node.item(0).getAttributes()
				.getNamedItem("component_id").getNodeValue();
		Assert.assertTrue(componentId
				.equals("urn:publicid:IDN+demo.fiteagle.org+node+http%3A%2F%2Fdemo.fiteagle.org%2Fresource%2FVMServer-1"));

		String componentManagerId = node.item(0).getAttributes()
				.getNamedItem("component_manager_id").getNodeValue();
		Assert.assertTrue(componentManagerId
				.equals("urn:publicid:IDN+demo.fiteagle.org+authority+cm"));

		String clientId = node.item(0).getAttributes()
				.getNamedItem("client_id").getNodeValue();
		Assert.assertTrue(clientId.equals("f4f-demo-gent-2015"));

		String componentName = node.item(0).getAttributes()
				.getNamedItem("component_name").getNodeValue();
		Assert.assertTrue(componentName.equals("VMServer-1"));

		NodeList sliverType = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "sliver_type");
		Assert.assertTrue(sliverType.getLength() == 1);

		String sliverName = sliverType.item(0).getAttributes()
				.getNamedItem("name").getNodeValue();
		Assert.assertTrue(sliverName
				.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn-domain-pc#VM"));

		NodeList monitoring = xmlDoc.getElementsByTagNameNS(
				"http://monitoring.service.tu-berlin.de/monitoring",
				"monitoring");
		Assert.assertTrue(monitoring.getLength() == 1);

		String monitoringUri = monitoring.item(0).getAttributes()
				.getNamedItem("uri").getNodeValue();
		Assert.assertTrue(monitoringUri
				.equals("http://federation.av.tu-berlin.de:3003"));

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);
	}

}
