package info.openmultinet.ontology.translators.geni.request;

import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.request.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.request.Bt;
import info.openmultinet.ontology.translators.geni.jaxb.request.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.request.Control;
import info.openmultinet.ontology.translators.geni.jaxb.request.Device;
import info.openmultinet.ontology.translators.geni.jaxb.request.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Dns;
import info.openmultinet.ontology.translators.geni.jaxb.request.Enodeb;
import info.openmultinet.ontology.translators.geni.jaxb.request.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.request.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.request.Gateway;
import info.openmultinet.ontology.translators.geni.jaxb.request.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Hss;
import info.openmultinet.ontology.translators.geni.jaxb.request.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.request.Location;
import info.openmultinet.ontology.translators.geni.jaxb.request.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Osco;
import info.openmultinet.ontology.translators.geni.jaxb.request.Position3D;
import info.openmultinet.ontology.translators.geni.jaxb.request.RoutableControlIp;
import info.openmultinet.ontology.translators.geni.jaxb.request.ServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Switch;
import info.openmultinet.ontology.translators.geni.jaxb.request.Ue;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

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
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from a request RSpec to create an OMN
 * model. For native RSpec elements.
 * 
 * @author robynloughnane
 *
 */
public class RequestExtract extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";
	private static final Logger LOG = Logger.getLogger(RequestExtract.class
			.getName());
	private static final String HOST = "http://open-multinet.info/example#";

	public static void extractDetails(Resource topology, Object o)
			throws MissingRspecElementException {

		JAXBElement<?> element = (JAXBElement<?>) o;
		if (element.getDeclaredType().equals(NodeContents.class)) {
			extractNodes(element, topology);
		} else if (element.getDeclaredType().equals(LinkContents.class)) {
			extractLinks(element, topology);
		} else {
			RequestExtract.LOG.log(Level.INFO, "Found unknown extension: " + o);
		}

	}

	private static void extractLinks(JAXBElement<?> element,
			final Resource topology) throws MissingRspecElementException {

		final LinkContents link = (LinkContents) element.getValue();
		final Model outputModel = topology.getModel();

		if (link.getClientId() == null) {
			throw new MissingRspecElementException("LinkContents > client_id ");
		}

		String componentManagerName = getComponentManagerName(link);
		String sliverTypeUrl = null;
		if (componentManagerName != null
				&& AbstractConverter.isUrn(componentManagerName)) {
			String[] parts = componentManagerName.split("\\+");
			if (parts.length > 1) {
				sliverTypeUrl = "http://" + parts[1] + "/" + link.getClientId();
			} else {
				sliverTypeUrl = HOST + link.getClientId();
			}
		} else {
			sliverTypeUrl = HOST + link.getClientId();
		}
		final Resource linkResource = outputModel.createResource(sliverTypeUrl);

		linkResource.addLiteral(Omn_resource.clientId, link.getClientId()); // required

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
			} else if (o.getClass().equals(LinkSharedVlan.class)) {
				RequestExtractExt.tryExtractSharedVlan(o, linkResource);
			} else {
				RequestExtract.LOG.log(Level.INFO,
						"Found unknown link extension: " + o);
			}
		}

		linkResource.addProperty(RDF.type, Omn_resource.Link);

		linkResource.addProperty(Omn.isResourceOf, topology);
		topology.addProperty(Omn.hasResource, linkResource);

	}

	private static String getComponentManagerName(LinkContents link)
			throws MissingRspecElementException {
		String componentManagerName = null;
		for (Object o : link.getAnyOrPropertyOrLinkType()) {

			if (o.getClass().equals(ComponentManager.class)) {
				final ComponentManager componentManager = (ComponentManager) o;
				componentManagerName = componentManager.getName();

				// name required
				if (componentManagerName == null) {
					throw new MissingRspecElementException(
							"component_manager > name");
				}
			}
		}
		return componentManagerName;
	}

	private static void extractComponentManager(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final ComponentManager componentManager = (ComponentManager) o;
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

	private static void extractLinkType(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final LinkType content = (LinkType) o;
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
			String sliverTypeUrl = HOST + linkName;
		}

		// class optional
		// TODO

	}

	private static void extractInterfaceRefs(JAXBElement<?> linkElement,
			Resource linkResource) {

		final InterfaceRefContents content = (InterfaceRefContents) linkElement
				.getValue();

		Resource interfaceResource = linkResource.getModel().createResource(
				HOST + content.getClientId());
		// interfaceResource.addProperty(Nml.isSink,
		// linkResource);
		// interfaceResource.addProperty(Nml.isSource,
		// linkResource);

		interfaceResource.addProperty(Omn_resource.clientId,
				content.getClientId());

		// linkResource.addProperty(Nml.hasPort,
		// interfaceResource);
		linkResource.addProperty(Omn_resource.hasInterface, interfaceResource);
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

		// linkPropertyResource.addProperty(Omn_resource.hasSink, destID);
		Resource destIdResource = null;
		if (AbstractConverter.isUrl(destID) || AbstractConverter.isUrn(destID)) {
			destIdResource = linkResource.getModel().createResource(destID);
		} else {
			destIdResource = linkResource.getModel().createResource(
					HOST + destID);
		}
		linkPropertyResource.addProperty(Omn_resource.hasSink, destIdResource);

		Resource sourceIDResource = null;
		if (AbstractConverter.isUrl(destID)
				|| AbstractConverter.isUrn(sourceID)) {
			sourceIDResource = linkResource.getModel().createResource(sourceID);
		} else {
			sourceIDResource = linkResource.getModel().createResource(
					HOST + sourceID);
		}
		linkPropertyResource.addProperty(Omn_resource.hasSource,
				sourceIDResource);

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
		// interfaceResource.addProperty(Nml.isSink,
		// linkResource);
		// interfaceResource.addProperty(Nml.isSource,
		// linkResource);
		// linkResource.addProperty(Nml.hasPort,
		// interfaceResource);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void extractNodes(JAXBElement<?> jaxbElement,
			final Resource topology) throws MissingRspecElementException {

		final NodeContents node = (NodeContents) jaxbElement.getValue();
		final Model model = topology.getModel();

		Resource omnResource = null;
		String clientId = node.getClientId();
		if (clientId == null) {
			throw new MissingRspecElementException("NodeContents > client_id");
		}

		if (AbstractConverter.isUrl(clientId)
				|| AbstractConverter.isUrn(clientId)) {
			omnResource = model.createResource(clientId);
		} else {
			omnResource = model.createResource(AbstractConverter.NAMESPACE
					+ node.getClientId());
		}

		omnResource.addProperty(RDFS.label, clientId);
		omnResource.addProperty(RDF.type, Omn_resource.Node);
		extractNodeDetails(omnResource, node);

		List<Object> anyOrRelationOrLocation = node
				.getAnyOrRelationOrLocation();
		if (anyOrRelationOrLocation.size() > 0) {
			for (Object o : anyOrRelationOrLocation) {
				if (o instanceof JAXBElement) {
					JAXBElement element = (JAXBElement) o;
					tryExtractSliverType(omnResource, element);
					tryExtractServices(omnResource, element);
					tryExtractInterfaces(omnResource, element);
					tryExtractHardwareType(omnResource, element);
					tryExtractLocation(omnResource, element);
				} else if (o.getClass().equals(Fd.class)) {
					RequestExtractExt.tryExtractEmulabFd(omnResource, o);
				} else if (o.getClass().equals(Ue.class)) {
					RequestExtractExt.tryExtractUe(omnResource, o);
				} else if (o.getClass().equals(Epc.class)) {
					RequestExtractExt.tryExtractEpc(omnResource, o);
				} else if (o.getClass().equals(Control.class)) {
					RequestExtractExt.tryExtractFivegControl(omnResource, o);
				} else if (o.getClass().equals(Enodeb.class)) {
					RequestExtractExt.tryExtractFivegEnodeb(omnResource, o);
				} else if (o.getClass().equals(Bt.class)) {
					RequestExtractExt.tryExtractFivegBt(omnResource, o);
				} else if (o.getClass().equals(Hss.class)) {
					RequestExtractExt.tryExtractFivegHss(omnResource, o);
				} else if (o.getClass().equals(Switch.class)) {
					RequestExtractExt.tryExtractFivegSwitch(omnResource, o);
				} else if (o.getClass().equals(Gateway.class)) {
					RequestExtractExt.tryExtractFivegGateway(omnResource, o);
				} else if (o.getClass().equals(Dns.class)) {
					RequestExtractExt.tryExtractFivegDns(omnResource, o);
				} else if (o.getClass().equals(AccessNetwork.class)) {
					RequestExtractExt.tryExtractAccessNetwork(omnResource, o);
				} else if (o.getClass().equals(Device.class)) {
					RequestExtractExt.tryExtractAcs(omnResource, o);
				} else if (o.getClass().equals(Monitoring.class)) {
					RequestExtractExt.tryExtractMonitoring(omnResource, o);
				} else if (o.getClass().equals(Osco.class)) {
					RequestExtractExt.tryExtractOsco(omnResource, o);
				} else if (o.getClass().equals(RoutableControlIp.class)) {
					omnResource.addProperty(Omn_domain_pc.routableControlIp,
							"true");
				} else if (o.getClass().equals(Location.class)) {
					RequestExtractExt.extractJfedLocation(omnResource, o);
				} else {
					RequestExtract.LOG.log(Level.INFO,
							"Found unknown ElementNSImpl extension: " + o);
				}
			}
		}

		omnResource.addProperty(Omn.isResourceOf, topology);
		topology.addProperty(Omn.hasResource, omnResource);

	}

	private static void tryExtractInterfaces(Resource omnResource,
			JAXBElement element) throws MissingRspecElementException {

		if (element.getDeclaredType().equals(InterfaceContents.class)) {
			Model outputModel = omnResource.getModel();
			InterfaceContents content = (InterfaceContents) element.getValue();

			// client id is required
			String clientId = content.getClientId();
			if (clientId == null) {
				throw new MissingRspecElementException(
						"NodeContents > client_id");
			}

			Resource interfaceResource;
			if (AbstractConverter.isUrl(clientId)
					|| AbstractConverter.isUrn(clientId)) {
				interfaceResource = outputModel.createResource(content
						.getClientId());
			} else {
				interfaceResource = outputModel.createResource(HOST
						+ content.getClientId());
			}
			// interfaceResource.addProperty(RDF.type,
			// Nml.Port);
			// omnResource.addProperty(Nml.hasPort,
			// interfaceResource);

			List<Object> interfaces = content.getAnyOrIp();
			// iterate through the interfaces and add to model
			for (int i = 0; i < interfaces.size(); i++) {
				Object interfaceObject = interfaces.get(i);
				tryExtractIPAddress(interfaceObject, interfaceResource);
			}

			interfaceResource.addProperty(RDF.type, Omn_resource.Interface);
			omnResource.addProperty(Omn_resource.hasInterface,
					interfaceResource);
			if (content.getClientId() != null) {
				interfaceResource.addProperty(Omn_resource.clientId,
						content.getClientId());
			}

			if (content.getComponentId() != null) {
				URI uri = URI.create(content.getComponentId());
				interfaceResource.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}
		}
	}

	private static void tryExtractIPAddress(Object interfaceObject,
			Resource interfaceResource) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<IpContents> availablityJaxb = (JAXBElement<IpContents>) interfaceObject;
			final IpContents availability = availablityJaxb.getValue();

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnIpAddress = interfaceResource.getModel()
					.createResource(uuid);
			omnIpAddress.addProperty(RDF.type, Omn_resource.IPAddress);

			// add address (required)
			if (availability.getAddress() == null) {
				throw new MissingRspecElementException(
						"rspec:IpContents > Address");
			}
			omnIpAddress.addLiteral(Omn_resource.address,
					availability.getAddress());

			// add netmask
			if (availability.getNetmask() != null
					&& !availability.getNetmask().equals("")) {
				omnIpAddress.addLiteral(Omn_resource.netmask,
						availability.getNetmask());
			}

			// add type
			if (availability.getType() != null) {
				omnIpAddress.addLiteral(Omn_resource.type,
						availability.getType());
			}

			interfaceResource.addProperty(Omn_resource.hasIPAddress,
					omnIpAddress);

		} catch (final ClassCastException e) {
			RequestExtract.LOG.finer(e.getMessage());
		}

	}

	private static Resource extractNodeDetails(Resource omnResource,
			NodeContents node) throws MissingRspecElementException {
		Model model = omnResource.getModel();
		if (node.getClientId() == null) {
			throw new MissingRspecElementException("NodeContents > client_id");
		} else {
			omnResource.addProperty(Omn_lifecycle.hasID, node.getClientId());
		}

		Resource implementedBy = null;
		if (null != node.getComponentId() && !node.getComponentId().isEmpty()) {

			String componentId = node.getComponentId();
			implementedBy = model.createResource(CommonMethods
					.generateUrlFromUrn(componentId));

			URI uri = URI.create(componentId);
			omnResource.addLiteral(Omn_lifecycle.hasComponentID, uri);

			omnResource.addProperty(Omn_lifecycle.implementedBy, implementedBy);
			if (null != node.getComponentName()
					&& !node.getComponentName().isEmpty()) {
				implementedBy.addProperty(Omn_lifecycle.hasComponentName,
						node.getComponentName());
			}
		}

		if (null != node.isExclusive()) {
			omnResource.addProperty(Omn_resource.isExclusive,
					model.createTypedLiteral(node.isExclusive()));
		}

		if (node.getComponentManagerId() != null) {
			RDFNode manager = ResourceFactory.createResource(node
					.getComponentManagerId());
			// TODO: must be URI here
			omnResource.addProperty(Omn_lifecycle.managedBy, manager);
		}

		return implementedBy;
	}

	// public static void extractRelationOrLocation(final Resource omnResource,
	// List<Object> anyOrRelationOrLocation)
	// throws MissingRspecElementException {
	// for (Object o : anyOrRelationOrLocation) {
	// if (o instanceof JAXBElement) {
	// JAXBElement element = (JAXBElement) o;
	// tryExtractSliverType(omnResource, element);
	// tryExtractServices(omnResource, element);
	// tryExtractInterfaces(omnResource, element);
	// tryExtractHardwareType(omnResource, element);
	// tryExtractLocation(omnResource, element);
	// } else if (o.getClass().equals(Monitoring.class)) {
	// tryExtractMonitoring(omnResource, o);
	// } else if (o.getClass().equals(Osco.class)) {
	// tryExtractOsco(omnResource, o);
	// } else if (o.getClass().equals(RoutableControlIp.class)) {
	// omnResource
	// .addProperty(Omn_domain_pc.routableControlIp, "true");
	// } else if (o.getClass().equals(Location.class)) {
	// extractJfedLocation(omnResource, o);
	// } else {
	// RequestConverter.LOG.log(Level.INFO,
	// "Found unknown ElementNSImpl extension: " + o);
	// }
	// }
	// }

	private static void tryExtractLocation(Resource omnNode, JAXBElement element)
			throws MissingRspecElementException {
		if (element.getDeclaredType().equals(LocationContents.class)) {

			final JAXBElement<LocationContents> locationJaxb = (JAXBElement<LocationContents>) element;
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
				// String latitude = location.getLatitude();
				// String longitude = location.getLongitude();
				// String country = location.getCountry();
				//
				// // country is required, when location is specified
				// if (country == null) {
				// throw new MissingRspecElementException(
				// "LocationContents > country");
				// } else {
				// omnNode.addProperty(Geonames.countryCode, country);
				// }
				//
				// if (latitude != null) {
				// omnNode.addProperty(Geo.lat, latitude);
				// }
				// if (longitude != null) {
				// omnNode.addProperty(Geo.long_, longitude);
				// }
			}
		}
	}

	private static void tryExtractHardwareType(Resource omnNode,
			JAXBElement element) throws MissingRspecElementException {
		if (element.getDeclaredType().equals(HardwareTypeContents.class)) {

			final HardwareTypeContents hw = (HardwareTypeContents) element
					.getValue();

			String hardwareTypeName = hw.getName();
			if (hardwareTypeName == null) {
				throw new MissingRspecElementException(
						"HardwareTypeContents > name");
			}

			final Resource omnHw;
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
				RequestExtractExt.tryExtractEmulabNodeType(hwObject, omnHw);
			}
			omnNode.addProperty(Omn_resource.hasHardwareType, omnHw);

		}
	}

	private static void tryExtractServices(Resource omnResource,
			JAXBElement element) {
		if (element.getDeclaredType().equals(ServiceContents.class)) {
			ServiceContents serviceContents = (ServiceContents) element
					.getValue();
			for (Object service : serviceContents.getAnyOrLoginOrInstall()) {
				if (service instanceof JAXBElement) {
					JAXBElement serviceElement = (JAXBElement) service;
					extractInstallService(omnResource.getModel(), omnResource,
							serviceElement);
					extractExecuteService(omnResource.getModel(), omnResource,
							serviceElement);
					extractLoginService(omnResource.getModel(), omnResource,
							serviceElement);
				}
			}
		}
	}

	private static void tryExtractSliverType(Resource omnResource,
			JAXBElement element) throws MissingRspecElementException {
		if (element.getDeclaredType().equals(NodeContents.SliverType.class)) {
			NodeContents.SliverType sliverType = (NodeContents.SliverType) element
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
				// sliverTypeResource = omnResource.getModel().createResource(
				// sliverName);
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverName));
			} else {
				// set type of node
				String sliverTypeUrl = HOST + sliverName;
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverTypeUrl));

				// create sliver type blank node
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				sliverTypeResource = omnResource.getModel()
						.createResource(uuid);

			}

			omnResource.addProperty(Omn_resource.hasSliverType,
					sliverTypeResource);
			sliverTypeResource.addProperty(Omn_lifecycle.hasSliverName,
					sliverName);
			sliverTypeResource.addProperty(RDF.type, Omn_resource.SliverType);

			for (Object rspecSliverObject : sliverType.getAnyOrDiskImage()) {
				tryExtractDiskImage(rspecSliverObject, sliverTypeResource);
			}
		}
	}

	private static void tryExtractDiskImage(Object rspecSliverObject,
			Resource omnSliver) throws MissingRspecElementException {

		if (rspecSliverObject instanceof JAXBElement) {
			// check if disk_image
			if (((JAXBElement<?>) rspecSliverObject).getDeclaredType().equals(
					DiskImageContents.class)) {

				DiskImageContents diskImageContents = (DiskImageContents) ((JAXBElement<?>) rspecSliverObject)
						.getValue();

				String diskImageURL = diskImageContents.getUrl();

				Resource diskImage = null;
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

				String url = diskImageContents.getUrl();
				if (url != null) {
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageURI, url);
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

	private static void extractLoginService(Model model, Resource omnResource,
			JAXBElement serviceElement) {
		if (serviceElement.getDeclaredType().equals(LoginServiceContents.class)) {
			LoginServiceContents loginObject = (LoginServiceContents) serviceElement
					.getValue();

			Resource loginService = model
					.createResource(Omn_service.LoginService);

			if (loginObject.getAuthentication() != null) {
				loginService.addProperty(Omn_service.authentication,
						loginObject.getAuthentication());
			}
			if (loginObject.getHostname() != null) {
				loginService.addProperty(Omn_service.hostname,
						loginObject.getHostname());
			}
			if (loginObject.getPort() != null) {
				loginService.addProperty(Omn_service.port,
						loginObject.getPort());
			}
			loginService.addProperty(RDFS.label, "LoginService");
			omnResource.addProperty(Omn.hasService, loginService);
		}

	}

	public static void extractExecuteService(final Model model,
			final Resource omnResource, JAXBElement serviceElement) {
		if (serviceElement.getDeclaredType().equals(
				ExecuteServiceContents.class)) {
			ExecuteServiceContents execObject = (ExecuteServiceContents) serviceElement
					.getValue();

			Resource execService = model
					.createResource(Omn_service.ExecuteService);

			execService.addProperty(Omn_service.command,
					execObject.getCommand());
			execService.addProperty(Omn_service.shell, execObject.getShell());

			omnResource.addProperty(Omn.hasService, execService);
		}
	}

	public static void extractInstallService(final Model model,
			final Resource omnResource, JAXBElement serviceElement) {
		if (serviceElement.getDeclaredType().equals(
				InstallServiceContents.class)) {
			InstallServiceContents serviceObject = (InstallServiceContents) serviceElement
					.getValue();
			Resource installService = model
					.createResource(Omn_service.InstallService);

			installService.addProperty(Omn_service.installPath,
					serviceObject.getInstallPath());
			installService.addProperty(Omn_service.url, serviceObject.getUrl());

			omnResource.addProperty(Omn.hasService, installService);
		}
	}
}