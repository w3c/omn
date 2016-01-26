package info.openmultinet.ontology.translators.geni;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

public class RequestConverterTest {

	@Test
	public void testConvertingRSpecToGraph() throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out.println("*******testConvertingRSpecToGraph()*******");
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_unbound2.xml");
		final Model model = RequestConverter.getModel(rspec);

		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
	}

	@Test
	public void testConvertingBoundRspec2Graph() throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out.println("*******testConvertingBoundRspec2Graph()*******");
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_bound.xml");
		final Model model = RequestConverter.getModel(rspec);
		final String rspecOut = Parser.toString(model);

		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(rspecOut);
		System.out.println("===============================");

		Assert.assertTrue("should have an 'implementedBy' property",
				rspecOut.contains("implementedBy"));
		Assert.assertTrue("should reflect the sliver type",
				rspecOut.contains("raw-pc"));

	}

	@Test
	public void testConvertingBoundMotorRspec2Graph() throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out
				.println("*******testConvertingBoundMotorRspec2Graph()*******");
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_motor.xml");
		final Model model = RequestConverter.getModel(rspec);
		final String rspecOut = Parser.toString(model);

		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(rspecOut);
		System.out.println("===============================");

		Assert.assertTrue("should have an 'implementedBy' property",
				rspecOut.contains("implementedBy"));
		Assert.assertTrue("should reflect the sliver type",
				rspecOut.contains("MotorGarage"));

	}

	@Test
	public void testConvertingBoundRSpec4PhysicalNode2Graph()
			throws JAXBException, InvalidModelException,
			MissingRspecElementException, InvalidRspecValueException {
		System.out
				.println("*******testConvertingBoundRSpec4PhysicalNode2Graph()*******");
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_bound_rawpc.xml");
		final Model model = RequestConverter.getModel(rspec);
		final String graphOut = Parser.toString(model);

		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(graphOut);
		System.out.println("===============================");

		Assert.assertTrue("should have an 'implementedBy' property",
				graphOut.contains("implementedBy"));
		Assert.assertTrue("should reflect the sliver type",
				graphOut.contains("raw-pc"));
	}

	@Test
	public void testConvertingUnboundRspec2Graph() throws JAXBException,
			InvalidModelException, IOException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out.println("*******testConvertingUnboundRspec2Graph()*******");
		final String filename = "/geni/request/request_unbound.xml";
		final InputStream inputRspec = RequestConverterTest.class
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
		final String outputRspec = RequestConverter.getRSpec(infModel);
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");
	}

	@Test
	public void testSliverTypeEqualsRDFType() throws IOException,
			JAXBException, InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out.println("*******testSliverTypeEqualsRDFType()*******");
		final String filename = "/geni/request/request_unbound.xml";
		final InputStream inputRspec = RequestConverterTest.class
				.getResourceAsStream(filename);
		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(AbstractConverter.toString(filename));
		System.out.println("===============================");

		final Model model = RequestConverter.getModel(inputRspec);
		final ResIterator resourceIterator = model
				.listResourcesWithProperty(Omn.isResourceOf);

		Resource resource = resourceIterator.nextResource();
		String label = resource.getProperty(RDFS.label).getObject().asLiteral()
				.getString();

		Assert.assertTrue(resource.hasProperty(RDF.type, Omn_resource.Node));
		Assert.assertTrue(resource.hasProperty(RDF.type,
				model.getResource("http://open-multinet.info/example#raw-pc")));
	}

	@Test
	public void testPaper2015Roundtrip() throws JAXBException,
			InvalidModelException, IOException, MissingRspecElementException,
			InvalidRspecValueException {
		System.out.println("*******testPaper2015Roundtrip()*******");
		final String filename = "/geni/request/request_paper2015.xml";
		final InputStream inputRspec = RequestConverterTest.class
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
		final String outputRspec = RequestConverter.getRSpec(infModel);
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");
		Assert.assertTrue("Should have a sliver_type",
				outputRspec.contains("sliver_type"));
	}

	@Test
	@Ignore
	// fixme: this test is slow!
	public void testRSpecTOSCARoundtrip() throws JAXBException,
			InvalidModelException, IOException, MultipleNamespacesException,
			RequiredResourceNotFoundException, MultiplePropertyValuesException,
			MissingRspecElementException, InvalidRspecValueException {
		System.out.println("*******testRSpecTOSCARoundtrip()*******");
		final String filename = "/geni/request/request_paper2015.xml";
		final InputStream inputRspec = RequestConverterTest.class
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

		List<String> additionalOntologies = new ArrayList<String>();
		additionalOntologies.add("/ontologies/motor.ttl");
		final InfModel infModel = new Parser(model, additionalOntologies)
				.getInfModel();
		final String outputTosca = OMN2Tosca.getTopology(infModel);
		System.out.println("Generated this tosca:");
		System.out.println("===============================");
		System.out.println(outputTosca);
		System.out.println("===============================");
	}

}
