package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.util.List;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractConverter {

	protected final static String VENDOR = "omnlib";
	
	protected static void validateModel(List<Resource> groups) throws InvalidModelException {
		if (groups.isEmpty())
			throw new InvalidModelException("No group in model found");
		if (groups.size() > 1)
			throw new InvalidModelException("More than one group in model found");
	}

}
