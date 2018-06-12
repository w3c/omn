package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.geni.RequestConverter;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class DemoTestRequest {

	private RequestConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new RequestConverter();
	}

	@Test
	@Ignore
	// fixme
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {

		System.out.println("================================================");
		System.out.println("Reading Graph");
		System.out.println("================================================");
		parser.read("/omn/paper2015iswc/omn_2_request.ttl");
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));

		System.out.println("================================================");
		System.out.println("Converting to RSpec");
		System.out.println("================================================");
		long start;
		start = System.nanoTime();
		final String rspec = RequestConverter.getRSpec(model);
		System.out.println(rspec);
		Assert.assertTrue("should be a request",
				rspec.contains("type=\"request\""));
		System.out.println("Duration: " + (System.nanoTime() - start));

		System.out.println("================================================");
		System.out.println("Converting back to Graph");
		System.out.println("================================================");
		start = System.nanoTime();
		final InputStream inputRspec = new ByteArrayInputStream(
				rspec.getBytes());
		final Model newModel = RequestConverter.getModel(inputRspec);
		System.out.println(Parser.toString(newModel));
		System.out.println("Duration: " + (System.nanoTime() - start));

		final ResIterator topology = newModel.listResourcesWithProperty(
				RDF.type, Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
		Resource requestResource = topology.nextResource();

		StmtIterator nodes = requestResource.listProperties(Omn.hasResource);
		Assert.assertTrue("should have a resource", nodes.hasNext());

		Resource resourceResource = nodes.next().getResource();
		Assert.assertTrue("object of hasResource is of type node",
				resourceResource.hasProperty(RDF.type, Omn_resource.Node));

		Resource loginResource = resourceResource.getProperty(Omn.hasService)
				.getObject().asResource();
		Assert.assertTrue("object of hasService is of type LoginService",
				loginResource.hasProperty(RDF.type, Omn_service.LoginService));

		Resource monitoringResource = resourceResource
				.getProperty(Omn_lifecycle.usesService).getObject()
				.asResource();
		System.out.println(monitoringResource.getProperty(RDF.type));
		Assert.assertTrue(
				"object of usesService is of type OMSPService",
				monitoringResource
						.getProperty(RDF.type)
						.toString()
						.contains(
								"https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn-monitoring#OMSPService"));
		Assert.assertTrue("label is OMSPSerivce", monitoringResource
				.getProperty(RDFS.label).getObject().asLiteral().toString()
				.equals("OMSPService"));
	}
}