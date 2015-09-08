package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.yang.vnfI.OMN2Vnfi;

import java.io.IOException;
import java.io.InputStream;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class OMN2VnfiScaleTest {
	
	
	private Parser parser;
	
	@Before
	public void setup()
			throws InvalidModelException{
		parser = new Parser();
		
	}
	
	@Test
	public void testConvertYang() throws InvalidModelException
			{
		
		InputStream input = getClass().getResourceAsStream("/omn/scaleInstance.ttl");
		Parser parser = new Parser(input);
		final Model model = parser.getModel();
		OMN2Vnfi test = new OMN2Vnfi();
		System.out.println(" the corresponding instance is \n "+ test.getInstanceModel(model) );
		
	}
}
