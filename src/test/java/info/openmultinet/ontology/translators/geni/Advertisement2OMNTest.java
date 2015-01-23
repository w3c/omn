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

public class Advertisement2OMNTest {

	@Test
	public void test() throws JAXBException, InvalidModelException {
		InputStream rspec = Advertisement2OMNTest.class.getResourceAsStream("/advertisement_vwall1.xml");
		Model model = Advertisement2OMN.getModel(rspec);
		ResIterator topology = model.listResourcesWithProperty(RDF.type, Omn_lifecycle.Offering);
		Assert.assertTrue("should have a topology", topology.hasNext());
		System.out.println(Parser.toString(model));
		InfModel infModel = new Parser(model).getModel();
		String advertisement = OMN2Advertisement.getRSpec(infModel);
		System.out.println(advertisement);
	}

}
