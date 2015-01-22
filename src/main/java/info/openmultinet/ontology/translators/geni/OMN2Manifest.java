package info.openmultinet.ontology.translators.geni;


import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.ObjectFactory;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RspecTypeContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.logging.Logger;

import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class OMN2Manifest extends AbstractConverter {

	private static final Logger LOG = Logger.getLogger(OMN2Manifest.class.getName());
	
	public static String getRSpec(Model model) throws JAXBException, InvalidModelException {
		RSpecContents manifest = new RSpecContents();
		manifest.setType(RspecTypeContents.MANIFEST);
		manifest.setGeneratedBy(VENDOR);
		setGeneratedTime(manifest);
		
		model2rspec(model, manifest);
		JAXBElement<RSpecContents> rspec = new ObjectFactory()
				.createRspec(manifest);
		return toString(rspec, "info.openmultinet.ontology.translators.geni.jaxb.manifest");
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

			manifest.getAnyOrNodeOrLink().add(new ObjectFactory().createNode(node));
		}
	}

	private static void setComponentDetails(Statement resource,
			NodeContents node) {
		node.setComponentId(resource.getResource().getURI());
		node.setComponentName(resource.getResource().getLocalName());
	}

	private static void setComponentManagerId(Statement resource,
			NodeContents node) {
		List<Statement> implementedBy = resource.getResource().listProperties(Omn_lifecycle.implementedBy).toList();
		for (Statement implementer : implementedBy) {
			node.setComponentManagerId(implementer.getResource().getURI());
		}
	}
}
