package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.advertisement.AdExtract;
import info.openmultinet.ontology.translators.geni.advertisement.AdExtractExt;
import info.openmultinet.ontology.translators.geni.advertisement.AdSet;
import info.openmultinet.ontology.translators.geni.advertisement.AdSetExt;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Available;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Datapath;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ExternalReferenceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.GroupContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecOpstate;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Sliver;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.StitchContent;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
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

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Main methods for converting to and from between advertisement RSpecs and OMN
 * models.
 * 
 * @author robynloughnane
 *
 */
public class AdvertisementConverter extends AbstractConverter {

	private static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.advertisement";
	// private static final String PREFIX =
	// "http://open-multinet.info/omnlib/converter";

	private static final Logger LOG = Logger
			.getLogger(AdvertisementConverter.class.getName());
	private Model model;
	private JAXBContext context;
	private Unmarshaller unmarshaller;
	private ObjectFactory of;
	private XMLInputFactory xmlif;
	private boolean verbose = false;

	public AdvertisementConverter() throws JAXBException {
		super();
		this.model = ModelFactory.createInfModel(this.reasoner,
				ModelFactory.createDefaultModel());
		this.context = JAXBContext.newInstance(RSpecContents.class);
		this.unmarshaller = context.createUnmarshaller();
		this.of = new ObjectFactory();
		this.xmlif = XMLInputFactory.newInstance();
	}

	public void setVerbose(boolean verbosity) {
		this.verbose = verbosity;
	}

	public void resetModel() {
		this.model = ModelFactory.createInfModel(this.reasoner,
				ModelFactory.createDefaultModel());
	}

	public Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {

		final RSpecContents rspecAdvertisement = getRspec(input);

		return getModel(rspecAdvertisement);
	}

	// @fixme: expensive/long running method call
	public RSpecContents getRspec(final InputStream input)
			throws JAXBException, XMLStreamException {

		XMLStreamReader xmler = xmlif.createXMLStreamReader(input);

		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(xmler,
				RSpecContents.class);
		final RSpecContents advertisement = rspec.getValue();
		return advertisement;
	}

	@SuppressWarnings("rawtypes")
	public Model getModel(RSpecContents rspec)
			throws MissingRspecElementException, InvalidRspecValueException {

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		final Resource offering = model.createResource(uuid);

		// final Resource offering = model.createResource(
		// AdvertisementConverter.PREFIX + "#advertisement").addProperty(
		// RDF.type, Omn_lifecycle.Offering);

		offering.addProperty(RDF.type, Omn_lifecycle.Offering);
		offering.addProperty(RDFS.label, "Offering");

		final List rspecObjects = (List) rspec.getAnyOrNodeOrLink();

		for (Object rspecObject : rspecObjects) {

			AdExtract.tryExtractNode(rspecObject, offering);
			AdExtract.tryExtractLink(rspecObject, offering);
			AdExtract.tryExtractExternalRef(rspecObject, offering);
			AdExtractExt.tryExtractSharedVlan(rspecObject, offering);
			AdExtractExt.tryExtractRoutableAddresses(rspecObject, offering);
			AdExtractExt.tryExtractOpstate(rspecObject, offering);
			AdExtractExt.tryExtractOlChannel(rspecObject, offering);
			AdExtractExt.tryExtractOlLease(rspecObject, offering);

			if (rspecObject.toString().contains("stitching")) {
				AdExtractExt.extractStitching(rspecObject, offering);
			} else if (rspecObject instanceof org.apache.xerces.dom.ElementNSImpl) {
				AdExtractExt.extractOpenflow(rspecObject, offering);
				// } else if (rspecObject.getClass().equals(Epc.class)) {
				// extractEpc(offering, rspecObject);
			}
		}

		return model;
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

	private void model2rspec(final Model model, final RSpecContents ad)
			throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Offering).toList();
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();

		// set external_ref
		if (group.hasProperty(Omn_lifecycle.hasID)) {
			ExternalReferenceContents exrefContents = of
					.createExternalReferenceContents();
			String componentId = group.getProperty(Omn_lifecycle.hasID)
					.getObject().asLiteral().getString();
			exrefContents.setComponentId(componentId);

			if (group.hasProperty(Omn_lifecycle.managedBy)) {
				String component_manager_id = group
						.getProperty(Omn_lifecycle.managedBy).getObject()
						.asLiteral().getString();
				exrefContents.setComponentManagerId(component_manager_id);
			}

			ad.getAnyOrNodeOrLink().add(of.createExternalRef(exrefContents));
		}

		List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();
		List<Statement> components = group.listProperties(Omn.hasComponent)
				.toList();
		resources.addAll(components);
		List<Statement> leases = group.listProperties(Omn_lifecycle.hasLease)
				.toList();
		resources.addAll(leases);

		convertStatementsToNodesAndLinks(ad, resources);
	}

	private void convertStatementsToNodesAndLinks(
			final RSpecContents advertisement,
			final List<Statement> omnResources) throws InvalidModelException {
		for (final Statement omnResource : omnResources) {
			// if type doesn't match anything else, then assume it's a node
			if ((omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Node))
					|| (!omnResource.getResource().hasProperty(RDF.type,
							Omn_resource.Link)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_lifecycle.Opstate)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_domain_pc.SharedVlan)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_resource.Stitching)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_domain_pc.Datapath)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_resource.Openflow)
							&& !omnResource.getResource().hasProperty(RDF.type,
									Omn_domain_wireless.Channel) && !omnResource
							.getResource().hasProperty(RDF.type,
									Omn_lifecycle.Lease))) {

				if (verbose) {
					AdSet.setNodesVerbose(omnResource, advertisement);
				} else {
					// @todo: check type of resource here and not only generate
					// nodes
					final NodeContents geniNode = new NodeContents();

					AdSet.setCloud(omnResource, geniNode);
					AdSet.setComponentDetails(omnResource, geniNode);
					AdSet.setComponentManagerId(omnResource, geniNode);
					AdSet.setHardwareTypes(omnResource, geniNode);
					AdSet.setSliverTypes(omnResource, geniNode);
					AdSet.setLocation(omnResource, geniNode);
					AdSet.setAvailability(omnResource, geniNode);
					AdSetExt.setMonitoringService(omnResource, geniNode);
					AdSetExt.setOsco(omnResource, geniNode);
					AdSet.setInterface(omnResource, geniNode);
					AdSetExt.setFd(omnResource, geniNode);
					AdSetExt.setTrivialBandwidth(omnResource, geniNode);
					AdSetExt.setAccessNetwork(omnResource, geniNode);
					AdSetExt.setGateway(omnResource, geniNode);
					AdSetExt.setEPC(omnResource, geniNode);
					AdSetExt.setUE(omnResource, geniNode);
					AdSetExt.setACS(omnResource, geniNode);

					ResIterator infrastructures = omnResource.getModel()
							.listResourcesWithProperty(Omn.isResourceOf,
									Omn_federation.Infrastructure);
					if (infrastructures.hasNext()) {
						Resource infrastructure = infrastructures.next();
						geniNode.setComponentManagerId(infrastructure.getURI());
					}

					advertisement.getAnyOrNodeOrLink().add(
							this.of.createNode(geniNode));
				}
			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_domain_pc.Datapath)) {

				// TODO: set datapath as direct sub element of RSpec
				final Datapath dp = new Datapath();
				GroupContents gc = new GroupContents();
				Sliver of = new Sliver();
				AdSetExt.setOpenflowDatapath(omnResource, dp);
				gc.setDatapath(dp);
				of.getGroup().add(gc);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Link)) {
				final LinkContents link = new LinkContents();

				AdSet.setLinkDetails(omnResource, link);
				AdSet.setInterfaceRefs(omnResource, link);
				AdSet.setLinkProperties(omnResource, link);

				advertisement.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Openflow)) {
				final Sliver of = new Sliver();
				AdSetExt.setOpenflow(omnResource, of);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_domain_wireless.Channel)) {
				final Channel of = new Channel();
				AdSetExt.setOlChannel(omnResource, of);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_lifecycle.Lease)) {
				final Lease of = new Lease();
				AdSetExt.setOlLease(omnResource, of);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_domain_pc.SharedVlan)) {

				RspecSharedVlan sharedVlan = this.of.createRspecSharedVlan();

				StmtIterator availables = omnResource.getResource()
						.listProperties(Omn_domain_pc.hasAvailable);

				while (availables.hasNext()) {
					Resource availableResource = availables.next().getObject()
							.asResource();
					Available available = this.of.createAvailable();

					if (availableResource.hasProperty(Omn_domain_pc.restricted)) {
						boolean restricted = availableResource
								.getProperty(Omn_domain_pc.restricted)
								.getObject().asLiteral().getBoolean();
						available.setRestricted(restricted);
					}
					if (availableResource.hasProperty(RDFS.label)) {
						String name = availableResource.getProperty(RDFS.label)
								.getObject().asLiteral().getString();
						available.setName(name);
					}
					sharedVlan.getAvailable().add(available);
				}

				advertisement.getAnyOrNodeOrLink().add(sharedVlan);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_lifecycle.Opstate)) {

				RspecOpstate rspecOpstate = this.of.createRspecOpstate();

				AdSetExt.setOpstateAttributes(omnResource, rspecOpstate);
				AdSet.setSliverTypes(omnResource, rspecOpstate);
				AdSetExt.setStates(omnResource, rspecOpstate);

				advertisement.getAnyOrNodeOrLink().add(rspecOpstate);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Stitching)) {
				ObjectFactory of = new ObjectFactory();

				StitchContent stitchContent = of.createStitchContent();
				AdSetExt.setStitching(omnResource, stitchContent);

				JAXBElement<StitchContent> stitching = of
						.createStitching(stitchContent);

				advertisement.getAnyOrNodeOrLink().add(stitching);
				// } else if (omnResource.getResource().hasProperty(RDF.type,
				// info.openmultinet.ontology.vocabulary.Epc.EPC)) {
				// setEPC(omnResource, advertisement);
			}
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
