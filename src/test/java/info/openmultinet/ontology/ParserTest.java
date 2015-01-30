package info.openmultinet.ontology;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class ParserTest {

	private InputStream input;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException {
		this.input = ParserTest.class.getResourceAsStream("/omn/request.ttl");
		this.parser = new Parser(this.input);
	}

	@Test
	public void testPerformance() throws IOException {

	}

	@Test
	public void testSPARQLQuery() throws IOException {
		final String queryString = "SELECT ?group WHERE {?group a omn:Group}";
		final ResultSet result = this.parser.query(queryString);
		Assert.assertTrue("expecting to find a group via reasoning",
				result.hasNext());

		while (result.hasNext()) {
			final QuerySolution solution = result.nextSolution();
			final RDFNode node = solution.get("group");
			final String groupURI = node.asNode().getURI();
			System.out
			.println("Details about the requested group: " + groupURI);
			final Resource requestedGroupDetails = this.parser.getInfModel()
					.getResource(groupURI);
			this.parser.printStatements(requestedGroupDetails, null, null);

			final Property requestedResourceDetails = this.parser.getInfModel()
					.getProperty(Omn.hasResource.getURI());
			this.parser.printStatements(null, requestedResourceDetails, null);
		}
	}

	@Test
	public void testModelValidation() throws IOException {
		Assert.assertTrue("model should be valid", this.parser.isValid());
		Assert.assertTrue("report should be empty", this.parser
				.getValidationReport().isEmpty());
	}

	@Test
	public void testModelQuery() throws IOException {
		final Model model = this.parser.getInfModel();
		ResIterator requests;

		requests = model.listSubjectsWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue("expecting to find a request", requests.hasNext());

		requests = model.listSubjectsWithProperty(RDF.type, Omn.Group);
		Assert.assertTrue("expecting to find a group via reasoning",
				requests.hasNext());

		while (requests.hasNext()) {
			final Resource request = requests.next();
			final StmtIterator statements = request.listProperties();
			System.out.println("The request URI: " + request);
			while (statements.hasNext()) {
				final Statement statement = statements.next();
				if (statement.getPredicate().equals(Omn.hasResource)) {
					System.out.println("Requested Resource: "
							+ statement.getObject());
				} else if (statement.getPredicate().equals(RDFS.comment)) {
					System.out.println("Comment: " + statement.getObject());
				} else {
					System.out.println("Some more information: "
							+ statement.getPredicate() + " "
							+ statement.getObject());
				}
			}
		}
	}
}
