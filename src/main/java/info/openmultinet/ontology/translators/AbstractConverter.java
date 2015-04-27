package info.openmultinet.ontology.translators;

import java.net.URI;

import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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

	public static String generateUrnFromUrl(String url, String type) {
		// http://groups.geni.net/geni/wiki/GeniApiIdentifiers
		// urn:publicid:IDN+<authority string>+<type>+<name>
		// type can be interface, link or node

		if (url == null) {
			return "";
		}

		URI uri = URI.create(url);

		if (uri.getScheme().equals("http")) {
			String urn = "";
			String host = urlToGeniUrn(uri.getHost());
			String path = urlToGeniUrn(uri.getPath());
			String fragment = urlToGeniUrn(uri.getFragment());

			urn = "urn:publicid:IDN+" + host + "+" + urlToGeniUrn(type) + "+"
					+ host + path + fragment;

			return urn;
		} else {
			return url;
		}

	}

	private static String urlToGeniUrn(String dirtyString) {

		// http://groups.geni.net/geni/wiki/GeniApiIdentifiers
		// From Transcribe to
		// leading and trailing whitespace trim
		// whitespace collapse to a single '+'
		// '//' ':'
		// '::' ';'
		// '+' '%2B'
		// ":' '%3A'
		// '/' '%2F'
		// ';' '%3B'
		// ''' '%27'
		// '?' '%3F'
		// '#' '%23'
		// '%' '%25

		if (dirtyString == null) {
			return "";
		}
		String cleanString;
		cleanString = dirtyString.replaceAll(";", "%3B");
		cleanString = cleanString.replaceAll("%", "%25");
		cleanString = cleanString.replaceAll(":", "%3A");
		cleanString = cleanString.replaceAll("\\+", "%2B");
		cleanString = cleanString.replaceAll("//", ":");
		cleanString = cleanString.replaceAll("::", ";");
		cleanString = cleanString.replaceAll("/", "%2F");
		cleanString = cleanString.replaceAll("'", "%27");
		cleanString = cleanString.replaceAll("\\?", "%3F");
		cleanString = cleanString.replaceAll("#", "%23");
		cleanString = cleanString.trim();
		cleanString = cleanString.replaceAll("\\s+", "+");

		return cleanString;
	}

	public static String generateUrlFromUrn(String urn) {

		if (urn == null) {
			return "";
		}

		URI uri = URI.create(urn);

		if (uri.getScheme().equals("urn")) {

			String url = "";
			String[] parts = urn.split("\\+");

			if (parts.length > 1) {
				String part1 = geniUrntoUrl(parts[1]);
				String part2 = "";

				url = "http://";// + part1;

				if (parts.length > 3) {
					part2 = geniUrntoUrl(parts[3]);

					if (part2 != "") {
						url += part2;
						// url += "#" + part2;
					}
				}
			}

			return url;
		} else {
			return urn;
		}
	}

	private static String geniUrntoUrl(String dirtyString) {

		if (dirtyString == null) {
			return "";
		}
		String cleanString;

		cleanString = dirtyString.replaceAll("\\+", " ");
		cleanString = cleanString.replaceAll("%23", "#");
		cleanString = cleanString.replaceAll("%3F", "?");
		cleanString = cleanString.replaceAll("%27", "'");
		cleanString = cleanString.replaceAll("%2F", "/");
		cleanString = cleanString.replaceAll(";", "::");
		cleanString = cleanString.replaceAll(":", "//");
		cleanString = cleanString.replaceAll("%2B", "+");
		cleanString = cleanString.replaceAll("%3A", ":");
		cleanString = cleanString.replaceAll("%25", "%");
		cleanString = cleanString.replaceAll("%3B", ";");

		return cleanString;
	}

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

	public static boolean isUrl(String url) {

		URI uri = URI.create(url);

		if (uri.getScheme() != null) {
			if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {
				return true;
			}
		} 
		
		return false;

	}
}
