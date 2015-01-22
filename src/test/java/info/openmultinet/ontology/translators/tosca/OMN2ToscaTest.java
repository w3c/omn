package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.ServiceTypeNotFoundException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;

public class OMN2ToscaTest {

	private InputStream input;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException {
		this.input = getClass().getResourceAsStream("/tosca-request.ttl");
		this.parser = new Parser(input);
	}

	@Test
	public void testGetTopology() throws JAXBException, InvalidModelException, ServiceTypeNotFoundException, RequiredResourceNotFoundException {
		InfModel model = this.parser.getModel();
		String topology = OMN2Tosca.getTopology(model);
		System.out.println(topology);
		testToscaDefinitions(topology);
		Assert.assertTrue("Should contain the properties set", topology.contains("<osco:parameter2>bar</osco:parameter2>"));
		Assert.assertTrue("Should contain the properties set", topology.contains("<osco:port>8088</osco:port>"));
		Assert.assertTrue("Should contain the properties set", topology.contains("<osco:test_param>foo</osco:test_param>"));
		Assert.assertTrue("Should contain type definitions for parameters", topology.contains("<xs:element name=\"port\" type=\"xs:integer\"/>"));
		Assert.assertTrue("Should contain state definitions", topology.contains("<InstanceState state=\"Ready\"/>"));
	}
	
	private static void testToscaDefinitions(String topology){
	  Assert.assertTrue("Should be a tosca XML", topology.contains("<Definitions"));
	  Assert.assertTrue("Should contain a targetNamespace", topology.contains("targetNamespace="));
	  Assert.assertTrue("Should contain a NodeType element", topology.contains("<NodeType"));
    Assert.assertTrue("Should contain a Types element", topology.contains("<Types"));
	}

}
