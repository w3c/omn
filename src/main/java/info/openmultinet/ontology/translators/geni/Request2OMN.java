package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.RDF;

public class Request2OMN {

	private static final Logger LOG = Logger.getLogger(Request2OMN.class.getName());

	public static Model getModel(InputStream input) throws JAXBException, InvalidModelException {
	
		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model.createResource("http://open-multinet.info/example#request");
		topology.addProperty(RDF.type, Omn_lifecycle.Request);
		
		extractNodes(request, topology);
		
		return model;
	}

	private static void extractNodes(RSpecContents request, Resource topology) {
		
		List<JAXBElement<NodeContents>> nodes;
		try {
			nodes = (List) request.getAnyOrNodeOrLink();
			for (JAXBElement<NodeContents> nodeObject : nodes) {
				NodeContents node = nodeObject.getValue();
				Resource n = topology.getModel().createResource(node.getClientId());
				n.addProperty(RDF.type, Omn.Resource);
				n.addLiteral(Omn.isResourceOf, topology.getURI());
				topology.addProperty(Omn.hasResource, n.getURI());
			}	
		} catch (ClassCastException e) {
			LOG.finer(e.getMessage());
		}
		
	}

	public static String toString(Object jaxbObject) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("info.openmultinet.ontology.translators.geni.jaxb.request");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter stringWriter = new StringWriter();
		jaxbMarshaller.marshal(jaxbObject, stringWriter);

		return stringWriter.toString();
	}
}

