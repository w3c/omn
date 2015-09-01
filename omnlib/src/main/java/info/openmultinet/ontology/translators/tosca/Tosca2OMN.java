package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class Tosca2OMN extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(Tosca2OMN.class
			.getName());

	private static Random random = new Random();

	public static Model getModel(final InputStream input) throws JAXBException,
			UnsupportedException {
		final JAXBContext context = JAXBContext.newInstance(Definitions.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final Definitions definitions = unmarshaller.unmarshal(
				new StreamSource(input), Definitions.class).getValue();

		return getModel(definitions);
	}

	public static Model getModel(final Definitions definitions)
			throws UnsupportedException {
		final Model model = ModelFactory.createDefaultModel();

		processTypes(definitions, model);
		processNodeTypes(definitions, model);
		processRelationshipTypes(definitions, model);
		processTemplates(definitions, model);

		Parser.setCommonPrefixes(model);
		if (definitions.getTargetNamespace() != null) {
			model.setNsPrefix("target",
					getRDFNamespace(definitions.getTargetNamespace()));
		}
		return model;
	}

	private static void processTypes(Definitions definitions, Model model) {
		if (definitions.getTypes() != null) {
			final List<Object> types = definitions.getTypes().getAny();
			for (final Object schema : types) {
				if (schema instanceof Element) {
					final Element schemaElement = (Element) schema;
					if (isXMLSchemaElement(schemaElement)) {
						String namespace = getRDFNamespace(schemaElement
								.getAttribute("targetNamespace"));
						if (namespace.isEmpty() || namespace.equals("/")) {
							namespace = getRDFNamespace(definitions
									.getTargetNamespace());
						}
						for (int i = 0; i < (schemaElement.getChildNodes()
								.getLength()); i++) {
							final Node elementNode = schemaElement
									.getChildNodes().item(i);
							if (elementNode.getAttributes() != null
									&& elementNode.getAttributes()
											.getNamedItem("name") != null) {
								String propertyRef = elementNode
										.getAttributes().getNamedItem("name")
										.getNodeValue();
								Set<Resource> domainClasses = getAllNodeTypesWithProperyRef(
										definitions, model, namespace
												+ propertyRef);
								createTypeProperty(model, namespace,
										elementNode, domainClasses);
							}
						}
					}
				}
			}
		}
	}

	private static Set<Resource> getAllNodeTypesWithProperyRef(
			Definitions definitions, Model model, String propertyRef) {
		Set<Resource> types = new HashSet<Resource>();
		for (TExtensibleElements element : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TNodeType) {
				TNodeType nodeType = (TNodeType) element;
				QName propertiesRef = nodeType.getPropertiesDefinition()
						.getElement();
				String propertiesNamespace = getRDFNamespace(propertiesRef
						.getNamespaceURI());
				if (propertyRef.equals(propertiesNamespace
						+ propertiesRef.getLocalPart())) {
					String nodeNamespace = getRDFNamespace(nodeType
							.getTargetNamespace());
					Resource nodeTypeResource = model
							.createResource(nodeNamespace + nodeType.getName());
					types.add(nodeTypeResource);
				}
			}
		}
		return types;
	}

	private static void createTypeProperty(Model model, String namespace,
			Node elementNode, Set<Resource> domainClasses) {
		for (int j = 0; j < (elementNode.getChildNodes().getLength()); j++) {
			final Node typesNode = elementNode.getChildNodes().item(j);
			if ("complexType".equals(typesNode.getLocalName())) {
				createType(model, namespace, typesNode, domainClasses);
			}
		}
	}

	private static void createType(Model model, String namespace,
			Node typesNode, Set<Resource> domainClasses) {
		for (int k = 0; k < (typesNode.getChildNodes().getLength()); k++) {
			final Node sequenceNode = typesNode.getChildNodes().item(k);
			if ("sequence".equals(sequenceNode.getLocalName())) {
				for (int l = 0; l < (sequenceNode.getChildNodes().getLength()); l++) {
					final Node typeNode = sequenceNode.getChildNodes().item(l);
					if ("element".equals(typeNode.getLocalName())) {
						final String name = typeNode.getAttributes()
								.getNamedItem("name").getNodeValue();
						final String type = typeNode.getAttributes()
								.getNamedItem("type").getNodeValue();

						Property property = model.createProperty(namespace
								+ name);
						Resource rangeClass;
						if (type.startsWith("xs:")) {
							rangeClass = getXSDType(type, model);
							property.addProperty(RDF.type, OWL.DatatypeProperty);
						} else {
							rangeClass = model.createResource(namespace + type);
							rangeClass.addProperty(RDF.type, OWL2.Class);
							property.addProperty(RDF.type, OWL.ObjectProperty);
							Set<Resource> newDomainClasses = new HashSet<>();
							newDomainClasses.add(rangeClass);
							createTypeProperty(model, namespace, typeNode,
									newDomainClasses);
						}
						property.addProperty(RDFS.range, rangeClass);
						addDomainClassesToProperty(domainClasses, property);
					}
				}
			}
		}
	}

	private static void addDomainClassesToProperty(Set<Resource> domainClasses,
			Property property) {
		if (property.getProperty(RDFS.domain) == null) {
			if (domainClasses.size() == 1) {
				property.addProperty(RDFS.domain, domainClasses.iterator()
						.next());
			} else if (domainClasses.size() > 1) {
				Resource unionResource = property.getModel().createResource();
				property.addProperty(RDFS.domain, unionResource);

				RDFList domainClassesList = property.getModel().createList(
						domainClasses.iterator());
				unionResource.addProperty(OWL2.disjointUnionOf,
						domainClassesList);
			}
		} else {
			Resource existingDomainClass = property.getProperty(RDFS.domain)
					.getResource();
			if (existingDomainClass.isAnon()) {
				RDFList rangeClassesList = existingDomainClass
						.getProperty(OWL2.disjointUnionOf).getResource()
						.as(RDFList.class);
				Iterator<Resource> iter = domainClasses.iterator();
				while (iter.hasNext()) {
					rangeClassesList.add(iter.next());
				}
			} else {
				domainClasses.add(existingDomainClass);
				property.removeAll(RDFS.domain);
				Resource unionResource = property.getModel().createResource();
				property.addProperty(RDFS.domain, unionResource);

				RDFList rangeClassesList = property.getModel().createList(
						domainClasses.iterator());
				unionResource.addProperty(OWL2.disjointUnionOf,
						rangeClassesList);
			}
		}
	}

	private static Resource getXSDType(final String type, final Model model) {
		switch (type) {
		case "xs:string":
			return XSD.xstring;
		case "xs:integer":
			return XSD.xint;
		case "xs:int":
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
		if (element != null && element.getNamespaceURI() != null) {
			return element.getNamespaceURI().equals(
					"http://www.w3.org/2001/XMLSchema")
					&& element.getLocalName().equals("schema");
		} 
		return false;
	}

	private static void processNodeTypes(final Definitions definitions,
			final Model model) throws UnsupportedException {
		for (final TExtensibleElements templateOrNodeType : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (templateOrNodeType instanceof TNodeType) {
				createNodeType((TNodeType) templateOrNodeType, model);
				createStates((TNodeType) templateOrNodeType, model);
			}
		}
	}

	private static void processTemplates(final Definitions definitions,
			final Model model) throws UnsupportedException {
		for (final TExtensibleElements templateOrNodeType : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (templateOrNodeType instanceof TServiceTemplate) {
				processServiceTemplate((TServiceTemplate) templateOrNodeType,
						definitions, model);
			}
		}
	}

	private static void processServiceTemplate(
			TServiceTemplate serviceTemplate, Definitions definitions,
			Model model) throws UnsupportedException {
		String namespace = getRDFNamespace(definitions.getTargetNamespace());
		Resource topologyResource = null;
		try {
			topologyResource = createTopology(model, serviceTemplate, namespace);
		} catch (UnsupportedException e) {
			LOG.log(Level.WARNING,
					"No id for service template found, thus no topology will be created");
		}

		final TTopologyTemplate topologyTemplate = serviceTemplate
				.getTopologyTemplate();
		for (final TEntityTemplate entityTemplate : topologyTemplate
				.getNodeTemplateOrRelationshipTemplate()) {
			if (entityTemplate instanceof TNodeTemplate) {
				Resource node = createNode((TNodeTemplate) entityTemplate,
						namespace, model);
				if (topologyResource != null) {
					topologyResource.addProperty(Omn.hasResource, node);
				}

			} else if (entityTemplate instanceof TRelationshipTemplate) {
				createRelationship((TRelationshipTemplate) entityTemplate,
						namespace, model);
			}
		}
	}

	private static Resource createTopology(Model model,
			TServiceTemplate serviceTemplate, String namespace)
			throws UnsupportedException {
		Resource topologyResource = model.createResource(getURI(
				serviceTemplate, namespace));
		topologyResource.addProperty(RDF.type, Omn.Topology);
		topologyResource.addProperty(RDF.type, OWL2.NamedIndividual);
		return topologyResource;
	}

	private static Resource createNode(TNodeTemplate nodeTemplate,
			String namespace, Model model) throws UnsupportedException {
		Resource node = model.createResource(getURI(nodeTemplate, namespace));

		setNodeId(nodeTemplate, node);
		setNodeTypeAndState(nodeTemplate, node);
		setNodeProperties(nodeTemplate, node, namespace);
		return node;
	}

	private static void setNodeId(TNodeTemplate nodeTemplate, Resource node) {
		node.addProperty(Omn_lifecycle.hasID, nodeTemplate.getName());
	}

	private static void setNodeTypeAndState(TNodeTemplate nodeTemplate,
			Resource node) throws UnsupportedException {
		final QName type = nodeTemplate.getType();
		if (type == null) {
			throw new UnsupportedException("No type for nodeTemplate "
					+ nodeTemplate.getName() + " found");
		}
		final Resource nodeType = createResourceFromQName(type, node.getModel());
		node.addProperty(RDF.type, nodeType);
		node.addProperty(RDF.type, OWL2.NamedIndividual);

		setNodeState(nodeTemplate, node, nodeType.getNameSpace());
	}

	private static void setNodeState(TNodeTemplate nodeTemplate, Resource node,
			String namespace) {
		for (Object any : nodeTemplate.getAny()) {
			if (any instanceof ElementNSImpl) {
				Element element = (ElementNSImpl) any;
				if (element.getNodeName().equals(
						InstanceState.class.getSimpleName())) {
					if (element.getAttribute("state") != null) {
						Resource state = node.getModel().createResource(
								namespace + element.getAttribute("state"));
						node.addProperty(Omn_lifecycle.hasState, state);
					}
				}
			}
		}
	}

	private static void setNodeProperties(TNodeTemplate nodeTemplate,
			Resource node, String namespace) throws UnsupportedException {
		if (nodeTemplate.getProperties() != null) {
			final Object properties = nodeTemplate.getProperties().getAny();
			if (properties instanceof Node) {
				Node propertiesElement = (Node) properties;
				processPropertiesElement(node, propertiesElement, namespace);
			}
		}
	}

	private static void processPropertiesElement(Resource node,
			Node propertiesElement, String targetNamespace)
			throws UnsupportedException {
		for (int i = 0; i < (propertiesElement.getChildNodes().getLength()); i++) {
			Node propertyNode = propertiesElement.getChildNodes().item(i);
			if (propertyNode.getLocalName() != null) {
				String namespace = getRDFNamespace(propertyNode
						.getNamespaceURI());
				Property property = node.getModel().getProperty(
						namespace + propertyNode.getLocalName());
				Resource propertyRange = getPropertyRange(property);

				if (propertyNode.getChildNodes().getLength() == 1
						&& !propertyNode.getChildNodes().item(0)
								.hasChildNodes()) {
					if (propertyNode.getTextContent() != null) {
						final Literal literal = node.getModel()
								.createTypedLiteral(
										propertyNode.getTextContent(),
										propertyRange.getURI());
						node.addLiteral(property, literal);
					} else {
						throw new UnsupportedException(
								"Expected text content in property: "
										+ propertyNode.getLocalName());
					}
				} else {
					Node propertyValueName = propertyNode.getAttributes()
							.getNamedItem("name");
					String propertyValueNameString;
					if (propertyValueName == null) {
						propertyValueNameString = propertyNode.getLocalName()
								+ String.valueOf(random.nextInt());
					} else {
						propertyValueNameString = propertyValueName
								.getNodeValue();
					}
					Resource propertyValue = node.getModel().createResource(
							targetNamespace + propertyValueNameString);
					if (!propertyRange.equals(XSD.xstring)) {
						propertyValue.addProperty(RDF.type, propertyRange);
					}
					propertyValue.addProperty(RDF.type, OWL2.NamedIndividual);
					node.addProperty(property, propertyValue);

					processPropertiesElement(propertyValue, propertyNode,
							targetNamespace);
				}
			}
		}
	}

	private static Resource getPropertyRange(Resource property) {
		Resource propertyRange;
		try {
			propertyRange = property.getRequiredProperty(RDFS.range)
					.getResource();
		} catch (final PropertyNotFoundException e) {
			LOG.log(Level.WARNING,
					"No property range for property "
							+ property.getURI()
							+ " found, storing as string if it is a datatype property.");
			propertyRange = XSD.xstring;
		}
		return propertyRange;
	}

	private static void createRelationship(
			TRelationshipTemplate relationshipTemplate, String namespace,
			Model model) throws UnsupportedException {
		Property relationship = model.createProperty(getURI(
				relationshipTemplate, namespace));
		setRelationshipType(relationshipTemplate, relationship);

		Resource source = getRelationshipSource(relationshipTemplate, model,
				namespace);
		Resource target = getRelationshipTarget(relationshipTemplate, model,
				namespace);
		source.addProperty(relationship, target);
	}

	private static Resource getRelationshipSource(
			TRelationshipTemplate relationshipTemplate, Model model,
			String namespace) throws UnsupportedException {
		Object sourceElement = relationshipTemplate.getSourceElement().getRef();
		if (sourceElement instanceof TNodeTemplate) {
			TNodeTemplate sourceNode = (TNodeTemplate) sourceElement;
			return model.getResource(getURI(sourceNode, namespace));
		} else {
			throw new UnsupportedException(
					"The source element of relationshipTemplate "
							+ relationshipTemplate.getName()
							+ " must refer to a NodeTemplate.");
		}
	}

	private static Resource getRelationshipTarget(
			TRelationshipTemplate relationshipTemplate, Model model,
			String namespace) throws UnsupportedException {
		Object targetElement = relationshipTemplate.getTargetElement().getRef();
		if (targetElement instanceof TNodeTemplate) {
			TNodeTemplate targetNode = (TNodeTemplate) targetElement;
			return model.getResource(getURI(targetNode, namespace));
		} else {
			throw new UnsupportedException(
					"The target element of a RelationshipTemplate must refer to a NodeTemplate.");
		}
	}

	private static void setRelationshipType(
			TRelationshipTemplate relationshipTemplate, Property relationship)
			throws UnsupportedException {
		final QName type = relationshipTemplate.getType();
		if (type == null) {
			throw new UnsupportedException("No type for relationshipTemplate "
					+ relationshipTemplate.getName() + " found");
		}
		final Resource relationshipType = createResourceFromQName(type,
				relationship.getModel());

		if (!relationship.equals(relationshipType)) {
			relationship.addProperty(RDF.type, relationshipType);
			relationship.addProperty(RDF.type, OWL2.NamedIndividual);
		}
	}

	private static void createStates(TNodeType nodeType, Model model)
			throws UnsupportedException {
		if (nodeType.getInstanceStates() != null) {
			for (final InstanceState instanceState : nodeType
					.getInstanceStates().getInstanceState()) {
				Resource state;
				if (isURI(instanceState.getState())) {
					state = model.createResource(instanceState.getState());
				} else {
					String namespace = getRDFNamespace(nodeType
							.getTargetNamespace());
					state = model.createResource(namespace
							+ instanceState.getState());
				}
				state.addProperty(RDF.type, Omn_lifecycle.State);
			}
		}
	}

	private static Resource createNodeType(TNodeType nodeType, Model model) {
		final String namespace = getRDFNamespace(nodeType.getTargetNamespace());
		final Resource nodeTypeResource = model.getResource(namespace
				+ nodeType.getName());
		nodeTypeResource.addProperty(RDF.type, OWL.Class);
		nodeTypeResource.addProperty(RDFS.subClassOf, Omn.Resource);
		final PropertiesDefinition propertiesDefinition = nodeType
				.getPropertiesDefinition();
		if (propertiesDefinition != null) {
			createResourceFromQName(propertiesDefinition.getElement(), model);
		}
		return nodeTypeResource;
	}

	private static void processRelationshipTypes(Definitions definitions,
			Model model) {
		for (final TExtensibleElements element : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (element instanceof TRelationshipType) {
				createRelationshipType((TRelationshipType) element, model);
			}
		}
	}

	private static void createRelationshipType(
			TRelationshipType relationshipType, Model model) {
		String namespace = getRDFNamespace(relationshipType
				.getTargetNamespace());
		Resource relationshipTypeResource = model.createResource(namespace
				+ relationshipType.getName());
		relationshipTypeResource.addProperty(RDF.type, OWL2.ObjectProperty);
		relationshipTypeResource.addProperty(RDFS.subPropertyOf, Omn.relatesTo);

		setValidSource(relationshipType, relationshipTypeResource);
		setValidTarget(relationshipType, relationshipTypeResource);
	}

	private static void setValidTarget(TRelationshipType relationshipType,
			Resource relationshipTypeResource) {
		Resource target = Omn.Resource;
		if (relationshipType.getValidTarget() != null) {
			QName validTarget = relationshipType.getValidTarget().getTypeRef();
			target = createResourceFromQName(validTarget,
					relationshipTypeResource.getModel());
		}
		relationshipTypeResource.addProperty(RDFS.range, target);
	}

	private static void setValidSource(TRelationshipType relationshipType,
			Resource relationshipTypeResource) {
		Resource source = Omn.Resource;
		if (relationshipType.getValidSource() != null) {
			QName validSource = relationshipType.getValidSource().getTypeRef();
			source = createResourceFromQName(validSource,
					relationshipTypeResource.getModel());
		}
		relationshipTypeResource.addProperty(RDFS.domain, source);
	}

	protected static String getRDFNamespace(String namespace) {
		if (!(namespace.endsWith("/") || namespace.endsWith("#"))) {
			namespace = namespace.concat("#");
		}
		return namespace;
	}

	private static Resource createResourceFromQName(final QName qname,
			final Model model) {
		createPrefixMappingFromQName(qname, model);
		String namespace = getRDFNamespace(qname.getNamespaceURI());
		return model.createResource(namespace + qname.getLocalPart());
	}

	private static void createPrefixMappingFromQName(final QName qname,
			final Model model) {
		String prefix = qname.getPrefix();
		String namespace = qname.getNamespaceURI();
		if (prefix != null && !prefix.isEmpty()) {
			if (model.getNsPrefixURI(prefix) == null) {
				model.setNsPrefix(prefix, getRDFNamespace(namespace));
			}
		}
	}

	private static String getURI(TEntityTemplate entityTemplate,
			String namespace) throws UnsupportedException {
		if (isURI(entityTemplate.getId())) {
			return entityTemplate.getId();
		} else {
			return namespace + entityTemplate.getId();
		}
	}

	private static String getURI(TServiceTemplate serviceTemplate,
			String namespace) throws UnsupportedException {
		if (isURI(serviceTemplate.getId())) {
			return serviceTemplate.getId();
		} else {
			return namespace + serviceTemplate.getId();
		}
	}

	private static boolean isURI(String id) throws UnsupportedException {
		if (id == null) {
			throw new UnsupportedException(
					"Every node, relationship and service template needs to have an id set");
		}
		return id.startsWith("http://");
	}

	public static class UnsupportedException extends Exception {

		private static final long serialVersionUID = -7910873917548269970L;

		public UnsupportedException(String message) {
			super(message);
		}
	}

}
