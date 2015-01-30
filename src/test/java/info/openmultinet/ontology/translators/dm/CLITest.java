package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.junit.Test;

public class CLITest {

	@Test
	public void testMain() throws IOException, JAXBException, InvalidModelException, UnsupportedException, MultipleNamespacesException, RequiredResourceNotFoundException, MultiplePropertyValuesException {
        CLI.main(new String[] {"-i", "src/test/resources/geni/request/request_bound.xml", "-o" ,"ttl"});
	}

}
