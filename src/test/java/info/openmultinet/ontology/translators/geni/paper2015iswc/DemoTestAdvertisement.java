package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_federation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
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

public class DemoTestAdvertisement {

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
		start = System.nanoTime();
		
		System.out.println("================================================");
		System.out
				.println("Reading Graph");
		System.out.println("================================================");

		parser.read("/omn/paper2015iswc/omn_1_offer.ttl");
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));
		
		System.out.println("================================================");
		System.out
				.println("Converting to RSpec");
		System.out.println("================================================");
		
		final String rspec = converter.getRSpec(model);

		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a DiskImage",
				rspec.contains("disk_image"));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");
		

		System.out.println("================================================");
		System.out
				.println("Converting back to graph");
		System.out.println("================================================");
				
				
		start = System.nanoTime();
		InputStream input = IOUtils.toInputStream(rspec, "UTF-8");
		RSpecContents rspecContents = converter.getRspec(input);
		final Model newModel = converter.getModel(rspecContents);
		System.out.println("Result:");
		String modStr = Parser.toString(newModel);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
	}

}
