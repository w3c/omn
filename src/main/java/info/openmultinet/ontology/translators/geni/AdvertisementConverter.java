package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AvailableContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType.DiskImage;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliceInfo;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.commons.io.input.CloseShieldInputStream;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AdvertisementConverter extends AbstractConverter {

	private static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.advertisement";
	private static final String PREFIX = "http://open-multinet.info/omnlib/converter";
	private static final Logger LOG = Logger
			.getLogger(AdvertisementConverter.class.getName());
	private Model model;
	private JAXBContext context;
	private Unmarshaller unmarshaller;
	private ObjectFactory of;
	private XMLInputFactory xmlif;

	public AdvertisementConverter() throws JAXBException {
		super();
		this.model = ModelFactory.createDefaultModel();
		this.context = JAXBContext.newInstance(RSpecContents.class);
		this.unmarshaller = context.createUnmarshaller();
		this.of = new ObjectFactory();
		this.xmlif = XMLInputFactory.newInstance();
	}

	@SuppressWarnings("rawtypes")
	public Model getModel(RSpecContents rspec)
			throws MissingRspecElementException {

		final Resource offering = model.createResource(
				AdvertisementConverter.PREFIX + "#advertisement").addProperty(
				RDF.type, Omn_lifecycle.Offering);

		@SuppressWarnings("unchecked")
		final List<JAXBElement<?>> rspecObjects = (List) rspec
				.getAnyOrNodeOrLink();

		for (final Object rspecObject : rspecObjects) {
			tryExtractNode(rspecObject, offering);
			tryExtractLink(rspecObject, offering);
		}

		return model;
	}

	public Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException, XMLStreamException,
			MissingRspecElementException {

		final RSpecContents rspecRequest = getRspec(input);

		return getModel(rspecRequest);
	}

	// @fixme: expensive/long running method call
	public RSpecContents getRspec(final InputStream input)
			throws JAXBException, XMLStreamException {

		XMLStreamReader xmler = xmlif.createXMLStreamReader(input);

		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(xmler,
				RSpecContents.class);
		final RSpecContents request = rspec.getValue();
		return request;
	}

	private void tryExtractLink(final Object rspecObject,
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

	private void tryExtractNode(final Object object, final Resource topology)
			throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<NodeContents> nodeJaxb = (JAXBElement<NodeContents>) object;
			final NodeContents rspecNode = nodeJaxb.getValue();

			// String componentId =
			// AbstractConverter.generateUrlFromUrn(rspecNode
			// .getComponentId());
			String componentId = rspecNode.getComponentId();
			final Resource omnNode = topology.getModel().createResource(
					componentId);

			omnNode.addProperty(RDF.type, Omn_resource.Node);
			omnNode.addProperty(Omn.isResourceOf, topology);

			if (rspecNode.getComponentManagerId() != null) {
				RDFNode parent = ResourceFactory.createResource(rspecNode
						.getComponentManagerId());
				omnNode.addProperty(Omn_lifecycle.parentOf, parent);
			}

			topology.getModel().addLiteral(omnNode, Omn_resource.isExclusive,
					rspecNode.isExclusive());

			if (rspecNode.getComponentName() != null) {
				omnNode.addLiteral(RDFS.label, rspecNode.getComponentName());
			}

			for (Object rspecNodeObject : rspecNode
					.getAnyOrRelationOrLocation()) {
				tryExtractHardwareType(rspecNodeObject, omnNode);
				tryExtractSliverType(rspecNodeObject, omnNode);
				tryExtractLocation(rspecNodeObject, omnNode);
				tryExtractAvailability(rspecNodeObject, omnNode);
				tryExtractMonitoring(rspecNodeObject, omnNode);

			}

			topology.addProperty(Omn.hasResource, omnNode);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractEmulabNodeType(Object rspecHwObject, Resource omnHw) {
		try {
			@SuppressWarnings("unchecked")
			NodeType nodeType = (NodeType) rspecHwObject;

			String nodeTypeSlots = nodeType.getTypeSlots();

			omnHw.addProperty(Omn_domain_pc.hasEmulabNodeTypeSlots,
					nodeTypeSlots);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractMonitoring(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			Monitoring monitor = (Monitoring) rspecNodeObject;
			Resource monitoringResource = model.createResource(UUID
					.randomUUID().toString());
			if (monitor.getUri() != null && monitor.getUri() != "") {
				monitoringResource.addProperty(Omn_service.hasURI,
						monitor.getUri());
			}
			if (monitor.getType() != null && monitor.getType() != "") {
				monitoringResource.addProperty(RDF.type, monitor.getType());
				monitoringResource.addProperty(RDFS.label,
						AbstractConverter.getName(monitor.getType()));
			}
			omnNode.addProperty(Omn_lifecycle.usesService, monitoringResource);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractAvailability(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<AvailableContents> availablityJaxb = (JAXBElement<AvailableContents>) rspecNodeObject;
			final AvailableContents availability = availablityJaxb.getValue();

			omnNode.addLiteral(Omn_resource.isAvailable, availability.isNow());
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractLocation(Object rspecNodeObject, Resource omnNode)
			throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<LocationContents> locationJaxb = (JAXBElement<LocationContents>) rspecNodeObject;
			final LocationContents location = locationJaxb.getValue();

			if (location != null) {
				String latitude = location.getLatitude();
				String longitude = location.getLongitude();
				String country = location.getCountry();

				// country is required, when location is specified
				if (country == null) {
					throw new MissingRspecElementException(
							"LocationContents > country");
				} else {
					omnNode.addProperty(Omn_resource.country, country);
				}

				if (latitude != null) {
					omnNode.addProperty(Geo.lat, latitude);
				}
				if (longitude != null) {
					omnNode.addProperty(Geo.long_, longitude);
				}
			}
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractHardwareType(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<HardwareTypeContents> hwJaxb = (JAXBElement<HardwareTypeContents>) rspecNodeObject;
			final HardwareTypeContents hw = hwJaxb.getValue();

			final Resource omnHw = omnNode.getModel().createResource();
			RDFNode type = ResourceFactory.createProperty(hw.getName());

			// TODO: get rid of this line
			omnNode.addProperty(RDF.type, type);

			omnHw.addProperty(RDFS.label, type.toString());
			omnHw.addProperty(RDF.type, Omn_domain_pc.HardwareType);
			for (Object hwObject : hw.getAny()) {
				tryExtractEmulabNodeType(hwObject, omnHw);
			}
			omnNode.addProperty(Omn_domain_pc.hasHardwareType, omnHw);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		} catch (final InvalidPropertyURIException e) {
			AdvertisementConverter.LOG.info(e.getMessage());
		}
	}

	private void tryExtractSliverType(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<SliverType> sliverJaxb = (JAXBElement<SliverType>) rspecNodeObject;
			final SliverType sliver = sliverJaxb.getValue();

			final Resource omnSliver = omnNode.getModel().createResource(
					sliver.getName());
			RDFNode type = ResourceFactory.createProperty(sliver.getName());
			omnNode.addProperty(Omn_lifecycle.canImplement, type);
			for (Object rspecSliverObject : sliver.getAnyOrDiskImage()) {
				tryExtractCpus(rspecSliverObject, omnSliver);
				tryExtractDiskImage(rspecSliverObject, omnSliver);
			}

			// RDFNode type = ResourceFactory.createProperty(sliver.getName());
			omnNode.addProperty(Omn_lifecycle.canImplement, type);
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractCpus(Object rspecSliverObject, Resource omnSliver) {
		if (rspecSliverObject
				.getClass()
				.equals(info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc.class)) {
			Pc pc = (Pc) rspecSliverObject;

			if (pc.getCpus() != null) {
				omnSliver.addLiteral(Omn_domain_pc.hasCPU, pc.getCpus());
			}
		}
	}

	private void tryExtractDiskImage(Object rspecSliverObject,
			Resource omnSliver) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<DiskImageContents> diJaxb = (JAXBElement<DiskImageContents>) rspecSliverObject;
			final DiskImageContents diskImageContents = diJaxb.getValue();

			String diskImageURL = diskImageContents.getUrl();
			Resource diskImage = model.createResource(diskImageURL);
			diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);

			// add name info
			String name = diskImageContents.getName();
			diskImage.addLiteral(Omn_domain_pc.hasDiskimageLabel, name);
			omnSliver.addProperty(Omn_lifecycle.canImplement, diskImage);

			String os = diskImageContents.getOs();
			if (os != null) {
				diskImage.addLiteral(Omn_domain_pc.hasDiskimageOS, os);
			}

			String version = diskImageContents.getVersion();
			if (version != null) {
				diskImage
						.addLiteral(Omn_domain_pc.hasDiskimageVersion, version);
			}

			String description = diskImageContents.getDescription();
			if (description != null) {
				diskImage.addLiteral(Omn_domain_pc.hasDiskimageDescription,
						description);
			}

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		} catch (final InvalidPropertyURIException e) {
			AdvertisementConverter.LOG.info(e.getMessage());
		}

	}

	public String getRSpec(final Model model) throws JAXBException,
			InvalidModelException {
		final JAXBElement<RSpecContents> rspec = getRSpecObject(model);

		return AbstractConverter.toString(rspec, AdvertisementConverter.JAXB);
	}

	public JAXBElement<RSpecContents> getRSpecObject(final Model model)
			throws InvalidModelException {
		final RSpecContents advertisement = new RSpecContents();
		advertisement.setType(RspecTypeContents.ADVERTISEMENT);
		advertisement.setGeneratedBy(AbstractConverter.VENDOR);
		setTimeInformation(advertisement);

		model2rspec(model, advertisement);
		final JAXBElement<RSpecContents> rspec = this.of
				.createRspec(advertisement);
		return rspec;
	}

	private void model2rspec(final Model model, final RSpecContents manifest)
			throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Offering).toList();
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();

		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		convertStatementsToNodesAndLinks(manifest, resources);
	}

	private void convertStatementsToNodesAndLinks(final RSpecContents manifest,
			final List<Statement> omnResources) {
		for (final Statement omnResource : omnResources) {
			// @todo: check type of resource here and not only generate nodes
			final NodeContents geniNode = new NodeContents();

			setComponentDetails(omnResource, geniNode);
			setComponentManagerId(omnResource, geniNode);
			setHardwareTypes(omnResource, geniNode);
			setSliverTypes(omnResource, geniNode);
			setLocation(omnResource, geniNode);
			setAvailability(omnResource, geniNode);
			setMonitoringService(omnResource, geniNode);

			ResIterator infrastructures = omnResource.getModel()
					.listResourcesWithProperty(Omn.isResourceOf,
							Omn_federation.Infrastructure);
			if (infrastructures.hasNext()) {
				Resource infrastructure = infrastructures.next();
				geniNode.setComponentManagerId(infrastructure.getURI());
			}

			manifest.getAnyOrNodeOrLink().add(this.of.createNode(geniNode));
		}
	}

	private void setMonitoringService(Statement resource, NodeContents node) {
		Resource resourceResource = resource.getResource();
		if (resourceResource.hasProperty(Omn_lifecycle.usesService)) {
			Statement monitoringService = resourceResource
					.getProperty(Omn_lifecycle.usesService);
			Resource monitoringResource = monitoringService.getResource();
			Monitoring monitoring = new ObjectFactory().createMonitoring();

			if (monitoringResource.hasProperty(Omn_service.hasURI)) {
				Statement hasUri = monitoringService.getResource().getProperty(
						Omn_service.hasURI);

				System.out.println(hasUri.getObject().asLiteral().getString());
				// String uri = hasUri.getObject().asResource().getURI()
				// .toString();
				String uri = hasUri.getObject().asLiteral().getString();
				monitoring.setUri(uri);

			}

			if (monitoringResource.hasProperty(RDF.type)) {
				Statement hasType = monitoringService.getResource()
						.getProperty(RDF.type);

				// String type = hasType.getObject().asResource().getURI()
				// .toString();
				String type = hasType.getObject().asLiteral().getString();
				monitoring.setType(type);
			}

			node.getAnyOrRelationOrLocation().add(monitoring);
		}

	}

	private void setAvailability(Statement omnResource, NodeContents geniNode) {

		AvailableContents availabilty = of.createAvailableContents();
		Resource resource = omnResource.getResource();

		if (resource.hasProperty(Omn_resource.isAvailable)) {
			availabilty.setNow(resource.getProperty(Omn_resource.isAvailable)
					.getBoolean());

			geniNode.getAnyOrRelationOrLocation().add(
					of.createAvailable(availabilty));
		}
	}

	private void setLocation(Statement omnResource, NodeContents geniNode) {

		LocationContents location = of.createLocationContents();
		Resource omnRes = omnResource.getResource();

		if (omnRes.hasProperty(Omn_resource.country)) {
			location.setCountry(omnRes.getProperty(Omn_resource.country)
					.getString());
		} else {
			// country required
			location.setCountry("");
		}

		if (omnRes.hasProperty(Geo.lat)) {
			location.setLatitude(omnRes.getProperty(Geo.lat).getString());
		}

		if (omnRes.hasProperty(Geo.long_)) {
			location.setLongitude(omnRes.getProperty(Geo.long_).getString());
		}
		if (omnRes.hasProperty(Geo.lat) || omnRes.hasProperty(Geo.long_)) {
			geniNode.getAnyOrRelationOrLocation().add(
					of.createLocation(location));
		}
	}

	private void setSliverTypes(Statement omnResource, NodeContents geniNode) {

		StmtIterator canImplement = omnResource.getResource().listProperties(
				Omn_lifecycle.canImplement);
		SliverType sliver;

		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();
		while (canImplement.hasNext()) {
			Statement omnSliver = canImplement.next();
			String parentURI = omnSliver.getResource().getURI();
			sliver = of.createNodeContentsSliverType();
			sliver.setName(parentURI);
			if (null != parentURI) {
				geniNodeDetails.add(of.createNodeContentsSliverType(sliver));
				RDFNode sliverObject = omnSliver.getObject();
				Resource sliverResource = sliverObject.asResource();
				setCpus(sliverResource, sliver);
				setDiskImage(sliverResource, sliver);
			}
		}

	}

	private void setCpus(Resource sliverResource, SliverType sliver) {
		Pc pc = null;

		if (sliverResource.hasProperty(Omn_domain_pc.hasCPU)) {
			pc = new ObjectFactory().createPc();
			pc.setCpus(sliverResource.getProperty(Omn_domain_pc.hasCPU)
					.getObject().asLiteral().getInt());
		}
		if (pc != null) {
			sliver.getAnyOrDiskImage().add(pc);
		}

	}

	private void setDiskImage(Resource sliverResource, SliverType sliver) {
		// Resource sliverResource = sliverNode.asResource();

		if (sliverResource.hasProperty(RDFS.subClassOf, Omn_domain_pc.VM)) {

			StmtIterator omnSliverList = sliverResource
					.listProperties(Omn_domain_pc.hasDiskImage);

			while (omnSliverList.hasNext()) {
				Statement omnSliver = omnSliverList.next();
				RDFNode diskImageNode = omnSliver.getObject();
				Resource diskImageResource = diskImageNode.asResource();

				// check if the resource is a disk image
				if (diskImageResource.hasProperty(RDF.type,
						Omn_domain_pc.DiskImage)) {
					DiskImage diskImage = of
							.createNodeContentsSliverTypeDiskImage();
					String diskName = "";

					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
						diskName += diskImageResource
								.getProperty(Omn_domain_pc.hasDiskimageLabel)
								.getObject().asLiteral().getString();
					}
					diskImage.setName(diskName);

					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageDescription)) {
						String description = diskImageResource
								.getProperty(
										Omn_domain_pc.hasDiskimageDescription)
								.getObject().asLiteral().getString();
						diskImage.setDescription(description);
					}

					diskImage.setUrl(diskImageResource.getURI());
					sliver.getAnyOrDiskImage()
							.add(of.createNodeContentsSliverTypeDiskImage(diskImage));
				}
			}
		} else if (sliverResource.getURI().equals(Omn_domain_pc.VM.getURI())) {
			// TODO

		} else {
			while (sliverResource.hasProperty(Omn_lifecycle.canImplement)) {
				Statement omnSliver = sliverResource
						.getProperty(Omn_lifecycle.canImplement);
				omnSliver.remove();
				RDFNode diskImageNode = omnSliver.getObject();
				Resource diskImageResource = diskImageNode.asResource();

				// TODO: diskimage is handled in two places. Need to make a
				// single method.
				// check if the resource is a disk image
				if (diskImageResource.hasProperty(RDF.type,
						Omn_domain_pc.DiskImage)) {

					String diskName = "";
					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
						diskName += diskImageResource
								.getProperty(Omn_domain_pc.hasDiskimageLabel)
								.getObject().asLiteral().getString();
					}

					DiskImage diskImage = of
							.createNodeContentsSliverTypeDiskImage();

					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageDescription)) {
						String description = diskImageResource
								.getProperty(
										Omn_domain_pc.hasDiskimageDescription)
								.getObject().asLiteral().getString();
						diskImage.setDescription(description);
					}

					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageOS)) {
						String os = diskImageResource
								.getProperty(Omn_domain_pc.hasDiskimageOS)
								.getObject().asLiteral().getString();
						diskImage.setOs(os);
					}

					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageVersion)) {
						String version = diskImageResource
								.getProperty(Omn_domain_pc.hasDiskimageVersion)
								.getObject().asLiteral().getString();
						diskImage.setVersion(version);
					}

					diskImage.setName(diskName);
					sliver.getAnyOrDiskImage()
							.add(of.createNodeContentsSliverTypeDiskImage(diskImage));
				}
			}
		}
	}

	private void setHardwareTypes(Statement omnResource, NodeContents geniNode) {

		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(
				Omn_domain_pc.hasHardwareType);

		// check if the hardware type was specified as a type
		if (!types.hasNext()) {
			types = omnResource.getResource().listProperties(RDF.type);
			HardwareTypeContents hwType;

			while (types.hasNext()) {
				String rdfType = types.next().getResource().getURI();

				hwType = of.createHardwareTypeContents();
				hwType.setName(rdfType);
				if ((null != rdfType) && AbstractConverter.nonGeneric(rdfType)) {
					geniNodeDetails.add(of.createHardwareType(hwType));
				}
			}
		}

		while (types.hasNext()) {
			HardwareTypeContents hwType;
			Resource hwObject = types.next().getObject().asResource();
			String hwName = hwObject.getProperty(RDFS.label).getObject()
					.asLiteral().getString();

			hwType = of.createHardwareTypeContents();
			hwType.setName(hwName);

			// add emulab node slots
			if (hwObject.hasProperty(Omn_domain_pc.hasEmulabNodeTypeSlots)) {
				NodeType nodeType = of.createNodeType();
				String numSlots = hwObject
						.getProperty(Omn_domain_pc.hasEmulabNodeTypeSlots)
						.getObject().asLiteral().getString();
				nodeType.setTypeSlots(numSlots);
				hwType.getAny().add(nodeType);
			}

			geniNodeDetails.add(of.createHardwareType(hwType));

		}
	}

	private void setComponentDetails(final Statement resource,
			final NodeContents node) {

		String url = resource.getResource().getURI();
		// String urn = AbstractConverter.generateUrnFromUrl(url, "node");

		node.setComponentId(url);
		node.setComponentName(resource.getResource().getLocalName());
		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}
	}

	private void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		try {
			Statement parentOf = resource.getProperty(Omn_lifecycle.parentOf);
			node.setComponentManagerId(parentOf.getResource().getURI());
		} catch (PropertyNotFoundException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void setTimeInformation(final RSpecContents manifest) {
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

	public static String toString(JAXBElement<RSpecContents> advertisement)
			throws JAXBException {
		return toString(advertisement, JAXB);
	}

}
