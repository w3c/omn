package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TExtensibleElements;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeType;
import info.openmultinet.ontology.translators.tosca.jaxb.TRelationshipTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates.InstanceState;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.io.InputStream;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Tosca2OMN extends AbstractConverter {
  
  public static Model getModel(InputStream input) throws JAXBException, InvalidModelException {
    JAXBContext context = JAXBContext.newInstance(Definitions.class);
    Unmarshaller unmarshaller = context.createUnmarshaller();
    Definitions definitions = unmarshaller.unmarshal(new StreamSource(input), Definitions.class).getValue();
    Model model = tosca2Model(definitions);
    
    //TODO: create infModel? result will be huge..
//    Parser parser = new Parser(model);
//    InfModel infModel = parser.getModel();
    
    Parser.setCommonPrefixes(model);
    return model;
  }
  
  private static Model tosca2Model(Definitions definitions){
    Model model = ModelFactory.createDefaultModel();
    
    
    processTemplatesAndNodeTypes(definitions, model);
    
    return model;
  }
  
  private static void processTemplatesAndNodeTypes(Definitions definitions, Model model) {
    for (TExtensibleElements templateOrNodeType : definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if (templateOrNodeType instanceof TNodeType) {
        Resource serviceType = createServiceType((TNodeType) templateOrNodeType, model);
        createStates((TNodeType) templateOrNodeType, model);
      }
      else if(templateOrNodeType instanceof TServiceTemplate){
        processServiceTemplate((TServiceTemplate) templateOrNodeType, definitions, model);
      }
         
    }
  }
  
  private static void processServiceTemplate(TServiceTemplate serviceTemplate, Definitions definitions, Model model){
    TTopologyTemplate topologyTemplate = serviceTemplate.getTopologyTemplate();
    for(TEntityTemplate entityTemplate : topologyTemplate.getNodeTemplateOrRelationshipTemplate()){
      if(entityTemplate instanceof TNodeTemplate){
        Resource service = createService((TNodeTemplate) entityTemplate, definitions, model);
      }
      else if(entityTemplate instanceof TRelationshipTemplate){
        //TODO process relationship templates
      }
    }
  }

  private static Resource createService(TNodeTemplate nodeTemplate, Definitions definitions, Model model) {
    String namespace = definitions.getTargetNamespace();
    Resource service = model.createResource(namespace+nodeTemplate.getName());
    setServiceType(nodeTemplate, service, model);
    return service;
  }
  
  private static void setServiceType(TNodeTemplate nodeTemplate, Resource service, Model model){
    QName type = nodeTemplate.getType();
    Resource serviceType = model.createResource(type.getNamespaceURI()+type.getLocalPart());
    service.addProperty(RDF.type, serviceType);
  }

  private static void createStates(TNodeType nodeType, Model model) {
    String namespace = nodeType.getTargetNamespace();
    for (InstanceState instanceState : nodeType.getInstanceStates().getInstanceState()){
      Resource state = model.createResource(namespace+instanceState.getState());
      state.addProperty(RDF.type, Tosca.State);
    }
  }

  private static Resource createServiceType(TNodeType nodeType, Model model) {
    String namespace = nodeType.getTargetNamespace();
    Resource serviceType = model.getResource(namespace+nodeType.getName());
    serviceType.addProperty(RDF.type, Tosca.Service);
    return serviceType;
  }
  
}
