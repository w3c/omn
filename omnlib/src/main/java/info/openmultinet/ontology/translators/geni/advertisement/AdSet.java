package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.CommonMethods;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.AvailableContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Cloud;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ComponentManager;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.HardwareTypeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.InterfaceRefContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkPropertyContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LocationContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents.SliverType.DiskImage;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeType;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Pc;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.Position3D;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RspecOpstate;
import info.openmultinet.ontology.vocabulary.Geo;
import info.openmultinet.ontology.vocabulary.Geonames;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_domain_pc;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.math.BigDecimal;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.PropertyNotFoundException;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

/**
 * Helper methods for converting from OMN model to ad RSpec. For native RSpec
 * elements.
 * 
 * @author robynloughnane
 *
 */
public class AdSet extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(AdSet.class.getName());

	public static void setNodesVerbose(Statement omnResource,
			RSpecContents advertisement) {
		StmtIterator canImplement = omnResource.getResource().listProperties(
				Omn_lifecycle.canImplement);

		while (canImplement.hasNext()) {
			Statement omnSliver = canImplement.next();

			final NodeContents geniNode = new NodeContents();

			SliverType sliver1;
			ObjectFactory of = new ObjectFactory();
			sliver1 = of.createNodeContentsSliverType();
			String sliverUri = omnSliver.getObject().asResource().getURI();
			AdSetExt.setOsco(omnSliver, geniNode);
			
			// setOsco(omnSliver, sliver1);
			sliver1.setName(sliverUri);
			
			Resource sliverTypeResource = omnSliver.getObject().asResource();
			if (sliverTypeResource != null) {
				setDiskImage(sliverTypeResource, sliver1);
			}
			
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
			AdSetExt.setMonitoringService(omnResource, geniNode);
			setInterface(omnResource, geniNode);
			AdSetExt.setFd(omnResource, geniNode);
			AdSetExt.setTrivialBandwidth(omnResource, geniNode);

			ResIterator infrastructures = omnResource.getModel()
					.listResourcesWithProperty(Omn.isResourceOf,
							Omn_federation.Infrastructure);
			if (infrastructures.hasNext()) {
				Resource infrastructure = infrastructures.next();
				geniNode.setComponentManagerId(infrastructure.getURI());
			}

			advertisement.getAnyOrNodeOrLink().add(of.createNode(geniNode));
		}
	}

	public static void setHardwareTypesVerbose(Statement omnResource,
			NodeContents geniNode) {
		List<Object> geniNodeDetails = geniNode.getAnyOrRelationOrLocation();

		StmtIterator types = omnResource.getResource().listProperties(RDF.type);
		HardwareTypeContents hwType;

		while (types.hasNext()) {
			String rdfType = types.next().getResource().getURI();
			ObjectFactory of = new ObjectFactory();
			hwType = of.createHardwareTypeContents();
			hwType.setName(rdfType);
			if ((null != rdfType) && AbstractConverter.nonGeneric(rdfType)) {
				geniNodeDetails.add(of.createHardwareType(hwType));
			}
		}
	}

	public static void setComponentDetailsVerbose(Statement resource,
			NodeContents node) {
		if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
			node.setExclusive(resource.getResource()
					.getProperty(Omn_resource.isExclusive).getBoolean());
		}
	}

	public static void setCloud(Statement omnResource, NodeContents geniNode) {
		if (omnResource.getResource().hasProperty(RDF.type, Omn_resource.Cloud)) {
			Cloud cloud = new ObjectFactory().createCloud();
			geniNode.getAnyOrRelationOrLocation().add(cloud);
		}

	}

	public static void setLinkProperties(Statement resource, LinkContents link) {
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

	public static void setInterfaceRefs(Statement resource, LinkContents link) {
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

	public static void setLinkDetails(Statement resource, LinkContents link) {

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

	public static void setInterface(Statement resource,
			NodeContents nodeContents) {

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

			if (interfaceResource.hasProperty(Omn_lifecycle.hasComponentName)) {
				interfaceContents.setComponentName(interfaceResource
						.getProperty(Omn_lifecycle.hasComponentName)
						.getObject().asLiteral().getString());
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

	public static void setSliverTypes(Statement omnResource,
			RspecOpstate rspecOpstate) {
		// get sliver types
		StmtIterator canBeImplementBy = omnResource.getResource()
				.listProperties(Omn_lifecycle.canBeImplementedBy);
		info.openmultinet.ontology.translators.geni.jaxb.advertisement.SliverType sliver;

		List<Object> sliverTypeOrState = rspecOpstate.getSliverTypeOrState();
		while (canBeImplementBy.hasNext()) {
			Statement omnSliver = canBeImplementBy.next();
			ObjectFactory of = new ObjectFactory();
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

	public static void setAvailability(Statement omnResource,
			NodeContents geniNode) {
		ObjectFactory of = new ObjectFactory();
		AvailableContents availabilty = of.createAvailableContents();
		Resource resource = omnResource.getResource();

		if (resource.hasProperty(Omn_resource.isAvailable)) {
			availabilty.setNow(resource.getProperty(Omn_resource.isAvailable)
					.getBoolean());

			geniNode.getAnyOrRelationOrLocation().add(
					of.createAvailable(availabilty));
		}
	}

	public static void setLocation(Statement omnResource, NodeContents geniNode) {

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

	public static void setSliverTypes(Statement resource, NodeContents geniNode) {

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
				ObjectFactory of = new ObjectFactory();
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

	public static void setCpus(Resource sliverResource, SliverType sliver) {
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

	public static void setDiskImage(Resource sliverResource, SliverType sliver) {

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
				ObjectFactory of = new ObjectFactory();
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

	public static void setComponentDetails(final Statement resource,
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

	public static void setComponentManagerId(final Statement resource,
			final NodeContents node) {
		try {
			Statement managedBy = resource.getProperty(Omn_lifecycle.managedBy);
			node.setComponentManagerId(managedBy.getResource().getURI());
		} catch (PropertyNotFoundException e) {
			AdSet.LOG.finer(e.getMessage());
		}
	}

}
