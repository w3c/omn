package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.InputStream;
import java.util.Scanner;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class DMTest {

	@Test
	public void testConvertToGraphFromRspecAd()
			throws DeprecatedRspecVersionException, JAXBException,
			InvalidModelException, MissingRspecElementException,
			XMLStreamException, UnsupportedException {

		final String content = this.getFilecontent("/omn/migrationinstance.ttl");

		Model model = DeliveryMechanism.getModelFromUnkownInput(content);
	
		String modelString = Parser.toString(model);
		System.out.println(modelString);

	}

	public String getFilecontent(final String filename) {
		final InputStream rspec = RESTTest.class.getResourceAsStream(filename);
		@SuppressWarnings("resource")
		final String content = new Scanner(rspec, "UTF-8").useDelimiter("\\A")
				.next();
		return content;
	}

}
