package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.yang.vnfI.OMN2Vnfi;
import info.openmultinet.ontology.translators.yang.vnfI.VnfI2OMN;
import info.openmultinet.ontology.translators.yang.vnfI.VnfiInstanceExample;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;

import com.hp.hpl.jena.rdf.model.Model;

public class RoudTripVnfiScale {
	
	@Test
	public void testVnfi2OMN2Vnfi()
			throws InvalidModelException {
		
		VnfiInstanceExample instance = new VnfiInstanceExample();
		VNFInstance desc = instance.scaleInstance();
		System.out.println("########### The instance before translation is ########### \n" + desc.toString());
		Model model = VnfI2OMN.getScaleModel();
		String serializedModel = Vnfd2OMNTest.serializeModel(model, "TTL");
		
		System.out.println("####### The corresponding ontology is #######\n" + serializedModel);
		InputStream modelStream = new ByteArrayInputStream(serializedModel.getBytes());
		Parser parser = new Parser(modelStream);

		final Model model2 = parser.getModel();
		OMN2Vnfi test = new OMN2Vnfi();
		VNFInstance newInstance = test.getInstanceModel(model2);
		
		System.out.println("############# The instance after translation is ################\n"
				+ newInstance.toString());
	}
	
}
