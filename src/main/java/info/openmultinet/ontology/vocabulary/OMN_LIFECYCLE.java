package info.openmultinet.ontology.vocabulary;

import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class OMN_LIFECYCLE {

	private static final String BASE_URI = "http://open-multinet.info/ontology/omn-lifecycle#";
	public static final Resource Request = ResourceFactory.createResource(BASE_URI + "Request");
	public static String getURI() {return BASE_URI;}
}
