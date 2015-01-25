package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.AdvertisementConverter;
import info.openmultinet.ontology.translators.geni.ManifestConverter;
import info.openmultinet.ontology.translators.geni.RequestConverter;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
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
	public String convert(@PathParam("from") final String from,
			@PathParam("to") final String to,
			@FormParam("content") final String content) {
		RESTConverter.LOG.log(Level.INFO, "Converting from '" + from + "' to '"
				+ to + "'...");

		if ((null == content) || (0 == content.length())) {
			throw new ConverterWebApplicationException(
					Response.Status.NOT_ACCEPTABLE, "empty input");
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream();
		final InputStream stream = new ByteArrayInputStream(
				content.getBytes(StandardCharsets.UTF_8));
		Model model;
		try {
			if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)) {
				model = RequestConverter.getModel(stream);
			} else if (AbstractConverter.RSPEC_ADVERTISEMENT
					.equalsIgnoreCase(from)) {
				model = AdvertisementConverter.getModel(stream);
			} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(from)) {
				model = ManifestConverter.getModel(stream);
			} else if (AbstractConverter.TTL.equalsIgnoreCase(from)) {
				model = new Parser(stream).getModel();
			} else if (AbstractConverter.TOSCA.equalsIgnoreCase(from)) {
				model = Tosca2OMN.getModel(stream);
			} else {
				throw new ConverterWebApplicationException(
						Response.Status.NOT_ACCEPTABLE, "unknown input '"
								+ from + "'");
			}

			if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(to)) {
				final String rspec = AdvertisementConverter.getRSpec(model);
				baos.write(rspec.getBytes());
			} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(to)) {
				final String rspec = ManifestConverter.getRSpec(model);
				baos.write(rspec.getBytes());
			} else if (AbstractConverter.TOSCA.equalsIgnoreCase(to)) {
				final String toplogy = OMN2Tosca.getTopology(model);
				baos.write(toplogy.getBytes());
			} else if (AbstractConverter.TTL.equalsIgnoreCase(to)) {
				RDFDataMgr.write(baos, model, Lang.TTL);
			} else {
				throw new ConverterWebApplicationException(
						Response.Status.NOT_ACCEPTABLE, "unknown output '" + to
								+ "'");
			}
		} catch (RiotException
				| MultipleNamespacesException
				| RequiredResourceNotFoundException
				| MultiplePropertyValuesException e) {
			throw new ConverterWebApplicationException(
					Response.Status.BAD_REQUEST, e);
		} catch (JAXBException | InvalidModelException | IOException e) {
			throw new ConverterWebApplicationException(
					Response.Status.INTERNAL_SERVER_ERROR, e);
		}

		return baos.toString();
	}

	public static class ConverterWebApplicationException extends
			WebApplicationException {
		private static final long serialVersionUID = 7535158521225194221L;

		public ConverterWebApplicationException(final Status status,
				final Throwable cause) {
			super(Response.status(status).entity(cause).build());
		}

		public ConverterWebApplicationException(final Status status,
				final String message) {
			super(Response.status(status).entity(message).build());
		}
	}

}
