package info.openmultinet.ontology.translators.dm;

import info.openmultinet.ontology.Parser;
import info.openmultinet.ontology.exceptions.InvalidModelException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.translators.geni.Advertisement2OMN;
import info.openmultinet.ontology.translators.geni.OMN2Advertisement;
import info.openmultinet.ontology.translators.geni.OMN2Manifest;
import info.openmultinet.ontology.translators.geni.Request2OMN;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.RequiredResourceNotFoundException;
import info.openmultinet.ontology.translators.tosca.OMN2Tosca.ServiceTypeNotFoundException;
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

	private static final Logger LOG = Logger.getLogger(RESTConverter.class.getName());

	@POST
	@Path("{from}/{to}")
	@Produces("text/plain")
	public String convert(@PathParam("from") String from,	@PathParam("to") String to, @FormParam("content") String content) {
		LOG.log(Level.INFO, "Converting from '" + from + "' to '" + to + "'...");
		
		if ((null == content) || (0 == content.length())) {
		  throw new ConverterWebApplicationException(Response.Status.NOT_ACCEPTABLE, "empty input");
		}
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		InputStream stream = new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
		Model model;
		try {
			if (AbstractConverter.RSPEC_REQUEST.equalsIgnoreCase(from)) {
				model = Request2OMN.getModel(stream);
			} else if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(from)) {
				model = Advertisement2OMN.getModel(stream);
			} else if (AbstractConverter.TTL.equalsIgnoreCase(from)) {
				model = new Parser(stream).getModel(); 
			} else if (AbstractConverter.TOSCA.equalsIgnoreCase(from)) {
			  model = Tosca2OMN.getModel(stream);
			} else {
			  throw new ConverterWebApplicationException(Response.Status.NOT_ACCEPTABLE, "unknown input '"+from+"'");
			}

			if (AbstractConverter.RSPEC_ADVERTISEMENT.equalsIgnoreCase(to)) {
				String rspec = OMN2Advertisement.getRSpec(model);
				baos.write(rspec.getBytes());
			} else if (AbstractConverter.RSPEC_MANIFEST.equalsIgnoreCase(to)) {
				String rspec = OMN2Manifest.getRSpec(model);
				baos.write(rspec.getBytes());
			} else if (AbstractConverter.TOSCA.equalsIgnoreCase(to)) {
				String toplogy = OMN2Tosca.getTopology(model);
				baos.write(toplogy.getBytes());
			} else if (AbstractConverter.TTL.equalsIgnoreCase(to)) {
				RDFDataMgr.write(baos, model, Lang.TTL);
			} else {
			  throw new ConverterWebApplicationException(Response.Status.NOT_ACCEPTABLE, "unknown output '"+to+"'");
			}
		} catch (RiotException | ServiceTypeNotFoundException | RequiredResourceNotFoundException e) {
			throw new ConverterWebApplicationException(Response.Status.BAD_REQUEST, e);
		} catch (JAXBException | InvalidModelException | IOException e) {
		  throw new ConverterWebApplicationException(Response.Status.INTERNAL_SERVER_ERROR, e);
    }

		return baos.toString();
	}
	
  public static class ConverterWebApplicationException extends WebApplicationException {
    private static final long serialVersionUID = 7535158521225194221L;
    
    public ConverterWebApplicationException(Status status, Throwable cause) {
      super(Response.status(status).entity(cause).build());
    }
    
    public ConverterWebApplicationException(Status status, String message) {
      super(Response.status(status).entity(message).build());
    }
  }
	  
}
	