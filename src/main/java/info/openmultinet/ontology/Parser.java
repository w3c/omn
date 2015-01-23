package info.openmultinet.ontology;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Osco;
import info.openmultinet.ontology.vocabulary.Tosca;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
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
import com.hp.hpl.jena.rdf.model.RDFReader;
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


	protected InfModel model;

	// @todo: add support for all serializations, not only TTL
	public Parser(InputStream input) throws InvalidModelException {
		init(input);
	}
	
	public Parser(String filename) throws InvalidModelException {
		InputStream input = Parser.class.getResourceAsStream(filename);
		init(input);
	}
	
	public Parser(Model model) throws InvalidModelException{
	  init(model);
	}
	
	public void init(InputStream input) throws InvalidModelException {
		//@fixme: this is a slow/expensive operation
		Model data = ModelFactory.createDefaultModel();
		RDFReader arp = data.getReader("TTL");
		//@fixme: this is a slow/expensive operation
		arp.read(data, input, null);

		init(data);
	}
	
	public void init(Model model) throws InvalidModelException {
    this.model = createInfModel(model);
    if (!isValid(this.model))
      throw new InvalidModelException(getValidationReport(this.model));
	}
	
	public static InfModel createInfModel() throws InvalidModelException {	
		return createInfModel(ModelFactory.createDefaultModel());
	}

	public static InfModel createInfModel(Model data) throws InvalidModelException {
		Model schema = ModelFactory.createDefaultModel();

		schema.add(parse("/omn.ttl"));		
		schema.add(parse("/omn-federation.ttl"));
		schema.add(parse("/omn-lifecycle.ttl"));
		schema.add(parse("/omn-resource.ttl"));
		schema.add(parse("/omn-service.ttl"));
		schema.add(parse("/omn-component.ttl"));
		schema.add(parse("/osco.ttl"));
		schema.add(parse("/tosca.ttl"));
		
		
		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		//@fixme: this is a slow/expensive operation
		reasoner = reasoner.bindSchema(schema);

		InfModel infModel = ModelFactory.createInfModel(reasoner, data);
		setCommonPrefixes(infModel);

		return infModel;
	}
	
	public static void setCommonPrefixes(Model model){
	  model.setNsPrefix("omn", Omn.getURI());
	  model.setNsPrefix("omn-lifecycle", Omn_lifecycle.getURI());
	  model.setNsPrefix("rdf", RDF.getURI());
	  model.setNsPrefix("rdfs", RDFS.getURI());
	  model.setNsPrefix("owl", OWL.getURI());
	  model.setNsPrefix("tosca", Tosca.getURI());
	  model.setNsPrefix("osco", Osco.getURI());
	  model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
	}

	public static Model parse(String filename) {
		InputStream stream = Parser.class.getResourceAsStream(filename);
		Model model = ModelFactory.createDefaultModel().read(stream, StandardCharsets.UTF_8.toString(), "TTL");
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
				+ createPrefix("owl", OWL.getURI())
		    + createPrefix("osco", Osco.getURI())
		    + createPrefix("tosca", Tosca.getURI());
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

	public static String toString(Model model) {
		String result = "";
		for (StmtIterator i = model.listStatements(); i.hasNext();) {
			Statement stmt = i.nextStatement();
			result += PrintUtil.print(stmt) + NL;
		}
		return result;
	}
	
	public InfModel getModel() {
		return this.model;
	}

	public static boolean isValid(InfModel model) {
		ValidityReport validity = model.validate();
		return validity.isValid();
	}

	public static boolean isValid(Model model) {
		InfModel infModel = ModelFactory.createInfModel(
				ReasonerRegistry.getOWLMiniReasoner(), model);
		return isValid(infModel);
	}

	public static String getValidationReport(Model model) {
		InfModel infModel = ModelFactory.createInfModel(
				ReasonerRegistry.getOWLMiniReasoner(), model);
		return getValidationReport(infModel);
	}

	public static String getValidationReport(InfModel model) {
		String report = "";
		ValidityReport validity = model.validate();
		for (Iterator<Report> i = validity.getReports(); i.hasNext();) {
			report += " - " + i.next() + NL;
		}
		return report;
	}

	public boolean isValid() {
		return Parser.isValid(this.model);
	}

	public String getValidationReport() {
		return Parser.getValidationReport(this.model);
	}

	
}
