package info.openmultinet.ontology.translators.tosca;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Osco;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;

import javax.xml.bind.JAXBException;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;

public class RoundtripTest {

	static ArrayList<String> additionalOntologies;

	@Before
	public void createAdditionalOntologiesList() {
		additionalOntologies = new ArrayList<String>();
		additionalOntologies.add("/ontologies/osco.ttl");
	}

	// test broke when dom4j dependencies added to pom.xml
	// TODO fix
	// @Test
	// public void testOMN2Tosca2OMN() throws InvalidModelException,
	// JAXBException, MultipleNamespacesException,
	// RequiredResourceNotFoundException, MultiplePropertyValuesException,
	// UnsupportedException {
	// InputStream input =
	// this.getClass().getResourceAsStream("/omn/tosca-request-openmtc.ttl");
	// Parser parser = new Parser(input, additionalOntologies);
	//
	// final InfModel model = parser.getInfModel();
	// final String toscaDefinitions = OMN2Tosca.getTopology(model);
	//
	// InputStream topologyStream = new
	// ByteArrayInputStream(toscaDefinitions.getBytes());
	// Model resultModel = Tosca2OMN.getModel(topologyStream);
	//
	// for(Statement st : parser.getModel().listStatements().toList()){
	// Resource subject = null;
	// RDFNode object = null;
	// if(!st.getSubject().isAnon()){
	// subject = st.getSubject();
	// }
	// if(!st.getObject().isAnon()){
	// object = st.getObject();
	// }
	// Assert.assertTrue("Model should contain statement: "+st,
	// resultModel.contains(subject, st.getPredicate(), object));
	// }
	// }

	@Test
	//fixme: this test is slow!
	public void testTosca2OMN2Tosca() throws JAXBException,
			InvalidModelException, UnsupportedException,
			MultipleNamespacesException, RequiredResourceNotFoundException,
			MultiplePropertyValuesException {
		InputStream input = this.getClass().getResourceAsStream(
				"/tosca/response-ims.xml");
		final Model model = Tosca2OMN.getModel(input);

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		RDFDataMgr.write(baos, model, Lang.TTL);
		String serializedModel = baos.toString();

		System.out.println(serializedModel);

		Assert.assertTrue("Should contain a topology resource",
				model.containsResource(Omn.Topology));
		Assert.assertTrue("Should contain a dns node resource",
				model.containsResource(Osco.dns));
		Assert.assertTrue("Should contain a hss node resource",
				model.containsResource(Osco.hss));
		Assert.assertTrue("Should contain a scscf node resource",
				model.containsResource(Osco.scscf));
		Assert.assertTrue("Should contain a icscf node resource",
				model.containsResource(Osco.icscf));
		Assert.assertTrue("Should contain a pcscf node resource",
				model.containsResource(Osco.pcscf));

		InputStream modelStream = new ByteArrayInputStream(
				serializedModel.getBytes());
		Parser parser = new Parser(modelStream, additionalOntologies);
		InfModel infModel = parser.getInfModel();
		String toscaDefinitions = OMN2Tosca.getTopology(infModel);
		System.out.println(toscaDefinitions);
	}

}
