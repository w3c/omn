package info.openmultinet.ontology.translators.geni.advertisement;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;

public class TestRules {

	private static final String EXPECT_AMS = "AMService";
	private static final String EXPECT_MON = "omn-monitoring-unit";
	private static final String TEST_XML = "/geni/geni-fire-20151006/netmode.rspec.xml";
	private static final String TEST_TTL = "geni/geni-fire-20151006/netmode.rspec.xml.ttl";
	private static final String TEST_RULE = "/rules/ruleParserTest1.rules";
	private static final String TEST_IMINDS_XML = "/geni/fed4fire/vwall2.rspec.small.xml";
	private static final String TEST_IMIND_EXP = "Label for testing";

	@Test
	public void testSimpleRuleset() throws IOException {
		InputStream is = TestRules.class.getResourceAsStream(TEST_RULE);
		BufferedReader br = new BufferedReader(new InputStreamReader(is,
				"UTF-8"));
		Model rawModel1 = RDFDataMgr.loadModel(TEST_TTL);

		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(Rule
				.rulesParserFromReader(br)));
		reasoner.setDerivationLogging(true);
		InfModel infModel1 = ModelFactory.createInfModel(reasoner, rawModel1);
		Assert.assertTrue(infModel1.toString().contains(EXPECT_MON));
	}

	@Test
	public void testLoadAllRulesets() throws IOException, URISyntaxException {
		AbstractConverter.getResourceListing("rules");
		List<Rule> rules = AbstractConverter.getAllRules();
		Assert.assertTrue(rules.size() > 0);
	}

	@Test
	public void testIntegratedRules() throws IOException, URISyntaxException,
			JAXBException, InvalidModelException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {
		final String inputRSpecString = AbstractConverter.toString(TEST_XML);
		InputStream inputRSpecStream = IOUtils.toInputStream(inputRSpecString,
				StandardCharsets.UTF_8);
		Model inputRSpecModel = new AdvertisementConverter()
				.getModel(inputRSpecStream);
		Assert.assertTrue(inputRSpecModel.toString().contains(
				TestRules.EXPECT_AMS));
	}

	@Test
	public void testIntegratedIMindsRules() throws IOException,
			URISyntaxException, JAXBException, InvalidModelException,
			XMLStreamException, MissingRspecElementException,
			InvalidRspecValueException {
		final String inputRSpecString = AbstractConverter
				.toString(TEST_IMINDS_XML);
		InputStream inputRSpecStream = IOUtils.toInputStream(inputRSpecString,
				StandardCharsets.UTF_8);
		Model inputRSpecModel = new AdvertisementConverter()
				.getModel(inputRSpecStream);
		Assert.assertTrue(inputRSpecModel.toString().contains(
				TestRules.TEST_IMIND_EXP));
		// AdvertisementConverter.print(inputRSpecModel);
		RDFDataMgr.write(System.out, inputRSpecModel, Lang.N3);
	}

}
