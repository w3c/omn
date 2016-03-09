package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManifestConverterTest {

	@Test
	public void testConvertingComplexRSpecToGraph() throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		final InputStream rspec = ManifestConverterTest.class
				.getResourceAsStream("/geni/manifest/exogeni5nodemanifest.xml");
		final Model model = ManifestConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Manifest);
		Assert.assertTrue("should have a topology (manifest)",
				topology.hasNext());
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(Parser.toString(model));
		System.out.println("===============================");
	}

	@Test
	public void testRoundtripWithRequest() throws JAXBException, IOException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		final String filename = "/geni/request/request_bound.xml";
		final InputStream inputRspec = ManifestConverterTest.class
				.getResourceAsStream(filename);
		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(AbstractConverter.toString(filename));
		System.out.println("===============================");

		final Model model = RequestConverter.getModel(inputRspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(Parser.toString(model));
		System.out.println("===============================");

		final InfModel infModel = new Parser(model).getInfModel();
		final String outputRspec = ManifestConverter.getRSpec(infModel,
				"localhost");
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

	}

	@Test
	@Ignore
	//todo: takes too long
	public void testConvertingGraph2RSpec() throws JAXBException,
			InvalidModelException {
		InputStream input = ManifestConverterTest.class
				.getResourceAsStream("/omn/request.ttl");
		Parser parser = new Parser(input);

		final Model model = parser.getInfModel();
		final String rspec = ManifestConverter.getRSpec(model, "localhost");
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest",
				rspec.contains("type=\"manifest\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}

	@Test
	@Ignore
	//todo: takes too long
	// fixme: this test is slow!
	public void testPaper2015Roundtrip() throws JAXBException, IOException,
			InvalidModelException, MissingRspecElementException,
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

	}

}
