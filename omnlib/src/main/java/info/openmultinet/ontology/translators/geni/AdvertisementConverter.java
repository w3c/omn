package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AccessNetwork;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ActionSpec;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ApnContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Available;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AvailableContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Cloud;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ControlAddressContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Controller;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ControllerRole;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Datapath;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Datapath.Port;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.DlType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.DlVlan;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ENodeBContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Epc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.EpcIpContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ExternalReferenceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Fd;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.GroupContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HopContent;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.MatchContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NextHopContent;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContent;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType.DiskImage;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NwDst;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NwSrc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.OscoLocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.PacketContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.PathContent;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecOpstate;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Sliver;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.StateSpec;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.StitchContent;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.SubnetContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.SubscriberContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Ue;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.UeDiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.UeHardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.UseGroup;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.WaitSpec;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Osco;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URI;
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
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import org.apache.xerces.dom.ElementNSImpl;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class AdvertisementConverter extends AbstractConverter {

	private static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.advertisement";
	// private static final String PREFIX =
	// "http://open-multinet.info/omnlib/converter";
	private static final String HOST = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger
			.getLogger(AdvertisementConverter.class.getName());
	private Model model;
	private JAXBContext context;
	private Unmarshaller unmarshaller;
	private ObjectFactory of;
	private XMLInputFactory xmlif;
	private boolean verbose = false;

	public AdvertisementConverter() throws JAXBException {
		super();
		this.model = ModelFactory.createInfModel(this.reasoner,
				ModelFactory.createDefaultModel());
		this.context = JAXBContext.newInstance(RSpecContents.class);
		this.unmarshaller = context.createUnmarshaller();
		this.of = new ObjectFactory();
		this.xmlif = XMLInputFactory.newInstance();
	}

	public void setVerbose(boolean verbosity) {
		this.verbose = verbosity;
	}

	public void resetModel() {
		this.model = ModelFactory.createInfModel(this.reasoner,
				ModelFactory.createDefaultModel());
	}

	public Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException, XMLStreamException,
			MissingRspecElementException {

		final RSpecContents rspecAdvertisement = getRspec(input);

		return getModel(rspecAdvertisement);
	}

	// @fixme: expensive/long running method call
	public RSpecContents getRspec(final InputStream input)
			throws JAXBException, XMLStreamException {

		XMLStreamReader xmler = xmlif.createXMLStreamReader(input);

		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(xmler,
				RSpecContents.class);
		final RSpecContents advertisement = rspec.getValue();
		return advertisement;
	}

	@SuppressWarnings("rawtypes")
	public Model getModel(RSpecContents rspec)
			throws MissingRspecElementException {

		String uuid = "urn:uuid:" + UUID.randomUUID().toString();
		final Resource offering = model.createResource(uuid);

		// final Resource offering = model.createResource(
		// AdvertisementConverter.PREFIX + "#advertisement").addProperty(
		// RDF.type, Omn_lifecycle.Offering);

		offering.addProperty(RDF.type, Omn_lifecycle.Offering);
		offering.addProperty(RDFS.label, "Offering");

		@SuppressWarnings("unchecked")
		final List rspecObjects = (List) rspec.getAnyOrNodeOrLink();

		for (Object rspecObject : rspecObjects) {

			tryExtractNode(rspecObject, offering);
			tryExtractLink(rspecObject, offering);
			tryExtractExternalRef(rspecObject, offering);
			tryExtractSharedVlan(rspecObject, offering);
			tryExtractRoutableAddresses(rspecObject, offering);
			tryExtractOpstate(rspecObject, offering);

			if (rspecObject.toString().contains("stitching")) {
				extractStitching(rspecObject, offering);
			} else if (rspecObject instanceof org.apache.xerces.dom.ElementNSImpl) {
				extractOpenflow(rspecObject, offering);
				// } else if (rspecObject.getClass().equals(Epc.class)) {
				// extractEpc(offering, rspecObject);
			}
		}

		return model;
	}

	private void tryExtractEpc(Resource node, Object rspecObject) {

		try {
			@SuppressWarnings("unchecked")
			Epc epc = (Epc) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnEpc = node.getModel().createResource(uuid);

			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasEvolvedPacketCore,
					omnEpc);

			omnEpc.addProperty(RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.EvolvedPacketCore);

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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void extractENodeB(ENodeBContents eNodeBContents, Resource omnEpc) {

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

	private void extractApn(ApnContents apnContents, Resource omnEpc) {

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

	private void extractOpenflow(Object rspecObject, Resource topology) {

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

	private void extractOpenflowSliver(ElementNSImpl openflow, Resource topology) {
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
			AdvertisementConverter.LOG.log(Level.INFO,
					"Found unknown extension: " + child.getNodeName());

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

	private void extractOpenflowDatapath(Node openflow, Resource datapath) {

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
				Resource port = model.createResource(uuid1);
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

	private void tryExtractEmulabTrivialBandwidth(Object rspecNodeObject,
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void extractStitching(Object rspecObject, Resource offering)
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

	private void tryExtractInterface(Object rspecObject, Resource omnResource) {

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
				tryExtractEmulabInterface(interfaceObject, interfaceResource);
			}

			interfaceResource.addProperty(RDF.type, Omn_resource.Interface);
			omnResource.addProperty(Omn_resource.hasInterface,
					interfaceResource);

			if (content.getComponentId() != null) {
				URI uri = URI.create(content.getComponentId());
				interfaceResource.addLiteral(Omn_lifecycle.hasComponentID, uri);
			}

			if (content.getRole() != null) {
				interfaceResource.addProperty(Omn_lifecycle.hasRole,
						content.getRole());
			}

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void tryExtractEmulabInterface(Object interfaceObject,
			Resource interfaceResource) {
		try {
			if (interfaceObject.toString().contains("emulab:interface")) {
				AdvertisementConverter.LOG
						.info("emulab:interface extension not yet supported");
			}
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void tryExtractRoutableAddresses(Object rspecObject,
			Resource offering) {
		try {

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void tryExtractSharedVlan(Object rspecObject, Resource offering) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void tryExtractExternalRef(Object rspecObject, Resource offering)
			throws MissingRspecElementException {
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractLink(final Object rspecObject,
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
					AdvertisementConverter.LOG.log(Level.INFO,
							"Found unknown link extension: " + o);
				}
			}

			linkResource.addProperty(RDF.type, Omn_resource.Link);
			linkResource.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, linkResource);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void extractLinkType(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final LinkType content = (LinkType) o;

		// name required
		if (content.getName() == null) {
			throw new MissingRspecElementException("link_type > name");
		}
		linkResource.addProperty(RDFS.label, content.getName());

	}

	private void extractComponentManager(Object o, Resource linkResource) {

		final ComponentManager content = (ComponentManager) o;

		if (content.getName() != null) {
			URI uri = URI.create(content.getName());
			if (uri != null) {
				linkResource.addLiteral(Omn_lifecycle.hasComponentManagerName,
						uri);
			}
		}
	}

	private void extractLinkProperties(JAXBElement<?> linkElement,
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

	private void extractInterfaceRefs(JAXBElement<?> linkElement,
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

	private void tryExtractOpstate(Object rspecObject, Resource offering)
			throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractOpstateState(Object object, Resource opstate) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractAction(Object awd, Resource state) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractWait(Object awd, Resource state) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractDescription(Object awd, Resource state) {
		try {
			@SuppressWarnings("unchecked")
			String description = (String) awd;
			state.addProperty(RDFS.comment, description);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractOpstateSliverType(Object object, Resource opstate)
			throws MissingRspecElementException {

		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractNode(final Object object, final Resource topology)
			throws MissingRspecElementException {
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
				tryExtractAvailability(rspecNodeObject, omnNode);
				tryExtractMonitoring(rspecNodeObject, omnNode);
				tryExtractOsco(rspecNodeObject, omnNode);
				tryExtractInterface(rspecNodeObject, omnNode);
				tryExtractEmulabFd(rspecNodeObject, omnNode);
				tryExtractEmulabTrivialBandwidth(rspecNodeObject, omnNode);
				tryExtractAccessNetwork(omnNode, rspecNodeObject);
				tryExtractUe(omnNode, rspecNodeObject);
				tryExtractEpc(omnNode, rspecNodeObject);

			}

			topology.addProperty(Omn.hasResource, omnNode);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractAccessNetwork(Resource node, Object rspecNodeObject) {
		try {
			@SuppressWarnings("unchecked")
			AccessNetwork accessNetwork = (AccessNetwork) rspecNodeObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnAccessNetwork = node.getModel().createResource(uuid);

			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasAccessNetwork,
					omnAccessNetwork);

			omnAccessNetwork.addProperty(RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.AccessNetwork);

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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void extractIpContents(EpcIpContents ipContents,
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

	private void tryExtractUe(Resource node, Object rspecObject) {

		try {
			@SuppressWarnings("unchecked")
			Ue ue = (Ue) rspecObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnUe = node.getModel().createResource(uuid);

			node.addProperty(
					info.openmultinet.ontology.vocabulary.Epc.hasUserEquipment,
					omnUe);

			omnUe.addProperty(RDF.type,
					info.openmultinet.ontology.vocabulary.Epc.UserEquipment);

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
					extractDiskImage(diskImageContents, omnUe);
				} else if (o.getClass().equals(UeHardwareTypeContents.class)) {
					UeHardwareTypeContents hardwareTypeContents = (UeHardwareTypeContents) o;
					extractHardwareType(hardwareTypeContents, omnUe);
				}

			}

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void extractDiskImage(UeDiskImageContents diskImageContents,
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

	private void extractHardwareType(
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

	private void extractControlAddress(
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

	private void tryExtractOsco(Object rspecNodeObject, Resource omnOsco) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void extractOscoSubnet(SubnetContents subnetContents,
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

	private void extractOscoLocation(OscoLocationContents locationContents,
			Resource omnOsco) {

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

	private void extractOscoImage(ImageContents image, Resource omnNode) {
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

	private void tryExtractCloud(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final Cloud cloudJaxb = (Cloud) rspecNodeObject;
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			final Resource cloudResource = omnNode.getModel().createResource(
					uuid);

			omnNode.addProperty(RDF.type, Omn_resource.Cloud);
			omnNode.addProperty(Omn.hasResource, cloudResource);
			cloudResource.addProperty(Omn.isResourceOf, omnNode);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractEmulabFd(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			Fd featureDescription = (Fd) rspecNodeObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource fdResource = model.createResource(uuid);
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}

	}

	private void tryExtractEmulabNodeType(Object rspecHwObject, Resource omnHw) {
		try {
			@SuppressWarnings("unchecked")
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractMonitoring(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			Monitoring monitor = (Monitoring) rspecNodeObject;

			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource monitoringResource = model.createResource(uuid);
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractAvailability(Object rspecNodeObject, Resource omnNode) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<AvailableContents> availablityJaxb = (JAXBElement<AvailableContents>) rspecNodeObject;
			final AvailableContents availability = availablityJaxb.getValue();

			omnNode.addLiteral(Omn_resource.isAvailable, availability.isNow());
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractLocation(Object rspecNodeObject, Resource omnNode)
			throws MissingRspecElementException {
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
				omnNode.addProperty(Omn_resource.hasLocation, locationResource);
			}
		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractHardwareType(Object rspecNodeObject, Resource omnNode)
			throws MissingRspecElementException {
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
				tryExtractEmulabNodeType(hwObject, omnHw);
			}
			omnNode.addProperty(Omn_resource.hasHardwareType, omnHw);

		} catch (final ClassCastException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractSliverType(Object rspecNodeObject,
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
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void tryExtractCpus(Object rspecSliverObject, Resource omnSliver) {
		if (rspecSliverObject
				.getClass()
				.equals(info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc.class)) {
			Pc pc = (Pc) rspecSliverObject;

			if (pc.getCpus() != null) {
				omnSliver.addLiteral(Omn_domain_pc.hasCPU, pc.getCpus());
			}
		}
	}

	private void tryExtractDiskImage(Object rspecSliverObject,
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
				Resource diskImage = model.createResource(uuid);
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

	public String getRSpec(final Model model) throws JAXBException,
			InvalidModelException {
		final JAXBElement<RSpecContents> rspec = getRSpecObject(model);

		return AbstractConverter.toString(rspec, AdvertisementConverter.JAXB);
	}

	public JAXBElement<RSpecContents> getRSpecObject(final Model model)
			throws InvalidModelException {
		final RSpecContents advertisement = new RSpecContents();
		advertisement.setType(RspecTypeContents.ADVERTISEMENT);
		advertisement.setGeneratedBy(AbstractConverter.VENDOR);
		setTimeInformation(advertisement);

		model2rspec(model, advertisement);
		final JAXBElement<RSpecContents> rspec = this.of
				.createRspec(advertisement);
		return rspec;
	}

	private void model2rspec(final Model model, final RSpecContents ad)
			throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Offering).toList();
		AbstractConverter.validateModel(groups);
		final Resource group = groups.iterator().next();

		// set external_ref
		if (group.hasProperty(Omn_lifecycle.hasID)) {
			ExternalReferenceContents exrefContents = of
					.createExternalReferenceContents();
			String componentId = group.getProperty(Omn_lifecycle.hasID)
					.getObject().asLiteral().getString();
			exrefContents.setComponentId(componentId);

			if (group.hasProperty(Omn_lifecycle.managedBy)) {
				String component_manager_id = group
						.getProperty(Omn_lifecycle.managedBy).getObject()
						.asLiteral().getString();
				exrefContents.setComponentManagerId(component_manager_id);
			}

			ad.getAnyOrNodeOrLink().add(of.createExternalRef(exrefContents));
		}

		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		convertStatementsToNodesAndLinks(ad, resources);
	}

	private void convertStatementsToNodesAndLinks(
			final RSpecContents advertisement,
			final List<Statement> omnResources) {
		for (final Statement omnResource : omnResources) {
			// if type doesn't match anything else, then assume it's a node
			if (!omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Link)
					&& !omnResource.getResource().hasProperty(RDF.type,
							Omn_lifecycle.Opstate)
					&& !omnResource.getResource().hasProperty(RDF.type,
							Omn_domain_pc.SharedVlan)
					&& !omnResource.getResource().hasProperty(RDF.type,
							Omn_resource.Stitching)
					&& !omnResource.getResource().hasProperty(RDF.type,
							Omn_domain_pc.Datapath)
					&& !omnResource.getResource().hasProperty(RDF.type,
							Omn_resource.Openflow)
					&& !omnResource
							.getResource()
							.hasProperty(
									RDF.type,
									info.openmultinet.ontology.vocabulary.Epc.EvolvedPacketCore)) {

				if (verbose) {
					setNodesVerbose(omnResource, advertisement);
				} else {
					// @todo: check type of resource here and not only generate
					// nodes
					final NodeContents geniNode = new NodeContents();

					setCloud(omnResource, geniNode);
					setComponentDetails(omnResource, geniNode);
					setComponentManagerId(omnResource, geniNode);
					setHardwareTypes(omnResource, geniNode);
					setSliverTypes(omnResource, geniNode);
					setLocation(omnResource, geniNode);
					setAvailability(omnResource, geniNode);
					setMonitoringService(omnResource, geniNode);
					setOsco(omnResource, geniNode);
					setInterface(omnResource, geniNode);
					setFd(omnResource, geniNode);
					setTrivialBandwidth(omnResource, geniNode);
					setAccessNetwork(omnResource, geniNode);
					setEPC(omnResource, geniNode);
					setUE(omnResource, geniNode);

					ResIterator infrastructures = omnResource.getModel()
							.listResourcesWithProperty(Omn.isResourceOf,
									Omn_federation.Infrastructure);
					if (infrastructures.hasNext()) {
						Resource infrastructure = infrastructures.next();
						geniNode.setComponentManagerId(infrastructure.getURI());
					}

					advertisement.getAnyOrNodeOrLink().add(
							this.of.createNode(geniNode));
				}
			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_domain_pc.Datapath)) {

				// TODO: set datapath as direct sub element of RSpec
				final Datapath dp = new Datapath();
				GroupContents gc = new GroupContents();
				Sliver of = new Sliver();
				setOpenflowDatapath(omnResource, dp);
				gc.setDatapath(dp);
				of.getGroup().add(gc);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Link)) {
				final LinkContents link = new LinkContents();

				setLinkDetails(omnResource, link);
				setInterfaceRefs(omnResource, link);
				setLinkProperties(omnResource, link);

				advertisement.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Openflow)) {
				final Sliver of = new Sliver();
				setOpenflow(omnResource, of);
				advertisement.getAnyOrNodeOrLink().add(of);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_domain_pc.SharedVlan)) {

				RspecSharedVlan sharedVlan = this.of.createRspecSharedVlan();

				StmtIterator availables = omnResource.getResource()
						.listProperties(Omn_domain_pc.hasAvailable);

				while (availables.hasNext()) {
					Resource availableResource = availables.next().getObject()
							.asResource();
					Available available = this.of.createAvailable();

					if (availableResource.hasProperty(Omn_domain_pc.restricted)) {
						boolean restricted = availableResource
								.getProperty(Omn_domain_pc.restricted)
								.getObject().asLiteral().getBoolean();
						available.setRestricted(restricted);
					}
					if (availableResource.hasProperty(RDFS.label)) {
						String name = availableResource.getProperty(RDFS.label)
								.getObject().asLiteral().getString();
						available.setName(name);
					}
					sharedVlan.getAvailable().add(available);
				}

				advertisement.getAnyOrNodeOrLink().add(sharedVlan);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_lifecycle.Opstate)) {

				RspecOpstate rspecOpstate = this.of.createRspecOpstate();

				setOpstateAttributes(omnResource, rspecOpstate);
				setSliverTypes(omnResource, rspecOpstate);
				setStates(omnResource, rspecOpstate);

				advertisement.getAnyOrNodeOrLink().add(rspecOpstate);

			} else if (omnResource.getResource().hasProperty(RDF.type,
					Omn_resource.Stitching)) {
				ObjectFactory of = new ObjectFactory();

				StitchContent stitchContent = of.createStitchContent();
				setStitching(omnResource, stitchContent);

				JAXBElement<StitchContent> stitching = of
						.createStitching(stitchContent);

				advertisement.getAnyOrNodeOrLink().add(stitching);
				// } else if (omnResource.getResource().hasProperty(RDF.type,
				// info.openmultinet.ontology.vocabulary.Epc.EPC)) {
				// setEPC(omnResource, advertisement);
			}
		}
	}

	private void setAccessNetwork(Statement omnResource, NodeContents geniNode) {
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

	private void setIpAddress(AccessNetwork accessNetwork, Resource ipAddress) {

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

	private void setUE(Statement omnResource, NodeContents geniNode) {
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

	private void setUeDiskImage(Resource diskImageResource, Ue ue) {

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

	private void setHardwareType(Resource hardwareTypeResource, Ue ue) {

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

	private void setControlAddress(Resource controlAddressResource, Ue ue) {
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

	private void setEPC(Statement omnResource, NodeContents geniNode) {

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

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Epc.pdnGateway)) {
				String pdnGateway = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Epc.pdnGateway)
						.getObject().asLiteral().getString();
				epc.setPdnGateway(pdnGateway);
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
				epc.getApnOrEnodebOrSubscriber().add(setEpcApn(apn));
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
				epc.getApnOrEnodebOrSubscriber().add(subscriberContents);
			}

			geniNode.getAnyOrRelationOrLocation().add(epc);
		}
	}

	private void setENodeB(Epc epc, Resource eNodeB) {
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

		epc.getApnOrEnodebOrSubscriber().add(eNodeBContents);

	}

	private ApnContents setEpcApn(Resource apn) {
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

	private void setOsco(Statement resource, NodeContents node) {

		// private void setOsco(Statement resource, SliverType node) {
		Resource resourceResource = resource.getResource();

		if (CommonMethods.hasOscoProperty(resourceResource)) {

			info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco osco = new ObjectFactory()
					.createOsco();

			String aboutUri = resourceResource.getURI();
			if (aboutUri != null && aboutUri != "") {
				osco.setAbout(aboutUri);
			}

			if (resourceResource.hasProperty(Omn_lifecycle.hasID)) {
				String id = resourceResource.getProperty(Omn_lifecycle.hasID)
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

			if (resourceResource.hasProperty(Osco.ANNC_AUTO)) {
				String anncAutoString = resourceResource
						.getProperty(Osco.ANNC_AUTO).getObject().asLiteral()
						.getString();
				boolean anncAuto = Boolean.parseBoolean(anncAutoString);
				osco.setAnncAuto(anncAuto);
			}

			if (resourceResource.hasProperty(Osco.ANNC_DISABLED)) {
				String anncDisabledString = resourceResource
						.getProperty(Osco.ANNC_DISABLED).getObject()
						.asLiteral().getString();
				boolean anncDisabled = Boolean.parseBoolean(anncDisabledString);
				osco.setAnncDisabled(anncDisabled);
			}

			if (resourceResource.hasProperty(Osco.APP_ID)) {
				String appId = resourceResource.getProperty(Osco.APP_ID)
						.getObject().asLiteral().getString();
				osco.setAppId(appId);
			}

			if (resourceResource.hasProperty(Osco.APP_PORT)) {
				String appPort = resourceResource.getProperty(Osco.APP_PORT)
						.getObject().asLiteral().getString();
				int appPortInt = Integer.parseInt(appPort);
				BigInteger appPortBigInt = BigInteger.valueOf(appPortInt);
				osco.setAppPort(appPortBigInt);
			}

			if (resourceResource.hasProperty(Osco.Bit_Bucket_DB_IP)) {
				String bitBucketDbIp = resourceResource
						.getProperty(Osco.Bit_Bucket_DB_IP).getObject()
						.asLiteral().getString();
				osco.setBitBucketDbIp(bitBucketDbIp);
			}

			if (resourceResource.hasProperty(Osco.COAP_DISABLED)) {
				String coapDisabledString = resourceResource
						.getProperty(Osco.COAP_DISABLED).getObject()
						.asLiteral().getString();
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

			if (resourceResource.hasProperty(Osco.local_port)) {
				String localPort = resourceResource
						.getProperty(Osco.local_port).getObject().asLiteral()
						.getString();
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

			if (resourceResource.hasProperty(Osco.LOGGING_FILE)) {
				String loggingFile = resourceResource
						.getProperty(Osco.LOGGING_FILE).getObject().asLiteral()
						.getString();
				osco.setLoggingFile(loggingFile);
			}

			if (resourceResource.hasProperty(Osco.LOGGING_LEVEL)) {
				String loggingLevel = resourceResource
						.getProperty(Osco.LOGGING_LEVEL).getObject()
						.asLiteral().getString();
				osco.setLoggingLevel(loggingLevel);
			}

			if (resourceResource.hasProperty(Osco.m2m_conn_app_ip)) {
				String m2mConnAppIp = resourceResource
						.getProperty(Osco.m2m_conn_app_ip).getObject()
						.asLiteral().getString();
				osco.setM2MConnAppIp(m2mConnAppIp);
			}

			if (resourceResource.hasProperty(Osco.m2m_conn_app_port)) {
				String m2mConnAppPort = resourceResource
						.getProperty(Osco.m2m_conn_app_port).getObject()
						.asLiteral().getString();
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

			if (resourceResource.hasProperty(Osco.MGMT_INTF)) {
				String mgmtIntf = resourceResource.getProperty(Osco.MGMT_INTF)
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

			if (resourceResource.hasProperty(Osco.NOTIFY_CHAN_DISABLED)) {
				String notifyChanDisabledString = resourceResource
						.getProperty(Osco.NOTIFY_CHAN_DISABLED).getObject()
						.asLiteral().getString();
				boolean notifyChanDisabled = Boolean
						.parseBoolean(notifyChanDisabledString);
				osco.setNotifyChanDisabled(notifyChanDisabled);
			}

			if (resourceResource.hasProperty(Osco.NOTIFY_DISABLED)) {
				String notifyDisabledString = resourceResource
						.getProperty(Osco.NOTIFY_DISABLED).getObject()
						.asLiteral().getString();
				boolean notifyDisabled = Boolean
						.parseBoolean(notifyDisabledString);
				osco.setNotifyDisabled(notifyDisabled);
			}

			if (resourceResource.hasProperty(Osco.OMTC_URL)) {
				String omtcUrlString = resourceResource
						.getProperty(Osco.OMTC_URL).getObject().asLiteral()
						.getString();
				osco.setOmtcUrl(omtcUrlString);
			}

			if (resourceResource.hasProperty(Osco.REQUIRE_AUTH)) {
				String requireAuthString = resourceResource
						.getProperty(Osco.REQUIRE_AUTH).getObject().asLiteral()
						.getString();
				boolean requireAuth = Boolean.parseBoolean(requireAuthString);
				osco.setRequireAuth(requireAuth);
			}

			if (resourceResource.hasProperty(Osco.requires)) {
				String requires = resourceResource.getProperty(Osco.requires)
						.getObject().asResource().getURI();
				osco.setRequires(requires);
			}

			if (resourceResource.hasProperty(Osco.RETARGET_DISABLED)) {
				String retargetDisabledString = resourceResource
						.getProperty(Osco.RETARGET_DISABLED).getObject()
						.asLiteral().getString();
				boolean retargetDisabled = Boolean
						.parseBoolean(retargetDisabledString);
				osco.setRetargetDisabled(retargetDisabled);
			}

			if (resourceResource.hasProperty(Osco.SERVICE_PORT)) {
				String servicePort = resourceResource
						.getProperty(Osco.SERVICE_PORT).getObject().asLiteral()
						.getString();
				int servicePortInt = Integer.parseInt(servicePort);
				BigInteger servicePortBigInt = BigInteger
						.valueOf(servicePortInt);
				osco.setServicePort(servicePortBigInt);
			}

			if (resourceResource.hasProperty(Osco.SSL_ENABLED)) {
				String sslEnabledString = resourceResource
						.getProperty(Osco.SSL_ENABLED).getObject().asLiteral()
						.getString();
				boolean sslEnabled = Boolean.parseBoolean(sslEnabledString);
				osco.setSslEnabled(sslEnabled);
			}

			if (resourceResource.hasProperty(Osco.SSL_PORT)) {
				String sslPort = resourceResource.getProperty(Osco.SSL_PORT)
						.getObject().asLiteral().getString();
				int sslPortInt = Integer.parseInt(sslPort);
				BigInteger sslPortBigInt = BigInteger.valueOf(sslPortInt);
				osco.setSslPort(sslPortBigInt);
			}

			if (resourceResource
					.hasProperty(info.openmultinet.ontology.vocabulary.Osco.subnet)) {
				Resource subnet = resourceResource
						.getProperty(
								info.openmultinet.ontology.vocabulary.Osco.subnet)
						.getObject().asResource();
				setOscoSubnet(osco, subnet);
			}

			if (resourceResource.hasProperty(Osco.TEST_PARAM)) {
				String testParam = resourceResource
						.getProperty(Osco.TEST_PARAM).getObject().asLiteral()
						.getString();
				osco.setTestParam(testParam);
			}

			node.getAnyOrRelationOrLocation().add(osco);
			// node.getAnyOrDiskImage().add(osco);
		}
	}

	private void setOscoSubnet(
			info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco osco,
			Resource subnet) {
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

		if (subnet.hasProperty(info.openmultinet.ontology.vocabulary.Osco.name)) {
			String name = subnet
					.getProperty(
							info.openmultinet.ontology.vocabulary.Osco.name)
					.getObject().asLiteral().getString();
			subnetContents.setName(name);
		}

		String aboutUri = subnet.getURI();
		if (aboutUri != null && aboutUri != "") {
			subnetContents.setAbout(aboutUri);
		}

		osco.getImageOrOscoLocationOrSubnet().add(subnetContents);

	}

	private void setOscoLocation(
			info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco osco,
			Resource location) {
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

	private void setOscoImage(
			info.openmultinet.ontology.translators.geni.jaxb.advertisement.Osco osco,
			Resource imageResource) {

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

	private void setOpenflowDatapath(Statement omnResource, Datapath dp) {
		Resource object = omnResource.getResource();

		// set component ID
		if (object.hasProperty(Omn_lifecycle.hasComponentID)) {
			String componentId = object
					.getProperty(Omn_lifecycle.hasComponentID).getObject()
					.asLiteral().getString();
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
			String id = object.getProperty(Omn_lifecycle.hasID).getObject()
					.asLiteral().getString();
			dp.setDpid(id);
		}

		StmtIterator ports = object.listProperties(Omn_resource.hasInterface);

		while (ports.hasNext()) {
			Resource omnPort = ports.next().getResource().asResource();

			Port port = new Port();

			if (omnPort.hasProperty(RDFS.label)) {
				port.setName(omnPort.getProperty(RDFS.label).getObject()
						.asLiteral().getString());
			}
			if (omnPort.hasProperty(Omn_resource.port)) {
				int portNumber = omnPort.getProperty(Omn_resource.port)
						.getObject().asLiteral().getInt();
				port.setNum(portNumber);
			}

			dp.getPort().add(port);
		}
	}

	private void setOpenflow(Statement resource, Sliver of) {
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

			Statement typeStatement = types.next();
			Resource object = typeStatement.getObject().asResource();

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

				if (object.hasProperty(RDFS.label)) {
					gc.setName(object.getProperty(RDFS.label).getLiteral()
							.getString());
				}

				final Datapath dp = new Datapath();
				setOpenflowDatapath(typeStatement, dp);
				gc.setDatapath(dp);
				of.getGroup().add(gc);
			}
		}
	}

	private void setNodesVerbose(Statement omnResource,
			RSpecContents advertisement) {
		StmtIterator canImplement = omnResource.getResource().listProperties(
				Omn_lifecycle.canImplement);

		while (canImplement.hasNext()) {
			Statement omnSliver = canImplement.next();

			final NodeContents geniNode = new NodeContents();

			SliverType sliver1;
			sliver1 = of.createNodeContentsSliverType();
			String sliverUri = omnSliver.getObject().asResource().getURI();
			setOsco(omnSliver, geniNode);
			// setOsco(omnSliver, sliver1);
			sliver1.setName(sliverUri);

			JAXBElement<SliverType> sliverType = new ObjectFactory()
					.createNodeContentsSliverType(sliver1);

			geniNode.getAnyOrRelationOrLocation().add(sliverType);

			String sliverName = CommonMethods.getLocalName(sliverUri);
			String url = omnSliver.getSubject().asResource().getURI();
			String urn = CommonMethods.generateUrnFromUrl(url, "node") + "+"
					+ sliverName;
			geniNode.setComponentId(urn);
			geniNode.setComponentName(sliverName);
			// System.out.println(omnResource.getResource().getURI());
			setCloud(omnResource, geniNode);
			setComponentDetailsVerbose(omnResource, geniNode);
			setComponentManagerId(omnResource, geniNode);
			setHardwareTypesVerbose(omnResource, geniNode);
			setLocation(omnResource, geniNode);
			setAvailability(omnResource, geniNode);
			// setOsco(omnResource, geniNode);
			setMonitoringService(omnResource, geniNode);
			setInterface(omnResource, geniNode);
			setFd(omnResource, geniNode);
			setTrivialBandwidth(omnResource, geniNode);

			ResIterator infrastructures = omnResource.getModel()
					.listResourcesWithProperty(Omn.isResourceOf,
							Omn_federation.Infrastructure);
			if (infrastructures.hasNext()) {
				Resource infrastructure = infrastructures.next();
				geniNode.setComponentManagerId(infrastructure.getURI());
			}

			advertisement.getAnyOrNodeOrLink()
					.add(this.of.createNode(geniNode));
		}
	}

	private void setHardwareTypesVerbose(Statement omnResource,
			NodeContents geniNode) {
		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(RDF.type);
		HardwareTypeContents hwType;

		while (types.hasNext()) {
			String rdfType = types.next().getResource().getURI();

			hwType = of.createHardwareTypeContents();
			hwType.setName(rdfType);
			if ((null != rdfType) && AbstractConverter.nonGeneric(rdfType)) {
				geniNodeDetails.add(of.createHardwareType(hwType));
			}
		}
	}

	private void setComponentDetailsVerbose(Statement resource,
			NodeContents node) {
		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}
	}

	private void setTrivialBandwidth(Statement omnResource,
			NodeContents geniNode) {
		if (omnResource.getResource().hasProperty(
				Omn_domain_pc.hasEmulabTrivialBandwidth)) {
			// int value = omnResource.getResource()
			// .getProperty(Omn_domain_pc.hasEmulabTrivialBandwidth).getObject()
			// .asLiteral().getInt();

			// info.openmultinet.ontology.translators.geni.jaxb.advertisement.extensions.ObjectFactory
			// of = new
			// info.openmultinet.ontology.translators.geni.jaxb.advertisement.extensions.ObjectFactory();
			// TrivialBandwidth trivialBandwidth = of.createTrivialBandwidth();
			// trivialBandwidth.setValue(BigInteger.valueOf(value));
			// geniNode.getAnyOrRelationOrLocation().add(trivialBandwidth);
		}
	}

	private void setStitching(Statement resource, StitchContent stitchContent) {
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

	private void setCloud(Statement omnResource, NodeContents geniNode) {
		if (omnResource.getResource().hasProperty(RDF.type, Omn_resource.Cloud)) {
			Cloud cloud = new ObjectFactory().createCloud();
			geniNode.getAnyOrRelationOrLocation().add(cloud);
		}

	}

	private void setLinkProperties(Statement resource, LinkContents link) {
		List<Statement> linkProperties = resource.getResource()
				.listProperties(Omn_resource.hasProperty).toList();

		for (Statement linkPropertyStatement : linkProperties) {

			LinkPropertyContents newLinkProperty = new ObjectFactory()
					.createLinkPropertyContents();

			Resource linkResource = linkPropertyStatement.getResource();

			String sinkId = linkResource.getProperty(Omn_resource.hasSink)
					.getObject().asLiteral().getString();
			newLinkProperty.setDestId(sinkId);

			String sourceId = linkResource.getProperty(Omn_resource.hasSource)
					.getObject().asLiteral().getString();
			newLinkProperty.setSourceId(sourceId);

			if (linkResource.hasProperty(Omn_domain_pc.hasLatency)) {
				String latency = linkResource
						.getProperty(Omn_domain_pc.hasLatency).getObject()
						.asLiteral().getString();
				newLinkProperty.setLatency(latency);
			}

			if (linkResource.hasProperty(Omn_domain_pc.hasPacketLoss)) {
				String packetLoss = linkResource
						.getProperty(Omn_domain_pc.hasPacketLoss).getObject()
						.asLiteral().getString();
				newLinkProperty.setPacketLoss(packetLoss);
			}

			if (linkResource.hasProperty(Omn_domain_pc.hasCapacity)) {
				String capacity = linkResource
						.getProperty(Omn_domain_pc.hasCapacity).getObject()
						.asLiteral().getString();

				if (linkResource.hasProperty(Omn_domain_pc.hasCapacityUnit)) {
					String capacityUnit = linkResource
							.getProperty(Omn_domain_pc.hasCapacityUnit)
							.getObject().asLiteral().getString();
					capacity = capacity + capacityUnit;
				}

				newLinkProperty.setCapacity(capacity);
			}

			link.getAnyOrPropertyOrLinkType().add(
					new ObjectFactory().createProperty(newLinkProperty));
		}

	}

	private void setInterfaceRefs(Statement resource, LinkContents link) {
		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (Statement interface1 : interfaces) {

			InterfaceRefContents newInterface = new ObjectFactory()
					.createInterfaceRefContents();

			String componentId = interface1.getResource()
					.getProperty(Omn_lifecycle.hasComponentID).getObject()
					.asLiteral().getString();

			newInterface.setComponentId(componentId);

			JAXBElement<InterfaceRefContents> interfaceJaxb = new ObjectFactory()
					.createInterfaceRef(newInterface);

			link.getAnyOrPropertyOrLinkType().add(interfaceJaxb);
		}

	}

	private void setLinkDetails(Statement resource, LinkContents link) {

		// List<Statement> linkTypes = resource.getResource()
		// .listProperties(Omn_lifecycle.hasLinkName).toList();
		// for (Statement linkStatement : linkTypes) {
		// String linkName = linkStatement.getObject().asLiteral().getString();
		// LinkType linkType = new ObjectFactory().createLinkType();
		// linkType.setName(linkName);
		// link.getAnyOrPropertyOrLinkType().add(linkType);
		// }

		if (resource.getResource().hasProperty(RDFS.label)) {
			String linkName = resource.getResource().getProperty(RDFS.label)
					.getObject().asLiteral().getString();
			LinkType linkType = new ObjectFactory().createLinkType();
			linkType.setName(linkName);
			link.getAnyOrPropertyOrLinkType().add(linkType);
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.hasComponentID)) {
			String componentId = resource.getResource()
					.getProperty(Omn_lifecycle.hasComponentID).getObject()
					.asLiteral().getString();
			link.setComponentId(componentId);
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.hasComponentName)) {
			String componentName = resource.getResource()
					.getProperty(Omn_lifecycle.hasComponentName).getObject()
					.asLiteral().getString();
			link.setComponentName(componentName);
		}

		if (resource.getResource().hasProperty(
				Omn_lifecycle.hasComponentManagerName)) {

			List<Statement> componentManagers = resource.getResource()
					.listProperties(Omn_lifecycle.hasComponentManagerName)
					.toList();

			for (final Statement manager : componentManagers) {
				String managerName = manager.getObject().asLiteral()
						.getString();
				ComponentManager rspecManager = new ObjectFactory()
						.createComponentManager();
				rspecManager.setName(managerName);
				link.getAnyOrPropertyOrLinkType().add(rspecManager);
			}

		}

	}

	private void setFd(Statement omnResource, NodeContents geniNode) {

		List<Statement> resources = omnResource.getResource()
				.listProperties(Omn.hasAttribute).toList();

		for (final Statement resourceStatement : resources) {
			// add emulab node slots
			if (resourceStatement.getResource().hasProperty(RDF.type,
					Omn_domain_pc.FeatureDescription)) {

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

				geniNode.getAnyOrRelationOrLocation().add(fd);
			}
		}
	}

	private void setInterface(Statement resource, NodeContents nodeContents) {

		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (final Statement interface1 : interfaces) {
			InterfaceContents interfaceContents = new ObjectFactory()
					.createInterfaceContents();
			Resource interfaceResource = interface1.getResource();

			if (interfaceResource.hasProperty(Omn_lifecycle.hasComponentID)) {
				interfaceContents.setComponentId(interfaceResource
						.getProperty(Omn_lifecycle.hasComponentID).getObject()
						.asLiteral().getString());
			}

			if (interfaceResource.hasProperty(Omn_lifecycle.hasRole)) {
				interfaceContents.setRole(interfaceResource
						.getProperty(Omn_lifecycle.hasRole).getObject()
						.asLiteral().toString());
			}

			// setIpAddress(interfaceResource, interfaceContents);

			JAXBElement<InterfaceContents> interfaceRspec = new ObjectFactory()
					.createInterface(interfaceContents);
			nodeContents.getAnyOrRelationOrLocation().add(interfaceRspec);
		}
	}

	private void setOpstateAttributes(Statement omnResource,
			RspecOpstate rspecOpstate) {
		// set aggregateManagerId
		String aggregateManagerId = omnResource
				.getProperty(Omn_lifecycle.managedBy).getObject().asLiteral()
				.getString();
		// required
		if (aggregateManagerId == null) {
			aggregateManagerId = "";
		}
		rspecOpstate.setAggregateManagerId(aggregateManagerId);

		// set start state
		Resource start = omnResource.getProperty(Omn_lifecycle.hasStartState)
				.getObject().asResource();
		String geniStart = CommonMethods.convertOmnToGeniState(start);
		rspecOpstate.setStart(geniStart);

	}

	private void setStates(Statement omnResource, RspecOpstate rspecOpstate) {
		// get states
		StmtIterator states = omnResource.getResource().listProperties(
				Omn_lifecycle.hasState);
		while (states.hasNext()) {
			Statement stateStatement = states.next();
			StateSpec stateSpec = of.createStateSpec();

			// set name
			Resource stateResource = stateStatement.getObject().asResource();
			setStateName(stateResource, stateSpec);
			setDescription(stateResource, stateSpec);
			setWait(stateResource, stateSpec);
			setAction(stateResource, stateSpec);
			rspecOpstate.getSliverTypeOrState().add(stateSpec);
		}
	}

	private void setAction(Resource stateResource, StateSpec stateSpec) {

		StmtIterator actions = stateResource
				.listProperties(Omn_lifecycle.hasAction);
		while (actions.hasNext()) {
			Statement typeStatement = actions.next();
			Resource action = typeStatement.getObject().asResource();
			ActionSpec actionSpec = of.createActionSpec();

			// set next
			Resource next = action.getProperty(Omn_lifecycle.hasNext)
					.getObject().asResource();
			if (CommonMethods.isOmnState(next)) {
				String geniState = CommonMethods.convertOmnToGeniState(next);
				actionSpec.setNext(geniState);
			}

			// set name
			Resource name = action.getProperty(Omn_lifecycle.hasStateName)
					.getObject().asResource();
			if (CommonMethods.isOmnState(name)) {
				String geniState = CommonMethods.convertOmnToGeniState(name);
				actionSpec.setName(geniState);
			}

			// set description
			if (action.hasProperty(RDFS.comment)) {
				String description = action.getProperty(RDFS.comment)
						.getObject().asLiteral().getString();
				actionSpec.setDescription(description);
			}

			stateSpec.getActionOrWaitOrDescription().add(actionSpec);
		}
	}

	private void setWait(Resource stateResource, StateSpec stateSpec) {

		StmtIterator waits = stateResource
				.listProperties(Omn_lifecycle.hasWait);
		while (waits.hasNext()) {
			Statement typeStatement = waits.next();
			Resource wait = typeStatement.getObject().asResource();
			WaitSpec waitSpec = of.createWaitSpec();

			// set next
			Resource next = wait.getProperty(Omn_lifecycle.hasNext).getObject()
					.asResource();
			if (CommonMethods.isOmnState(next)) {
				String geniState = CommonMethods.convertOmnToGeniState(next);
				waitSpec.setNext(geniState);
			}

			// set type
			Resource type = wait.getProperty(Omn_lifecycle.hasType).getObject()
					.asResource();
			if (CommonMethods.isOmnState(type)) {
				String geniState = CommonMethods.convertOmnToGeniState(type);
				waitSpec.setType(geniState);
			}

			stateSpec.getActionOrWaitOrDescription().add(waitSpec);
		}
	}

	private void setStateName(Resource stateResource, StateSpec stateSpec) {

		StmtIterator types = stateResource.listProperties(RDF.type);
		while (types.hasNext()) {
			Statement typeStatement = types.next();
			Resource type = typeStatement.getObject().asResource();

			if (CommonMethods.isOmnState(type)) {
				String geniState = CommonMethods.convertOmnToGeniState(type);
				stateSpec.setName(geniState);
			}
		}
	}

	private void setDescription(Resource stateResource, StateSpec stateSpec) {
		StmtIterator descriptions = stateResource.listProperties(RDFS.comment);
		while (descriptions.hasNext()) {
			Statement descriptionStatement = descriptions.next();
			String descriptionString = descriptionStatement.getObject()
					.asLiteral().getString();
			stateSpec.getActionOrWaitOrDescription().add(descriptionString);
		}
	}

	private void setSliverTypes(Statement omnResource, RspecOpstate rspecOpstate) {
		// get sliver types
		StmtIterator canBeImplementBy = omnResource.getResource()
				.listProperties(Omn_lifecycle.canBeImplementedBy);
		info.openmultinet.ontology.translators.geni.jaxb.advertisement.SliverType sliver;

		List<Object> sliverTypeOrState = rspecOpstate.getSliverTypeOrState();
		while (canBeImplementBy.hasNext()) {
			Statement omnSliver = canBeImplementBy.next();

			sliver = of.createSliverType();

			String name = "";
			if (omnSliver.getObject().asResource().hasProperty(RDFS.label)) {
				name = omnSliver.getObject().asResource()
						.getProperty(RDFS.label).getObject().asLiteral()
						.getString();

			}
			sliver.setName(name);
			sliverTypeOrState.add(sliver);
		}

	}

	private void setMonitoringService(Statement resource, NodeContents node) {
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

	private void setAvailability(Statement omnResource, NodeContents geniNode) {

		AvailableContents availabilty = of.createAvailableContents();
		Resource resource = omnResource.getResource();

		if (resource.hasProperty(Omn_resource.isAvailable)) {
			availabilty.setNow(resource.getProperty(Omn_resource.isAvailable)
					.getBoolean());

			geniNode.getAnyOrRelationOrLocation().add(
					of.createAvailable(availabilty));
		}
	}

	private void setLocation(Statement omnResource, NodeContents geniNode) {

		StmtIterator locations = omnResource.getResource().listProperties(
				Omn_resource.hasLocation);

		while (locations.hasNext()) {

			Statement locationStatement = locations.next();

			LocationContents location = of.createLocationContents();
			Resource omnRes = locationStatement.getResource();

			if (omnRes.hasProperty(Geonames.countryCode)) {
				location.setCountry(omnRes.getProperty(Geonames.countryCode)
						.getString());
			} else {
				// country required
				location.setCountry("");
			}

			if (omnRes.hasProperty(Geo.lat)) {
				location.setLatitude(omnRes.getProperty(Geo.lat).getString());
			}

			if (omnRes.hasProperty(Geo.long_)) {
				location.setLongitude(omnRes.getProperty(Geo.long_).getString());
			}

			if (omnRes.hasProperty(RDFS.label)) {
				QName key = new QName("http://open-multinet.info/location",
						"name", "omn");
				String value = omnRes.getProperty(RDFS.label).getString();
				location.getOtherAttributes().put(key, value);
			}

			if (omnRes.hasProperty(Omn_lifecycle.hasID)) {
				QName key = new QName("http://open-multinet.info/location",
						"id", "omn");
				String value = omnRes.getProperty(Omn_lifecycle.hasID)
						.getString();
				location.getOtherAttributes().put(key, value);
			}

			geniNode.getAnyOrRelationOrLocation().add(
					of.createLocation(location));

		}

		if (omnResource.getResource().hasProperty(Geo.lat)
				|| omnResource.getResource().hasProperty(Geo.long_)
				|| omnResource.getResource().hasProperty(Geonames.countryCode)) {
			LocationContents locationContents = null;

			if (omnResource.getResource().hasProperty(Geo.lat)) {
				locationContents = new ObjectFactory().createLocationContents();
				locationContents.setLatitude(omnResource.getResource()
						.getProperty(Geo.lat).getObject().toString());
			}

			if (omnResource.getResource().hasProperty(Geo.long_)) {
				if (locationContents == null) {
					locationContents = new ObjectFactory()
							.createLocationContents();
				}
				locationContents.setLongitude(omnResource.getResource()
						.getProperty(Geo.long_).getObject().toString());
			}

			if (omnResource.getResource().hasProperty(Geonames.countryCode)) {
				if (locationContents == null) {
					locationContents = new ObjectFactory()
							.createLocationContents();
				}
				locationContents.setCountry(omnResource.getResource()
						.getProperty(Geonames.countryCode).getObject()
						.toString());
			} else {
				// country required
				locationContents.setCountry("");
			}

			if (locationContents != null) {
				JAXBElement<LocationContents> location = new ObjectFactory()
						.createLocation(locationContents);
				geniNode.getAnyOrRelationOrLocation().add(location);
			}
		}
	}

	private void setSliverTypes(Statement resource, NodeContents geniNode) {

		StmtIterator canImplement = resource.getResource().listProperties(
				Omn_lifecycle.canImplement);

		// check if name was string and not uri
		if (resource.getResource().hasProperty(Omn_resource.hasSliverType)) {

			final List<Statement> hasSliverNames = resource.getResource()
					.listProperties(Omn_resource.hasSliverType).toList();

			// if sliver type blank, abort
			if (hasSliverNames.get(0).getObject().asResource()
					.hasProperty(RDF.type, Omn_resource.SliverType)) {
				for (final Statement hasSliverName : hasSliverNames) {

					SliverType sliverType = new ObjectFactory()
							.createNodeContentsSliverType();

					Resource sliverTypeResource = hasSliverName.getObject()
							.asResource();
					if (sliverTypeResource
							.hasProperty(Omn_lifecycle.hasSliverName)) {
						String sliverName = sliverTypeResource
								.getProperty(Omn_lifecycle.hasSliverName)
								.getObject().asLiteral().getString();
						sliverType.setName(sliverName);
					}

					if (sliverTypeResource != null) {

						// setOsco(hasSliverName, sliverType);
						setDiskImage(sliverTypeResource, sliverType);
						JAXBElement<SliverType> sliver = new ObjectFactory()
								.createNodeContentsSliverType(sliverType);
						geniNode.getAnyOrRelationOrLocation().add(sliver);
					}
				}
			}
		} else if (canImplement.hasNext()) {
			while (canImplement.hasNext()) {
				SliverType sliver1;
				Statement omnSliver = canImplement.next();
				sliver1 = of.createNodeContentsSliverType();
				sliver1.setName(omnSliver.getObject().asResource().getURI());

				setDiskImage(omnSliver.getResource(), sliver1);
				// setOsco(omnSliver, sliver1);

				JAXBElement<SliverType> sliverType = new ObjectFactory()
						.createNodeContentsSliverType(sliver1);
				geniNode.getAnyOrRelationOrLocation().add(sliverType);
			}
		} else {
			SliverType sliverType = new ObjectFactory()
					.createNodeContentsSliverType();

			final List<Statement> hasTypes = resource.getResource()
					.listProperties(RDF.type).toList();
			Resource sliverResource = null;
			boolean sliverTypeNameExists = false;
			for (final Statement hasType : hasTypes) {
				sliverResource = hasType.getObject().asResource();
				if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
					sliverType.setName(sliverResource.getURI());
					sliverTypeNameExists = true;
				}
			}

			if (sliverTypeNameExists) {
				setDiskImage(sliverResource, sliverType);
				JAXBElement<SliverType> sliver = new ObjectFactory()
						.createNodeContentsSliverType(sliverType);
				geniNode.getAnyOrRelationOrLocation().add(sliver);
			}
		}
	}

	private void setCpus(Resource sliverResource, SliverType sliver) {
		Pc pc = null;

		if (sliverResource.hasProperty(Omn_domain_pc.hasCPU)) {
			pc = new ObjectFactory().createPc();
			pc.setCpus(sliverResource.getProperty(Omn_domain_pc.hasCPU)
					.getObject().asLiteral().getInt());
		}
		if (pc != null) {
			sliver.getAnyOrDiskImage().add(pc);
		}

	}

	private void setDiskImage(Resource sliverResource, SliverType sliver) {

		while (sliverResource.hasProperty(Omn_domain_pc.hasDiskImage)) {

			Statement omnSliver = sliverResource
					.getProperty(Omn_domain_pc.hasDiskImage);
			omnSliver.remove();
			RDFNode diskImageNode = omnSliver.getObject();
			Resource diskImageResource = diskImageNode.asResource();

			// TODO: diskimage is handled in two places. Need to make a
			// single method.
			// check if the resource is a disk image
			if (diskImageResource
					.hasProperty(RDF.type, Omn_domain_pc.DiskImage)) {

				String diskName = "";
				if (diskImageResource
						.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
					diskName += diskImageResource
							.getProperty(Omn_domain_pc.hasDiskimageLabel)
							.getObject().asLiteral().getString();
				}

				DiskImage diskImage = of
						.createNodeContentsSliverTypeDiskImage();

				if (diskImageResource
						.hasProperty(Omn_domain_pc.hasDiskimageDescription)) {
					String description = diskImageResource
							.getProperty(Omn_domain_pc.hasDiskimageDescription)
							.getObject().asLiteral().getString();
					diskImage.setDescription(description);
				}

				if (diskImageResource.hasProperty(Omn_domain_pc.hasDiskimageOS)) {
					String os = diskImageResource
							.getProperty(Omn_domain_pc.hasDiskimageOS)
							.getObject().asLiteral().getString();
					diskImage.setOs(os);
				}

				if (diskImageResource
						.hasProperty(Omn_domain_pc.hasDiskimageVersion)) {
					String version = diskImageResource
							.getProperty(Omn_domain_pc.hasDiskimageVersion)
							.getObject().asLiteral().getString();
					diskImage.setVersion(version);
				}

				if (diskImageResource
						.hasProperty(Omn_domain_pc.hasDiskimageURI)) {
					String uri = diskImageResource
							.getProperty(Omn_domain_pc.hasDiskimageURI)
							.getObject().asLiteral().getString();
					diskImage.setUrl(uri);
				}

				if (diskImageResource
						.hasProperty(Omn_domain_pc.diskimageDefault)) {
					String diskimageDefault = diskImageResource
							.getProperty(Omn_domain_pc.diskimageDefault)
							.getObject().asLiteral().getString();
					diskImage.setDefault(diskimageDefault);
				}

				diskImage.setName(diskName);
				sliver.getAnyOrDiskImage().add(
						of.createNodeContentsSliverTypeDiskImage(diskImage));
			}
		}
	}

	private void setHardwareTypes(Statement omnResource, NodeContents geniNode) {

		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(
				Omn_resource.hasHardwareType);

		while (types.hasNext()) {
			HardwareTypeContents hwType;
			Resource hwObject = types.next().getObject().asResource();
			String hwName = hwObject.getProperty(RDFS.label).getObject()
					.asLiteral().getString();

			hwType = of.createHardwareTypeContents();
			hwType.setName(hwName);

			// add emulab node slots
			if (hwObject.hasProperty(Omn_domain_pc.hasEmulabNodeTypeSlots)) {
				NodeType nodeType = of.createNodeType();
				// type slots is required
				String numSlots = hwObject
						.getProperty(Omn_domain_pc.hasEmulabNodeTypeSlots)
						.getObject().asLiteral().getString();
				nodeType.setTypeSlots(numSlots);

				if (hwObject.hasProperty(Omn_domain_pc.emulabNodeTypeStatic)) {
					String staticType = hwObject
							.getProperty(Omn_domain_pc.emulabNodeTypeStatic)
							.getObject().asLiteral().getString();
					nodeType.setStatic(staticType);
				}
				hwType.getAny().add(nodeType);
			}

			geniNodeDetails.add(of.createHardwareType(hwType));
		}
	}

	private void setComponentDetails(final Statement resource,
			final NodeContents node) {

		String url = resource.getResource().getURI();
		String urn = CommonMethods.generateUrnFromUrl(url, "node");

		node.setComponentId(urn);

		if (resource.getResource().hasProperty(Omn_lifecycle.hasComponentName)) {
			node.setComponentName(resource.getResource()
					.getProperty(Omn_lifecycle.hasComponentName).getObject()
					.asLiteral().getString());
		} else {
			node.setComponentName(resource.getResource().getLocalName());
		}

		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}
	}

	private void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		try {
			Statement managedBy = resource.getProperty(Omn_lifecycle.managedBy);
			node.setComponentManagerId(managedBy.getResource().getURI());
		} catch (PropertyNotFoundException e) {
			AdvertisementConverter.LOG.finer(e.getMessage());
		}
	}

	private void setTimeInformation(final RSpecContents manifest) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar = null;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
		} catch (final DatatypeConfigurationException e) {
			AdvertisementConverter.LOG.info(e.getMessage());
		}
		manifest.setGenerated(xmlGrogerianCalendar);
		manifest.setExpires(xmlGrogerianCalendar);
	}

	public static String toString(JAXBElement<RSpecContents> advertisement)
			throws JAXBException {
		return toString(advertisement, JAXB);
	}

}
