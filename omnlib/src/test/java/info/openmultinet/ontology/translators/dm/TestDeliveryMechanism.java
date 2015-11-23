package info.openmultinet.ontology.translators.dm;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

public class TestDeliveryMechanism {

	@Test
	public void testRequestWithNamespaces() throws IOException,
			DeprecatedRspecVersionException, JAXBException,
			InvalidModelException, MissingRspecElementException,
			XMLStreamException, UnsupportedException,
			InvalidRspecValueException {
		final String filename = "/geni/request/request_namespace.xml";
		final String inputRspec = AbstractConverter.toString(filename);
		Model model = DeliveryMechanism.getModelFromUnkownInput(inputRspec);
		Assert.assertNotNull(model);

		// System.out.println(Parser.toString(model));
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Request);
		Assert.assertTrue(topology.hasNext());
	}
}