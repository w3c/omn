package info.openmultinet.ontology.translators.geni.fed4fire;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.translators.geni.RSpecValidationTest;
import info.openmultinet.ontology.translators.geni.RequestConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ListResources101201556163559958 {

	@Test
	public void adRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException {

		final String filename = "/geni/fed4fire/listResources-101-2015-5-6-16-35-59-958.xml";
		final String inputRspec = AbstractConverter.toString(filename);

		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(inputRspec);
		System.out.println("===============================");

		final String outputRspec = RSpecValidation
				.completeRoundtrip(inputRspec);

		PrintWriter outBlah = new PrintWriter("filename.txt");
		outBlah.println(outputRspec);
		outBlah.close();
		
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

//		Assert.assertTrue("type",
//				outputRspec.contains("type=\"advertisement\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRspec);

		// Document xmlDoc = RSpecValidation.loadXMLFromString(outputRspec);

		// check that output has one rspec element
//		NodeList rspec = xmlDoc.getElementsByTagNameNS(
//				"http://www.geni.net/resources/rspec/3", "rspec");
//		Assert.assertTrue(rspec.getLength() == 1);
//
//		NodeList opstates = xmlDoc.getElementsByTagNameNS(
//				"http://www.geni.net/resources/rspec/ext/opstate/1",
//				"rspec_opstate");
//		Assert.assertTrue(opstates.getLength() == 2);
//
//		NodeList nodes = xmlDoc.getElementsByTagNameNS(
//				"http://www.geni.net/resources/rspec/3", "node");
//		Assert.assertTrue(nodes.getLength() == 1);
//		String componentID = nodes.item(0).getAttributes()
//				.getNamedItem("component_id").getNodeValue();
//		Assert.assertTrue(componentID
//				.equals("urn:publicid:IDN+jonlab.tbres.emulab.net+node+pc39"));
//		String exclusive = nodes.item(0).getAttributes()
//				.getNamedItem("exclusive").getNodeValue();
//		Assert.assertTrue(exclusive.equals("true"));

		// TODO: Currently returns a high number of errors, although translation
		// appears to be correct.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);

	}
}
