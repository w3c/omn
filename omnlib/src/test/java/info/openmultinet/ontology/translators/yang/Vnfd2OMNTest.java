package info.openmultinet.ontology.translators.yang;

import info.openmultinet.ontology.translators.yang.vnfd.Vnfd2OMN;
import java.io.StringWriter;
import org.junit.Test;
import com.hp.hpl.jena.rdf.model.Model;

public class Vnfd2OMNTest {
	
	
	
	
	@Test
	public void testConvertYang() {
		
		Model model = Vnfd2OMN.getModel();
		String serializedModel = Vnfd2OMNTest.serializeModel(model, "TTL");
		System.out.println(" the model is \n"+serializedModel);
	}
	
	public static String serializeModel(final Model rdfModel, final String serialization) {
		final StringWriter writer = new StringWriter();
		rdfModel.write(writer, serialization);
		return writer.toString();
	}
	
}
