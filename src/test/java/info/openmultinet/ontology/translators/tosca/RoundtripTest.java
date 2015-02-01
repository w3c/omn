package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Statement;

public class RoundtripTest {
  
  @Test
  public void testOMN2Tosca2OMN() throws InvalidModelException, JAXBException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException, UnsupportedException {
    InputStream input = this.getClass().getResourceAsStream("/omn/tosca-request-dummy.ttl");
    Parser parser = new Parser(input);
    
    final InfModel model = parser.getInfModel();
    final String toscaDefinitions = OMN2Tosca.getTopology(model);
    
    InputStream topologyStream = new ByteArrayInputStream(toscaDefinitions.getBytes());
    Model resultModel = Tosca2OMN.getModel(topologyStream);
    
    for(Statement st : parser.getModel().listStatements().toList()){
      Assert.assertTrue("Model should contain statement "+st, resultModel.contains(st));
    }
  }
  
  @Test
  public void testTosca2OMN2Tosca() throws JAXBException, InvalidModelException, UnsupportedException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException{
    InputStream input = this.getClass().getResourceAsStream("/tosca/tosca-request-ims.xml");
    final Model model = Tosca2OMN.getModel(input);
    
    final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    RDFDataMgr.write(baos, model, Lang.TTL);
    String serializedModel = baos.toString();
    
    System.out.println(serializedModel);
    
    InputStream modelStream = new ByteArrayInputStream(serializedModel.getBytes());
    Parser parser = new Parser(modelStream);
    InfModel infModel = parser.getInfModel();
    String toscaDefinitions = OMN2Tosca.getTopology(infModel);
    System.out.println(toscaDefinitions);
  }
  
}
