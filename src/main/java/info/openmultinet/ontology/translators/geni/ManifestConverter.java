package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
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
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.io.UnsupportedEncodingException;
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

		int i = 0;
		for (final Statement resource : resources) {
			final NodeContents node = new NodeContents();

			ManifestConverter.setComponentDetails(resource, node, hostname);
			//ManifestConverter.setComponentManagerId(resource, node);

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

		if (resource.getResource().hasProperty(RDF.type)) {
			SliverType value = new ObjectFactory().createNodeContentsSliverType();
			value.setName(resource.getResource()
					.getProperty(RDF.type).getObject().toString());
			
			JAXBElement<SliverType> sliverType = new ObjectFactory().createNodeContentsSliverType(value );
			node.getAnyOrRelationOrLocation().add(sliverType);
		}
		
		// check if the statement has the property hasService 
		ServiceContents serviceContents = null;
		while (resource.getResource().hasProperty(Omn.hasService)) {
			
			if(serviceContents == null){ 
				serviceContents = new ObjectFactory().createServiceContents();
			}
			// get the object resource of this relation
			Statement hasService = resource.getResource().getProperty(Omn.hasService);
			hasService.remove();
			RDFNode service = hasService.getObject();			
			Resource serviceResource = service.asResource();

			if(serviceResource.hasProperty(RDF.type,Omn_lifecycle.LoginService)){
				// get authentication
				String authentication = "";			
				if(serviceResource.hasProperty(Omn_lifecycle.authentication)){
					authentication += serviceResource.getProperty(Omn_lifecycle.authentication).getObject().asLiteral().getString();
				}
			
				// get hostname
				String hostnameLogin = "";		
				if(serviceResource.hasProperty(Omn_lifecycle.hostname)){
					hostnameLogin += serviceResource.getProperty(Omn_lifecycle.hostname).getObject().asLiteral().getString();
				}
			
				// get port
				String port = "";		
				if(serviceResource.hasProperty(Omn_lifecycle.port)){
					port += serviceResource.getProperty(Omn_lifecycle.port).getObject().asLiteral().getString();
				}
				
				// get username
				String username = "";	
				if(serviceResource.hasProperty(Omn_lifecycle.username)){
					username += serviceResource.getProperty(Omn_lifecycle.username).getObject().asLiteral().getString();
				}
						
				// create login 
				LoginServiceContents loginServiceContent = new ObjectFactory().createLoginServiceContents();
				loginServiceContent.setAuthentication(authentication); // required
				if(hostnameLogin != ""){
					loginServiceContent.setHostname(hostnameLogin);
				}
				if(port != ""){
					loginServiceContent.setPort(port);
				}
				if(username != ""){
					loginServiceContent.setUsername(username);
				}
				
				JAXBElement<LoginServiceContents> loginService = new ObjectFactory().createLogin(loginServiceContent);			
				serviceContents.getAnyOrLoginOrInstall().add(loginService);
			}
			
			if(serviceResource.hasProperty(RDF.type,Omn_lifecycle.ExecuteService)){
				// create execute 
				ExecuteServiceContents excuteServiceContent = new ObjectFactory().createExecuteServiceContents();
				JAXBElement<ExecuteServiceContents> executeService = new ObjectFactory().createExecute(excuteServiceContent);			
				serviceContents.getAnyOrLoginOrInstall().add(executeService);
			}
			
			if(serviceResource.hasProperty(RDF.type,Omn_lifecycle.InstallService)){
				// create execute 
				InstallServiceContents installServiceContent = new ObjectFactory().createInstallServiceContents();
				JAXBElement<InstallServiceContents> installService = new ObjectFactory().createInstall(installServiceContent);			
				serviceContents.getAnyOrLoginOrInstall().add(installService);
			}
		}	
		if(serviceContents != null){ 
			JAXBElement<ServiceContents> services = new ObjectFactory().createServices(serviceContents);
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
			    return URLDecoder.decode(matcher.group(1), StandardCharsets.UTF_8.toString());
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

		Resource topology = model.getResource(AbstractConverter.NAMESPACE + "manifest");
		
		for (Object o : manifest.getAnyOrNodeOrLink()) {
			if (o instanceof JAXBElement) {
				setDetails(model, topology, o);
			} else {
				ManifestConverter.LOG.log(Level.INFO, "Found unknown extsion: "+ o);
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
					if (nodeDetailElement.getDeclaredType().equals(NodeContents.SliverType.class)) {
						NodeContents.SliverType sliverType = (NodeContents.SliverType) nodeDetailElement.getValue();
						omnResource.addProperty(RDF.type, sliverType.getName());
					}
					
					// check if type is Services
					if (nodeDetailElement.getDeclaredType().equals(ServiceContents.class)) {
						
						// get value of the element
						ServiceContents service = (ServiceContents) nodeDetailElement.getValue();
						List<Object> services = service.getAnyOrLoginOrInstall();
						
						// iterate through the Services and add to model
						for (int i = 0; i < services.size(); i++) {			
								
							Object serviceObject = services.get(i);
							Resource omnService = null;
							
							// if login service
							if (((JAXBElement<?>) serviceObject).getDeclaredType().equals(LoginServiceContents.class)) {
								
								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type, Omn_lifecycle.LoginService);	
								LoginServiceContents serviceValue = (LoginServiceContents) ((JAXBElement<?>) serviceObject).getValue();
								
								// add authentication info
								String authentication = serviceValue.getAuthentication();	
								omnService.addProperty(Omn_lifecycle.authentication, authentication);
								
								// add hostname info
								String hostname = serviceValue.getHostname();	
								omnService.addProperty(Omn_lifecycle.hostname, hostname);
								
								// add port info
								String port = serviceValue.getPort();	
								omnService.addProperty(Omn_lifecycle.port, port);
								
								// add username info
								String username = serviceValue.getUsername();	
								omnService.addProperty(Omn_lifecycle.username, username);
							}

							// if execute service
							if (((JAXBElement<?>) serviceObject).getDeclaredType().equals(ExecuteServiceContents.class)) {
								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type, Omn_lifecycle.ExecuteService);
								
								// error in rspec manifest xsd, can't add attributes for execute service
							}
							
							// if install service
							if (((JAXBElement<?>) serviceObject).getDeclaredType().equals(InstallServiceContents.class)) {
								// create service resource
								omnService = model.createResource();
								omnService.addProperty(RDF.type, Omn_lifecycle.InstallService);
								
								// error in rspec manifest xsd, can't add attributes for install service
							}
							
							// add service to node
							if(omnService != null){
								omnResource.addProperty(Omn.hasService, omnService);
							}
						}
					}
				} else {
					ManifestConverter.LOG.log(Level.INFO, "Found unknown extsion: "+ nodeDetailObject);
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
