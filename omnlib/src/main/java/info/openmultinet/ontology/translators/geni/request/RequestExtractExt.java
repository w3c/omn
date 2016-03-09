package info.openmultinet.ontology.translators.geni.request;

import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.request.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.request.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.request.ApnContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Bt;
import info.openmultinet.ontology.translators.geni.jaxb.request.Control;
import info.openmultinet.ontology.translators.geni.jaxb.request.ControlAddressContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Device;
import info.openmultinet.ontology.translators.geni.jaxb.request.Dns;
import info.openmultinet.ontology.translators.geni.jaxb.request.ENodeBContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Enodeb;
import info.openmultinet.ontology.translators.geni.jaxb.request.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.request.EpcIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.request.FiveGIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Gateway;
import info.openmultinet.ontology.translators.geni.jaxb.request.Hss;
import info.openmultinet.ontology.translators.geni.jaxb.request.ImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.Location;
import info.openmultinet.ontology.translators.geni.jaxb.request.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.request.Osco;
import info.openmultinet.ontology.translators.geni.jaxb.request.OscoLocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.PDNGatewayContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ParameterContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.SubnetContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Switch;
import info.openmultinet.ontology.translators.geni.jaxb.request.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.request.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.UeHardwareTypeContents;
import info.openmultinet.ontology.vocabulary.Fiveg;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.math.BigInteger;
import java.net.URI;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from a request RSpec to create an OMN
 * model. These methods are for non-native RSpec extensions.
 * 
 * @author robynloughnane
 *
 */
public class RequestExtractExt extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";
	private static final Logger LOG = Logger.getLogger(RequestExtractExt.class
			.getName());

	public static void extractStitching(Resource topology, Object o) {
		Model model = topology.getModel();
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource stitchResource = model.createResource(uuid);
		stitchResource.addProperty(RDF.type, Omn_resource.Stitching);

		ElementNSImpl stitch = ((org.apache.xerces.dom.ElementNSImpl) o);
		NamedNodeMap attributes = stitch.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.item(i).getNodeName().equals("lastUpdateTime")) {
				String lastUpdate = attributes.item(i).getNodeValue();
				stitchResource.addProperty(Omn_domain_pc.lastUpdateTime,
						lastUpdate);
			}
		}

		NodeList children = ((org.apache.xerces.dom.ElementNSImpl) o)
				.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);

			if (child.getNodeName().contains("path")) {
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource path = model.createResource(uuid2);
				path.addProperty(RDF.type, Omn_resource.Path);

				NamedNodeMap pathAttributes = child.getAttributes();
				for (int j = 0; j < pathAttributes.getLength(); j++) {
					if (pathAttributes.item(j).getNodeName().equals("id")) {
						String id = pathAttributes.item(j).getNodeValue();
						path.addProperty(Omn_lifecycle.hasID, id);
					}
				}
				extractHops(path, child);
				stitchResource.addProperty(Omn.hasResource, path);
			}
		}

		topology.addProperty(Omn.hasResource, stitchResource);
	}

	private static void extractHops(Resource path, Node pathNode) {
		NodeList children = pathNode.getChildNodes();

		for (int k = 0; k < children.getLength(); k++) {

			Node child = children.item(k);
			NodeList grandchildren = child.getChildNodes();

			if (child.getNodeName().contains("hop")) {
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource hop = path.getModel().createResource(uuid);
				hop.addProperty(RDF.type, Omn_resource.Hop);
				path.addProperty(Omn.hasResource, hop);

				NamedNodeMap hopAttributes = child.getAttributes();
				for (int j = 0; j < hopAttributes.getLength(); j++) {
					if (hopAttributes.item(j).getNodeName().equals("id")) {
						String id = hopAttributes.item(j).getNodeValue();
						hop.addProperty(Omn_lifecycle.hasID, id);
					}

					if (hopAttributes.item(j).getNodeName().equals("type")) {
						String type = hopAttributes.item(j).getNodeValue();
						hop.addProperty(Omn_domain_pc.hasHopType, type);
					}
				}

				for (int i = 0; i < grandchildren.getLength(); i++) {
					Node grandchild = grandchildren.item(i);

					if (grandchild.getNodeName().contains("nextHop")) {
						String nextHop = grandchild.getTextContent();
						if (nextHop != null) {
							hop.addProperty(Omn_domain_pc.hasNextHop, nextHop);
						}
					}

					if (grandchild.getNodeName().contains("link")) {
						RequestExtractExt.LOG
								.log(Level.INFO,
										"Stitching link translation capability has yet to be done.");
					}
				}

			}
		}

	}

	public static void extractOpenflow(Resource topology, Object o) {

		Model model = topology.getModel();
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource openflowResource = model.createResource(uuid);
		openflowResource.addProperty(RDF.type, Omn_resource.Openflow);

		ElementNSImpl openflow = ((org.apache.xerces.dom.ElementNSImpl) o);
		NamedNodeMap attributes = openflow.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.item(i).getNodeName().equals("description")) {
				String description = attributes.item(i).getNodeValue();
				openflowResource.addProperty(RDFS.comment, description);
			}
			if (attributes.item(i).getNodeName().equals("email")) {
				String email = attributes.item(i).getNodeValue();
				Resource mailtoUri = model.createResource("mailto:" + email);
				openflowResource.addProperty(RDFS.seeAlso, mailtoUri);
			}
		}

		NodeList children = ((org.apache.xerces.dom.ElementNSImpl) o)
				.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			RequestExtractExt.LOG.log(Level.INFO, "Found unknown extension: "
					+ child.getNodeName());

			if (child.getNodeName().contains("controller")) {
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource controller = model.createResource(uuid2);
				controller.addProperty(RDF.type, Omn_domain_pc.Controller);

				NamedNodeMap controllerAttributes = child.getAttributes();
				for (int k = 0; k < controllerAttributes.getLength(); k++) {
					if (controllerAttributes.item(k).getNodeName()
							.equals("type")) {
						String type = controllerAttributes.item(k)
								.getNodeValue();
						controller.addProperty(Omn_domain_pc.hasControllerType,
								type);
					}
					if (controllerAttributes.item(k).getNodeName()
							.equals("url")) {
						String url = controllerAttributes.item(k)
								.getNodeValue();
						Resource urlResource = model.createResource(url);
						controller.addProperty(Omn_domain_pc.hasControllerUrl,
								urlResource);
					}
				}
				openflowResource.addProperty(Omn.hasResource, controller);
			}

			if (child.getNodeName().contains("match")) {
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource packet = model.createResource(uuid2);
				packet.addProperty(RDF.type, Omn_domain_pc.Packet);

				NodeList grandchildren = child.getChildNodes();
				for (int j = 0; j < grandchildren.getLength(); j++) {
					Node grandchild = grandchildren.item(j);

					if (grandchild.getNodeName().contains("use-group")) {
						NamedNodeMap usegroupAttributes = grandchild
								.getAttributes();
						for (int k = 0; k < usegroupAttributes.getLength(); k++) {
							if (usegroupAttributes.item(k).getNodeName()
									.equals("name")) {
								String name = usegroupAttributes.item(k)
										.getNodeValue();
								packet.addProperty(RDFS.label, name);
							}
						}
					}

					if (grandchild.getNodeName().contains("packet")) {
						NodeList greatGrandchildren = grandchild
								.getChildNodes();
						for (int k = 0; k < greatGrandchildren.getLength(); k++) {
							Node greatGrandchild = greatGrandchildren.item(k);

							if (greatGrandchild.getNodeName().contains(
									"dl_vlan")) {
								NamedNodeMap dvlanAttributes = greatGrandchild
										.getAttributes();
								for (int l = 0; l < dvlanAttributes.getLength(); l++) {
									if (dvlanAttributes.item(l).getNodeName()
											.equals("value")) {
										String value = dvlanAttributes.item(l)
												.getNodeValue();
										packet.addProperty(
												Omn_domain_pc.hasDlVlan, value);
									}
								}
							}
							if (greatGrandchild.getNodeName().contains(
									"dl_type")) {
								NamedNodeMap dvlanAttributes = greatGrandchild
										.getAttributes();
								for (int l = 0; l < dvlanAttributes.getLength(); l++) {
									if (dvlanAttributes.item(l).getNodeName()
											.equals("value")) {
										String value = dvlanAttributes.item(l)
												.getNodeValue();
										packet.addProperty(
												Omn_domain_pc.hasDlType, value);
									}
								}
							}

							if (greatGrandchild.getNodeName()
									.contains("nw_dst")) {
								NamedNodeMap dvlanAttributes = greatGrandchild
										.getAttributes();
								for (int l = 0; l < dvlanAttributes.getLength(); l++) {
									if (dvlanAttributes.item(l).getNodeName()
											.equals("value")) {
										String value = dvlanAttributes.item(l)
												.getNodeValue();
										packet.addProperty(
												Omn_domain_pc.hasNwDst, value);
									}
								}
							}

							if (greatGrandchild.getNodeName()
									.contains("nw_src")) {
								NamedNodeMap dvlanAttributes = greatGrandchild
										.getAttributes();
								for (int l = 0; l < dvlanAttributes.getLength(); l++) {
									if (dvlanAttributes.item(l).getNodeName()
											.equals("value")) {
										String value = dvlanAttributes.item(l)
												.getNodeValue();
										packet.addProperty(
												Omn_domain_pc.hasNwSrc, value);
									}
								}
							}
						}
					}
				}

				openflowResource.addProperty(Omn.hasResource, packet);
			}

			if (child.getNodeName().contains("group")) {
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource datapath = model.createResource(uuid2);
				datapath.addProperty(RDF.type, Omn_domain_pc.Datapath);

				// get group name
				NamedNodeMap groupAttributes = child.getAttributes();
				for (int l = 0; l < groupAttributes.getLength(); l++) {
					if (groupAttributes.item(l).getNodeName().equals("name")) {
						String groupName = groupAttributes.item(l)
								.getNodeValue();
						datapath.addProperty(RDFS.label, groupName);
					}
				}

				NodeList grandchildren1 = child.getChildNodes();
				for (int j = 0; j < grandchildren1.getLength(); j++) {
					Node grandchild = grandchildren1.item(j);

					if (grandchild.getNodeName().contains("datapath")) {
						NamedNodeMap datapathAttributes = grandchild
								.getAttributes();
						for (int k = 0; k < datapathAttributes.getLength(); k++) {
							if (datapathAttributes.item(k).getNodeName()
									.equals("component_id")) {
								String componentId = datapathAttributes.item(k)
										.getNodeValue();
								URI uri = URI.create(componentId);
								datapath.addLiteral(
										Omn_lifecycle.hasComponentID, uri);
							}
							if (datapathAttributes.item(k).getNodeName()
									.equals("component_manager_id")) {
								String componentManagerId = datapathAttributes
										.item(k).getNodeValue();
								datapath.addProperty(Omn_lifecycle.managedBy,
										componentManagerId);
							}
							if (datapathAttributes.item(k).getNodeName()
									.equals("dpid")) {
								String dpid = datapathAttributes.item(k)
										.getNodeValue();
								datapath.addProperty(Omn_lifecycle.hasID, dpid);
							}
						}
						openflowResource.addProperty(Omn.hasResource, datapath);
					}
				}
			}
		}
		topology.addProperty(Omn.hasResource, openflowResource);
	}

	static void tryExtractSharedVlan(Object rspecObject, Resource offering)
			throws MissingRspecElementException {

		final LinkSharedVlan vlan = (LinkSharedVlan) rspecObject;
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource sharedVlan = offering.getModel().createResource(uuid);
		sharedVlan.addProperty(RDF.type, Omn_domain_pc.SharedVlan);

		String name = vlan.getName();
		if (name == null) {
			throw new MissingRspecElementException("link_shared_vlan > name");
		}
		sharedVlan.addProperty(RDFS.label, name);

		String vlanTag = vlan.getVlantag();
		if (vlanTag != null) {
			sharedVlan.addProperty(Omn_domain_pc.vlanTag, vlanTag);
		}

		offering.addProperty(Omn.hasResource, sharedVlan);
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
			RequestExtractExt.LOG.finer(e.getMessage());
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

		BigInteger rateUp = pgwContents.getRateUp();
		BigInteger minusOne = BigInteger.valueOf(-1);
		if (rateUp != null && rateUp.compareTo(minusOne) != 0) {
			pgwResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Epc.rateCodeUp, rateUp);
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

	static void tryExtractUe(Resource node, Object o2) {
		try {
			Ue ue = (Ue) o2;
			// String resourceUri = node.getURI().toString() + "-details";
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
					extracUeDiskImage(diskImageContents, omnUe);
				} else if (o.getClass().equals(UeHardwareTypeContents.class)) {
					UeHardwareTypeContents hardwareTypeContents = (UeHardwareTypeContents) o;
					extracUeHardwareType(hardwareTypeContents, omnUe);
				}

			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	private static void extracUeHardwareType(
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

	private static void extracUeDiskImage(
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

	static void tryExtractAccessNetwork(Resource node, Object o2) {
		try {
			AccessNetwork accessNetwork = (AccessNetwork) o2;
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
					extractEpcIpContents(ipContents, omnAccessNetwork);
				}
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	private static void extractEpcIpContents(EpcIpContents ipContents,
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

	static void extractJfedLocation(Resource omnResource, Object o) {

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource locationResource = omnResource.getModel().createResource(uuid);
		locationResource.addProperty(RDF.type, Omn_resource.Location);

		Location location = (Location) o;

		String x = location.getX();
		if (x != null && x != "") {
			locationResource.addProperty(Omn_resource.jfedX, x);
		}

		String y = location.getY();
		if (y != null && y != "") {
			locationResource.addProperty(Omn_resource.jfedY, y);
		}

		omnResource.addProperty(Omn_resource.hasLocation, locationResource);
	}

	static void tryExtractOsco(Resource omnOsco, Object rspecNodeObject) {

		Osco osco = (Osco) rspecNodeObject;

		// Resource omnOsco;
		// String aboutUri = osco.getAbout();
		// if (aboutUri != null
		// && (AbstractConverter.isUrl(aboutUri) || AbstractConverter
		// .isUrn(aboutUri))) {
		// omnOsco = topology.getModel().createResource(aboutUri);
		// } else {
		// omnOsco = topology.getModel().createResource(aboutUri);
		// }
		// topology.addProperty(Omn.hasResource, omnOsco);
		// omnOsco.addProperty(Omn.isResourceOf, topology);

		String type = osco.getType();
		if (type != null && type != "") {
			if (AbstractConverter.isUrl(type) || AbstractConverter.isUrn(type)) {
				Resource typeResource = omnOsco.getModel().createResource(type);
				omnOsco.addProperty(RDF.type, typeResource);
			}
		}

		String id = osco.getId();
		if (id != null && id != "") {
			omnOsco.addProperty(info.openmultinet.ontology.vocabulary.Osco.id,
					id);
		}

		String implementedBy = osco.getImplementedBy();
		if (implementedBy != null && implementedBy != "") {
			Resource implementedByResource = omnOsco.getModel().createResource(
					implementedBy);
			omnOsco.addProperty(Omn_lifecycle.implementedBy,
					implementedByResource);
		}

		Boolean anncAuto = osco.isAnncAuto();
		if (anncAuto != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.ANNC_AUTO,
					String.valueOf(anncAuto));
		}

		Boolean anncDisabled = osco.isAnncDisabled();
		if (anncDisabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.ANNC_DISABLED,
					String.valueOf(anncDisabled));
		}

		String appId = osco.getAppId();
		if (appId != null && appId != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.APP_ID, appId);
		}

		BigInteger appPort = osco.getAppPort();
		if (appPort != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.APP_PORT,
					appPort);
		}

		String bitBucket = osco.getBitBucketDbIp();
		if (bitBucket != null && bitBucket != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.Bit_Bucket_DB_IP,
					bitBucket);
		}

		Boolean coapDisabled = osco.isCoapDisabled();
		if (coapDisabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.COAP_DISABLED,
					String.valueOf(coapDisabled));
		}

		String deployedOn = osco.getDeployedOn();
		if (deployedOn != null && deployedOn != "") {

			if (!AbstractConverter.isUrl(deployedOn)
					&& !AbstractConverter.isUrn(deployedOn)) {
				deployedOn = AbstractConverter.NAMESPACE
						+ CommonMethods.urlToGeniUrn(deployedOn);
			}

			Resource deployedOnResource = omnOsco.getModel().createResource(
					deployedOn);
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.deployedOn,
					deployedOnResource);
		}

		String extIp = osco.getExtIp();
		if (extIp != null && extIp != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.EXT_IP, extIp);
		}

		String fileServer = osco.getFileServer();
		if (fileServer != null && fileServer != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.FILE_SERVER,
					fileServer);
		}

		String flavour = osco.getFlavour();
		if (flavour != null && flavour != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.flavour, flavour);
		}

		String localPort = osco.getLocalPort();
		if (localPort != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.local_port,
					localPort);
		}

		String loggingFile = osco.getLoggingFile();
		if (loggingFile != null && loggingFile != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.LOGGING_FILE,
					loggingFile);
		}

		String loggingLevel = osco.getLoggingLevel();
		if (loggingLevel != null && loggingLevel != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.LOGGING_LEVEL,
					loggingLevel);
		}

		String m2mConnAppIp = osco.getM2MConnAppIp();
		if (m2mConnAppIp != null && m2mConnAppIp != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_ip,
					m2mConnAppIp);
		}

		BigInteger m2mConnAppPort = osco.getM2MConnAppPort();
		if (m2mConnAppPort != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.m2m_conn_app_port,
					m2mConnAppPort);
		}

		BigInteger maxNumInst = osco.getMaxNumInst();
		if (maxNumInst != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.maxNumInst,
					maxNumInst);
		}

		String mgmtIntf = osco.getMgmtIntf();
		if (mgmtIntf != null && mgmtIntf != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.MGMT_INTF,
					mgmtIntf);
		}

		BigInteger minNumInst = osco.getMinNumInst();
		if (minNumInst != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.minNumInst,
					minNumInst);
		}

		Boolean notifyChanDisabled = osco.isNotifyChanDisabled();
		if (notifyChanDisabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.NOTIFY_CHAN_DISABLED,
					String.valueOf(notifyChanDisabled));
		}

		Boolean notifyDisabled = osco.isNotifyDisabled();
		if (notifyDisabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.NOTIFY_DISABLED,
					String.valueOf(notifyDisabled));
		}

		String omtcUrl = osco.getOmtcUrl();
		if (omtcUrl != null && omtcUrl != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.OMTC_URL,
					omtcUrl);
		}

		Boolean requireAuth = osco.isRequireAuth();
		if (requireAuth != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.REQUIRE_AUTH,
					String.valueOf(requireAuth));
		}

		String requires = osco.getRequires();
		if (requires != null && requires != "") {
			Resource requiresResource = omnOsco.getModel().createResource(
					requires);
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.requires,
					requiresResource);
		}

		Boolean retargetDisabled = osco.isRetargetDisabled();
		if (retargetDisabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.RETARGET_DISABLED,
					String.valueOf(retargetDisabled));
		}

		BigInteger servicePort = osco.getServicePort();
		if (servicePort != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.SERVICE_PORT,
					servicePort);
		}

		Boolean sslEnabled = osco.isSslEnabled();
		if (sslEnabled != null) {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.SSL_ENABLED,
					String.valueOf(sslEnabled));
		}

		BigInteger sslPort = osco.getSslPort();
		if (sslPort != null) {
			omnOsco.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.SSL_PORT,
					sslPort);
		}

		String testParam = osco.getTestParam();
		if (testParam != null && testParam != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.TEST_PARAM,
					testParam);
		}

		String requirement = osco.getRequirement();
		if (requirement != null && requirement != "") {
			omnOsco.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.requirement,
					requirement);
		}

		List<Object> objects = osco.getImageOrOscoLocationOrSubnet();

		for (Object o : objects) {
			if (o.getClass().equals(ImageContents.class)) {
				ImageContents imageContents = (ImageContents) o;
				extractOscoImage(imageContents, omnOsco);
			} else if (o.getClass().equals(OscoLocationContents.class)) {
				OscoLocationContents locationContents = (OscoLocationContents) o;
				extractOscoLocation(locationContents, omnOsco);
			} else if (o.getClass().equals(SubnetContents.class)) {
				SubnetContents subnetContents = (SubnetContents) o;
				extractOscoSubnet(subnetContents, omnOsco);
			}
		}
	}

	private static void extractOscoLocation(
			OscoLocationContents locationContents, Resource omnOsco) {

		String aboutUri = locationContents.getAbout();
		Resource locationResource = null;

		if (AbstractConverter.isUrl(aboutUri)
				|| AbstractConverter.isUrn(aboutUri)) {
			locationResource = omnOsco.getModel().createResource(aboutUri);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			locationResource = omnOsco.getModel().createResource(uuid);
		}

		locationResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Osco.Location);

		String name = locationContents.getName();
		if (name != null && name != "") {
			locationResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.name, name);
		}

		omnOsco.addProperty(
				info.openmultinet.ontology.vocabulary.Osco.location,
				locationResource);

	}

	private static void extractOscoSubnet(SubnetContents subnetContents,
			Resource omnNode) {

		String aboutUri = subnetContents.getAbout();
		Resource subnetResource = null;

		if (aboutUri != null
				&& (AbstractConverter.isUrl(aboutUri) || AbstractConverter
						.isUrn(aboutUri))) {
			subnetResource = omnNode.getModel().createResource(aboutUri);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			subnetResource = omnNode.getModel().createResource(uuid);
		}

		subnetResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Osco.Subnet);

		String datacenter = subnetContents.getDatacenter();
		if (datacenter != null && datacenter != "") {
			subnetResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.datacenter,
					datacenter);
		}

		// String mgmt = subnetContents.isMgmt().toString();
		// Boolean mgmtBoolean = Boolean.valueOf(mgmt);
		Boolean mgmtBoolean = subnetContents.isMgmt();
		if (mgmtBoolean != null) {
			// if (mgmt != null && mgmt != "") {
			subnetResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.mgmt,
					mgmtBoolean);
		}

		String name = subnetContents.getName();
		if (name != null && name != "") {
			subnetResource.addProperty(RDFS.label, name);
		}

		omnNode.addProperty(info.openmultinet.ontology.vocabulary.Osco.subnet,
				subnetResource);

	}

	private static void extractOscoImage(ImageContents image, Resource omnNode) {

		String aboutUri = image.getAbout();
		Resource imageResource = null;

		if (aboutUri != null
				&& (AbstractConverter.isUrl(aboutUri) || AbstractConverter
						.isUrn(aboutUri))) {
			imageResource = omnNode.getModel().createResource(aboutUri);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			imageResource = omnNode.getModel().createResource(uuid);
		}

		imageResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Osco.Image);

		String datacenter = image.getDatacenter();
		if (datacenter != null && datacenter != "") {
			imageResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.datacenter,
					datacenter);
		}

		String id = image.getId();
		if (id != null && id != "") {
			imageResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.id, id);
		}

		omnNode.addProperty(info.openmultinet.ontology.vocabulary.Osco.image,
				imageResource);

	}

	static void tryExtractMonitoring(Resource omnResource, Object o) {
		// if (o.getClass().equals(Monitoring.class)) {

		Monitoring monitor = (Monitoring) o;
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource monitoringResource = omnResource.getModel().createResource(
				uuid);
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
		omnResource.addProperty(Omn_lifecycle.usesService, monitoringResource);
		// }
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
			RequestExtractExt.LOG.finer(e.getMessage());
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
			RequestExtractExt.LOG.finer(e.getMessage());
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
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractFivegGateway(Resource node, Object rspecObject) {
		try {
			Gateway gateway = (Gateway) rspecObject;

			node.addProperty(RDF.type, Fiveg.Gateway);

			String version = gateway.getVersion();
			if (version != null && version != "") {
				node.addProperty(Fiveg.version, version);
			}

			// String deployedOn = gateway.getDeployedOn();
			// if (deployedOn != null && deployedOn != "") {
			// Resource deployedOnResource = node.getModel().createResource(
			// deployedOn);
			// node.addProperty(
			// info.openmultinet.ontology.vocabulary.Osco.deployedOn,
			// deployedOnResource);
			// }

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
			if (cloudIpAddress != null) {
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
			}

			String requires = gateway.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}
	}

	public static void tryExtractFivegDns(Resource node, Object rspecObject) {
		try {
			Dns dns = (Dns) rspecObject;
			node.addProperty(RDF.type, Fiveg.DomainNameSystem);

			String additionals = dns.getAdditionals();
			if (additionals != null && additionals != "") {
				node.addProperty(Fiveg.additionals, additionals);
			}

			String domainName = dns.getDomainName();
			if (domainName != null && domainName != "") {
				node.addProperty(Fiveg.domainName, domainName);
			}

			String domainNs = dns.getDomainNs();
			if (domainNs != null && domainNs != "") {
				node.addProperty(Fiveg.domainNs, domainNs);

			}

			BigInteger mgmtIntf = dns.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger minNumIntf = dns.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger netAIntf = dns.getNetAIntf();
			if (netAIntf != null) {
				node.addLiteral(Fiveg.ipServicesNetwork, netAIntf);
			}

			String requires = dns.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}
	}

	public static void tryExtractFivegSwitch(Resource node, Object rspecObject) {
		try {
			Switch switchObject = (Switch) rspecObject;
			node.addProperty(RDF.type, Fiveg.Switch);

			String pgwUBaseId = switchObject.getPgwUBaseId();
			if (pgwUBaseId != null && pgwUBaseId != "") {
				node.addProperty(Fiveg.pgwUBaseId, pgwUBaseId);
			}
			Boolean upstartOn = switchObject.isUpstartOn();

			if (upstartOn != null) {
				node.addLiteral(Fiveg.upstartOn, upstartOn);
			}

			BigInteger mgmtIntf = switchObject.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger netAIntf = switchObject.getNetAIntf();
			if (netAIntf != null) {
				node.addLiteral(Fiveg.ipServicesNetwork, netAIntf);
			}

			BigInteger pgwUDownloadInterface = switchObject
					.getPgwUDownloadInterface();
			if (pgwUDownloadInterface != null) {
				node.addLiteral(Fiveg.pgwUDownloadInterface,
						pgwUDownloadInterface);
			}

			BigInteger pgwUUploadInterface = switchObject
					.getPgwUUploadInterface();
			if (pgwUUploadInterface != null) {
				node.addLiteral(Fiveg.pgwUUploadInterface, pgwUUploadInterface);
			}

			BigInteger minNumIntf = switchObject.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger sgwUDownloadInterface = switchObject
					.getSgwUDownloadInterface();
			if (sgwUDownloadInterface != null) {
				node.addLiteral(Fiveg.sgwUDownloadInterface,
						sgwUDownloadInterface);
			}

			BigInteger sgwUUploadInterface = switchObject
					.getSgwUUploadInterface();
			if (sgwUUploadInterface != null) {
				node.addLiteral(Fiveg.sgwUUploadInterface, sgwUUploadInterface);
			}

			BigInteger netDIntf = switchObject.getNetDIntf();
			if (netDIntf != null) {
				node.addLiteral(Fiveg.ranBackhaul, netDIntf);
			}

			String requires = switchObject.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
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
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void extractOlLease(Resource topology, Object rspecObject) {
		try {
			Lease lease = (Lease) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnLease = topology.getModel().createResource(uuid);
			omnLease.addProperty(RDF.type, Omn_lifecycle.Lease);
			topology.addProperty(Omn_lifecycle.hasLease, omnLease);

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
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractFivegBt(Resource node, Object rspecObject) {
		try {
			Bt btObject = (Bt) rspecObject;
			node.addProperty(RDF.type, Fiveg.BenchmarkingTool);

			Boolean upstartOn = btObject.isUpstartOn();
			if (upstartOn != null) {
				node.addLiteral(Fiveg.upstartOn, upstartOn);
			}

			BigInteger consolePort = btObject.getConsolePort();
			if (consolePort != null) {
				node.addLiteral(Fiveg.consolePort, consolePort);
			}

			String btHostName = btObject.getBtHostName();
			if (btHostName != null && !btHostName.equals("")) {
				node.addLiteral(Fiveg.benchmarkingToolHostName, btHostName);
			}

			BigInteger mgmtIntf = btObject.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger netCIntf = btObject.getNetCIntf();
			if (netCIntf != null) {
				node.addLiteral(Fiveg.subscriberIpRange, netCIntf);
			}

			BigInteger minNumIntf = btObject.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger netDIntf = btObject.getNetDIntf();
			if (netDIntf != null) {
				node.addLiteral(Fiveg.ranBackhaul, netDIntf);
			}

			BigInteger dnsIntf = btObject.getDnsIntf();
			if (dnsIntf != null) {
				node.addLiteral(Fiveg.dnsInterface, dnsIntf);
			}

			String netIp = btObject.getNetIp();
			if (netIp != null && !netIp.equals("")) {
				node.addLiteral(Fiveg.netIp, netIp);
			}

			String netMask = btObject.getNetMask();
			if (netMask != null && !netMask.equals("")) {
				node.addLiteral(Fiveg.netMask, netMask);
			}

			String ipRangeStart = btObject.getIpRangeStart();
			if (ipRangeStart != null && !ipRangeStart.equals("")) {
				node.addLiteral(Fiveg.ipRangeStart, ipRangeStart);
			}

			String ipRangeEnd = btObject.getIpRangeEnd();
			if (ipRangeEnd != null && !ipRangeEnd.equals("")) {
				node.addLiteral(Fiveg.ipRangeEnd, ipRangeEnd);
			}

			String imsiRangeStart = btObject.getImsiRangeStart();
			if (imsiRangeStart != null && !imsiRangeStart.equals("")) {
				node.addLiteral(Fiveg.imsiRangeStart, imsiRangeStart);
			}

			String imsiRangeEnd = btObject.getImsiRangeEnd();
			if (imsiRangeEnd != null && !imsiRangeEnd.equals("")) {
				node.addLiteral(Fiveg.imsiRangeEnd, imsiRangeEnd);
			}

			String ueAddr = btObject.getUeAddr();
			if (ueAddr != null && !ueAddr.equals("")) {
				node.addLiteral(Fiveg.userEquipmentAddress, ueAddr);
			}

			String cloudPublicRouterIp = btObject.getCloudPublicRouterIp();
			if (cloudPublicRouterIp != null && !cloudPublicRouterIp.equals("")) {
				node.addLiteral(Fiveg.cloudPublicRouterIp, cloudPublicRouterIp);
			}

			Boolean useFLoatingIps = btObject.isUseFloatingIps();
			if (useFLoatingIps != null) {
				node.addLiteral(Fiveg.useFloatingIps, useFLoatingIps);
			}

			String requires = btObject.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractFivegControl(Resource node, Object rspecObject) {
		try {
			Control control = (Control) rspecObject;
			node.addProperty(RDF.type, Fiveg.Control);

			BigInteger mmeConsolePort = control.getMmeConsolePort();
			if (mmeConsolePort != null) {
				node.addLiteral(Fiveg.mmeConsolePort, mmeConsolePort);
			}

			Boolean init = control.isInit();
			if (init != null) {
				node.addLiteral(Fiveg.init, init);
			}

			String pgwCOfpCtrTransport = control.getPgwCOfpCtrTransport();
			if (pgwCOfpCtrTransport != null) {
				node.addLiteral(Fiveg.pgwCOfpCtrTransport, pgwCOfpCtrTransport);
			}

			BigInteger sgwCOfpCtrPort = control.getSgwCOfpCtrPort();
			if (sgwCOfpCtrPort != null) {
				node.addLiteral(Fiveg.sgwCOfpCtrPort, sgwCOfpCtrPort);
			}

			BigInteger sgwCJsonSrvPort = control.getSgwCJsonSrvPort();
			if (sgwCJsonSrvPort != null) {
				node.addLiteral(Fiveg.sgwCJsonSrvPort, sgwCJsonSrvPort);
			}

			BigInteger pgwCConsolePort = control.getPgwCConsolePort();
			if (pgwCConsolePort != null) {
				node.addLiteral(Fiveg.pgwCConsolePort, pgwCConsolePort);
			}

			BigInteger pgwCOfpCtrPort = control.getPgwCOfpCtrPort();
			if (pgwCOfpCtrPort != null) {
				node.addLiteral(Fiveg.pgwCOfpCtrPort, pgwCOfpCtrPort);
			}

			String mmeHostName = control.getMmeHostName();
			if (mmeHostName != null) {
				node.addLiteral(Fiveg.mmeHostName, mmeHostName);
			}

			String pgwCTemplateConfigFile = control.getPgwCTemplateConfigFile();
			if (pgwCTemplateConfigFile != null) {
				node.addLiteral(Fiveg.pgwCTemplateConfigFile,
						pgwCTemplateConfigFile);
			}

			Boolean upstartOn = control.isUpstartOn();
			if (upstartOn != null) {
				node.addLiteral(Fiveg.upstartOn, upstartOn);
			}

			BigInteger mgmtIntf = control.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger dnsIntf = control.getDnsIntf();
			if (dnsIntf != null) {
				node.addLiteral(Fiveg.dnsInterface, dnsIntf);
			}

			BigInteger minNumIntf = control.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger netDIntf = control.getNetDIntf();
			if (netDIntf != null) {
				node.addLiteral(Fiveg.ranBackhaul, netDIntf);
			}

			String requires = control.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractFivegEnodeb(Resource node, Object rspecObject) {
		try {
			Enodeb enodebObject = (Enodeb) rspecObject;
			node.addProperty(RDF.type, Fiveg.ENodeB);

			Boolean upstartOn = enodebObject.isUpstartOn();
			if (upstartOn != null) {
				node.addLiteral(Fiveg.upstartOn, upstartOn);
			}

			BigInteger consolePort = enodebObject.getConsolePort();
			if (consolePort != null) {
				node.addLiteral(Fiveg.consolePort, consolePort);
			}

			String enodebHostName = enodebObject.getEnodebHostName();
			if (enodebHostName != null && !enodebHostName.equals("")) {
				node.addLiteral(Fiveg.enodebHostName, enodebHostName);
			}

			BigInteger mgmtIntf = enodebObject.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger netCIntf = enodebObject.getNetCIntf();
			if (netCIntf != null) {
				node.addLiteral(Fiveg.subscriberIpRange, netCIntf);
			}

			BigInteger minNumIntf = enodebObject.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			BigInteger netDIntf = enodebObject.getNetDIntf();
			if (netDIntf != null) {
				node.addLiteral(Fiveg.ranBackhaul, netDIntf);
			}

			BigInteger dnsIntf = enodebObject.getDnsIntf();
			if (dnsIntf != null) {
				node.addLiteral(Fiveg.dnsInterface, dnsIntf);
			}

			String netIp = enodebObject.getNetIp();
			if (netIp != null && !netIp.equals("")) {
				node.addLiteral(Fiveg.netIp, netIp);
			}

			String netMask = enodebObject.getNetMask();
			if (netMask != null && !netMask.equals("")) {
				node.addLiteral(Fiveg.netMask, netMask);
			}

			String ipRangeStart = enodebObject.getIpRangeStart();
			if (ipRangeStart != null && !ipRangeStart.equals("")) {
				node.addLiteral(Fiveg.ipRangeStart, ipRangeStart);
			}

			String ipRangeEnd = enodebObject.getIpRangeEnd();
			if (ipRangeEnd != null && !ipRangeEnd.equals("")) {
				node.addLiteral(Fiveg.ipRangeEnd, ipRangeEnd);
			}

			String imsiRangeStart = enodebObject.getImsiRangeStart();
			if (imsiRangeStart != null && !imsiRangeStart.equals("")) {
				node.addLiteral(Fiveg.imsiRangeStart, imsiRangeStart);
			}

			String imsiRangeEnd = enodebObject.getImsiRangeEnd();
			if (imsiRangeEnd != null && !imsiRangeEnd.equals("")) {
				node.addLiteral(Fiveg.imsiRangeEnd, imsiRangeEnd);
			}

			String ueAddr = enodebObject.getUeAddr();
			if (ueAddr != null && !ueAddr.equals("")) {
				node.addLiteral(Fiveg.userEquipmentAddress, ueAddr);
			}

			String cloudPublicRouterIp = enodebObject.getCloudPublicRouterIp();
			if (cloudPublicRouterIp != null && !cloudPublicRouterIp.equals("")) {
				node.addLiteral(Fiveg.cloudPublicRouterIp, cloudPublicRouterIp);
			}

			Boolean useFLoatingIps = enodebObject.isUseFloatingIps();
			if (useFLoatingIps != null) {
				node.addLiteral(Fiveg.useFloatingIps, useFLoatingIps);
			}

			String requires = enodebObject.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractFivegHss(Resource node, Object rspecObject) {
		try {
			Hss hss = (Hss) rspecObject;
			node.addProperty(RDF.type, Fiveg.HomeSubscriberServer);

			BigInteger localDb = hss.getLocalDb();
			if (localDb != null) {
				node.addLiteral(Fiveg.localDatabase, localDb);
			}

			BigInteger dbProvi = hss.getDbProvi();
			if (dbProvi != null) {
				node.addLiteral(Fiveg.databaseProvi, dbProvi);
			}

			String dbUser = hss.getDbUser();
			if (dbUser != null) {
				node.addLiteral(Fiveg.databaseUser, dbUser);
			}

			String dbName = hss.getDbName();
			if (dbName != null) {
				node.addLiteral(Fiveg.databaseName, dbName);
			}

			String dbPw = hss.getDbPw();
			if (dbPw != null) {
				node.addLiteral(Fiveg.databasePassword, dbPw);
			}

			String domainName = hss.getDomainName();
			if (domainName != null) {
				node.addLiteral(Fiveg.domainName, domainName);
			}

			BigInteger port = hss.getPort();
			if (port != null) {
				node.addLiteral(Fiveg.port, port);
			}

			Boolean slfPresence = hss.isSlfPresence();
			if (slfPresence != null) {
				node.addLiteral(Fiveg.slfPresence, slfPresence);
			}

			BigInteger consolePortOne = hss.getConsolePortOne();
			if (consolePortOne != null) {
				node.addLiteral(Fiveg.consolePortOne, consolePortOne);
			}

			BigInteger consolePortTwo = hss.getConsolePortTwo();
			if (consolePortTwo != null) {
				node.addLiteral(Fiveg.consolePortTwo, consolePortTwo);
			}

			String consolePortBindOne = hss.getConsolePortBindOne();
			if (consolePortBindOne != null) {
				node.addLiteral(Fiveg.consolePortBindOne, consolePortBindOne);
			}

			String consolePortBindTwo = hss.getConsolePortBindTwo();
			if (consolePortBindTwo != null) {
				node.addLiteral(Fiveg.consolePortBindTwo, consolePortBindTwo);
			}

			BigInteger diameterListenIntf = hss.getDiameterListenIntf();
			if (diameterListenIntf != null) {
				node.addLiteral(Fiveg.diameterListenIntf, diameterListenIntf);
			}

			String defaultRouteVia = hss.getDefaultRouteVia();
			if (defaultRouteVia != null) {
				node.addLiteral(Fiveg.defaultRouteVia, defaultRouteVia);
			}

			String version = hss.getVersion();
			if (version != null) {
				node.addLiteral(Fiveg.version, version);
			}

			BigInteger mgmtIntf = hss.getMgmtIntf();
			if (mgmtIntf != null) {
				node.addLiteral(Fiveg.managementInterface, mgmtIntf);
			}

			BigInteger dnsIntf = hss.getDnsIntf();
			if (dnsIntf != null) {
				node.addLiteral(Fiveg.dnsInterface, dnsIntf);
			}

			BigInteger minNumIntf = hss.getMinNumIntf();
			if (minNumIntf != null) {
				node.addLiteral(Fiveg.minInterfaces, minNumIntf);
			}

			String requires = hss.getRequires();
			if (requires != null && !requires.equals("")) {
				node.addLiteral(Fiveg.requires, requires);
			}

		} catch (final ClassCastException e) {
			RequestExtractExt.LOG.finer(e.getMessage());
		}

	}
}
