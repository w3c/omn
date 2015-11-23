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

public class ManifestAcsTest {

	@Test(expected = InvalidModelException.class)
	public void testConvertingGraph2RSpec() throws InvalidModelException,
			JAXBException {
		InputStream input = ManifestConverterTest.class
				.getResourceAsStream("/omn/acs.ttl");
		Parser parser = new Parser(input);

		final Model model = parser.getInfModel();
		final String rspec = ManifestConverter.getRSpec(model, "localhost");
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest",
				rspec.contains("type=\"manifest\""));
	}

	@Test
	public void manifestRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		final String filename = "/geni/manifest/acs.xml";
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
	}
}
