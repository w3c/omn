package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementConverterTest {

	@Test
	@Ignore
	public void testAdv2omn() throws JAXBException, InvalidModelException {
		final InputStream rspec = AdvertisementConverterTest.class
				.getResourceAsStream("/geni/advertisement/advertisement_vwall1.xml");
		final Model model = AdvertisementConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Offering);
		Assert.assertTrue("should have a topology", topology.hasNext());
	}

	@Test
	public void testGraph2Advertisement() throws JAXBException, InvalidModelException {
		InputStream input = ParserTest.class
				.getResourceAsStream("/omn/offer_paper2015.ttl");
		Parser parser = new Parser(input);
		final InfModel model = parser.getInfModel();
		System.out.println(Parser.toString(model));
		final String rspec = AdvertisementConverter.getRSpec(model);
		
		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));
		Assert.assertTrue("should have a motorgarage", rspec.contains("MotorGarage"));
	}

}
