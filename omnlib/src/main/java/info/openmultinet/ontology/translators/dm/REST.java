package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.exceptions.DeprecatedRspecVersionException;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.exceptions.MissingRspecElementException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultipleNamespacesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.MultiplePropertyValuesException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.Tosca2OMN.UnsupportedException;

import java.io.IOException;
import java.util.UnknownFormatConversionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.apache.jena.riot.RiotException;

@Path("/")
public class REST extends DeliveryMechanism {

	private static final Logger LOG = Logger.getLogger(REST.class.getName());

	@POST
	@Path("{from}/{to}")
	@Produces("text/plain")
	public String post(@PathParam("from") final String from,
			@PathParam("to") final String to,
			@FormParam("content") final String content) {
		REST.LOG.log(Level.INFO, "Converting from '" + from + "' to '" + to
				+ "'...");

		if ((null == content) || (0 == content.length())) {
			throw new ConverterWebApplicationException(
					Response.Status.NOT_ACCEPTABLE, "empty input");
		}

		String result = "";

		try {
			result = DeliveryMechanism.convert(from, to, content);
		} catch (MissingRspecElementException | InvalidRspecValueException
				| RiotException | MultipleNamespacesException
				| RequiredResourceNotFoundException
				| MultiplePropertyValuesException
				| DeprecatedRspecVersionException | UnsupportedException e) {
			throw new ConverterWebApplicationException(
			// send Client Error 400
			// indicates error within document
					Response.Status.BAD_REQUEST, e);
		} catch (UnknownFormatConversionException e) {
			throw new ConverterWebApplicationException(
			// send Client Error 406
					Response.Status.NOT_ACCEPTABLE, e.getMessage());
		} catch (XMLStreamException | JAXBException | InvalidModelException
				| IOException e) {
			throw new ConverterWebApplicationException(
			// send Server Error 500
					Response.Status.INTERNAL_SERVER_ERROR, e.getMessage());
		}

		return result;
	}

	// for testing purposes
	// http://localhost:8080/omnweb/convert/test
	@GET
	@Path("test")
	public String test() {
		return "OK";
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
