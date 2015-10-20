package info.openmultinet.ontology;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;
import info.openmultinet.ontology.vocabulary.Omn_domain_nfv;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotNull;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

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
	private Model model;
	protected InfModel infModel;
	private Model data;
	private static Reasoner reasoner;
	public String inputType;

	// @todo: add support for all serializations, not only TTL
	public Parser(final InputStream input) throws InvalidModelException {
		this.read(input);
	}

	public Parser(final InputStream input, List<String> additionalOntologies)
			throws InvalidModelException {
		this.read(input, additionalOntologies);
	}

	public Parser(final String filename) throws InvalidModelException {
		this.read(filename);
	}

	public Parser(final String filename, List<String> additionalOntologies)
			throws InvalidModelException {
		this.read(filename, additionalOntologies);
	}

	public Parser(final Model model) throws InvalidModelException {
		this.read(model, new ArrayList<String>());
	}

	public Parser(final Model model, List<String> additionalOntologies)
			throws InvalidModelException {
		this.read(model, additionalOntologies);
	}

	public Parser() {
		this.init();
	}

	public Parser(InputStream input, String inputType)
			throws InvalidModelException {
		this.inputType = inputType;
		this.read(input);
	}

	private void init() {
		// @fixme: this is a slow/expensive operation
		this.data = ModelFactory.createDefaultModel();
	}

//	public void setInputType(String inputType) {
//		this.inputType = inputType;
//	}

	public void read(@NotNull final InputStream input,
			List<String> additionalOntologies) throws InvalidModelException {
		init();
		if (null == input)
			throw new IllegalArgumentException("input must not be null");

		RDFReader arp;
		if (inputType != null && inputType.equals(AbstractConverter.RDFXML)) {
			arp = data.getReader();
		} else {
			arp = data.getReader("TTL");
		}

		try {
			// @fixme: this is a slow/expensive operation
			arp.read(data, input, null);
		} catch (org.apache.jena.riot.RiotException e) {
			throw new InvalidModelException(
					"org.apache.jena.riot.RiotException");
		}
		this.read(data, additionalOntologies);
	}

	public void read(@NotNull final InputStream input)
			throws InvalidModelException {
		read(input, new ArrayList<String>());
	}

	public void read(@NotNull final Model model,
			List<String> additionalOntologies) throws InvalidModelException {
		this.model = model;
		Parser.reasoner = Parser.createReasoner(additionalOntologies);
		this.infModel = Parser.createInfModel(model);
		if (!Parser.isValid(this.infModel)) {
			throw new InvalidModelException(
					Parser.getValidationReport(this.infModel));
		}
	}

	public void read(String filename, List<String> additionalOntologies)
			throws InvalidModelException {
		final InputStream input = Parser.class.getResourceAsStream(filename);
		read(input, additionalOntologies);
	}

	public void read(String filename) throws InvalidModelException {
		read(filename, new ArrayList<String>());
	}

	public static InfModel createInfModel() throws InvalidModelException {
		return Parser.createInfModel(ModelFactory.createDefaultModel());
	}

	public static InfModel createInfModel(final Model data)
			throws InvalidModelException {
		final InfModel infModel = ModelFactory.createInfModel(Parser.reasoner,
				data);
		Parser.setCommonPrefixes(infModel);
		return infModel;
	}

	public static Reasoner createReasoner() {
		return createReasoner(new ArrayList<String>());
	}

	public static Reasoner createReasoner(List<String> additionalFileNames) {
		final Model schema = ModelFactory.createDefaultModel();

		schema.add(Parser.parse("/omn.ttl"));
		schema.add(Parser.parse("/omn-federation.ttl"));
		schema.add(Parser.parse("/omn-lifecycle.ttl"));
		schema.add(Parser.parse("/omn-resource.ttl"));
		schema.add(Parser.parse("/omn-service.ttl"));
		schema.add(Parser.parse("/omn-component.ttl"));
		schema.add(Parser.parse("/osco.ttl"));
		for (String filename : additionalFileNames) {
			schema.add(Parser.parse(filename));
		}

		Reasoner reasoner = ReasonerRegistry.getOWLMiniReasoner();
		// @fixme: this is a slow/expensive operation
		reasoner = reasoner.bindSchema(schema);
		return reasoner;
	}

	public static void setCommonPrefixes(final Model model) {
		model.setNsPrefix("omn", Omn.getURI());
		model.setNsPrefix("omn-lifecycle", Omn_lifecycle.getURI());
		model.setNsPrefix("omn-resource", Omn_resource.getURI());
		model.setNsPrefix("rdf", RDF.getURI());
		model.setNsPrefix("rdfs", RDFS.getURI());
		model.setNsPrefix("owl", OWL.getURI());
		model.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		model.setNsPrefix("geo", "http://www.w3.org/2003/01/geo/wgs84_pos#");
		// added the omn-domain-nfv prefix
		model.setNsPrefix("omn-domain-nfv", Omn_domain_nfv.getURI());
	}

	public static Model parse(final String filename) {
		final InputStream stream = Parser.class.getResourceAsStream(filename);
		final Model model = ModelFactory.createDefaultModel().read(stream,
				StandardCharsets.UTF_8.toString(), "TTL");
		return model;
	}

	public ResultSetRewindable query(String queryString) {
		queryString = Parser.getDefaultPrefixes() + queryString;
		final Query query = QueryFactory.create(queryString);
		final QueryExecution qexec = QueryExecutionFactory.create(query,
				this.infModel);
		final ResultSetRewindable rewindable = ResultSetFactory
				.makeRewindable(qexec.execSelect());
		return rewindable;
	}

	public static String getDefaultPrefixes() {
		return Parser.createPrefix("omn", Omn.getURI())
				+ Parser.createPrefix("omn-lifecycle", Omn_lifecycle.getURI())
				+ Parser.createPrefix("rdf", RDF.getURI())
				+ Parser.createPrefix("rdfs", RDFS.getURI())
				+ Parser.createPrefix("owl", OWL.getURI());
	}

	public static String createPrefix(final String name, final String URI) {
		return "PREFIX " + name + ": <" + URI + ">" + Parser.NL;
	}

	public void printStatements(final Resource s, final Property p,
			final Resource o) {
		for (final StmtIterator i = this.infModel.listStatements(s, p, o); i
				.hasNext();) {
			final Statement stmt = i.nextStatement();
			System.out.println(" - " + PrintUtil.print(stmt));
		}
	}

	public static String toString(final Model model) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		setCommonPrefixes(model);
		RDFDataMgr.write(baos, model, Lang.TURTLE);
		return baos.toString();
	}

	public InfModel getInfModel() {
		return this.infModel;
	}

	public Model getModel() {
		return this.model;
	}

	public static boolean isValid(final InfModel model) {
		final ValidityReport validity = model.validate();
		return validity.isValid();
	}

	public static boolean isValid(final Model model) {
		final InfModel infModel = ModelFactory.createInfModel(
				ReasonerRegistry.getOWLMiniReasoner(), model);
		return Parser.isValid(infModel);
	}

	public static String getValidationReport(final Model model) {
		final InfModel infModel = ModelFactory.createInfModel(
				ReasonerRegistry.getOWLMiniReasoner(), model);
		return Parser.getValidationReport(infModel);
	}

	public static String getValidationReport(final InfModel model) {
		String report = "";
		final ValidityReport validity = model.validate();
		for (final Iterator<Report> i = validity.getReports(); i.hasNext();) {
			report += " - " + i.next() + Parser.NL;
		}
		return report;
	}

	public boolean isValid() {
		return Parser.isValid(this.infModel);
	}

	public String getValidationReport() {
		return Parser.getValidationReport(this.infModel);
	}

}
