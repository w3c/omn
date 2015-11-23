package info.openmultinet.ontology.translators.geni.manifest;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverterTest;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.translators.geni.RequestConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Exogeni5NodeManifestTest {

	@Test
	public void manifestRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		final String filename = "/geni/manifest/exogeni5nodemanifest.xml";
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

		// PrintWriter outFile = new PrintWriter("filename.xml");
		// outFile.println(outputRspec);
		// outFile.close();

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
				outputRspec.contains("client_id=\"nodeA\""));

		Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
		NodeList rspec = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "rspec");
		Assert.assertTrue(rspec.getLength() == 1);

		NodeList nodes = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "node");
		Assert.assertTrue(nodes.getLength() == 5);

		NodeList links = xmlDoc.getElementsByTagNameNS(
				"http://www.geni.net/resources/rspec/3", "link");
		Assert.assertTrue(links.getLength() == 6);

		NodeList sliceInfo = xmlDoc
				.getElementsByTagNameNS(
						"http://groups.geni.net/exogeni/attachment/wiki/RspecExtensions/slice-info/1",
						"geni_slice_info");
		Assert.assertTrue(sliceInfo.getLength() == 1);

		String sliceUuid = sliceInfo.item(0).getAttributes()
				.getNamedItem("uuid").getNodeValue();
		Assert.assertTrue(sliceUuid
				.equals("2127113a-474f-4a9e-aad0-d4e7b22267ca"));

		String sliceUrn = sliceInfo.item(0).getAttributes().getNamedItem("urn")
				.getNodeValue();
		Assert.assertTrue(sliceUrn
				.equals("urn:publicid:IDN+panther:GIMITesting+slice+dbhatexo5node"));

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);
	}
}
