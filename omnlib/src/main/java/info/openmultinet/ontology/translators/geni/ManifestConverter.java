package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliceInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.GeniSliverInfo;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RspecTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServicesPostBootScript;
import info.openmultinet.ontology.vocabulary.Geo;
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
import java.util.UUID;
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

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
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

		if (group.hasProperty(RDF.type, Omn_lifecycle.Manifest)) {
			setGeniSliceInfo(group, manifest);
		}

		ManifestConverter.convertStatementsToNodesAndLinks(manifest, resources,
				hostname);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents manifest, final List<Statement> resources,
			String hostname) {

		for (final Statement resource : resources) {
			if (!resource.getResource()
					.hasProperty(RDF.type, Omn_resource.Link)) {
				final NodeContents node = new NodeContents();

				setComponentDetails(resource, node);
				setLocation(resource, node);
				// setComponentManagerId(resource, node);
				setMonitoringService(resource, node);
				setGeniSliverInfo(resource, node);
				// setState
				// setVMID
				setSliverType(resource, node);
				setServices(resource, node);
				setInterfaces(resource, node);

				manifest.getAnyOrNodeOrLink().add(
						new ObjectFactory().createNode(node));
			} else {
				final LinkContents link = new LinkContents();
				setGeniSliverInfo(resource, link);
				setLinkDetails(resource, link, hostname);
				setInterfaceRefs(resource, link, hostname);

				manifest.getAnyOrNodeOrLink().add(
						new ObjectFactory().createLink(link));
			}
		}
	}

	private static void setGeniSliceInfo(Resource group, RSpecContents manifest) {

		GeniSliceInfo geniSliceInfo = null;
		Resource omnResource = group;

		if (omnResource.hasProperty(Omn_lifecycle.hasState)) {
			if (geniSliceInfo == null) {
				geniSliceInfo = new ObjectFactory().createGeniSliceInfo();
			}
			Resource state = omnResource.getProperty(Omn_lifecycle.hasState)
					.getObject().asResource();

			String stateGeni = CommonMethods.convertOmnToGeniState(state);
			if (stateGeni != null && stateGeni != "") {
				geniSliceInfo.setState(stateGeni);
			}

			// if (state.getURI().equals(Omn_lifecycle.Active.getURI())) {
			// geniSliceInfo.setState("ready_busy");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Allocated.getURI())) {
			// geniSliceInfo.setState("allocated");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Error.getURI())) {
			// geniSliceInfo.setState("failed");
			// }
			// if
			// (state.getURI().equals(Omn_lifecycle.NotYetInitialized.getURI()))
			// {
			// geniSliceInfo.setState("instantiating");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Pending.getURI())) {
			// geniSliceInfo.setState("pending_allocation");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Preinit.getURI())) {
			// geniSliceInfo.setState("configuring");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Provisioned.getURI())) {
			// geniSliceInfo.setState("provisioned");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Ready.getURI())) {
			// geniSliceInfo.setState("ready");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Stopping.getURI())) {
			// geniSliceInfo.setState("stopping");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Unallocated.getURI())) {
			// geniSliceInfo.setState("unallocated");
			// }

		}

		if (omnResource.hasProperty(Omn_domain_pc.hasUUID)) {
			geniSliceInfo.setUuid(omnResource
					.getProperty(Omn_domain_pc.hasUUID).getObject().asLiteral()
					.getString());
		}

		if (omnResource.hasProperty(Omn.hasURI)) {
			geniSliceInfo.setUrn(omnResource.getProperty(Omn.hasURI)
					.getObject().asLiteral().getString());
		}

		if (geniSliceInfo != null) {
			manifest.getAnyOrNodeOrLink().add(geniSliceInfo);
		}

	}

	private static void setInterfaceRefs(Statement resource, LinkContents link,
			String hostname) {

		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (Statement interface1 : interfaces) {

			InterfaceRefContents newInterface = new ObjectFactory()
					.createInterfaceRefContents();
			String clientId = interface1.getObject().asLiteral().getString();

			newInterface.setClientId(clientId);
			link.getAnyOrPropertyOrLinkType().add(
					new ObjectFactory()
							.createLinkContentsInterfaceRef(newInterface));
		}
	}

	private static void setLinkDetails(Statement resource, LinkContents link,
			String hostname) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasSliverID)) {
			String sliverId = resource.getResource()
					.getProperty(Omn_lifecycle.hasSliverID).getObject()
					.asLiteral().getString();
			link.setSliverId(sliverId);
		} else {
			link.setSliverId(CommonMethods.generateUrnFromUrl(resource
					.getResource().getURI(), "sliver"));
		}

		if (resource.getResource().hasProperty(Omn_resource.clientId)) {
			String clientId = resource.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();
			link.setClientId(clientId);
		}

		if (resource.getResource().hasProperty(Omn_domain_pc.vlanTag)) {
			String vlanTag = resource.getResource()
					.getProperty(Omn_domain_pc.vlanTag).getObject().asLiteral()
					.getString();
			link.setVlantag(vlanTag);
		}

	}

	private static void setGeniSliverInfo(Statement resource, Object node) {

		GeniSliverInfo geniSliverInfo = null;
		Resource omnResource = resource.getResource();
		if (omnResource.hasProperty(Omn_lifecycle.creator)) {
			geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			geniSliverInfo.setCreatorUrn(omnResource
					.getProperty(Omn_lifecycle.creator).getObject().asLiteral()
					.getString());
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.creationTime)) {
			if (geniSliverInfo == null) {
				geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			}

			XSDDateTime creationTime = (XSDDateTime) omnResource
					.getProperty(Omn_lifecycle.creationTime).getObject()
					.asLiteral().getValue();
			XMLGregorianCalendar xgc = xsdToXmlTime(creationTime);

			if (xgc != null) {
				geniSliverInfo.setCreationTime(xgc);
			}
		}

		if (omnResource.hasProperty(Omn_lifecycle.startTime)) {
			if (geniSliverInfo == null) {
				geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			}

			XSDDateTime startTime = (XSDDateTime) omnResource
					.getProperty(Omn_lifecycle.startTime).getObject()
					.asLiteral().getValue();
			XMLGregorianCalendar xgc = xsdToXmlTime(startTime);

			if (xgc != null) {
				geniSliverInfo.setStartTime(xgc);
			}
		}

		if (omnResource.hasProperty(Omn_lifecycle.expirationTime)) {
			if (geniSliverInfo == null) {
				geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			}

			XSDDateTime expTime = (XSDDateTime) omnResource
					.getProperty(Omn_lifecycle.expirationTime).getObject()
					.asLiteral().getValue();
			XMLGregorianCalendar xgc = xsdToXmlTime(expTime);

			if (xgc != null) {
				geniSliverInfo.setExpirationTime(xgc);
				;
			}
		}

		if (omnResource.hasProperty(Omn_lifecycle.resourceId)) {
			if (geniSliverInfo == null) {
				geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			}
			geniSliverInfo.setResourceId(omnResource
					.getProperty(Omn_lifecycle.resourceId).getObject()
					.asLiteral().getString());
		}

		if (omnResource.hasProperty(Omn_lifecycle.hasState)) {
			if (geniSliverInfo == null) {
				geniSliverInfo = new ObjectFactory().createGeniSliverInfo();
			}
			Resource state = omnResource.getProperty(Omn_lifecycle.hasState)
					.getObject().asResource();

			String stateGeni = CommonMethods.convertOmnToGeniState(state);
			if (stateGeni != null && stateGeni != "") {
				geniSliverInfo.setState(stateGeni);
			}

			// if (state.getURI().equals(Omn_lifecycle.Active.getURI())) {
			// geniSliverInfo.setState("ready_busy");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Allocated.getURI())) {
			// geniSliverInfo.setState("allocated");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Error.getURI())) {
			// geniSliverInfo.setState("failed");
			// }
			// if
			// (state.getURI().equals(Omn_lifecycle.NotYetInitialized.getURI()))
			// {
			// geniSliverInfo.setState("instantiating");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Pending.getURI())) {
			// geniSliverInfo.setState("pending_allocation");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Preinit.getURI())) {
			// geniSliverInfo.setState("configuring");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Provisioned.getURI())) {
			// geniSliverInfo.setState("provisioned");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Ready.getURI())) {
			// geniSliverInfo.setState("ready");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Stopping.getURI())) {
			// geniSliverInfo.setState("stopping");
			// }
			// if (state.getURI().equals(Omn_lifecycle.Unallocated.getURI())) {
			// geniSliverInfo.setState("unallocated");
			// }

		}

		if (geniSliverInfo != null) {
			if (node.getClass().equals(NodeContents.class)) {
				((NodeContents) node).getAnyOrRelationOrLocation().add(
						geniSliverInfo);
			}
			if (node.getClass().equals(LinkContents.class)) {
				((LinkContents) node).getAnyOrPropertyOrLinkType().add(
						geniSliverInfo);
			}
		}

	}

	private static void setInterfaces(Statement resource, NodeContents node) {

		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (final Statement interface1 : interfaces) {

			InterfaceContents interfaceContents = null;
			Resource interfaceResource = interface1.getResource();

			if (interfaceResource.hasProperty(Omn_resource.macAddress)) {
				interfaceContents = new ObjectFactory()
						.createInterfaceContents();
				interfaceContents.setMacAddress(interfaceResource
						.getProperty(Omn_resource.macAddress).getObject()
						.asLiteral().toString());
			}
			if (interfaceResource.hasProperty(Omn_resource.clientId)) {
				if (interfaceContents == null) {
					interfaceContents = new InterfaceContents();
				}
				interfaceContents.setClientId(interfaceResource
						.getProperty(Omn_resource.clientId).getObject()
						.asLiteral().toString());
			}

			setIpAddress(interfaceResource, interfaceContents);

			if (interfaceContents != null) {
				JAXBElement<InterfaceContents> interfaceJaxb = new ObjectFactory()
						.createInterface(interfaceContents);
				node.getAnyOrRelationOrLocation().add(interfaceJaxb);
			}
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
			JAXBElement<IpContents> ipJaxb = new ObjectFactory()
					.createIp(ipContents);
			interfaceContents.getAnyOrIpOrHost().add(ipJaxb);
		}
	}

	private static void setLocation(Statement resource, NodeContents node) {
		LocationContents locationContents = null;

		if (resource.getResource().hasProperty(Geo.lat)) {
			locationContents = new ObjectFactory().createLocationContents();
			locationContents.setLatitude(resource.getResource()
					.getProperty(Geo.lat).getObject().toString());
		}

		if (resource.getResource().hasProperty(Geo.long_)) {
			if (locationContents == null) {
				locationContents = new ObjectFactory().createLocationContents();
			}
			locationContents.setLongitude(resource.getResource()
					.getProperty(Geo.long_).getObject().toString());
		}

		if (resource.getResource().hasProperty(Omn_resource.country)) {
			if (locationContents == null) {
				locationContents = new ObjectFactory().createLocationContents();
			}
			locationContents.setCountry(resource.getResource()
					.getProperty(Omn_resource.country).getObject().toString());
		}

		if (locationContents != null) {
			JAXBElement<LocationContents> location = new ObjectFactory()
					.createLocation(locationContents);
			node.getAnyOrRelationOrLocation().add(location);
		}

	}

	private static void setMonitoringService(Statement resource,
			NodeContents node) {
		// Resource resourceResource = resource.getResource();
		// if (resourceResource.hasProperty(Omn_lifecycle.usesService)) {
		// Statement monitoringService = resourceResource
		// .getProperty(Omn_lifecycle.usesService);
		// Resource monitoringResource = monitoringService.getResource();
		// Monitoring monitoring = new ObjectFactory().createMonitoring();
		//
		// if (monitoringResource.hasProperty(Omn_service.hasURI)) {
		// Statement hasUri = monitoringService.getResource().getProperty(
		// Omn_service.hasURI);
		//
		// String uri = hasUri.getObject().asResource().getURI()
		// .toString();
		// monitoring.setUri(uri);
		// }
		//
		// if (monitoringResource.hasProperty(RDF.type)) {
		// Statement hasType = monitoringService.getResource()
		// .getProperty(RDF.type);
		//
		// String type = hasType.getObject().asResource().getURI()
		// .toString();
		// monitoring.setType(type);
		// }
		//
		// node.getAnyOrRelationOrLocation().add(monitoring);
		// }
	}

	private static void setServices(Statement resource, NodeContents node) {
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

			setProxyService(serviceResource, serviceContents);
			setLoginService(serviceResource, serviceContents);
			setExecutiveService(serviceResource, serviceContents);
			setInstallService(serviceResource, serviceContents);
			setPostBootScriptService(serviceResource, serviceContents);
		}
		if (serviceContents != null) {
			JAXBElement<ServiceContents> services = new ObjectFactory()
					.createServices(serviceContents);
			node.getAnyOrRelationOrLocation().add(services);
		}

	}

	private static void setProxyService(Resource serviceResource,
			ServiceContents serviceContents) {
		// TODO Auto-generated method stub
		
	}

	private static void setPostBootScriptService(Resource serviceResource,
			ServiceContents serviceContents) {

		if (serviceResource.hasProperty(RDF.type, Omn_service.PostBootScript)) {

			// get type
			String type = "";
			if (serviceResource.hasProperty(Omn_service.postBootScriptType)) {
				type += serviceResource
						.getProperty(Omn_service.postBootScriptType)
						.getObject().asLiteral().getString();
			}

			// get text
			String text = "";
			if (serviceResource.hasProperty(Omn_service.postBootScriptText)) {
				text += serviceResource
						.getProperty(Omn_service.postBootScriptText)
						.getObject().asLiteral().getString();
			}

			// create execute
			ServicesPostBootScript servicesPostBootScript = new ObjectFactory()
					.createServicesPostBootScript();

			servicesPostBootScript.setType(type);

			if (text != "") {
				servicesPostBootScript.setContent(text);
			}

			serviceContents.getAnyOrLoginOrInstall()
					.add(servicesPostBootScript);
		}

	}

	private static void setSliverType(Statement resource, NodeContents geniNode) {
		// SliverType sliverType = new ObjectFactory()
		// .createNodeContentsSliverType();
		// Resource omnSliver = null;
		//
		// final List<Statement> hasTypes = resource.getResource()
		// .listProperties(RDF.type).toList();
		Resource node = resource.getResource();
		if (node.hasProperty(Omn_lifecycle.hasSliverID)) {
			String sliverId = node.getProperty(Omn_lifecycle.hasSliverID)
					.getObject().asLiteral().getString();
			geniNode.setSliverId(sliverId);
		} else {
			geniNode.setSliverId(CommonMethods.generateUrnFromUrl(resource
					.getResource().getURI(), "sliver"));
		}
		// for (final Statement hasType : hasTypes) {
		// Resource sliverResource = hasType.getObject().asResource();
		// if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
		// omnSliver = sliverResource;
		// sliverType.setName(sliverResource.getURI());
		// }
		// }
		//
		// // check if name was string and not uri
		// if (resource.getResource().hasProperty(Omn_lifecycle.hasSliverName))
		// {
		// String sliverName = resource.getResource()
		// .getProperty(Omn_lifecycle.hasSliverName).getObject()
		// .asLiteral().getString();
		// sliverType.setName(sliverName);
		// }
		//
		// if (omnSliver != null) {
		// setDiskImage(omnSliver, sliverType);
		// }
		//
		// JAXBElement<SliverType> sliver = new ObjectFactory()
		// .createNodeContentsSliverType(sliverType);
		// node.getAnyOrRelationOrLocation().add(sliver);

		if (node.hasProperty(Omn_resource.hasSliverType)) {

			final List<Statement> hasSliverNames = node.listProperties(
					Omn_resource.hasSliverType).toList();

			for (final Statement hasSliverName : hasSliverNames) {

				SliverType sliverType = new ObjectFactory()
						.createNodeContentsSliverType();

				Resource sliverTypeResource = hasSliverName.getObject()
						.asResource();
				if (sliverTypeResource.hasProperty(Omn_lifecycle.hasSliverName)) {
					String sliverName = sliverTypeResource
							.getProperty(Omn_lifecycle.hasSliverName)
							.getObject().asLiteral().getString();
					sliverType.setName(sliverName);
				}

				if (sliverTypeResource != null) {
					setDiskImage(sliverTypeResource, sliverType);
				}

				JAXBElement<SliverType> sliver = new ObjectFactory()
						.createNodeContentsSliverType(sliverType);

				geniNode.getAnyOrRelationOrLocation().add(sliver);
			}
		} else {
			SliverType sliverType = new ObjectFactory()
					.createNodeContentsSliverType();

			final List<Statement> hasTypes = node.listProperties(RDF.type)
					.toList();

			for (final Statement hasType : hasTypes) {
				Resource sliverResource = hasType.getObject().asResource();
				if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
					sliverType.setName(sliverResource.getURI());
				}
			}

			JAXBElement<SliverType> sliver = new ObjectFactory()
					.createNodeContentsSliverType(sliverType);
			geniNode.getAnyOrRelationOrLocation().add(sliver);
		}
	}

	private static void setDiskImage(Resource resource, SliverType sliver) {
		// check if the resource is a disk image
		// if (resourceResource.hasProperty(RDF.type, Omn_domain_pc.DiskImage))
		// {
		//
		// String diskName = "";
		// if (resourceResource.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
		// diskName += resourceResource
		// .getProperty(Omn_domain_pc.hasDiskimageLabel)
		// .getObject().asLiteral().getString();
		// }
		//
		// String diskVersion = "";
		// if (resourceResource.hasProperty(Omn_domain_pc.hasDiskimageVersion))
		// {
		// diskVersion += resourceResource
		// .getProperty(Omn_domain_pc.hasDiskimageVersion)
		// .getObject().asLiteral().getString();
		// }
		//
		// DiskImageContents diskImageContents = new ObjectFactory()
		// .createDiskImageContents();
		// if (diskName != "") {
		// diskImageContents.setName(diskName);
		// }
		// if (diskVersion != "") {
		// diskImageContents.setVersion(diskVersion);
		// }
		// JAXBElement<DiskImageContents> diskImage = new ObjectFactory()
		// .createDiskImage(diskImageContents);
		// sliverName.getAnyOrDiskImage().add(diskImage);
		// }

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

		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			final boolean isExclusive = resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean();
			node.setExclusive(isExclusive);
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource()
					.getProperty(Omn_lifecycle.hasID).getString());
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.managedBy)) {
			node.setComponentManagerId(resource.getResource()
					.getProperty(Omn_lifecycle.managedBy).getObject()
					.toString());
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			RDFNode implementedBy = resource.getResource()
					.getProperty(Omn_lifecycle.implementedBy).getObject();

			String urn = CommonMethods.generateUrnFromUrl(
					implementedBy.toString(), "node");

			node.setComponentId(urn);
			// node.setComponentName(implementedBy.asNode().getLocalName());

			// if (implementedBy.asResource().hasProperty(
			// Omn_lifecycle.hasComponentName)) {
			// String componentName = implementedBy.asResource()
			// .getProperty(Omn_lifecycle.hasComponentName)
			// .getObject().asLiteral().getString();
			// node.setComponentName(componentName);
			// }
		}

		if (resource.getResource().hasProperty(Omn_lifecycle.hasComponentName)) {
			node.setComponentName(resource.getResource()
					.getProperty(Omn_lifecycle.hasComponentName).getObject()
					.asLiteral().getString());
		}

		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
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

	private static void setLoginService(Resource serviceResource,
			ServiceContents serviceContents) {
		if (serviceResource.hasProperty(RDF.type, Omn_service.LoginService)) {
			// get authentication
			String authentication = "";
			if (serviceResource.hasProperty(Omn_service.authentication)) {
				authentication += serviceResource
						.getProperty(Omn_service.authentication).getObject()
						.asLiteral().getString();
			} else if (serviceResource.hasProperty(Omn_service.publickey)) {
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

			// get username
			String username = "";
			if (serviceResource.hasProperty(Omn_service.username)) {
				username += serviceResource.getProperty(Omn_service.username)
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
			if (username != "") {
				loginServiceContent.setUsername(username);
			}

			JAXBElement<LoginServiceContents> loginService = new ObjectFactory()
					.createLogin(loginServiceContent);
			serviceContents.getAnyOrLoginOrInstall().add(loginService);
		}
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

	public static Model getModel(final InputStream stream)
			throws JAXBException, MissingRspecElementException {
		final Model model = ManifestConverter.createModelTemplate();
		final RSpecContents manifest = ManifestConverter.getManifest(stream);

		ManifestConverter.convertManifest2Model(manifest, model);

		return model;
	}

	private static void convertManifest2Model(final RSpecContents manifest,
			final Model model) throws MissingRspecElementException {

		Resource topology = model.getResource(AbstractConverter.NAMESPACE
				+ "manifest");

		for (Object o : manifest.getAnyOrNodeOrLink()) {
			if (o instanceof JAXBElement) {
				extractDetails(model, topology, o);
			} else if (o.getClass().equals(GeniSliceInfo.class)) {
				extractGeniSliceInfo(topology, o);
			} else {
				ManifestConverter.LOG.log(Level.INFO,
						"Found unknown extension: " + o);
			}
		}
	}

	private static void extractGeniSliceInfo(Resource topology, Object o) {

		// get value of the element
		GeniSliceInfo geniSliceInfo = (GeniSliceInfo) o;
		OntClass stateClass = null;
		String stateString = geniSliceInfo.getState();

		switch (stateString) {
		case "ready_busy":
			stateClass = Omn_lifecycle.Active;
			break;
		case "allocated":
			stateClass = Omn_lifecycle.Allocated;
			break;
		case "failed":
			stateClass = Omn_lifecycle.Error;
			break;
		case "instantiating":
			stateClass = Omn_lifecycle.NotYetInitialized;
			break;
		case "pending_allocation":
			stateClass = Omn_lifecycle.Pending;
			break;
		case "configuring":
			stateClass = Omn_lifecycle.Preinit;
			break;
		case "provisioned":
			stateClass = Omn_lifecycle.Provisioned;
			break;
		case "ready":
			stateClass = Omn_lifecycle.Ready;
			break;
		case "stopping":
			stateClass = Omn_lifecycle.Stopping;
			break;
		case "unallocated":
			stateClass = Omn_lifecycle.Unallocated;
			break;
		}
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

	public static void extractDetails(final Model model, Resource topology,
			Object o) throws MissingRspecElementException {
		JAXBElement<?> element = (JAXBElement<?>) o;

		if (element.getDeclaredType().equals(NodeContents.class)) {
			extractNode(element, topology);

		} else if (o
				.getClass()
				.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Monitoring.class)) {
			// TODO
			ManifestConverter.LOG.log(Level.INFO, "TODO: monitoring extension");
		} else if (element.getDeclaredType().equals(LinkContents.class)) {
			extractLink(element, topology);
		}
	}

	private static void extractNode(JAXBElement<?> element, Resource topology)
			throws MissingRspecElementException {
		NodeContents node = (NodeContents) element.getValue();

		String sliverID = node.getSliverId();
		Resource omnResource = null;
		if (sliverID != null) {
			omnResource = topology.getModel().createResource(
					CommonMethods.generateUrlFromUrn(sliverID));
			omnResource.addProperty(Omn_lifecycle.hasSliverID, sliverID);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			omnResource = topology.getModel().createResource(uuid);
		}

		// .createResource(parseSliverID(node.getSliverId()));

		// client_id is required by manifest-common.xsd
		String clientId = node.getClientId();
		if (clientId == null) {
			throw new MissingRspecElementException("NodeContents > client_id");
		}
		omnResource.addProperty(Omn_lifecycle.hasID, clientId);

		omnResource.addProperty(RDFS.label, node.getClientId());
		omnResource.addProperty(RDF.type, Omn.Resource);

		if (null != node.isExclusive()) {
			omnResource.addProperty(Omn_resource.isExclusive, topology
					.getModel().createTypedLiteral(node.isExclusive()));
		}
		
		extractComponentDetails(node, omnResource);

		for (Object nodeDetailObject : node.getAnyOrRelationOrLocation()) {
			if (nodeDetailObject instanceof JAXBElement) {
				JAXBElement<?> nodeDetailElement = (JAXBElement<?>) nodeDetailObject;

				extractLocation(nodeDetailElement, omnResource);
				extractSliverType(nodeDetailElement, omnResource);
				extractServices(nodeDetailElement, omnResource,
						topology.getModel());
				extractInterfaces(nodeDetailElement, omnResource,
						topology.getModel());

			} else if (nodeDetailObject.getClass().equals(GeniSliverInfo.class)) {
				extractGeniSliverInfo(nodeDetailObject, omnResource);
			} else {
				ManifestConverter.LOG.log(Level.INFO,
						"Found unknown extsion within node: "
								+ nodeDetailObject.getClass());
			}
		}

		topology.addProperty(Omn.hasResource, omnResource);
	}

	private static void extractComponentDetails(NodeContents node,
			Resource omnResource) {
		if (node.getComponentManagerId() != null) {
			RDFNode manager = ResourceFactory.createResource(node
					.getComponentManagerId());
			omnResource.addProperty(Omn_lifecycle.managedBy, manager);
		}
		

		// component id is not required
		String componentIdOriginal = node.getComponentId();
		if (componentIdOriginal != null) {
			String componentId = CommonMethods
					.generateUrlFromUrn(componentIdOriginal);
			Resource componentIDResource = omnResource.getModel().createResource(
					componentId);
			omnResource.addProperty(Omn_lifecycle.implementedBy,
					componentIDResource);
		}

		// component name is not required
		String componentName = node.getComponentName();
		if (componentName != null) {
			omnResource.addProperty(Omn_lifecycle.hasComponentName,
					componentName);
		}

		
	}

	private static void extractLink(JAXBElement<?> element, Resource topology)
			throws MissingRspecElementException {
		LinkContents link = (LinkContents) element.getValue();
		Resource linkResource;
		String sliverID = link.getSliverId();
		if (sliverID != null) {
			linkResource = topology.getModel().createResource(
					CommonMethods.generateUrlFromUrn(sliverID));
			linkResource.addProperty(Omn_lifecycle.hasSliverID, sliverID);
		} else {
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			linkResource = topology.getModel().createResource(uuid);
		}

		if (link.getClientId() == null) {
			throw new MissingRspecElementException("LinkContents > client_id ");
		}

		linkResource.addLiteral(Omn_resource.clientId, link.getClientId()); // required
		linkResource.addProperty(RDF.type, Omn_resource.Link);

		if (link.getVlantag() != null) {
			linkResource.addLiteral(Omn_domain_pc.vlanTag, link.getVlantag());
		}
		// Get source and sink interfaces
		@SuppressWarnings("unchecked")
		List<Object> linkContents = link.getAnyOrPropertyOrLinkType();
		for (Object linkObject : linkContents) {
			if (linkObject instanceof JAXBElement) {
				JAXBElement<?> linkElement = (JAXBElement<?>) linkObject;

				if (linkElement.getDeclaredType().equals(
						InterfaceRefContents.class)) {
					InterfaceRefContents interfaceRefContents = (InterfaceRefContents) linkElement
							.getValue();
					linkResource.addProperty(Omn_resource.hasInterface,
							interfaceRefContents.getClientId());
				}

			} else if (linkObject.getClass().equals(GeniSliverInfo.class)) {
				extractGeniSliverInfo(linkObject, linkResource);
			} else {
				ManifestConverter.LOG.log(Level.INFO,
						"Unknown extension witin link");
			}
		}
		topology.addProperty(Omn.hasResource, linkResource);
	}

	private static void extractGeniSliverInfo(Object nodeDetailObject,
			Resource omnResource) {

		// get value of the element
		GeniSliverInfo geniSliverInfo = (GeniSliverInfo) nodeDetailObject;
		// OntClass stateClass = null;
		String stateString = geniSliverInfo.getState();
		OntClass stateClass = CommonMethods.convertGeniStateToOmn(stateString);

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
			omnResource.addProperty(Omn_lifecycle.hasState, stateClass);
		}

		if (geniSliverInfo.getResourceId() != null
				&& geniSliverInfo.getResourceId() != "") {
			omnResource.addLiteral(Omn_lifecycle.resourceId,
					geniSliverInfo.getResourceId());
		}

		if (geniSliverInfo.getStartTime() != null) {
			// XMLGregorianCalendar startTime = geniSliverInfo.getStartTime();
			// Calendar startTimeCalendar = startTime.toGregorianCalendar();
			// XSDDateTime startTimeXSDDateTime = new XSDDateTime(
			// startTimeCalendar);

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

	private static void extractPostBootScript(Object nodeDetailObject,
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

	private static void extractLocation(JAXBElement<?> nodeDetailElement,
			Resource omnResource) {
		// check if type is location
		if (nodeDetailElement.getDeclaredType().equals(LocationContents.class)) {

			// get value of the element
			LocationContents locationContents = (LocationContents) nodeDetailElement
					.getValue();
			if (locationContents.getLatitude() != null
					&& !locationContents.getLatitude().equals("")) {
				omnResource
						.addProperty(Geo.lat, locationContents.getLatitude());
			}
			if (locationContents.getLatitude() != null
					&& !locationContents.getLatitude().equals("")) {
				omnResource.addProperty(Geo.long_,
						locationContents.getLongitude());
			}

			// country is required
			String country = locationContents.getCountry();
			omnResource.addProperty(Omn_resource.country, country);
		}
	}

	private static void extractSliverType(JAXBElement<?> nodeDetailElement,
			Resource omnResource) throws MissingRspecElementException {
		if (nodeDetailElement.getDeclaredType().equals(
				NodeContents.SliverType.class)) {

			NodeContents.SliverType sliverType = (NodeContents.SliverType) nodeDetailElement
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
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				sliverTypeResource = omnResource.getModel()
						.createResource(uuid);
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverName));
			} else {
				String sliverTypeUrl = "http://open-multinet.info/example#"
						+ sliverName;
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				sliverTypeResource = omnResource.getModel()
						.createResource(uuid);
				omnResource.addProperty(RDF.type, omnResource.getModel()
						.createResource(sliverTypeUrl));
			}

			// Resource sliver =
			// model.createResource(UUID.randomUUID().toString());
			// if (sliverType.getName() != null) {
			// if (AbstractConverter.isUrl(sliverType.getName())) {
			// sliver.addProperty(RDF.type, sliverType.getName());
			// }
			// sliver.addProperty(RDFS.label,
			// AbstractConverter.getName(sliverType.getName()));
			// }
			omnResource.addProperty(Omn_resource.hasSliverType,
					sliverTypeResource);
			sliverTypeResource.addProperty(Omn_lifecycle.hasSliverName,
					sliverName);
			sliverTypeResource.addProperty(RDF.type, Omn_resource.SliverType);

			List<Object> sliverContents = sliverType.getAnyOrDiskImage();
			for (int i = 0; i < sliverContents.size(); i++) {
				Object sliverObject = sliverContents.get(i);
				tryExtractDiskImage(sliverObject, sliverTypeResource);
			}
			// omnResource.addProperty(RDF.type, sliverTypeResource);
		}
	}

	private static void tryExtractDiskImage(Object sliverObject,
			Resource omnSliver) throws MissingRspecElementException {
		if (sliverObject instanceof JAXBElement) {
			// check if disk_image
			if (((JAXBElement<?>) sliverObject).getDeclaredType().equals(
					DiskImageContents.class)) {

				// Resource diskImage =
				// sliver.getModel().createResource(UUID.randomUUID().toString());
				// diskImage.addProperty(RDF.type, Omn_domain_pc.DiskImage);
				DiskImageContents diskImageContents = (DiskImageContents) ((JAXBElement<?>) sliverObject)
						.getValue();
				//
				// // add name info
				// String name = diskImageContents.getName();
				// if (name != null && !name.equals("")) {
				// diskImage.addLiteral(Omn_domain_pc.hasDiskimageLabel, name);
				// }
				// // add version info
				// String version = diskImageContents.getVersion();
				// if (version != null && !version.equals("")) {
				// diskImage.addLiteral(Omn_domain_pc.hasDiskimageVersion,
				// version);
				// }
				// sliver.addProperty(Omn.hasResource, diskImage);

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
					diskImage.addLiteral(Omn_domain_pc.hasDiskimageVersion,
							version);
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
						Resource diskImageResource = diskImageStatement
								.getObject().asResource();
						if (diskImageResource
								.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
							String diskImageLabel = diskImageResource
									.getProperty(
											Omn_domain_pc.hasDiskimageLabel)
									.getObject().asLiteral().getString();
							if (diskImageLabel.equals(name)) {
								alreadyExists = true;
							}
						}
					}
				}

				if (!alreadyExists) {
					omnSliver
							.addProperty(Omn_domain_pc.hasDiskImage, diskImage);
				}
			}
		}
	}

	private static void extractInterfaces(JAXBElement<?> nodeDetailElement,
			Resource omnResource, Model model) {

		// check if type is interface
		if (nodeDetailElement.getDeclaredType().equals(InterfaceContents.class)) {

			// get value of the element
			InterfaceContents interfaceContents = (InterfaceContents) nodeDetailElement
					.getValue();
			List<Object> interfaces = interfaceContents.getAnyOrIpOrHost();
			String uuid = "urn:uuid:" + UUID.randomUUID().toString();
			Resource omnInteface = model.createResource(uuid);

			if (interfaceContents.getMacAddress() != null) {
				omnInteface.addProperty(Omn_resource.macAddress,
						interfaceContents.getMacAddress());
			}
			if (interfaceContents.getClientId() != null) {
				omnInteface.addProperty(Omn_resource.clientId,
						interfaceContents.getClientId());
			}
			// iterate through the interfaces and add to model
			for (int i = 0; i < interfaces.size(); i++) {

				Object interfaceObject = interfaces.get(i);
				String uuid2 = "urn:uuid:" + UUID.randomUUID().toString();
				Resource omnIpAddress = model.createResource(uuid2);
				tryExtractIPAddress(interfaceObject, omnInteface, omnIpAddress);

				// add interface to node
				if (omnInteface != null) {
					omnResource.addProperty(Omn_resource.hasInterface,
							omnInteface);
				}
			}
		}
	}

	private static void extractServices(JAXBElement<?> nodeDetailElement,
			Resource omnResource, Model model) {
		// check if type is Services
		if (nodeDetailElement.getDeclaredType().equals(ServiceContents.class)) {

			// get value of the element
			ServiceContents service = (ServiceContents) nodeDetailElement
					.getValue();
			List<Object> services = service.getAnyOrLoginOrInstall();

			// add blank service, if blank service node in rspec
			if (services.size() == 0) {
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				Resource omnService = model.createResource(uuid);
				omnResource.addProperty(Omn.hasService, omnService);
			}

			// iterate through the Services and add to model
			for (int i = 0; i < services.size(); i++) {

				Object serviceObject = services.get(i);
				Resource omnService = null;
				String uuid = "urn:uuid:" + UUID.randomUUID().toString();
				omnService = model.createResource(uuid);
				// if login service
				if (serviceObject instanceof JAXBElement) {
					final JAXBElement<?> serviceObjectJaxb = (JAXBElement<?>) serviceObject;
					final Class<?> serviceType = serviceObjectJaxb
							.getDeclaredType();

					if (serviceType.equals(LoginServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.LoginService);
						omnService.addProperty(RDFS.label, "LoginService");
						LoginServiceContents serviceValue = (LoginServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add authentication info
						String authentication = serviceValue
								.getAuthentication();
						omnService.addLiteral(Omn_service.authentication,
								authentication);

						// add hostname info
						String hostname = serviceValue.getHostname();
						omnService.addLiteral(Omn_service.hostname, hostname);

						// add port info
						String portString = serviceValue.getPort();
						int port = Integer.parseInt(portString);
						omnService.addLiteral(Omn_service.port, port);

						// add username info
						String username = serviceValue.getUsername();
						omnService.addLiteral(Omn_service.username, username);

					}
					// if execute service
					if (serviceType.equals(ExecuteServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.ExecuteService);
						ExecuteServiceContents serviceValue = (ExecuteServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add command info
						String command = serviceValue.getCommand();
						omnService.addLiteral(Omn_service.command, command);

						// add shell info
						String shell = serviceValue.getShell();
						omnService.addLiteral(Omn_service.shell, shell);
					}

					// if install service
					if (serviceType.equals(InstallServiceContents.class)) {

						omnService.addProperty(RDF.type,
								Omn_service.InstallService);
						InstallServiceContents serviceValue = (InstallServiceContents) ((JAXBElement<?>) serviceObject)
								.getValue();

						// add install path info
						String installPath = serviceValue.getInstallPath();
						omnService.addLiteral(Omn_service.installPath,
								installPath);

						// add url path info
						String url = serviceValue.getUrl();
						URI urlURI = URI.create(url);
						omnService.addLiteral(Omn_service.url, urlURI);

					}
				} else if (serviceObject
						.getClass()
						.equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.ServicesPostBootScript.class)) {
					omnService
							.addProperty(RDF.type, Omn_service.PostBootScript);
					extractPostBootScript(serviceObject, omnService);
				} else if (serviceObject.getClass().equals(info.openmultinet.ontology.translators.geni.jaxb.manifest.Proxy.class)){
					ManifestConverter.LOG.info("proxy lkjblskdfjlskdjfdklfj");
				}

				// add service to node
				if (omnService != null) {
					omnResource.addProperty(Omn.hasService, omnService);
				}
			}
		}
	}

	private static void tryExtractIPAddress(Object rspecNodeObject,
			Resource omnNode, Resource omnIpAddress) {
		try {
			@SuppressWarnings("unchecked")
			final JAXBElement<IpContents> availablityJaxb = (JAXBElement<IpContents>) rspecNodeObject;
			final IpContents availability = availablityJaxb.getValue();

			omnIpAddress.addLiteral(Omn_resource.address,
					availability.getAddress());
			if (availability.getNetmask() != null
					&& !availability.getNetmask().equals("")) {
				omnIpAddress.addLiteral(Omn_resource.netmask,
						availability.getNetmask());
			}
			omnIpAddress.addLiteral(Omn_resource.type, availability.getType());
			omnNode.addProperty(Omn_resource.hasIPAddress, omnIpAddress);

		} catch (final ClassCastException e) {
			ManifestConverter.LOG.finer(e.getMessage());
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
		topology.addProperty(RDFS.label, "Manifest");
		return model;
	}
}