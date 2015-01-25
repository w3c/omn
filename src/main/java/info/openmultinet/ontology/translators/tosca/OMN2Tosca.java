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
import info.openmultinet.ontology.translators.tosca.jaxb.TRelationshipTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TRelationshipType;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates.InstanceState;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.util.ArrayList;
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
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OMN2Tosca extends AbstractConverter {
  
  private static final Logger LOG = Logger.getLogger(OMN2Tosca.class.getName());
  
  private static ObjectFactory objFactory = new ObjectFactory();
  
  public static String getTopology(Model model) throws JAXBException, InvalidModelException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Definitions definitions = objFactory.createDefinitions();
    
    model2Tosca(model, definitions);
    
    return toString(definitions, "info.openmultinet.ontology.translators.tosca.jaxb");
  }
  
  private static void model2Tosca(Model model, Definitions definitions) throws InvalidModelException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
    setTargetNamespaceAndName(definitions, model);
    createServiceTemplates(definitions, model);
  }
  
  private static void createServiceTemplates(TDefinitions definitions, Model model) throws MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException{
    List<TExtensibleElements> definitionsContent = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();

    Types definitionsTypes = objFactory.createTDefinitionsTypes();
    definitions.setTypes(definitionsTypes);
    List<Object> types = definitionsTypes.getAny();
    
    ResIterator topologyIterator = model.listResourcesWithProperty(RDF.type, Omn.Topology);
    while(topologyIterator.hasNext()){
      Resource topologyResource = topologyIterator.next();
      createServiceTemplate(model, definitionsContent, types, topologyResource);
    }
  }

  private static void createServiceTemplate(Model model, List<TExtensibleElements> definitionsContent, List<Object> types, Resource topologyResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    TServiceTemplate serviceTemplate = objFactory.createTServiceTemplate();
    definitionsContent.add(serviceTemplate);
    serviceTemplate.setId(topologyResource.getURI());
    
    TTopologyTemplate topologyTemplate = objFactory.createTTopologyTemplate();
    serviceTemplate.setTopologyTemplate(topologyTemplate);
    
    List<TEntityTemplate> nodesAndRelationshipTemplates = topologyTemplate.getNodeTemplateOrRelationshipTemplate();
    
    ResIterator nodeIterator = model.listResourcesWithProperty(RDF.type, Tosca.Node);
    while(nodeIterator.hasNext()){
      Resource nodeResource = nodeIterator.next();
      Resource nodeTypeResource = calculateInferredPropertyValue(nodeResource, RDF.type);
      
      types.add(createTypes(nodeTypeResource));
      
      definitionsContent.add(createNodeType(nodeTypeResource));
      
      nodesAndRelationshipTemplates.add(createNodeTemplate(nodeResource, nodeTypeResource));
    }
    
    nodeIterator = model.listResourcesWithProperty(RDF.type, Tosca.Node);
    while(nodeIterator.hasNext()){
      Resource nodeResource = nodeIterator.next();
      
      List<TRelationshipTemplate> relationshipTemplates = createRelationshipTemplates(nodeResource, nodesAndRelationshipTemplates);      
      for(TRelationshipTemplate relationshipTemplate : relationshipTemplates){
        nodesAndRelationshipTemplates.add(relationshipTemplate);
        
        TRelationshipType relationshipType = createRelationshipType(relationshipTemplate, model);
        definitionsContent.add(relationshipType);
      }
    }
  }
  
  private static void setTargetNamespaceAndName(TDefinitions definitions, Model model) throws MultipleNamespacesException{
    String targetNamespace = getXMLNamespace(getTopologiesNamespace(model));
    definitions.setTargetNamespace(targetNamespace);
    definitions.setName(targetNamespace);
  }
  
  private static String getXMLNamespace(String namespace){
    return namespace.replace("#", "/");
  }
  
  private static String getXMLNamespace(Resource resource){
    return getXMLNamespace(resource.getNameSpace());
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
  
  private static String getTopologiesNamespace(Model model) throws MultipleNamespacesException{
    ResIterator iter = model.listResourcesWithProperty(RDF.type, Omn.Topology);
    String targetNamespace = "";
    while(iter.hasNext()){
      String namespace = iter.next().getNameSpace();
      if(!targetNamespace.isEmpty() && !targetNamespace.equals(namespace)){
        throw new MultipleNamespacesException("Multiple topology namespaces are found: "+targetNamespace+" and "+namespace+" . This is not supported by TOSCA.");
      }
      targetNamespace = namespace;
    }
    return targetNamespace;
  }
  
  private static Element createTypes(Resource nodeType) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
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
    
    createPropertyTypes(nodeType, sequence);
    
    return types.getDocumentElement();
  }
  
  private static void createPropertyTypes(Resource nodeType, Element sequence) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    ResIterator propertiesIterator = nodeType.getModel().listSubjectsWithProperty(RDFS.domain, nodeType);
    while(propertiesIterator.hasNext()){
      Resource property = propertiesIterator.next();
      if(property.hasProperty(RDF.type, OWL.ObjectProperty)){
        createObjectPropertyType(property, sequence);
      }
      else if(property.hasProperty(RDF.type, OWL.DatatypeProperty)){
        createDatatypePropertyType(property, sequence);
      }
    }
  }

  private static void createObjectPropertyType(Resource property, Element sequence) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Element element = sequence.getOwnerDocument().createElement("xs:element");
    sequence.appendChild(element);
    element.setAttribute("name", property.getLocalName());
    
    Resource range = calculateInferredPropertyValue(property, RDFS.range);
    element.setAttribute("type", range.getLocalName());
    
    Element complexType = sequence.getOwnerDocument().createElement("xs:complexType");
    element.appendChild(complexType);
    
    Element subSequence = sequence.getOwnerDocument().createElement("xs:sequence");
    complexType.appendChild(subSequence);
    
    
    createPropertyTypes(range, subSequence);
    
    sequence.appendChild(element);
  }
  
  private static void createDatatypePropertyType(Resource property, Element sequence) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Element type = sequence.getOwnerDocument().createElement("xs:element");
    type.setAttribute("name", property.getLocalName());
    Resource range = calculateInferredPropertyValue(property, RDFS.range);
    
    if(range.getNameSpace().equals("http://www.w3.org/2001/XMLSchema#")){
      type.setAttribute("type", "xs:"+range.getLocalName());
    }
    else{
      type.setAttribute("type", range.getURI());
    }
    sequence.appendChild(type);
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
    
    List<Resource> states = nodeTypeResource.getModel().listSubjectsWithProperty(RDFS.subClassOf, Tosca.State).toList();    
    List<Resource> redundantStates = calculateRedundantResources(states);
    states.removeAll(redundantStates);
    
    for(Resource state : states){
      InstanceState instanceState = objFactory.createTTopologyElementInstanceStatesInstanceState();
      instanceState.setState(state.getLocalName());
      instanceStates.getInstanceState().add(instanceState);
    }
    nodeType.setInstanceStates(instanceStates);
  }
  
  private static String getNodeTypePropertiesName(Resource nodeTypeResource){
    return nodeTypeResource.getLocalName()+"Properties";
  }
  
  private static TNodeTemplate createNodeTemplate(Resource node, Resource nodeTypeResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
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
  
  private static Element createNodeProperties(Resource node, Resource nodeType, TNodeTemplate nodeTemplate) throws NoPropertiesFoundException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Document doc = createDocument();
    
    String nodeTypeNamespace = getXMLNamespace(nodeType);
    String nodeTypePrefix = getNSPrefix(nodeType);
    Element nodeProperties = doc.createElementNS(nodeTypeNamespace, nodeTypePrefix+":"+getNodeTypePropertiesName(nodeType));
    doc.appendChild(nodeProperties);
    
    createProperties(node, nodeType, nodeProperties, nodeTypeNamespace, nodeTypePrefix);    
    if(0 == nodeProperties.getChildNodes().getLength()){
      throw new NoPropertiesFoundException();
    }
    
    return doc.getDocumentElement();
  }
  
  private static void createProperties(Resource node, Resource nodeType, Element element, String namespace, String prefix) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    StmtIterator propertiesIterator = node.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      Property property = propertyStatement.getPredicate();
      
      if(property.hasProperty(RDFS.domain, nodeType)) {
        if(property.hasProperty(RDF.type, OWL.ObjectProperty)){
          createObjectProperty(node, propertyStatement, element, namespace, prefix);
        }
        else if(property.hasProperty(RDF.type, OWL.DatatypeProperty)){
          createDatatypeProperty(propertyStatement, element, namespace, prefix);
        }
      }
    }
  }
  
  private static void createObjectProperty(Resource node, Statement propertyStatement, Element nodeProperties, String namespace, String prefix) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Element parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, prefix+":"+propertyStatement.getPredicate().getLocalName());
    
    StmtIterator propertyValuesIterator = node.listProperties(propertyStatement.getPredicate());
    while(propertyValuesIterator.hasNext()){
      propertyStatement = propertyValuesIterator.next();
      
      Element subNode = nodeProperties.getOwnerDocument().createElementNS(namespace, prefix+":"+propertyStatement.getResource().getLocalName());
      parameter.appendChild(subNode);
      
      node = propertyStatement.getResource();
      Resource nodeType = calculateInferredPropertyValue(node, RDF.type);
      createProperties(propertyStatement.getResource(), nodeType, subNode, namespace, prefix);
    }
    nodeProperties.appendChild(parameter);
  }
  
  private static void createDatatypeProperty(Statement propertyStatement, Element nodeProperties, String namespace, String prefix){
    Element parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, prefix+":"+propertyStatement.getPredicate().getLocalName());
    parameter.setTextContent(propertyStatement.getLiteral().getString());
    nodeProperties.appendChild(parameter);
  }
  
  private static List<TRelationshipTemplate> createRelationshipTemplates(Resource nodeResource, List<TEntityTemplate> nodesAndRelationshipTemplates) throws RequiredResourceNotFoundException {
    List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();
    
    StmtIterator relationIterator = nodeResource.listProperties();
    while (relationIterator.hasNext()) {
      Statement relationStatement = relationIterator.next();
      Property relation = relationStatement.getPredicate();
      
      StmtIterator relationTypeIterator = relation.listProperties(RDF.type);
      while(relationTypeIterator.hasNext()){
        Resource relationType = relationTypeIterator.next().getResource();
        if (relationType.hasProperty(RDFS.subPropertyOf, Tosca.relatesTo)) {
          relationshipTemplates.add(createRelationshipTemplate(relationStatement, nodesAndRelationshipTemplates, relationType));
        }
      }
    }
    return relationshipTemplates;
  }

  private static TRelationshipTemplate createRelationshipTemplate(Statement relationStatement, List<TEntityTemplate> nodesAndRelationshipTemplates, Resource relationType) throws RequiredResourceNotFoundException {
    TRelationshipTemplate relationshipTemplate = objFactory.createTRelationshipTemplate();
    
    relationshipTemplate.setId(relationStatement.getPredicate().getURI());
    relationshipTemplate.setName(relationStatement.getPredicate().getLocalName());
    
    setType(relationshipTemplate, relationType);
    
    TRelationshipTemplate.SourceElement sourceElement = objFactory.createTRelationshipTemplateSourceElement();
    TNodeTemplate sourceNode = getNodeTemplateByID(relationStatement.getSubject().getURI(), nodesAndRelationshipTemplates);
    sourceElement.setRef(sourceNode);
    relationshipTemplate.setSourceElement(sourceElement);
    
    TRelationshipTemplate.TargetElement targetElement = objFactory.createTRelationshipTemplateTargetElement();
    TNodeTemplate targetNode = getNodeTemplateByID(relationStatement.getResource().getURI(), nodesAndRelationshipTemplates);
    targetElement.setRef(targetNode);
    relationshipTemplate.setTargetElement(targetElement);
    
    return relationshipTemplate;
  }
  
  private static void setType(TRelationshipTemplate relationshipTemplate, Resource relationType){
    String namespace = getXMLNamespace(relationType);
    String prefix = getNSPrefix(relationType);
    QName type = new QName(namespace, relationType.getLocalName(), prefix);
    relationshipTemplate.setType(type);
  }
  
  private static TRelationshipType createRelationshipType(TRelationshipTemplate relationshipTemplate, Model model) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    TRelationshipType relationshipType = objFactory.createTRelationshipType();
    
    QName type = relationshipTemplate.getType();
    relationshipType.setName(type.getLocalPart());
    relationshipType.setTargetNamespace(type.getNamespaceURI());
    
    Resource relationshipTypeResource = model.getResource(type.getNamespaceURI()+type.getLocalPart());
    setValidSource(relationshipType, relationshipTypeResource);
    setValidTarget(relationshipType, relationshipTypeResource);
    return relationshipType;
  }

  private static void setValidSource(TRelationshipType relationshipType, Resource relationshipTypeResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Resource source = calculateInferredPropertyValue(relationshipTypeResource, (RDFS.domain));
    String namespace = getXMLNamespace(source);
    String prefix = getNSPrefix(source);
    QName typeRef = new QName(namespace, source.getLocalName(), prefix);
    
    TRelationshipType.ValidSource validSource = objFactory.createTRelationshipTypeValidSource();
    validSource.setTypeRef(typeRef);
    relationshipType.setValidSource(validSource);
  }

  private static void setValidTarget(TRelationshipType relationshipType, Resource relationshipTypeResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Resource target = calculateInferredPropertyValue(relationshipTypeResource, (RDFS.range));
    
    String namespace = getXMLNamespace(target);
    String prefix = getNSPrefix(target);
    QName typeRef = new QName(namespace, target.getLocalName(), prefix);
    
    TRelationshipType.ValidTarget validTarget = objFactory.createTRelationshipTypeValidTarget();
    validTarget.setTypeRef(typeRef);
    relationshipType.setValidTarget(validTarget);
  }
  
  private static TNodeTemplate getNodeTemplateByID(String id, List<TEntityTemplate> nodesAndRelationshipTemplates) throws RequiredResourceNotFoundException{
    for(TEntityTemplate entitiyTemplate : nodesAndRelationshipTemplates){
      if(entitiyTemplate instanceof TNodeTemplate){
        if(id.equals(entitiyTemplate.getId())){
          return (TNodeTemplate) entitiyTemplate;
        }
      }
    }
    throw new RequiredResourceNotFoundException("The relationship source or target element with id "+id+" was not found");
  }
  
  private static Resource calculateInferredPropertyValue(Resource resource, Property property) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    List<Resource> inferredProperties = calculateInferredPropertyValues(resource, property);
    if(inferredProperties.size() == 0){
      throw new RequiredResourceNotFoundException("No property "+property+" found for: "+resource);
    } else if(inferredProperties.size() == 1){
      return inferredProperties.get(0);
    } else {
      throw new MultiplePropertyValuesException("Multiple values where found but only one was expected for property "+property+" of "+resource);
    }
  }
  
  private static List<Resource> calculateInferredPropertyValues(Resource resource, Property property) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    List<Resource> allProperties = new ArrayList<>();
    for(Statement propertyStatement : resource.listProperties(property).toList()){
      allProperties.add(propertyStatement.getResource());
    }

    List<Resource> redundantResources = calculateRedundantResources(allProperties);
  
    allProperties.removeAll(redundantResources);
    return allProperties;
  }
  
  private static List<Resource> calculateRedundantResources(List<Resource> resources){
    List<Resource> redundantResources = new ArrayList<>();
    for(Resource resource : resources){
      for(Resource resource2 : resources){
        if(resource.hasProperty(RDFS.subClassOf, resource2) && !resource.equals(resource2)){
          redundantResources.add(resource2);
        }
      }
      if(resource.equals(OWL2.NamedIndividual)){
        redundantResources.add(resource);
      }
    }
    return redundantResources;
  }
  
  private static Document createDocument(){
    DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder docBuilder = null;
    try {
      docBuilder = docFactory.newDocumentBuilder();
    } catch (ParserConfigurationException e) {
      LOG.log(Level.SEVERE, e.getMessage());
    }
    return docBuilder.newDocument();
  }
  
  public static class RequiredResourceNotFoundException extends Exception{

    private static final long serialVersionUID = 3219300357589016712L;

    public RequiredResourceNotFoundException(String message){
      super(message);
    }
  }
  
  public static class MultiplePropertyValuesException extends Exception{

    private static final long serialVersionUID = -8968550173858347068L;

    public MultiplePropertyValuesException(String message){
      super(message);
    }
  }
  
  public static class MultipleNamespacesException extends Exception{

    private static final long serialVersionUID = -6296855743962011943L;

    public MultipleNamespacesException(String message){
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
