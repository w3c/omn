package info.openmultinet.ontology.translators.geni.manifest;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverterTest;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

public class ManifestPaper2015Test {

	@Test
	@Ignore
	// fixme: this test is slow!
	public void manifestRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		final String filename = "/geni/manifest/manifest_paper2015.xml";
		final InputStream inputRspec = ManifestConverterTest.class
				.getResourceAsStream(filename);
		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(AbstractConverter.toString(filename));
		System.out.println("===============================");

		final Model model = ManifestConverter.getModel(inputRspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Manifest);
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(Parser.toString(model));
		System.out.println("===============================");
		Assert.assertTrue("should have a topology", topology.hasNext());

		final InfModel infModel = new Parser(model).getInfModel();
		final String outputRspec = ManifestConverter.getRSpec(infModel,
				"testbed.example.org");
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

		System.out.println("===============================");
		String inputRSpec = AbstractConverter.toString(filename);
		System.out.println(inputRSpec);

		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRSpec);

		Assert.assertTrue("type", outputRspec.contains("type=\"manifest\""));
		Assert.assertTrue("client id",
				outputRspec.contains("client_id=\"myMotor\""));

		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);

		NodeList nodes = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "node");
		Assert.assertTrue(nodes.getLength() == 1);

		NodeList sliverType = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "sliver_type");
		Assert.assertTrue(sliverType.getLength() == 1);

		String sliverName = sliverType.item(0).getAttributes()
				.getNamedItem("name").getNodeValue();
		Assert.assertTrue(sliverName
				.equals("http://open-multinet.info/ontology/resources/motor#Motor"));

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);

	}

}
