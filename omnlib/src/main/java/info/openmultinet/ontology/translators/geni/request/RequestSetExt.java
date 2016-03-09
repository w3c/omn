package info.openmultinet.ontology.translators.geni.request;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.request.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.request.Bt;
import info.openmultinet.ontology.translators.geni.jaxb.request.Control;
import info.openmultinet.ontology.translators.geni.jaxb.request.Enodeb;
import info.openmultinet.ontology.translators.geni.jaxb.request.Hss;
import info.openmultinet.ontology.translators.geni.jaxb.request.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.request.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.request.ApnContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ControlAddressContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Controller;
import info.openmultinet.ontology.translators.geni.jaxb.request.ControllerRole;
import info.openmultinet.ontology.translators.geni.jaxb.request.Datapath;
import info.openmultinet.ontology.translators.geni.jaxb.request.Device;
import info.openmultinet.ontology.translators.geni.jaxb.request.DlType;
import info.openmultinet.ontology.translators.geni.jaxb.request.DlVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.Dns;
import info.openmultinet.ontology.translators.geni.jaxb.request.ENodeBContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.request.EpcIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.request.FiveGIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Gateway;
import info.openmultinet.ontology.translators.geni.jaxb.request.GroupContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.HopContent;
import info.openmultinet.ontology.translators.geni.jaxb.request.ImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.MatchContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.request.NextHopContent;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.NwDst;
import info.openmultinet.ontology.translators.geni.jaxb.request.NwSrc;
import info.openmultinet.ontology.translators.geni.jaxb.request.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.request.Osco;
import info.openmultinet.ontology.translators.geni.jaxb.request.OscoLocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.PDNGatewayContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.PacketContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ParameterContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.PathContent;
import info.openmultinet.ontology.translators.geni.jaxb.request.RoutableControlIp;
import info.openmultinet.ontology.translators.geni.jaxb.request.Sliver;
import info.openmultinet.ontology.translators.geni.jaxb.request.StitchContent;
import info.openmultinet.ontology.translators.geni.jaxb.request.SubnetContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Switch;
import info.openmultinet.ontology.translators.geni.jaxb.request.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.request.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.UeHardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.UseGroup;
import info.openmultinet.ontology.vocabulary.Fiveg;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.math.BigInteger;
import java.util.List;

import javax.xml.datatype.XMLGregorianCalendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods for converting from OMN model to request RSpec. These methods
 * are for non-native RSpec extensions.
 * 
 * @author robynloughnane
 *
 */
public class RequestSetExt extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";

	private static void setUeDiskImage(Resource diskImageResource, Ue ue) {

		ObjectFactory objectFactory = new ObjectFactory();
		UeDiskImageContents diskImageContents = objectFactory
				.createUeDiskImageContents();

		if (diskImageResource.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
			String name = diskImageResource
					.getProperty(Omn_domain_pc.hasDiskimageLabel).getObject()
					.asLiteral().getString();
			diskImageContents.setName(name);
		}

		if (diskImageResource
				.hasProperty(Omn_domain_pc.hasDiskimageDescription)) {
			String description = diskImageResource
					.getProperty(Omn_domain_pc.hasDiskimageDescription)
					.getObject().asLiteral().getString();
			diskImageContents.setDescription(description);
		}

		ue.getApnOrControlAddressOrUeHardwareType().add(diskImageContents);

	}

	private static void setHardwareType(Resource hardwareTypeResource, Ue ue) {

		ObjectFactory objectFactory = new ObjectFactory();
		UeHardwareTypeContents hardwareTypeContents = objectFactory
				.createUeHardwareTypeContents();

		if (hardwareTypeResource.hasProperty(RDFS.label)) {
			String name = hardwareTypeResource.getProperty(RDFS.label)
					.getObject().asLiteral().getString();
			hardwareTypeContents.setName(name);
		}

		ue.getApnOrControlAddressOrUeHardwareType().add(hardwareTypeContents);

	}

	private static void setControlAddress(Resource controlAddressResource, Ue ue) {
		ObjectFactory objectFactory = new ObjectFactory();
		ControlAddressContents controlAddressContents = objectFactory
				.createControlAddressContents();

		if (controlAddressResource
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.address)) {
			String address = controlAddressResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.address)
					.getObject().asLiteral().getString();
			controlAddressContents.setAddress(address);
		}

		if (controlAddressResource
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.netmask)) {
			String netmask = controlAddressResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.netmask)
					.getObject().asLiteral().getString();
			controlAddressContents.setNetmask(netmask);
		}

		if (controlAddressResource
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.type)) {
			String type = controlAddressResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.type)
					.getObject().asLiteral().getString();
			controlAddressContents.setType(type);
		}

		ue.getApnOrControlAddressOrUeHardwareType().add(controlAddressContents);

	}

	public static void setUE(Statement omnResource, NodeContents geniNode) {
		if (omnResource.getResource().hasProperty(
				info.openmultinet.ontology.vocabulary.Epc.hasUserEquipment)) {

			Resource resourceResource = omnResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.hasUserEquipment)
					.getObject().asResource();

			Ue ue = new ObjectFactory().createUe();

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.lteSupport)) {
				String lteSuport = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.lteSupport)
						.getObject().asLiteral().getString();
				boolean lteSuportBool = Boolean.parseBoolean(lteSuport);
				ue.setLteSupport(lteSuportBool);
			}

			StmtIterator apns = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.hasAccessPointName);
			while (apns.hasNext()) {
				Statement apnStatement = apns.next();
				Resource apn = apnStatement.getObject().asResource();
				ue.getApnOrControlAddressOrUeHardwareType().add(setEpcApn(apn));
			}

			StmtIterator controlAddresses = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.hasControlAddress);
			while (controlAddresses.hasNext()) {
				Statement controlAddressStatement = controlAddresses.next();
				Resource controlAddressResource = controlAddressStatement
						.getObject().asResource();
				setControlAddress(controlAddressResource, ue);
			}

			StmtIterator hardwareTypes = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Omn_resource.hasHardwareType);
			while (hardwareTypes.hasNext()) {
				Statement hardwareTypeStatement = hardwareTypes.next();
				Resource hardwareTypeResource = hardwareTypeStatement
						.getObject().asResource();
				setHardwareType(hardwareTypeResource, ue);
			}

			StmtIterator diskImages = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Omn_domain_pc.hasDiskImage);
			while (diskImages.hasNext()) {
				Statement diskImageStatement = diskImages.next();
				Resource diskImageResource = diskImageStatement.getObject()
						.asResource();
				setUeDiskImage(diskImageResource, ue);
			}

			geniNode.getAnyOrRelationOrLocation().add(ue);
		}

	}

	public static void setEPC(Statement omnResource, NodeContents geniNode) {
		if (omnResource.getResource().hasProperty(
				info.openmultinet.ontology.vocabulary.Epc.hasEvolvedPacketCore)) {

			Resource resourceResource = omnResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.hasEvolvedPacketCore)
					.getObject().asResource();

			Epc epc = new ObjectFactory().createEpc();

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.mmeAddress)) {
				String mmeAddress = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.mmeAddress)
						.getObject().asLiteral().getString();
				epc.setMmeAddress(mmeAddress);
			}

			// if (resourceResource
			// .hasProperty(info.openmultinet.ontology.vocabulary.Epc.pdnGateway))
			// {
			// String pdnGateway = resourceResource
			// .getProperty(
			// info.openmultinet.ontology.vocabulary.Epc.pdnGateway)
			// .getObject().asLiteral().getString();
			// epc.setPdnGateway(pdnGateway);
			// }

			StmtIterator pgws = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.pdnGateway);
			while (pgws.hasNext()) {
				Statement pgwStatement = pgws.next();
				Resource pgw = pgwStatement.getObject().asResource();
				setPGW(epc, pgw);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.servingGateway)) {
				String servingGateway = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.servingGateway)
						.getObject().asLiteral().getString();
				epc.setServingGateway(servingGateway);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.vendor)) {
				String vendor = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.vendor)
						.getObject().asLiteral().getString();
				epc.setVendor(vendor);
			}

			StmtIterator apns = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.hasAccessPointName);
			while (apns.hasNext()) {
				Statement apnStatement = apns.next();
				Resource apn = apnStatement.getObject().asResource();
				epc.getApnOrEnodebOrPdnGateway().add(setEpcApn(apn));
			}

			StmtIterator eNodeBs = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.hasENodeB);
			while (eNodeBs.hasNext()) {
				Statement eNodeBStatement = eNodeBs.next();
				Resource eNodeB = eNodeBStatement.getObject().asResource();
				setENodeB(epc, eNodeB);
			}

			StmtIterator subscribers = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.subscriber);
			while (subscribers.hasNext()) {
				Statement subscriberStatement = subscribers.next();
				String subscriber = subscriberStatement.getObject().asLiteral()
						.getString();
				SubscriberContents subscriberContents = new ObjectFactory()
						.createSubscriberContents();
				subscriberContents.setImsiNumber(subscriber);
				epc.getApnOrEnodebOrPdnGateway().add(subscriberContents);
			}

			geniNode.getAnyOrRelationOrLocation().add(epc);
		}

	}

	private static void setPGW(Epc epc, Resource pgw) {
		PDNGatewayContents pgwContents = new ObjectFactory()
				.createPDNGatewayContents();

		if (pgw.hasProperty(info.openmultinet.ontology.vocabulary.Epc.rateCodeUp)) {
			int rate = pgw
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.rateCodeUp)
					.getObject().asLiteral().getInt();
			BigInteger bigRate = BigInteger.valueOf(rate);
			pgwContents.setRateUp(bigRate);
		}
		if (pgw.hasProperty(info.openmultinet.ontology.vocabulary.Epc.rateCodeDown)) {
			int rate = pgw
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.rateCodeDown)
					.getObject().asLiteral().getInt();
			BigInteger bigRate = BigInteger.valueOf(rate);
			pgwContents.setRateDown(bigRate);
		}

		if (pgw.hasProperty(info.openmultinet.ontology.vocabulary.Epc.delayCode)) {
			int delay = pgw
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.delayCode)
					.getObject().asLiteral().getInt();
			BigInteger bigDelay = BigInteger.valueOf(delay);
			pgwContents.setDelay(bigDelay);

		}

		if (pgw.hasProperty(info.openmultinet.ontology.vocabulary.Epc.packetlossCode)) {
			int loss = pgw
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.packetlossCode)
					.getObject().asLiteral().getInt();
			BigInteger bigLoss = BigInteger.valueOf(loss);
			pgwContents.setLoss(bigLoss);

		}

		if (pgw.hasProperty(RDFS.label)) {
			String name = pgw.getProperty(RDFS.label).getObject().asLiteral()
					.getString();
			pgwContents.setName(name);
		}

		epc.getApnOrEnodebOrPdnGateway().add(pgwContents);

	}

	private static void setENodeB(Epc epc, Resource eNodeB) {
		ENodeBContents eNodeBContents = new ObjectFactory()
				.createENodeBContents();

		if (eNodeB
				.hasProperty(info.openmultinet.ontology.vocabulary.Epc.eNodeBAddress)) {
			String address = eNodeB
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.eNodeBAddress)
					.getObject().asLiteral().getString();
			eNodeBContents.setAddress(address);
		}

		if (eNodeB
				.hasProperty(info.openmultinet.ontology.vocabulary.Epc.eNodeBName)) {
			String name = eNodeB
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.eNodeBName)
					.getObject().asLiteral().getString();
			eNodeBContents.setName(name);
		}

		epc.getApnOrEnodebOrPdnGateway().add(eNodeBContents);

	}

	public static void setAccessNetwork(Statement omnResource,
			NodeContents geniNode) {
		// Resource resourceResource = omnResource.getResource();
		// if (resourceResource
		// .hasProperty(RDF.type,
		// info.openmultinet.ontology.vocabulary.Epc.AccessNetwork)) {
		if (omnResource.getResource().hasProperty(
				info.openmultinet.ontology.vocabulary.Epc.hasAccessNetwork)) {

			Resource resourceResource = omnResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.hasAccessNetwork)
					.getObject().asResource();

			AccessNetwork accessNetwork = new ObjectFactory()
					.createAccessNetwork();

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.eNodeBId)) {
				String eNodeBId = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.eNodeBId)
						.getObject().asLiteral().getString();
				accessNetwork.setEnodebId(eNodeBId);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.publicLandMobileNetworkId)) {
				String publicLandMobileNetworkId = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.publicLandMobileNetworkId)
						.getObject().asLiteral().getString();
				accessNetwork.setPlmnId(publicLandMobileNetworkId);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.band)) {
				String band = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.band)
						.getObject().asLiteral().getString();
				int bandInt = Integer.parseInt(band);
				BigInteger bandBigInt = BigInteger.valueOf(bandInt);
				accessNetwork.setBand(bandBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.vendor)) {
				String vendor = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.vendor)
						.getObject().asLiteral().getString();
				accessNetwork.setVendor(vendor);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.baseModel)) {
				String baseModel = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.baseModel)
						.getObject().asLiteral().getString();
				accessNetwork.setBaseModel(baseModel);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.evolvedPacketCoreAddress)) {
				String evolvedPacketCoreAddress = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.evolvedPacketCoreAddress)
						.getObject().asLiteral().getString();
				accessNetwork.setEpcAddress(evolvedPacketCoreAddress);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.mode)) {
				String mode = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.mode)
						.getObject().asLiteral().getString();
				accessNetwork.setMode(mode);
			}

			StmtIterator apns = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Epc.hasAccessPointName);
			while (apns.hasNext()) {
				Statement apnStatement = apns.next();
				Resource apn = apnStatement.getObject().asResource();
				// accessNetwork.getApn().add(setEpcApn(apn));
				accessNetwork.getApnOrIpAddress().add(setEpcApn(apn));
			}

			StmtIterator ipAddresses = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Omn_resource.hasIPAddress);
			while (ipAddresses.hasNext()) {
				Statement ipAddressStatement = ipAddresses.next();
				Resource ipAddress = ipAddressStatement.getObject()
						.asResource();
				setIpAddress(accessNetwork, ipAddress);
			}

			geniNode.getAnyOrRelationOrLocation().add(accessNetwork);
		}

	}

	private static Object setEpcApn(Resource apn) {
		ApnContents apnContents = new ObjectFactory().createApnContents();

		if (apn.hasProperty(info.openmultinet.ontology.vocabulary.Epc.networkIdentifier)) {
			String networkId = apn
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.networkIdentifier)
					.getObject().asLiteral().getString();
			apnContents.setNetworkId(networkId);
		}

		if (apn.hasProperty(info.openmultinet.ontology.vocabulary.Epc.operatorIdentifier)) {
			String operatorId = apn
					.getProperty(
							info.openmultinet.ontology.vocabulary.Epc.operatorIdentifier)
					.getObject().asLiteral().getString();
			apnContents.setOperatorId(operatorId);
		}

		return apnContents;
	}

	private static void setIpAddress(AccessNetwork accessNetwork,
			Resource ipAddress) {
		ObjectFactory objectFactory = new ObjectFactory();
		EpcIpContents controlAddressContents = objectFactory
				.createEpcIpContents();

		if (ipAddress
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.address)) {
			String address = ipAddress
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.address)
					.getObject().asLiteral().getString();
			controlAddressContents.setAddress(address);
		}

		if (ipAddress
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.netmask)) {
			String netmask = ipAddress
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.netmask)
					.getObject().asLiteral().getString();
			controlAddressContents.setNetmask(netmask);
		}

		if (ipAddress
				.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.type)) {
			String type = ipAddress
					.getProperty(
							info.openmultinet.ontology.vocabulary.Omn_resource.type)
					.getObject().asLiteral().getString();
			controlAddressContents.setType(type);
		}

		accessNetwork.getApnOrIpAddress().add(controlAddressContents);

	}

	// private static void setOsco(Statement resource, RSpecContents request) {
	public static void setOsco(Statement resource, NodeContents node) {
		Resource resourceResource = resource.getResource();
		// check whether file has any osco properties
		if (CommonMethods.hasOscoProperty(resourceResource)) {

			Osco osco = new ObjectFactory().createOsco();

			String aboutUri = resourceResource.getURI();
			if (aboutUri != null && aboutUri != "") {
				osco.setAbout(aboutUri);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.id)) {
				String id = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.id)
						.getObject().asLiteral().getString();
				if (id != null && id != "") {
					osco.setId(id);
				}
			}

			if (resourceResource.hasProperty(Omn_lifecycle.implementedBy)) {
				String implementedBy = resourceResource
						.getProperty(Omn_lifecycle.implementedBy).getObject()
						.asResource().getURI();
				if (implementedBy != null && implementedBy != "") {
					osco.setImplementedBy(implementedBy);
				}
			}

			if (resourceResource.hasProperty(RDF.type)) {
				final List<Statement> hasTypes = resourceResource
						.listProperties(RDF.type).toList();
				for (final Statement hasType : hasTypes) {
					String type = hasType.getResource().getURI();
					if (AbstractConverter.nonGeneric(type)) {
						osco.setType(type);
					}
				}
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.ANNC_AUTO)) {
				String anncAutoString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.ANNC_AUTO)
						.getObject().asLiteral().getString();
				boolean anncAuto = Boolean.parseBoolean(anncAutoString);
				osco.setAnncAuto(anncAuto);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.ANNC_DISABLED)) {
				String anncDisabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.ANNC_DISABLED)
						.getObject().asLiteral().getString();
				boolean anncDisabled = Boolean.parseBoolean(anncDisabledString);
				osco.setAnncDisabled(anncDisabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.APP_ID)) {
				String appId = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.APP_ID)
						.getObject().asLiteral().getString();
				osco.setAppId(appId);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.APP_PORT)) {
				String appPort = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.APP_PORT)
						.getObject().asLiteral().getString();
				int appPortInt = Integer.parseInt(appPort);
				BigInteger appPortBigInt = BigInteger.valueOf(appPortInt);
				osco.setAppPort(appPortBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.Bit_Bucket_DB_IP)) {
				String bitBucketDbIp = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.Bit_Bucket_DB_IP)
						.getObject().asLiteral().getString();
				osco.setBitBucketDbIp(bitBucketDbIp);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.COAP_DISABLED)) {
				String coapDisabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.COAP_DISABLED)
						.getObject().asLiteral().getString();
				boolean coapDisabled = Boolean.parseBoolean(coapDisabledString);
				osco.setCoapDisabled(coapDisabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.deployedOn)) {
				String deployedOn = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.deployedOn)
						.getObject().asResource().getURI();
				if (deployedOn != null && deployedOn != "") {
					osco.setDeployedOn(deployedOn);
				}
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.EXT_IP)) {
				String extIp = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.EXT_IP)
						.getObject().asLiteral().getString();
				osco.setExtIp(extIp);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.FILE_SERVER)) {
				String fileServer = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.FILE_SERVER)
						.getObject().asLiteral().getString();
				osco.setFileServer(fileServer);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.flavour)) {
				String flavour = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.flavour)
						.getObject().asLiteral().getString();
				osco.setFlavour(flavour);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.image)) {
				Resource image = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.image)
						.getObject().asResource();
				setOscoImage(osco, image);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.local_port)) {
				String localPort = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.local_port)
						.getObject().asLiteral().getString();
				osco.setLocalPort(localPort);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.location)) {
				Resource location = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.location)
						.getObject().asResource();
				setOscoLocation(osco, location);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.LOGGING_FILE)) {
				String loggingFile = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.LOGGING_FILE)
						.getObject().asLiteral().getString();
				osco.setLoggingFile(loggingFile);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.LOGGING_LEVEL)) {
				String loggingLevel = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.LOGGING_LEVEL)
						.getObject().asLiteral().getString();
				osco.setLoggingLevel(loggingLevel);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_ip)) {
				String m2mConnAppIp = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_ip)
						.getObject().asLiteral().getString();
				osco.setM2MConnAppIp(m2mConnAppIp);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_port)) {
				String m2mConnAppPort = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_port)
						.getObject().asLiteral().getString();
				int m2mConnAppPortInt = Integer.parseInt(m2mConnAppPort);
				BigInteger m2mConnAppPortBigInt = BigInteger
						.valueOf(m2mConnAppPortInt);
				osco.setM2MConnAppPort(m2mConnAppPortBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.maxNumInst)) {
				String maxNumInst = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.maxNumInst)
						.getObject().asLiteral().getString();
				int maxNumInstInt = Integer.parseInt(maxNumInst);
				BigInteger maxNumInstBigInt = BigInteger.valueOf(maxNumInstInt);
				osco.setMaxNumInst(maxNumInstBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.MGMT_INTF)) {
				String mgmtIntf = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.MGMT_INTF)
						.getObject().asLiteral().getString();
				osco.setMgmtIntf(mgmtIntf);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.minNumInst)) {
				String minNumInst = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.minNumInst)
						.getObject().asLiteral().getString();
				int minNumInstInt = Integer.parseInt(minNumInst);
				BigInteger minNumInstBigInt = BigInteger.valueOf(minNumInstInt);
				osco.setMinNumInst(minNumInstBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.NOTIFY_CHAN_DISABLED)) {
				String notifyChanDisabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.NOTIFY_CHAN_DISABLED)
						.getObject().asLiteral().getString();
				boolean notifyChanDisabled = Boolean
						.parseBoolean(notifyChanDisabledString);
				osco.setNotifyChanDisabled(notifyChanDisabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.NOTIFY_DISABLED)) {
				String notifyDisabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.NOTIFY_DISABLED)
						.getObject().asLiteral().getString();
				boolean notifyDisabled = Boolean
						.parseBoolean(notifyDisabledString);
				osco.setNotifyDisabled(notifyDisabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.OMTC_URL)) {
				String omtcUrlString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.OMTC_URL)
						.getObject().asLiteral().getString();
				osco.setOmtcUrl(omtcUrlString);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.REQUIRE_AUTH)) {
				String requireAuthString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.REQUIRE_AUTH)
						.getObject().asLiteral().getString();
				boolean requireAuth = Boolean.parseBoolean(requireAuthString);
				osco.setRequireAuth(requireAuth);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.requires)) {
				String requires = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.requires)
						.getObject().asResource().getURI();
				osco.setRequires(requires);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.RETARGET_DISABLED)) {
				String retargetDisabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.RETARGET_DISABLED)
						.getObject().asLiteral().getString();
				boolean retargetDisabled = Boolean
						.parseBoolean(retargetDisabledString);
				osco.setRetargetDisabled(retargetDisabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.SERVICE_PORT)) {
				String servicePort = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.SERVICE_PORT)
						.getObject().asLiteral().getString();
				int servicePortInt = Integer.parseInt(servicePort);
				BigInteger servicePortBigInt = BigInteger
						.valueOf(servicePortInt);
				osco.setServicePort(servicePortBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.SSL_ENABLED)) {
				String sslEnabledString = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.SSL_ENABLED)
						.getObject().asLiteral().getString();
				boolean sslEnabled = Boolean.parseBoolean(sslEnabledString);
				osco.setSslEnabled(sslEnabled);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.SSL_PORT)) {
				String sslPort = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.SSL_PORT)
						.getObject().asLiteral().getString();
				int sslPortInt = Integer.parseInt(sslPort);
				BigInteger sslPortBigInt = BigInteger.valueOf(sslPortInt);
				osco.setSslPort(sslPortBigInt);
			}

			StmtIterator resources = resource.getResource().listProperties(
					info.openmultinet.ontology.vocabulary.Osco.subnet);

			while (resources.hasNext()) {
				Resource subnet = resources.next().getObject().asResource();
				// if (resourceResource
				// .hasProperty(info.openmultinet.ontology.vocabulary.Osco.subnet))
				// {
				// Resource subnet = resourceResource
				// .getProperty(
				// info.openmultinet.ontology.vocabulary.Osco.subnet)
				// .getObject().asResource();
				setOscoSubnet(osco, subnet);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.TEST_PARAM)) {
				String testParam = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.TEST_PARAM)
						.getObject().asLiteral().getString();
				osco.setTestParam(testParam);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.requirement)) {
				String requirement = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.requirement)
						.getObject().asLiteral().getString();
				osco.setRequirement(requirement);
			}

			// request.getAnyOrNodeOrLink().add(osco);
			node.getAnyOrRelationOrLocation().add(osco);
		}

	}

	private static void setOscoSubnet(Osco osco, Resource subnet) {
		SubnetContents subnetContents = new ObjectFactory()
				.createSubnetContents();

		if (subnet
				.hasProperty(info.openmultinet.ontology.vocabulary.Osco.datacenter)) {
			String datacenter = subnet
					.getProperty(
							info.openmultinet.ontology.vocabulary.Osco.datacenter)
					.getObject().asLiteral().getString();
			subnetContents.setDatacenter(datacenter);
		}

		if (subnet.hasProperty(info.openmultinet.ontology.vocabulary.Osco.mgmt)) {
			String mgmt = subnet
					.getProperty(
							info.openmultinet.ontology.vocabulary.Osco.mgmt)
					.getObject().asLiteral().getString();
			boolean mgmtBoolean = Boolean.parseBoolean(mgmt);
			subnetContents.setMgmt(mgmtBoolean);
		}

		if (subnet.hasProperty(RDFS.label)) {
			String name = subnet.getProperty(RDFS.label).getObject()
					.asLiteral().getString();
			subnetContents.setName(name);
		}

		String aboutUri = subnet.getURI();
		if (aboutUri != null && aboutUri != "") {
			subnetContents.setAbout(aboutUri);
		}

		osco.getImageOrOscoLocationOrSubnet().add(subnetContents);

	}

	private static void setOscoLocation(Osco osco, Resource location) {

		OscoLocationContents locationContents = new ObjectFactory()
				.createOscoLocationContents();

		if (location
				.hasProperty(info.openmultinet.ontology.vocabulary.Osco.name)) {
			String name = location
					.getProperty(
							info.openmultinet.ontology.vocabulary.Osco.name)
					.getObject().asLiteral().getString();
			locationContents.setName(name);
		}

		String aboutUri = location.getURI();
		if (aboutUri != null && aboutUri != "") {
			locationContents.setAbout(aboutUri);
		}

		osco.getImageOrOscoLocationOrSubnet().add(locationContents);
	}

	private static void setOscoImage(Osco osco, Resource imageResource) {

		ImageContents imageContents = new ObjectFactory().createImageContents();

		if (imageResource
				.hasProperty(info.openmultinet.ontology.vocabulary.Osco.datacenter)) {
			String datacenter = imageResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Osco.datacenter)
					.getObject().asLiteral().getString();
			imageContents.setDatacenter(datacenter);
		}

		if (imageResource
				.hasProperty(info.openmultinet.ontology.vocabulary.Osco.id)) {
			String id = imageResource
					.getProperty(info.openmultinet.ontology.vocabulary.Osco.id)
					.getObject().asLiteral().getString();
			imageContents.setId(id);
		}

		String aboutUri = imageResource.getURI();
		if (aboutUri != null && aboutUri != "") {
			imageContents.setAbout(aboutUri);
		}

		osco.getImageOrOscoLocationOrSubnet().add(imageContents);
	}

	public static void setStitching(Statement resource,
			StitchContent stitchContent) {

		if (resource.getResource().hasProperty(Omn_domain_pc.lastUpdateTime)) {
			String lastUpdateTime = resource.getResource()
					.getProperty(Omn_domain_pc.lastUpdateTime).getObject()
					.asLiteral().getString();
			stitchContent.setLastUpdateTime(lastUpdateTime);
		}

		StmtIterator resources = resource.getResource().listProperties(
				Omn.hasResource);

		while (resources.hasNext()) {
			Resource object = resources.next().getObject().asResource();
			if (object.hasProperty(RDF.type, Omn_resource.Path)) {
				final PathContent pathContent = new PathContent();

				if (object.hasProperty(Omn_lifecycle.hasID)) {
					String id = object.getProperty(Omn_lifecycle.hasID)
							.getObject().asLiteral().getString();
					pathContent.setId(id);
				}

				StmtIterator hops = object.listProperties(Omn.hasResource);

				while (hops.hasNext()) {
					HopContent hopContent = new HopContent();
					Resource hopObject = hops.next().getObject().asResource();
					if (hopObject.hasProperty(Omn_lifecycle.hasID)) {
						String id = hopObject.getProperty(Omn_lifecycle.hasID)
								.getObject().asLiteral().getString();
						hopContent.setId(id);
					}
					if (hopObject.hasProperty(Omn_domain_pc.hasHopType)) {
						String type = hopObject
								.getProperty(Omn_domain_pc.hasHopType)
								.getObject().asLiteral().getString();
						hopContent.setType(type);
					}
					if (hopObject.hasProperty(Omn_domain_pc.hasNextHop)) {
						String nextHop = hopObject
								.getProperty(Omn_domain_pc.hasNextHop)
								.getObject().asLiteral().getString();

						NextHopContent nextHopContent = new NextHopContent();
						nextHopContent.setValue(nextHop);
						hopContent.getNextHop().add(nextHopContent);
					}
					pathContent.getHop().add(hopContent);
				}

				stitchContent.getPath().add(pathContent);
			}
		}

	}

	public static void setOpenflow(Statement resource, Sliver of) {
		// get sliver description
		if (resource.getResource().hasProperty(RDFS.comment)) {
			String description = resource.getResource()
					.getProperty(RDFS.comment).getObject().asLiteral()
					.getString();
			of.setDescription(description);
		}

		// get sliver email
		if (resource.getResource().hasProperty(RDFS.seeAlso)) {
			String email = resource.getResource().getProperty(RDFS.seeAlso)
					.getObject().asResource().getURI().toString();
			if (email.contains("mailto:")) {
				email = email.substring(7, email.length());
			}
			of.setEmail(email);
		}

		StmtIterator types = resource.getResource().listProperties(
				Omn.hasResource);

		while (types.hasNext()) {

			Resource object = types.next().getObject().asResource();

			// set controller details
			if (object.hasProperty(RDF.type, Omn_domain_pc.Controller)) {

				Controller controller = new Controller();

				// set controller type
				if (object.hasProperty(Omn_domain_pc.hasControllerType)) {
					String type = object
							.getProperty(Omn_domain_pc.hasControllerType)
							.getObject().asLiteral().getString();
					controller.setType(ControllerRole.fromValue(type));
				}

				// set controller URL
				if (object.hasProperty(Omn_domain_pc.hasControllerUrl)) {
					String uri = object
							.getProperty(Omn_domain_pc.hasControllerUrl)
							.getObject().asResource().getURI().toString();
					controller.setUrl(uri);
				}

				of.getController().add(controller);
			}

			// set packet details
			if (object.hasProperty(RDF.type, Omn_domain_pc.Packet)) {

				UseGroup useGroup = new UseGroup();
				MatchContents match = new MatchContents();

				// set use group name
				if (object.hasProperty(RDFS.label)) {
					String usegroupName = object.getProperty(RDFS.label)
							.getObject().asLiteral().getString();
					useGroup.setName(usegroupName);
					;
				}

				PacketContents packet = new PacketContents();
				if (object.hasProperty(Omn_domain_pc.hasDlType)) {
					DlType dlType = new DlType();
					String dlTypeValue = object
							.getProperty(Omn_domain_pc.hasDlType).getObject()
							.asLiteral().getString();
					dlType.setValue(dlTypeValue);
					packet.setDlType(dlType);
				}

				if (object.hasProperty(Omn_domain_pc.hasDlVlan)) {
					DlVlan dlVlan = new DlVlan();
					String dlVlanValue = object
							.getProperty(Omn_domain_pc.hasDlVlan).getObject()
							.asLiteral().getString();
					dlVlan.setValue(dlVlanValue);
					packet.setDlVlan(dlVlan);
				}

				if (object.hasProperty(Omn_domain_pc.hasNwDst)) {
					NwDst nwDst = new NwDst();
					String dlVlanValue = object
							.getProperty(Omn_domain_pc.hasNwDst).getObject()
							.asLiteral().getString();
					nwDst.setValue(dlVlanValue);
					packet.setNwDst(nwDst);
				}

				if (object.hasProperty(Omn_domain_pc.hasNwSrc)) {
					NwSrc nwSrc = new NwSrc();
					String dlVlanValue = object
							.getProperty(Omn_domain_pc.hasNwSrc).getObject()
							.asLiteral().getString();
					nwSrc.setValue(dlVlanValue);
					packet.setNwSrc(nwSrc);
				}

				match.setPacket(packet);

				match.setUseGroup(useGroup);
				of.getMatch().add(match);
			}

			if (object.hasProperty(RDF.type, Omn_domain_pc.Datapath)) {
				GroupContents gc = new GroupContents();
				// set group name
				if (object.hasProperty(RDFS.label)) {
					String uri = object.getProperty(RDFS.label).getObject()
							.asLiteral().getString();
					gc.setName(uri);
				}

				Datapath dp = new Datapath();
				// set component ID
				if (object.hasProperty(Omn_lifecycle.hasComponentID)) {
					String componentId = object
							.getProperty(Omn_lifecycle.hasComponentID)
							.getObject().asLiteral().getString();
					dp.setComponentId(componentId);
				}

				// set component Manager ID
				if (object.hasProperty(Omn_lifecycle.managedBy)) {
					String componentManagerId = object
							.getProperty(Omn_lifecycle.managedBy).getObject()
							.asLiteral().getString();
					dp.setComponentManagerId(componentManagerId);
				}

				// set ID
				if (object.hasProperty(Omn_lifecycle.hasID)) {
					String id = object.getProperty(Omn_lifecycle.hasID)
							.getObject().asLiteral().getString();
					dp.setDpid(id);
				}

				gc.setDatapath(dp);
				of.getGroup().add(gc);
			}
		}
	}

	public static void setSharedVlan(Statement resource, LinkContents link) {

		StmtIterator types = resource.getResource().listProperties(
				Omn.hasResource);

		while (types.hasNext()) {

			Resource object = types.next().getObject().asResource();
			if (object.hasProperty(RDF.type, Omn_domain_pc.SharedVlan)) {
				ObjectFactory of = new ObjectFactory();
				LinkSharedVlan sharedVlanRspec = of.createLinkSharedVlan();
				if (object.hasProperty(RDFS.label)) {
					String name = object.getProperty(RDFS.label).getObject()
							.asLiteral().getString();
					sharedVlanRspec.setName(name);
				}
				if (object.hasProperty(Omn_domain_pc.vlanTag)) {
					String vlanTag = object.getProperty(Omn_domain_pc.vlanTag)
							.getObject().asLiteral().getString();
					sharedVlanRspec.setVlantag(vlanTag);
				}
				link.getAnyOrPropertyOrLinkType().add(sharedVlanRspec);
			}
		}
	}

	public static void setEmulabExtension(Statement resource, NodeContents node) {

		Resource resourceResource = resource.getResource();

		// checks if routableControlIp property present
		// it's a boolean property, so if present add to RSpec
		if (resourceResource.hasProperty(Omn_domain_pc.routableControlIp)) {
			RoutableControlIp routableControlIp = new ObjectFactory()
					.createRoutableControlIp();
			node.getAnyOrRelationOrLocation().add(routableControlIp);
		}
	}

	public static void setMonitoringService(Statement resource,
			NodeContents node) {

		Resource resourceResource = resource.getResource();
		if (resourceResource.hasProperty(Omn_lifecycle.usesService)) {
			Statement monitoringService = resourceResource
					.getProperty(Omn_lifecycle.usesService);
			Resource monitoringResource = monitoringService.getResource();
			Monitoring monitoring = new ObjectFactory().createMonitoring();

			if (monitoringResource.hasProperty(Omn.hasURI)) {
				Statement hasUri = monitoringService.getResource().getProperty(
						Omn.hasURI);
				String uri = hasUri.getObject().asLiteral().getString();
				monitoring.setUri(uri);

			}

			if (monitoringResource.hasProperty(RDF.type)) {
				final List<Statement> hasTypes = monitoringResource
						.listProperties(RDF.type).toList();

				for (final Statement hasType : hasTypes) {

					String type = hasType.getResource().getURI();

					if (AbstractConverter.nonGeneric(type)) {
						monitoring.setType(type);
					}
				}
			}

			node.getAnyOrRelationOrLocation().add(monitoring);
		}
	}

	public static void setAcs(Statement omnResource, NodeContents geniNode)
			throws InvalidModelException {
		if (omnResource.getResource().hasProperty(
				info.openmultinet.ontology.vocabulary.Acs.hasDevice)) {

			Resource resourceResource = omnResource
					.getProperty(
							info.openmultinet.ontology.vocabulary.Acs.hasDevice)
					.getObject().asResource();

			Device acsDevice = new ObjectFactory().createDevice();

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Acs.hasAcsId)) {
				String acsId = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Acs.hasAcsId)
						.getObject().asLiteral().getString();
				acsDevice.setId(acsId);
			}

			StmtIterator parameters = resourceResource
					.listProperties(info.openmultinet.ontology.vocabulary.Acs.hasParameter);
			while (parameters.hasNext()) {
				Statement parameterStatement = parameters.next();
				Resource parameter = parameterStatement.getObject()
						.asResource();

				ParameterContents parameterContents = new ObjectFactory()
						.createParameterContents();

				if (!parameter
						.hasProperty(info.openmultinet.ontology.vocabulary.Acs.hasParamName)
						|| !parameter
								.hasProperty(info.openmultinet.ontology.vocabulary.Acs.hasParamValue)) {
					throw new InvalidModelException(
							"Missing ACS paramter name or value.");
				}

				String name = parameter
						.getProperty(
								info.openmultinet.ontology.vocabulary.Acs.hasParamName)
						.getObject().asLiteral().getString();
				parameterContents.setName(name);

				String value = parameter
						.getProperty(
								info.openmultinet.ontology.vocabulary.Acs.hasParamValue)
						.getObject().asLiteral().getString();
				parameterContents.setValue(value);

				acsDevice.getParam().add(parameterContents);
			}

			geniNode.getAnyOrRelationOrLocation().add(acsDevice);

		}

	}

	public static void setFd(Statement resource, NodeContents node) {
		List<Statement> resources = resource.getResource()
				.listProperties(Omn.hasAttribute).toList();

		for (final Statement resourceStatement : resources) {
			// add emulab node slots
			if (resourceStatement.getResource().hasProperty(RDF.type,
					Omn_domain_pc.FeatureDescription)) {
				ObjectFactory of = new ObjectFactory();
				// name is required
				Fd fd = of.createFd();
				String name = resourceStatement.getResource()
						.getProperty(Omn_domain_pc.hasEmulabFdName).getObject()
						.asLiteral().getString();
				fd.setName(name);

				// weight is required
				String weight = resourceStatement.getResource()
						.getProperty(Omn_domain_pc.hasEmulabFdWeight)
						.getObject().asLiteral().getString();
				fd.setWeight(weight);

				if (resourceStatement.getResource().hasProperty(
						Omn_domain_pc.emulabFdViolatable)) {
					String violatable = resourceStatement.getResource()
							.getProperty(Omn_domain_pc.emulabFdViolatable)
							.getObject().asLiteral().getString();
					fd.setViolatable(violatable);
				}

				if (resourceStatement.getResource().hasProperty(
						Omn_domain_pc.hasEmulabFdLocalOperator)) {
					String localOperator = resourceStatement
							.getResource()
							.getProperty(Omn_domain_pc.hasEmulabFdLocalOperator)
							.getObject().asLiteral().getString();
					fd.setLocalOperator(localOperator);
				}

				if (resourceStatement.getResource().hasProperty(
						Omn_domain_pc.hasEmulabFdGlobalOperator)) {
					String localOperator = resourceStatement
							.getResource()
							.getProperty(
									Omn_domain_pc.hasEmulabFdGlobalOperator)
							.getObject().asLiteral().getString();
					fd.setGlobalOperator(localOperator);
				}

				node.getAnyOrRelationOrLocation().add(fd);
			}
		}
	}

	public static void setFivegGateway(Statement omnResource, NodeContents node) {
		if (omnResource.getResource().hasProperty(RDF.type, Fiveg.Gateway)) {

			Resource resourceResource = omnResource.getResource();

			Gateway fiveg = new ObjectFactory().createGateway();

			if (resourceResource.hasProperty(Fiveg.version)) {
				String version = resourceResource.getProperty(Fiveg.version)
						.getObject().asLiteral().getString();
				fiveg.setVersion(version);
			}

			if (resourceResource.hasProperty(Fiveg.upstartOn)) {
				String upstartOn = resourceResource
						.getProperty(Fiveg.upstartOn).getObject().asLiteral()
						.getString();
				boolean upstartOnBool = Boolean.parseBoolean(upstartOn);
				fiveg.setUpstartOn(upstartOnBool);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ipServicesNetwork)) {
				int netAIntf = resourceResource
						.getProperty(Fiveg.ipServicesNetwork).getObject()
						.asLiteral().getInt();
				BigInteger bigNetAIntf = BigInteger.valueOf(netAIntf);
				fiveg.setNetAIntf(bigNetAIntf);
			}

			if (resourceResource.hasProperty(Fiveg.cloudManagementIP)) {

				Resource ipAddress = resourceResource
						.getProperty(Fiveg.cloudManagementIP).getObject()
						.asResource();
				FiveGIpContents ipAddressContents = new ObjectFactory()
						.createFiveGIpContents();
				fiveg.setCloudMgmtGwIp(ipAddressContents);

				if (ipAddress
						.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.address)) {
					String address = ipAddress
							.getProperty(
									info.openmultinet.ontology.vocabulary.Omn_resource.address)
							.getObject().asLiteral().getString();
					fiveg.getCloudMgmtGwIp().setAddress(address);
					// ipAddressContents.setAddress(address);
				}

				if (ipAddress
						.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.netmask)) {
					String netmask = ipAddress
							.getProperty(
									info.openmultinet.ontology.vocabulary.Omn_resource.netmask)
							.getObject().asLiteral().getString();
					fiveg.getCloudMgmtGwIp().setNetmask(netmask);
					// ipAddressContents.setNetmask(netmask);
				}

				if (ipAddress
						.hasProperty(info.openmultinet.ontology.vocabulary.Omn_resource.type)) {
					String type = ipAddress
							.getProperty(
									info.openmultinet.ontology.vocabulary.Omn_resource.type)
							.getObject().asLiteral().getString();
					// ipAddressContents.setType(type);
					fiveg.getCloudMgmtGwIp().setType(type);
				}
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}
	}

	public static void setFivegDns(Statement omnResource, NodeContents node) {
		if (omnResource.getResource().hasProperty(RDF.type,
				Fiveg.DomainNameSystem)) {

			Resource resourceResource = omnResource.getResource();

			Dns fiveg = new ObjectFactory().createDns();

			if (resourceResource.hasProperty(Fiveg.additionals)) {
				String additionals = resourceResource
						.getProperty(Fiveg.additionals).getObject().asLiteral()
						.getString();
				fiveg.setAdditionals(additionals);
			}

			if (resourceResource.hasProperty(Fiveg.domainName)) {
				String domainName = resourceResource
						.getProperty(Fiveg.domainName).getObject().asLiteral()
						.getString();
				fiveg.setDomainName(domainName);
			}

			if (resourceResource.hasProperty(Fiveg.domainNs)) {
				String domainNs = resourceResource.getProperty(Fiveg.domainNs)
						.getObject().asLiteral().getString();
				fiveg.setDomainNs(domainNs);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ipServicesNetwork)) {
				int netAIntf = resourceResource
						.getProperty(Fiveg.ipServicesNetwork).getObject()
						.asLiteral().getInt();
				BigInteger bigNetAIntf = BigInteger.valueOf(netAIntf);
				fiveg.setNetAIntf(bigNetAIntf);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}
	}

	public static void setFivegSwitch(Statement omnResource, NodeContents node) {
		if (omnResource.getResource().hasProperty(RDF.type, Fiveg.Switch)) {

			Resource resourceResource = omnResource.getResource();

			Switch fiveg = new ObjectFactory().createSwitch();

			if (resourceResource.hasProperty(Fiveg.pgwUBaseId)) {
				String pgwUBaseId = resourceResource
						.getProperty(Fiveg.pgwUBaseId).getObject().asLiteral()
						.getString();
				fiveg.setPgwUBaseId(pgwUBaseId);
			}

			if (resourceResource.hasProperty(Fiveg.upstartOn)) {
				String upstartOn = resourceResource
						.getProperty(Fiveg.upstartOn).getObject().asLiteral()
						.getString();
				boolean upstartOnBool = Boolean.parseBoolean(upstartOn);
				fiveg.setUpstartOn(upstartOnBool);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ipServicesNetwork)) {
				int netAIntf = resourceResource
						.getProperty(Fiveg.ipServicesNetwork).getObject()
						.asLiteral().getInt();
				BigInteger bigNetAIntf = BigInteger.valueOf(netAIntf);
				fiveg.setNetAIntf(bigNetAIntf);
			}

			if (resourceResource.hasProperty(Fiveg.pgwUDownloadInterface)) {
				int pgwUDownloadInterface = resourceResource
						.getProperty(Fiveg.pgwUDownloadInterface).getObject()
						.asLiteral().getInt();
				BigInteger pgwUDownloadIntf = BigInteger
						.valueOf(pgwUDownloadInterface);
				fiveg.setPgwUDownloadInterface(pgwUDownloadIntf);
			}

			if (resourceResource.hasProperty(Fiveg.pgwUUploadInterface)) {
				int pgwUUploadInterface = resourceResource
						.getProperty(Fiveg.pgwUUploadInterface).getObject()
						.asLiteral().getInt();
				BigInteger pgwUUploadIntf = BigInteger
						.valueOf(pgwUUploadInterface);
				fiveg.setPgwUUploadInterface(pgwUUploadIntf);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.sgwUDownloadInterface)) {
				int sgwUDownloadInterface = resourceResource
						.getProperty(Fiveg.sgwUDownloadInterface).getObject()
						.asLiteral().getInt();
				BigInteger sgwUDownloadIntf = BigInteger
						.valueOf(sgwUDownloadInterface);
				fiveg.setSgwUDownloadInterface(sgwUDownloadIntf);
			}

			if (resourceResource.hasProperty(Fiveg.sgwUUploadInterface)) {
				int sgwUUploadInterface = resourceResource
						.getProperty(Fiveg.sgwUUploadInterface).getObject()
						.asLiteral().getInt();
				BigInteger sgwUUploadIntf = BigInteger
						.valueOf(sgwUUploadInterface);
				fiveg.setSgwUUploadInterface(sgwUUploadIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ranBackhaul)) {
				int netDIntf = resourceResource.getProperty(Fiveg.ranBackhaul)
						.getObject().asLiteral().getInt();
				BigInteger bigNetDIntf = BigInteger.valueOf(netDIntf);
				fiveg.setNetDIntf(bigNetDIntf);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}

	}

	public static void setOlChannel(Statement resource, Channel of) {
		Resource resourceResource = resource.getResource();

		if (resourceResource.hasProperty(Omn_domain_wireless.channelNum)) {
			int channelNum = resourceResource
					.getProperty(Omn_domain_wireless.channelNum).getLiteral()
					.getInt();
			of.setComponentName(Integer.toString(channelNum));
		} else if (resourceResource.hasProperty(Omn_lifecycle.hasComponentName)) {
			String componentName = resourceResource
					.getProperty(Omn_lifecycle.hasComponentName).getLiteral()
					.getString();
			of.setComponentName(componentName);
		}

		if (resourceResource.hasProperty(Omn_domain_wireless.usesFrequency)) {
			Resource freqResource = resourceResource
					.getProperty(Omn_domain_wireless.usesFrequency).getObject()
					.asResource();

			String frequency = CommonMethods
					.getStringFromFrequency(freqResource);
			of.setFrequency(frequency);
		}

		if (resourceResource.hasProperty(Omn_lifecycle.hasComponentID)) {
			String componentId = resourceResource
					.getProperty(Omn_lifecycle.hasComponentID).getObject()
					.asLiteral().getString();
			of.setComponentId(componentId);
		}

		if (resourceResource.hasProperty(Omn_lifecycle.managedBy)) {
			String componentManagerId = resourceResource
					.getProperty(Omn_lifecycle.managedBy).getObject()
					.asResource().getURI();
			of.setComponentManagerId(componentManagerId);
		}
	}

	public static void setOlLease(Statement resource, NodeContents node) {
		if (resource.getResource().hasProperty(RDF.type, Omn_lifecycle.Lease)) {
			final Lease of = new Lease();

			Resource resourceResource = resource.getResource();
			if (resourceResource.hasProperty(Omn_lifecycle.hasID)) {
				String leaseId = resourceResource
						.getProperty(Omn_lifecycle.hasID).getObject()
						.asLiteral().getString();
				of.setLeaseID(leaseId);
			}

			if (resourceResource.hasProperty(Omn_lifecycle.hasIdRef)) {
				String leaseIdRef = resourceResource
						.getProperty(Omn_lifecycle.hasIdRef).getObject()
						.asLiteral().getString();
				if (leaseIdRef.equals(resourceResource.getURI())) {
					of.setLeaseREF(of);
				}
			}

			if (resourceResource.hasProperty(Omn_lifecycle.hasSliceID)) {
				String sliceId = resourceResource
						.getProperty(Omn_lifecycle.hasSliceID).getObject()
						.asLiteral().getString();
				of.setSliceId(sliceId);
			}

			if (resourceResource.hasProperty(Omn_domain_pc.hasUUID)) {
				String uuid = resourceResource
						.getProperty(Omn_domain_pc.hasUUID).getObject()
						.asLiteral().getString();
				of.setUuid(uuid);
			}

			if (resourceResource.hasProperty(Omn_lifecycle.startTime)) {

				Object startTime = ((Object) resourceResource
						.getProperty(Omn_lifecycle.startTime).getObject()
						.asLiteral().getValue());

				XMLGregorianCalendar start = null;
				if (startTime instanceof XSDDateTime) {
					XSDDateTime time = (XSDDateTime) startTime;
					start = AbstractConverter.xsdToXmlTime(time);
				}
				of.setValidFrom(start);
			}

			if (resourceResource.hasProperty(Omn_lifecycle.expirationTime)) {

				Object endTime = ((Object) resourceResource
						.getProperty(Omn_lifecycle.expirationTime).getObject()
						.asLiteral().getValue());

				XMLGregorianCalendar end = null;
				if (endTime instanceof XSDDateTime) {
					XSDDateTime time = (XSDDateTime) endTime;
					end = AbstractConverter.xsdToXmlTime(time);
				}
				of.setValidUntil(end);
			}

			node.getAnyOrRelationOrLocation().add(of);
		}

	}

	public static void setOlLease(Statement resource,
			info.openmultinet.ontology.translators.geni.jaxb.request.Lease of) {
		Resource resourceResource = resource.getResource();

		if (resourceResource.hasProperty(Omn_lifecycle.hasID)) {
			String leaseId = resourceResource.getProperty(Omn_lifecycle.hasID)
					.getObject().asLiteral().getString();
			of.setLeaseID(leaseId);
		}

		if (resourceResource.hasProperty(Omn_lifecycle.hasIdRef)) {
			String leaseIdRef = resourceResource
					.getProperty(Omn_lifecycle.hasIdRef).getObject()
					.asLiteral().getString();
			if (leaseIdRef.equals(resourceResource.getURI())) {
				of.setLeaseREF(of);
			}
		}

		if (resourceResource.hasProperty(Omn_lifecycle.hasSliceID)) {
			String sliceId = resourceResource
					.getProperty(Omn_lifecycle.hasSliceID).getObject()
					.asLiteral().getString();
			of.setSliceId(sliceId);
		}

		if (resourceResource.hasProperty(Omn_domain_pc.hasUUID)) {
			String uuid = resourceResource.getProperty(Omn_domain_pc.hasUUID)
					.getObject().asLiteral().getString();
			of.setUuid(uuid);
		}

		if (resourceResource.hasProperty(Omn_lifecycle.startTime)) {

			Object startTime = ((Object) resourceResource
					.getProperty(Omn_lifecycle.startTime).getObject()
					.asLiteral().getValue());

			XMLGregorianCalendar start = null;
			if (startTime instanceof XSDDateTime) {
				XSDDateTime time = (XSDDateTime) startTime;
				start = AbstractConverter.xsdToXmlTime(time);
			}
			of.setValidFrom(start);
		}

		if (resourceResource.hasProperty(Omn_lifecycle.expirationTime)) {

			Object endTime = ((Object) resourceResource
					.getProperty(Omn_lifecycle.expirationTime).getObject()
					.asLiteral().getValue());

			XMLGregorianCalendar end = null;
			if (endTime instanceof XSDDateTime) {
				XSDDateTime time = (XSDDateTime) endTime;
				end = AbstractConverter.xsdToXmlTime(time);
			}
			of.setValidUntil(end);
		}

	}

	public static void setFivegBt(Statement omnResource, NodeContents node) {

		if (omnResource.getResource().hasProperty(RDF.type,
				Fiveg.BenchmarkingTool)) {

			Resource resourceResource = omnResource.getResource();

			Bt fiveg = new ObjectFactory().createBt();

			if (resourceResource.hasProperty(Fiveg.consolePort)) {
				int consolePort = resourceResource
						.getProperty(Fiveg.consolePort).getObject().asLiteral()
						.getInt();
				BigInteger bigConsolePort = BigInteger.valueOf(consolePort);
				fiveg.setConsolePort(bigConsolePort);
			}

			if (resourceResource.hasProperty(Fiveg.benchmarkingToolHostName)) {
				String benchmarkingToolHostName = resourceResource
						.getProperty(Fiveg.benchmarkingToolHostName)
						.getObject().asLiteral().getString();
				fiveg.setBtHostName(benchmarkingToolHostName);
			}

			if (resourceResource.hasProperty(Fiveg.upstartOn)) {
				String upstartOn = resourceResource
						.getProperty(Fiveg.upstartOn).getObject().asLiteral()
						.getString();
				boolean upstartOnBool = Boolean.parseBoolean(upstartOn);
				fiveg.setUpstartOn(upstartOnBool);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.subscriberIpRange)) {
				int netCIntf = resourceResource
						.getProperty(Fiveg.subscriberIpRange).getObject()
						.asLiteral().getInt();
				BigInteger bigNetCIntf = BigInteger.valueOf(netCIntf);
				fiveg.setNetCIntf(bigNetCIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ranBackhaul)) {
				int netDIntf = resourceResource.getProperty(Fiveg.ranBackhaul)
						.getObject().asLiteral().getInt();
				BigInteger bigNetDIntf = BigInteger.valueOf(netDIntf);
				fiveg.setNetDIntf(bigNetDIntf);
			}

			if (resourceResource.hasProperty(Fiveg.dnsInterface)) {
				int dnsIntf = resourceResource.getProperty(Fiveg.dnsInterface)
						.getObject().asLiteral().getInt();
				BigInteger bigDnsIntf = BigInteger.valueOf(dnsIntf);
				fiveg.setDnsIntf(bigDnsIntf);
			}

			if (resourceResource.hasProperty(Fiveg.netIp)) {
				String netIp = resourceResource.getProperty(Fiveg.netIp)
						.getObject().asLiteral().getString();
				fiveg.setNetIp(netIp);
			}

			if (resourceResource.hasProperty(Fiveg.netMask)) {
				String netMask = resourceResource.getProperty(Fiveg.netMask)
						.getObject().asLiteral().getString();
				fiveg.setNetMask(netMask);
			}

			if (resourceResource.hasProperty(Fiveg.ipRangeStart)) {
				String ipRangeStart = resourceResource
						.getProperty(Fiveg.ipRangeStart).getObject()
						.asLiteral().getString();
				fiveg.setIpRangeStart(ipRangeStart);
			}
			if (resourceResource.hasProperty(Fiveg.ipRangeEnd)) {
				String ipRangeEnd = resourceResource
						.getProperty(Fiveg.ipRangeEnd).getObject().asLiteral()
						.getString();
				fiveg.setIpRangeEnd(ipRangeEnd);
			}

			if (resourceResource.hasProperty(Fiveg.imsiRangeStart)) {
				String imsiRangeStart = resourceResource
						.getProperty(Fiveg.imsiRangeStart).getObject()
						.asLiteral().getString();
				fiveg.setImsiRangeStart(imsiRangeStart);
			}
			if (resourceResource.hasProperty(Fiveg.imsiRangeEnd)) {
				String imsiRangeEnd = resourceResource
						.getProperty(Fiveg.imsiRangeEnd).getObject()
						.asLiteral().getString();
				fiveg.setImsiRangeEnd(imsiRangeEnd);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.userEquipmentAddress)) {
				String userEquipmentAddress = resourceResource
						.getProperty(Fiveg.userEquipmentAddress).getObject()
						.asLiteral().getString();
				fiveg.setUeAddr(userEquipmentAddress);
			}

			if (resourceResource.hasProperty(Fiveg.cloudPublicRouterIp)) {
				String cloudPublicRouterIp = resourceResource
						.getProperty(Fiveg.cloudPublicRouterIp).getObject()
						.asLiteral().getString();
				fiveg.setCloudPublicRouterIp(cloudPublicRouterIp);
			}

			if (resourceResource.hasProperty(Fiveg.useFloatingIps)) {
				String useFloatingIps = resourceResource
						.getProperty(Fiveg.useFloatingIps).getObject()
						.asLiteral().getString();
				boolean useFloatingIpsBool = Boolean
						.parseBoolean(useFloatingIps);
				fiveg.setUseFloatingIps(useFloatingIpsBool);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}

	}

	public static void setFivegEnodeb(Statement omnResource, NodeContents node) {

		if (omnResource.getResource().hasProperty(RDF.type, Fiveg.ENodeB)) {

			Resource resourceResource = omnResource.getResource();

			Enodeb fiveg = new ObjectFactory().createEnodeb();

			if (resourceResource.hasProperty(Fiveg.consolePort)) {
				int consolePort = resourceResource
						.getProperty(Fiveg.consolePort).getObject().asLiteral()
						.getInt();
				BigInteger bigConsolePort = BigInteger.valueOf(consolePort);
				fiveg.setConsolePort(bigConsolePort);
			}

			if (resourceResource.hasProperty(Fiveg.enodebHostName)) {
				String enodebHostName = resourceResource
						.getProperty(Fiveg.enodebHostName).getObject()
						.asLiteral().getString();
				fiveg.setEnodebHostName(enodebHostName);
			}

			if (resourceResource.hasProperty(Fiveg.upstartOn)) {
				String upstartOn = resourceResource
						.getProperty(Fiveg.upstartOn).getObject().asLiteral()
						.getString();
				boolean upstartOnBool = Boolean.parseBoolean(upstartOn);
				fiveg.setUpstartOn(upstartOnBool);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.subscriberIpRange)) {
				int netCIntf = resourceResource
						.getProperty(Fiveg.subscriberIpRange).getObject()
						.asLiteral().getInt();
				BigInteger bigNetCIntf = BigInteger.valueOf(netCIntf);
				fiveg.setNetCIntf(bigNetCIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ranBackhaul)) {
				int netDIntf = resourceResource.getProperty(Fiveg.ranBackhaul)
						.getObject().asLiteral().getInt();
				BigInteger bigNetDIntf = BigInteger.valueOf(netDIntf);
				fiveg.setNetDIntf(bigNetDIntf);
			}

			if (resourceResource.hasProperty(Fiveg.dnsInterface)) {
				int dnsIntf = resourceResource.getProperty(Fiveg.dnsInterface)
						.getObject().asLiteral().getInt();
				BigInteger bigDnsIntf = BigInteger.valueOf(dnsIntf);
				fiveg.setDnsIntf(bigDnsIntf);
			}

			if (resourceResource.hasProperty(Fiveg.netIp)) {
				String netIp = resourceResource.getProperty(Fiveg.netIp)
						.getObject().asLiteral().getString();
				fiveg.setNetIp(netIp);
			}

			if (resourceResource.hasProperty(Fiveg.netMask)) {
				String netMask = resourceResource.getProperty(Fiveg.netMask)
						.getObject().asLiteral().getString();
				fiveg.setNetMask(netMask);
			}

			if (resourceResource.hasProperty(Fiveg.ipRangeStart)) {
				String ipRangeStart = resourceResource
						.getProperty(Fiveg.ipRangeStart).getObject()
						.asLiteral().getString();
				fiveg.setIpRangeStart(ipRangeStart);
			}
			if (resourceResource.hasProperty(Fiveg.ipRangeEnd)) {
				String ipRangeEnd = resourceResource
						.getProperty(Fiveg.ipRangeEnd).getObject().asLiteral()
						.getString();
				fiveg.setIpRangeEnd(ipRangeEnd);
			}

			if (resourceResource.hasProperty(Fiveg.imsiRangeStart)) {
				String imsiRangeStart = resourceResource
						.getProperty(Fiveg.imsiRangeStart).getObject()
						.asLiteral().getString();
				fiveg.setImsiRangeStart(imsiRangeStart);
			}
			if (resourceResource.hasProperty(Fiveg.imsiRangeEnd)) {
				String imsiRangeEnd = resourceResource
						.getProperty(Fiveg.imsiRangeEnd).getObject()
						.asLiteral().getString();
				fiveg.setImsiRangeEnd(imsiRangeEnd);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.userEquipmentAddress)) {
				String userEquipmentAddress = resourceResource
						.getProperty(Fiveg.userEquipmentAddress).getObject()
						.asLiteral().getString();
				fiveg.setUeAddr(userEquipmentAddress);
			}

			if (resourceResource.hasProperty(Fiveg.cloudPublicRouterIp)) {
				String cloudPublicRouterIp = resourceResource
						.getProperty(Fiveg.cloudPublicRouterIp).getObject()
						.asLiteral().getString();
				fiveg.setCloudPublicRouterIp(cloudPublicRouterIp);
			}

			if (resourceResource.hasProperty(Fiveg.useFloatingIps)) {
				String useFloatingIps = resourceResource
						.getProperty(Fiveg.useFloatingIps).getObject()
						.asLiteral().getString();
				boolean useFloatingIpsBool = Boolean
						.parseBoolean(useFloatingIps);
				fiveg.setUseFloatingIps(useFloatingIpsBool);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}

	}

	public static void setFivegControl(Statement omnResource, NodeContents node) {

		if (omnResource.getResource().hasProperty(RDF.type, Fiveg.Control)) {

			Resource resourceResource = omnResource.getResource();

			Control fiveg = new ObjectFactory().createControl();

			if (resourceResource.hasProperty(Fiveg.mmeConsolePort)) {
				int mmeConsolePort = resourceResource
						.getProperty(Fiveg.mmeConsolePort).getObject()
						.asLiteral().getInt();
				BigInteger mmeConsolePortBig = BigInteger
						.valueOf(mmeConsolePort);
				fiveg.setMmeConsolePort(mmeConsolePortBig);
			}

			if (resourceResource.hasProperty(Fiveg.init)) {
				String init = resourceResource.getProperty(Fiveg.init)
						.getObject().asLiteral().getString();
				boolean initBool = Boolean.parseBoolean(init);
				fiveg.setInit(initBool);
			}

			if (resourceResource.hasProperty(Fiveg.pgwCOfpCtrTransport)) {
				String pgwCOfpCtrTransport = resourceResource
						.getProperty(Fiveg.pgwCOfpCtrTransport).getObject()
						.asLiteral().getString();
				fiveg.setPgwCOfpCtrTransport(pgwCOfpCtrTransport);
			}

			if (resourceResource.hasProperty(Fiveg.sgwCOfpCtrPort)) {
				int sgwCOfpCtrPort = resourceResource
						.getProperty(Fiveg.sgwCOfpCtrPort).getObject()
						.asLiteral().getInt();
				BigInteger sgwCOfpCtrPortBig = BigInteger
						.valueOf(sgwCOfpCtrPort);
				fiveg.setSgwCOfpCtrPort(sgwCOfpCtrPortBig);
			}

			if (resourceResource.hasProperty(Fiveg.sgwCJsonSrvPort)) {
				int sgwCJsonSrvPort = resourceResource
						.getProperty(Fiveg.sgwCJsonSrvPort).getObject()
						.asLiteral().getInt();
				BigInteger sgwCJsonSrvPortBig = BigInteger
						.valueOf(sgwCJsonSrvPort);
				fiveg.setSgwCJsonSrvPort(sgwCJsonSrvPortBig);
			}

			if (resourceResource.hasProperty(Fiveg.pgwCConsolePort)) {
				int pgwCConsolePort = resourceResource
						.getProperty(Fiveg.pgwCConsolePort).getObject()
						.asLiteral().getInt();
				BigInteger pgwCConsolePortBig = BigInteger
						.valueOf(pgwCConsolePort);
				fiveg.setPgwCConsolePort(pgwCConsolePortBig);
			}

			if (resourceResource.hasProperty(Fiveg.pgwCOfpCtrPort)) {
				int pgwCOfpCtrPort = resourceResource
						.getProperty(Fiveg.pgwCOfpCtrPort).getObject()
						.asLiteral().getInt();
				BigInteger pgwCOfpCtrPortBig = BigInteger
						.valueOf(pgwCOfpCtrPort);
				fiveg.setPgwCOfpCtrPort(pgwCOfpCtrPortBig);
			}

			if (resourceResource.hasProperty(Fiveg.mmeHostName)) {
				String mmeHostName = resourceResource
						.getProperty(Fiveg.mmeHostName).getObject().asLiteral()
						.getString();
				fiveg.setMmeHostName(mmeHostName);
			}

			if (resourceResource.hasProperty(Fiveg.pgwCTemplateConfigFile)) {
				String pgwCTemplateConfigFile = resourceResource
						.getProperty(Fiveg.pgwCTemplateConfigFile).getObject()
						.asLiteral().getString();
				fiveg.setPgwCTemplateConfigFile(pgwCTemplateConfigFile);
			}

			if (resourceResource.hasProperty(Fiveg.upstartOn)) {
				String upstartOn = resourceResource
						.getProperty(Fiveg.upstartOn).getObject().asLiteral()
						.getString();
				boolean upstartOnBool = Boolean.parseBoolean(upstartOn);
				fiveg.setUpstartOn(upstartOnBool);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.dnsInterface)) {
				int dnsIntf = resourceResource.getProperty(Fiveg.dnsInterface)
						.getObject().asLiteral().getInt();
				BigInteger bigDnsIntf = BigInteger.valueOf(dnsIntf);
				fiveg.setDnsIntf(bigDnsIntf);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.ranBackhaul)) {
				int netDIntf = resourceResource.getProperty(Fiveg.ranBackhaul)
						.getObject().asLiteral().getInt();
				BigInteger bigNetDIntf = BigInteger.valueOf(netDIntf);
				fiveg.setNetDIntf(bigNetDIntf);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}

	}

	public static void setFivegHss(Statement omnResource, NodeContents node) {

		if (omnResource.getResource().hasProperty(RDF.type,
				Fiveg.HomeSubscriberServer)) {

			Resource resourceResource = omnResource.getResource();

			Hss fiveg = new ObjectFactory().createHss();

			if (resourceResource.hasProperty(Fiveg.localDatabase)) {
				int localDatabase = resourceResource
						.getProperty(Fiveg.localDatabase).getObject()
						.asLiteral().getInt();
				BigInteger localDatabaseBig = BigInteger.valueOf(localDatabase);
				fiveg.setLocalDb(localDatabaseBig);
			}

			if (resourceResource.hasProperty(Fiveg.databaseProvi)) {
				int databaseProvi = resourceResource
						.getProperty(Fiveg.databaseProvi).getObject()
						.asLiteral().getInt();
				BigInteger databaseProviBig = BigInteger.valueOf(databaseProvi);
				fiveg.setDbProvi(databaseProviBig);
			}

			if (resourceResource.hasProperty(Fiveg.databaseUser)) {
				String databaseUser = resourceResource
						.getProperty(Fiveg.databaseUser).getObject()
						.asLiteral().getString();
				fiveg.setDbUser(databaseUser);
			}

			if (resourceResource.hasProperty(Fiveg.databasePassword)) {
				String databasePassword = resourceResource
						.getProperty(Fiveg.databasePassword).getObject()
						.asLiteral().getString();
				fiveg.setDbPw(databasePassword);
			}

			if (resourceResource.hasProperty(Fiveg.databaseName)) {
				String databaseName = resourceResource
						.getProperty(Fiveg.databaseName).getObject()
						.asLiteral().getString();
				fiveg.setDbName(databaseName);
			}

			if (resourceResource.hasProperty(Fiveg.domainName)) {
				String domainName = resourceResource
						.getProperty(Fiveg.domainName).getObject().asLiteral()
						.getString();
				fiveg.setDomainName(domainName);
			}

			if (resourceResource.hasProperty(Fiveg.port)) {
				int port = resourceResource.getProperty(Fiveg.port).getObject()
						.asLiteral().getInt();
				BigInteger portBig = BigInteger.valueOf(port);
				fiveg.setPort(portBig);
			}

			if (resourceResource.hasProperty(Fiveg.slfPresence)) {
				String slfPresence = resourceResource
						.getProperty(Fiveg.slfPresence).getObject().asLiteral()
						.getString();
				boolean slfPresenceBool = Boolean.parseBoolean(slfPresence);
				fiveg.setSlfPresence(slfPresenceBool);
			}

			if (resourceResource.hasProperty(Fiveg.consolePortOne)) {
				int consolePortOne = resourceResource
						.getProperty(Fiveg.consolePortOne).getObject()
						.asLiteral().getInt();
				BigInteger consolePortOneBig = BigInteger
						.valueOf(consolePortOne);
				fiveg.setConsolePortOne(consolePortOneBig);
			}

			if (resourceResource.hasProperty(Fiveg.consolePortTwo)) {
				int consolePortTwo = resourceResource
						.getProperty(Fiveg.consolePortTwo).getObject()
						.asLiteral().getInt();
				BigInteger consolePortTwoBig = BigInteger
						.valueOf(consolePortTwo);
				fiveg.setConsolePortTwo(consolePortTwoBig);
			}

			if (resourceResource.hasProperty(Fiveg.consolePortBindOne)) {
				String consolePortBindOne = resourceResource
						.getProperty(Fiveg.consolePortBindOne).getObject()
						.asLiteral().getString();
				fiveg.setConsolePortBindOne(consolePortBindOne);
			}

			if (resourceResource.hasProperty(Fiveg.consolePortBindTwo)) {
				String consolePortBindTwo = resourceResource
						.getProperty(Fiveg.consolePortBindTwo).getObject()
						.asLiteral().getString();
				fiveg.setConsolePortBindTwo(consolePortBindTwo);
			}

			if (resourceResource.hasProperty(Fiveg.diameterListenIntf)) {
				int diameterListenIntf = resourceResource
						.getProperty(Fiveg.diameterListenIntf).getObject()
						.asLiteral().getInt();
				BigInteger diameterListenIntfBig = BigInteger
						.valueOf(diameterListenIntf);
				fiveg.setDiameterListenIntf(diameterListenIntfBig);
			}

			if (resourceResource.hasProperty(Fiveg.managementInterface)) {
				int mgmtIntf = resourceResource
						.getProperty(Fiveg.managementInterface).getObject()
						.asLiteral().getInt();
				BigInteger bigMgmtIntf = BigInteger.valueOf(mgmtIntf);
				fiveg.setMgmtIntf(bigMgmtIntf);
			}

			if (resourceResource.hasProperty(Fiveg.dnsInterface)) {
				int dnsIntf = resourceResource.getProperty(Fiveg.dnsInterface)
						.getObject().asLiteral().getInt();
				BigInteger bigDnsIntf = BigInteger.valueOf(dnsIntf);
				fiveg.setDnsIntf(bigDnsIntf);
			}

			if (resourceResource.hasProperty(Fiveg.defaultRouteVia)) {
				String defaultRouteVia = resourceResource
						.getProperty(Fiveg.defaultRouteVia).getObject()
						.asLiteral().getString();
				fiveg.setDefaultRouteVia(defaultRouteVia);
			}

			if (resourceResource.hasProperty(Fiveg.minInterfaces)) {
				int minNumIntf = resourceResource
						.getProperty(Fiveg.minInterfaces).getObject()
						.asLiteral().getInt();
				BigInteger bigMinNumIntf = BigInteger.valueOf(minNumIntf);
				fiveg.setMinNumIntf(bigMinNumIntf);
			}

			if (resourceResource.hasProperty(Fiveg.version)) {
				String version = resourceResource.getProperty(Fiveg.version)
						.getObject().asLiteral().getString();
				fiveg.setVersion(version);
			}

			if (resourceResource.hasProperty(Fiveg.requires)) {
				String requires = resourceResource.getProperty(Fiveg.requires)
						.getObject().asLiteral().getString();
				fiveg.setRequires(requires);
			}

			node.getAnyOrRelationOrLocation().add(fiveg);
		}

	}
}
