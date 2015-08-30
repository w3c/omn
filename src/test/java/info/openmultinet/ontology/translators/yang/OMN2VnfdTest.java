package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.yang.vnfd.OMN2Vnfd;
import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class OMN2VnfdTest {

	private Parser parser;

	@Before
	public void setup()
			throws InvalidModelException, JAXBException {
		parser = new Parser();
		
	}

	@Test
	public void testConvertYang()
			throws InvalidModelException {

		InputStream input = getClass().getResourceAsStream("/omn/vnfd.ttl");
		Parser parser = new Parser(input);
		final Model model = parser.getModel();
		OMN2Vnfd test = new OMN2Vnfd();
		System.out.println(test.getDescriptor(model));

	}
}
