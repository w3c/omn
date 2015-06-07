package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;

import com.hp.hpl.jena.rdf.model.Model;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;

import com.hp.hpl.jena.rdf.model.InfModel;

public class ToscaDebugTest {

	public static void main(String[] args) {

		// curl --data-urlencode
		// content@src/test/resources/omn/request.ttl
		// http://federation.av.tu-berlin.de:8080/omnlib/convert/ttl/tosca

		System.out.println("*******************************************");
		System.out.println("*******   Get model                ********");
		System.out.println("*******************************************");
		InputStream stream = null;
		try {
			stream = new FileInputStream("./src/test/resources/omn/request.ttl");
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
		}

//		String myString = null;
//		try {
//			myString = IOUtils.toString(stream, "UTF-8");
//		} catch (IOException e1) {
//			e1.printStackTrace();
//		}
//		System.out.println(myString);
		
		
		System.out.println("*******************************************");
		System.out.println("*******   Convert to TOSCA         ********");
		System.out.println("*******************************************");
		Model model = null;
		try {
			model = new Parser(stream).getInfModel();
		} catch (InvalidModelException e) {
			e.printStackTrace();
		}
		String topology = null;
		try {
			topology = OMN2Tosca.getTopology(model);
		} catch (JAXBException | InvalidModelException
				| MultipleNamespacesException
				| RequiredResourceNotFoundException
				| MultiplePropertyValuesException e) {
			e.printStackTrace();
		}
		System.out.println(topology);

	}
}