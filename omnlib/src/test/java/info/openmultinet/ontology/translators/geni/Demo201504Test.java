package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class Demo201504Test {

	@Test
	public void testConvertingRSpecToGraph() throws JAXBException,
			InvalidModelException, MissingRspecElementException,
			InvalidRspecValueException {
		final InputStream rspec = RequestConverterTest.class
				.getResourceAsStream("/geni/request/request_demo201504.xml");
		final Model model = RequestConverter.getModel(rspec);
		final String rspecOut = Parser.toString(model);
		System.out.println(rspecOut);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue("should translate component_manager_id",
				rspecOut.contains("authority+cm"));

		final String outputRspec = ManifestConverter.getRSpec(model,
				"localhost");
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");
		Assert.assertTrue("should translate component_manager_id",
				outputRspec.contains("authority+cm"));
	}
}
