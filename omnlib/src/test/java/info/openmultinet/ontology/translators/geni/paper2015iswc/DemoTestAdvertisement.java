package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Omn_resource;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class DemoTestAdvertisement {

	private AdvertisementConverter converter;
	private Parser parser;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.parser = new Parser();
		this.converter = new AdvertisementConverter();
	}

	@Test
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {
		long start;
		start = System.nanoTime();

		System.out.println("================================================");
		System.out.println("Reading Graph");
		System.out.println("================================================");

		parser.read("/omn/paper2015iswc/omn_1_offer.ttl");
		final Model model = parser.getModel();
		System.out.println(Parser.toString(model));

		System.out.println("================================================");
		System.out.println("Converting to RSpec");
		System.out.println("================================================");

		final String rspec = converter.getRSpec(model);

		System.out.println(rspec);
		Assert.assertTrue("should be an advertisement",
				rspec.contains("type=\"advertisement\""));

		Assert.assertTrue("should have a DiskImage",
				rspec.contains("disk_image"));

		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Converting back to graph");
		System.out.println("================================================");

		start = System.nanoTime();
		InputStream input = IOUtils.toInputStream(rspec, "UTF-8");
		RSpecContents rspecContents = converter.getRspec(input);
		final Model newModel = converter.getModel(rspecContents);
		System.out.println("Result:");
		String modStr = Parser.toString(newModel);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));

		final ResIterator topology = newModel.listResourcesWithProperty(
				RDF.type, Omn_lifecycle.Offering);
		Assert.assertTrue("should have a topology", topology.hasNext());
		Resource requestResource = topology.nextResource();

		StmtIterator nodes = requestResource.listProperties(Omn.hasResource);
		Assert.assertTrue("should have a resource", nodes.hasNext());

		Resource resourceResource = nodes.next().getResource();
		Assert.assertTrue("object of hasResource is of type node",
				resourceResource.hasProperty(RDF.type, Omn_resource.Node));
		Assert.assertTrue(
				"object of hasResource is vm-server123",
				resourceResource.getURI().toString()
						.equals("http://demo.fiteagle.org/about#vm-server123"));

		StmtIterator vms = resourceResource
				.listProperties(Omn_lifecycle.canImplement);
		Resource anyVm = vms.next().getResource();

		Assert.assertTrue(
				"vm-server123 can implement a VM",
				anyVm.getURI()
						.toString()
						.equals("http://demo.fiteagle.org/resource/vm-server/123/mySmallVM")
						|| anyVm.getURI()
								.toString()
								.equals("http://demo.fiteagle.org/resource/vm-server/123/myLargeVM")
						|| anyVm.getURI()
								.toString()
								.equals("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn-domain-pc#VM"));

	}

}
