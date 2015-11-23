package info.openmultinet.ontology.translators.geni.paper2015iswc;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;

public class AdRspecDemoTest {

	private AdvertisementConverter converter;

	@Before
	public void setup() throws InvalidModelException, JAXBException {
		this.converter = new AdvertisementConverter();
	}

	@Test
	public void testLoginRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, InvalidRspecValueException {
		long start;
		String inputFile = "/geni/advertisement/demo-test.xml";

		System.out.println("================================================");
		System.out.println("Operation: Reading reference advertisement RSpec");
		start = System.nanoTime();
		final InputStream input = AdvertisementConverter.class
				.getResourceAsStream(inputFile);
		System.out.println("Input:");
		String inStr = AbstractConverter.toString(inputFile);
		System.out
				.println(StringUtils.abbreviateMiddle(inStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to object model (jaxb)");
		RSpecContents rspec = converter.getRspec(input);
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		System.out.println("================================================");
		System.out.println("Operation: Converting to omn graph");
		start = System.nanoTime();
		final Model model = converter.getModel(rspec);
		System.out.println("Result:");
		String modStr = Parser.toString(model);
		System.out.println(StringUtils
				.abbreviateMiddle(modStr, "\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));

		System.out.println("================================================");
		System.out
				.println("Converting to reference advertisement RSpec again...");
		start = System.nanoTime();
		final String advertisement = converter.getRSpec(model);
		System.out.println("Result:");
		System.out.println(StringUtils.abbreviateMiddle(advertisement,
				"\n...\n", 4096));
		System.out.println("Duration: " + (System.nanoTime() - start));
		System.out.println("================================================");

		Assert.assertTrue("type",
				advertisement.contains("type=\"advertisement\""));
		Assert.assertTrue(
				"component_id",
				advertisement
						.contains("component_id=\"urn:publicid:IDN+testbed.example.org+node+http%3A%2F%2Ftestbed.example.org%2Fresources%23Openstack-1\""));
		Assert.assertTrue(
				"component_manager_id",
				advertisement
						.contains("component_manager_id=\"urn:publicid:IDN+testbed.example.org+authority+cm\""));
		Assert.assertTrue("component_name",
				advertisement.contains("component_name=\"Openstack-1\""));
		Assert.assertTrue("exclusive",
				advertisement.contains("exclusive=\"false\""));
		Assert.assertTrue("sliver 1",
				advertisement.contains("sliver_type name=\"VM-large\""));
		Assert.assertTrue("sliver 2",
				advertisement.contains("sliver_type name=\"VM-medium\""));
		Assert.assertTrue("sliver 3",
				advertisement.contains("sliver_type name=\"VM-small\""));
		Assert.assertTrue("disk_image 1",
				advertisement.contains("disk_image name=\"OpenMTC-image-1\""));
		Assert.assertTrue("disk_image 2",
				advertisement.contains("disk_image name=\"cirros-linux\""));
		Assert.assertTrue("disk_image 3",
				advertisement.contains("disk_image name=\"ubuntu-14.04\""));
	}

}
