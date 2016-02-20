package info.openmultinet.ontology.translators.geni.manifest;

import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ApnContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ControlAddressContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Device;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ENodeBContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.EpcIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliceInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliverInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.PDNGatewayContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ParameterContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Proxy;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Reservation;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServicesPostBootScript;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.UeHardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.FiveGIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Gateway;
import info.openmultinet.ontology.vocabulary.Fiveg;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;
import info.openmultinet.ontology.vocabulary.Osco;

import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from a manifest RSpec to create an OMN
 * model. These methods are for non-native RSpec extensions.
 * 
 * @author robynloughnane
 *
 */
public class ManifestExtractExt extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(ManifestExtractExt.class
			.getName());

	public static void extractGeniSliceInfo(Resource topology, Object o) {

		// get value of the element
		GeniSliceInfo geniSliceInfo = (GeniSliceInfo) o;
		OntClass stateClass = null;
		String stateString = geniSliceInfo.getState();
		stateClass = CommonMethods.convertGeniStateToOmn(stateString);
		// switch (stateString) {
		// case "ready_busy":
		// stateClass = Omn_lifecycle.Active;
		// break;
		// case "allocated":
		// stateClass = Omn_lifecycle.Allocated;
		// break;
		// case "failed":
		// stateClass = Omn_lifecycle.Error;
		// break;
		// case "instantiating":
		// stateClass = Omn_lifecycle.NotYetInitialized;
		// break;
		// case "pending_allocation":
		// stateClass = Omn_lifecycle.Pending;
		// break;
		// case "configuring":
		// stateClass = Omn_lifecycle.Preinit;
		// break;
		// case "provisioned":
		// stateClass = Omn_lifecycle.Provisioned;
		// break;
		// case "ready":
		// stateClass = Omn_lifecycle.Ready;
		// break;
		// case "stopping":
		// stateClass = Omn_lifecycle.Stopping;
		// break;
		// case "unallocated":
		// stateClass = Omn_lifecycle.Unallocated;
		// break;
		// }
		if (stateClass != null) {
			topology.addProperty(Omn_lifecycle.hasState, stateClass);
		}

		if (geniSliceInfo.getUuid() != null && geniSliceInfo.getUuid() != "") {
			topology.addLiteral(Omn_domain_pc.hasUUID, geniSliceInfo.getUuid());
		}

		if (geniSliceInfo.getUrn() != null && geniSliceInfo.getUrn() != "") {
			topology.addLiteral(Omn.hasURI, geniSliceInfo.getUrn());
		}
	}

	static void extractMonitoring(Object rspecNodeObject, Resource omnNode) {

		Monitoring monitor = (Monitoring) rspecNodeObject;

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource monitoringResource = omnNode.getModel().createResource(uuid);

		if (monitor.getUri() != null && monitor.getUri() != "") {
			monitoringResource.addProperty(Omn.hasURI, monitor.getUri());
		}

		if (monitor.getType() != null && monitor.getType() != "") {
			Resource typeResource = monitoringResource.getModel()
					.createResource(monitor.getType());
			monitoringResource.addProperty(RDF.type, typeResource);
			monitoringResource.addProperty(RDFS.label,
					AbstractConverter.getName(monitor.getType()));
		}

		omnNode.addProperty(Omn_lifecycle.usesService, monitoringResource);

	}

	static void extractOsco(Object nodeDetailObject, Resource omnNode) {

		info.openmultinet.ontology.translators.geni.jaxb.manifest.Osco osco = (info.openmultinet.ontology.translators.geni.jaxb.manifest.Osco) nodeDetailObject;

		String appPort = osco.getAppPort();
		if (appPort != null && appPort != "") {
			omnNode.addProperty(Osco.APP_PORT, appPort);
		}

		String loggingFile = osco.getLoggingFile();
		if (loggingFile != null && loggingFile != "") {
			omnNode.addProperty(Osco.LOGGING_FILE, loggingFile);
		}

		String loggingLevel = osco.getLoggingLevel();
		if (loggingLevel != null && loggingLevel != "") {
			omnNode.addProperty(Osco.LOGGING_LEVEL, loggingLevel);
		}

		String mgmtIntf = osco.getMgmtIntf();
		if (mgmtIntf != null && mgmtIntf != "") {
			omnNode.addProperty(Osco.MGMT_INTF, mgmtIntf);
		}

		String requires = osco.getRequires();
		if (requires != null && requires != "") {
			Resource requiresResource = omnNode.getModel().createResource(
					requires);
			omnNode.addProperty(Osco.requires, requiresResource);
		}

		String servicePort = osco.getServicePort();
		if (servicePort != null && servicePort != "") {
			omnNode.addProperty(Osco.SERVICE_PORT, servicePort);
		}

		Boolean requireAuth = osco.isRequireAuth();
		if (requireAuth != null) {
			omnNode.addProperty(Osco.REQUIRE_AUTH, String.valueOf(requireAuth));
		}

		Boolean notifyDisabled = osco.isNotifyDisabled();
		if (notifyDisabled != null) {
			omnNode.addProperty(Osco.NOTIFY_DISABLED,
					String.valueOf(notifyDisabled));
		}

		Boolean retargetDisabled = osco.isRetargetDisabled();
		if (retargetDisabled != null) {
			omnNode.addProperty(Osco.RETARGET_DISABLED,
					String.valueOf(retargetDisabled));
		}

		Boolean notifyChanDisabled = osco.isNotifyChanDisabled();
		if (notifyChanDisabled != null) {
			omnNode.addProperty(Osco.NOTIFY_CHAN_DISABLED,
					String.valueOf(notifyChanDisabled));
		}

		Boolean coapDisabled = osco.isCoapDisabled();
		if (coapDisabled != null) {
			omnNode.addProperty(Osco.COAP_DISABLED,
					String.valueOf(coapDisabled));
		}

		Boolean anncAuto = osco.isAnncAuto();
		if (anncAuto != null) {
			omnNode.addProperty(Osco.ANNC_AUTO, String.valueOf(anncAuto));
		}

		Boolean anncDisabled = osco.isAnncDisabled();
		if (anncDisabled != null) {
			omnNode.addProperty(Osco.ANNC_DISABLED,
					String.valueOf(anncDisabled));
		}

	}

	static void extractReservation(Object nodeDetailObject, Resource omnResource) {

		// get value of the element
		Reservation reservation = (Reservation) nodeDetailObject;
		XMLGregorianCalendar expirationTime = reservation.getExpirationTime();
		String stateString = reservation.getReservationState();
		OntClass stateClass = CommonMethods.convertGeniStateToOmn(stateString);

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource reservationResource = omnResource.getModel().createResource(
				uuid);

		if (stateClass != null) {
			reservationResource.addProperty(Omn_lifecycle.hasReservationState,
					stateClass);
		}

		if (expirationTime != null) {
			XSDDateTime expirationTimeXsd = xmlToXsdTime(expirationTime);
			reservationResource.addLiteral(Omn_lifecycle.expirationTime,
					expirationTimeXsd);
		}

		omnResource.addProperty(Omn.hasReservation, reservationResource);
	}

	static void extractGeniSliverInfo(Object nodeDetailObject,
			Resource omnResource) {

		// get value of the element
		GeniSliverInfo geniSliverInfo = (GeniSliverInfo) nodeDetailObject;
		String stateString = geniSliverInfo.getState();
		OntClass stateClass = CommonMethods.convertGeniStateToOmn(stateString);

		if (stateClass != null) {
			omnResource.addProperty(Omn_lifecycle.hasState, stateClass);
		}

		if (geniSliverInfo.getResourceId() != null
				&& geniSliverInfo.getResourceId() != "") {
			omnResource.addLiteral(Omn_lifecycle.resourceId,
					geniSliverInfo.getResourceId());
		}

		if (geniSliverInfo.getStartTime() != null) {
			XSDDateTime startTimeXSDDateTime = xmlToXsdTime(geniSliverInfo
					.getStartTime());
			omnResource.addLiteral(Omn_lifecycle.startTime,
					startTimeXSDDateTime);
		}

		if (geniSliverInfo.getExpirationTime() != null) {

			XSDDateTime expTimeXSDDateTime = xmlToXsdTime(geniSliverInfo
					.getExpirationTime());
			omnResource.addLiteral(Omn_lifecycle.expirationTime,
					expTimeXSDDateTime);
		}

		if (geniSliverInfo.getCreationTime() != null) {
			XSDDateTime creationTimeXSDDateTime = xmlToXsdTime(geniSliverInfo
					.getCreationTime());
			omnResource.addLiteral(Omn_lifecycle.creationTime,
					creationTimeXSDDateTime);
		}

		if (geniSliverInfo.getCreatorUrn() != null
				&& geniSliverInfo.getCreatorUrn() != "") {
			omnResource.addLiteral(Omn_lifecycle.creator,
					geniSliverInfo.getCreatorUrn());
		}
	}

	static void extractPostBootScript(Object nodeDetailObject,
			Resource omnResource) {

		ServicesPostBootScript postBootScript = (ServicesPostBootScript) nodeDetailObject;

		if (postBootScript.getType() != null && postBootScript.getType() != "") {
			omnResource.addProperty(Omn_service.postBootScriptType,
					postBootScript.getType());
		}

		if (postBootScript.getContent() != null
				&& postBootScript.getContent() != "") {
			omnResource.addProperty(Omn_service.postBootScriptText,
					postBootScript.getContent());
		}
	}

	static void extractProxyService(Object serviceObject, Resource omnService) {

		Proxy proxy = (Proxy) serviceObject;

		if (proxy.getProxy() != null && proxy.getProxy() != "") {
			omnService.addProperty(Omn_service.proxyProxy, proxy.getProxy());
		}

		if (proxy.getFor() != null && proxy.getFor() != "") {
			omnService.addProperty(Omn_service.proxyFor, proxy.getFor());
		}
	}

	static void tryExtractEpc(Resource node, Object rspecObject) {

		try {
			Epc epc = (Epc) rspecObject;
			// String resourceUri = node.getURI().toString() + "-details";
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnEpc = node.getModel().createResource(uuid);
			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasEvolvedPacketCore,
					omnEpc);

			omnEpc.addProperty(
					RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.EvolvedPacketCoreDetails);

			String mme = epc.getMmeAddress();
			if (mme != null && mme != "") {
				omnEpc.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.mmeAddress,
						mme);
			}

			// String pdn = epc.getPdnGateway();
			// if (pdn != null && pdn != "") {
			// omnEpc.addProperty(
			// info.openmultinet.ontology.vocabulary.Epc.pdnGateway,
			// pdn);
			// }

			String servingGateway = epc.getServingGateway();
			if (servingGateway != null && servingGateway != "") {
				omnEpc.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.servingGateway,
						servingGateway);
			}

			String vendor = epc.getVendor();
			if (vendor != null && vendor != "") {
				omnEpc.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.vendor,
						vendor);
			}

			List<Object> objects = epc.getApnOrEnodebOrPdnGateway();
			for (Object o : objects) {
				if (o.getClass().equals(ApnContents.class)) {
					ApnContents apnContents = (ApnContents) o;
					extractApn(apnContents, omnEpc);
				} else if (o.getClass().equals(ENodeBContents.class)) {
					ENodeBContents eNodeBContents = (ENodeBContents) o;
					extractENodeB(eNodeBContents, omnEpc);
				} else if (o.getClass().equals(PDNGatewayContents.class)) {
					PDNGatewayContents pgwContents = (PDNGatewayContents) o;
					extractPDNGateway(pgwContents, omnEpc);
				} else if (o.getClass().equals(SubscriberContents.class)) {
					SubscriberContents subscriberContents = (SubscriberContents) o;
					String imsiNumber = subscriberContents.getImsiNumber();
					omnEpc.addProperty(
							info.openmultinet.ontology.vocabulary.Epc.subscriber,
							imsiNumber);
				}
			}
		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void extractPDNGateway(PDNGatewayContents pgwContents,
			Resource omnEpc) {
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource pgwResource = omnEpc.getModel().createResource(uuid);
		pgwResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Epc.PDNGateway);

		String name = pgwContents.getName();
		if (name != null && name != "") {
			pgwResource.addProperty(RDFS.label, name);
		}

		BigInteger rate = pgwContents.getRateUp();
		BigInteger minusOne = BigInteger.valueOf(-1);
		if (rate != null && rate.compareTo(minusOne) != 0) {
			pgwResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Epc.rateCodeUp, rate);
		}
		BigInteger rateDown = pgwContents.getRateDown();
		if (rateDown != null && rateDown.compareTo(minusOne) != 0) {
			pgwResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Epc.rateCodeDown, rateDown);
		}

		BigInteger delay = pgwContents.getDelay();
		if (delay != null && delay.compareTo(minusOne) != 0) {
			pgwResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Epc.delayCode, delay);
		}

		BigInteger loss = pgwContents.getLoss();
		if (loss != null && loss.compareTo(minusOne) != 0) {
			pgwResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Epc.packetlossCode,
					loss);
		}

		omnEpc.addProperty(
				info.openmultinet.ontology.vocabulary.Epc.pdnGateway,
				pgwResource);

	}

	private static void extractENodeB(ENodeBContents eNodeBContents,
			Resource omnEpc) {

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource enodebResource = omnEpc.getModel().createResource(uuid);
		enodebResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Epc.ENodeB);

		String address = eNodeBContents.getAddress();
		if (address != null && address != "") {
			enodebResource.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.eNodeBAddress,
					address);
		}

		String name = eNodeBContents.getName();
		if (name != null && name != "") {
			enodebResource.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.eNodeBName, name);
		}

		omnEpc.addProperty(info.openmultinet.ontology.vocabulary.Epc.hasENodeB,
				enodebResource);

	}

	private static void extractApn(ApnContents apnContents, Resource omnEpc) {

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource apnResource = omnEpc.getModel().createResource(uuid);
		apnResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Epc.AccessPointName);

		String networkId = apnContents.getNetworkId();
		if (networkId != null && networkId != "") {
			apnResource
					.addProperty(
							info.openmultinet.ontology.vocabulary.Epc.networkIdentifier,
							networkId);
		}

		String operatorId = apnContents.getOperatorId();
		if (operatorId != null && operatorId != "") {
			apnResource
					.addProperty(
							info.openmultinet.ontology.vocabulary.Epc.operatorIdentifier,
							operatorId);
		}

		omnEpc.addProperty(
				info.openmultinet.ontology.vocabulary.Epc.hasAccessPointName,
				apnResource);

	}

	public static void tryExtractAccessNetwork(Resource node,
			Object rspecNodeObject) {
		try {
			AccessNetwork accessNetwork = (AccessNetwork) rspecNodeObject;
			// String resourceUri = node.getURI().toString() + "-details";
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnAccessNetwork = node.getModel().createResource(uuid);

			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasAccessNetwork,
					omnAccessNetwork);

			omnAccessNetwork
					.addProperty(
							RDF.type,
							info.openmultinet.ontology.vocabulary.Epc.AccessNetworkDetails);

			String enodeb = accessNetwork.getEnodebId();
			if (enodeb != null && enodeb != "") {
				omnAccessNetwork.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.eNodeBId,
						enodeb);
			}

			String plmnId = accessNetwork.getPlmnId();
			if (plmnId != null && plmnId != "") {
				omnAccessNetwork
						.addProperty(
								info.openmultinet.ontology.vocabulary.Epc.publicLandMobileNetworkId,
								plmnId);
			}

			BigInteger band = accessNetwork.getBand();
			if (band != null) {
				omnAccessNetwork.addLiteral(
						info.openmultinet.ontology.vocabulary.Epc.band, band);
			}

			String vendor = accessNetwork.getVendor();
			if (vendor != null && vendor != "") {
				omnAccessNetwork.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.vendor,
						vendor);
			}

			String baseModel = accessNetwork.getBaseModel();
			if (baseModel != null && baseModel != "") {
				omnAccessNetwork.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.baseModel,
						baseModel);
			}

			String epcAddress = accessNetwork.getEpcAddress();
			if (epcAddress != null && epcAddress != "") {
				omnAccessNetwork
						.addProperty(
								info.openmultinet.ontology.vocabulary.Epc.evolvedPacketCoreAddress,
								epcAddress);
			}

			String mode = accessNetwork.getMode();
			if (mode != null && mode != "") {
				omnAccessNetwork.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.mode, mode);
			}

			// List<ApnContents> objects = accessNetwork.getApn();
			// for (ApnContents o : objects) {
			// extractApn(o, omnAccessNetwork);
			// }

			List<Object> objects = accessNetwork.getApnOrIpAddress();
			for (Object o : objects) {
				if (o.getClass().equals(ApnContents.class)) {
					ApnContents apnContents = (ApnContents) o;
					extractApn(apnContents, omnAccessNetwork);
				} else if (o.getClass().equals(EpcIpContents.class)) {
					EpcIpContents ipContents = (EpcIpContents) o;
					extractIpContents(ipContents, omnAccessNetwork);
				}
			}

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	private static void extractIpContents(EpcIpContents ipContents,
			Resource omnAccessNetwork) {
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource controlResource = omnAccessNetwork.getModel().createResource(
				uuid);
		controlResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Omn_resource.IPAddress);

		String address = ipContents.getAddress();
		if (address != null && address != "") {
			controlResource.addProperty(Omn_resource.address, address);
		}

		String netmask = ipContents.getNetmask();
		if (netmask != null && netmask != "") {
			controlResource.addProperty(Omn_resource.netmask, netmask);
		}

		String type = ipContents.getType();
		if (type != null && type != "") {
			controlResource.addProperty(Omn_resource.type, type);
		}

		omnAccessNetwork
				.addProperty(
						info.openmultinet.ontology.vocabulary.Omn_resource.hasIPAddress,
						controlResource);

	}

	public static void tryExtractUserEquipment(Resource node, Object rspecObject) {
		try {
			Ue ue = (Ue) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			// String resourceUri = node.getURI().toString() + "-details";
			Resource omnUe = node.getModel().createResource(uuid);
			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasUserEquipment,
					omnUe);

			omnUe.addProperty(
					RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.UserEquipmentDetails);

			Boolean lteSupport = ue.isLteSupport();
			if (lteSupport != null) {
				omnUe.addLiteral(
						info.openmultinet.ontology.vocabulary.Epc.lteSupport,
						lteSupport);
			}

			List<Object> objects = ue.getApnOrControlAddressOrUeHardwareType();
			for (Object o : objects) {
				if (o.getClass().equals(ApnContents.class)) {
					ApnContents apnContents = (ApnContents) o;
					extractApn(apnContents, omnUe);
				} else if (o.getClass().equals(ControlAddressContents.class)) {
					ControlAddressContents controlAddressContents = (ControlAddressContents) o;
					extractControlAddress(controlAddressContents, omnUe);
				} else if (o.getClass().equals(UeDiskImageContents.class)) {
					UeDiskImageContents diskImageContents = (UeDiskImageContents) o;
					extractEpcDiskImage(diskImageContents, omnUe);
				} else if (o.getClass().equals(UeHardwareTypeContents.class)) {
					UeHardwareTypeContents hardwareTypeContents = (UeHardwareTypeContents) o;
					extractEpcHardwareType(hardwareTypeContents, omnUe);
				}

			}

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	private static void extractEpcHardwareType(
			UeHardwareTypeContents hardwareTypeContents, Resource omnUe) {
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource hardwareTypeResource = omnUe.getModel().createResource(uuid);
		hardwareTypeResource
				.addProperty(
						RDF.type,
						info.openmultinet.ontology.vocabulary.Omn_resource.HardwareType);

		String name = hardwareTypeContents.getName();
		if (name != null && name != "") {
			hardwareTypeResource.addProperty(RDFS.label, name);
		}

		omnUe.addProperty(
				info.openmultinet.ontology.vocabulary.Omn_resource.hasHardwareType,
				hardwareTypeResource);

	}

	private static void extractEpcDiskImage(
			UeDiskImageContents diskImageContents, Resource omnUe) {
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource diskImageResource = omnUe.getModel().createResource(uuid);
		diskImageResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Omn_domain_pc.DiskImage);

		String name = diskImageContents.getName();
		if (name != null && name != "") {
			diskImageResource
					.addProperty(Omn_domain_pc.hasDiskimageLabel, name);
		}

		String description = diskImageContents.getDescription();
		if (description != null && description != "") {
			diskImageResource.addProperty(
					Omn_domain_pc.hasDiskimageDescription, description);
		}

		omnUe.addProperty(
				info.openmultinet.ontology.vocabulary.Omn_domain_pc.hasDiskImage,
				diskImageResource);

	}

	private static void extractControlAddress(
			ControlAddressContents controlAddressContents, Resource omnUe) {
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource controlResource = omnUe.getModel().createResource(uuid);
		controlResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Epc.ControlAddress);

		String address = controlAddressContents.getAddress();
		if (address != null && address != "") {
			controlResource.addProperty(Omn_resource.address, address);
		}

		String netmask = controlAddressContents.getNetmask();
		if (netmask != null && netmask != "") {
			controlResource.addProperty(Omn_resource.netmask, netmask);
		}

		String type = controlAddressContents.getType();
		if (type != null && type != "") {
			controlResource.addProperty(Omn_resource.type, type);
		}

		omnUe.addProperty(
				info.openmultinet.ontology.vocabulary.Epc.hasControlAddress,
				controlResource);

	}

	public static void tryExtractAcs(Resource node, Object rspecObject) {
		try {
			Device acsDevice = (Device) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnAcs = node.getModel().createResource(uuid);
			node.addProperty(
					info.openmultinet.ontology.vocabulary.Acs.hasDevice, omnAcs);

			omnAcs.addProperty(RDF.type,
					info.openmultinet.ontology.vocabulary.Acs.AcsDevice);

			String acsId = acsDevice.getId();
			if (acsId != null && acsId != "") {
				omnAcs.addProperty(
						info.openmultinet.ontology.vocabulary.Acs.hasAcsId,
						acsId);
			}

			List<ParameterContents> objects = acsDevice.getParam();
			for (ParameterContents o : objects) {

				String uuid1 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource param = node.getModel().createResource(uuid1);
				omnAcs.addProperty(
						info.openmultinet.ontology.vocabulary.Acs.hasParameter,
						param);
				param.addProperty(RDF.type,
						info.openmultinet.ontology.vocabulary.Acs.AcsParameter);

				String name = o.getName();
				param.addProperty(
						info.openmultinet.ontology.vocabulary.Acs.hasParamName,
						name);

				String value = o.getValue();
				param.addProperty(
						info.openmultinet.ontology.vocabulary.Acs.hasParamValue,
						value);
			}
		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractEmulabNodeType(Object hwObject, Resource omnHw) {

		try {
			NodeType nodeType = (NodeType) hwObject;

			String nodeTypeSlots = nodeType.getTypeSlots();
			omnHw.addProperty(Omn_domain_pc.hasEmulabNodeTypeSlots,
					nodeTypeSlots);

			String staticSlot = nodeType.getStatic();
			if (staticSlot != null) {
				omnHw.addProperty(Omn_domain_pc.emulabNodeTypeStatic,
						staticSlot);
			}

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractEmulabFd(Resource omnNode,
			Object rspecNodeObject) {
		try {
			Fd featureDescription = (Fd) rspecNodeObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource fdResource = omnNode.getModel().createResource(uuid);
			fdResource.addProperty(RDF.type, Omn_domain_pc.FeatureDescription);

			// name is required
			String name = featureDescription.getName();
			fdResource.addProperty(Omn_domain_pc.hasEmulabFdName, name);

			// weight is required
			String weight = featureDescription.getWeight();
			fdResource.addProperty(Omn_domain_pc.hasEmulabFdWeight, weight);

			// violatable is not required
			String violatable = featureDescription.getViolatable();
			if (violatable != null) {
				fdResource.addProperty(Omn_domain_pc.emulabFdViolatable,
						violatable);
			}

			// local operator is not required
			String localOperator = featureDescription.getLocalOperator();
			if (localOperator != null) {
				fdResource.addProperty(Omn_domain_pc.hasEmulabFdLocalOperator,
						localOperator);
			}

			// global operator is not required
			String globalOperator = featureDescription.getGlobalOperator();
			if (globalOperator != null) {
				fdResource.addProperty(Omn_domain_pc.hasEmulabFdGlobalOperator,
						globalOperator);
			}

			omnNode.addProperty(Omn.hasAttribute, fdResource);

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractGateway(Resource node, Object rspecObject) {
		try {
			Gateway gateway = (Gateway) rspecObject;

			node.addProperty(RDF.type, Fiveg.Gateway);

			String version = gateway.getVersion();
			if (version != null && version != "") {
				node.addProperty(Fiveg.version, version);
			}

			Boolean upstartOn = gateway.isUpstartOn();
			if (upstartOn != null) {
				node.addLiteral(Fiveg.upstartOn, upstartOn);
			}

			BigInteger mgmtIntf = gateway.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger minNumIntf = gateway.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger netAIntf = gateway.getNetAIntf();
			if (netAIntf != null) {
				node.addLiteral(Fiveg.ipServicesNetwork, netAIntf);
			}

			FiveGIpContents cloudIpAddress = gateway.getCloudMgmtGwIp();
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource ipResource = node.getModel().createResource(uuid);
			ipResource.addProperty(RDF.type, Omn_resource.IPAddress);

			String address = cloudIpAddress.getAddress();
			if (address != null && address != "") {
				ipResource.addProperty(Omn_resource.address, address);
			}

			String netmask = cloudIpAddress.getNetmask();
			if (netmask != null && netmask != "") {
				ipResource.addProperty(Omn_resource.netmask, netmask);
			}

			String type = cloudIpAddress.getType();
			if (type != null && type != "") {
				ipResource.addProperty(Omn_resource.type, type);
			}

			node.addProperty(Fiveg.cloudManagementIP, ipResource);

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractOlChannel(Resource topology, Object o)
			throws InvalidRspecValueException {
		try {
			Channel channel = (Channel) o;

			String uuid;
			String componentId = channel.getComponentId();
			if (componentId != null
					&& (AbstractConverter.isUrl(componentId) || AbstractConverter
							.isUrn(componentId))) {
				uuid = componentId;
			} else {
				uuid = "urn:uuid:" + UUID.randomUUID().toString();
			}

			Resource omnChannel = topology.getModel().createResource(uuid);
			omnChannel.addProperty(RDF.type, Omn_domain_wireless.Channel);
			topology.addProperty(Omn.hasComponent, omnChannel);

			if (channel.getFrequency() != null) {
				String frequency = channel.getFrequency();
				Resource frequencyIndividual = CommonMethods.getFrequency(
						frequency, topology.getModel());

				omnChannel.addProperty(Omn_domain_wireless.usesFrequency,
						frequencyIndividual);
			}

			// check that componentId was uri
			if (componentId != null && uuid.equals(componentId)) {
				omnChannel.addLiteral(Omn_lifecycle.hasComponentID,
						URI.create(componentId));
			}

			if (channel.getComponentManagerId() != null) {

				String managerId = channel.getComponentManagerId();
				Resource managedByResource = null;
				if (AbstractConverter.isUrl(managerId)
						|| AbstractConverter.isUrn(managerId)) {
					managedByResource = omnChannel.getModel().createResource(
							managerId);
				} else {
					String uuid1 = "urn:uuid:" + UUID.randomUUID().toString();
					managedByResource = omnChannel.getModel().createResource(
							uuid1);
				}
				omnChannel.addProperty(Omn_lifecycle.managedBy,
						managedByResource);
			}

			if (channel.getComponentName() != null) {
				String componentName = channel.getComponentName();

				try {
					int channelNum = Integer.parseInt(componentName);
					BigInteger channelNumBig = BigInteger.valueOf(channelNum);
					omnChannel.addLiteral(Omn_domain_wireless.channelNum,
							channelNumBig);
				} catch (NumberFormatException e) {
					omnChannel.addProperty(Omn_lifecycle.hasComponentName,
							componentName);
				}
			}

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractOlLease(Resource omnNode, Object o) {
		try {
			Lease lease = (Lease) o;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnLease = omnNode.getModel().createResource(uuid);
			omnLease.addProperty(RDF.type, Omn_lifecycle.Lease);
			omnNode.addProperty(Omn_lifecycle.hasLease, omnLease);
			
			if (lease.getLeaseID() != null) {
				String leaseId = lease.getLeaseID();
				omnLease.addLiteral(Omn_lifecycle.hasID, leaseId);
			}

			if (lease.getUuid() != null) {
				String uuidString = lease.getUuid();
				omnLease.addLiteral(Omn_domain_pc.hasUUID, uuidString);
			}

			if (lease.getSliceId() != null) {
				String sliceId = lease.getSliceId();
				omnLease.addLiteral(Omn_lifecycle.hasSliceID, sliceId);
			}

			// TODO:
			// <xs:attribute name="leaseREF" type="xs:IDREF" />
			if (lease.getLeaseREF() != null) {
				Object leaseIdRef = lease.getLeaseREF();
				if (leaseIdRef.equals(lease)) {
					omnLease.addLiteral(Omn_lifecycle.hasIdRef,
							omnLease.getURI());
				}
			}

			if (lease.getValidFrom() != null) {
				XMLGregorianCalendar from = lease.getValidFrom();
				Calendar dateTime = from.toGregorianCalendar();
				omnLease.addLiteral(Omn_lifecycle.startTime, dateTime);
			}

			if (lease.getValidUntil() != null) {
				XMLGregorianCalendar until = lease.getValidUntil();
				Calendar dateTime = until.toGregorianCalendar();
				omnLease.addLiteral(Omn_lifecycle.expirationTime, dateTime);
			}

		} catch (final ClassCastException e) {
			ManifestExtractExt.LOG.finer(e.getMessage());
		}

	}
}