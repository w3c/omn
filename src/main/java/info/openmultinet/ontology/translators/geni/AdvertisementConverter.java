package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecTypeContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_federation;
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
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementConverter extends AbstractConverter {

	private static final String PREFIX = "http://open-multinet.info/omnlib/converter";
	private static final Logger LOG = Logger
			.getLogger(AdvertisementConverter.class.getName());

	@SuppressWarnings("rawtypes")
	public static Model getModel(final InputStream input) throws JAXBException,
	InvalidModelException {

		final RSpecContents rspecRequest = AdvertisementConverter
				.getRspec(input);

		final Model model = ModelFactory.createDefaultModel();
		final Resource topology = model
				.createResource(AdvertisementConverter.PREFIX
						+ "#advertisement");
		topology.addProperty(RDF.type, Omn_lifecycle.Offering);

		@SuppressWarnings("unchecked")
		final List<JAXBElement<?>> rspecObjects = (List) rspecRequest
		.getAnyOrNodeOrLink();
		for (final Object rspecObject : rspecObjects) {
			AdvertisementConverter.tryExtractNode(rspecObject, topology);
			AdvertisementConverter.tryExtractLink(rspecObject, topology);
		}

		return model;
	}

	private static RSpecContents getRspec(final InputStream input)
			throws JAXBException {
		final JAXBContext context = JAXBContext
				.newInstance(RSpecContents.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		final RSpecContents request = rspec.getValue();
		return request;
	}

	private static void tryExtractLink(final Object rspecObject,
			final Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<LinkContents> nodeJaxb = (JAXBElement<LinkContents>) rspecObject;
			final LinkContents rspecLink = nodeJaxb.getValue();
			final Resource omnLink = topology.getModel().createResource(
					rspecLink.getComponentId());
			omnLink.addProperty(RDF.type, Omn_resource.Link);
			omnLink.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, omnLink);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractNode(final Object object,
			final Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<NodeContents> nodeJaxb = (JAXBElement<NodeContents>) object;
			final NodeContents rspecNode = nodeJaxb.getValue();
			final Resource omnNode = topology.getModel().createResource(
					rspecNode.getComponentId());
			
			omnNode.addProperty(RDF.type, Omn_resource.Node);
			omnNode.addProperty(Omn.isResourceOf, topology);
			
			for (Object rspecNodeObject : rspecNode.getAnyOrRelationOrLocation()) {
				tryExtractHardwareType(rspecNodeObject, omnNode);
				tryExtractSliverType(rspecNodeObject, omnNode);
			}
			
			topology.addProperty(Omn.hasResource, omnNode);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractHardwareType(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<HardwareTypeContents> hwJaxb = (JAXBElement<HardwareTypeContents>) rspecNodeObject;
			final HardwareTypeContents hw = hwJaxb.getValue();
			
			RDFNode type = ResourceFactory.createProperty(hw.getName());
			omnNode.addProperty(RDF.type, type);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractSliverType(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<SliverType> sliverJaxb = (JAXBElement<SliverType>) rspecNodeObject;
			SliverType sliver = sliverJaxb.getValue();
			
			RDFNode type = ResourceFactory.createProperty(sliver.getName());
			omnNode.addProperty(Omn_lifecycle.parentOf, type);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.advertisement";

	public static String getRSpec(final Model model) throws JAXBException,
	InvalidModelException {
		final RSpecContents advertisement = new RSpecContents();
		advertisement.setType(RspecTypeContents.ADVERTISEMENT);
		advertisement.setGeneratedBy(AbstractConverter.VENDOR);
		AdvertisementConverter.setTimeInformation(advertisement);
		
		AdvertisementConverter.model2rspec(model, advertisement);
		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
		.createRspec(advertisement);
		
		return AbstractConverter.toString(rspec, AdvertisementConverter.JAXB);
	}

	private static void model2rspec(final Model model,
			final RSpecContents manifest) throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Offering).toList();
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();

		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		AdvertisementConverter.convertStatementsToNodesAndLinks(manifest,
				resources);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents manifest, final List<Statement> omnResources) {
		for (final Statement omnResource : omnResources) {
			// @todo: check type of resource here and not only generate nodes
			final NodeContents geniNode = new NodeContents();

			AdvertisementConverter.setComponentDetails(omnResource, geniNode);
			AdvertisementConverter.setComponentManagerId(omnResource, geniNode);
			AdvertisementConverter.setHardwareTypes(omnResource, geniNode);
			AdvertisementConverter.setSliverTypes(omnResource, geniNode);
			
			ResIterator infrastructures = omnResource.getModel().listResourcesWithProperty(Omn.isResourceOf, Omn_federation.Infrastructure);
			if (infrastructures.hasNext()) {
				Resource infrastructure = infrastructures.next();
				geniNode.setComponentManagerId(infrastructure.getURI());
			}			

			manifest.getAnyOrNodeOrLink().add(
					new ObjectFactory().createNode(geniNode));
		}
	}

	private static void setSliverTypes(Statement omnResource,
			NodeContents geniNode) {
		
		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();
		
		StmtIterator parentOf = omnResource.getResource().listProperties(Omn_lifecycle.parentOf);
		ObjectFactory of = new ObjectFactory();
		SliverType sliver;
		
		while (parentOf.hasNext()) {
			String parentURI = parentOf.next().getResource().getURI();
			sliver = of.createNodeContentsSliverType();
			sliver.setName(parentURI);
			if (null != parentURI)
				geniNodeDetails.add(of.createNodeContentsSliverType(sliver));
		}
	}

	private static void setHardwareTypes(Statement omnResource,
			NodeContents geniNode) {
		
		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();
		
		StmtIterator types = omnResource.getResource().listProperties(RDF.type);
		ObjectFactory of = new ObjectFactory();
		HardwareTypeContents hwType;
		
		while (types.hasNext()) {
			String rdfType = types.next().getResource().getURI();
			hwType = of.createHardwareTypeContents();
			hwType.setName(rdfType);
			if (null != rdfType)
				geniNodeDetails.add(of.createHardwareType(hwType));
		}
	}

	private static void setComponentDetails(final Statement resource,
			final NodeContents node) {
		node.setComponentId(resource.getResource().getURI());
		node.setComponentName(resource.getResource().getLocalName());
		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}
	}

	private static void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		final List<Statement> implementedBy = resource.getResource()
				.listProperties(Omn_lifecycle.implementedBy).toList();
		for (final Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}

	private static void setTimeInformation(final RSpecContents manifest) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar = null;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);			
		} catch (final DatatypeConfigurationException e) {
			AdvertisementConverter.LOG.info(e.getMessage());
		}
		manifest.setGenerated(xmlGrogerianCalendar);
		manifest.setExpires(xmlGrogerianCalendar);
	}


}
