package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.LinkContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.InputStream;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Advertisement2OMN extends AbstractConverter {

	private static final String PREFIX = "http://open-multinet.info/omnlib/converter";
	private static final Logger LOG = Logger.getLogger(Advertisement2OMN.class
			.getName());

	@SuppressWarnings("rawtypes")
	public static Model getModel(InputStream input) throws JAXBException,
			InvalidModelException {

		RSpecContents rspecRequest = getRspec(input);

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model.createResource(PREFIX + "#advertisement");
		topology.addProperty(RDF.type, Omn_lifecycle.Offering);

		@SuppressWarnings("unchecked")
		List<JAXBElement<?>> rspecObjects = (List) rspecRequest.getAnyOrNodeOrLink();
		for (Object rspecObject : rspecObjects) {
			tryExtractNode(rspecObject, topology);
			tryExtractLink(rspecObject, topology);
		}

		return model;
	}

	private static RSpecContents getRspec(InputStream input)
			throws JAXBException {
		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();
		return request;
	}

	private static void tryExtractLink(Object rspecObject, Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<LinkContents> nodeJaxb = (JAXBElement<LinkContents>) rspecObject;
			LinkContents rspecLink = nodeJaxb.getValue();
			Resource omnLink = topology.getModel().createResource(
					rspecLink.getComponentId());
			omnLink.addProperty(RDF.type, Omn_resource.Link);
			omnLink.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, omnLink);
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}

	private static void tryExtractNode(Object object, Resource topology) {
		try {
			@SuppressWarnings("unchecked")
			JAXBElement<NodeContents> nodeJaxb = (JAXBElement<NodeContents>) object;
			NodeContents rspecNode = nodeJaxb.getValue();
			Resource omnNode = topology.getModel().createResource(
					rspecNode.getComponentId());
			omnNode.addProperty(RDF.type, Omn_resource.Node);
			omnNode.addProperty(Omn.isResourceOf, topology);
			topology.addProperty(Omn.hasResource, omnNode);
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}
}
