package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.commons.cli.BasicParser;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.io.FileUtils;

public class CLI {
	private static final String NAME = CLI.class.getCanonicalName();

	public static void main(String[] args) throws IOException, JAXBException,
			InvalidModelException, UnsupportedException,
			MultipleNamespacesException, RequiredResourceNotFoundException,
			MultiplePropertyValuesException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException,
			InvalidRspecValueException {

		CommandLine lvCmd = null;
		HelpFormatter lvFormater = new HelpFormatter();
		CommandLineParser lvParser = new BasicParser();
		Options lvOptions = new Options();

		Option lvHelp = new Option("h", "help", false, "shows this help.");
		lvOptions.addOption(lvHelp);

		lvOptions.addOption(OptionBuilder.withLongOpt("input")
				.withArgName("FILENAME").withDescription("input filename.")
				.hasArg().isRequired().create("i"));

		lvOptions.addOption(OptionBuilder.withLongOpt("output")
				.withArgName("FORMAT")
				.withDescription("output format (TTL/TOSCA/RSpec).").hasArg()
				.isRequired().create("o"));

		try {
			lvCmd = lvParser.parse(lvOptions, args);

			if (lvCmd.hasOption('h')) {
				lvFormater.printHelp(NAME, lvOptions);
				return;
			}
		} catch (ParseException pvException) {
			lvFormater.printHelp(NAME, lvOptions);
			System.out.println("Error:" + pvException.getMessage());
			return;
		}

		String filename = lvCmd.getOptionValue("i");
		String outSerialization = lvCmd.getOptionValue("o");
		String inSerialization = guessSerialization(filename);

		String content = FileUtils.readFileToString(new File(filename),
				StandardCharsets.UTF_8.toString());
		System.out.println(DeliveryMechanism.convert(inSerialization,
				outSerialization, content));
	}

	private static String guessSerialization(String filename) {
		return AbstractConverter.ANYFORMAT;
	}

}
