package info.openmultinet.ontology.translators.geni.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.geni.AbstractConverter;
import info.openmultinet.ontology.translators.geni.OMN2Advertisement;
import info.openmultinet.ontology.translators.geni.OMN2Manifest;
import info.openmultinet.ontology.translators.geni.Request2OMN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Logger;

import javax.ws.rs.BadRequestException;
import javax.ws.rs.FormParam;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.xml.bind.JAXBException;

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RiotException;

import com.hp.hpl.jena.rdf.model.Model;

@Path("/convert")
public class RESTConverter {

	private static final Logger LOG = Logger.getLogger(RESTConverter.class
			.getName());

	@POST
	@Path("{from}/{to}")
	@Produces("text/plain")
	public String convert(@PathParam("from") String from,
			@PathParam("to") String to, @FormParam("content") String content) {
		LOG.info("Converting from '" + from + "' to '" + to + "'...");

		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream stream = new ByteArrayInputStream(
				content.getBytes(StandardCharsets.UTF_8));
		Model model;

		try {
			if (AbstractConverter.RSPEC_REQUEST.equals(from)) {
				model = Request2OMN.getModel(stream);
			} else {
				model = new Parser(stream).getModel();
			}

			if (AbstractConverter.RSPEC_ADVERTISEMENT.equals(to)) {
				String rspec = OMN2Advertisement.getRSpec(model);
				baos.write(rspec.getBytes());
			} else if (AbstractConverter.RSPEC_MANIFEST.equals(to)) {
				String rspec = OMN2Manifest.getRSpec(model);
				baos.write(rspec.getBytes());
			} else {
				RDFDataMgr.write(baos, model, Lang.TTL);
			}
		} catch (RiotException e) {
			throw new BadRequestException(e);
		} catch (JAXBException | InvalidModelException | IOException e) {
			throw new InternalServerErrorException(e);
		}

		return baos.toString();
	}
}
