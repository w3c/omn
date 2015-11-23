package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import nu.xom.Builder;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.custommonkey.xmlunit.DetailedDiff;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.ElementNameAndAttributeQualifier;
import org.custommonkey.xmlunit.Validator;
import org.dom4j.DocumentException;
import org.dom4j.io.SAXReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class RSpecValidation {

	/**
	 * returns the number of non-recoverable XMLunit differences (errors) in
	 * proportion to the number of nodes in the rspec
	 * 
	 * @param input
	 *            RSpec as string
	 * @return
	 * @throws MissingRspecElementException
	 * @throws DeprecatedRspecVersionException
	 * @throws FileNotFoundException
	 * @throws InvalidRspecValueException
	 */
	static public double getProportionalError(String input)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

		String output = completeRoundtrip(input);
		String inputNew = null;
		String outputNew = null;

		// clean up input doc
		Document inputDoc = null;
		try {
			inputDoc = RSpecValidation.loadXMLFromString(input);
			RSpecValidation.wipeRootNamespaces(inputDoc);
			inputNew = RSpecValidation.getStringFromXml(inputDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// clean up output doc
		Document outputDoc = null;
		try {
			outputDoc = RSpecValidation.loadXMLFromString(output);
			RSpecValidation.wipeRootNamespaces(outputDoc);
			outputNew = RSpecValidation.getStringFromXml(outputDoc);
			System.out.println(outputNew);
		} catch (Exception e) {

			e.printStackTrace();
		}

		// get number of differences and calculate proportional error rate
		int numDiffs = getNumberDiffs(inputNew, outputNew);
		System.out.println("Number of differences: " + numDiffs);
		int nodeCount = inputDoc.getElementsByTagName("*").getLength();

		System.out.println("Number of input nodes: " + nodeCount);
		double errorRate = ((double) numDiffs) / (2 * nodeCount);
		errorRate = errorRate < 1 ? errorRate : 1;

		return errorRate;
	}

	static public int[] getDiffsNodes(String input)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {
		return getDiffsNodesVerbose(input, false);
	}

	/**
	 * returns the number of non-recoverable XMLunit differences (errors) and
	 * the number of nodes in the rspec
	 * 
	 * @param input
	 *            RSpec as string
	 * @return
	 * @throws MissingRspecElementException
	 * @throws DeprecatedRspecVersionException
	 * @throws FileNotFoundException
	 * @throws InvalidRspecValueException
	 */
	static public int[] getDiffsNodesVerbose(String input, boolean verbosity)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

		String output = completeRoundtripVerbose(input, verbosity);
		String inputNew = null;
		String outputNew = null;

		// clean up input doc
		Document inputDoc = null;
		try {
			inputDoc = RSpecValidation.loadXMLFromString(input);
			RSpecValidation.wipeRootNamespaces(inputDoc);
			inputNew = RSpecValidation.getStringFromXml(inputDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// clean up output doc
		Document outputDoc = null;
		try {
			outputDoc = RSpecValidation.loadXMLFromString(output);
			RSpecValidation.wipeRootNamespaces(outputDoc);
			outputNew = RSpecValidation.getStringFromXml(outputDoc);
		} catch (Exception e) {

			e.printStackTrace();
		}

		// get number of differences and calculate proportional error rate
		int numDiffs = getNumberDiffs(inputNew, outputNew);
		System.out.println("Number of differences: " + numDiffs);
		NodeList inNodes = inputDoc.getElementsByTagName("*");
		int attributesIn = 0;
		for (int i = 0; i < inNodes.getLength(); i++) {
			Element node = (Element) inNodes.item(i);
			if (node.getLocalName() != null) {
				if (!node.getLocalName().equals("rspec")) {
					attributesIn += node.getAttributes().getLength();
				}
			}
		}
		System.out.println("Number of input attributes: " + attributesIn);
		int nodeCountInput = inNodes.getLength();
		System.out.println("Number of input nodes: " + nodeCountInput);

		NodeList outNodes = outputDoc.getElementsByTagName("*");
		int attributesOut = 0;
		for (int i = 0; i < outNodes.getLength(); i++) {
			Element node = (Element) outNodes.item(i);
			if (node.getLocalName() != null) {
				if (!node.getLocalName().equals("rspec")) {
					attributesOut += node.getAttributes().getLength();
				}
			}
		}
		System.out.println("Number of output attributes: " + attributesOut);
		int nodeCountOutput = outNodes.getLength();
		System.out.println("Number of output nodes: " + nodeCountOutput);
		int[] diffsNodes = { numDiffs, nodeCountInput, nodeCountOutput,
				attributesIn, attributesOut };

		return diffsNodes;
	}

	/**
	 * returns the number of non-recoverable XMLunit differences (errors) and
	 * the number of nodes in the rspec
	 * 
	 * @param input
	 *            RSpec as string
	 * @return
	 * @throws MissingRspecElementException
	 * @throws DeprecatedRspecVersionException
	 */
	static public int[] getDiffsNodesWithOutput(String input, String output)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException {

		String inputNew = null;
		String outputNew = null;

		// clean up input doc
		Document inputDoc = null;
		try {
			inputDoc = RSpecValidation.loadXMLFromString(input);
			RSpecValidation.wipeRootNamespaces(inputDoc);
			inputNew = RSpecValidation.getStringFromXml(inputDoc);
		} catch (Exception e) {
			e.printStackTrace();
		}

		// clean up output doc
		Document outputDoc = null;
		try {
			outputDoc = RSpecValidation.loadXMLFromString(output);
			RSpecValidation.wipeRootNamespaces(outputDoc);
			outputNew = RSpecValidation.getStringFromXml(outputDoc);
		} catch (Exception e) {

			e.printStackTrace();
		}

		// get number of differences and calculate proportional error rate
		int numDiffs = getNumberDiffs(inputNew, outputNew);
		System.out.println("Number of differences: " + numDiffs);
		NodeList inNodes = inputDoc.getElementsByTagName("*");
		int attributesIn = 0;
		for (int i = 0; i < inNodes.getLength(); i++) {
			Element node = (Element) inNodes.item(i);
			if (node.getLocalName() != null) {
				if (!node.getLocalName().equals("rspec")) {
					attributesIn += node.getAttributes().getLength();
				}
			}
		}
		System.out.println("Number of input attributes: " + attributesIn);
		int nodeCountInput = inNodes.getLength();
		System.out.println("Number of input nodes: " + nodeCountInput);

		NodeList outNodes = outputDoc.getElementsByTagName("*");
		int attributesOut = 0;
		for (int i = 0; i < outNodes.getLength(); i++) {
			Element node = (Element) outNodes.item(i);
			if (node.getLocalName() != null) {
				if (!node.getLocalName().equals("rspec")) {
					attributesOut += node.getAttributes().getLength();
				}
			}
		}
		System.out.println("Number of output attributes: " + attributesOut);
		int nodeCountOutput = outNodes.getLength();
		System.out.println("Number of output nodes: " + nodeCountOutput);
		int[] diffsNodes = { numDiffs, nodeCountInput, nodeCountOutput,
				attributesIn, attributesOut };

		return diffsNodes;
	}

	/**
	 * returns the number of non-recoverable differences generated by XMLunit
	 * 
	 * @param input
	 *            RSpec
	 * @param output
	 *            RSpec
	 * @return
	 */
	private static int getNumberDiffs(String input, String output) {

		org.custommonkey.xmlunit.XMLUnit.setIgnoreWhitespace(true);
		org.custommonkey.xmlunit.XMLUnit.setIgnoreComments(true);

		Diff d = null;
		try {
			d = new Diff(input, output);
		} catch (SAXException | IOException e) {

			e.printStackTrace();
		}

		d.overrideElementQualifier(new ElementNameAndAttributeQualifier());
		DetailedDiff myDiff = new DetailedDiff(d);

		int numDiffs = 0;
		@SuppressWarnings("rawtypes")
		java.util.Iterator iter = myDiff.getAllDifferences().iterator();
		// for (@SuppressWarnings("rawtypes")
		// java.util.Iterator iter = myDiff.getAllDifferences().iterator(); iter
		// .hasNext();) {
		while (iter.hasNext()) {
			Difference diff = (Difference) iter.next();
			if (!diff.isRecoverable()) {
				numDiffs++;
				System.out.println(diff.toString());
			}
		}
		return numDiffs;

	}

	/**
	 * loads and XML document from a string
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Document loadXMLFromString(String xml) {

		Document xmlDoc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			byte[] bytes = xml.getBytes();
			ByteArrayInputStream byteArrray = new ByteArrayInputStream(bytes);
			xmlDoc = builder.parse(byteArrray);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			return null;
			// e.printStackTrace();
		}

		return xmlDoc;
	}

	/**
	 * loads and XML document from a stream
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static Document loadXMLFromStream(InputStream xml) {

		Document xmlDoc = null;
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setNamespaceAware(true);

		try {
			DocumentBuilder builder = factory.newDocumentBuilder();
			xmlDoc = builder.parse(xml);
		} catch (SAXException | IOException | ParserConfigurationException e) {
			return null;
			// e.printStackTrace();
		}

		return xmlDoc;
	}

	/**
	 * gets a string from an XML document
	 * 
	 * @param xml
	 * @return
	 * @throws Exception
	 */
	public static final String getStringFromXml(Document xml) {

		Transformer tf;
		Writer out = null;
		try {
			tf = TransformerFactory.newInstance().newTransformer();
			tf.setOutputProperty(OutputKeys.ENCODING, "UTF-8");
			tf.setOutputProperty(OutputKeys.INDENT, "yes");
			out = new StringWriter();
			tf.transform(new DOMSource(xml), new StreamResult(out));
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			e.printStackTrace();
		}

		return out.toString();

	}

	/**
	 * wipes all attributes in the RSpec node, except for "type"
	 * 
	 * @param xml
	 */
	public static void wipeRootNamespaces(Document xml) {

		Node root = xml.getElementsByTagName("rspec").item(0);
		if (root != null) {
			NodeList rootchildren = root.getChildNodes();
			Element newroot = xml.createElement(root.getNodeName());

			String type = root.getAttributes().getNamedItem("type")
					.getNodeValue();
			newroot.setAttribute("type", type);

			for (int i = 0; i < rootchildren.getLength(); i++) {
				newroot.appendChild(rootchildren.item(i).cloneNode(true));
			}
			xml.replaceChild(newroot, root);
		}
	}

	/**
	 * returns the RSpec type, "advertisement", "manifest", or "request", as
	 * well as "tosca" and "ttl"
	 * 
	 * @param xml
	 * @return
	 */
	public static String getType(String input) {
		// TODO: this method could potentially be improved by using validation
		// against XSD docs
		Document xml = null;

		try {
			xml = RSpecValidation.loadXMLFromString(input);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (xml != null) {
			// NodeList rspecNode = xml.getElementsByTagName("rspec");
			NodeList rspecNode = xml.getElementsByTagNameNS(
					"http://www.geni.net/resources/rspec/3", "rspec");

			if (rspecNode != null && rspecNode.getLength() > 0) {

				Node root = rspecNode.item(0);
				Node typeNode = root.getAttributes().getNamedItem("type");
				if (typeNode != null) {
					return typeNode.getNodeValue();
				}
			} else {

				System.out.println("*********** got to here");

				// check for tosca namespace
				// xmlns="http://docs.oasis-open.org/tosca/ns/2011/12"
				NodeList toscaNode = xml.getElementsByTagName("Definitions");
				if (toscaNode != null && toscaNode.getLength() > 0) {
					for (int i = 0; i < toscaNode.getLength(); i++) {
						Node rootTosca = toscaNode.item(i);
						String nameSpace = rootTosca.getNamespaceURI()
								.toString();
						if (nameSpace
								.equals("http://docs.oasis-open.org/tosca/ns/2011/12")) {
							return AbstractConverter.TOSCA;
						}
					}
				}

				InputStream inputStream = null;
				try {
					inputStream = IOUtils.toInputStream(input, "UTF-8");
				} catch (IOException e1) {
					e1.printStackTrace();
				}

				try {
					new Parser(inputStream, AbstractConverter.RDFXML);
					return AbstractConverter.RDFXML;
				} catch (InvalidModelException e) {
					e.printStackTrace();
				}
			}
		} else {

			InputStream inputStream = null;
			try {
				inputStream = IOUtils.toInputStream(input, "UTF-8");
			} catch (IOException e1) {
				e1.printStackTrace();
			}

			Parser parser = new Parser();

			try {
				parser.read(inputStream);
				return AbstractConverter.TTL;
			} catch (InvalidModelException e) {
				e.printStackTrace();

			}
		}

		return null;
	}

	public static String completeRoundtrip(String input)
			throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {
		return completeRoundtripVerbose(input, false);
	}

	/**
	 * generates an Open Multinet model from an RSpec, then generates a new
	 * RSpec from the generated model and returns the new RSpec as a string
	 * 
	 * @param input
	 * @return
	 * @throws MissingRspecElementException
	 * @throws DeprecatedRspecVersionException
	 * @throws FileNotFoundException
	 * @throws InvalidRspecValueException
	 */
	public static String completeRoundtripVerbose(String input,
			boolean verbosity) throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

		String output = null;
		Model model;

		input = RSpecValidation.fixVersion(input);
		String type = getType(input);

		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(input, "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		if (type != null) {
			if (type.equals("advertisement")) {
				try {
					AdvertisementConverter converter = new AdvertisementConverter();
					converter.setVerbose(verbosity);
					RSpecContents rspec = converter.getRspec(inputStream);
					model = converter.getModel(rspec);

					String modelString = Parser.toString(model);
					System.out.println(modelString);

					// PrintWriter outBlah = new PrintWriter("model.txt");
					// outBlah.println(modelString);
					// outBlah.close();

					output = converter.getRSpec(model);
				} catch (JAXBException | InvalidModelException
				// | XMLStreamException | FileNotFoundException e) {
						| XMLStreamException e) {
					e.printStackTrace();
				}
			} else if (type.equals("manifest")) {
				try {
					model = ManifestConverter.getModel(inputStream);
					System.out.println(Parser.toString(model));

					InfModel infModel = new Parser(model).getInfModel();
					output = ManifestConverter.getRSpec(infModel,
							"instageni.gpolab.bbn.com");
				} catch (JAXBException | InvalidModelException e) {
					e.printStackTrace();
				}
			} else if (type.equals("request")) {
				try {
					model = RequestConverter.getModel(inputStream);
					System.out.println(Parser.toString(model));
					output = RequestConverter.getRSpec(model);
				} catch (JAXBException | InvalidModelException
						| MissingRspecElementException e) {
					e.printStackTrace();
				}
			}
		}
		return output;
	}

	/**
	 * compares an RSpec string against the XSD file using XMLUnit
	 * 
	 * @param input
	 *            RSpec string
	 * @return whether RSpec is valid or not
	 */
	public static boolean validateRspecXMLUnit(String input) {
		boolean isValid = false;
		InputSource is;

		try {
			is = new InputSource(new StringReader(input));
			Validator v = new Validator(is);
			v.useXMLSchema(true);
			File schema = null;

			// get rspec as string in order to check type
			String type = RSpecValidation.getType(input);

			if (type != null) {
				// set schema according to rspec type
				if (type.equals("advertisement")) {
					schema = new File(
							"./src/main/resources/geni/advertisement/ad.xsd");
				}

				if (type.equals("manifest")) {
					schema = new File(
							"./src/main/resources/geni/manifest/manifest.xsd");
				}

				if (type.equals("request")) {
					schema = new File(
							"./src/main/resources/geni/request/request.xsd");
				}

				// check against XSD whether rspec is valid or not
				v.setJAXP12SchemaSource(schema);
				isValid = v.isValid();
			}

		} catch (SAXException e) {
			e.printStackTrace();
		}
		return isValid;
	}

	/**
	 * Validate RSpec using javax.xml.validation library
	 * 
	 * @param path
	 * @param type
	 * @return
	 */
	public static boolean validateRspecSchemaFactory(String path, String type) {
		// https://simonharrer.wordpress.com/2012/11/05/xml-validation-with-the-java-api/
		boolean isValid = false;

		Schema schema = null;
		javax.xml.validation.Validator validator = null;
		SchemaFactory sFactory = SchemaFactory
				.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		if (type != null) {
			switch (type) {
			case "advertisement":
				try {
					schema = sFactory.newSchema(new File(
							"./src/main/resources/geni/advertisement/ad.xsd"));
				} catch (SAXException e2) {
					e2.printStackTrace();
				}
				break;
			case "manifest":
				try {
					schema = sFactory.newSchema(new File(
							"./src/main/resources/geni/manifest/manifest.xsd"));
				} catch (SAXException e1) {
					e1.printStackTrace();
				}
				break;
			case "request":
				try {
					schema = sFactory.newSchema(new File(
							"./src/main/resources/geni/request/request.xsd"));
				} catch (SAXException e) {
					e.printStackTrace();
				}
				break;
			}
		}

		if (schema != null) {
			validator = schema.newValidator();
		}

		if (validator != null) {
			try {
				validator.validate(new StreamSource(new File(path)));
				isValid = true;
			} catch (SAXException | IOException e) {
			}
		}

		return isValid;
	}

	/**
	 * Validation method.
	 * 
	 * @param xmlFilePath
	 *            The xml file we are trying to validate.
	 * @param xmlSchemaFilePath
	 *            The schema file we are using for the validation. This method
	 *            assumes the schema file is valid.
	 * @return True if valid, false if not valid or bad parse or exception/error
	 *         during parse.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 *             source: http://www.edankert.com/validate.html#
	 *             Validate_using_internal_XSD
	 */
	public static boolean validateDOM(String xmlFilePath,
			String xmlSchemaFilePath) throws ParserConfigurationException,
			SAXException, IOException {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");

		factory.setSchema(schemaFactory
				.newSchema(new Source[] { new StreamSource(xmlSchemaFilePath) }));

		DocumentBuilder builder = factory.newDocumentBuilder();

		SimpleErrorHandler errors = new SimpleErrorHandler();
		builder.setErrorHandler(errors);

		Document document = builder.parse(new InputSource(xmlFilePath));

		return errors.getValid();
	}

	/**
	 * Validation method.
	 * 
	 * @param xmlFilePath
	 *            The xml file we are trying to validate.
	 * @param xmlSchemaFilePath
	 *            The schema file we are using for the validation. This method
	 *            assumes the schema file is valid.
	 * @return True if valid, false if not valid or bad parse or exception/error
	 *         during parse.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 *             source: http://www.edankert.com/validate.html#
	 *             Validate_using_internal_XSD
	 */
	public static boolean validateSAX(String xmlFilePath,
			String xmlSchemaFilePath) throws ParserConfigurationException,
			SAXException, IOException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");

		factory.setSchema(schemaFactory
				.newSchema(new Source[] { new StreamSource(xmlSchemaFilePath) }));

		SAXParser parser = factory.newSAXParser();
		SimpleErrorHandler errors = new SimpleErrorHandler();
		XMLReader reader = parser.getXMLReader();
		reader.setErrorHandler(errors);
		reader.parse(new InputSource(xmlFilePath));

		return errors.getValid();
	}

	/**
	 * Validation method.
	 * 
	 * @param xmlFilePath
	 *            The xml file we are trying to validate.
	 * @param xmlSchemaFilePath
	 *            The schema file we are using for the validation. This method
	 *            assumes the schema file is valid.
	 * @return True if valid, false if not valid or bad parse or exception/error
	 *         during parse.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 *             source: http://www.edankert.com/validate.html#
	 *             Validate_using_internal_XSD
	 * @throws DocumentException
	 */
	public static boolean validateDom4j(String xmlFilePath,
			String xmlSchemaFilePath) throws ParserConfigurationException,
			SAXException, IOException, DocumentException {

		SAXParserFactory factory = SAXParserFactory.newInstance();

		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");

		factory.setSchema(schemaFactory
				.newSchema(new Source[] { new StreamSource(xmlSchemaFilePath) }));

		SAXParser parser = factory.newSAXParser();

		SAXReader reader = new SAXReader(parser.getXMLReader());
		reader.setValidation(false);

		SimpleErrorHandler errors = new SimpleErrorHandler();

		reader.setErrorHandler(errors);
		reader.read(xmlFilePath);

		return errors.getValid();

	}

	/**
	 * Validation method.
	 * 
	 * @param xmlFilePath
	 *            The xml file we are trying to validate.
	 * @param xmlSchemaFilePath
	 *            The schema file we are using for the validation. This method
	 *            assumes the schema file is valid.
	 * @return True if valid, false if not valid or bad parse or exception/error
	 *         during parse.
	 * @throws ParserConfigurationException
	 * @throws IOException
	 * @throws SAXException
	 * 
	 *             source: http://www.edankert.com/validate.html#
	 *             Validate_using_internal_XSD
	 * @throws DocumentException
	 * @throws ParsingException
	 * @throws ValidityException
	 */
	public static boolean validateXom(String xmlFilePath,
			String xmlSchemaFilePath) throws ParserConfigurationException,
			SAXException, IOException, DocumentException, ValidityException,
			ParsingException {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		factory.setValidating(false);
		factory.setNamespaceAware(true);

		SchemaFactory schemaFactory = SchemaFactory
				.newInstance("http://www.w3.org/2001/XMLSchema");
		factory.setSchema(schemaFactory
				.newSchema(new Source[] { new StreamSource(xmlSchemaFilePath) }));

		SAXParser parser = factory.newSAXParser();
		XMLReader reader = parser.getXMLReader();

		SimpleErrorHandler errors = new SimpleErrorHandler();
		reader.setErrorHandler(errors);

		Builder builder = new Builder(reader);
		builder.build(xmlFilePath);

		return errors.getValid();
	}

	/**
	 * compares an RSpec string against the XSD file using RSpecLint and returns
	 * whether the file is a valide RSpec or not.
	 * 
	 * Note: only tested on Mac OS.
	 * 
	 * Note: requires executable instance of RDFLint in src folder
	 * http://trac.gpolab.bbn.com/gcf/attachment/wiki/OmniTroubleShoot/rspeclint
	 * 
	 * @param input
	 *            RSpec string
	 * @return whether RSpec is valid or not
	 */
	public static boolean rspecLintMacOnly(String filepath) {

		String rspecString = null;

		try {
			rspecString = AbstractConverter.toString(filepath);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		filepath = "src/test/resources" + filepath;

		String command1a = "./src/rspeclint";
		String command1b = "http://www.geni.net/resources/rspec/3";

		String command1c = null;
		String type = RSpecValidation.getType(rspecString);

		if (type != null) {
			switch (type) {
			case "advertisement":
				command1c = "http://www.geni.net/resources/rspec/3/ad.xsd";
				break;
			case "manifest":
				command1c = "http://www.geni.net/resources/rspec/3/manifest.xsd";
				break;
			case "request":
				command1c = "http://www.geni.net/resources/rspec/3/request.xsd";
				break;
			}
		}
		if (command1c != null) {
			String[] commands = new String[4];
			commands[0] = command1a;
			commands[1] = command1b;
			commands[2] = command1c;
			commands[3] = filepath;

			Process process;
			try {
				process = Runtime.getRuntime().exec(commands);
				process.waitFor();
				if (process.exitValue() == 0) {
					return true;
				}
			} catch (Exception e) {
				System.out.println("Exception: " + e.toString());
			}
		} else {
			System.out.println("Error: Unable to determin RSpec type.");
		}
		return false;
	}

	/**
	 * Returns whether a files extension is that of an RSpec
	 * 
	 * @param file
	 * @return
	 */
	public static boolean rspecFileExtension(File file) {
		String fileExt = null;
		try {
			fileExt = FilenameUtils.getExtension(file.getCanonicalPath());
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		boolean fileExtensionOK = fileExt.equals("xml")
				|| fileExt.equals("manifest") || fileExt.equals("request")
				|| fileExt.equals("rspec");

		return fileExtensionOK;
	}

	/**
	 * makes sure that the RSpec version is 2 or 3 version 1 is not supported
	 * 
	 * @param xml
	 * @return
	 * @throws DeprecatedRspecVersionException
	 */
	public static String fixVersion(String input)
			throws DeprecatedRspecVersionException {

		Document xml = null;
		String newString = null;

		try {
			xml = RSpecValidation.loadXMLFromString(input);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (xml != null) {
			NodeList rspecNode = xml.getElementsByTagName("rspec");
			// NodeList rspecNode = xml.getElementsByTagNameNS(
			// "http://www.geni.net/resources/rspec/3", "rspec");
			if (rspecNode != null && rspecNode.getLength() > 0) {

				Node root = rspecNode.item(0);

				Node typeNode = root.getAttributes().getNamedItem("xmlns");

				if (typeNode != null) {

					String namespace = typeNode.getNodeValue();

					// System.out.println("RSpec version: " + namespace);
					if (namespace
							.equals("http://www.protogeni.net/resources/rspec/0.1")) {
						// System.out
						// .println("RSpec version 0.1 is not supported.");
						throw new DeprecatedRspecVersionException("0.1");
					}

					if (namespace
							.equals("http://www.geni.net/resources/rspec/3")) {
						return input;
					}

					if (namespace
							.equals("http://www.protogeni.net/resources/rspec/2")) {
						// System.out
						// .println("Converting RSpec version 2 to version 3.");
						input = input.replaceAll(
								"http://www.protogeni.net/resources/rspec/2",
								"http://www.geni.net/resources/rspec/3");
						return input;
					}
				}
			} else {

				NodeList rspecNode1 = xml.getElementsByTagNameNS(
						"http://www.geni.net/resources/rspec/3", "rspec");
				if (rspecNode1 != null && rspecNode1.getLength() > 0) {
					return input;
				}

				NodeList rspecNode2 = xml.getElementsByTagNameNS(
						"http://www.protogeni.net/resources/rspec/2", "rspec");
				if (rspecNode2 != null && rspecNode2.getLength() > 0) {
					input = input.replaceAll(
							"http://www.protogeni.net/resources/rspec/2",
							"http://www.geni.net/resources/rspec/3");
					return input;
				}
			}
		}
		return newString;
	}

	/**
	 * makes sure that the RSpec version is 2 or 3 version 1 is not supported
	 * 
	 * @param xml
	 * @return
	 */
	public static boolean checkVersion(String input) {

		Document xml = null;

		try {
			xml = RSpecValidation.loadXMLFromString(input);
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		if (xml != null) {
			NodeList rspecNode = xml.getElementsByTagName("rspec");
			if (rspecNode != null && rspecNode.getLength() > 0) {
				Node root = rspecNode.item(0);
				Node typeNode = root.getAttributes().getNamedItem("xmlns");
				if (typeNode != null) {
					String namespace = typeNode.getNodeValue();
					System.out.println("RSpec version: " + namespace);
					if (namespace
							.equals("http://www.protogeni.net/resources/rspec/0.1")) {
						System.out
								.println("RSpec version 0.1 is not supported.");
						return false;
					}
					if (namespace
							.equals("http://www.geni.net/resources/rspec/3")) {
						return true;
					}
					if (namespace
							.equals("http://www.protogeni.net/resources/rspec/2")) {
						return true;
					}
				}
			}
		}
		return false;
	}
}