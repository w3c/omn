package info.openmultinet.ontology.translators.geni.gimiv3;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class FourNodesTest {

	@Test
	public void manifestRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {
		final String filename = "/geni/gimiv3/4nodes.manifest";
		final InputStream inputRspec = FourNodesTest.class
				.getResourceAsStream(filename);
		System.out.println("Converting this input from '" + filename + "':");
		System.out.println("===============================");
		System.out.println(AbstractConverter.toString(filename));
		System.out.println("===============================");

		final Model model = ManifestConverter.getModel(inputRspec);
		final ResIterator topology = model.listResourcesWithProperty(RDF.type,
				Omn_lifecycle.Manifest);
		System.out.println("Generated this graph:");
		System.out.println("===============================");
		System.out.println(Parser.toString(model));
		System.out.println("===============================");
		Assert.assertTrue("should have a topology", topology.hasNext());

		final InfModel infModel = new Parser(model).getInfModel();
		final String outputRspec = ManifestConverter.getRSpec(infModel,
				"instageni.gpolab.bbn.com");
		System.out.println("Generated this rspec:");
		System.out.println("===============================");
		System.out.println(outputRspec);
		System.out.println("===============================");

		Assert.assertTrue("type", outputRspec.contains("type=\"manifest\""));
		Assert.assertTrue(
				"sliver_id",
				outputRspec
						.contains("sliver_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+sliver+bd6b53fd-9185-4cd5-99cb-f9b8736010b2:0\""));
		Assert.assertTrue("exclusive",
				outputRspec.contains("exclusive=\"false\""));

		// TODO fix this for manifest converter
		// Assert.assertTrue("component_name",
		// outputRspec.contains("component_name=\"orca-vm-cloud\""));
		Assert.assertTrue(
				"component_manager_id",
				outputRspec
						.contains("component_manager_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+authority+am\""));
		Assert.assertTrue(
				"component_id",
				outputRspec
						.contains("component_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+node+orca-vm-cloud\""));
		Assert.assertTrue("client_id", outputRspec.contains("client_id=\"0\""));
		Assert.assertTrue("latitude",
				outputRspec.contains("latitude=\"35.939518\""));
		Assert.assertTrue("longitude",
				outputRspec.contains("longitude=\"-79.017426\""));
		Assert.assertTrue("country",
				outputRspec.contains("country=\"Unspecified\""));
		Assert.assertTrue("sliver_type", outputRspec.contains("sliver_type"));

		// TODO fix this for manifest converter
		// Assert.assertTrue("sliver_type name",
		// outputRspec.contains("name=\"xo.small\""));
		Assert.assertTrue("disk image", outputRspec.contains("disk_image"));
		Assert.assertTrue(
				"disk image version",
				outputRspec
						.contains("version=\"807c4570e46413cba1faf3a25fdfff8361489c69\""));
		Assert.assertTrue(
				"disk image name",
				outputRspec
						.contains("name=\"http://pkg.mytestbed.net/geni/deb7-64-p2p.xml\""));
		Assert.assertTrue("login", outputRspec.contains("login"));
		Assert.assertTrue("login username",
				outputRspec.contains("username=\"root\""));
		Assert.assertTrue("login hostname",
				outputRspec.contains("hostname=\"152.54.14.17\""));
		Assert.assertTrue("login authentication",
				outputRspec.contains("authentication=\"ssh-keys\""));
		Assert.assertTrue("services_post_boot_script",
				outputRspec.contains("services_post_boot_script"));
		Assert.assertTrue("services_post_boot_script type",
				outputRspec.contains("type=\"velocity\""));
		Assert.assertTrue("services_post_boot_script text",
				outputRspec.contains("#!/bin/bash"));
		Assert.assertTrue("ip address",
				outputRspec.contains("address=\"192.168.1.1\""));
		Assert.assertTrue("ip netmask",
				outputRspec.contains("netmask=\"255.255.255.0\""));
		Assert.assertTrue("ip type", outputRspec.contains("type=\"ipv4\""));
		Assert.assertTrue("geni_sliver_info",
				outputRspec.contains("geni_sliver_info"));
		Assert.assertTrue(
				"geni_sliver_info resource_id",
				outputRspec
						.contains("resource_id=\"rci-w7:bdd5b251-4a63-4bd4-b77c-cad57974602d\""));
		// Assert.assertTrue("geni_sliver_info state",
		// outputRspec.contains("state=\"ready\""));
		Assert.assertTrue("geni_sliver_info start_time",
				outputRspec.contains("start_time=\"2013-12-03T00:57:47.000Z\""));
		Assert.assertTrue("geni_sliver_info expiration_time", outputRspec
				.contains("expiration_time=\"2013-12-10T00:55:03.000Z\""));
		Assert.assertTrue("geni_sliver_info creation_time", outputRspec
				.contains("creation_time=\"2013-12-03T00:57:47.000Z\""));
		Assert.assertTrue(
				"geni_sliver_info creator_urn",
				outputRspec
						.contains("creator_urn=\"johren@bbn.com, urn:publicid:IDN+ch.geni.net+user+johren\""));

		Assert.assertTrue("link", outputRspec.contains("link"));
		Assert.assertTrue(
				"link sliver_id",
				outputRspec
						.contains("sliver_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+sliver+bd6b53fd-9185-4cd5-99cb-f9b8736010b2:link0\""));
		Assert.assertTrue("link client_id",
				outputRspec.contains("client_id=\"link0\""));
		Assert.assertTrue("link vlantag",
				outputRspec.contains("vlantag=\"2200\""));
		Assert.assertTrue("interface_ref",
				outputRspec.contains("interface_ref"));
		Assert.assertTrue("interface_ref client_id",
				outputRspec.contains("client_id=\"3:if0\""));

		Assert.assertTrue("geni_slice_info",
				outputRspec.contains("geni_slice_info"));
		Assert.assertTrue("geni_slice_info uuid", outputRspec
				.contains("uuid=\"5fcc5c81-c686-4e02-89bb-8dd7162697d3\""));
		Assert.assertTrue(
				"geni_slice_info urn",
				outputRspec
						.contains("urn=\"urn:publicid:IDN+ch.geni.net:GIMITesting+slice+joOEDLTut\""));

		System.out.println("===============================");
		String inputRSpec = AbstractConverter.toString(filename);
		System.out.println(inputRSpec);
		System.out.println("Diffs:");
		int[] diffsNodes = RSpecValidation.getDiffsNodes(inputRSpec);

		// TODO: This test does not consistently return 0, only sometimes. Need
		// to debug.
		// Assert.assertTrue("No differences between input and output files",
		// diffsNodes[0] == 0);

	}

}
