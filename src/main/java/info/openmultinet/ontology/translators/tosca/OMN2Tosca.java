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
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
  
  public static final String JAXB_PACKAGE_NAME = "info.openmultinet.ontology.translators.tosca.jaxb";
  
  private static ObjectFactory objFactory = new ObjectFactory();
  
  public static String getTopology(Model model) throws JAXBException, InvalidModelException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Definitions definitions = objFactory.createDefinitions();
    
    setTargetNamespaceAndName(definitions, model);
    createServiceTemplates(definitions, model);
    
    return toString(definitions, JAXB_PACKAGE_NAME);
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
    serviceTemplate.setName(topologyResource.getLocalName());
    serviceTemplate.setId(topologyResource.getURI());
    
    TTopologyTemplate topologyTemplate = objFactory.createTTopologyTemplate();
    serviceTemplate.setTopologyTemplate(topologyTemplate);
    
    List<TEntityTemplate> nodesAndRelationshipTemplates = topologyTemplate.getNodeTemplateOrRelationshipTemplate();
    
    StmtIterator resourceIterator = topologyResource.listProperties(Omn.hasResource);
    while(resourceIterator.hasNext()){
      Resource nodeResource = resourceIterator.next().getResource();
      Resource nodeTypeResource = calculateInferredPropertyValue(nodeResource, RDF.type);
      
      TNodeType nodeType = createNodeType(nodeTypeResource);
      if(!containsNodeTypeWithName(definitionsContent, nodeType)){
        definitionsContent.add(nodeType);
      }
      
      nodesAndRelationshipTemplates.add(createNodeTemplate(nodeResource, nodeTypeResource, types));
    }
    
    resourceIterator = topologyResource.listProperties(Omn.hasResource);
    while(resourceIterator.hasNext()){
      Resource nodeResource = resourceIterator.next().getResource();
      
      List<TRelationshipTemplate> relationshipTemplates = createRelationshipTemplates(nodeResource, nodesAndRelationshipTemplates);      
      for(TRelationshipTemplate relationshipTemplate : relationshipTemplates){
        nodesAndRelationshipTemplates.add(relationshipTemplate);
        
        TRelationshipType relationshipType = createRelationshipType(relationshipTemplate, model);
        if(!containsRelationshipTypeWithName(definitionsContent, relationshipType)){
          definitionsContent.add(relationshipType);
        }
      }
    }
  }

  private static boolean containsNodeTypeWithName(List<TExtensibleElements> definitionsContent, TNodeType nodeType) {
    for(TExtensibleElements element : definitionsContent){
      if(element instanceof TNodeType && nodeType.getName().equals(((TNodeType) element).getName())){
        return true;
      }
    }
    return false;
  }
  
  private static boolean containsRelationshipTypeWithName(List<TExtensibleElements> definitionsContent, TRelationshipType relationshipType) {
    for(TExtensibleElements element : definitionsContent){
      if(element instanceof TRelationshipType && relationshipType.getName().equals(((TRelationshipType) element).getName())){
        return true;
      }
    }
    return false;
  }
  
  private static void setTargetNamespaceAndName(TDefinitions definitions, Model model) throws MultipleNamespacesException{
    String targetNamespace = getXMLNamespace(getTopologiesNamespace(model));
    definitions.setTargetNamespace(targetNamespace);
    definitions.setName(targetNamespace);
  }
  
  private static String getXMLNamespace(String namespace){
    return namespace.replace("#", "");
  }
  
  private static String getXMLNamespace(Resource resource){
    return getXMLNamespace(resource.getNameSpace());
  }
  
  private static String getNSPrefix(Resource resource) throws NoPrefixMappingFoundException{
    Map<String, String> prefixMap = resource.getModel().getNsPrefixMap();
    for(Map.Entry<String, String> mapping : prefixMap.entrySet()){
      if(mapping.getValue().equals(resource.getNameSpace())){
        return mapping.getKey();
      }
    }
    throw new NoPrefixMappingFoundException("No prefix mapping found for namespace: "+resource.getNameSpace());
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
  
  private static boolean typesAlreadyExist(Resource nodeType, List<Object> types){
    for(Object type : types){
      if(type instanceof Element){
        Element typeElement = (Element) type;
        NodeList propertyElements = typeElement.getElementsByTagName("xs:element");
        for(int i = 0; i < propertyElements.getLength(); i++){
          Node propertyElement = propertyElements.item(i);
          String propertiesName = propertyElement.getAttributes().getNamedItem("name").getNodeValue();
          if(getNodeTypePropertiesName(nodeType).equals(propertiesName)){
            return true;
          }
        }
      }
    }
    return false;
  }
  
  private static Element createTypes(Resource nodeType, List<Object> types) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Document doc = createDocument();
    
    Element schema = doc.createElement("xs:schema");
    schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
    schema.setAttribute("elementFormDefault", "qualified");
    schema.setAttribute("attributeFormDefault", "unqualified");
    schema.setAttribute("targetNamespace", getXMLNamespace(nodeType));
    doc.appendChild(schema);
    
    Element element = doc.createElement("xs:element");
    schema.appendChild(element);
    element.setAttribute("name", getNodeTypePropertiesName(nodeType));
    
    Element complexType = doc.createElement("xs:complexType");
    element.appendChild(complexType);
    
    Element sequence = doc.createElement("xs:sequence");
    complexType.appendChild(sequence);
    
    types.add(doc.getDocumentElement());
    
    return sequence;
  }
  
  
  private static Element createObjectPropertyType(Resource property, Element sequence) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Element element = sequence.getOwnerDocument().createElement("xs:element");
    sequence.appendChild(element);
    element.setAttribute("name", property.getLocalName());
    
    Resource range = calculateInferredPropertyValue(property, RDFS.range);
    element.setAttribute("type", range.getLocalName());
    
    Element complexType = sequence.getOwnerDocument().createElement("xs:complexType");
    element.appendChild(complexType);
    
    Element subSequence = sequence.getOwnerDocument().createElement("xs:sequence");
    complexType.appendChild(subSequence);
    
    sequence.appendChild(element);
    
    return subSequence;
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
    QName propertiesReference;
    try {
      propertiesReference = new QName(nodeTypeNameSpace, getNodeTypePropertiesName(nodeTypeResource), getNSPrefix(nodeTypeResource));
    } catch (NoPrefixMappingFoundException e) {
      propertiesReference = new QName(nodeTypeNameSpace, getNodeTypePropertiesName(nodeTypeResource));
    }
    nodeTypeProperties.setElement(propertiesReference);
    nodeType.setPropertiesDefinition(nodeTypeProperties);
  }
  
  private static void setInstanceStates(Resource nodeTypeResource, TNodeType nodeType) {
    TTopologyElementInstanceStates instanceStates = objFactory.createTTopologyElementInstanceStates();
    
    Set<Resource> states = nodeTypeResource.getModel().listSubjectsWithProperty(RDF.type, Omn_lifecycle.State).toSet();
    
    for(Resource state : states){
      if(state.getNameSpace().equals(nodeTypeResource.getNameSpace())){
        InstanceState instanceState = objFactory.createTTopologyElementInstanceStatesInstanceState();
        instanceState.setState(state.getURI());
        instanceStates.getInstanceState().add(instanceState);
      }
    }
    nodeType.setInstanceStates(instanceStates);
  }
  
  private static String getNodeTypePropertiesName(Resource nodeTypeResource){
    return nodeTypeResource.getLocalName()+"Properties";
  }
  
  private static TNodeTemplate createNodeTemplate(Resource node, Resource nodeTypeResource, List<Object> types) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    TNodeTemplate nodeTemplate = objFactory.createTNodeTemplate();
    setNameAndTypeAndID(node, nodeTypeResource, nodeTemplate);
    
    try {
      Properties properties = objFactory.createTEntityTemplateProperties();
      Element nodeProperties = createNodePropertiesAndTypes(node, nodeTypeResource, nodeTemplate, types);
      properties.setAny(nodeProperties);
      nodeTemplate.setProperties(properties);
    } catch (NoPropertiesFoundException e) {
      LOG.log(Level.INFO, e.getMessage());
    }
    
    return nodeTemplate;
  }
  
  private static void setNameAndTypeAndID(Resource node, Resource nodeTypeResource, TNodeTemplate nodeTemplate) {
    nodeTemplate.setName(getName(node));
    nodeTemplate.setId(getId(node));
    
    String nodeTypeNameSpace = getXMLNamespace(nodeTypeResource);
    QName type;
    try {
      type = new QName(nodeTypeNameSpace, nodeTypeResource.getLocalName(), getNSPrefix(nodeTypeResource));
    } catch (NoPrefixMappingFoundException e) {
      type = new QName(nodeTypeNameSpace, nodeTypeResource.getLocalName());
    }
    nodeTemplate.setType(type);
  }
  
  private static String getName(Resource resource){
    if(resource.hasProperty(Omn_lifecycle.hasID)){
      return resource.getProperty(Omn_lifecycle.hasID).getLiteral().getString();
    }
    else{
      if(resource.isAnon()){
        return resource.getId().getLabelString();
      }
      else{
        return resource.getURI();
      }
    }
  }
  
  private static String getId(Resource resource){
    if(resource.isAnon()){
      return resource.getId().getLabelString();
    }
    else{
      return resource.getURI();
    }
  }
  
  private static Element createNodePropertiesAndTypes(Resource node, Resource nodeType, TNodeTemplate nodeTemplate, List<Object> types) throws NoPropertiesFoundException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Document doc = createDocument();
    
    String nodeTypeNamespace = getXMLNamespace(nodeType);
    Element nodeProperties;
    try {
      nodeProperties = doc.createElementNS(nodeTypeNamespace, getNSPrefix(nodeType)+":"+getNodeTypePropertiesName(nodeType));
    } catch (NoPrefixMappingFoundException e) {
      nodeProperties = doc.createElementNS(nodeTypeNamespace, getNodeTypePropertiesName(nodeType));
    }
    doc.appendChild(nodeProperties);
    
    if(!typesAlreadyExist(nodeType, types)){
      Element propertiesSeq = createTypes(nodeType, types);
      createPropertyTypes(node, propertiesSeq);
    }
    
    createProperties(node, nodeType, nodeProperties, nodeTypeNamespace);  
    
    if(0 == nodeProperties.getChildNodes().getLength()){
      throw new NoPropertiesFoundException("No properties found for node "+getName(node));
    }
    
    return doc.getDocumentElement();
  }
  
  private static List<Resource> irrelevantProperties = new ArrayList<>();
  static {
    irrelevantProperties.add(OWL.sameAs);
    irrelevantProperties.add(RDF.type);
    irrelevantProperties.add(Omn.isResourceOf);
    irrelevantProperties.add(Omn_lifecycle.implementedBy);
    irrelevantProperties.add(Omn_lifecycle.hasID);
    irrelevantProperties.add(Omn_lifecycle.hasState);
    irrelevantProperties.add(Omn.relatesTo);
  }
  
  private static void createPropertyTypes(Resource node, Element propertiesSeq) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    StmtIterator propertiesIterator = node.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      Property property = propertyStatement.getPredicate();
      
      if(!irrelevantProperties.contains(property) && !isRelationshipProperty(property)){
        if(property.hasProperty(RDF.type, OWL.ObjectProperty)){
          Element subSequence = createObjectPropertyType(property, propertiesSeq);
          Resource subNode = propertyStatement.getResource();
          createPropertyTypes(subNode, subSequence);
        }
        else if(property.hasProperty(RDF.type, OWL.DatatypeProperty)){
          createDatatypePropertyType(property, propertiesSeq);
        }
      }
    }
  }
  
  private static void createProperties(Resource node, Resource nodeType, Element element, String namespace) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    StmtIterator propertiesIterator = node.listProperties();
    while(propertiesIterator.hasNext()){
      Statement propertyStatement = propertiesIterator.next();
      Property property = propertyStatement.getPredicate();
      
      if(!irrelevantProperties.contains(property) && !isRelationshipProperty(property)){
        if(property.hasProperty(RDF.type, OWL.ObjectProperty)){
          createObjectProperty(propertyStatement, element, namespace, nodeType);
        }
        else if(property.hasProperty(RDF.type, OWL.DatatypeProperty)){
          createDatatypeProperty(propertyStatement, element, namespace, nodeType);
        }
      }
    }
  }
  
  private static void createObjectProperty(Statement propertyStatement, Element nodeProperties, String namespace, Resource nodeType) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Element parameter;
    try {
      parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, getNSPrefix(nodeType)+":"+propertyStatement.getPredicate().getLocalName());
    } catch (NoPrefixMappingFoundException e) {
      parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, propertyStatement.getPredicate().getLocalName());
    }
    
    if(!propertyStatement.getResource().isAnon()){
      parameter.setAttribute("name", propertyStatement.getResource().getLocalName());
    }
    
    Resource subNode = propertyStatement.getResource();
    Resource newNodeType = calculateInferredPropertyValue(subNode, RDF.type);
    createProperties(subNode, newNodeType, parameter, namespace);
    
    nodeProperties.appendChild(parameter);
  }
  
  private static void createDatatypeProperty(Statement propertyStatement, Element nodeProperties, String namespace, Resource nodeType) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Element parameter;
    try {
      parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, getNSPrefix(nodeType)+":"+propertyStatement.getPredicate().getLocalName());
    } catch (NoPrefixMappingFoundException e) {
      parameter = nodeProperties.getOwnerDocument().createElementNS(namespace, propertyStatement.getPredicate().getLocalName());
    }
    parameter.setTextContent(propertyStatement.getLiteral().getString());
    nodeProperties.appendChild(parameter);
  }
  
  private static List<TRelationshipTemplate> createRelationshipTemplates(Resource nodeResource, List<TEntityTemplate> nodesAndRelationshipTemplates) throws RequiredResourceNotFoundException {
    List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();
    
    StmtIterator relationIterator = nodeResource.listProperties();
    while (relationIterator.hasNext()) {
      Statement relationStatement = relationIterator.next();
      Property relation = relationStatement.getPredicate();
      
      if(isRelationshipProperty(relation)){
        relationshipTemplates.add(createRelationshipTemplate(relationStatement, nodesAndRelationshipTemplates));
      }
    }
    return relationshipTemplates;
  }
  
  private static boolean isRelationshipProperty(Property property){
    if(property.equals(Omn.relatesTo)){
      return false;
    }
    try{
      getRelationshipPropertyType(property);
    } catch(RequiredResourceNotFoundException e){
      return false;
    }
    return true;
  }
  
  private static Resource getRelationshipPropertyType(Property property) throws RequiredResourceNotFoundException{
    if(property.hasProperty(RDFS.subPropertyOf, Omn.relatesTo)){
      return property;
    }
    StmtIterator relationTypeIterator = property.listProperties(RDF.type);
    while(relationTypeIterator.hasNext()){
      Resource relationType = relationTypeIterator.next().getResource();
      if (relationType.hasProperty(RDFS.subPropertyOf, Omn.relatesTo)) {
        return relationType;
      }
    }
    throw new RequiredResourceNotFoundException("No relationship type found for relation: "+property);
  }

  private static TRelationshipTemplate createRelationshipTemplate(Statement relationStatement, List<TEntityTemplate> nodesAndRelationshipTemplates) throws RequiredResourceNotFoundException {
    TRelationshipTemplate relationshipTemplate = objFactory.createTRelationshipTemplate();
    
    relationshipTemplate.setId(relationStatement.getPredicate().getURI());
    relationshipTemplate.setName(relationStatement.getPredicate().getLocalName());
    
    setType(relationshipTemplate, getRelationshipPropertyType(relationStatement.getPredicate()));
    
    TRelationshipTemplate.SourceElement sourceElement = objFactory.createTRelationshipTemplateSourceElement();
    TNodeTemplate sourceNode = getNodeTemplateByName(getName(relationStatement.getSubject()), nodesAndRelationshipTemplates);
    sourceElement.setRef(sourceNode);
    relationshipTemplate.setSourceElement(sourceElement);
    
    TRelationshipTemplate.TargetElement targetElement = objFactory.createTRelationshipTemplateTargetElement();
    TNodeTemplate targetNode = getNodeTemplateByName(getName(relationStatement.getResource()), nodesAndRelationshipTemplates);
    targetElement.setRef(targetNode);
    relationshipTemplate.setTargetElement(targetElement);
    
    return relationshipTemplate;
  }
  
  private static void setType(TRelationshipTemplate relationshipTemplate, Resource relationType){
    String namespace = getXMLNamespace(relationType);
    QName type;
    try {
      type = new QName(namespace, relationType.getLocalName(), getNSPrefix(relationType));
    } catch (NoPrefixMappingFoundException e) {
      type = new QName(namespace, relationType.getLocalName());
    }
    relationshipTemplate.setType(type);
  }
  
  private static TRelationshipType createRelationshipType(TRelationshipTemplate relationshipTemplate, Model model) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    TRelationshipType relationshipType = objFactory.createTRelationshipType();
    
    QName type = relationshipTemplate.getType();
    relationshipType.setName(type.getLocalPart());
    relationshipType.setTargetNamespace(type.getNamespaceURI());
    
    String namespace = Tosca2OMN.getRDFNamespace(type.getNamespaceURI());
    Resource relationshipTypeResource = model.getResource(namespace+type.getLocalPart());
    setValidSource(relationshipType, relationshipTypeResource);
    setValidTarget(relationshipType, relationshipTypeResource);
    return relationshipType;
  }

  private static void setValidSource(TRelationshipType relationshipType, Resource relationshipTypeResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Resource source = calculateInferredPropertyValue(relationshipTypeResource, RDFS.domain);
    String namespace = getXMLNamespace(source);
    QName typeRef;
    try {
      typeRef = new QName(namespace, source.getLocalName(), getNSPrefix(source));
    } catch (NoPrefixMappingFoundException e) {
      typeRef = new QName(namespace, source.getLocalName());
    }
    
    TRelationshipType.ValidSource validSource = objFactory.createTRelationshipTypeValidSource();
    validSource.setTypeRef(typeRef);
    relationshipType.setValidSource(validSource);
  }

  private static void setValidTarget(TRelationshipType relationshipType, Resource relationshipTypeResource) throws RequiredResourceNotFoundException, MultiplePropertyValuesException {
    Resource target = calculateInferredPropertyValue(relationshipTypeResource, (RDFS.range));
    
    String namespace = getXMLNamespace(target);
    QName typeRef;
    try {
      typeRef = new QName(namespace, target.getLocalName(), getNSPrefix(target));
    } catch (NoPrefixMappingFoundException e) {
      typeRef = new QName(namespace, target.getLocalName());
    }
    
    TRelationshipType.ValidTarget validTarget = objFactory.createTRelationshipTypeValidTarget();
    validTarget.setTypeRef(typeRef);
    relationshipType.setValidTarget(validTarget);
  }
  
  private static TNodeTemplate getNodeTemplateByName(String name, List<TEntityTemplate> nodesAndRelationshipTemplates) throws RequiredResourceNotFoundException{
    for(TEntityTemplate entitiyTemplate : nodesAndRelationshipTemplates){
      if(entitiyTemplate instanceof TNodeTemplate){
        if(name.equals(((TNodeTemplate) entitiyTemplate).getName())){
          return (TNodeTemplate) entitiyTemplate;
        }
      }
    }
    throw new RequiredResourceNotFoundException("The relationship source or target element with name "+name+" was not found");
  }
  
  private static Resource calculateInferredPropertyValue(Resource resource, Property property) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Set<Resource> inferredProperties = calculateInferredPropertyValues(resource, property);
    if(inferredProperties.size() == 0){
      throw new RequiredResourceNotFoundException("No property "+property+" found for: "+resource);
    } else if(inferredProperties.size() == 1){
      return inferredProperties.iterator().next();
    } else {
      throw new MultiplePropertyValuesException("Multiple values where found but only one was expected for property "+property+" of "+resource+": "+inferredProperties);
    }
  }
  
  private static Set<Resource> calculateInferredPropertyValues(Resource resource, Property property) throws RequiredResourceNotFoundException, MultiplePropertyValuesException{
    Set<Resource> allProperties = new HashSet<Resource>();
    for(Statement propertyStatement : resource.listProperties(property).toList()){
      if(!propertyStatement.getResource().isAnon()){
        allProperties.add(propertyStatement.getResource());
      }
    }

    Set<Resource> redundantResources = calculateRedundantResources(allProperties);
  
    allProperties.removeAll(redundantResources);
    return allProperties;
  }
  
  private static Set<Resource> calculateRedundantResources(Set<Resource> resources){
    Set<Resource> redundantResources = new HashSet<Resource>();
    for(Resource resource : resources){
      for(Resource resource2 : resources){
        if(resource.equals(OWL2.NamedIndividual) || resource.equals(Omn.Service) || resource.equals(Omn_resource.Node) || resource.equals(Omn_resource.NetworkObject)){
          redundantResources.add(resource);
        }
        else if(resource.hasProperty(RDFS.subClassOf, resource2) && !resource.equals(resource2)){
          redundantResources.add(resource2);
        }
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

    public NoPropertiesFoundException(String message){
      super(message);
    }
  }
  
  public static class NoPrefixMappingFoundException extends Exception{

    private static final long serialVersionUID = 7286796960642767251L;

    public NoPrefixMappingFoundException(String message){
      super(message);
    }
  }
  
}
