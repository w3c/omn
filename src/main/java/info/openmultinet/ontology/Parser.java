package info.openmultinet.ontology;

import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.InputStream;
import java.net.URL;
import java.util.Iterator;

import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.ResultSetFactory;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.Reasoner;
import com.hp.hpl.jena.reasoner.ReasonerRegistry;
import com.hp.hpl.jena.reasoner.ValidityReport;
import com.hp.hpl.jena.reasoner.ValidityReport.Report;
import com.hp.hpl.jena.util.PrintUtil;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

public class Parser {

	private static String NL = System.getProperty("line.separator");
	
	private Reasoner reasoner;

	protected InfModel model;
	
	//@todo: add support for all serializations, not only TTL
	public Parser(InputStream input) {
		Model data = ModelFactory.createDefaultModel().read(input, null, "TTL");
		Model schema = ModelFactory.createDefaultModel(); 
		schema.add(parse("/omn.ttl"));
		schema.add(parse("/omn-federation.ttl"));
		schema.add(parse("/omn-lifecycle.ttl"));
		schema.add(parse("/omn-resource.ttl"));
		schema.add(parse("/omn-service.ttl"));
		schema.add(parse("/omn-component.ttl"));

        reasoner = ReasonerRegistry.getOWLMiniReasoner().bindSchema(schema);
        model = ModelFactory.createInfModel(reasoner, data);
	}

	public static Model parse(String filename) {
		URL stream = Parser.class.getResource(filename);
		Model model = ModelFactory.createDefaultModel().read(stream.toString(),
				"TTL");
		return model;
	}

	public ResultSetRewindable query(String queryString) {
		queryString = getDefaultPrefixes() + queryString;
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, this.model);
		ResultSetRewindable rewindable = ResultSetFactory.makeRewindable(qexec
				.execSelect());
		return rewindable;
	}

	public static String getDefaultPrefixes() {
		return createPrefix("omn", Omn.getURI())
				+ createPrefix("omn-lifecycle", Omn_lifecycle.getURI())
				+ createPrefix("rdf", RDF.getURI())
				+ createPrefix("rdfs", RDFS.getURI())
				+ createPrefix("owl", OWL.getURI());
	}

	public static String createPrefix(String name, String URI) {
		return "PREFIX " + name + ": <" + URI + ">" + NL;
	}

	public void printStatements(Resource s, Property p, Resource o) {
		for (StmtIterator i = model.listStatements(s, p, o); i.hasNext();) {
			Statement stmt = i.nextStatement();
			System.out.println(" - " + PrintUtil.print(stmt));
		}
	}

	public InfModel getModel() {
		return this.model;
	}

	public boolean isValid() {
		ValidityReport validity = this.model.validate();
		return validity.isValid();
	}

	public String getValidationReport() {
		String report = "";
		ValidityReport validity = this.model.validate();
		for (Iterator<Report> i = validity.getReports(); i.hasNext(); ) {
	        report += " - " + i.next() + NL;
	    }
		return report;
	}
}
