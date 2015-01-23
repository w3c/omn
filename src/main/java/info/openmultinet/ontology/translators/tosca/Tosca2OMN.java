package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityType.PropertiesDefinition;
import info.openmultinet.ontology.translators.tosca.jaxb.TExtensibleElements;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeType;
import info.openmultinet.ontology.translators.tosca.jaxb.TRelationshipTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates.InstanceState;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Tosca2OMN extends AbstractConverter {
  
  private static final Logger LOG = Logger.getLogger(Tosca2OMN.class.getName());
  
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
    
    processTypes(definitions, model);
    processNodeTypes(definitions, model);
    processTemplates(definitions, model);
    
    return model;
  }
  
  private static void processTypes(Definitions definitions, Model model) {
    List<Object> types = definitions.getTypes().getAny();
    for (Object schema : types) {
      if (schema instanceof Element) {
        Element schemaElement = (Element) schema;
        if(isXMLSchemaElement(schemaElement)){
          String namespace = schemaElement.getAttribute("targetNamespace");
          for(int i = 0; i < schemaElement.getChildNodes().getLength()-1; i++){
            Node elementNode = schemaElement.getChildNodes().item(i);
            String superPropertyName = elementNode.getAttributes().getNamedItem("name").getNodeValue();
            Property superProperty = model.createProperty(namespace+superPropertyName);
            for(int j = 0; j < schemaElement.getChildNodes().getLength()-1; j++){
              Node typesNode = elementNode.getChildNodes().item(j);
              if(typesNode.getLocalName().equals("complexType")){
                for(int k = 0; k < schemaElement.getChildNodes().getLength()-1; k++){
                  Node sequenceNode = typesNode.getChildNodes().item(k);
                  if(sequenceNode.getLocalName().equals("sequence")){
                    for(int l = 0; l < sequenceNode.getChildNodes().getLength()-1; l++){
                      Node typeNode = sequenceNode.getChildNodes().item(l);
                      if(typeNode.getLocalName().equals("element")){
                        String name = typeNode.getAttributes().getNamedItem("name").getNodeValue();
                        String type = typeNode.getAttributes().getNamedItem("type").getNodeValue();
                        Property property = model.createProperty(namespace+name);
                        property.addProperty(RDFS.subPropertyOf, superProperty);
                        property.addProperty(RDFS.range, getXSDType(type, model));
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
  
  private static Resource getXSDType(String type, Model model){
    switch(type){
      case "xs:string":
        return XSD.xstring;
      case "xs:integer":
        return XSD.xint;
    }
    return model.createResource(type);
  }
  
  private static boolean isXMLSchemaElement(Element element){
    return element.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema") && element.getLocalName().equals("schema");
  }

  private static void processNodeTypes(Definitions definitions, Model model) {
    for (TExtensibleElements templateOrNodeType : definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if (templateOrNodeType instanceof TNodeType) {
        createServiceType((TNodeType) templateOrNodeType, model);
        createStates((TNodeType) templateOrNodeType, model);
      }
    }
  }
  
  private static void processTemplates(Definitions definitions, Model model) {
    for (TExtensibleElements templateOrNodeType : definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if(templateOrNodeType instanceof TServiceTemplate){
        processServiceTemplate((TServiceTemplate) templateOrNodeType, definitions, model);
      }
    }
  }
  
  private static void processServiceTemplate(TServiceTemplate serviceTemplate, Definitions definitions, Model model){
    TTopologyTemplate topologyTemplate = serviceTemplate.getTopologyTemplate();
    for(TEntityTemplate entityTemplate : topologyTemplate.getNodeTemplateOrRelationshipTemplate()){
      if(entityTemplate instanceof TNodeTemplate){
        createService((TNodeTemplate) entityTemplate, definitions, model);
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
    setServiceProperties(nodeTemplate, service, model);
    return service;
  }
  
  private static void setServiceProperties(TNodeTemplate nodeTemplate, Resource service, Model model) {
    Object properties = nodeTemplate.getProperties().getAny();
    if(properties instanceof Element){
      Element propertiesElement = (Element) properties;
      for(int i = 0; i < propertiesElement.getChildNodes().getLength()-1; i++){
        Node propertyNode = propertiesElement.getChildNodes().item(i);
        String namespace = propertyNode.getNamespaceURI();
        Property property = model.getProperty(namespace+propertyNode.getLocalName());
        Resource propertyRange;
        try{
          propertyRange = property.getRequiredProperty(RDFS.range).getResource();
        } catch(PropertyNotFoundException e){
          LOG.log(Level.INFO, "No property range for property "+property.getURI()+" found, storing as string.");
          propertyRange = XSD.xstring;
        }
        Literal literal = model.createTypedLiteral(propertyNode.getTextContent(), propertyRange.getURI());
        service.addLiteral(property, literal);
      }
    }
    else{
      //TODO
    }
  }
  
  private static void setServiceType(TNodeTemplate nodeTemplate, Resource service, Model model){
    QName type = nodeTemplate.getType();
    Resource serviceType = createResourceFromQName(type, model);
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
    serviceType.addProperty(RDFS.subClassOf, Tosca.Node);
    PropertiesDefinition propertiesDefinition = nodeType.getPropertiesDefinition();
    Resource properties = createResourceFromQName(propertiesDefinition.getElement(), model);
    properties.addProperty(RDFS.domain, serviceType);
    return serviceType;
  }
  
  private static Resource createResourceFromQName(QName qname, Model model){
    return model.createResource(qname.getNamespaceURI()+qname.getLocalPart());
  }
  
}
