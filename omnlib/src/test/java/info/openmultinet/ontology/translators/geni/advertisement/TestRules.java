package info.openmultinet.ontology.translators.geni.advertisement;

import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.riot.RDFDataMgr;
import org.junit.Assert;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.rulesys.GenericRuleReasoner;
import com.hp.hpl.jena.reasoner.rulesys.Rule;

import info.openmultinet.ontology.translators.AbstractConverter;

public class TestRules {

	private static final String EXPECTED1 = "omn-monitoring-unit";
	private static final String RAWFILE1 = "geni/geni-fire-20151006/netmode.rspec.xml.ttl";
	private static final String RULEFILE1 = "/rules/rule1.txt";

	@Test
	public void test() throws IOException {

		final String rule1 = AbstractConverter.toString(RULEFILE1);
		Model rawModel1 = RDFDataMgr.loadModel(RAWFILE1);
		
		Reasoner reasoner = new GenericRuleReasoner(Rule.parseRules(rule1));
		reasoner.setDerivationLogging(true);
		InfModel infModel1 = ModelFactory.createInfModel(reasoner, rawModel1);
		Iterator<?> list = infModel1.listStatements();
		while (list.hasNext()) {
		    System.out.println(" - " + list.next());
		}
		
		Assert.assertTrue(infModel1.toString().contains("omn-monitoring-unit"));
	}
}
