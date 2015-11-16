package info.openmultinet.ontology.translators.geni.advertisement;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Wgs84;

import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Logger;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;

public class AdGeoTest {
	private static Logger LOGGER = Logger.getLogger(AdGeoTest.class.getName());

	@Test
	public void convertModelToAdRspec() throws JAXBException,
			InvalidModelException, IOException, XMLStreamException,
			MissingRspecElementException, DeprecatedRspecVersionException {

		AdvertisementConverter converter = new AdvertisementConverter();
		// converter.setVerbose(true);
		Parser parser = new Parser();
		InputStream input = AdGeoTest.class
				.getResourceAsStream("/omn/ad-no-location.ttl");
		parser.read(input);
		final Model model = parser.getModel();
		addDefaultGeoInformation(model);

		System.out.println(Parser.toString(model));
		System.out
				.println("===================================================");
		final String rspec = converter.getRSpec(model);
		System.out.println(rspec);
	}

	/**
	 * copied from org.fiteagle.core.resourceAdapterManager, but with dummy
	 * lat/long values
	 * 
	 * @param model
	 */
	public void addDefaultGeoInformation(Model model) {

		LOGGER.info("Looking for GEO information");
		ResIterator adapters = model
				.listSubjectsWithProperty(Omn_lifecycle.canImplement);

		while (adapters.hasNext()) {
			Resource adapter = adapters.next();
			LOGGER.info("Checking: " + adapter.getURI());
			if (null == adapter.getProperty(Wgs84.lat)) {
				RDFNode globalLat = ResourceFactory.createPlainLiteral("55");
				RDFNode globalLong = ResourceFactory.createTypedLiteral(66);
				LOGGER.info("Adding: " + globalLat + ", " + globalLong);
				adapter.addProperty(Wgs84.lat, globalLat);
				adapter.addProperty(Wgs84.long_, globalLong);
			}
		}
	}
}
