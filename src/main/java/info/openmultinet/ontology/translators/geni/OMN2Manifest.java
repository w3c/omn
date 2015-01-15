package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.geni.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RspecTypeContents;
import info.openmultinet.ontology.vocabulary.Omn;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class OMN2Manifest extends AbstractConverter {

	public static String getRSpec(Model model) throws JAXBException, InvalidModelException {
		RSpecContents rspecContent = new RSpecContents();
		rspecContent.setType(RspecTypeContents.MANIFEST);
		model2rspec(model, rspecContent);
		JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(rspecContent);
		return toString(rspec);
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
			NodeContents nodeContent = new NodeContents();

			nodeContent.setClientId(resource.getResource().getURI());
			nodeContent.setComponentName(resource.getResource().getLocalName());

			JAXBElement<NodeContents> node = new ObjectFactory()
					.createNode(nodeContent);
			manifest.getAnyOrNodeOrLink().add(node);
		}
	}

	private static String toString(Object jaxbObject) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext
				.newInstance("info.openmultinet.ontology.translators.geni.jaxb.manifest");
		Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		StringWriter stringWriter = new StringWriter();
		jaxbMarshaller.marshal(jaxbObject, stringWriter);

		return stringWriter.toString();
	}
}
