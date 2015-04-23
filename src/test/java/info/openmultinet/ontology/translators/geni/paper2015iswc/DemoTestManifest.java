package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.LoginManifestConverterTest;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverterTest;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class DemoTestManifest {

	private ManifestConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new ManifestConverter();
	}

	@Test
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {

		System.out.println("================================================");
		System.out.println("Reading Graph");
		System.out.println("================================================");
		parser.read("/omn/paper2015iswc/omn_3_manifest.ttl");
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));

		System.out.println("================================================");
		System.out.println("Converting to RSpec");
		System.out.println("================================================");
		long start;
		start = System.nanoTime();
		final String rspec = ManifestConverter.getRSpec(model, "localhost");
		System.out.println(rspec);
		Assert.assertTrue("should be a manifest",
				rspec.contains("type=\"manifest\""));
		System.out.println("Duration: " + (System.nanoTime() - start));

		System.out.println("================================================");
		System.out.println("Converting back to Graph");
		System.out.println("================================================");
		start = System.nanoTime();
		final InputStream inputRspec = new ByteArrayInputStream(
				rspec.getBytes());
		final Model newModel = ManifestConverter.getModel(inputRspec);
		System.out.println(Parser.toString(newModel));
		System.out.println("Duration: " + (System.nanoTime() - start));
	}
}
