package info.openmultinet.ontology.translators;

import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.StringWriter;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractConverter {

	protected final static String VENDOR = "omnlib";
	public final static String TTL = "ttl";
	public final static String RSPEC_REQUEST = "request";
	public final static String RSPEC_MANIFEST = "manifest";
	public final static String RSPEC_ADVERTISEMENT = "advertisement";
	public static final String TOSCA = "tosca";
	
	protected static void validateModel(List<Resource> groups) throws InvalidModelException {
		if (groups.isEmpty())
			throw new InvalidModelException("No group in model found");
		if (groups.size() > 1)
			throw new InvalidModelException("More than one group in model found");
	}

	protected static String toString(Object jaxbObject, String namespaces)
			throws JAXBException {
				JAXBContext jaxbContext = JAXBContext
						.newInstance(namespaces);
				Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
				jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
				StringWriter stringWriter = new StringWriter();
				jaxbMarshaller.marshal(jaxbObject, stringWriter);
			
				return stringWriter.toString();
			}

}
