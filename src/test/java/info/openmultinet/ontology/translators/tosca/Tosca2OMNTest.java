package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Osco;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Tosca2OMNTest {


	@Test
	public void testConvertToscaRequest() throws JAXBException, InvalidModelException, UnsupportedException {
	  InputStream input = getClass().getResourceAsStream("/tosca/request-dummy.xml");
	  
	  final Model model = Tosca2OMN.getModel(input);

		final String serializedModel = Tosca2OMNTest.serializeModel(model,
				"TTL");
		System.out.println(serializedModel);

		Assert.assertTrue("Should contain a topology resource",
        model.containsResource(Omn.Topology));
		Assert.assertTrue("Should contain the dummy node resource",
				model.contains(Osco.dummy, RDFS.subClassOf, Omn.Resource));
		Assert.assertTrue("Should contain state resources",
				model.containsResource(Omn_lifecycle.State));
		Assert.assertTrue("Should contain state resources",
				model.containsResource(Omn_lifecycle.Ready));
		Assert.assertTrue("Should contain parameter resources",
				model.containsResource(Osco.parameter2));
		Assert.assertTrue("port should be of range int",
				model.contains(Osco.port, RDFS.range, XSD.xint));
		Assert.assertTrue("parameter2 should be of range string",
				model.contains(Osco.parameter2, RDFS.range, XSD.xstring));
		Assert.assertTrue("post should be of range int",
        model.contains(Osco.port, RDFS.range, XSD.xint));
		Assert.assertTrue("Should contain the service properties",
				model.containsResource(Osco.test_param));
	}
	
	@Test
  public void testConvertToscaResponseTypes() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/response-types.xml");
    
    final Model model = Tosca2OMN.getModel(input);

    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);

    Assert.assertFalse("Should not contain a topology resource",
        model.containsResource(Omn.Topology));
    Assert.assertTrue("Should contain resources",
        model.containsResource(Omn.Resource));
    Assert.assertTrue("Should contain the ssh node resource",
        model.containsResource(Osco.ssh));
    Assert.assertTrue("Should contain key parameter resource",
        model.containsResource(Osco.key));
    Assert.assertTrue("Should contain datatype properties",
        model.containsResource(OWL.DatatypeProperty));
    Assert.assertTrue("Should contain object properties",
        model.containsResource(OWL.ObjectProperty));
  }
	
	@Test
  public void testConvertToscaSingleNodeResponse() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/response-single-node.xml");
    
    final Model model = Tosca2OMN.getModel(input);

    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);

    Assert.assertFalse("Should not contain a topology resource",
        model.containsResource(Omn.Topology));
    Assert.assertTrue("Should contain the ssh node resource",
        model.containsResource(Osco.ssh));
    Assert.assertTrue("Should contain parameter resources",
        model.containsResource(Osco.key));
    Assert.assertTrue("Should contain the floating IP",
        serializedModel.contains("130.149.247.218"));
  }

	public static String serializeModel(final Model rdfModel,
			final String serialization) {
		final StringWriter writer = new StringWriter();
		rdfModel.write(writer, serialization);
		return writer.toString();
	}
}
