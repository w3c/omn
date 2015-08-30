package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.translators.yang.vnfI.VnfI2OMN;
import info.openmultinet.ontology.translators.yang.vnfI.VnfiInstanceExample;
import org.junit.Test;
import org.opendaylight.yang.gen.v1.urn.ietf.params.xml.ns.yang.ietf.vnf.instance.rev140103.VNFInstance;
import com.hp.hpl.jena.rdf.model.Model;

public class VnfIMigrate2OMNTest {

	@Test
	public void testConvertYang() {
		
		VnfiInstanceExample instance = new VnfiInstanceExample();
		
		VNFInstance desc = instance.migrateInstance();
		System.out.println("########### The instance is  ########### \n" + desc.toString());

		Model model = VnfI2OMN.getMigrateModel();
		
		String serializedModel = Vnfd2OMNTest.serializeModel(model, "TTL");
		System.out.println(serializedModel);
	}

	
}
