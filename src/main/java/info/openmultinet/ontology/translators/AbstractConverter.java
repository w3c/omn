package info.openmultinet.ontology.translators;

import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
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
	protected static final String NAMESPACE = "http://open-multinet.info/example#";

	protected static void validateModel(final List<Resource> groups)
			throws InvalidModelException {
		if (groups.isEmpty()) {
			throw new InvalidModelException("No group in model found");
		}
		if (groups.size() > 1) {
			throw new InvalidModelException(
					"Found '" + groups.size() + "' groups, which is more than one");
		}
	}

	public static String toString(final Object jaxbObject,
			final String namespaces) throws JAXBException {
		final JAXBContext jaxbContext = JAXBContext.newInstance(namespaces);
		final Marshaller jaxbMarshaller = jaxbContext.createMarshaller();
		jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		final StringWriter stringWriter = new StringWriter();
		jaxbMarshaller.marshal(jaxbObject, stringWriter);

		return stringWriter.toString();
	}

	public static String toString(final String filename) throws IOException {
		StringBuffer result = new StringBuffer();
		final InputStream inputStream = AbstractConverter.class
				.getResourceAsStream(filename);
		final BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		while ((line = br.readLine()) != null) {
			result.append(line).append("\n");
		}
		return result.toString();
	}
}
