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

		System.out.println("Reading large advertisement RSpec...");
		System.out.println(System.currentTimeMillis());

		final InputStream rspec = RoundtripTest.class
				.getResourceAsStream("/advertisement_vwall1.xml");

		System.out.println("Converting to omn graph...");
		System.out.println(System.currentTimeMillis());

		final Model model = AdvertisementConverter.getModel(rspec);

		System.out.println("Converting to large advertisement RSpec again...");
		System.out.println(System.currentTimeMillis());

		final InfModel infModel = new Parser(model).getInfModel();
		final String advertisement = AdvertisementConverter.getRSpec(infModel);

		System.out.println("This is the result:");
		System.out.println(System.currentTimeMillis());

		System.out.println(advertisement);
	}

}
