package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.graph.Triple;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.sparql.core.BasicPattern;
import com.hp.hpl.jena.sparql.core.Var;
import com.hp.hpl.jena.sparql.syntax.ElementGroup;
import com.hp.hpl.jena.sparql.syntax.ElementTriplesBlock;
import com.hp.hpl.jena.sparql.syntax.Template;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementDemoTest {

	private AdvertisementConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new AdvertisementConverter();
	}

	@Test
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		long start;
		String inputFile = "/geni/advertisement/demo-test.xml";

		System.out.println("================================================");
		System.out.println("Operation: Reading reference advertisement RSpec");
		start = System.nanoTime();
		final InputStream input = AdvertisementConverter.class
				.getResourceAsStream(inputFile);
		System.out.println("Input:");
		String inStr = AbstractConverter.toString(inputFile);
		System.out
				.println(StringUtils.abbreviateMiddle(inStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to object model (jaxb)");
		RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to omn graph");
		start = System.nanoTime();
		final Model model = converter.getModel(rspec);
		System.out.println("Result:");
		String modStr = Parser.toString(model);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out
				.println("Converting to reference advertisement RSpec again...");
		start = System.nanoTime();
		final String advertisement = converter.getRSpec(model);
		System.out.println("Result:");
		System.out.println(StringUtils.abbreviateMiddle(advertisement,
				"\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

	}

}
