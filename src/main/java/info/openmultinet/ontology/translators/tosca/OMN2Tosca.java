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
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class OMN2Tosca extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(OMN2Tosca.class
			.getName());

	private static ObjectFactory objFactory = new ObjectFactory();

	public static String getTopology(final Model model) throws JAXBException,
			InvalidModelException, NodeTypeNotFoundException,
			MultipleNamespacesException, RequiredResourceNotFoundException {
		final Definitions definitions = OMN2Tosca.objFactory
				.createDefinitions();

		OMN2Tosca.model2Tosca(model, definitions);

		return AbstractConverter.toString(definitions,
				"info.openmultinet.ontology.translators.tosca.jaxb");
	}

	private static void model2Tosca(final Model model,
			final Definitions definitions) throws InvalidModelException,
			NodeTypeNotFoundException, MultipleNamespacesException,
			RequiredResourceNotFoundException {
		OMN2Tosca.setTargetNamespaceAndName(definitions, model);
		OMN2Tosca.createServiceTemplates(definitions, model);
	}

	private static void createServiceTemplates(final TDefinitions definitions,
			final Model model) throws MultipleNamespacesException,
			NodeTypeNotFoundException, RequiredResourceNotFoundException {
		final List<TExtensibleElements> definitionsContent = definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();

		final Types definitionsTypes = OMN2Tosca.objFactory
				.createTDefinitionsTypes();
		definitions.setTypes(definitionsTypes);
		final List<Object> types = definitionsTypes.getAny();

		final ResIterator topologyIterator = model.listResourcesWithProperty(
				RDF.type, Omn.Topology);
		while (topologyIterator.hasNext()) {
			final Resource topologyResource = topologyIterator.next();
			OMN2Tosca.createServiceTemplate(model, definitionsContent, types,
					topologyResource);
		}
	}

	private static void createServiceTemplate(final Model model,
			final List<TExtensibleElements> definitionsContent,
			final List<Object> types, final Resource topologyResource)
			throws NodeTypeNotFoundException, RequiredResourceNotFoundException {
		final TServiceTemplate serviceTemplate = OMN2Tosca.objFactory
				.createTServiceTemplate();
		definitionsContent.add(serviceTemplate);
		serviceTemplate.setId(topologyResource.getURI());

		final TTopologyTemplate topologyTemplate = OMN2Tosca.objFactory
				.createTTopologyTemplate();
		serviceTemplate.setTopologyTemplate(topologyTemplate);

		final List<TEntityTemplate> nodesAndRelationshipTemplates = topologyTemplate
				.getNodeTemplateOrRelationshipTemplate();

		ResIterator nodeIterator = model.listResourcesWithProperty(RDF.type,
				Tosca.Node);
		while (nodeIterator.hasNext()) {
			final Resource nodeResource = nodeIterator.next();
			final Resource nodeTypeResource = OMN2Tosca
					.getNodeType(nodeResource);

			try {
				types.add(OMN2Tosca.createTypes(nodeTypeResource));
			} catch (final NoPropertiesFoundException e) {
				OMN2Tosca.LOG.log(
						Level.INFO,
						"No properties found for node type "
								+ nodeTypeResource.getURI());
			}

			definitionsContent.add(OMN2Tosca.createNodeType(nodeTypeResource));

			nodesAndRelationshipTemplates.add(OMN2Tosca.createNodeTemplate(
					nodeResource, nodeTypeResource));
		}

		nodeIterator = model.listResourcesWithProperty(RDF.type, Tosca.Node);
		while (nodeIterator.hasNext()) {
			final Resource nodeResource = nodeIterator.next();

			final List<TRelationshipTemplate> relationshipTemplates = OMN2Tosca
					.createRelationshipTemplates(nodeResource,
							nodesAndRelationshipTemplates);
			for (final TRelationshipTemplate relationshipTemplate : relationshipTemplates) {
				nodesAndRelationshipTemplates.add(relationshipTemplate);

				final TRelationshipType relationshipType = OMN2Tosca
						.createRelationshipType(relationshipTemplate, model);
				definitionsContent.add(relationshipType);
			}
		}
	}

	private static void setTargetNamespaceAndName(
			final TDefinitions definitions, final Model model)
			throws MultipleNamespacesException {
		final String targetNamespace = OMN2Tosca.getXMLNamespace(OMN2Tosca
				.getTopologiesNamespace(model));
		definitions.setTargetNamespace(targetNamespace);
		definitions.setName(targetNamespace);
	}

	private static String getXMLNamespace(final String namespace) {
		return namespace.replace("#", "/");
	}

	private static String getXMLNamespace(final Resource resource) {
		return OMN2Tosca.getXMLNamespace(resource.getNameSpace());
	}

	private static String getNSPrefix(final Resource resource) {
		final Map<String, String> prefixMap = resource.getModel()
				.getNsPrefixMap();
		for (final Map.Entry<String, String> mapping : prefixMap.entrySet()) {
			if (mapping.getValue().equals(resource.getNameSpace())) {
				return mapping.getKey();
			}
		}
		// TODO:
		return "";
	}

	private static String getTopologiesNamespace(final Model model)
			throws MultipleNamespacesException {
		final ResIterator iter = model.listResourcesWithProperty(RDF.type,
				Omn.Topology);
		String targetNamespace = "";
		while (iter.hasNext()) {
			final String namespace = iter.next().getNameSpace();
			if (!targetNamespace.isEmpty()
					&& !targetNamespace.equals(namespace)) {
				throw new MultipleNamespacesException(
						"Multiple topology namespaces are found: "
								+ targetNamespace + " and " + namespace
								+ " . This is not supported by TOSCA");
			}
			targetNamespace = namespace;
		}
		return targetNamespace;
	}

	private static Element createTypes(final Resource nodeType)
			throws NodeTypeNotFoundException, NoPropertiesFoundException {
		final Document types = OMN2Tosca.createDocument();

		final Element schema = types.createElement("xs:schema");
		schema.setAttribute("xmlns:xs", "http://www.w3.org/2001/XMLSchema");
		schema.setAttribute("elementFormDefault", "qualified");
		schema.setAttribute("attributeFormDefault", "unqualified");
		schema.setAttribute("targetNamespace",
				OMN2Tosca.getXMLNamespace(nodeType));
		types.appendChild(schema);

		final Element element = types.createElement("xs:element");
		schema.appendChild(element);
		element.setAttribute("name",
				OMN2Tosca.getNodeTypePropertiesName(nodeType));

		final Element complexType = types.createElement("xs:complexType");
		element.appendChild(complexType);

		final Element sequence = types.createElement("xs:sequence");
		complexType.appendChild(sequence);

		final StmtIterator propertiesIterator = nodeType.getModel()
				.listStatements(null, RDFS.domain, nodeType);
		if (!propertiesIterator.hasNext()) {
			throw new NoPropertiesFoundException();
		}
		while (propertiesIterator.hasNext()) {
			final Resource property = propertiesIterator.next().getSubject();
			final Element type = OMN2Tosca.createType(types, property);
			sequence.appendChild(type);
		}

		return types.getDocumentElement();
	}

	private static Element createType(final Document types,
			final Resource property) {
		final Element type = types.createElement("xs:element");
		type.setAttribute("name", property.getLocalName());
		final Resource range = property.getRequiredProperty(RDFS.range)
				.getResource();

		if (range.getNameSpace().equals("http://www.w3.org/2001/XMLSchema#")) {
			type.setAttribute("type", "xs:" + range.getLocalName());
		} else {
			type.setAttribute("type", range.getURI());
		}
		return type;
	}

	private static Document createDocument() {
		final DocumentBuilderFactory docFactory = DocumentBuilderFactory
				.newInstance();
		DocumentBuilder docBuilder = null;
		try {
			docBuilder = docFactory.newDocumentBuilder();
		} catch (final ParserConfigurationException e) {
			e.printStackTrace();
		}
		return docBuilder.newDocument();
	}

	private static TNodeType createNodeType(final Resource nodeTypeResource) {
		final TNodeType nodeType = OMN2Tosca.objFactory.createTNodeType();
		OMN2Tosca.setName(nodeTypeResource, nodeType);
		OMN2Tosca.setNodeTypeProperties(nodeTypeResource, nodeType);
		OMN2Tosca.setInstanceStates(nodeTypeResource, nodeType);
		return nodeType;
	}

	private static void setName(final Resource nodeTypeResource,
			final TNodeType nodeType) {
		nodeType.setName(nodeTypeResource.getLocalName());
		nodeType.setTargetNamespace(OMN2Tosca.getXMLNamespace(nodeTypeResource));
	}

	private static void setNodeTypeProperties(final Resource nodeTypeResource,
			final TNodeType nodeType) {
		final PropertiesDefinition nodeTypeProperties = OMN2Tosca.objFactory
				.createTEntityTypePropertiesDefinition();
		final String nodeTypeNameSpace = OMN2Tosca
				.getXMLNamespace(nodeTypeResource);
		final String nodeTypePrefix = OMN2Tosca.getNSPrefix(nodeTypeResource);
		final QName propertiesReference = new QName(nodeTypeNameSpace,
				OMN2Tosca.getNodeTypePropertiesName(nodeTypeResource),
				nodeTypePrefix);
		nodeTypeProperties.setElement(propertiesReference);
		nodeType.setPropertiesDefinition(nodeTypeProperties);
	}

	private static void setInstanceStates(final Resource nodeTypeResource,
			final TNodeType nodeType) {
		final TTopologyElementInstanceStates instanceStates = OMN2Tosca.objFactory
				.createTTopologyElementInstanceStates();

		final StmtIterator stateIterator = nodeTypeResource.getModel()
				.listStatements(null, RDFS.subClassOf, Tosca.State);
		while (stateIterator.hasNext()) {
			final Resource state = stateIterator.next().getSubject();
			if (!state.equals(Tosca.State)) {
				final InstanceState instanceState = OMN2Tosca.objFactory
						.createTTopologyElementInstanceStatesInstanceState();
				instanceState.setState(state.getLocalName());
				instanceStates.getInstanceState().add(instanceState);
			}
		}
		nodeType.setInstanceStates(instanceStates);
	}

	private static String getNodeTypePropertiesName(
			final Resource nodeTypeResource) {
		return nodeTypeResource.getLocalName() + "Properties";
	}

	private static TNodeTemplate createNodeTemplate(final Resource node,
			final Resource nodeTypeResource) throws NodeTypeNotFoundException {
		final TNodeTemplate nodeTemplate = OMN2Tosca.objFactory
				.createTNodeTemplate();
		OMN2Tosca.setNameAndTypeAndID(node, nodeTypeResource, nodeTemplate);

		try {
			final Properties properties = OMN2Tosca.objFactory
					.createTEntityTemplateProperties();
			final Element nodeProperties = OMN2Tosca.createNodeProperties(node,
					nodeTypeResource, nodeTemplate);
			properties.setAny(nodeProperties);
			nodeTemplate.setProperties(properties);
		} catch (final NoPropertiesFoundException e) {
			OMN2Tosca.LOG.log(Level.INFO, "No properties found for node "
					+ node.getURI());
		}

		return nodeTemplate;
	}

	private static void setNameAndTypeAndID(final Resource node,
			final Resource nodeTypeResource, final TNodeTemplate nodeTemplate) {
		nodeTemplate.setName(node.getLocalName());
		final String nodeTypeNameSpace = OMN2Tosca
				.getXMLNamespace(nodeTypeResource);
		final String nodeTypePrefix = OMN2Tosca.getNSPrefix(nodeTypeResource);
		final QName type = new QName(nodeTypeNameSpace,
				nodeTypeResource.getLocalName(), nodeTypePrefix);
		nodeTemplate.setId(node.getURI());
		nodeTemplate.setType(type);
	}

	private static Element createNodeProperties(final Resource node,
			final Resource nodeTypeResource, final TNodeTemplate nodeTemplate)
			throws NodeTypeNotFoundException, NoPropertiesFoundException {
		final Document doc = OMN2Tosca.createDocument();

		final String nodeTypeNamespace = OMN2Tosca
				.getXMLNamespace(nodeTypeResource);
		final String nodeTypePrefix = OMN2Tosca.getNSPrefix(nodeTypeResource);
		final Element nodeProperties = doc
				.createElementNS(nodeTypeNamespace, nodeTypePrefix + ":"
						+ OMN2Tosca.getNodeTypePropertiesName(nodeTypeResource));
		doc.appendChild(nodeProperties);

		final StmtIterator propertiesIterator = node.listProperties();
		while (propertiesIterator.hasNext()) {
			final Statement propertyStatement = propertiesIterator.next();
			if (propertyStatement.getPredicate().hasProperty(RDFS.domain,
					nodeTypeResource)) {
				final Element parameter = doc.createElementNS(
						nodeTypeNamespace, nodeTypePrefix
								+ ":"
								+ propertyStatement.getPredicate()
										.getLocalName());
				parameter.setTextContent(propertyStatement.getLiteral()
						.getString());
				nodeProperties.appendChild(parameter);
			}
		}
		if (0 == nodeProperties.getChildNodes().getLength()) {
			throw new NoPropertiesFoundException();
		}
		return doc.getDocumentElement();
	}

	private static Resource getNodeType(final Resource node)
			throws NodeTypeNotFoundException {
		final StmtIterator propertiesIterator = node.listProperties(RDF.type);
		while (propertiesIterator.hasNext()) {
			final Resource nodeTypeResource = propertiesIterator.next()
					.getResource();
			if (nodeTypeResource.hasProperty(RDFS.subClassOf, Tosca.Node)
					&& !nodeTypeResource.equals(Tosca.Node)) {
				return nodeTypeResource;
			}
		}
		throw new NodeTypeNotFoundException("no node type found for: "
				+ node.getURI());
	}

	private static List<TRelationshipTemplate> createRelationshipTemplates(
			final Resource nodeResource,
			final List<TEntityTemplate> nodesAndRelationshipTemplates)
			throws RequiredResourceNotFoundException {
		final List<TRelationshipTemplate> relationshipTemplates = new ArrayList<>();

		final StmtIterator relationIterator = nodeResource.listProperties();
		while (relationIterator.hasNext()) {
			final Statement relationStatement = relationIterator.next();
			final Property relation = relationStatement.getPredicate();

			final StmtIterator relationTypeIterator = relation
					.listProperties(RDF.type);
			while (relationTypeIterator.hasNext()) {
				final Resource relationType = relationTypeIterator.next()
						.getResource();
				if (relationType.hasProperty(RDFS.subPropertyOf,
						Tosca.relatesTo)) {
					relationshipTemplates
							.add(OMN2Tosca
									.createRelationshipTemplate(
											relationStatement,
											nodesAndRelationshipTemplates,
											relationType));
				}
			}
		}
		return relationshipTemplates;
	}

	private static TRelationshipTemplate createRelationshipTemplate(
			final Statement relationStatement,
			final List<TEntityTemplate> nodesAndRelationshipTemplates,
			final Resource relationType)
			throws RequiredResourceNotFoundException {
		final TRelationshipTemplate relationshipTemplate = OMN2Tosca.objFactory
				.createTRelationshipTemplate();

		relationshipTemplate.setId(relationStatement.getPredicate().getURI());
		relationshipTemplate.setName(relationStatement.getPredicate()
				.getLocalName());

		OMN2Tosca.setType(relationshipTemplate, relationType);

		final TRelationshipTemplate.SourceElement sourceElement = OMN2Tosca.objFactory
				.createTRelationshipTemplateSourceElement();
		final TNodeTemplate sourceNode = OMN2Tosca.getNodeTemplateByID(
				relationStatement.getSubject().getURI(),
				nodesAndRelationshipTemplates);
		sourceElement.setRef(sourceNode);
		relationshipTemplate.setSourceElement(sourceElement);

		final TRelationshipTemplate.TargetElement targetElement = OMN2Tosca.objFactory
				.createTRelationshipTemplateTargetElement();
		final TNodeTemplate targetNode = OMN2Tosca.getNodeTemplateByID(
				relationStatement.getResource().getURI(),
				nodesAndRelationshipTemplates);
		targetElement.setRef(targetNode);
		relationshipTemplate.setTargetElement(targetElement);

		return relationshipTemplate;
	}

	private static void setType(
			final TRelationshipTemplate relationshipTemplate,
			final Resource relationType) {
		final String namespace = OMN2Tosca.getXMLNamespace(relationType);
		final String prefix = OMN2Tosca.getNSPrefix(relationType);
		final QName type = new QName(namespace, relationType.getLocalName(),
				prefix);
		relationshipTemplate.setType(type);
	}

	private static TRelationshipType createRelationshipType(
			final TRelationshipTemplate relationshipTemplate, final Model model) {
		final TRelationshipType relationshipType = OMN2Tosca.objFactory
				.createTRelationshipType();

		final QName type = relationshipTemplate.getType();
		relationshipType.setName(type.getLocalPart());
		relationshipType.setTargetNamespace(type.getNamespaceURI());

		final Resource relationshipTypeResource = model.getResource(type
				.getNamespaceURI() + type.getLocalPart());
		OMN2Tosca.setValidSource(relationshipType, relationshipTypeResource);
		OMN2Tosca.setValidTarget(relationshipType, relationshipTypeResource);
		return relationshipType;
	}

	private static void setValidSource(
			final TRelationshipType relationshipType,
			final Resource relationshipTypeResource) {
		final StmtIterator rangeIter = relationshipTypeResource
				.listProperties(RDFS.domain);
		while (rangeIter.hasNext()) {
			final Resource target = rangeIter.next().getResource();
			if (target.hasProperty(RDFS.subClassOf, Tosca.Node)
					&& !target.equals(Tosca.Node)) {
				final String namespace = OMN2Tosca.getXMLNamespace(target);
				final String prefix = OMN2Tosca.getNSPrefix(target);
				final QName typeRef = new QName(namespace,
						target.getLocalName(), prefix);

				final TRelationshipType.ValidSource validSource = OMN2Tosca.objFactory
						.createTRelationshipTypeValidSource();
				validSource.setTypeRef(typeRef);
				relationshipType.setValidSource(validSource);
			}
		}
	}

	private static void setValidTarget(
			final TRelationshipType relationshipType,
			final Resource relationshipTypeResource) {
		final StmtIterator rangeIter = relationshipTypeResource
				.listProperties(RDFS.range);
		while (rangeIter.hasNext()) {
			final Resource target = rangeIter.next().getResource();
			if (target.hasProperty(RDFS.subClassOf, Tosca.Node)
					&& !target.equals(Tosca.Node)) {
				final String namespace = OMN2Tosca.getXMLNamespace(target);
				final String prefix = OMN2Tosca.getNSPrefix(target);
				final QName typeRef = new QName(namespace,
						target.getLocalName(), prefix);

				final TRelationshipType.ValidTarget validTarget = OMN2Tosca.objFactory
						.createTRelationshipTypeValidTarget();
				validTarget.setTypeRef(typeRef);
				relationshipType.setValidTarget(validTarget);
			}
		}
	}

	private static TNodeTemplate getNodeTemplateByID(final String id,
			final List<TEntityTemplate> nodesAndRelationshipTemplates)
			throws RequiredResourceNotFoundException {
		for (final TEntityTemplate entitiyTemplate : nodesAndRelationshipTemplates) {
			if (entitiyTemplate instanceof TNodeTemplate) {
				if (id.equals(entitiyTemplate.getId())) {
					return (TNodeTemplate) entitiyTemplate;
				}
			}
		}
		throw new RequiredResourceNotFoundException(
				"The relationship source or target element with id " + id
						+ " was not found");
	}

	public static class RequiredResourceNotFoundException extends Exception {

		private static final long serialVersionUID = 3219300357589016712L;

		public RequiredResourceNotFoundException(final String message) {
			super(message);
		}
	}

	public static class NodeTypeNotFoundException extends Exception {

		private static final long serialVersionUID = -6079715571448444400L;

		public NodeTypeNotFoundException(final String message) {
			super(message);
		}
	}

	public static class MultipleNamespacesException extends Exception {

		private static final long serialVersionUID = -6296855743962011943L;

		public MultipleNamespacesException(final String message) {
			super(message);
		}
	}

	public static class NoPropertiesFoundException extends Exception {

		private static final long serialVersionUID = -4379252875775867346L;

		public NoPropertiesFoundException() {
			super();
		}
	}

}
