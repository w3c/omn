package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ActionSpec;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ApnContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Available;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AvailableContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Channel;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ControlAddressContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Device;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ENodeBContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.EpcIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.FiveGIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Gateway;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Lease;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.OscoLocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.PDNGatewayContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ParameterContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecOpstate;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.StateSpec;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.SubnetContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.UeHardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.WaitSpec;
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

import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods to extract information from an advertisement RSpec to create
 * an OMN model. These methods are for non-native RSpec extensions.
 * 
 * @author robynloughnane
 *
 */
public class AdExtractExt extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(AdExtractExt.class
			.getName());

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
				} else if (o.getClass().equals(SubscriberContents.class)) {
					SubscriberContents subscriberContents = (SubscriberContents) o;
					String imsiNumber = subscriberContents.getImsiNumber();
					omnEpc.addProperty(
							info.openmultinet.ontology.vocabulary.Epc.subscriber,
							imsiNumber);
				} else if (o.getClass().equals(PDNGatewayContents.class)) {
					PDNGatewayContents pgwContents = (PDNGatewayContents) o;
					extractPDNGateway(pgwContents, omnEpc);
				}
			}
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
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

	public static void extractOpenflow(Object rspecObject, Resource topology) {

		ElementNSImpl openflow = (ElementNSImpl) rspecObject;

		if (openflow.getNamespaceURI().equals(
				"http://www.geni.net/resources/rspec/ext/openflow/3")
				|| openflow.getNamespaceURI().equals(
						"http://www.geni.net/resources/rspec/ext/openflow/4")) {

			if (openflow.getLocalName().toLowerCase().equals("datapath")) {
				Model model = topology.getModel();

				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource datapath = model.createResource(uuid2);
				datapath.addProperty(RDF.type, Omn_domain_pc.Datapath);

				extractOpenflowDatapath(openflow, datapath);

				topology.addProperty(Omn.hasResource, datapath);

			} else if (openflow.getLocalName().toLowerCase().equals("sliver")) {
				extractOpenflowSliver(openflow, topology);
			}
		}
	}

	private static void extractOpenflowSliver(ElementNSImpl openflow,
			Resource topology) {
		Model model = topology.getModel();
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource openflowResource = model.createResource(uuid);
		openflowResource.addProperty(RDF.type, Omn_resource.Openflow);

		// ElementNSImpl openflow = ((org.apache.xerces.dom.ElementNSImpl) o);
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

		NodeList children = openflow.getChildNodes();

		for (int i = 0; i < children.getLength(); i++) {
			Node child = children.item(i);
			AdExtractExt.LOG.log(Level.INFO, "Found unknown extension: "
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
						extractOpenflowDatapath(grandchild, datapath);
					}
				}
				openflowResource.addProperty(Omn.hasResource, datapath);
			}
		}
		topology.addProperty(Omn.hasResource, openflowResource);

	}

	private static void extractOpenflowDatapath(Node openflow, Resource datapath) {

		NamedNodeMap datapathAttributes = openflow.getAttributes();

		for (int k = 0; k < datapathAttributes.getLength(); k++) {
			if (datapathAttributes.item(k).getNodeName().equals("component_id")) {
				String componentId = datapathAttributes.item(k).getNodeValue();
				URI uri = URI.create(componentId);
				datapath.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}
			if (datapathAttributes.item(k).getNodeName()
					.equals("component_manager_id")) {
				String componentManagerId = datapathAttributes.item(k)
						.getNodeValue();
				datapath.addProperty(Omn_lifecycle.managedBy,
						componentManagerId);
			}
			if (datapathAttributes.item(k).getNodeName().equals("dpid")) {
				String dpid = datapathAttributes.item(k).getNodeValue();
				datapath.addProperty(Omn_lifecycle.hasID, dpid);
			}
		}

		NodeList children = openflow.getChildNodes();
		for (int i = 0; i < children.getLength(); i++) {

			Node child = children.item(i);

			if (child.getNodeName().contains("port")) {
				String uuid1 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource port = datapath.getModel().createResource(uuid1);
				port.addProperty(RDF.type, Omn_resource.Interface);

				NamedNodeMap portAttributes = child.getAttributes();

				for (int k1 = 0; k1 < portAttributes.getLength(); k1++) {
					if (portAttributes.item(k1).getNodeName().equals("name")) {
						String name = portAttributes.item(k1).getNodeValue();
						port.addProperty(RDFS.label, name);
					}
					if (portAttributes.item(k1).getNodeName().equals("num")) {
						String number = portAttributes.item(k1).getNodeValue();
						BigInteger intNumber = BigInteger.valueOf(Integer
								.parseInt(number));
						port.addLiteral(Omn_resource.port, intNumber);
					}
				}
				datapath.addProperty(Omn_resource.hasInterface, port);
			}

		}

	}

	static void tryExtractEmulabTrivialBandwidth(Object rspecNodeObject,
			Resource omnNode) {
		try {
			if (rspecNodeObject.toString().contains("emulab:trivial_bandwidth")) {
				ElementNSImpl trivialBandwidth = ((org.apache.xerces.dom.ElementNSImpl) rspecNodeObject);
				NamedNodeMap attributes = trivialBandwidth.getAttributes();
				for (int i = 0; i < attributes.getLength(); i++) {
					if (attributes.item(i).getNodeName().equals("value")) {
						String value = attributes.item(i).getNodeValue();
						omnNode.addProperty(
								Omn_domain_pc.hasEmulabTrivialBandwidth, value);
					}
				}
			}
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void extractStitching(Object rspecObject, Resource offering)
			throws MissingRspecElementException {

		Model model = offering.getModel();
		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		Resource stitchResource = model.createResource(uuid);
		stitchResource.addProperty(RDF.type, Omn_resource.Stitching);

		ElementNSImpl stitch = ((org.apache.xerces.dom.ElementNSImpl) rspecObject);
		NamedNodeMap attributes = stitch.getAttributes();
		for (int i = 0; i < attributes.getLength(); i++) {
			if (attributes.item(i).getNodeName().equals("lastUpdateTime")) {
				String lastUpdate = attributes.item(i).getNodeValue();
				stitchResource.addProperty(Omn_domain_pc.lastUpdateTime,
						lastUpdate);
			}
		}

		NodeList children = ((org.apache.xerces.dom.ElementNSImpl) rspecObject)
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
				// extractHops(path, child);
				stitchResource.addProperty(Omn.hasResource, path);
			}
		}

		offering.addProperty(Omn.hasResource, stitchResource);

	}

	static void tryExtractEmulabInterface(Object interfaceObject,
			Resource interfaceResource) {
		try {
			if (interfaceObject.toString().contains("emulab:interface")) {
				AdExtractExt.LOG
						.fine("emulab:interface extension not yet supported");
			}
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractRoutableAddresses(Object rspecObject,
			Resource offering) {
		try {

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractSharedVlan(Object rspecObject,
			Resource offering) {
		try {
			final RspecSharedVlan vlan = (RspecSharedVlan) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource sharedVlan = offering.getModel().createResource(uuid);
			sharedVlan.addProperty(RDF.type, Omn_domain_pc.SharedVlan);

			List<Available> availables = vlan.getAvailable();
			for (Available available : availables) {

				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource availableOmn = offering.getModel().createResource(
						uuid2);

				if (available.isRestricted() != null) {
					String restricted = available.isRestricted().toString();
					if (restricted != null) {
						availableOmn.addProperty(Omn_domain_pc.restricted,
								restricted);
					}
				}

				if (available.getName() != null) {
					availableOmn.addProperty(RDFS.label, available.getName());
				}
				sharedVlan
						.addProperty(Omn_domain_pc.hasAvailable, availableOmn);
			}

			offering.addProperty(Omn.hasResource, sharedVlan);
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractOpstate(Object rspecObject, Resource offering)
			throws MissingRspecElementException {
		try {
			final RspecOpstate nodeJaxb = (RspecOpstate) rspecObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource opstate = offering.getModel().createResource(uuid);
			opstate.addProperty(RDF.type, Omn_lifecycle.Opstate);

			// extract start
			// start is required
			String start = nodeJaxb.getStart();
			if (start == null) {
				throw new MissingRspecElementException("RspecOpstate > start");
			}
			opstate.addProperty(Omn_lifecycle.hasStartState,
					CommonMethods.convertGeniStateToOmn(start));

			// extract aggregate manager id
			// aggregate_manager_id is required
			String aggregateManagerId = nodeJaxb.getAggregateManagerId();
			if (aggregateManagerId == null) {
				throw new MissingRspecElementException(
						"RspecOpstate > aggregate_manager_id");
			}
			opstate.addProperty(Omn_lifecycle.managedBy, aggregateManagerId);

			List<Object> sliverStates = nodeJaxb.getSliverTypeOrState();
			for (Object object : sliverStates) {
				tryExtractOpstateSliverType(object, opstate);
				tryExtractOpstateState(object, opstate);
			}

			offering.addProperty(Omn.hasResource, opstate);
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractOpstateState(Object object, Resource opstate) {
		try {
			StateSpec stateSpecs = (StateSpec) object;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource state = opstate.getModel().createResource(uuid);

			String geniType = stateSpecs.getName();
			OntClass omnType = CommonMethods.convertGeniStateToOmn(geniType);
			if (omnType != null) {
				state.addProperty(RDF.type, omnType);
				opstate.addProperty(Omn_lifecycle.hasState, state);
			} else {
				return;
			}

			List<Object> actionWaitDescription = stateSpecs
					.getActionOrWaitOrDescription();
			for (Object awd : actionWaitDescription) {
				tryExtractAction(awd, state);
				tryExtractWait(awd, state);
				tryExtractDescription(awd, state);
			}

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractAction(Object awd, Resource state) {
		try {
			ActionSpec action = (ActionSpec) awd;

			String description = action.getDescription();
			String next = action.getNext();
			String name = action.getName();

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource actionResource = state.getModel().createResource(uuid);
			actionResource.addProperty(RDF.type, Omn_lifecycle.Action);

			if (next != null) {
				OntClass omnNext = CommonMethods.convertGeniStateToOmn(next);
				actionResource.addProperty(Omn_lifecycle.hasNext, omnNext);
			}
			if (description != null) {
				actionResource.addProperty(RDFS.comment, description);
			}

			if (name != null) {
				OntClass omnType = CommonMethods.convertGeniStateToOmn(name);
				actionResource.addProperty(Omn_lifecycle.hasStateName, omnType);
			}

			state.addProperty(Omn_lifecycle.hasAction, actionResource);

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractWait(Object awd, Resource state) {
		try {
			WaitSpec wait = (WaitSpec) awd;

			String description = wait.getDescription();
			String next = wait.getNext();
			String type = wait.getType();

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource waitResource = state.getModel().createResource(uuid);
			waitResource.addProperty(RDF.type, Omn_lifecycle.Wait);

			if (next != null) {
				OntClass omnNext = CommonMethods.convertGeniStateToOmn(next);
				waitResource.addProperty(Omn_lifecycle.hasNext, omnNext);
			}
			if (description != null) {
				waitResource.addProperty(RDFS.comment, description);
			}

			if (type != null) {
				OntClass omnType = CommonMethods.convertGeniStateToOmn(type);
				waitResource.addProperty(Omn_lifecycle.hasType, omnType);
			}

			state.addProperty(Omn_lifecycle.hasWait, waitResource);

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractDescription(Object awd, Resource state) {
		try {
			String description = (String) awd;
			state.addProperty(RDFS.comment, description);

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractOpstateSliverType(Object object,
			Resource opstate) throws MissingRspecElementException {

		try {
			info.openmultinet.ontology.translators.geni.jaxb.advertisement.SliverType sliver = (info.openmultinet.ontology.translators.geni.jaxb.advertisement.SliverType) object;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			final Resource omnSliver = opstate.getModel().createResource(uuid);

			// name is required
			String name = sliver.getName();
			if (name == null) {
				throw new MissingRspecElementException(
						"opstate > slivertype > name");
			}
			omnSliver.addProperty(RDFS.label, name);
			opstate.addProperty(Omn_lifecycle.canBeImplementedBy, omnSliver);

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	static void tryExtractAccessNetwork(Resource node, Object rspecNodeObject) {
		try {
			AccessNetwork accessNetwork = (AccessNetwork) rspecNodeObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			// String resourceUri = node.getURI().toString() + "-details";
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
			AdExtractExt.LOG.finer(e.getMessage());
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

	static void tryExtractUe(Resource node, Object rspecObject) {

		try {
			Ue ue = (Ue) rspecObject;
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
					extractDiskImage(diskImageContents, omnUe);
				} else if (o.getClass().equals(UeHardwareTypeContents.class)) {
					UeHardwareTypeContents hardwareTypeContents = (UeHardwareTypeContents) o;
					extractHardwareType(hardwareTypeContents, omnUe);
				}

			}

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	private static void extractDiskImage(UeDiskImageContents diskImageContents,
			Resource omnUe) {
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

	private static void extractHardwareType(
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

	static void tryExtractOsco(Object rspecNodeObject, Resource omnOsco) {
		try {
			info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco osco = (info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco) rspecNodeObject;

			// Resource omnOsco;
			// String aboutUri = osco.getAbout();
			// if (aboutUri != null
			// && (AbstractConverter.isUrl(aboutUri) || AbstractConverter
			// .isUrn(aboutUri))) {
			// omnOsco = omnNode.getModel().createResource(aboutUri);
			// } else {
			// omnOsco = omnNode.getModel().createResource(aboutUri);
			// }
			// omnNode.addProperty(Omn.hasResource, omnOsco);
			// omnOsco.addProperty(Omn.isResourceOf, omnNode);

			String type = osco.getType();
			if (type != null && type != "") {
				if (AbstractConverter.isUrl(type)
						|| AbstractConverter.isUrn(type)) {
					Resource typeResource = omnOsco.getModel().createResource(
							type);
					omnOsco.addProperty(RDF.type, typeResource);
				}
			}
			String id = osco.getId();
			if (id != null && id != "") {
				omnOsco.addProperty(Omn_lifecycle.hasID, id);
			}

			String implementedBy = osco.getImplementedBy();
			if (implementedBy != null && implementedBy != "") {
				Resource implementedByResource = omnOsco.getModel()
						.createResource(implementedBy);
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
						info.openmultinet.ontology.vocabulary.Osco.APP_ID,
						appId);
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
				Resource deployedOnResource = omnOsco.getModel()
						.createResource(deployedOn);
				omnOsco.addProperty(
						info.openmultinet.ontology.vocabulary.Osco.deployedOn,
						deployedOnResource);
			}

			String extIp = osco.getExtIp();
			if (extIp != null && extIp != "") {
				omnOsco.addProperty(
						info.openmultinet.ontology.vocabulary.Osco.EXT_IP,
						extIp);
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
						info.openmultinet.ontology.vocabulary.Osco.flavour,
						flavour);
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

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	private static void extractOscoSubnet(SubnetContents subnetContents,
			Resource omnNode) {
		String aboutUri = subnetContents.getAbout();
		Resource subnetResource = null;

		if (AbstractConverter.isUrl(aboutUri)
				|| AbstractConverter.isUrn(aboutUri)) {
			subnetResource = omnNode.getModel().createResource(aboutUri);
		} else {
			subnetResource = omnNode.getModel().createResource();
		}

		subnetResource.addProperty(RDF.type,
				info.openmultinet.ontology.vocabulary.Osco.Subnet);

		String datacenter = subnetContents.getDatacenter();
		if (datacenter != null && datacenter != "") {
			subnetResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.datacenter,
					datacenter);
		}

		String mgmt = subnetContents.isMgmt().toString();
		Boolean mgmtBoolean = Boolean.valueOf(mgmt);
		if (mgmt != null && mgmt != "") {
			subnetResource.addLiteral(
					info.openmultinet.ontology.vocabulary.Osco.mgmt,
					mgmtBoolean);
		}

		String name = subnetContents.getName();
		if (name != null && name != "") {
			subnetResource.addProperty(
					info.openmultinet.ontology.vocabulary.Osco.name, name);
		}

		omnNode.addProperty(info.openmultinet.ontology.vocabulary.Osco.subnet,
				subnetResource);

	}

	private static void extractOscoLocation(
			OscoLocationContents locationContents, Resource omnOsco) {

		String aboutUri = locationContents.getAbout();
		Resource locationResource = null;

		if (AbstractConverter.isUrl(aboutUri)
				|| AbstractConverter.isUrn(aboutUri)) {
			locationResource = omnOsco.getModel().createResource(aboutUri);
		} else {
			locationResource = omnOsco.getModel().createResource();
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

	private static void extractOscoImage(ImageContents image, Resource omnNode) {
		String aboutUri = image.getAbout();
		Resource imageResource = null;

		if (AbstractConverter.isUrl(aboutUri)
				|| AbstractConverter.isUrn(aboutUri)) {
			imageResource = omnNode.getModel().createResource(aboutUri);
		} else {
			imageResource = omnNode.getModel().createResource();
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

	static void tryExtractEmulabFd(Object rspecNodeObject, Resource omnNode) {
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
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	static void tryExtractEmulabNodeType(Object rspecHwObject, Resource omnHw) {
		try {
			NodeType nodeType = (NodeType) rspecHwObject;

			String nodeTypeSlots = nodeType.getTypeSlots();
			omnHw.addProperty(Omn_domain_pc.hasEmulabNodeTypeSlots,
					nodeTypeSlots);

			String staticSlot = nodeType.getStatic();
			if (staticSlot != null) {
				omnHw.addProperty(Omn_domain_pc.emulabNodeTypeStatic,
						staticSlot);
			}

		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	static void tryExtractMonitoring(Object rspecNodeObject, Resource omnNode) {
		try {
			Monitoring monitor = (Monitoring) rspecNodeObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource monitoringResource = omnNode.getModel().createResource(
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
			omnNode.addProperty(Omn_lifecycle.usesService, monitoringResource);
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	static void tryExtractAvailability(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<AvailableContents> availablityJaxb = (JAXBElement<AvailableContents>) rspecNodeObject;
			final AvailableContents availability = availablityJaxb.getValue();

			omnNode.addLiteral(Omn_resource.isAvailable, availability.isNow());
		} catch (final ClassCastException e) {
			AdExtractExt.LOG.finer(e.getMessage());
		}
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
			AdExtractExt.LOG.finer(e.getMessage());
		}
	}

	public static void tryExtractOlChannel(Object rspecObject, Resource omnNode)
			throws InvalidRspecValueException {
		try {
			Channel channel = (Channel) rspecObject;

			String uuid;
			String componentId = channel.getComponentId();
			if (componentId != null
					&& (AbstractConverter.isUrl(componentId) || AbstractConverter
							.isUrn(componentId))) {
				uuid = componentId;
			} else {
				uuid = "urn:uuid:" + UUID.randomUUID().toString();
			}

			Resource omnChannel = omnNode.getModel().createResource(uuid);
			omnChannel.addProperty(RDF.type, Omn_domain_wireless.Channel);
			omnNode.addProperty(Omn.hasComponent, omnChannel);

			if (channel.getFrequency() != null) {
				String frequency = channel.getFrequency();
				Resource frequencyIndividual = CommonMethods.getFrequency(
						frequency, omnNode.getModel());

				omnChannel.addProperty(Omn_domain_wireless.usesFrequency,
						frequencyIndividual);
			}

			if (componentId != null) {
				omnChannel.addProperty(Omn_lifecycle.hasComponentID,
						componentId);
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
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

	public static void tryExtractOlLease(Object rspecObject, Resource omnNode) {
		try {

			Lease lease = (Lease) rspecObject;

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
			AdExtractExt.LOG.finer(e.getMessage());
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
			AdExtractExt.LOG.finer(e.getMessage());
		}

	}

}
