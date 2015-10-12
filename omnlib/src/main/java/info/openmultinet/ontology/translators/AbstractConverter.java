package info.openmultinet.ontology.translators;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import info.openmultinet.ontology.exceptions.InvalidModelException;

public abstract class AbstractConverter {

	protected Reasoner reasoner = null;
	protected final static String VENDOR = "omnlib";
	public final static String RDFXML = "rdfxml";
	public final static String TTL = "ttl";
	public final static String JSON = "json-ld";
	public final static String RSPEC_REQUEST = "request";
	public final static String RSPEC_MANIFEST = "manifest";
	public final static String RSPEC_ADVERTISEMENT = "advertisement";
	public static final String TOSCA = "tosca";
	public static final String ANYFORMAT = "to";
	protected static final String NAMESPACE = "http://open-multinet.info/example#";
	private static final Logger LOG = Logger.getLogger(AbstractConverter.class
			.getName());

	public AbstractConverter() {
		super();
		try {
			this.reasoner = new GenericRuleReasoner(getAllRules());
		} catch (URISyntaxException | IOException e) {
			LOG.warning("Couldn't create reasoner: " + e);
		}
	}

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

		if (inputStream == null) {
			return null;
		}

		final BufferedReader br = new BufferedReader(new InputStreamReader(
				inputStream));
		String line = null;
		while ((line = br.readLine()) != null) {
			result.append(line).append("\n");
		}
		return result.toString();
	}

	public static List<Rule> getAllRules() throws URISyntaxException, IOException {
		String[] a = getResourceListing(AbstractConverter.class, "rules");
		List<Rule> rules = new LinkedList<Rule>();
		for (String x : a) {
			final String ruleFile = File.separator + "rules" + File.separator + x;
			String newRules = AbstractConverter.toString(ruleFile);
			for (Rule rule : Rule.parseRules(newRules)) {
				rules.add(rule);
			}
		}
		return rules;
	}
	/**
	 * Returns whether the URI is a generic OWL/RDFS/OMN class or not
	 *
	 * @param uri
	 * @return
	 */
	public static boolean nonGeneric(String uri) {
		if (uri == null) {
			return false;
		}

		boolean nonGeneric = true;

		if (uri.equals("http://www.w3.org/2002/07/owl#Thing")) {
			nonGeneric = false;
		} else if (uri.equals("http://www.w3.org/2000/01/rdf-schema#Resource")) {
			nonGeneric = false;
		} else if (uri.equals("http://www.w3.org/2002/07/owl#NamedIndividual")) {
			nonGeneric = false;
		} else if (uri
				.equals("http://open-multinet.info/ontology/omn-resource#Node")) {
			nonGeneric = false;
		} else if (uri
				.equals("http://open-multinet.info/ontology/omn#Resource")) {
			nonGeneric = false;
		} else if (uri
				.equals("http://open-multinet.info/ontology/omn-resource#NetworkObject")) {
			nonGeneric = false;
		} else if (uri.equals("http://open-multinet.info/ontology/omn#Group")) {
			nonGeneric = false;
		} else if (uri.equals("http://open-multinet.info/ontology/omn#Service")) {
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
	 * Method to determine whether a given string is a URN or not
	 *
	 * @param string
	 * @return boolean, true if string is a URN
	 */
	public static boolean isUrn(String urn) {

		URI uri = null;
		uri = URI.create(urn);

		if (uri != null) {
			if (uri.getScheme() != null) {
				if (uri.getScheme().equals("urn")) {
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

	/**
	   * List directory contents for a resource folder. Not recursive.
	   * This is basically a brute-force implementation.
	   * Works for regular files and also JARs.
	   *
	   * @author Greg Briggs
	   * @param clazz Any java class that lives in the same place as the resources you want.
	   * @param path Should end with "/", but not start with one.
	   * @return Just the name of each member item, not the full paths.
	   * @throws URISyntaxException
	   * @throws IOException
	   */
	  private static String[] getResourceListing(Class clazz, String path) throws URISyntaxException, IOException {
	      URL dirURL = clazz.getClassLoader().getResource(path);
	      if (dirURL != null && dirURL.getProtocol().equals("file")) {
	        /* A file path: easy enough */
	        return new File(dirURL.toURI()).list();
	      }

	      if (dirURL == null) {
	        /*
	         * In case of a jar file, we can't actually find a directory.
	         * Have to assume the same jar as clazz.
	         */
	        String me = clazz.getName().replace(".", "/")+".class";
	        dirURL = clazz.getClassLoader().getResource(me);
	      }

	      if (dirURL.getProtocol().equals("jar")) {
	        /* A JAR path */
	        String jarPath = dirURL.getPath().substring(5, dirURL.getPath().indexOf("!")); //strip out only the JAR file
	        JarFile jar = new JarFile(URLDecoder.decode(jarPath, "UTF-8"));
	        Enumeration<JarEntry> entries = jar.entries(); //gives ALL entries in jar
	        Set<String> result = new HashSet<String>(); //avoid duplicates in case it is a subdirectory
	        while(entries.hasMoreElements()) {
	          String name = entries.nextElement().getName();
	          if (name.startsWith(path)) { //filter according to the path
	            String entry = name.substring(path.length());
	            int checkSubdir = entry.indexOf("/");
	            if (checkSubdir >= 0) {
	              // if it is a subdirectory, we just return the directory name
	              entry = entry.substring(0, checkSubdir);
	            }
	            result.add(entry);
	          }
	        }
	        return result.toArray(new String[result.size()]);
	      }

	      throw new UnsupportedOperationException("Cannot list files for URL "+dirURL);
	  }

	public static void print(Model model) {
		Iterator<?> list = model.listStatements();
		while (list.hasNext()) {
		    System.out.println(" - " + list.next());
		}
	}

}
