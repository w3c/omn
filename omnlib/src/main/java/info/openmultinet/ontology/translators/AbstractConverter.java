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
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.GregorianCalendar;
import java.util.LinkedList;
import java.util.List;
import java.util.TimeZone;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.jboss.vfs.VirtualFile;

import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import info.openmultinet.ontology.exceptions.InvalidModelException;

public abstract class AbstractConverter {

	public static final String FOLDER_RULES = "rules";
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
		this.reasoner = new GenericRuleReasoner(getAllRules());
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

	public static List<Rule> getAllRules() {
		List<Rule> rules = new LinkedList<Rule>();

		List<URI> listOfRuleSets;
		try {
			listOfRuleSets = getResourceListing(AbstractConverter.FOLDER_RULES);
			for (URI ruleSet : listOfRuleSets) {
				// String newRuleSet = IOUtils.toString(ruleSet,
				// Charset.defaultCharset());
				BufferedReader br = new BufferedReader(new InputStreamReader(
						ruleSet.toURL().openStream(), Charset.defaultCharset()));
				for (Rule rule : Rule
						.parseRules(Rule.rulesParserFromReader(br))) {
					rules.add(rule);
				}
			}
		} catch (IOException | URISyntaxException e) {
			LOG.info("Could not get inferencing rules: " + e.getMessage()
					+ " - " + e.toString());
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
				.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn-resource#Node")) {
			nonGeneric = false;
		} else if (uri
				.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn#Resource")) {
			nonGeneric = false;
		} else if (uri
				.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn-resource#NetworkObject")) {
			nonGeneric = false;
		} else if (uri.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn#Group")) {
			nonGeneric = false;
		} else if (uri.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn#Service")) {
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

		try {
			uri = URI.create(url);
		} catch (java.lang.IllegalArgumentException
				| java.lang.NullPointerException e) {
			return false;
		}

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

		try {
			uri = URI.create(urn);
		} catch (java.lang.IllegalArgumentException
				| java.lang.NullPointerException e) {
			return false;
		}

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

	public static List<URI> getResourceListing(String path) throws IOException,
			URISyntaxException {
		final File fileName = new File(AbstractConverter.class
				.getProtectionDomain().getCodeSource().getLocation().getPath());
		final List<URI> files = new LinkedList<URI>();
		LOG.info("Looking for files in: " + fileName);

		if (fileName.isFile()) {
			addFilesFromJar(path, fileName, files);
		} else {
			LOG.info("I think we're not in a simple jar file...");
			URL url = AbstractConverter.class.getResource("/" + path);
			if (url != null) {
				try {
					LOG.info("Found URL: " + url);
					String protocol = url.getProtocol();

					if ("vfs".equalsIgnoreCase(protocol)) {
						URLConnection conn = url.openConnection();
						VirtualFile virtualFile = (VirtualFile) conn
								.getContent();
						String realJarFile = virtualFile.getPhysicalFile()
								.getParentFile().getParentFile()
								+ File.separator + fileName.getName();
						LOG.info("Guessing real file: " + realJarFile);
						addFilesFromJar(path, new File(realJarFile), files);
					} else if ("file".equalsIgnoreCase(protocol)) {
						File folder = new File(url.toURI());
						LOG.info("Looking now for files in: " + folder);
						for (File file : folder.listFiles()) {
							LOG.info("Adding: " + file);
							files.add(file.toURI());
						}
					} else {
						throw new URISyntaxException(url.toString(),
								"Unsupported protocol: " + protocol);
					}
				} catch (URISyntaxException ex) {
					LOG.log(Level.WARNING, "Should not happen", ex);
				} catch (IllegalArgumentException ex) {
					LOG.log(Level.WARNING, "Couldn't read file from: " + url,
							ex);
				}
			}
		}
		return files;
	}

	public static void addFilesFromJar(String path, final File fileName,
			final List<URI> files) throws IOException, URISyntaxException {
		LOG.info("I think we're in a jar file...");
		final JarFile jar = new JarFile(fileName);
		final Enumeration<JarEntry> entries = jar.entries();
		while (entries.hasMoreElements()) {
			final String name = entries.nextElement().getName();
			if (name.startsWith(path + "/") && !name.endsWith("/")) {
				String newName = "jar:" + fileName.toURI() + "!/" + name;
				LOG.info("Adding: " + newName);
				files.add(new URI(newName));
			}
		}
		jar.close();
	}

	public static void print(Model model) {
		RDFDataMgr.write(System.out, model, Lang.NT);
	}

}
