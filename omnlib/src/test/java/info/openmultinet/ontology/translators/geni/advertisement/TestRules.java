package info.openmultinet.ontology.translators.geni.advertisement;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.List;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
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
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;

public class TestRules {

	private static final String EXPECTED1 = "omn-monitoring-unit";
	private static final String RAWXML = "/geni/geni-fire-20151006/netmode.rspec.xml";
	private static final String RAWFILE1 = "geni/geni-fire-20151006/netmode.rspec.xml.ttl";
	private static final String RULEFILE1 = "/rules/rule1.txt";
	private static final String FOLDER = "/rules/";

	@Test
	public void testSimpleRuleset() throws IOException {

		final String rule1 = AbstractConverter.toString(RULEFILE1);
		Model rawModel1 = RDFDataMgr.loadModel(RAWFILE1);
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rule1));
		reasoner.setDerivationLogging(true);
		InfModel infModel1 = ModelFactory.createInfModel(reasoner, rawModel1);
		Assert.assertTrue(infModel1.toString().contains(EXPECTED1));
	}

	@Test
	public void testLoadAllRulesets() throws IOException, URISyntaxException {
		List<Rule> rules = AbstractConverter.getAllRules();
		Assert.assertTrue(rules.size() > 0);
	}
	@Test
	public void testIntegratedRules() throws IOException, URISyntaxException, JAXBException, InvalidModelException, XMLStreamException, MissingRspecElementException {
		final String inputRspec = AbstractConverter.toString(RAWXML);
		InputStream foo = IOUtils.toInputStream(inputRspec, StandardCharsets.UTF_8);
		Model a = new AdvertisementConverter().getModel(foo);
		AbstractConverter.print(a);
		Assert.assertTrue(a.toString().contains("AMService"));
	}
	
}
