package info.openmultinet.ontology.translators.geni.examples2;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.LoginManifestConverterTest;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverterTest;
import info.openmultinet.ontology.translators.geni.jaxb.advertisement.RSpecContents;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.vocabulary.RDF;


public class FourNodesTest {


	@Test
	public void manifestRoundtrip() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException {
		final String filename = "/geni/examples2/4nodes.manifest";
		final InputStream inputRspec = LoginManifestConverterTest.class
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
		Assert.assertTrue("sliver_id", outputRspec.contains("sliver_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+sliver+bd6b53fd-9185-4cd5-99cb-f9b8736010b2:0\""));
		Assert.assertTrue("exclusive", outputRspec.contains("exclusive=\"false\""));
		Assert.assertTrue("component_name", outputRspec.contains("component_name=\"orca-vm-cloud\""));
		Assert.assertTrue("component_manager_id", outputRspec.contains("component_manager_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+authority+am\""));
		Assert.assertTrue("component_id", outputRspec.contains("component_id=\"urn:publicid:IDN+exogeni.net:rcivmsite+node+orca-vm-cloud\""));
		Assert.assertTrue("client_id", outputRspec.contains("client_id=\"0\""));
		Assert.assertTrue("latitude", outputRspec.contains("latitude=\"35.939518\""));
		Assert.assertTrue("longitude", outputRspec.contains("longitude=\"-79.017426\""));
		Assert.assertTrue("country", outputRspec.contains("country=\"Unspecified\""));
		Assert.assertTrue("sliver_type", outputRspec.contains("sliver_type"));
		Assert.assertTrue("sliver_type name", outputRspec.contains("name=\"xo.small\""));
		Assert.assertTrue("disk image", outputRspec.contains("disk_image"));
		Assert.assertTrue("disk image version", outputRspec.contains("version=\"807c4570e46413cba1faf3a25fdfff8361489c69\""));
		Assert.assertTrue("disk image name", outputRspec.contains("name=\"http://pkg.mytestbed.net/geni/deb7-64-p2p.xml\""));
		Assert.assertTrue("login", outputRspec.contains("login"));
		Assert.assertTrue("login username", outputRspec.contains("username=\"root\""));
		Assert.assertTrue("login hostname", outputRspec.contains("hostname=\"152.54.14.17\""));
		Assert.assertTrue("login authentication", outputRspec.contains("authentication=\"ssh-keys\""));
		Assert.assertTrue("services_post_boot_script", outputRspec.contains("services_post_boot_script"));
		Assert.assertTrue("services_post_boot_script type", outputRspec.contains("type=\"velocity\""));
		Assert.assertTrue("services_post_boot_script text", outputRspec.contains("#!/bin/bash"));
		Assert.assertTrue("ip address", outputRspec.contains("address=\"192.168.1.1\""));
		Assert.assertTrue("ip netmask", outputRspec.contains("netmask=\"255.255.255.0\""));
		Assert.assertTrue("ip type", outputRspec.contains("type=\"ipv4\""));
	}

}
