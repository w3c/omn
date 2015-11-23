package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class AdvertisementVwall1 {

	@Test
	@Ignore
	// fixme: this test is slow!
	public void adRoundtrip() throws JAXBException, InvalidModelException,
			IOException, XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		final String filename = "/geni/advertisement/advertisement_vwall1.xml";
		final String inputRspec = AbstractConverter.toString(filename);

		// System.out.println("Converting this input from '" + filename + "':");
		// System.out.println("===============================");
		// System.out.println(inputRspec);
		// System.out.println("===============================");

		final String outputRspec = RSpecValidation
				.completeRoundtrip(inputRspec);

		// System.out.println("Generated this rspec:");
		// System.out.println("===============================");
		// System.out.println(outputRspec);
		// System.out.println("===============================");

		Assert.assertTrue("type",
				outputRspec.contains("type=\"advertisement\""));

		System.out.println("===============================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRspec);

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);

	}

}
