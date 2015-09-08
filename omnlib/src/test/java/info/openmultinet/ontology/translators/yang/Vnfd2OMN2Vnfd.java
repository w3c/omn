package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.yang.vnfd.OMN2Vnfd;
import info.openmultinet.ontology.translators.yang.vnfd.Vnfd2OMN;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnfd.rev140103.VNFDescriptor;

import com.hp.hpl.jena.rdf.model.Model;

public class Vnfd2OMN2Vnfd {
	
	@Test
	public void testVnfd2OMN2Vnfd()
			throws InvalidModelException {
		System.out.println("############# The descriptor before translation is ################\n"
				+ Vnfd2OMN.getDesc().toString());
		Model model = Vnfd2OMN.getModel();
		String serializedModel = Vnfd2OMNTest.serializeModel(model, "TTL");
		
		System.out.println("############ The corresponding ontolgy is ########## \n" + serializedModel);

		InputStream modelStream = new ByteArrayInputStream(serializedModel.getBytes());
		Parser parser = new Parser(modelStream);
		final Model model2 = parser.getModel();
		OMN2Vnfd test = new OMN2Vnfd();
		VNFDescriptor desc = test.getDescriptor(model2);
		System.out.println("############# The descriptor after translation is ################\n" + desc.toString());
	}
	
}
