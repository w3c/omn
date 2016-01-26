package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.geni.ManifestConverter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

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
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {

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
