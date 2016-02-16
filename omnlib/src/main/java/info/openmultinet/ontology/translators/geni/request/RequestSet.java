package info.openmultinet.ontology.translators.geni.request;

import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.request.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.request.DiskImageContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ExecuteServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InstallServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.IpContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.request.Location;
import info.openmultinet.ontology.translators.geni.jaxb.request.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.LoginServiceContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.request.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.request.Position3D;
import info.openmultinet.ontology.translators.geni.jaxb.request.ServiceContents;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_service;

import java.math.BigDecimal;
import java.util.List;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods for converting from OMN model to request RSpec. For native
 * RSpec elements.
 * 
 * @author robynloughnane
 *
 */
public class RequestSet extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";

	public static void setLocation(Statement omnResource, NodeContents geniNode) {
		// ObjectFactory of = new ObjectFactory();
		// LocationContents location = of.createLocationContents();
		// Resource omnRes = omnResource.getResource();
		//
		// if (omnRes.hasProperty(Geonames.countryCode)) {
		// location.setCountry(omnRes.getProperty(Geonames.countryCode)
		// .getString());
		// } else {
		// // country required
		// location.setCountry("");
		// }
		//
		// if (omnRes.hasProperty(Geo.lat)) {
		// location.setLatitude(omnRes.getProperty(Geo.lat).getString());
		// }
		//
		// if (omnRes.hasProperty(Geo.long_)) {
		// location.setLongitude(omnRes.getProperty(Geo.long_).getString());
		// }
		// if (omnRes.hasProperty(Geo.lat) || omnRes.hasProperty(Geo.long_)) {
		// geniNode.getAnyOrRelationOrLocation().add(
		// of.createLocation(location));
		// }
		StmtIterator locations = omnResource.getResource().listProperties(
				Omn_resource.hasLocation);

		while (locations.hasNext()) {

			Statement locationStatement = locations.next();
			Resource omnRes = locationStatement.getResource();
			ObjectFactory of = new ObjectFactory();

			// add jFed location
			if (omnRes.hasProperty(Omn_resource.jfedX)
					|| omnRes.hasProperty(Omn_resource.jfedY)) {

				Location loc = of.createLocation();

				if (omnRes.hasProperty(Omn_resource.jfedX)) {
					String x = omnRes.getProperty(Omn_resource.jfedX)
							.getString();
					loc.setX(x);
				}

				if (omnRes.hasProperty(Omn_resource.jfedY)) {
					String y = omnRes.getProperty(Omn_resource.jfedY)
							.getString();
					loc.setY(y);
				}

				geniNode.getAnyOrRelationOrLocation().add(loc);
			}

			// add normal RSpec location
			if (omnRes.hasProperty(Geonames.countryCode)
					|| omnRes.hasProperty(Geo.lat)
					|| omnRes.hasProperty(Geo.long_)) {

				LocationContents location = of.createLocationContents();

				if (omnRes.hasProperty(Geonames.countryCode)) {
					location.setCountry(omnRes
							.getProperty(Geonames.countryCode).getString());
				} else {
					// country required
					location.setCountry("");
				}

				if (omnRes.hasProperty(Geo.lat)) {
					location.setLatitude(omnRes.getProperty(Geo.lat)
							.getString());
				}

				if (omnRes.hasProperty(Geo.long_)) {
					location.setLongitude(omnRes.getProperty(Geo.long_)
							.getString());
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
				
				geniNode.getAnyOrRelationOrLocation().add(
						of.createLocation(location));
			}
		}
	}

	public static void setHardwareTypes(Statement omnResource,
			NodeContents geniNode) {

		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(
				Omn_resource.hasHardwareType);
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

				if (hwObject.hasProperty(Omn_domain_pc.emulabNodeTypeStatic)) {
					String staticType = hwObject
							.getProperty(Omn_domain_pc.emulabNodeTypeStatic)
							.getObject().asLiteral().getString();
					nodeType.setStatic(staticType);
				}
			}

			geniNodeDetails.add(of.createHardwareType(hwType));
		}
	}

	public static void setLinkProperties(Statement resource, LinkContents link) {
		List<Statement> linkProperties = resource.getResource()
				.listProperties(Omn_resource.hasProperty).toList();

		for (Statement linkPropertyStatement : linkProperties) {

			LinkPropertyContents newLinkProperty = new ObjectFactory()
					.createLinkPropertyContents();

			Resource linkResource = linkPropertyStatement.getResource();

			Resource sinkResource = linkResource
					.getProperty(Omn_resource.hasSink).getObject().asResource();
			String sinkId;
			if (sinkResource.hasProperty(Omn_resource.clientId)) {
				sinkId = sinkResource.getProperty(Omn_resource.clientId)
						.getObject().asLiteral().getString();
			} else {
				sinkId = sinkResource.getURI();
			}
			newLinkProperty.setDestId(sinkId);

			Resource sourceResource = linkResource
					.getProperty(Omn_resource.hasSource).getObject()
					.asResource();
			String sourceId;
			if (sourceResource.hasProperty(Omn_resource.clientId)) {
				sourceId = sourceResource.getProperty(Omn_resource.clientId)
						.getObject().asLiteral().getString();
			} else {
				sourceId = sourceResource.getURI();
			}
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

	public static void setInterfaceRefs(Statement resource, LinkContents link) {
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

	public static void setLinkDetails(Statement resource, LinkContents link) {
		if (resource.getResource().hasProperty(Omn_resource.clientId)) {
			String clientId = resource.getResource()
					.getProperty(Omn_resource.clientId).getObject().asLiteral()
					.getString();
			link.setClientId(clientId);
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
	}

	public static void setInterfaces(Statement resource, NodeContents node) {

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

			if (interfaceResource.hasProperty(Omn_lifecycle.hasComponentID)) {
				interfaceContents.setComponentId(interfaceResource
						.getProperty(Omn_lifecycle.hasComponentID).getObject()
						.asLiteral().getString());
			}

			setIpAddress(interfaceResource, interfaceContents);

			JAXBElement<InterfaceContents> interfaceRspec = new ObjectFactory()
					.createInterface(interfaceContents);
			node.getAnyOrRelationOrLocation().add(interfaceRspec);
		}
	}

	public static void setServices(Statement resource, NodeContents node)
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

	public static void setSliverType(Statement resource, NodeContents geniNode) {

		// check if name was string and not uri
		// if (resource.getResource().hasProperty(Omn_lifecycle.hasSliverName))
		// {
		//
		// SliverType sliverType = new ObjectFactory()
		// .createNodeContentsSliverType();
		//
		// String sliverName = resource.getResource()
		// .getProperty(Omn_lifecycle.hasSliverName).getObject()
		// .asLiteral().getString();
		// sliverType.setName(sliverName);
		//
		// final List<Statement> hasTypes = resource.getResource()
		// .listProperties(RDF.type).toList();
		//
		// // find the URI of the sliver
		// Resource omnSliver = null;
		// for (final Statement hasType : hasTypes) {
		// RDFNode sliverNode = hasType.getObject();
		// Resource sliverResource = sliverNode.asResource();
		//
		// if (AbstractConverter.nonGeneric(sliverResource.getURI())) {
		// omnSliver = sliverResource;
		// }
		// }
		// setDiskImage(omnSliver, sliverType);
		//
		// JAXBElement<SliverType> sliver = new ObjectFactory()
		// .createNodeContentsSliverType(sliverType);
		//
		// node.getAnyOrRelationOrLocation().add(sliver);
		// }

		if (resource.getResource().hasProperty(Omn_resource.hasSliverType)) {

			final List<Statement> hasSliverNames = resource.getResource()
					.listProperties(Omn_resource.hasSliverType).toList();

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

			final List<Statement> hasTypes = resource.getResource()
					.listProperties(RDF.type).toList();

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
		List<Statement> diskImages = resource.listProperties(
				Omn_domain_pc.hasDiskImage).toList();

		for (Statement diskImageStatement : diskImages) {
			DiskImageContents diskImageContents = new ObjectFactory()
					.createDiskImageContents();
			Resource diskImageResource = diskImageStatement.getResource();

			// set name
			if (diskImageResource.hasProperty(Omn_domain_pc.hasDiskimageLabel)) {
				String diskImageName = diskImageResource
						.getProperty(Omn_domain_pc.hasDiskimageLabel)
						.getObject().asLiteral().getString();
				diskImageContents.setName(diskImageName);
			}

			// set version
			if (diskImageResource
					.hasProperty(Omn_domain_pc.hasDiskimageVersion)) {
				String diskImageVersion = diskImageResource
						.getProperty(Omn_domain_pc.hasDiskimageVersion)
						.getObject().asLiteral().getString();
				diskImageContents.setVersion(diskImageVersion);
			}

			if (diskImageResource.hasProperty(Omn_domain_pc.hasDiskimageURI)) {
				String uri = diskImageResource
						.getProperty(Omn_domain_pc.hasDiskimageURI).getObject()
						.asLiteral().getString();
				diskImageContents.setUrl(uri);
			}

			if (diskImageResource
					.hasProperty(Omn_domain_pc.hasDiskimageDescription)) {
				String description = diskImageResource
						.getProperty(Omn_domain_pc.hasDiskimageDescription)
						.getObject().asLiteral().getString();
				diskImageContents.setDescription(description);
			}

			if (diskImageResource.hasProperty(Omn_domain_pc.hasDiskimageOS)) {
				String os = diskImageResource
						.getProperty(Omn_domain_pc.hasDiskimageOS).getObject()
						.asLiteral().getString();
				diskImageContents.setOs(os);
			}

			sliver.getAnyOrDiskImage().add(
					new ObjectFactory().createDiskImage(diskImageContents));
		}

	}

	public static void setComponentDetails(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource()
					.getProperty(Omn_lifecycle.hasID).getString());
		}
	}

	public static void setComponentId(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			RDFNode implementedBy = resource.getResource()
					.getProperty(Omn_lifecycle.implementedBy).getObject();

			String componentId = implementedBy.toString();
			node.setComponentId(CommonMethods.generateUrnFromUrl(componentId,
					"node"));

			// override above way of setting component ID, if the specific
			// property hasComponentID is set
			// if (resource.getResource()
			// .hasProperty(Omn_lifecycle.hasComponentID)) {
			// componentId = resource.getResource()
			// .getProperty(Omn_lifecycle.hasComponentID).getObject()
			// .asLiteral().getString();
			// node.setComponentId(componentId);
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

}