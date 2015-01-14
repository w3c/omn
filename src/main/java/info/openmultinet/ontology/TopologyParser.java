package info.openmultinet.ontology;

import info.openmultinet.ontology.vocabulary.Omn;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class TopologyParser extends Parser {

	public TopologyParser(InputStream input) {
		super(input);
	}

	public ResIterator getGroups() {
		return this.model
				.listSubjectsWithProperty(RDF.type, Omn.Group);
	}

	public ResIterator getResources() {
		return this.model
				.listSubjectsWithProperty(Omn.hasResource);
	}

	public StmtIterator getResources(Resource group) {
		return group.listProperties(Omn.hasResource);
	}
	
}
