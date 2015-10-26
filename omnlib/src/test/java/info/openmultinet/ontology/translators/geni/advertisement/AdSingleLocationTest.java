package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverterTest;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class AdSingleLocationTest {
	
	@Test
	public void modelToRspec() throws FileNotFoundException,
			InvalidModelException, JAXBException {
		InputStream input = new FileInputStream(
				"./src/test/resources/omn/ad-location.ttl");
		Parser parser = new Parser();
		parser.read(input);
		final Model model = parser.getModel();
		String modelString = Parser.toString(model);
		System.out.println(modelString);

		AdvertisementConverter converter = new AdvertisementConverter();
		String outputRspec = converter.getRSpec(model);
		System.out.println(outputRspec);
	}

}
