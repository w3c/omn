package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RspecTypeContents;
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
	private static final Logger LOG = Logger.getLogger(RequestConverter.class.getName());

	public static String getRSpec(Model model) throws JAXBException, InvalidModelException {
		RSpecContents request = new RSpecContents();
		request.setType(RspecTypeContents.REQUEST);
		request.setGeneratedBy(VENDOR);
		setGeneratedTime(request);
		
		model2rspec(model, request);
		
		JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(request);
		return toString(rspec, RequestConverter.JAXB);
	}

	private static void setGeneratedTime(RSpecContents manifest) {
		GregorianCalendar gregorianCalendar = new GregorianCalendar();
		gregorianCalendar.setTime(new Date(System.currentTimeMillis()));
		XMLGregorianCalendar xmlGrogerianCalendar;
		try {
			xmlGrogerianCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
			manifest.setGenerated(xmlGrogerianCalendar);
		} catch (DatatypeConfigurationException e) {
			LOG.info(e.getMessage());
		}
	}

	private static void model2rspec(Model model, RSpecContents manifest) throws InvalidModelException {
		List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		validateModel(groups);

		Resource group = groups.iterator().next();
		List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

		convertStatementsToNodesAndLinks(manifest, resources);
	}

	private static void convertStatementsToNodesAndLinks(
			RSpecContents manifest, List<Statement> resources) {
		
		for (Statement resource : resources) {
			NodeContents node = new NodeContents();

			setComponentDetails(resource, node);
			setComponentManagerId(resource, node);
			boolean isExclusive = resource.getResource().getProperty(Omn_resource.isExclusive).getBoolean();
			node.setExclusive(isExclusive);

			manifest.getAnyOrNodeOrLink().add(new ObjectFactory().createNode(node));
		}
	}

	private static void setComponentDetails(Statement resource,
			NodeContents node) {
		//node.setComponentId(resource.getResource().getURI());
		node.setClientId(resource.getResource().getLocalName());
	}

	private static void setComponentManagerId(Statement resource,
			NodeContents node) {
		List<Statement> implementedBy = resource.getResource().listProperties(Omn_lifecycle.implementedBy).toList();
		for (Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}


	public static Model getModel(InputStream input) throws JAXBException,
			InvalidModelException {

		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model
				.createResource(NAMESPACE + "request");
		topology.addProperty(RDF.type, Omn_lifecycle.Request);

		extractNodes(request, topology);

		return model;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static void extractNodes(RSpecContents request, Resource topology) {

		List<JAXBElement<NodeContents>> nodes;
		try {
			nodes = (List) request.getAnyOrNodeOrLink();
			for (JAXBElement<NodeContents> nodeObject : nodes) {
				NodeContents node = nodeObject.getValue();
				Model model = topology.getModel();
				Resource omnResource = model.createResource(
						NAMESPACE+node.getClientId());
				omnResource.addProperty(RDF.type, Omn_resource.Node);
				omnResource.addProperty(RDFS.label, node.getClientId());
				omnResource.addProperty(Omn.isResourceOf, topology);
				if (null != node.isExclusive())
					omnResource.addProperty(Omn_resource.isExclusive, model.createTypedLiteral(node.isExclusive()));
				topology.addProperty(Omn.hasResource, omnResource);
			}
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}
}
