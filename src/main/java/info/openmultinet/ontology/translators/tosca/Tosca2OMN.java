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

	private static final Logger LOG = Logger.getLogger(Tosca2OMN.class
			.getName());

	public static Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException {
		final JAXBContext context = JAXBContext.newInstance(Definitions.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final Definitions definitions = unmarshaller.unmarshal(
				new StreamSource(input), Definitions.class).getValue();
		final Model model = Tosca2OMN.tosca2Model(definitions);

		// TODO: create infModel? result will be huge..
		// Parser parser = new Parser(model);
		// InfModel infModel = parser.getModel();

		Parser.setCommonPrefixes(model);
		return model;
	}

	private static Model tosca2Model(final Definitions definitions) {
		final Model model = ModelFactory.createDefaultModel();

		Tosca2OMN.processTypes(definitions, model);
		Tosca2OMN.processNodeTypes(definitions, model);
		Tosca2OMN.processTemplates(definitions, model);

		return model;
	}

	private static void processTypes(final Definitions definitions,
			final Model model) {
		final List<Object> types = definitions.getTypes().getAny();
		for (final Object schema : types) {
			if (schema instanceof Element) {
				final Element schemaElement = (Element) schema;
				if (Tosca2OMN.isXMLSchemaElement(schemaElement)) {
					final String namespace = schemaElement
							.getAttribute("targetNamespace");
					for (int i = 0; i < (schemaElement.getChildNodes()
							.getLength() - 1); i++) {
						final Node elementNode = schemaElement.getChildNodes()
								.item(i);
						final String superPropertyName = elementNode
								.getAttributes().getNamedItem("name")
								.getNodeValue();
						final Property superProperty = model
								.createProperty(namespace + superPropertyName);
						for (int j = 0; j < (schemaElement.getChildNodes()
								.getLength() - 1); j++) {
							final Node typesNode = elementNode.getChildNodes()
									.item(j);
							if (typesNode.getLocalName().equals("complexType")) {
								for (int k = 0; k < (schemaElement
										.getChildNodes().getLength() - 1); k++) {
									final Node sequenceNode = typesNode
											.getChildNodes().item(k);
									if (sequenceNode.getLocalName().equals(
											"sequence")) {
										for (int l = 0; l < (sequenceNode
												.getChildNodes().getLength() - 1); l++) {
											final Node typeNode = sequenceNode
													.getChildNodes().item(l);
											if (typeNode.getLocalName().equals(
													"element")) {
												final String name = typeNode
														.getAttributes()
														.getNamedItem("name")
														.getNodeValue();
												final String type = typeNode
														.getAttributes()
														.getNamedItem("type")
														.getNodeValue();
												final Property property = model
														.createProperty(namespace
																+ name);
												property.addProperty(
														RDFS.subPropertyOf,
														superProperty);
												property.addProperty(
														RDFS.range, Tosca2OMN
																.getXSDType(
																		type,
																		model));
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

	private static Resource getXSDType(final String type, final Model model) {
		switch (type) {
		case "xs:string":
			return XSD.xstring;
		case "xs:integer":
			return XSD.xint;
		}
		return model.createResource(type);
	}

	private static boolean isXMLSchemaElement(final Element element) {
		return element.getNamespaceURI().equals(
				"http://www.w3.org/2001/XMLSchema")
				&& element.getLocalName().equals("schema");
	}

	private static void processNodeTypes(final Definitions definitions,
			final Model model) {
		for (final TExtensibleElements templateOrNodeType : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (templateOrNodeType instanceof TNodeType) {
				Tosca2OMN.createServiceType((TNodeType) templateOrNodeType,
						model);
				Tosca2OMN.createStates((TNodeType) templateOrNodeType, model);
			}
		}
	}

	private static void processTemplates(final Definitions definitions,
			final Model model) {
		for (final TExtensibleElements templateOrNodeType : definitions
				.getServiceTemplateOrNodeTypeOrNodeTypeImplementation()) {
			if (templateOrNodeType instanceof TServiceTemplate) {
				Tosca2OMN.processServiceTemplate(
						(TServiceTemplate) templateOrNodeType, definitions,
						model);
			}
		}
	}

	private static void processServiceTemplate(
			final TServiceTemplate serviceTemplate,
			final Definitions definitions, final Model model) {
		final TTopologyTemplate topologyTemplate = serviceTemplate
				.getTopologyTemplate();
		for (final TEntityTemplate entityTemplate : topologyTemplate
				.getNodeTemplateOrRelationshipTemplate()) {
			if (entityTemplate instanceof TNodeTemplate) {
				Tosca2OMN.createService((TNodeTemplate) entityTemplate,
						definitions, model);
			} else if (entityTemplate instanceof TRelationshipTemplate) {
				// TODO process relationship templates
			}
		}
	}

	private static Resource createService(final TNodeTemplate nodeTemplate,
			final Definitions definitions, final Model model) {
		final String namespace = definitions.getTargetNamespace();
		final Resource service = model.createResource(namespace
				+ nodeTemplate.getName());
		Tosca2OMN.setServiceType(nodeTemplate, service, model);
		Tosca2OMN.setServiceProperties(nodeTemplate, service, model);
		return service;
	}

	private static void setServiceProperties(final TNodeTemplate nodeTemplate,
			final Resource service, final Model model) {
		final Object properties = nodeTemplate.getProperties().getAny();
		if (properties instanceof Element) {
			final Element propertiesElement = (Element) properties;
			for (int i = 0; i < (propertiesElement.getChildNodes().getLength() - 1); i++) {
				final Node propertyNode = propertiesElement.getChildNodes()
						.item(i);
				final String namespace = propertyNode.getNamespaceURI();
				final Property property = model.getProperty(namespace
						+ propertyNode.getLocalName());
				Resource propertyRange;
				try {
					propertyRange = property.getRequiredProperty(RDFS.range)
							.getResource();
				} catch (final PropertyNotFoundException e) {
					Tosca2OMN.LOG.log(
							Level.INFO,
							"No property range for property "
									+ property.getURI()
									+ " found, storing as string.");
					propertyRange = XSD.xstring;
				}
				final Literal literal = model.createTypedLiteral(
						propertyNode.getTextContent(), propertyRange.getURI());
				service.addLiteral(property, literal);
			}
		} else {
			// TODO
		}
	}

	private static void setServiceType(final TNodeTemplate nodeTemplate,
			final Resource service, final Model model) {
		final QName type = nodeTemplate.getType();
		final Resource serviceType = Tosca2OMN.createResourceFromQName(type,
				model);
		service.addProperty(RDF.type, serviceType);
	}

	private static void createStates(final TNodeType nodeType, final Model model) {
		final String namespace = nodeType.getTargetNamespace();
		for (final InstanceState instanceState : nodeType.getInstanceStates()
				.getInstanceState()) {
			final Resource state = model.createResource(namespace
					+ instanceState.getState());
			state.addProperty(RDF.type, Tosca.State);
		}
	}

	private static Resource createServiceType(final TNodeType nodeType,
			final Model model) {
		final String namespace = nodeType.getTargetNamespace();
		final Resource serviceType = model.getResource(namespace
				+ nodeType.getName());
		serviceType.addProperty(RDFS.subClassOf, Tosca.Node);
		final PropertiesDefinition propertiesDefinition = nodeType
				.getPropertiesDefinition();
		final Resource properties = Tosca2OMN.createResourceFromQName(
				propertiesDefinition.getElement(), model);
		properties.addProperty(RDFS.domain, serviceType);
		return serviceType;
	}

	private static Resource createResourceFromQName(final QName qname,
			final Model model) {
		return model.createResource(qname.getNamespaceURI()
				+ qname.getLocalPart());
	}

}
