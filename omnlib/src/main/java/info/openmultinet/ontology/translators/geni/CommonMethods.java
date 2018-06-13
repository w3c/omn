package info.openmultinet.ontology.translators.geni;

import info.openmultinet.ontology.exceptions.InvalidRspecValueException;
import info.openmultinet.ontology.translators.AbstractConverter;
import info.openmultinet.ontology.vocabulary.Omn_domain_wireless;
import info.openmultinet.ontology.vocabulary.Omn_lifecycle;
import info.openmultinet.ontology.vocabulary.Osco;

import java.net.URI;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.OWL2;
import com.hp.hpl.jena.vocabulary.RDF;

public class CommonMethods {

	public static String dropLocalName(String url) {

		int index = 0;
		if (url.contains("#")) {
			index = url.lastIndexOf('#');
		} else {
			index = url.lastIndexOf('/');
		}

		String newUrl;
		if (index > 0) {
			newUrl = url.substring(0, index);
		} else {
			newUrl = url;
		}
		return newUrl;
	}

	public static String getLocalName(String url) {
		int index = 0;
		if (url.contains("#")) {
			index = url.lastIndexOf('#');
		} else {
			index = url.lastIndexOf('/');
		}
		return url.substring(index + 1, url.length());
	}

	public static String generateUrnFromUrl(String url, String type) {
		// http://groups.geni.net/geni/wiki/GeniApiIdentifiers
		// urn:publicid:IDN+<authority string>+<type>+<name>
		// type can be interface, link or node

		if (url == null) {
			return "";
		}

		URI uri = URI.create(url);
		if (uri.getScheme() == null) {
			return "";
		}

		if (AbstractConverter.isUrn(url)) {
			return url;
		}

		if (uri.getScheme().equals("http") || uri.getScheme().equals("https")) {

			// AbstractConverter.LOG.info(uri.getScheme() + ": " +
			// uri.toString());

			String urn = "";
			String host = urlToGeniUrn(uri.getHost());
			String path = urlToGeniUrn(uri.getPath());
			String fragment = urlToGeniUrn(uri.getFragment());
			String scheme = urlToGeniUrn(uri.getScheme());

			urn = "urn:publicid:IDN+" + host + "+" + urlToGeniUrn(type) + "+"
					+ scheme + "%3A%2F%2F" + host + path;

			if (fragment != null && !fragment.equals("")) {
				urn += "%23" + fragment;
			}

			return urn;
		} else {

			return url;
		}
	}

	public static String urlToGeniUrn(String dirtyString) {

		// http://groups.geni.net/geni/wiki/GeniApiIdentifiers
		// From Transcribe to
		// leading and trailing whitespace trim
		// whitespace collapse to a single '+'
		// '//' ':'
		// '::' ';'
		// '+' '%2B'
		// ":' '%3A'
		// '/' '%2F'
		// ';' '%3B'
		// ''' '%27'
		// '?' '%3F'
		// '#' '%23'
		// '%' '%25

		if (dirtyString == null) {
			return "";
		}
		String cleanString;
		cleanString = dirtyString.replaceAll(";", "%3B");
		cleanString = cleanString.replaceAll("%", "%25");
		cleanString = cleanString.replaceAll(":", "%3A");
		cleanString = cleanString.replaceAll("\\+", "%2B");
		cleanString = cleanString.replaceAll("//", ":");
		cleanString = cleanString.replaceAll("::", ";");
		cleanString = cleanString.replaceAll("/", "%2F");
		cleanString = cleanString.replaceAll("'", "%27");
		cleanString = cleanString.replaceAll("\\?", "%3F");
		cleanString = cleanString.replaceAll("#", "%23");
		cleanString = cleanString.trim();
		cleanString = cleanString.replaceAll("\\s+", "+");

		return cleanString;
	}

	public static String generateUrlFromUrn(String urn) {

		if (urn == null) {
			return "";
		}

		URI uri = URI.create(urn);
		if (uri == null) {
			return "";
		}

		if (AbstractConverter.isUrl(urn)) {
			return urn;
		}

		if (uri.getScheme().equals("urn")) {

			String url = "";
			String[] parts = urn.split("\\+");

			if (parts.length > 1) {
				if (parts.length > 3) {
					if (AbstractConverter.isUrl(geniUrntoUrl(parts[3]))) {
						String http = geniUrntoUrl(parts[3]);
						url += http;
					} else {
						return urn;
					}
				}
			}
			return url;
		} else {
			return urn;
		}
	}

	public static String geniUrntoUrl(String dirtyString) {

		if (dirtyString == null) {
			return "";
		}
		String cleanString;

		cleanString = dirtyString.replaceAll("\\+", " ");
		cleanString = cleanString.replaceAll("%23", "#");
		cleanString = cleanString.replaceAll("%3F", "?");
		cleanString = cleanString.replaceAll("%27", "'");
		cleanString = cleanString.replaceAll("%2F", "/");
		cleanString = cleanString.replaceAll(";", "::");
		cleanString = cleanString.replaceAll(":", "//");
		cleanString = cleanString.replaceAll("%2B", "+");
		cleanString = cleanString.replaceAll("%3A", ":");
		cleanString = cleanString.replaceAll("%25", "%");
		cleanString = cleanString.replaceAll("%3B", ";");

		return cleanString;
	}

	public static OntClass convertGeniStateToOmn(String geniState) {

		OntClass omnState = Omn_lifecycle.Unknown;
		if (null == geniState)
			return omnState;
		switch (geniState) {
		case "geni_ready_busy":
			omnState = Omn_lifecycle.Active;
			break;
		case "ready_busy":
			omnState = Omn_lifecycle.Active;
			break;
		case "geni_allocated":
			omnState = Omn_lifecycle.Allocated;
			break;
		case "allocated":
			omnState = Omn_lifecycle.Allocated;
			break;
		case "geni_configuring":
			omnState = Omn_lifecycle.Preinit;
			break;
		case "configuring":
			omnState = Omn_lifecycle.Preinit;
			break;
		case "geni_failed":
			omnState = Omn_lifecycle.Error;
			break;
		case "failed":
			omnState = Omn_lifecycle.Error;
			break;
		case "geni_failure":
			omnState = Omn_lifecycle.Failure;
			break;
		case "Nascent":
			omnState = Omn_lifecycle.Nascent;
			break;
		case "geni_instantiating":
			omnState = Omn_lifecycle.NotYetInitialized;
			break;
		case "instantiating":
			omnState = Omn_lifecycle.NotYetInitialized;
			break;
		case "geni_notready":
			omnState = Omn_lifecycle.NotReady;
			break;
		case "geni_pending_allocation":
			omnState = Omn_lifecycle.Pending;
			break;
		case "pending_allocation":
			omnState = Omn_lifecycle.Pending;
			break;
		case "geni_provisioned":
			omnState = Omn_lifecycle.Provisioned;
			break;
		case "provisioned":
			omnState = Omn_lifecycle.Provisioned;
			break;
		case "geni_ready":
			omnState = Omn_lifecycle.Ready;
			break;
		case "ready":
			omnState = Omn_lifecycle.Ready;
			break;
		case "geni_reload":
			omnState = Omn_lifecycle.Reload;
			break;
		case "geni_restart":
			omnState = Omn_lifecycle.Restart;
			break;
		case "geni_start":
			omnState = Omn_lifecycle.Start;
			break;
		case "geni_stop":
			omnState = Omn_lifecycle.Stop;
			break;
		case "geni_stopping":
			omnState = Omn_lifecycle.Stopping;
			break;
		case "stopping":
			omnState = Omn_lifecycle.Stopping;
			break;
		case "geni_success":
			omnState = Omn_lifecycle.Success;
			break;
		case "geni_update_users":
			omnState = Omn_lifecycle.UpdateUsers;
			break;
		case "geni_update_users_cancel":
			omnState = Omn_lifecycle.UpdateUsersCancel;
			break;
		case "geni_updating_users":
			omnState = Omn_lifecycle.UpdatingUsers;
			break;
		case "geni_unallocated":
			omnState = Omn_lifecycle.Unallocated;
			break;
		case "unallocated":
			omnState = Omn_lifecycle.Unallocated;
			break;
		case "Uncompleted":
			omnState = Omn_lifecycle.Uncompleted;
			break;
		}

		return omnState;
	}

	public static String convertOmnToGeniState(Resource start) {

		String geniState = "";

		if (start.equals(Omn_lifecycle.Active)) {
			geniState = "geni_ready_busy";
		} else if (start.equals(Omn_lifecycle.Allocated)) {
			geniState = "geni_allocated";
		} else if (start.equals(Omn_lifecycle.Error)) {
			geniState = "geni_failed";
		} else if (start.equals(Omn_lifecycle.Failure)) {
			geniState = "geni_failure";
		} else if (start.equals(Omn_lifecycle.Nascent)) {
			// comparable GENI state here?
			geniState = "Nascent";
		} else if (start.equals(Omn_lifecycle.NotReady)) {
			geniState = "geni_notready";
		} else if (start.equals(Omn_lifecycle.NotYetInitialized)) {
			geniState = "geni_instantiating";
		} else if (start.equals(Omn_lifecycle.Pending)) {
			geniState = "geni_pending_allocation";
		} else if (start.equals(Omn_lifecycle.Preinit)) {
			geniState = "geni_configuring";
		} else if (start.equals(Omn_lifecycle.Provisioned)) {
			geniState = "geni_provisioned";
		} else if (start.equals(Omn_lifecycle.Ready)) {
			geniState = "geni_ready";
		} else if (start.equals(Omn_lifecycle.Reload)) {
			geniState = "geni_reload";
		} else if (start.equals(Omn_lifecycle.Restart)) {
			geniState = "geni_restart";
		} else if (start.equals(Omn_lifecycle.Start)) {
			geniState = "geni_start";
		} else if (start.equals(Omn_lifecycle.Success)) {
			geniState = "geni_success";
		} else if (start.equals(Omn_lifecycle.Stop)) {
			geniState = "geni_stop";
		} else if (start.equals(Omn_lifecycle.Stopping)) {
			geniState = "geni_stopping";
		} else if (start.equals(Omn_lifecycle.Unallocated)) {
			geniState = "geni_unallocated";
		} else if (start.equals(Omn_lifecycle.Uncompleted)) {
			// comparable GENI state here?
			geniState = "Uncompleted";
		} else if (start.equals(Omn_lifecycle.UpdateUsers)) {
			geniState = "geni_update_users";
		} else if (start.equals(Omn_lifecycle.UpdateUsersCancel)) {
			geniState = "geni_update_users_cancel";
		} else if (start.equals(Omn_lifecycle.UpdatingUsers)) {
			geniState = "geni_updating_users";
		}

		return geniState;
	}

	public static boolean isOmnState(Resource type) {

		boolean geniState = false;

		if (type.equals(Omn_lifecycle.Action)
				|| type.equals(Omn_lifecycle.Active)
				|| type.equals(Omn_lifecycle.Allocated)
				|| type.equals(Omn_lifecycle.Cleaned)
				|| type.equals(Omn_lifecycle.Error)
				|| type.equals(Omn_lifecycle.Failure)
				|| type.equals(Omn_lifecycle.Initialized)
				|| type.equals(Omn_lifecycle.Installed)
				|| type.equals(Omn_lifecycle.Nascent)
				|| type.equals(Omn_lifecycle.NotReady)
				|| type.equals(Omn_lifecycle.NotYetInitialized)
				|| type.equals(Omn_lifecycle.Pending)
				|| type.equals(Omn_lifecycle.Preinit)
				|| type.equals(Omn_lifecycle.Provisioned)
				|| type.equals(Omn_lifecycle.Ready)
				|| type.equals(Omn_lifecycle.Reload)
				|| type.equals(Omn_lifecycle.Removing)
				|| type.equals(Omn_lifecycle.Restart)
				|| type.equals(Omn_lifecycle.Start)
				|| type.equals(Omn_lifecycle.Started)
				|| type.equals(Omn_lifecycle.Stop)
				|| type.equals(Omn_lifecycle.Stopped)
				|| type.equals(Omn_lifecycle.Stopping)
				|| type.equals(Omn_lifecycle.Success)
				|| type.equals(Omn_lifecycle.Uncompleted)
				|| type.equals(Omn_lifecycle.UpdateUsers)
				|| type.equals(Omn_lifecycle.UpdateUsersCancel)
				|| type.equals(Omn_lifecycle.Updating)
				|| type.equals(Omn_lifecycle.UpdatingUsers)
				|| type.equals(Omn_lifecycle.Unallocated)
				|| type.equals(Omn_lifecycle.Wait)) {
			geniState = true;
		}

		return geniState;
	}

	public static String[] splitNumberUnit(String numberAndUnit) {

		String[] parts = numberAndUnit.split("(?<=\\d)(?=\\D)");

		return parts;
	}

	public static boolean hasOscoProperty(Resource resource) {

		boolean hasOscoProperty = false;

		if (resource.hasProperty(Osco.additionals)
				|| resource.hasProperty(Osco.ANNC_AUTO)
				|| resource.hasProperty(Osco.ANNC_DISABLED)
				|| resource.hasProperty(Osco.APP_ID)
				|| resource.hasProperty(Osco.APP_PORT)
				|| resource.hasProperty(Osco.Bit_Bucket_DB_IP)
				|| resource.hasProperty(Osco.COAP_DISABLED)
				|| resource.hasProperty(Osco.CONSOLE_PORT_BIND_ONE)
				|| resource.hasProperty(Osco.CONSOLE_PORT_BIND_TWO)
				|| resource.hasProperty(Osco.CONSOLE_PORT_ONE)
				|| resource.hasProperty(Osco.CONSOLE_PORT_TWO)
				|| resource.hasProperty(Osco.datacenter)
				|| resource.hasProperty(Osco.db_name)
				|| resource.hasProperty(Osco.db_provi)
				|| resource.hasProperty(Osco.db_pw)
				|| resource.hasProperty(Osco.db_user)
				|| resource.hasProperty(Osco.DEFAULT_ROUTE_VIA)
				|| resource.hasProperty(Osco.deployedOn)
				|| resource.hasProperty(Osco.DIAMETER_LISTEN_INTF)
				|| resource.hasProperty(Osco.DIAMETER_PORT)
				|| resource.hasProperty(Osco.DNS_INTF)
				|| resource.hasProperty(Osco.domain_name)
				|| resource.hasProperty(Osco.domain_ns)
				|| resource.hasProperty(Osco.EXT_IP)
				|| resource.hasProperty(Osco.FILE_SERVER)
				|| resource.hasProperty(Osco.fixedIp)
				|| resource.hasProperty(Osco.flavour)
				|| resource.hasProperty(Osco.floatingIp)
				|| resource.hasProperty(Osco.ICSCF_NAME)
				|| resource.hasProperty(Osco.ICSCF_PORT)
				|| resource.hasProperty(Osco.id)
				|| resource.hasProperty(Osco.image)
				|| resource.hasProperty(Osco.key)
				|| resource.hasProperty(Osco.local_port)
				|| resource.hasProperty(Osco.localDB)
				|| resource.hasProperty(Osco.location)
				|| resource.hasProperty(Osco.LOGGING_FILE)
				|| resource.hasProperty(Osco.LOGGING_LEVEL)
				|| resource.hasProperty(Osco.m2m_conn_app_ip)
				|| resource.hasProperty(Osco.m2m_conn_app_port)
				|| resource.hasProperty(Osco.maxNumInst)
				|| resource.hasProperty(Osco.mgmt)
				|| resource.hasProperty(Osco.MGMT_INTF)
				|| resource.hasProperty(Osco.MIN_NUM_INTF)
				|| resource.hasProperty(Osco.minNumInst)
				|| resource.hasProperty(Osco.name)
				|| resource.hasProperty(Osco.nameserver)
				|| resource.hasProperty(Osco.NET_A_INTF)
				|| resource.hasProperty(Osco.NOTIFY_CHAN_DISABLED)
				|| resource.hasProperty(Osco.NOTIFY_DISABLED)
				|| resource.hasProperty(Osco.OMTC_URL)
				|| resource.hasProperty(Osco.parameter1)
				|| resource.hasProperty(Osco.parameter2)
				|| resource.hasProperty(Osco.PCSCF_NAME)
				|| resource.hasProperty(Osco.PCSCF_PORT)
				|| resource.hasProperty(Osco.port)
				|| resource.hasProperty(Osco.PORT)
				|| resource.hasProperty(Osco.REQUIRE_AUTH)
				|| resource.hasProperty(Osco.requires)
				|| resource.hasProperty(Osco.RETARGET_DISABLED)
				|| resource.hasProperty(Osco.SCSCF_NAME)
				|| resource.hasProperty(Osco.SCSCF_PORT)
				|| resource.hasProperty(Osco.SERVICE_PORT)
				|| resource.hasProperty(Osco.SLF_PRESENCE)
				|| resource.hasProperty(Osco.SSL_ENABLED)
				|| resource.hasProperty(Osco.SSL_PORT)
				|| resource.hasProperty(Osco.subnet)
				|| resource.hasProperty(Osco.TEST_PARAM)
				|| resource.hasProperty(Osco.VERSION)) {
			hasOscoProperty = true;
		}

		return hasOscoProperty;
	}

	public static Resource getFrequency(String frequency, Model model)
			throws InvalidRspecValueException {
		Resource frequencyIndividual = null;
		if (frequency.toLowerCase().trim().equals("2.412ghz")) {
			frequencyIndividual = Omn_domain_wireless.GHZ;
		} else if (frequency.toLowerCase().equals("2.417ghz")) {
			frequencyIndividual = Omn_domain_wireless.GHZ_INSTANCE;
		} else if (frequency.matches("[0-9]+.[0-9]+[gG][hH][zZ]")) {
			frequencyIndividual = model.createResource(
							"https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn_wireless.owl#"
									+ frequency);
		} else {
			throw new InvalidRspecValueException("Frequency");
		}
		
		frequencyIndividual.addProperty(RDF.type,
				Omn_domain_wireless.Frequency);
		frequencyIndividual.addProperty(RDF.type, OWL2.NamedIndividual);
		
		return frequencyIndividual;
	}

	public static String getStringFromFrequency(Resource frequencyResource) {
		String frequency = null;

		String frequencyUri = frequencyResource.getURI().toString();
		if (frequencyUri.equals(Omn_domain_wireless.GHZ.getURI().toString())) {
			frequency = "2.412GHZ";
		} else if (frequencyUri.equals(Omn_domain_wireless.GHZ_INSTANCE
				.getURI().toString())) {
			frequency = "2.417GHZ";
		} else if (frequencyUri
				.contains("https://github.com/w3c/omn/blob/master/omnlib/ontologies/omn_wireless.owl#")) {
			String[] parts = frequencyUri.split("#");
			if (parts.length > 1) {
				frequency = parts[1];
			}
		}

		return frequency;
	}

}