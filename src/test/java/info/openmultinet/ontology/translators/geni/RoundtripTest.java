package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class RoundtripTest {

	@Test
	public void testAdvertismentRoundtrip() throws JAXBException, InvalidModelException {
		
		System.out.println("Reading large advertisement RSpec...");
		System.out.println(System.currentTimeMillis());
		
		InputStream rspec = RoundtripTest.class.getResourceAsStream("/advertisement_vwall1.xml");
		
		System.out.println("Converting to omn graph...");
		System.out.println(System.currentTimeMillis());
		
		Model model = Advertisement2OMN.getModel(rspec);
		
		System.out.println("Converting to large advertisement RSpec again...");
		System.out.println(System.currentTimeMillis());
		
		InfModel infModel = new Parser(model).getModel();
		String advertisement = OMN2Advertisement.getRSpec(infModel);
		
		System.out.println("This is the result:");
		System.out.println(System.currentTimeMillis());
		
		System.out.println(advertisement);
	}

}
