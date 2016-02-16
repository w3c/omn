package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Cloud;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ExternalReferenceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType.DiskImage;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Position3D;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from an advertisement RSpec to create
 * an OMN model. For native RSpec elements.
 * 
 * @author robynloughnane
 *
 */
public class AdExtract extends AbstractConverter {

	private static final String HOST = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger.getLogger(AdExtract.class
			.getName());

	private static void tryExtractInterface(Object rspecObject,
			Resource omnResource) {

		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<InterfaceContents> nodeJaxb = (JAXBElement<InterfaceContents>) rspecObject;
			final InterfaceContents content = nodeJaxb.getValue();

			Model outputModel = omnResource.getModel();
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource interfaceResource = outputModel.createResource(uuid);

			List<Object> interfaces = content.getAnyOrIpOrMonitoring();
			for (int i = 0; i < interfaces.size(); i++) {
				Object interfaceObject = interfaces.get(i);
				// tryExtractIPAddress(interfaceObject, interfaceResource);
				AdExtractExt.tryExtractEmulabInterface(interfaceObject,
						interfaceResource);
			}

			interfaceResource.addProperty(RDF.type, Omn_resource.Interface);
			omnResource.addProperty(Omn_resource.hasInterface,
					interfaceResource);

			if (content.getComponentId() != null) {
				URI uri = URI.create(content.getComponentId());
				interfaceResource.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}

			if (content.getComponentName() != null) {
				String componentName = content.getComponentName();
				interfaceResource.addLiteral(Omn_lifecycle.hasComponentName,
						componentName);
			}

			if (content.getRole() != null) {
				interfaceResource.addProperty(Omn_lifecycle.hasRole,
						content.getRole());
			}

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractExternalRef(Object rspecObject,
			Resource offering) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<ExternalReferenceContents> exRefJaxb = (JAXBElement<ExternalReferenceContents>) rspecObject;
			final ExternalReferenceContents exRef = exRefJaxb.getValue();

			// <xs:attribute name="component_id" use="required"/>
			if (exRef.getComponentId() == null) {
				throw new MissingRspecElementException(
						"ExternalReferenceContents > component_id");
			}
			offering.addProperty(Omn_lifecycle.hasID, exRef.getComponentId());

			if (exRef.getComponentManagerId() != null) {
				offering.addProperty(Omn_lifecycle.managedBy,
						exRef.getComponentManagerId());
			}

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	public static void tryExtractLink(final Object rspecObject,
			final Resource topology) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<LinkContents> nodeJaxb = (JAXBElement<LinkContents>) rspecObject;
			final LinkContents link = nodeJaxb.getValue();
			final Resource linkResource = topology.getModel().createResource(
					link.getComponentId());

			// component_id is required
			if (link.getComponentId() == null) {
				throw new MissingRspecElementException(
						"LinkContents > component_id ");
			}
			String componentId = link.getComponentId();
			URI uri = URI.create(componentId);
			linkResource.addLiteral(Omn_lifecycle.hasComponentID, uri);

			String componentName = link.getComponentName();
			if (componentName != null) {
				linkResource.addProperty(Omn_lifecycle.hasComponentName,
						componentName);
			}

			for (Object o : link.getAnyOrPropertyOrLinkType()) {
				if (o instanceof JAXBElement) {
					JAXBElement<?> linkElement = (JAXBElement<?>) o;
					if (linkElement.getDeclaredType().equals(
							InterfaceRefContents.class)) {
						extractInterfaceRefs(linkElement, linkResource);
					} else if (linkElement.getDeclaredType().equals(
							LinkPropertyContents.class)) {
						extractLinkProperties(linkElement, linkResource);
					}
				} else if (o.getClass().equals(ComponentManager.class)) {
					extractComponentManager(o, linkResource);
				} else if (o.getClass().equals(LinkType.class)) {
					extractLinkType(o, linkResource);
				} else {
					AdExtract.LOG.log(Level.INFO,
							"Found unknown link extension: " + o);
				}
			}

			linkResource.addProperty(RDF.type, Omn_resource.Link);
			linkResource.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, linkResource);

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void extractLinkType(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final LinkType content = (LinkType) o;

		// name required
		if (content.getName() == null) {
			throw new MissingRspecElementException("link_type > name");
		}
		linkResource.addProperty(RDFS.label, content.getName());

	}

	private static void extractComponentManager(Object o, Resource linkResource) {

		final ComponentManager content = (ComponentManager) o;

		if (content.getName() != null) {
			URI uri = URI.create(content.getName());
			if (uri != null) {
				linkResource.addLiteral(Omn_lifecycle.hasComponentManagerName,
						uri);
			}
		}
	}

	private static void extractLinkProperties(JAXBElement<?> linkElement,
			Resource linkResource) throws MissingRspecElementException {

		final LinkPropertyContents content = (LinkPropertyContents) linkElement
				.getValue();

		String sourceID = content.getSourceId();
		String destID = content.getDestId();

		if (sourceID == null || destID == null) {
			throw new MissingRspecElementException(
					"LinkPropertyContents > source_id/dest_id");
		}

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource linkPropertyResource = linkResource.getModel().createResource(
				uuid);
		linkPropertyResource.addProperty(RDF.type, Omn_resource.LinkProperty);
		linkPropertyResource.addProperty(Omn_resource.hasSink, destID);
		linkPropertyResource.addProperty(Omn_resource.hasSource, sourceID);

		String capacity = content.getCapacity();
		if (capacity != null) {
			String[] numberAndUnit = CommonMethods.splitNumberUnit(capacity);

			int number = Integer.parseInt(numberAndUnit[0]);
			BigInteger numberBigInt = BigInteger.valueOf(number);
			linkPropertyResource.addLiteral(Omn_domain_pc.hasCapacity,
					numberBigInt);
			if (numberAndUnit.length > 1) {
				linkPropertyResource.addProperty(Omn_domain_pc.hasCapacityUnit,
						numberAndUnit[1]);
			}
		}

		String latency = content.getLatency();
		if (latency != null) {
			linkPropertyResource.addProperty(Omn_domain_pc.hasLatency, latency);
		}

		String packetLoss = content.getPacketLoss();
		if (packetLoss != null) {
			linkPropertyResource.addProperty(Omn_domain_pc.hasPacketLoss,
					packetLoss);
		}

		linkResource
				.addProperty(Omn_resource.hasProperty, linkPropertyResource);

	}

	private static void extractInterfaceRefs(JAXBElement<?> linkElement,
			Resource linkResource) {
		final InterfaceRefContents content = (InterfaceRefContents) linkElement
				.getValue();

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource interfaceResource = linkResource.getModel().createResource(
				uuid);
		URI uri = URI.create(content.getComponentId());
		interfaceResource.addLiteral(Omn_lifecycle.hasComponentID, uri);
		linkResource.addProperty(Omn_resource.hasInterface, interfaceResource);

	}

	public static void tryExtractNode(final Object object,
			final Resource topology) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<NodeContents> nodeJaxb = (JAXBElement<NodeContents>) object;
			final NodeContents rspecNode = nodeJaxb.getValue();

			// if it is an acceptable OMN URN, it will be converted into a URL
			String componentId = CommonMethods.generateUrlFromUrn(rspecNode
					.getComponentId());

			final Resource omnNode = topology.getModel().createResource(
					componentId);

			omnNode.addProperty(RDF.type, Omn_resource.Node);
			omnNode.addProperty(Omn.isResourceOf, topology);

			if (rspecNode.getComponentManagerId() != null) {
				RDFNode componentManager = ResourceFactory
						.createResource(rspecNode.getComponentManagerId());
				omnNode.addProperty(Omn_lifecycle.managedBy, componentManager);
			}

			topology.getModel().addLiteral(omnNode, Omn_resource.isExclusive,
					rspecNode.isExclusive());

			if (rspecNode.getComponentName() != null) {
				omnNode.addLiteral(RDFS.label, rspecNode.getComponentName());
				omnNode.addProperty(Omn_lifecycle.hasComponentName,
						rspecNode.getComponentName());
			}

			// blank node indicates that node does not yet have a declared
			// sliver type; to be overwritten if has sliver type
			// http://www.ietf.org/rfc/rfc4122.txt
			// String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			// Resource sliverType = topology.getModel().createResource(uuid);
			// omnNode.addProperty(Omn_resource.hasSliverType, sliverType);

			for (Object rspecNodeObject : rspecNode
					.getAnyOrRelationOrLocation()) {

				tryExtractCloud(rspecNodeObject, omnNode);
				tryExtractHardwareType(rspecNodeObject, omnNode);
				tryExtractSliverType(rspecNodeObject, omnNode);
				tryExtractLocation(rspecNodeObject, omnNode);
				AdExtractExt.tryExtractAvailability(rspecNodeObject, omnNode);
				AdExtractExt.tryExtractMonitoring(rspecNodeObject, omnNode);
				AdExtractExt.tryExtractOsco(rspecNodeObject, omnNode);
				tryExtractInterface(rspecNodeObject, omnNode);
				AdExtractExt.tryExtractEmulabFd(rspecNodeObject, omnNode);
				AdExtractExt.tryExtractEmulabTrivialBandwidth(rspecNodeObject,
						omnNode);
				AdExtractExt.tryExtractAccessNetwork(omnNode, rspecNodeObject);
				AdExtractExt.tryExtractUe(omnNode, rspecNodeObject);
				AdExtractExt.tryExtractGateway(omnNode, rspecNodeObject);
				AdExtractExt.tryExtractEpc(omnNode, rspecNodeObject);
				AdExtractExt.tryExtractAcs(omnNode, rspecNodeObject);
			}

			topology.addProperty(Omn.hasResource, omnNode);

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractCloud(Object rspecNodeObject, Resource omnNode) {
		try {
			// keep cloudJaxb here although not used, as needed to double check
			// a cloud is present
			final Cloud cloudJaxb = (Cloud) rspecNodeObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			final Resource cloudResource = omnNode.getModel().createResource(
					uuid);

			omnNode.addProperty(RDF.type, Omn_resource.Cloud);
			omnNode.addProperty(Omn.hasResource, cloudResource);
			cloudResource.addProperty(Omn.isResourceOf, omnNode);

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractLocation(Object rspecNodeObject,
			Resource omnNode) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<LocationContents> locationJaxb = (JAXBElement<LocationContents>) rspecNodeObject;
			final LocationContents location = locationJaxb.getValue();

			if (location != null) {

				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource locationResource = omnNode.getModel().createResource(
						uuid);
				locationResource.addProperty(RDF.type, Omn_resource.Location);

				String latitude = location.getLatitude();
				String longitude = location.getLongitude();
				String country = location.getCountry();

				// country is required, when location is specified
				if (country == null) {
					throw new MissingRspecElementException(
							"LocationContents > country");
				} else {
					locationResource.addProperty(Geonames.countryCode, country);
				}

				if (latitude != null) {
					locationResource.addProperty(Geo.lat, latitude);
				}
				if (longitude != null) {
					locationResource.addProperty(Geo.long_, longitude);
				}

				Map<QName, String> otherLocationAttributes = location
						.getOtherAttributes();
				for (Entry<QName, String> entry : otherLocationAttributes
						.entrySet()) {

					QName key = entry.getKey();
					String value = entry.getValue();

					if (key.getNamespaceURI().equals(
							"http://open-multinet.info/location")) {

						if (key.getLocalPart().equals("id")) {
							locationResource.addProperty(Omn_lifecycle.hasID,
									value);
						} else if (key.getLocalPart().equals("name")) {
							locationResource.addProperty(RDFS.label, value);
						}
					}
				}

				List<Object> locationSubElements = location.getAny();
				for (Object object : locationSubElements) {
					if (object instanceof Position3D) {

						Position3D position3d = (Position3D) object;
						BigDecimal x = position3d.getX();
						BigDecimal y = position3d.getY();
						BigDecimal z = position3d.getZ();

						locationResource.addLiteral(Omn_resource.x, x);
						locationResource.addLiteral(Omn_resource.y, y);
						locationResource.addLiteral(Omn_resource.z, z);
					}
				}

				omnNode.addProperty(Omn_resource.hasLocation, locationResource);
			}
		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractHardwareType(Object rspecNodeObject,
			Resource omnNode) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<HardwareTypeContents> hwJaxb = (JAXBElement<HardwareTypeContents>) rspecNodeObject;
			final HardwareTypeContents hw = hwJaxb.getValue();

			final Resource omnHw;

			String hardwareTypeName = hw.getName();
			// <xs:attribute name="component_id" use="required"/>
			if (hardwareTypeName == null) {
				throw new MissingRspecElementException(
						"HardwareTypeContents > name");
			}

			if (AbstractConverter.isUrl(hardwareTypeName)
					|| AbstractConverter.isUrn(hardwareTypeName)) {
				omnHw = omnNode.getModel().createResource(hardwareTypeName);
				omnNode.addProperty(RDF.type, omnHw);
			} else {
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				omnHw = omnNode.getModel().createResource(uuid);
			}

			omnHw.addProperty(RDFS.label, hardwareTypeName);
			omnHw.addProperty(RDF.type, Omn_resource.HardwareType);
			for (Object hwObject : hw.getAny()) {
				AdExtractExt.tryExtractEmulabNodeType(hwObject, omnHw);
			}
			omnNode.addProperty(Omn_resource.hasHardwareType, omnHw);

		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractSliverType(Object rspecNodeObject,
			Resource omnResource) throws MissingRspecElementException {

		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<SliverType> sliverJaxb = (JAXBElement<SliverType>) rspecNodeObject;
			final SliverType sliverType = sliverJaxb.getValue();
			String sliverName = sliverType.getName();
			if (sliverName == null) {
				throw new MissingRspecElementException(
						"SliverTypeContents > name");
			}
			Resource sliverTypeResource = null;

			// // Note: Do not change sliver type here, as Fiteagle will
			// // not work
			// if (AbstractConverter.isUrl(sliverName)) {
			// // sliverTypeResource = omnResource.getModel().createResource();
			// omnResource.addProperty(RDF.type, omnResource.getModel()
			// .createResource(sliverName));
			// } else {
			// String sliverTypeUrl = "http://open-multinet.info/example#"
			// + sliverName;
			// // sliverTypeResource = omnResource.getModel().createResource();
			// omnResource.addProperty(RDF.type, omnResource.getModel()
			// .createResource(sliverTypeUrl));
			// }

			// override existing sliverType if blank
			StmtIterator existingSliverTypes = omnResource
					.listProperties(Omn_resource.hasSliverType);
			if (existingSliverTypes.hasNext()) {
				Resource existingSliver = existingSliverTypes.next()
						.getObject().asResource();
				// String existingSliverUri = existingSliver.getURI();
				// if (existingSliverUri == null) {
				if (!existingSliver.hasProperty(RDF.type,
						Omn_resource.SliverType)) {
					sliverTypeResource = existingSliver;
				} else {
					if (AbstractConverter.isUrl(sliverName)
							|| AbstractConverter.isUrn(sliverName)) {
						sliverTypeResource = omnResource.getModel()
								.createResource(sliverName);
					} else {
						String uuid = "urn:uuid:"
								+ UUID.randomUUID().toString();
						sliverTypeResource = omnResource.getModel()
								.createResource(uuid);
					}
					omnResource.addProperty(Omn_resource.hasSliverType,
							sliverTypeResource);
				}
			} else {
				if (AbstractConverter.isUrl(sliverName)
						|| AbstractConverter.isUrn(sliverName)) {
					sliverTypeResource = omnResource.getModel().createResource(
							sliverName);
					omnResource.addProperty(RDF.type, sliverTypeResource);
				} else {
					String uuid = "urn:uuid:" + UUID.randomUUID().toString();
					sliverTypeResource = omnResource.getModel().createResource(
							uuid);
				}
				omnResource.addProperty(Omn_resource.hasSliverType,
						sliverTypeResource);
			}

			sliverTypeResource.addProperty(Omn_lifecycle.hasSliverName,
					sliverName);
			sliverTypeResource.addProperty(RDF.type, Omn_resource.SliverType);

			if (!AbstractConverter.isUrl(sliverName)) {
				sliverName = HOST + sliverName;
			}
			omnResource.addProperty(Omn_lifecycle.canImplement, omnResource
					.getModel().createResource(sliverName));

			for (Object rspecSliverObject : sliverType.getAnyOrDiskImage()) {
				tryExtractCpus(rspecSliverObject, sliverTypeResource);
				tryExtractDiskImage(rspecSliverObject, sliverTypeResource);
				// tryExtractOsco(rspecSliverObject, sliverTypeResource);
			}
		} catch (final ClassCastException e) {
			AdExtract.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractCpus(Object rspecSliverObject,
			Resource omnSliver) {
		if (rspecSliverObject
				.getClass()
				.equals(info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc.class)) {
			Pc pc = (Pc) rspecSliverObject;

			if (pc.getCpus() != null) {
				omnSliver.addLiteral(Omn_domain_pc.hasCPU, pc.getCpus());
			}
		}
	}

	private static void tryExtractDiskImage(Object rspecSliverObject,
			Resource omnSliver) {

		if (rspecSliverObject instanceof JAXBElement) {
			// check if disk_image
			if (((JAXBElement<?>) rspecSliverObject).getDeclaredType().equals(
					SliverType.DiskImage.class)) {

				DiskImageContents diskImageContents = (DiskImageContents) ((JAXBElement<?>) rspecSliverObject)
						.getValue();

				// removed, as does not account for multiple disk images with
				// same url
				// String diskImageURL = diskImageContents.getUrl();
				// Resource diskImage = model.createResource(diskImageURL);
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource diskImage = omnSliver.getModel().createResource(uuid);
				diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);

				// add name info
				String name = diskImageContents.getName();
				diskImage.addLiteral(Omn_domain_pc.hasDiskimageLabel, name);
				// omnSliver.addProperty(Omn_lifecycle.canImplement, diskImage);
				omnSliver.addProperty(Omn_domain_pc.hasDiskImage, diskImage);

				String os = diskImageContents.getOs();
				if (os != null) {
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageOS, os);
				}

				String version = diskImageContents.getVersion();
				if (version != null) {
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageVersion,
							version);
				}

				String description = diskImageContents.getDescription();
				if (description != null) {
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageDescription,
							description);
				}

				String url = diskImageContents.getUrl();
				if (url != null) {
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageURI, url);
				}

				// System.out.println(rspecSliverObject.toString());
				@SuppressWarnings("unchecked")
				JAXBElement<DiskImage> diskImageElement = (JAXBElement<DiskImage>) rspecSliverObject;
				String defaultString = diskImageElement.getValue().getDefault();

				if (defaultString != null) {
					diskImage.addLiteral(Omn_domain_pc.diskimageDefault,
							defaultString);
				}
			}
		}
	}
}