package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.ParserTest;
import info.openmultinet.ontology.exceptions.InvalidModelException;

import java.io.InputStream;

import javax.xml.bind.JAXBException;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;

public class OMN2ToscaTest {

	private InputStream input;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException {
		this.input = ParserTest.class.getResourceAsStream("/tosca-request.ttl");
		this.parser = new Parser(input);
	}

	@Test
	public void testGetTopology() throws JAXBException, InvalidModelException {
		InfModel model = this.parser.getModel();
		String topology = OMN2Tosca.getTopology(model);
		System.out.println(topology);
		Assert.assertTrue("Should be a tosca XML", topology.contains("<Definitions"));
	}

}
