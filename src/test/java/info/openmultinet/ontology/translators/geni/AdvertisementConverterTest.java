package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class AdvertisementConverterTest {

	@Test
	public void testAdv2omn() throws JAXBException, InvalidModelException {
		final InputStream rspec = AdvertisementConverterTest.class
				.getResourceAsStream("/geni/advertisement/advertisement_vwall1.xml");
		final Model model = AdvertisementConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Offering);
		Assert.assertTrue("should have a topology", topology.hasNext());
	}

}
