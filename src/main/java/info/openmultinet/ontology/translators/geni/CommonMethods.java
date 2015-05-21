package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URI;

import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class CommonMethods {

	public static String generateUrnFromUrl(String url, String type) {
		// http://groups.geni.net/geni/wiki/GeniApiIdentifiers
		// urn:publicid:IDN+<authority string>+<type>+<name>
		// type can be interface, link or node

		if (url == null) {
			return "";
		}

		URI uri = URI.create(url);
		if (uri.getScheme() == null) {
			return "";
		}

		if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {

			// AbstractConverter.LOG.info(uri.getScheme() + ": " +
			// uri.toString());

			String urn = "";
			String host = urlToGeniUrn(uri.getHost());
			String path = urlToGeniUrn(uri.getPath());
			String fragment = urlToGeniUrn(uri.getFragment());
			String scheme = urlToGeniUrn(uri.getScheme());

			urn = "urn:publicid:IDN+" + host + "+" + urlToGeniUrn(type) + "+"
					+ scheme + "%3A%2F%2F" + host + path;

			if (fragment != null && !fragment.equals("")) {
				urn += "%23" + fragment;
			}

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
		if (uri == null) {
			return "";
		}

		if (uri.getScheme().equals("urn")) {

			String url = "";
			String[] parts = urn.split("\\+");

			if (parts.length > 1) {
				if (parts.length > 3) {
					if (AbstractConverter.isUrl(geniUrntoUrl(parts[3]))) {
						String http = geniUrntoUrl(parts[3]);
						url += http;
					} else {
						return urn;
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

}