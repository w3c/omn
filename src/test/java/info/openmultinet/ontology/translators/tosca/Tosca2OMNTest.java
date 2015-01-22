package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Osco;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.io.InputStream;
import java.io.StringWriter;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class Tosca2OMNTest {
  
  private InputStream input;
  
  @Before
  public void setup() throws InvalidModelException {
    input = getClass().getResourceAsStream("/tosca-request.xml");
  }
  
  @Test
  public void testGetTopology() throws JAXBException, InvalidModelException {
    Model model = Tosca2OMN.getModel(input);
    StmtIterator iter = model.listStatements();
    
    String serializedModel = serializeModel(model, "TTL");
    System.out.println(serializedModel);
    
    Assert.assertTrue("Should contain a service resource", model.containsResource(Tosca.Service));
    Assert.assertTrue("Should contain state resources", model.containsResource(Tosca.State));
    Assert.assertTrue("Should contain the service type", model.containsResource(Osco.dummy));
  }
  
  
  public static String serializeModel(Model rdfModel, String serialization) {
    StringWriter writer = new StringWriter();
    rdfModel.write(writer, serialization);
    return writer.toString();
  }
}
