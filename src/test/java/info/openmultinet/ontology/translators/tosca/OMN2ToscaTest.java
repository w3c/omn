package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.NodeTypeNotFoundException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;

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
	public void testGetTopology() throws JAXBException, InvalidModelException, NodeTypeNotFoundException, MultipleNamespacesException, RequiredResourceNotFoundException {
		InfModel model = this.parser.getModel();
		String topology = OMN2Tosca.getTopology(model);
		System.out.println(topology);
		
		testGeneralToscaDefinitions(topology);
	  testTypes(topology);
		testNodeTypes(topology);
		testNodeTemplates(topology);
		testRelationshipTemplates(topology);
	}
	
	private static void testGeneralToscaDefinitions(String topology){
	  Assert.assertTrue("Should be a tosca XML", topology.contains("<Definitions"));
	  Assert.assertTrue("Should contain a targetNamespace", topology.contains("targetNamespace="));
	  Assert.assertTrue("Should contain a NodeType element", topology.contains("<NodeType"));
    Assert.assertTrue("Should contain a Types element", topology.contains("<Types"));
	}
	
	private static void testTypes(String topology){
	  Assert.assertTrue("Should contain type definitions for parameters", topology.contains("<xs:element name=\"port\" type=\"xs:integer\"/>"));
	}
	
	private static void testNodeTypes(String topology){
	  Assert.assertTrue("Should contain a ServiceContainer NodeType element", topology.contains("<NodeType name=\"ServiceContainer\""));
	  Assert.assertTrue("Should contain a dummy NodeType element", topology.contains("<NodeType name=\"dummy\""));
	  Assert.assertTrue("Should contain property definitions", topology.contains("<PropertiesDefinition element=\"osco:dummyProperties\" xmlns:osco=\"http://opensdncore.org/ontology/\"/>"));
	  Assert.assertTrue("Should contain state definitions", topology.contains("<InstanceState state=\"Ready\"/>"));
	}
	
	private static void testNodeTemplates(String topology){
	  Assert.assertTrue("Should contain a container NodeTemplate element", topology.contains("<NodeTemplate name=\"container1\""));
	  Assert.assertTrue("Should contain the properties set", topology.contains("<osco:parameter2>bar</osco:parameter2>"));
    Assert.assertTrue("Should contain the properties set", topology.contains("<osco:port>8088</osco:port>"));
    Assert.assertTrue("Should contain the properties set", topology.contains("<osco:test_param>foo</osco:test_param>"));
	}
	
	private static void testRelationshipTemplates(String topology){
	  Assert.assertTrue("Should contain a relationship template element", topology.contains("<RelationshipTemplate"));
	  Assert.assertTrue("relationship template should have the right name", topology.contains("<RelationshipTemplate name=\"deployedOnContainer1\""));
	  Assert.assertTrue("relationship template should have the right source", topology.contains("<SourceElement ref=\"http://open-multinet.info/ontology/examples#dummy1\"/>"));
	  Assert.assertTrue("relationship template should have the right target", topology.contains("<TargetElement ref=\"http://open-multinet.info/ontology/examples#container1\"/>"));
	}

}
