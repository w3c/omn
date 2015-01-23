package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

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

public class Request2OMN {

	private static final Logger LOG = Logger.getLogger(Request2OMN.class
			.getName());

	public static Model getModel(InputStream input) throws JAXBException,
			InvalidModelException {

		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(
				new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model
				.createResource("http://open-multinet.info/example#request");
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
				Resource omnResource = topology.getModel().createResource(
						node.getComponentId());
				omnResource.addProperty(RDF.type, Omn.Resource);
				omnResource.addProperty(Omn.isResourceOf, topology);
				topology.addProperty(Omn.hasResource, omnResource);
			}
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
	}
}
