package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RequestConverter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class ToscaXmlTest {

	@Test
	public void testConvertToscaFile() throws InvalidModelException,
			JAXBException, MissingRspecElementException, FileNotFoundException,
			InvalidRspecValueException {

		System.out.println("******************************************");
		System.out.println("********    loading this file    ********");
		System.out.println("******************************************");

		InputStream input = new FileInputStream(
				"./src/test/resources/tosca/newTubitTosca.xml");

		BufferedReader bufferedReader = new BufferedReader(
				new InputStreamReader(input), 1);
		String line;
		try {
			while ((line = bufferedReader.readLine()) != null) {
				System.out.println(line);
			}
			input.close();
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		InputStream input1 = new FileInputStream(
				"./src/test/resources/tosca/newTubitTosca.xml");
		Parser parser = new Parser(input1, AbstractConverter.RDFXML);
		try {
			input1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		System.out.println("******************************************");
		System.out.println("********    loading this model    ********");
		System.out.println("******************************************");
		final Model model = parser.getModel();
		String modelString = Parser.toString(model);
		System.out.println(modelString);

		System.out.println("******************************************");
		System.out.println("********  converting to rspec     ********");
		System.out.println("******************************************");

		final String outputRspec = RequestConverter.getRSpec(model);
		System.out.println(outputRspec);

		System.out.println("******************************************");
		System.out.println("********  converting back to model ********");
		System.out.println("******************************************");

		Model modelNew = null;
		InputStream inputStream = null;
		try {
			inputStream = IOUtils.toInputStream(outputRspec, "UTF-8");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		try {
			modelNew = RequestConverter.getModel(inputStream);
			// String output = RequestConverter.getRSpec(model);
		} catch (JAXBException | InvalidModelException
				| MissingRspecElementException e) {
			e.printStackTrace();
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		// RDFDataMgr.write(baos, modelNew, Lang.RDFXML);
		RDFDataMgr.write(baos, modelNew, Lang.TTL);
		System.out.println(baos.toString());
	}
}