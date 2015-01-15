package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.geni.jaxb.request.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.request.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;

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

	public static Model getModel(InputStream input) throws JAXBException {
		
		JAXBContext context = JAXBContext.newInstance(RSpecContents.class);
		Unmarshaller unmarshaller = context.createUnmarshaller();
		JAXBElement<RSpecContents> rspec = unmarshaller.unmarshal(new StreamSource(input), RSpecContents.class);
		RSpecContents request = rspec.getValue();

		Model model = ModelFactory.createDefaultModel();
		Resource topology = model.createResource("http://open-multinet.info/example#request");
		topology.addProperty(RDF.type, Omn_lifecycle.Request);
		
		
		//@todo: on purpose clean and small - have to check exceptions to get links and others
		List<JAXBElement<NodeContents>> nodes = (List) request.getAnyOrNodeOrLink();
		for (JAXBElement<NodeContents> nodeObject : nodes) {
			NodeContents node = nodeObject.getValue();
			model.createResource(node.getClientId());
		}
		
		return model;
	}

	private static String toString(Object jaxbObject) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("info.openmultinet.ontology.translators.geni.jaxb.request");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter stringWriter = new StringWriter();
		jaxbMarshaller.marshal(jaxbObject, stringWriter);

		return stringWriter.toString();
	}
}

