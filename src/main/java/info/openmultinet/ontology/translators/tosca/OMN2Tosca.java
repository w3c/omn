package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.ObjectFactory;
import info.openmultinet.ontology.translators.tosca.jaxb.TDefinitions.Types;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate.Properties;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityType.PropertiesDefinition;
import info.openmultinet.ontology.translators.tosca.jaxb.TExtensibleElements;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeType;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates.InstanceState;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OMN2Tosca extends AbstractConverter {
  
  private static ObjectFactory objFactory = new ObjectFactory();
  
  public static String getTopology(Model model) throws JAXBException, InvalidModelException, ServiceTypeNotFoundException, RequiredResourceNotFoundException {
    Definitions definitions = objFactory.createDefinitions();
    
    model2Tosca(model, definitions);
    
    return toString(definitions, "info.openmultinet.ontology.translators.tosca.jaxb");
  }
  
  private static void model2Tosca(Model model, Definitions definitions) throws InvalidModelException, ServiceTypeNotFoundException, RequiredResourceNotFoundException {
    setTargetNamespaceAndName(model, definitions);
    
    List<TExtensibleElements> templatesAndNodeTypes = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();

    Types types = objFactory.createTDefinitionsTypes();
    definitions.setTypes(types);
    
    TServiceTemplate serviceTemplate = objFactory.createTServiceTemplate();
    templatesAndNodeTypes.add(serviceTemplate);
    serviceTemplate.setId(getTopologyResource(model).getURI());
    
    TTopologyTemplate topologyTemplate = objFactory.createTTopologyTemplate();
    serviceTemplate.setTopologyTemplate(topologyTemplate);
    
    List<TEntityTemplate> nodeTemplates = topologyTemplate.getNodeTemplateOrRelationshipTemplate();
    ResIterator serviceIterator = model.listResourcesWithProperty(RDF.type, Tosca.Service);
    while(serviceIterator.hasNext()){
      Resource service = serviceIterator.next();
      Resource serviceType = getServiceType(service);
      
      Element doc = createTypes(service, serviceType);
      types.getAny().add(doc);
      
      TNodeType nodeType = createNodeType(serviceType);
      templatesAndNodeTypes.add(nodeType);
      
      TNodeTemplate nodeTemplate = createNodeTemplate(service, serviceType);
      nodeTemplates.add(nodeTemplate);
    }
  }
  
  private static void setTargetNamespaceAndName(Model model, Definitions definitions) throws RequiredResourceNotFoundException{
    String targetNamespace = getXMLNamespace(getTopologyResource(model));
    definitions.setTargetNamespace(targetNamespace);
    definitions.setName(targetNamespace);
  }
  
  private static String getXMLNamespace(Resource resource){
    return resource.getNameSpace().replace("#", "");
  }
  
  private static String getNSPrefix(Resource resource){
    Map<String, String> prefixMap = resource.getModel().getNsPrefixMap();
    for(Map.Entry<String, String> mapping : prefixMap.entrySet()){
      if(mapping.getValue().equals(resource.getNameSpace())){
        return mapping.getKey();
      }
    }
    //TODO:
    return "";
  }
  
  private static Resource getTopologyResource(Model model) throws RequiredResourceNotFoundException{
    ResIterator iter = model.listResourcesWithProperty(RDF.type, Omn.Topology);
    if(iter.hasNext()){
      return iter.next();
    }
    if(iter.hasNext()){
      //TODO: allow multiple topologies in 1 request?
    }
    throw new RequiredResourceNotFoundException("No Resource of type "+Omn.Topology.getURI()+" could be found");
  }
  
  private static Element createTypes(Resource service, Resource serviceType) throws ServiceTypeNotFoundException{
    Document types = createDocument();
    
    Element schema = types.createElement("xs:schema");
    schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
    schema.setAttribute("elementFormDefault", "qualified");
    schema.setAttribute("attributeFormDefault", "unqualified");
    schema.setAttribute("targetNamespace", getXMLNamespace(serviceType));
    types.appendChild(schema);
    
    Element element = types.createElement("xs:element");
    schema.appendChild(element);
    element.setAttribute("name", getServiceTypePropertiesName(serviceType));
    
    Element complexType = types.createElement("xs:complexType");
    element.appendChild(complexType);
    
    Element sequence = types.createElement("xs:sequence");
    complexType.appendChild(sequence);
    
    StmtIterator propertiesIterator = serviceType.getModel().listStatements(null, RDFS.domain, serviceType);
    while(propertiesIterator.hasNext()){
      Resource property = propertiesIterator.next().getSubject();
      if(property.hasProperty(RDFS.subPropertyOf, Tosca.ServiceProperty)){
        Element type = createType(types, property);
        sequence.appendChild(type);
      }
    }
    
    return types.getDocumentElement();
  }

  private static Element createType(Document types, Resource property) {
    Element type = types.createElement("xs:element");
    type.setAttribute("name", property.getLocalName());
    Resource range = property.getRequiredProperty(RDFS.range).getResource();
    
    if(range.getNameSpace().equals("http://www.w3.org/2001/XMLSchema#")){
      type.setAttribute("type", "xs:"+range.getLocalName());
    }
    else{
      type.setAttribute("type", range.getURI());
    }
    return type;
  }
  
  private static Document createDocument(){
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      e.printStackTrace();
    }
    return docBuilder.newDocument();
  }
  
  private static TNodeType createNodeType(Resource serviceType){
    TNodeType nodeType = objFactory.createTNodeType();
    setName(serviceType, nodeType);
    setNodeTypeProperties(serviceType, nodeType);
    setInstanceStates(serviceType, nodeType);
    return nodeType;
  }

  private static void setName(Resource serviceType, TNodeType nodeType) {
    nodeType.setName(serviceType.getLocalName());
    nodeType.setTargetNamespace(getXMLNamespace(serviceType));
  }
  
  private static void setNodeTypeProperties(Resource serviceType, TNodeType nodeType){
    PropertiesDefinition nodeTypeProperties = objFactory.createTEntityTypePropertiesDefinition();
    String serviceTypeNameSpace = getXMLNamespace(serviceType);
    String serviceTypePrefix = getNSPrefix(serviceType);
    QName propertiesReference = new QName(serviceTypeNameSpace, getServiceTypePropertiesName(serviceType), serviceTypePrefix);
    nodeTypeProperties.setElement(propertiesReference);
    nodeType.setPropertiesDefinition(nodeTypeProperties);
  }
  
  private static void setInstanceStates(Resource serviceType, TNodeType nodeType) {
    TTopologyElementInstanceStates instanceStates = objFactory.createTTopologyElementInstanceStates();
    
    StmtIterator stateIterator = serviceType.getModel().listStatements(null, RDFS.subClassOf, Tosca.State);
    while(stateIterator.hasNext()){
      Resource state = stateIterator.next().getSubject();
      if(!state.equals(Tosca.State)){
        InstanceState instanceState = objFactory.createTTopologyElementInstanceStatesInstanceState();
        instanceState.setState(state.getLocalName());
        instanceStates.getInstanceState().add(instanceState);
      }
    }
    nodeType.setInstanceStates(instanceStates);
  }
  
  private static String getServiceTypePropertiesName(Resource serviceType){
    return serviceType.getLocalName()+"Properties";
  }
  
  private static TNodeTemplate createNodeTemplate(Resource service, Resource serviceType) throws ServiceTypeNotFoundException{
    TNodeTemplate nodeTemplate = objFactory.createTNodeTemplate();
    setNameAndTypeAndID(service, serviceType, nodeTemplate);
    setServiceProperties(service, serviceType, nodeTemplate);
    return nodeTemplate;
  }
  
  private static void setNameAndTypeAndID(Resource service, Resource serviceType, TNodeTemplate nodeTemplate) {
    nodeTemplate.setName(service.getLocalName());
    String serviceTypeNameSpace = getXMLNamespace(serviceType);
    String serviceTypePrefix = getNSPrefix(serviceType);
    QName type = new QName(serviceTypeNameSpace, serviceType.getLocalName(), serviceTypePrefix);
    nodeTemplate.setId(service.getURI());
    nodeTemplate.setType(type);
  }
  
  private static void setServiceProperties(Resource service, Resource serviceType, TNodeTemplate nodeTemplate) throws ServiceTypeNotFoundException {
    Document doc = createDocument();
    
    String serviceTypeNamespace = getXMLNamespace(serviceType);
    String serviceTypePrefix = getNSPrefix(serviceType);
    Element serviceProperties = doc.createElementNS(serviceTypeNamespace, serviceTypePrefix+":"+getServiceTypePropertiesName(serviceType));
    doc.appendChild(serviceProperties);
    
    StmtIterator propertiesIterator = service.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      if(propertyStatement.getPredicate().hasProperty(RDFS.subPropertyOf, Tosca.ServiceProperty) && !propertyStatement.getPredicate().equals(Tosca.ServiceProperty)){
        //TODO: check if parameter is in serviceType
        Element parameter = doc.createElementNS(serviceTypeNamespace, serviceTypePrefix+":"+propertyStatement.getPredicate().getLocalName());
        parameter.setTextContent(propertyStatement.getLiteral().getString());
        serviceProperties.appendChild(parameter);
      }
    }
    Properties properties = objFactory.createTEntityTemplateProperties();
    properties.setAny(doc.getDocumentElement());
    nodeTemplate.setProperties(properties);
  }
  
  private static Resource getServiceType(Resource service) throws ServiceTypeNotFoundException {
    StmtIterator propertiesIterator = service.listProperties(RDF.type);
    while (propertiesIterator.hasNext()) {
      Resource serviceType = propertiesIterator.next().getResource();
      if (serviceType.hasProperty(RDFS.subClassOf, Tosca.Service)
          && !serviceType.equals(Tosca.Service)) {
        return serviceType;
      }
    }
    throw new ServiceTypeNotFoundException("no service type found for: "+service.getURI());
  }
  
  public static class ServiceTypeNotFoundException extends Exception{

    private static final long serialVersionUID = -6079715571448444400L;
    
    public ServiceTypeNotFoundException(String message){
      super(message);
    }
  }
  
  public static class RequiredResourceNotFoundException extends Exception{

    private static final long serialVersionUID = -6296855743962011943L;

    public RequiredResourceNotFoundException(String message){
      super(message);
    }
  }
  
}
