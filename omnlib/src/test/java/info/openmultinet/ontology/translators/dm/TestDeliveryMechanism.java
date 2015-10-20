package info.openmultinet.ontology.translators.dm;

import java.io.IOException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

public class TestDeliveryMechanism {


    @Test
    @Ignore
    //@fixme: robyn
    public void testRequestWithNamespaces() throws IOException, DeprecatedRspecVersionException, JAXBException, InvalidModelException, MissingRspecElementException, XMLStreamException, UnsupportedException {
	final String filename = "/geni/request/request_namespace.xml";
	final String inputRspec = AbstractConverter.toString(filename);
	Model a = DeliveryMechanism.getModelFromUnkownInput(inputRspec);
	Assert.assertNotNull(a);
    }

}
