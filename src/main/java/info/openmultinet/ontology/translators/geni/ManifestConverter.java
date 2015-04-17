package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.User;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.transform.stream.StreamSource;

import org.apache.xerces.dom.ElementNSImpl;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ManifestConverter extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(ManifestConverter.class
			.getName());

	public static String getRSpec(final Model model, String hostname)
			throws JAXBException, InvalidModelException {
		final RSpecContents manifest = new RSpecContents();
		manifest.setType(RspecTypeContents.MANIFEST);
		manifest.setGeneratedBy(AbstractConverter.VENDOR);
		ManifestConverter.setGeneratedTime(manifest);

		ManifestConverter.model2rspec(model, manifest, hostname);
		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(manifest);
		return AbstractConverter.toString(rspec,
				"info.openmultinet.ontology.translators.geni.jaxb.manifest");
	}

	private static void setGeneratedTime(final RSpecContents manifest) {
		final GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance()
					.newXMLGregorianCalendar(gregorianCalendar);
			manifest.setGenerated(xmlGrogerianCalendar);
		} catch (final DatatypeConfigurationException e) {
			ManifestConverter.LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(final Model model,
			final RSpecContents manifest, String hostname)
			throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Topology).toList();
		AbstractConverter.validateModel(groups);

		final Resource group = groups.iterator().next();
		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		ManifestConverter.convertStatementsToNodesAndLinks(manifest, resources,
				hostname);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents manifest, final List<Statement> resources,
			String hostname) {

		for (final Statement resource : resources) {
			final NodeContents node = new NodeContents();

			ManifestConverter.setComponentDetails(resource, node, hostname);
			// ManifestConverter.setComponentManagerId(resource, node);

			manifest.getAnyOrNodeOrLink().add(
					new ObjectFactory().createNode(node));
		}
	}

	private static void setComponentDetails(final Statement resource,
			final NodeContents node, String hostname) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource()
					.getProperty(Omn_lifecycle.hasID).getString());
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			RDFNode implementedBy = resource.getResource()
					.getProperty(Omn_lifecycle.implementedBy).getObject();

			node.setComponentId(implementedBy.toString());
			node.setComponentName(implementedBy.asNode().getLocalName());
		}
		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}

		// check if the statement has sliver type
		if (resource.getResource().hasProperty(RDF.type)) {

			RDFNode sliverNode = resource.getResource().getProperty(RDF.type)
					.getObject();
			Resource sliverResource = sliverNode.asResource();
			SliverType sliverName = new ObjectFactory()
					.createNodeContentsSliverType();

			if (sliverResource.hasProperty(RDFS.label)) {
				sliverName.setName(sliverResource.getProperty(RDFS.label)
						.getObject().toString());
			}

			// get resource
			if (sliverResource.hasProperty(Omn.hasResource)) {

				RDFNode resourceNode = sliverResource.getProperty(
						Omn.hasResource).getObject();
				Resource resourceResource = resourceNode.asResource();
				
				// check if the resource is a disk image
				if (resourceResource.hasProperty(RDF.type,
						Omn_domain_pc.DiskImage)) {

					String diskName = "";
					if (resourceResource
							.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
						diskName += resourceResource
								.getProperty(Omn_domain_pc.hasDiskimageLabel)
								.getObject().asLiteral().getString();
					}

					DiskImageContents diskImageContents = new ObjectFactory()
							.createDiskImageContents();
					if (diskName != "") {
						diskImageContents.setName(diskName);
					}
					JAXBElement<DiskImageContents> diskImage = new ObjectFactory()
							.createDiskImage(diskImageContents);
					sliverName.getAnyOrDiskImage().add(diskImage);
				}
			}
			
			JAXBElement<SliverType> sliver = new ObjectFactory()
					.createNodeContentsSliverType(sliverName);
			node.getAnyOrRelationOrLocation().add(sliver);

		}


		// check if the statement has the property hasService
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

			if (serviceResource.hasProperty(RDF.type, Omn_service.LoginService)) {
				// get authentication
				String authentication = "";
				if (serviceResource.hasProperty(Omn_service.authentication)) {
					authentication += serviceResource
							.getProperty(Omn_service.authentication)
							.getObject().asLiteral().getString();
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

				// get username
				String username = "";
				if (serviceResource.hasProperty(Omn_service.username)) {
					username += serviceResource
							.getProperty(Omn_service.username).getObject()
							.asLiteral().getString();
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
				if (username != "") {
					loginServiceContent.setUsername(username);
				}

				JAXBElement<LoginServiceContents> loginService = new ObjectFactory()
						.createLogin(loginServiceContent);
				serviceContents.getAnyOrLoginOrInstall().add(loginService);
			}

			if (serviceResource.hasProperty(RDF.type,
					Omn_service.ExecuteService)) {

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

			if (serviceResource.hasProperty(RDF.type,
					Omn_service.InstallService)) {
				

				// get install path
				String installPath = "";
				if (serviceResource.hasProperty(Omn_service.installPath)) {
					installPath += serviceResource.getProperty(Omn_service.installPath)
							.getObject().asLiteral().getString();
				}

				// get url
				String url = "";
				if (serviceResource.hasProperty(Omn_service.url)) {
					url += serviceResource.getProperty(Omn_service.url)
							.getObject().asLiteral().getString();
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
		if (serviceContents != null) {
			JAXBElement<ServiceContents> services = new ObjectFactory()
					.createServices(serviceContents);
			node.getAnyOrRelationOrLocation().add(services);
		}

		node.setSliverId(generateSliverID(hostname, resource.getResource()
				.getURI()));

	}

	public static String generateSliverID(String hostname, String uri) {
		try {
			return "urn:publicid:IDN+" + hostname + "+sliver+"
					+ URLEncoder.encode(uri, StandardCharsets.UTF_8.toString());
		} catch (UnsupportedEncodingException e) {
			return uri;
		}
	}

	public static String parseSliverID(String sliverID) {
		try {
			Pattern pattern = Pattern.compile("\\+sliver\\+(.*)");
			Matcher matcher = pattern.matcher(sliverID);
			if (matcher.find()) {
				return URLDecoder.decode(matcher.group(1),
						StandardCharsets.UTF_8.toString());
			} else {
				return sliverID;
			}
		} catch (UnsupportedEncodingException e) {
			return sliverID;
		}
	}

	private static void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		final List<Statement> implementedBy = resource.getResource()
				.listProperties(Omn_lifecycle.implementedBy).toList();
		for (final Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}

	public static Model getModel(final InputStream stream) throws JAXBException {
		final Model model = ManifestConverter.createModelTemplate();
		final RSpecContents manifest = ManifestConverter.getManifest(stream);

		ManifestConverter.convertManifest2Model(manifest, model);

		return model;
	}

	private static void convertManifest2Model(final RSpecContents manifest,
			final Model model) {

		Resource topology = model.getResource(AbstractConverter.NAMESPACE
				+ "manifest");

		for (Object o : manifest.getAnyOrNodeOrLink()) {
			if (o instanceof JAXBElement) {
				setDetails(model, topology, o);
			} else {
				ManifestConverter.LOG.log(Level.INFO, "Found unknown extsion: "
						+ o);
			}
		}

	}

	public static void setDetails(final Model model, Resource topology, Object o) {
		JAXBElement<?> element = (JAXBElement<?>) o;

		if (element.getDeclaredType().equals(NodeContents.class)) {
			NodeContents node = (NodeContents) element.getValue();

			final Resource omnResource = model
					.createResource(parseSliverID(node.getSliverId()));

			omnResource.addProperty(Omn_lifecycle.hasID, node.getClientId());

			for (Object nodeDetailObject : node.getAnyOrRelationOrLocation()) {
				if (nodeDetailObject instanceof JAXBElement) {
					JAXBElement<?> nodeDetailElement = (JAXBElement<?>) nodeDetailObject;
					if (nodeDetailElement.getDeclaredType().equals(
							NodeContents.SliverType.class)) {

						NodeContents.SliverType sliverType = (NodeContents.SliverType) nodeDetailElement
								.getValue();

						Resource sliver = model.createResource();
						sliver.addProperty(RDFS.label, sliverType.getName());
						List<Object> sliverContents = sliverType
								.getAnyOrDiskImage();

						for (int i = 0; i < sliverContents.size(); i++) {
							Object sliverObject = sliverContents.get(i);

							// check if disk_image
							if (((JAXBElement<?>) sliverObject)
									.getDeclaredType().equals(
											DiskImageContents.class)) {

								Resource diskImage = model.createResource();
								diskImage.addProperty(RDF.type,
										Omn_domain_pc.DiskImage);
								DiskImageContents diskImageContents = (DiskImageContents) ((JAXBElement<?>) sliverObject)
										.getValue();

								// add name info
								String name = diskImageContents.getName();
								diskImage.addLiteral(
										Omn_domain_pc.hasDiskimageLabel, name);
								sliver.addProperty(Omn.hasResource, diskImage);
							}

						}

						omnResource.addProperty(RDF.type, sliver);
						// omnResource.addProperty(RDF.type,
						// sliverType.getName());
					}

					// check if type is Services
					if (nodeDetailElement.getDeclaredType().equals(
							ServiceContents.class)) {

						// get value of the element
						ServiceContents service = (ServiceContents) nodeDetailElement
								.getValue();
						List<Object> services = service
								.getAnyOrLoginOrInstall();

						// iterate through the Services and add to model
						for (int i = 0; i < services.size(); i++) {

							Object serviceObject = services.get(i);
							Resource omnService = null;

							// if login service
							if (((JAXBElement<?>) serviceObject)
									.getDeclaredType().equals(
											LoginServiceContents.class)) {

								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type,
										Omn_service.LoginService);
								LoginServiceContents serviceValue = (LoginServiceContents) ((JAXBElement<?>) serviceObject)
										.getValue();

								// add authentication info
								String authentication = serviceValue
										.getAuthentication();
								omnService.addLiteral(
										Omn_service.authentication,
										authentication);

								// add hostname info
								String hostname = serviceValue.getHostname();
								omnService.addLiteral(Omn_service.hostname,
										hostname);

								// add port info
								String portString = serviceValue.getPort();
								int port = Integer.parseInt(portString);
								omnService.addLiteral(Omn_service.port, port);

								// add username info
								String username = serviceValue.getUsername();
								omnService.addLiteral(Omn_service.username,
										username);
							}

							// if execute service
							if (((JAXBElement<?>) serviceObject)
									.getDeclaredType().equals(
											ExecuteServiceContents.class)) {
								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type,
										Omn_service.ExecuteService);
								ExecuteServiceContents serviceValue = (ExecuteServiceContents) ((JAXBElement<?>) serviceObject)
										.getValue();

								// add command info
								String command = serviceValue.getCommand();
								omnService.addLiteral(Omn_service.command,
										command);

								// add shell info
								String shell = serviceValue.getShell();
								omnService
										.addLiteral(Omn_service.shell, shell);
							}

							// if install service
							if (((JAXBElement<?>) serviceObject)
									.getDeclaredType().equals(
											InstallServiceContents.class)) {
								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type,
										Omn_service.InstallService);
								InstallServiceContents serviceValue = (InstallServiceContents) ((JAXBElement<?>) serviceObject)
										.getValue();
								
								// add install path info
								String installPath = serviceValue.getInstallPath();
								omnService
										.addLiteral(Omn_service.installPath, installPath);
								
								// add url path info
								String url = serviceValue.getUrl();
								URI urlURI = URI.create(url);
								omnService
										.addLiteral(Omn_service.url, urlURI);
								
							}

							// add service to node
							if (omnService != null) {
								omnResource.addProperty(Omn.hasService,
										omnService);
							}
						}
					}
				} else {
					ManifestConverter.LOG.log(Level.INFO,
							"Found unknown extsion: " + nodeDetailObject);
				}
			}

			Resource foo = model.createResource(node.getComponentId());
			omnResource.addProperty(Omn_lifecycle.implementedBy, foo);

			topology.addProperty(Omn.hasResource, omnResource);
		}
	}

	public static RSpecContents getManifest(final InputStream stream)
			throws JAXBException {
		final JAXBContext context = JAXBContext
				.newInstance(RSpecContents.class);
		final Unmarshaller unmarshaller = context.createUnmarshaller();
		final JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(stream), RSpecContents.class);
		return rspec.getValue();
	}

	public static Model createModelTemplate() throws JAXBException {
		final Model model = ModelFactory.createDefaultModel();
		final Resource topology = model
				.createResource(AbstractConverter.NAMESPACE + "manifest");
		topology.addProperty(RDF.type, Omn_lifecycle.Manifest);
		return model;
	}
}
