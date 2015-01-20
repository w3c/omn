package info.openmultinet.ontology.translators.tosca;

import java.util.List;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.NodeContents;
import info.openmultinet.ontology.translators.geni.jaxb.manifest.RSpecContents;
import info.openmultinet.ontology.translators.tosca.jaxb.Definitions;
import info.openmultinet.ontology.translators.tosca.jaxb.ObjectFactory;
import info.openmultinet.ontology.translators.tosca.jaxb.TEntityTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TExtensibleElements;
import info.openmultinet.ontology.translators.tosca.jaxb.TNodeTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TServiceTemplate;
import info.openmultinet.ontology.translators.tosca.jaxb.TTopologyTemplate;
import info.openmultinet.ontology.vocabulary.Omn;

import javax.xml.bind.JAXBException;
import javax.xml.namespace.QName;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.RDF;

public class OMN2Tosca extends AbstractConverter {

	public static String getTopology(Model model) throws JAXBException {
		Definitions definitions = new ObjectFactory().createDefinitions();
		List<TExtensibleElements> stuff = definitions.getServiceTemplateOrNodeTypeOrNodeTypeImplementation();
		TServiceTemplate serviceTemplate = new ObjectFactory().createTServiceTemplate();
		
		TTopologyTemplate topology = new ObjectFactory().createTTopologyTemplate();
		List<TEntityTemplate> nodeTemplates = topology.getNodeTemplateOrRelationshipTemplate();
		
		TNodeTemplate nodeTemplate = new ObjectFactory().createTNodeTemplate();
		
		QName type = new QName("foo");
		nodeTemplate.setType(type );
		nodeTemplates.add(nodeTemplate);
		
		serviceTemplate.setTopologyTemplate(topology);
		stuff.add(serviceTemplate);
		
		return toString(definitions, "info.openmultinet.ontology.translators.tosca.jaxb");
	}

	private static void model2rspec(Model model, RSpecContents manifest) throws InvalidModelException {
		List<Resource> groups = model.listSubjectsWithProperty(RDF.type,
				Omn.Group).toList();
		validateModel(groups);

		Resource group = groups.iterator().next();
		List<Statement> resources = group.listProperties(Omn.hasResource)
				.toList();

//		convertStatementsToNodesAndLinks(manifest, resources);
	}
/*
	private static void convertStatementsToNodesAndLinks(
			RSpecContents manifest, List<Statement> resources) {
		
		for (Statement resource : resources) {
			NodeContents node = new NodeContents();

			setType(resource, node);
			
			manifest.getAnyOrNodeOrLink().add(new ObjectFactory().createNode(node));
		}
	}
*/
	private static void setType(Statement resource,
			TNodeTemplate node) {
		//add TOSCA Ontology
		//node.setName(resource.getResource().getRequiredProperty(p);
	}

}
