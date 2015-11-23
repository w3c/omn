package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.junit.Test;

public class MonitoringTest {

	@Test
	public void testRequest() throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

		// check that RSpec is valid
		String path = "./src/test/resources/geni/monitoring/monitoring-request.xml";
		String inputString = null;

		try {
			inputString = AbstractConverter.toString(path.substring(20));
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean validSchemaFactory = false;
		boolean validRSpecLint = false;
		boolean validXMLUnit = false;

		if (inputString != null) {
			String type = RSpecValidation.getType(inputString);
			validSchemaFactory = RSpecValidation.validateRspecSchemaFactory(
					path, type);
			validRSpecLint = RSpecValidation.rspecLintMacOnly(path
					.substring(20));
			validXMLUnit = RSpecValidation.validateRspecXMLUnit(inputString);
		}

		System.out.println("========================================");
		System.out.println("Tesing Request RSpec");
		System.out.println("Check validity of file:");
		System.out.println(path.substring(20));
		System.out.println("validSchemaFactory: " + validSchemaFactory);
		System.out.println("validXMLUnit: " + validXMLUnit);
		System.out.println("validRSpecLint: " + validRSpecLint);
		System.out.println("========================================");

		// print RSpec input
		System.out.println("Input RSpec:");
		System.out.println(inputString);
		System.out.println("========================================");
		System.out.println("Model:");
		String outputString = RSpecValidation.completeRoundtrip(inputString);
		System.out.println("========================================");
		System.out.println("Output RSpec:");
		System.out.println(outputString);

		System.out.println("========================================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputString);
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);
	}

	@Test
	public void testAd() throws MissingRspecElementException,
			DeprecatedRspecVersionException, FileNotFoundException,
			InvalidRspecValueException {

		// check that RSpec is valid
		String path = "./src/test/resources/geni/monitoring/monitoring-ad.xml";
		String inputString = null;

		try {
			inputString = AbstractConverter.toString(path.substring(20));
		} catch (IOException e) {
			e.printStackTrace();
		}

		boolean validSchemaFactory = false;
		boolean validRSpecLint = false;
		boolean validXMLUnit = false;

		if (inputString != null) {
			String type = RSpecValidation.getType(inputString);
			validSchemaFactory = RSpecValidation.validateRspecSchemaFactory(
					path, type);
			validRSpecLint = RSpecValidation.rspecLintMacOnly(path
					.substring(20));
			validXMLUnit = RSpecValidation.validateRspecXMLUnit(inputString);
		}

		System.out.println("========================================");
		System.out.println("Tesing Ad RSpec");
		System.out.println("Check validity of file:");
		System.out.println(path.substring(20));
		System.out.println("validSchemaFactory: " + validSchemaFactory);
		System.out.println("validXMLUnit: " + validXMLUnit);
		System.out.println("validRSpecLint: " + validRSpecLint);

		// print RSpec input
		System.out.println("========================================");
		System.out.println("Input RSpec:");
		System.out.println(inputString);
		System.out.println("========================================");
		System.out.println("Model:");
		String outputString = RSpecValidation.completeRoundtrip(inputString);

		System.out.println("========================================");
		System.out.println("Output RSpec:");
		System.out.println(outputString);

		System.out.println("========================================");
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputString);
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);
	}
}