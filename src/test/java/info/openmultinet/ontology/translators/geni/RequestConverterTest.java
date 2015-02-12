package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.validation.constraints.AssertTrue;
import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class RequestConverterTest {

	@Test
	public void testConvertingRSpecToGraph() throws JAXBException, InvalidModelException {
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_unbound2.xml");
		final Model model = RequestConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
	}

	@Test
	public void testConvertingBoundRspec2Graph() throws JAXBException, InvalidModelException {
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_bound.xml");
		final Model model = RequestConverter.getModel(rspec);
		final String rspecOut = Parser.toString(model);
		
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(rspecOut);
		System.out.println("===============================");
		
		Assert.assertTrue("should have an 'implementedBy' property", rspecOut.contains("implementedBy"));
		Assert.assertTrue("should reflect the sliver type", rspecOut.contains("raw-pc"));

	}

	@Test
	public void testConvertingUnboundRspec2Graph() throws JAXBException,
			InvalidModelException, IOException {
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
    public void testSliverTypeEqualsRDFType() throws IOException, JAXBException, InvalidModelException {
        final String filename = "/geni/request/request_unbound.xml";
        final InputStream inputRspec = RequestConverterTest.class
                .getResourceAsStream(filename);
        System.out.println("Converting this input from '" + filename + "':");
        System.out.println("===============================");
        System.out.println(AbstractConverter.toString(filename));
        System.out.println("===============================");

        final Model model = RequestConverter.getModel(inputRspec);
        final ResIterator resourceIterator = model.listResourcesWithProperty(Omn.isResourceOf);

        Assert.assertEquals(resourceIterator.nextResource().getProperty(RDF.type).getObject().asResource().getURI(), "raw-pc");
    }

	@Test
	public void testPaper2015Roundtrip() throws JAXBException,
			InvalidModelException, IOException {
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
		Assert.assertTrue("Should have a sliver_type", outputRspec.contains("sliver_type"));
	}

}
