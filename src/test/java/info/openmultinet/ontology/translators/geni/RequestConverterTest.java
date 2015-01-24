package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class RequestConverterTest {

	@Test
	public void test() throws JAXBException, InvalidModelException {
		InputStream rspec = RequestConverterTest.class.getResourceAsStream("/request.rspec");
		Model model = RequestConverter.getModel(rspec);
		ResIterator topology = model.listResourcesWithProperty(RDF.type, Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
		
	}

	@Test
	public void testUnboundRequest() throws JAXBException, InvalidModelException, IOException {
		String filename = "/request_unbound.xml";
		InputStream inputRspec = RequestConverterTest.class.getResourceAsStream(filename);
		System.out.println("Converting this input from '"+filename+"':");
		System.out.println("===============================");
		System.out.println(AbstractConverter.toString(filename));
		System.out.println("===============================");
		
		Model model = RequestConverter.getModel(inputRspec);
		ResIterator topology = model.listResourcesWithProperty(RDF.type, Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(Parser.toString(model));
		System.out.println("===============================");
		
		
		InfModel infModel = new Parser(model).getModel();
		String outputRspec = RequestConverter.getRSpec(infModel);
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");
	}
	
}
