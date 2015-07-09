package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.Available;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkSharedVlan;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.request.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.request.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.request.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.Monitoring;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.request.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RoutableControlIp;
import info.openmultinet.ontology.translators.geni.jaxb.request.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ServiceContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.InvalidPropertyURIException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RequestConverter extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";
	private static final Logger LOG = Logger.getLogger(RequestConverter.class
			.getName());

	public static String getRSpec(final Model model) throws JAXBException,
			InvalidModelException, MissingRspecElementException {
		final RSpecContents request = new RSpecContents();
		request.setType(RspecTypeContents.REQUEST);
		request.setGeneratedBy(AbstractConverter.VENDOR);
		RequestConverter.setGeneratedTime(request);

		RequestConverter.model2rspec(model, request);

		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(request);
		return AbstractConverter.toString(rspec, RequestConverter.JAXB);
	}

	private static void setGeneratedTime(final RSpecContents request) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
			request.setGenerated(xmlGrogerianCalendar);
		} catch (final DatatypeConfigurationException e) {
			RequestConverter.LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(final Model model,
			final RSpecContents request) throws InvalidModelException,
			MissingRspecElementException {
		List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		if (groups.size() == 0) {
			groups = model.listSubjectsWithProperty(RDF.type, Omn.Topology)
					.toList();
		}
		AbstractConverter.validateModel(groups);

		final Resource group = groups.iterator().next();
		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		RequestConverter.convertStatementsToNodesAndLinks(request, resources);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents request, final List<Statement> resources)
			throws MissingRspecElementException {

		for (final Statement resource : resources) {
			if (!resource.getResource()
					.hasProperty(RDF.type, Omn_resource.Link)) {

				final NodeContents node = new NodeContents();

				RequestConverter.setComponentDetails(resource, node);
				RequestConverter.setComponentId(resource, node);
				if (resource.getResource()
						.hasProperty(Omn_resource.isExclusive)) {
					final boolean isExclusive = resource.getResource()
							.getProperty(Omn_resource.isExclusive).getBoolean();
					node.setExclusive(isExclusive);
				}

				setInterfaces(resource, node);
				setSliverType(resource, node);
				setServices(resource, node);
				setHardwareTypes(resource, node);
				setMonitoringService(resource, node);
				setEmulabExtension(resource, node);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createNode(node));
			} else {

				final LinkContents link = new LinkContents();

				// setGeniSliverInfo(resource, link);
				setLinkDetails(resource, link);
				setInterfaceRefs(resource, link);
				setLinkProperties(resource, link);
				setSharedVlan(resource, link);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));
			}
		}
	}

	private static void setSharedVlan(Statement resource, LinkContents link) {

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
					String vlanTag = object.getProperty(Omn_domain_pc.vlanTag).getObject()
							.asLiteral().getString();
					sharedVlanRspec.setVlantag(vlanTag);
				}
				link.getAnyOrPropertyOrLinkType().add(sharedVlanRspec);
			}
		}
	}

	private static void setHardwareTypes(Statement omnResource,
			NodeContents geniNode) {

		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(
				Omn_domain_pc.hasHardwareType);
		ObjectFactory of = new ObjectFactory();

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
				String numSlots = hwObject
						.getProperty(Omn_domain_pc.hasEmulabNodeTypeSlots)
						.getObject().asLiteral().getString();
				nodeType.setTypeSlots(numSlots);
				hwType.getAny().add(nodeType);
			}
			geniNodeDetails.add(of.createHardwareType(hwType));
		}
	}

	private static void setLinkProperties(Statement resource, LinkContents link) {
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

			link.getAnyOrPropertyOrLinkType().add(
					new ObjectFactory().createProperty(newLinkProperty));
		}

	}

	private static void setInterfaceRefs(Statement resource, LinkContents link) {
		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (Statement interface1 : interfaces) {

			InterfaceRefContents newInterface = new ObjectFactory()
					.createInterfaceRefContents();

			String clientId = interface1.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();

			newInterface.setClientId(clientId);

			JAXBElement<InterfaceRefContents> interfaceJaxb = new ObjectFactory()
					.createLinkContentsInterfaceRef(newInterface);

			link.getAnyOrPropertyOrLinkType().add(interfaceJaxb);
		}

	}

	private static void setIpAddress(Resource interfaceResource,
			InterfaceContents interfaceContents) {

		IpContents ipContents = null;
		if (interfaceResource.hasProperty(Omn_resource.hasIPAddress)) {
			Statement ipAddress = interfaceResource
					.getProperty(Omn_resource.hasIPAddress);
			if (ipAddress.getResource().hasProperty(Omn_resource.type)) {
				ipContents = new ObjectFactory().createIpContents();
				ipContents.setType(ipAddress.getResource()
						.getProperty(Omn_resource.type).getObject().asLiteral()
						.getString());
			}

			if (ipAddress.getResource().hasProperty(Omn_resource.netmask)) {
				if (ipContents == null) {
					ipContents = new ObjectFactory().createIpContents();
				}
				ipContents.setNetmask(ipAddress.getResource()
						.getProperty(Omn_resource.netmask).getObject()
						.asLiteral().getString());
			}

			if (ipAddress.getResource().hasProperty(Omn_resource.address)) {
				if (ipContents == null) {
					ipContents = new ObjectFactory().createIpContents();
				}
				ipContents.setAddress(ipAddress.getResource()
						.getProperty(Omn_resource.address).getObject()
						.asLiteral().getString());
			}
		}

		if (ipContents != null) {
			JAXBElement<info.openmultinet.ontology.translators.geni.jaxb.request.IpContents> ipJaxb = new ObjectFactory()
					.createIp(ipContents);
			interfaceContents.getAnyOrIp().add(ipJaxb);
		}
	}

	private static void setLinkDetails(Statement resource, LinkContents link) {
		if (resource.getResource().hasProperty(Omn_resource.clientId)) {
			String clientId = resource.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();
			link.setClientId(clientId);
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.hasLinkName)) {
			String linkName = resource.getResource()
					.getProperty(Omn_lifecycle.hasLinkName).getObject()
					.asLiteral().getString();
			LinkType linkType = new ObjectFactory().createLinkType();
			linkType.setName(linkName);
			link.getAnyOrPropertyOrLinkType().add(linkType);
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

	private static void setEmulabExtension(Statement resource, NodeContents node) {

		Resource resourceResource = resource.getResource();

		// checks if routableControlIp property present
		// it's a boolean property, so if present add to RSpec
		if (resourceResource.hasProperty(Omn_domain_pc.routableControlIp)) {
			RoutableControlIp routableControlIp = new ObjectFactory()
					.createRoutableControlIp();
			node.getAnyOrRelationOrLocation().add(routableControlIp);
		}
	}

	private static void setInterfaces(Statement resource, NodeContents node) {

		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (final Statement interface1 : interfaces) {
			InterfaceContents interfaceContents = new ObjectFactory()
					.createInterfaceContents();
			Resource interfaceResource = interface1.getResource();

			if (interfaceResource.hasProperty(Omn_resource.clientId)) {
				interfaceContents.setClientId(interfaceResource
						.getProperty(Omn_resource.clientId).getObject()
						.asLiteral().toString());
			}

			setIpAddress(interfaceResource, interfaceContents);

			JAXBElement<InterfaceContents> interfaceRspec = new ObjectFactory()
					.createInterface(interfaceContents);
			node.getAnyOrRelationOrLocation().add(interfaceRspec);
		}
	}

	private static void setServices(Statement resource, NodeContents node)
			throws MissingRspecElementException {

		ServiceContents serviceContents = null;
		while (resource.getResource().hasProperty(Omn.hasService)) {

			if (serviceContents == null) {
				serviceContents = new ObjectFactory().createServiceContents();
			}
			// get the object resource of this relation
			Statement hasService = resource.getResource().getProperty(
					Omn.hasService);
			hasService.remove();
			RDFNode service = hasService.getObject();
			Resource serviceResource = service.asResource();

			setLoginService(serviceResource, serviceContents);
			setExecutiveService(serviceResource, serviceContents);
			setInstallService(serviceResource, serviceContents);

		}
		if (serviceContents != null) {
			JAXBElement<ServiceContents> services = new ObjectFactory()
					.createServices(serviceContents);
			node.getAnyOrRelationOrLocation().add(services);
		}

	}

	private static void setExecutiveService(Resource serviceResource,
			ServiceContents serviceContents) {
		if (serviceResource.hasProperty(RDF.type, Omn_service.ExecuteService)) {

			// get command
			String command = "";
			if (serviceResource.hasProperty(Omn_service.command)) {
				command += serviceResource.getProperty(Omn_service.command)
						.getObject().asLiteral().getString();
			}

			// get shell
			String shell = "";
			if (serviceResource.hasProperty(Omn_service.shell)) {
				shell += serviceResource.getProperty(Omn_service.shell)
						.getObject().asLiteral().getString();
			}

			// create execute
			ExecuteServiceContents excuteServiceContent = new ObjectFactory()
					.createExecuteServiceContents();

			excuteServiceContent.setCommand(command); // required
			excuteServiceContent.setShell(shell); // required

			JAXBElement<ExecuteServiceContents> executeService = new ObjectFactory()
					.createExecute(excuteServiceContent);
			serviceContents.getAnyOrLoginOrInstall().add(executeService);
		}
	}

	private static void setInstallService(Resource serviceResource,
			ServiceContents serviceContents) {

		if (serviceResource.hasProperty(RDF.type, Omn_service.InstallService)) {

			// get install path
			String installPath = "";
			if (serviceResource.hasProperty(Omn_service.installPath)) {
				installPath += serviceResource
						.getProperty(Omn_service.installPath).getObject()
						.asLiteral().getString();
			}

			// get url
			String url = "";
			if (serviceResource.hasProperty(Omn_service.url)) {
				url += serviceResource.getProperty(Omn_service.url).getObject()
						.asLiteral().getString();
			}

			// create execute
			InstallServiceContents installServiceContent = new ObjectFactory()
					.createInstallServiceContents();

			installServiceContent.setInstallPath(installPath); // required
			installServiceContent.setUrl(url); // required

			JAXBElement<InstallServiceContents> installService = new ObjectFactory()
					.createInstall(installServiceContent);
			serviceContents.getAnyOrLoginOrInstall().add(installService);
		}
	}

	private static void setMonitoringService(Statement resource,
			NodeContents node) {

		Resource resourceResource = resource.getResource();
		if (resourceResource.hasProperty(Omn_lifecycle.usesService)) {
			Statement monitoringService = resourceResource
					.getProperty(Omn_lifecycle.usesService);
			Resource monitoringResource = monitoringService.getResource();
			Monitoring monitoring = new ObjectFactory().createMonitoring();

			if (monitoringResource.hasProperty(Omn_service.hasURI)) {
				Statement hasUri = monitoringService.getResource().getProperty(
						Omn_service.hasURI);
				String uri = hasUri.getObject().asLiteral().getString();
				monitoring.setUri(uri);

			}

			if (monitoringResource.hasProperty(RDF.type)) {
				Statement hasType = monitoringService.getResource()
						.getProperty(RDF.type);
				String type = hasType.getObject().asLiteral().getString();
				monitoring.setType(type);
			}

			node.getAnyOrRelationOrLocation().add(monitoring);
		}
	}

	private static void setLoginService(Resource serviceResource,
			ServiceContents serviceContents)
			throws MissingRspecElementException {
		if (serviceResource.hasProperty(RDF.type, Omn_service.LoginService)) {

			// get authentication
			String authentication = "";
			if (serviceResource.hasProperty(Omn_service.authentication)) {
				authentication += serviceResource
						.getProperty(Omn_service.authentication).getObject()
						.asLiteral().getString();
			} else {
				throw new MissingRspecElementException(
						"LoginServiceContents > authentication");
			}

			if (serviceResource.hasProperty(Omn_service.publickey)) {
				authentication += serviceResource
						.getProperty(Omn_service.publickey).getObject()
						.asLiteral().getString();
			}

			// get hostname
			String hostnameLogin = "";
			if (serviceResource.hasProperty(Omn_service.hostname)) {
				hostnameLogin += serviceResource
						.getProperty(Omn_service.hostname).getObject()
						.asLiteral().getString();
			}

			// get port
			String port = "";
			if (serviceResource.hasProperty(Omn_service.port)) {
				port += serviceResource.getProperty(Omn_service.port)
						.getObject().asLiteral().getString();
			}

			// create login
			LoginServiceContents loginServiceContent = new ObjectFactory()
					.createLoginServiceContents();

			loginServiceContent.setAuthentication(authentication); // required

			if (hostnameLogin != "") {
				loginServiceContent.setHostname(hostnameLogin);
			}
			if (port != "") {
				loginServiceContent.setPort(port);
			}

			JAXBElement<LoginServiceContents> loginService = new ObjectFactory()
					.createLogin(loginServiceContent);
			serviceContents.getAnyOrLoginOrInstall().add(loginService);
		}

	}

	private static void setSliverType(Statement resource, NodeContents node) {
		SliverType sliverType = new ObjectFactory()
				.createNodeContentsSliverType();
		Resource omnSliver = null;

		final List<Statement> hasTypes = resource.getResource()
				.listProperties(RDF.type).toList();

		for (final Statement hasType : hasTypes) {
			RDFNode sliverNode = hasType.getObject();
			Resource sliverResource = sliverNode.asResource();

			if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
				omnSliver = sliverResource;
				sliverType.setName(sliverResource.getURI());
			}
		}

		// check if name was string and not uri
		if (resource.getResource().hasProperty(Omn_lifecycle.hasSliverName)) {
			String sliverName = resource.getResource()
					.getProperty(Omn_lifecycle.hasSliverName).getObject()
					.asLiteral().getString();
			sliverType.setName(sliverName);
		}

		if (omnSliver != null) {
			setDiskImage(omnSliver, sliverType);
		}

		JAXBElement<SliverType> sliver = new ObjectFactory()
				.createNodeContentsSliverType(sliverType);
		node.getAnyOrRelationOrLocation().add(sliver);
	}

	private static void setDiskImage(Resource resource, SliverType sliver) {
		List<Statement> diskImages = resource.listProperties(
				Omn_domain_pc.hasDiskImage).toList();

		for (Statement diskImageStatement : diskImages) {
			DiskImageContents diskImageContents = new ObjectFactory()
					.createDiskImageContents();
			Resource linkResource = diskImageStatement.getResource();

			// set name
			if (linkResource.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
				String diskImageName = linkResource
						.getProperty(Omn_domain_pc.hasDiskimageLabel)
						.getObject().asLiteral().getString();
				diskImageContents.setName(diskImageName);
			}

			// set version
			if (linkResource.hasProperty(Omn_domain_pc.hasDiskimageVersion)) {
				String diskImageVersion = linkResource
						.getProperty(Omn_domain_pc.hasDiskimageVersion)
						.getObject().asLiteral().getString();
				diskImageContents.setVersion(diskImageVersion);
			}

			sliver.getAnyOrDiskImage().add(
					new ObjectFactory().createDiskImage(diskImageContents));
		}

	}

	private static void setComponentDetails(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource()
					.getProperty(Omn_lifecycle.hasID).getString());
		}
	}

	private static void setComponentId(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			RDFNode implementedBy = resource.getResource()
					.getProperty(Omn_lifecycle.implementedBy).getObject();

			String componentId = implementedBy.toString();
			if (AbstractConverter.isUrn(componentId)) {
				node.setComponentId(componentId);
			} else {
				// TODO: add these lines in to remove error in RSpec round trips
				// if (resource.getResource().hasProperty(
				// Omn_lifecycle.hasOriginalID)) {
				// node.setComponentId(resource.getResource()
				// .getProperty(Omn_lifecycle.hasOriginalID)
				// .getObject().asLiteral().getString());
				// } else {
				node.setComponentId(CommonMethods.generateUrnFromUrl(
						componentId, "node"));
			}
			// }

			if (implementedBy.asResource().hasProperty(
					Omn_lifecycle.hasComponentName)) {
				String componentName = implementedBy.asResource()
						.getProperty(Omn_lifecycle.hasComponentName)
						.getObject().asLiteral().getString();
				node.setComponentName(componentName);
			}
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.managedBy)) {
			Statement managedBy = resource.getProperty(Omn_lifecycle.managedBy);
			node.setComponentManagerId(CommonMethods.generateUrnFromUrl(
					managedBy.getResource().getURI(), "authority"));
		}
	}

	public static Model getModel(final InputStream input) throws JAXBException,
			InvalidModelException, MissingRspecElementException {

		final JAXBContext context = JAXBContext
				.newInstance(RSpecContents.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		final RSpecContents request = rspec.getValue();

		final Model model = ModelFactory.createDefaultModel();
		final Resource topology = model
				.createResource(AbstractConverter.NAMESPACE + "request");
		topology.addProperty(RDF.type, Omn_lifecycle.Request);
		topology.addProperty(RDFS.label, Omn_lifecycle.Request);
		topology.addProperty(RDF.type, Omn.Topology);

		// RequestConverter.extractNodes(request, topology);
		// RequestConverter.extractLinks(request, topology);

		for (Object o : request.getAnyOrNodeOrLink()) {
			if (o instanceof JAXBElement) {
				extractDetails(model, topology, o);
			} else {
				RequestConverter.LOG.log(Level.INFO,
						"Found unknown extension: " + o);
			}
		}

		// NetworkTopologyExtractor.extractTopologyInformation(request,
		// topology);

		return model;
	}

	private static void extractDetails(Model model, Resource topology, Object o)
			throws MissingRspecElementException {
		JAXBElement<?> element = (JAXBElement<?>) o;
		if (element.getDeclaredType().equals(NodeContents.class)) {
			extractNodes(element, topology);
		} else if (element.getDeclaredType().equals(LinkContents.class)) {
			extractLinks(element, topology);
		} else {
			RequestConverter.LOG.log(Level.INFO, "Found unknown extension: "
					+ o);
		}

	}

	@SuppressWarnings("unchecked")
	private static void extractLinks(JAXBElement<?> element,
			final Resource topology) throws MissingRspecElementException {

		final LinkContents link = (LinkContents) element.getValue();
		final Model outputModel = topology.getModel();

		if (link.getClientId() == null) {
			throw new MissingRspecElementException("LinkContents > client_id ");
		}
		final Resource linkResource = outputModel
				.createResource(AbstractConverter.NAMESPACE
						+ link.getClientId());

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
				tryExtractSharedVlan(o, linkResource);
			} else {
				RequestConverter.LOG.log(Level.INFO,
						"Found unknown link extension: " + o);
			}
		}

		linkResource.addProperty(RDF.type, Omn_resource.Link);

		linkResource.addProperty(Omn.isResourceOf, topology);
		topology.addProperty(Omn.hasResource, linkResource);

	}

	private static void tryExtractSharedVlan(Object rspecObject,
			Resource offering) throws MissingRspecElementException {

		final LinkSharedVlan vlan = (LinkSharedVlan) rspecObject;
		Resource sharedVlan = offering.getModel().createResource();
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

	private static void extractComponentManager(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final ComponentManager content = (ComponentManager) o;

		// name required
		if (content.getName() == null) {
			throw new MissingRspecElementException("component_manager > name");
		}
		linkResource.addProperty(Omn_lifecycle.hasComponentManagerName,
				content.getName());

	}

	private static void extractLinkType(Object o, Resource linkResource)
			throws MissingRspecElementException {

		final LinkType content = (LinkType) o;

		// name required
		if (content.getName() == null) {
			throw new MissingRspecElementException("link_type > name");
		}
		linkResource.addProperty(Omn_lifecycle.hasLinkName, content.getName());

		// class optional
		// TODO

	}

	private static void extractInterfaceRefs(JAXBElement<?> linkElement,
			Resource linkResource) {

		final InterfaceRefContents content = (InterfaceRefContents) linkElement
				.getValue();

		Resource interfaceResource = linkResource.getModel().createResource(
				"http://open-multinet.info/example#" + content.getClientId());
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

		Resource linkPropertyResource = linkResource.getModel()
				.createResource();
		linkPropertyResource.addProperty(RDF.type, Omn_resource.LinkProperty);
		linkPropertyResource.addProperty(Omn_resource.hasSink, destID);
		linkPropertyResource.addProperty(Omn_resource.hasSource, sourceID);

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
	private static void extractNodes(JAXBElement<?> element,
			final Resource topology) throws MissingRspecElementException {

		final NodeContents node = (NodeContents) element.getValue();
		final Model model = topology.getModel();
		final Resource omnResource = model
				.createResource(AbstractConverter.NAMESPACE
						+ node.getClientId());
		omnResource.addProperty(RDFS.label, node.getClientId());
		List<Object> anyOrRelationOrLocation = node
				.getAnyOrRelationOrLocation();
		if (anyOrRelationOrLocation.size() > 0) {
			extractRelationOrLocation(model, omnResource,
					anyOrRelationOrLocation);
		}
		omnResource.addProperty(RDF.type, Omn_resource.Node);

		extractNodeDetails(omnResource, node);

		omnResource.addProperty(Omn.isResourceOf, topology);
		topology.addProperty(Omn.hasResource, omnResource);

	}

	private static void tryExtractInterfaces(Resource omnResource,
			JAXBElement element) throws MissingRspecElementException {

		if (element.getDeclaredType().equals(InterfaceContents.class)) {
			Model outputModel = omnResource.getModel();
			InterfaceContents content = (InterfaceContents) element.getValue();
			Resource interfaceResource = outputModel
					.createResource("http://open-multinet.info/example#"
							+ content.getClientId());
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
		}
	}

	private static void tryExtractIPAddress(Object interfaceObject,
			Resource interfaceResource) throws MissingRspecElementException {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<IpContents> availablityJaxb = (JAXBElement<IpContents>) interfaceObject;
			final IpContents availability = availablityJaxb.getValue();

			Resource omnIpAddress = interfaceResource.getModel()
					.createResource();
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
			RequestConverter.LOG.finer(e.getMessage());
		}

	}

	private static void extractNodeDetails(Resource omnResource,
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
			if (AbstractConverter.isUrl(componentId)) {
				implementedBy = model.createResource(componentId);
				// TODO: add this line get rid of differences in
				// component ID for an RSpec round trip
				// omnResource.addProperty(Omn_lifecycle.hasOriginalID,
				// componentId);
			} else {
				implementedBy = model.createResource(CommonMethods
						.generateUrlFromUrn(componentId));
			}

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
			omnResource.addProperty(Omn_lifecycle.managedBy, manager);
		}

	}

	public static void extractRelationOrLocation(final Model model,
			final Resource omnResource, List<Object> anyOrRelationOrLocation)
			throws MissingRspecElementException {
		for (Object o : anyOrRelationOrLocation) {
			if (o instanceof JAXBElement) {
				JAXBElement element = (JAXBElement) o;
				tryExtractSliverType(omnResource, element);
				tryExtractServices(omnResource, element);
				tryExtractInterfaces(omnResource, element);
				tryExtractHardwareType(omnResource, element);
			} else {
				tryExtractMonitoring(omnResource, o);
				if (o.getClass().equals(RoutableControlIp.class)) {
					omnResource.addProperty(Omn_domain_pc.routableControlIp,
							"true");
				}
			}
		}
	}

	private static void tryExtractHardwareType(Resource omnNode,
			JAXBElement element) {
		if (element.getDeclaredType().equals(HardwareTypeContents.class)) {

			final HardwareTypeContents hw = (HardwareTypeContents) element
					.getValue();

			final Resource omnHw = omnNode.getModel().createResource();
			RDFNode type = ResourceFactory.createProperty(hw.getName());

			// TODO: get rid of this line
			omnNode.addProperty(RDF.type, type);

			omnHw.addProperty(RDFS.label, type.toString());
			omnHw.addProperty(RDF.type, Omn_domain_pc.HardwareType);
			// for (Object hwObject : hw.getAny()) {
			// tryExtractEmulabNodeType(hwObject, omnHw);
			// }
			omnNode.addProperty(Omn_domain_pc.hasHardwareType, omnHw);

		}
	}

	private static void tryExtractMonitoring(Resource omnResource, Object o) {
		if (o.getClass().equals(Monitoring.class)) {

			Monitoring monitor = (Monitoring) o;
			Resource monitoringResource = omnResource.getModel()
					.createResource(UUID.randomUUID().toString());
			if (monitor.getUri() != null && monitor.getUri() != "") {
				monitoringResource.addProperty(Omn_service.hasURI,
						monitor.getUri());
			}
			if (monitor.getType() != null && monitor.getType() != "") {
				monitoringResource.addProperty(RDF.type, monitor.getType());
				monitoringResource.addProperty(RDFS.label,
						AbstractConverter.getName(monitor.getType()));
			}
			omnResource.addProperty(Omn_lifecycle.usesService,
					monitoringResource);
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
				sliverTypeResource = omnResource.getModel().createResource(
						sliverName);
				omnResource.addProperty(RDF.type, sliverTypeResource);
			} else {
				sliverTypeResource = omnResource.getModel().createResource(
						"http://open-multinet.info/example#" + sliverName);
				omnResource.addProperty(RDF.type, sliverTypeResource);
				omnResource
						.addProperty(Omn_lifecycle.hasSliverName, sliverName);
			}

			for (Object rspecSliverObject : sliverType.getAnyOrDiskImage()) {
				tryExtractDiskImage(rspecSliverObject, sliverTypeResource);
			}
		}
	}

	private static void tryExtractDiskImage(Object rspecSliverObject,
			Resource omnSliver) throws MissingRspecElementException {

		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<DiskImageContents> diJaxb = (JAXBElement<DiskImageContents>) rspecSliverObject;
			final DiskImageContents diskImageContents = diJaxb.getValue();

			String diskImageURL = diskImageContents.getUrl();
			Resource diskImage = omnSliver.getModel().createResource(
					diskImageURL);
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
				diskImage
						.addLiteral(Omn_domain_pc.hasDiskimageVersion, version);
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
					Resource diskImageResource = diskImageStatement.getObject()
							.asResource();
					if (diskImageResource
							.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
						String diskImageLabel = diskImageResource
								.getProperty(Omn_domain_pc.hasDiskimageLabel)
								.getObject().asLiteral().getString();
						if (diskImageLabel.equals(name)) {
							alreadyExists = true;
						}
					}
				}
			}

			if (!alreadyExists) {
				omnSliver.addProperty(Omn_domain_pc.hasDiskImage, diskImage);
			}
		} catch (final ClassCastException e) {
			RequestConverter.LOG.finer(e.getMessage());
		} catch (final InvalidPropertyURIException e) {
			RequestConverter.LOG.info(e.getMessage());
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

	// private static class NetworkTopologyExtractor {
	//
	// static void extractTopologyInformation(final RSpecContents request,
	// final Resource topology) throws MissingRspecElementException {
	//
	// List<JAXBElement<NodeContents>> xmlElements;
	// Model outputModel = topology.getModel();
	//
	// try {
	// xmlElements = (List) request.getAnyOrNodeOrLink();
	// for (JAXBElement element : xmlElements) {
	//
	// // If it's a node, then extract the node information and its
	// // corresponding interfaces
	// if (element.getDeclaredType() == NodeContents.class) {
	// JAXBElement<NodeContents> nodeObject =
	// (JAXBElement<NodeContents>) element;
	//
	// NodeContents node = nodeObject.getValue();
	//
	// String clientId = node.getClientId();
	// Resource omnResource = outputModel
	// .createResource("http://open-multinet.info/example#"
	// + clientId);
	//
	// // omnResource.addProperty(RDF.type, Nml.Node);
	// omnResource.addProperty(RDF.type, Omn_resource.Node);
	//
	// }
	// // If it's a link, then extract the link information
	// // and its corresponding interfaces
	// else if (element.getDeclaredType() == LinkContents.class) {
	//
	// JAXBElement<LinkContents> linkObject = (JAXBElement<LinkContents>)
	// element;
	// LinkContents link = linkObject.getValue();
	//
	// if (link.getClientId() == null) {
	// throw new MissingRspecElementException(
	// "LinkContents > client_id ");
	// }
	// Resource linkResource = outputModel
	// .createResource("http://open-multinet.info/example#"
	// + link.getClientId());
	// // linkResource.addProperty(RDF.type, Nml.Link);
	// linkResource.addProperty(RDF.type, Omn_resource.Link);
	//
	// linkResource.addLiteral(Omn_resource.clientId,
	// link.getClientId()); // required
	//
	// // Get source and sink interfaces
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// List<JAXBElement<InterfaceRefContents>> interfaces = (List) link
	// .getAnyOrPropertyOrLinkType();
	// for (JAXBElement<InterfaceRefContents> interfaceRefContents : interfaces)
	// {
	// try {
	// InterfaceRefContents content = interfaceRefContents
	// .getValue();
	// Resource interfaceResource = outputModel
	// .createResource("http://open-multinet.info/example#"
	// + content.getClientId());
	// // interfaceResource.addProperty(Nml.isSink,
	// // linkResource);
	// // interfaceResource.addProperty(Nml.isSource,
	// // linkResource);
	//
	// interfaceResource.addProperty(
	// Omn_resource.clientId,
	// content.getClientId());
	//
	// // linkResource.addProperty(Nml.hasPort,
	// // interfaceResource);
	// linkResource.addProperty(
	// Omn_resource.hasInterface,
	// interfaceResource);
	// } catch (ClassCastException exp) {
	//
	// }
	// }
	//
	// // Get source and sink interfaces
	// @SuppressWarnings({ "unchecked", "rawtypes" })
	// List<JAXBElement<LinkPropertyContents>> properties = (List) link
	// .getAnyOrPropertyOrLinkType();
	// for (JAXBElement<LinkPropertyContents> propertyContents : properties) {
	// try {
	// LinkPropertyContents content = propertyContents
	// .getValue();
	// String sourceID = content.getSourceId();
	// String destID = content.getDestId();
	//
	// if (sourceID == null || destID == null) {
	// throw new MissingRspecElementException(
	// "LinkPropertyContents > source_id/dest_id");
	// }
	//
	// Resource linkPropertyResource = outputModel
	// .createResource();
	// linkPropertyResource.addProperty(RDF.type,
	// Omn_resource.LinkProperty);
	// linkPropertyResource.addProperty(
	// Omn_resource.hasSink, destID);
	// linkPropertyResource.addProperty(
	// Omn_resource.hasSource, sourceID);
	//
	// linkResource.addProperty(
	// Omn_resource.hasProperty,
	// linkPropertyResource);
	// // interfaceResource.addProperty(Nml.isSink,
	// // linkResource);
	// // interfaceResource.addProperty(Nml.isSource,
	// // linkResource);
	// // linkResource.addProperty(Nml.hasPort,
	// // interfaceResource);
	// } catch (ClassCastException exp) {
	//
	// }
	// }
	// topology.addProperty(Omn.hasResource, linkResource);
	// }
	// }
	// } catch (ClassCastException e) {
	// LOG.warning(e.getMessage());
	// }
	// }
	// }

}
