package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.ObjectFactory;
import info.openmultinet.ontology.translators.tosca.jaxb.TDefinitions;
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
import java.util.logging.Level;
import java.util.logging.Logger;

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
  
  private static final Logger LOG = Logger.getLogger(OMN2Tosca.class.getName());
  
  private static ObjectFactory objFactory = new ObjectFactory();
  
  public static String getTopology(Model model) throws JAXBException, InvalidModelException, NodeTypeNotFoundException, RequiredResourceNotFoundException {
    Definitions definitions = objFactory.createDefinitions();
    
    model2Tosca(model, definitions);
    
    return toString(definitions, "info.openmultinet.ontology.translators.tosca.jaxb");
  }
  
  private static void model2Tosca(Model model, Definitions definitions) throws InvalidModelException, NodeTypeNotFoundException, RequiredResourceNotFoundException {
    setTargetNamespaceAndName(model, definitions);
    createNodeTypesNodeTemplatesAndTypes(definitions, model);
    createRelationshipTypes(definitions, model);
  }
  
  private static void createNodeTypesNodeTemplatesAndTypes(TDefinitions definitions, Model model) throws RequiredResourceNotFoundException, NodeTypeNotFoundException{
    List<TExtensibleElements> templatesAndNodeTypes = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
    
    Types types = objFactory.createTDefinitionsTypes();
    definitions.setTypes(types);
    
    TServiceTemplate serviceTemplate = objFactory.createTServiceTemplate();
    templatesAndNodeTypes.add(serviceTemplate);
    serviceTemplate.setId(getTopologyResource(model).getURI());
    
    TTopologyTemplate topologyTemplate = objFactory.createTTopologyTemplate();
    serviceTemplate.setTopologyTemplate(topologyTemplate);
    
    List<TEntityTemplate> nodeTemplates= topologyTemplate.getNodeTemplateOrRelationshipTemplate();
    ResIterator nodeIterator = model.listResourcesWithProperty(RDF.type, Tosca.Node);
    while(nodeIterator.hasNext()){
      Resource nodeResource = nodeIterator.next();
      Resource nodeTypeResource = getNodeType(nodeResource);
      
      try {
        Element doc = createTypes(nodeTypeResource);
        types.getAny().add(doc);
      } catch (NoPropertiesFoundException e) {
        LOG.log(Level.INFO, "No properties found for node type "+nodeTypeResource.getURI());
      }
      
      TNodeType nodeType = createNodeType(nodeTypeResource);
      templatesAndNodeTypes.add(nodeType);
      
      TNodeTemplate nodeTemplate = createNodeTemplate(nodeResource, nodeTypeResource);
      nodeTemplates.add(nodeTemplate);
    }
  }
  
  private static void setTargetNamespaceAndName(Model model, Definitions definitions) throws RequiredResourceNotFoundException{
    String targetNamespace = getXMLNamespace(getTopologyResource(model));
    definitions.setTargetNamespace(targetNamespace);
    definitions.setName(targetNamespace);
  }
  
  private static String getXMLNamespace(Resource resource){
    return resource.getNameSpace().replace("#", "/");
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
  
  private static Element createTypes(Resource nodeType) throws NodeTypeNotFoundException, NoPropertiesFoundException{
    Document types = createDocument();
    
    Element schema = types.createElement("xs:schema");
    schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
    schema.setAttribute("elementFormDefault", "qualified");
    schema.setAttribute("attributeFormDefault", "unqualified");
    schema.setAttribute("targetNamespace", getXMLNamespace(nodeType));
    types.appendChild(schema);
    
    Element element = types.createElement("xs:element");
    schema.appendChild(element);
    element.setAttribute("name", getNodeTypePropertiesName(nodeType));
    
    Element complexType = types.createElement("xs:complexType");
    element.appendChild(complexType);
    
    Element sequence = types.createElement("xs:sequence");
    complexType.appendChild(sequence);
    
    StmtIterator propertiesIterator = nodeType.getModel().listStatements(null, RDFS.domain, nodeType);
    if(!propertiesIterator.hasNext()){
      throw new NoPropertiesFoundException();
    }
    while(propertiesIterator.hasNext()){
      Resource property = propertiesIterator.next().getSubject();
      Element type = createType(types, property);
      sequence.appendChild(type);
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
  
  private static TNodeType createNodeType(Resource nodeTypeResource){
    TNodeType nodeType = objFactory.createTNodeType();
    setName(nodeTypeResource, nodeType);
    setNodeTypeProperties(nodeTypeResource, nodeType);
    setInstanceStates(nodeTypeResource, nodeType);
    return nodeType;
  }

  private static void setName(Resource nodeTypeResource, TNodeType nodeType) {
    nodeType.setName(nodeTypeResource.getLocalName());
    nodeType.setTargetNamespace(getXMLNamespace(nodeTypeResource));
  }
  
  private static void setNodeTypeProperties(Resource nodeTypeResource, TNodeType nodeType){
    PropertiesDefinition nodeTypeProperties = objFactory.createTEntityTypePropertiesDefinition();
    String nodeTypeNameSpace = getXMLNamespace(nodeTypeResource);
    String nodeTypePrefix = getNSPrefix(nodeTypeResource);
    QName propertiesReference = new QName(nodeTypeNameSpace, getNodeTypePropertiesName(nodeTypeResource), nodeTypePrefix);
    nodeTypeProperties.setElement(propertiesReference);
    nodeType.setPropertiesDefinition(nodeTypeProperties);
  }
  
  private static void setInstanceStates(Resource nodeTypeResource, TNodeType nodeType) {
    TTopologyElementInstanceStates instanceStates = objFactory.createTTopologyElementInstanceStates();
    
    StmtIterator stateIterator = nodeTypeResource.getModel().listStatements(null, RDFS.subClassOf, Tosca.State);
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
  
  private static String getNodeTypePropertiesName(Resource nodeTypeResource){
    return nodeTypeResource.getLocalName()+"Properties";
  }
  
  private static TNodeTemplate createNodeTemplate(Resource node, Resource nodeTypeResource) throws NodeTypeNotFoundException{
    TNodeTemplate nodeTemplate = objFactory.createTNodeTemplate();
    setNameAndTypeAndID(node, nodeTypeResource, nodeTemplate);
    
    try {
      Properties properties = objFactory.createTEntityTemplateProperties();
      Element nodeProperties = createNodeProperties(node, nodeTypeResource, nodeTemplate);
      properties.setAny(nodeProperties);
      nodeTemplate.setProperties(properties);
    } catch (NoPropertiesFoundException e) {
      LOG.log(Level.INFO, "No properties found for node "+node.getURI());
    }
    
    return nodeTemplate;
  }
  
  private static void setNameAndTypeAndID(Resource node, Resource nodeTypeResource, TNodeTemplate nodeTemplate) {
    nodeTemplate.setName(node.getLocalName());
    String nodeTypeNameSpace = getXMLNamespace(nodeTypeResource);
    String nodeTypePrefix = getNSPrefix(nodeTypeResource);
    QName type = new QName(nodeTypeNameSpace, nodeTypeResource.getLocalName(), nodeTypePrefix);
    nodeTemplate.setId(node.getURI());
    nodeTemplate.setType(type);
  }
  
  private static Element createNodeProperties(Resource node, Resource nodeTypeResource, TNodeTemplate nodeTemplate) throws NodeTypeNotFoundException, NoPropertiesFoundException {
    Document doc = createDocument();
    
    String nodeTypeNamespace = getXMLNamespace(nodeTypeResource);
    String nodeTypePrefix = getNSPrefix(nodeTypeResource);
    Element nodeProperties = doc.createElementNS(nodeTypeNamespace, nodeTypePrefix+":"+getNodeTypePropertiesName(nodeTypeResource));
    doc.appendChild(nodeProperties);
    
    StmtIterator propertiesIterator = node.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      if(propertyStatement.getPredicate().hasProperty(RDFS.domain, nodeTypeResource)) {
        Element parameter = doc.createElementNS(nodeTypeNamespace, nodeTypePrefix+":"+propertyStatement.getPredicate().getLocalName());
        parameter.setTextContent(propertyStatement.getLiteral().getString());
        nodeProperties.appendChild(parameter);
      }
    }
    if(0 == nodeProperties.getChildNodes().getLength()){
      throw new NoPropertiesFoundException();
    }
    return doc.getDocumentElement();
  }
  
  private static Resource getNodeType(Resource node) throws NodeTypeNotFoundException {
    StmtIterator propertiesIterator = node.listProperties(RDF.type);
    while (propertiesIterator.hasNext()) {
      Resource nodeTypeResource = propertiesIterator.next().getResource();
      if (nodeTypeResource.hasProperty(RDFS.subClassOf, Tosca.Node)
          && !nodeTypeResource.equals(Tosca.Node)) {
        return nodeTypeResource;
      }
    }
    throw new NodeTypeNotFoundException("no node type found for: "+node.getURI());
  }
  
 private static void createRelationshipTypes(Definitions definitions, Model model) {
   ResIterator relationIterator = model.listResourcesWithProperty(RDFS.subPropertyOf, Tosca.dependsOn);
   while(relationIterator.hasNext()){
     Resource relation = relationIterator.next();
     System.out.println(relation);
   }
  }

  
  public static class NodeTypeNotFoundException extends Exception{

    private static final long serialVersionUID = -6079715571448444400L;
    
    public NodeTypeNotFoundException(String message){
      super(message);
    }
  }
  
  public static class RequiredResourceNotFoundException extends Exception{

    private static final long serialVersionUID = -6296855743962011943L;

    public RequiredResourceNotFoundException(String message){
      super(message);
    }
  }
  
  public static class NoPropertiesFoundException extends Exception{

    private static final long serialVersionUID = -4379252875775867346L;

    public NoPropertiesFoundException(){
      super();
    }
  }
  
}
