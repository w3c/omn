package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class RoundtripTest {

	@Test
	public void testAdvertismentRoundtrip() throws JAXBException,
			InvalidModelException {
		long start ;
		
		System.out.println("Operation: Reading reference advertisement RSpec");
		start = System.currentTimeMillis();
		final InputStream rspec = RoundtripTest.class
				.getResourceAsStream("/geni/advertisement/advertisement_paper2015.xml");
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		
		
		System.out.println("Operation: Converting to omn graph");
		start = System.currentTimeMillis();
		final Model model = AdvertisementConverter.getModel(rspec);
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		System.out.println("Result:"); 
		System.out.println(Parser.toString(model));
		
		
		System.out.println("Converting to reference advertisement RSpec again...");
		start = System.currentTimeMillis();
		final InfModel infModel = new Parser(model).getInfModel();
		System.out.println("Duration: " + (System.currentTimeMillis() - start));
		final String advertisement = AdvertisementConverter.getRSpec(infModel);
		System.out.println("Duration: " + (System.currentTimeMillis() - start));

		System.out.println("This is the result:");
		System.out.println(System.currentTimeMillis());

		System.out.println(advertisement);
	}

}
