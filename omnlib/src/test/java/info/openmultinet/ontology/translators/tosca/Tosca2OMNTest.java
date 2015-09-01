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
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Tosca2OMNTest {
  
  @Test
  public void testConvertToscaDummyRequest() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/request-dummy.xml");
    
    final Model model = Tosca2OMN.getModel(input);
    
    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);
    
    Assert.assertTrue("Should contain a topology resource", model.containsResource(Omn.Topology));
    Assert.assertTrue("Should contain the dummy node resource",
        model.contains(Osco.dummy, RDFS.subClassOf, Omn.Resource));
    Assert.assertTrue("Should contain state resources", model.containsResource(Omn_lifecycle.State));
    Assert.assertTrue("Should contain parameter resources", model.containsResource(Osco.parameter2));
    Assert.assertTrue("parameter2 should be of range string", model.contains(Osco.parameter2, RDFS.range, XSD.xstring));
    Assert.assertTrue("minNumInst should be of range int", model.contains(Osco.minNumInst, RDFS.range, XSD.xint));
    Assert.assertTrue("Should contain the service properties", model.containsResource(Osco.TEST_PARAM));
  }
  
  @Test
  public void testConvertToscaResponseTypes() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/response-types.xml");
    
    final Model model = Tosca2OMN.getModel(input);
    
    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);
    
    Assert.assertFalse("Should not contain a topology resource", model.containsResource(Omn.Topology));
    Assert.assertTrue("Should contain resources", model.containsResource(Omn.Resource));
    Assert.assertTrue("Should contain the ssh node resource", model.containsResource(Osco.ssh));
    Assert.assertTrue("Should contain key parameter resource", model.containsResource(Osco.key));
    
    Assert.assertNotNull("name parameter resource should have a domain", model.getProperty(Osco.name, RDFS.domain));
    Resource nameDomain = model.getProperty(Osco.name, RDFS.domain).getResource();
    Assert.assertNotNull("name parameter resource domain should be a union",
        nameDomain.getProperty(OWL2.disjointUnionOf));
    RDFList domainClassesList = nameDomain.getProperty(OWL2.disjointUnionOf).getResource().as(RDFList.class);
    Assert.assertTrue("name parameter resource should be of domain Subnet", domainClassesList.contains(Osco.Subnet));
    Assert.assertTrue("name parameter resource should be of domain Location", domainClassesList.contains(Osco.Location));
    Assert.assertFalse("name parameter resource should be of domain Image", domainClassesList.contains(Osco.Image));
    
    Assert.assertFalse("floatingIp parameter resource should not be of domain Location",
        model.contains(Osco.floatingIp, RDFS.domain, Osco.Location));
    Assert.assertTrue("subnet parameter resource should be of domain ServiceContainer",
        model.contains(Osco.subnet, RDFS.domain, Osco.ServiceContainer));
    Assert.assertTrue("Should contain datatype properties", model.containsResource(OWL.DatatypeProperty));
    Assert.assertTrue("Should contain object properties", model.containsResource(OWL.ObjectProperty));
    Assert.assertTrue("Should contain prefix mapping for osco",
        "http://opensdncore.org/ontology/".equals(model.getNsPrefixURI("osco")));
  }

  @Test
  public void testConvertToscaSingleNodeResponse() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/response-single-node.xml");
    
    final Model model = Tosca2OMN.getModel(input);
    
    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);
    
    Assert.assertFalse("Should not contain a topology resource", model.containsResource(Omn.Topology));
    Assert.assertTrue("Should contain the ssh node resource", model.containsResource(Osco.ssh));
    Assert.assertTrue("Should contain parameter resources", model.containsResource(Osco.key));
    Assert.assertTrue("Should contain the floating IP", serializedModel.contains("130.149.247.218"));
  }
  
  @Test
  public void testConvertOpenMTCRequest() throws JAXBException, InvalidModelException, UnsupportedException {
    InputStream input = getClass().getResourceAsStream("/tosca/request-openmtc.xml");
    
    final Model model = Tosca2OMN.getModel(input);
    
    final String serializedModel = Tosca2OMNTest.serializeModel(model, "TTL");
    System.out.println(serializedModel);
  }
  
  public static String serializeModel(final Model rdfModel, final String serialization) {
    final StringWriter writer = new StringWriter();
    rdfModel.write(writer, serialization);
    return writer.toString();
  }
}
