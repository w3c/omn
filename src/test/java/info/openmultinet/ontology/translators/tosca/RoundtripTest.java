package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

public class RoundtripTest {
  
  @Test
  public void test() throws InvalidModelException, JAXBException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException, UnsupportedException {
    InputStream input = this.getClass().getResourceAsStream("/tosca-request.ttl");
    Parser parser = new Parser(input);
    
    final InfModel model = parser.getInfModel();
    final String topology = OMN2Tosca.getTopology(model);
    
    InputStream topologyStream = new ByteArrayInputStream(topology.getBytes());
    Model resultModel = Tosca2OMN.getModel(topologyStream);
    
    final String serializedModel = Tosca2OMNTest.serializeModel(resultModel,"TTL");
    System.out.println(serializedModel);
    
    for(Statement st : parser.getModel().listStatements().toList()){
      Assert.assertTrue("Model should contain statement "+st, resultModel.contains(st));
    }
  }
  
}
