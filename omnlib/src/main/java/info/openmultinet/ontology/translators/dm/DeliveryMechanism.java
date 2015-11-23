package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.RSpecValidation;
import info.openmultinet.ontology.translators.geni.RequestConverter;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.UnknownFormatConversionException;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class DeliveryMechanism {

	public static String convert(String from, String to, String content)
			throws JAXBException, InvalidModelException, UnsupportedException,
			IOException, MultipleNamespacesException,
			RequiredResourceNotFoundException, MultiplePropertyValuesException,
			XMLStreamException, MissingRspecElementException,
			DeprecatedRspecVersionException, InvalidRspecValueException {

		if (AbstractConverter.ANYFORMAT.equalsIgnoreCase(from)) {
			from = RSpecValidation.getType(content);
		}

		if (to.equalsIgnoreCase(from)) {
			return content;
		}

		// check if RSpec version 2 is used and convert to version 3 or throw
		// exception if version 0.1
		if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)
				|| AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(from)
				|| AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(from)) {
			content = RSpecValidation.fixVersion(content);
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final InputStream stream = new ByteArrayInputStream(
				content.getBytes(StandardCharsets.UTF_8));
		Model model = null;

		if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)) {
			model = RequestConverter.getModel(stream);
		} else if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(from)) {
			model = new AdvertisementConverter().getModel(stream);
		} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(from)) {
			model = ManifestConverter.getModel(stream);
		} else if (AbstractConverter.RDFXML.equalsIgnoreCase(from)) {
			Parser parser = new Parser(stream, AbstractConverter.RDFXML);
			model = parser.getInfModel();
		} else if (AbstractConverter.TTL.equalsIgnoreCase(from)) {
			model = new Parser(stream).getInfModel();
		} else if (AbstractConverter.TOSCA.equalsIgnoreCase(from)) {
			model = Tosca2OMN.getModel(stream);
		} else {
			throw new UnknownFormatConversionException("unknown input '" + from
					+ "'");
		}

		// String modelString = Parser.toString(model);
		// System.out.println(modelString);

		if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(to)) {
			final String rspec = new AdvertisementConverter().getRSpec(model);
			baos.write(rspec.getBytes());
		} else if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(to)) {
			final String rspec = RequestConverter.getRSpec(model);
			baos.write(rspec.getBytes());
		} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(to)) {
			final String rspec = ManifestConverter.getRSpec(model, "localhost");
			baos.write(rspec.getBytes());
		} else if (AbstractConverter.TOSCA.equalsIgnoreCase(to)) {
			final String toplogy = OMN2Tosca.getTopology(model);
			baos.write(toplogy.getBytes());
		} else if (AbstractConverter.TTL.equalsIgnoreCase(to)) {
			RDFDataMgr.write(baos, model, Lang.TTL);
		} else if (AbstractConverter.RDFXML.equalsIgnoreCase(to)) {
			RDFDataMgr.write(baos, model, Lang.RDFXML);
		} else if (AbstractConverter.JSON.equalsIgnoreCase(to)) {
			RDFDataMgr.write(baos, model, Lang.JSONLD);
		} else {
			throw new UnknownFormatConversionException("unknown output '" + to
					+ "'");
		}

		return baos.toString();
	}

	/**
	 * Gives standard model (not inferred model!) back
	 *
	 * @param content
	 * @return standard model (not inferred model!)
	 * @throws DeprecatedRspecVersionException
	 * @throws JAXBException
	 * @throws InvalidModelException
	 * @throws MissingRspecElementException
	 * @throws XMLStreamException
	 * @throws UnsupportedException
	 * @throws InvalidRspecValueException
	 */
	public static Model getModelFromUnkownInput(String content)
			throws DeprecatedRspecVersionException, JAXBException,
			InvalidModelException, MissingRspecElementException,
			XMLStreamException, UnsupportedException,
			InvalidRspecValueException {

		String from = RSpecValidation.getType(content);

		// check if RSpec version 2 is used and convert to version 3 or throw
		// exception if version 0.1
		if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)
				|| AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(from)
				|| AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(from)) {
			content = RSpecValidation.fixVersion(content);
		}

		final InputStream stream = new ByteArrayInputStream(
				content.getBytes(StandardCharsets.UTF_8));
		Model model = null;

		if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)) {
			model = RequestConverter.getModel(stream);
		} else if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(from)) {
			model = new AdvertisementConverter().getModel(stream);
		} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(from)) {
			model = ManifestConverter.getModel(stream);
		} else if (AbstractConverter.TTL.equalsIgnoreCase(from)) {

			model = ModelFactory.createDefaultModel();
			model.read(stream, StandardCharsets.UTF_8.toString(), "TTL");

		} else if (AbstractConverter.TOSCA.equalsIgnoreCase(from)) {
			model = Tosca2OMN.getModel(stream);
		} else {
			throw new UnknownFormatConversionException("unknown input '" + from
					+ "'");
		}

		return model;
	}

}
