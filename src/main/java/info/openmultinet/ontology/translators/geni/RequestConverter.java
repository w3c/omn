package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.request.*;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents.SliverType;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
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
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class RequestConverter extends AbstractConverter {

	public static final String JAXB = "info.openmultinet.ontology.translators.geni.jaxb.request";
	private static final Logger LOG = Logger.getLogger(RequestConverter.class
			.getName());

	public static String getRSpec(final Model model) throws JAXBException,
			InvalidModelException {
		final RSpecContents request = new RSpecContents();
		request.setType(RspecTypeContents.REQUEST);
		request.setGeneratedBy(AbstractConverter.VENDOR);
		RequestConverter.setGeneratedTime(request);

		RequestConverter.model2rspec(model, request);

		final JAXBElement<RSpecContents> rspec = new ObjectFactory()
		.createRspec(request);
		return AbstractConverter.toString(rspec, RequestConverter.JAXB);
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
			RequestConverter.LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(final Model model,
			final RSpecContents manifest) throws InvalidModelException {
		final List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		AbstractConverter.validateModel(groups);

		final Resource group = groups.iterator().next();
		final List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		RequestConverter.convertStatementsToNodesAndLinks(manifest, resources);
	}

	private static void convertStatementsToNodesAndLinks(
			final RSpecContents manifest, final List<Statement> resources) {

		for (final Statement resource : resources) {
			final NodeContents node = new NodeContents();

			RequestConverter.setComponentDetails(resource, node);
			RequestConverter.setComponentId(resource, node);
			if (resource.getResource().hasProperty(Omn_resource.isExclusive)) {
				final boolean isExclusive = resource.getResource()
						.getProperty(Omn_resource.isExclusive).getBoolean();
				node.setExclusive(isExclusive);	
			}
			
			SliverType sliverType = new ObjectFactory().createNodeContentsSliverType();
			sliverType.setName(resource.getProperty(RDF.type).getObject().toString());
			JAXBElement<SliverType> sliver = new ObjectFactory ().createNodeContentsSliverType(sliverType );
			node.getAnyOrRelationOrLocation().add(sliver);
			
			manifest.getAnyOrNodeOrLink().add(
					new ObjectFactory().createNode(node));
		}
	}

	private static void setComponentDetails(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.hasID)) {
			node.setClientId(resource.getResource().getProperty(Omn_lifecycle.hasID).getString());	
		}
		
	}

	private static void setComponentId(final Statement resource,
			final NodeContents node) {
		if (resource.getResource().hasProperty(Omn_lifecycle.implementedBy)) {
			node.setComponentId(resource.getResource().getProperty(Omn_lifecycle.implementedBy).getObject().toString());
		}
	}

	public static Model getModel(final InputStream input) throws JAXBException,
	InvalidModelException {

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

		RequestConverter.extractNodes(request, topology);

        NetworkTopologyExtractor.extractTopologyInformation(request, topology);

		return model;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void extractNodes(final RSpecContents request,
			final Resource topology) {

		List<JAXBElement<NodeContents>> nodes;
		try {
			nodes = (List) request.getAnyOrNodeOrLink();
			for (final JAXBElement<NodeContents> nodeObject : nodes) {
				final NodeContents node = nodeObject.getValue();
				final Model model = topology.getModel();
				final Resource omnResource = model
						.createResource(AbstractConverter.NAMESPACE
								+ node.getClientId());

                List<Object> anyOrRelationOrLocation = node.getAnyOrRelationOrLocation();
                if(anyOrRelationOrLocation.size()>0) {
                    for (Object o : anyOrRelationOrLocation) {
                        if (o instanceof JAXBElement) {
                            JAXBElement element = (JAXBElement) o;
                            if (element.getDeclaredType().equals(NodeContents.SliverType.class)) {
                                NodeContents.SliverType sliverType = (NodeContents.SliverType) element.getValue();
//                                omnResource.addProperty(RDF.type, model.createResource(sliverType.getName()));
                                if(sliverType.getName().contains(":"))
                                    omnResource.addProperty(RDF.type, model.createResource(sliverType.getName()));
                                else
                                    omnResource.addProperty(RDF.type, model.createResource("http://open-multinet.info/example#"+
                                            sliverType.getName()));
                            }


                        }
                    }
                }else{
                    omnResource.addProperty(RDF.type, Omn_resource.Node);
                }

				omnResource.addProperty(Omn_lifecycle.hasID, node.getClientId());
				if (null != node.getComponentId() && ! node.getComponentId().isEmpty())
					omnResource.addProperty(Omn_lifecycle.implementedBy, model.createResource(node.getComponentId()));
				omnResource.addProperty(Omn.isResourceOf, topology);
				if (null != node.isExclusive()) {
					omnResource.addProperty(Omn_resource.isExclusive,
							model.createTypedLiteral(node.isExclusive()));
				}
				topology.addProperty(Omn.hasResource, omnResource);
				//todo: details such as sliver type
				//List<Object> details = node.getAnyOrRelationOrLocation();
			}
		} catch (final ClassCastException e) {
			RequestConverter.LOG.finer(e.getMessage());
		}
	}

    private static class NetworkTopologyExtractor {

        static void extractTopologyInformation(final RSpecContents request, final Resource topology) {

            List<JAXBElement<NodeContents>> xmlElements;
            Model outputModel = topology.getModel();

            try {
                xmlElements = (List) request.getAnyOrNodeOrLink();
                for (JAXBElement element : xmlElements) {

                    //If it's a node, then extract the node information and its corresponding interfaces
                    if (element.getDeclaredType() == NodeContents.class) {
                        JAXBElement<NodeContents> nodeObject = (JAXBElement<NodeContents>) element;

                        NodeContents node = nodeObject.getValue();


                        Resource omnResource = outputModel
                                .createResource("http://open-multinet.info/example#"
                                        + node.getClientId());
//                        omnResource.addProperty(RDF.type, Nml.Node);
                        omnResource.addProperty(RDF.type, Omn_resource.Node);

                        List<JAXBElement<InterfaceContents>> interfaces = (List) node.getAnyOrRelationOrLocation();
                        for (JAXBElement<InterfaceContents> interfaceContent : interfaces) {
                            try {
                                InterfaceContents content = interfaceContent.getValue();
                                Resource interfaceResource = outputModel.createResource("http://open-multinet.info/example#" + content.getClientId());
//                                interfaceResource.addProperty(RDF.type, Nml.Port);
//                                omnResource.addProperty(Nml.hasPort, interfaceResource);

                                interfaceResource.addProperty(RDF.type, Omn_resource.Interface);
                                omnResource.addProperty(Omn_resource.hasInterface, interfaceResource);
                            } catch (ClassCastException exp) {

                            }
                        }
                    }
                    //If it's an interface, then extract the node information and its corresponding interfaces
                    else if (element.getDeclaredType() == LinkContents.class) {

                        JAXBElement<LinkContents> linkObject = (JAXBElement<LinkContents>) element;
                        LinkContents link = linkObject.getValue();

                        Resource linkResource = outputModel
                                .createResource("http://open-multinet.info/example#"
                                        + link.getClientId());
//                        linkResource.addProperty(RDF.type, Nml.Link);
                        linkResource.addProperty(RDF.type, Omn_resource.Link);

                        //Get source and sink interfaces
                        List<JAXBElement<InterfaceRefContents>> interfaces = (List) link.getAnyOrPropertyOrLinkType();
                        for (JAXBElement<InterfaceRefContents> interfaceRefContents : interfaces) {
                            try {
                                InterfaceRefContents content = interfaceRefContents.getValue();
                                Resource interfaceResource = outputModel.createResource("http://open-multinet.info/example#" + content.getClientId());
//                                interfaceResource.addProperty(Nml.isSink, linkResource);
//                                interfaceResource.addProperty(Nml.isSource, linkResource);

                                interfaceResource.addProperty(Omn_resource.isSink, linkResource);
                                interfaceResource.addProperty(Omn_resource.isSource, linkResource);

//                            linkResource.addProperty(Nml.hasPort, interfaceResource);
                            } catch (ClassCastException exp) {

                            }
                        }

                    }
                }
            } catch (ClassCastException e) {
                LOG.warning(e.getMessage());
            }
        }
    }

}
