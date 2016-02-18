package info.openmultinet.ontology.translators.geni.manifest;

import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.Position3D;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ServiceContents;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods for converting from OMN model to manifest RSpec. For native
 * RSpec elements.
 * 
 * @author robynloughnane
 *
 */
public class ManifestSet extends AbstractConverter {

	private static final String HOST = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger.getLogger(ManifestSet.class
			.getName());

	public static void setInterfaceRefs(Statement resource, LinkContents link,
			String hostname) {

		List<Statement> interfaces = resource.getResource()
				.listProperties(Omn_resource.hasInterface).toList();

		for (Statement interface1 : interfaces) {

			Resource interfaceResource = interface1.getObject().asResource();
			InterfaceRefContents newInterface = new ObjectFactory()
					.createInterfaceRefContents();

			// clientId is required
			// String clientId = interface1.getObject().asLiteral().getString();
			String clientId = interfaceResource
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();
			newInterface.setClientId(clientId);

			// componentId optional
			if (interfaceResource.hasProperty(Omn_lifecycle.hasComponentID)) {
				String componentId = interfaceResource
						.getProperty(Omn_lifecycle.hasComponentID).getObject()
						.asLiteral().getString();
				newInterface.setComponentId(componentId);
			}

			// sliverId optional
			if (interfaceResource.hasProperty(Omn_lifecycle.hasSliverID)) {
				String sliverId = interfaceResource
						.getProperty(Omn_lifecycle.hasSliverID).getObject()
						.asLiteral().getString();
				newInterface.setSliverId(sliverId);
			}

			link.getAnyOrPropertyOrLinkType().add(
					new ObjectFactory()
							.createLinkContentsInterfaceRef(newInterface));
		}
	}

	public static void setLinkDetails(Statement resource, LinkContents link,
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

		// if (resource.getResource().hasProperty(Omn_lifecycle.hasLinkName)) {
		// String linkName = resource.getResource()
		// .getProperty(Omn_lifecycle.hasLinkName).getObject()
		// .asLiteral().getString();
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

	public static void setInterfaces(Statement resource, NodeContents node) {

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

			// componentId optional
			if (interfaceResource.hasProperty(Omn_lifecycle.hasComponentID)) {
				String componentId = interfaceResource
						.getProperty(Omn_lifecycle.hasComponentID).getObject()
						.asLiteral().getString();
				interfaceContents.setComponentId(componentId);
			}

			// sliverId optional
			if (interfaceResource.hasProperty(Omn_lifecycle.hasSliverID)) {
				String sliverId = interfaceResource
						.getProperty(Omn_lifecycle.hasSliverID).getObject()
						.asLiteral().getString();
				interfaceContents.setSliverId(sliverId);
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

	public static void setLocation(Statement omnResource, NodeContents node) {
		
		StmtIterator locations = omnResource.getResource().listProperties(
				Omn_resource.hasLocation);

		while (locations.hasNext()) {

			Statement locationStatement = locations.next();
			ObjectFactory of = new ObjectFactory();
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

			Position3D position3d = null;
			if (omnRes.hasProperty(Omn_resource.x)
					|| omnRes.hasProperty(Omn_resource.y)
					|| omnRes.hasProperty(Omn_resource.z)) {
				position3d = of.createPosition3D();

				if (omnRes.hasProperty(Omn_resource.x)) {
					double x = omnRes.getProperty(Omn_resource.x).getObject()
							.asLiteral().getDouble();
					BigDecimal xBigDecimal = BigDecimal.valueOf(x);
					position3d.setX(xBigDecimal);
				}
				if (omnRes.hasProperty(Omn_resource.y)) {
					double y = omnRes.getProperty(Omn_resource.y).getObject()
							.asLiteral().getDouble();
					BigDecimal yBigDecimal = BigDecimal.valueOf(y);
					position3d.setY(yBigDecimal);
				}
				if (omnRes.hasProperty(Omn_resource.z)) {
					double z = omnRes.getProperty(Omn_resource.z).getObject()
							.asLiteral().getDouble();
					BigDecimal zBigDecimal = BigDecimal.valueOf(z);
					position3d.setZ(zBigDecimal);
				}
			}

			if (position3d != null) {
				location.getAny().add(position3d);
			}

			node.getAnyOrRelationOrLocation().add(
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
				node.getAnyOrRelationOrLocation().add(location);
			}
		}
	}

	public static void setServices(Statement resource, NodeContents node) {
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

			ManifestSetExt.setProxyService(serviceResource, serviceContents);
			setLoginService(serviceResource, serviceContents);
			setExecutiveService(serviceResource, serviceContents);
			setInstallService(serviceResource, serviceContents);
			ManifestSetExt.setPostBootScriptService(serviceResource,
					serviceContents);
		}
		if (serviceContents != null) {
			JAXBElement<ServiceContents> services = new ObjectFactory()
					.createServices(serviceContents);
			node.getAnyOrRelationOrLocation().add(services);
		}

	}

	public static void setSliverType(Statement resource, NodeContents geniNode) {
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

			boolean sliverTypeNameExists = false;
			for (final Statement hasType : hasTypes) {
				Resource sliverResource = hasType.getObject().asResource();
				if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
					sliverType.setName(sliverResource.getURI());
					sliverTypeNameExists = true;
				}
			}

			if (sliverTypeNameExists) {
				JAXBElement<SliverType> sliver = new ObjectFactory()
						.createNodeContentsSliverType(sliverType);
				geniNode.getAnyOrRelationOrLocation().add(sliver);
			}
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

	public static void setComponentDetails(final Statement resource,
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

	private static void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		final List<Statement> implementedBy = resource.getResource()
				.listProperties(Omn_lifecycle.implementedBy).toList();
		for (final Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}

	public static void setHardwareTypes(Statement omnResource,
			NodeContents geniNode) {
		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(
				Omn_resource.hasHardwareType);

		while (types.hasNext()) {
			HardwareTypeContents hwType;
			Resource hwObject = types.next().getObject().asResource();
			String hwName = hwObject.getProperty(RDFS.label).getObject()
					.asLiteral().getString();
			ObjectFactory of = new ObjectFactory();
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

}