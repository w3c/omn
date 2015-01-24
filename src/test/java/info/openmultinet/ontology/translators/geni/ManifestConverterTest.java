package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class ManifestConverterTest {

	@Test
	@Ignore
	public void testSimpleManifest() throws JAXBException,
			InvalidModelException {
		final InputStream rspec = ManifestConverterTest.class
				.getResourceAsStream("/manifest_tobedefined.xml");
		final Model model = ManifestConverter.getModel(rspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Manifest);
		Assert.assertTrue("should have a topology (manifest)",
				topology.hasNext());
	}
}
