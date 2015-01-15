package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Request2OMNTest {

	@Test
	public void test() throws JAXBException {
		InputStream rspec = Request2OMNTest.class.getResourceAsStream("/request.rspec");
		Model model = Request2OMN.getModel(rspec);
		ResIterator topology = model.listResourcesWithProperty(RDF.type, Omn_lifecycle.Request);
		Assert.assertTrue("should have a topology", topology.hasNext());
	}

}
