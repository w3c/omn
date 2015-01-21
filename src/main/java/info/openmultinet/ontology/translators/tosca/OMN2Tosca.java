package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.ObjectFactory;
import info.openmultinet.ontology.translators.tosca.jaxb.Parameter;
import info.openmultinet.ontology.translators.tosca.jaxb.Parameters;
import info.openmultinet.ontology.translators.tosca.jaxb.ServiceProperties;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate.Properties;
import info.openmultinet.ontology.translators.tosca.jaxb.TExtensibleElements;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OMN2Tosca extends AbstractConverter {
  
  public static String getTopology(Model model) throws JAXBException, InvalidModelException {
    Definitions definitions = new ObjectFactory().createDefinitions();
    
    model2Tosca(model, definitions);
    
    return toString(definitions, "info.openmultinet.ontology.translators.tosca.jaxb");
  }
  
  private static void model2Tosca(Model model, Definitions definitions) throws InvalidModelException {
    List<TExtensibleElements> templatesAndTypes = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
    
    TServiceTemplate serviceTemplate = new ObjectFactory().createTServiceTemplate();
    templatesAndTypes.add(serviceTemplate);
    
    TTopologyTemplate topology = new ObjectFactory().createTTopologyTemplate();
    serviceTemplate.setTopologyTemplate(topology);
    
    List<TEntityTemplate> nodeTemplates = topology.getNodeTemplateOrRelationshipTemplate();
    ResIterator serviceIterator = model.listResourcesWithProperty(RDF.type, Tosca.Service);
    while(serviceIterator.hasNext()){
      Resource service = serviceIterator.next();
      
      TNodeTemplate nodeTemplate = createNodeTemplate(service);
      
      nodeTemplates.add(nodeTemplate);
    }
  }
  
  private static TNodeTemplate createNodeTemplate(Resource service){
    TNodeTemplate nodeTemplate = new ObjectFactory().createTNodeTemplate();
    setNameAndType(service, nodeTemplate);
    setServiceProperties(service, nodeTemplate);
    return nodeTemplate;
  }
  
  private static void setNameAndType(Resource service, TNodeTemplate nodeTemplate) {
    nodeTemplate.setName(service.getRequiredProperty(Tosca.name).getString());
    QName type = new QName(service.getRequiredProperty(Tosca.type).getString());
    nodeTemplate.setType(type);
  }
  
  private static void setServiceProperties(Resource service, TNodeTemplate nodeTemplate) {
    Parameters parameters = new Parameters();
    StmtIterator propertiesIterator = service.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      if(propertyStatement.getPredicate().hasProperty(RDFS.subPropertyOf, Tosca.ServiceProperty)){
        Parameter parameter = new Parameter();
        parameter.setKey(propertyStatement.getPredicate().getLocalName());
        parameter.setValue(propertyStatement.getLiteral().getString());
        parameters.getParameter().add(parameter);
      }
    }
    ServiceProperties serviceProperties = new ServiceProperties();
    serviceProperties.setParameters(parameters);
    Properties p = new Properties();
    p.setAny(serviceProperties);
    nodeTemplate.setProperties(p);
  }
  
  
}
