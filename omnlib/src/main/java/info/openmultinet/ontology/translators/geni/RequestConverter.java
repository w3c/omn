package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.request.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.request.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Sliver;
import info.openmultinet.ontology.translators.geni.jaxb.request.StitchContent;
import info.openmultinet.ontology.translators.geni.request.RequestExtract;
import info.openmultinet.ontology.translators.geni.request.RequestExtractExt;
import info.openmultinet.ontology.translators.geni.request.RequestSet;
import info.openmultinet.ontology.translators.geni.request.RequestSetExt;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
import javax.xml.namespace.QName;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Main methods for converting to and from between request RSpecs and OMN
 * models.
 * 
 * @author robynloughnane
 *
 */
public class RequestConverter extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";
	private static final Logger LOG = Logger.getLogger(RequestConverter.class
			.getName());

	public static String getRSpec(final Model model) throws JAXBException,
			InvalidModelException, MissingRspecElementException {
		final RSpecContents request = new RSpecContents();
		request.setType(RspecTypeContents.REQUEST);
		request.setGeneratedBy(AbstractConverter.VENDOR);
		RequestConverter.setGeneratedTime(request);

		RequestConverter.model2rspec(model, request);

		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(request);
		return AbstractConverter.toString(rspec, RequestConverter.JAXB);
	}

	private static void setGeneratedTime(final RSpecContents request) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
			request.setGenerated(xmlGrogerianCalendar);
		} catch (final DatatypeConfigurationException e) {
			RequestConverter.LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(final Model model,
			final RSpecContents request) throws InvalidModelException,
			MissingRspecElementException {
		List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		if (groups.size() == 0) {
			groups = model.listSubjectsWithProperty(RDF.type, Omn.Topology)
					.toList();
		}
		AbstractConverter.validateModel(groups);

		// save uri of topology in special rspec field
		final Resource group = groups.iterator().next();
		String uri = group.getURI();
		QName key = new QName("http://opensdncore.org/ontology/", "uri", "osco");
		request.getOtherAttributes().put(key, uri);

		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();
		List<Statement> leases = group.listProperties(Omn_lifecycle.hasLease)
				.toList();
		resources.addAll(leases);
		List<Statement> components = group.listProperties(Omn.hasComponent)
				.toList();
		resources.addAll(components);

		RequestConverter.convertStatementsToNodesAndLinks(request, resources);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents request, final List<Statement> resources)
			throws MissingRspecElementException, InvalidModelException {

		for (final Statement resource : resources) {
			if (!resource.getResource()
					.hasProperty(RDF.type, Omn_resource.Link)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_lifecycle.Lease)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_domain_wireless.Channel)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_resource.Openflow)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_resource.Stitching)
			// && !resource
			// .getResource()
			// .hasProperty(
			// RDF.type,
			// info.openmultinet.ontology.vocabulary.Osco.m2m_server)
			// && !resource
			// .getResource()
			// .hasProperty(
			// RDF.type,
			// info.openmultinet.ontology.vocabulary.Osco.m2m_gateway)
			// && !resource
			// .getResource()
			// .hasProperty(
			// RDF.type,
			// info.openmultinet.ontology.vocabulary.Osco.ServiceContainer)
			) {

				final NodeContents node = new NodeContents();

				RequestSet.setComponentDetails(resource, node);
				RequestSet.setComponentId(resource, node);
				if (resource.getResource()
						.hasProperty(Omn_resource.isExclusive)) {
					final boolean isExclusive = resource.getResource()
							.getProperty(Omn_resource.isExclusive).getBoolean();
					node.setExclusive(isExclusive);
				}
				RequestSet.setLocation(resource, node);
				RequestSet.setInterfaces(resource, node);
				RequestSet.setSliverType(resource, node);
				RequestSet.setServices(resource, node);
				RequestSet.setHardwareTypes(resource, node);
				RequestSetExt.setFd(resource, node);
				RequestSetExt.setMonitoringService(resource, node);
				RequestSetExt.setOsco(resource, node);
				RequestSetExt.setEmulabExtension(resource, node);
				RequestSetExt.setAccessNetwork(resource, node);
				RequestSetExt.setFivegGateway(resource, node);
				RequestSetExt.setFivegEnodeb(resource, node);
				RequestSetExt.setFivegDns(resource, node);
				RequestSetExt.setFivegSwitch(resource, node);
				RequestSetExt.setFivegBt(resource, node);
				RequestSetExt.setFivegControl(resource, node);
				RequestSetExt.setFivegHss(resource, node);
				RequestSetExt.setEPC(resource, node);
				RequestSetExt.setUE(resource, node);
				RequestSetExt.setAcs(resource, node);
				RequestSetExt.setOlLease(resource, node);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createNode(node));
			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_resource.Link)) {

				final LinkContents link = new LinkContents();

				// setGeniSliverInfo(resource, link);
				RequestSet.setLinkDetails(resource, link);
				RequestSet.setInterfaceRefs(resource, link);
				RequestSet.setLinkProperties(resource, link);
				RequestSetExt.setSharedVlan(resource, link);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));
			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_lifecycle.Lease)) {
				final Lease of = new Lease();
				RequestSetExt.setOlLease(resource, of);
				request.getAnyOrNodeOrLink().add(of);

			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_domain_wireless.Channel)) {
				final Channel of = new Channel();
				RequestSetExt.setOlChannel(resource, of);
				request.getAnyOrNodeOrLink().add(of);

			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_resource.Openflow)) {
				final Sliver of = new Sliver();
				RequestSetExt.setOpenflow(resource, of);
				request.getAnyOrNodeOrLink().add(of);
			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_resource.Stitching)) {
				ObjectFactory of = new ObjectFactory();

				StitchContent stitchContent = of.createStitchContent();
				RequestSetExt.setStitching(resource, stitchContent);

				JAXBElement<StitchContent> stitching = of
						.createStitching(stitchContent);

				request.getAnyOrNodeOrLink().add(stitching);
				// } else if (resource.getResource().hasProperty(RDF.type,
				// info.openmultinet.ontology.vocabulary.Osco.m2m_server)
				// || resource
				// .getResource()
				// .hasProperty(
				// RDF.type,
				// info.openmultinet.ontology.vocabulary.Osco.m2m_gateway)
				// || resource
				// .getResource()
				// .hasProperty(
				// RDF.type,
				// info.openmultinet.ontology.vocabulary.Osco.ServiceContainer))
				// {
				// setOsco(resource, request);
			}
		}
	}

	public static Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {

		final JAXBContext context = JAXBContext
				.newInstance(RSpecContents.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		final RSpecContents request = rspec.getValue();

		final Model model = ModelFactory.createDefaultModel();

		Resource topology = null;
		// final Resource topology = model
		// .createResource(AbstractConverter.NAMESPACE + "request");
		Map<QName, String> attributes = request.getOtherAttributes();
		for (Entry<QName, String> entry : attributes.entrySet()) {
			QName key = entry.getKey();
			String value = entry.getValue();

			if (key.getNamespaceURI()
					.equals("http://opensdncore.org/ontology/")) {

				if (key.getLocalPart().equals("uri")) {
					topology = model.createResource(value);
				}
			}
		}

		if (topology == null) {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			topology = model.createResource(uuid);
		}

		topology.addProperty(RDF.type, Omn_lifecycle.Request);
		topology.addProperty(RDFS.label, "Request");
		topology.addProperty(RDF.type, Omn.Topology);

		// RequestConverter.extractNodes(request, topology);
		// RequestConverter.extractLinks(request, topology);

		for (Object o : request.getAnyOrNodeOrLink()) {

			if (o instanceof JAXBElement) {
				RequestExtract.extractDetails(topology, o);
			} else if (o instanceof org.apache.xerces.dom.ElementNSImpl) {
				if (o.toString().contains("openflow:sliver")) {
					RequestExtractExt.extractOpenflow(topology, o);
				} else if (o.toString().contains("stitch:stitching")) {
					RequestExtractExt.extractStitching(topology, o);
				} else {
					RequestConverter.LOG.log(Level.INFO,
							"Found unknown ElementNSImpl extension: " + o);
				}
				// } else if (o.getClass().equals(Osco.class)) {
				// tryExtractOsco(topology, o);
			} else if (o instanceof Lease) {
				RequestExtractExt.extractOlLease(topology, o);
			} else if (o instanceof Channel) {
				RequestExtractExt.tryExtractOlChannel(topology, o);
			} else {
				RequestConverter.LOG.log(Level.INFO,
						"Found unknown extension: " + o);
			}
		}

		// NetworkTopologyExtractor.extractTopologyInformation(request,
		// topology);

		return model;
	}
}