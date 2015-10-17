package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;

import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;

public class OMN2ToscaTest {

  static ArrayList<String> additionalOntologies;
  
  @BeforeClass
  public static void createAdditionalOntologiesList(){
    additionalOntologies = new ArrayList<String>();
    additionalOntologies.add("/ontologies/osco.ttl");
  }
  
	@Test
	//fixme: this test is slow!
	public void testConvertDummyTopology() throws JAXBException, InvalidModelException, MultipleNamespacesException,
			RequiredResourceNotFoundException, MultiplePropertyValuesException {
	  InputStream input = getClass().getResourceAsStream("/omn/tosca-request-dummy.ttl");
	  
	  Parser parser = new Parser(input, additionalOntologies);
	  
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
	
	@Test
	//fixme: this test is slow!
  public void testConvertOpenMTCTopology() throws JAXBException, InvalidModelException, MultipleNamespacesException,
      RequiredResourceNotFoundException, MultiplePropertyValuesException {
    InputStream input = getClass().getResourceAsStream("/omn/tosca-request-openmtc.ttl");
    Parser parser = new Parser(input, additionalOntologies);
    
    final InfModel model = parser.getInfModel();
    final String topology = OMN2Tosca.getTopology(model);
    System.out.println(topology);

    testGeneralToscaDefinitions(topology);
    String containerTypes = "<element name=\"ServiceContainerProperties\">";
    // String containerTypes = "<xs:element name=\"ServiceContainerProperties\">";
    Assert.assertTrue("Should contain the container node properties type", topology.contains(containerTypes));
    Assert.assertTrue("Should not contain twice the same types", topology.lastIndexOf(containerTypes) == topology.indexOf(containerTypes));
    
    String containerNodeType = "<NodeType name=\"ServiceContainer\" targetNamespace=\"http://opensdncore.org/ontology/\"";
    Assert.assertTrue("Should contain the container node type", topology.contains(containerNodeType));
    Assert.assertTrue("Should not contain twice the same node type", topology.lastIndexOf(containerNodeType) == topology.indexOf(containerNodeType));
    
    String relationshipType = "<RelationshipType name=\"deployedOn\" targetNamespace=\"http://opensdncore.org/ontology/\">";
    Assert.assertTrue("Should contain the deployedOn relationship type", topology.contains(relationshipType));
    Assert.assertTrue("Should not contain twice the same relationship type", topology.lastIndexOf(relationshipType) == topology.indexOf(relationshipType));
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
				// topology.contains("<xs:element name=\"parameter2\" type=\"xs:string\"/>"));
				topology.contains("<element name=\"parameter2\" type=\"xs:string\"/>"));
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
				topology.contains("<InstanceState state=\""));
	}

	private static void testNodeTemplates(final String topology) {
		Assert.assertTrue("Should contain a container NodeTemplate element",
				topology.contains("<NodeTemplate name=\"container1\""));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:parameter2>bar</osco:parameter2>"));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:PORT>8088</osco:PORT>"));
		Assert.assertTrue("Should contain the properties set",
				topology.contains("<osco:TEST_PARAM>foo</osco:TEST_PARAM>"));
	}

	private static void testRelationshipTemplates(final String topology) {
		Assert.assertTrue("Should contain a relationship template element",
				topology.contains("<RelationshipTemplate"));
		Assert.assertTrue(
				"relationship template should have the right name",
				topology.contains("<RelationshipTemplate name=\"deployedOn\""));
		Assert.assertTrue(
				"relationship template should have the right source",
				topology.contains("<SourceElement ref=\""));
		Assert.assertTrue(
				"relationship template should have the right target",
				topology.contains("<TargetElement ref=\""));
	}

	private static void testRelationshipTypes(final String topology) {
		Assert.assertTrue("Should contain a relationship type element",
				topology.contains("<RelationshipType"));
		Assert.assertTrue("relationship type should have the right name",
				topology.contains("<RelationshipType name=\"deployedOn\""));
	}

}
