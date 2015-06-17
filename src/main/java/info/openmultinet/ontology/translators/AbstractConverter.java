package info.openmultinet.ontology.translators;

import java.net.URI;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Resource;

public abstract class AbstractConverter {

	protected final static String VENDOR = "omnlib";
	public final static String TTL = "ttl";
	public final static String RSPEC_REQUEST = "request";
	public final static String RSPEC_MANIFEST = "manifest";
	public final static String RSPEC_ADVERTISEMENT = "advertisement";
	public static final String TOSCA = "tosca";
	public static final String ANYFORMAT = "to";
	protected static final String NAMESPACE = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger.getLogger(AbstractConverter.class
			.getName());

	protected static void validateModel(final List<Resource> groups)
			throws InvalidModelException {
		if (groups.isEmpty()) {
			throw new InvalidModelException("No group in model found");
		}
		if (groups.size() > 1) {
			throw new InvalidModelException("Found '" + groups.size()
					+ "' groups, which is more than one");
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

	/**
	 * Returns whether the URI is a generic OWL/RDFS/OMN class or not
	 * @param uri
	 * @return
	 */
	public static boolean nonGeneric(String uri) {
		if (uri == null) {
			return true;
		}

		boolean nonGeneric = true;

		if (uri.equals("http://www.w3.org/2002/07/owl#Thing")) {
			nonGeneric = false;
		}

		if (uri.equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
			nonGeneric = false;
		}

		if (uri.equals("http://www.w3.org/2002/07/owl#NamedIndividual")) {
			nonGeneric = false;
		}

		if (uri.equals("http://open-multinet.info/ontology/omn-resource#Node")) {
			nonGeneric = false;
		}

		if (uri.equals("http://open-multinet.info/ontology/omn#Resource")) {
			nonGeneric = false;
		}

		if (uri.equals("http://open-multinet.info/ontology/omn-resource#NetworkObject")) {
			nonGeneric = false;
		}

		return nonGeneric;
	}

	/**
	 * Returns the name of resource from a URL
	 * 
	 * @param url
	 * @return
	 */
	public static String getName(String url) {

		String name = "";
		if (url == null) {
			return name;
		}

		URI uri = URI.create(url);

		if (uri.getScheme() != null) {
			if (uri.getScheme().equals("http")) {

				if (url.contains("#")) {
					name = uri.getFragment();
				} else {
					String[] parts = url.split("/");
					name = parts[parts.length - 1];
				}
			}
		} else {
			name = url;
		}
		return name;
	}

	/**
	 * Method to determine whether a given string is a URL or not
	 * 
	 * @param string
	 * @return boolean, true if string is a URL
	 */
	public static boolean isUrl(String url) {

		URI uri = null;
		uri = URI.create(url);

		if (uri != null) {
			if (uri.getScheme() != null) {
				if (uri.getScheme().equals("http")
						|| uri.getScheme().equals("https")) {
					return true;
				}
			}
		}
		return false;

	}

	/**
	 * Convert XMLGregorianCalendar to XSDDateTime
	 * 
	 * @param time
	 * @return
	 */
	public static XSDDateTime xmlToXsdTime(XMLGregorianCalendar time) {

		Calendar timeCalendar = time.toGregorianCalendar();
		XSDDateTime timeXSDDateTime = new XSDDateTime(timeCalendar);
		return timeXSDDateTime;
	}

	/**
	 * Method for converting XSDDateTime to XMLGregorianCalendar
	 * 
	 * @param XSDDateTime
	 *            time
	 * @return XMLGregorianCalendar time
	 */
	public static XMLGregorianCalendar xsdToXmlTime(XSDDateTime time) {

		Calendar timeCalendar = time.asCalendar();
		GregorianCalendar gc = new GregorianCalendar();
		gc.setTimeInMillis(timeCalendar.getTimeInMillis());
		TimeZone timezone = timeCalendar.getTimeZone();
		gc.setTimeZone(timezone);
		XMLGregorianCalendar xc = null;
		try {
			xc = DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
		} catch (DatatypeConfigurationException e) {
			e.printStackTrace();
		}

		return xc;
	}

}
