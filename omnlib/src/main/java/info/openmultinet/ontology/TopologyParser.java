package info.openmultinet.ontology;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn;

import java.io.InputStream;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class TopologyParser extends Parser {

	public TopologyParser(final InputStream input) throws InvalidModelException {
		super(input);
	}

	public TopologyParser(InputStream input, String inputType)
			throws InvalidModelException {
		super(input, inputType);
	}

	public ResIterator getGroups() {
		return this.infModel.listSubjectsWithProperty(RDF.type, Omn.Group);
	}

	public ResIterator getResources() {
		return this.infModel.listSubjectsWithProperty(Omn.hasResource);
	}

	public StmtIterator getResources(final Resource group) {
		return group.listProperties(Omn.hasResource);
	}

}
