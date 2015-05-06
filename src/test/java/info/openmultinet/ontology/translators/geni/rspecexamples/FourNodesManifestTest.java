package info.openmultinet.ontology.translators.geni.rspecexamples;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverterTest;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;


public class FourNodesManifestTest {


	@Test
	public void testRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		
//		final InputStream rspec = ManifestConverterTest.class
//				.getResourceAsStream("/geni/rspecexamples/4nodesmanifest.xml");
//		final Model model = ManifestConverter.getModel(rspec);
//		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
//				Omn_lifecycle.Manifest);
//		Assert.assertTrue("should have a topology (manifest)",
//				topology.hasNext());
//		System.out.println("Generated this graph:");
//		System.out.println("===============================");
//		System.out.println(Parser.toString(model));
//		System.out.println("===============================");
		
	}

}
