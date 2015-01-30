package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;

public class OMN2ToscaTest {

	@Test
	public void testGetTopology() throws JAXBException, InvalidModelException, MultipleNamespacesException,
			RequiredResourceNotFoundException, MultiplePropertyValuesException {
	  InputStream input = getClass().getResourceAsStream("/omn/tosca-request.ttl");
	  Parser parser = new Parser(input);
	  
		final InfModel model = parser.getInfModel();
		final String topology = OMN2Tosca.getTopology(model);
		System.out.println(topology);

		testGeneralToscaDefinitions(topology);
		testTypes(topology);
		testNodeTypes(topology);
		testNodeTemplates(topology);
		testRelationshipTemplates(topology);
		testRelationshipTypes(topology);
	}

	private static void testGeneralToscaDefinitions(final String topology) {
		Assert.assertTrue("Should be a tosca XML",
				topology.contains("<Definitions"));
		Assert.assertTrue("Should contain a targetNamespace",
				topology.contains("targetNamespace="));
		Assert.assertTrue("Should contain a NodeType element",
				topology.contains("<NodeType"));
		Assert.assertTrue("Should contain a Types element",
				topology.contains("<Types"));
	}

	private static void testTypes(final String topology) {
		Assert.assertTrue(
				"Should contain type definitions for parameters",
				topology.contains("<xs:element name=\"port\" type=\"xs:integer\"/>"));
	}

	private static void testNodeTypes(final String topology) {
		Assert.assertTrue("Should contain a ServiceContainer NodeType element",
				topology.contains("<NodeType name=\"ServiceContainer\""));
		Assert.assertTrue("Should contain a dummy NodeType element",
				topology.contains("<NodeType name=\"dummy\""));
		Assert.assertTrue(
				"Should contain property definitions",
				topology.contains("<PropertiesDefinition element=\"osco:dummyProperties\" xmlns:osco=\"http://opensdncore.org/ontology/\"/>"));
		Assert.assertTrue("Should contain state definitions",
				topology.contains("<InstanceState state=\"http://open-multinet.info/ontology/omn-lifecycle#Active\"/>"));
	}

	private static void testNodeTemplates(final String topology) {
		Assert.assertTrue("Should contain a container NodeTemplate element",
				topology.contains("<NodeTemplate name=\"container1\""));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:parameter2>bar</osco:parameter2>"));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:port>8088</osco:port>"));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:test_param>foo</osco:test_param>"));
	}

	private static void testRelationshipTemplates(final String topology) {
		Assert.assertTrue("Should contain a relationship template element",
				topology.contains("<RelationshipTemplate"));
		Assert.assertTrue(
				"relationship template should have the right name",
				topology.contains("<RelationshipTemplate name=\"deployedOnContainer1\""));
		Assert.assertTrue(
				"relationship template should have the right source",
				topology.contains("<SourceElement ref=\"http://open-multinet.info/ontology/examples/dummy1\"/>"));
		Assert.assertTrue(
				"relationship template should have the right target",
				topology.contains("<TargetElement ref=\"http://open-multinet.info/ontology/examples/container1\"/>"));
	}

	private static void testRelationshipTypes(final String topology) {
		Assert.assertTrue("Should contain a relationship type element",
				topology.contains("<RelationshipType"));
		Assert.assertTrue("relationship type should have the right name",
				topology.contains("<RelationshipType name=\"deployedOn\""));
	}

}
