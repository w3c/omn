package info.openmultinet.ontology.translators.geni.manifest;

import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Device;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliverInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Position3D;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Gateway;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.math.BigDecimal;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from a manifest RSpec to create an OMN
 * model. For native RSpec elements.
 * 
 * @author robynloughnane
 *
 */
public class ManifestExtract extends AbstractConverter {

	private static final String HOST = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger.getLogger(ManifestExtract.class
			.getName());

	public static void extractDetails(final Model model, Resource topology,
			Object o) throws MissingRspecElementException {
		JAXBElement<?> element = (JAXBElement<?>) o;

		if (element.getDeclaredType().equals(NodeContents.class)) {
			extractNode(element, topology);
		} else if (element.getDeclaredType().equals(LinkContents.class)) {
			extractLink(element, topology);
		}
	}

	private static void extractNode(JAXBElement<?> element, Resource topology)
			throws MissingRspecElementException {
		NodeContents node = (NodeContents) element.getValue();

		String sliverID = node.getSliverId();
		Resource omnResource = null;
		if (sliverID != null) {
			omnResource = topology.getModel().createResource(
					CommonMethods.generateUrlFromUrn(sliverID));
			omnResource.addProperty(Omn_lifecycle.hasSliverID, sliverID);

		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			omnResource = topology.getModel().createResource(uuid);
		}

		// .createResource(parseSliverID(node.getSliverId()));

		// client_id is required by manifest-common.xsd
		String clientId = node.getClientId();
		if (clientId == null) {
			throw new MissingRspecElementException("NodeContents > client_id");
		}
		omnResource.addProperty(Omn_lifecycle.hasID, clientId);

		omnResource.addProperty(RDFS.label, node.getClientId());
		omnResource.addProperty(RDF.type, Omn.Resource);

		if (null != node.isExclusive()) {
			omnResource.addProperty(Omn_resource.isExclusive, topology
					.getModel().createTypedLiteral(node.isExclusive()));
		}

		extractComponentDetails(node, omnResource);

		for (Object nodeDetailObject : node.getAnyOrRelationOrLocation()) {
			if (nodeDetailObject instanceof JAXBElement) {
				JAXBElement<?> nodeDetailElement = (JAXBElement<?>) nodeDetailObject;

				tryExtractHardwareType(nodeDetailElement, omnResource);
				extractLocation(nodeDetailElement, omnResource);
				extractSliverType(nodeDetailElement, omnResource);
				extractServices(nodeDetailElement, omnResource,
						topology.getModel());
				extractInterfaces(nodeDetailElement, omnResource,
						topology.getModel());
			} else if (nodeDetailObject.getClass().equals(Fd.class)) {
				ManifestExtractExt.tryExtractEmulabFd(omnResource,
						nodeDetailObject);
			} else if (nodeDetailObject.getClass().equals(GeniSliverInfo.class)) {
				ManifestExtractExt.extractGeniSliverInfo(nodeDetailObject,
						omnResource);
			} else if (nodeDetailObject.getClass().equals(Gateway.class)) {
				ManifestExtractExt.tryExtractGateway(omnResource,
						nodeDetailObject);
			} else if (nodeDetailObject.getClass().equals(Epc.class)) {
				ManifestExtractExt.tryExtractEpc(omnResource, nodeDetailObject);
			} else if (nodeDetailObject.getClass().equals(AccessNetwork.class)) {
				ManifestExtractExt.tryExtractAccessNetwork(omnResource,
						nodeDetailObject);
			} else if (nodeDetailObject.getClass().equals(Ue.class)) {
				ManifestExtractExt.tryExtractUserEquipment(omnResource,
						nodeDetailObject);
			} else if (nodeDetailObject.getClass().equals(Device.class)) {
				ManifestExtractExt.tryExtractAcs(omnResource, nodeDetailObject);
			} else if (nodeDetailObject
					.getClass()
					.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Reservation.class)) {

				ManifestExtractExt.extractReservation(nodeDetailObject,
						omnResource);
			} else if (nodeDetailObject
					.getClass()
					.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Monitoring.class)) {
				ManifestExtractExt.extractMonitoring(nodeDetailObject,
						omnResource);
			} else if (nodeDetailObject
					.getClass()
					.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Osco.class)) {
				ManifestExtractExt.extractOsco(nodeDetailObject, omnResource);
			} else {
				ManifestExtract.LOG.log(Level.INFO,
						"Found unknown extsion within node: "
								+ nodeDetailObject.getClass());
			}
		}

		topology.addProperty(Omn.hasResource, omnResource);
	}

	private static void tryExtractHardwareType(JAXBElement<?> rspecNodeObject,
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
				ManifestExtractExt.tryExtractEmulabNodeType(hwObject, omnHw);
			}
			omnNode.addProperty(Omn_resource.hasHardwareType, omnHw);

		} catch (final ClassCastException e) {
			ManifestExtract.LOG.finer(e.getMessage());
		}

	}

	private static void extractComponentDetails(NodeContents node,
			Resource omnResource) {
		if (node.getComponentManagerId() != null) {
			RDFNode manager = ResourceFactory.createResource(node
					.getComponentManagerId());
			omnResource.addProperty(Omn_lifecycle.managedBy, manager);
		}

		// component id is not required
		String componentIdOriginal = node.getComponentId();
		if (componentIdOriginal != null) {
			String componentId = CommonMethods
					.generateUrlFromUrn(componentIdOriginal);
			Resource componentIDResource = omnResource.getModel()
					.createResource(componentId);
			omnResource.addProperty(Omn_lifecycle.implementedBy,
					componentIDResource);
		}

		// component name is not required
		String componentName = node.getComponentName();
		if (componentName != null) {
			omnResource.addProperty(Omn_lifecycle.hasComponentName,
					componentName);
		}

	}

	private static void extractLink(JAXBElement<?> element, Resource topology)
			throws MissingRspecElementException {
		LinkContents link = (LinkContents) element.getValue();
		Resource linkResource;
		String sliverID = link.getSliverId();
		if (sliverID != null) {
			linkResource = topology.getModel().createResource(
					CommonMethods.generateUrlFromUrn(sliverID));
			linkResource.addProperty(Omn_lifecycle.hasSliverID, sliverID);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			linkResource = topology.getModel().createResource(uuid);
		}

		if (link.getClientId() == null) {
			throw new MissingRspecElementException("LinkContents > client_id ");
		}

		linkResource.addLiteral(Omn_resource.clientId, link.getClientId()); // required
		linkResource.addProperty(RDF.type, Omn_resource.Link);

		if (link.getVlantag() != null) {
			linkResource.addLiteral(Omn_domain_pc.vlanTag, link.getVlantag());
		}
		// Get source and sink interfaces
		@SuppressWarnings("unchecked")
		List<Object> linkContents = link.getAnyOrPropertyOrLinkType();
		for (Object linkObject : linkContents) {
			if (linkObject instanceof JAXBElement) {
				JAXBElement<?> linkElement = (JAXBElement<?>) linkObject;

				extractIntefaceRef(linkElement, linkResource);
			} else if (linkObject.getClass().equals(ComponentManager.class)) {
				extractComponentManager(linkObject, linkResource);
			} else if (linkObject.getClass().equals(LinkType.class)) {
				extractLinkType(linkObject, linkResource);
			} else if (linkObject.getClass().equals(GeniSliverInfo.class)) {
				ManifestExtractExt.extractGeniSliverInfo(linkObject,
						linkResource);
			} else {
				ManifestExtract.LOG.log(Level.INFO,
						"Unknown extension witin link");
			}
		}
		topology.addProperty(Omn.hasResource, linkResource);
	}

	private static void extractLinkType(Object linkObject, Resource linkResource)
			throws MissingRspecElementException {

		LinkType content = (LinkType) linkObject;
		String linkName = content.getName();

		// name required
		if (linkName == null) {
			throw new MissingRspecElementException("link_type > name");
		}
		// linkResource.addProperty(Omn_lifecycle.hasLinkName, linkName);
		linkResource.addProperty(RDFS.label, linkName);

		// add link type as RDF type, analog to sliver type
		if (AbstractConverter.isUrl(linkName)) {
			linkResource.addProperty(RDF.type, linkResource.getModel()
					.createResource(linkName));
		} else {
			// set type of node
			String sliverTypeUrl = HOST + linkName;
			linkResource.addProperty(RDF.type, linkResource.getModel()
					.createResource(sliverTypeUrl));
		}
	}

	private static void extractComponentManager(Object linkObject,
			Resource linkResource) throws MissingRspecElementException {

		final ComponentManager componentManager = (ComponentManager) linkObject;
		String componentManagerName = componentManager.getName();

		// name required
		if (componentManagerName == null) {
			throw new MissingRspecElementException("component_manager > name");
		}

		// add managedBy property if the component name is a URN
		if (AbstractConverter.isUrn(componentManagerName)) {
			RDFNode componentManagerResource = ResourceFactory
					.createResource(componentManagerName);
			linkResource.addProperty(Omn_lifecycle.managedBy,
					componentManagerResource);
		}
		URI uri = URI.create(componentManagerName);
		if (uri != null) {
			linkResource.addLiteral(Omn_lifecycle.hasComponentManagerName, uri);
		}
	}

	private static void extractIntefaceRef(JAXBElement<?> linkElement,
			Resource linkResource) {
		if (linkElement.getDeclaredType().equals(InterfaceRefContents.class)) {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource interfaceResource = linkResource.getModel()
					.createResource(uuid);
			interfaceResource.addProperty(RDF.type, Omn_resource.Interface);

			InterfaceRefContents interfaceRefContents = (InterfaceRefContents) linkElement
					.getValue();

			String clientId = interfaceRefContents.getClientId();
			interfaceResource.addProperty(Omn_resource.clientId, clientId);

			String sliverId = interfaceRefContents.getSliverId();
			if (sliverId != null) {
				interfaceResource.addProperty(Omn_lifecycle.hasSliverID,
						sliverId);
			}

			String componentId = interfaceRefContents.getComponentId();
			if (componentId != null) {
				// Resource componentIdResource = linkResource.getModel()
				// .createResource(componentId);
				URI uri = URI.create(componentId);
				interfaceResource.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}

			linkResource.addProperty(Omn_resource.hasInterface,
					interfaceResource);
		}
	}

	private static void extractLocation(JAXBElement<?> nodeDetailElement,
			Resource omnResource) throws MissingRspecElementException {
		// check if type is location
		if (nodeDetailElement.getDeclaredType().equals(LocationContents.class)) {

			// get value of the element
			LocationContents location = (LocationContents) nodeDetailElement
					.getValue();

			if (location != null) {

				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource locationResource = omnResource.getModel()
						.createResource(uuid);
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
					System.out.println(object.getClass());

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

				omnResource.addProperty(Omn_resource.hasLocation,
						locationResource);
			}
		}
	}

	private static void extractSliverType(JAXBElement<?> nodeDetailElement,
			Resource omnResource) throws MissingRspecElementException {
		if (nodeDetailElement.getDeclaredType().equals(
				NodeContents.SliverType.class)) {

			NodeContents.SliverType sliverType = (NodeContents.SliverType) nodeDetailElement
					.getValue();

			String sliverName = sliverType.getName();
			if (sliverName == null) {
				throw new MissingRspecElementException(
						"SliverTypeContents > name");
			}
			Resource sliverTypeResource = null;
			// Note: Do not change sliver type here, as Fiteagle will
			// not work
			if (AbstractConverter.isUrl(sliverName)) {
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				sliverTypeResource = omnResource.getModel()
						.createResource(uuid);
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverName));
			} else {
				String sliverTypeUrl = HOST + sliverName;
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				sliverTypeResource = omnResource.getModel()
						.createResource(uuid);
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverTypeUrl));
			}

			// Resource sliver =
			// model.createResource(UUID.randomUUID().toString());
			// if (sliverType.getName() != null) {
			// if (AbstractConverter.isUrl(sliverType.getName())) {
			// sliver.addProperty(RDF.type, sliverType.getName());
			// }
			// sliver.addProperty(RDFS.label,
			// AbstractConverter.getName(sliverType.getName()));
			// }
			omnResource.addProperty(Omn_resource.hasSliverType,
					sliverTypeResource);
			sliverTypeResource.addProperty(Omn_lifecycle.hasSliverName,
					sliverName);
			sliverTypeResource.addProperty(RDF.type, Omn_resource.SliverType);

			List<Object> sliverContents = sliverType.getAnyOrDiskImage();
			for (int i = 0; i < sliverContents.size(); i++) {
				Object sliverObject = sliverContents.get(i);
				tryExtractDiskImage(sliverObject, sliverTypeResource);
			}
			// omnResource.addProperty(RDF.type, sliverTypeResource);
		}
	}

	private static void tryExtractDiskImage(Object sliverObject,
			Resource omnSliver) throws MissingRspecElementException {
		if (sliverObject instanceof JAXBElement) {
			// check if disk_image
			if (((JAXBElement<?>) sliverObject).getDeclaredType().equals(
					DiskImageContents.class)) {

				// Resource diskImage =
				// sliver.getModel().createResource(UUID.randomUUID().toString());
				// diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);
				DiskImageContents diskImageContents = (DiskImageContents) ((JAXBElement<?>) sliverObject)
						.getValue();
				//
				// // add name info
				// String name = diskImageContents.getName();
				// if (name != null && !name.equals("")) {
				// diskImage.addLiteral(Omn_domain_pc.hasDiskimageLabel, name);
				// }
				// // add version info
				// String version = diskImageContents.getVersion();
				// if (version != null && !version.equals("")) {
				// diskImage.addLiteral(Omn_domain_pc.hasDiskimageVersion,
				// version);
				// }
				// sliver.addProperty(Omn.hasResource, diskImage);

				String diskImageURL = diskImageContents.getUrl();
				Resource diskImage;
				if (diskImageURL != null) {
					diskImage = omnSliver.getModel().createResource(
							diskImageURL);
				} else {
					String uuid = "urn:uuid:" + UUID.randomUUID().toString();
					diskImage = omnSliver.getModel().createResource(uuid);
				}
				diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);

				// add name info
				String name = diskImageContents.getName();
				if (name == null) {
					throw new MissingRspecElementException(
							"DiskImageContents > name");
				}
				diskImage.addLiteral(Omn_domain_pc.hasDiskimageLabel, name);

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

				// check that does not extract twice
				boolean alreadyExists = false;
				if (omnSliver.hasProperty(Omn_domain_pc.hasDiskImage)) {
					StmtIterator diskImages = omnSliver
							.listProperties(Omn_domain_pc.hasDiskImage);
					while (diskImages.hasNext()) {
						Statement diskImageStatement = diskImages.next();
						Resource diskImageResource = diskImageStatement
								.getObject().asResource();
						if (diskImageResource
								.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
							String diskImageLabel = diskImageResource
									.getProperty(
											Omn_domain_pc.hasDiskimageLabel)
									.getObject().asLiteral().getString();
							if (diskImageLabel.equals(name)) {
								alreadyExists = true;
							}
						}
					}
				}

				if (!alreadyExists) {
					omnSliver
							.addProperty(Omn_domain_pc.hasDiskImage, diskImage);
				}
			}
		}
	}

	private static void extractInterfaces(JAXBElement<?> nodeDetailElement,
			Resource omnResource, Model model) {

		// check if type is interface
		if (nodeDetailElement.getDeclaredType().equals(InterfaceContents.class)) {

			// get value of the element
			InterfaceContents interfaceContents = (InterfaceContents) nodeDetailElement
					.getValue();
			List<Object> interfaces = interfaceContents.getAnyOrIpOrHost();
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnInterface = model.createResource(uuid);

			if (interfaceContents.getMacAddress() != null) {
				omnInterface.addProperty(Omn_resource.macAddress,
						interfaceContents.getMacAddress());
			}

			String clientId = interfaceContents.getClientId();
			if (clientId != null) {
				omnInterface.addProperty(Omn_resource.clientId, clientId);
			}

			String sliverId = interfaceContents.getSliverId();
			if (sliverId != null) {
				omnInterface.addProperty(Omn_lifecycle.hasSliverID, sliverId);
			}

			String componentId = interfaceContents.getComponentId();
			if (componentId != null) {
				Resource componentIdResource = omnInterface.getModel()
						.createResource(componentId);
				URI uri = URI.create(componentId);
				// omnInterface.addProperty(Omn_lifecycle.hasComponentID,
				// componentIdResource);
				omnInterface.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}

			// iterate through the interfaces and add to model
			for (int i = 0; i < interfaces.size(); i++) {
				Object interfaceObject = interfaces.get(i);
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource omnIpAddress = model.createResource(uuid2);
				tryExtractIPAddress(interfaceObject, omnInterface, omnIpAddress);

			}

			// add interface to node
			if (omnInterface != null) {
				omnResource
						.addProperty(Omn_resource.hasInterface, omnInterface);
			}
		}
	}

	private static void extractServices(JAXBElement<?> nodeDetailElement,
			Resource omnResource, Model model) {
		// check if type is Services
		if (nodeDetailElement.getDeclaredType().equals(ServiceContents.class)) {

			// get value of the element
			ServiceContents service = (ServiceContents) nodeDetailElement
					.getValue();
			List<Object> services = service.getAnyOrLoginOrInstall();

			// add blank service, if blank service node in rspec
			if (services.size() == 0) {
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource omnService = model.createResource(uuid);
				omnResource.addProperty(Omn.hasService, omnService);
			}

			// iterate through the Services and add to model
			for (int i = 0; i < services.size(); i++) {

				Object serviceObject = services.get(i);
				Resource omnService = null;
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				omnService = model.createResource(uuid);
				// if login service
				if (serviceObject instanceof JAXBElement) {
					final JAXBElement<?> serviceObjectJaxb = (JAXBElement<?>) serviceObject;
					final Class<?> serviceType = serviceObjectJaxb
							.getDeclaredType();

					if (serviceType.equals(LoginServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.LoginService);
						omnService.addProperty(RDFS.label, "LoginService");
						LoginServiceContents serviceValue = (LoginServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add authentication info
						String authentication = serviceValue
								.getAuthentication();
						omnService.addLiteral(Omn_service.authentication,
								authentication);

						// add hostname info
						String hostname = serviceValue.getHostname();
						if (hostname != null) {
							omnService.addLiteral(Omn_service.hostname,
									hostname);
						}
						// add port info
						String portString = serviceValue.getPort();
						if (portString != null) {
							int port = Integer.parseInt(portString);
							omnService.addLiteral(Omn_service.port, port);
						}
						// add username info
						String username = serviceValue.getUsername();
						if (username != null) {
							omnService.addLiteral(Omn_service.username,
									username);
						}
					}
					// if execute service
					if (serviceType.equals(ExecuteServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.ExecuteService);
						ExecuteServiceContents serviceValue = (ExecuteServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add command info
						String command = serviceValue.getCommand();
						omnService.addLiteral(Omn_service.command, command);

						// add shell info
						String shell = serviceValue.getShell();
						omnService.addLiteral(Omn_service.shell, shell);
					}

					// if install service
					if (serviceType.equals(InstallServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.InstallService);
						InstallServiceContents serviceValue = (InstallServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add install path info
						String installPath = serviceValue.getInstallPath();
						omnService.addLiteral(Omn_service.installPath,
								installPath);

						// add url path info
						String url = serviceValue.getUrl();
						URI urlURI = URI.create(url);
						omnService.addLiteral(Omn_service.url, urlURI);

					}
				} else if (serviceObject
						.getClass()
						.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.ServicesPostBootScript.class)) {
					omnService
							.addProperty(RDF.type, Omn_service.PostBootScript);
					ManifestExtractExt.extractPostBootScript(serviceObject,
							omnService);
				} else if (serviceObject
						.getClass()
						.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Proxy.class)) {
					omnService.addProperty(RDF.type, Omn_service.Proxy);
					ManifestExtractExt.extractProxyService(serviceObject,
							omnService);
				}

				// add service to node
				if (omnService != null) {
					omnResource.addProperty(Omn.hasService, omnService);
				}
			}
		}
	}

	private static void tryExtractIPAddress(Object rspecNodeObject,
			Resource omnNode, Resource omnIpAddress) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<IpContents> availablityJaxb = (JAXBElement<IpContents>) rspecNodeObject;
			final IpContents availability = availablityJaxb.getValue();

			omnIpAddress.addLiteral(Omn_resource.address,
					availability.getAddress());
			if (availability.getNetmask() != null
					&& !availability.getNetmask().equals("")) {
				omnIpAddress.addLiteral(Omn_resource.netmask,
						availability.getNetmask());
			}
			omnIpAddress.addLiteral(Omn_resource.type, availability.getType());
			omnNode.addProperty(Omn_resource.hasIPAddress, omnIpAddress);

		} catch (final ClassCastException e) {
			ManifestExtract.LOG.finer(e.getMessage());
		}
	}

}