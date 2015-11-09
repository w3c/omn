package info.openmultinet.ontology.translators.geni.manifest;

import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
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
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ParameterContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Proxy;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Reservation;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServicesPostBootScript;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.UeHardwareTypeContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;
import info.openmultinet.ontology.vocabulary.Osco;

import java.math.BigInteger;
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

	private static final String HOST = "http://open-multinet.info/example#";
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
			@SuppressWarnings("unchecked")
			Epc epc = (Epc) rspecObject;
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

			String pdn = epc.getPdnGateway();
			if (pdn != null && pdn != "") {
				omnEpc.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.pdnGateway,
						pdn);
			}

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

			List<Object> objects = epc.getApnOrEnodebOrSubscriber();
			for (Object o : objects) {
				if (o.getClass().equals(ApnContents.class)) {
					ApnContents apnContents = (ApnContents) o;
					extractApn(apnContents, omnEpc);
				} else if (o.getClass().equals(ENodeBContents.class)) {
					ENodeBContents eNodeBContents = (ENodeBContents) o;
					extractENodeB(eNodeBContents, omnEpc);
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
			@SuppressWarnings("unchecked")
			AccessNetwork accessNetwork = (AccessNetwork) rspecNodeObject;
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
			@SuppressWarnings("unchecked")
			Ue ue = (Ue) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnUe = node.getModel().createResource(uuid);
			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasUserEquipment,
					omnUe);

			omnUe.addProperty(
					RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.UserEquipmentDetails);

			Boolean lteSupport = ue.isLteSupport();
			if (lteSupport != null) {
				omnUe.addProperty(
						info.openmultinet.ontology.vocabulary.Epc.lteSupport,
						String.valueOf(lteSupport));
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
}