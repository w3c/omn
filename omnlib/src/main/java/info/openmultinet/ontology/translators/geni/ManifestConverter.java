package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.advertisement.AdSetExt;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliceInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.manifest.ManifestExtract;
import info.openmultinet.ontology.translators.geni.manifest.ManifestExtractExt;
import info.openmultinet.ontology.translators.geni.manifest.ManifestSet;
import info.openmultinet.ontology.translators.geni.manifest.ManifestSetExt;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Main methods for converting to and from between manifest RSpecs and OMN
 * models.
 * 
 * @author robynloughnane
 *
 */
public class ManifestConverter extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(ManifestConverter.class
			.getName());

	/**
	 * Method to get a manifest RSpec from an OMN Model
	 * 
	 * @param model
	 * @param hostname
	 * @return
	 * @throws JAXBException
	 * @throws InvalidModelException
	 */
	public static String getRSpec(final Model model, String hostname)
			throws JAXBException, InvalidModelException {
		final RSpecContents manifest = new RSpecContents();
		manifest.setType(RspecTypeContents.MANIFEST);
		manifest.setGeneratedBy(AbstractConverter.VENDOR);
		ManifestConverter.setGeneratedTime(manifest);

		ManifestConverter.model2rspec(model, manifest, hostname);
		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(manifest);
		return AbstractConverter.toString(rspec,
				"info.openmultinet.ontology.translators.geni.jaxb.manifest");
	}

	private static void setGeneratedTime(final RSpecContents manifest) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
			manifest.setGenerated(xmlGrogerianCalendar);
		} catch (final DatatypeConfigurationException e) {
			ManifestConverter.LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(final Model model,
			final RSpecContents manifest, String hostname)
			throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Topology).toList();

		AbstractConverter.validateModel(groups);

		final Resource group = groups.iterator().next();

		if (group.hasProperty(Omn_lifecycle.expirationTime)) {
			XSDDateTime creationTime = (XSDDateTime) group
					.getProperty(Omn_lifecycle.expirationTime).getObject()
					.asLiteral().getValue();
			XMLGregorianCalendar xgc = xsdToXmlTime(creationTime);

			if (xgc != null) {
				manifest.setExpires(xgc);
			}
		}

		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();
		List<Statement> components = group.listProperties(Omn.hasComponent)
				.toList();
		resources.addAll(components);
		List<Statement> leases = group.listProperties(Omn_lifecycle.hasLease)
				.toList();
		resources.addAll(leases);

		if (group.hasProperty(RDF.type, Omn_lifecycle.Manifest)) {
			ManifestSetExt.setGeniSliceInfo(group, manifest);
		}

		ManifestConverter.convertStatementsToNodesAndLinks(manifest, resources,
				hostname);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents manifest, final List<Statement> resources,
			String hostname) throws InvalidModelException {

		for (final Statement resource : resources) {
			if (!resource.getResource()
					.hasProperty(RDF.type, Omn_resource.Link)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_domain_wireless.Channel)
					&& !resource.getResource().hasProperty(RDF.type,
							Omn_lifecycle.Lease)) {
				
				final NodeContents node = new NodeContents();

				ManifestSet.setComponentDetails(resource, node);
				ManifestSet.setLocation(resource, node);
				// setComponentManagerId(resource, node);
				ManifestSet.setHardwareTypes(resource, node);
				ManifestSetExt.setReservation(resource, node);
				ManifestSetExt.setMonitoringService(resource, node);
				ManifestSetExt.setOsco(resource, node);
				ManifestSetExt.setGeniSliverInfo(resource, node);
				// setState
				// setVMID
				ManifestSet.setSliverType(resource, node);
				ManifestSet.setServices(resource, node);
				ManifestSet.setInterfaces(resource, node);
				ManifestSetExt.setFd(resource, node);
				ManifestSetExt.setEPC(resource, node);
				ManifestSetExt.setGateway(resource, node);
				ManifestSetExt.setAccessNetwork(resource, node);
				ManifestSetExt.setUserEquipment(resource, node);
				ManifestSetExt.setAcs(resource, node);

				manifest.getAnyOrNodeOrLink().add(
						new ObjectFactory().createNode(node));

			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_domain_wireless.Channel)) {
				
				final Channel of = new Channel();
				ManifestSetExt.setOlChannel(resource, of);
				manifest.getAnyOrNodeOrLink().add(of);

			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_lifecycle.Lease)) {
				
				final Lease of = new Lease();
				ManifestSetExt.setOlLease(resource, of);
				manifest.getAnyOrNodeOrLink().add(of);

			} else if (resource.getResource().hasProperty(RDF.type,
					Omn_resource.Link)) {

				final LinkContents link = new LinkContents();
				ManifestSetExt.setGeniSliverInfo(resource, link);
				ManifestSet.setLinkDetails(resource, link, hostname);
				ManifestSet.setInterfaceRefs(resource, link, hostname);

				manifest.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));
			}
		}
	}

	public static Model getModel(final InputStream stream)
			throws JAXBException, MissingRspecElementException,
			InvalidModelException, InvalidRspecValueException {
		final Model model = ManifestConverter.createModelTemplate();
		final RSpecContents manifest = ManifestConverter.getManifest(stream);

		ManifestConverter.convertManifest2Model(manifest, model);

		return model;
	}

	private static void convertManifest2Model(final RSpecContents manifest,
			final Model model) throws MissingRspecElementException,
			InvalidModelException, InvalidRspecValueException {

		// Resource topology = model.getResource(AbstractConverter.NAMESPACE
		// + "manifest");
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Manifest).toList();
		AbstractConverter.validateModel(groups);
		Resource topology = groups.get(0);

		for (Object o : manifest.getAnyOrNodeOrLink()) {
			if (o instanceof JAXBElement) {
				ManifestExtract.extractDetails(model, topology, o);
			} else if (o.getClass().equals(GeniSliceInfo.class)) {
				ManifestExtractExt.extractGeniSliceInfo(topology, o);
			} else if (o.getClass().equals(Channel.class)) {
				ManifestExtractExt.tryExtractOlChannel(topology, o);
			} else if (o.getClass().equals(Lease.class)) {
				ManifestExtractExt.tryExtractOlLease(topology, o);
			} else {
				ManifestConverter.LOG.log(Level.INFO,
						"Found unknown extension: " + o);
			}
		}
	}

	public static RSpecContents getManifest(final InputStream stream)
			throws JAXBException {
		final JAXBContext context = JAXBContext
				.newInstance(RSpecContents.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(stream), RSpecContents.class);
		return rspec.getValue();
	}

	public static Model createModelTemplate() throws JAXBException {
		final Model model = ModelFactory.createDefaultModel();
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		final Resource topology = model.createResource(uuid);
		// final Resource topology = model
		// .createResource(AbstractConverter.NAMESPACE + "manifest");
		topology.addProperty(RDF.type, Omn_lifecycle.Manifest);
		topology.addProperty(RDFS.label, "Manifest");
		return model;
	}

	public static String generateSliverID(String hostname, String uri) {
		try {
			return "urn:publicid:IDN+" + hostname + "+sliver+"
					+ URLEncoder.encode(uri, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return uri;
		}
	}

	public static String parseSliverID(String sliverID) {
		try {
			Pattern pattern = Pattern.compile("\\+sliver\\+(.*)");
			Matcher matcher = pattern.matcher(sliverID);
			if (matcher.find()) {
				return URLDecoder.decode(matcher.group(1),
						StandardCharsets.UTF_8.toString());
			} else {
				return sliverID;
			}
		} catch (UnsupportedEncodingException e) {
			return sliverID;
		}
	}

}