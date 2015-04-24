package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RequestConverter;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;


public class RequestRspecDemoTest {

	@Test
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		long start;
		String inputFile = "/omn/paper2015iswc/request.xml";

		System.out.println("================================================");
		System.out.println("Operation: Reading reference request RSpec");
		start = System.nanoTime();
//		final InputStream input = AdvertisementConverter.class
//				.getResourceAsStream(inputFile);
		final InputStream rspec = RequestRspecDemoTest.class
				.getResourceAsStream(inputFile);
		
		System.out.println("Input:");
		String inStr = AbstractConverter.toString(inputFile);
		System.out
				.println(StringUtils.abbreviateMiddle(inStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to object model (jaxb)");
		
		

		
		
		
		// RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to omn graph");
		start = System.nanoTime();
		final Model model = RequestConverter.getModel(rspec);
		// final Model model = converter.getModel(rspec);
		System.out.println("Result:");
		String modStr = Parser.toString(model);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));

		System.out.println("================================================");
		System.out
				.println("Converting to reference request RSpec again...");
		start = System.nanoTime();
		// final String request = converter.getRSpec(model);
		final String request = RequestConverter.getRSpec(model);
		System.out.println("Result:");
		System.out.println(StringUtils.abbreviateMiddle(request,
				"\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

	}

}
