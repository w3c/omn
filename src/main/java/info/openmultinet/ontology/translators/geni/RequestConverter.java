package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.request.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkPropertyContents;
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

				setSliverType(resource, node);
				setServices(resource, node);
				setMonitoringService(resource, node);
				setInterfaces(resource, node);
				setEmulabExtension(resource, node);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createNode(node));
			} else {

				final LinkContents link = new LinkContents();

				// setGeniSliverInfo(resource, link);
				setLinkDetails(resource, link);
				setInterfaceRefs(resource, link);

				request.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));
			}
		}
	}

	private static void setInterfaceRefs(Statement resource, LinkContents link) {
		// TODO Auto-generated method stub
		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (Statement interface1 : interfaces) {

			InterfaceRefContents newInterface = new ObjectFactory()
					.createInterfaceRefContents();
			// String clientId = interface1.getObject().asLiteral().getString();
			String clientId = interface1.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();

			newInterface.setClientId(clientId);
			link.getAnyOrPropertyOrLinkType().add(
					new ObjectFactory()
							.createLinkContentsInterfaceRef(newInterface));
		}

	}

	private static void setLinkDetails(Statement resource, LinkContents link) {
		// TODO Auto-generated method stub

		if (resource.getResource().hasProperty(Omn_resource.clientId)) {
			String clientId = resource.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();
			link.setClientId(clientId);
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

			InterfaceContents interfaceContents = null;
			Resource interfaceResource = interface1.getResource();

			if (interfaceResource.hasProperty(Omn_resource.clientId)) {
				if (interfaceContents == null) {
					interfaceContents = new InterfaceContents();
				}
				interfaceContents.setClientId(interfaceResource
						.getProperty(Omn_resource.clientId).getObject()
						.asLiteral().toString());
			}

			if (interfaceContents != null) {
				JAXBElement<InterfaceContents> interfaceJaxb = new ObjectFactory()
						.createInterface(interfaceContents);
				node.getAnyOrRelationOrLocation().add(interfaceJaxb);
			}
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
			// setExecutiveService(serviceResource, serviceContents);
			// setInstallService(serviceResource, serviceContents);

		}
		if (serviceContents != null) {
			JAXBElement<ServiceContents> services = new ObjectFactory()
					.createServices(serviceContents);
			node.getAnyOrRelationOrLocation().add(services);
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

				System.out.println(hasUri.getObject().asLiteral().getString());
				// String uri = hasUri.getObject().asResource().getURI()
				// .toString();
				String uri = hasUri.getObject().asLiteral().getString();
				monitoring.setUri(uri);

			}

			if (monitoringResource.hasProperty(RDF.type)) {
				Statement hasType = monitoringService.getResource()
						.getProperty(RDF.type);

				// String type = hasType.getObject().asResource().getURI()
				// .toString();
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

			// ********not present in Request RSpec xsd**************
			// get username
			// String username = "";
			// if (serviceResource.hasProperty(Omn_service.username)) {
			// username += serviceResource.getProperty(Omn_service.username)
			// .getObject().asLiteral().getString();
			// }

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

		final List<Statement> hasTypes = resource.getResource()
				.listProperties(RDF.type).toList();

		for (final Statement hasType : hasTypes) {
			RDFNode sliverNode = hasType.getObject();
			Resource sliverResource = sliverNode.asResource();

			if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
				SliverType sliverType = new ObjectFactory()
						.createNodeContentsSliverType();
				sliverType.setName(sliverNode.toString());
				JAXBElement<SliverType> sliver = new ObjectFactory()
						.createNodeContentsSliverType(sliverType);
				node.getAnyOrRelationOrLocation().add(sliver);
			}
		}
	}

	private static void setComponentDetails(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource()
					.getProperty(Omn_lifecycle.hasID).getString());
		}
	}

	// if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
	// RDFNode implementedBy = resource.getResource()
	// .getProperty(Omn_lifecycle.implementedBy).getObject();
	//
	// String urn = AbstractConverter.generateUrnFromUrl(
	// implementedBy.toString(), "node");
	//
	// node.setComponentId(urn);
	// node.setComponentName(implementedBy.asNode().getLocalName());
	// }
	//

	private static void setComponentId(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			RDFNode implementedBy = resource.getResource()
					.getProperty(Omn_lifecycle.implementedBy).getObject();

			node.setComponentId(CommonMethods.generateUrnFromUrl(
					implementedBy.toString(), "node"));

			if (implementedBy.asResource().hasProperty(RDFS.label)) {
				node.setComponentName(implementedBy.asResource()
						.getProperty(RDFS.label).getObject().asLiteral()
						.toString());
			} else {
				node.setComponentName(implementedBy.asNode().getLocalName());
			}

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

		RequestConverter.extractNodes(request, topology);

		NetworkTopologyExtractor.extractTopologyInformation(request, topology);

		return model;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void extractNodes(final RSpecContents request,
			final Resource topology) throws MissingRspecElementException {

		List<JAXBElement<NodeContents>> nodes;
		try {
			nodes = (List) request.getAnyOrNodeOrLink();

			for (final JAXBElement<NodeContents> nodeObject : nodes) {
				final NodeContents node = nodeObject.getValue();
				final Model model = topology.getModel();
				final Resource omnResource = model
						.createResource(AbstractConverter.NAMESPACE
								+ node.getClientId());

				List<Object> anyOrRelationOrLocation = node
						.getAnyOrRelationOrLocation();
				if (anyOrRelationOrLocation.size() > 0) {
					extractRelationOrLocation(model, omnResource,
							anyOrRelationOrLocation);
				} else {
					omnResource.addProperty(RDF.type, Omn_resource.Node);
				}

				if (node.getClientId() == null) {
					throw new MissingRspecElementException(
							"NodeContents > client_id");
				} else {
					omnResource.addProperty(Omn_lifecycle.hasID,
							node.getClientId());
					omnResource.addProperty(RDFS.label, node.getClientId());
				}

				Resource implementedBy = null;
				if (null != node.getComponentId()
						&& !node.getComponentId().isEmpty()) {

					implementedBy = model.createResource(CommonMethods
							.generateUrlFromUrn(node.getComponentId()));

					omnResource.addProperty(Omn_lifecycle.implementedBy,
							implementedBy);
					if (null != node.getComponentName()
							&& !node.getComponentName().isEmpty()) {
						implementedBy.addProperty(RDFS.label,
								node.getComponentName());
					}
				}
				omnResource.addProperty(Omn.isResourceOf, topology);
				if (null != node.isExclusive()) {
					omnResource.addProperty(Omn_resource.isExclusive,
							model.createTypedLiteral(node.isExclusive()));
				}

				if (node.getComponentManagerId() != null) {
					RDFNode manager = ResourceFactory.createResource(node
							.getComponentManagerId());
					omnResource.addProperty(Omn_lifecycle.managedBy, manager);
				}

				topology.addProperty(Omn.hasResource, omnResource);
				// todo: details such as sliver type
				// List<Object> details = node.getAnyOrRelationOrLocation();
			}
		} catch (final ClassCastException e) {
			RequestConverter.LOG.finer(e.getMessage());
		}
	}

	public static void extractRelationOrLocation(final Model model,
			final Resource omnResource, List<Object> anyOrRelationOrLocation)
			throws MissingRspecElementException {
		for (Object o : anyOrRelationOrLocation) {
			if (o instanceof JAXBElement) {
				JAXBElement element = (JAXBElement) o;
				if (element.getDeclaredType().equals(
						NodeContents.SliverType.class)) {
					NodeContents.SliverType sliverType = (NodeContents.SliverType) element
							.getValue();
					// omnResource.addProperty(RDF.type,
					// model.createResource(sliverType.getName()));

					// name is required by slivertype
					if (sliverType.getName() == null) {
						throw new MissingRspecElementException(
								"SliverTypeContents > name");
					}
					if (sliverType.getName().contains(":")) {
						omnResource.addProperty(RDF.type,
								model.createResource(sliverType.getName()));
					} else {
						omnResource
								.addProperty(
										RDF.type,
										model.createResource("http://open-multinet.info/example#"
												+ sliverType.getName()));
					}
				}
				if (element.getDeclaredType().equals(ServiceContents.class)) {
					ServiceContents serviceContents = (ServiceContents) element
							.getValue();
					for (Object service : serviceContents
							.getAnyOrLoginOrInstall()) {
						if (service instanceof JAXBElement) {
							JAXBElement serviceElement = (JAXBElement) service;
							extractInstallService(model, omnResource,
									serviceElement);
							extractExecuteService(model, omnResource,
									serviceElement);
							extractLoginService(model, omnResource,
									serviceElement);
						}
					}
				}
			} else {
				if (o.getClass()
						.equals(info.openmultinet.ontology.translators.geni.jaxb.request.Monitoring.class)) {

					Monitoring monitor = (Monitoring) o;
					Resource monitoringResource = model.createResource(UUID
							.randomUUID().toString());
					if (monitor.getUri() != null && monitor.getUri() != "") {
						monitoringResource.addProperty(Omn_service.hasURI,
								monitor.getUri());
					}
					if (monitor.getType() != null && monitor.getType() != "") {
						monitoringResource.addProperty(RDF.type,
								monitor.getType());
						monitoringResource.addProperty(RDFS.label,
								AbstractConverter.getName(monitor.getType()));
					}
					omnResource.addProperty(Omn_lifecycle.usesService,
							monitoringResource);
				}
				if (o.getClass()
						.equals(info.openmultinet.ontology.translators.geni.jaxb.request.RoutableControlIp.class)) {
					omnResource.addProperty(Omn_domain_pc.routableControlIp,
							"true");
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

	private static class NetworkTopologyExtractor {

		static void extractTopologyInformation(final RSpecContents request,
				final Resource topology) throws MissingRspecElementException {

			List<JAXBElement<NodeContents>> xmlElements;
			Model outputModel = topology.getModel();

			try {
				xmlElements = (List) request.getAnyOrNodeOrLink();
				for (JAXBElement element : xmlElements) {

					// If it's a node, then extract the node information and its
					// corresponding interfaces
					if (element.getDeclaredType() == NodeContents.class) {
						JAXBElement<NodeContents> nodeObject = (JAXBElement<NodeContents>) element;

						NodeContents node = nodeObject.getValue();

						Resource omnResource = outputModel
								.createResource("http://open-multinet.info/example#"
										+ node.getClientId());
						// omnResource.addProperty(RDF.type, Nml.Node);
						omnResource.addProperty(RDF.type, Omn_resource.Node);

						List<Object> interfaces = node
								.getAnyOrRelationOrLocation();

						for (Object interfaceContentObject : interfaces) {
							try {
								@SuppressWarnings("unchecked")
								JAXBElement<InterfaceContents> interfaceContent = (JAXBElement<InterfaceContents>) interfaceContentObject;
								InterfaceContents content = interfaceContent
										.getValue();
								Resource interfaceResource = outputModel
										.createResource("http://open-multinet.info/example#"
												+ content.getClientId());
								// interfaceResource.addProperty(RDF.type,
								// Nml.Port);
								// omnResource.addProperty(Nml.hasPort,
								// interfaceResource);

								interfaceResource.addProperty(RDF.type,
										Omn_resource.Interface);
								omnResource.addProperty(
										Omn_resource.hasInterface,
										interfaceResource);
								if (content.getClientId() != null) {
									interfaceResource.addProperty(
											Omn_resource.clientId,
											content.getClientId());
								}
							} catch (ClassCastException exp) {

							}
						}
					}
					// If it's a link, then extract the link information
					// and its corresponding interfaces
					else if (element.getDeclaredType() == LinkContents.class) {

						JAXBElement<LinkContents> linkObject = (JAXBElement<LinkContents>) element;
						LinkContents link = linkObject.getValue();

						if (link.getClientId() == null) {
							throw new MissingRspecElementException(
									"LinkContents > client_id ");
						}
						Resource linkResource = outputModel
								.createResource("http://open-multinet.info/example#"
										+ link.getClientId());
						// linkResource.addProperty(RDF.type, Nml.Link);
						linkResource.addProperty(RDF.type, Omn_resource.Link);

						linkResource.addLiteral(Omn_resource.clientId,
								link.getClientId()); // required

						// Get source and sink interfaces
						@SuppressWarnings({ "unchecked", "rawtypes" })
						List<JAXBElement<InterfaceRefContents>> interfaces = (List) link
								.getAnyOrPropertyOrLinkType();
						for (JAXBElement<InterfaceRefContents> interfaceRefContents : interfaces) {
							try {
								InterfaceRefContents content = interfaceRefContents
										.getValue();
								Resource interfaceResource = outputModel
										.createResource("http://open-multinet.info/example#"
												+ content.getClientId());
								// interfaceResource.addProperty(Nml.isSink,
								// linkResource);
								// interfaceResource.addProperty(Nml.isSource,
								// linkResource);

								// interfaceResource.addProperty(
								// Omn_resource.isSink, linkResource);
								// interfaceResource.addProperty(
								// Omn_resource.isSource, linkResource);
								interfaceResource.addProperty(
										Omn_resource.clientId,
										content.getClientId());
								// linkResource.addProperty(Nml.hasPort,
								// interfaceResource);
								linkResource.addProperty(
										Omn_resource.hasInterface,
										interfaceResource);
							} catch (ClassCastException exp) {

							}
						}

						// Get source and sink interfaces
						@SuppressWarnings({ "unchecked", "rawtypes" })
						List<JAXBElement<LinkPropertyContents>> properties = (List) link
								.getAnyOrPropertyOrLinkType();
						for (JAXBElement<LinkPropertyContents> propertyContents : properties) {
							try {
								LinkPropertyContents content = propertyContents
										.getValue();

								// interfaceResource.addProperty(Nml.isSink,
								// linkResource);
								// interfaceResource.addProperty(Nml.isSource,
								// linkResource);

								// interfaceResource.addProperty(
								// Omn_resource.isSink, linkResource);
								// interfaceResource.addProperty(
								// Omn_resource.isSource, linkResource);
								// interfaceResource.addProperty(
								// Omn_resource.clientId,
								// content.getClientId());
								// linkResource.addProperty(Nml.hasPort,
								// interfaceResource);
								// linkResource.addProperty(
								// Omn_resource.hasInterface,
								// interfaceResource);
							} catch (ClassCastException exp) {

							}
						}
						topology.addProperty(Omn.hasResource, linkResource);
					}
				}
			} catch (ClassCastException e) {
				LOG.warning(e.getMessage());
			}
		}
	}

}
