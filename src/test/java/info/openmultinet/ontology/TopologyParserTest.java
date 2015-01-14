package info.openmultinet.ontology;

import java.io.IOException;
import java.io.InputStream;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class TopologyParserTest {

	private InputStream input;
	private TopologyParser parser;

	@Before
	public void setup() {
		this.input = ParserTest.class.getResourceAsStream("/request.ttl");
		this.parser = new TopologyParser(input);
	}

	@Test
	public void testGetTopologies() throws IOException {
		ResIterator groups = this.parser.getGroups();
		Assert.assertTrue("expecting to find a group via reasoning",
				groups.hasNext());

		ResIterator resourcesInGraph = this.parser.getResources();
		Assert.assertTrue(
				"expecting to find at least one resource in the graph",
				resourcesInGraph.hasNext());

		Resource topology = groups.next();
		StmtIterator resourcesInTopology = this.parser.getResources(topology);
		Assert.assertTrue(
				"expecting to find at least one resource in the topology",
				resourcesInTopology.hasNext());

		boolean hasMotor = false;
		while (resourcesInTopology.hasNext()) {
			Statement resource = resourcesInTopology.next();
			if (resource.getResource().getURI().contains("Motor"))
				hasMotor = true;
		}
		Assert.assertTrue("expecting to find at least one motor", hasMotor);
	}

}
