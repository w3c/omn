package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecTypeContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementConverter extends AbstractConverter {

	private static final String PREFIX = "http://open-multinet.info/omnlib/converter";
	private static final Logger LOG = Logger
			.getLogger(AdvertisementConverter.class.getName());

	@SuppressWarnings("rawtypes")
	public static Model getModel(InputStream input) throws JAXBException,
			InvalidModelException {

		RSpecContents rspecRequest = getRspec(input);

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model.createResource(PREFIX + "#advertisement");
		topology.addProperty(RDF.type, Omn_lifecycle.Offering);

		@SuppressWarnings("unchecked")
		List<JAXBElement<?>> rspecObjects = (List) rspecRequest
				.getAnyOrNodeOrLink();
		for (Object rspecObject : rspecObjects) {
			tryExtractNode(rspecObject, topology);
			tryExtractLink(rspecObject, topology);
		}

		return model;
	}

	private static RSpecContents getRspec(InputStream input)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();
		return request;
	}

	private static void tryExtractLink(Object rspecObject, Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<LinkContents> nodeJaxb = (JAXBElement<LinkContents>) rspecObject;
			LinkContents rspecLink = nodeJaxb.getValue();
			Resource omnLink = topology.getModel().createResource(
					rspecLink.getComponentId());
			omnLink.addProperty(RDF.type, Omn_resource.Link);
			omnLink.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, omnLink);
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractNode(Object object, Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<NodeContents> nodeJaxb = (JAXBElement<NodeContents>) object;
			NodeContents rspecNode = nodeJaxb.getValue();
			Resource omnNode = topology.getModel().createResource(
					rspecNode.getComponentId());
			omnNode.addProperty(RDF.type, Omn_resource.Node);
			omnNode.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, omnNode);
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}

	private static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.advertisement";

	public static String getRSpec(Model model) throws JAXBException,
			InvalidModelException {
		RSpecContents advertisement = new RSpecContents();
		advertisement.setType(RspecTypeContents.ADVERTISEMENT);
		advertisement.setGeneratedBy(VENDOR);
		setGeneratedTime(advertisement);

		model2rspec(model, advertisement);
		JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(advertisement);
		return toString(rspec, JAXB);
	}

	private static void model2rspec(Model model, RSpecContents manifest)
			throws InvalidModelException {
		List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		validateModel(groups);
		Resource group = groups.iterator().next();

		List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		convertStatementsToNodesAndLinks(manifest, resources);
	}

	private static void convertStatementsToNodesAndLinks(
			RSpecContents manifest, List<Statement> resources) {
		for (Statement resource : resources) {
			// @todo: check type of resource here and not only generate nodes
			NodeContents node = new NodeContents();

			setComponentDetails(resource, node);
			setComponentManagerId(resource, node);

			manifest.getAnyOrNodeOrLink().add(
					new ObjectFactory().createNode(node));
		}
	}

	private static void setComponentDetails(Statement resource,
			NodeContents node) {
		node.setComponentId(resource.getResource().getURI());
		node.setComponentName(resource.getResource().getLocalName());
		if (resource.getResource().hasProperty(Omn_resource.isExclusive))
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
	}

	private static void setComponentManagerId(Statement resource,
			NodeContents node) {
		List<Statement> implementedBy = resource.getResource()
				.listProperties(Omn_lifecycle.implementedBy).toList();
		for (Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}

	private static void setGeneratedTime(RSpecContents manifest) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
			manifest.setGenerated(xmlGrogerianCalendar);
		} catch (DatatypeConfigurationException e) {
			LOG.info(e.getMessage());
		}
	}

}
