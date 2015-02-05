package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManifestConverterTest {

	@Test
	public void testConvertingSimpleRSpecToGraph() throws JAXBException,
			InvalidModelException {
		final InputStream rspec = ManifestConverterTest.class
				.getResourceAsStream("/geni/manifest/exogeni5nodemanifest.xml");
		final Model model = ManifestConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Manifest);
		Assert.assertTrue("should have a topology (manifest)",
				topology.hasNext());
	}

	@Test
	public void testRoundtripWithRequest() throws JAXBException, IOException, InvalidModelException {
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
		final String outputRspec = ManifestConverter.getRSpec(infModel);
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

	}

	@Test
	public void testConvertingGraph2RSpec() throws JAXBException,
			InvalidModelException {
		InputStream input = ParserTest.class
				.getResourceAsStream("/omn/request.ttl");
		Parser parser = new Parser(input);
		
		final Model model = parser.getInfModel();
		final String rspec = ManifestConverter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest",
				rspec.contains("type=\"manifest\""));
		Assert.assertTrue("should have a motor", rspec.contains("Motor"));
	}
}
