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
import info.openmultinet.ontology.translators.tosca.jaxb.TRelationshipType;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyElementInstanceStates.InstanceState;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;
import java.util.List;
import java.util.Random;
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
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Tosca2OMN extends AbstractConverter {
  
  private static final Logger LOG = Logger.getLogger(Tosca2OMN.class.getName());
  
  private static Random random = new Random();
  
  public static Model getModel(final InputStream input) throws JAXBException, InvalidModelException, UnsupportedException {
    final JAXBContext context = JAXBContext.newInstance(Definitions.class);
    final Unmarshaller unmarshaller = context.createUnmarshaller();
    final Definitions definitions = unmarshaller.unmarshal(new StreamSource(input), Definitions.class).getValue();
    final Model model = tosca2Model(definitions);
    
    Parser.setCommonPrefixes(model);
    model.setNsPrefix("target", getRDFNamespace(definitions.getTargetNamespace()));
    return model;
  }
  
  private static Model tosca2Model(final Definitions definitions) throws UnsupportedException {
    final Model model = ModelFactory.createDefaultModel();
    
    processTypes(definitions, model);
    processNodeTypes(definitions, model);
    processRelationshipTypes(definitions, model);
    processTemplates(definitions, model);
    
    return model;
  }
  
  private static void processTypes(final Definitions definitions, final Model model) {
    if(definitions.getTypes() != null){
      final List<Object> types = definitions.getTypes().getAny();
      for (final Object schema : types) {
        if (schema instanceof Element) {
          final Element schemaElement = (Element) schema;
          if (isXMLSchemaElement(schemaElement)) {
            String namespace = getRDFNamespace(schemaElement.getAttribute("targetNamespace"));
            if(namespace.isEmpty() || namespace.equals("/")){
              namespace = getRDFNamespace(definitions.getTargetNamespace());
            }
            for (int i = 0; i < (schemaElement.getChildNodes().getLength() - 1); i++) {
              final Node elementNode = schemaElement.getChildNodes().item(i);
              final String superPropertyName = elementNode.getAttributes().getNamedItem("name").getNodeValue();
              final Resource superProperty = model.createResource(namespace+superPropertyName);
              try{
                createTypeProperty(model, namespace, elementNode, superProperty);
              } catch(PropertyNotFoundException e){
                LOG.log(Level.WARNING, "No domain found for property "+superProperty+" .");
              } 
            }
          }
        }
      }
    }
  }

  private static void createTypeProperty(Model model, String namespace, Node elementNode, Resource superProperty) {
    for (int j = 0; j < (elementNode.getChildNodes().getLength() - 1); j++) {
      final Node typesNode = elementNode.getChildNodes().item(j);
      if (typesNode.getLocalName().equals("complexType")) {
        createType(model, namespace, superProperty, typesNode);
      }
    }
  }

  private static void createType(Model model, String namespace, Resource superProperty, Node typesNode) {
    for (int k = 0; k < (typesNode.getChildNodes().getLength() - 1); k++) {
      final Node sequenceNode = typesNode.getChildNodes().item(k);
      if (sequenceNode.getLocalName().equals("sequence")) {
        for (int l = 0; l < (sequenceNode.getChildNodes().getLength() - 1); l++) {
          final Node typeNode = sequenceNode.getChildNodes().item(l);
          if (typeNode.getLocalName().equals("element")) {
            final String name = typeNode.getAttributes().getNamedItem("name").getNodeValue();
            final String type = typeNode.getAttributes().getNamedItem("type").getNodeValue();
            
            Property property = model.createProperty(namespace + name);
            Resource propertyRangeClass;
            if(type.startsWith("xs:")){
              propertyRangeClass = getXSDType(type, model);
              property.addProperty(RDF.type, OWL.DatatypeProperty);
            }
            else{
              propertyRangeClass = model.createResource(namespace + type);
              propertyRangeClass.addProperty(RDF.type, OWL2.Class);
              property.addProperty(RDF.type, OWL.ObjectProperty);
              createTypeProperty(model, namespace, typeNode, null);
            }
            if(superProperty != null){
              property.addProperty(RDFS.subPropertyOf, superProperty);
            }
            property.addProperty(RDFS.range, propertyRangeClass);
          }
        }
      }
    }
  }
  
  private static Resource getXSDType(final String type, final Model model) {
    switch (type) {
      case "xs:string":
        return XSD.xstring;
      case "xs:integer":
        return XSD.xint;
      case "xs:boolean":
        return XSD.xboolean;
      case "xs:long":
        return XSD.xlong;
      case "xs:double":
        return XSD.xdouble;
      case "xs:float":
        return XSD.xfloat;     
    }
    return model.createResource(type);
  }
  
  private static boolean isXMLSchemaElement(final Element element) {
    return element.getNamespaceURI().equals("http://www.w3.org/2001/XMLSchema")
        && element.getLocalName().equals("schema");
  }
  
  private static void processNodeTypes(final Definitions definitions, final Model model) {
    for (final TExtensibleElements templateOrNodeType : definitions
        .getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if (templateOrNodeType instanceof TNodeType) {
        createNodeType((TNodeType) templateOrNodeType, model);
        createStates((TNodeType) templateOrNodeType, model);
      }
    }
  }
  
  private static void processTemplates(final Definitions definitions, final Model model) throws UnsupportedException {
    for (final TExtensibleElements templateOrNodeType : definitions
        .getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if (templateOrNodeType instanceof TServiceTemplate) {
        processServiceTemplate((TServiceTemplate) templateOrNodeType, definitions, model);
      }
    }
  }
  
  private static void processServiceTemplate(TServiceTemplate serviceTemplate, Definitions definitions, Model model) throws UnsupportedException {
    String namespace = getRDFNamespace(definitions.getTargetNamespace());
    Resource topologyResource = createTopology(model, serviceTemplate, namespace);
    
    final TTopologyTemplate topologyTemplate = serviceTemplate.getTopologyTemplate();
    for (final TEntityTemplate entityTemplate : topologyTemplate.getNodeTemplateOrRelationshipTemplate()) {
      if (entityTemplate instanceof TNodeTemplate) {
        Resource node = createNode((TNodeTemplate) entityTemplate, namespace, model);
        topologyResource.addProperty(Omn.hasResource, node);
        
      } else if (entityTemplate instanceof TRelationshipTemplate) {
        createRelationship((TRelationshipTemplate) entityTemplate, namespace, model);
      }
    }
  }
  
  private static Resource createTopology(Model model, TServiceTemplate serviceTemplate, String namespace){
    Resource topologyResource = model.createResource(namespace+serviceTemplate.getName());
    topologyResource.addProperty(RDF.type, Omn.Topology);
    topologyResource.addProperty(RDF.type, OWL2.NamedIndividual);
    return topologyResource;
  }
  
  private static Resource createNode(final TNodeTemplate nodeTemplate, String namespace, final Model model) throws UnsupportedException {
    final Resource node = model.createResource(namespace + nodeTemplate.getName());
    setNodeType(nodeTemplate, node, model);
    setNodeProperties(nodeTemplate, node, model);
    return node;
  }
  
  private static void setNodeProperties(TNodeTemplate nodeTemplate, Resource node, Model model) throws UnsupportedException {
    if(nodeTemplate.getProperties() != null){
      final Object properties = nodeTemplate.getProperties().getAny();
      if (properties instanceof Node) {
        Node propertiesElement = (Node) properties;
        processPropertiesElement(node, model, propertiesElement);
      }
    }
  }

  private static void processPropertiesElement(Resource node, Model model, Node propertiesElement) throws UnsupportedException {
    for (int i = 0; i < (propertiesElement.getChildNodes().getLength()); i++) {
      Node propertyNode = propertiesElement.getChildNodes().item(i);
      String namespace = getRDFNamespace(propertyNode.getNamespaceURI());
      Property property = model.getProperty(namespace + propertyNode.getLocalName());      
      Resource propertyRange = getPropertyRange(property);
      
      if(propertyNode.getChildNodes().getLength() == 1){
        if(propertyNode.getTextContent() != null){
          final Literal literal = model.createTypedLiteral(propertyNode.getTextContent(), propertyRange.getURI());
          node.addLiteral(property, literal);
        }
        else{
          throw new UnsupportedException("Expected text content in property: "+propertyNode.getLocalName());
        }
      }
      else if(propertyNode.getChildNodes().getLength() > 1){
        Node propertyValueName = propertyNode.getAttributes().getNamedItem("name");
        String propertyValueNameString;
        if(propertyValueName == null){
          propertyValueNameString = propertyNode.getLocalName()+String.valueOf(random.nextInt());
        }
        else{
          propertyValueNameString = propertyValueName.getNodeValue();
        }
        Resource propertyValue = model.createResource(node.getNameSpace() + propertyValueNameString);
        propertyValue.addProperty(RDF.type, propertyRange);
        propertyValue.addProperty(RDF.type, OWL2.NamedIndividual);
        node.addProperty(property, propertyValue);
        
        processPropertiesElement(propertyValue, model, propertyNode);
      }
    }
  }
  
  private static Resource getPropertyRange(Resource property){
    Resource propertyRange;
    try {
      propertyRange = property.getRequiredProperty(RDFS.range).getResource();
    } catch (final PropertyNotFoundException e) {
      LOG.log(Level.WARNING, "No property range for property " + property.getURI() + " found, storing as string.");
      propertyRange = XSD.xstring;
    }
    return propertyRange;
  }
  
  private static void setNodeType(final TNodeTemplate nodeTemplate, final Resource node, final Model model) throws UnsupportedException {
    final QName type = nodeTemplate.getType();
    if(type == null){
      throw new UnsupportedException("No type for nodeTemplate "+nodeTemplate.getName()+" found");
    }
    final Resource nodeType = createResourceFromQName(type, model);
    node.addProperty(RDF.type, nodeType);
    node.addProperty(RDF.type, OWL2.NamedIndividual);
  }
  
  private static void createRelationship(TRelationshipTemplate relationshipTemplate, String namespace, Model model) throws UnsupportedException {
    final Property relationship = model.createProperty(namespace + relationshipTemplate.getName());
    setRelationshipType(relationshipTemplate, relationship, model);
    
    Resource source = getRelationshipSource(relationshipTemplate, model, namespace);
    Resource target = getRelationshipTarget(relationshipTemplate, model, namespace);
    source.addProperty(relationship, target);
  }
  
  private static Resource getRelationshipSource(TRelationshipTemplate relationshipTemplate, Model model, String namespace) throws UnsupportedException {
    Object sourceElement = relationshipTemplate.getSourceElement().getRef();
    if(sourceElement instanceof TNodeTemplate){
      TNodeTemplate sourceNode = (TNodeTemplate) sourceElement;
      String sourceName = sourceNode.getName();
      return model.getResource(namespace + sourceName);
    }
    else{
      throw new UnsupportedException("The source element of relationshipTemplate "+relationshipTemplate.getName()+" must refer to a NodeTemplate.");
    }
  }
  
  private static Resource getRelationshipTarget(TRelationshipTemplate relationshipTemplate, Model model, String namespace) throws UnsupportedException {
    Object targetElement = relationshipTemplate.getTargetElement().getRef();
    if(targetElement instanceof TNodeTemplate){
      TNodeTemplate targetNode = (TNodeTemplate) targetElement;
      String targetName = targetNode.getName();
      return model.getResource(namespace + targetName);
    }
    else{
      throw new UnsupportedException("The target element of a RelationshipTemplate must refer to a NodeTemplate.");
    }
  }

  private static void setRelationshipType(TRelationshipTemplate relationshipTemplate, Property relationship, Model model) throws UnsupportedException {
    final QName type = relationshipTemplate.getType();
    if(type == null){
      throw new UnsupportedException("No type for relationshipTemplate "+relationshipTemplate.getName()+" found");
    }
    final Resource relationshipType = createResourceFromQName(type, model);
    relationship.addProperty(RDF.type, relationshipType);
    relationship.addProperty(RDF.type, OWL2.NamedIndividual);
  }
  
  private static void createStates(final TNodeType nodeType, final Model model) {
    if(nodeType.getInstanceStates() != null){
      for (final InstanceState instanceState : nodeType.getInstanceStates().getInstanceState()) {
        final Resource state = model.createResource(instanceState.getState());
        state.addProperty(RDF.type, Omn_lifecycle.State);
      }
    }
  }
  
  private static Resource createNodeType(TNodeType nodeType, Model model) {
    final String namespace = getRDFNamespace(nodeType.getTargetNamespace());
    final Resource nodeTypeResource = model.getResource(namespace + nodeType.getName());
    nodeTypeResource.addProperty(RDFS.subClassOf, Omn.Resource);
    final PropertiesDefinition propertiesDefinition = nodeType.getPropertiesDefinition();
    if(propertiesDefinition != null){
      createResourceFromQName(propertiesDefinition.getElement(), model);
    }
    return nodeTypeResource;
  }
  
  private static void processRelationshipTypes(final Definitions definitions, final Model model) {
    for (final TExtensibleElements element : definitions
        .getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
      if (element instanceof TRelationshipType) {
        createRelationshipType((TRelationshipType) element, model);
      }
    }
  }
  
  private static void createRelationshipType(TRelationshipType relationshipType, Model model) {
    String namespace = getRDFNamespace(relationshipType.getTargetNamespace());
    Resource relationshipTypeResource = model.createResource(namespace + relationshipType.getName());
    relationshipTypeResource.addProperty(RDF.type, OWL2.ObjectProperty);
    relationshipTypeResource.addProperty(RDFS.subPropertyOf, Omn.relatesTo);
    
    setValidSource(relationshipType, relationshipTypeResource);
    setValidTarget(relationshipType, relationshipTypeResource);
  }

  private static void setValidTarget(TRelationshipType relationshipType, Resource relationshipTypeResource) {
    if(relationshipType.getValidTarget() != null){
      QName validTarget = relationshipType.getValidTarget().getTypeRef();
      Resource target = createResourceFromQName(validTarget, relationshipTypeResource.getModel());
      relationshipTypeResource.addProperty(RDFS.range, target);
    }
  }

  private static void setValidSource(TRelationshipType relationshipType, Resource relationshipTypeResource) {
    if(relationshipType.getValidSource() != null){
      QName validSource = relationshipType.getValidSource().getTypeRef();
      Resource source = createResourceFromQName(validSource, relationshipTypeResource.getModel());
      relationshipTypeResource.addProperty(RDFS.domain, source);
    }
  }
  
  private static String getRDFNamespace(String namespace){
    if(namespace == null){
      return "";
    }
    if(!(namespace.endsWith("/") || namespace.endsWith("#"))){
      namespace = namespace.concat("/");
    }
    return namespace;
  }

  private static Resource createResourceFromQName(final QName qname, final Model model) {
    String namespace = getRDFNamespace(qname.getNamespaceURI());
    return model.createResource(namespace + qname.getLocalPart());
  }
  
  public static class UnsupportedException extends Exception{

    private static final long serialVersionUID = -7910873917548269970L;

    public UnsupportedException(String message){
      super(message);
    }
  }
  
}
