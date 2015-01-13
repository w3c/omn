package info.openmultinet.ontology.vocabulary;

import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;



public class OMN {

	private static final String BASE_URI = "http://open-multinet.info/ontology/omn#";
	public static final Resource Group = ResourceFactory.createResource(BASE_URI + "Group");
	public static final Property hasResource = ResourceFactory.createProperty(BASE_URI + "hasResource");
	public static String getURI() {return BASE_URI;}
}
